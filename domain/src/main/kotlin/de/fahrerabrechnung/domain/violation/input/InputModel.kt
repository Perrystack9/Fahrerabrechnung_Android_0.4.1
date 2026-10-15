package de.fahrerabrechnung.domain.violation.input

import de.fahrerabrechnung.domain.model.ActivityType
import java.time.Instant
import java.util.Collections

/** M1 facts only. No driving/rest rule, budget, verdict or display time zone lives here. */
const val INPUT_INTERPRETATION = "m1-card-input-1"
const val MANUAL_TACHOGRAPH_LABEL = "Manueller Nachtrag am EG-Kontrollgerät"

internal fun <T> frozen(values: Collection<T>): List<T> = Collections.unmodifiableList(ArrayList(values))
internal fun <K, V> frozenMap(values: Map<K, V>): Map<K, V> = Collections.unmodifiableMap(LinkedHashMap(values))

data class InputRange(val start: Instant, val end: Instant) {
    init { require(start < end) }
    fun overlaps(other: InputRange) = start < other.end && other.start < end
    fun contains(at: Instant) = start <= at && at < end
}

enum class InputFamily(val ef: Int?) {
    ACTIVITIES(0x0504), VEHICLES(0x0505), SPECIAL_CONDITIONS(0x0522), LOAD_TYPE(0x0530),
    PLACES(0x0506), GNSS(0x0524), BORDERS(0x0528), OTHER(null)
}

enum class FileAvailability { PRESENT, EMPTY, MISSING, READ_FAILED, INVALID, NOT_ATTEMPTED, NOT_SUPPORTED, NOT_STORED, LEGACY_UNKNOWN }
enum class CrewEvidence { SOLO, CREW, NOT_RECORDED, CONFLICT }
enum class SlotEvidence { DRIVER, CO_DRIVER, NOT_RELEVANT, CONFLICT }
enum class InputQuality { KNOWN, UNKNOWN, CONFLICT }
enum class InputIssueCode {
    NO_PROFILE, NO_ACTIVITY_DATA, SOURCE_REJECTED, INCOMPLETE_APPLICATION, MISSING_BATCH_RELATION,
    NON_MIDNIGHT_DATE, REVERSED_OR_DUPLICATE_DAY, INVALID_CHANGE_SEQUENCE, AMBIGUOUS_MINUTE,
    UNKNOWN_AFTER_WITHDRAWAL, MANUAL_DRIVING_ANOMALY, OPEN_END, MISSING_INTERVAL, SOURCE_CONFLICT,
    CREW_CONFLICT, SLOT_CONFLICT, CONTEXT_CONFLICT, SOURCE_UNAVAILABLE
}

/** IDs are the actual existing IDs. Null ancestry is not repaired by guessing a newer batch. */
data class InputSourceKey(
    val profileId: String, val cardId: String, val sessionId: String, val sourceId: String?,
    val generation: String, val application: String?, val batchId: String?, val runId: String?,
) {
    init {
        require(listOf(profileId, cardId, sessionId).none(String::isBlank))
        require(sourceId == null || sourceId.isNotBlank())
        require(generation in setOf("GEN1", "GEN2", "GEN2V2"))
        require(application == null || application == if (generation == "GEN1") "GEN1" else "GEN2")
    }
    val stableKey: String get() = InputRevision.hash("source-key") {
        source(this@InputSourceKey)
    }
}

/** No certificate trust is inferred by M1. Math remains the existing separate 40128 API. */
data class ReceiptEvidence(
    val family: String, val ef: Int, val fingerprint: String, val originalSourceId: String?,
    val certificateSourceId: String?, val captureStatus: String,
    val originalDigest: String?, val signatureDigest: String?, val certificateDigest: String?,
    val issuerSourceId: String? = null, val issuerDigest: String? = null,
) {
    val certificateTrust: String get() = "NOT_EVALUATED"
    val mathematicalStatus: String get() = "NOT_RUN_BY_M1"
}

data class FileEvidence(
    val family: InputFamily, val ef: Int?, val availability: FileAvailability,
    val sourceId: String?, val contentDigest: String?, val completeOriginalId: String? = null,
    val completeOriginalDigest: String? = null,
)

data class InputSource(
    val key: InputSourceKey,
    val files: List<FileEvidence>,
    val receipts: List<ReceiptEvidence>,
    val metadataDigests: Map<String, String> = emptyMap(),
)

data class RecordedChange(
    val minuteOfDay: Int, val activity: ActivityType, val coDriverSlot: Boolean,
    val rawC: Boolean, val cardInserted: Boolean,
) { init { require(activity != ActivityType.UNBEKANNT) } }

