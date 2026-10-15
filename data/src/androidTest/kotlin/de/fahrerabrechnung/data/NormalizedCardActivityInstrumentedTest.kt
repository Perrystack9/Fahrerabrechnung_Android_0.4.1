package de.fahrerabrechnung.data

import android.content.Context
import android.database.sqlite.SQLiteException
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import de.fahrerabrechnung.data.card.*
import de.fahrerabrechnung.data.db.*
import de.fahrerabrechnung.data.security.EncryptedDatabaseFactory
import java.nio.ByteBuffer
import java.security.SecureRandom
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/** Synthetic source files only; all persistence and migration checks use real SQLCipher. */
@RunWith(AndroidJUnit4::class)
class NormalizedCardActivityInstrumentedTest {
    private lateinit var context: Context
    private lateinit var db: AppDatabase
    private lateinit var passphrase: ByteArray
    private val clock = Clock.fixed(Instant.parse("2026-10-01T12:00:00Z"), ZoneOffset.UTC)
    private val ownHolder = DriverIdentity("ALPHA", "TEST PERSON", LocalDate.of(2000, 2, 29), "de")

    @Before fun prepare() {
        context = ApplicationProvider.getApplicationContext()
        context.deleteDatabase(AppDatabase.DATABASE_NAME)
        passphrase = ByteArray(32).also(SecureRandom()::nextBytes)
        db = EncryptedDatabaseFactory.create(context, passphrase)
    }

    @After fun close() {
        if (::db.isInitialized && db.isOpen) db.close()
        context.deleteDatabase(AppDatabase.DATABASE_NAME)
    }

    private fun activities() = CardActivityImportRepository(db.cardActivityImportDao(), clock)
    private fun vehicles() = CardVehicleImportRepository(db.cardVehicleImportDao(), clock)
    private fun pairs() = CardSourcePairRepository(db, activities(), vehicles(), clock)
    private fun imports() = DriverCardImportRepository(db, activities(), vehicles(), pairs(), clock)
    private fun card(number: String = "synthetic-own", issued: Long = 0) = DriverCard(CardGeneration.GEN2V2,
        CardIdentity(number, 13, "Fixture", Instant.ofEpochSecond(issued), Instant.EPOCH,
            Instant.parse("2030-01-01T00:00:00Z")), ownHolder)

    private data class Day(val date: String = "2026-03-29", val distance: Int = 37,
        val words: List<Int> = listOf(0, 0x9800 or 90, 0xE800 or 90, 0x3000 or 180, 0x4000 or 1320))

    private fun read(card: DriverCard = card(), days: List<Day> = listOf(Day()), offset: Int = 0): CardReadResult {
        var previousLength = 0
        val records = days.map { day ->
            val length = 12 + day.words.size * 2
            ByteBuffer.allocate(length).putShort(previousLength.toShort()).putShort(length.toShort())
                .putInt(Instant.parse("${day.date}T00:00:00Z").epochSecond.toInt())
                .putShort(1).putShort(day.distance.toShort()).apply {
                    day.words.forEach { putShort(it.toShort()) }
                }.array().also { previousLength = length }
        }
        val ring = ByteArray(records.sumOf { it.size } + 32)
        val oldest = offset % ring.size
        var cursor = oldest
        var newest = oldest
        records.forEach { record ->
            newest = cursor
            record.forEach { byte -> ring[cursor] = byte; cursor = (cursor + 1) % ring.size }
        }
        val epoch = Instant.parse("2026-03-29T00:00:00Z").epochSecond.toInt()
        val vehicle = ByteBuffer.allocate(48).put(byteArrayOf(0, 0, 1, 0, 0, 2))
            .putInt(epoch + 3600).putInt(epoch + 7200).put(13.toByte()).put(1.toByte())
            .put("TEST OWN".padEnd(13).toByteArray()).putShort(1).put("WDB12345678901234".toByteArray()).array()
        return CardReadResult(CardRecognition.DRIVER, card.generation, card,
            CardActivityFile(oldest, newest, ring), CardVehiclesFile(0, 1, card.generation, vehicle))
    }

