package de.fahrerabrechnung.domain.violation.weekly

import de.fahrerabrechnung.domain.violation.driving.LegalAssessment
import de.fahrerabrechnung.domain.violation.driving.RuleLaw
import de.fahrerabrechnung.domain.violation.input.*
import de.fahrerabrechnung.domain.violation.rest.*
import java.time.Duration
import java.time.Instant

const val WEEKLY_REST_INTERPRETATION = "561-m4-weekly-rest-1"

/** These are proof restrictions, not user-facing infringement statuses. */
enum class WeeklyRestriction {
    L02_UTC_WEEK_PROJECTION_ONLY, L02_NIGHT_TIME_UNCONFIRMED,
    L04_ASSIGNMENT_REVIEW_OUTSTANDING, L04_LONG_REST_PARTITION_UNCONFIRMED,
    LEFT_WEEKLY_HISTORY_MISSING, ACTIVITY_GAP, SCOPE_UNDETERMINED, SCOPE_EXCLUDED,
    HISTORICAL_ACTIVATION_UNCONFIRMED, BEFORE_TEMPORAL_SCOPE,
    REST_PREREQUISITE_UNCONFIRMED, OPEN_REST_END, CONTEXT_PREREQUISITE_UNCONFIRMED,
    CONTEXT_CONFLICT, ALTERNATIVE_ASSIGNMENT_UNRESOLVED, COMPUTATION_LIMIT_REACHED,
    NO_GLOBAL_ASSIGNMENT_WITNESS, BUS_POST_REST_PARTITION_UNCONFIRMED,
    BUS_NIGHT_BREAK_CREDIT_UNCONFIRMED,
}

/** A legal allocation boundary is never presented as an observed activity/end marker. */
data class WeeklyRestUse(
    val id: String, val candidateId: String, val kind: WeeklyRestKind,
    val base: List<RestSlice>, val assignedWeek: Instant?, val extra: Boolean,
    val actualEndProved: Boolean, val restrictions: List<WeeklyRestriction>,
    val busPostAnchorId: String? = null, val basePartIndex: Int = 0,
) {
    init {
        require(base.isNotEmpty()); require(extra == (assignedWeek == null))
        val amount = base.sumOf { it.seconds }
        require(if (kind == WeeklyRestKind.REGULAR) amount >= 162000 else amount in 86400 until 162000)
    }
    val range: InputRange get() = InputRange(base.first().range.start, base.last().range.end)
    val seconds: Long get() = base.sumOf { it.seconds }
}

data class WeeklyCompensationDebt(
    val id: String, val originId: String, val candidateId: String,
    val originRange: InputRange, val assignedWeek: Instant, val seconds: Long,
    /** Exclusive calendar boundary; completion exactly at the boundary is on time. */
    val due: Instant, val law: RuleLaw, val restrictions: List<WeeklyRestriction>,
    val afterBusDerogation: Boolean = false,
) {
    init { require(seconds in 1..75600); require(due > originRange.start) }
}

data class CompensationReservation(
    val debtId: String, val donorCandidateId: String, val range: InputRange,
    val base: InputRange, val due: Instant, val restrictions: List<WeeklyRestriction>,
) {
    val seconds: Long get() = Duration.between(range.start, range.end).seconds
    val onTime: Boolean get() = range.end <= due
    /** A late completed payment does not erase its earlier missed deadline. */
    val completedLateBySeconds: Long get() = Duration.between(due, range.end).seconds.coerceAtLeast(0)
}

enum class WeeklyWindowState { FULFILLED_PROJECTION, OPEN, NORMAL_RULE_DEFICIT, INDETERMINATE }
data class WeeklyCalendarWindow(
    val id: String, val law: RuleLaw, val range: InputRange,
    val requiredCount: Int, val requiredRegularCount: Int,
    val useIds: List<String>, val regularCount: Int, val state: WeeklyWindowState,
    val coverage: RuleInputCoverage, val restrictions: List<WeeklyRestriction>,
) {
    val establishesLegalAllClear: Boolean get() = false
}

data class InternationalWeeklyPair(
    val firstUseId: String, val secondUseId: String, val debtIds: List<String>,
    val nextRegularUseId: String?, val compensationBeforeRegular: Boolean?,
    val eligibility: RestEligibility, val restrictions: List<WeeklyRestriction>,
    val contextEvidence: List<String>, val needs: List<ContextNeed>,
    val locationEvidence: List<InputPoint> = emptyList(),
)

