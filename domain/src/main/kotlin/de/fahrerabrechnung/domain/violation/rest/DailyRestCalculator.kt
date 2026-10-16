package de.fahrerabrechnung.domain.violation.rest

import de.fahrerabrechnung.domain.violation.driving.DrivingCalculation
import de.fahrerabrechnung.domain.violation.driving.DrivingCalculator
import de.fahrerabrechnung.domain.violation.input.*
import java.time.Instant
import java.util.TreeMap

/** One chronological M1/M2 consumer. No import, database, display calendar or wall clock. */
object DailyRestCalculator {
    private data class Path(
        val start: Instant, val anchor: RestAnchor, val counter: DailyReductionCounter,
        val serviceUses: Map<String, Int>, val historyFrom: Instant,
        val methodRestrictions: List<RestRestriction>, val mustFindings: Set<String>,
    ) {
        val key: String get() = InputRevision.hash("m3-path") {
            instant(start); text(anchor.candidateId); text(anchor.method.name)
            int(counter.minimumObservedCount); nullable(counter.maximumPossibleCount?.toString())
            nullable(counter.weeklyAnchor?.candidateId); counter.reductions.forEach { text(it.id) }
            counter.restrictions.sortedBy { it.name }.forEach { text(it.name) }
            methodRestrictions.sortedBy { it.name }.forEach { text(it.name) }
            serviceUses.toSortedMap().forEach { (key, count) -> text(key); int(count) }
        }
    }

