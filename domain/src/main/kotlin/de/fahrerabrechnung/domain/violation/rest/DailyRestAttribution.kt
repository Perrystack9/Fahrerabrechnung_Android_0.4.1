package de.fahrerabrechnung.domain.violation.rest

import de.fahrerabrechnung.domain.violation.input.*

/** M4's derived reservation, separate from the unchanged physical activity/rest evidence. */
data class DailyRestAllocation(val credit: List<RestSlice>, val weekly: List<WeeklyRestQualification>,
    val restrictions: List<RestRestriction>, val reducedWeeklyResetProved: Boolean = false)

data class DailyRestAttribution(val candidateIds: Set<String>, val allocations: Map<String, DailyRestAllocation>) {
    val revision: String get() = InputRevision.hash("m4-daily-rest-attribution") {
        candidateIds.sorted().forEach { text(it) }
        allocations.toSortedMap().forEach { (id, allocation) ->
            text(id); allocation.credit.forEach { instant(it.range.start); instant(it.range.end) }
            allocation.weekly.forEach { text(it.kind.name); text(it.eligibility.name) }
            allocation.restrictions.sortedBy { it.name }.forEach { text(it.name) }
            bool(allocation.reducedWeeklyResetProved)
        }
    }
    internal fun apply(candidates: List<RestCandidate>): List<RestCandidate> {
        require(candidateIds.all { id -> candidates.any { it.id == id } })
        require(allocations.keys.all { it in candidateIds })
        return frozen(candidates.filter { it.id in candidateIds }.map { candidate ->
            val allocation = allocations[candidate.id] ?: return@map candidate
            require(allocation.credit.isNotEmpty())
            require(allocation.credit.zipWithNext().all { (a, b) -> a.range.end <= b.range.start })
            require(allocation.credit.all { slice -> candidate.rest.any {
                it.range.start <= slice.range.start && it.range.end >= slice.range.end &&
                    it.support == slice.support && it.manualTachograph == slice.manualTachograph } })
            candidate.copy(allocation = allocation,
                restrictions = frozen((candidate.restrictions + allocation.restrictions).distinct()))
        })
    }
}