data class BusWeeklyDerogation(
    val anchorCandidateId: String, val serviceId: String?, val range: InputRange,
    val eligibility: RestEligibility, val restrictions: List<WeeklyRestriction>,
    val contextEvidence: List<String>, val needs: List<ContextNeed>,
    val postRestCandidateId: String?, val postRegularCount: Int,
    val postReducedSeconds: Long?, val postRestComplete: Boolean?,
    val nightProjections: List<BusNightProjection>,
)

data class BusNightProjection(val drivingPeriodId: String, val drivingSeconds: Long,
    val excessSeconds: Long, val allDrivingCrewRecorded: Boolean,
    val restrictions: List<WeeklyRestriction>)

/** One correlated ledger. Payments, week roles and debts from different ledgers must not be mixed. */
data class WeeklyAllocation(
    val id: String, val uses: List<WeeklyRestUse>, val debts: List<WeeklyCompensationDebt>,
    val reservations: List<CompensationReservation>, val windows: List<WeeklyCalendarWindow>,
    val internationalPairs: List<InternationalWeeklyPair>, val buses: List<BusWeeklyDerogation>,
    val restrictions: List<WeeklyRestriction>, val dailyCredit: DailyRestAttribution,
    val deadlines: List<WeeklyDeadline>, val findings: List<WeeklyRestFinding>,
) {
    /** Per correlated attribution only. Historical debt/payment evidence is never deleted. */
    val openDebts: List<WeeklyCompensationDebt> get() = frozen(debts.filter { debt -> reservations.none { it.debtId == debt.id } })
    val fulfilledDebts: List<WeeklyCompensationDebt> get() = frozen(debts.filter { debt -> reservations.any { it.debtId == debt.id } })
}

data class WeeklyDeadline(
    val id: String, val anchorCandidateId: String, val anchorActualEnd: Instant,
    val deadline: Instant, val observedThrough: Instant, val nextCandidateId: String?,
    val nextBaseStart: Instant?, val standardSeconds: Long, val lateBySeconds: Long,
    val coverage: RuleInputCoverage, val restrictions: List<WeeklyRestriction>,
) {
    val conditionalSecondsRemaining: Long? get() =
        Duration.between(observedThrough, deadline).seconds.coerceAtLeast(0).takeIf { nextCandidateId == null }
    val provenSecondsRemaining: Long? get() = conditionalSecondsRemaining.takeIf {
        restrictions.isEmpty() && coverage.factualPrerequisitesCovered && coverage.excludedScope.isEmpty()
    }
}

/** Arithmetic only: Art.12, guilt and severity are not adjudicated by M4. */
data class WeeklyRestFinding(
    val id: String, val law: RuleLaw, val range: InputRange,
    val mathematicalOnset: Instant?, val excessOrMissingSeconds: Long,
    val sourceCandidateIds: List<String>, val allocationIds: List<String>,
    val presentInEveryRetainedPath: Boolean, val restrictions: List<WeeklyRestriction>,
) {
    init { require(excessOrMissingSeconds > 0) }
    val normalRuleDeficitIsProved: Boolean get() = presentInEveryRetainedPath && restrictions.isEmpty()
    val legalAssessment: LegalAssessment get() = LegalAssessment.NOT_DETERMINED
    val establishesArticle12Debt: Boolean get() = false
}

data class WeeklyRestIssue(val ruleIds: Set<String>, val reason: String, val range: InputRange? = null)
data class WeeklyRestCalculation(
    val profileId: String, val inputRevision: String, val dataRevision: String,
    val provenanceRevision: String, val drivingRevision: String, val dailyRevision: String,
    val revision: String, val observedThrough: Instant?, val candidates: List<RestCandidate>,
    val allocations: List<WeeklyAllocation>, val deadlines: List<WeeklyDeadline>,
    val findings: List<WeeklyRestFinding>, val contextNeeds: List<ContextNeed>,
    val issues: List<WeeklyRestIssue>, val alternativesComplete: Boolean,
    val dailyRest: DailyRestCalculation, val sourceSnapshot: ViolationInputSnapshot,
) {
    val interpretation: String get() = WEEKLY_REST_INTERPRETATION
    val establishesLegalAllClear: Boolean get() = false
    val isCompleteEngine: Boolean get() = false
}
