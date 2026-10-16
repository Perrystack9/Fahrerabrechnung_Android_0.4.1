package de.fahrerabrechnung.domain.violation.weekly

import de.fahrerabrechnung.domain.violation.input.*
import de.fahrerabrechnung.domain.violation.rest.*
import java.time.Duration

internal data class CompensationChoice(val base: List<RestSlice>, val reservations: List<CompensationReservation>)

/** Enumerates disjoint whole blocks; no FIFO, splitting of a debt or sharing a minute. */
internal object CompensationAllocator {
    fun choices(rest: RestCandidate, debts: List<WeeklyCompensationDebt>, check: () -> Unit): Sequence<CompensationChoice> = sequence {
        yield(CompensationChoice(rest.rest, emptyList()))
        // The base ferry qualification comes from M3. A ferry interruption is not continuous compensation.
        // Until a separate base/compensation/ferry attribution is proved, do not manufacture such credit.
        if (rest.interrupted || !rest.actualEndProved || rest.eligibility == RestEligibility.REJECTED) return@sequence
        val usable = debts.filter { it.originRange.end <= rest.range.start }
        val minimumBase = 9 * 3600L
        val available = rest.netRestSeconds - minimumBase
        suspend fun SequenceScope<CompensationChoice>.walk(selected: List<WeeklyCompensationDebt>, remaining: List<WeeklyCompensationDebt>, amount: Long) {
            check()
            if (selected.isNotEmpty()) {
                // Base can be between complete debt blocks. Both orders are retained where deadlines differ.
                for (position in 0..selected.size) {
                    check()
                    val before = selected.take(position).sumOf { it.seconds }
                    val after = selected.drop(position).sumOf { it.seconds }
                    val baseRange = InputRange(rest.range.start.plusSeconds(before), rest.range.end.minusSeconds(after))
                    val base = rest.rest.mapNotNull { slice ->
                        val a = maxOf(baseRange.start, slice.range.start); val b = minOf(baseRange.end, slice.range.end)
                        if (a < b) slice.copy(range = InputRange(a, b)) else null
                    }
                    var at = rest.range.start
                    val payments = mutableListOf<CompensationReservation>()
                    selected.forEachIndexed { index, debt ->
                        if (index == position) at = baseRange.end
                        val end = at.plusSeconds(debt.seconds)
                        payments += CompensationReservation(debt.id, rest.id, InputRange(at, end), baseRange, debt.due,
                            frozen((debt.restrictions + WeeklyRestriction.L04_ASSIGNMENT_REVIEW_OUTSTANDING).distinct()))
                        at = end
                    }
                    yield(CompensationChoice(frozen(base), frozen(payments)))
                }
            }
            for (debt in remaining) {
                check()
                if (amount + debt.seconds <= available)
                    walk(selected + debt, remaining.filterNot { it.id == debt.id }, amount + debt.seconds)
            }
        }
        walk(emptyList(), usable, 0)
    }

    fun validate(rest: RestCandidate, choice: CompensationChoice, debts: List<WeeklyCompensationDebt>) {
        require(choice.base.isNotEmpty() && choice.base.sumOf { it.seconds } >= 32400)
        val amounts = debts.associateBy { it.id }
        require(choice.reservations.map { it.debtId }.distinct().size == choice.reservations.size)
        val ranges = choice.base.map { it.range } + choice.reservations.map { it.range }
        require(ranges.sortedBy { it.start }.zipWithNext().all { (a, b) -> a.end <= b.start })
        require(ranges.all { it.start >= rest.range.start && it.end <= rest.range.end })
        require(ranges.sumOf { Duration.between(it.start, it.end).seconds } == rest.netRestSeconds)
        choice.reservations.forEach { require(it.seconds == amounts.getValue(it.debtId).seconds) }
    }
}
