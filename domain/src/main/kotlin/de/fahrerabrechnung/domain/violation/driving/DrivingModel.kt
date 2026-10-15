package de.fahrerabrechnung.domain.violation.driving

import de.fahrerabrechnung.domain.violation.input.*
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

const val DRIVING_INTERPRETATION = "561-m2-driving-breaks-1"

/** Research parameters, not a selectable display zone or a declaration that L02 is closed. */
enum class WeekTimeEvidence { UTC_TECHNICAL_PROJECTION_ONLY }
enum class ExtensionWeekEvidence { FIRST_POSITIVE_DRIVING_OVER_NINE_HOURS_PROVISIONAL }
enum class DailyDrivingAnchorKind { RECORDED_REST_AT_LEAST_NINE_HOURS, C2011_3759_SEVEN_HOUR_CONVENTION }
enum class CalculationRestriction {
    L02_WEEK_TIME_NOT_LEGALLY_CONFIRMED, L02_EXTENSION_WEEK_NOT_LEGALLY_CONFIRMED,
    C2011_3759_RECOMMENDED_DRIVING_CONVENTION, LEFT_HISTORY_MISSING, ACTIVITY_OR_SCOPE_GAP,
    M3_FERRY_REST_BOUNDARY_NOT_EVALUATED, HISTORICAL_CHANGE_TIME_NOT_CONFIRMED,
    PASSENGER_SERVICE_UNDETERMINED, M4_BUS_TWELVE_DAY_NIGHT_NOT_EVALUATED,
    TRAVEL_REST_PREREQUISITE_NOT_ESTABLISHED,
}

/** Only the normal limits are calculated. No Art.12 acceptance, liability, severity or debt. */
enum class Article12Information { GENERAL_DEPARTURE_NOT_ASSESSED, POSSIBLE_ONE_HOUR_RETURN, POSSIBLE_TWO_HOUR_RETURN }
enum class LegalAssessment { NOT_DETERMINED }

data class DrivingPolicy(
    val weekTimeEvidence: WeekTimeEvidence = WeekTimeEvidence.UTC_TECHNICAL_PROJECTION_ONLY,
    val extensionWeekEvidence: ExtensionWeekEvidence = ExtensionWeekEvidence.FIRST_POSITIVE_DRIVING_OVER_NINE_HOURS_PROVISIONAL,
) {
    val revision: String get() = InputRevision.hash("m2-policy") {
        text(DRIVING_INTERPRETATION); text(weekTimeEvidence.name); text(extensionWeekEvidence.name)
    }
}

data class RuleLaw(val id: String, val article: String, val version: String, val sourceIds: List<String>)
data class DrivingSlice(val range: InputRange, val support: List<IntervalSupport>) {
    val seconds: Long get() = Duration.between(range.start, range.end).seconds
}

data class DrivingAnchor(val range: InputRange, val kind: DailyDrivingAnchorKind,
    val support: List<IntervalSupport>) {
    // Deliberately NOT a daily-rest verdict or a reset of any Art.8 reduction counter.
    val establishesLegalDailyRest: Boolean get() = false
}

data class NormalLimitFinding(
    val id: String, val law: RuleLaw, val calculationId: String,
    val assessedRange: InputRange, val countedSeconds: Long, val normalLimitSeconds: Long,
    val boundaryReachedAt: Instant, val excessDriving: List<DrivingSlice>,
    val quantityIsExact: Boolean, val restrictions: List<CalculationRestriction>,
    val inputRevision: String,
) {
    init {
        require(countedSeconds > normalLimitSeconds && normalLimitSeconds > 0)
        require(excessDriving.isNotEmpty() && excessDriving.all { it.seconds > 0 })
        require(excessDriving.zipWithNext().all { (a, b) -> a.range.end <= b.range.start })
    }
    val excessSeconds: Long get() = excessDriving.sumOf { it.seconds }
    /** For incomplete history this is the latest proved onset, not an invented exact earlier onset. */
    val firstExcessAt: Instant get() = excessDriving.first().range.start
    val onsetIsExact: Boolean get() = quantityIsExact
    val affectedRange: InputRange get() = InputRange(firstExcessAt, excessDriving.last().range.end)
    val legalAssessment: LegalAssessment get() = LegalAssessment.NOT_DETERMINED
    val establishesArticle12Debt: Boolean get() = false
    val article12Information: Article12Information get() = when {
        !quantityIsExact || firstExcessAt.atOffset(ZoneOffset.MIN).toLocalDate() < LocalDate.of(2020, 8, 20) ->
            Article12Information.GENERAL_DEPARTURE_NOT_ASSESSED
        law.id in setOf("D01", "D02", "D03") && excessSeconds <= 3600 -> Article12Information.POSSIBLE_ONE_HOUR_RETURN
        law.id in setOf("D01", "D02", "D03") && excessSeconds <= 7200 -> Article12Information.POSSIBLE_TWO_HOUR_RETURN
        else -> Article12Information.GENERAL_DEPARTURE_NOT_ASSESSED
    }
}

data class BreakCredit(val range: InputRange, val ruleId: String, val support: List<IntervalSupport>)

data class DrivingProjection(
    val id: String, val law: RuleLaw, val range: InputRange, val observedThrough: Instant,
    val driving: List<DrivingSlice>, val normalLimitSeconds: Long, val quantityIsExact: Boolean,
    val restrictions: List<CalculationRestriction>, val coverage: RuleInputCoverage,
    val anchor: DrivingAnchor? = null, val breakCredits: List<BreakCredit> = emptyList(),
) {
    val countedSeconds: Long get() = driving.sumOf { it.seconds }
    /** Arithmetic under the stated parameters. Never a UI budget without the separate proof. */
    val conditionalHeadroomSeconds: Long get() = maxOf(0, normalLimitSeconds - countedSeconds)
    val provenRemainingSeconds: Long? get() = conditionalHeadroomSeconds.takeIf {
        quantityIsExact && restrictions.isEmpty() && coverage.factualPrerequisitesCovered && coverage.excludedScope.isEmpty()
    }
    val isLegalAllClear: Boolean get() = false
}

/** A calendar-week ledger. There is intentionally no reset(weeklyRest) operation. */
data class WeeklyDrivingExtensions(
    val week: InputRange, val events: List<Instant>, val historyComplete: Boolean,
    val restrictions: List<CalculationRestriction>,
) {
    val observedOccurrences: Int get() = events.size
    val conditionallyAvailable: Int? get() = (2 - observedOccurrences).coerceAtLeast(0).takeIf { historyComplete }
    val provenAvailable: Int? get() = conditionallyAvailable.takeIf { restrictions.isEmpty() }
}

data class DrivingCheckIssue(val ruleIds: Set<String>, val reason: String, val range: InputRange? = null)

data class DrivingCalculation(
    val profileId: String, val inputRevision: String, val dataRevision: String, val provenanceRevision: String,
    val revision: String, val policyRevision: String, val observedThrough: Instant?,
    val projections: List<DrivingProjection>, val findings: List<NormalLimitFinding>,
    val extensions: List<WeeklyDrivingExtensions>, val issues: List<DrivingCheckIssue>,
    /** Full original/input/receipt evidence stays reachable, including rejected-source limitations. */
    val sourceSnapshot: ViolationInputSnapshot,
    /** At observedThrough only. No wall-clock countdown; null means the budget is not proved. */
    val provenCurrentBreakHeadroomSeconds: Long? = null,
) {
    val interpretation: String get() = DRIVING_INTERPRETATION
    val isCompleteEngine: Boolean get() = false
    val establishesLegalAllClear: Boolean get() = false
}
