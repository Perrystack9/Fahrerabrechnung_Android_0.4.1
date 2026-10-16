package de.fahrerabrechnung.domain.violation.weekly

import de.fahrerabrechnung.domain.violation.driving.RuleLaw
import de.fahrerabrechnung.domain.violation.input.*
import de.fahrerabrechnung.domain.violation.rest.*
import java.time.*
import java.time.temporal.TemporalAdjusters

/** UTC is the agreed technical projection, not a resolution of the open legal week question L02. */
internal object WeeklyTime {
    const val DAY = 86400L
    const val WEEK = 7 * DAY
    const val REGULAR = 45 * 3600L
    const val REDUCED = 24 * 3600L
    const val SIX_DAYS = 6 * DAY
    const val TWELVE_DAYS = 12 * DAY
    val busStart: LocalDate = LocalDate.of(2010, 6, 4)
    val busDigital: LocalDate = LocalDate.of(2014, 1, 1)
    fun week(at: Instant): Instant = at.atOffset(ZoneOffset.UTC).toLocalDate()
        .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).atStartOfDay().toInstant(ZoneOffset.UTC)
    fun due(assignedWeek: Instant): Instant = assignedWeek.plusSeconds(4 * WEEK)
    fun weeks(range: InputRange): List<Instant> {
        val first = week(range.start)
        val last = week(range.end.minusNanos(1))
        return generateSequence(first) { it.plusSeconds(WEEK) }.takeWhile { it <= last }.toList()
    }
    fun law(id: String, at: Instant): RuleLaw {
        val current = RestLawVersions.phase(InputRange(at, at.plusSeconds(1)), RestLawVersions.ferryWeekly) == RestDatePhase.AFTER
        val bus = RestLawVersions.phase(InputRange(at, at.plusSeconds(1)), RestLawVersions.bus) == RestDatePhase.AFTER
        val article = when (id) {
            "W03" -> "Art.8(9)"
            "W04" -> if (current) "Art.8(6b)" else "Art.8(6)"
            "W05", "W09" -> "Art.8(7), Art.8(6b), Art.8(9)"
            "W07" -> "Art.8(6), Unterabs.3–4"
            "W08" -> "Art.8(6b), Unterabs.2"
            "W10", "W11", "W12", "W13" -> "Art.8(6a)"
            "W14" -> "Art.4(h), Art.8(6)"
            else -> "Art.8(6)"
        }
        return RuleLaw(id, article, when {
            id in setOf("W10", "W11", "W12", "W13") -> if (bus) "561/2006;2024-05-22" else "561/2006;2010-06-04;2014-01-01"
            current -> "561/2006;2020-08-20"
            else -> "561/2006;2007-04-11"
        }, frozen(listOf("Q01-B", "Q01-K-DE") +
            if (id in setOf("W10", "W11", "W12", "W13")) listOf("Q15") + if (bus) listOf("Q03-DE") else emptyList()
            else if (current) listOf("Q02-DE") else emptyList()))
    }
    fun phaseRestrictions(range: InputRange): List<WeeklyRestriction> = when (RestLawVersions.phase(range, RestLawVersions.base)) {
        RestDatePhase.BEFORE -> listOf(WeeklyRestriction.BEFORE_TEMPORAL_SCOPE)
        RestDatePhase.UNCONFIRMED -> listOf(WeeklyRestriction.HISTORICAL_ACTIVATION_UNCONFIRMED)
        RestDatePhase.AFTER -> emptyList()
    }
    fun coverage(snapshot: ViolationInputSnapshot, range: InputRange, id: String): RuleInputCoverage =
        InputCoverage.forRule(snapshot, range, RuleInputRequirement(id,
            setOf(InputDependency.ACTIVITY, InputDependency.SCOPE), ScopeArticle.DAILY_REST_8))
    fun restrictions(coverage: RuleInputCoverage): List<WeeklyRestriction> = buildList {
        if (coverage.gaps.any { it.dependency == InputDependency.ACTIVITY }) add(WeeklyRestriction.ACTIVITY_GAP)
        if (coverage.gaps.any { it.dependency == InputDependency.SCOPE }) add(WeeklyRestriction.SCOPE_UNDETERMINED)
        if (coverage.excludedScope.isNotEmpty()) add(WeeklyRestriction.SCOPE_EXCLUDED)
    }
}
