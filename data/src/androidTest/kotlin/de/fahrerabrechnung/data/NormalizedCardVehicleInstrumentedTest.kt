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
import java.time.ZoneOffset
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/** Synthetic cards and files; persistence, foreign keys and migration run on real SQLCipher. */
@RunWith(AndroidJUnit4::class)
class NormalizedCardVehicleInstrumentedTest {
    private lateinit var context: Context
    private lateinit var db: AppDatabase
    private lateinit var passphrase: ByteArray
    private val clock = Clock.fixed(Instant.parse("2026-10-02T08:00:00Z"), ZoneOffset.UTC)
    private val epoch = Instant.parse("2026-03-29T00:00:00Z").epochSecond
    private val holder = DriverIdentity("ALPHA", "TEST PERSON", LocalDate.of(2000, 2, 29), "de")

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
    private fun card(number: String = "synthetic-own", issued: Long = 0,
        generation: CardGeneration = CardGeneration.GEN2V2) = DriverCard(generation,
        CardIdentity(number, 13, "Fixture", Instant.ofEpochSecond(issued), Instant.EPOCH,
            Instant.parse("2030-01-01T00:00:00Z")), holder)

    private data class Use(val first: Long, val last: Long?, val nation: Int = 13,
        val plate: String = "TEST OWN", val vin: String = "WDB12345678901234",
        val begin: Int = 100, val end: Int = 137, val counter: Int = 1)

    private fun uses() = listOf(Use(epoch + 1800, epoch + 7200),
        Use(epoch + 10800, null, plate = "TEST Ä", begin = 137, counter = 65535))

    private fun read(card: DriverCard = card(), uses: List<Use> = uses(), shift: Int = 0,
        distance: Int = 37): CardReadResult {
        require(uses.size in 1..4 && shift in 0..3)
        val width = if (card.generation == CardGeneration.GEN1) 31 else 48
        val ring = ByteArray(4 * width)
        // Unused slots may contain nonzero identification filler, without becoming invented uses.
        for (index in 0..3) for (offset in 14 until width) ring[index * width + offset] = 0x7F
        uses.forEachIndexed { index, use ->
            val buffer = ByteBuffer.allocate(width)
            fun u24(value: Int) { buffer.put((value shr 16).toByte()).put((value shr 8).toByte()).put(value.toByte()) }
            u24(use.begin); u24(use.end)
            buffer.putInt(use.first.toInt()).putInt((use.last ?: 0).toInt())
                .put(use.nation.toByte()).put(1.toByte())
                .put(use.plate.padEnd(13).toByteArray(Charsets.ISO_8859_1)).putShort(use.counter.toShort())
            if (width == 48) buffer.put(use.vin.toByteArray(Charsets.US_ASCII))
            buffer.array().copyInto(ring, ((index + shift) % 4) * width)
        }
        val day = ByteBuffer.allocate(16).putShort(0).putShort(16).putInt(epoch.toInt())
            .putShort(1).putShort(distance.toShort()).putShort(0).putShort((0x1800 or 90).toShort()).array()
        return CardReadResult(CardRecognition.DRIVER, card.generation, card,
            CardActivityFile(0, 0, day), CardVehiclesFile((uses.lastIndex + shift) % 4, 4, card.generation, ring))
    }

    private fun count(table: String): Int = db.openHelper.readableDatabase.query("SELECT COUNT(*) FROM $table").use {
        check(it.moveToFirst()); it.getInt(0)
    }
    private fun counts() = listOf("driver_vehicle_uses", "driver_vehicle_use_sources").map(::count)
    private suspend inline fun <reified T : Throwable> rejected(noinline action: suspend () -> Unit) {
        try { action() } catch (failure: Throwable) {
            if (failure is T) return
            throw failure
        }
        throw AssertionError("Expected ${T::class.java.simpleName}")
    }

