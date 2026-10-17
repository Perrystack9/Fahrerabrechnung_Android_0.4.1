package de.fahrerabrechnung.domain.violation.weekly

import de.fahrerabrechnung.domain.violation.input.*
import de.fahrerabrechnung.domain.violation.rest.*
import java.time.Instant
import org.junit.Assert.*
import org.junit.Test

/** The historic night/week projections explicitly retain L02/L04; no display zone is a legal fact. */
class BusWeeklyReferenceTest {
    private fun at(text: String) = Instant.parse(text)
    private val monday = at("2026-09-07T00:00:00Z")
    private fun bus(postMinutes: Int = 5400, late: Int = 0, digital: Boolean? = true, manual: Boolean = false) =
        WeeklyFixture(monday.minusSeconds(162000), passengers = true, occasional = true)
            .r("2026-09-05T03:00:00Z", 2700).service(monday, digital = digital)
            .until(monday.plusSeconds(12 * 86400 + late * 60L)).apply { recorded.r(postMinutes, manual) }
    private fun result(f: WeeklyFixture) = WeeklyRestCalculator.calculate(f.snapshot())

    @Test fun LG77ExactlyTwelveDaysUsesTheConditional288HourProjection() {
        val r = result(bus())
        assertTrue(r.alternativesComplete)
        assertEquals(monday.plusSeconds(1036800), r.deadlines.first().deadline)
        assertEquals(0, r.deadlines.first().lateBySeconds)
        assertTrue(r.allocations.flatMap { it.buses }.any { it.eligibility == RestEligibility.PROVED })
        assertTrue(r.deadlines.all { it.provenSecondsRemaining == null })
    }
    @Test fun LG77OneMinuteAfterTwelveDaysIsOneMinuteLate() {
        val r = result(bus(late = 1))
        assertEquals(60, r.deadlines.first().lateBySeconds)
        assertTrue(r.findings.any { it.law.id == "W10" && it.excessOrMissingSeconds == 60L })
        assertTrue(r.findings.none { it.normalRuleDeficitIsProved })
    }
    @Test fun LG77OneMinuteBeforeTwelveDaysIsNotLate() {
        assertEquals(0, result(bus(late = -1)).deadlines.first().lateBySeconds)
    }
    @Test fun LG78NinetyHoursHaveTwoDistinctRegularBaseAllocationsAndOnePhysicalEnd() {
        val r = result(bus())
        val ledger = r.allocations.first { it.uses.count { u -> u.busPostAnchorId != null } == 2 }
        val parts = ledger.uses.filter { it.busPostAnchorId != null }
        assertEquals(listOf(WeeklyRestKind.REGULAR, WeeklyRestKind.REGULAR), parts.map { it.kind })
        assertEquals(listOf(162000L, 162000L), parts.map { it.seconds })
        assertEquals(parts.first().range.end, parts.last().range.start)
        assertFalse(parts.first().actualEndProved)
        assertTrue(parts.last().actualEndProved)
        assertEquals(1, r.candidates.count { it.range.start == monday.plusSeconds(1036800) })
        assertTrue(ledger.debts.isEmpty())
    }
    @Test fun LG78SixtyNineHoursHave45And24AndASeparate21HourDebt() {
        val r = result(bus(postMinutes = 4140))
        val debts = r.allocations.flatMap { it.debts }.filter { it.afterBusDerogation }
        assertFalse(debts.isEmpty())
        assertTrue(debts.all { it.seconds == 75600L && it.due == at("2026-10-12T00:00:00Z") })
        assertTrue(r.allocations.filter { it.debts.any { d -> d.afterBusDerogation } }.all { ledger ->
            ledger.uses.filter { it.busPostAnchorId != null }.map { it.seconds } == listOf(162000L, 86400L) })
    }
    @Test fun LG78FortyFiveHoursAloneDoNotMeetTheBusPostRestRequirement() {
        val r = result(bus(postMinutes = 2700))
        assertTrue(r.allocations.all { it.uses.none { u -> u.busPostAnchorId != null } })
        assertTrue(r.findings.any { it.law.id == "W11" && it.excessOrMissingSeconds == 86400L })
        assertTrue(r.findings.none { it.normalRuleDeficitIsProved })
    }
    private fun night(minutes: Int, crew: Boolean = false, digital: Boolean? = true): WeeklyFixture {
        val f = WeeklyFixture(monday.minusSeconds(162000), passengers = true, occasional = true)
            .r("2026-09-05T03:00:00Z", 2700).service(monday, digital = digital).until("2026-09-09T22:00:00Z")
        f.recorded.d(minutes, crew)
        return f.r("2026-09-19T00:00:00Z", 5400)
    }
    @Test fun LG79NightDriving179MinutesHasNoThreeHourExcess() {
        assertTrue(result(night(179)).allocations.flatMap { it.buses }.flatMap { it.nightProjections }.all { it.excessSeconds == 0L })
    }
    @Test fun LG79NightDriving180MinutesHasNoThreeHourExcess() {
        assertTrue(result(night(180)).allocations.flatMap { it.buses }.flatMap { it.nightProjections }.all { it.excessSeconds == 0L })
    }
    @Test fun LG79NightDriving181MinutesHasOneConditionalMinuteAndDoesNotCloseL02() {
        val r = result(night(181))
        assertTrue(r.findings.any { it.law.id == "W12" && it.excessOrMissingSeconds == 60L })
        assertTrue(r.findings.filter { it.law.id == "W12" }.all { WeeklyRestriction.L02_NIGHT_TIME_UNCONFIRMED in it.restrictions })
        assertTrue(r.findings.none { it.normalRuleDeficitIsProved })
    }
    @Test fun LG80RecordedCrewProvidesTheNightAlternativeWithoutASecondCardOrRoutineConfirmation() {
        val r = result(night(181, crew = true))
        assertTrue(r.allocations.flatMap { it.buses }.flatMap { it.nightProjections }.all { it.allDrivingCrewRecorded && it.excessSeconds == 0L })
        assertTrue(r.contextNeeds.none { it.reason.contains("CREW") || it.reason.contains("SECOND_CARD") })
    }
    @Test fun LG80CrewDoesNotProveTheMissingDigitalVehicleCondition() {
        val r = result(night(181, crew = true, digital = null))
        assertTrue(r.contextNeeds.any { it.key == ContextKey.BUS_DIGITAL_TACHOGRAPH_ANNEX_IB })
        assertTrue(r.allocations.flatMap { it.buses }.all { it.eligibility == RestEligibility.CONDITIONAL })
    }
    @Test fun KnownNonDigitalVehicleRulesThePost2014ExceptionOut() {
        val r = result(bus(digital = false))
        assertTrue(r.allocations.flatMap { it.buses }.isEmpty())
        assertEquals(518400, r.deadlines.first().deadline.epochSecond - monday.epochSecond)
    }
    private fun historic(start: String, international: Boolean, digital: Boolean? = true, foreign24: Boolean? = true): WeeklyFixture {
        val end = at(start)
        return WeeklyFixture(end.minusSeconds(162000), passengers = true, occasional = true)
            .apply { recorded.r(2700) }.service(end, digital = digital, international = international, foreign24 = foreign24)
            .until(end.plusSeconds(12 * 86400)).apply { recorded.r(5400) }
    }
    @Test fun LG76BeforeMay2024ADomesticServiceCannotClaimTheTwelveDayException() {
        assertTrue(result(historic("2024-04-01T00:00:00Z", international = false)).allocations.flatMap { it.buses }.isEmpty())
    }
    @Test fun LG76AConfirmedCurrentDomesticOccasionalServiceCanHaveTheProjection() {
        assertFalse(result(bus()).allocations.flatMap { it.buses }.isEmpty())
    }
    @Test fun LG116HistoricInternationalServiceNeedsItsForeign24HourCondition() {
        assertTrue(result(historic("2024-04-01T00:00:00Z", true, foreign24 = false)).allocations.flatMap { it.buses }.isEmpty())
        val unknown = result(historic("2024-04-01T00:00:00Z", true, foreign24 = null))
        assertTrue(unknown.contextNeeds.any { it.key == ContextKey.OCCASIONAL_FOREIGN_STAY_24H })
        assertTrue(unknown.findings.none { it.normalRuleDeficitIsProved })
    }
    @Test fun BeforeJune2010ThereIsNoBusTwelveDayDerogation() {
        assertTrue(result(historic("2010-04-01T00:00:00Z", true)).allocations.flatMap { it.buses }.isEmpty())
    }
    @Test fun Before2014TheLaterDigitalConditionIsNotRetroactivelyRequired() {
        val r = result(historic("2013-09-02T00:00:00Z", true, digital = null))
        assertFalse(r.allocations.flatMap { it.buses }.isEmpty())
        assertTrue(r.contextNeeds.none { it.key == ContextKey.BUS_DIGITAL_TACHOGRAPH_ANNEX_IB })
    }
    @Test fun AServiceAcrossTheMay2024BoundaryRemainsHistoricallyConditional() {
        val r = result(historic("2024-05-13T00:00:00Z", false))
        assertTrue(r.allocations.flatMap { it.buses }.all { WeeklyRestriction.HISTORICAL_ACTIVATION_UNCONFIRMED in it.restrictions })
        assertTrue(r.findings.none { it.normalRuleDeficitIsProved })
    }
    @Test fun RecordedManualPostRestIsUsedWithItsUnchangedOriginSemantics() {
        val r = result(bus(manual = true))
        val post = r.candidates.single { it.range.start == at("2026-09-19T00:00:00Z") }
        assertTrue(post.rest.all { it.manualTachograph })
        assertTrue(r.allocations.any { it.uses.count { u -> u.candidateId == post.id && u.busPostAnchorId != null } == 2 })
    }
    @Test fun MerePassengersWithoutAnOccasionalServiceDoNotCreateABusPrivilege() {
        val f = WeeklyFixture(monday.minusSeconds(162000), passengers = true)
            .r("2026-09-05T03:00:00Z", 2700).r("2026-09-19T00:00:00Z", 5400)
        assertTrue(result(f).allocations.flatMap { it.buses }.isEmpty())
    }
}
