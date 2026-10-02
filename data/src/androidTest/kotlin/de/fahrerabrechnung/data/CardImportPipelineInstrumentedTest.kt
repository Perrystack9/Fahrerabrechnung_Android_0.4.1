package de.fahrerabrechnung.data

import android.content.Context
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
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/** Synthetic inputs, real SQLCipher, actual transaction boundaries and upgrade chain. */
@RunWith(AndroidJUnit4::class)
class CardImportPipelineInstrumentedTest {
    private lateinit var context: Context
    private lateinit var db: AppDatabase
    private lateinit var passphrase: ByteArray
    private lateinit var activities: CardActivityImportRepository
    private lateinit var vehicles: CardVehicleImportRepository
    private lateinit var pairs: CardSourcePairRepository
    private lateinit var imports: DriverCardImportRepository
    private lateinit var pipeline: DriverCardImportPipeline
    private val clock = Clock.fixed(Instant.parse("2026-10-02T09:00:00Z"), ZoneOffset.UTC)

    @Before fun prepare() {
        context = ApplicationProvider.getApplicationContext()
        context.deleteDatabase(AppDatabase.DATABASE_NAME)
        passphrase = ByteArray(32).also(SecureRandom()::nextBytes)
        open()
    }
    private fun open() {
        db = EncryptedDatabaseFactory.create(context, passphrase)
        activities = CardActivityImportRepository(db.cardActivityImportDao(), clock)
        vehicles = CardVehicleImportRepository(db.cardVehicleImportDao(), clock)
        pairs = CardSourcePairRepository(db, activities, vehicles, clock)
        imports = DriverCardImportRepository(db, activities, vehicles, pairs, clock)
        pipeline = DriverCardImportPipeline(db, imports, activities, vehicles, clock)
    }
    @After fun close() { if (::db.isInitialized && db.isOpen) db.close(); context.deleteDatabase(AppDatabase.DATABASE_NAME) }

    private fun card(number: String = "fixture-own", holder: DriverIdentity =
        DriverIdentity("ALPHA", "TEST PERSON", LocalDate.of(2000, 2, 29), "de")) =
        DriverCard(CardGeneration.GEN2V2, CardIdentity(number, 13, "Fixture", Instant.EPOCH,
            Instant.EPOCH, Instant.parse("2030-01-01T00:00:00Z")), holder)

    private fun read(card: DriverCard = card(), plate: String = "TEST OWN", openEnd: Boolean = false): CardReadResult {
        val epoch = Instant.parse("2026-09-28T00:00:00Z").epochSecond.toInt()
        val day = ByteBuffer.allocate(14).putShort(0).putShort(14).putInt(epoch)
            .putShort(1).putShort(0).putShort(0).array()
        val record = ByteBuffer.allocate(48).put(byteArrayOf(0, 0, 1, 0, 0, 2))
            .putInt(epoch + 3600).putInt(if (openEnd) 0 else epoch + 7200).put(13.toByte()).put(1.toByte())
            .put(plate.padEnd(13).toByteArray()).putShort(1).put("WDB12345678901234".toByteArray()).array()
        return CardReadResult(CardRecognition.DRIVER, card.generation, card, CardActivityFile(0, 0, day),
            CardVehiclesFile(0, 1, card.generation, record))
    }
    private fun CardImportRun.file(kind: CardImportFileKind) = files.single { it.kind == kind }
    private suspend inline fun <reified T : Throwable> rejected(noinline action: suspend () -> Unit) {
        try { action() } catch (e: Throwable) { if (e is T) return; throw e }
        throw AssertionError("Expected ${T::class.java.simpleName}")
    }
    private fun count(table: String): Int = db.openHelper.readableDatabase.query("SELECT COUNT(*) FROM $table").use {
        it.moveToFirst(); it.getInt(0)
    }

    @Test fun unconfirmedAndForeignCardsCannotCreateAnyPersistentRun() = runBlocking {
        rejected<CardBindingRequired> { pipeline.process(read(), "unconfirmed") }
        assertNull(imports.profile()); assertEquals(0, count("card_import_runs"))
        imports.bind(card(), true)
        val foreign = card("fixture-foreign", DriverIdentity("BETA", "OTHER PERSON", LocalDate.of(1980, 1, 1), "en"))
        rejected<ForeignCardRejected> { pipeline.process(read(foreign), "foreign") }
        val unclear = card("fixture-unclear", card().holder.copy(birthDate = null))
        rejected<CardBindingRequired> { pipeline.process(read(unclear), "unclear") }
        assertEquals(1, count("driver_cards")); assertEquals(0, count("card_import_runs"))
        assertEquals(0, count("import_sessions")); assertEquals(0, count("card_activity_imports"))
        assertEquals(0, count("card_vehicle_imports"))
    }

