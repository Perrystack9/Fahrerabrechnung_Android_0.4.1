package de.fahrerabrechnung.domain.violation.input

enum class InputDependency { ACTIVITY, CREW, SCOPE, SPECIAL_CONDITIONS, LOAD_TYPE, LOCATION, CONTEXT }
data class RuleInputRequirement(val ruleId: String, val dependencies: Set<InputDependency>,
    val scopeArticle: ScopeArticle? = null, val contextKeys: Set<ContextKey> = emptySet()) {
    init { require(ruleId.isNotBlank()) }
}
data class CoverageGap(val dependency: InputDependency, val range: InputRange, val reason: String)
data class RuleInputCoverage(val ruleId: String, val range: InputRange, val gaps: List<CoverageGap>,
    val excludedScope: List<InputRange>, val fileEvidence: List<FileEvidence>,
    val sourceLimitations: List<InputIssue>) {
    /** Factual prerequisite coverage only; never a legal verdict, budget or all-clear. */
    val factualPrerequisitesCovered: Boolean get() = gaps.isEmpty()
}

object InputCoverage {
    fun forRule(snapshot: ViolationInputSnapshot, range: InputRange, requirement: RuleInputRequirement): RuleInputCoverage {
        val gaps = mutableListOf<CoverageGap>()
        val excluded = mutableListOf<InputRange>()
        for (dependency in requirement.dependencies.sortedBy { it.name }) {
            when (dependency) {
                InputDependency.ACTIVITY, InputDependency.CREW -> {
                    var cursor = range.start
                    for (interval in snapshot.intervals.filter { it.range.overlaps(range) }) {
                        val start = maxOf(range.start, interval.range.start); val end = minOf(range.end, interval.range.end)
                        if (cursor < start) gaps += CoverageGap(dependency, InputRange(cursor, start), "NO_CLOSED_INTERVAL")
                        if (interval.meaning.quality != InputQuality.KNOWN) gaps += CoverageGap(dependency,
                            InputRange(start, end), interval.meaning.reason?.name ?: interval.meaning.quality.name)
                        else if (dependency == InputDependency.CREW && (interval.meaning.crew !in setOf(CrewEvidence.CREW, CrewEvidence.SOLO) ||
                            interval.meaning.slot in setOf(SlotEvidence.CONFLICT, SlotEvidence.NOT_RELEVANT)))
                            gaps += CoverageGap(dependency, InputRange(start, end), "CREW_NOT_ESTABLISHED")
                        cursor = end
                    }
                    if (cursor < range.end) gaps += CoverageGap(dependency, InputRange(cursor, range.end), "OPEN_OR_MISSING_END")
                }
                InputDependency.SCOPE -> {
                    var cursor = range.start
                    for (segment in snapshot.scopes.filter { it.range.overlaps(range) }) {
                        val start = maxOf(range.start, segment.range.start); val end = minOf(range.end, segment.range.end)
                        if (cursor < start) gaps += CoverageGap(dependency, InputRange(cursor, start), "NO_SCOPE_CONTEXT")
                        val decisions = segment.decisions.filter { requirement.scopeArticle == null || it.article == requirement.scopeArticle }
                        if (decisions.isEmpty() || decisions.any { it.status in setOf(ScopeStatus.UNKNOWN, ScopeStatus.UNSUPPORTED) })
                            gaps += CoverageGap(dependency, InputRange(start, end), "SCOPE_UNDETERMINED")
                        else if (decisions.any { it.status == ScopeStatus.NOT_APPLICABLE }) excluded += InputRange(start, end)
                        cursor = end
                    }
                    if (cursor < range.end) gaps += CoverageGap(dependency, InputRange(cursor, range.end), "NO_SCOPE_CONTEXT")
                }
                InputDependency.CONTEXT -> {
                    val boundaries = sortedSetOf(range.start, range.end)
                    snapshot.context.assertions.filter { it.key in requirement.contextKeys }.forEach {
                        if (it.from > range.start && it.from < range.end) boundaries += it.from
                        it.until?.takeIf { end -> end > range.start && end < range.end }?.let(boundaries::add)
                    }
                    boundaries.toList().zipWithNext().forEach { (start, end) ->
                        val part = InputRange(start, end)
                        requirement.contextKeys.forEach { key ->
                            val value = snapshot.context.resolve(key, part)
                            if (value.value == null) gaps += CoverageGap(dependency, part,
                                (if (value.conflict) "CONFLICTING_" else "MISSING_") + key.name)
                        }
                    }
                }
                // Point provenance / retained file availability is not continuous temporal coverage.
                InputDependency.SPECIAL_CONDITIONS, InputDependency.LOAD_TYPE, InputDependency.LOCATION ->
                    gaps += CoverageGap(dependency, range, "POINT_SOURCE_REQUIRES_CONCRETE_ASSESSMENT")
            }
        }
        return RuleInputCoverage(requirement.ruleId, range, frozen(gaps.distinct()), frozen(excluded.distinct()),
            frozen(snapshot.sources.flatMap { it.files }), frozen(snapshot.issues.filter {
                it.code in setOf(InputIssueCode.SOURCE_REJECTED, InputIssueCode.NON_MIDNIGHT_DATE,
                    InputIssueCode.REVERSED_OR_DUPLICATE_DAY, InputIssueCode.INVALID_CHANGE_SEQUENCE, InputIssueCode.INCOMPLETE_APPLICATION)
            }))
    }
}