    fun calculate(snapshot: ViolationInputSnapshot, driving: DrivingCalculation? = null,
        maxPathEvaluations: Int = 20000, checkCancellation: () -> Unit = {}): DailyRestCalculation {
        val drivingResult = driving ?: DrivingCalculator.calculate(snapshot, checkCancellation = checkCancellation)
        require(maxPathEvaluations > 0)
        require(drivingResult.inputRevision == snapshot.revision && drivingResult.sourceSnapshot == snapshot)
        require(snapshot.intervals.zipWithNext().all { (a, b) -> a.range.end <= b.range.start })
        val candidates = RestCandidates.build(snapshot, checkCancellation)
        val byId = candidates.associateBy { it.id }
        val anchors = linkedSetOf<RestAnchor>()
        val windows = linkedMapOf<String, DailyRestWindow>()
        val findings = linkedMapOf<String, DailyRestFinding>()
        val issues = mutableListOf<DailyRestIssue>()
        val needs = mutableListOf<ContextNeed>()
        val finals = mutableListOf<DailyReductionCounter>()
        val terminalFindings = mutableListOf<Set<String>>()
        val frontier = TreeMap<Instant, MutableMap<String, Path>>()
        val observed = snapshot.latestClosedEnd
        var complete = true
        fun enqueue(path: Path) {
            checkCancellation()
            val sameTime = frontier.getOrPut(path.start) { linkedMapOf() }
            val old = sameTime[path.key]
            sameTime[path.key] = if (old == null) path else old.copy(mustFindings = old.mustFindings.intersect(path.mustFindings))
        }
        fun terminal(path: Path) { finals += path.counter; terminalFindings += path.mustFindings }
        val initial = candidates.filter { it.actualEndProved && it.eligibility != RestEligibility.REJECTED &&
            it.netRestSeconds >= if (it.interrupted) 39600 else 32400 }
        val firstEnd = initial.minOfOrNull { it.range.end }
        if (firstEnd == null || observed == null) {
            issues += DailyRestIssue(setOf("R01", "R04"), "NO_PROVED_PREVIOUS_DAILY_OR_WEEKLY_REST_END")
        } else for (candidate in initial.filter { it.range.end == firstEnd }) {
            val anchor = RestAnchor(candidate.id, candidate.range, RestAnchorMethod.ACTUAL_REST_END, null, candidate.restrictions)
            anchors += anchor
            val unknown = DailyReductionCounter(null, 0, null, emptyList(), listOf(RestRestriction.LEFT_REDUCTION_HISTORY_MISSING))
            val counter = afterWeekly(candidate, anchor, unknown)
            enqueue(Path(anchor.at, anchor, counter, emptyMap(), anchor.at, candidate.restrictions, emptySet()))
        }
        var evaluated = 0
        traversal@ while (frontier.isNotEmpty()) {
            checkCancellation()
            if (evaluated >= maxPathEvaluations) {
                complete = false
                issues += DailyRestIssue(setOf("R01", "R02", "R03", "R04", "R05", "R06"), "ALTERNATIVE_SEARCH_LIMIT_NO_VERDICT")
                break
            }
            val paths = frontier.pollFirstEntry().value.values
            for (path in paths) {
                checkCancellation()
                if (evaluated >= maxPathEvaluations) {
                    complete = false
                    issues += DailyRestIssue(setOf("R01–R07"), "ALTERNATIVE_SEARCH_LIMIT_NO_VERDICT")
                    break@traversal
                }
                evaluated++
                if (observed == null || path.start >= observed) { terminal(path); continue }
                val choices = RestContexts.modes(snapshot, drivingResult, path.start, observed, candidates, path.serviceUses, path.historyFrom)
                val evaluatedWindows = choices.map { choice -> choice to RestWindowEvaluator.evaluate(snapshot, path.start,
                    observed, path.anchor, choice, path.counter, candidates, path.methodRestrictions, checkCancellation) }
                val standard = evaluatedWindows.firstOrNull { it.first.mode == RestWindowMode.SOLO_24 }?.second
                // A proved legal route supersedes the arithmetic failure of a shorter inapplicable window.
                val fulfilled = evaluatedWindows.filter { it.second.options.any { o -> o.eligibility == RestEligibility.PROVED } }
                val retained = if (fulfilled.isEmpty()) evaluatedWindows else fulfilled
                for ((choice, window) in retained) {
                    checkCancellation()
                    windows[window.id] = window
                    if (choice.mode != RestWindowMode.SOLO_24 && standard?.state != RestWindowState.FULFILLED &&
                        (window.options.isNotEmpty() || window.potentialDeficitSeconds < (standard?.potentialDeficitSeconds ?: 0)))
                        needs += choice.needs
                    for (option in window.options) option.candidates.mapNotNull(byId::get).forEach { needs += it.needs }
                    var must = path.mustFindings
                    val through = minOf(window.deadline, observed)
                    if (window.potentialDeficitSeconds > 0 && window.coverage.factualPrerequisitesCovered &&
                        window.coverage.excludedScope.isEmpty() && RestRestriction.BEFORE_561_TEMPORAL_SCOPE !in window.restrictions) {
                        val id = InputRevision.hash("m3-normal-rest-deficit") {
                            text(DAILY_REST_INTERPRETATION); text(snapshot.profileId); instant(window.referenceStart)
                            text(window.mode.name); long(window.minimumRequiredSeconds); text(window.law.id)
                        }
                        val onset = RestWindowEvaluator.onset(window, choice, candidates, checkCancellation)
                        findings[id] = DailyRestFinding(id, window.id, window.law, InputRange(window.referenceStart, through),
                            window.potentialDeficitSeconds, window.minimumRequiredSeconds, window.creditedSeconds,
                            onset?.first, onset?.second, onset?.first?.takeIf { it < through }?.let { InputRange(it, through) },
                            false, window.restrictions.isEmpty(), window.restrictions, snapshot.revision,
                            (window.potentialRestCapacitySeconds - window.creditedSeconds).coerceAtLeast(0), observed >= window.deadline)
                        must = must + id
                    }
                    if (window.options.isNotEmpty()) {
                        for (option in window.options) {
                            checkCancellation()
                            val last = byId.getValue(option.candidates.last())
                            val counter = nextCounter(path, window, option, last)
                            val uses = if (choice.mode == RestWindowMode.OCCASIONAL_BUS_25 && choice.serviceId != null &&
                                last.restSecondsThrough(path.start.plusSeconds(86400)) <
                                    (if (option.kind == DailyRestKind.SPLIT_REGULAR) 32400 else 39600))
                                path.serviceUses + (choice.serviceId to ((path.serviceUses[choice.serviceId] ?: 0) + 1)) else path.serviceUses
                            if (option.actualEnd == null) {
                                val next = path.copy(counter = counter, serviceUses = uses, mustFindings = must)
                                if (!resumeAfterUncertainEnd(next, last.range.end, candidates, ::enqueue, anchors)) terminal(next)
                            } else {
                                val anchor = RestAnchor(last.id, last.range, RestAnchorMethod.ACTUAL_REST_END, true, option.restrictions)
                                anchors += anchor
                                enqueue(Path(anchor.at, anchor, counter, uses, path.historyFrom, option.restrictions, must))
                            }
                        }
                    } else if (through < window.deadline) {
                        terminal(path.copy(counter = counterAfterCoverage(path.counter, window), mustFindings = must))
                    } else {
                        val minimum = if (choice.mode == RestWindowMode.CREW_30 || path.counter.minimumObservedCount < 3) 32400 else 39600
                        val crossing = candidates.filter { it.range.start < window.deadline && it.range.end > window.deadline &&
                            it.eligibility != RestEligibility.REJECTED && it.netRestSeconds >= if (it.interrupted) 39600 else minimum }
                        val firstLateEnd = crossing.filter { it.actualEndProved }.minOfOrNull { it.range.end }
                        if (firstLateEnd != null) for (rest in crossing.filter { it.actualEndProved && it.range.end == firstLateEnd }) {
                            val restrictions = frozen((rest.restrictions + RestRestriction.Q07_SUCCESSIVE_WINDOW_INTERPRETATION_OPEN).distinct())
                            val anchor = RestAnchor(rest.id, rest.range, RestAnchorMethod.Q07_LATE_REST_END, false, restrictions)
                            anchors += anchor
                            val counter = afterWeekly(rest, anchor, counterAfterCoverage(path.counter, window))
                            enqueue(Path(anchor.at, anchor, counter, path.serviceUses, path.historyFrom, restrictions, must))
                        } else if (candidates.any { !it.actualEndProved && it.range.start < window.deadline && it.range.end >= observed &&
                                it.eligibility != RestEligibility.REJECTED }) {
                            terminal(path.copy(counter = counterAfterCoverage(path.counter, window), mustFindings = must))
                        } else enqueue(path.copy(start = window.deadline, counter = counterAfterCoverage(path.counter, window),
                            methodRestrictions = frozen((path.methodRestrictions + RestRestriction.Q07_SUCCESSIVE_WINDOW_INTERPRETATION_OPEN).distinct()),
                            mustFindings = must))
                    }
                }
            }
        }
        val mandatory = if (!complete || terminalFindings.isEmpty()) emptySet() else terminalFindings.reduce(Set<String>::intersect)
        val resultFindings = findings.values.map { it.copy(presentInEveryRetainedPath = it.id in mandatory,
            restrictions = frozen((it.restrictions + if (!complete) listOf(RestRestriction.COMPUTATION_LIMIT_REACHED)
                else if (it.id !in mandatory) listOf(RestRestriction.ALTERNATIVE_ASSIGNMENT_UNRESOLVED) else emptyList()).distinct())) }
        val openVariants = windows.values.filter { it.state == RestWindowState.OPEN }.map {
            listOf(it.referenceStart.toString(), it.mode.name, it.counterBefore.minimumObservedCount.toString(),
                it.counterBefore.maximumPossibleCount.toString()) }.distinct().size
        val resultWindows = windows.values.map {
            it.copy(restrictions = frozen((it.restrictions + if (!complete) listOf(RestRestriction.COMPUTATION_LIMIT_REACHED)
                else if (it.state == RestWindowState.OPEN && openVariants > 1)
                    listOf(RestRestriction.ALTERNATIVE_ASSIGNMENT_UNRESOLVED) else emptyList()).distinct()))
        }
        val finalCounters = finals.distinct().let { counters ->
            val distinctQuantities = counters.map { it.minimumObservedCount to it.maximumPossibleCount }.distinct().size
            counters.map { it.copy(restrictions = frozen((it.restrictions +
                if (!complete) listOf(RestRestriction.COMPUTATION_LIMIT_REACHED) else if (distinctQuantities > 1)
                    listOf(RestRestriction.ALTERNATIVE_ASSIGNMENT_UNRESOLVED) else emptyList()).distinct())) }
        }
        snapshot.issues.forEach { issues += DailyRestIssue(setOf("R01–R07", "F01–F05"), "INPUT_" + it.code.name, it.range) }
        val revision = InputRevision.hash("m3-daily-rest-calculation") {
            text(DAILY_REST_INTERPRETATION); text(snapshot.revision); text(drivingResult.revision); int(maxPathEvaluations)
        }
        checkCancellation()
        return DailyRestCalculation(snapshot.profileId, snapshot.revision, snapshot.dataRevision, snapshot.provenanceRevision,
            drivingResult.revision, revision, observed, candidates, frozen(anchors.sortedWith(compareBy({ it.at }, { it.candidateId }))),
            frozen(resultWindows.sortedWith(compareBy({ it.referenceStart }, { it.mode.name }, { it.id }))),
            frozen(resultFindings.sortedWith(compareBy({ it.assessedRange.start }, { it.id }))), frozen(finalCounters),
            frozen(needs.distinctBy { it.id }.sortedBy { it.id }), frozen(issues.distinct()), complete, snapshot)
    }

