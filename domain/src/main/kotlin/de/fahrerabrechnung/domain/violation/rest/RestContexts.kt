package de.fahrerabrechnung.domain.violation.rest

import de.fahrerabrechnung.domain.model.ActivityType
import de.fahrerabrechnung.domain.violation.driving.DrivingCalculation
import de.fahrerabrechnung.domain.violation.input.*
import java.time.Duration
import java.time.Instant

internal data class RestModeChoice(val mode: RestWindowMode, val eligibility: RestEligibility,
    val restrictions: List<RestRestriction> = emptyList(), val serviceId: String? = null,
    val contextEvidence: List<String> = emptyList(), val needs: List<ContextNeed> = emptyList())

/** Reads the same contextual assertions and M2 driving slices. No routine crew confirmation. */
internal object RestContexts {
    /** A recorded point is a concrete obstacle, never an extrapolated transport interval. */
    fun transportConflicts(snapshot: ViolationInputSnapshot, range: InputRange): List<InputPoint> =
        snapshot.points.filter { point ->
            val at = point.timeUtc
            point.family == InputFamily.LOAD_TYPE && at != null && at >= range.start && at < range.end &&
                when (snapshot.context.resolveAt(ContextKey.TRANSPORT_KIND, at).text) {
                    "GOODS" -> point.fields["loadTypeRaw"] == "2"
                    "PASSENGERS" -> point.fields["loadTypeRaw"] == "1"
                    else -> false
                }
        }

    fun modes(snapshot: ViolationInputSnapshot, driving: DrivingCalculation, start: Instant, observed: Instant,
        candidates: List<RestCandidate>, serviceUses: Map<String, Int>, historyFrom: Instant): List<RestModeChoice> {
        val restStart = candidates.filter { it.range.start >= start && it.eligibility != RestEligibility.REJECTED &&
            it.netRestSeconds >= if (it.interrupted) 39600 else 32400 }.minByOrNull { it.range.end }?.range?.start
        val crewEnd = minOf(restStart ?: observed, start.plusSeconds(RestWindowMode.CREW_30.seconds), observed)
        val crew = crew(snapshot, start, crewEnd)
        if (crew == RestEligibility.PROVED) return listOf(RestModeChoice(RestWindowMode.CREW_30, crew))
        val crewLimits = if (crew == RestEligibility.CONDITIONAL) listOf(RestRestriction.CREW_PREREQUISITE_UNCONFIRMED) else emptyList()
        val choices = mutableListOf(RestModeChoice(RestWindowMode.SOLO_24,
            if (crewLimits.isEmpty()) RestEligibility.PROVED else RestEligibility.CONDITIONAL, crewLimits))
        if (crew == RestEligibility.CONDITIONAL) choices += RestModeChoice(RestWindowMode.CREW_30, crew, crewLimits)
        choices += buses(snapshot, driving, start, observed, serviceUses, historyFrom).map {
            it.copy(restrictions = frozen((it.restrictions + crewLimits).distinct()),
                eligibility = if (crewLimits.isEmpty()) it.eligibility else RestEligibility.CONDITIONAL)
        }
        return frozen(choices)
    }

    private fun crew(snapshot: ViolationInputSnapshot, start: Instant, end: Instant): RestEligibility {
        if (start >= end) return RestEligibility.REJECTED
        val range = InputRange(start, end)
        val intervals = snapshot.intervals.filter { it.range.overlaps(range) }
        val hasCrew = intervals.any { it.meaning.quality == InputQuality.KNOWN &&
            it.meaning.crew == CrewEvidence.CREW && it.meaning.activity != ActivityType.PAUSE_RUHE }
        // Art.4(o), TRACE II example3: preparatory other work does not start the optional driving hour.
        val first = intervals.firstOrNull { it.meaning.quality == InputQuality.KNOWN &&
            it.meaning.activity == ActivityType.LENKZEIT }?.range?.start?.let { maxOf(it, start) }
        val optionalEnd = first?.plusSeconds(3600)
        val knownSoloLater = optionalEnd != null && intervals.any { it.range.end > optionalEnd &&
            it.meaning.quality == InputQuality.KNOWN && it.meaning.activity == ActivityType.LENKZEIT &&
            it.meaning.crew == CrewEvidence.SOLO }
        if (knownSoloLater) return RestEligibility.REJECTED
        // Contrary non-driving markers after the beginning of driving are not an invented crew fact.
        val contraryLater = optionalEnd != null && intervals.any { it.range.end > optionalEnd &&
            it.meaning.quality == InputQuality.KNOWN && it.meaning.activity != ActivityType.PAUSE_RUHE &&
            it.meaning.crew == CrewEvidence.SOLO }
        val coverage = InputCoverage.forRule(snapshot, range, RuleInputRequirement("R05", setOf(InputDependency.ACTIVITY)))
        val unrecorded = intervals.any { it.meaning.quality != InputQuality.KNOWN ||
            (it.meaning.activity != ActivityType.PAUSE_RUHE && (it.meaning.crew !in setOf(CrewEvidence.SOLO, CrewEvidence.CREW) ||
                it.meaning.slot in setOf(SlotEvidence.NOT_RELEVANT, SlotEvidence.CONFLICT))) }
        if (!coverage.factualPrerequisitesCovered || unrecorded || contraryLater) return RestEligibility.CONDITIONAL
        return if (hasCrew) RestEligibility.PROVED else RestEligibility.REJECTED
    }

