package de.fahrerabrechnung.domain.violation.weekly

import de.fahrerabrechnung.domain.violation.input.*
import de.fahrerabrechnung.domain.violation.rest.*
import java.time.Instant
import org.junit.Assert.*
import org.junit.Test

class WeeklyGuardRegressionTest {
    private fun at(text: String) = Instant.parse(text)
    private val t = at("2026-09-07T00:00:00Z")
    private fun countries(first: Int = 0x11, second: Int = 0x06) = WeeklyFixture(at("2026-08-31T00:00:00Z")).countries()
        .r("2026-09-05T00:00:00Z", 2700).r("2026-09-12T00:00:00Z", 1440)
        .r("2026-09-19T00:00:00Z", 1440)
        .countryPoint("2026-09-12T00:00:00Z", first).countryPoint("2026-09-19T00:00:00Z", second, border = true)
        .r("2026-09-24T06:00:00Z", 5220)

    @Test fun RecordedCountriesAtBothActualStartsReplaceUnnecessaryCountryQuestions() {
        val r = WeeklyRestCalculator.calculate(countries().snapshot())
        val pairs = r.allocations.flatMap { it.internationalPairs }
        assertTrue(pairs.any { it.eligibility == RestEligibility.PROVED })
        assertTrue(r.contextNeeds.none { it.key == ContextKey.REST_START_COUNTRY })
        assertTrue(pairs.filter { it.eligibility == RestEligibility.PROVED }.all { it.locationEvidence.size == 2 })
        assertTrue(pairs.flatMap { it.contextEvidence }.any { it.startsWith("point:") })
    }
    @Test fun ContradictingRecordedAndDeclaredCountriesCannotCreateAnAssuredInternationalPrerequisite() {
        val f = countries().restCountry("2026-09-19T00:00:00Z", "DE")
        val r = WeeklyRestCalculator.calculate(f.snapshot())
        assertTrue(r.allocations.flatMap { it.internationalPairs }.all { it.eligibility != RestEligibility.PROVED })
        assertTrue(r.contextNeeds.any { it.reason == "RECORDED_REST_START_COUNTRY_CONFLICT" })
        assertTrue(r.findings.none { it.normalRuleDeficitIsProved })
    }
    @Test fun RecordedStartInEmployerCountryRulesExceptionOutWithoutOverridingItByAMissingContext() {
        assertTrue(WeeklyRestCalculator.calculate(countries(second = 0x0d).snapshot()).allocations.flatMap { it.internationalPairs }.isEmpty())
    }
    @Test fun AnEarlierCountryPointIsNotExtendedAcrossTheUnknownLocationUntilRestStart() {
        val f = WeeklyFixture(at("2026-08-31T00:00:00Z")).countries().r("2026-09-05T00:00:00Z", 2700)
            .r("2026-09-12T00:00:00Z", 1440).r("2026-09-19T00:00:00Z", 1440)
            .countryPoint("2026-09-11T23:59:00Z", 0x11).countryPoint("2026-09-18T23:59:00Z", 0x06)
            .r("2026-09-24T06:00:00Z", 5220)
        val r = WeeklyRestCalculator.calculate(f.snapshot())
        assertEquals(2, r.contextNeeds.filter { it.key == ContextKey.REST_START_COUNTRY }.map { it.at }.distinct().size)
    }
    @Test fun NoInformationReservedAndWorldCodesDoNotBecomeACountry() {
        for (raw in listOf("0", "51", "58", "252", "253", "254", "255", "999", "FR", "-1"))
            assertNull(raw, RecordedRestCountry.country(raw))
    }
    @Test fun NationNumericIsNotConfusedWithIsoOrBumperAlpha() {
        assertEquals("FR", RecordedRestCountry.country("17"))
        assertEquals("FO", RecordedRestCountry.country("20"))
        assertEquals("DE", RecordedRestCountry.country("13"))
        assertEquals("BE", RecordedRestCountry.country("6"))
        assertEquals("PL", RecordedRestCountry.country("40"))
        assertEquals("GB", RecordedRestCountry.canonical("UK"))
    }
    private fun ferry(net: Int = 1440, gap: Int = 60, complete: Boolean = true,
        date: Instant = t): RestFixture = RestFixture(date, prefix = false).r(net / 2).w(gap).r(net - net / 2)
        .ferryMarker(date).apply { if (complete) ferryContext(date, facility = "CABIN", scheduledMinutes = 480) }
    @Test fun FerryWeeklyUseCountsOnlyNetRestAndKeepsPhysicalInterruptionsAndOrigins() {
        val r = WeeklyRestCalculator.calculate(ferry().snapshot())
        val id = r.candidates.single { it.interrupted }.id
        assertTrue(r.allocations.any { ledger -> ledger.uses.any { it.candidateId == id && it.seconds == 86400L } })
        assertEquals(90000L, r.candidates.single { it.id == id }.range.end.epochSecond - t.epochSecond)
        assertTrue(r.allocations.flatMap { it.uses }.filter { it.candidateId == id }.all { it.base.sumOf { s -> s.seconds } == 86400L })
    }
    @Test fun FerryMissingPrerequisitesRemainConditionalWithoutInventingWeeklyCredit() {
        val r = WeeklyRestCalculator.calculate(ferry(complete = false).snapshot())
        assertTrue(r.allocations.flatMap { it.uses }.all { WeeklyRestriction.REST_PREREQUISITE_UNCONFIRMED in it.restrictions })
        assertTrue(r.findings.none { it.normalRuleDeficitIsProved })
    }
    @Test fun FerrySixtyOneMinutesCannotCreateACombinedWeeklyRest() {
        val r = WeeklyRestCalculator.calculate(ferry(gap = 61).snapshot())
        assertTrue(r.allocations.all { it.uses.isEmpty() })
    }
    @Test fun Pre2020FerryWeeklyExceptionIsNotAppliedToOldRecordings() {
        val r = WeeklyRestCalculator.calculate(ferry(date = at("2020-08-17T00:00:00Z")).snapshot())
        assertTrue(r.allocations.all { it.uses.isEmpty() })
    }
    @Test fun AnArticleSpecificWeeklyExemptionDoesNotExemptDailyRestOrGrantAWeeklyBudget() {
        val f = RestFixture().w(900).r(540).assertion(ContextKey.OPERATION_COUNTRY, ContextValue.Text("DE"),
            ContextSubject(ContextSubjectKind.PROFILE, "P1"), t.minusSeconds(162000), null)
        val source = f.snapshot()
        val adjustment = ReviewedScopeAdjustment(InputRange(source.intervals.first().range.start, checkNotNull(source.latestClosedEnd)),
            "DE", setOf(ScopeArticle.WEEKLY_REST_8), "13", "https://example.invalid/synthetic-weekly-only",
            "a".repeat(64), "synthetic-reviewed-rule", true)
        val adjusted = f.snapshot(reviewed = listOf(adjustment))
        assertNotEquals(source.revision, adjusted.revision)
        val r = WeeklyRestCalculator.calculate(adjusted)
        assertTrue(r.deadlines.all { it.provenSecondsRemaining == null })
        assertTrue(r.dailyRest.finalCounters.all { it.provenRemaining == null })
        assertTrue(r.findings.none { it.normalRuleDeficitIsProved })
    }
    @Test fun AllocationLimitIsAnExplicitIncompleteResultWithNoAssuredNestedBudget() {
        val r = WeeklyRestCalculator.calculate(countries().snapshot(), maxAllocations = 1)
        assertFalse(r.alternativesComplete)
        assertTrue(r.allocations.flatMap { it.deadlines }.all { it.provenSecondsRemaining == null })
        assertTrue(r.findings.none { it.normalRuleDeficitIsProved })
    }
    @Test fun PublishedListsAndMapsCannotLoseOriginsThroughCallerMutation() {
        val r = WeeklyRestCalculator.calculate(countries().snapshot())
        try { (r.allocations as MutableList).clear(); fail("mutable allocations") } catch (_: UnsupportedOperationException) { }
        val ledger = r.allocations.first()
        try { (ledger.uses as MutableList).clear(); fail("mutable uses") } catch (_: UnsupportedOperationException) { }
        try { (ledger.dailyCredit.allocations as MutableMap).clear(); fail("mutable attributions") } catch (_: UnsupportedOperationException) { }
    }
    @Test fun TwoDailyDrivingExtensionsAndDailyRestReductionsHaveSeparateResetRules() {
        val f = RestFixture().dl(541).w(269).r(540).dl(541).w(269).r(540).w(60).r(2700).dl(541)
        val r = WeeklyRestCalculator.calculate(f.snapshot())
        assertTrue(r.sourceSnapshot.intervals.isNotEmpty())
        val beforeRest = r.dailyRest.windows.map { it.counterBefore.minimumObservedCount }
        assertTrue(beforeRest.contains(2))
        assertTrue(r.dailyRest.finalCounters.all { it.minimumObservedCount == 0 })
        // The new rest never mutates M2's per-week extension ledger.
        val driving = de.fahrerabrechnung.domain.violation.driving.DrivingCalculator.calculate(f.snapshot())
        assertEquals(r.drivingRevision, driving.revision)
        assertEquals(3, driving.extensions.first { it.week.start == t }.observedOccurrences)
    }
}
