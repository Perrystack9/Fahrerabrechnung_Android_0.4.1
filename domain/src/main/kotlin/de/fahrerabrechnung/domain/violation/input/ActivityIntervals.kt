package de.fahrerabrechnung.domain.violation.input

import de.fahrerabrechnung.domain.model.ActivityType
import java.time.Instant
import java.util.TreeMap

/** Interprets already decoded 0504 values. This is not another card parser or a legal rest solver. */
object ActivityIntervals {
    fun meaning(change: RecordedChange): ActivityMeaning = when {
        !change.cardInserted && !change.rawC -> ActivityMeaning.unknown(InputIssueCode.UNKNOWN_AFTER_WITHDRAWAL)
        !change.cardInserted && change.activity == ActivityType.LENKZEIT ->
            ActivityMeaning.unknown(InputIssueCode.MANUAL_DRIVING_ANOMALY)
        !change.cardInserted -> ActivityMeaning(change.activity, InputQuality.KNOWN,
            CrewEvidence.NOT_RECORDED, SlotEvidence.NOT_RELEVANT, true)
        else -> ActivityMeaning(change.activity, InputQuality.KNOWN,
            if (change.rawC) CrewEvidence.CREW else CrewEvidence.SOLO,
            if (change.coDriverSlot) SlotEvidence.CO_DRIVER else SlotEvidence.DRIVER, false)
    }

    fun prepare(sources: List<ActivitySource>, checkCancellation: () -> Unit = {}): PreparedActivities {
        val slices = mutableListOf<ActivityInterval>()
        val observations = mutableListOf<ActivityObservation>()
        val issues = mutableListOf<InputIssue>()
        for (source in sources.sortedBy { it.source.key.stableKey }) {
            checkCancellation()
            val days = source.days
            val invalid = mutableSetOf<Int>()
            for ((index, day) in days.withIndex()) {
                checkCancellation()
                if (day.dateUtc.nano != 0 || Math.floorMod(day.dateUtc.epochSecond, 86_400L) != 0L) {
                    invalid += index
                    issues += InputIssue(InputIssueCode.NON_MIDNIGHT_DATE, source.source.key, at = day.dateUtc)
                }
                if (day.recordIndex != index || day.changes.isEmpty() || day.changes.size > 1440 ||
                    day.changes.firstOrNull()?.minuteOfDay != 0 ||
                    day.changes.any { it.minuteOfDay !in 0..1439 } ||
                    day.changes.zipWithNext().any { (a, b) -> a.minuteOfDay > b.minuteOfDay }) {
                    invalid += index
                    issues += InputIssue(InputIssueCode.INVALID_CHANGE_SEQUENCE, source.source.key, at = day.dateUtc)
                }
                if (index > 0 && day.dateUtc <= days[index - 1].dateUtc) {
                    invalid += index - 1; invalid += index
                    issues += InputIssue(InputIssueCode.REVERSED_OR_DUPLICATE_DAY, source.source.key, at = day.dateUtc)
                }
            }
            val grouped = days.mapIndexed { index, day ->
                if (index in invalid) emptyList() else group(source.source.key, day)
            }
            for ((index, dayPoints) in grouped.withIndex()) {
                checkCancellation()
                observations += dayPoints
                for ((pointIndex, point) in dayPoints.withIndex()) {
                    point.meaning.reason?.let { issues += InputIssue(it, source.source.key, at = point.at) }
                    val sameDayEnd = dayPoints.getOrNull(pointIndex + 1)
                    val nextDay = grouped.getOrNull(index + 1)?.firstOrNull()
                    val midnightEnd = nextDay?.takeIf {
                        days[index + 1].dateUtc == days[index].dateUtc.plusSeconds(86_400) &&
                            it.at == days[index + 1].dateUtc &&
                            point.meaning.reason != InputIssueCode.AMBIGUOUS_MINUTE &&
                            it.meaning.reason != InputIssueCode.AMBIGUOUS_MINUTE
                    }
                    val end = sameDayEnd ?: midnightEnd
                    if (end == null) {
                        issues += InputIssue(InputIssueCode.OPEN_END, source.source.key, at = point.at)
                    } else if (point.at < end.at) {
                        slices += ActivityInterval(InputRange(point.at, end.at), point.meaning,
                            frozen(listOf(IntervalSupport(point.references, end.references,
                                if (sameDayEnd != null) EndEvidenceKind.NEXT_CHANGE else EndEvidenceKind.NEXT_UTC_DAY))))
                    }
                }
            }
        }
        val ordered = observations.sortedWith(compareBy({ it.at }, { it.references.first().stableKey }))
        val canonical = consolidate(slices, ordered, checkCancellation)
        canonical.forEach { interval ->
            if (interval.meaning.quality == InputQuality.CONFLICT)
                issues += InputIssue(InputIssueCode.SOURCE_CONFLICT, range = interval.range)
            if (interval.meaning.crew == CrewEvidence.CONFLICT)
                issues += InputIssue(InputIssueCode.CREW_CONFLICT, range = interval.range)
            if (interval.meaning.slot == SlotEvidence.CONFLICT)
                issues += InputIssue(InputIssueCode.SLOT_CONFLICT, range = interval.range)
            if (interval.meaning.reason == InputIssueCode.MISSING_INTERVAL)
                issues += InputIssue(InputIssueCode.MISSING_INTERVAL, range = interval.range)
        }
        return PreparedActivities(frozen(canonical), frozen(ordered), frozen(issues.distinct().sortedWith(
            compareBy({ it.at ?: it.range?.start }, { it.code.name }, { it.source?.stableKey }))))
    }