    @Test fun completeSupportedSourcesPersistExactRunMetadataAndEncryptedData() = runBlocking {
        imports.bind(card(), true)
        val run = pipeline.process(read(openEnd = true), "saved")
        assertEquals(CardImportRunStatus.SAVED, run.status); assertEquals(2, run.newSourceCount)
        assertEquals(1, run.file(CardImportFileKind.ACTIVITIES).decodedCount)
        assertEquals(1, run.file(CardImportFileKind.VEHICLES).decodedCount)
        assertNull(run.file(CardImportFileKind.VEHICLES).lastUtcEpochSeconds)
        assertEquals(setOf(CardImportFileStatus.STORED), run.files.map { it.status }.toSet())
        val saved = imports.get("saved")!!
        val savedVehicles = checkNotNull(saved.vehicles)
        assertEquals("TEST OWN", savedVehicles.uses.single().registrationNumber)
        assertNull(savedVehicles.uses.single().lastUseUtc)
        assertEquals(1, count("driver_activity_days")); assertEquals(1, count("driver_vehicle_uses"))
        db.close(); open()
        assertEquals(run, pipeline.history().single())
        assertArrayEquals(saved.activity!!.file.dailyRecords, imports.get("saved")!!.activity!!.file.dailyRecords)
        assertTrue(pipeline.earlierImports().isEmpty())
        assertFalse(context.getDatabasePath(AppDatabase.DATABASE_NAME).readBytes().take(16).toByteArray()
            .contentEquals("SQLite format 3\u0000".toByteArray()))
    }

    @Test fun retryIsIdempotentAndASeparateUnchangedReadAddsOnlyItsRun() = runBlocking {
        imports.bind(card(), true)
        val first = pipeline.process(read(), "first")
        assertEquals(first, pipeline.process(read(), "first"))
        val second = pipeline.process(read(), "second")
        assertEquals(0, second.newSourceCount)
        assertEquals(2, pipeline.history().size); assertEquals(2, count("import_sessions"))
        assertEquals(1, count("card_activity_imports")); assertEquals(1, count("card_vehicle_imports"))
        assertEquals(1, count("driver_activity_days")); assertEquals(1, count("driver_vehicle_uses"))
    }

    @Test fun anExistingRequestCannotBeReusedForDifferentBytesOrAnotherPhysicalCard() = runBlocking {
        imports.bind(card(), true); val first = pipeline.process(read(), "request")
        rejected<IllegalArgumentException> { pipeline.process(read(plate = "TEST CHANGED"), "request") }
        rejected<IllegalArgumentException> { pipeline.process(read(card("fixture-next")), "request") }
        assertEquals(listOf(first), pipeline.history()); assertEquals(1, count("driver_cards"))
        assertEquals(1, count("card_vehicle_imports"))
    }

    @Test fun unavailableVehiclesDoNotAttachAnOlderVehicleFileToTheNewRun() = runBlocking {
        imports.bind(card(), true); pipeline.process(read(), "old")
        val partial = pipeline.process(read().copy(vehicles = null, vehicleReadFailed = true), "new")
        assertEquals(CardImportRunStatus.PARTIAL, partial.status)
        assertEquals(CardImportFileStatus.READ_FAILED, partial.file(CardImportFileKind.VEHICLES).status)
        assertNull(imports.get("new")!!.vehicles); assertNull(imports.get("new")!!.pair)
        assertNotNull(imports.get("old")!!.vehicles)
        assertEquals(1, count("card_vehicle_imports")); assertEquals(1, count("driver_vehicle_uses"))
    }

    @Test fun invalidActivitiesLeaveValidVehiclesUsableWithoutInventingAnyDay() = runBlocking {
        imports.bind(card(), true)
        val run = pipeline.process(read().copy(activity = CardActivityFile(0, 0, byteArrayOf(1))), "vehicles-only")
        assertEquals(CardImportRunStatus.PARTIAL, run.status)
        assertEquals(CardImportFileStatus.INVALID, run.file(CardImportFileKind.ACTIVITIES).status)
        assertNull(imports.get("vehicles-only")!!.activity)
        assertEquals(0, count("driver_activity_days")); assertEquals(1, count("driver_vehicle_uses"))
    }

