package de.fahrerabrechnung.domain.violation.input

import org.junit.Assert.*
import org.junit.Test
import java.time.Instant

class ScopeContextAcceptanceTest {
    private val from = Instant.parse("2026-09-07T00:00:00Z")
    private val range = InputRange(from, from.plusSeconds(3600))
    private val subject = ContextSubject(ContextSubjectKind.PROFILE, "P1")
    private fun assertion(key: ContextKey, value: ContextValue, actual: ContextSubject = subject,
        start: Instant = Instant.parse("2026-01-01T00:00:00Z"), end: Instant? = null) =
        ContextAssertion(key, value, actual, start, end, ContextOrigin.USER_CONTEXT, "synthetic-$key", from)
    private fun text(key: ContextKey, value: String) = assertion(key, ContextValue.Text(value))
    private fun flag(key: ContextKey, value: Boolean) = assertion(key, ContextValue.Flag(value))
    private fun number(key: ContextKey, value: Long) = assertion(key, ContextValue.Number(value))
    private fun context(vararg entries: ContextAssertion) = InputContext("P1", "revision", entries.toList())
    private fun goods(mass: Long, vararg extra: ContextAssertion) = context(
        flag(ContextKey.ROAD_CARRIAGE, true), text(ContextKey.JOURNEY_AREA, "EU_ONLY"),
        text(ContextKey.TRANSPORT_KIND, "GOODS"), number(ContextKey.MAX_MASS_KG, mass), *extra)
    private fun passengers(length: Long?) = context(flag(ContextKey.ROAD_CARRIAGE, true),
        text(ContextKey.JOURNEY_AREA, "EU_ONLY"), text(ContextKey.TRANSPORT_KIND, "PASSENGERS"),
        number(ContextKey.PASSENGER_CAPACITY, 50), text(ContextKey.PASSENGER_SERVICE, "REGULAR"),
        *listOfNotNull(length?.let { number(ContextKey.LINE_LENGTH_METRES, it) }).toTypedArray())
    private fun decision(context: InputContext, at: InputRange = range, calendar: ScopeCalendar = ScopeCalendar()) =
        ScopeGuard.evaluate(at, context, calendar).single().decisions.first()

    @Test fun mi15_goodsThresholdIncludesTrailerMassAndIsStrictlyGreaterThan3500() {
        assertEquals(ScopeStatus.APPLICABLE, decision(goods(3501)).status)
        assertEquals(ScopeStatus.UNKNOWN, decision(goods(3500)).status)
        assertEquals(ScopeStatus.NOT_APPLICABLE, decision(goods(3500, text(ContextKey.JOURNEY_KIND, "DOMESTIC"))).status)
        assertEquals(ScopeStatus.UNKNOWN, decision(goods(3501).copy(assertions = goods(3501).assertions.filter { it.key != ContextKey.MAX_MASS_KG })).status)
    }

    @Test fun mi16_scopeExtensionUsesHistoricalDateStrictMassAndConcreteOwnAccountFacts() {
        val before = InputRange(Instant.parse("2026-06-30T12:00:00Z"), Instant.parse("2026-06-30T13:00:00Z"))
        val after = InputRange(Instant.parse("2026-07-02T12:00:00Z"), Instant.parse("2026-07-02T13:00:00Z"))
        val calendar = ScopeCalendar("UTC", "MI16-explicit-reference-convention")
        for (journey in listOf("INTERNATIONAL", "CABOTAGE")) {
            for (mass in listOf(2501L, 3500L)) {
                val regular = goods(mass, text(ContextKey.JOURNEY_KIND, journey), flag(ContextKey.OWN_ACCOUNT, false))
                assertEquals(ScopeStatus.NOT_APPLICABLE, decision(regular, before, calendar).status)
                assertEquals(ScopeStatus.APPLICABLE, decision(regular, after, calendar).status)
                assertEquals(ScopeStatus.UNKNOWN, decision(regular, after).status)
                val needs = ScopeGuard.evaluate(after, regular, calendar).single().needs
                assertTrue(needs.none { it.key == ContextKey.DRIVING_MAIN_ACTIVITY })
            }
            assertEquals(ScopeStatus.NOT_APPLICABLE, decision(goods(2500, text(ContextKey.JOURNEY_KIND, journey)), after, calendar).status)
        }
        assertEquals(ScopeStatus.NOT_APPLICABLE, decision(goods(3000, text(ContextKey.JOURNEY_KIND, "DOMESTIC")), after, calendar).status)
        assertEquals(ScopeStatus.NOT_APPLICABLE, decision(goods(3000, text(ContextKey.JOURNEY_KIND, "INTERNATIONAL"),
            flag(ContextKey.OWN_ACCOUNT, true), flag(ContextKey.DRIVING_MAIN_ACTIVITY, false)), after, calendar).status)
        assertEquals(ScopeStatus.UNKNOWN, decision(goods(3000, text(ContextKey.JOURNEY_KIND, "INTERNATIONAL"),
            flag(ContextKey.OWN_ACCOUNT, true)), after, calendar).status)
        val crossing = InputRange(Instant.parse("2026-06-30T23:00:00Z"), Instant.parse("2026-07-01T01:00:00Z"))
        assertEquals(listOf(ScopeStatus.NOT_APPLICABLE, ScopeStatus.APPLICABLE), ScopeGuard.evaluate(crossing,
            goods(3000, text(ContextKey.JOURNEY_KIND, "INTERNATIONAL"), flag(ContextKey.OWN_ACCOUNT, false)), calendar)
            .map { it.decisions.first().status })
    }

