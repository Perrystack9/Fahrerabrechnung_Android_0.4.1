package de.fahrerabrechnung.domain.violation.rest

import de.fahrerabrechnung.domain.violation.input.*
import java.time.Duration
import java.time.Instant

internal data class RestScore(val required: Long, val credit: Long, val kind: DailyRestKind,
    val eligibility: RestEligibility, val restrictions: List<RestRestriction>, val candidates: List<String>) {
    val deficit get() = maxOf(0, required - credit)
}

internal object RestWindowEvaluator {
    private fun seconds(start: Instant, end: Instant) = Duration.between(start, end).seconds.coerceAtLeast(0)
    private fun credit(candidate: RestCandidate, start: Instant, through: Instant): Long = candidate.rest.sumOf {
        val from = maxOf(start, it.range.start); val end = minOf(through, it.range.end)
        if (from < end) seconds(from, end) else 0
    }
    private fun reductionRestrictions(counter: DailyReductionCounter): List<RestRestriction> =
        if (counter.exact) emptyList() else frozen((counter.restrictions + RestRestriction.LEFT_REDUCTION_HISTORY_MISSING).distinct())

    fun options(start: Instant, through: Instant, choice: RestModeChoice, counter: DailyReductionCounter,
        candidates: List<RestCandidate>, check: () -> Unit): List<DailyRestOption> {
        val usable = candidates.filter { it.range.start >= start && it.range.start < through &&
            it.eligibility != RestEligibility.REJECTED }
        val result = mutableListOf<DailyRestOption>()
        fun add(kind: DailyRestKind, parts: List<RestCandidate>, consumes: Boolean, extra: List<RestRestriction> = emptyList()) {
            val restrictions = (choice.restrictions + parts.flatMap { it.restrictions } + extra).distinct()
            val status = if (choice.eligibility == RestEligibility.PROVED && parts.all { it.eligibility == RestEligibility.PROVED } &&
                restrictions.isEmpty()) RestEligibility.PROVED else RestEligibility.CONDITIONAL
            val last = parts.last()
            val id = InputRevision.hash("m3-daily-rest-option") {
                text(DAILY_REST_INTERPRETATION); instant(start); text(choice.mode.name); text(kind.name)
                parts.forEach { text(it.id) }; nullable(choice.serviceId)
            }
            result += DailyRestOption(id, kind, frozen(parts.map { it.id }), parts.sumOf { credit(it, start, through) },
                last.range.end.takeIf { last.actualEndProved }, consumes, status, frozen(restrictions), choice.serviceId,
                frozen((choice.contextEvidence + parts.flatMap { it.contextEvidence }).distinct()))
        }
        for (candidate in usable) {
            check()
            val amount = credit(candidate, start, through)
            if (amount >= 39600) add(DailyRestKind.REGULAR, listOf(candidate), false)
            if (!candidate.interrupted && amount >= 32400 && amount < 39600) {
                if (choice.mode == RestWindowMode.CREW_30) add(DailyRestKind.CREW_NINE_HOURS, listOf(candidate), false)
                else if (counter.minimumObservedCount < 3) add(DailyRestKind.REDUCED, listOf(candidate), true,
                    reductionRestrictions(counter) + if (choice.mode == RestWindowMode.OCCASIONAL_BUS_25)
                        listOf(RestRestriction.BUS_REDUCED_25_HOUR_INTERPRETATION_OPEN) else emptyList())
            }
        }
        if (choice.mode != RestWindowMode.CREW_30) for ((index, first) in usable.withIndex()) {
            check()
            val firstCredit = credit(first, start, through)
            if (firstCredit < 10800 || firstCredit >= 39600) continue
            for (second in usable.drop(index + 1)) {
                check()
                if (first.range.end > second.range.start || credit(second, start, through) < 32400) continue
                if (first.interruptions.size + second.interruptions.size > 2 ||
                    first.interruptionSeconds + second.interruptionSeconds > 3600) continue
                add(DailyRestKind.SPLIT_REGULAR, listOf(first, second), false,
                    if (first.interrupted) listOf(RestRestriction.FERRY_SPLIT_ALLOCATION_GUIDANCE_ONLY) else emptyList())
            }
        }
        // A completed regular daily rest ends the duty period. Never skip it to a later preferred date.
        val barrier = result.filter { it.kind == DailyRestKind.REGULAR && it.eligibility == RestEligibility.PROVED }
            .mapNotNull { it.actualEnd }.minOrNull()
        return frozen(result.filter { barrier == null || it.actualEnd?.let { end -> end <= barrier } != false }
            .distinctBy { it.id }.sortedWith(compareBy({ it.actualEnd }, { it.kind.name }, { it.id })))
    }

