package de.fahrerabrechnung.domain.violation.weekly

import de.fahrerabrechnung.domain.violation.driving.*
import de.fahrerabrechnung.domain.violation.input.*
import de.fahrerabrechnung.domain.violation.rest.*
import java.time.Instant
import java.util.TimeZone
import java.util.concurrent.CancellationException
import org.junit.Assert.*
import org.junit.Test

/** M0 hand-calculated LG expectations. W01/week/debt numbers remain explicit L02 projections. */
class WeeklyRestReferenceTest {
    private fun at(value: String) = Instant.parse(value)
    private val monday = at("2026-09-07T00:00:00Z")
    private fun calculate(f: WeeklyFixture) = WeeklyRestCalculator.calculate(f.snapshot())
    private fun pair(r: WeeklyRestCalculation, start: Instant = monday) = r.allocations.flatMap { it.windows }
        .filter { it.range.start == start && it.requiredCount == 2 }

    @Test fun LG58TwoRegularRestsMeetTheClosedTwoWeekProjection() {
        val r = calculate(WeeklyFixture().r("2026-09-12T03:00:00Z", 2700)
            .r("2026-09-19T03:00:00Z", 2700).until("2026-09-21T00:00:00Z"))
        assertTrue(r.alternativesComplete)
        assertTrue(pair(r).all { it.state == WeeklyWindowState.FULFILLED_PROJECTION })
        assertTrue(r.allocations.all { it.debts.isEmpty() })
        assertFalse(r.establishesLegalAllClear)
    }
    @Test fun LG58OneRequiredReducedRestProducesOne21HourDebt() {
        val r = calculate(WeeklyFixture().r("2026-09-12T03:00:00Z", 2700)
            .r("2026-09-19T03:00:00Z", 1440).until("2026-09-21T00:00:00Z"))
        assertFalse(r.allocations.isEmpty())
        assertTrue(r.allocations.all { it.debts.size == 1 && it.debts.single().seconds == 75600L })
        assertTrue(pair(r).all { it.state == WeeklyWindowState.FULFILLED_PROJECTION })
    }
    @Test fun LG59EveryOverlappingPairIsExaminedAndMissingInternationalFactsRemainOpen() {
        val f = WeeklyFixture().countries().r("2026-09-12T03:00:00Z", 2700)
            .r("2026-09-19T03:00:00Z", 1440).r("2026-09-26T03:00:00Z", 1440)
            .r("2026-10-03T03:00:00Z", 2700).until("2026-10-05T00:00:00Z")
        val r = calculate(f)
        assertTrue(r.allocations.any { it.windows.any { w -> w.range.start == monday.plusSeconds(604800) && w.requiredCount == 2 } })
        assertTrue(r.contextNeeds.any { it.key == ContextKey.REST_START_COUNTRY })
        assertTrue(r.findings.none { it.law.id == "W01" && it.normalRuleDeficitIsProved })
    }
    private fun limit(minutesLate: Int = 0): WeeklyFixture = WeeklyFixture(monday.minusSeconds(162000))
        .r("2026-09-05T03:00:00Z", 2700).until(monday.plusSeconds(6 * 86400 + minutesLate * 60L))
        .apply { recorded.r(2700) }
    @Test fun LG60ExactlySixDaysIsNotLate() {
        val r = calculate(limit())
        assertEquals(monday.plusSeconds(518400), r.deadlines.first().deadline)
        assertEquals(0, r.deadlines.first().lateBySeconds)
        assertTrue(r.findings.none { it.law.id == "W02" })
    }
    @Test fun LG60OneMinuteLateHasExactOnsetAndOneMinuteQuantity() {
        val r = calculate(limit(1))
        val finding = r.findings.single { it.law.id == "W02" }
        assertEquals(60, finding.excessOrMissingSeconds)
        assertEquals(monday.plusSeconds(518400), finding.mathematicalOnset)
        assertEquals(monday.plusSeconds(518460), finding.range.end)
        assertTrue(finding.normalRuleDeficitIsProved)
        assertEquals(LegalAssessment.NOT_DETERMINED, finding.legalAssessment)
        assertFalse(finding.establishesArticle12Debt)
    }
    @Test fun LG61RestEndAfterTheStartDeadlineDoesNotMakeItsStartLate() {
        val r = calculate(limit())
        assertTrue(r.candidates.any { it.range.end == at("2026-09-14T21:00:00Z") })
        assertEquals(0, r.deadlines.first().lateBySeconds)
    }
    @Test fun LG62OneCrossWeekRestCannotSatisfyTwoWeeklySlots() {
        val r = calculate(WeeklyFixture().r("2026-09-13T23:00:00Z", 2700).until("2026-09-21T00:00:00Z"))
        assertTrue(r.allocations.all { it.uses.size == 1 })
        assertEquals(setOf(monday, monday.plusSeconds(604800)), r.allocations.map { it.uses.single().assignedWeek }.toSet())
        assertTrue(pair(r).all { it.state == WeeklyWindowState.NORMAL_RULE_DEFICIT })
        assertTrue(r.findings.filter { it.law.id == "W01" }.all { WeeklyRestriction.L02_UTC_WEEK_PROJECTION_ONLY in it.restrictions })
    }
    @Test fun LG63GlobalCrossWeekAssignmentRetainsACompleteWitness() {
        val f = WeeklyFixture(at("2026-09-06T23:00:00Z")).r("2026-09-06T23:00:00Z", 2700)
            .r("2026-09-13T23:00:00Z", 2700).r("2026-09-20T23:00:00Z", 2700).until("2026-09-28T00:00:00Z")
        val r = calculate(f)
        assertTrue(r.allocations.any { ledger -> listOf(monday, monday.plusSeconds(604800)).all { week ->
            ledger.windows.any { it.range.start == week && it.requiredCount == 2 && it.state == WeeklyWindowState.FULFILLED_PROJECTION } } })
        assertTrue(r.findings.none { it.law.id == "W01" && it.normalRuleDeficitIsProved })
    }
    @Test fun LG101TwentyThreeHoursIsADailyRestButNotAWeeklyAnchorOrDebt() {
        val f = WeeklyFixture(monday.minusSeconds(162000)).r("2026-09-05T03:00:00Z", 2700)
        f.recorded.w(60).r(1380).d(540)
        val r = calculate(f)
        val short = r.candidates.single { it.netRestSeconds == 23 * 3600L }
        assertTrue(r.dailyRest.anchors.any { it.candidateId == short.id && it.at == monday.plusSeconds(86400) })
        assertTrue(r.allocations.all { it.uses.none { u -> u.candidateId == short.id } && it.debts.none { d -> d.candidateId == short.id } })
        assertEquals(monday, r.deadlines.first().anchorActualEnd)
    }
    private fun episode(): WeeklyFixture = WeeklyFixture(monday.minusSeconds(162000)).r("2026-09-05T03:00:00Z", 2700)
        .r("2026-09-13T07:00:00Z", 1380).r("2026-09-14T07:00:00Z", 1380)
        .r("2026-09-15T07:00:00Z", 1440)
    @Test fun LG102Repeated23HourRestsCreateOnePersistent55HourEpisode() {
        val r = calculate(episode())
        val late = r.findings.filter { it.law.id == "W02" }
        assertEquals(1, late.size)
        assertEquals(55 * 3600L, late.single().excessOrMissingSeconds)
        assertEquals(at("2026-09-13T00:00:00Z"), late.single().mathematicalOnset)
        assertEquals(at("2026-09-15T07:00:00Z"), late.single().range.end)
    }
    @Test fun LG103TwentyThreeHours59NeverGeneratesACompensationDebt() {
        val r = calculate(WeeklyFixture().r("2026-09-12T00:00:00Z", 1439))
        assertTrue(r.allocations.all { it.uses.isEmpty() && it.debts.isEmpty() })
        assertTrue(r.deadlines.isEmpty())
    }
    @Test fun LG103Exactly24HoursCanGenerate21HoursOnlyInARequiredRole() {
        val r = calculate(WeeklyFixture().r("2026-09-12T00:00:00Z", 1440))
        assertTrue(r.allocations.any { it.debts.isEmpty() })
        assertTrue(r.allocations.any { it.debts.singleOrNull()?.seconds == 75600L })
    }
    @Test fun LG103FortyFourHours59HasOneMinuteDebtAnd45HasNone() {
        val short = calculate(WeeklyFixture().r("2026-09-12T00:00:00Z", 2699))
        assertTrue(short.allocations.any { it.debts.singleOrNull()?.seconds == 60L })
        val full = calculate(WeeklyFixture().r("2026-09-12T00:00:00Z", 2700))
        assertTrue(full.allocations.all { it.debts.isEmpty() })
    }
    @Test fun LG104CrossWeekDebtVariantsKeepDifferentDueDatesCorrelated() {
        val r = calculate(WeeklyFixture().r("2026-09-13T23:00:00Z", 1440))
        assertEquals(setOf(at("2026-10-05T00:00:00Z"), at("2026-10-12T00:00:00Z")),
            r.allocations.flatMap { it.debts }.map { it.due }.toSet())
        assertTrue(r.allocations.all { it.debts.size <= 1 })
    }
    @Test fun LG114Extra24HoursBesideSufficientRegularRestsDoesNotCreateADefiniteDebt() {
        val r = calculate(WeeklyFixture(at("2026-08-31T00:00:00Z"))
            .r("2026-09-05T00:00:00Z", 2700).r("2026-09-12T00:00:00Z", 2700)
            .r("2026-09-16T00:00:00Z", 1440).r("2026-09-19T00:00:00Z", 2700).until("2026-09-21T00:00:00Z"))
        assertTrue(r.allocations.any { it.debts.isEmpty() })
        assertTrue(r.allocations.any { it.uses.any { u -> u.kind == WeeklyRestKind.REDUCED && u.extra } })
    }
    @Test fun LG115LaterRestDoesNotEraseTheEarlierOneMinuteDelay() {
        val f = limit(1)
        val before = calculate(f).findings.single { it.law.id == "W02" }
        f.r("2026-09-20T00:00:00Z", 2700)
        val after = calculate(f).findings.single { it.id == before.id }
        assertEquals(before.excessOrMissingSeconds, after.excessOrMissingSeconds)
        assertEquals(before.mathematicalOnset, after.mathematicalOnset)
    }
    @Test fun AnUnavoidableEarlierDelayDoesNotRemoveALaterRequiredCompensationDebt() {
        val f = limit(1).r("2026-09-19T03:00:00Z", 1440).until("2026-09-21T00:00:00Z")
        val r = calculate(f)
        assertTrue(r.alternativesComplete)
        assertTrue(r.allocations.all { it.debts.size == 1 && it.debts.single().seconds == 75600L })
        assertTrue(r.findings.any { it.law.id == "W02" && it.excessOrMissingSeconds == 60L && it.normalRuleDeficitIsProved })
    }
    @Test fun TwoRegularRestsInOneOfTheWeeksCanSatisfyTheTwoWeekMinimum() {
        val r = calculate(WeeklyFixture().r("2026-09-15T00:00:00Z", 2700)
            .r("2026-09-17T00:00:00Z", 2700).until("2026-09-21T00:00:00Z"))
        assertTrue(pair(r).all { it.state == WeeklyWindowState.FULFILLED_PROJECTION })
        assertTrue(r.allocations.all { it.uses.all { u -> u.assignedWeek == monday.plusSeconds(604800) } })
    }
    @Test fun AGapCannotBecomeRestOrAnAssuredDeadlineViolation() {
        val f = WeeklyFixture(monday.minusSeconds(162000)).r("2026-09-05T03:00:00Z", 2700)
        f.recorded.u(7 * 1440)
        val r = calculate(f)
        assertTrue(r.candidates.none { it.range.start >= monday })
        assertTrue(r.findings.none { it.normalRuleDeficitIsProved })
        assertTrue(r.deadlines.all { it.provenSecondsRemaining == null })
    }
    @Test fun OpenTwentyThreeHourTailDoesNotPrematurelyAssertAMissedWeeklyRest() {
        val f = WeeklyFixture(monday.minusSeconds(162000)).r("2026-09-05T03:00:00Z", 2700)
            .until("2026-09-12T23:00:00Z")
        f.recorded.r(1380)
        val r = WeeklyRestCalculator.calculate(f.snapshot(withEnd = false))
        assertTrue(r.findings.none { it.normalRuleDeficitIsProved })
    }
    @Test fun DuplicateG1G2ImportsDoNotDuplicateRestDebtsOrEpisodesAndKeepBothSources() {
        val f = episode()
        val plain = WeeklyRestCalculator.calculate(f.snapshot())
        val doubled = WeeklyRestCalculator.calculate(f.snapshot(duplicate = true))
        assertEquals(plain.findings.map { it.id to it.excessOrMissingSeconds }, doubled.findings.map { it.id to it.excessOrMissingSeconds })
        assertEquals(plain.candidates.map { it.id }, doubled.candidates.map { it.id })
        assertTrue(doubled.candidates.flatMap { it.rest }.all { it.support.flatMap { s -> s.begin + s.end }
            .map { ref -> ref.source.sessionId }.distinct().size == 2 })
        assertNotEquals(plain.provenanceRevision, doubled.provenanceRevision)
    }
    @Test fun FixedSnapshotIsDeterministicAndDoesNotReadTheWallClock() {
        val snapshot = episode().snapshot()
        assertEquals(WeeklyRestCalculator.calculate(snapshot), WeeklyRestCalculator.calculate(snapshot))
    }
    @Test fun SearchLimitCannotPublishAssuredPartialResultsOrBudgets() {
        val r = WeeklyRestCalculator.calculate(episode().snapshot(), maxSearchStates = 1)
        assertFalse(r.alternativesComplete)
        assertTrue(r.findings.none { it.normalRuleDeficitIsProved })
        assertTrue(r.dailyRest.windows.all { it.provenSecondsUntilLatestRestStart == null })
        assertTrue(r.dailyRest.finalCounters.all { it.provenRemaining == null })
        assertTrue(r.issues.any { it.reason == "ALLOCATION_SEARCH_LIMIT_NO_VERDICT" })
    }
    @Test fun CancellationIsRethrownInsteadOfPublishingAPartialCalculation() {
        var calls = 0
        try {
            WeeklyRestCalculator.calculate(episode().snapshot(), checkCancellation = { if (++calls == 6) throw CancellationException("synthetic") })
            fail("cancellation swallowed")
        } catch (expected: CancellationException) { assertEquals("synthetic", expected.message) }
    }
    @Test fun AForeignSnapshotOrMixedRevisionCannotEnterM4() {
        val first = limit().snapshot(); val other = limit(1).snapshot()
        try {
            WeeklyRestCalculator.calculate(first, daily = DailyRestCalculator.calculate(other))
            fail("mixed snapshot accepted")
        } catch (_: IllegalArgumentException) { /* revision contract */ }
    }
    @Test fun DisplayZoneAndDstDoNotChangeUtcDurationsOrWeekProjections() {
        val snapshot = WeeklyFixture(at("2026-10-19T00:00:00Z"))
            .r("2026-10-24T00:00:00Z", 2700).until("2026-10-26T00:00:00Z").snapshot()
        val previous = TimeZone.getDefault()
        try {
            val results = listOf("UTC", "Europe/Berlin", "America/New_York").map { zone ->
                TimeZone.setDefault(TimeZone.getTimeZone(zone)); WeeklyRestCalculator.calculate(snapshot)
            }
            assertEquals(1, results.map { it.revision }.distinct().size)
            assertTrue(results.all { it.candidates.single().netRestSeconds == 162000L })
        } finally { TimeZone.setDefault(previous) }
    }
    @Test fun ACompleteLaterWeeklyRestCanAnchorAnIndependentKnownTailAfterAGap() {
        val f = WeeklyFixture(monday.minusSeconds(162000)).r("2026-09-05T03:00:00Z", 2700)
        f.recorded.u(3 * 1440).r(2700)
        val r = calculate(f)
        val latest = r.candidates.maxBy { it.range.end }
        val next = r.deadlines.single { it.anchorCandidateId == latest.id }
        assertEquals(518400, next.provenSecondsRemaining)
        assertTrue(r.findings.none { it.normalRuleDeficitIsProved })
    }
    @Test fun ProvenanceSnapshotIsRetainedWithoutSourceOrSchemaMutation() {
        val snapshot = episode().snapshot(duplicate = true)
        val before = snapshot.revision
        val r = WeeklyRestCalculator.calculate(snapshot)
        assertSame(snapshot, r.sourceSnapshot)
        assertEquals(before, snapshot.revision)
        assertEquals(snapshot.dataRevision, r.dataRevision)
        assertEquals(snapshot.provenanceRevision, r.provenanceRevision)
    }
}
