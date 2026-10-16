package de.fahrerabrechnung.domain.violation.weekly

import de.fahrerabrechnung.domain.violation.driving.*
import de.fahrerabrechnung.domain.violation.input.*
import de.fahrerabrechnung.domain.violation.rest.*
import java.time.Duration
import java.time.Instant

internal data class WeeklyEvaluation(val allocation: WeeklyAllocation, val deadlines: List<WeeklyDeadline>,
    val findings: List<WeeklyRestFinding>, val weekConstraints: Set<String>, val jointConstraints: Set<String>)

internal object WeeklyRules {
    fun evaluate(snapshot: ViolationInputSnapshot, driving: DrivingCalculation, path: WeeklyAssignmentDraft,
        check: () -> Unit): WeeklyEvaluation {
        val observed = snapshot.latestClosedEnd
        val id = InputRevision.hash("m4-allocation") {
            path.physical.forEach { text(it.id) }; path.uses.forEach { text(it.id) }
            path.payments.forEach { text(it.debtId); instant(it.range.start); instant(it.range.end) }
        }
        val used = path.uses.filterNot { it.extra }.sortedBy { it.range.start }
        val pairs = used.zipWithNext().mapNotNull { (first, second) ->
            check()
            if (first.kind != WeeklyRestKind.REDUCED || second.kind != WeeklyRestKind.REDUCED ||
                second.assignedWeek != first.assignedWeek?.plusSeconds(WeeklyTime.WEEK)) return@mapNotNull null
            if ((first.restrictions + second.restrictions).any { it in setOf(WeeklyRestriction.SCOPE_EXCLUDED,
                    WeeklyRestriction.SCOPE_UNDETERMINED) }) return@mapNotNull null
            val prerequisites = WeeklyContexts.international(snapshot, first, second)
            if (prerequisites.eligibility == RestEligibility.REJECTED) return@mapNotNull null
            val next = path.uses.filter { it.kind == WeeklyRestKind.REGULAR && it.range.start >= second.range.end }
                .minByOrNull { it.range.start }
            val debts = path.debts.filter { it.candidateId in listOf(first.candidateId, second.candidateId) }
            val reservations = path.payments.filter { it.debtId in debts.map { d -> d.id } }
            val compensated = next?.let { regular ->
                reservations.size == 2 && reservations.all { it.donorCandidateId == regular.candidateId && it.range.end <= regular.range.start } &&
                    reservations.maxOfOrNull { it.range.end } == regular.range.start &&
                    reservations.sortedBy { it.range.start }.zipWithNext().all { (a, b) -> a.range.end == b.range.start }
            }
            InternationalWeeklyPair(first.id, second.id, frozen(debts.map { it.id }), next?.id, compensated,
                prerequisites.eligibility, frozen((prerequisites.restrictions + first.restrictions + second.restrictions +
                    WeeklyRestriction.L02_UTC_WEEK_PROJECTION_ONLY + WeeklyRestriction.L04_ASSIGNMENT_REVIEW_OUTSTANDING).distinct()),
                prerequisites.evidence, prerequisites.needs, prerequisites.locationEvidence)
        }
        val physicalById = path.physical.associateBy { it.id }
        val chronological = timingUses(path)
        val buses = mutableListOf<BusWeeklyDerogation>()
        for ((index, use) in chronological.withIndex()) {
            check()
            if (!use.actualEndProved || use.kind != WeeklyRestKind.REGULAR || observed == null) continue
            val next = chronological.getOrNull(index + 1)?.let { physicalById[it.candidateId] }
            val anchor = physicalById.getValue(use.candidateId)
            val until = next?.range?.start ?: observed
            if (until > anchor.range.end) buses += WeeklyContexts.bus(snapshot, driving, anchor, until, next, check)
        }
        val windows = windows(snapshot, path, pairs, buses, check)
        val deadlines = deadlines(snapshot, path, buses, check)
        val findings = mutableListOf<WeeklyRestFinding>()
        fun finding(rule: String, key: String, range: InputRange, onset: Instant?, quantity: Long,
            sourceIds: List<String>, restrictions: List<WeeklyRestriction>) {
            if (quantity <= 0) return
            findings += WeeklyRestFinding(InputRevision.hash("m4-finding") { text(snapshot.profileId); text(rule); text(key) },
                WeeklyTime.law(rule, range.start), range, onset, quantity, frozen(sourceIds.distinct()), listOf(id), false,
                frozen(restrictions.distinct()))
        }
        for (window in windows.filter { it.state == WeeklyWindowState.NORMAL_RULE_DEFICIT }) {
            val chosen = window.useIds.mapNotNull { useId -> path.uses.firstOrNull { it.id == useId } }
            val minimumMissing = when {
                window.requiredCount == 4 -> maxOf(0, 4 - chosen.size) * WeeklyTime.REDUCED +
                    maxOf(0, 2 - window.regularCount) * (WeeklyTime.REGULAR - WeeklyTime.REDUCED)
                chosen.isEmpty() -> WeeklyTime.REGULAR + WeeklyTime.REDUCED
                chosen.size == 1 -> if (window.regularCount == 1) WeeklyTime.REDUCED else WeeklyTime.REGULAR
                else -> (WeeklyTime.REGULAR - (chosen.maxOfOrNull { it.seconds } ?: 0)).coerceAtLeast(0)
            }
            finding(window.law.id, window.id, window.range, window.range.end, minimumMissing,
                chosen.map { it.candidateId }, window.restrictions)
        }
        for (deadline in deadlines.filter { it.lateBySeconds > 0 }) {
            val end = deadline.nextBaseStart?.let { minOf(it, deadline.observedThrough) } ?: deadline.observedThrough
            if (end > deadline.deadline) finding(if (deadline.standardSeconds == WeeklyTime.TWELVE_DAYS) "W10" else "W02",
                deadline.id, InputRange(deadline.deadline, end), deadline.deadline, deadline.lateBySeconds,
                listOfNotNull(deadline.anchorCandidateId, deadline.nextCandidateId), deadline.restrictions)
        }
        if (observed != null) for (debt in path.debts.filter { it.due <= observed }) {
            check()
            val payment = path.payments.firstOrNull { it.debtId == debt.id }
            if (payment?.onTime == true) continue
            val through = minOf(observed, payment?.range?.end ?: observed)
            val missing = if (payment == null) debt.seconds else minOf(debt.seconds,
                Duration.between(debt.due, payment.range.end).seconds.coerceAtLeast(0))
            val from = debt.originRange.start
            val coverage = WeeklyTime.coverage(snapshot, InputRange(debt.originRange.end, maxOf(debt.due, through)), "W04")
            finding("W04", debt.originId, InputRange(from, maxOf(through, debt.due)), debt.due, missing,
                listOfNotNull(debt.candidateId, payment?.donorCandidateId), debt.restrictions + WeeklyTime.restrictions(coverage))
        }
        for (pair in pairs.filter { it.compensationBeforeRegular == false }) {
            val next = path.uses.firstOrNull { it.id == pair.nextRegularUseId } ?: continue
            val amount = path.debts.filter { it.id in pair.debtIds }.sumOf { it.seconds }
            val origins = path.uses.filter { it.id in listOf(pair.firstUseId, pair.secondUseId) }.map { it.candidateId }
            finding("W08", origins.joinToString("/"), next.range, next.range.start, amount,
                path.uses.filter { it.id in listOf(pair.firstUseId, pair.secondUseId, pair.nextRegularUseId) }.map { it.candidateId },
                pair.restrictions + WeeklyRestriction.L04_ASSIGNMENT_REVIEW_OUTSTANDING)
        }
        for (bus in buses) {
            val next = bus.postRestCandidateId?.let(physicalById::get)
            if (next != null && next.actualEndProved && bus.postRestComplete == false)
                finding("W11", bus.anchorCandidateId, next.range, next.range.start,
                    (69 * 3600L - next.netRestSeconds).coerceAtLeast(0), listOf(bus.anchorCandidateId, next.id),
                    bus.restrictions + WeeklyRestriction.BUS_POST_REST_PARTITION_UNCONFIRMED)
            for (night in bus.nightProjections.filter { it.excessSeconds > 0 }) {
                val period = driving.projections.first { it.id == night.drivingPeriodId }
                finding("W12", bus.anchorCandidateId + period.id, period.range, null, night.excessSeconds,
                    listOf(bus.anchorCandidateId), bus.restrictions + night.restrictions)
            }
        }
        val credit = DailyRestAttribution(java.util.Collections.unmodifiableSet(LinkedHashSet(path.physical.map { it.id })), frozenMap(path.daily))
        val allocation = WeeklyAllocation(id, frozen(path.uses), frozen(path.debts), frozen(path.payments),
            frozen(windows), frozen(pairs), frozen(buses),
            listOf(WeeklyRestriction.L04_ASSIGNMENT_REVIEW_OUTSTANDING), credit,
            frozen(deadlines.map { it.copy(restrictions = frozen((it.restrictions +
                WeeklyRestriction.ALTERNATIVE_ASSIGNMENT_UNRESOLVED).distinct())) }), frozen(findings))
        val weekConstraints = windows.filter { it.state == WeeklyWindowState.NORMAL_RULE_DEFICIT }.map { it.id }.toSet()
        val jointConstraints = findings.filter { it.law.id !in setOf("W01", "W07") }.map { it.id }.toSet() +
            buses.filter { bus -> bus.postRestComplete == true && path.uses.count { it.busPostAnchorId == bus.anchorCandidateId } != 2 }
                .map { "bus-post-parts:${it.anchorCandidateId}" }
        return WeeklyEvaluation(allocation, frozen(deadlines), frozen(findings), weekConstraints, jointConstraints)
    }