    @Test fun exactUsesOrderOpenEndsAndSourceValuesSurviveEncryptedReopen() = runBlocking {
        val input = read(shift = 3)
        val expected = CardVehiclesParser.parse(input.vehicles!!)
        imports().bind(card(), true)
        val saved = imports().save(input, "roundtrip", CardImportSelection.PAIR)
        assertEquals(listOf(2, 2), counts())
        assertEquals(expected, saved.vehicles!!.uses)
        db.close()
        db = EncryptedDatabaseFactory.create(context, passphrase)
        val reopened = imports().latest()!!
        assertEquals(saved.profileId, reopened.profileId)
        assertEquals(saved.driverCardId, reopened.driverCardId)
        assertEquals(expected, reopened.vehicles!!.uses)
        assertEquals(expected, imports().vehicleSources().single().uses)
        assertEquals(expected, reopened.pair!!.vehicles.uses)
        assertEquals(listOf(3, 0), expected.map { it.ringIndex })
        assertEquals("TEST Ä", expected.last().registrationNumber)
        assertNull(expected.last().lastUseUtc)
        assertNull(expected.last().odometerEndKm)
        assertEquals(65535, expected.last().vuDataBlockCounter)
        assertEquals(CardVehicleHistory.build(listOf(saved.vehicles!!)),
            CardVehicleHistory.build(imports().vehicleSources()))
    }

    @Test fun genOneUsesKeepAbsentVinAndTheirExactSourceData() = runBlocking {
        val own = card(generation = CardGeneration.GEN1)
        val input = read(own)
        imports().bind(own, true)
        val saved = imports().save(input, "gen-one", CardImportSelection.VEHICLES)
        assertEquals(CardVehiclesParser.parse(input.vehicles!!), saved.vehicles!!.uses)
        assertTrue(saved.vehicles!!.uses.all { it.vin == null })
        assertEquals(listOf(2, 2), counts())
        assertEquals(0, count("driver_activity_days"))
    }

    @Test fun repeatedReadsAndSameRequestRetryDeduplicateUsesAndSources() = runBlocking {
        imports().bind(card(), true)
        val first = imports().save(read(), "first", CardImportSelection.PAIR)
        val second = imports().save(read(), "second", CardImportSelection.PAIR)
        val retry = imports().save(read(), "second", CardImportSelection.PAIR)
        assertEquals(2, first.newSourceCount)
        assertEquals(0, second.newSourceCount)
        assertEquals(second.id, retry.id)
        assertEquals(listOf(2, 2), counts())
        assertEquals(2, imports().all().size)
        assertEquals(first.vehicles!!.uses, retry.vehicles!!.uses)
        assertEquals(1, imports().vehicleSources().size)
    }

    @Test fun ringShiftKeepsContentsButPreservesBothSourceOrdersAndPositions() = runBlocking {
        val cardId = imports().bind(card(), true)
        val first = imports().save(read(), "old-ring", CardImportSelection.VEHICLES)
        val secondInput = read(shift = 2)
        val second = imports().save(secondInput, "new-ring", CardImportSelection.VEHICLES)
        assertEquals(1, second.newSourceCount)
        assertEquals(listOf(2, 4), counts())
        assertEquals(listOf(0, 1), first.vehicles!!.uses.map { it.ringIndex })
        assertEquals(listOf(2, 3), second.vehicles!!.uses.map { it.ringIndex })
        assertEquals(CardVehiclesParser.parse(secondInput.vehicles!!), second.vehicles!!.uses)
        val contentIds = db.cardVehicleImportDao().all().map { source ->
            db.driverVehicleUseDao().sourceUses(cardId, source.id).map { it.use.id }
        }
        assertEquals(contentIds.first(), contentIds.last())
    }

    @Test fun newlyClosedUseRetainsItsPreviousOpenVersionAndImport() = runBlocking {
        imports().bind(card(), true)
        val first = imports().save(read(), "open-version", CardImportSelection.PAIR)
        val changed = uses().mapIndexed { index, use ->
            if (index == 1) use.copy(last = epoch + 18000, end = 190) else use
        }
        val second = imports().save(read(uses = changed), "closed-version", CardImportSelection.PAIR)
        assertEquals(1, second.newSourceCount)
        assertEquals(listOf(3, 4), counts())
        val oldUse = imports().all().first { it.id == first.id }.vehicles!!.uses.last()
        assertNull(oldUse.lastUseUtc)
        assertNull(oldUse.odometerEndKm)
        assertEquals(Instant.ofEpochSecond(epoch + 18000), second.vehicles!!.uses.last().lastUseUtc)
        assertEquals(190, second.vehicles!!.uses.last().odometerEndKm)
        assertEquals(1, count("driver_activity_days"))
        assertEquals(1, db.cardActivityImportDao().all().size)
    }

