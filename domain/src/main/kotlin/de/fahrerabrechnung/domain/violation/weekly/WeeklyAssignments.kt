package de.fahrerabrechnung.domain.violation.weekly

import de.fahrerabrechnung.domain.violation.input.*
import de.fahrerabrechnung.domain.violation.rest.*
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
    fun enumerate(snapshot: ViolationInputSnapshot, candidates: List<RestCandidate>, budget: WeeklySearchBudget,
        accept: (WeeklyAssignmentDraft) -> Unit) {
        val natural = candidates.filterNot { it.interrupted }.sortedBy { it.range.start }
        fun visit(index: Int, path: WeeklyAssignmentDraft) {
            budget.step()
            if (index == natural.size) { accept(path); return }
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
                    if (qualification?.eligibility == RestEligibility.CONDITIONAL || rest.eligibility == RestEligibility.CONDITIONAL)
                        restrictions += WeeklyRestriction.REST_PREREQUISITE_UNCONFIRMED
                    if (!rest.actualEndProved) restrictions += WeeklyRestriction.OPEN_REST_END
                    restrictions += WeeklyTime.phaseRestrictions(baseRange)
                    if (choice.reservations.isNotEmpty()) restrictions += WeeklyRestriction.L04_ASSIGNMENT_REVIEW_OUTSTANDING
                    val weeks = if (qualification == null) emptyList() else WeeklyTime.weeks(baseRange)
                    if (weeks.size > 2) restrictions += WeeklyRestriction.L04_LONG_REST_PARTITION_UNCONFIRMED
                    // Calendar role is independent of the actual rest/end marker and the timing anchor.
                    // Counting an ordinary regular rest creates no debt and consumes no other rest.
                    // Its absent calendar role cannot improve any minimum-rest constraint.
                    val roles: List<Instant?> = if (kind == WeeklyRestKind.REGULAR && weeks.isNotEmpty()) weeks.take(2)
                        else listOf(null) + weeks.take(2)
                    for (week in roles) {
                        budget.step()
                        val use = if (qualification == null || kind == null) null else WeeklyRestUse(
                            InputRevision.hash("m4-weekly-use") {
                                text(snapshot.profileId); text(rest.id); text(kind.name); nullable(week?.toString())
                                choice.base.forEach { instant(it.range.start); instant(it.range.end) }
                            }, rest.id, kind, frozen(choice.base), week, week == null, rest.actualEndProved, frozen(restrictions))
                        val debt = if (use?.kind == WeeklyRestKind.REDUCED && week != null &&
                            rest.actualEndProved) debt(snapshot, use, rest, week) else null
                        val daily = if (choice.reservations.isEmpty()) path.daily else {
                            // A daily-only base loses the compensation credit. For a weekly package
                            // Art.8(3)/Q08-II-6 permit the daily rest to extend into the whole packet.
                            val weekly = if (use == null || qualification == null) emptyList() else listOf(qualification)
                            path.daily + (rest.id to DailyRestAllocation(
                                if (use == null) frozen(choice.base) else rest.rest,
                                frozen(weekly), listOf(RestRestriction.L04_COMPENSATION_ASSIGNMENT_UNCONFIRMED)))
                        }
                        visit(nextIndex, WeeklyAssignmentDraft(path.physical + rest,
                            path.uses + listOfNotNull(use), path.debts + listOfNotNull(debt),
                            path.payments + choice.reservations, daily))
                    }
                }
            }
        }
        visit(0, WeeklyAssignmentDraft(emptyList(), emptyList(), emptyList(), emptyList(), emptyMap()))
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
