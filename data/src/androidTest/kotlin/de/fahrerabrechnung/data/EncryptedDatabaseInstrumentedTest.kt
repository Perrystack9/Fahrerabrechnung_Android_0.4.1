package de.fahrerabrechnung.data

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import de.fahrerabrechnung.data.card.*
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import java.nio.ByteBuffer
import de.fahrerabrechnung.data.db.AppDatabase
import de.fahrerabrechnung.data.db.ProfileEntity
import de.fahrerabrechnung.data.db.CardActivityImportEntity
import de.fahrerabrechnung.data.db.CardVehicleImportEntity
import de.fahrerabrechnung.data.security.EncryptedDatabaseFactory
import java.io.File
import java.security.SecureRandom
import kotlinx.coroutines.runBlocking
import android.database.sqlite.SQLiteException
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EncryptedDatabaseInstrumentedTest {
    private lateinit var context: Context
    private lateinit var db: AppDatabase
    private lateinit var correctPassphrase: ByteArray

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        context.deleteDatabase(AppDatabase.DATABASE_NAME)
        correctPassphrase = ByteArray(32).also(SecureRandom()::nextBytes)
        db = EncryptedDatabaseFactory.create(context, correctPassphrase)
    }

    @After
    fun tearDown() {
        if (::db.isInitialized && db.isOpen) db.close()
        context.deleteDatabase(AppDatabase.DATABASE_NAME)
    }

    @Test
    fun encryptedDatabaseReopensWithCorrectKeyAndSupportsBasicCrud() = runBlocking {
        val now = System.currentTimeMillis()
        db.profileDao().insert(ProfileEntity("p1", "Testprofil", now, now))
        assertEquals("Testprofil", db.profileDao().getById("p1")?.name)

        db.close()

        val reopened = EncryptedDatabaseFactory.create(context, correctPassphrase)
        try {
            assertEquals("Testprofil", reopened.profileDao().getById("p1")?.name)
        } finally {
            reopened.close()
        }

        val header = File(context.getDatabasePath(AppDatabase.DATABASE_NAME).absolutePath)
            .inputStream().use { input -> ByteArray(16).also { input.read(it) } }
            .toString(Charsets.US_ASCII)
        assertFalse(
            "Encrypted DB must not expose the SQLite plaintext header",
            header.startsWith("SQLite format 3"),
        )
    }

    @Test
    fun encryptedDatabaseCannotBeReadWithWrongKey() {
        runBlocking {
            val now = System.currentTimeMillis()
            db.profileDao().insert(ProfileEntity("p1", "Testprofil", now, now))
        }
        db.close()

        val wrongPassphrase = ByteArray(32).also(SecureRandom()::nextBytes)
        val wrongKeyDb = EncryptedDatabaseFactory.create(context, wrongPassphrase)
        try {
            assertThrows(SQLiteException::class.java) {
                runBlocking { wrongKeyDb.profileDao().getById("p1") }
            }
        } finally {
            wrongKeyDb.close()
        }
    }

    @Test
    fun activitySnapshotSurvivesEncryptedReopenAndDuplicateIsIgnored() = runBlocking {
        val snapshot = CardActivityImportEntity(
            id = "same-source", cardNumber = "test-card", generation = "GEN2V2",
            oldestDayOffset = 0, newestDayOffset = 0,
            dailyRecords = byteArrayOf(1, 2, 3), dayCount = 1, importedAtEpochMillis = 100L,
        )
        assertFalse(db.cardActivityImportDao().insert(snapshot) == -1L)
        assertEquals(-1L, db.cardActivityImportDao().insert(snapshot))
        db.close()

        val reopened = EncryptedDatabaseFactory.create(context, correctPassphrase)
        try {
            val saved = reopened.cardActivityImportDao().latest()!!
            assertEquals(snapshot.cardNumber, saved.cardNumber)
            assertEquals(snapshot.dayCount, saved.dayCount)
            assertEquals(snapshot.dailyRecords.toList(), saved.dailyRecords.toList())
        } finally {
            reopened.close()
        }
    }

    @Test
    fun activitySnapshotsAreListedNewestFirstAfterEncryptedReopen() = runBlocking {
        val older = CardActivityImportEntity(
            id = "older", cardNumber = "test-card", generation = "GEN2",
            oldestDayOffset = 0, newestDayOffset = 0,
            dailyRecords = byteArrayOf(1), dayCount = 1, importedAtEpochMillis = 100L,
        )
        val newer = older.copy(id = "newer", importedAtEpochMillis = 200L,
            dailyRecords = byteArrayOf(2))
        db.cardActivityImportDao().insert(older)
        db.cardActivityImportDao().insert(newer)
        db.close()

        val reopened = EncryptedDatabaseFactory.create(context, correctPassphrase)
        try {
            assertEquals(listOf("newer", "older"), reopened.cardActivityImportDao().all().map { it.id })
        } finally {
            reopened.close()
        }
    }

    @Test
    fun vehicleSnapshotSurvivesEncryptedReopenAndDuplicateIsIgnored() = runBlocking {
        val snapshot = CardVehicleImportEntity(
            id = "same-vehicle-source", cardNumber = "test-card", generation = "GEN2V2",
            newestRecordIndex = 0, recordCount = 1,
            records = byteArrayOf(4, 5, 6), useCount = 1, importedAtEpochMillis = 200L,
        )
        assertFalse(db.cardVehicleImportDao().insert(snapshot) == -1L)
        assertEquals(-1L, db.cardVehicleImportDao().insert(snapshot))
        db.close()

        val reopened = EncryptedDatabaseFactory.create(context, correctPassphrase)
        try {
            val saved = reopened.cardVehicleImportDao().latest()!!
            assertEquals(snapshot.cardNumber, saved.cardNumber)
            assertEquals(snapshot.useCount, saved.useCount)
            assertEquals(snapshot.records.toList(), saved.records.toList())
            assertEquals(1, reopened.cardVehicleImportDao().all().size)
        } finally {
            reopened.close()
        }
    }

    private val testClock = Clock.fixed(Instant.parse("2026-09-29T12:00:00Z"), ZoneOffset.UTC)

    private fun pairs(database: AppDatabase = db) = CardSourcePairRepository(database,
        CardActivityImportRepository(database.cardActivityImportDao(), testClock),
        CardVehicleImportRepository(database.cardVehicleImportDao(), testClock), testClock)

    /** Fabricated bytes only; no real card data in fixtures. */
    private fun completeRead(cardNumber: String = "synthetic-card"): CardReadResult {
        val day = ByteBuffer.allocate(14).putShort(0).putShort(14)
            .putInt(Instant.parse("2026-09-28T00:00:00Z").epochSecond.toInt())
            .putShort(1).putShort(0).putShort(0).array()
        val vehicle = ByteBuffer.allocate(48)
            .put(byteArrayOf(0, 0, 1, 0, 0, 2))
            .putInt(1_700_000_000).putInt(1_700_001_000)
            .put(13.toByte()).put(1.toByte())
            .put("TEST 1       ".toByteArray()).putShort(1)
            .put("WDB12345678901234".toByteArray()).array()
        val identity = CardIdentity(cardNumber, 13, "Test", Instant.EPOCH,
            Instant.EPOCH, Instant.parse("2030-01-01T00:00:00Z"))
        val card = DriverCard(CardGeneration.GEN2V2, identity,
            DriverIdentity("Test", "Fixture", java.time.LocalDate.of(2000, 2, 29), "de"))
        return CardReadResult(CardRecognition.DRIVER, CardGeneration.GEN2V2, card,
            CardActivityFile(0, 0, day), CardVehiclesFile(0, 1, CardGeneration.GEN2V2, vehicle))
    }

    @Test fun pairedSourcesDeduplicateAndReopenTogetherWithoutCard() = runBlocking {
        val read = completeRead()
        assertTrue(pairs().save(read))
        assertFalse(pairs().save(read))
        assertEquals(1, db.cardActivityImportDao().all().size)
        assertEquals(1, db.cardVehicleImportDao().all().size)
        db.close()
        db = EncryptedDatabaseFactory.create(context, correctPassphrase)
        val saved = pairs().all().single()
        assertEquals("synthetic-card", saved.activity.cardNumber)
        assertEquals(saved.activity.cardNumber, saved.vehicles.cardNumber)
        assertEquals(1, saved.activity.dayCount)
        assertEquals("TEST 1", saved.vehicles.uses.single().registrationNumber)
        assertEquals(testClock.millis(), saved.savedAtEpochMillis)
        assertEquals(read.activity!!.dailyRecords.toList(), saved.activity.file.dailyRecords.toList())
        assertEquals(read.vehicles!!.records.toList(), saved.vehicles.file.records.toList())
    }

    @Test fun invalidSecondFileCannotPartiallySaveFirstFile() = runBlocking {
        val read = completeRead().copy(vehicles = CardVehiclesFile(0, 1,
            CardGeneration.GEN2V2, byteArrayOf(1)))
        try { pairs().save(read); fail("Invalid vehicle source must fail") }
        catch (_: ParseError) { }
        assertTrue(db.cardActivityImportDao().all().isEmpty())
        assertTrue(db.cardVehicleImportDao().all().isEmpty())
        assertTrue(pairs().all().isEmpty())
        try { pairs().save(completeRead().copy(vehicleReadFailed = true)); fail("Failed read must fail") }
        catch (_: IllegalArgumentException) { }
        try { pairs().save(completeRead().copy(vehicles = null)); fail("Missing file must fail") }
        catch (_: IllegalArgumentException) { }
        assertTrue(db.cardActivityImportDao().all().isEmpty())
    }

    @Test fun databaseFailureRollsBackBothFilesAndKeepsEarlierSnapshots() = runBlocking {
        CardActivityImportRepository(db.cardActivityImportDao(), testClock).save(completeRead("older-card"))
        db.openHelper.writableDatabase.execSQL("""CREATE TRIGGER reject_pair BEFORE INSERT ON card_source_pairs BEGIN SELECT RAISE(ABORT, 'synthetic failure'); END""")
        try { pairs().save(completeRead()); fail("Injected database failure must fail") }
        catch (_: SQLiteException) { }
        assertEquals(listOf("older-card"), db.cardActivityImportDao().all().map { it.cardNumber })
        assertTrue(db.cardVehicleImportDao().all().isEmpty())
        assertTrue(pairs().all().isEmpty())
        db.openHelper.writableDatabase.execSQL("DROP TRIGGER reject_pair")
        assertTrue(pairs().save(completeRead()))
        assertEquals(2, db.cardActivityImportDao().all().size)
        assertEquals(1, pairs().all().size)
    }

    @Test fun differentReadsKeepTheirExactFilesEvenWhenOneFileIsUnchanged() = runBlocking {
        val read = completeRead()
        assertTrue(pairs().save(read))
        val newerRecords = read.vehicles!!.records.copyOf().also { it[5] = 3 }
        assertTrue(pairs().save(read.copy(vehicles = CardVehiclesFile(0, 1, CardGeneration.GEN2V2, newerRecords))))
        assertEquals(1, db.cardActivityImportDao().all().size)
        assertEquals(2, db.cardVehicleImportDao().all().size)
        assertEquals(setOf(2, 3), pairs().all().map { it.vehicles.uses.single().odometerEndKm }.toSet())
    }

    @Test fun encryptedVersionThreeUpgradePreservesStandaloneFilesWithoutInventingPairs() = runBlocking {
        val read = completeRead()
        CardActivityImportRepository(db.cardActivityImportDao(), testClock).save(read)
        CardVehicleImportRepository(db.cardVehicleImportDao(), testClock).save(read)
        // Retain the v3 tables; remove the additive v4/v5 tables and Room identity.
        db.openHelper.writableDatabase.apply {
            execSQL("DROP TABLE driver_import_sources")
            execSQL("DROP TABLE driver_card_identities")
            execSQL("DROP TABLE local_driver_profile")
            execSQL("DROP TABLE card_source_pairs")
            execSQL("DELETE FROM room_master_table")
            version = 3
        }
        db.close()
        db = EncryptedDatabaseFactory.create(context, correctPassphrase)
        assertEquals(1, db.cardActivityImportDao().all().size)
        assertEquals(1, db.cardVehicleImportDao().all().size)
        assertTrue(pairs().all().isEmpty())
        assertEquals(AppDatabase.SCHEMA_VERSION, db.openHelper.writableDatabase.version)
        // A fresh explicit same-read save may reuse identical standalone snapshots.
        assertTrue(pairs().save(read))
        assertEquals(1, db.cardActivityImportDao().all().size)
        assertEquals(1, db.cardVehicleImportDao().all().size)
        assertEquals(1, pairs().all().size)
    }

}