    @Test fun invalidAndEmptyVehicleFilesHaveHonestSeparateStatuses() = runBlocking {
        imports.bind(card(), true)
        val malformed = read().copy(vehicles = CardVehiclesFile(0, 1, CardGeneration.GEN2V2, byteArrayOf(1)))
        val invalid = pipeline.process(malformed, "invalid")
        assertEquals(CardImportFileStatus.INVALID, invalid.file(CardImportFileKind.VEHICLES).status)
        val empty = pipeline.process(read().copy(vehicles = CardVehiclesFile(0, 1, CardGeneration.GEN2V2, ByteArray(48))), "empty")
        assertEquals(CardImportRunStatus.PARTIAL, empty.status)
        assertEquals(CardImportFileStatus.EMPTY, empty.file(CardImportFileKind.VEHICLES).status)
        assertEquals(0, count("card_vehicle_imports")); assertEquals(0, count("driver_vehicle_uses"))
    }

    @Test fun noUsableSourceRecordsAFailureWithoutADataSession() = runBlocking {
        imports.bind(card(), true)
        val failed = pipeline.process(read().copy(activity = null, vehicles = null), "no-data")
        assertEquals(CardImportRunStatus.FAILED, failed.status); assertEquals("NO_USABLE_DATA", failed.failureCode)
        assertNull(failed.sessionId); assertEquals(0, count("import_sessions"))
        assertEquals(0, count("card_activity_imports")); assertEquals(0, count("card_vehicle_imports"))
        assertEquals(failed, pipeline.process(read().copy(activity = null, vehicles = null), "no-data"))
    }

    @Test fun normalizationFailureRollsBackAllNewDataButRetainsTheFailedRun() = runBlocking {
        imports.bind(card(), true); val earlier = pipeline.process(read(), "earlier")
        db.openHelper.writableDatabase.execSQL("CREATE TRIGGER reject_use BEFORE INSERT ON driver_vehicle_uses BEGIN SELECT RAISE(ABORT, 'fixture'); END")
        val failed = pipeline.process(read(plate = "TEST NEW"), "failed")
        assertEquals(CardImportRunStatus.FAILED, failed.status); assertEquals("STORAGE_FAILED", failed.failureCode)
        assertNull(failed.sessionId); assertEquals(0, failed.newSourceCount)
        assertEquals(setOf(CardImportFileStatus.NOT_STORED), failed.files.map { it.status }.toSet())
        assertEquals(1, count("import_sessions")); assertEquals(1, count("card_vehicle_imports"))
        assertEquals(1, count("driver_vehicle_uses")); assertEquals(1, count("card_source_pairs"))
        assertEquals(earlier, pipeline.history().first { it.id == "earlier" })
        db.openHelper.writableDatabase.execSQL("DROP TRIGGER reject_use")
        assertEquals(failed, pipeline.process(read(plate = "TEST NEW"), "failed"))
        assertEquals(CardImportRunStatus.SAVED, pipeline.process(read(plate = "TEST NEW"), "new-attempt").status)
    }

    @Test fun failureOfFinalRunReceiptAlsoRollsBackTheDataSession() = runBlocking {
        imports.bind(card(), true)
        db.openHelper.writableDatabase.execSQL("CREATE TRIGGER reject_receipt BEFORE UPDATE ON card_import_runs WHEN NEW.status = 'SAVED' BEGIN SELECT RAISE(ABORT, 'fixture'); END")
        val failed = pipeline.process(read(), "failed-receipt")
        assertEquals(CardImportRunStatus.FAILED, failed.status)
        assertEquals(0, count("import_sessions")); assertEquals(0, count("card_activity_imports"))
        assertEquals(0, count("card_vehicle_imports")); assertEquals(0, count("driver_activity_days"))
        assertEquals(0, count("driver_vehicle_uses"))
    }

    @Test fun interruptedProcessIsRecoveredOnceWithoutGuessingOrImportingItsData() = runBlocking {
        val cardId = imports.bind(card(), true)
        pipeline.history() // initialize the current process
        db.cardImportRunDao().insertRun(CardImportRunEntity("interrupted", cardId, "fixture-hash", "IN_PROGRESS", 1, null, null, 0, null))
        db.cardImportRunDao().insertFiles(CardImportFileKind.entries.map {
            CardImportFileResultEntity("interrupted", it.name, "READY", 1, 2, 3, null)
        })
        db.close(); open()
        val recovered = pipeline.history().single()
        assertEquals(CardImportRunStatus.INTERRUPTED, recovered.status)
        assertEquals(setOf(CardImportFileStatus.NOT_STORED), recovered.files.map { it.status }.toSet())
        assertEquals(recovered, pipeline.history().single())
        assertEquals(0, count("import_sessions")); assertEquals(0, count("card_activity_imports"))
        assertEquals(CardImportRunStatus.SAVED, pipeline.process(read(), "fresh").status)
    }

