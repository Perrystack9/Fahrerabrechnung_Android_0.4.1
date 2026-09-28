package de.fahrerabrechnung.data

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
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
}
