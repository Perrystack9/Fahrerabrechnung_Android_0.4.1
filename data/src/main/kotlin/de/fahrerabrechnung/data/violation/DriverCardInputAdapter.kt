package de.fahrerabrechnung.data.violation

import androidx.room.withTransaction
import de.fahrerabrechnung.data.card.*
import de.fahrerabrechnung.data.db.*
import de.fahrerabrechnung.domain.model.ActivityType
import de.fahrerabrechnung.domain.violation.input.*
import java.util.Collections
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive

internal data class CapturedInput(val token: String, val sources: InputSources?, val context: InputContext?)

/** One own-profile transaction, all explicitly bound application children, no primary-only query. */
class DriverCardInputAdapter @Inject constructor(
    private val db: AppDatabase,
    private val imports: DriverCardImportRepository,
    private val contexts: ViolationContextRepository,
) {
    internal suspend fun capture(): CapturedInput = db.withTransaction {
        val profile = db.driverProfileDao().localProfile()
            ?: return@withTransaction CapturedInput("no-profile", null, null)
        val manifest = manifestInside(profile)
        val context = contexts.currentInside(profile.id)
        val descriptors = mutableListOf<InputSource>()
        val activities = mutableListOf<ActivitySource>()
        val points = mutableListOf<InputPoint>()
        val issues = mutableListOf<InputIssue>()
        val cards = db.driverProfileDao().cards(profile.id).associateBy { it.cardId }
        for (binding in db.driverProfileDao().imports(profile.id).sortedBy { it.sessionId }) {
            currentCoroutineContext().ensureActive()
            try {
                val identity = checkNotNull(cards[binding.driverCardId])
                val saved = checkNotNull(imports.get(binding.sessionId))
                check(saved.profileId == profile.id && saved.driverCardId == identity.cardId && saved.id == binding.sessionId)
                val attempt = db.cardReadBatchDao().attemptForSession(saved.id)
                val run = db.cardImportRunDao().runs(profile.id).singleOrNull { it.sessionId == saved.id }
                check(run == null || run.driverCardId == saved.driverCardId)
                check(attempt == null || attempt.runId == run?.id)
                val generations = (listOfNotNull(saved.activity?.generation, attempt?.generation?.let(CardGeneration::valueOf),
                    saved.vehicles?.file?.generation, saved.specificConditions?.file?.generation, saved.loadType?.file?.generation,
                    saved.places?.file?.generation, saved.gnssPositions?.file?.generation, saved.borderCrossings?.file?.generation,
                    saved.controls?.file?.generation, saved.events?.file?.generation, saved.faults?.file?.generation,
                    saved.gnssAuthentication?.file?.generation, saved.placesAuthentication?.file?.generation,
                    saved.loadUnload?.file?.generation, saved.vehicleUnits?.file?.generation) +
                    saved.identitySources.values.map { it.file.generation } + saved.coreSnapshots.values.map { it.snapshot.generation } +
                    saved.metadata.values.filter { !it.file.kind.inMasterFile }.map { it.file.generation } +
                    saved.certificates.values.map { it.file.generation } + saved.coreSignatures.values.map { it.generation } +
                    saved.identitySignatures.values.map { it.generation } + saved.eventFaultSignatures.values.map { it.generation }).distinct()
                val generation = checkNotNull(generations.singleOrNull())
                check(attempt == null || (attempt.generation == generation.name && saved.application?.name == attempt.application))
                val key = InputSourceKey(profile.id, identity.cardId, saved.id, binding.activityId,
                    generation.name, saved.application?.name, saved.batchId, run?.id)
                val fileResults = run?.let { db.cardImportRunDao().files(it.id) }.orEmpty().associateBy { it.kind }
                val files = mutableListOf<FileEvidence>()
                val metadata = mutableMapOf<String, String>()
                fun file(family: InputFamily, ef: Int, kind: CardImportFileKind, id: String?, bytes: ByteArray?,
                    count: Int?, supported: Boolean = true, completeId: String? = null, completeBytes: ByteArray? = null) {
                    val recorded = fileResults[kind.name]?.status
                    val availability = when {
                        bytes != null -> if (count == 0) FileAvailability.EMPTY else FileAvailability.PRESENT
                        !supported -> FileAvailability.NOT_SUPPORTED
                        recorded == null -> if (run == null) FileAvailability.LEGACY_UNKNOWN else FileAvailability.NOT_ATTEMPTED
                        else -> when (CardImportFileStatus.valueOf(recorded)) {
                            CardImportFileStatus.EMPTY -> FileAvailability.EMPTY
                            CardImportFileStatus.MISSING -> FileAvailability.MISSING
                            CardImportFileStatus.READ_FAILED -> FileAvailability.READ_FAILED
                            CardImportFileStatus.INVALID -> FileAvailability.INVALID
                            CardImportFileStatus.NOT_ATTEMPTED -> FileAvailability.NOT_ATTEMPTED
                            CardImportFileStatus.NOT_STORED -> FileAvailability.NOT_STORED
                            CardImportFileStatus.READY, CardImportFileStatus.STORED -> FileAvailability.NOT_STORED
                        }
                    }
                    files += FileEvidence(family, ef, availability, id, bytes?.let(InputRevision::digest),
                        completeId, completeBytes?.let(InputRevision::digest))
                }
                val complete = saved.coreSnapshots[CardCoreEf.ACTIVITY]
                val days = saved.activity?.let { activity ->
                    check(activity.generation == generation && activity.cardNumber == identity.cardNumber)
                    check(binding.activityId == CardActivityImportRepository.fingerprint(generation, identity.cardNumber, activity.file))
                    val raw = CardActivityParser.parse(activity.file)
                    check(raw == activity.days) // The existing normalized source must describe precisely this raw snapshot.
                    complete?.let {
                        check(it.driverCardId == identity.cardId && it.snapshot.generation == generation)
                        val original = it.snapshot.activity()
                        check(original.oldestDayOffset == activity.file.oldestDayOffset && original.newestDayOffset == activity.file.newestDayOffset &&
                            original.dailyRecords.contentEquals(activity.file.dailyRecords))
                    }
                    raw
                }.orEmpty()
                file(InputFamily.ACTIVITIES, 0x0504, CardImportFileKind.ACTIVITIES, binding.activityId,
                    saved.activity?.file?.dailyRecords, saved.activity?.dayCount, completeId = complete?.id,
                    completeBytes = complete?.snapshot?.bytes)
                saved.activity?.let { activity ->
                    metadata["0504-pointer-metadata"] = InputRevision.hash("0504-pointers") {
                        int(activity.file.oldestDayOffset); int(activity.file.newestDayOffset)
                    }
                }
                val conditions = saved.specificConditions
                check(conditions == null || conditions.file.generation == generation)
                file(InputFamily.SPECIAL_CONDITIONS, 0x0522, CardImportFileKind.SPECIFIC_CONDITIONS,
                    conditions?.id, conditions?.file?.bytes, conditions?.records?.size)
                val load = saved.loadType
                check(load == null || load.file.generation == generation)
                file(InputFamily.LOAD_TYPE, 0x0530, CardImportFileKind.LOAD_TYPE_ENTRIES,
                    load?.id, load?.file?.bytes, load?.records?.size, generation == CardGeneration.GEN2V2)
                val vehicles = saved.vehicles
                check(vehicles == null || (vehicles.file.generation == generation && vehicles.cardNumber == identity.cardNumber))
                val vehicleOriginal = saved.coreSnapshots[CardCoreEf.VEHICLES]
                file(InputFamily.VEHICLES, 0x0505, CardImportFileKind.VEHICLES, binding.vehicleId,
                    vehicles?.file?.records, vehicles?.uses?.size, completeId = vehicleOriginal?.id,
                    completeBytes = vehicleOriginal?.snapshot?.bytes)
                val places = saved.places
                check(places == null || places.file.generation == generation)
                file(InputFamily.PLACES, 0x0506, CardImportFileKind.WORK_PERIOD_PLACES,
                    places?.id, places?.file?.bytes, places?.records?.size)
                val gnss = saved.gnssPositions
                check(gnss == null || gnss.file.generation == generation)
                file(InputFamily.GNSS, 0x0524, CardImportFileKind.GNSS_POSITIONS,
                    gnss?.id, gnss?.file?.bytes, gnss?.records?.size, generation != CardGeneration.GEN1)
                val borders = saved.borderCrossings
                check(borders == null || borders.file.generation == generation)
                file(InputFamily.BORDERS, 0x0528, CardImportFileKind.BORDER_CROSSINGS,
                    borders?.id, borders?.file?.bytes, borders?.records?.size, generation == CardGeneration.GEN2V2)
                // Every exact metadata/MF source and receipt remains independently addressable.
                for ((kind, value) in saved.metadata) metadata["metadata-${kind.name}-${value.id}"] = InputRevision.digest(value.file.bytes)
                for ((kind, value) in saved.certificates) metadata["certificate-${kind.name}-${value.id}"] = InputRevision.digest(value.file.bytes)
                for ((kind, value) in saved.identitySources) metadata["identity-${kind.name}-${value.id}"] = InputRevision.digest(value.file.bytes)
                for ((kind, value) in saved.communication) metadata["communication-${kind.name}-${value.id}"] = InputRevision.digest(value.file.bytes)
                saved.directory?.let { metadata["directory-${it.id}"] = InputRevision.digest(it.file.bytes) }
                saved.gnssAuthentication?.let { metadata["gnss-auth-${it.id}"] = InputRevision.digest(it.file.bytes) }
                saved.placesAuthentication?.let { metadata["places-auth-${it.id}"] = InputRevision.digest(it.file.bytes) }
                saved.loadUnload?.let { metadata["load-unload-${it.id}"] = InputRevision.digest(it.file.bytes) }
                saved.vehicleUnits?.let { metadata["vehicle-units-${it.id}"] = InputRevision.digest(it.file.bytes) }
                saved.controls?.let { metadata["controls-${it.id}"] = InputRevision.digest(it.file.bytes) }
                saved.events?.let { metadata["events-${it.id}"] = InputRevision.digest(it.file.bytes) }
                saved.faults?.let { metadata["faults-${it.id}"] = InputRevision.digest(it.file.bytes) }
                val receipts = receiptEvidence(saved)
                val descriptor = InputSource(key, immutable(files), immutable(receipts), immutableMap(metadata))
                descriptors += descriptor
                if (saved.activity != null) activities += ActivitySource(descriptor, immutable(days.mapIndexed { index, day ->
                    RecordedDay(index, day.ringOffset, day.recordDateUtc, CardActivityDayFingerprint.id(identity.cardId, day),
                        day.dailyPresenceCounter, day.distanceKm, immutable(day.changes.map { change ->
                            RecordedChange(change.minuteOfDay, when (change.activity) {
                                DriverActivity.REST -> ActivityType.PAUSE_RUHE
                                DriverActivity.AVAILABILITY -> ActivityType.BEREITSCHAFT
                                DriverActivity.WORK -> ActivityType.SONSTIGE_ARBEIT
                                DriverActivity.DRIVING -> ActivityType.LENKZEIT
                            }, change.slot == CardSlot.CO_DRIVER, change.crewDriving, change.cardInserted)
                        }))
                }))
                conditions?.records?.forEach { value -> points += InputPoint(key, InputFamily.SPECIAL_CONDITIONS,
                    value.slotIndex, value.timeUtc, mapOf("typeRaw" to value.type.toString())) }
                load?.records?.forEach { value -> points += InputPoint(key, InputFamily.LOAD_TYPE,
                    value.slotIndex, value.entryTimeUtc, mapOf("entryTimeRaw" to value.entryTimeRaw.toString(), "loadTypeRaw" to value.loadTypeRaw.toString())) }
                vehicles?.uses?.forEach { value -> points += InputPoint(key, InputFamily.VEHICLES, value.ringIndex, value.firstUseUtc,
                    stringFields("lastUseUtc" to value.lastUseUtc?.toString(), "registrationNation" to value.registrationNation,
                        "registrationNumber" to value.registrationNumber, "vin" to value.vin, "odometerBeginKm" to value.odometerBeginKm,
                        "odometerEndKm" to value.odometerEndKm, "vuDataBlockCounter" to value.vuDataBlockCounter)) }
                places?.records?.forEach { value -> points += InputPoint(key, InputFamily.PLACES, value.slotIndex, value.entryTimeUtc,
                    stringFields("entryTimeRaw" to value.entryTimeRaw, "entryTypeRaw" to value.entryTypeRaw, "countryRaw" to value.countryRaw,
                        "regionRaw" to value.regionRaw, "odometerRaw" to value.odometerRaw, "positionTimeRaw" to value.positionTimeRaw,
                        "accuracyRaw" to value.accuracyRaw, "latitudeRaw" to value.latitudeRaw, "longitudeRaw" to value.longitudeRaw)) }
                gnss?.records?.forEach { value -> points += InputPoint(key, InputFamily.GNSS, value.slotIndex, value.positionTimeUtc,
                    stringFields("accumulatedDrivingTimeRaw" to value.accumulatedDrivingTimeRaw, "positionTimeRaw" to value.positionTimeRaw,
                        "accuracyRaw" to value.accuracyRaw, "latitudeRaw" to value.latitudeRaw, "longitudeRaw" to value.longitudeRaw,
                        "odometerRaw" to value.odometerRaw)) }
                borders?.records?.forEach { value -> points += InputPoint(key, InputFamily.BORDERS, value.slotIndex, value.positionTimeUtc,
                    stringFields("countryLeftRaw" to value.countryLeftRaw, "countryEnteredRaw" to value.countryEnteredRaw,
                        "positionTimeRaw" to value.positionTimeRaw, "accuracyRaw" to value.accuracyRaw, "latitudeRaw" to value.latitudeRaw,
                        "longitudeRaw" to value.longitudeRaw, "authenticationStatusRaw" to value.authenticationStatusRaw, "odometerRaw" to value.odometerRaw)) }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: ParseError) {
                issues += InputIssue(InputIssueCode.SOURCE_REJECTED, sessionId = binding.sessionId, cardId = binding.driverCardId)
                manifest["rejected-${binding.sessionId}"] = "INVALID_EXACT_SOURCE"
            } catch (_: IllegalArgumentException) {
                issues += InputIssue(InputIssueCode.SOURCE_REJECTED, sessionId = binding.sessionId, cardId = binding.driverCardId)
                manifest["rejected-${binding.sessionId}"] = "INVALID_EXACT_SOURCE"
            } catch (_: IllegalStateException) {
                issues += InputIssue(InputIssueCode.SOURCE_REJECTED, sessionId = binding.sessionId, cardId = binding.driverCardId)
                manifest["rejected-${binding.sessionId}"] = "INVALID_EXACT_SOURCE"
            }
        }
        if (activities.isEmpty()) issues += InputIssue(InputIssueCode.NO_ACTIVITY_DATA)
        for (batch in db.cardReadBatchDao().batches(profile.id)) {
            for (attempt in db.cardReadBatchDao().attempts(batch.id).filter { it.status == CardApplicationReadStatus.READ_FAILED.name })
                issues += InputIssue(InputIssueCode.INCOMPLETE_APPLICATION, sessionId = attempt.sessionId,
                    cardId = attempt.driverCardId, batchId = batch.id, application = attempt.application)
        }
        val token = tokenInside(profile, context)
        CapturedInput(token, InputSources(profile.id, immutable(cards.keys.sorted()), immutable(activities), immutable(points),
            immutable(descriptors), immutable(issues), immutableMap(manifest)), context)
    }

    /** Caller can publish while this transaction still owns the read boundary. */
    internal suspend fun publishIfCurrent(token: String, publish: () -> Unit): Boolean = db.withTransaction {
        val profile = db.driverProfileDao().localProfile()
        val current = if (profile == null) "no-profile" else tokenInside(profile, contexts.currentInside(profile.id))
        if (current != token) false else { currentCoroutineContext().ensureActive(); publish(); true }
    }

    private suspend fun tokenInside(profile: ProfileEntity, context: InputContext): String {
        val manifest = manifestInside(profile).toSortedMap()
        return InputRevision.hash("input-manifest-token") {
            text(profile.id); text(context.revisionId); int(manifest.size)
            manifest.forEach { (key, value) -> text(key); text(value) }
        }
    }

    private suspend fun manifestInside(profile: ProfileEntity): MutableMap<String, String> {
        val manifest = sortedMapOf<String, String>()
        manifest["profile"] = InputRevision.hash("profile") { text(profile.id); text(profile.name); long(profile.createdAtEpochMillis); long(profile.updatedAtEpochMillis) }
        for (card in db.driverProfileDao().cards(profile.id)) manifest["card-${card.cardId}"] = InputRevision.hash("physical-card") {
            text(card.cardId); text(card.cardKey); text(card.cardNumber); text(card.generation); int(card.issuingState)
            long(card.issuedEpochSeconds); long(card.validFromEpochSeconds); long(card.expiresEpochSeconds)
            text(card.surname); text(card.givenNames); nullable(card.birthDateIso)
        }
        for (source in db.driverProfileDao().imports(profile.id)) {
            manifest["session-${source.sessionId}"] = InputRevision.hash("bound-import") {
                text(source.sessionId); text(source.driverCardId); nullable(source.activityId); nullable(source.vehicleId); nullable(source.pairId)
                int(source.newSourceCount)
            }
            source.activityId?.let { id ->
                val row = db.cardActivityImportDao().getById(id)
                manifest["activity-row-$id"] = if (row == null) "MISSING_EXACT_ROW" else InputRevision.hash("activity-row") {
                    text(row.id); text(row.cardNumber); text(row.generation); int(row.oldestDayOffset); int(row.newestDayOffset)
                    int(row.dayCount); text(InputRevision.digest(row.dailyRecords))
                }
            }
        }
        for (run in db.cardImportRunDao().runs(profile.id)) {
            manifest["run-${run.id}"] = InputRevision.hash("import-run") {
                text(run.id); text(run.driverCardId); text(run.requestFingerprint); text(run.status)
                nullable(run.sessionId); nullable(run.failureCode); int(run.newSourceCount)
            }
            for (file in db.cardImportRunDao().files(run.id)) manifest["run-${run.id}-${file.kind}"] = InputRevision.hash("file-result") {
                text(file.kind); text(file.status); int(file.decodedCount); nullable(file.failureCode)
                nullable(file.firstUtcEpochSeconds?.toString()); nullable(file.lastUtcEpochSeconds?.toString())
            }
        }
        for (batch in db.cardReadBatchDao().batches(profile.id)) {
            manifest["batch-${batch.id}"] = InputRevision.hash("read-batch") {
                text(batch.id); text(batch.driverCardId); text(batch.requestFingerprint); text(batch.primaryApplication)
            }
            for (attempt in db.cardReadBatchDao().attempts(batch.id)) manifest["batch-${batch.id}-${attempt.application}"] = InputRevision.hash("application-attempt") {
                text(attempt.driverCardId); text(attempt.application); text(attempt.status); nullable(attempt.failureCode)
                nullable(attempt.generation); nullable(attempt.runId); nullable(attempt.sessionId)
            }
            for (file in db.cardReadBatchDao().masterFiles(batch.id)) manifest["master-${batch.id}-${file.fileId}"] = InputRevision.hash("master-reference") {
                text(file.driverCardId); int(file.fileId); nullable(file.sourceId); bool(file.readFailed)
            }
        }
        return manifest
    }

    private suspend fun receiptEvidence(saved: SavedDriverImport): List<ReceiptEvidence> {
        val inputs = CardCoreSignatureStore(db.cardCoreSignatureDao(), db.cardMetadataDao()).verificationInputs(saved).map { it.signatureInput() } +
            CardIdentitySignatureStore(db.cardIdentitySignatureDao(), db.cardMetadataDao()).verificationInputs(saved) +
            CardEventFaultSignatureStore(db.cardEventFaultSignatureDao(), db.cardMetadataDao()).verificationInputs(saved)
        return inputs.mapNotNull { input ->
            val source = input.source
            check(source.driverCardId == saved.driverCardId && source.sessionId == saved.id && source.batchId == saved.batchId)
            source.receiptFingerprint?.let { fingerprint -> ReceiptEvidence(source.family.name, source.ef.fileId, fingerprint,
                source.originalSourceId, source.certificateSourceId, checkNotNull(input.receipt).status.name,
                source.originalDigest, source.signatureDigest, source.certificateDigest, source.issuerSourceId, source.issuerDigest) }
        }
    }
}

private fun <T> immutable(values: Collection<T>): List<T> = Collections.unmodifiableList(ArrayList(values))
private fun <K, V> immutableMap(values: Map<K, V>): Map<K, V> = Collections.unmodifiableMap(LinkedHashMap(values))
private fun stringFields(vararg fields: Pair<String, Any?>): Map<String, String> = fields.mapNotNull { (key, value) ->
    value?.let { key to when (it) { is String -> it; is Int -> it.toString(); is Long -> it.toString(); else -> error("Unsupported point field") } }
}.toMap()
