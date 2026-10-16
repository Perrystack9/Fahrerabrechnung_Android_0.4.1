package de.fahrerabrechnung.domain.violation.rest

import de.fahrerabrechnung.domain.violation.driving.LegalAssessment
import de.fahrerabrechnung.domain.violation.driving.RuleLaw
import de.fahrerabrechnung.domain.violation.input.*
import java.time.Duration
import java.time.Instant

const val DAILY_REST_INTERPRETATION = "561-m3-daily-rest-1"

enum class RestEligibility { PROVED, CONDITIONAL, REJECTED }
enum class RestRestriction {
    LEFT_DAILY_ANCHOR_MISSING, LEFT_REDUCTION_HISTORY_MISSING, ACTIVITY_GAP, SCOPE_UNDETERMINED, SCOPE_EXCLUDED,
    TRAVEL_REST_PREREQUISITE_UNCONFIRMED, FERRY_PREREQUISITE_MISSING, FERRY_CONTEXT_CONFLICT,
    FERRY_INTERRUPTION_LIMIT_EXCEEDED, FERRY_FACILITY_RULED_OUT, FERRY_SPLIT_ALLOCATION_GUIDANCE_ONLY,
    HISTORICAL_ACTIVATION_UNCONFIRMED, BEFORE_561_TEMPORAL_SCOPE, MIXED_HISTORICAL_VERSION,
    Q07_SUCCESSIVE_WINDOW_INTERPRETATION_OPEN, CREW_PREREQUISITE_UNCONFIRMED,
    L09_CREW_REDUCTION_ACCOUNTING_UNCONFIRMED, BUS_SERVICE_PREREQUISITE_UNCONFIRMED,
    BUS_SERVICE_HISTORY_INCOMPLETE, BUS_REDUCED_25_HOUR_INTERPRETATION_OPEN,
    WEEKLY_ASSIGNMENT_REQUIRES_M4, OPEN_REST_CLASSIFICATION_PENDING,
    ALTERNATIVE_ASSIGNMENT_UNRESOLVED, COMPUTATION_LIMIT_REACHED,
}

/** Actual M1 intervals only; no hypothetical future rest is materialised as a slice. */
data class RestSlice(val range: InputRange, val support: List<IntervalSupport>, val manualTachograph: Boolean) {
    val seconds: Long get() = Duration.between(range.start, range.end).seconds
}
data class RestInterruption(val range: InputRange, val activities: List<ActivityInterval>) {
    val seconds: Long get() = Duration.between(range.start, range.end).seconds
}
enum class WeeklyRestKind { REDUCED, REGULAR }
data class WeeklyRestQualification(val kind: WeeklyRestKind, val eligibility: RestEligibility,
    val law: RuleLaw, val restrictions: List<RestRestriction>, val contextEvidence: List<String>, val needs: List<ContextNeed>)

data class RestCandidate(
    val id: String, val range: InputRange, val rest: List<RestSlice>,
    val interruptions: List<RestInterruption>, val actualEndProved: Boolean,
    val eligibility: RestEligibility, val restrictions: List<RestRestriction>,
    val ferryPoints: List<InputPoint>, val contextEvidence: List<String>, val needs: List<ContextNeed>,
    val weekly: List<WeeklyRestQualification>,
) {
    init {
        require(rest.isNotEmpty() && rest.zipWithNext().all { (a, b) -> a.range.end <= b.range.start })
        require(rest.first().range.start == range.start && rest.last().range.end == range.end)
    }
    val netRestSeconds: Long get() = rest.sumOf { it.seconds }
    val interrupted: Boolean get() = interruptions.isNotEmpty()
    val interruptionSeconds: Long get() = interruptions.sumOf { it.seconds }
    val recordedFerryEnd: Boolean get() = ferryPoints.any {
        it.fields["typeRaw"] == "4" && it.source.generation != "GEN1" && it.timeUtc != null
    }
    fun restSecondsThrough(at: Instant): Long = rest.sumOf {
        if (at <= it.range.start) 0 else Duration.between(it.range.start, minOf(at, it.range.end)).seconds
    }
}

enum class DailyRestKind { REGULAR, REDUCED, SPLIT_REGULAR, CREW_NINE_HOURS }
enum class RestWindowMode(val seconds: Long) { SOLO_24(86400), OCCASIONAL_BUS_25(90000), CREW_30(108000) }
enum class RestAnchorMethod { ACTUAL_REST_END, Q07_LATE_REST_END }
data class RestAnchor(val candidateId: String, val range: InputRange, val method: RestAnchorMethod,
    val timelyInPreviousWindow: Boolean?, val restrictions: List<RestRestriction>) {
    val at: Instant get() = range.end
}

