package de.fahrerabrechnung.domain.violation.weekly

import de.fahrerabrechnung.domain.violation.driving.*
import de.fahrerabrechnung.domain.violation.input.*
import de.fahrerabrechnung.domain.violation.rest.*

/** One read-only M1/M2/M3 consumer, with explicit bounds and no wall clock/display timezone. */
object WeeklyRestCalculator {
    fun calculate(snapshot: ViolationInputSnapshot, driving: DrivingCalculation? = null,
        daily: DailyRestCalculation? = null, maxSearchStates: Int = 50000,
        maxAllocations: Int = 2048, maxDailyAttributions: Int = 64,
        checkCancellation: () -> Unit = {}): WeeklyRestCalculation {
        require(maxSearchStates > 0 && maxAllocations > 0 && maxDailyAttributions > 0)
        val drivingResult = driving ?: DrivingCalculator.calculate(snapshot, checkCancellation = checkCancellation)
        val dailyResult = daily ?: DailyRestCalculator.calculate(snapshot, drivingResult, checkCancellation = checkCancellation)
        require(drivingResult.sourceSnapshot == snapshot && dailyResult.sourceSnapshot == snapshot)
        require(dailyResult.drivingRevision == drivingResult.revision)
        // Source candidates, never a credit projection imported from another allocation.
        require(dailyResult.candidates.all { it.allocation == null })
        val budget = WeeklySearchBudget(maxSearchStates, checkCancellation)
        val evaluated = linkedMapOf<String, WeeklyEvaluation>()
        val issues = mutableListOf<WeeklyRestIssue>()
        if (dailyResult.candidates.any { it.netRestSeconds >= WeeklyTime.REDUCED && WeeklyTime.weeks(it.range).size > 2 })
            issues += WeeklyRestIssue(setOf("W03", "W09"), "LONG_REST_PARTITION_REQUIRES_L04")
        var complete = true
        try {
            WeeklyAssignments.enumerate(snapshot, drivingResult, dailyResult.candidates, budget) { path ->
                checkCancellation()
                val evaluation = WeeklyRules.evaluate(snapshot, drivingResult, path, checkCancellation)
                evaluated[evaluation.allocation.id] = evaluation
                if (evaluated.size > maxAllocations) throw WeeklySearchLimit()
            }
        } catch (_: WeeklySearchLimit) {
            complete = false
            issues += WeeklyRestIssue(setOf("W01–W14"), "ALLOCATION_SEARCH_LIMIT_NO_VERDICT")
        }
        // Solve week roles, compensation placement and timing TOGETHER. No priority of a calendar
        // rule over a timing/compensation rule. A fixed failure shared by ALL possibilities stays
        // documented but cannot erase unrelated later obligations. No violation-count objective.
        val jointWitnesses = witnesses(evaluated.values.toList()) { value ->
            value.weekConstraints.map { "week:$it" }.toSet() + value.jointConstraints
        }
        val retained = if (!complete || jointWitnesses.isEmpty()) evaluated.values.toList() else jointWitnesses
        val globalWitness = jointWitnesses.any { it.weekConstraints.isEmpty() && it.jointConstraints.isEmpty() }
        if (complete && retained.isNotEmpty() && !globalWitness)
            issues += WeeklyRestIssue(setOf("W01", "W04", "W07", "W08", "W09", "W11"), "NO_GLOBAL_ASSIGNMENT_WITNESS")
        val attributionVariants = retained.map { it.allocation.dailyCredit }.distinctBy { it.revision }
        val dailyVariants = mutableListOf<DailyRestCalculation>()
        if (attributionVariants.size > maxDailyAttributions) {
            complete = false
            issues += WeeklyRestIssue(setOf("W09", "R01–R07"), "DAILY_ATTRIBUTION_SEARCH_LIMIT_NO_VERDICT")
        } else for (attribution in attributionVariants) {
            checkCancellation()
            dailyVariants += if (attribution.allocations.isEmpty() && attribution.candidateIds == dailyResult.candidates.map { it.id }.toSet())
                dailyResult else DailyRestCalculator.calculate(snapshot, drivingResult,
                    checkCancellation = checkCancellation, attribution = attribution)
        }
        val combinedDaily = DailyRestAttributions.combine(dailyResult, dailyVariants, complete)
        val allocations = retained.map { it.allocation.copy(restrictions = frozen((it.allocation.restrictions +
            if (!complete) listOf(WeeklyRestriction.COMPUTATION_LIMIT_REACHED) else if (!globalWitness)
                listOf(WeeklyRestriction.NO_GLOBAL_ASSIGNMENT_WITNESS) else emptyList()).distinct())) }
        val deadlines = retained.flatMap { it.deadlines }.groupBy { it.id }.map { (id, group) ->
            val same = group.map { listOf(it.deadline, it.nextBaseStart, it.lateBySeconds) }.distinct().size == 1
            val every = retained.all { result -> result.deadlines.any { it.id == id } }
            val selected = group.minBy { it.lateBySeconds }
            selected.copy(restrictions = frozen((group.flatMap { it.restrictions } +
                if (!complete) listOf(WeeklyRestriction.COMPUTATION_LIMIT_REACHED) else if (!same || !every)
                    listOf(WeeklyRestriction.ALTERNATIVE_ASSIGNMENT_UNRESOLVED) else emptyList()).distinct()))
        }
        val findings = retained.flatMap { it.findings }.groupBy { it.id }.map { (id, group) ->
            val same = group.map { it.excessOrMissingSeconds to it.mathematicalOnset }.distinct().size == 1
            val every = retained.all { result -> result.findings.any { it.id == id } }
            val selected = group.minBy { it.excessOrMissingSeconds }
            selected.copy(presentInEveryRetainedPath = complete && every,
                mathematicalOnset = selected.mathematicalOnset.takeIf { point -> group.all { it.mathematicalOnset == point } },
                sourceCandidateIds = frozen(group.flatMap { it.sourceCandidateIds }.distinct()),
                allocationIds = frozen(group.flatMap { it.allocationIds }.distinct()),
                restrictions = frozen((group.flatMap { it.restrictions } + if (!complete)
                    listOf(WeeklyRestriction.COMPUTATION_LIMIT_REACHED) else if (!same || !every)
                    listOf(WeeklyRestriction.ALTERNATIVE_ASSIGNMENT_UNRESOLVED) else emptyList()).distinct()))
        }
        val needs = retained.flatMap { result -> result.allocation.internationalPairs.flatMap { it.needs } +
            result.allocation.buses.flatMap { it.needs } } + combinedDaily.contextNeeds
        if (dailyResult.candidates.none { it.weekly.any { q -> q.eligibility != RestEligibility.REJECTED } })
            issues += WeeklyRestIssue(setOf("W02", "W14"), "NO_PROVED_PREVIOUS_WEEKLY_REST")
        snapshot.issues.forEach { issues += WeeklyRestIssue(setOf("W01–W14"), "INPUT_${it.code.name}", it.range) }
        val revision = InputRevision.hash("m4-weekly-rest-calculation") {
            text(WEEKLY_REST_INTERPRETATION); text(snapshot.revision); text(drivingResult.revision); text(dailyResult.revision)
            int(maxSearchStates); int(maxAllocations); int(maxDailyAttributions); bool(complete)
            allocations.forEach { text(it.id) }; text(combinedDaily.revision)
        }
        checkCancellation()
        return WeeklyRestCalculation(snapshot.profileId, snapshot.revision, snapshot.dataRevision, snapshot.provenanceRevision,
            drivingResult.revision, dailyResult.revision, revision, snapshot.latestClosedEnd, dailyResult.candidates,
            frozen(allocations), frozen(deadlines.sortedWith(compareBy({ it.anchorActualEnd }, { it.id }))),
            frozen(findings.sortedWith(compareBy({ it.range.start }, { it.id }))),
            frozen(needs.distinctBy { it.id }.sortedBy { it.id }), frozen(issues.distinct()), complete,
            combinedDaily, snapshot)
    }

    private fun witnesses(values: List<WeeklyEvaluation>, constraints: (WeeklyEvaluation) -> Set<String>): List<WeeklyEvaluation> {
        if (values.isEmpty()) return emptyList()
        val sharedFailures = values.map(constraints).reduce { a, b -> a.intersect(b) }
        return values.filter { (constraints(it) - sharedFailures).isEmpty() }
    }
}
