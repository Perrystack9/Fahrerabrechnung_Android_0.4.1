package de.fahrerabrechnung.data.card

import androidx.room.withTransaction
import de.fahrerabrechnung.data.db.*
import java.nio.ByteBuffer
import java.security.MessageDigest
import java.time.Clock
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

enum class CardImportRunStatus { IN_PROGRESS, SAVED, PARTIAL, FAILED, INTERRUPTED }
enum class CardImportFileKind { ACTIVITIES, VEHICLES, CONTROLS, EVENTS, FAULTS, SPECIFIC_CONDITIONS, GNSS_POSITIONS, GNSS_AUTHENTICATION, WORK_PERIOD_PLACES, WORK_PERIOD_PLACES_AUTHENTICATION, BORDER_CROSSINGS, LOAD_UNLOAD_OPERATIONS, LOAD_TYPE_ENTRIES, VEHICLE_UNITS_USED, DRIVING_LICENCE_INFO, CURRENT_USAGE, LAST_CARD_DOWNLOAD, ICC_IDENTIFICATION, IC_IDENTIFICATION, CARD_CERTIFICATE, CARD_SIGNING_CERTIFICATE, CA_CERTIFICATE, LINK_CERTIFICATE, APPLICATION_IDENTIFICATION, DRIVER_IDENTIFICATION, APPLICATION_IDENTIFICATION_V2, MF_DIRECTORY, MF_ATR_INFO, MF_EXTENDED_LENGTH, ACTIVITY_ORIGINAL, VEHICLES_ORIGINAL, ACTIVITY_SIGNATURE, VEHICLES_SIGNATURE }

internal fun CardCommunicationKind.importFileKind(): CardImportFileKind = when (this) {
    CardCommunicationKind.ATR_INFO -> CardImportFileKind.MF_ATR_INFO
    CardCommunicationKind.EXTENDED_LENGTH -> CardImportFileKind.MF_EXTENDED_LENGTH
}

internal fun CardIdentitySourceKind.importFileKind(): CardImportFileKind = when (this) {
    CardIdentitySourceKind.APPLICATION -> CardImportFileKind.APPLICATION_IDENTIFICATION
    CardIdentitySourceKind.IDENTIFICATION -> CardImportFileKind.DRIVER_IDENTIFICATION
    CardIdentitySourceKind.APPLICATION_V2 -> CardImportFileKind.APPLICATION_IDENTIFICATION_V2
}

internal fun CardCertificateKind.importFileKind(): CardImportFileKind = when (this) {
    CardCertificateKind.CARD -> CardImportFileKind.CARD_CERTIFICATE
    CardCertificateKind.SIGNING -> CardImportFileKind.CARD_SIGNING_CERTIFICATE
    CardCertificateKind.CA -> CardImportFileKind.CA_CERTIFICATE
    CardCertificateKind.LINK -> CardImportFileKind.LINK_CERTIFICATE
}

internal fun CardMetadataKind.importFileKind(): CardImportFileKind = when (this) {
    CardMetadataKind.DRIVING_LICENCE -> CardImportFileKind.DRIVING_LICENCE_INFO
    CardMetadataKind.CURRENT_USAGE -> CardImportFileKind.CURRENT_USAGE
    CardMetadataKind.LAST_CARD_DOWNLOAD -> CardImportFileKind.LAST_CARD_DOWNLOAD
    CardMetadataKind.ICC_IDENTIFICATION -> CardImportFileKind.ICC_IDENTIFICATION
    CardMetadataKind.IC_IDENTIFICATION -> CardImportFileKind.IC_IDENTIFICATION
}

enum class CardImportFileStatus { READY, STORED, MISSING, READ_FAILED, INVALID, EMPTY, NOT_STORED, NOT_ATTEMPTED }

data class CardImportFileResult(
    val kind: CardImportFileKind, val status: CardImportFileStatus, val decodedCount: Int,
    val firstUtcEpochSeconds: Long?, val lastUtcEpochSeconds: Long?, val failureCode: String?,
)

data class CardImportRun(
    val id: String, val driverCardId: String, val cardNumber: String, val generation: CardGeneration,
    val status: CardImportRunStatus, val startedAtEpochMillis: Long, val finishedAtEpochMillis: Long?,
    val sessionId: String?, val newSourceCount: Int, val failureCode: String?,
    val files: List<CardImportFileResult>,
    val batchId: String? = null,
    val application: CardApplication? = null,
)