    @Test fun mi17_passengerPointNeedsServiceOnlyUntilAnswerAndFullLineLengthIsDecisive() {
        val empty = InputContext.empty("P1")
        assertEquals(ContextKey.PASSENGER_SERVICE, InputContextNeeds.passengerAt(empty, from).single().key)
        val regular = context(text(ContextKey.PASSENGER_SERVICE, "REGULAR"))
        assertEquals(ContextKey.LINE_LENGTH_METRES, InputContextNeeds.passengerAt(regular, from).single().key)
        val occasional = context(text(ContextKey.PASSENGER_SERVICE, "OCCASIONAL"))
        assertTrue(InputContextNeeds.passengerAt(occasional, from).isEmpty())
        assertEquals(ScopeStatus.NOT_APPLICABLE, decision(passengers(50_000)).status)
        assertEquals(ScopeStatus.APPLICABLE, decision(passengers(50_001)).status)
        assertEquals(ScopeStatus.UNKNOWN, decision(passengers(null)).status)
        // No per-trip distance key can accidentally substitute for the full line.
        assertFalse(ContextKey.entries.any { it.name == "TRIP_LENGTH" })
    }

    @Test fun mi18_cardAndOosNeverProveScopePartialReviewedAdjustmentIsArticleTerritoryAndTimeBound() {
        assertEquals(ScopeStatus.UNKNOWN, decision(InputContext.empty("P1")).status)
        val regular = goods(40000, text(ContextKey.OPERATION_COUNTRY, "DE"))
        val adjustment = ReviewedScopeAdjustment(InputRange(from.plusSeconds(1200), from.plusSeconds(2400)), "DE",
            setOf(ScopeArticle.BREAK_7), "13", "https://example.invalid/reviewed-synthetic-manifest", "a".repeat(64), "test-review", true)
        val segments = ScopeGuard.evaluate(range, regular, reviewed = listOf(adjustment))
        assertEquals(3, segments.size)
        assertEquals(listOf(ScopeStatus.APPLICABLE, ScopeStatus.NOT_APPLICABLE, ScopeStatus.APPLICABLE),
            segments.map { it.decisions.single { d -> d.article == ScopeArticle.BREAK_7 }.status })
        assertTrue(segments.all { it.decisions.single { d -> d.article == ScopeArticle.DRIVING_6 }.status == ScopeStatus.APPLICABLE })
        assertTrue(ScopeGuard.evaluate(range, goods(40000, text(ContextKey.OPERATION_COUNTRY, "FR")), reviewed = listOf(adjustment))
            .all { segment -> segment.decisions.all { it.status == ScopeStatus.APPLICABLE } })
        val aetr = regular.copy(assertions = regular.assertions.filter { it.key != ContextKey.JOURNEY_AREA } + text(ContextKey.JOURNEY_AREA, "AETR_MIXED"))
        assertEquals(ScopeStatus.UNSUPPORTED, decision(aetr).status)
        assertEquals("AETR_WHOLE_JOURNEY", decision(aetr).reason)
        assertThrows(IllegalArgumentException::class.java) { adjustment.copy(legalArticle = "12") }
        assertThrows(IllegalArgumentException::class.java) { adjustment.copy(legalArticle = "11") }
    }

    @Test fun mi19_nonRegulatedCommercialDrivingHasSeparateOtherWorkHintNeverRest() {
        val commercial = goods(2000, flag(ContextKey.COMMERCIAL_OPERATION, true))
        assertEquals(ScopeStatus.NOT_APPLICABLE, decision(commercial).status)
        assertEquals(LegalActivityHint.OTHER_WORK, decision(commercial).activityHint)
        assertEquals(LegalActivityHint.OTHER_WORK, decision(goods(2000, flag(ContextKey.TRANSFER_REQUIRES_OTHER_WORK, true))).activityHint)
        assertEquals(LegalActivityHint.UNDETERMINED, decision(goods(2000)).activityHint)
    }