    private fun counterAfterCoverage(counter: DailyReductionCounter, window: DailyRestWindow): DailyReductionCounter =
        if (window.coverage.factualPrerequisitesCovered && window.coverage.excludedScope.isEmpty()) counter else
            DailyReductionCounter(counter.weeklyAnchor, 0, null, counter.reductions,
                listOf(RestRestriction.LEFT_REDUCTION_HISTORY_MISSING, RestRestriction.ACTIVITY_GAP))

    private fun nextCounter(path: Path, window: DailyRestWindow, option: DailyRestOption,
        rest: RestCandidate): DailyReductionCounter {
        var counter = counterAfterCoverage(path.counter, window)
        if (option.consumesReduction && window.coverage.factualPrerequisitesCovered && window.coverage.excludedScope.isEmpty()) {
            val id = InputRevision.hash("m3-daily-reduction") {
                instant(window.referenceStart); text(rest.id)
            }
            val pending = RestRestriction.OPEN_REST_CLASSIFICATION_PENDING in option.restrictions
            counter = counter.copy(minimumObservedCount = counter.minimumObservedCount + if (pending) 0 else 1,
                maximumPossibleCount = counter.maximumPossibleCount?.plus(1),
                reductions = if (pending) counter.reductions else frozen(counter.reductions +
                    DailyRestReduction(id, rest.id, window.referenceStart, option.netRestSecondsInWindow)))
        }
        if (option.kind == DailyRestKind.CREW_NINE_HOURS) counter = counter.copy(
            maximumPossibleCount = counter.maximumPossibleCount?.plus(1),
            restrictions = frozen((counter.restrictions + RestRestriction.L09_CREW_REDUCTION_ACCOUNTING_UNCONFIRMED).distinct()))
        if (option.restrictions.isNotEmpty()) counter = counter.copy(restrictions = frozen((counter.restrictions + option.restrictions).distinct()))
        val anchor = RestAnchor(rest.id, rest.range, RestAnchorMethod.ACTUAL_REST_END, true, option.restrictions)
        return if (window.coverage.factualPrerequisitesCovered && window.coverage.excludedScope.isEmpty())
            afterWeekly(rest, anchor, counter) else counter
    }

