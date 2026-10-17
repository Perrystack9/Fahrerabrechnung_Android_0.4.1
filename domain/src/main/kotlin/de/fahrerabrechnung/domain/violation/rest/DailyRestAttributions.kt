package de.fahrerabrechnung.domain.violation.rest

import de.fahrerabrechnung.domain.violation.input.*

/** Combines runs of the existing M3 calculator, preserving correlated M4 allocation uncertainty. */
internal object DailyRestAttributions {
    fun combine(base: DailyRestCalculation, variants: List<DailyRestCalculation>,
        complete: Boolean): DailyRestCalculation {
        val actual = variants.ifEmpty { listOf(base) }.distinctBy { it.revision }
        require(actual.all { it.sourceSnapshot == base.sourceSnapshot && it.drivingRevision == base.drivingRevision })
        if (actual.size == 1 && complete) return actual.single()
        val incomplete = !complete || actual.any { !it.alternativesComplete }
        val revision = InputRevision.hash("m4-correlated-daily-rest") {
            text(base.inputRevision); bool(complete); actual.map { it.revision }.sorted().forEach { text(it) }
        }
        fun restrictions(values: List<RestRestriction>, differs: Boolean): List<RestRestriction> = frozen((values +
            if (incomplete) listOf(RestRestriction.COMPUTATION_LIMIT_REACHED)
            else if (differs) listOf(RestRestriction.ALTERNATIVE_ASSIGNMENT_UNRESOLVED) else emptyList()).distinct())
        val windows = actual.flatMap { it.windows }.groupBy { it.id }.map { (id, group) ->
            val present = actual.all { v -> v.windows.any { it.id == id } }
            val selected = group.minBy { it.creditedSeconds }
            selected.copy(restrictions = restrictions(group.flatMap { it.restrictions }, !present || group.distinct().size > 1))
        }
        val findings = actual.flatMap { it.findings }.groupBy { it.id }.map { (id, group) ->
            val present = actual.all { v -> v.findings.any { it.id == id && it.presentInEveryRetainedPath } }
            val same = group.map { listOf(it.missingRestSeconds, it.creditedRestSeconds, it.optimisticAdditionalRestSeconds) }.distinct().size == 1
            val selected = group.minBy { it.missingRestSeconds }
            selected.copy(presentInEveryRetainedPath = present && complete,
                quantityIsProved = group.all { it.quantityIsProved } && same,
                quantityIsExact = group.all { it.quantityIsExact } && same,
                mathematicalOnset = selected.mathematicalOnset.takeIf { point -> group.all { it.mathematicalOnset == point } },
                firstProvableAt = group.mapNotNull { it.firstProvableAt }.maxOrNull().takeIf { group.all { it.firstProvableAt != null } },
                affectedRange = selected.affectedRange.takeIf { range -> group.all { it.affectedRange == range } },
                restrictions = restrictions(group.flatMap { it.restrictions }, !present || !same),
                assessmentWindowIds = frozen(group.flatMap { it.assessmentWindowIds }.distinct().sorted()))
        }
        val counters = actual.flatMap { it.finalCounters }.distinct()
        val differentCounters = counters.map { it.minimumObservedCount to it.maximumPossibleCount }.distinct().size > 1
        return base.copy(revision = revision, anchors = frozen(actual.flatMap { it.anchors }.distinct()),
            windows = frozen(windows.sortedWith(compareBy({ it.referenceStart }, { it.id }))),
            findings = frozen(findings.sortedWith(compareBy({ it.assessedRange.start }, { it.id }))),
            finalCounters = frozen(counters.map { it.copy(restrictions = restrictions(it.restrictions, differentCounters)) }),
            contextNeeds = frozen(actual.flatMap { it.contextNeeds }.distinctBy { it.id }.sortedBy { it.id }),
            issues = frozen((actual.flatMap { it.issues } + if (incomplete)
                listOf(DailyRestIssue(setOf("R01–R07", "W09"), "M4_ATTRIBUTION_SEARCH_INCOMPLETE_NO_VERDICT")) else emptyList()).distinct()),
            alternativesComplete = !incomplete)
    }
}
