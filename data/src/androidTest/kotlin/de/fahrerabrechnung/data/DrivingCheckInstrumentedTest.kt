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

/** Real SQLCipher, existing parser/import pipeline and both applications; entirely synthetic data. */
@RunWith(AndroidJUnit4::class)
class DrivingCheckInstrumentedTest {
    private lateinit var context: Context
    private lateinit var db: AppDatabase
    private lateinit var key: ByteArray
    private lateinit var pipeline: DriverCardImportPipeline
    private lateinit var contexts: ViolationContextRepository
    private lateinit var adapter: DriverCardInputAdapter
    private val clock = Clock.fixed(Instant.parse("2026-10-15T07:00:00Z"), ZoneOffset.UTC)
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
    private suspend fun snapshot(): ViolationInputSnapshot {
        val input=adapter.capture()
        return InputSnapshotBuilder.build(checkNotNull(input.sources),checkNotNull(input.context))
    }
    private suspend fun setScope(): InputContext {
        val profile=checkNotNull(db.driverProfileDao().localProfile()).id
        val current=contexts.current(profile)
        val subject=ContextSubject(ContextSubjectKind.PROFILE,profile)
        fun assertion(k:ContextKey,v:ContextValue)=ContextAssertion(k,v,subject,M1InputFixture.day,null,
            ContextOrigin.USER_CONTEXT,"synthetic-M2-context",clock.instant())
        return contexts.replace(profile,current.revisionId,listOf(
            assertion(ContextKey.ROAD_CARRIAGE,ContextValue.Flag(true)),
            assertion(ContextKey.TRANSPORT_KIND,ContextValue.Text("GOODS")),
            assertion(ContextKey.JOURNEY_AREA,ContextValue.Text("EU_ONLY")),
            assertion(ContextKey.MAX_MASS_KG,ContextValue.Number(40000))))
    }
    private suspend fun importDrive(request:String="M2-both") {
        pipeline.processApplications(M1InputFixture.batch(0,0x1800+660,0x1000+931),request,true)
    }

    @Test fun bothApplicationsAndRepeatedImportsCountOnceWithAllOriginalEvidencePreserved()=runBlocking {
        importDrive();setScope()
        val before=snapshot()
        val one=DrivingCalculator.calculate(before)
        assertEquals(2,before.sources.size)
        assertEquals(60,one.findings.single {it.law.id=="B01"}.excessSeconds)
        assertTrue(one.findings.single {it.law.id=="B01"}.excessDriving.all {
            it.support.flatMap {s->s.begin}.map {r->r.source.sessionId}.distinct().size==2})
        assertEquals(before.revision,snapshot().revision)
        val originals=before.sources.flatMap {it.files}.map {it.completeOriginalDigest to it.contentDigest}
        pipeline.processApplications(M1InputFixture.batch(0,0x1800+660,0x1000+931),"M2-replay")
        val two=DrivingCalculator.calculate(snapshot())
        assertEquals(one.findings.map {it.id},two.findings.map {it.id})
        assertEquals(one.findings.map {it.excessSeconds},two.findings.map {it.excessSeconds})
        assertTrue(two.sourceSnapshot.sources.flatMap {it.files}.map {it.completeOriginalDigest to it.contentDigest}.containsAll(originals))
        assertTrue(two.sourceSnapshot.sources.size>=2)
    }
    @Test fun downstreamPublicationRejectsOldContextAndOldImportTokens()=runBlocking {
        importDrive();val context=setScope();val first=snapshot()
        assertTrue(adapter.publishIfCurrent(first) {})
        contexts.replace(first.profileId,context.revisionId,emptyList())
        assertFalse(adapter.publishIfCurrent(first) {fail("Old context result published")})
        val second=snapshot();assertTrue(adapter.publishIfCurrent(second) {})
        pipeline.processApplications(M1InputFixture.batch(),"M2-new-source")
        assertFalse(adapter.publishIfCurrent(second) {fail("Old source result published")})
    }
    @Test fun automaticObserverUsesCompleteCommittedM1InputsAndRecalculatesAfterContext()=runBlocking {
        val repository=DrivingCheckRepository(ViolationInputRepository(db,adapter),adapter)
        val events=Channel<DrivingCheckState>(Channel.UNLIMITED)
        val observer=launch(Dispatchers.Default) {repository.observe().collect {events.send(it)}}
        suspend fun ready():DrivingCalculation=withTimeout(30000) {
            while(true) {
                when(val state=events.receive()) {
                    is DrivingCheckState.Ready->return@withTimeout state.calculation
                    is DrivingCheckState.Failed->fail("M2 observer failed")
                    else->Unit
                }
            }
            error("unreachable")
        }
        try {
            withTimeout(30000) {while(events.receive()!=DrivingCheckState.NoProfile) {}}
            importDrive()
            val unknown=ready()
            assertEquals(2,unknown.sourceSnapshot.sources.size)
            assertTrue(unknown.findings.isEmpty())
            assertTrue(unknown.projections.all {it.provenRemainingSeconds==null})
            val revised=setScope()
            val known=withTimeout(30000) {
                var current=ready()
                while(current.sourceSnapshot.context.revisionId!=revised.revisionId) current=ready()
                current
            }
            assertEquals(2,known.sourceSnapshot.sources.size)
            assertEquals(60,known.findings.single {it.law.id=="B01"}.excessSeconds)
            assertNotEquals(unknown.revision,known.revision)
        } finally {observer.cancelAndJoin();events.close()}
    }
    @Test fun actualManualRestAndUnknownWithdrawalKeepDifferentM2Proofs()=runBlocking {
        pipeline.processApplications(M1InputFixture.batch(0,0x1800+660,0x6000+930,0x1800+975,0x1000+976),"manual",true)
        setScope()
        val known=DrivingCalculator.calculate(snapshot())
        val p=known.projections.last {it.law.id=="B01"}
        assertEquals(269*60L,p.provenRemainingSeconds)
        assertTrue(known.findings.none {it.law.id=="B01"})
        assertTrue(known.sourceSnapshot.intervals.any {it.meaning.manualTachograph})
    }
    @Test fun unknownWithdrawalDoesNotCreateARestOrALegalAllClear()=runBlocking {
        pipeline.processApplications(M1InputFixture.batch(0,0x1800+660,0x2000+930,0x1800+975,0x1000+976),"unknown",true)
        setScope()
        val r=DrivingCalculator.calculate(snapshot())
        assertNull(r.projections.last {it.law.id=="B01"}.provenRemainingSeconds)
        assertFalse(r.establishesLegalAllClear)
        assertTrue(r.issues.any {it.reason.contains("UNDETERMINED")})
    }
    @Test fun readOnlyCalculationReopensWithoutDerivedTablesOrRewritingSources()=runBlocking {
        importDrive();val context=setScope();val before=snapshot();val result=DrivingCalculator.calculate(before)
        assertEquals(context.revisionId,contexts.current(before.profileId).revisionId)
        db.close();open()
        val after=snapshot();val reopened=DrivingCalculator.calculate(after)
        assertEquals(before.revision,after.revision)
        assertEquals(result,reopened)
        assertEquals(23,AppDatabase.SCHEMA_VERSION)
        assertTrue(db.openHelper.writableDatabase.query("PRAGMA integrity_check").use {it.moveToFirst()&&it.getString(0)=="ok"})
    }
}