    private fun group(source: InputSourceKey, day: RecordedDay): List<ActivityObservation> =
        day.changes.withIndex().groupBy { it.value.minuteOfDay }.map { (minute, entries) ->
            val meanings = entries.map { meaning(it.value) }.distinct()
            ActivityObservation(day.dateUtc.plusSeconds(minute * 60L),
                meanings.singleOrNull() ?: ActivityMeaning.unknown(InputIssueCode.AMBIGUOUS_MINUTE),
                frozen(entries.map { ObservationRef(source, day.recordIndex, day.ringOffset, day.fingerprint, it.index) }),
                frozen(entries.map { it.value }))
        }

    private data class Events(val begin: MutableList<Int> = mutableListOf(), val end: MutableList<Int> = mutableListOf())

    /** A sweep line counts every actual instant at most once, regardless of import order. */
    private fun consolidate(slices: List<ActivityInterval>, points: List<ActivityObservation>,
        checkCancellation: () -> Unit): List<ActivityInterval> {
        val events = TreeMap<Instant, Events>()
        slices.forEachIndexed { index, interval ->
            events.getOrPut(interval.range.start, ::Events).begin += index
            events.getOrPut(interval.range.end, ::Events).end += index
        }
        val byTime = points.groupBy { it.at }
        val closedBegins = slices.flatMap { it.support }.flatMap { it.begin }.map { it.stableKey }.toSet()
        val openPoints = points.filter { point -> point.references.none { it.stableKey in closedBegins } }.toSet()
        byTime.keys.forEach { events.getOrPut(it, ::Events) }
        val times = events.keys.toList()
        val active = linkedMapOf<Int, ActivityInterval>()
        val unresolvedPoints = linkedMapOf<String, ActivityObservation>()
        val result = mutableListOf<ActivityInterval>()
        for ((index, start) in times.withIndex()) {
            checkCancellation()
            val changes = events.getValue(start)
            changes.end.forEach(active::remove)
            changes.begin.forEach { active[it] = slices[it] }
            for (point in byTime[start].orEmpty()) {
                val source = point.references.first().source.stableKey
                unresolvedPoints.remove(source)
                if (point in openPoints && point.meaning.quality == InputQuality.KNOWN) unresolvedPoints[source] = point
            }
            val end = times.getOrNull(index + 1) ?: break
            val values = active.values.toList()
            val known = values.filter { it.meaning.quality == InputQuality.KNOWN }
            val activities = known.map { it.meaning.activity }.distinct()
            // An unended conflicting observation cannot assert a duration, but it also cannot be
            // ignored in favour of another source's old closed state. Carry an explicit obstacle
            // until a further observation from that source; never create a positive interval from it.
            val openConflict = known.isNotEmpty() && unresolvedPoints.values.any { it.meaning.activity !in activities }
            val meaning = when {
                activities.size > 1 || openConflict -> ActivityMeaning(ActivityType.UNBEKANNT, InputQuality.CONFLICT,
                    CrewEvidence.CONFLICT, SlotEvidence.CONFLICT, false, InputIssueCode.SOURCE_CONFLICT)
                known.isNotEmpty() -> {
                    val crews = known.map { it.meaning.crew }.filter { it != CrewEvidence.NOT_RECORDED }.distinct()
                    val slots = known.map { it.meaning.slot }.filter { it != SlotEvidence.NOT_RELEVANT }.distinct()
                    val openCrewConflict = crews.isNotEmpty() && unresolvedPoints.values.any {
                        it.meaning.crew != CrewEvidence.NOT_RECORDED && it.meaning.crew !in crews }
                    val openSlotConflict = slots.isNotEmpty() && unresolvedPoints.values.any {
                        it.meaning.slot != SlotEvidence.NOT_RELEVANT && it.meaning.slot !in slots }
                    ActivityMeaning(activities.single(), InputQuality.KNOWN,
                        if (crews.size > 1 || openCrewConflict) CrewEvidence.CONFLICT else crews.singleOrNull() ?: CrewEvidence.NOT_RECORDED,
                        if (slots.size > 1 || openSlotConflict) SlotEvidence.CONFLICT else slots.singleOrNull() ?: SlotEvidence.NOT_RELEVANT,
                        known.any { it.meaning.manualTachograph })
                }
                values.isNotEmpty() -> ActivityMeaning.unknown(
                    values.mapNotNull { it.meaning.reason }.minByOrNull { it.ordinal } ?: InputIssueCode.MISSING_INTERVAL)
                else -> ActivityMeaning.unknown(InputIssueCode.MISSING_INTERVAL)
            }
            val support = values.flatMap { it.support }.toMutableList()
            val conflictingPoints = unresolvedPoints.values.filter { point -> known.any { interval ->
                interval.meaning.activity != point.meaning.activity ||
                    (interval.meaning.crew != CrewEvidence.NOT_RECORDED && point.meaning.crew != CrewEvidence.NOT_RECORDED &&
                        interval.meaning.crew != point.meaning.crew) ||
                    (interval.meaning.slot != SlotEvidence.NOT_RELEVANT && point.meaning.slot != SlotEvidence.NOT_RELEVANT &&
                        interval.meaning.slot != point.meaning.slot)
            } }
            for (point in conflictingPoints) {
                support += IntervalSupport(point.references, frozen(known.flatMap { it.support }.flatMap { it.end }.distinct()),
                    EndEvidenceKind.OPEN_POINT_CONFLICT)
            }
            // Same-time older known points can share a newer compatible end; unknown is not a positive conflict.
            for (point in byTime[start].orEmpty().filter { it.meaning.quality == InputQuality.KNOWN }) {
                for (interval in known.filter { compatible(it.meaning, point.meaning) }) {
                    interval.support.forEach { supporting ->
                        val begin = (supporting.begin + point.references).distinct().sortedBy { it.stableKey }
                        if (begin.size != supporting.begin.size) support += IntervalSupport(frozen(begin), supporting.end,
                            EndEvidenceKind.COMPATIBLE_SOURCE)
                    }
                }
            }
            if (support.isEmpty()) {
                val begin = byTime[start].orEmpty().flatMap { it.references }
                val ending = byTime[end].orEmpty().flatMap { it.references }
                if (begin.isNotEmpty() || ending.isNotEmpty()) support += IntervalSupport(frozen(begin), frozen(ending), EndEvidenceKind.GAP_BRACKETS)
            }
            result += ActivityInterval(InputRange(start, end), meaning,
                frozen(support.distinct().sortedBy { it.begin.firstOrNull()?.stableKey.orEmpty() + it.kind.name }))
        }
        return result
    }

    private fun compatible(a: ActivityMeaning, b: ActivityMeaning) = a.activity == b.activity &&
        (a.crew == b.crew || a.crew == CrewEvidence.NOT_RECORDED || b.crew == CrewEvidence.NOT_RECORDED) &&
        (a.slot == b.slot || a.slot == SlotEvidence.NOT_RELEVANT || b.slot == SlotEvidence.NOT_RELEVANT)
}
