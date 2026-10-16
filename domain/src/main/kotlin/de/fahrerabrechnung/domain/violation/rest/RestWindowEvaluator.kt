package de.fahrerabrechnung.domain.violation.rest

import de.fahrerabrechnung.domain.violation.input.*
import java.time.Duration
import java.time.Instant

internal data class RestScore(val required: Long, val capacity: Long, val actualCredit: Long, val kind: DailyRestKind,
    val eligibility: RestEligibility, val restrictions: List<RestRestriction>, val candidates: List<String>) {
    val deficit get() = maxOf(0, required - capacity)
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
                    reductionRestrictions(counter) +
                        (if (!candidate.actualEndProved && through < start.plusSeconds(choice.mode.seconds))
                            listOf(RestRestriction.OPEN_REST_CLASSIFICATION_PENDING) else emptyList()) +
                        (if (choice.mode == RestWindowMode.OCCASIONAL_BUS_25)
                            listOf(RestRestriction.BUS_REDUCED_25_HOUR_INTERPRETATION_OPEN) else emptyList()))
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
        val firstRegular = result.filter { it.kind == DailyRestKind.REGULAR && it.eligibility == RestEligibility.PROVED }
            .mapNotNull { it.actualEnd }.minOrNull()
        // A ferry rest can extend a completed eleven-hour part to its actual longer end (R07).
        val firstCandidates = result.filter { it.actualEnd == firstRegular }.flatMap { it.candidates }.toSet()
        return frozen(result.filter { option -> firstRegular == null || option.actualEnd?.let { it <= firstRegular } != false ||
            option.candidates.any { id -> usable.any { it.id == id && it.interrupted &&
                it.rest.any { slice -> slice.range.end == firstRegular } && firstCandidates.isNotEmpty() } } }
            .distinctBy { it.id }.sortedWith(compareBy({ it.actualEnd }, { it.kind.name }, { it.id })))
    }

    /** Capacity may include an optimistic future scalar; actualCredit contains recorded rest only. */
    fun potential(start: Instant, cut: Instant, deadline: Instant, choice: RestModeChoice,
        counter: DailyReductionCounter, candidates: List<RestCandidate>, check: () -> Unit): RestScore {
        val future = seconds(cut, deadline)
        fun interruptions(c: RestCandidate) = c.interruptions.filter { it.range.start < cut }
        fun interruptionTime(c: RestCandidate) = interruptions(c).sumOf { seconds(it.range.start, minOf(it.range.end, cut)) }
        val usable = candidates.filter { c -> c.range.start >= start && c.range.start < cut &&
            (c.eligibility != RestEligibility.REJECTED || c.restrictions.all {
                it == RestRestriction.FERRY_INTERRUPTION_LIMIT_EXCEEDED }) &&
            interruptions(c).size <= 2 && interruptionTime(c) <= 3600 }
        val scores = mutableListOf<RestScore>()
        fun add(required: Long, amount: Long, actual: Long, kind: DailyRestKind, parts: List<RestCandidate>,
            extra: List<RestRestriction> = emptyList()) {
            val restrictions = (choice.restrictions + parts.flatMap { it.restrictions }.filterNot {
                it == RestRestriction.FERRY_INTERRUPTION_LIMIT_EXCEEDED } + extra).distinct()
            val eligibility = if (choice.eligibility == RestEligibility.PROVED && restrictions.isEmpty() &&
                parts.all { it.eligibility == RestEligibility.PROVED || it.restrictions.all { r ->
                    r == RestRestriction.FERRY_INTERRUPTION_LIMIT_EXCEEDED } }) RestEligibility.PROVED else RestEligibility.CONDITIONAL
            scores += RestScore(required, minOf(required, amount), minOf(required, actual), kind, eligibility,
                frozen(restrictions), frozen(parts.map { it.id }))
        }
        fun available(candidate: RestCandidate): Long {
            val actual = credit(candidate, start, cut)
            val latest = candidate.rest.lastOrNull { it.range.start < cut } ?: return 0
            val tail = latest.range.end >= cut && (latest.range.end > cut || !candidate.actualEndProved)
            if (tail) return actual + future
            // Hypothetical continuation of a ferry rest after an already elapsed interruption.
            val ferry = candidate.interrupted || candidate.ferryPoints.isNotEmpty()
            val extraGap = seconds(latest.range.end, cut)
            val existingGap = candidate.interruptions.any { it.range.start == latest.range.end && it.range.start < cut }
            val count = interruptions(candidate).size + if (existingGap || extraGap == 0L) 0 else 1
            val time = interruptionTime(candidate) + if (existingGap) 0 else extraGap
            return actual + if (ferry && count <= 2 && time <= 3600) future else 0
        }
        add(if (choice.mode == RestWindowMode.CREW_30) 32400 else 39600, future, 0,
            if (choice.mode == RestWindowMode.CREW_30) DailyRestKind.CREW_NINE_HOURS else DailyRestKind.REGULAR, emptyList())
        for (candidate in usable) {
            check()
            val required = if (choice.mode == RestWindowMode.CREW_30 && !candidate.interrupted) 32400L else 39600L
            add(required, available(candidate), credit(candidate, start, cut),
                if (required == 32400L) DailyRestKind.CREW_NINE_HOURS else DailyRestKind.REGULAR, listOf(candidate))
        }
        if (choice.mode != RestWindowMode.CREW_30 && counter.minimumObservedCount < 3) {
            val restrictions = reductionRestrictions(counter) + if (choice.mode == RestWindowMode.OCCASIONAL_BUS_25)
                listOf(RestRestriction.BUS_REDUCED_25_HOUR_INTERPRETATION_OPEN) else emptyList()
            add(32400, future, 0, DailyRestKind.REDUCED, emptyList(), restrictions)
            usable.filterNot { it.interrupted }.forEach {
                // A reduced daily rest must remain continuous; ferry gaps cannot be joined here.
                val actual = credit(it, start, cut)
                val tail = it.range.end > cut || (it.range.end == cut && !it.actualEndProved)
                add(32400, actual + if (tail) future else 0, actual, DailyRestKind.REDUCED, listOf(it), restrictions)
            }
        }
        if (choice.mode != RestWindowMode.CREW_30) for (first in usable) {
            check()
            if (credit(first, start, cut) < 10800 || first.range.end > cut) continue
            val firstLimits = if (first.interrupted) listOf(RestRestriction.FERRY_SPLIT_ALLOCATION_GUIDANCE_ONLY) else emptyList()
            add(43200, 10800 + future, 10800, DailyRestKind.SPLIT_REGULAR, listOf(first), firstLimits)
            for (second in usable) {
                check()
                if (first.range.end > second.range.start || first.id == second.id ||
                    first.interruptions.size + interruptions(second).size > 2 ||
                    first.interruptionSeconds + interruptionTime(second) > 3600) continue
                add(43200, 10800 + minOf(32400, available(second)),
                    10800 + minOf(32400, credit(second, start, cut)), DailyRestKind.SPLIT_REGULAR,
                    listOf(first, second), firstLimits)
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
        if (regular != null) {
            val required = if (regular.kind == DailyRestKind.SPLIT_REGULAR) 43200L else
                if (regular.kind == DailyRestKind.CREW_NINE_HOURS) 32400L else 39600L
            score = RestScore(required, required, required, regular.kind, regular.eligibility, regular.restrictions, regular.candidates)
        }
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
        var law = RestLawVersions.daily(lawId, choice.mode)
        if (score.candidates.any { id -> candidates.any { it.id == id && it.interrupted } }) law = law.copy(
            article = law.article + ", Art.9(1)", version = law.version + ";Art.9:" +
                if (RestLawVersions.phase(range, RestLawVersions.ferryWeekly) == RestDatePhase.AFTER) "2020-08-20" else "2007-04-11",
            sourceIds = frozen((law.sourceIds + "M3-GN6" + if (RestLawVersions.phase(range, RestLawVersions.ferryWeekly) == RestDatePhase.AFTER)
                listOf("Q02") else emptyList()).distinct()))
        if (RestRestriction.Q07_SUCCESSIVE_WINDOW_INTERPRETATION_OPEN in methodRestrictions)
            law = law.copy(sourceIds = frozen((law.sourceIds + "Q07").distinct()))
        val id = InputRevision.hash("m3-rest-window") {
            text(DAILY_REST_INTERPRETATION); text(snapshot.profileId); instant(start); text(choice.mode.name)
            int(counter.minimumObservedCount); nullable(counter.maximumPossibleCount?.toString()); nullable(choice.serviceId)
            text(anchor.candidateId); counter.reductions.forEach { text(it.id) }
            (counter.restrictions + methodRestrictions + choice.restrictions).distinct().sortedBy { it.name }.forEach { text(it.name) }
        }
        return DailyRestWindow(id, law, anchor, start, choice.mode, deadline, observed,
            counter, options, state, score.required, score.actualCredit, score.capacity, score.candidates, score.kind,
            score.deficit, coverage, frozen(restrictions),
            frozen((choice.contextEvidence + options.flatMap { it.contextEvidence } +
                candidates.filter { it.id in score.candidates }.flatMap { it.contextEvidence }).distinct()),
            budget.takeIf { !inRest && options.isEmpty() })
    }

    fun onset(window: DailyRestWindow, choice: RestModeChoice, candidates: List<RestCandidate>,
        check: () -> Unit): Pair<Instant, Instant>? {
        if (window.potentialDeficitSeconds <= 0 || !window.coverage.factualPrerequisitesCovered ||
            window.coverage.excludedScope.isNotEmpty()) return null
        val through = minOf(window.observedThrough, window.deadline)
        var lo = 0L; var hi = seconds(window.referenceStart, through)
        while (lo < hi) {
            check()
            val mid = (lo + hi) / 2
            val cut = window.referenceStart.plusSeconds(mid)
            if (potential(window.referenceStart, cut, window.deadline, choice, window.counterBefore, candidates, check).deficit > 0) hi = mid
            else lo = mid + 1
        }
        val boundary = window.referenceStart.plusSeconds(maxOf(0, lo - 1))
        return boundary to minOf(through, boundary.plusSeconds(60))
    }
}
