package de.fahrerabrechnung.domain.violation.weekly

import de.fahrerabrechnung.domain.violation.input.*
import de.fahrerabrechnung.domain.violation.rest.*
import de.fahrerabrechnung.domain.violation.driving.DrivingCalculation
import java.time.Instant

internal class WeeklySearchLimit : RuntimeException()
internal class WeeklySearchBudget(private val maximum: Int, private val check: () -> Unit) {
    var evaluated = 0
        private set
    fun step() { check(); if (evaluated++ >= maximum) throw WeeklySearchLimit() }
}
internal data class WeeklyAssignmentDraft(
    val physical: List<RestCandidate>, val uses: List<WeeklyRestUse>,
    val debts: List<WeeklyCompensationDebt>, val payments: List<CompensationReservation>,
    val daily: Map<String, DailyRestAllocation>,
)

/** Joint chronological enumeration; a physical interval is consumed once, including ferry alternatives. */
internal object WeeklyAssignments {
    fun enumerate(snapshot: ViolationInputSnapshot, driving: DrivingCalculation, candidates: List<RestCandidate>, budget: WeeklySearchBudget,
        accept: (WeeklyAssignmentDraft) -> Unit) {
        val natural = candidates.filterNot { it.interrupted }.sortedBy { it.range.start }
        fun children(index: Int, path: WeeklyAssignmentDraft): Sequence<Pair<Int, WeeklyAssignmentDraft>> = sequence {
            budget.step()
            val first = natural[index]
            val options = candidates.filter { it.range.start == first.range.start &&
                (it.id == first.id || it.interrupted && it.eligibility != RestEligibility.REJECTED) }
                .sortedWith(compareBy({ it.range.end }, { it.id }))
            for (rest in options) {
                budget.step()
                val nextIndex = natural.indexOfFirst { it.range.start >= rest.range.end }.takeIf { it >= 0 } ?: natural.size
                val unpaid = path.debts.filter { debt -> path.payments.none { it.debtId == debt.id } }
                for (choice in CompensationAllocator.choices(rest, unpaid, budget::step)) {
                    budget.step()
                    if (choice.reservations.isNotEmpty()) CompensationAllocator.validate(rest, choice, unpaid)
                    val amount = choice.base.sumOf { it.seconds }
                    val kind = when {
                        amount >= WeeklyTime.REGULAR -> WeeklyRestKind.REGULAR
                        amount >= WeeklyTime.REDUCED -> WeeklyRestKind.REDUCED
                        else -> null
                    }
                    val qualification = kind?.let { k -> rest.weekly.firstOrNull { it.kind == k && it.eligibility != RestEligibility.REJECTED } }
                    val baseRange = InputRange(choice.base.first().range.start, choice.base.last().range.end)
                    val restrictions = linkedSetOf<WeeklyRestriction>()
                    // A possible 24h weekly role is still subject to Art.8(6)/L04, including extras.
                    // It must not certify a new six-day budget merely because its duration fits.
                    if (kind == WeeklyRestKind.REDUCED) restrictions += WeeklyRestriction.L04_ASSIGNMENT_REVIEW_OUTSTANDING
                    val originScope = WeeklyTime.restrictions(WeeklyTime.coverage(snapshot, rest.range, "W01–W14"))
                    if (qualification != null) restrictions += originScope
                    if (qualification?.eligibility == RestEligibility.CONDITIONAL || rest.eligibility == RestEligibility.CONDITIONAL)
                        restrictions += WeeklyRestriction.REST_PREREQUISITE_UNCONFIRMED
                    if (!rest.actualEndProved) restrictions += WeeklyRestriction.OPEN_REST_END
                    val startProved = WeeklyTime.provedStart(snapshot, rest)
                    if (kind == WeeklyRestKind.REDUCED && !startProved) {
                        restrictions += WeeklyRestriction.LEFT_WEEKLY_HISTORY_MISSING
                        restrictions += WeeklyRestriction.REST_PREREQUISITE_UNCONFIRMED
                    }
                    restrictions += WeeklyTime.phaseRestrictions(baseRange)
                    if (choice.reservations.isNotEmpty()) restrictions += WeeklyRestriction.L04_ASSIGNMENT_REVIEW_OUTSTANDING
                    val weeks = if (qualification == null) emptyList() else WeeklyTime.weeks(baseRange)
                    if (weeks.size > 2) restrictions += WeeklyRestriction.L04_LONG_REST_PARTITION_UNCONFIRMED
                    // Calendar role is independent of the actual rest/end marker and the timing anchor.
                    // Counting an ordinary regular rest creates no debt and consumes no other rest.
                    // Its absent calendar role cannot improve any minimum-rest constraint.
                    val roles: List<Instant?> = if (kind == WeeklyRestKind.REGULAR && weeks.isNotEmpty()) weeks
                        else listOf(null) + weeks
                    for (week in roles) {
                        budget.step()
                        val use = if (qualification == null) null else WeeklyRestUse(
                            InputRevision.hash("m4-weekly-use") {
                                text(snapshot.profileId); text(rest.id); text(kind.name); nullable(week?.toString())
                                choice.base.forEach { instant(it.range.start); instant(it.range.end) }
                            }, rest.id, kind, frozen(choice.base), week, week == null, rest.actualEndProved, frozen(restrictions))
                        val debt = if (use?.kind == WeeklyRestKind.REDUCED && week != null &&
                            rest.actualEndProved && startProved) debt(snapshot, use, rest, week) else null
                        val daily = if (choice.reservations.isEmpty() && (rest.weekly.isEmpty() || originScope.isEmpty())) path.daily else {
                            // A daily-only base loses the compensation credit. For a weekly package
                            // Art.8(3)/Q08-II-6 permit the daily rest to extend into the whole packet.
                            val scopeLimits = originScope.map { if (it == WeeklyRestriction.SCOPE_EXCLUDED)
                                RestRestriction.SCOPE_EXCLUDED else RestRestriction.SCOPE_UNDETERMINED }
                            val weekly = if (use == null || qualification == null) emptyList() else listOf(qualification.let {
                                if (originScope.isEmpty()) it else it.copy(eligibility = RestEligibility.CONDITIONAL,
                                    restrictions = frozen((it.restrictions + scopeLimits).distinct())) })
                            path.daily + (rest.id to DailyRestAllocation(
                                if (use == null) frozen(choice.base) else rest.rest,
                                frozen(weekly), frozen((scopeLimits + if (choice.reservations.isNotEmpty())
                                    listOf(RestRestriction.L04_COMPENSATION_ASSIGNMENT_UNCONFIRMED) else emptyList()).distinct())))
                        }
                        yield(nextIndex to WeeklyAssignmentDraft(path.physical + rest,
                            path.uses + listOfNotNull(use), path.debts + listOfNotNull(debt),
                            path.payments + choice.reservations, daily))
                    }
                    // Specific Art.8(6a)(b) projection, not a general division of every long rest.
                    val previous = path.uses.lastOrNull()
                    val anchor = previous?.takeIf { it.kind == WeeklyRestKind.REGULAR && it.actualEndProved }
                        ?.let { p -> path.physical.lastOrNull { it.id == p.candidateId } }
                    if (anchor != null && !rest.interrupted && amount >= 69 * 3600L &&
                        WeeklyContexts.bus(snapshot, driving, anchor, rest.range.start, rest, budget::step).isNotEmpty()) {
                        val cut = baseRange.start.plusSeconds(WeeklyTime.REGULAR)
                        fun slice(range: InputRange): List<RestSlice> = frozen(choice.base.mapNotNull { source ->
                            val a = maxOf(source.range.start, range.start); val b = minOf(source.range.end, range.end)
                            if (a < b) source.copy(range = InputRange(a, b)) else null
                        })
                        val firstPart = slice(InputRange(baseRange.start, cut))
                        val secondPart = slice(InputRange(cut, baseRange.end))
                        val secondKind = if (secondPart.sumOf { it.seconds } >= WeeklyTime.REGULAR)
                            WeeklyRestKind.REGULAR else WeeklyRestKind.REDUCED
                        val limits = frozen((restrictions + WeeklyRestriction.BUS_POST_REST_PARTITION_UNCONFIRMED +
                            WeeklyRestriction.L04_ASSIGNMENT_REVIEW_OUTSTANDING + if (!startProved)
                            listOf(WeeklyRestriction.LEFT_WEEKLY_HISTORY_MISSING,
                                WeeklyRestriction.REST_PREREQUISITE_UNCONFIRMED) else emptyList()).distinct())
                        for (firstWeek in WeeklyTime.weeks(InputRange(baseRange.start, cut)))
                            for (secondWeek in WeeklyTime.weeks(InputRange(cut, baseRange.end))) {
                                budget.step()
                                fun part(indexPart: Int, kindPart: WeeklyRestKind, slices: List<RestSlice>, weekPart: Instant) =
                                    WeeklyRestUse(InputRevision.hash("m4-bus-post-rest-use") {
                                        text(rest.id); text(anchor.id); int(indexPart); text(kindPart.name); instant(weekPart)
                                        slices.forEach { instant(it.range.start); instant(it.range.end) }
                                    }, rest.id, kindPart, slices, weekPart, false,
                                        rest.actualEndProved && indexPart == 1, limits, anchor.id, indexPart)
                                val firstUse = part(0, WeeklyRestKind.REGULAR, firstPart, firstWeek)
                                val secondUse = part(1, secondKind, secondPart, secondWeek)
                                val debt = if (secondKind == WeeklyRestKind.REDUCED && rest.actualEndProved && startProved) {
                                    val origin = InputRevision.hash("m4-bus-debt-origin") { text(snapshot.profileId); text(rest.id); text(anchor.id) }
                                    val deficit = WeeklyTime.REGULAR - secondUse.seconds
                                    val due = WeeklyTime.due(WeeklyTime.week(rest.range.start))
                                    WeeklyCompensationDebt(InputRevision.hash("m4-bus-debt-variant") { text(origin); instant(secondWeek); long(deficit) },
                                        origin, rest.id, secondUse.range, secondWeek, deficit, due,
                                        WeeklyTime.law("W11", rest.range.start), frozen((limits + WeeklyRestriction.L02_UTC_WEEK_PROJECTION_ONLY).distinct()), true)
                                } else null
                                val daily = path.daily + (rest.id to DailyRestAllocation(rest.rest,
                                    rest.weekly.filter { it.kind == WeeklyRestKind.REGULAR }.map { q ->
                                        if (originScope.isEmpty()) q else q.copy(eligibility = RestEligibility.CONDITIONAL) },
                                    frozen((listOf(RestRestriction.L04_COMPENSATION_ASSIGNMENT_UNCONFIRMED) +
                                        if (originScope.isNotEmpty()) listOf(RestRestriction.SCOPE_UNDETERMINED) else emptyList()).distinct())))
                                yield(nextIndex to WeeklyAssignmentDraft(path.physical + rest, path.uses + listOf(firstUse, secondUse),
                                    path.debts + listOfNotNull(debt), path.payments + choice.reservations, daily))
                            }
                    }
                }
            }
        }
        // A long import must hit an explicit proof bound, never the JVM call-stack limit.
        // Lazy frames also avoid materialising a factorial compensation frontier in memory.
        val stack = java.util.ArrayDeque<Iterator<Pair<Int, WeeklyAssignmentDraft>>>()
        stack.addLast(sequenceOf(0 to WeeklyAssignmentDraft(emptyList(), emptyList(), emptyList(), emptyList(), emptyMap())).iterator())
        while (stack.isNotEmpty()) {
            budget.step()
            val frame = checkNotNull(stack.peekLast())
            if (!frame.hasNext()) { stack.removeLast(); continue }
            val (index, path) = frame.next()
            if (index == natural.size) accept(path) else stack.addLast(children(index, path).iterator())
        }
    }

    private fun debt(snapshot: ViolationInputSnapshot, use: WeeklyRestUse, rest: RestCandidate, week: Instant): WeeklyCompensationDebt {
        val origin = InputRevision.hash("m4-debt-origin") { text(snapshot.profileId); instant(rest.range.start) }
        val amount = WeeklyTime.REGULAR - use.seconds
        val id = InputRevision.hash("m4-debt-variant") { text(origin); instant(week); long(amount) }
        return WeeklyCompensationDebt(id, origin, rest.id, rest.range, week, amount,
            WeeklyTime.due(week), WeeklyTime.law("W04", use.range.start),
            frozen((use.restrictions + WeeklyRestriction.L02_UTC_WEEK_PROJECTION_ONLY +
                WeeklyRestriction.L04_ASSIGNMENT_REVIEW_OUTSTANDING).distinct()))
    }
}
