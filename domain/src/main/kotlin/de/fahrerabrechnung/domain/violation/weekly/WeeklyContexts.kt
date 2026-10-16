package de.fahrerabrechnung.domain.violation.weekly

import de.fahrerabrechnung.domain.violation.driving.*
import de.fahrerabrechnung.domain.violation.input.*
import de.fahrerabrechnung.domain.violation.rest.*
import java.time.*

internal data class WeeklyPrerequisite(val eligibility: RestEligibility,
    val restrictions: List<WeeklyRestriction>, val evidence: List<String>, val needs: List<ContextNeed>,
    val locationEvidence: List<InputPoint>)

internal object WeeklyContexts {
    fun international(snapshot: ViolationInputSnapshot, first: WeeklyRestUse, second: WeeklyRestUse): WeeklyPrerequisite {
        val range = InputRange(first.range.start, second.range.end)
        val restrictions = linkedSetOf<WeeklyRestriction>()
        val evidence = linkedSetOf<String>()
        val needs = mutableListOf<ContextNeed>()
        val locations = mutableListOf<InputPoint>()
        var rejected = false
        when (RestLawVersions.phase(range, RestLawVersions.ferryWeekly)) {
            RestDatePhase.BEFORE -> rejected = true
            RestDatePhase.UNCONFIRMED -> restrictions += WeeklyRestriction.HISTORICAL_ACTIVATION_UNCONFIRMED
            RestDatePhase.AFTER -> Unit
        }
        val kind = snapshot.context.resolve(ContextKey.TRANSPORT_KIND, range)
        evidence += kind.evidence.map { it.semanticId }
        if (kind.text == "PASSENGERS") rejected = true
        else if (kind.text != "GOODS") {
            restrictions += WeeklyRestriction.CONTEXT_PREREQUISITE_UNCONFIRMED
            needs += ContextNeed(ContextKey.TRANSPORT_KIND, null, range, "CONCRETE_INTERNATIONAL_WEEKLY_PAIR")
        }
        if (RestContexts.transportConflicts(snapshot, range).isNotEmpty()) restrictions += WeeklyRestriction.CONTEXT_CONFLICT
        for (use in listOf(first, second)) {
            val at = use.range.start
            // Physical start, not an unstable candidate id containing a still-growing end.
            val subject = ContextSubject(ContextSubjectKind.REST, "rest-${at.epochSecond}")
            val values = listOf(ContextKey.EMPLOYER_COUNTRY, ContextKey.RESIDENCE_COUNTRY, ContextKey.REST_START_COUNTRY)
                .associateWith { key -> snapshot.context.resolveAt(key, at, if (key == ContextKey.REST_START_COUNTRY) subject else null) }
            val points = RecordedRestCountry.points(snapshot, at)
            locations += points; evidence += points.map(RecordedRestCountry::evidence)
            val recorded = RecordedRestCountry.countries(points)
            val declared = values.getValue(ContextKey.REST_START_COUNTRY).evidence.mapNotNull {
                RecordedRestCountry.canonical((it.value as? ContextValue.Text)?.value) }.toSet()
            val countries = (recorded + declared).toSet()
            val countryConflict = countries.size > 1 || values.getValue(ContextKey.REST_START_COUNTRY).conflict
            if (countryConflict) {
                restrictions += WeeklyRestriction.CONTEXT_CONFLICT
                needs += ContextNeed(ContextKey.REST_START_COUNTRY, subject, null, "RECORDED_REST_START_COUNTRY_CONFLICT", at)
            }
            values.forEach { (key, value) ->
                evidence += value.evidence.map { it.semanticId }
                if (value.conflict) restrictions += WeeklyRestriction.CONTEXT_CONFLICT
                if (value.text == null && (key != ContextKey.REST_START_COUNTRY || recorded.isEmpty() && !countryConflict)) {
                    restrictions += WeeklyRestriction.CONTEXT_PREREQUISITE_UNCONFIRMED
                    needs += ContextNeed(key, if (key == ContextKey.REST_START_COUNTRY) subject else null, null,
                        "CONCRETE_INTERNATIONAL_WEEKLY_START", at)
                }
            }
            val country = countries.singleOrNull()
            if (country != null && (country == RecordedRestCountry.canonical(values.getValue(ContextKey.EMPLOYER_COUNTRY).text) ||
                    country == RecordedRestCountry.canonical(values.getValue(ContextKey.RESIDENCE_COUNTRY).text))) rejected = true
        }
        return WeeklyPrerequisite(if (rejected) RestEligibility.REJECTED else if (restrictions.isEmpty())
            RestEligibility.PROVED else RestEligibility.CONDITIONAL, frozen(restrictions), frozen(evidence),
            frozen(needs.distinctBy { it.id }), frozen(locations.distinct()))
    }