data class RecordedDay(
    val recordIndex: Int, val ringOffset: Int, val dateUtc: Instant,
    val fingerprint: String, val presenceCounter: String, val distanceKm: Int,
    val changes: List<RecordedChange>,
)

data class ActivitySource(val source: InputSource, val days: List<RecordedDay>) {
    init { require(source.key.sourceId != null) }
}

/** A point retains physical record position and its actual original; it is not a duration. */
data class InputPoint(
    val source: InputSourceKey, val family: InputFamily, val recordIndex: Int,
    val timeUtc: Instant?, val fields: Map<String, String>,
)

data class ObservationRef(
    val source: InputSourceKey, val recordIndex: Int, val ringOffset: Int,
    val dayFingerprint: String, val changeIndex: Int,
) {
    val stableKey: String get() = InputRevision.hash("observation-ref") {
        source(this@ObservationRef.source); int(recordIndex); int(ringOffset)
        text(dayFingerprint); int(changeIndex)
    }
}

data class ActivityMeaning(
    val activity: ActivityType, val quality: InputQuality,
    val crew: CrewEvidence, val slot: SlotEvidence, val manualTachograph: Boolean,
    val reason: InputIssueCode? = null,
) {
    companion object {
        fun unknown(reason: InputIssueCode) = ActivityMeaning(ActivityType.UNBEKANNT, InputQuality.UNKNOWN,
            CrewEvidence.NOT_RECORDED, SlotEvidence.NOT_RELEVANT, false, reason)
    }
}

/** Supporting begin/end anchors may lie outside a clipped canonical interval. */
enum class EndEvidenceKind { NEXT_CHANGE, NEXT_UTC_DAY, COMPATIBLE_SOURCE, GAP_BRACKETS, OPEN_POINT_CONFLICT }
data class IntervalSupport(val begin: List<ObservationRef>, val end: List<ObservationRef>,
    val kind: EndEvidenceKind = EndEvidenceKind.NEXT_CHANGE)

data class ActivityInterval(
    val range: InputRange, val meaning: ActivityMeaning, val support: List<IntervalSupport>,
) {
    val resolutionSeconds: Int get() = 60
    val factId: String get() = InputRevision.hash("activity-fact") {
        instant(range.start); instant(range.end); meaning(this@ActivityInterval.meaning)
    }
}

data class ActivityObservation(val at: Instant, val meaning: ActivityMeaning, val references: List<ObservationRef>,
    val rawChanges: List<RecordedChange> = emptyList())
data class InputIssue(val code: InputIssueCode, val source: InputSourceKey? = null,
    val range: InputRange? = null, val at: Instant? = null,
    val sessionId: String? = null, val cardId: String? = null,
    val batchId: String? = null, val application: String? = null)

data class InputSources(
    val profileId: String, val ownCardIds: List<String>, val activities: List<ActivitySource>,
    val points: List<InputPoint>, val sources: List<InputSource>, val issues: List<InputIssue>,
    /** Includes failed/missing applications and exact existing parent/child manifests. */
    val manifest: Map<String, String>,
)

data class PreparedActivities(
    val intervals: List<ActivityInterval>, val observations: List<ActivityObservation>,
    val issues: List<InputIssue>,
)

data class ViolationInputSnapshot(
    val profileId: String, val ownCardIds: List<String>, val sources: List<InputSource>,
    val intervals: List<ActivityInterval>, val observations: List<ActivityObservation>,
    val points: List<InputPoint>, val issues: List<InputIssue>, val context: InputContext,
    val scopes: List<ScopeSegment>, val contextNeeds: List<ContextNeed>, val manifest: Map<String, String>,
    val dataRevision: String, val provenanceRevision: String,
    val revision: String,
) {
    val interpretation: String get() = INPUT_INTERPRETATION
    val scopeInterpretation: String get() = ScopeGuard.INTERPRETATION
    val latestObservation: Instant? get() = observations.maxOfOrNull { it.at }
    /** A later 0522/GNSS/etc. point never closes an activity interval. */
    val latestPointObservation: Instant? get() = points.mapNotNull { it.timeUtc }.maxOrNull()
    val latestClosedEnd: Instant? get() = intervals.maxOfOrNull { it.range.end }
    // A closed unknown interval is not proof of an activity, let alone a legal rest period.
    val latestKnownClosedEnd: Instant? get() = intervals.filter { it.meaning.quality == InputQuality.KNOWN }
        .maxOfOrNull { it.range.end }
}