    private fun count(table: String): Int = db.openHelper.readableDatabase.query("SELECT COUNT(*) FROM $table").use {
        check(it.moveToFirst()); it.getInt(0)
    }

    private fun counts() = listOf("driver_activity_days", "driver_activity_changes", "driver_activity_day_sources").map(::count)

    private suspend inline fun <reified T : Throwable> rejected(noinline action: suspend () -> Unit) {
        try { action() } catch (failure: Throwable) {
            if (failure is T) return
            throw failure
        }
        throw AssertionError("Expected ${T::class.java.simpleName}")
    }

    @Test fun exactSourceDaysFlagsAndOrderSurviveEncryptedReopenAndLocalProjection() = runBlocking {
        val input = read(days = listOf(Day("2026-03-28"), Day(), Day("2026-03-31")), offset = 91)
        val expected = CardActivityParser.parse(input.activity!!)
        imports().bind(card(), true)
        val saved = imports().save(input, "roundtrip", CardImportSelection.PAIR)
        assertEquals(listOf(3, 15, 3), counts())
        assertEquals(expected, saved.activity!!.days)
        db.close()
        db = EncryptedDatabaseFactory.create(context, passphrase)
        val reopened = imports().latest()!!
        assertEquals(saved.profileId, reopened.profileId)
        assertEquals(saved.driverCardId, reopened.driverCardId)
        val reopenedActivity = checkNotNull(reopened.activity)
        assertEquals(expected, reopenedActivity.days)
        for (zone in listOf("Europe/Berlin", "UTC", "America/New_York")) {
            assertEquals(CardDayTimeline.build(expected, ZoneId.of(zone)),
                CardDayTimeline.build(reopenedActivity.days, ZoneId.of(zone)))
        }
        assertEquals(listOf(0, 90, 90, 180, 1320), reopenedActivity.days.first().changes.map { it.minuteOfDay })
        assertFalse(reopenedActivity.days.first().changes[2].cardInserted)
        assertEquals(CardSlot.CO_DRIVER, reopenedActivity.days.first().changes[2].slot)
        assertTrue(reopenedActivity.days.first().changes[2].crewDriving)
        assertFalse(reopenedActivity.days.any { it.recordDateUtc == Instant.parse("2026-03-30T00:00:00Z") })
    }

    @Test fun repeatedReadAndRetryReuseNormalizedRowsButKeepSeparateSessions() = runBlocking {
        imports().bind(card(), true)
        val first = imports().save(read(), "first", CardImportSelection.PAIR)
        val second = imports().save(read(), "second", CardImportSelection.PAIR)
        val retry = imports().save(read(), "second", CardImportSelection.PAIR)
        assertEquals(2, first.newSourceCount)
        assertEquals(0, second.newSourceCount)
        assertEquals(second.id, retry.id)
        assertEquals(listOf(1, 5, 1), counts())
        assertEquals(2, imports().all().size)
        assertEquals(first.activity!!.days, second.activity!!.days)
    }

    @Test fun shiftedRingKeepsOneDayContentAndBothExactSourcePositions() = runBlocking {
        val cardId = imports().bind(card(), true)
        val firstInput = read(offset = 0)
        val secondInput = read(offset = 51)
        val first = imports().save(firstInput, "first-ring", CardImportSelection.ACTIVITY)
        val second = imports().save(secondInput, "second-ring", CardImportSelection.ACTIVITY)
        assertEquals(1, second.newSourceCount)
        assertEquals(listOf(1, 5, 2), counts())
        assertEquals(0, first.activity!!.days.single().ringOffset)
        val secondActivity = checkNotNull(second.activity)
        assertEquals(51, secondActivity.days.single().ringOffset)
        val snapshots = db.cardActivityImportDao().all()
        val ids = snapshots.map { db.driverActivityDao().sourceDays(cardId, it.id).single().day.id }.distinct()
        assertEquals(1, ids.size)
        assertEquals(CardActivityParser.parse(secondInput.activity!!), secondActivity.days)
    }