    @Test fun mi21_ferryQuestionsRequireConcreteAssessmentAndReuseSameSubjectAnswer() {
        val rest = ContextSubject(ContextSubjectKind.REST, "concrete-ferry-rest")
        val empty = InputContext.empty("P1")
        val first = InputContextNeeds.ferry(empty, range, rest, FerryRestKind.REGULAR_WEEKLY)
        assertEquals(setOf(ContextKey.FERRY_SLEEPING_FACILITY, ContextKey.FERRY_SCHEDULED_MINUTES), first.map { it.key }.toSet())
        assertEquals(first, InputContextNeeds.ferry(empty, range, rest, FerryRestKind.REGULAR_WEEKLY))
        val answer = context(assertion(ContextKey.FERRY_SLEEPING_FACILITY, ContextValue.Text("CABIN"), rest, range.start, range.end),
            assertion(ContextKey.FERRY_SCHEDULED_MINUTES, ContextValue.Number(480), rest, range.start, range.end))
        assertTrue(InputContextNeeds.ferry(answer, range, rest, FerryRestKind.REGULAR_WEEKLY).isEmpty())
        val other = ContextSubject(ContextSubjectKind.REST, "different-ferry-rest")
        assertEquals(2, InputContextNeeds.ferry(answer, range, other, FerryRestKind.REGULAR_WEEKLY).size)
        assertEquals(1, InputContextNeeds.ferry(empty, range, rest, FerryRestKind.REGULAR_DAILY).size)
        assertFalse(ContextKey.entries.any { it.name in setOf("HOME_ADDRESS", "WEEKLY_REST_ACCOMMODATION") })
    }

    @Test fun mi22_pairCountriesRemainTiedToBothSpecificRestsAndValidity() {
        val one = ContextSubject(ContextSubjectKind.REST, "rest-one") to range
        val two = ContextSubject(ContextSubjectKind.REST, "rest-two") to InputRange(range.end, range.end.plusSeconds(86400))
        val context = context(text(ContextKey.EMPLOYER_COUNTRY, "DE"), text(ContextKey.RESIDENCE_COUNTRY, "DE"),
            assertion(ContextKey.REST_START_COUNTRY, ContextValue.Text("FR"), one.first, one.second.start, one.second.end))
        val needs = InputContextNeeds.internationalPair(context, one, two)
        assertEquals(1, needs.size)
        assertEquals(two.first, needs.single().subject)
        assertEquals(ContextKey.REST_START_COUNTRY, needs.single().key)
        assertEquals(needs.single().id, InputContextNeeds.internationalPair(context.copy(revisionId = "reimport"), one, two).single().id)
    }

    @Test fun mi23_onlyTemporalFactsChangeContextNoDisplayOrImportClockInputExists() {
        val base = goods(40000)
        val changedRecordingTime = base.copy(assertions = base.assertions.map { it.copy(recordedAt = from.plusSeconds(9999)) })
        assertEquals(base.semanticRevision, changedRecordingTime.semanticRevision)
        val operation = ContextSubject(ContextSubjectKind.OPERATION, "limited-operation")
        val override = assertion(ContextKey.MAX_MASS_KG, ContextValue.Number(2000), operation,
            from.plusSeconds(1200), from.plusSeconds(2400))
        val revised = base.copy(revisionId = "new-revision", assertions = base.assertions + override)
        assertNotEquals(base.semanticRevision, revised.semanticRevision)
        assertEquals(listOf(ScopeStatus.APPLICABLE, ScopeStatus.NOT_APPLICABLE, ScopeStatus.APPLICABLE),
            ScopeGuard.evaluate(range, revised).map { it.decisions.first().status })
        assertEquals(ContextValue.Number(40000), revised.resolveAt(ContextKey.MAX_MASS_KG, from).value)
        assertEquals(ContextValue.Number(2000), revised.resolveAt(ContextKey.MAX_MASS_KG, from.plusSeconds(1200)).value)
        assertEquals(ContextValue.Number(40000), revised.resolveAt(ContextKey.MAX_MASS_KG, from.plusSeconds(2400)).value)
    }

    @Test fun conflictingContextNeverUsesLatestEntryAndMissingQuestionsAreMinimal() {
        val base = goods(40000)
        val conflicting = base.copy(assertions = base.assertions + assertion(ContextKey.MAX_MASS_KG,
            ContextValue.Number(2000), start = range.start, end = range.end))
        assertEquals(ScopeStatus.UNKNOWN, decision(conflicting).status)
        assertEquals("CONTEXT_CONFLICT", ScopeGuard.evaluate(range, conflicting).single().needs.single().reason)
        val notRoad = context(flag(ContextKey.ROAD_CARRIAGE, false))
        assertTrue(ScopeGuard.evaluate(range, notRoad).single().needs.isEmpty())
        val smallPassenger = context(flag(ContextKey.ROAD_CARRIAGE, true), text(ContextKey.JOURNEY_AREA, "EU_ONLY"),
            text(ContextKey.TRANSPORT_KIND, "PASSENGERS"), number(ContextKey.PASSENGER_CAPACITY, 9))
        assertTrue(ScopeGuard.evaluate(range, smallPassenger).single().needs.isEmpty())
    }
}
