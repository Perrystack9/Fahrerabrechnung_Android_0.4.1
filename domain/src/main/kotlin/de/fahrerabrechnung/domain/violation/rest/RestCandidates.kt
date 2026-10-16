package de.fahrerabrechnung.domain.violation.rest

import de.fahrerabrechnung.domain.model.ActivityType
import de.fahrerabrechnung.domain.violation.driving.RuleLaw
import de.fahrerabrechnung.domain.violation.input.*
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

internal enum class RestDatePhase { BEFORE, AFTER, UNCONFIRMED }
internal object RestLawVersions {
    val base: LocalDate = LocalDate.of(2007, 4, 11)
    val ferryWeekly: LocalDate = LocalDate.of(2020, 8, 20)
    val bus: LocalDate = LocalDate.of(2024, 5, 22)
    fun phase(range: InputRange, date: LocalDate): RestDatePhase = when {
        range.end.atOffset(ZoneOffset.MAX).toLocalDate() < date -> RestDatePhase.BEFORE
        range.start.atOffset(ZoneOffset.MIN).toLocalDate() >= date -> RestDatePhase.AFTER
        else -> RestDatePhase.UNCONFIRMED
    }
    fun daily(id: String, mode: RestWindowMode) = RuleLaw(id,
        "VO (EG) Nr.561/2006 Art.4(g/o), Art.8" + when (mode) {
            RestWindowMode.SOLO_24 -> "(2–4)"
            RestWindowMode.CREW_30 -> "(5)"
            RestWindowMode.OCCASIONAL_BUS_25 -> "(2a)"
        }, if (mode == RestWindowMode.OCCASIONAL_BUS_25) "561/2006+2024/1258:2024-05-22" else "561/2006:2007-04-11",
        if (mode == RestWindowMode.OCCASIONAL_BUS_25) listOf("Q01-B", "Q03") else listOf("Q01-B"))
    fun weekly(kind: WeeklyRestKind, ferry: Boolean) = RuleLaw(if (ferry) {
        if (kind == WeeklyRestKind.REGULAR) "F04" else "F03"
    } else "R07", "VO (EG) Nr.561/2006 Art.4(h), Art.8(3)" + if (ferry) ", Art.9(1)" else "",
        if (ferry) "561/2006+2020/1054:2020-08-20" else "561/2006:2007-04-11",
        if (ferry) listOf("Q01-B", "Q02") else listOf("Q01-B"))
}

/** Combines already canonical M1 intervals. Never decodes a card file or fills an activity gap. */
internal object RestCandidates {
    private data class Block(val slices: List<RestSlice>) {
        val range get() = InputRange(slices.first().range.start, slices.last().range.end)
    }
    fun build(snapshot: ViolationInputSnapshot, check: () -> Unit): List<RestCandidate> {
        val blocks = mutableListOf<Block>()
        var current = mutableListOf<RestSlice>()
        fun finish() { if (current.isNotEmpty()) blocks += Block(frozen(current)); current = mutableListOf() }
        for (interval in snapshot.intervals) {
            check()
            if (interval.meaning.quality != InputQuality.KNOWN || interval.meaning.activity != ActivityType.PAUSE_RUHE) {
                finish(); continue
            }
            // A concrete transfer context can invalidate a portion without editing the recorded activity.
            val boundaries = sortedSetOf(interval.range.start, interval.range.end)
            snapshot.context.assertions.filter { it.key == ContextKey.TRANSFER_REQUIRES_OTHER_WORK }.forEach {
                if (it.from > interval.range.start && it.from < interval.range.end) boundaries += it.from
                it.until?.takeIf { end -> end > interval.range.start && end < interval.range.end }?.let(boundaries::add)
            }
            for ((start, end) in boundaries.toList().zipWithNext()) {
                val range = InputRange(start, end)
                if (snapshot.context.resolve(ContextKey.TRANSFER_REQUIRES_OTHER_WORK, range).flag == true) { finish(); continue }
                if (current.lastOrNull()?.range?.end?.let { it != start } == true) finish()
                current += RestSlice(range, interval.support, interval.meaning.manualTachograph)
            }
        }
        finish()
        val result = mutableListOf<RestCandidate>()
        for ((index, block) in blocks.withIndex()) {
            check()
            result += candidate(snapshot, block.slices, emptyList())
            var slices = block.slices
            val interruptions = mutableListOf<RestInterruption>()
            // Include the first rejected third interruption as an explanation; never credit it.
            for (nextIndex in index + 1..minOf(index + 3, blocks.lastIndex)) {
                check()
                val next = blocks[nextIndex]
                val gap = InputRange(slices.last().range.end, next.range.start)
                val activities = snapshot.intervals.filter { it.range.overlaps(gap) }.map {
                    it.copy(range = InputRange(maxOf(it.range.start, gap.start), minOf(it.range.end, gap.end)))
                }
                interruptions += RestInterruption(gap, frozen(activities))
                slices = slices + next.slices
                if (ferryPoints(snapshot, InputRange(slices.first().range.start, slices.last().range.end)).isNotEmpty())
                    result += candidate(snapshot, slices, interruptions)
                if (interruptions.sumOf { it.seconds } > 3600) break
            }
        }
        return frozen(result.distinctBy { it.id }.sortedWith(compareBy({ it.range.start }, { it.range.end }, { it.id })))
    }