    fun bus(snapshot: ViolationInputSnapshot, driving: DrivingCalculation, anchor: RestCandidate,
        until: Instant, next: RestCandidate?, check: () -> Unit): List<BusWeeklyDerogation> {
        val start = anchor.range.end
        if (until <= start.plusSeconds(WeeklyTime.SIX_DAYS) ||
            anchor.effectiveWeekly.none { it.kind == WeeklyRestKind.REGULAR && it.eligibility != RestEligibility.REJECTED }) return emptyList()
        val range = InputRange(start, until)
        val scope = WeeklyTime.coverage(snapshot, range, "W10–W13")
        if (scope.excludedScope.isNotEmpty() || scope.gaps.any { it.dependency == InputDependency.SCOPE }) return emptyList()
        if (snapshot.context.resolve(ContextKey.TRANSPORT_KIND, range).text != "PASSENGERS" ||
            snapshot.context.resolve(ContextKey.PASSENGER_SERVICE, range).text != "OCCASIONAL" ||
            RestLawVersions.phase(range, WeeklyTime.busStart) == RestDatePhase.BEFORE) return emptyList()
        val entries = snapshot.context.assertions.filter { it.key == ContextKey.SINGLE_OCCASIONAL_SERVICE &&
            it.from < until && (it.until == null || it.until > start) }.groupBy { it.subject }
        val subjects: List<ContextSubject?> = if (entries.isEmpty()) listOf(null) else entries.keys.toList()
        return frozen(subjects.mapNotNull { subject ->
            check()
            val restrictions = linkedSetOf<WeeklyRestriction>()
            val evidence = linkedSetOf<String>()
            val needs = mutableListOf<ContextNeed>()
            var rejected = false
            if (RestLawVersions.phase(range, WeeklyTime.busStart) != RestDatePhase.AFTER)
                restrictions += WeeklyRestriction.HISTORICAL_ACTIVATION_UNCONFIRMED
            val single = snapshot.context.resolve(ContextKey.SINGLE_OCCASIONAL_SERVICE, range, subject)
            evidence += single.evidence.map { it.semanticId }
            if (single.flag == false) return@mapNotNull null
            if (single.flag != true || subject == null || entries.size > 1) {
                restrictions += WeeklyRestriction.CONTEXT_PREREQUISITE_UNCONFIRMED
                needs += ContextNeed(ContextKey.SINGLE_OCCASIONAL_SERVICE, subject, range, "CONCRETE_TWELVE_DAY_SERVICE")
            }
            fun flag(key: ContextKey) {
                val value = snapshot.context.resolve(key, range, subject)
                evidence += value.evidence.map { it.semanticId }
                if (value.flag == false) rejected = true
                else if (value.flag != true) {
                    restrictions += WeeklyRestriction.CONTEXT_PREREQUISITE_UNCONFIRMED
                    needs += ContextNeed(key, subject, range, "CONCRETE_TWELVE_DAY_PREREQUISITE")
                }
                if (value.conflict) restrictions += WeeklyRestriction.CONTEXT_CONFLICT
            }
            when (RestLawVersions.phase(range, RestLawVersions.bus)) {
                RestDatePhase.BEFORE -> {
                    val international = snapshot.context.resolve(ContextKey.JOURNEY_KIND, range, subject)
                    evidence += international.evidence.map { it.semanticId }
                    if (international.text == "DOMESTIC") rejected = true
                    else if (international.text != "INTERNATIONAL") {
                        restrictions += WeeklyRestriction.CONTEXT_PREREQUISITE_UNCONFIRMED
                        needs += ContextNeed(ContextKey.JOURNEY_KIND, subject, range, "HISTORICAL_INTERNATIONAL_BUS_SERVICE")
                    }
                    flag(ContextKey.OCCASIONAL_FOREIGN_STAY_24H)
                }
                RestDatePhase.UNCONFIRMED -> restrictions += WeeklyRestriction.HISTORICAL_ACTIVATION_UNCONFIRMED
                RestDatePhase.AFTER -> Unit
            }
            val night = mutableListOf<BusNightProjection>()
            when (RestLawVersions.phase(range, WeeklyTime.busDigital)) {
                RestDatePhase.UNCONFIRMED -> restrictions += WeeklyRestriction.HISTORICAL_ACTIVATION_UNCONFIRMED
                RestDatePhase.BEFORE -> Unit
                RestDatePhase.AFTER -> {
                    flag(ContextKey.BUS_DIGITAL_TACHOGRAPH_ANNEX_IB)
                    for (period in driving.projections.filter { it.law.id == "B01" && it.range.overlaps(range) }) {
                        check()
                        val slices = period.driving.filter { it.range.overlaps(range) }
                        if (slices.isEmpty()) continue
                        // The threshold concerns the whole Art.7 period, not just its night minutes.
                        val allCrew = period.driving.all { part -> snapshot.intervals.filter { it.range.overlaps(part.range) }.let { records ->
                            records.isNotEmpty() && records.all { it.meaning.quality == InputQuality.KNOWN && it.meaning.crew == CrewEvidence.CREW } } }
                        val uncertainBreak = !period.quantityIsExact || period.breakCredits.any {
                            it.conditionalCrewSplit || it.historicalTimeUnconfirmed } || period.restrictions.any {
                            it in setOf(CalculationRestriction.ACTIVITY_OR_SCOPE_GAP, CalculationRestriction.L09_PARTIAL_CREW_AVAILABILITY_SPLIT_NOT_CONFIRMED) }
                        val limits = linkedSetOf<WeeklyRestriction>()
                        if (uncertainBreak) limits += WeeklyRestriction.BUS_NIGHT_BREAK_CREDIT_UNCONFIRMED
                        val over = (period.countedSeconds - 10800).coerceAtLeast(0)
                        // With >3h solo driving the UTC night is only a projection. No display zone certifies L02.
                        if (over > 0 && !allCrew) limits += WeeklyRestriction.L02_NIGHT_TIME_UNCONFIRMED
                        val projectedNight = slices.any { nightUtc(it.range) }
                        night += BusNightProjection(period.id, period.countedSeconds,
                            if (projectedNight && !allCrew) over else 0, allCrew, frozen(limits))
                        restrictions += limits
                    }
                }
            }
            restrictions += WeeklyTime.restrictions(WeeklyTime.coverage(snapshot, range, "W10–W13"))
            if (RestContexts.transportConflicts(snapshot, range).isNotEmpty()) restrictions += WeeklyRestriction.CONTEXT_CONFLICT
            if (rejected) return@mapNotNull null
            val net = next?.netRestSeconds
            val postRegular = if (net != null && net >= 90 * 3600L) 2 else if (net != null && net >= 45 * 3600L) 1 else 0
            val postReduced = net?.takeIf { it in 69 * 3600L until 90 * 3600L }?.minus(45 * 3600L)
            val postComplete = net?.let { it >= 69 * 3600L && next.actualEndProved }
            val common = restrictions.toList()
            BusWeeklyDerogation(anchor.id, subject?.id, range,
                if (common.isEmpty()) RestEligibility.PROVED else RestEligibility.CONDITIONAL, frozen(common),
                frozen(evidence), frozen(needs.distinctBy { it.id }), next?.id, postRegular, postReduced,
                postComplete, frozen(night))
        })
    }

    private fun nightUtc(range: InputRange): Boolean {
        var date = range.start.atOffset(ZoneOffset.UTC).toLocalDate().minusDays(1)
        val last = range.end.atOffset(ZoneOffset.UTC).toLocalDate()
        while (date <= last) {
            val from = date.atTime(22, 0).toInstant(ZoneOffset.UTC)
            if (InputRange(from, from.plusSeconds(8 * 3600)).overlaps(range)) return true
            date = date.plusDays(1)
        }
        return false
    }
}
