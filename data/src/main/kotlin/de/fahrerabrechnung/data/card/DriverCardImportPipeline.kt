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
enum class CardImportFileKind { ACTIVITIES, VEHICLES, CONTROLS, EVENTS, FAULTS, SPECIFIC_CONDITIONS, GNSS_POSITIONS, GNSS_AUTHENTICATION, WORK_PERIOD_PLACES, WORK_PERIOD_PLACES_AUTHENTICATION, BORDER_CROSSINGS, LOAD_UNLOAD_OPERATIONS, LOAD_TYPE_ENTRIES }
enum class CardImportFileStatus { READY, STORED, MISSING, READ_FAILED, INVALID, EMPTY, NOT_STORED }

data class CardImportFileResult(
    val kind: CardImportFileKind, val status: CardImportFileStatus, val decodedCount: Int,
    val firstUtcEpochSeconds: Long?, val lastUtcEpochSeconds: Long?, val failureCode: String?,
)

data class CardImportRun(
    val id: String, val driverCardId: String, val cardNumber: String, val generation: CardGeneration,
    val status: CardImportRunStatus, val startedAtEpochMillis: Long, val finishedAtEpochMillis: Long?,
    val sessionId: String?, val newSourceCount: Int, val failureCode: String?,
    val files: List<CardImportFileResult>,
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
                    dao.updateRun(current.copy(status = if (plan.files.all { it.status == CardImportFileStatus.READY })
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
            dao.runs(profile.id).map { decode(it) }
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
        val files = dao.files(run.id)
        val kinds = files.map { it.kind }.toSet()
        // Historic two/three-file receipts retain their original coverage and fingerprint.
        check(kinds.containsAll(setOf(CardImportFileKind.ACTIVITIES.name, CardImportFileKind.VEHICLES.name)) &&
            kinds.all { it in CardImportFileKind.entries.map { kind -> kind.name } } && files.size == kinds.size)
        run.sessionId?.let { check(database.driverProfileDao().sources(it)?.driverCardId == run.driverCardId) }
        return CardImportRun(run.id, run.driverCardId, identity.cardNumber, CardGeneration.valueOf(identity.generation),
            CardImportRunStatus.valueOf(run.status), run.startedAtEpochMillis, run.finishedAtEpochMillis,
            run.sessionId, run.newSourceCount, run.failureCode, files.map {
                CardImportFileResult(CardImportFileKind.valueOf(it.kind), CardImportFileStatus.valueOf(it.status),
                    it.decodedCount, it.firstUtcEpochSeconds, it.lastUtcEpochSeconds, it.failureCode)
            })
    }

    private fun prepare(result: CardReadResult): Plan {
        require(result.recognition == CardRecognition.DRIVER && result.card != null && result.card.generation == result.generation)
        val read = result.copy(activity = result.activity?.let { CardActivityFile(it.oldestDayOffset, it.newestDayOffset, it.dailyRecords.copyOf()) },
            vehicles = result.vehicles?.let { CardVehiclesFile(it.newestRecordIndex, it.recordCount, it.generation, it.records.copyOf()) },
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
            loadType = result.loadType?.let { CardLoadTypeFile(it.generation, it.recordCount, it.bytes) })
        val activity = inspect(CardImportFileKind.ACTIVITIES, read.activity != null, read.activityReadFailed) {
            val days = CardActivityParser.parse(checkNotNull(read.activity))
            val prepared = activities.prepare(read)
            CardImportFileResult(CardImportFileKind.ACTIVITIES, CardImportFileStatus.READY, prepared.dayCount,
                days.minOf { it.recordDateUtc.epochSecond }, days.maxOf { it.recordDateUtc.epochSecond }, null)
        }
        val vehicle = inspect(CardImportFileKind.VEHICLES, read.vehicles != null, read.vehicleReadFailed) {
            val uses = CardVehiclesParser.parse(checkNotNull(read.vehicles))
            if (uses.isEmpty()) CardImportFileResult(CardImportFileKind.VEHICLES, CardImportFileStatus.EMPTY,
                0, null, null, "EMPTY")
            else CardImportFileResult(CardImportFileKind.VEHICLES, CardImportFileStatus.READY, vehicles.prepare(read).useCount,
                uses.minOf { it.firstUseUtc.epochSecond },
                // An open end remains unknown; do not turn its first-use instant into an end.
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
        return Plan(read, listOfNotNull(activity, vehicle, control,
            eventFault(read.events, read.eventReadFailed, CardImportFileKind.EVENTS, CardEventFaultKind.EVENTS),
            eventFault(read.faults, read.faultReadFailed, CardImportFileKind.FAULTS, CardEventFaultKind.FAULTS), conditions, positions, authentication, places, placeAuth, borders, loads, types))
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
            return digest.digest().joinToString("") { "%02x".format(it) }
        }
    }
}

private fun CardImportFileResult.entity(runId: String) = CardImportFileResultEntity(runId, kind.name, status.name,
    decodedCount, firstUtcEpochSeconds, lastUtcEpochSeconds, failureCode)