    private fun windows(snapshot: ViolationInputSnapshot, path: WeeklyAssignmentDraft,
        pairs: List<InternationalWeeklyPair>, buses: List<BusWeeklyDerogation>, check: () -> Unit): List<WeeklyCalendarWindow> {
        val observed = snapshot.latestClosedEnd ?: return emptyList()
        val first = snapshot.intervals.firstOrNull()?.range?.start ?: return emptyList()
        val result = mutableListOf<WeeklyCalendarWindow>()
        var week = WeeklyTime.week(first)
        val last = WeeklyTime.week(observed)
        while (week <= last) {
            check()
            for (count in listOf(2, 4)) {
                val range = InputRange(week, week.plusSeconds(count * WeeklyTime.WEEK))
                if (count == 4 && pairs.none { p -> path.uses.any { it.id == p.firstUseId && it.assignedWeek in WeeklyTime.weeks(range) } }) continue
                val until = minOf(observed, range.end)
                if (until <= range.start) continue
                val coverage = WeeklyTime.coverage(snapshot, InputRange(range.start, until), if (count == 2) "W01" else "W07")
                val chosen = path.uses.filter { !it.extra && it.assignedWeek?.let { w -> w >= week && w < range.end } == true }
                val regular = chosen.count { it.kind == WeeklyRestKind.REGULAR }
                val pairIds = pairs.filter { it.firstUseId in chosen.map { u -> u.id } && it.secondUseId in chosen.map { u -> u.id } }
                val bus = buses.filter { it.range.overlaps(range) && it.eligibility != RestEligibility.REJECTED }
                val ordinary = chosen.size >= count && regular >= if (count == 2) 1 else 2
                val exception = count == 2 && (pairIds.isNotEmpty() || bus.isNotEmpty())
                val restrictions = linkedSetOf(WeeklyRestriction.L02_UTC_WEEK_PROJECTION_ONLY)
                restrictions += WeeklyTime.restrictions(coverage)
                restrictions += WeeklyTime.phaseRestrictions(InputRange(range.start, until))
                restrictions += chosen.flatMap { it.restrictions }
                if (path.physical.any { it.range.overlaps(range) && WeeklyTime.weeks(it.range).size > 2 })
                    restrictions += WeeklyRestriction.L04_LONG_REST_PARTITION_UNCONFIRMED
                if (first > range.start) restrictions += WeeklyRestriction.LEFT_WEEKLY_HISTORY_MISSING
                if (exception && !ordinary) {
                    restrictions += pairIds.flatMap { it.restrictions }
                    restrictions += bus.flatMap { it.restrictions }
                }
                val pending = path.physical.any { !it.actualEndProved && it.range.start < range.end && it.range.end == observed }
                val state = when {
                    !coverage.factualPrerequisitesCovered || coverage.excludedScope.isNotEmpty() -> WeeklyWindowState.INDETERMINATE
                    chosen.any { use -> use.restrictions.any { it in setOf(WeeklyRestriction.REST_PREREQUISITE_UNCONFIRMED,
                        WeeklyRestriction.SCOPE_UNDETERMINED, WeeklyRestriction.SCOPE_EXCLUDED) } } -> WeeklyWindowState.INDETERMINATE
                    !ordinary && exception && (pairIds.any { it.eligibility != RestEligibility.PROVED } ||
                        bus.any { it.eligibility != RestEligibility.PROVED }) -> WeeklyWindowState.INDETERMINATE
                    ordinary || exception -> WeeklyWindowState.FULFILLED_PROJECTION
                    observed < range.end || pending -> WeeklyWindowState.OPEN
                    else -> WeeklyWindowState.NORMAL_RULE_DEFICIT
                }
                val id = InputRevision.hash("m4-calendar-window") { text(snapshot.profileId); instant(week); int(count) }
                result += WeeklyCalendarWindow(id, WeeklyTime.law(if (count == 2) "W01" else "W07", range.start), range,
                    count, if (count == 2) 1 else 2, frozen(chosen.map { it.id }), regular, state, coverage, frozen(restrictions))
            }
            week = week.plusSeconds(WeeklyTime.WEEK)
        }
        return frozen(result)
    }