    private fun endProved(snapshot: ViolationInputSnapshot, end: Instant): Boolean =
        snapshot.intervals.any { it.range.start == end && it.meaning.quality == InputQuality.KNOWN &&
            it.meaning.activity != ActivityType.PAUSE_RUHE } ||
        snapshot.observations.filter { it.at == end }.map { it.meaning }.distinct().singleOrNull()?.let {
            it.quality == InputQuality.KNOWN && it.activity != ActivityType.PAUSE_RUHE
        } == true || snapshot.context.assertions.any { it.key == ContextKey.TRANSFER_REQUIRES_OTHER_WORK &&
            it.from == end && it.value == ContextValue.Flag(true) &&
            snapshot.context.resolveAt(it.key, end).flag == true }

    private fun marker(point: InputPoint) = point.family == InputFamily.SPECIAL_CONDITIONS &&
        (point.fields["typeRaw"] == "3" || (point.fields["typeRaw"] == "4" && point.source.generation != "GEN1"))

    private fun ferryPoints(snapshot: ViolationInputSnapshot, range: InputRange): List<InputPoint> {
        val points = snapshot.points.filter(::marker)
        val inside = points.filter { it.timeUtc?.let { time ->
            if (it.fields["typeRaw"] == "3") time >= range.start && time < range.end
            else time > range.start && time <= range.end
        } == true }
        // G1's single point never creates a transport interval or a fictitious end.
        val prior = points.filter { it.source.generation != "GEN1" && it.timeUtc?.let { time -> time <= range.start } == true }
        val latest = prior.mapNotNull { it.timeUtc }.maxOrNull()
        val atLatest = prior.filter { it.timeUtc == latest }
        return frozen((inside + (if (atLatest.any { it.fields["typeRaw"] == "3" }) atLatest else emptyList()) +
            points.filter { it.timeUtc == null }).distinct())
    }

    internal fun ferryContext(context: InputContext, key: ContextKey, range: InputRange): ResolvedContext {
        val entries = context.assertions.filter { it.key == key && it.covers(range) &&
            it.subject.kind in setOf(ContextSubjectKind.REST, ContextSubjectKind.JOURNEY) }
        val values = entries.map { it.value }.distinct()
        return ResolvedContext(values.singleOrNull(), values.size > 1, frozen(entries.sortedBy { it.semanticId }))
    }

