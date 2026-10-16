package de.fahrerabrechnung.domain.violation.rest

import de.fahrerabrechnung.domain.violation.input.*
import org.junit.Assert.*
import org.junit.Test
import java.time.Instant
import java.util.concurrent.CancellationException

class RestGuardRegressionTest {
    private val t=Instant.parse("2026-09-07T00:00:00Z")
    private fun transformed(key: ContextKey, value: ContextValue?) = { c: InputContext ->
        c.copy(assertions=c.assertions.filter { it.key!=key } + if(value==null) emptyList() else
            listOf(c.assertions.first { it.key==key }.copy(value=value))) }
    private fun blocked(key: ContextKey,value: ContextValue?) {
        val s=RestFixture(priorReductions=3).w(781).r(660).snapshot(contextTransform=transformed(key,value))
        val r=DailyRestCalculator.calculate(s)
        assertTrue(r.findings.none { it.normalRuleDeficitIsProved })
        assertTrue(r.windows.all { it.provenSecondsUntilLatestRestStart==null })
        assertTrue(r.finalCounters.all { it.provenRemaining==null })
        assertFalse(r.establishesLegalAllClear)
    }
    @Test fun AetrContextDoesNotProduceGuaranteedEuOutputs()=blocked(ContextKey.JOURNEY_AREA,ContextValue.Text("AETR_MIXED"))
    @Test fun ProvenNonRoadContextDoesNotProduceGuaranteedEuOutputs()=blocked(ContextKey.ROAD_CARRIAGE,ContextValue.Flag(false))
    @Test fun MissingScopeContextDoesNotProduceGuaranteedOutputs()=blocked(ContextKey.ROAD_CARRIAGE,null)
    @Test fun RegularPassengerLineAtFiftyKmDoesNotProduceEuVerdicts() {
        val s=RestFixture(priorReductions=3,passengers=true).w(781).r(660).snapshot(
            contextTransform=transformed(ContextKey.LINE_LENGTH_METRES,ContextValue.Number(50000)))
        val r=DailyRestCalculator.calculate(s)
        assertTrue(r.findings.isEmpty());assertTrue(r.finalCounters.all { it.provenRemaining==null })
    }
    @Test fun FuturePossibleRestIsNeverCountedAsRecordedRest() {
        val r=DailyRestCalculator.calculate(RestFixture(priorReductions=3).w(781).snapshot())
        val w=r.windows.single { it.referenceStart==t };val f=r.findings.single { it.assessedRange.start==t }
        assertEquals(0L,w.creditedSeconds);assertEquals(659*60L,w.potentialRestCapacitySeconds)
        assertEquals(0L,f.creditedRestSeconds);assertEquals(659*60L,f.optimisticAdditionalRestSeconds)
        assertFalse(f.quantityIsExact);assertTrue(f.normalRuleDeficitIsProved)
        assertTrue(w.creditedRestCandidates.isEmpty())
    }
    @Test fun RecordedRestIsCreditedWhileTheFutureRemainsOnlyAnOptimisticScalar() {
        val f=RestFixture(priorReductions=3).w(781).r(60).r(60)
        val r=DailyRestCalculator.calculate(f.snapshot(withEnd=false))
        val w=r.windows.single { it.referenceStart==t }
        assertEquals(3600L,w.creditedSeconds)
        assertEquals(659*60L,w.potentialRestCapacitySeconds)
        assertEquals(599*60L,r.findings.single { it.assessedRange.start==t }.optimisticAdditionalRestSeconds)
    }
    @Test fun PrepWorkBeforeDrivingDoesNotSpendTheOptionalCrewHour() {
        val f=RestFixture().w(60,crew=true).w(90).d(60).d(210,crew=true).a(45,crew=true)
            .w(525,crew=true).d(270,crew=true).r(540)
        val r=DailyRestCalculator.calculate(f.snapshot())
        assertEquals(RestWindowMode.CREW_30,r.windows.single { it.referenceStart==t }.mode)
        assertTrue(r.findings.none { it.assessedRange.start==t })
    }
    @Test fun ConflictingCrewAndAvailabilityNeverBecomeDailyRest() {
        val f=RestFixture().w(675,crew=true).d(270,crew=true).a(45,crew=true).d(270,crew=true).r(540)
        val r=DailyRestCalculator.calculate(f.snapshot(conflictingCrew=true))
        assertTrue(r.findings.none { it.normalRuleDeficitIsProved })
        assertTrue(r.candidates.filter { it.range.start>=t }.all { it.netRestSeconds<=32400 })
    }
    @Test fun TransferContextCutsRecordedRestWithoutEditingOriginalActivityOrLosingTheReason() {
        val f=RestFixture().w(780).r(660);val raw=f.snapshot()
        val subject=ContextSubject(ContextSubjectKind.JOURNEY,"transfer")
        f.assertion(ContextKey.TRANSFER_REQUIRES_OTHER_WORK,ContextValue.Flag(true),subject,
            t.plusSeconds(840*60L),t.plusSeconds(900*60L))
        val s=f.snapshot();val r=DailyRestCalculator.calculate(s)
        assertEquals(raw.intervals,s.intervals)
        assertEquals(listOf(60*60L,540*60L),r.candidates.filter { it.range.start>=t }.map { it.netRestSeconds })
        val id=s.context.assertions.single { it.key==ContextKey.TRANSFER_REQUIRES_OTHER_WORK }.semanticId
        assertTrue(r.candidates.filter { it.range.start>=t }.all { id in it.contextEvidence })
    }
    @Test fun CancellationIsAlsoRespectedWhenTheCalculatorObtainsItsOwnM2Input() {
        assertThrows(CancellationException::class.java) {
            DailyRestCalculator.calculate(RestFixture().w(780).r(660).snapshot(),
                checkCancellation={throw CancellationException()})
        }
    }
    @Test fun ReducedWeeklyCandidateDoesNotInventAnUnconditionalM4Assignment() {
        val r=DailyRestCalculator.calculate(RestFixture(priorReductions=3).w(60).r(1440).w(1).snapshot())
        assertTrue(r.finalCounters.all { it.provenRemaining==null })
        assertTrue(r.finalCounters.any { RestRestriction.WEEKLY_ASSIGNMENT_REQUIRES_M4 in it.restrictions })
    }
    @Test fun KnownLaterSubperiodCanResumeAfterADataGap() {
        val f=RestFixture().w(60).u(660).r(660).w(780).r(660)
        val r=DailyRestCalculator.calculate(f.snapshot())
        assertTrue(r.windows.any { it.referenceStart==t.plusSeconds(1380*60L) && it.state==RestWindowState.FULFILLED })
        assertFalse(r.establishesLegalAllClear)
    }
}