    /** Optimistic scalar capacity; future time is not recorded, credited or persisted as rest. */
    fun potential(start: Instant, cut: Instant, deadline: Instant, choice: RestModeChoice,
        counter: DailyReductionCounter, candidates: List<RestCandidate>, check: () -> Unit): RestScore {
        val future = seconds(cut, deadline)
        val usable = candidates.filter { it.range.start < cut && it.range.end > start && it.eligibility != RestEligibility.REJECTED }
        val scores = mutableListOf<RestScore>()
        fun status(parts: List<RestCandidate>, restrictions: List<RestRestriction>) =
            if (choice.eligibility == RestEligibility.PROVED && parts.all { it.eligibility == RestEligibility.PROVED } && restrictions.isEmpty())
                RestEligibility.PROVED else RestEligibility.CONDITIONAL
        fun add(required: Long, amount: Long, kind: DailyRestKind, parts: List<RestCandidate>, extra: List<RestRestriction> = emptyList()) {
            val restrictions = (choice.restrictions + parts.flatMap { it.restrictions } + extra).distinct()
            scores += RestScore(required, minOf(required, amount), kind, status(parts, restrictions), frozen(restrictions), frozen(parts.map { it.id }))
        }
        fun available(candidate: RestCandidate): Long {
            val actual = credit(candidate, start, cut)
            val tailCanContinue = candidate.rest.any { it.range.start < cut && it.range.end > cut } ||
                (candidate.range.end == cut && !candidate.actualEndProved)
            return actual + if (tailCanContinue) future else 0
        }
        add(if (choice.mode == RestWindowMode.CREW_30) 32400 else 39600, future,
            if (choice.mode == RestWindowMode.CREW_30) DailyRestKind.CREW_NINE_HOURS else DailyRestKind.REGULAR, emptyList())
        for (candidate in usable) {
            check()
            val required = if (choice.mode == RestWindowMode.CREW_30 && !candidate.interrupted) 32400L else 39600L
            add(required, available(candidate), if (required == 32400L) DailyRestKind.CREW_NINE_HOURS else DailyRestKind.REGULAR,
                listOf(candidate))
        }
        if (choice.mode != RestWindowMode.CREW_30 && counter.minimumObservedCount < 3) {
            val restrictions = reductionRestrictions(counter) + if (choice.mode == RestWindowMode.OCCASIONAL_BUS_25)
                listOf(RestRestriction.BUS_REDUCED_25_HOUR_INTERPRETATION_OPEN) else emptyList()
            add(32400, future, DailyRestKind.REDUCED, emptyList(), restrictions)
            usable.filterNot { it.interrupted }.forEach {
                add(32400, available(it), DailyRestKind.REDUCED, listOf(it), restrictions)
            }
        }
        if (choice.mode != RestWindowMode.CREW_30) for (first in usable) {
            check()
            if (credit(first, start, cut) < 10800 || first.range.end > cut) continue
            val firstLimits = if (first.interrupted) listOf(RestRestriction.FERRY_SPLIT_ALLOCATION_GUIDANCE_ONLY) else emptyList()
            add(43200, 10800 + future, DailyRestKind.SPLIT_REGULAR, listOf(first), firstLimits)
            for (second in usable) {
                check()
                if (first.range.end > second.range.start || first.id == second.id ||
                    first.interruptions.size + second.interruptions.size > 2 ||
                    first.interruptionSeconds + second.interruptionSeconds > 3600) continue
                add(43200, 10800 + minOf(32400, available(second)), DailyRestKind.SPLIT_REGULAR, listOf(first, second), firstLimits)
            }
        }
        return scores.minWith(compareBy<RestScore>({ it.deficit }, { it.eligibility != RestEligibility.PROVED }, { it.required },
            { it.candidates.joinToString() }))
    }