    private fun deadlines(snapshot: ViolationInputSnapshot, path: WeeklyAssignmentDraft,
        buses: List<BusWeeklyDerogation>, check: () -> Unit): List<WeeklyDeadline> {
        val observed = snapshot.latestClosedEnd ?: return emptyList()
        val physical = path.physical.associateBy { it.id }
        val chronological = timingUses(path)
        return frozen(chronological.mapIndexedNotNull { index, anchor ->
            check()
            if (!anchor.actualEndProved) return@mapIndexedNotNull null
            val actual = physical.getValue(anchor.candidateId)
            val next = chronological.getOrNull(index + 1)
            val end = minOf(observed, next?.range?.start ?: observed)
            if (end < actual.range.end) return@mapIndexedNotNull null
            val bus = buses.filter { it.anchorCandidateId == anchor.candidateId }
            val limit = if (bus.isEmpty()) WeeklyTime.SIX_DAYS else WeeklyTime.TWELVE_DAYS
            val deadline = actual.range.end.plusSeconds(limit)
            val coverageRange = if (end > actual.range.end) InputRange(actual.range.end, end) else actual.range
            val coverage = WeeklyTime.coverage(snapshot, coverageRange, "W02")
            val restrictions = linkedSetOf<WeeklyRestriction>()
            restrictions += anchor.restrictions
            restrictions += next?.restrictions.orEmpty()
            restrictions += WeeklyTime.restrictions(coverage)
            restrictions += bus.flatMap { it.restrictions }
            if (bus.isNotEmpty()) restrictions += WeeklyRestriction.BUS_POST_REST_PARTITION_UNCONFIRMED
            if (bus.size > 1) restrictions += WeeklyRestriction.ALTERNATIVE_ASSIGNMENT_UNRESOLVED
            if (path.physical.any { !it.actualEndProved && it.range.start <= deadline && it.range.end >= end })
                restrictions += WeeklyRestriction.OPEN_REST_END
            val late = Duration.between(deadline, end).seconds.coerceAtLeast(0)
            WeeklyDeadline(InputRevision.hash("m4-weekly-deadline") { text(snapshot.profileId); text(anchor.candidateId) },
                anchor.candidateId, actual.range.end, deadline, observed, next?.candidateId, next?.range?.start,
                limit, late, coverage, frozen(restrictions))
        })
    }

    private data class TimingUse(val candidateId: String, val range: InputRange, val kind: WeeklyRestKind,
        val actualEndProved: Boolean, val restrictions: List<WeeklyRestriction>)

    /** Compound allocation parts do not create intermediate observed weekly-rest ends. */
    private fun timingUses(path: WeeklyAssignmentDraft): List<TimingUse> = path.uses.groupBy { it.candidateId }
        .values.map { parts ->
            val first = parts.minBy { it.range.start }; val last = parts.maxBy { it.range.start }
            TimingUse(first.candidateId, InputRange(first.range.start, last.range.end), last.kind,
                path.physical.first { it.id == first.candidateId }.actualEndProved,
                frozen(parts.flatMap { it.restrictions }.distinct()))
        }.sortedBy { it.range.start }
}
