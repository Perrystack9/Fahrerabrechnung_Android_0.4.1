package de.fahrerabrechnung.domain.violation.weekly

import de.fahrerabrechnung.domain.violation.input.*
import de.fahrerabrechnung.domain.violation.rest.*
import java.time.Instant
import org.junit.Assert.*
import org.junit.Test

class InternationalWeeklyReferenceTest {
    @Test fun FourWeekRegularityShortfallUsesTheActualTwoLongestReducedBases() {
        // 44:59 and 44:58 miss 45h by 1 and 2 minutes. Neither is a 24h base.
        assertEquals(180L, WeeklyRules.minimumRestShortfall(listOf(2699L, 2698L, 1800L, 1440L).map { it * 60 }, 4, 2))
    }
    @Test fun MissingFourthRestAndMissingRegularityDoNotAssumeEveryReductionWasTwentyFourHours() {
        // 45h,30h,40h: another 24h plus 5h to the longest reduced base = 29h lower bound.
        assertEquals(29 * 3600L, WeeklyRules.minimumRestShortfall(listOf(45L, 30L, 40L).map { it * 3600 }, 4, 2))
    }
    @Test fun EmptyFourWeekProjectionNeedsTwoFortyFiveAndTwoTwentyFourHourBases() {
        assertEquals(138 * 3600L, WeeklyRules.minimumRestShortfall(emptyList(), 4, 2))
    }
    @Test fun AlreadyPresentRegularBasesDoNotCreateAnotherRegularityShortfall() {
        assertEquals(48 * 3600L, WeeklyRules.minimumRestShortfall(listOf(45L, 45L).map { it * 3600 }, 4, 2))
        assertEquals(0L, WeeklyRules.minimumRestShortfall(listOf(45L, 45L, 24L, 24L).map { it * 3600 }, 4, 2))
    }
    private fun at(text: String) = Instant.parse(text)
    private val from = at("2026-08-31T00:00:00Z")
    private fun standard(firstCountry: String? = "FR", secondCountry: String? = "BE") =
        WeeklyFixture(from).countries().r("2026-09-05T00:00:00Z", 2700)
            .r("2026-09-12T00:00:00Z", 1440).r("2026-09-19T00:00:00Z", 1440).apply {
                if (firstCountry != null) restCountry("2026-09-12T00:00:00Z", firstCountry)
                if (secondCountry != null) restCountry("2026-09-19T00:00:00Z", secondCountry)
            }
    private fun result(f: WeeklyFixture) = WeeklyRestCalculator.calculate(f.snapshot())