    private fun buses(snapshot: ViolationInputSnapshot, driving: DrivingCalculation, start: Instant, observed: Instant,
        uses: Map<String, Int>, historyFrom: Instant): List<RestModeChoice> {
        val end = minOf(observed, start.plusSeconds(90000))
        if (start >= end) return emptyList()
        val range = InputRange(start, end)
        if (snapshot.context.resolve(ContextKey.TRANSPORT_KIND, range).text != "PASSENGERS" ||
            snapshot.context.resolve(ContextKey.PASSENGER_SERVICE, range).text != "OCCASIONAL") return emptyList()
        val phase = RestLawVersions.phase(InputRange(start, start.plusSeconds(90000)), RestLawVersions.bus)
        if (phase == RestDatePhase.BEFORE) return emptyList()
        // The non-overlapping D03 slices reuse the existing M2 driving/scope interpretation.
        val seconds = driving.projections.filter { it.law.id == "D03" }.flatMap { it.driving }
            .distinctBy { it.range }.sumOf { part ->
                if (!part.range.overlaps(range)) 0 else Duration.between(maxOf(start, part.range.start), minOf(end, part.range.end)).seconds
            }
        if (seconds > 25200) return emptyList()
        val coverage = InputCoverage.forRule(snapshot, range,
            RuleInputRequirement("R06", setOf(InputDependency.ACTIVITY, InputDependency.SCOPE), ScopeArticle.DRIVING_6))
        val serviceEntries = snapshot.context.assertions.filter { it.key == ContextKey.SINGLE_OCCASIONAL_SERVICE &&
            it.from <= start && (it.until == null || it.until > start) }.groupBy { it.subject }
        val common = linkedSetOf<RestRestriction>()
        if (serviceEntries.size > 1) common += RestRestriction.BUS_SERVICE_PREREQUISITE_UNCONFIRMED
        if (phase != RestDatePhase.AFTER) common += RestRestriction.HISTORICAL_ACTIVATION_UNCONFIRMED
        if (!coverage.factualPrerequisitesCovered || coverage.excludedScope.isNotEmpty()) common += RestRestriction.ACTIVITY_GAP
        if (serviceEntries.isEmpty()) return listOf(RestModeChoice(RestWindowMode.OCCASIONAL_BUS_25, RestEligibility.CONDITIONAL,
            frozen(common + RestRestriction.BUS_SERVICE_PREREQUISITE_UNCONFIRMED),
            needs = listOf(ContextNeed(ContextKey.SINGLE_OCCASIONAL_SERVICE, null, range, "CONCRETE_25_HOUR_SERVICE"))))
        val result = mutableListOf<RestModeChoice>()
        for ((subject, entries) in serviceEntries) {
            val restrictions = common.toMutableSet()
            val evidence = entries.map { it.semanticId }.toMutableSet()
            val needs = mutableListOf<ContextNeed>()
            val declared = snapshot.context.resolve(ContextKey.SINGLE_OCCASIONAL_SERVICE, range, subject)
            if (declared.flag == false) continue
            val periods = entries.map { it.from to it.until }.distinct()
            val period = periods.singleOrNull()
            val length = period?.second?.let { Duration.between(period.first, it).seconds }
            if (length != null && length < 6 * 86400) continue
            val maximum = if (length != null && length >= 8 * 86400) 2 else 1
            if ((uses[subject.id] ?: 0) >= maximum) continue
            if (declared.flag != true || length == null || periods.size != 1 || period.second?.let { it < end } == true) {
                restrictions += RestRestriction.BUS_SERVICE_PREREQUISITE_UNCONFIRMED
                needs += ContextNeed(ContextKey.SINGLE_OCCASIONAL_SERVICE, subject, range, "CONCRETE_SERVICE_DURATION_OR_BOUNDARY")
            }
            if (historyFrom > (period?.first ?: start)) restrictions += RestRestriction.BUS_SERVICE_HISTORY_INCOMPLETE
            for (key in listOf(ContextKey.ROAD_SAFETY_PRESERVED, ContextKey.WORKING_CONDITIONS_PRESERVED,
                ContextKey.APPLICABLE_MAX_WORKING_TIME_RESPECTED)) {
                val value = snapshot.context.resolve(key, range, subject)
                evidence += value.evidence.map { it.semanticId }
                if (value.flag == false) {
                    // This declared prerequisite positively rules the exception out.
                    restrictions.clear(); restrictions += RestRestriction.BUS_SERVICE_PREREQUISITE_UNCONFIRMED
                    needs.clear(); break
                }
                if (value.flag != true) {
                    restrictions += RestRestriction.BUS_SERVICE_PREREQUISITE_UNCONFIRMED
                    needs += ContextNeed(key, subject, range, "CONCRETE_25_HOUR_PREREQUISITE")
                }
            }
            if (listOf(ContextKey.ROAD_SAFETY_PRESERVED, ContextKey.WORKING_CONDITIONS_PRESERVED,
                    ContextKey.APPLICABLE_MAX_WORKING_TIME_RESPECTED).any {
                    snapshot.context.resolve(it, range, subject).flag == false }) continue
            result += RestModeChoice(RestWindowMode.OCCASIONAL_BUS_25,
                if (restrictions.isEmpty()) RestEligibility.PROVED else RestEligibility.CONDITIONAL,
                frozen(restrictions), subject.id, frozen(evidence), frozen(needs.distinctBy { it.id }))
        }
        return frozen(result)
    }
}