    private fun afterWeekly(rest: RestCandidate, anchor: RestAnchor, counter: DailyReductionCounter): DailyReductionCounter {
        if (!rest.actualEndProved) return counter
        if (rest.weekly.any { it.kind == WeeklyRestKind.REGULAR && it.eligibility == RestEligibility.PROVED })
            return DailyReductionCounter(anchor, 0, 0, emptyList(), emptyList())
        if (rest.weekly.any { it.eligibility != RestEligibility.REJECTED }) {
            // M4 must resolve reduced/additional/compensation assignments jointly. Never invent a debt here.
            return DailyReductionCounter(anchor, 0, counter.maximumPossibleCount, counter.reductions,
                frozen((counter.restrictions + RestRestriction.WEEKLY_ASSIGNMENT_REQUIRES_M4).distinct()))
        }
        return counter
    }

    private fun resumeAfterUncertainEnd(path: Path, after: Instant, candidates: List<RestCandidate>,
        enqueue: (Path) -> Unit, anchors: MutableSet<RestAnchor>): Boolean {
        val next = candidates.filter { it.range.start >= after && it.actualEndProved && it.netRestSeconds >= 39600 &&
            it.eligibility == RestEligibility.PROVED }.minByOrNull { it.range.end } ?: return false
        val anchor = RestAnchor(next.id, next.range, RestAnchorMethod.ACTUAL_REST_END, null, emptyList())
        anchors += anchor
        val unknown = DailyReductionCounter(path.counter.weeklyAnchor, 0, null, path.counter.reductions,
            listOf(RestRestriction.LEFT_REDUCTION_HISTORY_MISSING))
        enqueue(path.copy(start = anchor.at, anchor = anchor, counter = afterWeekly(next, anchor, unknown),
            methodRestrictions = emptyList()))
        return true
    }
}