    @Test fun LG69Extra24HourRestDoesNotHaveToCreateAThirdDebt() {
        val f = WeeklyFixture(from).countries().r("2026-09-05T00:00:00Z", 2700)
            .r("2026-09-12T00:00:00Z", 1440).restCountry("2026-09-12T00:00:00Z", "FR")
            .r("2026-09-16T00:00:00Z", 1440).r("2026-09-20T00:00:00Z", 1620)
            .restCountry("2026-09-20T00:00:00Z", "BE").r("2026-09-25T00:00:00Z", 5040)
            .until("2026-09-28T12:00:00Z")
        val r = result(f)
        assertTrue(r.alternativesComplete)
        assertTrue(r.allocations.any { ledger -> ledger.debts.map { it.seconds }.sorted() == listOf(64800L, 75600L) &&
            ledger.uses.any { it.range.start == at("2026-09-16T00:00:00Z") && it.extra } })
    }
    @Test fun LG70Both21HourCompensationsPrecedeThe45HourBody() {
        val r = result(standard().r("2026-09-24T06:00:00Z", 5220).until("2026-09-28T00:00:00Z"))
        assertTrue(r.alternativesComplete)
        assertTrue(r.allocations.any { ledger ->
            ledger.internationalPairs.any { it.eligibility == RestEligibility.PROVED && it.compensationBeforeRegular == true } &&
                ledger.reservations.size == 2 && ledger.reservations.sumOf { it.seconds } == 42 * 3600L
        })
        r.allocations.flatMap { it.reservations }.groupBy { it.donorCandidateId }.values.forEach { group ->
            // Check each correlated allocation separately below; unioned alternatives can overlap.
            assertTrue(group.all { it.seconds == 21 * 3600L })
        }
        r.allocations.forEach { ledger -> assertTrue(ledger.reservations.sortedBy { it.range.start }
            .zipWithNext().all { (a, b) -> a.range.end <= b.range.start }) }
    }
    @Test fun LG71BeginningInResidenceCountryRulesTheInternationalExceptionOut() {
        val r = result(standard(secondCountry = "PL").r("2026-09-24T06:00:00Z", 5220).until("2026-09-28T00:00:00Z"))
        assertTrue(r.allocations.all { it.internationalPairs.isEmpty() })
        assertTrue(r.issues.any { it.reason == "NO_GLOBAL_ASSIGNMENT_WITNESS" })
    }
    @Test fun LG71BeginningInEmployerCountryRulesTheInternationalExceptionOut() {
        val r = result(standard(firstCountry = "DE").r("2026-09-24T06:00:00Z", 5220))
        assertTrue(r.allocations.all { it.internationalPairs.isEmpty() })
    }
    @Test fun LG71UnknownCountryIsAConcreteNeedAndNeverAnAssuredException() {
        val r = result(standard(secondCountry = null).r("2026-09-24T06:00:00Z", 5220))
        val pairs = r.allocations.flatMap { it.internationalPairs }
        assertTrue(pairs.any { it.eligibility == RestEligibility.CONDITIONAL })
        assertEquals(setOf(at("2026-09-19T00:00:00Z")), r.contextNeeds.filter { it.key == ContextKey.REST_START_COUNTRY }.map { it.at }.toSet())
        assertTrue(r.findings.none { it.normalRuleDeficitIsProved })
    }
    private fun twentyFourAnd25(packetStart: String) = WeeklyFixture(from).countries()
        .r("2026-09-05T00:00:00Z", 2700)
        .r("2026-09-12T07:00:00Z", 1440).restCountry("2026-09-12T07:00:00Z", "FR")
        .r("2026-09-19T07:00:00Z", 1500).restCountry("2026-09-19T07:00:00Z", "BE")
        .r(packetStart, 5160)
    @Test fun LG72TwentyFourPlus25Needs41HoursBefore45AndStartsAfter103Hours() {
        val r = result(twentyFourAnd25("2026-09-24T15:00:00Z"))
        val ledger = r.allocations.first { it.internationalPairs.any { p -> p.compensationBeforeRegular == true } }
        assertEquals(listOf(72000L, 75600L), ledger.debts.map { it.seconds }.sorted())
        val packet = ledger.uses.single { it.range.end == at("2026-09-28T05:00:00Z") }
        assertEquals(at("2026-09-26T08:00:00Z"), packet.range.start)
        assertEquals(162000, packet.seconds)
        assertEquals(0, r.deadlines.first { it.anchorActualEnd == at("2026-09-20T08:00:00Z") }.lateBySeconds)
    }
    @Test fun LG73StartingTheWholePacketAt144HoursMakesIts45HourBody41HoursLate() {
        val r = result(twentyFourAnd25("2026-09-26T08:00:00Z"))
        // Inspect the legally ordered allocation, not an unrelated unpaid/extra-rest alternative.
        val ordered = r.allocations.filter { it.internationalPairs.any { p -> p.compensationBeforeRegular == true } }
        assertFalse(ordered.isEmpty())
        val body = ordered.first().uses.single { it.range.start == at("2026-09-28T01:00:00Z") }
        assertEquals(162000, body.seconds)
        assertTrue(ordered.flatMap { it.findings }.any { it.law.id == "W02" && it.excessOrMissingSeconds == 2460 * 60L })
        assertTrue(r.findings.filter { it.law.id == "W02" }.none { it.normalRuleDeficitIsProved })
    }
    @Test fun LG74FourWeekWindowWithOnlyOneRegularRestCannotFulfilInternationalMinimum() {
        val f = standard().r("2026-09-26T00:00:00Z", 1440).restCountry("2026-09-26T00:00:00Z", "FR")
            .until("2026-09-28T00:00:00Z")
        val r = result(f)
        assertTrue(r.allocations.any { ledger -> ledger.windows.any { it.requiredCount == 4 &&
            it.range.start == from && it.state == WeeklyWindowState.NORMAL_RULE_DEFICIT } })
        assertTrue(r.findings.filter { it.law.id == "W07" }.all { WeeklyRestriction.L02_UTC_WEEK_PROJECTION_ONLY in it.restrictions })
    }
    @Test fun LG75OpenFourWeekWindowIsNotAFutureViolation() {
        val r = result(standard().until("2026-09-21T00:00:00Z"))
        assertTrue(r.allocations.flatMap { it.windows }.filter { it.requiredCount == 4 && it.range.start == from }
            .all { it.state == WeeklyWindowState.OPEN })
        assertTrue(r.findings.none { it.law.id == "W07" || it.law.id == "W08" })
    }
    @Test fun LG106BothDebtOrdersArePermittedButNoSuffixPaymentSatisfiesThePrefixRequirement() {
        val r = result(twentyFourAnd25("2026-09-24T15:00:00Z"))
        val ledgers = r.allocations.filter { it.internationalPairs.any { p -> p.compensationBeforeRegular == true } }
        assertEquals(2, ledgers.map { it.reservations.sortedBy { p -> p.range.start }.map { p -> p.seconds } }.distinct().size)
        assertTrue(ledgers.all { ledger -> val regular = ledger.uses.last();
            ledger.reservations.all { it.range.end <= regular.range.start } })
    }
    @Test fun LG107RollingFourWeekWindowsAreNotNonOverlappingBuckets() {
        val f = standard().r("2026-09-26T00:00:00Z", 5220)
            .r("2026-10-03T00:00:00Z", 1440).restCountry("2026-10-03T00:00:00Z", "FR")
            .r("2026-10-10T00:00:00Z", 1440).restCountry("2026-10-10T00:00:00Z", "BE")
            .until("2026-10-12T00:00:00Z")
        val r = result(f)
        assertTrue(r.allocations.flatMap { it.windows }.any { it.requiredCount == 4 &&
            it.range.start == at("2026-09-07T00:00:00Z") })
        assertTrue(r.allocations.flatMap { it.windows }.any { it.requiredCount == 4 &&
            it.range.start == at("2026-09-14T00:00:00Z") })
    }
    @Test fun LG108ConcreteCountryAnswerIsReusedAndRevisesTheCalculationWithoutChangingActivityData() {
        val f = standard(secondCountry = null).r("2026-09-24T06:00:00Z", 5220)
        val before = result(f)
        f.restCountry("2026-09-19T00:00:00Z", "BE")
        val after = result(f)
        assertNotEquals(before.inputRevision, after.inputRevision)
        assertEquals(before.sourceSnapshot.intervals, after.sourceSnapshot.intervals)
        assertTrue(after.contextNeeds.none { it.key == ContextKey.REST_START_COUNTRY })
        assertEquals(after.revision, result(f).revision)
    }
}