/** Supported-source pipeline, not a claim that every driver-card EF is imported. */
@Singleton
class DriverCardImportPipeline @Inject constructor(
    private val database: AppDatabase,
    private val imports: DriverCardImportRepository,
    private val activities: CardActivityImportRepository,
    private val vehicles: CardVehicleImportRepository,
    private val clock: Clock,
) {
    private val dao get() = database.cardImportRunDao()
    private val initialization = Mutex()
    private var initialized = false

    /** Validate all identities first; bind and save all child receipts in one outer transaction. */
    suspend fun processApplications(batch: CardApplicationReadBatch, requestId: String,
                                    confirmedOwnCard: Boolean = false): SavedCardReadBatch {
        require(requestId.isNotBlank() && requestId.length <= 128)
        require(batch.identityAgreement == CardApplicationIdentityAgreement.AGREED)
        val attempts = batch.applications
        val plans = attempts.mapNotNull { attempt -> attempt.result?.let { attempt.application to prepare(it) } }.toMap()
        val primary = if (CardApplication.GEN2 in plans) CardApplication.GEN2 else CardApplication.GEN1
        val card = checkNotNull(plans.getValue(primary).read.card)
        initialize() // Process-scoped recovery must finish outside the rollbackable group.
        return database.withTransaction {
            val batches = database.cardReadBatchDao()
            require(dao.run(requestId) == null) // Never reuse a historic single-application request as a parent.
            require(database.driverProfileDao().sources(requestId) == null)
            val cardId = imports.bind(card, confirmedOwnCard)
            val fingerprint = CardBatchRequests.fingerprint(cardId, batch, plans.mapValues { it.value.fingerprint(cardId) })
            batches.batch(requestId)?.let { existing ->
                require(existing.driverCardId == cardId && existing.requestFingerprint == fingerprint)
                return@withTransaction decodeBatch(existing)
            }
            val now = clock.millis()
            val parent = CardReadBatchEntity(requestId, cardId, fingerprint, now, primary.name)
            batches.insertBatch(parent)
            val savedAttempts = attempts.map { attempt ->
                val read = attempt.result
                val run = read?.let {
                    val id = CardBatchRequests.child(requestId, attempt.application)
                    require(dao.run(id) == null)
                    require(database.driverProfileDao().sources(id) == null)
                    process(it, id).also { child ->
                        check(child.driverCardId == cardId)
                        check(child.failureCode != "STORAGE_FAILED" && child.status !in setOf(CardImportRunStatus.IN_PROGRESS, CardImportRunStatus.INTERRUPTED))
                        check(child.sessionId != null) // Validated 0520 is a persistable own source.
                    }
                }
                CardApplicationAttemptEntity(requestId, attempt.application.name, cardId, attempt.status.name,
                    attempt.failure?.name, read?.generation?.name, run?.id, run?.sessionId)
            }
            batches.insertAttempts(savedAttempts)
            batches.insertMasterFiles(batch.masterFiles.map { capture ->
                val bytes = capture.bytes
                val id = bytes?.let { CardBatchRequests.master(cardId, capture.fileId, it) }
                if (bytes != null) {
                    check(bytes.size <= 128)
                    val row = CardMasterSourceEntity(checkNotNull(id), cardId, capture.fileId, bytes, now)
                    batches.insertMaster(row)
                    val stored = checkNotNull(batches.master(id))
                    check(stored.driverCardId == cardId && stored.fileId == capture.fileId && stored.bytes.contentEquals(bytes))
                }
                CardBatchMasterFileEntity(requestId, capture.fileId, cardId, id, capture.readFailed)
            })
            decodeBatch(parent)
        }
    }

    suspend fun applicationBatch(id: String): SavedCardReadBatch? = database.withTransaction {
        val row = database.cardReadBatchDao().batch(id) ?: return@withTransaction null
        val profile = imports.profile() ?: return@withTransaction null
        if (database.driverProfileDao().card(row.driverCardId)?.profileId != profile.id) return@withTransaction null
        decodeBatch(row)
    }

    private suspend fun decodeBatch(row: CardReadBatchEntity): SavedCardReadBatch {
        val batches = database.cardReadBatchDao()
        val profile = checkNotNull(imports.profile())
        check(database.driverProfileDao().card(row.driverCardId)?.profileId == profile.id)
        val attempts = batches.attempts(row.id)
        check(attempts.map { it.application }.toSet() == CardApplication.entries.map { it.name }.toSet())
        val applications = attempts.map { attempt ->
            check(attempt.driverCardId == row.driverCardId)
            val application = CardApplication.valueOf(attempt.application)
            val generation = attempt.generation?.let(CardGeneration::valueOf)
            generation?.let { check(application.supports(it)) }
            val run = attempt.runId?.let { decode(checkNotNull(dao.run(it))) }
            check((attempt.status == CardApplicationReadStatus.READ.name) == (run != null))
            run?.let { check(it.driverCardId == row.driverCardId && it.generation == generation && it.sessionId == attempt.sessionId) }
            SavedCardApplication(application, CardApplicationReadStatus.valueOf(attempt.status), generation,
                attempt.failureCode?.let(ReaderFailure::valueOf), run)
        }
        val master = batches.masterFiles(row.id).map { ref ->
            check(ref.driverCardId == row.driverCardId && (ref.sourceId == null) == ref.readFailed)
            val original = ref.sourceId?.let { checkNotNull(batches.master(it)) }
            original?.let {
                check(it.driverCardId == row.driverCardId && it.fileId == ref.fileId)
                check(it.id == CardBatchRequests.master(row.driverCardId, ref.fileId, it.bytes))
            }
            SavedCardMasterFile(ref.fileId, original?.bytes, ref.readFailed)
        }
        return SavedCardReadBatch(row.id, row.driverCardId, row.savedAtEpochMillis,
            CardApplication.valueOf(row.primaryApplication), applications, master)
    }

    // Singleton lifetime is the app process. Recover once, before any new process-owned run starts.
    private suspend fun initialize() = initialization.withLock {
        if (!initialized) {
            database.withTransaction {
                dao.unfinished().forEach { finishUnstored(it, CardImportRunStatus.INTERRUPTED, "INTERRUPTED") }
            }
            initialized = true
        }
    }

    suspend fun process(result: CardReadResult, requestId: String): CardImportRun {
        require(requestId.isNotBlank() && requestId.length <= 128)
        // Freeze mutable raw buffers before parsing, fingerprinting or any persistent mutation.
        val plan = prepare(result)
        initialize()
        val start = database.withTransaction {
            // Reuse existing explicit/strong matching rules. Foreign/unconfirmed reads write nothing.
            val cardId = imports.bind(checkNotNull(plan.read.card))
            val fingerprint = plan.fingerprint(cardId)
            dao.run(requestId)?.also {
                require(it.driverCardId == cardId && it.requestFingerprint == fingerprint)
            } ?: CardImportRunEntity(requestId, cardId, fingerprint,
                CardImportRunStatus.IN_PROGRESS.name, clock.millis(), null, null, 0, null).also {
                dao.insertRun(it)
                dao.insertFiles(plan.files.map { file -> file.entity(requestId) })
            }
        }
        if (start.status != CardImportRunStatus.IN_PROGRESS.name) return database.withTransaction { decode(start) }
        return try {
            database.withTransaction {
                val current = checkNotNull(dao.run(requestId))
                if (current.status != CardImportRunStatus.IN_PROGRESS.name) return@withTransaction decode(current)
                val selection = plan.selection
                if (selection == null) {
                    finishUnstored(current, CardImportRunStatus.FAILED, "NO_USABLE_DATA")
                } else {
                    val saved = imports.save(plan.usableRead, requestId, selection)
                    check(saved.driverCardId == current.driverCardId)
                    dao.updateFiles(dao.files(requestId).map {
                        if (it.status == CardImportFileStatus.READY.name) it.copy(status = CardImportFileStatus.STORED.name) else it
                    })
                    dao.updateRun(current.copy(status = if (plan.files.all { it.status == CardImportFileStatus.READY ||
                        (it.status == CardImportFileStatus.EMPTY && when (it.kind) {
                            CardImportFileKind.ACTIVITIES -> plan.read.coreReads[CardCoreEf.ACTIVITY]?.snapshot != null
                            CardImportFileKind.VEHICLES -> plan.read.coreReads[CardCoreEf.VEHICLES]?.snapshot != null
                            else -> false
                        }) })
                        CardImportRunStatus.SAVED.name else CardImportRunStatus.PARTIAL.name,
                        finishedAtEpochMillis = clock.millis(), sessionId = saved.id, newSourceCount = saved.newSourceCount))
                }
                decode(checkNotNull(dao.run(requestId)))
            }
        } catch (cancelled: CancellationException) {
            withContext(NonCancellable) { database.withTransaction {
                dao.run(requestId)?.takeIf { it.status == CardImportRunStatus.IN_PROGRESS.name }?.let {
                    finishUnstored(it, CardImportRunStatus.INTERRUPTED, "INTERRUPTED")
                }
            } }
            throw cancelled
        } catch (_: Exception) {
            // Source/session/normalization writes have rolled back together. The failed attempt remains.
            database.withTransaction {
                val current = checkNotNull(dao.run(requestId))
                if (current.status == CardImportRunStatus.IN_PROGRESS.name)
                    finishUnstored(current, CardImportRunStatus.FAILED, "STORAGE_FAILED")
                decode(checkNotNull(dao.run(requestId)))
            }
        }
    }

    suspend fun history(): List<CardImportRun> {
        initialize()
        return database.withTransaction {
            val profile = imports.profile() ?: return@withTransaction emptyList()
            dao.runs(profile.id).filter { run ->
                val attempt = database.cardReadBatchDao().attemptForRun(run.id)
                attempt == null || database.cardReadBatchDao().batch(attempt.batchId)?.primaryApplication == attempt.application
            }.map { decode(it) }
        }
    }

    suspend fun earlierImports(): List<EarlierDriverImportRow> {
        initialize()
        return database.withTransaction {
            val profile = imports.profile() ?: return@withTransaction emptyList()
            dao.earlierImports(profile.id)
        }
    }

    private suspend fun finishUnstored(run: CardImportRunEntity, status: CardImportRunStatus, failure: String) {
        dao.updateFiles(dao.files(run.id).map {
            if (it.status == CardImportFileStatus.READY.name) it.copy(status = CardImportFileStatus.NOT_STORED.name, failureCode = failure) else it
        })
        dao.updateRun(run.copy(status = status.name, finishedAtEpochMillis = clock.millis(), failureCode = failure))
    }

    private suspend fun decode(run: CardImportRunEntity): CardImportRun {
        val profile = checkNotNull(imports.profile())
        check(checkNotNull(database.driverProfileDao().card(run.driverCardId)).profileId == profile.id)
        val identity = database.driverProfileDao().cards(profile.id).single { it.cardId == run.driverCardId }
        val attempt = database.cardReadBatchDao().attemptForRun(run.id)
        val files = dao.files(run.id)
        val kinds = files.map { it.kind }.toSet()
        // Historic two/three-file receipts retain their original coverage and fingerprint.
        check(kinds.containsAll(setOf(CardImportFileKind.ACTIVITIES.name, CardImportFileKind.VEHICLES.name)) &&
            kinds.all { it in CardImportFileKind.entries.map { kind -> kind.name } } && files.size == kinds.size)
        run.sessionId?.let { check(database.driverProfileDao().sources(it)?.driverCardId == run.driverCardId) }
        return CardImportRun(run.id, run.driverCardId, identity.cardNumber, CardGeneration.valueOf(attempt?.generation ?: identity.generation),
            CardImportRunStatus.valueOf(run.status), run.startedAtEpochMillis, run.finishedAtEpochMillis,
            run.sessionId, run.newSourceCount, run.failureCode, files.map {
                CardImportFileResult(CardImportFileKind.valueOf(it.kind), CardImportFileStatus.valueOf(it.status),
                    it.decodedCount, it.firstUtcEpochSeconds, it.lastUtcEpochSeconds, it.failureCode)
            }, attempt?.batchId, attempt?.application?.let(CardApplication::valueOf))
    }

    private fun prepare(result: CardReadResult): Plan {
        require(result.recognition == CardRecognition.DRIVER && result.card != null && result.card.generation == result.generation)
        val core = result.coreReads.mapValues { (kind, value) ->
            require(kind == value.kind && (value.snapshot == null || value.snapshot.generation == result.generation))
            value.copy(snapshot = value.snapshot?.let { CardCoreSnapshot(kind, it.generation, it.bytes) })
        }.toMap()
        val rawActivity = if (CardCoreEf.ACTIVITY in core) try { core.getValue(CardCoreEf.ACTIVITY).snapshot?.activity() }
            catch (_: ParseError) { null } else result.activity
        val rawVehicles = if (CardCoreEf.VEHICLES in core) try { core.getValue(CardCoreEf.VEHICLES).snapshot?.vehicles() }
            catch (_: ParseError) { null } else result.vehicles
        val read = result.copy(coreReads = core,
            coreSignatures = result.coreSignatures.mapValues { it.value.freeze() }.toMap(),
            activityReadFailed = core[CardCoreEf.ACTIVITY]?.let { it.status == CardCoreReadStatus.READ_FAILED } ?: result.activityReadFailed,
            vehicleReadFailed = core[CardCoreEf.VEHICLES]?.let { it.status == CardCoreReadStatus.READ_FAILED } ?: result.vehicleReadFailed,
            activity = rawActivity?.let { CardActivityFile(it.oldestDayOffset, it.newestDayOffset, it.dailyRecords.copyOf()) },
            vehicles = rawVehicles?.let { CardVehiclesFile(it.newestRecordIndex, it.recordCount, it.generation, it.records.copyOf()) },
            controls = result.controls?.let { CardControlFile(it.generation, it.bytes) },
            events = result.events?.let { CardEventFaultFile(it.kind, it.generation, it.recordsPerType, it.bytes) },
            faults = result.faults?.let { CardEventFaultFile(it.kind, it.generation, it.recordsPerType, it.bytes) },
            specificConditions = result.specificConditions?.let { CardSpecificConditionFile(it.generation, it.recordCount, it.bytes) },
            gnssPositions = result.gnssPositions?.let { CardGnssAccumulatedDrivingFile(it.generation, it.recordCount, it.bytes) },
            gnssAuthentication = result.gnssAuthentication?.let { CardGnssAuthenticationFile(it.generation, it.bytes) },
            places = result.places?.let { CardPlacesFile(it.generation, it.recordCount, it.bytes) },
            placesAuthentication = result.placesAuthentication?.let { CardPlacesAuthenticationFile(it.generation, it.bytes) },
            borderCrossings = result.borderCrossings?.let { CardBorderCrossingsFile(it.generation, it.recordCount, it.bytes) },
            loadUnload = result.loadUnload?.let { CardLoadUnloadFile(it.generation, it.recordCount, it.bytes) },
            loadType = result.loadType?.let { CardLoadTypeFile(it.generation, it.recordCount, it.bytes) },
            vehicleUnits = result.vehicleUnits?.let { CardVehicleUnitsFile(it.generation, it.recordCount, it.bytes) },
            metadata = result.metadata.mapValues { (_, value) -> value.copy(file = value.file?.let { CardMetadataFile(it.kind, it.generation, it.bytes) }) },
            certificates = result.certificates.mapValues { (_, value) -> value.copy(file = value.file?.let { CardCertificateFile(it.kind, it.generation, it.bytes) }) },
            directory = result.directory?.let { CardDirectoryFile(it.generation, it.bytes) },
            identitySources = result.identitySources.mapValues { (_, value) -> value.copy(file = value.file?.let { CardIdentitySourceFile(it.kind, it.generation, it.bytes) }) },
            communication = result.communication.mapValues { (_, value) -> value.copy(file = value.file?.let { CardCommunicationFile(it.kind, it.generation, it.bytes) }) })
        validateCoreSignatures(read)
        // Preserve historical result/fingerprint semantics when no full original was captured.
        val activityRaw = core[CardCoreEf.ACTIVITY]
        val vehicleRaw = core[CardCoreEf.VEHICLES]
        val activity = activityRaw?.takeIf { it.status != CardCoreReadStatus.COMPLETE }?.result()
            ?.copy(kind = CardImportFileKind.ACTIVITIES) ?: inspect(CardImportFileKind.ACTIVITIES,
                read.activity != null || activityRaw?.snapshot != null,
                activityRaw == null && read.activityReadFailed) {
            val file = read.activity ?: throw ParseError(0, "Driver_Activity_Data", "invalid original pointer")
            val days = CardActivityParser.parse(file)
            if (days.isEmpty() && activityRaw?.snapshot != null) CardImportFileResult(CardImportFileKind.ACTIVITIES, CardImportFileStatus.EMPTY,
                0, null, null, "EMPTY")
            else CardImportFileResult(CardImportFileKind.ACTIVITIES, CardImportFileStatus.READY, activities.prepare(read).dayCount,
                days.minOf { it.recordDateUtc.epochSecond }, days.maxOf { it.recordDateUtc.epochSecond }, null)
        }
        val vehicle = vehicleRaw?.takeIf { it.status != CardCoreReadStatus.COMPLETE }?.result()
            ?.copy(kind = CardImportFileKind.VEHICLES) ?: inspect(CardImportFileKind.VEHICLES,
                read.vehicles != null || vehicleRaw?.snapshot != null,
                vehicleRaw == null && read.vehicleReadFailed) {
            val file = read.vehicles ?: throw ParseError(0, "Vehicles_Used", "invalid original pointer")
            val uses = CardVehiclesParser.parse(file)
            if (uses.isEmpty() && vehicleRaw?.snapshot != null && file.newestRecordIndex != 0)
                throw ParseError(0, "vehiclePointerNewestRecord", "unused file has non-default pointer")
            if (uses.isEmpty()) CardImportFileResult(CardImportFileKind.VEHICLES, CardImportFileStatus.EMPTY,
                0, null, null, "EMPTY")
            else CardImportFileResult(CardImportFileKind.VEHICLES, CardImportFileStatus.READY, vehicles.prepare(read).useCount,
                uses.minOf { it.firstUseUtc.epochSecond },
                if (uses.any { it.lastUseUtc == null }) null else uses.maxOf { checkNotNull(it.lastUseUtc).epochSecond }, null)
        }
        // A missing field on an older technical result means not attempted, not a failed control read.
        val control = if (read.controls != null || read.controlReadFailed)
            inspect(CardImportFileKind.CONTROLS, read.controls != null, read.controlReadFailed) {
                val file = checkNotNull(read.controls)
                require(file.generation == read.generation)
                val record = CardControlParser.parse(file)
                // A valid unused EF is still a complete source, with zero recorded controls.
                CardImportFileResult(CardImportFileKind.CONTROLS, CardImportFileStatus.READY,
                    if (record == null) 0 else 1, record?.timeUtc?.epochSecond, record?.timeUtc?.epochSecond, null)
            } else null
        fun eventFault(file: CardEventFaultFile?, failed: Boolean, kind: CardImportFileKind,
                       expected: CardEventFaultKind): CardImportFileResult? = if (file != null || failed)
            inspect(kind, file != null, failed) {
                val complete = checkNotNull(file)
                require(complete.generation == read.generation && complete.kind == expected)
                val records = CardEventFaultParser.parse(complete)
                CardImportFileResult(kind, CardImportFileStatus.READY, records.size,
                    if (records.any { it.beginUtc == null }) null else records.minOfOrNull { checkNotNull(it.beginUtc).epochSecond },
                    if (records.any { it.endUtc == null }) null else records.maxOfOrNull { checkNotNull(it.endUtc).epochSecond }, null)
            } else null
        val conditions = if (read.specificConditions != null || read.specificConditionReadFailed)
            inspect(CardImportFileKind.SPECIFIC_CONDITIONS, read.specificConditions != null, read.specificConditionReadFailed) {
                val file = checkNotNull(read.specificConditions)
                require(file.generation == read.generation)
                val records = CardSpecificConditionParser.parse(file)
                CardImportFileResult(CardImportFileKind.SPECIFIC_CONDITIONS, CardImportFileStatus.READY, records.size,
                    if (records.any { it.timeUtc == null }) null else records.minOfOrNull { checkNotNull(it.timeUtc).epochSecond },
                    if (records.any { it.timeUtc == null }) null else records.maxOfOrNull { checkNotNull(it.timeUtc).epochSecond }, null)
            } else null
        val positions = if (read.gnssPositions != null || read.gnssPositionReadFailed)
            inspect(CardImportFileKind.GNSS_POSITIONS, read.gnssPositions != null, read.gnssPositionReadFailed) {
                val file = checkNotNull(read.gnssPositions)
                require(file.generation == read.generation)
                val records = CardGnssAccumulatedDrivingParser.parse(file)
                CardImportFileResult(CardImportFileKind.GNSS_POSITIONS, CardImportFileStatus.READY, records.size,
                    if (records.any { it.positionTimeUtc == null }) null else records.minOfOrNull { checkNotNull(it.positionTimeUtc).epochSecond },
                    if (records.any { it.positionTimeUtc == null }) null else records.maxOfOrNull { checkNotNull(it.positionTimeUtc).epochSecond }, null)
            } else null
        val authentication = if (read.gnssAuthentication != null || read.gnssAuthenticationReadFailed)
            inspect(CardImportFileKind.GNSS_AUTHENTICATION, read.gnssAuthentication != null, read.gnssAuthenticationReadFailed) {
                val file = checkNotNull(read.gnssAuthentication)
                require(file.generation == read.generation)
                val records = CardGnssAuthenticationParser.parse(file)
                CardImportFileResult(CardImportFileKind.GNSS_AUTHENTICATION, CardImportFileStatus.READY, records.size,
                    if (records.any { it.accumulatedDrivingTimeUtc == null }) null else records.minOfOrNull { checkNotNull(it.accumulatedDrivingTimeUtc).epochSecond },
                    if (records.any { it.accumulatedDrivingTimeUtc == null }) null else records.maxOfOrNull { checkNotNull(it.accumulatedDrivingTimeUtc).epochSecond }, null)
            } else null
        val places = if (read.places != null || read.placeReadFailed)
            inspect(CardImportFileKind.WORK_PERIOD_PLACES, read.places != null, read.placeReadFailed) {
                val file = checkNotNull(read.places)
                require(file.generation == read.generation)
                val records = CardPlacesParser.parse(file)
                CardImportFileResult(CardImportFileKind.WORK_PERIOD_PLACES, CardImportFileStatus.READY, records.size,
                    if (records.any { it.entryTimeUtc == null }) null else records.minOfOrNull { checkNotNull(it.entryTimeUtc).epochSecond },
                    if (records.any { it.entryTimeUtc == null }) null else records.maxOfOrNull { checkNotNull(it.entryTimeUtc).epochSecond }, null)
            } else null
        val placeAuth = if (read.placesAuthentication != null || read.placesAuthenticationReadFailed)
            inspect(CardImportFileKind.WORK_PERIOD_PLACES_AUTHENTICATION, read.placesAuthentication != null, read.placesAuthenticationReadFailed) {
                val file = checkNotNull(read.placesAuthentication)
                require(file.generation == read.generation)
                val records = CardPlacesAuthenticationParser.parse(file)
                CardImportFileResult(CardImportFileKind.WORK_PERIOD_PLACES_AUTHENTICATION, CardImportFileStatus.READY, records.size,
                    if (records.any { it.entryTimeUtc == null }) null else records.minOfOrNull { checkNotNull(it.entryTimeUtc).epochSecond },
                    if (records.any { it.entryTimeUtc == null }) null else records.maxOfOrNull { checkNotNull(it.entryTimeUtc).epochSecond }, null)
            } else null
        val borders = if (read.borderCrossings != null || read.borderCrossingsReadFailed)
            inspect(CardImportFileKind.BORDER_CROSSINGS, read.borderCrossings != null, read.borderCrossingsReadFailed) {
                val file = checkNotNull(read.borderCrossings)
                require(file.generation == read.generation)
                val records = CardBorderCrossingsParser.parse(file)
                CardImportFileResult(CardImportFileKind.BORDER_CROSSINGS, CardImportFileStatus.READY, records.size,
                    if (records.any { it.positionTimeUtc == null }) null else records.minOfOrNull { checkNotNull(it.positionTimeUtc).epochSecond },
                    if (records.any { it.positionTimeUtc == null }) null else records.maxOfOrNull { checkNotNull(it.positionTimeUtc).epochSecond }, null)
            } else null
        val loads = if (read.loadUnload != null || read.loadUnloadReadFailed)
            inspect(CardImportFileKind.LOAD_UNLOAD_OPERATIONS, read.loadUnload != null, read.loadUnloadReadFailed) {
                val file = checkNotNull(read.loadUnload)
                require(file.generation == read.generation)
                val records = CardLoadUnloadParser.parse(file)
                CardImportFileResult(CardImportFileKind.LOAD_UNLOAD_OPERATIONS, CardImportFileStatus.READY, records.size,
                    if (records.any { it.operationTimeUtc == null }) null else records.minOfOrNull { checkNotNull(it.operationTimeUtc).epochSecond },
                    if (records.any { it.operationTimeUtc == null }) null else records.maxOfOrNull { checkNotNull(it.operationTimeUtc).epochSecond }, null)
            } else null
        val types = if (read.loadType != null || read.loadTypeReadFailed)
            inspect(CardImportFileKind.LOAD_TYPE_ENTRIES, read.loadType != null, read.loadTypeReadFailed) {
                val file = checkNotNull(read.loadType)
                require(file.generation == read.generation)
                val records = CardLoadTypeParser.parse(file)
                CardImportFileResult(CardImportFileKind.LOAD_TYPE_ENTRIES, CardImportFileStatus.READY, records.size,
                    if (records.any { it.entryTimeUtc == null }) null else records.minOfOrNull { checkNotNull(it.entryTimeUtc).epochSecond },
                    if (records.any { it.entryTimeUtc == null }) null else records.maxOfOrNull { checkNotNull(it.entryTimeUtc).epochSecond }, null)
            } else null
        val units = if (read.vehicleUnits != null || read.vehicleUnitsReadFailed)
            inspect(CardImportFileKind.VEHICLE_UNITS_USED, read.vehicleUnits != null, read.vehicleUnitsReadFailed) {
                val file = checkNotNull(read.vehicleUnits)
                require(file.generation == read.generation)
                val records = CardVehicleUnitsParser.parse(file)
                CardImportFileResult(CardImportFileKind.VEHICLE_UNITS_USED, CardImportFileStatus.READY, records.size,
                    if (records.any { it.useBeginUtc == null }) null else records.minOfOrNull { checkNotNull(it.useBeginUtc).epochSecond },
                    if (records.any { it.useBeginUtc == null }) null else records.maxOfOrNull { checkNotNull(it.useBeginUtc).epochSecond }, null)
            } else null
        val metadata = CardMetadataKind.entries.mapNotNull { kind -> read.metadata[kind]?.let { value ->
            inspect(kind.importFileKind(), value.file != null, value.readFailed) {
                val file = checkNotNull(value.file)
                require(file.kind == kind && file.generation == read.generation)
                val record = CardMetadataParser.parse(file)
                CardImportFileResult(kind.importFileKind(), CardImportFileStatus.READY, if (record == null) 0 else 1,
                    record?.timeUtc?.epochSecond, record?.timeUtc?.epochSecond, null)
            }
        } }
        val certificates = CardCertificateKind.entries.mapNotNull { kind -> read.certificates[kind]?.let { value ->
            inspect(kind.importFileKind(), value.file != null, value.readFailed) {
                val file = checkNotNull(value.file)
                require(file.kind == kind && file.generation == read.generation && kind.supports(read.generation))
                val record = CardCertificateParser.parse(file)
                // A certificate period is not an activity/source-time range or an import timestamp.
                CardImportFileResult(kind.importFileKind(), CardImportFileStatus.READY, if (record == null) 0 else 1,
                    null, null, null)
            }
        } }
        val identitySources = CardIdentitySourceKind.entries.mapNotNull { kind -> read.identitySources[kind]?.let { value ->
            inspect(kind.importFileKind(), value.file != null, value.readFailed) {
                val file = checkNotNull(value.file)
                require(file.kind == kind && kind.supports(read.generation))
                val record = CardIdentitySourceParser.validate(file, checkNotNull(read.card))
                // Identity validity and capacity fields are not recorded driving/activity coverage.
                CardImportFileResult(kind.importFileKind(), CardImportFileStatus.READY, if (record == null) 0 else 1, null, null, null)
            }
        } }
        val communication = CardCommunicationKind.entries.mapNotNull { kind -> read.communication[kind]?.let { value ->
            inspect(kind.importFileKind(), value.file != null, value.readFailed) {
                val file = checkNotNull(value.file)
                require(file.kind == kind && file.generation == read.generation && kind.supports(read.generation))
                val record = CardCommunicationParser.parse(file)
                CardImportFileResult(kind.importFileKind(), CardImportFileStatus.READY,
                    if (record == null) 0 else 1, null, null, null)
            }
        } }
        val directory = if (read.directory != null || read.directoryReadFailed)
            inspect(CardImportFileKind.MF_DIRECTORY, read.directory != null, read.directoryReadFailed) {
                val file = checkNotNull(read.directory)
                require(file.generation == read.generation)
                val record = CardDirectoryParser.parse(file)
                CardImportFileResult(CardImportFileKind.MF_DIRECTORY, CardImportFileStatus.READY,
                    record?.applications?.size ?: 0, null, null, null)
            } else null
        return Plan(read, listOfNotNull(activity, vehicle, control,
            eventFault(read.events, read.eventReadFailed, CardImportFileKind.EVENTS, CardEventFaultKind.EVENTS),
            eventFault(read.faults, read.faultReadFailed, CardImportFileKind.FAULTS, CardEventFaultKind.FAULTS), conditions, positions, authentication, places, placeAuth, borders, loads, types, units) + metadata + certificates + identitySources + listOfNotNull(directory) + communication + CardCoreEf.entries.mapNotNull { core[it]?.result() } + CardCoreEf.entries.mapNotNull { read.coreSignatures[it]?.result() })
    }

    private inline fun inspect(kind: CardImportFileKind, present: Boolean, failed: Boolean,
                               validate: () -> CardImportFileResult): CardImportFileResult {
        val status = when {
            failed -> CardImportFileStatus.READ_FAILED
            !present -> CardImportFileStatus.MISSING
            else -> try { return validate() } catch (_: ParseError) { CardImportFileStatus.INVALID }
                catch (_: IllegalArgumentException) { CardImportFileStatus.INVALID }
        }
        return CardImportFileResult(kind, status, 0, null, null, status.name)
    }

    private data class Plan(val read: CardReadResult, val files: List<CardImportFileResult>) {
        val selection: CardImportSelection? get() {
            if (read.coreReads.values.any { it.snapshot != null }) return CardImportSelection.ALL_AVAILABLE
            if (files.any { it.kind !in setOf(CardImportFileKind.ACTIVITIES, CardImportFileKind.VEHICLES) && it.status == CardImportFileStatus.READY })
                return CardImportSelection.ALL_AVAILABLE
            val activity = files[0].status == CardImportFileStatus.READY
            val vehicle = files[1].status == CardImportFileStatus.READY
            return when { activity && vehicle -> CardImportSelection.PAIR
                activity -> CardImportSelection.ACTIVITY; vehicle -> CardImportSelection.VEHICLES; else -> null }
        }

        val usableRead: CardReadResult get() = read.copy(
            activity = read.activity.takeIf { files[0].status == CardImportFileStatus.READY },
            vehicles = read.vehicles.takeIf { files[1].status == CardImportFileStatus.READY },
            controls = read.controls.takeIf { files.any { it.kind == CardImportFileKind.CONTROLS && it.status == CardImportFileStatus.READY } },
            events = read.events.takeIf { files.any { it.kind == CardImportFileKind.EVENTS && it.status == CardImportFileStatus.READY } },
            faults = read.faults.takeIf { files.any { it.kind == CardImportFileKind.FAULTS && it.status == CardImportFileStatus.READY } },
            specificConditions = read.specificConditions.takeIf { files.any { it.kind == CardImportFileKind.SPECIFIC_CONDITIONS && it.status == CardImportFileStatus.READY } },
            gnssPositions = read.gnssPositions.takeIf { files.any { it.kind == CardImportFileKind.GNSS_POSITIONS && it.status == CardImportFileStatus.READY } },
            gnssAuthentication = read.gnssAuthentication.takeIf { files.any { it.kind == CardImportFileKind.GNSS_AUTHENTICATION && it.status == CardImportFileStatus.READY } },
            places = read.places.takeIf { files.any { it.kind == CardImportFileKind.WORK_PERIOD_PLACES && it.status == CardImportFileStatus.READY } },
            placesAuthentication = read.placesAuthentication.takeIf { files.any { it.kind == CardImportFileKind.WORK_PERIOD_PLACES_AUTHENTICATION && it.status == CardImportFileStatus.READY } },
            borderCrossings = read.borderCrossings.takeIf { files.any { it.kind == CardImportFileKind.BORDER_CROSSINGS && it.status == CardImportFileStatus.READY } },
            loadUnload = read.loadUnload.takeIf { files.any { it.kind == CardImportFileKind.LOAD_UNLOAD_OPERATIONS && it.status == CardImportFileStatus.READY } },
            loadType = read.loadType.takeIf { files.any { it.kind == CardImportFileKind.LOAD_TYPE_ENTRIES && it.status == CardImportFileStatus.READY } },
            vehicleUnits = read.vehicleUnits.takeIf { files.any { it.kind == CardImportFileKind.VEHICLE_UNITS_USED && it.status == CardImportFileStatus.READY } },
            metadata = read.metadata.filter { (kind, _) -> files.any { it.kind == kind.importFileKind() && it.status == CardImportFileStatus.READY } },
            certificates = read.certificates.filter { (kind, _) -> files.any { it.kind == kind.importFileKind() && it.status == CardImportFileStatus.READY } },
            directory = read.directory.takeIf { files.any { it.kind == CardImportFileKind.MF_DIRECTORY && it.status == CardImportFileStatus.READY } },
            identitySources = read.identitySources.filter { (kind, _) -> files.any { it.kind == kind.importFileKind() && it.status == CardImportFileStatus.READY } },
            communication = read.communication.filter { (kind, _) -> files.any { it.kind == kind.importFileKind() && it.status == CardImportFileStatus.READY } },
        )

        fun fingerprint(cardId: String): String {
            val digest = MessageDigest.getInstance("SHA-256")
            fun text(value: String) { val bytes = value.toByteArray(Charsets.UTF_8)
                digest.update(ByteBuffer.allocate(4).putInt(bytes.size).array()); digest.update(bytes) }
            text("card-import-run-v1"); text(cardId); text(read.generation.name)
            files.forEach { text(it.kind.name); text(it.status.name) }
            read.activity?.let { digest.update(1.toByte()); digest.update(ByteBuffer.allocate(8)
                .putInt(it.oldestDayOffset).putInt(it.newestDayOffset).array());
                digest.update(ByteBuffer.allocate(4).putInt(it.dailyRecords.size).array()); digest.update(it.dailyRecords) }
                ?: digest.update(0.toByte())
            read.vehicles?.let { digest.update(1.toByte()); text(it.generation.name); digest.update(ByteBuffer.allocate(8)
                .putInt(it.newestRecordIndex).putInt(it.recordCount).array());
                digest.update(ByteBuffer.allocate(4).putInt(it.records.size).array()); digest.update(it.records) }
                ?: digest.update(0.toByte())
            // Only new original captures extend a request; old fingerprints are untouched.
            for (kind in CardCoreEf.entries) read.coreReads[kind]?.let { captured ->
                text(kind.storageKind); text(captured.status.name); text(captured.failure?.name.orEmpty())
                captured.snapshot?.let { file ->
                    text(file.generation.name)
                    val bytes = file.bytes
                    digest.update(ByteBuffer.allocate(4).putInt(bytes.size).array()); digest.update(bytes)
                }
            }
            digest.update(if (read.vehicleReadFailed) 1.toByte() else 0.toByte())
            // Preserve the exact legacy fingerprint when no control EF was attempted.
            if (read.controls != null || read.controlReadFailed) {
                read.controls?.let { text(it.generation.name)
                    digest.update(ByteBuffer.allocate(4).putInt(it.bytes.size).array()); digest.update(it.bytes) }
                digest.update(if (read.controlReadFailed) 1.toByte() else 0.toByte())
            }
            // Append only attempted new EFs; existing two/three-file requests stay byte-identical.
            fun extra(kind: CardEventFaultKind, file: CardEventFaultFile?, failed: Boolean) {
                if (file != null || failed) {
                    text(kind.name)
                    file?.let { text(it.kind.name); text(it.generation.name)
                        digest.update(ByteBuffer.allocate(8).putInt(it.recordsPerType).putInt(it.bytes.size).array())
                        digest.update(it.bytes) }
                    digest.update(if (failed) 1.toByte() else 0.toByte())
                }
            }
            extra(CardEventFaultKind.EVENTS, read.events, read.eventReadFailed)
            extra(CardEventFaultKind.FAULTS, read.faults, read.faultReadFailed)
            // No addition to the fingerprint of a historical request that never attempted this EF.
            if (read.specificConditions != null || read.specificConditionReadFailed) {
                text("SPECIFIC_CONDITIONS")
                read.specificConditions?.let { text(it.generation.name)
                    digest.update(ByteBuffer.allocate(8).putInt(it.recordCount).putInt(it.bytes.size).array())
                    digest.update(it.bytes) }
                digest.update(if (read.specificConditionReadFailed) 1.toByte() else 0.toByte())
            }
            if (read.gnssPositions != null || read.gnssPositionReadFailed) {
                text("GNSS_POSITIONS")
                read.gnssPositions?.let { text(it.generation.name)
                    digest.update(ByteBuffer.allocate(8).putInt(it.recordCount).putInt(it.bytes.size).array()); digest.update(it.bytes) }
                digest.update(if (read.gnssPositionReadFailed) 1.toByte() else 0.toByte())
            }
            if (read.gnssAuthentication != null || read.gnssAuthenticationReadFailed) {
                text("GNSS_AUTHENTICATION")
                read.gnssAuthentication?.let { text(it.generation.name)
                    digest.update(ByteBuffer.allocate(4).putInt(it.bytes.size).array()); digest.update(it.bytes) }
                digest.update(if (read.gnssAuthenticationReadFailed) 1.toByte() else 0.toByte())
            }
            // Old requests that did not attempt Places retain their original fingerprint and coverage.
            if (read.places != null || read.placeReadFailed) {
                text("WORK_PERIOD_PLACES")
                read.places?.let { text(it.generation.name)
                    digest.update(ByteBuffer.allocate(8).putInt(it.recordCount).putInt(it.bytes.size).array()); digest.update(it.bytes) }
                digest.update(if (read.placeReadFailed) 1.toByte() else 0.toByte())
            }
            if (read.placesAuthentication != null || read.placesAuthenticationReadFailed) {
                text("WORK_PERIOD_PLACES_AUTHENTICATION")
                read.placesAuthentication?.let { text(it.generation.name)
                    digest.update(ByteBuffer.allocate(4).putInt(it.bytes.size).array()); digest.update(it.bytes) }
                digest.update(if (read.placesAuthenticationReadFailed) 1.toByte() else 0.toByte())
            }
            // Older requests preserve their original coverage and fingerprint.
            if (read.borderCrossings != null || read.borderCrossingsReadFailed) {
                text("BORDER_CROSSINGS")
                read.borderCrossings?.let { text(it.generation.name)
                    digest.update(ByteBuffer.allocate(8).putInt(it.recordCount).putInt(it.bytes.size).array()); digest.update(it.bytes) }
                digest.update(if (read.borderCrossingsReadFailed) 1.toByte() else 0.toByte())
            }
            // No changes to historical requests that never attempted the new source.
            if (read.loadUnload != null || read.loadUnloadReadFailed) {
                text("LOAD_UNLOAD_OPERATIONS")
                read.loadUnload?.let { text(it.generation.name)
                    digest.update(ByteBuffer.allocate(8).putInt(it.recordCount).putInt(it.bytes.size).array()); digest.update(it.bytes) }
                digest.update(if (read.loadUnloadReadFailed) 1.toByte() else 0.toByte())
            }
            // Keep historical requests that never attempted this source unchanged.
            if (read.loadType != null || read.loadTypeReadFailed) {
                text("LOAD_TYPE_ENTRIES")
                read.loadType?.let { text(it.generation.name)
                    digest.update(ByteBuffer.allocate(8).putInt(it.recordCount).putInt(it.bytes.size).array()); digest.update(it.bytes) }
                digest.update(if (read.loadTypeReadFailed) 1.toByte() else 0.toByte())
            }
            // Historical requests which never attempted this source keep their fingerprint.
            if (read.vehicleUnits != null || read.vehicleUnitsReadFailed) {
                text("VEHICLE_UNITS_USED")
                read.vehicleUnits?.let { text(it.generation.name)
                    digest.update(ByteBuffer.allocate(8).putInt(it.recordCount).putInt(it.bytes.size).array()); digest.update(it.bytes) }
                digest.update(if (read.vehicleUnitsReadFailed) 1.toByte() else 0.toByte())
            }
            // An empty map means these EFs were not attempted on the historical request.
            for (kind in CardMetadataKind.entries) read.metadata[kind]?.let { value ->
                text("CARD_METADATA_${kind.name}")
                value.file?.let { text(it.kind.name); text(it.generation.name)
                    digest.update(ByteBuffer.allocate(4).putInt(it.bytes.size).array()); digest.update(it.bytes) }
                digest.update(if (value.readFailed) 1.toByte() else 0.toByte())
            }
            // Historical requests with no certificate attempts retain exactly their former fingerprint.
            for (kind in CardCertificateKind.entries) read.certificates[kind]?.let { value ->
                text("CARD_CERTIFICATE_${kind.name}")
                value.file?.let { text(it.kind.name); text(it.generation.name)
                    digest.update(ByteBuffer.allocate(4).putInt(it.bytes.size).array()); digest.update(it.bytes) }
                digest.update(if (value.readFailed) 1.toByte() else 0.toByte())
            }
            for (kind in CardIdentitySourceKind.entries) read.identitySources[kind]?.let { value ->
                text("CARD_ORIGINAL_IDENTIFICATION_${kind.name}")
                value.file?.let { text(it.kind.name); text(it.generation.name)
                    digest.update(ByteBuffer.allocate(4).putInt(it.bytes.size).array()); digest.update(it.bytes) }
                digest.update(if (value.readFailed) 1.toByte() else 0.toByte())
            }
            // No DIR attempt means the exact pre-DIR fingerprint remains unchanged.
            for (kind in CardCommunicationKind.entries) read.communication[kind]?.let { value ->
                text("CARD_COMMUNICATION_${kind.name}")
                value.file?.let { text(it.kind.name); text(it.generation.name)
                    digest.update(ByteBuffer.allocate(4).putInt(it.bytes.size).array()); digest.update(it.bytes) }
                digest.update(if (value.readFailed) 1.toByte() else 0.toByte())
            }
            if (read.directory != null || read.directoryReadFailed) {
                text("MF_DIRECTORY")
                read.directory?.let { text(it.generation.name)
                    val bytes = it.bytes
                    digest.update(ByteBuffer.allocate(4).putInt(bytes.size).array()); digest.update(bytes) }
                digest.update(if (read.directoryReadFailed) 1.toByte() else 0.toByte())
            }
            // No map means the byte-for-byte pre-signature request remains unchanged.
            for (kind in CardCoreEf.entries) read.coreSignatures[kind]?.let {
                text("CARD_CORE_SIGNATURE_V1_${kind.name}")
                text(CardCoreSignatureStore.requestFingerprint(cardId, it))
            }
            return digest.digest().joinToString("") { "%02x".format(it) }
        }
    }
}

private fun CardImportFileResult.entity(runId: String) = CardImportFileResultEntity(runId, kind.name, status.name,
    decodedCount, firstUtcEpochSeconds, lastUtcEpochSeconds, failureCode)
