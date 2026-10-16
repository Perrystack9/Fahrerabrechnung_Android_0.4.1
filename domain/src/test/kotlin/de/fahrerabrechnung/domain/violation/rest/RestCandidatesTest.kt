package de.fahrerabrechnung.domain.violation.rest

import de.fahrerabrechnung.domain.violation.input.*
import java.time.Instant
import org.junit.Assert.*
import org.junit.Test

class RestCandidatesTest {
    private val t = Instant.parse("2026-09-07T00:00:00Z")
    private fun RestFixture.candidates() = RestCandidates.build(snapshot()) {}
    private fun ferry(net: Int = 660, firstGap: Int = 30, secondGap: Int = 30,
        start: Instant = t): RestFixture {
        val f = RestFixture(start).w(720).r(180).w(firstGap).r(180).w(secondGap).r(net - 360)
        return f.ferryMarker(start.plusSeconds(720 * 60L)).ferryMarker(f.cursor, end = true)
            .ferryContext(start.plusSeconds(720 * 60L))
    }
    @Test fun LG99ManualRestIsKnownButWithdrawalUnknownIsNotARestCandidate() {
        val manual = RestFixture().w(900).r(540, manual = true).candidates().last()
        assertEquals(540 * 60L, manual.netRestSeconds)
        assertTrue(manual.rest.all { it.manualTachograph })
        val unknown = RestFixture().w(900).u(540).candidates()
        assertTrue(unknown.none { it.range.start >= t })
    }
    @Test fun LG101And103TwentyThreeHoursIsNotAWeeklyCandidate() {
        for (minutes in listOf(1380, 1439)) {
            val rest = RestFixture().w(60).r(minutes).candidates().last()
            assertTrue(rest.weekly.isEmpty()); assertTrue(rest.actualEndProved)
        }
        for (minutes in listOf(1440, 2699, 2700)) {
            val rest = RestFixture().w(60).r(minutes).candidates().last()
            assertTrue(rest.weekly.any { it.kind == WeeklyRestKind.REDUCED })
            assertEquals(minutes >= 2700, rest.weekly.any { it.kind == WeeklyRestKind.REGULAR })
        }
    }
    @Test fun LG81And109FerryCreditsOnlyNetRestWithTwoInterruptionsAndAllOrigins() {
        val rest = ferry().candidates().single { it.interruptions.size == 2 }
        assertEquals(660 * 60L, rest.netRestSeconds); assertEquals(60 * 60L, rest.interruptionSeconds)
        assertEquals(RestEligibility.PROVED, rest.eligibility)
        assertTrue(rest.recordedFerryEnd)
        assertEquals(2, rest.rest.flatMap { it.support }.flatMap { it.begin }.map { it.source.application }.distinct().size)
    }
    @Test fun LG82SixtyOneMinutesAndThirdInterruptionAreRejected() {
        assertEquals(RestEligibility.REJECTED, ferry(firstGap = 31).candidates().single { it.interruptions.size == 2 }.eligibility)
        val f = RestFixture().w(600).r(180).w(20).r(180).w(20).r(180).w(20).r(120)
        f.ferryMarker(t.plusSeconds(600 * 60L)).ferryContext(t.plusSeconds(600 * 60L))
        assertEquals(RestEligibility.REJECTED, f.candidates().single { it.interruptions.size == 3 }.eligibility)
    }
    @Test fun LG83G1StartDoesNotInventATransportEndButConcretePrerequisitesCanQualify() {
        val f = RestFixture().w(720).r(180).w(30).r(480)
        val start = t.plusSeconds(720 * 60L)
        f.ferryMarker(start, gen2 = false).ferryContext(start)
        val rest = f.candidates().single { it.interrupted && it.range.start >= t }
        assertFalse(rest.recordedFerryEnd); assertEquals(RestEligibility.PROVED, rest.eligibility)
    }
    @Test fun LG87And110MissingFerryPrerequisitesStayConditionalAndAnswersRemoveNeeds() {
        val f = RestFixture(prefix = false).r(1350).w(60).r(1350).ferryMarker(t)
        val open = f.candidates().single { it.interrupted }
        assertEquals(RestEligibility.CONDITIONAL, open.weekly.single { it.kind == WeeklyRestKind.REGULAR }.eligibility)
        assertTrue(open.needs.isNotEmpty())
        f.ferryContext(t, facility = "CABIN", scheduledMinutes = 480)
        val known = f.candidates().single { it.interrupted }
        assertEquals(RestEligibility.PROVED, known.weekly.single { it.kind == WeeklyRestKind.REGULAR }.eligibility)
        assertTrue(known.weekly.flatMap { it.needs }.isEmpty())
        assertEquals(known.id, f.candidates().single { it.interrupted }.id)
    }
    @Test fun LG86Scheduled479And480AreDifferentFromRecorded46HourElapsedRest() {
        for (schedule in listOf(479L, 480L)) {
            val f = RestFixture(prefix = false).r(1350).w(60).r(1350).ferryMarker(t)
                .ferryContext(t, facility = "CABIN", scheduledMinutes = schedule)
            val rest = f.candidates().single { it.interrupted }
            assertEquals(2700 * 60L, rest.netRestSeconds)
            assertEquals(if (schedule == 480L) RestEligibility.PROVED else RestEligibility.REJECTED,
                rest.weekly.single { it.kind == WeeklyRestKind.REGULAR }.eligibility)
        }
    }
    @Test fun LG85WeeklyFerryExceptionIsNotAppliedBefore2020() {
        for ((start, eligible) in listOf(Instant.parse("2020-08-17T00:00:00Z") to false,
            Instant.parse("2020-08-22T00:00:00Z") to true)) {
            val f = RestFixture(start, prefix = false).r(720).w(30).r(720).ferryMarker(start).ferryContext(start)
            val rest = f.candidates().single { it.interrupted }
            assertEquals(if (eligible) RestEligibility.PROVED else RestEligibility.REJECTED,
                rest.weekly.single().eligibility)
        }
    }
    @Test fun HistoricalChangeCrossingIsAnExplicitlyConditionalWeeklyCandidate() {
        val start = Instant.parse("2020-08-19T23:00:00Z")
        val f = RestFixture(start, prefix = false).r(720).w(30).r(720).ferryMarker(start).ferryContext(start)
        assertEquals(RestEligibility.CONDITIONAL, f.candidates().single { it.interrupted }.weekly.single().eligibility)
    }
    @Test fun OngoingRestAtMidnightHasAProvedMinimumButNoProvedActualEnd() {
        // A real next-day REST marker closes the known prefix; no work/change ends the rest.
        val f = RestFixture().w(60).r(1380).r(60)
        val snapshot = f.snapshot(withEnd = false)
        val rest = RestCandidates.build(snapshot) {}.last()
        assertTrue(rest.netRestSeconds >= 660 * 60L)
        assertFalse(rest.actualEndProved)
        assertEquals(t.plusSeconds(86400), rest.range.end)
    }
    @Test fun ExactDuplicateApplicationsDoNotDoubleRestOrLoseProvenance() {
        val f = RestFixture().w(780).r(660)
        val one = RestCandidates.build(f.snapshot()) {}.last()
        val two = RestCandidates.build(f.snapshot(duplicate = true)) {}.last()
        assertEquals(one.id, two.id); assertEquals(one.netRestSeconds, two.netRestSeconds)
        assertEquals(2, two.rest.flatMap { it.support }.flatMap { it.begin }.map { it.source.sessionId }.distinct().size)
    }
    @Test fun NewExceptionContextCannotBeASilentProfileWideDefault() {
        for (key in listOf(ContextKey.FERRY_ACCOMPANIES_VEHICLE, ContextKey.SINGLE_OCCASIONAL_SERVICE,
            ContextKey.ROAD_SAFETY_PRESERVED, ContextKey.WORKING_CONDITIONS_PRESERVED,
            ContextKey.APPLICABLE_MAX_WORKING_TIME_RESPECTED)) {
            assertThrows(IllegalArgumentException::class.java) {
                ContextAssertion(key, ContextValue.Flag(true), ContextSubject(ContextSubjectKind.PROFILE, "P1"),
                    t, null, ContextOrigin.USER_CONTEXT, "synthetic", t)
            }
        }
    }
    @Test fun LG109AllNetWeeklyThresholdsUseNetRestAndNeverTheElapsedFerryTime() {
        for(net in listOf(659,660,1439,1440,2699,2700)) {
            val first=if(net>=2699) 900 else 180
            val f=RestFixture(prefix=false).r(first).w(30).r(first).w(30).r(net-2*first)
                .ferryMarker(t).ferryContext(t,facility="CABIN",scheduledMinutes=480)
            val c=f.candidates().single {it.interruptions.size==2 && it.range.start==t}
            assertEquals(net*60L,c.netRestSeconds)
            assertEquals((net+60)*60L,java.time.Duration.between(c.range.start,c.range.end).seconds)
            assertEquals(net>=1440,c.weekly.any {it.kind==WeeklyRestKind.REDUCED && it.eligibility==RestEligibility.PROVED})
            assertEquals(net>=2700,c.weekly.any {it.kind==WeeklyRestKind.REGULAR && it.eligibility==RestEligibility.PROVED})
        }
    }
    @Test fun SleepingFacilityNoneDoesNotBecomeAnOptimisticValidFerryRest() {
        val f=RestFixture().w(720).r(180).w(30).r(480).ferryMarker(t.plusSeconds(720*60L))
            .ferryContext(t.plusSeconds(720*60L),facility="NONE")
        val c=f.candidates().single {it.interrupted && it.range.start>=t}
        assertEquals(RestEligibility.REJECTED,c.eligibility)
        assertTrue(RestRestriction.FERRY_FACILITY_RULED_OUT in c.restrictions)
        assertTrue(DailyRestCalculator.calculate(f.snapshot()).windows.single().options.isEmpty())
    }
    @Test fun FerryBeginAtPreviousRestEndAndEndAtNewRestStartDoNotRetroactivelyReclassifyEither() {
        val f=RestFixture().w(60).r(660).w(30);val next=f.cursor
        f.r(660).ferryMarker(t.plusSeconds(720*60L)).ferryMarker(next,end=true)
        val c=f.candidates().filter {it.range.start>=t && !it.interrupted}
        assertEquals(2,c.size)
        assertTrue(c.all {it.ferryPoints.isEmpty() && it.eligibility==RestEligibility.PROVED})
    }

}