    @Test fun replacementWithSameNumberAndBytesKeepsPhysicalCardProvenanceSeparate() = runBlocking {
        imports().bind(card(), true)
        val first = imports().save(read(), "old-card", CardImportSelection.PAIR)
        val replacement = imports().save(read(card(issued = 120)), "replacement", CardImportSelection.PAIR)
        assertEquals(first.profileId, replacement.profileId)
        assertNotEquals(first.driverCardId, replacement.driverCardId)
        assertEquals(0, replacement.newSourceCount)
        assertEquals(listOf(4, 4), counts())
        val source = db.cardVehicleImportDao().all().single()
        val oldUse = db.driverVehicleUseDao().sourceUses(first.driverCardId, source.id).first()
        val newUse = db.driverVehicleUseDao().sourceUses(replacement.driverCardId, source.id).first()
        assertNotEquals(oldUse.use.id, newUse.use.id)
        rejected<SQLiteException> {
            db.driverVehicleUseDao().insertSource(DriverVehicleUseSourceEntity(replacement.driverCardId,
                source.id, 9, oldUse.use.id, 0))
        }
        assertEquals(2, imports().vehicleSources().size)
        assertEquals(first.vehicles!!.uses, imports().all().first { it.id == first.id }.vehicles!!.uses)
    }

    @Test fun unconfirmedAndForeignCardsCannotCreateUsesSourcesOrSessions() = runBlocking {
        rejected<CardBindingRequired> { imports().save(read(), "unconfirmed", CardImportSelection.PAIR) }
        assertEquals(listOf(0, 0), counts())
        imports().bind(card(), true)
        val foreign = card("synthetic-foreign").copy(holder =
            DriverIdentity("BETA", "OTHER PERSON", LocalDate.of(1980, 1, 1), "en"))
        rejected<ForeignCardRejected> { imports().save(read(foreign), "foreign", CardImportSelection.PAIR) }
        assertEquals(listOf(0, 0), counts())
        assertTrue(imports().all().isEmpty())
        assertEquals(0, db.cardVehicleImportDao().all().size)
    }

    @Test fun archiveAndActivityOnlyReadsDoNotNormalizeOrGuessVehicleAssignments() = runBlocking {
        val input = read()
        pairs().save(input)
        assertEquals(listOf(0, 0), counts())
        assertEquals(CardVehiclesParser.parse(input.vehicles!!), vehicles().latest()!!.uses)
        assertNull(imports().profile())
        imports().bind(card(), true)
        val activityOnly = imports().save(input.copy(vehicles = null), "activity-only", CardImportSelection.ACTIVITY)
        assertNull(activityOnly.vehicles)
        assertTrue(imports().vehicleSources().isEmpty())
        assertEquals(listOf(0, 0), counts())
        val fresh = imports().save(input, "fresh-own-read", CardImportSelection.PAIR)
        assertEquals(0, fresh.newSourceCount)
        assertEquals(listOf(2, 2), counts())
        assertNull(imports().all().first { it.id == activityOnly.id }.vehicles)
    }

    @Test fun eitherNormalizationFailureRollsBackCardFilesPairSessionAndDaysTogether() = runBlocking {
        imports().bind(card(), true)
        val old = imports().save(read(), "kept", CardImportSelection.PAIR)
        for (table in listOf("driver_vehicle_uses", "driver_vehicle_use_sources")) {
            db.openHelper.writableDatabase.execSQL("CREATE TRIGGER reject_vehicle_normalization BEFORE INSERT ON $table BEGIN SELECT RAISE(ABORT, 'synthetic failure'); END")
            rejected<SQLiteException> {
                imports().save(read(card("synthetic-new-$table"), distance = 42), "failed-$table", CardImportSelection.PAIR)
            }
            db.openHelper.writableDatabase.execSQL("DROP TRIGGER reject_vehicle_normalization")
            assertEquals(listOf(2, 2), counts())
            assertEquals(listOf(old.id), imports().all().map { it.id })
            assertEquals(1, db.driverProfileDao().cards(old.profileId).size)
            assertEquals(1, db.cardActivityImportDao().all().size)
            assertEquals(1, db.cardVehicleImportDao().all().size)
            assertEquals(1, pairs().all().size)
            assertEquals(1, count("driver_activity_days"))
            assertEquals(1, count("driver_activity_day_sources"))
            assertEquals(old.vehicles!!.uses, imports().latest()!!.vehicles!!.uses)
        }
    }

