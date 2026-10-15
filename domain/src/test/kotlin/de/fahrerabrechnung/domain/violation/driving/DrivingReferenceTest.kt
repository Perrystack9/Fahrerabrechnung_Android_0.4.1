package de.fahrerabrechnung.domain.violation.driving

import de.fahrerabrechnung.domain.violation.input.*
import java.time.*
import java.util.TimeZone
import org.junit.Assert.*
import org.junit.Test

class DrivingReferenceTest {
    private fun at(value: String) = Instant.parse(value)
    private fun run(f: DrivingFixture) = DrivingCalculator.calculate(f.snapshot())
    private fun DrivingCalculation.daily() = projections.filter { it.law.id in setOf("D01", "D02") }
    private fun DrivingCalculation.breaks() = findings.filter { it.law.id == "B01" }
    private fun priorExtensions(count: Int, lastMinutes: Int): DrivingCalculation {
        val f = DrivingFixture(at("2026-09-07T06:00:00Z"))
        repeat(2) { f.day(if (it < count) 600 else 0) }
        return run(f.dl(lastMinutes))
    }

    @Test fun lg17_nineHoursDoesNotConsumeAnExtension() {
        val r = run(DrivingFixture().dl(540))
        assertEquals(540 * 60L, r.daily().last().countedSeconds)
        assertTrue(r.extensions.all { it.observedOccurrences == 0 })
        assertTrue(r.findings.isEmpty())
    }
    @Test fun lg18_availableNormalExtensionAllowsNineHoursTwentyFive() {
        for (prior in 0..1) {
            val r = priorExtensions(prior, 565)
            val p = r.daily().last()
            assertEquals(600 * 60L, p.normalLimitSeconds)
            assertEquals(35 * 60L, p.conditionalHeadroomSeconds)
            assertEquals(prior + 1, r.extensions.last().observedOccurrences)
            assertFalse(r.findings.any { it.calculationId == p.id })
            assertNull(p.provenRemainingSeconds) // L02 is not closed by test expectations.
        }
    }
    @Test fun lg19_thirdExtensionUsesNineHoursAndExcludesTheInterveningPause() {
        val r = priorExtensions(2, 565)
        val finding = r.findings.single { it.law.id == "D01" }
        assertEquals(25 * 60L, finding.excessSeconds)
        assertEquals(at("2026-09-09T15:45:00Z"), finding.boundaryReachedAt)
        assertEquals(at("2026-09-09T16:30:00Z"), finding.firstExcessAt)
        assertEquals(at("2026-09-09T16:55:00Z"), finding.affectedRange.end)
    }
    @Test fun lg20_tenHourBoundaryAndOneMinuteAfter() {
        for (minutes in listOf(600, 601)) {
            val r = run(DrivingFixture().dl(minutes))
            val findings = r.findings.filter { it.law.id in setOf("D01", "D02") }
            assertEquals(if (minutes == 600) 0 else 1, findings.size)
            findings.singleOrNull()?.let { assertEquals(60, it.excessSeconds); assertEquals(at("2026-09-07T11:30:00Z"), it.firstExcessAt) }
        }
    }
    @Test fun lg21_thirdNineHourOneMinuteDayIsNotPermitted() {
        val r = run(DrivingFixture(at("2026-09-07T06:00:00Z")).day(541).day(541).dl(541))
        assertEquals(1, r.findings.count { it.law.id == "D01" && it.excessSeconds == 60L })
        assertEquals(3, r.extensions.last().observedOccurrences)
    }
    @Test fun lg22_firstPositiveExtensionMinuteBeforeAtAndAfterMondayIsExplicitlyProvisional() {
        for ((start, expectedWeek) in listOf("2026-09-13T13:29:00Z" to "2026-09-07T00:00:00Z",
            "2026-09-13T13:30:00Z" to "2026-09-14T00:00:00Z", "2026-09-13T13:31:00Z" to "2026-09-14T00:00:00Z")) {
            val r = run(DrivingFixture(at(start)).dl(565))
            val ledger = r.extensions.single { it.observedOccurrences == 1 }
            assertEquals(at(expectedWeek), ledger.week.start)
            assertTrue(ledger.restrictions.contains(CalculationRestriction.L02_EXTENSION_WEEK_NOT_LEGALLY_CONFIRMED))
            assertNull(ledger.provenAvailable)
            assertEquals(1, r.daily().size)
        }
    }
    @Test fun lg23_fiftySixHourWeekExactAndOneMinuteEitherSide() {
        for (last in 539..541) {
            val r = run(DrivingFixture(at("2026-09-07T06:00:00Z")).week(600, 600, 540, 540, 540, last, 0))
            val p = r.projections.single { it.law.id == "D03" && it.range.start == at("2026-09-07T00:00:00Z") }
            assertEquals((2820 + last) * 60L, p.countedSeconds)
            val excess = r.findings.filter { it.calculationId == p.id }
            assertEquals(if (last == 541) 1 else 0, excess.size)
            excess.singleOrNull()?.let { assertEquals(60, it.excessSeconds); assertEquals(at("2026-09-12T18:59:00Z"), it.firstExcessAt) }
            assertNull(p.provenRemainingSeconds)
        }
    }
    @Test fun lg24_allOverlappingTwoWeekPairsAreChecked() {
        val r = run(DrivingFixture(at("2026-09-07T06:00:00Z")).week(540,540,540,540,540,0,0)
            .week(540,540,540,540,540,0,0).week(540,540,540,540,541,0,0))
        val pair = r.findings.single { it.law.id == "D04" }
        assertEquals(at("2026-09-14T00:00:00Z"), pair.assessedRange.start)
        assertEquals(60, pair.excessSeconds)
    }
    @Test fun lg25_openTwoWeekPairCanAlreadyHaveANumericOverrun() {
        for (last in 599..601) {
            val f = DrivingFixture(at("2026-08-31T06:00:00Z")).week(600,600,600,600,600,0,0)
            f.day(600).day(600).day(600).dl(last)
            val r = run(f)
            val p = r.projections.single { it.law.id == "D04" && it.range.start == at("2026-08-31T00:00:00Z") }
            assertEquals((4800 + last) * 60L, p.countedSeconds)
            assertEquals(if (last == 601) 60L else 0L, r.findings.filter { it.calculationId == p.id }.sumOf { it.excessSeconds })
            assertTrue(p.observedThrough < p.range.end)
        }
    }
    @Test fun lg26_driveAcrossMondayIsSplitWithoutLosingOrDuplicatingASecond() {
        val r = run(DrivingFixture(at("2026-09-13T23:30:00Z")).d(60))
        val weeks = r.projections.filter { it.law.id == "D03" }
        assertEquals(listOf(1800L,1800L), weeks.map { it.countedSeconds })
        assertEquals(3600, weeks.sumOf { it.countedSeconds })
    }
    @Test fun lg27_countryPointDoesNotResetDrivingCounters() {
        val f = DrivingFixture().d(120)
        val source = f.snapshot().sources.first().key
        val snapshot = f.snapshot(pointsExtra = listOf(InputPoint(source, InputFamily.BORDERS, 0,
            f.start.plusSeconds(3600), mapOf("countryRaw" to "FR"))))
        val r = DrivingCalculator.calculate(snapshot)
        assertEquals(7200, r.daily().single().countedSeconds)
    }
    @Test fun lg28_sevenHourConventionDoesNotPretendToBeLegalDailyRest() {
        for (rest in listOf(419,420,539)) {
            val r = run(DrivingFixture().dl(600).r(rest).dl(600))
            assertEquals(if (rest == 419) listOf(1200L) else listOf(600L,600L), r.daily().map { it.countedSeconds / 60 })
            if (rest >= 420) {
                val anchor = r.daily().last().anchor!!
                assertEquals(DailyDrivingAnchorKind.C2011_3759_SEVEN_HOUR_CONVENTION, anchor.kind)
                assertFalse(anchor.establishesLegalDailyRest)
                assertTrue(r.daily().last().restrictions.contains(CalculationRestriction.C2011_3759_RECOMMENDED_DRIVING_CONVENTION))
            }
        }
    }
    @Test fun weeklyRestInsideSameWeekDoesNotResetTwoExtensionCounter() {
        val r = run(DrivingFixture(at("2026-09-07T06:00:00Z")).day(600).dl(600).r(2700).dl(541))
        assertEquals(1, r.findings.count { it.law.id == "D01" && it.excessSeconds == 60L })
        assertTrue(r.daily().last().anchor!!.range.end > at("2026-09-10T00:00:00Z"))
        assertFalse(r.daily().last().anchor!!.establishesLegalDailyRest)
    }
    @Test fun mondayResetsOnlyCalendarExtensionLedgerWithoutResettingTheOngoingDrivingDay() {
        val r = run(DrivingFixture(at("2026-09-13T13:30:00Z")).dl(601))
        assertEquals(1, r.daily().size)
        assertEquals(601 * 60L, r.daily().single().countedSeconds)
        assertEquals(1, r.extensions.last().observedOccurrences)
        assertEquals(60, r.findings.single { it.law.id == "D02" }.excessSeconds)
    }
    @Test fun lg29_fourAndHalfHoursIsAllowedOneAdditionalMinuteIsCountedExactly() {
        for (minutes in 270..271) {
            val r = run(DrivingFixture().d(minutes))
            assertEquals(if (minutes == 270) 0 else 1, r.breaks().size)
            r.breaks().singleOrNull()?.let {
                assertEquals(at("2026-09-07T04:30:00Z"), it.firstExcessAt)
                assertEquals(60, it.excessSeconds)
                assertTrue(it.quantityIsExact)
            }
        }
    }
    @Test fun lg30_onlyFortyFiveMinutesResetTheUnsplitBreakCounter() {
        for (rest in 44..45) {
            val r = run(DrivingFixture().d(270).r(rest).d(1))
            assertEquals(if (rest == 44) 60L else 0L, r.breaks().sumOf { it.excessSeconds })
        }
    }
    @Test fun lg31_fifteenThenThirtyQualifiesWithEvidenceOfBothParts() {
        val r = run(DrivingFixture().d(120).r(15).d(150).r(30).d(1))
        assertTrue(r.breaks().isEmpty())
        val p = r.projections.last { it.law.id == "B01" }
        assertEquals(listOf(15L,30L), p.breakCredits.map { Duration.between(it.range.start, it.range.end).toMinutes() })
        assertTrue(p.breakCredits.all { it.support.isNotEmpty() && it.ruleId == "B02" })
        assertEquals(269 * 60L, p.provenRemainingSeconds)
    }
    @Test fun lg32_thirtyThenFifteenDoesNotQualifyInOrdinaryGoodsTransport() {
        val r = run(DrivingFixture().d(120).r(30).d(150).r(15).d(1))
        assertEquals(60, r.breaks().single().excessSeconds)
    }
    @Test fun lg33_smallBreaksCannotBeSummedIntoAnInventedFortyFiveMinuteBreak() {
        val r = run(DrivingFixture().d(90).r(10).d(90).r(10).d(90).r(25).d(1))
        assertEquals(60, r.breaks().single().excessSeconds)
    }
    @Test fun lg34_otherWorkAndOrdinaryAvailabilityDoNotResetDriving() {
        for (availability in listOf(false,true)) {
            val f = DrivingFixture().d(150)
            if (availability) f.a(60) else f.w(60)
            val r = run(f.d(121))
            assertEquals(60, r.breaks().single().excessSeconds)
            assertEquals(at("2026-09-07T05:30:00Z"), r.breaks().single().firstExcessAt)
            assertEquals(271 * 60L, r.daily().single().countedSeconds)
        }
    }
    @Test fun lg35_busSplitIsHistoricallyVersionedWithoutCertifyingAnUnprovedActivationHour() {
        for ((date, expected) in listOf("2024-05-21T00:00:00Z" to 60L,"2024-05-22T00:00:00Z" to 0L,
            "2024-05-23T00:00:00Z" to 0L)) {
            for ((one,two) in listOf(30 to 15,20 to 25)) {
                val r = run(DrivingFixture(at(date),passengers=true,occasional=true).d(120).r(one).d(150).r(two).d(1))
                assertEquals(expected, r.breaks().sumOf { it.excessSeconds })
                if (date.startsWith("2024-05-22")) assertTrue(r.projections.any {
                    it.restrictions.contains(CalculationRestriction.HISTORICAL_CHANGE_TIME_NOT_CONFIRMED) })
            }
        }
    }
    @Test fun lg36_busStillRequiresFortyFiveMinutesInTwoParts() {
        for (second in listOf(15,30)) {
            val r = run(DrivingFixture(passengers=true,occasional=true).d(120).r(15).d(150).r(second).d(1))
            assertEquals(if (second == 15) 60L else 0L, r.breaks().sumOf { it.excessSeconds })
        }
    }
    @Test fun lg37_recordedCrewPassengerBreakNeedsNoSecondCardOrConfirmation() {
        val r = run(DrivingFixture().d(270,true).a(45,true).d(1,true))
        assertTrue(r.breaks().isEmpty())
        assertTrue(r.projections.last { it.law.id == "B01" }.breakCredits.any { it.ruleId == "B04" })
        assertFalse(r.issues.any { it.reason.contains("SECOND_CARD") || it.reason.contains("CONFIRM") })
    }
    @Test fun partialCrewAvailabilitySplitIsConditionalAndAProvenFullBreakRestoresCertainty() {
        for (crewFirst in listOf(false, true)) {
            val f = DrivingFixture().d(120, true)
            if (crewFirst) f.a(15, true) else f.r(15)
            f.d(150, true)
            if (crewFirst) f.r(30) else f.a(30, true)
            f.d(1, true)
            val r = run(f)
            assertTrue(r.breaks().isEmpty())
            val p = r.projections.last { it.law.id == "B01" }
            assertFalse(p.quantityIsExact)
            assertEquals(1, p.breakCredits.count { it.conditionalCrewSplit })
            assertTrue(CalculationRestriction.L09_PARTIAL_CREW_AVAILABILITY_SPLIT_NOT_CONFIRMED in p.restrictions)
            assertNull(r.provenCurrentBreakHeadroomSeconds)
            assertTrue(r.issues.any { it.reason == "L09_PARTIAL_CREW_SPLIT_REQUIRES_REVIEW" })
            val continued = run(f.d(270, true)).breaks().single()
            assertEquals(60, continued.excessSeconds)
            assertFalse(continued.quantityIsExact)
            assertFalse(continued.onsetIsExact)
            assertEquals(LegalAssessment.NOT_DETERMINED, continued.legalAssessment)
            val later = run(f.r(45).d(1))
            assertEquals(269 * 60L, later.provenCurrentBreakHeadroomSeconds)
            assertTrue(later.projections.last { it.law.id == "B01" }.quantityIsExact)
            assertTrue(later.issues.any { it.reason == "L09_PARTIAL_CREW_SPLIT_REQUIRES_REVIEW" })
        }
        val bus = run(DrivingFixture(passengers=true, occasional=true).d(120, true).a(30, true).d(150, true).r(15).d(1))
        assertTrue(bus.breaks().isEmpty())
        assertFalse(bus.projections.last { it.law.id == "B01" }.quantityIsExact)
        assertTrue(bus.projections.last { it.law.id == "B01" }.breakCredits.all { it.occasionalBus })
    }
    @Test fun actualRecordedRestSplitInCrewNeedsNoAdditionalLegalOrUserConfirmation() {
        val r = run(DrivingFixture().d(120, true).r(15).d(150, true).r(30).d(1, true))
        assertTrue(r.breaks().isEmpty())
        assertTrue(r.projections.last { it.law.id == "B01" }.quantityIsExact)
        assertEquals(269 * 60L, r.provenCurrentBreakHeadroomSeconds)
        assertFalse(r.issues.any { it.reason.contains("PARTIAL_CREW") || it.reason.contains("CONFIRM") })
    }
    @Test fun shorterOccasionalBusFirstPartSurvivesALongerEarlierOrdinaryPart() {
        val f = DrivingFixture(passengers=true).d(60).r(30).d(60).r(20).d(150).r(25).d(1)
        val snapshot = f.snapshot()
        val changeAt = f.start.plusSeconds(90 * 60L)
        val regular = snapshot.context.assertions.single { it.key == ContextKey.PASSENGER_SERVICE }
        val context = snapshot.context.copy(assertions = snapshot.context.assertions.map {
            if (it == regular) it.copy(until = changeAt) else it
        } + regular.copy(value = ContextValue.Text("OCCASIONAL"), from = changeAt))
        val range = InputRange(snapshot.intervals.first().range.start, snapshot.intervals.last().range.end)
        val r = DrivingCalculator.calculate(snapshot.copy(context=context, scopes=ScopeGuard.evaluate(range, context)))
        assertTrue(r.breaks().isEmpty())
        val p = r.projections.last { it.law.id == "B01" }
        assertEquals(listOf(20L, 25L), p.breakCredits.map { Duration.between(it.range.start, it.range.end).toMinutes() })
        assertTrue(p.breakCredits.all { it.occasionalBus && !it.conditionalCrewSplit })
        assertTrue(p.quantityIsExact)
    }
    @Test fun lg37_workAndManualAvailabilityCannotMasqueradeAsACrewPassengerBreak() {
        for (work in listOf(true,false)) {
            val f = DrivingFixture().d(270,true)
            if (work) f.w(45) else f.a(45,true,inserted=false)
            val r = run(f.d(1,true))
            assertEquals(60, r.breaks().single().excessSeconds)
        }
    }
    @Test fun lg37_unknownTimeIsNeitherAProvenBreakNorAProvenBreakViolation() {
        val r = run(DrivingFixture().d(270,true).u(45).d(1,true))
        assertTrue(r.breaks().isEmpty())
        val p = r.projections.last { it.law.id == "B01" }
        assertFalse(p.quantityIsExact)
        assertNull(p.provenRemainingSeconds)
        assertTrue(p.breakCredits.isEmpty())
        assertTrue(r.issues.any { it.reason == "BREAK_ACTIVITY_OR_SCOPE_UNDETERMINED" })
    }
    @Test fun lg38_aLaterBreakNeverDeletesAnAlreadyRecordedOverrun() {
        val r = run(DrivingFixture().d(271).r(45).d(1))
        assertEquals(1, r.breaks().size)
        assertEquals(60, r.breaks().single().excessSeconds)
        assertEquals(1, r.projections.last { it.law.id == "B01" }.countedSeconds / 60)
    }
    @Test fun lg39_dailyAndBreakOverrunsRemainIndependentWithoutAddingPenaltyDurations() {
        val r = run(DrivingFixture().d(601))
        assertEquals(60, r.findings.single { it.law.id == "D02" }.excessSeconds)
        assertEquals(331 * 60L, r.breaks().single().excessSeconds)
        assertEquals(2, r.findings.size)
    }
    @Test fun lg99_knownManualRestIsCountedNormallyUnknownWithdrawalIsNotRest() {
        val known = run(DrivingFixture().d(270).r(45,manual=true).d(1))
        val unknown = run(DrivingFixture().d(270).u(45).d(1))
        assertTrue(known.breaks().isEmpty()); assertTrue(unknown.breaks().isEmpty())
        assertTrue(known.projections.last { it.law.id == "B01" }.quantityIsExact)
        assertFalse(unknown.projections.last { it.law.id == "B01" }.quantityIsExact)
    }
    @Test fun lg111_normalExtensionComesBeforeArticleTwelveCandidateClassification() {
        for (extra in listOf(0,1,60,61,120,121)) {
            val r = run(DrivingFixture().dl(600 + extra))
            val found = r.findings.filter { it.law.id == "D02" }
            assertEquals(if (extra == 0) 0 else 1, found.size)
            found.singleOrNull()?.let {
                assertEquals(extra * 60L,it.excessSeconds)
                assertEquals(when {
                    extra <= 60 -> Article12Information.POSSIBLE_ONE_HOUR_RETURN
                    extra <= 120 -> Article12Information.POSSIBLE_TWO_HOUR_RETURN
                    else -> Article12Information.GENERAL_DEPARTURE_NOT_ASSESSED
                }, it.article12Information)
                assertEquals(LegalAssessment.NOT_DETERMINED,it.legalAssessment)
                assertFalse(it.establishesArticle12Debt)
            }
        }
    }
    @Test fun lg112_twoWeekLimitGetsNoSpecialOneOrTwoHourReturnJustification() {
        val r = run(DrivingFixture(at("2026-09-07T06:00:00Z")).week(600,600,600,600,600,0,0)
            .week(600,600,600,600,1,0,0))
        val pair = r.findings.single { it.law.id == "D04" }
        assertEquals(60,pair.excessSeconds)
        assertEquals(Article12Information.GENERAL_DEPARTURE_NOT_ASSESSED,pair.article12Information)
        assertEquals(LegalAssessment.NOT_DETERMINED,pair.legalAssessment)
    }
    @Test fun lg113_thirtyMinutePauseDoesNotReplaceOrdinaryFortyFiveMinuteDutyOrProveArticleTwelve() {
        for (pause in listOf(29,30,45)) {
            val r = run(DrivingFixture().dl(600).r(pause).d(120))
            val day = r.findings.single { it.law.id == "D02" }
            assertEquals(7200,day.excessSeconds)
            assertEquals(Article12Information.POSSIBLE_TWO_HOUR_RETURN,day.article12Information)
            assertFalse(day.establishesArticle12Debt)
            assertEquals(LegalAssessment.NOT_DETERMINED,day.legalAssessment)
        }
    }
    @Test fun repeatedSourcesHaveTheSameQuantitiesAndEventIdentityButRetainAllProvenance() {
        val fixture = DrivingFixture().d(601)
        val one = DrivingCalculator.calculate(fixture.snapshot())
        val two = DrivingCalculator.calculate(fixture.snapshot(extraIdenticalSource=true))
        assertEquals(one.findings.map { it.id },two.findings.map { it.id })
        assertEquals(one.findings.map { it.excessSeconds },two.findings.map { it.excessSeconds })
        assertNotEquals(one.provenanceRevision,two.provenanceRevision)
        assertNotEquals(one.revision,two.revision)
        assertEquals(2,two.sourceSnapshot.sources.size)
        assertTrue(two.findings.all { finding -> finding.excessDriving.all { slice ->
            slice.support.flatMap { it.begin }.map { it.source.sessionId }.toSet().size == 2 } })
    }
    @Test fun unknownLeftHistoryHidesBudgetsButLongEnoughClosedDrivingStillProvesALowerBound() {
        val r = run(DrivingFixture(anchored=false).d(601))
        assertTrue(r.findings.all { !it.quantityIsExact })
        assertTrue(r.projections.all { it.provenRemainingSeconds == null })
        assertEquals(60,r.findings.single { it.law.id == "D02" }.excessSeconds)
        assertTrue(r.projections.none { it.isLegalAllClear })
        assertFalse(r.establishesLegalAllClear)
    }
    @Test fun aRealLaterBreakRestoresLocalAssessabilityWithoutHidingAnEarlierGap() {
        val r = run(DrivingFixture().d(20).u(90).r(45).d(271))
        assertEquals(60,r.breaks().single().excessSeconds)
        assertTrue(r.breaks().single().quantityIsExact)
        assertTrue(r.issues.any { it.reason.contains("UNDETERMINED") })
    }
    @Test fun openActivityIsNeverExtendedToTheImportClockOrMidnight() {
        val f = DrivingFixture().d(271)
        val snapshot = f.snapshot(withEnd=false)
        val r = DrivingCalculator.calculate(snapshot)
        assertTrue(r.findings.isEmpty())
        assertEquals(f.start,r.observedThrough)
        assertFalse(r.establishesLegalAllClear)
        assertTrue(r.issues.any { it.reason == "INPUT_OPEN_END" })
    }
    @Test fun aDisplayOrDefaultZoneChangeCannotChangeUtcDurationsIdentifiersOrWeekProjection() {
        val snapshot = DrivingFixture(at("2026-10-25T00:00:00Z")).d(271).snapshot()
        val original = TimeZone.getDefault()
        try {
            val calculations = listOf("UTC","Europe/Berlin","America/New_York").map {
                TimeZone.setDefault(TimeZone.getTimeZone(it)); DrivingCalculator.calculate(snapshot)
            }
            assertEquals(1,calculations.map { it.revision }.distinct().size)
            assertEquals(1,calculations.map { it.findings }.distinct().size)
            assertEquals(60,calculations.first().breaks().single().excessSeconds)
            assertEquals(at("2026-10-25T04:30:00Z"),calculations.first().breaks().single().firstExcessAt)
        } finally { TimeZone.setDefault(original) }
    }
    @Test fun scopeUnknownOrAnAetrWholeJourneyCannotGenerateEuFindingsOrBudgets() {
        val fixture = DrivingFixture().d(700)
        for (area in listOf("AETR_MIXED","EU_EEA_CH")) {
            val snapshot = fixture.snapshot()
            val context = snapshot.context.copy(assertions = snapshot.context.assertions.map {
                if (it.key == ContextKey.JOURNEY_AREA) it.copy(value=ContextValue.Text(area)) else it
            })
            val range = InputRange(snapshot.intervals.first().range.start,snapshot.intervals.last().range.end)
            val scoped = snapshot.copy(context=context,scopes=ScopeGuard.evaluate(range,context))
            val r = DrivingCalculator.calculate(scoped)
            assertTrue(r.findings.isEmpty())
            assertTrue(r.projections.all { it.provenRemainingSeconds == null })
            assertTrue(r.issues.any { it.reason.contains("SCOPE_UNDETERMINED") })
        }
    }
    @Test fun regularBusLineAtExactlyFiftyKmIsExcludedWithoutChangingTheRawDriving() {
        val snapshot = DrivingFixture(passengers=true).d(700).snapshot()
        val context = snapshot.context.copy(assertions=snapshot.context.assertions.map {
            if (it.key==ContextKey.LINE_LENGTH_METRES) it.copy(value=ContextValue.Number(50000)) else it })
        val range=InputRange(snapshot.intervals.first().range.start,snapshot.intervals.last().range.end)
        val r=DrivingCalculator.calculate(snapshot.copy(context=context,scopes=ScopeGuard.evaluate(range,context)))
        assertTrue(r.findings.isEmpty())
        assertTrue(r.sourceSnapshot.intervals.any { it.meaning.activity.name=="LENKZEIT" })
        assertTrue(r.projections.all { it.provenRemainingSeconds==null })
    }
    @Test fun cancellationPropagatesInsteadOfReturningAnAllClear() {
        var calls=0
        try {
            DrivingCalculator.calculate(DrivingFixture().dl(600).snapshot(),checkCancellation={
                calls++; if(calls==3) throw java.util.concurrent.CancellationException("synthetic cancellation") })
            fail("Cancellation must propagate")
        } catch (_: java.util.concurrent.CancellationException) { assertEquals(3,calls) }
    }
    @Test fun missingWeekHistoryDoesNotCreateAThirdSlotAfterTwoProvenExtensions() {
        val r=run(DrivingFixture(at("2026-09-07T06:00:00Z")).day(600).day(600).u(1).r(660).dl(565))
        assertEquals(25*60L,r.findings.single {it.law.id=="D01"}.excessSeconds)
        assertFalse(r.extensions.last().historyComplete)
        assertNull(r.extensions.last().conditionallyAvailable)
    }
    @Test fun ferryDependentDailyRestIsDeferredToM3WithoutInventingADailyOverrun() {
        val f=DrivingFixture().dl(600)
        val ferryAt=f.cursor
        f.r(300).w(20).r(240).d(60)
        val source=f.snapshot().sources.first().key
        val snapshot=f.snapshot(pointsExtra=listOf(InputPoint(source,InputFamily.SPECIAL_CONDITIONS,0,ferryAt,mapOf("typeRaw" to "3"))))
        val r=DrivingCalculator.calculate(snapshot)
        assertFalse(r.findings.any {it.law.id in setOf("D01","D02")})
        assertTrue(r.issues.any {it.reason=="FERRY_DAILY_BOUNDARY_REQUIRES_M3"})
    }
    @Test fun laterFerryMarkerCannotEraseAPreviouslyProvenNormalLimitOverrun() {
        val f=DrivingFixture().dl(601)
        val ferryAt=f.cursor
        f.w(1).r(300).w(20).r(240).d(60)
        val source=f.snapshot().sources.first().key
        val r=DrivingCalculator.calculate(f.snapshot(pointsExtra=listOf(
            InputPoint(source,InputFamily.SPECIAL_CONDITIONS,0,ferryAt,mapOf("typeRaw" to "3")))))
        assertEquals(60,r.findings.single {it.law.id=="D02"}.excessSeconds)
        assertTrue(r.issues.any {it.reason=="FERRY_DAILY_BOUNDARY_REQUIRES_M3"})
    }
    @Test fun actualCrewAndSlotConflictsDoNotProduceAFabricatedConfirmedBreakFailure() {
        val snapshot=DrivingFixture().d(270,true).a(45,true).d(1,true).snapshot(conflictingCrewSource=true)
        assertTrue(snapshot.issues.any {it.code==InputIssueCode.CREW_CONFLICT})
        val r=DrivingCalculator.calculate(snapshot)
        assertTrue(r.breaks().isEmpty())
        assertNull(r.provenCurrentBreakHeadroomSeconds)
        assertFalse(r.projections.last {it.law.id=="B01"}.quantityIsExact)
    }
    @Test fun pre2020OverrunDoesNotReceiveTheLaterSpecialReturnRuleHint() {
        val r=run(DrivingFixture(at("2019-09-09T00:00:00Z")).dl(660))
        assertEquals(Article12Information.GENERAL_DEPARTURE_NOT_ASSESSED,
            r.findings.single {it.law.id=="D02"}.article12Information)
    }
    @Test fun completedFortyFiveMinuteBreakRestoresCurrentBudgetWithoutInventingAFollowingMinute() {
        val f=DrivingFixture().d(270).r(45)
        val r=run(f)
        assertEquals(270*60L,r.provenCurrentBreakHeadroomSeconds)
        assertEquals(f.cursor,r.observedThrough)
        assertEquals(270*60L,r.projections.filter {it.law.id=="B01"}.sumOf {it.countedSeconds})
        val short=run(DrivingFixture().d(270).r(44))
        assertEquals(0L,short.provenCurrentBreakHeadroomSeconds)
    }
    @Test fun latestGapHidesCurrentBudgetWithoutDeletingAnEarlierRecordedOverrun() {
        val r=run(DrivingFixture().d(271).u(45))
        assertEquals(60,r.breaks().single().excessSeconds)
        assertNull(r.provenCurrentBreakHeadroomSeconds)
        assertTrue(r.breaks().single().onsetIsExact)
    }
    @Test fun incompleteHistoryReportsALatestProvedOnsetInsteadOfAnExactClaim() {
        val r=run(DrivingFixture(anchored=false).d(601))
        assertTrue(r.findings.all {!it.onsetIsExact})
        assertTrue(r.findings.all {it.article12Information==Article12Information.GENERAL_DEPARTURE_NOT_ASSESSED})
    }

}