    @Test fun changedDayKeepsBothVersionsAndNeverRewritesTheOlderImport() = runBlocking {
        imports().bind(card(), true)
        val first = imports().save(read(), "old-day", CardImportSelection.PAIR)
        val changed = imports().save(read(days = listOf(Day(distance = 42))), "changed-day", CardImportSelection.PAIR)
        assertEquals(1, changed.newSourceCount)
        assertEquals(listOf(2, 10, 2), counts())
        assertEquals(37, imports().all().first { it.id == first.id }.activity!!.days.single().distanceKm)
        assertEquals(42, imports().all().first { it.id == changed.id }.activity!!.days.single().distanceKm)
        assertEquals(1, db.cardVehicleImportDao().all().size)
    }

    @Test fun replacementWithSameNumberKeepsPhysicalCardsAndTheirDayProvenanceSeparate() = runBlocking {
        imports().bind(card(), true)
        val first = imports().save(read(), "old-card", CardImportSelection.PAIR)
        val replacement = imports().save(read(card(issued = 120)), "replacement", CardImportSelection.PAIR)
        assertEquals(first.profileId, replacement.profileId)
        assertNotEquals(first.driverCardId, replacement.driverCardId)
        assertEquals(0, replacement.newSourceCount)
        assertEquals(listOf(2, 10, 2), counts())
        val source = db.cardActivityImportDao().all().single()
        val oldDay = db.driverActivityDao().sourceDays(first.driverCardId, source.id).single()
        val newDay = db.driverActivityDao().sourceDays(replacement.driverCardId, source.id).single()
        assertNotEquals(oldDay.day.id, newDay.day.id)
        rejected<SQLiteException> {
            db.driverActivityDao().insertSource(DriverActivityDaySourceEntity(replacement.driverCardId,
                source.id, 9, oldDay.day.id, 0))
        }
        assertEquals(first.activity!!.days, imports().all().first { it.id == first.id }.activity!!.days)
    }

    @Test fun unconfirmedAndForeignCardsCannotCreateNormalizedRows() = runBlocking {
        rejected<CardBindingRequired> { imports().save(read(), "unconfirmed", CardImportSelection.PAIR) }
        assertEquals(listOf(0, 0, 0), counts())
        imports().bind(card(), true)
        val foreign = card("synthetic-foreign").copy(holder =
            DriverIdentity("BETA", "OTHER PERSON", LocalDate.of(1980, 1, 1), "en"))
        rejected<ForeignCardRejected> { imports().save(read(foreign), "foreign", CardImportSelection.PAIR) }
        assertEquals(listOf(0, 0, 0), counts())
        assertTrue(imports().all().isEmpty())
    }

    @Test fun vehicleOnlyAndLegacyPreviewDoNotPopulateDaysOrGuessProfileAssignments() = runBlocking {
        val archive = read()
        pairs().save(archive)
        assertEquals(listOf(0, 0, 0), counts())
        assertEquals(CardActivityParser.parse(archive.activity!!), activities().latest()!!.days)
        imports().bind(card(), true)
        val vehicleOnly = imports().save(archive.copy(activity = null), "vehicles", CardImportSelection.VEHICLES)
        assertNull(vehicleOnly.activity)
        assertEquals(listOf(0, 0, 0), counts())
        val fresh = imports().save(archive, "fresh-own-read", CardImportSelection.PAIR)
        assertEquals(0, fresh.newSourceCount)
        assertEquals(listOf(1, 5, 1), counts())
        assertEquals(2, imports().all().size)
        assertNull(imports().all().first { it.id == vehicleOnly.id }.activity)
    }