/** This ledger has no calendar-week reset and no compensation/debt operation. */
data class DailyRestReduction(val id: String, val restCandidateId: String, val windowStart: Instant,
    val restInWindowSeconds: Long)
data class DailyReductionCounter(
    val weeklyAnchor: RestAnchor?, val minimumObservedCount: Int, val maximumPossibleCount: Int?,
    /** Recorded events still relevant to at least one unresolved history; size is not the definite count. */
    val reductions: List<DailyRestReduction>, val restrictions: List<RestRestriction>,
) {
    val exact: Boolean get() = maximumPossibleCount == minimumObservedCount && restrictions.isEmpty()
    val provenRemaining: Int? get() = (3 - minimumObservedCount).coerceAtLeast(0).takeIf { exact }
}

data class DailyRestOption(
    val id: String, val kind: DailyRestKind, val candidates: List<String>,
    val netRestSecondsInWindow: Long, val actualEnd: Instant?, val consumesReduction: Boolean,
    val eligibility: RestEligibility, val restrictions: List<RestRestriction>,
    val serviceId: String? = null, val contextEvidence: List<String> = emptyList(),
)
enum class RestWindowState { FULFILLED, OPEN, NORMAL_RULE_DEFICIT, INDETERMINATE }
data class DailyRestWindow(
    val id: String, val law: RuleLaw, val anchor: RestAnchor, val referenceStart: Instant,
    val mode: RestWindowMode, val deadline: Instant, val observedThrough: Instant,
    val counterBefore: DailyReductionCounter, val options: List<DailyRestOption>,
    val state: RestWindowState, val minimumRequiredSeconds: Long, val creditedSeconds: Long,
    val potentialRestCapacitySeconds: Long, val creditedRestCandidates: List<String>, val patternKind: DailyRestKind,
    val potentialDeficitSeconds: Long, val coverage: RuleInputCoverage,
    val restrictions: List<RestRestriction>, val contextEvidence: List<String>,
    /** At observedThrough, without a wall clock or a product warning threshold. */
    val conditionalSecondsUntilLatestRestStart: Long?,
) {
    val provenSecondsUntilLatestRestStart: Long? get() = conditionalSecondsUntilLatestRestStart.takeIf {
        state == RestWindowState.OPEN && restrictions.isEmpty() && coverage.factualPrerequisitesCovered &&
            coverage.excludedScope.isEmpty()
    }
    val establishesLegalAllClear: Boolean get() = false
}

/** A normal-rule arithmetic deficit. Art.12, liability and severity are never adjudicated here. */
data class DailyRestFinding(
    val id: String, val windowId: String, val law: RuleLaw, val assessedRange: InputRange,
    val missingRestSeconds: Long, val requiredRestSeconds: Long, val creditedRestSeconds: Long,
    val mathematicalOnset: Instant?, val firstProvableAt: Instant?, val affectedRange: InputRange?,
    val presentInEveryRetainedPath: Boolean, val quantityIsProved: Boolean,
    val restrictions: List<RestRestriction>, val inputRevision: String,
    val optimisticAdditionalRestSeconds: Long, val quantityIsExact: Boolean,
) {
    init { require(missingRestSeconds > 0) }
    val normalRuleDeficitIsProved: Boolean get() = quantityIsProved && presentInEveryRetainedPath && restrictions.isEmpty()
    val legalAssessment: LegalAssessment get() = LegalAssessment.NOT_DETERMINED
    val establishesArticle12Debt: Boolean get() = false
}

data class DailyRestIssue(val ruleIds: Set<String>, val reason: String, val range: InputRange? = null)
data class DailyRestCalculation(
    val profileId: String, val inputRevision: String, val dataRevision: String, val provenanceRevision: String,
    val drivingRevision: String, val revision: String, val observedThrough: Instant?,
    val candidates: List<RestCandidate>, val anchors: List<RestAnchor>, val windows: List<DailyRestWindow>,
    val findings: List<DailyRestFinding>, val finalCounters: List<DailyReductionCounter>,
    val contextNeeds: List<ContextNeed>, val issues: List<DailyRestIssue>,
    val alternativesComplete: Boolean, val sourceSnapshot: ViolationInputSnapshot,
) {
    val interpretation: String get() = DAILY_REST_INTERPRETATION
    val establishesLegalAllClear: Boolean get() = false
    val isCompleteEngine: Boolean get() = false
}
