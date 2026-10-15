package de.fahrerabrechnung.domain.violation.input

import de.fahrerabrechnung.domain.model.ActivityType
import java.time.Instant
import org.junit.Assert.*
import org.junit.Test

class InputCoverageTest {
    private val day = Instant.parse("2026-09-07T00:00:00Z")
    private fun at(minute: Long) = day.plusSeconds(minute * 60)
    private fun snapshot(): ViolationInputSnapshot {
        val key = InputSourceKey("P", "C", "S", "raw", "GEN1", null, null, null)
        val source = InputSource(key, listOf(FileEvidence(InputFamily.SPECIAL_CONDITIONS, 0x0522,
            FileAvailability.EMPTY, "empty-file", "digest")), emptyList())
        val changes = listOf(RecordedChange(0, ActivityType.SONSTIGE_ARBEIT, false, false, true),
            RecordedChange(60, ActivityType.PAUSE_RUHE, false, false, false),
            RecordedChange(120, ActivityType.SONSTIGE_ARBEIT, false, false, true),
            RecordedChange(180, ActivityType.PAUSE_RUHE, false, false, true))
        return InputSnapshotBuilder.build(InputSources("P", listOf("C"),
            listOf(ActivitySource(source, listOf(RecordedDay(0, 0, day, "day", "0001", 0, changes)))),
            emptyList(), listOf(source), emptyList(), emptyMap()), InputContext.empty("P"))
    }

    @Test fun aLaterUnknownOrOpenTailDoesNotInvalidateEarlierCompleteFacts() {
        val snapshot = snapshot()
        fun coverage(start: Long, end: Long) = InputCoverage.forRule(snapshot, InputRange(at(start), at(end)),
            RuleInputRequirement("facts", setOf(InputDependency.ACTIVITY)))
        assertTrue(coverage(0, 60).factualPrerequisitesCovered)
        assertFalse(coverage(60, 120).factualPrerequisitesCovered)
        assertTrue(coverage(120, 180).factualPrerequisitesCovered)
        assertFalse(coverage(180, 181).factualPrerequisitesCovered)
        assertEquals(InputRange(at(60), at(120)), coverage(0, 180).gaps.single().range)
        assertFalse(coverage(-1, 60).factualPrerequisitesCovered)
    }

    @Test fun emptyPointFileAndKnownActivitiesNeverProveContextOrLegalAllClear() {
        val snapshot = snapshot()
        val time = InputRange(at(0), at(60))
        assertTrue(InputCoverage.forRule(snapshot, time, RuleInputRequirement("activity-only", setOf(InputDependency.ACTIVITY))).factualPrerequisitesCovered)
        assertFalse(InputCoverage.forRule(snapshot, time, RuleInputRequirement("scope", setOf(InputDependency.SCOPE), ScopeArticle.DRIVING_6)).factualPrerequisitesCovered)
        val ferry = InputCoverage.forRule(snapshot, time, RuleInputRequirement("ferry", setOf(InputDependency.SPECIAL_CONDITIONS)))
        assertFalse(ferry.factualPrerequisitesCovered)
        assertEquals(FileAvailability.EMPTY, ferry.fileEvidence.single().availability)
    }

    @Test fun contextCoverageSplitsAtValidityChangesRatherThanDroppingAdjacentAnswers() {
        val snapshot = snapshot()
        fun entry(start: Long, end: Long) = ContextAssertion(ContextKey.MAX_MASS_KG, ContextValue.Number(40000),
            ContextSubject(ContextSubjectKind.PROFILE, "P"), at(start), at(end), ContextOrigin.USER_CONTEXT, "test", day)
        val full = snapshot.copy(context = InputContext("P", "two", listOf(entry(0, 30), entry(30, 60))))
        val requirement = RuleInputRequirement("context", setOf(InputDependency.CONTEXT), contextKeys = setOf(ContextKey.MAX_MASS_KG))
        assertTrue(InputCoverage.forRule(full, InputRange(at(0), at(60)), requirement).factualPrerequisitesCovered)
        val gap = full.copy(context = InputContext("P", "gap", listOf(entry(0, 29), entry(30, 60))))
        assertEquals(InputRange(at(29), at(30)), InputCoverage.forRule(gap, InputRange(at(0), at(60)), requirement).gaps.single().range)
    }
}
