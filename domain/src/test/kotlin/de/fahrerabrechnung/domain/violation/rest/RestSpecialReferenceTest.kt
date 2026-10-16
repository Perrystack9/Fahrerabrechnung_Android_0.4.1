package de.fahrerabrechnung.domain.violation.rest

import java.time.Instant
import org.junit.Assert.*
import org.junit.Test

/** Fixed M0 arithmetic, independently specified before these implementations. */
class RestSpecialReferenceTest {
    private val t = Instant.parse("2026-09-07T00:00:00Z")
    private fun bus(days: Int = 6, safety: Boolean? = true, start: Instant = t) =
        RestFixture(start, priorReductions = 3, passengers = true, occasional = true)
            .service(start, start.plusSeconds(days * 86400L), safety = safety)
    private fun RestFixture.busDay(driving: Int = 420) = apply { dl(driving).w(795 - driving).r(660) }
    private fun RestFixture.normalDay() = apply { w(780).r(660) }
    private fun calculate(f: RestFixture) = DailyRestCalculator.calculate(f.snapshot())
    private fun ferry(net: Int = 660, firstWork: Int = 720, secondGap: Int = 30,
        gen2: Boolean = true, context: Boolean = true): RestFixture {
        val f = RestFixture(priorReductions = 3).w(firstWork)
        val from = f.cursor
        f.r(180).w(30).r(180).w(secondGap).r(net - 360)
        f.ferryMarker(from, gen2 = gen2)
        if (gen2) f.ferryMarker(f.cursor, end = true)
        if (context) f.ferryContext(from)
        return f
    }
    @Test fun LG50And98ProvedTwentyFiveHoursDoesNotCreateANormalTwentyFourHourDeficit() {
        val f = bus().normalDay().normalDay(); val target = f.cursor
        val r = calculate(f.busDay())
        val window = r.windows.single { it.referenceStart == target }
        assertEquals(RestWindowMode.OCCASIONAL_BUS_25, window.mode)
        assertEquals(RestWindowState.FULFILLED, window.state)
        assertEquals(39600L, window.creditedSeconds)
        assertTrue(r.findings.none { it.assessedRange.start == target })
    }
    @Test fun LG51SixDayServiceAllowsOnlyOneTwentyFiveHourUse() {
        val f = bus().normalDay().normalDay(); val first = f.cursor
        f.busDay(); val second = f.cursor
        val r = calculate(f.busDay())
        assertTrue(r.windows.any { it.referenceStart == first && it.mode == RestWindowMode.OCCASIONAL_BUS_25 })
        assertEquals(RestWindowMode.SOLO_24, r.windows.single { it.referenceStart == second }.mode)
        assertEquals(3600L, r.findings.single { it.assessedRange.start == second }.missingRestSeconds)
    }
    @Test fun LG52EightDayServiceAllowsTwoUsesButNotThree() {
        val f = bus(8).normalDay(); val first = f.cursor
        f.busDay(); val second = f.cursor
        f.busDay(); val third = f.cursor
        val r = calculate(f.busDay())
        assertTrue(r.windows.filter { it.referenceStart in setOf(first, second) }.all {
            it.mode == RestWindowMode.OCCASIONAL_BUS_25 && it.state == RestWindowState.FULFILLED })
        assertEquals(RestWindowMode.SOLO_24, r.windows.single { it.referenceStart == third }.mode)
        assertEquals(3600L, r.findings.single { it.assessedRange.start == third }.missingRestSeconds)
    }
    @Test fun OrdinaryOnTimeDaysDoNotSpendTheSingleServiceException() {
        val f = bus().normalDay().normalDay().normalDay().normalDay(); val target = f.cursor
        val r = calculate(f.busDay())
        assertEquals(RestWindowMode.OCCASIONAL_BUS_25, r.windows.single { it.referenceStart == target }.mode)
        assertTrue(r.findings.none { it.assessedRange.start == target })
    }
    @Test fun LG51SevenHoursAndOneMinuteDrivingDoesNotQualify() {
        val f = bus().normalDay();val target = f.cursor
        val r = calculate(f.busDay(421))
        assertTrue(r.windows.filter { it.referenceStart == target }.all { it.mode == RestWindowMode.SOLO_24 })
        assertEquals(3600L, r.findings.single { it.assessedRange.start == target }.missingRestSeconds)
    }
    @Test fun LG51FiveDayServiceAndFailedSafetyDoNotQualify() {
        for (f in listOf(bus(5), bus(safety = false))) {
            f.normalDay();val target = f.cursor
            val r = calculate(f.busDay())
            assertTrue(r.windows.filter { it.referenceStart == target }.all { it.mode == RestWindowMode.SOLO_24 })
        }
    }
    @Test fun LG53UnknownSafetyDoesNotBecomeAssuredComplianceOrANormalRuleConviction() {
        val f = bus(safety = null).normalDay();val target = f.cursor
        val r = calculate(f.busDay())
        assertTrue(r.windows.filter { it.referenceStart == target }.all { it.provenSecondsUntilLatestRestStart == null })
        assertTrue(r.findings.filter { it.assessedRange.start == target }.none { it.normalRuleDeficitIsProved })
        assertTrue(r.contextNeeds.any { it.key.name == "ROAD_SAFETY_PRESERVED" })
    }
    @Test fun HistoricalBusActivationIsNotAppliedBefore2024AndIsConditionalAtTheDateBoundary() {
        for (date in listOf("2024-05-20T00:00:00Z", "2024-05-22T00:00:00Z")) {
            val start=Instant.parse(date)
            val r=calculate(bus(start=start).busDay())
            if (date.contains("05-20")) assertTrue(r.windows.all { it.mode == RestWindowMode.SOLO_24 })
            else assertTrue(r.windows.filter { it.mode == RestWindowMode.OCCASIONAL_BUS_25 }.all {
                RestRestriction.HISTORICAL_ACTIVATION_UNCONFIRMED in it.restrictions })
        }
    }
    @Test fun LG81ElevenNetHoursWithTwoThirtyMinuteInterruptionsFulfilTheDailyWindow() {
        val r=calculate(ferry())
        val w=r.windows.single { it.referenceStart == t }
        assertEquals(RestWindowState.FULFILLED,w.state)
        assertEquals(39600L,w.creditedSeconds)
        assertTrue(w.law.article.contains("Art.9"))
        assertTrue(r.findings.none { it.assessedRange.start == t })
        assertEquals(3600L,r.candidates.single { it.interruptions.size==2 }.interruptionSeconds)
    }
    @Test fun LG82SixtyOneMinutesAndThirdInterruptionDoNotQualify() {
        val three=RestFixture(priorReductions=3).w(720);val from=three.cursor
        three.r(180).w(20).r(180).w(20).r(180).w(20).r(120).ferryMarker(from)
            .ferryMarker(three.cursor,end=true).ferryContext(from)
        for(f in listOf(ferry(firstWork=719,secondGap=31),three)) {
            val r=calculate(f)
            assertTrue(r.windows.single { it.referenceStart==t }.options.isEmpty())
            assertTrue(r.findings.any { it.assessedRange.start==t })
        }
    }
    @Test fun LG83Gen1PointWithConcreteContextWorksWithoutAnInventedFerryEnd() {
        val r=calculate(ferry(gen2=false))
        assertEquals(RestWindowState.FULFILLED,r.windows.single { it.referenceStart==t }.state)
        assertTrue(r.candidates.filter { it.interrupted }.none { it.recordedFerryEnd })
    }
    @Test fun MissingDecisiveFerryContextDoesNotYieldAnAssuredVerdict() {
        val r=calculate(ferry(context=false))
        assertTrue(r.windows.single { it.referenceStart==t }.options.all { it.eligibility==RestEligibility.CONDITIONAL })
        assertTrue(r.findings.none { it.normalRuleDeficitIsProved })
        assertTrue(r.contextNeeds.isNotEmpty())
    }
    @Test fun LG84InterruptedNineNetHoursCannotBeAReducedDailyRest() {
        val f=RestFixture().w(890);val from=f.cursor
        f.r(270).w(10).r(270).ferryMarker(from).ferryMarker(f.cursor,end=true).ferryContext(from)
        val r=calculate(f)
        assertTrue(r.windows.single { it.referenceStart==t }.options.isEmpty())
        assertTrue(r.findings.any { it.assessedRange.start==t })
    }
    @Test fun LG88SplitThreePlusNineCanUseAFerryInterruptionInItsSecondPart() {
        val f=RestFixture(priorReductions=3).w(180).r(180).w(300).r(240);val from=f.cursor
        f.w(30).r(300).ferryMarker(from).ferryMarker(f.cursor,end=true).ferryContext(from.minusSeconds(240*60L))
        val r=calculate(f)
        assertTrue(r.windows.single { it.referenceStart==t }.options.any { it.kind==DailyRestKind.SPLIT_REGULAR })
        assertTrue(r.findings.none { it.assessedRange.start==t })
    }
    @Test fun LG109SixHundredFiftyNineMinutesHaveOneMinuteMissingWithExactFerryOnset() {
        val r=calculate(ferry(net=659,firstWork=721))
        val finding=r.findings.single { it.assessedRange.start==t }
        assertEquals(60L,finding.missingRestSeconds)
        assertEquals(659*60L,finding.creditedRestSeconds)
        // At19:00 the second interruption starts;60min would still fit, its61st minute of combined loss does not.
        assertEquals(t.plusSeconds(19*3600),finding.mathematicalOnset)
        assertEquals(t.plusSeconds(19*3600+60),finding.firstProvableAt)
    }
    @Test fun LG110ContextRevisionReevaluatesWithoutChangingActivitiesOrOriginalEvidence() {
        val f=ferry(context=false);val before=f.snapshot();val a=DailyRestCalculator.calculate(before)
        f.ferryContext(t.plusSeconds(720*60L));val after=f.snapshot();val b=DailyRestCalculator.calculate(after)
        assertEquals(before.intervals,after.intervals)
        assertEquals(before.sources,after.sources)
        assertNotEquals(a.inputRevision,b.inputRevision)
        assertEquals(RestWindowState.FULFILLED,b.windows.single { it.referenceStart==t }.state)
    }
    @Test fun R07LongerFerryRestAlternativesKeepTheirActualEndAndWeeklyPrerequisites() {
        val f=RestFixture().w(60);val from=f.cursor
        f.r(1350).w(60).r(1350).ferryMarker(from).ferryMarker(f.cursor,end=true)
            .ferryContext(from,facility="CABIN",scheduledMinutes=480)
        val r=calculate(f)
        val full=r.candidates.single { it.interrupted && it.range.start==from }
        assertTrue(full.weekly.any { it.kind==WeeklyRestKind.REGULAR && it.eligibility==RestEligibility.PROVED })
        assertTrue(r.anchors.any { it.candidateId==full.id && it.at==f.cursor })
        assertTrue(r.alternativesComplete)
    }
}
