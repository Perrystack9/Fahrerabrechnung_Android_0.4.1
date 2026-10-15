package de.fahrerabrechnung.data

import android.content.Context
import androidx.room.withTransaction
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import de.fahrerabrechnung.data.card.*
import de.fahrerabrechnung.data.db.*
import de.fahrerabrechnung.data.security.EncryptedDatabaseFactory
import de.fahrerabrechnung.data.violation.*
import de.fahrerabrechnung.domain.violation.input.*
import java.security.SecureRandom
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import kotlinx.coroutines.*
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ViolationContextInstrumentedTest {
    private lateinit var context: Context
    private lateinit var db: AppDatabase
    private lateinit var key: ByteArray
    private lateinit var repository: ViolationContextRepository
    private lateinit var profile: String
    private val clock = Clock.fixed(Instant.parse("2026-10-15T07:00:00Z"), ZoneOffset.UTC)
    @Before fun before() = runBlocking {
        context = ApplicationProvider.getApplicationContext(); context.deleteDatabase(AppDatabase.DATABASE_NAME)
        key = ByteArray(32).also(SecureRandom()::nextBytes); open()
        val activities = CardActivityImportRepository(db.cardActivityImportDao(), clock)
        val vehicles = CardVehicleImportRepository(db.cardVehicleImportDao(), clock)
        val imports = DriverCardImportRepository(db, activities, vehicles, CardSourcePairRepository(db, activities, vehicles, clock), clock)
        imports.bind(checkNotNull(M1InputFixture.read().card), true)
        profile = checkNotNull(db.driverProfileDao().localProfile()).id
    }
    private fun open() {
        db = EncryptedDatabaseFactory.create(context, key); repository = ViolationContextRepository(db, clock)
    }
    @After fun after() { db.close(); context.deleteDatabase(AppDatabase.DATABASE_NAME) }
    private fun entry(mass: Long = 40000) = ContextAssertion(ContextKey.MAX_MASS_KG, ContextValue.Number(mass),
        ContextSubject(ContextSubjectKind.PROFILE, profile), M1InputFixture.day, null, ContextOrigin.USER_CONTEXT, "synthetic-profile", clock.instant())

    @Test fun mi23_appendOnlyHistoryRepeatAnswerReopenAndTemporalReplacement() = runBlocking {
        assertEquals("none", repository.current(profile).revisionId)
        assertTrue(db.violationContextDao().history(profile).isEmpty())
        val first = repository.replace(profile, "none", listOf(entry()))
        val repeated = repository.replace(profile, first.revisionId, listOf(entry().copy(recordedAt = clock.instant().plusSeconds(100))))
        assertEquals(first, repeated)
        assertEquals(1, db.violationContextDao().history(profile).size)
        val second = repository.replace(profile, first.revisionId, listOf(entry().copy(until = clock.instant()),
            entry(2000).copy(from = clock.instant())))
        assertNotEquals(first.revisionId, second.revisionId)
        val rows = db.violationContextDao().history(profile)
        assertEquals(listOf(1L, 2L), rows.map { it.sequence }); assertEquals(first.revisionId, rows.last().previousId)
        db.close(); open()
        assertEquals(second, repository.current(profile))
        assertEquals(rows, db.violationContextDao().history(profile))
        assertEquals(ContextValue.Number(40000), second.resolveAt(ContextKey.MAX_MASS_KG, M1InputFixture.day).value)
        assertEquals(ContextValue.Number(2000), second.resolveAt(ContextKey.MAX_MASS_KG, clock.instant()).value)
    }

    @Test fun concurrentWritersRejectStaleExpectedRevisionWithoutLostHistory() = runBlocking {
        val results = listOf(2000L, 40000L).map { mass -> async(Dispatchers.Default) {
            try { repository.replace(profile, "none", listOf(entry(mass))); "stored" }
            catch (_: ContextRevisionConflict) { "conflict" }
        } }.awaitAll()
        assertEquals(listOf("conflict", "stored"), results.sorted())
        assertEquals(1, db.violationContextDao().history(profile).size)
    }

    @Test fun transactionRollbackAndCancellationLeaveContextAndCardSourcesUntouched() = runBlocking {
        val first = repository.replace(profile, "none", listOf(entry()))
        try {
            db.withTransaction { repository.replace(profile, first.revisionId, listOf(entry(2000))); error("synthetic rollback") }
        } catch (_: IllegalStateException) { }
        assertEquals(first, repository.current(profile))
        val job = launch { currentCoroutineContext().cancel(); repository.replace(profile, first.revisionId, emptyList()) }
        job.join(); assertTrue(job.isCancelled)
        assertEquals(first, repository.current(profile))
        assertEquals(1, db.violationContextDao().history(profile).size)
        assertEquals(1, db.driverProfileDao().cards(profile).size)
    }

    @Test fun crossProfileAndInvalidRestContextAreRejectedWithoutBackfill() = runBlocking {
        db.profileDao().insert(ProfileEntity("foreign", "Synthetic foreign", 1, 1))
        try { repository.current("foreign"); fail("Foreign profile must not be read") } catch (_: IllegalArgumentException) { }
        try { repository.replace("foreign", "none", listOf(entry())); fail("Foreign profile must not be written") }
        catch (_: IllegalArgumentException) { }
        assertThrows(IllegalArgumentException::class.java) { entry().copy(key = ContextKey.REST_START_COUNTRY, value = ContextValue.Text("FR")) }
        assertTrue(db.violationContextDao().history(profile).isEmpty())
        assertTrue(db.violationContextDao().history("foreign").isEmpty())
    }

    @Test fun corruptContextNeverBecomesAnEmptyDefaultOrPositiveScope() = runBlocking {
        val first = repository.replace(profile, "none", listOf(entry()))
        db.openHelper.writableDatabase.execSQL("UPDATE violation_context_revisions SET payload = '{}' WHERE id = ?", arrayOf(first.revisionId))
        try { repository.current(profile); fail("Corrupt revision must be rejected") } catch (_: IllegalStateException) { }
        assertEquals(1, db.violationContextDao().history(profile).size)
    }

    @Test fun deterministicIdsAreContentAndPredecessorBoundNotSubmissionClock() {
        val digest = "a".repeat(64); val semantic = "b".repeat(64)
        val first = ViolationContextRepository.revisionId("P1", null, 1, digest, semantic)
        assertEquals(first, ViolationContextRepository.revisionId("P1", null, 1, digest, semantic))
        assertNotEquals(first, ViolationContextRepository.revisionId("P2", null, 1, digest, semantic))
        assertNotEquals(first, ViolationContextRepository.revisionId("P1", first, 2, digest, semantic))
    }
}