    private fun candidate(snapshot: ViolationInputSnapshot, rest: List<RestSlice>,
        interruptions: List<RestInterruption>): RestCandidate {
        val range = InputRange(rest.first().range.start, rest.last().range.end)
        val points = ferryPoints(snapshot, range)
        val restrictions = linkedSetOf<RestRestriction>()
        val evidence = linkedSetOf<String>()
        val needs = mutableListOf<ContextNeed>()
        var eligibility = RestEligibility.PROVED
        fun conditional(reason: RestRestriction) {
            if (eligibility != RestEligibility.REJECTED) eligibility = RestEligibility.CONDITIONAL
            restrictions += reason
        }
        if (interruptions.size > 2 || interruptions.sumOf { it.seconds } > 3600) {
            eligibility = RestEligibility.REJECTED; restrictions += RestRestriction.FERRY_INTERRUPTION_LIMIT_EXCEEDED
        }
        for (gap in interruptions) {
            val coverage = InputCoverage.forRule(snapshot, gap.range, RuleInputRequirement("F02", setOf(InputDependency.ACTIVITY)))
            if (!coverage.factualPrerequisitesCovered) conditional(RestRestriction.ACTIVITY_GAP)
        }
        for (slice in rest) {
            val transfer = snapshot.context.resolve(ContextKey.TRANSFER_REQUIRES_OTHER_WORK, slice.range)
            evidence += transfer.evidence.map { it.semanticId }
            if (transfer.conflict) conditional(RestRestriction.TRAVEL_REST_PREREQUISITE_UNCONFIRMED)
        }
        // A context-created boundary must retain the assertion that actually cuts the recorded rest.
        evidence += snapshot.context.assertions.filter { it.key == ContextKey.TRANSFER_REQUIRES_OTHER_WORK &&
            (it.from == range.end || it.until == range.start) }.map { it.semanticId }
        val subject = snapshot.context.assertions.filter { it.covers(range) && it.subject.kind in
            setOf(ContextSubjectKind.REST, ContextSubjectKind.JOURNEY) && it.key in
            setOf(ContextKey.FERRY_SLEEPING_FACILITY, ContextKey.FERRY_ACCOMPANIES_VEHICLE, ContextKey.FERRY_SCHEDULED_MINUTES) }
            .map { it.subject }.distinct().singleOrNull() ?: ContextSubject(ContextSubjectKind.REST, "rest-${range.start.epochSecond}")
        fun read(key: ContextKey): ResolvedContext = ferryContext(snapshot.context, key, range).also {
            evidence += it.evidence.map(ContextAssertion::semanticId)
            if (it.value == null) {
                needs += ContextNeed(key, subject, range, "CONCRETE_M3_FERRY_PREREQUISITE")
                conditional(if (it.conflict) RestRestriction.FERRY_CONTEXT_CONFLICT else RestRestriction.FERRY_PREREQUISITE_MISSING)
            }
        }
        if (points.isNotEmpty() || interruptions.isNotEmpty()) {
            if (points.isEmpty() || points.any { it.timeUtc == null }) conditional(RestRestriction.FERRY_PREREQUISITE_MISSING)
            if (points.groupBy { it.timeUtc }.values.any { group -> group.map { it.fields["typeRaw"] }.distinct().size > 1 })
                conditional(RestRestriction.FERRY_CONTEXT_CONFLICT)
            val accompanies = read(ContextKey.FERRY_ACCOMPANIES_VEHICLE)
            val facility = read(ContextKey.FERRY_SLEEPING_FACILITY)
            if (accompanies.flag == false) conditional(RestRestriction.FERRY_CONTEXT_CONFLICT)
            if (facility.text == "NONE") {
                eligibility = RestEligibility.REJECTED; restrictions += RestRestriction.FERRY_FACILITY_RULED_OUT
            }
            if (RestLawVersions.phase(range, RestLawVersions.ferryWeekly) != RestDatePhase.AFTER && facility.text == "CABIN")
                conditional(RestRestriction.HISTORICAL_ACTIVATION_UNCONFIRMED)
        }
        val weekly = mutableListOf<WeeklyRestQualification>()
        val net = rest.sumOf { it.seconds }
        for ((kind, minimum) in listOf(WeeklyRestKind.REDUCED to 86400L, WeeklyRestKind.REGULAR to 162000L)) {
            if (net < minimum) continue
            val ferry = interruptions.isNotEmpty()
            var status = eligibility
            val limits = restrictions.toMutableSet()
            val contextIds = evidence.toMutableSet()
            val missing = needs.toMutableList()
            when (RestLawVersions.phase(range, RestLawVersions.base)) {
                RestDatePhase.BEFORE -> { status = RestEligibility.REJECTED; limits += RestRestriction.BEFORE_561_TEMPORAL_SCOPE }
                RestDatePhase.UNCONFIRMED -> {
                    if (status != RestEligibility.REJECTED) status = RestEligibility.CONDITIONAL
                    limits += RestRestriction.HISTORICAL_ACTIVATION_UNCONFIRMED
                }
                RestDatePhase.AFTER -> Unit
            }
            if (ferry) {
                when (RestLawVersions.phase(range, RestLawVersions.ferryWeekly)) {
                    RestDatePhase.BEFORE -> status = RestEligibility.REJECTED
                    RestDatePhase.UNCONFIRMED -> {
                        if (status != RestEligibility.REJECTED) status = RestEligibility.CONDITIONAL
                        limits += RestRestriction.MIXED_HISTORICAL_VERSION
                    }
                    RestDatePhase.AFTER -> Unit
                }
                if (kind == WeeklyRestKind.REGULAR) {
                    val facility = ferryContext(snapshot.context, ContextKey.FERRY_SLEEPING_FACILITY, range)
                    val schedule = ferryContext(snapshot.context, ContextKey.FERRY_SCHEDULED_MINUTES, range)
                    contextIds += facility.evidence.map { it.semanticId }; contextIds += schedule.evidence.map { it.semanticId }
                    if (facility.text?.let { it != "CABIN" } == true || schedule.number?.let { it < 480 } == true)
                        status = RestEligibility.REJECTED
                    if (facility.value == null || schedule.value == null) {
                        if (status != RestEligibility.REJECTED) status = RestEligibility.CONDITIONAL
                        limits += RestRestriction.FERRY_PREREQUISITE_MISSING
                        if (schedule.value == null) missing += ContextNeed(ContextKey.FERRY_SCHEDULED_MINUTES, subject, range,
                            "CONCRETE_M3_REGULAR_WEEKLY_FERRY_PREREQUISITE")
                    }
                }
            }
            weekly += WeeklyRestQualification(kind, status, RestLawVersions.weekly(kind, ferry),
                frozen(limits), frozen(contextIds), frozen(missing.distinctBy { it.id }))
        }
        val id = InputRevision.hash("m3-rest-candidate") {
            text(DAILY_REST_INTERPRETATION); text(snapshot.profileId); instant(range.start); instant(range.end)
            int(rest.size); rest.forEach { instant(it.range.start); instant(it.range.end) }
            int(interruptions.size); interruptions.forEach { instant(it.range.start); instant(it.range.end) }
        }
        return RestCandidate(id, range, frozen(rest), frozen(interruptions), endProved(snapshot, range.end), eligibility,
            frozen(restrictions), points, frozen(evidence), frozen(needs.distinctBy { it.id }), frozen(weekly))
    }
}