    @Test fun normalizationFailuresRollBackNewCardSourcesPairSessionAndDaysTogether() = runBlocking {
        imports().bind(card(), true)
        val old = imports().save(read(), "kept", CardImportSelection.PAIR)
        for (table in listOf("driver_activity_changes", "driver_activity_day_sources")) {
            db.openHelper.writableDatabase.execSQL("CREATE TRIGGER reject_normalization BEFORE INSERT ON $table BEGIN SELECT RAISE(ABORT, 'synthetic failure'); END")
            rejected<SQLiteException> {
                imports().save(read(card("synthetic-new-$table")), "failed-$table", CardImportSelection.PAIR)
            }
            db.openHelper.writableDatabase.execSQL("DROP TRIGGER reject_normalization")
            assertEquals(listOf(1, 5, 1), counts())
            assertEquals(listOf(old.id), imports().all().map { it.id })
            assertEquals(1, db.driverProfileDao().cards(old.profileId).size)
            assertEquals(1, db.cardActivityImportDao().all().size)
            assertEquals(1, db.cardVehicleImportDao().all().size)
            assertEquals(1, pairs().all().size)
            assertEquals(old.activity!!.days, imports().latest()!!.activity!!.days)
        }
    }

    @Test fun partialNormalizedDataIsRejectedInsteadOfSilentlyInventingMissingChanges() = runBlocking {
        imports().bind(card(), true)
        imports().save(read(), "saved", CardImportSelection.PAIR)
        db.openHelper.writableDatabase.execSQL("DELETE FROM driver_activity_changes WHERE changeIndex = 2")
        rejected<IllegalStateException> { imports().latest() }
        assertEquals(1, count("driver_import_sources"))
    }

    @Test fun migrationFiveToSixPreservesBindingAndSourcesWithoutBackfillAndFreshReadNormalizes() = runBlocking {
        imports().bind(card(), true)
        val before = imports().save(read(), "before-migration", CardImportSelection.PAIR)
        val profile = imports().profile()
        val identity = db.driverProfileDao().cards(before.profileId)
        pairs().save(read(card("synthetic-legacy")))
        val snapshots = db.cardActivityImportDao().all()
        db.openHelper.writableDatabase.apply {
            execSQL("DROP TABLE violation_context_revisions") // Empty M1 relation is absent in the historical fixture.
            execSQL("DROP TABLE card_import_file_results")
            execSQL("DROP TABLE card_import_runs")
            execSQL("DROP TABLE driver_vehicle_use_sources")
            execSQL("DROP TABLE driver_vehicle_uses")
            execSQL("DROP TABLE driver_activity_day_sources")
            execSQL("DROP TABLE driver_activity_changes")
            execSQL("DROP TABLE driver_activity_days")
            execSQL("DROP TABLE IF EXISTS card_event_fault_signature_receipts")
            execSQL("DELETE FROM room_master_table")
            version = 5
        }
        db.close()
        db = EncryptedDatabaseFactory.create(context, passphrase)
        assertEquals(AppDatabase.SCHEMA_VERSION, db.openHelper.writableDatabase.version)
        assertEquals(listOf(0, 0, 0), counts())
        assertEquals(profile, imports().profile())
        assertEquals(identity, db.driverProfileDao().cards(before.profileId))
        assertEquals(before.activity!!.days, imports().latest()!!.activity!!.days)
        assertEquals(listOf(before.id), imports().all().map { it.id })
        assertEquals(listOf(0, 0, 0), counts())
        snapshots.forEach { old ->
            val current = db.cardActivityImportDao().getById(old.id)!!
            assertEquals(old.importedAtEpochMillis, current.importedAtEpochMillis)
            assertArrayEquals(old.dailyRecords, current.dailyRecords)
        }
        val fresh = imports().save(read(), "after-migration", CardImportSelection.PAIR)
        assertEquals(0, fresh.newSourceCount)
        assertEquals(before.driverCardId, fresh.driverCardId)
        assertEquals(listOf(1, 5, 1), counts())
        assertEquals(before.savedAtEpochMillis, imports().all().first { it.id == before.id }.savedAtEpochMillis)
        assertEquals(2, db.cardActivityImportDao().all().size)
    }
}