    @Test fun incompleteNormalizedSourceIsRejectedInsteadOfFallingBackToRawBytes() = runBlocking {
        imports().bind(card(), true)
        imports().save(read(), "saved", CardImportSelection.PAIR)
        db.openHelper.writableDatabase.execSQL("DELETE FROM driver_vehicle_use_sources WHERE recordIndex = 1")
        rejected<IllegalStateException> { imports().latest() }
        rejected<IllegalStateException> { imports().vehicleSources() }
        assertEquals(2, vehicles().latest()!!.uses.size)
        assertEquals(1, count("driver_import_sources"))
    }

    @Test fun changedNormalizedContentOrInvalidRingPositionIsRejected() = runBlocking {
        imports().bind(card(), true)
        imports().save(read(), "saved", CardImportSelection.PAIR)
        db.openHelper.writableDatabase.execSQL("UPDATE driver_vehicle_uses SET registrationNation = 14")
        rejected<IllegalStateException> { imports().latest() }
        db.openHelper.writableDatabase.execSQL("UPDATE driver_vehicle_uses SET registrationNation = 13")
        db.openHelper.writableDatabase.execSQL("UPDATE driver_vehicle_use_sources SET ringIndex = 9 WHERE recordIndex = 1")
        rejected<IllegalStateException> { imports().latest() }
    }

    @Test fun migrationSixToSevenKeepsExistingDaysBindingsAndArchiveWithoutBackfill() = runBlocking {
        imports().bind(card(), true)
        val before = imports().save(read(), "before-migration", CardImportSelection.PAIR)
        pairs().save(read(card("synthetic-legacy")))
        val profile = imports().profile()
        val identities = db.driverProfileDao().cards(before.profileId)
        val sources = db.cardVehicleImportDao().all()
        val daySources = db.cardActivityImportDao().all().map { source ->
            db.driverActivityDao().sourceDays(before.driverCardId, source.id)
        }
        db.openHelper.writableDatabase.apply {
            execSQL("DROP TABLE driver_vehicle_use_sources")
            execSQL("DROP TABLE driver_vehicle_uses")
            execSQL("DELETE FROM room_master_table")
            version = 6
        }
        db.close()
        db = EncryptedDatabaseFactory.create(context, passphrase)
        assertEquals(AppDatabase.SCHEMA_VERSION, db.openHelper.writableDatabase.version)
        assertEquals(listOf(0, 0), counts())
        assertEquals(profile, imports().profile())
        assertEquals(identities, db.driverProfileDao().cards(before.profileId))
        assertEquals(before.activity!!.days, imports().latest()!!.activity!!.days)
        assertEquals(before.vehicles!!.uses, imports().latest()!!.vehicles!!.uses)
        assertEquals(listOf(before.id), imports().all().map { it.id })
        assertEquals(1, imports().vehicleSources().size)
        // Read-only access and retry of an old request never normalize older imports.
        imports().save(read(), before.id, CardImportSelection.PAIR)
        assertEquals(listOf(0, 0), counts())
        sources.forEach { old ->
            val current = db.cardVehicleImportDao().getById(old.id)!!
            assertEquals(old.importedAtEpochMillis, current.importedAtEpochMillis)
            assertArrayEquals(old.records, current.records)
        }
        assertEquals(daySources, db.cardActivityImportDao().all().map { source ->
            db.driverActivityDao().sourceDays(before.driverCardId, source.id)
        })
        val fresh = imports().save(read(), "after-migration", CardImportSelection.PAIR)
        assertEquals(0, fresh.newSourceCount)
        assertEquals(before.driverCardId, fresh.driverCardId)
        assertEquals(listOf(2, 2), counts())
        assertEquals(before.savedAtEpochMillis, imports().all().first { it.id == before.id }.savedAtEpochMillis)
        assertEquals(2, db.cardVehicleImportDao().all().size)
        assertEquals(1, count("driver_activity_days"))
    }
}
