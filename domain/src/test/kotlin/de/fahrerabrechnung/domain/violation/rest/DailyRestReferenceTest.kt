package de.fahrerabrechnung.domain.violation.rest

import de.fahrerabrechnung.domain.violation.driving.*
import java.time.Instant
import java.util.TimeZone
import java.util.concurrent.CancellationException
import org.junit.Assert.*
import org.junit.Test

/** Expected numbers are the fixed M0 hand calculations, not calculator-generated oracles. */
class DailyRestReferenceTest {
    private val t = Instant.parse("2026-09-07T00:00:00Z")
    private fun calculate(f: RestFixture) = DailyRestCalculator.calculate(f.snapshot())
    private fun DailyRestCalculation.at(at: Instant) = windows.single { it.referenceStart == at }
    private fun DailyRestCalculation.targetFindings(at: Instant = t) = findings.filter { it.assessedRange.start >= at }

    @Test fun LG40ElevenHoursWithin24IsRegularWithoutReduction() {
        val r = calculate(RestFixture().w(780).r(660))
        assertEquals(RestWindowState.FULFILLED, r.at(t).state)
        assertEquals(DailyRestKind.REGULAR, r.at(t).options.single().kind)
        assertEquals(660 * 60L, r.at(t).options.single().netRestSecondsInWindow)
        assertEquals(0, r.finalCounters.single().minimumObservedCount)
        assertEquals(3, r.finalCounters.single().provenRemaining)
        assertTrue(r.targetFindings().isEmpty())
    }
    @Test fun LG41OneMinuteLateUsesAnAvailableReductionButDoesNotRepairAnExhaustedCounter() {
        val free = calculate(RestFixture().w(781).r(660))
        assertEquals(DailyRestKind.REDUCED, free.at(t).options.single().kind)
        assertEquals(659 * 60L, free.at(t).options.single().netRestSecondsInWindow)
        assertEquals(1, free.finalCounters.single().minimumObservedCount)
        assertTrue(free.targetFindings().isEmpty())
        val used = calculate(RestFixture(priorReductions = 3).w(781).r(660))
        val deficit = used.targetFindings().single()
        assertEquals(60L, deficit.missingRestSeconds)
        assertEquals(t.plusSeconds(13 * 3600), deficit.mathematicalOnset)
        assertEquals(t.plusSeconds(13 * 3600 + 60), deficit.firstProvableAt)
        assertTrue(deficit.normalRuleDeficitIsProved)
        assertEquals(LegalAssessment.NOT_DETERMINED, deficit.legalAssessment)
        assertFalse(deficit.establishesArticle12Debt)
        assertTrue(used.anchors.any { it.at == t.plusSeconds(86460) && it.timelyInPreviousWindow == false })
    }
    @Test fun LG42OnlyThreeReducedDailyRestsBetweenTheRecordedWeeklyRests() {
        val f = RestFixture()
        repeat(4) { f.w(900).r(540) }
        val r = calculate(f)
        assertEquals(listOf(0, 1, 2, 3), r.windows.map { it.counterBefore.minimumObservedCount })
        assertEquals(120 * 60L, r.targetFindings().single().missingRestSeconds)
        assertEquals(3, r.finalCounters.single().minimumObservedCount)
        assertEquals(3, r.finalCounters.single().reductions.size)
    }
    @Test fun LG43MondayNeverResetsTheDailyReductionCounter() {
        val sunday = Instant.parse("2026-09-13T00:00:00Z")
        val f = RestFixture(sunday)
        repeat(3) { f.w(900).r(540) }
        val r = calculate(f)
        assertEquals(1, r.at(sunday.plusSeconds(86400)).counterBefore.minimumObservedCount)
        assertEquals(2, r.at(sunday.plusSeconds(2 * 86400)).counterBefore.minimumObservedCount)
        assertEquals(3, r.finalCounters.single().minimumObservedCount)
        assertTrue(r.targetFindings(sunday).isEmpty())
    }
    @Test fun LG44SplitThreePlusNineDoesNotConsumeAFourthReduction() {
        val r = calculate(RestFixture(priorReductions = 3).w(240).r(180).w(480).r(540))
        assertEquals(DailyRestKind.SPLIT_REGULAR, r.at(t).options.single().kind)
        assertEquals(720 * 60L, r.at(t).options.single().netRestSecondsInWindow)
        assertEquals(3, r.finalCounters.single().minimumObservedCount)
        assertTrue(r.targetFindings().isEmpty())
    }
    @Test fun LG45TwoHours59ThenNineCanBeReducedButIsNotASplitRegularRest() {
        val r = calculate(RestFixture().w(240).r(179).w(481).r(540))
        assertEquals(DailyRestKind.REDUCED, r.at(t).options.single().kind)
        assertEquals(1, r.finalCounters.single().minimumObservedCount)
    }
    @Test fun LG45ThreeThenEightHours59LeavesOneMinuteMissing() {
        val r = calculate(RestFixture().w(240).r(180).w(481).r(539))
        assertTrue(r.at(t).options.isEmpty())
        assertEquals(60L, r.targetFindings().single().missingRestSeconds)
    }
    @Test fun LG45NineThenThreeDoesNotInventAnOrderViolation() {
        val r = calculate(RestFixture().w(240).r(540).w(480).r(180))
        assertEquals(DailyRestKind.REDUCED, r.at(t).options.single().kind)
        assertTrue(r.windows.any { it.referenceStart == t.plusSeconds(780 * 60L) })
        assertTrue(r.targetFindings().isEmpty())
    }
    @Test fun LG46LateFullRestCreatesAnActual27HourAnchorAndPreservesTheFirstDeficit() {
        val r = calculate(RestFixture().w(960).r(660))
        val finding = r.targetFindings().single()
        assertEquals(60 * 60L, finding.missingRestSeconds)
        assertEquals(480 * 60L, finding.creditedRestSeconds)
        val anchor = r.anchors.single { it.at == t.plusSeconds(27 * 3600) }
        assertEquals(RestAnchorMethod.Q07_LATE_REST_END, anchor.method)
        assertEquals(false, anchor.timelyInPreviousWindow)
        assertTrue(RestRestriction.Q07_SUCCESSIVE_WINDOW_INTERPRETATION_OPEN in anchor.restrictions)
    }
    @Test fun LG47ThreeInsufficientRestWindowsStaySeparateAndExplicitlyUseQ07() {
        val f = RestFixture()
        repeat(3) { f.w(1080).r(360) }
        val r = calculate(f)
        assertEquals(3, r.targetFindings().size)
        assertEquals(listOf(180 * 60L, 180 * 60L, 180 * 60L), r.targetFindings().map { it.missingRestSeconds })
        assertTrue(r.windows.drop(1).all { RestRestriction.Q07_SUCCESSIVE_WINDOW_INTERPRETATION_OPEN in it.restrictions })
        assertTrue(r.anchors.none { it.range.start >= t })
    }
    @Test fun LG48OwnRecordedCrewCompletesNineHoursIn30WithoutAnotherCard() {
        val f = RestFixture().w(675, crew = true).d(270, crew = true).a(45, crew = true).d(270, crew = true).r(540)
        val r = calculate(f)
        assertEquals(RestWindowMode.CREW_30, r.at(t).mode)
        assertEquals(t.plusSeconds(30 * 3600), r.at(t).deadline)
        assertEquals(RestWindowState.FULFILLED, r.at(t).state)
        assertTrue(r.targetFindings().isEmpty())
        assertEquals(1, r.sourceSnapshot.ownCardIds.size)
        assertTrue(r.contextNeeds.none { it.reason.contains("CREW") })
        assertNull(r.finalCounters.single().provenRemaining)
        assertTrue(RestRestriction.L09_CREW_REDUCTION_ACCOUNTING_UNCONFIRMED in r.finalCounters.single().restrictions)
    }
    @Test fun LG48ThirtyHoursAndOneMinuteCannotRetroactivelyRepairTheCrewWindow() {
        val f = RestFixture().w(676, crew = true).d(270, crew = true).a(45, crew = true).d(270, crew = true).r(540)
        val r = calculate(f)
        assertEquals(RestWindowMode.CREW_30, r.at(t).mode)
        assertEquals(60L, r.targetFindings().single().missingRestSeconds)
    }
    @Test fun LG49SoloAfterTheOptionalHourDisprovesCrewQualification() {
        val f = RestFixture().w(675, crew = true).d(61)
            .d(209, crew = true).a(45, crew = true).d(270, crew = true).r(540)
        val r = calculate(f)
        assertEquals(RestWindowMode.SOLO_24, r.at(t).mode)
        assertTrue(r.targetFindings().isNotEmpty())
    }
    @Test fun LG49MissingCrewIntervalIsNeitherCrewProofNorASoloViolation() {
        val f = RestFixture().w(60, crew = true).u(60).w(555, crew = true)
            .d(270, crew = true).a(45, crew = true).d(270, crew = true).r(540)
        val r = calculate(f)
        assertTrue(r.windows.filter { it.referenceStart == t }.all { it.provenSecondsUntilLatestRestStart == null })
        assertTrue(r.targetFindings().none { it.normalRuleDeficitIsProved })
    }
    @Test fun LG54WeeklyRestExtendsDailyRestWithoutAnExtraElevenHoursOrAnEarlyNewClock() {
        val r = calculate(RestFixture(priorReductions = 3).w(780).r(2700))
        assertEquals(1, r.windows.count { it.referenceStart >= t })
        assertEquals(RestWindowState.FULFILLED, r.at(t).state)
        assertTrue(r.anchors.any { it.at == t.plusSeconds((780 + 2700) * 60L) })
        assertEquals(0, r.finalCounters.single().minimumObservedCount)
        assertEquals(3, r.finalCounters.single().provenRemaining)
    }
    @Test fun LG55BudgetIsAtRecordedNoonWithNoWallClockOrFixedWarningThreshold() {
        val r = calculate(RestFixture(priorReductions = 3).w(720))
        assertEquals(RestWindowState.OPEN, r.at(t).state)
        assertEquals(3600L, r.at(t).provenSecondsUntilLatestRestStart)
        assertTrue(r.targetFindings().isEmpty())
    }
    @Test fun LG56IrreversibleOneMinuteDeficitIsProvedBeforeTheWindowEnds() {
        val r = calculate(RestFixture(priorReductions = 3).w(781))
        val finding = r.targetFindings().single()
        assertEquals(60L, finding.missingRestSeconds)
        assertEquals(t.plusSeconds(13 * 3600), finding.mathematicalOnset)
        assertEquals(t.plusSeconds(13 * 3600 + 60), finding.firstProvableAt)
        assertTrue(finding.normalRuleDeficitIsProved)
        assertNull(r.at(t).provenSecondsUntilLatestRestStart)
    }
    @Test fun LG56AnAvailableNineHourAlternativePreventsThatEarlyDeficit() {
        val r = calculate(RestFixture().w(781))
        assertTrue(r.targetFindings().isEmpty())
        assertEquals(119 * 60L, r.at(t).provenSecondsUntilLatestRestStart)
    }
    @Test fun LG57UnknownElevenHoursNeverBecomeRestOrAConfirmedInfringement() {
        val r = calculate(RestFixture().w(780).u(660))
        assertTrue(r.targetFindings().isEmpty())
        assertTrue(r.windows.filter { it.referenceStart == t }.all { it.state == RestWindowState.INDETERMINATE })
        assertTrue(r.windows.filter { it.referenceStart == t }.all { it.provenSecondsUntilLatestRestStart == null })
        assertFalse(r.establishesLegalAllClear)
        assertTrue(r.candidates.none { it.range.start >= t })
    }
    @Test fun LG99ManualNineAndElevenAreCountedExactlyAsOtherRecordedRest() {
        for ((work, rest) in listOf(900 to 540, 780 to 660)) {
            val r = calculate(RestFixture().w(work).r(rest, manual = true))
            assertEquals(RestWindowState.FULFILLED, r.at(t).state)
            assertEquals(rest * 60L, r.at(t).options.single().netRestSecondsInWindow)
            assertTrue(r.candidates.last().rest.all { it.manualTachograph })
        }
    }
    @Test fun LG100ManualCBitDoesNotGrantAThirtyHourCrewWindow() {
        val r = calculate(RestFixture().w(1260).r(540, manual = true))
        assertEquals(RestWindowMode.SOLO_24, r.at(t).mode)
        assertTrue(r.targetFindings().isNotEmpty())
    }
    @Test fun LG101TwentyThreeHourRestPreservesTheReductionCounterAndCreatesADailyAnchor() {
        val r = calculate(RestFixture(priorReductions = 1).w(60).r(1380))
        assertEquals(1, r.finalCounters.single().minimumObservedCount)
        val rest = r.candidates.last()
        assertTrue(rest.weekly.isEmpty())
        assertTrue(r.anchors.any { it.candidateId == rest.id && it.at == t.plusSeconds(86400) })
        assertTrue(r.targetFindings().isEmpty())
    }
    @Test fun SevenHourDrivingConventionNeverBecomesAM3DailyRestAnchor() {
        val r = calculate(RestFixture().w(1020).r(420))
        assertTrue(r.anchors.none { it.range.start >= t })
        assertTrue(r.at(t).options.isEmpty())
        assertEquals(120 * 60L, r.targetFindings().single().missingRestSeconds)
    }
    @Test fun UnknownEarlierReductionHistoryHidesAvailabilityInsteadOfAssumingZero() {
        val r = calculate(RestFixture(weeklyPrefix = false).w(900).r(540))
        assertNull(r.finalCounters.single().provenRemaining)
        assertTrue(RestRestriction.LEFT_REDUCTION_HISTORY_MISSING in r.finalCounters.single().restrictions)
        assertTrue(r.windows.single().options.all { it.eligibility == RestEligibility.CONDITIONAL })
    }
    @Test fun OngoingNineHourRestCanStillBecomeRegularAndDoesNotConsumeAConfirmedReduction() {
        val f = RestFixture().w(660).r(540).r(120)
        val r = DailyRestCalculator.calculate(f.snapshot(withEnd = false))
        assertEquals(t.plusSeconds(20 * 3600), r.observedThrough)
        assertEquals(0, r.finalCounters.single().minimumObservedCount)
        assertEquals(1, r.finalCounters.single().maximumPossibleCount)
        assertNull(r.finalCounters.single().provenRemaining)
        assertTrue(r.anchors.none { it.range.start >= t })
        f.w(1)
        val closed = calculate(f)
        assertEquals(0, closed.finalCounters.single().minimumObservedCount)
        assertTrue(closed.anchors.any { it.at == t.plusSeconds(22 * 3600) })
    }
    @Test fun OngoingFortyFiveHourMinimumNeverInventsTheActualWeeklyRestEnd() {
        val f = RestFixture(priorReductions = 3).w(60).r(2820).r(60)
        val r = DailyRestCalculator.calculate(f.snapshot(withEnd = false))
        assertTrue(r.candidates.last().netRestSeconds >= 2700 * 60L)
        assertFalse(r.candidates.last().actualEndProved)
        assertTrue(r.anchors.none { it.range.start >= t })
        assertEquals(3, r.finalCounters.single().minimumObservedCount)
    }
    @Test fun WeeklyRestResetsOnlyTheDailyReductionLedgerAndNotM2ExtensionEvents() {
        val f = RestFixture().dl(570).r(540).dl(570).r(2700)
        val snapshot = f.snapshot()
        val driving = DrivingCalculator.calculate(snapshot)
        val r = DailyRestCalculator.calculate(snapshot, driving)
        assertEquals(2, driving.extensions.single { it.week.contains(t) }.observedOccurrences)
        assertEquals(0, r.finalCounters.single().minimumObservedCount)
        assertEquals(3, r.finalCounters.single().provenRemaining)
        assertFalse(driving.extensions.single { it.week.contains(t) }.provenAvailable != null)
    }
    @Test fun ReimportDoesNotDuplicateRestEventsAndKeepsEveryOrigin() {
        val f = RestFixture(priorReductions = 3).w(781).r(660)
        val one = DailyRestCalculator.calculate(f.snapshot())
        val two = DailyRestCalculator.calculate(f.snapshot(duplicate = true))
        assertEquals(one.findings.map { it.id }, two.findings.map { it.id })
        assertEquals(one.findings.map { it.missingRestSeconds }, two.findings.map { it.missingRestSeconds })
        assertNotEquals(one.provenanceRevision, two.provenanceRevision)
        assertEquals(2, two.sourceSnapshot.sources.size)
    }
    @Test fun UTCAndDSTDoNotSubtractLocalClockLabelsOrChangeResults() {
        val start = Instant.parse("2026-03-28T08:00:00Z")
        val snapshot = RestFixture(start).w(780).r(660).snapshot()
        val old = TimeZone.getDefault()
        try {
            TimeZone.setDefault(TimeZone.getTimeZone("Europe/Berlin")); val berlin = DailyRestCalculator.calculate(snapshot)
            TimeZone.setDefault(TimeZone.getTimeZone("America/Los_Angeles")); val other = DailyRestCalculator.calculate(snapshot)
            assertEquals(berlin, other)
            assertEquals(660 * 60L, berlin.candidates.last().netRestSeconds)
            assertTrue(berlin.findings.isEmpty())
        } finally { TimeZone.setDefault(old) }
    }
    @Test fun ACalculationFromAnotherInputRevisionIsRejected() {
        val a = RestFixture().w(780).r(660).snapshot()
        val b = RestFixture().w(781).r(660).snapshot()
        assertThrows(IllegalArgumentException::class.java) { DailyRestCalculator.calculate(a, DrivingCalculator.calculate(b)) }
    }
    @Test fun CancellationPropagatesAndCannotPublishAPartialCalculation() {
        val s = RestFixture().w(780).r(660).snapshot()
        val driving = DrivingCalculator.calculate(s)
        assertThrows(CancellationException::class.java) {
            DailyRestCalculator.calculate(s, driving, checkCancellation = { throw CancellationException() })
        }
    }
    @Test fun ResourceLimitNeverReturnsAGuaranteedBudgetOrDeficit() {
        val s = RestFixture().w(781).r(660).snapshot()
        val r = DailyRestCalculator.calculate(s, maxPathEvaluations = 1)
        assertFalse(r.alternativesComplete)
        assertTrue(r.findings.none { it.normalRuleDeficitIsProved })
        assertTrue(r.windows.all { it.provenSecondsUntilLatestRestStart == null })
        assertTrue(r.finalCounters.all { it.provenRemaining == null })
    }
}
