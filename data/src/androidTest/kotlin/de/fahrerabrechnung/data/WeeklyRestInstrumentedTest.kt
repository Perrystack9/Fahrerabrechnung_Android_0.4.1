package de.fahrerabrechnung.data

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import de.fahrerabrechnung.data.card.*
import de.fahrerabrechnung.data.db.AppDatabase
import de.fahrerabrechnung.data.security.EncryptedDatabaseFactory
import de.fahrerabrechnung.data.violation.*
import de.fahrerabrechnung.domain.violation.input.*
import de.fahrerabrechnung.domain.violation.weekly.*
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

/** Real encrypted DB, existing G1/G2 parser/import and final M1 revision CAS; entirely synthetic. */
@RunWith(AndroidJUnit4::class)
class WeeklyRestInstrumentedTest {
    private lateinit var context: Context
    private lateinit var db: AppDatabase
    private lateinit var key: ByteArray
    private lateinit var pipeline: DriverCardImportPipeline
    private lateinit var contexts: ViolationContextRepository
    private lateinit var adapter: DriverCardInputAdapter
    private val monday=M1InputFixture.day
    private val clock=Clock.fixed(Instant.parse("2026-10-16T12:00:00Z"),ZoneOffset.UTC)
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
    private suspend fun scope(area:String="EU_ONLY",extra:List<ContextAssertion> = emptyList()):InputContext {
        val profile=checkNotNull(db.driverProfileDao().localProfile()).id
        val old=contexts.current(profile);val subject=ContextSubject(ContextSubjectKind.PROFILE,profile)
        fun a(k:ContextKey,v:ContextValue)=ContextAssertion(k,v,subject,monday.minusSeconds(30*86400),null,
            ContextOrigin.USER_CONTEXT,"synthetic-M4-context",clock.instant())
        return contexts.replace(profile,old.revisionId,listOf(a(ContextKey.ROAD_CARRIAGE,ContextValue.Flag(true)),
            a(ContextKey.TRANSPORT_KIND,ContextValue.Text("GOODS")),a(ContextKey.JOURNEY_AREA,ContextValue.Text(area)),
            a(ContextKey.MAX_MASS_KG,ContextValue.Number(40000)))+extra)
    }
    private fun batch(late:Int=1,manual:Boolean=false,unknown:Boolean=false,conflict:Boolean=false,
        sameMinute:Boolean=false,openRest:Boolean=false):CardApplicationReadBatch {
        fun read(generation:CardGeneration):CardReadResult {
            val days=mutableListOf<Pair<Instant,IntArray>>()
            days+=monday.minusSeconds(2*86400) to intArrayOf(0x2000,180)
            days+=monday.minusSeconds(86400) to intArrayOf(0)
            repeat(6) { days+=monday.plusSeconds(it*86400L) to intArrayOf(0x1000) }
            val minute=late
            val restWord=when {unknown->0x2000;manual->0x6000;conflict && generation!=CardGeneration.GEN1->0x1000;else->0}
            days+=monday.plusSeconds(6*86400) to when {
                minute==0->intArrayOf(restWord)
                sameMinute->intArrayOf(0x1000,restWord+minute,0x1000+minute)
                else->intArrayOf(0x1000,restWord+minute)
            }
            days+=monday.plusSeconds(7*86400) to if(openRest) intArrayOf(restWord) else intArrayOf(restWord,0x1000+1260+minute)
            days+=monday.plusSeconds(8*86400) to if(openRest) intArrayOf(restWord) else intArrayOf(0x1000)
            var previous=0;var newest=0;var offset=0
            val records=days.map { (date,words)->M1InputFixture.record(*words,date=date,previous=previous).also {
                previous=it.size;newest=offset;offset+=it.size } }
            val original=CardCoreSnapshot(CardCoreEf.ACTIVITY,generation,
                ByteBuffer.allocate(4+offset).putShort(0).putShort(newest.toShort()).apply {records.forEach {put(it)}}.array())
            return M1InputFixture.read(generation).copy(activity=original.activity(),
                coreReads=mapOf(CardCoreEf.ACTIVITY to CardCoreRead(CardCoreEf.ACTIVITY,CardCoreReadStatus.COMPLETE,original)))
        }
        return CardApplicationReadBatch(listOf(CardApplicationAttempt.read(CardApplication.GEN1,read(CardGeneration.GEN1)),
            CardApplicationAttempt.read(CardApplication.GEN2,read(CardGeneration.GEN2V2))),emptyList())
    }
    private suspend fun import(data:CardApplicationReadBatch=batch(),request:String="M4") {
        pipeline.processApplications(data,request,true)
    }
    @Test fun DT37BothApplicationsAndRepeatedImportsKeepOneLateEpisodeAndEveryOrigin()=runBlocking {
        val data=batch();import(data);scope()
        val one=WeeklyRestCalculator.calculate(snapshot())
        val episode=one.findings.single {it.law.id=="W02"}
        assertEquals(60L,episode.excessOrMissingSeconds)
        val originals=one.sourceSnapshot.sources.flatMap {it.files}.map {it.completeOriginalDigest to it.contentDigest}
        assertTrue(one.candidates.flatMap {it.rest}.all {slice->slice.support.flatMap {it.begin}.map {it.source.application}.distinct().size==2})
        import(data,"M4-reimport")
        val two=WeeklyRestCalculator.calculate(snapshot())
        assertEquals(one.findings.map {it.id to it.excessOrMissingSeconds},two.findings.map {it.id to it.excessOrMissingSeconds})
        assertTrue(two.sourceSnapshot.sources.flatMap {it.files}.map {it.completeOriginalDigest to it.contentDigest}.containsAll(originals))
    }
    private fun ledgerBatch():CardApplicationReadBatch {
        val start=Instant.parse("2026-08-31T00:00:00Z")
        val end=Instant.parse("2026-10-02T00:00:00Z")
        val changes=mutableListOf(start to 0x1000)
        for ((date,minutes) in listOf("2026-09-05T00:00:00Z" to 2700,"2026-09-12T00:00:00Z" to 1440,
            "2026-09-19T00:00:00Z" to 2700,"2026-09-26T00:00:00Z" to 1440,"2026-09-30T00:00:00Z" to 1920)) {
            val at=Instant.parse(date);changes+=at to 0;changes+=at.plusSeconds(minutes*60L) to 0x1000
        }
        changes+=end to 0x1000
        fun read(generation:CardGeneration):CardReadResult {
            var date=start;var previous=0;var newest=0;var offset=0
            val records=mutableListOf<ByteArray>()
            while(date<=end) {
                val next=date.plusSeconds(86400)
                val current=changes.filter {it.first>=date&&it.first<next}.toMutableList()
                if(current.none {it.first==date}) current.add(0,date to changes.last {it.first<date}.second)
                val words=current.map {it.second+((it.first.epochSecond-date.epochSecond)/60).toInt()}.toIntArray()
                val record=M1InputFixture.record(*words,date=date,previous=previous)
                records+=record;previous=record.size;newest=offset;offset+=record.size;date=next
            }
            val original=CardCoreSnapshot(CardCoreEf.ACTIVITY,generation,
                ByteBuffer.allocate(4+offset).putShort(0).putShort(newest.toShort()).apply {records.forEach {put(it)}}.array())
            return M1InputFixture.read(generation).copy(activity=original.activity(),coreReads=mapOf(
                CardCoreEf.ACTIVITY to CardCoreRead(CardCoreEf.ACTIVITY,CardCoreReadStatus.COMPLETE,original)))
        }
        return CardApplicationReadBatch(listOf(CardApplicationAttempt.read(CardApplication.GEN1,read(CardGeneration.GEN1)),
            CardApplicationAttempt.read(CardApplication.GEN2,read(CardGeneration.GEN2V2))),emptyList())
    }
    @Test fun DT38OneWholeDebtCanBeAllocatedAndTheOtherStaysOpenAcrossReopenAndReplay()=runBlocking {
        val data=ledgerBatch();import(data,"M4-ledger");scope()
        val before=WeeklyRestCalculator.calculate(snapshot())
        assertTrue(before.alternativesComplete)
        val witness=before.allocations.first {it.debts.size==2 && it.fulfilledDebts.size==1 && it.openDebts.size==1}
        assertEquals(75600,witness.fulfilledDebts.single().seconds)
        assertEquals(75600,witness.openDebts.single().seconds)
        assertEquals(1,witness.reservations.size)
        val originalFiles=before.sourceSnapshot.sources.flatMap {it.files}.map {it.completeOriginalDigest to it.contentDigest}
        db.close();open()
        val reopened=WeeklyRestCalculator.calculate(snapshot())
        assertEquals(before,reopened)
        import(data,"M4-ledger-replay")
        val replayed=WeeklyRestCalculator.calculate(snapshot())
        assertTrue(replayed.allocations.any {it.id==witness.id && it.openDebts.map {d->d.id}==witness.openDebts.map {d->d.id}})
        assertTrue(replayed.sourceSnapshot.sources.flatMap {it.files}.map {it.completeOriginalDigest to it.contentDigest}.containsAll(originalFiles))
        val profile=before.profileId;val subject=ContextSubject(ContextSubjectKind.PROFILE,profile)
        scope(extra=listOf(ContextAssertion(ContextKey.EMPLOYER_COUNTRY,ContextValue.Text("DE"),subject,
            monday.minusSeconds(30*86400),null,ContextOrigin.USER_CONTEXT,"synthetic-ledger-context",clock.instant())))
        val revised=WeeklyRestCalculator.calculate(snapshot())
        assertNotEquals(replayed.revision,revised.revision)
        assertTrue(revised.allocations.any {it.fulfilledDebts.size==1 && it.openDebts.size==1})
    }
    @Test fun CombinedObserverPublishesOneSnapshotForM2M3AndM4()=runBlocking {
        val repository=DrivingCheckRepository(ViolationInputRepository(db,adapter),adapter)
        val events=Channel<DrivingCheckState>(Channel.UNLIMITED)
        val observer=launch(Dispatchers.Default) {repository.observe().collect {events.send(it)}}
        suspend fun ready(contextRevision:String?=null):DrivingCheckState.Ready=withTimeout(30000) {
            while(true) when(val event=events.receive()) {
                is DrivingCheckState.Ready->if(contextRevision==null || event.weeklyRest.sourceSnapshot.context.revisionId==contextRevision) return@withTimeout event
                is DrivingCheckState.Failed->fail("Combined M2/M3/M4 calculation failed")
                else->Unit
            }
            error("unreachable")
        }
        try {
            withTimeout(30000) {while(events.receive()!=DrivingCheckState.NoProfile) {}}
            import();val unknown=ready()
            assertSame(unknown.calculation.sourceSnapshot,unknown.dailyRest.sourceSnapshot)
            assertSame(unknown.calculation.sourceSnapshot,unknown.weeklyRest.sourceSnapshot)
            val ctx=scope();val known=ready(ctx.revisionId)
            assertSame(known.dailyRest,known.weeklyRest.dailyRest)
            assertSame(known.calculation.sourceSnapshot,known.weeklyRest.sourceSnapshot)
            assertEquals(known.calculation.revision,known.weeklyRest.drivingRevision)
            assertNotEquals(unknown.weeklyRest.revision,known.weeklyRest.revision)
            assertEquals(60,known.weeklyRest.findings.single {it.law.id=="W02"}.excessOrMissingSeconds)
        } finally {observer.cancelAndJoin();events.close()}
    }
    @Test fun FinalCasRejectsStaleContextAfterAllThreeCalculations()=runBlocking {
        import();val ctx=scope();val old=snapshot();WeeklyRestCalculator.calculate(old)
        assertTrue(adapter.publishIfCurrent(old) {})
        contexts.replace(old.profileId,ctx.revisionId,emptyList())
        assertFalse(adapter.publishIfCurrent(old) {fail("Stale weekly context published")})
    }
    @Test fun FinalCasRejectsStaleImportAfterAllThreeCalculations()=runBlocking {
        import();scope();val old=snapshot();WeeklyRestCalculator.calculate(old)
        import(batch(late=0),"M4-new-import")
        assertFalse(adapter.publishIfCurrent(old) {fail("Stale weekly source published")})
    }
    @Test fun NewContextKeysPersistEncryptedAndReopenAtUnchangedRoom23()=runBlocking {
        import();val subject=ContextSubject(ContextSubjectKind.SERVICE,"concrete-bus-service")
        val extras=listOf(ContextKey.BUS_DIGITAL_TACHOGRAPH_ANNEX_IB,ContextKey.OCCASIONAL_FOREIGN_STAY_24H).map {
            ContextAssertion(it,ContextValue.Flag(true),subject,monday,monday.plusSeconds(12*86400),
                ContextOrigin.USER_CONTEXT,"synthetic-M4-technical-evidence",clock.instant()) }
        val ctx=scope(extra=extras);val before=WeeklyRestCalculator.calculate(snapshot())
        db.close();open()
        assertEquals(ctx,contexts.current(before.profileId))
        assertEquals(before,WeeklyRestCalculator.calculate(snapshot()))
        assertEquals(23,AppDatabase.SCHEMA_VERSION)
        val header=context.getDatabasePath(AppDatabase.DATABASE_NAME).inputStream().use {ByteArray(16).also {bytes->check(it.read(bytes)==16)}}
        assertFalse(header.contentEquals("SQLite format 3\u0000".toByteArray()))
        assertTrue(db.openHelper.writableDatabase.query("PRAGMA integrity_check").use {it.moveToFirst()&&it.getString(0)=="ok"})
    }
    @Test fun ActualManualRestQualifiesButDoesNotBecomeRecordedCrew()=runBlocking {
        import(batch(manual=true));scope();val r=WeeklyRestCalculator.calculate(snapshot())
        val rest=r.candidates.single {it.range.start>=monday}
        assertTrue(rest.rest.all {it.manualTachograph})
        assertTrue(r.sourceSnapshot.intervals.filter {it.range.overlaps(rest.range)}.all {it.meaning.crew!=CrewEvidence.CREW})
        assertEquals(60,r.findings.single {it.law.id=="W02"}.excessOrMissingSeconds)
    }
    @Test fun UnknownCardWithdrawalCannotFulfilWeeklyRestOrGuaranteeBudgets()=runBlocking {
        import(batch(unknown=true));scope();val r=WeeklyRestCalculator.calculate(snapshot())
        assertTrue(r.candidates.none {it.range.start>=monday})
        assertTrue(r.findings.none {it.normalRuleDeficitIsProved})
        assertTrue(r.deadlines.all {it.provenSecondsRemaining==null})
    }
    @Test fun ConflictingApplicationsPreserveSourcesAndDoNotInventWeeklyRest()=runBlocking {
        import(batch(conflict=true));scope();val r=WeeklyRestCalculator.calculate(snapshot())
        assertTrue(r.sourceSnapshot.intervals.any {it.meaning.quality==InputQuality.CONFLICT})
        assertEquals(2,r.sourceSnapshot.sources.size)
        assertTrue(r.findings.none {it.normalRuleDeficitIsProved})
    }
    @Test fun EqualMinuteAmbiguityDoesNotChooseAConvenientRestByStorageOrder()=runBlocking {
        import(batch(sameMinute=true));scope();val r=WeeklyRestCalculator.calculate(snapshot())
        assertTrue(r.sourceSnapshot.issues.any {it.code==InputIssueCode.AMBIGUOUS_MINUTE})
        assertTrue(r.findings.none {it.normalRuleDeficitIsProved})
    }
    @Test fun MidnightMarkersDoNotCreateAClosedWeeklyEnd()=runBlocking {
        import(batch(openRest=true));scope();val r=WeeklyRestCalculator.calculate(snapshot())
        val rest=r.candidates.single {it.range.start>=monday}
        assertFalse(rest.actualEndProved)
        assertTrue(r.deadlines.none {it.anchorCandidateId==rest.id})
    }
    @Test fun UnsupportedAetrScopeCannotProduceAssuredEuWeeklyResults()=runBlocking {
        import();scope("AETR_MIXED");val r=WeeklyRestCalculator.calculate(snapshot())
        assertTrue(r.findings.none {it.normalRuleDeficitIsProved})
        assertTrue(r.deadlines.all {it.provenSecondsRemaining==null})
    }
    @Test fun CancellationIsRethrownAndLeavesEncryptedOriginalsUnchanged()=runBlocking {
        import();scope();val before=snapshot()
        try {
            WeeklyRestCalculator.calculate(before,checkCancellation={throw CancellationException("synthetic")})
            fail("cancellation swallowed")
        } catch (_:CancellationException) { /* expected */ }
        assertEquals(before,snapshot())
    }
}