    fun evaluate(snapshot: ViolationInputSnapshot, start: Instant, observed: Instant, anchor: RestAnchor,
        choice: RestModeChoice, counter: DailyReductionCounter, candidates: List<RestCandidate>,
        methodRestrictions: List<RestRestriction>, check: () -> Unit): DailyRestWindow {
        val deadline = start.plusSeconds(choice.mode.seconds)
        val through = minOf(observed, deadline)
        val range = InputRange(start, through)
        val coverage = InputCoverage.forRule(snapshot, range,
            RuleInputRequirement("R01–R07", setOf(InputDependency.ACTIVITY, InputDependency.SCOPE), ScopeArticle.DAILY_REST_8))
        val options = options(start, through, choice, counter, candidates, check)
        var score = potential(start, through, deadline, choice, counter, candidates, check)
        val regular = options.firstOrNull { it.eligibility == RestEligibility.PROVED && it.kind != DailyRestKind.REDUCED }
        if (regular != null) score = RestScore(if (regular.kind == DailyRestKind.SPLIT_REGULAR) 43200 else
            if (choice.mode == RestWindowMode.CREW_30) 32400 else 39600,
            if (regular.kind == DailyRestKind.SPLIT_REGULAR) 43200 else if (choice.mode == RestWindowMode.CREW_30) 32400 else 39600,
            regular.kind, regular.eligibility, regular.restrictions, regular.candidates)
        val restrictions = linkedSetOf<RestRestriction>()
        restrictions += choice.restrictions; restrictions += methodRestrictions
        if (!coverage.factualPrerequisitesCovered) restrictions += RestRestriction.ACTIVITY_GAP
        if (coverage.gaps.any { it.dependency == InputDependency.SCOPE }) restrictions += RestRestriction.SCOPE_UNDETERMINED
        if (coverage.excludedScope.isNotEmpty()) restrictions += RestRestriction.SCOPE_EXCLUDED
        when (RestLawVersions.phase(range, RestLawVersions.base)) {
            RestDatePhase.BEFORE -> restrictions += RestRestriction.BEFORE_561_TEMPORAL_SCOPE
            RestDatePhase.UNCONFIRMED -> restrictions += RestRestriction.HISTORICAL_ACTIVATION_UNCONFIRMED
            RestDatePhase.AFTER -> Unit
        }
        restrictions += score.restrictions
        val state = when {
            !coverage.factualPrerequisitesCovered || coverage.excludedScope.isNotEmpty() -> RestWindowState.INDETERMINATE
            options.any { it.eligibility == RestEligibility.PROVED } -> RestWindowState.FULFILLED
            options.isNotEmpty() -> RestWindowState.INDETERMINATE
            score.deficit > 0 -> RestWindowState.NORMAL_RULE_DEFICIT
            else -> RestWindowState.OPEN
        }
        val firstPart = candidates.any { it.eligibility != RestEligibility.REJECTED && it.range.start >= start &&
            it.range.end <= through && it.netRestSeconds >= 10800 }
        val futureMinimum = if (choice.mode == RestWindowMode.CREW_30 || counter.minimumObservedCount < 3 || firstPart) 32400L else 39600L
        val inRest = candidates.any { it.range.start < through && it.range.end >= through && !it.actualEndProved }
        val budget = seconds(start, deadline) - seconds(start, through) - futureMinimum
        val lawId = when {
            choice.mode == RestWindowMode.CREW_30 -> "R05"
            choice.mode == RestWindowMode.OCCASIONAL_BUS_25 -> "R06"
            score.kind == DailyRestKind.SPLIT_REGULAR -> "R03"
            score.kind == DailyRestKind.REDUCED || counter.minimumObservedCount >= 3 -> "R02"
            else -> "R01"
        }
        val id = InputRevision.hash("m3-rest-window") {
            text(DAILY_REST_INTERPRETATION); text(snapshot.profileId); instant(start); text(choice.mode.name)
            int(counter.minimumObservedCount); nullable(counter.maximumPossibleCount?.toString()); nullable(choice.serviceId)
        }
        return DailyRestWindow(id, RestLawVersions.daily(lawId, choice.mode), anchor, start, choice.mode, deadline, observed,
            counter, options, state, score.required, score.credit, score.deficit, coverage, frozen(restrictions),
            frozen((choice.contextEvidence + options.flatMap { it.contextEvidence }).distinct()),
            budget.takeIf { !inRest && options.isEmpty() })
    }

    fun onset(window: DailyRestWindow, choice: RestModeChoice, candidates: List<RestCandidate>,
        check: () -> Unit): Pair<Instant, Instant>? {
        if (window.potentialDeficitSeconds <= 0 || !window.coverage.factualPrerequisitesCovered ||
            window.coverage.excludedScope.isNotEmpty() || candidates.any { it.interrupted &&
                it.range.overlaps(InputRange(window.referenceStart, minOf(window.deadline, window.observedThrough))) }) return null
        val through = minOf(window.observedThrough, window.deadline)
        var lo = 0L
        var hi = seconds(window.referenceStart, through)
        while (lo < hi) {
            check()
            val mid = (lo + hi) / 2
            val cut = window.referenceStart.plusSeconds(mid)
            if (potential(window.referenceStart, cut, window.deadline, choice, window.counterBefore, candidates, check).deficit > 0) hi = mid
            else lo = mid + 1
        }
        val boundary = window.referenceStart.plusSeconds(maxOf(0, lo - 1))
        val proof = minOf(through, boundary.plusSeconds(60))
        return boundary to proof
    }
}
