package de.fahrerabrechnung.data

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import de.fahrerabrechnung.data.card.*
import de.fahrerabrechnung.data.db.AppDatabase
import de.fahrerabrechnung.data.security.EncryptedDatabaseFactory
import de.fahrerabrechnung.data.violation.*
import de.fahrerabrechnung.domain.violation.driving.*
import de.fahrerabrechnung.domain.violation.input.*
import de.fahrerabrechnung.domain.violation.rest.*
import java.nio.ByteBuffer
import java.security.SecureRandom
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collect
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith

/** Synthetic multi-day EF0504 via the existing parser and import pipeline, in real SQLCipher. */
@RunWith(AndroidJUnit4::class)
class DailyRestInstrumentedTest {
    private lateinit var context: Context
    private lateinit var db: AppDatabase
    private lateinit var key: ByteArray
    private lateinit var pipeline: DriverCardImportPipeline
    private lateinit var contexts: ViolationContextRepository
    private lateinit var adapter: DriverCardInputAdapter
    private val t=M1InputFixture.day
    private val clock=Clock.fixed(Instant.parse("2026-10-16T05:00:00Z"),ZoneOffset.UTC)
    @Before fun before() {
        context=ApplicationProvider.getApplicationContext();context.deleteDatabase(AppDatabase.DATABASE_NAME)
        key=ByteArray(32).also(SecureRandom()::nextBytes);open()
    }
    private fun open() {
        db=EncryptedDatabaseFactory.create(context,key)
        val activities=CardActivityImportRepository(db.cardActivityImportDao(),clock)
        val vehicles=CardVehicleImportRepository(db.cardVehicleImportDao(),clock)
        val imports=DriverCardImportRepository(db,activities,vehicles,CardSourcePairRepository(db,activities,vehicles,clock),clock)
        pipeline=DriverCardImportPipeline(db,imports,activities,vehicles,clock)
        contexts=ViolationContextRepository(db,clock);adapter=DriverCardInputAdapter(db,imports,contexts)
    }
    @After fun after() {db.close();context.deleteDatabase(AppDatabase.DATABASE_NAME)}
    private suspend fun snapshot():ViolationInputSnapshot {
        val input=adapter.capture()
        return InputSnapshotBuilder.build(checkNotNull(input.sources),checkNotNull(input.context))
    }
    private suspend fun scope(area:String="EU_ONLY", extra:List<ContextAssertion> = emptyList()):InputContext {
        val profile=checkNotNull(db.driverProfileDao().localProfile()).id
        val previous=contexts.current(profile);val subject=ContextSubject(ContextSubjectKind.PROFILE,profile)
        fun a(k:ContextKey,v:ContextValue)=ContextAssertion(k,v,subject,t.minusSeconds(30*86400),null,
            ContextOrigin.USER_CONTEXT,"synthetic-M3-context",clock.instant())
        return contexts.replace(profile,previous.revisionId,listOf(
            a(ContextKey.ROAD_CARRIAGE,ContextValue.Flag(true)),a(ContextKey.TRANSPORT_KIND,ContextValue.Text("GOODS")),
            a(ContextKey.JOURNEY_AREA,ContextValue.Text(area)),a(ContextKey.MAX_MASS_KG,ContextValue.Number(40000)))+extra)
    }
    private fun batch(prior:Int=0,manual:Boolean=false,unknown:Boolean=false,late:Boolean=false,
        conflict:Boolean=false,sameMinute:Boolean=false,openRest:Boolean=false):CardApplicationReadBatch {
        fun read(generation:CardGeneration):CardReadResult {
            val days=mutableListOf<Pair<Instant,IntArray>>()
            val prefixEnd=t.minusSeconds(prior*86400L)
            days += prefixEnd.minusSeconds(2*86400L) to intArrayOf(0x2000,180)
            days += prefixEnd.minusSeconds(86400) to intArrayOf(0)
            repeat(prior) {days += prefixEnd.plusSeconds(it*86400L) to intArrayOf(0x1000,900)}
            val restMinute=if(late) 781 else 900
            val rest=when {unknown->0x2000;manual->0x6000;conflict && generation!=CardGeneration.GEN1->0x1000;else->0}
            days += t to (if(sameMinute) intArrayOf(0x1000,restMinute,0x1000+restMinute)
                else intArrayOf(0x1000,rest+restMinute))
            days += t.plusSeconds(86400) to when {openRest->intArrayOf(0);late->intArrayOf(0,0x1001);else->intArrayOf(0x1000)}
            var previous=0;var newest=0;var offset=0
            val records=days.map { (date,words)->
                val bytes=M1InputFixture.record(*words,date=date,previous=previous)
                previous=bytes.size;newest=offset;offset+=bytes.size;bytes
            }
            val original=CardCoreSnapshot(CardCoreEf.ACTIVITY,generation,
                ByteBuffer.allocate(4+offset).putShort(0).putShort(newest.toShort()).apply {records.forEach {put(it)}}.array())
            return M1InputFixture.read(generation).copy(activity=original.activity(),
                coreReads=mapOf(CardCoreEf.ACTIVITY to CardCoreRead(CardCoreEf.ACTIVITY,CardCoreReadStatus.COMPLETE,original)))
        }
        return CardApplicationReadBatch(listOf(
            CardApplicationAttempt.read(CardApplication.GEN1,read(CardGeneration.GEN1)),
            CardApplicationAttempt.read(CardApplication.GEN2,read(CardGeneration.GEN2V2))),emptyList())
    }
    private suspend fun import(request:String="M3",data:CardApplicationReadBatch=batch()) {
        pipeline.processApplications(data,request,true)
    }
    @Test fun bothApplicationsAndReimportCountOneReductionAndRetainEveryOriginal()=runBlocking {
        val data=batch(manual=true);import(data=data);scope()
        val first=snapshot();val one=DailyRestCalculator.calculate(first)
        assertEquals(2,first.sources.size)
        assertEquals(1,one.finalCounters.single().minimumObservedCount)
        val rest=one.candidates.single {it.range.start==t.plusSeconds(900*60L)}
        assertTrue(rest.rest.all {it.manualTachograph})
        assertEquals(2,rest.rest.flatMap {it.support}.flatMap {it.begin}.map {it.source.application}.distinct().size)
        val originals=first.sources.flatMap {it.files}.map {it.completeOriginalDigest to it.contentDigest}
        import("M3-reimport",data)
        val two=DailyRestCalculator.calculate(snapshot())
        assertEquals(one.findings.map {it.id},two.findings.map {it.id})
        assertEquals(one.candidates.map {it.id},two.candidates.map {it.id})
        assertTrue(two.sourceSnapshot.sources.flatMap {it.files}.map {it.completeOriginalDigest to it.contentDigest}.containsAll(originals))
    }
    @Test fun unknownWithdrawalNeverBecomesRestOrASafeBudget()=runBlocking {
        import(data=batch(unknown=true));scope();val r=DailyRestCalculator.calculate(snapshot())
        assertTrue(r.candidates.none {it.range.start>=t})
        assertTrue(r.findings.isEmpty())
        assertTrue(r.windows.filter {it.referenceStart==t}.all {it.provenSecondsUntilLatestRestStart==null})
        assertFalse(r.establishesLegalAllClear)
    }
    @Test fun threeActualPriorReductionsAreNotResetByTheMondayBoundary()=runBlocking {
        import(data=batch(prior=3,late=true));scope();val r=DailyRestCalculator.calculate(snapshot())
        val w=r.windows.single {it.referenceStart==t}
        assertEquals(3,w.counterBefore.minimumObservedCount)
        val f=r.findings.single {it.assessedRange.start==t}
        assertEquals(60L,f.missingRestSeconds);assertEquals(659*60L,f.creditedRestSeconds)
    }
    @Test fun observerPublishesM2AndM3ForExactlyTheSameCapturedRevision()=runBlocking {
        val repo=DrivingCheckRepository(ViolationInputRepository(db,adapter),adapter)
        val events=Channel<DrivingCheckState>(Channel.UNLIMITED)
        val job=launch(Dispatchers.Default) {repo.observe().collect {events.send(it)}}
        suspend fun ready(revision:String?=null):DrivingCheckState.Ready=withTimeout(30000) {
            while(true) when(val e=events.receive()) {
                is DrivingCheckState.Ready->if(revision==null || e.dailyRest.sourceSnapshot.context.revisionId==revision) return@withTimeout e
                is DrivingCheckState.Failed->fail("Combined time calculation failed")
                else->Unit
            }
            error("unreachable")
        }
        try {
            withTimeout(30000) {while(events.receive()!=DrivingCheckState.NoProfile) {}}
            import();val unknown=ready()
            assertSame(unknown.calculation.sourceSnapshot,unknown.dailyRest.sourceSnapshot)
            assertEquals(unknown.calculation.revision,unknown.dailyRest.drivingRevision)
            val ctx=scope();val known=ready(ctx.revisionId)
            assertEquals(known.calculation.inputRevision,known.dailyRest.inputRevision)
            assertSame(known.calculation.sourceSnapshot,known.dailyRest.sourceSnapshot)
            assertNotEquals(unknown.dailyRest.revision,known.dailyRest.revision)
            assertEquals(1,known.dailyRest.finalCounters.single().minimumObservedCount)
        } finally {job.cancelAndJoin();events.close()}
    }
    @Test fun newConcreteContextKeysPersistEncryptedAndReopenWithoutASchemaChange()=runBlocking {
        import();val subject=ContextSubject(ContextSubjectKind.SERVICE,"service")
        val from=t.minusSeconds(86400);val until=t.plusSeconds(8*86400)
        val extra=listOf(ContextKey.SINGLE_OCCASIONAL_SERVICE,ContextKey.ROAD_SAFETY_PRESERVED,
            ContextKey.WORKING_CONDITIONS_PRESERVED,ContextKey.APPLICABLE_MAX_WORKING_TIME_RESPECTED).map {
            ContextAssertion(it,ContextValue.Flag(true),subject,from,until,ContextOrigin.USER_CONTEXT,"synthetic",clock.instant())
        } + ContextAssertion(ContextKey.FERRY_ACCOMPANIES_VEHICLE,ContextValue.Flag(true),
            ContextSubject(ContextSubjectKind.JOURNEY,"ferry"),from,until,ContextOrigin.USER_CONTEXT,"synthetic",clock.instant())
        val ctx=scope(extra=extra);val s=snapshot();val result=DailyRestCalculator.calculate(s)
        db.close();open()
        assertEquals(ctx,contexts.current(s.profileId));assertEquals(result,DailyRestCalculator.calculate(snapshot()))
        assertEquals(23,AppDatabase.SCHEMA_VERSION)
        val header=java.io.File(context.getDatabasePath(AppDatabase.DATABASE_NAME).path).inputStream().use {ByteArray(16).also { bytes -> check(it.read(bytes)==16) }}
        assertFalse(header.contentEquals("SQLite format 3\u0000".toByteArray()))
        assertTrue(db.openHelper.writableDatabase.query("PRAGMA integrity_check").use {it.moveToFirst()&&it.getString(0)=="ok"})
    }
    @Test fun finalPublicationRejectsStaleContextAndImportEvenAfterBothCalculations()=runBlocking {
        import();val ctx=scope();val old=snapshot();DailyRestCalculator.calculate(old)
        assertTrue(adapter.publishIfCurrent(old) {})
        contexts.replace(old.profileId,ctx.revisionId,emptyList())
        assertFalse(adapter.publishIfCurrent(old) {fail("Stale context result published")})
        val newer=snapshot();DailyRestCalculator.calculate(newer)
        import("M3-new-source",batch(late=true))
        assertFalse(adapter.publishIfCurrent(newer) {fail("Stale import result published")})
    }
    @Test fun conflictingApplicationsRetainEvidenceWithoutAnAssuredRestVerdict()=runBlocking {
        import(data=batch(conflict=true));scope();val s=snapshot();val r=DailyRestCalculator.calculate(s)
        assertEquals(2,s.sources.size)
        assertTrue(s.intervals.any {it.meaning.quality==InputQuality.CONFLICT})
        assertTrue(r.findings.none {it.normalRuleDeficitIsProved})
        assertTrue(r.candidates.none {it.range.start>=t})
    }
    @Test fun unsupportedAetrContextDoesNotPublishGuaranteedEuResults()=runBlocking {
        import(data=batch(prior=3,late=true));scope("AETR_MIXED");val r=DailyRestCalculator.calculate(snapshot())
        assertTrue(r.findings.isEmpty())
        assertTrue(r.windows.all {it.provenSecondsUntilLatestRestStart==null})
        assertTrue(r.finalCounters.all {it.provenRemaining==null})
    }
    @Test fun sameMinuteRestAndWorkCannotBeResolvedByTheirStorageOrder()=runBlocking {
        import(data=batch(sameMinute=true));scope();val r=DailyRestCalculator.calculate(snapshot())
        assertTrue(r.sourceSnapshot.issues.any {it.code==InputIssueCode.AMBIGUOUS_MINUTE})
        assertTrue(r.findings.none {it.normalRuleDeficitIsProved})
        assertTrue(r.candidates.none {it.range.start>=t})
    }
    @Test fun nextMidnightRestMarkerProvesNineHoursButNoActualRestEnd()=runBlocking {
        import(data=batch(openRest=true));scope();val r=DailyRestCalculator.calculate(snapshot())
        val rest=r.candidates.single {it.range.start>=t}
        assertEquals(32400L,rest.netRestSeconds);assertFalse(rest.actualEndProved)
        assertEquals(t.plusSeconds(86400),r.observedThrough)
        assertTrue(r.anchors.none {it.range.start>=t})
    }
}