    @Test fun earlierHistoryIsMetadataOnlyAndNeverIncludesTheUnassignedArchive() = runBlocking {
        pairs.save(read(card("fixture-archive"), "TEST ARCHIVE"))
        pairs.save(read())
        val cardId = imports.bind(card(), true)
        val profileId = imports.profile()!!.id
        val snapshotId = db.cardActivityImportDao().all().single { it.cardNumber == "fixture-own" }.id
        val vehicleId = db.cardVehicleImportDao().all().single { it.cardNumber == "fixture-own" }.id
        val pairId = db.cardSourcePairDao().all().single { it.activityId == snapshotId }.id
        // A pre-normalization, explicitly mapped own session; the unassigned archive stays separate.
        db.driverProfileDao().insertSession(ImportSessionEntity("old-own", profileId, "PARTIAL", 1, 1))
        db.driverProfileDao().insertSources(DriverImportSourcesEntity("old-own", cardId, snapshotId, vehicleId, pairId, 2))
        db.openHelper.writableDatabase.execSQL("UPDATE card_activity_imports SET dailyRecords = X'01' WHERE id = ?", arrayOf(snapshotId))
        val metadata = pipeline.earlierImports().single()
        assertEquals("old-own", metadata.id); assertEquals(1, metadata.dayCount)
        assertTrue(pipeline.history().isEmpty()); assertEquals(1, count("driver_cards"))
        rejected<Exception> { imports.get("old-own") }
        assertEquals(2, count("card_activity_imports"))
    }

    @Test fun simultaneousRetryCreatesOneRunAndOneExactSession() = runBlocking {
        imports.bind(card(), true)
        val a = async { pipeline.process(read(), "same-request") }
        val b = async { pipeline.process(read(), "same-request") }
        assertEquals(a.await(), b.await())
        assertEquals(1, count("card_import_runs")); assertEquals(1, count("import_sessions"))
        assertEquals(1, count("card_activity_imports")); assertEquals(1, count("card_vehicle_imports"))
    }

    @Test fun migrationSevenToEightPreservesProfileSourcesAndNormalizedRowsWithoutBackfill() = runBlocking {
        imports.bind(card(), true); val old = imports.save(read(), "old", CardImportSelection.PAIR)
        pairs.save(read(card("fixture-unassigned"), "TEST ARCHIVE"))
        val profile = imports.profile(); val identity = db.driverProfileDao().cards(old.profileId)
        val originalSession = db.driverProfileDao().session(old.id, old.profileId)
        db.openHelper.writableDatabase.apply {
            execSQL("DROP TABLE card_import_file_results"); execSQL("DROP TABLE card_import_runs")
            execSQL("DELETE FROM room_master_table"); version = 7
        }
        db.close(); open()
        assertEquals(8, db.openHelper.readableDatabase.version)
        assertEquals(profile, imports.profile()); assertEquals(identity, db.driverProfileDao().cards(old.profileId))
        assertEquals(originalSession, db.driverProfileDao().session(old.id, old.profileId))
        assertEquals(1, count("driver_activity_days")); assertEquals(1, count("driver_vehicle_uses"))
        assertEquals(2, count("card_activity_imports")); assertEquals(2, count("card_vehicle_imports"))
        assertEquals(0, count("card_import_runs")); assertEquals(0, count("card_import_file_results"))
        assertEquals(listOf("old"), pipeline.earlierImports().map { it.id }); assertTrue(pipeline.history().isEmpty())
        assertEquals("TEST OWN", imports.get("old")!!.vehicles!!.uses.single().registrationNumber)
        assertEquals(0, pipeline.process(read(), "fresh").newSourceCount)
        assertEquals(originalSession, db.driverProfileDao().session(old.id, old.profileId))
    }

    @Test fun replacementWithSameNumberAndBytesRetainsItsOwnPhysicalProvenance() = runBlocking {
        imports.bind(card(), true)
        val first = pipeline.process(read(), "original")
        val originalIdentity = db.driverProfileDao().cards(imports.profile()!!.id).single()
        val replacement = card().copy(card = card().card.copy(issued = Instant.ofEpochSecond(1)))
        val next = pipeline.process(read(replacement), "replacement")
        assertNotEquals(first.driverCardId, next.driverCardId)
        assertEquals(0, next.newSourceCount)
        assertEquals(2, count("driver_cards")); assertEquals(2, count("driver_activity_days"))
        assertEquals(2, count("driver_vehicle_uses"))
        assertEquals(originalIdentity, db.driverProfileDao().cards(imports.profile()!!.id).first { it.cardId == first.driverCardId })
        assertEquals(first, pipeline.history().first { it.id == "original" })
    }
}
