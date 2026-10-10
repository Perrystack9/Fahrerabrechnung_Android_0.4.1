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

    @Test fun failedActivityReadPersistsOnlyVehiclesAndNeverAttachesAnOlderDay() = runBlocking {
        imports.bind(card(), true); val old = pipeline.process(read(), "old")
        val failedRead = read().copy(activity = null, activityReadFailed = true)
        val run = pipeline.process(failedRead, "vehicles-after-read-error")
        assertEquals(CardImportRunStatus.PARTIAL, run.status)
        assertEquals(CardImportFileStatus.READ_FAILED, run.file(CardImportFileKind.ACTIVITIES).status)
        assertEquals(0, run.file(CardImportFileKind.ACTIVITIES).decodedCount)
        assertNull(run.file(CardImportFileKind.ACTIVITIES).firstUtcEpochSeconds)
        assertEquals(CardImportFileStatus.STORED, run.file(CardImportFileKind.VEHICLES).status)
        assertEquals(0, run.newSourceCount)
        val saved = imports.get(run.sessionId!!)!!
        assertNull(saved.activity); assertNull(saved.pair)
        assertEquals("TEST OWN", saved.vehicles!!.uses.single().registrationNumber)
        assertNotNull(imports.get(old.sessionId!!)!!.activity)
        assertEquals(1, count("driver_activity_days")); assertEquals(1, count("driver_vehicle_uses"))
        assertEquals(run, pipeline.process(failedRead, "vehicles-after-read-error"))
        db.close(); open()
        assertEquals(run, pipeline.history().single { it.id == run.id })
        assertNull(imports.get(run.sessionId!!)!!.activity)
        assertNotNull(imports.get(run.sessionId!!)!!.vehicles)
    }

    @Test fun failedActivityReadIsDistinctFromMissingInputForTheSameRequest() = runBlocking {
        imports.bind(card(), true)
        val missing = read().copy(activity = null)
        val earlier = pipeline.process(missing, "request-missing")
        assertEquals(CardImportFileStatus.MISSING, earlier.file(CardImportFileKind.ACTIVITIES).status)
        rejected<IllegalArgumentException> {
            pipeline.process(missing.copy(activityReadFailed = true), "request-missing")
        }
        assertEquals(listOf(earlier), pipeline.history())
        assertEquals(1, count("import_sessions"))
    }

    @Test fun twoFailedReadsKeepEarlierDataAndNeverCreateAnEmptyDataSession() = runBlocking {
        imports.bind(card(), true); val old = pipeline.process(read(), "old")
        val empty = read().copy(activity = null, vehicles = null, activityReadFailed = true, vehicleReadFailed = true)
        val failed = pipeline.process(empty, "two-read-errors")
        assertEquals(CardImportRunStatus.FAILED, failed.status)
        assertEquals("NO_USABLE_DATA", failed.failureCode); assertNull(failed.sessionId)
        assertEquals(setOf(CardImportFileStatus.READ_FAILED), failed.files.map { it.status }.toSet())
        assertEquals(0, failed.newSourceCount)
        assertEquals(1, count("import_sessions")); assertEquals(1, count("driver_activity_days"))
        assertEquals(1, count("driver_vehicle_uses"))
        assertNotNull(imports.get(old.sessionId!!)!!.activity); assertNotNull(imports.get(old.sessionId!!)!!.vehicles)
        assertEquals(failed, pipeline.process(empty, "two-read-errors"))
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
        assertEquals(AppDatabase.SCHEMA_VERSION, db.openHelper.readableDatabase.version)
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

    private fun controlFile(generation: CardGeneration = CardGeneration.GEN2V2,
                            time: Int = 1790000000): CardControlFile = CardControlFile(generation,
        ByteBuffer.allocate(46).put(0xF8.toByte()).putInt(time).put(3.toByte()).put(13.toByte())
            .put("TEST CONTROL".padEnd(16).toByteArray()).put(13.toByte()).put(1.toByte())
            .put("TEST CONTROL".padEnd(13).toByteArray()).putInt(time - 200).putInt(time - 100).array())

    @Test fun allThreeSourcesPersistAndReopenWithExactControlMetadataAndDeduplication() = runBlocking {
        imports.bind(card(), true)
        val input = read().copy(controls = controlFile())
        val first = pipeline.process(input, "three-sources")
        assertEquals(CardImportRunStatus.SAVED, first.status); assertEquals(3, first.newSourceCount)
        assertEquals(3, first.files.size)
        assertEquals(1, first.file(CardImportFileKind.CONTROLS).decodedCount)
        assertEquals(1790000000L, first.file(CardImportFileKind.CONTROLS).firstUtcEpochSeconds)
        val saved = checkNotNull(imports.get(first.id)!!.controls)
        assertArrayEquals(input.controls!!.bytes, saved.file.bytes)
        assertEquals(first.driverCardId, saved.driverCardId)
        assertEquals("TEST CONTROL", saved.record!!.controlCardNumber)
        assertEquals(first, pipeline.process(input, first.id))
        val repeat = pipeline.process(input, "another-read")
        assertEquals(0, repeat.newSourceCount); assertEquals(1, count("card_control_imports"))
        assertEquals(2, count("driver_import_control_sources"))
        db.close(); open()
        assertArrayEquals(saved.file.bytes, imports.get(first.id)!!.controls!!.file.bytes)
        assertEquals(saved.record, imports.get(first.id)!!.controls!!.record)
        assertEquals(first, pipeline.history().single { it.id == first.id })
    }

    @Test fun changedControlsRetainBothSourcesAndExactEarlierSession() = runBlocking {
        imports.bind(card(), true)
        val old = pipeline.process(read().copy(controls = controlFile()), "old-control")
        val next = pipeline.process(read().copy(controls = controlFile(time = 1790000100)), "new-control")
        assertEquals(1, next.newSourceCount); assertEquals(2, count("card_control_imports"))
        assertEquals(1790000000L, imports.get(old.id)!!.controls!!.record!!.timeUtc.epochSecond)
        assertEquals(1790000100L, imports.get(next.id)!!.controls!!.record!!.timeUtc.epochSecond)
        assertEquals(1, count("card_activity_imports")); assertEquals(1, count("card_vehicle_imports"))
        db.close(); open()
        assertEquals(1790000000L, imports.get(old.id)!!.controls!!.record!!.timeUtc.epochSecond)
    }

    @Test fun unavailableOrInvalidControlsLeaveOtherSourcesAndNoOlderControlAttached() = runBlocking {
        imports.bind(card(), true)
        pipeline.process(read().copy(controls = controlFile()), "old")
        val failed = pipeline.process(read().copy(controlReadFailed = true), "failed-control")
        assertEquals(CardImportRunStatus.PARTIAL, failed.status)
        assertEquals(CardImportFileStatus.READ_FAILED, failed.file(CardImportFileKind.CONTROLS).status)
        assertNotNull(imports.get(failed.id)!!.activity); assertNotNull(imports.get(failed.id)!!.vehicles)
        assertNull(imports.get(failed.id)!!.controls)
        val invalid = pipeline.process(read().copy(controls = CardControlFile(card().generation, ByteArray(45))), "invalid-control")
        assertEquals(CardImportFileStatus.INVALID, invalid.file(CardImportFileKind.CONTROLS).status)
        assertNull(imports.get(invalid.id)!!.controls); assertNotNull(imports.get("old")!!.controls)
        val wrongGeneration = pipeline.process(read().copy(controls = controlFile(CardGeneration.GEN1)), "wrong-control-generation")
        assertEquals(CardImportFileStatus.INVALID, wrongGeneration.file(CardImportFileKind.CONTROLS).status)
        assertNull(imports.get(wrongGeneration.id)!!.controls)
        assertEquals(1, count("card_control_imports")); assertEquals(1, count("driver_import_control_sources"))
    }

    @Test fun controlsAloneCreateOnlyTheirExactPartialSessionWithoutInventingDaysOrVehicles() = runBlocking {
        imports.bind(card(), true); pipeline.process(read(), "older-days")
        val run = pipeline.process(read().copy(activity = null, vehicles = null,
            activityReadFailed = true, vehicleReadFailed = true, controls = controlFile()), "control-only")
        assertEquals(CardImportRunStatus.PARTIAL, run.status); assertEquals(1, run.newSourceCount)
        val saved = imports.get(run.id)!!
        assertNull(saved.activity); assertNull(saved.vehicles); assertNull(saved.pair); assertNotNull(saved.controls)
        assertEquals(1, count("driver_activity_days")); assertEquals(1, count("driver_vehicle_uses"))
        db.close(); open()
        assertNotNull(imports.get(run.id)!!.controls); assertNull(imports.get(run.id)!!.activity)
        assertNotNull(imports.get("older-days")!!.activity)
        for (generation in listOf(CardGeneration.GEN1, CardGeneration.GEN2)) {
            val own = card("fixture-${generation.name}").copy(generation = generation)
            val input = CardReadResult(CardRecognition.DRIVER, generation, own, controls = controlFile(generation))
            val imported = pipeline.process(input, "control-only-${generation.name}")
            assertEquals(CardImportRunStatus.PARTIAL, imported.status)
            assertEquals(generation, imports.get(imported.id)!!.controls!!.file.generation)
            assertEquals(imported.driverCardId, imports.get(imported.id)!!.controls!!.driverCardId)
            assertNull(imports.get(imported.id)!!.activity); assertNull(imports.get(imported.id)!!.vehicles)
        }
        assertEquals(1, count("driver_activity_days")); assertEquals(1, count("driver_vehicle_uses"))
    }

    @Test fun validUnusedControlFileIsStoredWithoutInventingAControl() = runBlocking {
        imports.bind(card(), true)
        val raw = CardControlFile(card().generation, ByteArray(46))
        val run = pipeline.process(read().copy(controls = raw), "unused-control")
        assertEquals(CardImportRunStatus.SAVED, run.status)
        assertEquals(CardImportFileStatus.STORED, run.file(CardImportFileKind.CONTROLS).status)
        assertEquals(0, run.file(CardImportFileKind.CONTROLS).decodedCount)
        assertNull(run.file(CardImportFileKind.CONTROLS).firstUtcEpochSeconds)
        assertArrayEquals(raw.bytes, imports.get(run.id)!!.controls!!.file.bytes)
        assertNull(imports.get(run.id)!!.controls!!.record)
        assertEquals(1, count("card_control_imports"))
    }

    @Test fun foreignAndUnconfirmedControlSourcesCannotBePersistedOrBound() = runBlocking {
        rejected<CardBindingRequired> { pipeline.process(read().copy(controls = controlFile()), "unconfirmed-control") }
        assertEquals(0, count("card_control_imports")); assertNull(imports.profile())
        imports.bind(card(), true)
        val foreign = card("fixture-foreign", DriverIdentity("BETA", "OTHER PERSON", LocalDate.of(1980, 1, 1), "en"))
        rejected<ForeignCardRejected> { pipeline.process(read(foreign).copy(controls = controlFile()), "foreign-control") }
        assertEquals(0, count("card_control_imports")); assertEquals(0, count("card_import_runs"))
        assertEquals(1, count("driver_cards")); assertEquals(0, count("driver_import_control_sources"))
    }

    @Test fun replacementKeepsControlSourcesUnderItsOwnPhysicalCardWithoutRewritingOldImports() = runBlocking {
        imports.bind(card(), true)
        val first = pipeline.process(read().copy(controls = controlFile()), "old-card-control")
        val replacement = card().copy(card = card().card.copy(issued = Instant.ofEpochSecond(1)))
        val second = pipeline.process(read(replacement).copy(controls = controlFile()), "replacement-control")
        assertNotEquals(first.driverCardId, second.driverCardId)
        assertEquals(1, second.newSourceCount); assertEquals(2, count("card_control_imports"))
        val old = imports.get(first.id)!!.controls!!; val newer = imports.get(second.id)!!.controls!!
        assertNotEquals(old.id, newer.id); assertNotEquals(old.driverCardId, newer.driverCardId)
        assertArrayEquals(old.file.bytes, newer.file.bytes)
        assertEquals(first.driverCardId, imports.get(first.id)!!.driverCardId)
    }

    @Test fun failureAfterControlWritesRollsBackAllSourcesAndPreservesTheFailedReceipt() = runBlocking {
        imports.bind(card(), true); pipeline.process(read(), "old-data")
        db.openHelper.writableDatabase.execSQL("CREATE TRIGGER fixture_finish_failure BEFORE UPDATE ON card_import_runs WHEN NEW.status = 'SAVED' BEGIN SELECT RAISE(ABORT, 'fixture'); END")
        val failed = pipeline.process(read(plate = "TEST CHANGED").copy(controls = controlFile()), "rollback-control")
        assertEquals(CardImportRunStatus.FAILED, failed.status); assertNull(failed.sessionId)
        assertEquals("STORAGE_FAILED", failed.failureCode)
        assertEquals(setOf(CardImportFileStatus.NOT_STORED), failed.files.map { it.status }.toSet())
        assertEquals(0, count("card_control_imports")); assertEquals(0, count("driver_import_control_sources"))
        assertEquals(1, count("import_sessions")); assertEquals(1, count("card_vehicle_imports"))
        assertEquals(1, count("driver_vehicle_uses")); assertEquals("TEST OWN", imports.get("old-data")!!.vehicles!!.uses.single().registrationNumber)
    }

    @Test fun differentControlBytesOrAttemptScopeCannotReuseAnExistingRequest() = runBlocking {
        imports.bind(card(), true)
        val first = pipeline.process(read().copy(controls = controlFile()), "same-control-request")
        rejected<IllegalArgumentException> { pipeline.process(read().copy(controls = controlFile(time = 1790000100)), first.id) }
        rejected<IllegalArgumentException> { pipeline.process(read(), first.id) }
        rejected<IllegalArgumentException> { pipeline.process(read().copy(controlReadFailed = true), first.id) }
        assertEquals(1, count("card_control_imports")); assertEquals(first, pipeline.history().single())
    }

    @Test fun migrationEightToNinePreservesHistoricCoverageAndAllOldImportsWithoutBackfill() = runBlocking {
        imports.bind(card(), true)
        val old = pipeline.process(read(), "schema-eight-run")
        val source = db.driverProfileDao().sources(old.id)
        val rows = db.cardImportRunDao().files(old.id)
        val profile = imports.profile(); val identities = db.driverProfileDao().cards(profile!!.id)
        db.openHelper.writableDatabase.apply {
            execSQL("DROP TABLE driver_import_control_sources"); execSQL("DROP TABLE card_control_imports")
            execSQL("DELETE FROM room_master_table"); version = 8
        }
        db.close(); open()
        assertEquals(AppDatabase.SCHEMA_VERSION, db.openHelper.readableDatabase.version)
        assertEquals(old, pipeline.history().single()); assertEquals(rows, db.cardImportRunDao().files(old.id))
        assertEquals(source, db.driverProfileDao().sources(old.id)); assertEquals(profile, imports.profile())
        assertEquals(identities, db.driverProfileDao().cards(profile.id))
        assertNull(imports.get(old.id)!!.controls); assertEquals(0, count("card_control_imports"))
        assertEquals(0, count("driver_import_control_sources")); assertEquals(2, old.files.size)
        assertEquals(old, pipeline.process(read(), old.id))
        val fresh = pipeline.process(read().copy(controls = controlFile()), "fresh-schema-nine")
        assertEquals(1, fresh.newSourceCount); assertEquals(3, fresh.files.size)
        assertNull(imports.get(old.id)!!.controls); assertEquals(rows, db.cardImportRunDao().files(old.id))
        assertEquals(1, count("driver_activity_days")); assertEquals(1, count("driver_vehicle_uses"))
    }

    private fun issueFile(kind: CardEventFaultKind, seconds: Int = 1790000000,
                          plate: String = "TEST EVENT", generation: CardGeneration = CardGeneration.GEN2V2,
                          empty: Boolean = false): CardEventFaultFile {
        val perType = if (kind == CardEventFaultKind.EVENTS) 12 else 24
        val raw = ByteArray(CardEventFaultParser.fileLength(kind, generation, perType))
        if (!empty) ByteBuffer.allocate(24).put((if (kind == CardEventFaultKind.EVENTS) 3 else 0x40).toByte())
            .putInt(seconds).putInt(seconds - 100).put(13.toByte()).put(1.toByte())
            .put(plate.padEnd(13).toByteArray()).array().copyInto(raw, 24)
        return CardEventFaultFile(kind, generation, perType, raw)
    }
    private fun allFiles(card: DriverCard = card()) = read(card).copy(controls = controlFile(),
        events = issueFile(CardEventFaultKind.EVENTS), faults = issueFile(CardEventFaultKind.FAULTS, plate = "TEST FAULT"))

    @Test fun allFiveSourcesPersistEncryptedReopenExactlyAndDeduplicateWithoutLosingSessionLinks() = runBlocking {
        imports.bind(card(), true)
        val input = allFiles(); val first = pipeline.process(input, "five-first")
        assertEquals(CardImportRunStatus.SAVED, first.status); assertEquals(5, first.files.size)
        assertEquals(5, first.newSourceCount)
        val saved = imports.get(first.id)!!
        assertArrayEquals(input.events!!.bytes, saved.events!!.file.bytes)
        assertArrayEquals(input.faults!!.bytes, saved.faults!!.file.bytes)
        assertEquals(1790000000L, saved.events!!.records.single().beginUtc!!.epochSecond)
        assertEquals(1789999900L, saved.events!!.records.single().endUtc!!.epochSecond)
        assertEquals(1, saved.events!!.records.single().slotIndex)
        assertEquals("TEST FAULT", saved.faults!!.records.single().registrationNumber)
        assertEquals(0, pipeline.process(input, "five-again").newSourceCount)
        assertEquals(first, pipeline.process(input, first.id))
        assertEquals(2, count("card_event_fault_imports")); assertEquals(4, count("driver_import_event_fault_sources"))
        assertFalse(context.getDatabasePath(AppDatabase.DATABASE_NAME).readBytes().take(16).toByteArray()
            .contentEquals("SQLite format 3\u0000".toByteArray()))
        db.close(); open()
        assertArrayEquals(saved.events!!.file.bytes, imports.get(first.id)!!.events!!.file.bytes)
        assertEquals(saved.events!!.records, imports.get(first.id)!!.events!!.records)
        assertEquals(saved.faults!!.records, imports.get(first.id)!!.faults!!.records)
        assertEquals(first, pipeline.history().first { it.id == first.id })
    }

    @Test fun changedEventsAndFaultsRetainBothVersionsAndNeverAttachToOlderThreeFileSessions() = runBlocking {
        imports.bind(card(), true)
        val legacy = pipeline.process(read().copy(controls = controlFile()), "legacy-three")
        val old = pipeline.process(allFiles(), "issues-old")
        val next = pipeline.process(allFiles().copy(events = issueFile(CardEventFaultKind.EVENTS, seconds = 1790000100),
            faults = issueFile(CardEventFaultKind.FAULTS, seconds = 1790000200)), "issues-new")
        assertEquals(2, old.newSourceCount); assertEquals(2, next.newSourceCount)
        assertNull(imports.get(legacy.id)!!.events); assertNull(imports.get(legacy.id)!!.faults)
        assertEquals(3, pipeline.history().first { it.id == legacy.id }.files.size)
        assertEquals(1790000000L, imports.get(old.id)!!.events!!.records.single().beginUtc!!.epochSecond)
        assertEquals(1790000100L, imports.get(next.id)!!.events!!.records.single().beginUtc!!.epochSecond)
        assertEquals(4, count("card_event_fault_imports"))
        db.close(); open()
        assertEquals(1790000000L, imports.get(old.id)!!.faults!!.records.single().beginUtc!!.epochSecond)
        assertNull(imports.get(legacy.id)!!.faults)
    }

    @Test fun missingInvalidWrongKindOrGenerationOfEitherNewFileLeavesTheOtherSourcesUsable() = runBlocking {
        imports.bind(card(), true); pipeline.process(allFiles(), "prior-issues")
        for (kind in CardEventFaultKind.entries) for (problem in 0..3) {
            val file = when (problem) {
                0 -> null
                1 -> CardEventFaultFile(kind, card().generation, if (kind == CardEventFaultKind.EVENTS) 12 else 24, ByteArray(23))
                2 -> issueFile(if (kind == CardEventFaultKind.EVENTS) CardEventFaultKind.FAULTS else CardEventFaultKind.EVENTS)
                else -> issueFile(kind, generation = CardGeneration.GEN1)
            }
            val input = if (kind == CardEventFaultKind.EVENTS) allFiles().copy(events = file, eventReadFailed = problem == 0)
                else allFiles().copy(faults = file, faultReadFailed = problem == 0)
            val run = pipeline.process(input, "problem-${kind.name}-$problem")
            assertEquals(CardImportRunStatus.PARTIAL, run.status); assertEquals(5, run.files.size)
            val status = run.file(if (kind == CardEventFaultKind.EVENTS) CardImportFileKind.EVENTS else CardImportFileKind.FAULTS).status
            assertEquals(if (problem == 0) CardImportFileStatus.READ_FAILED else CardImportFileStatus.INVALID, status)
            val saved = imports.get(run.id)!!
            assertNotNull(saved.activity); assertNotNull(saved.vehicles); assertNotNull(saved.controls)
            if (kind == CardEventFaultKind.EVENTS) { assertNull(saved.events); assertNotNull(saved.faults) }
            else { assertNull(saved.faults); assertNotNull(saved.events) }
            assertEquals(0, run.newSourceCount)
        }
        assertEquals(2, count("card_event_fault_imports"))
    }

    @Test fun validUnusedNewSourcesAreStoredEvenWithoutAnyUsableCoreSourceAndDoNotInventDays() = runBlocking {
        imports.bind(card(), true)
        val input = allFiles().copy(activity = null, vehicles = null, controls = null,
            activityReadFailed = true, vehicleReadFailed = true, controlReadFailed = true,
            events = issueFile(CardEventFaultKind.EVENTS, empty = true), faults = issueFile(CardEventFaultKind.FAULTS, empty = true))
        val run = pipeline.process(input, "unused-new")
        assertEquals(CardImportRunStatus.PARTIAL, run.status); assertEquals(2, run.newSourceCount)
        for (kind in listOf(CardImportFileKind.EVENTS, CardImportFileKind.FAULTS)) {
            assertEquals(CardImportFileStatus.STORED, run.file(kind).status); assertEquals(0, run.file(kind).decodedCount)
            assertNull(run.file(kind).firstUtcEpochSeconds); assertNull(run.file(kind).lastUtcEpochSeconds)
        }
        assertTrue(imports.get(run.id)!!.events!!.records.isEmpty()); assertTrue(imports.get(run.id)!!.faults!!.records.isEmpty())
        assertEquals(0, count("driver_activity_days")); assertEquals(0, count("driver_vehicle_uses"))
        assertEquals(0, pipeline.process(input, "unused-again").newSourceCount)
    }

    @Test fun allFiveReadFailuresRetainTheFailedReceiptWithoutAnyNewDataSession() = runBlocking {
        imports.bind(card(), true); val old = pipeline.process(allFiles(), "good-five")
        val input = allFiles().copy(activity = null, vehicles = null, controls = null, events = null, faults = null,
            activityReadFailed = true, vehicleReadFailed = true, controlReadFailed = true, eventReadFailed = true, faultReadFailed = true)
        val failed = pipeline.process(input, "bad-five")
        assertEquals(CardImportRunStatus.FAILED, failed.status); assertNull(failed.sessionId)
        assertEquals(5, failed.files.size); assertTrue(failed.files.all { it.status == CardImportFileStatus.READ_FAILED })
        assertEquals(1, count("import_sessions")); assertEquals(2, count("card_event_fault_imports"))
        assertNotNull(imports.get(old.id)!!.events); assertEquals(failed, pipeline.process(input, failed.id))
    }

    @Test fun unconfirmedForeignAndUnclearCardsCannotPersistTheNewFiles() = runBlocking {
        rejected<CardBindingRequired> { pipeline.process(allFiles(), "new-unconfirmed") }
        assertEquals(0, count("card_event_fault_imports")); assertNull(imports.profile())
        imports.bind(card(), true)
        val foreign = card("other-card", DriverIdentity("OTHER", "OTHER PERSON", LocalDate.of(1980, 1, 1), "en"))
        rejected<ForeignCardRejected> { pipeline.process(allFiles(foreign), "new-foreign") }
        val unclear = card("unclear-card", card().holder.copy(birthDate = null))
        rejected<CardBindingRequired> { pipeline.process(allFiles(unclear), "new-unclear") }
        assertEquals(0, count("card_event_fault_imports")); assertEquals(0, count("driver_import_event_fault_sources"))
        assertEquals(0, count("card_import_runs")); assertEquals(1, count("driver_cards"))
    }

    @Test fun replacementCardsKeepSeparatePhysicalEventAndFaultSourcesUnderTheSameDriverProfile() = runBlocking {
        imports.bind(card(), true); val old = pipeline.process(allFiles(), "own-old")
        val next = pipeline.process(allFiles(card("replacement-card")), "own-replacement")
        val a = imports.get(old.id)!!; val b = imports.get(next.id)!!
        assertEquals(a.profileId, b.profileId); assertNotEquals(a.driverCardId, b.driverCardId)
        assertNotEquals(a.events!!.id, b.events!!.id); assertNotEquals(a.faults!!.id, b.faults!!.id)
        assertArrayEquals(a.events!!.file.bytes, b.events!!.file.bytes)
        assertEquals(old.driverCardId, imports.get(old.id)!!.events!!.driverCardId)
        assertEquals(4, count("card_event_fault_imports"))
    }

    @Test fun failureAfterEventAndFaultWritesRollsBackEveryNewSourceAndKeepsOldData() = runBlocking {
        imports.bind(card(), true); pipeline.process(read().copy(controls = controlFile()), "before-new-files")
        db.openHelper.writableDatabase.execSQL("CREATE TRIGGER fixture_new_finish_failure BEFORE UPDATE ON card_import_runs WHEN NEW.status = 'SAVED' BEGIN SELECT RAISE(ABORT, 'fixture'); END")
        val failed = pipeline.process(allFiles().copy(vehicles = read(plate = "TEST CHANGE").vehicles), "rollback-new-files")
        assertEquals(CardImportRunStatus.FAILED, failed.status); assertNull(failed.sessionId)
        assertEquals("STORAGE_FAILED", failed.failureCode); assertTrue(failed.files.all { it.status == CardImportFileStatus.NOT_STORED })
        assertEquals(0, count("card_event_fault_imports")); assertEquals(0, count("driver_import_event_fault_sources"))
        assertEquals(1, count("import_sessions")); assertEquals(1, count("card_control_imports"))
        assertEquals(1, count("card_vehicle_imports")); assertEquals(1, count("driver_vehicle_uses"))
        assertNotNull(imports.get("before-new-files")!!.controls)
    }

    @Test fun changedNewFilesOrAttemptCoverageCannotReuseAnExistingRequest() = runBlocking {
        imports.bind(card(), true); val first = pipeline.process(allFiles(), "immutable-five")
        rejected<IllegalArgumentException> { pipeline.process(allFiles().copy(events = issueFile(CardEventFaultKind.EVENTS, seconds = 1790000200)), first.id) }
        rejected<IllegalArgumentException> { pipeline.process(allFiles().copy(faults = issueFile(CardEventFaultKind.FAULTS, seconds = 1790000300)), first.id) }
        rejected<IllegalArgumentException> { pipeline.process(read().copy(controls = controlFile()), first.id) }
        assertEquals(first, pipeline.history().single()); assertEquals(2, count("card_event_fault_imports"))
    }

    @Test fun migrationNineToTenPreservesTwoAndThreeFileReceiptsAndDoesNotBackfillNewSources() = runBlocking {
        imports.bind(card(), true)
        val two = pipeline.process(read(), "old-two")
        val three = pipeline.process(read().copy(controls = controlFile()), "old-three")
        val before = listOf(two, three).associate { it.id to db.cardImportRunDao().run(it.id) }
        val identities = db.driverProfileDao().cards(imports.profile()!!.id)
        val control = imports.get(three.id)!!.controls!!
        db.openHelper.writableDatabase.apply {
            execSQL("DROP TABLE driver_import_event_fault_sources"); execSQL("DROP TABLE card_event_fault_imports")
            execSQL("DELETE FROM room_master_table"); version = 9
        }
        db.close(); open()
        assertEquals(AppDatabase.SCHEMA_VERSION, db.openHelper.readableDatabase.version)
        for (run in listOf(two, three)) {
            assertEquals(before[run.id], db.cardImportRunDao().run(run.id))
            assertNull(imports.get(run.id)!!.events); assertNull(imports.get(run.id)!!.faults)
        }
        assertEquals(two, pipeline.process(read(), two.id))
        assertEquals(three, pipeline.process(read().copy(controls = controlFile()), three.id))
        assertEquals(identities, db.driverProfileDao().cards(imports.profile()!!.id))
        assertArrayEquals(control.file.bytes, imports.get(three.id)!!.controls!!.file.bytes)
        assertEquals(0, count("card_event_fault_imports")); assertEquals(0, count("driver_import_event_fault_sources"))
        val fresh = pipeline.process(allFiles(), "new-five")
        assertEquals(2, fresh.newSourceCount); assertEquals(5, fresh.files.size)
        assertNull(imports.get(three.id)!!.events); assertEquals(before[two.id], db.cardImportRunDao().run(two.id))
    }

    @Test fun corruptNewRawSourceDoesNotHideHistoryOrUnrelatedSavedSessions() = runBlocking {
        imports.bind(card(), true); val old = pipeline.process(read().copy(controls = controlFile()), "safe-legacy")
        val run = pipeline.process(allFiles(), "corrupt-event")
        val id = imports.get(run.id)!!.events!!.id
        db.openHelper.writableDatabase.execSQL("UPDATE card_event_fault_imports SET bytes = ? WHERE id = ?", arrayOf(ByteArray(23), id))
        assertEquals(2, pipeline.history().size); assertNotNull(imports.get(old.id)!!.controls)
        rejected<IllegalStateException> { imports.get(run.id) }
    }

    @Test fun newSourceForeignKeysRejectWrongPhysicalCardOrFileKindLinks() = runBlocking {
        imports.bind(card(), true); val first = pipeline.process(allFiles(), "source-fk")
        val second = pipeline.process(read(), "empty-link")
        val source = imports.get(first.id)!!.events!!
        rejected<android.database.sqlite.SQLiteException> {
            db.cardEventFaultDao().insertSource(DriverImportEventFaultSourceEntity(second.id,
                CardEventFaultKind.FAULTS.name, second.driverCardId, source.id))
        }
        rejected<android.database.sqlite.SQLiteException> {
            db.cardEventFaultDao().insertSource(DriverImportEventFaultSourceEntity(second.id,
                CardEventFaultKind.EVENTS.name, "other-physical-card", source.id))
        }
        assertNull(imports.get(second.id)!!.events); assertNull(imports.get(second.id)!!.faults)
    }

    private fun conditionFile(time: Long = 1790000001L, type: Int = 1, empty: Boolean = false) =
        CardSpecificConditionFile(CardGeneration.GEN2V2, 112, ByteArray(562).also {
            if (!empty) ByteBuffer.wrap(it).apply { position(2); putInt(time.toInt()); put(type.toByte()) }
        })
    private fun sixFiles() = allFiles().copy(specificConditions = conditionFile())

    @Test fun sixSourcesPersistExactMetadataDeduplicateAndReopenEncrypted() = runBlocking {
        imports.bind(card(), true)
        val input = sixFiles(); val first = pipeline.process(input, "six")
        assertEquals(CardImportRunStatus.SAVED, first.status); assertEquals(6, first.newSourceCount)
        assertEquals(6, first.files.size); assertEquals(1, first.file(CardImportFileKind.SPECIFIC_CONDITIONS).decodedCount)
        assertEquals(1790000001L, first.file(CardImportFileKind.SPECIFIC_CONDITIONS).firstUtcEpochSeconds)
        val source = imports.get(first.id)!!.specificConditions!!
        assertEquals(first.driverCardId, source.driverCardId); assertArrayEquals(input.specificConditions!!.bytes, source.file.bytes)
        assertEquals(first, pipeline.process(input, first.id))
        assertEquals(0, pipeline.process(input, "six-again").newSourceCount)
        assertEquals(1, count("card_specific_condition_imports")); assertEquals(2, count("driver_import_specific_condition_sources"))
        db.close(); open()
        assertEquals(first, pipeline.history().single { it.id == first.id })
        assertEquals(source.records, imports.get(first.id)!!.specificConditions!!.records)
        assertFalse(context.getDatabasePath(AppDatabase.DATABASE_NAME).readBytes().take(16).toByteArray()
            .contentEquals("SQLite format 3\u0000".toByteArray()))
    }

    @Test fun emptyAndUnknownTimeConditionSourcesRemainDistinctHonestStoredSources() = runBlocking {
        imports.bind(card(), true)
        val empty = sixFiles().copy(specificConditions = conditionFile(empty = true))
        val first = pipeline.process(empty, "empty-condition")
        val result = first.file(CardImportFileKind.SPECIFIC_CONDITIONS)
        assertEquals(CardImportFileStatus.STORED, result.status); assertEquals(0, result.decodedCount)
        assertNull(result.firstUtcEpochSeconds); assertNull(result.lastUtcEpochSeconds)
        assertTrue(imports.get(first.id)!!.specificConditions!!.records.isEmpty())
        assertEquals(0, pipeline.process(empty, "empty-repeat").newSourceCount)
        val unknown = pipeline.process(sixFiles().copy(specificConditions = conditionFile(time = 0)), "unknown-time")
        assertEquals(1, unknown.newSourceCount); assertEquals(1, unknown.file(CardImportFileKind.SPECIFIC_CONDITIONS).decodedCount)
        assertNull(unknown.file(CardImportFileKind.SPECIFIC_CONDITIONS).firstUtcEpochSeconds)
        assertNull(unknown.file(CardImportFileKind.SPECIFIC_CONDITIONS).lastUtcEpochSeconds)
        assertNull(imports.get(unknown.id)!!.specificConditions!!.records.single().timeUtc)
    }

    @Test fun failedInvalidWrongGenerationOrPointerConditionsKeepTheOtherFiveSources() = runBlocking {
        imports.bind(card(), true)
        val failures = listOf(
            sixFiles().copy(specificConditions = null, specificConditionReadFailed = true),
            sixFiles().copy(specificConditions = CardSpecificConditionFile(CardGeneration.GEN2V2, 112, ByteArray(561))),
            sixFiles().copy(specificConditions = CardSpecificConditionFile(CardGeneration.GEN1, 56, ByteArray(280))),
            sixFiles().copy(specificConditions = CardSpecificConditionFile(CardGeneration.GEN2V2, 112, ByteArray(562).also { it[1] = 112 })),
            sixFiles().copy(specificConditionReadFailed = true))
        for ((index, input) in failures.withIndex()) {
            val run = pipeline.process(input, "condition-error-$index")
            assertEquals(CardImportRunStatus.PARTIAL, run.status)
            assertEquals(if (input.specificConditionReadFailed) CardImportFileStatus.READ_FAILED else CardImportFileStatus.INVALID,
                run.file(CardImportFileKind.SPECIFIC_CONDITIONS).status)
            val saved = imports.get(run.id)!!
            assertNotNull(saved.activity); assertNotNull(saved.vehicles); assertNotNull(saved.controls)
            assertNotNull(saved.events); assertNotNull(saved.faults); assertNull(saved.specificConditions)
        }
        assertEquals(0, count("card_specific_condition_imports"))
    }

    @Test fun conditionOnlySourcePersistsWhenAllOtherAttemptedFilesFail() = runBlocking {
        imports.bind(card(), true)
        val input = sixFiles().copy(activity = null, vehicles = null, controls = null, events = null, faults = null,
            activityReadFailed = true, vehicleReadFailed = true, controlReadFailed = true, eventReadFailed = true, faultReadFailed = true)
        val run = pipeline.process(input, "condition-only")
        assertEquals(CardImportRunStatus.PARTIAL, run.status); assertEquals(1, run.newSourceCount)
        assertNotNull(imports.get(run.id)!!.specificConditions); assertNull(imports.get(run.id)!!.activity)
        assertEquals(1, count("import_sessions")); assertEquals(0, count("driver_activity_days"))
    }

    @Test fun olderRequestsKeepCoverageFingerprintAndSourcesWhileNewVersionsRemainSeparate() = runBlocking {
        imports.bind(card(), true)
        val old = pipeline.process(allFiles(), "before-conditions")
        val row = db.cardImportRunDao().run(old.id); val files = db.cardImportRunDao().files(old.id)
        val first = pipeline.process(sixFiles(), "condition-first")
        assertEquals(1, first.newSourceCount); assertNull(imports.get(old.id)!!.specificConditions)
        assertEquals(old, pipeline.process(allFiles(), old.id))
        assertEquals(row, db.cardImportRunDao().run(old.id)); assertEquals(files, db.cardImportRunDao().files(old.id))
        rejected<IllegalArgumentException> { pipeline.process(sixFiles(), old.id) }
        rejected<IllegalArgumentException> { pipeline.process(sixFiles().copy(specificConditions = conditionFile(type = 2)), first.id) }
        rejected<IllegalStateException> { imports.save(sixFiles().copy(specificConditions = conditionFile(type = 2)), first.id, CardImportSelection.ALL_AVAILABLE) }
        val next = pipeline.process(sixFiles().copy(specificConditions = conditionFile(type = 2)), "condition-next")
        assertEquals(1, next.newSourceCount)
        assertNotEquals(imports.get(first.id)!!.specificConditions!!.id, imports.get(next.id)!!.specificConditions!!.id)
        assertEquals(1, imports.get(first.id)!!.specificConditions!!.records.single().type)
        assertEquals(2, imports.get(next.id)!!.specificConditions!!.records.single().type)
    }

    @Test fun unconfirmedForeignAndUnclearCardsCannotWriteConditionsAndReplacementsStaySeparate() = runBlocking {
        rejected<CardBindingRequired> { pipeline.process(sixFiles(), "unconfirmed-conditions") }
        imports.bind(card(), true)
        val foreign = card("fixture-foreign-condition", DriverIdentity("BETA", "OTHER", LocalDate.of(1980, 1, 1), "en"))
        rejected<ForeignCardRejected> { pipeline.process(sixFiles().copy(card = foreign), "foreign-conditions") }
        val unclear = card("fixture-unclear-condition", card().holder.copy(birthDate = null))
        rejected<CardBindingRequired> { pipeline.process(sixFiles().copy(card = unclear), "unclear-conditions") }
        assertEquals(0, count("card_specific_condition_imports")); assertEquals(0, count("card_import_runs"))
        val old = pipeline.process(sixFiles(), "physical-first")
        val new = pipeline.process(sixFiles().copy(card = card("fixture-replacement")), "physical-new")
        val a = imports.get(old.id)!!.specificConditions!!; val b = imports.get(new.id)!!.specificConditions!!
        assertNotEquals(a.driverCardId, b.driverCardId); assertNotEquals(a.id, b.id)
        assertArrayEquals(a.file.bytes, b.file.bytes)
        assertEquals(old.driverCardId, imports.get(old.id)!!.specificConditions!!.driverCardId)
        assertEquals(2, count("card_specific_condition_imports"))
    }

    @Test fun conditionWriteRollsBackWithTheOtherSourcesOnStorageFailure() = runBlocking {
        imports.bind(card(), true); pipeline.process(allFiles(), "before-condition-rollback")
        db.openHelper.writableDatabase.execSQL("CREATE TRIGGER fixture_condition_fail BEFORE UPDATE ON card_import_runs WHEN NEW.status = 'SAVED' BEGIN SELECT RAISE(ABORT, 'fixture'); END")
        val failed = pipeline.process(sixFiles().copy(vehicles = read(plate = "TEST CHANGE").vehicles), "condition-rollback")
        assertEquals(CardImportRunStatus.FAILED, failed.status); assertEquals("STORAGE_FAILED", failed.failureCode)
        assertNull(failed.sessionId); assertTrue(failed.files.all { it.status == CardImportFileStatus.NOT_STORED })
        assertEquals(0, count("card_specific_condition_imports")); assertEquals(0, count("driver_import_specific_condition_sources"))
        assertEquals(1, count("import_sessions")); assertEquals(1, count("card_vehicle_imports"))
        assertNotNull(imports.get("before-condition-rollback")!!.events)
    }

    @Test fun migrationTenToElevenKeepsOldReceiptsSourcesAndProfilesWithoutBackfill() = runBlocking {
        imports.bind(card(), true)
        val old = pipeline.process(allFiles(), "schema-ten")
        val row = db.cardImportRunDao().run(old.id); val files = db.cardImportRunDao().files(old.id)
        val identities = db.driverProfileDao().cards(imports.profile()!!.id)
        val eventBytes = imports.get(old.id)!!.events!!.file.bytes
        db.openHelper.writableDatabase.apply {
            execSQL("DROP TABLE driver_import_specific_condition_sources"); execSQL("DROP TABLE card_specific_condition_imports")
            execSQL("DELETE FROM room_master_table"); version = 10
        }
        db.close(); open()
        assertEquals(AppDatabase.SCHEMA_VERSION, db.openHelper.readableDatabase.version)
        assertEquals(row, db.cardImportRunDao().run(old.id)); assertEquals(files, db.cardImportRunDao().files(old.id))
        assertEquals(identities, db.driverProfileDao().cards(imports.profile()!!.id))
        assertArrayEquals(eventBytes, imports.get(old.id)!!.events!!.file.bytes)
        assertNull(imports.get(old.id)!!.specificConditions); assertEquals(old, pipeline.process(allFiles(), old.id))
        assertEquals(0, count("card_specific_condition_imports")); assertEquals(0, count("driver_import_specific_condition_sources"))
        val fresh = pipeline.process(sixFiles(), "schema-eleven")
        assertEquals(1, fresh.newSourceCount); assertNotNull(imports.get(fresh.id)!!.specificConditions)
        assertNull(imports.get(old.id)!!.specificConditions)
    }

    @Test fun corruptedConditionsKeepHistoryUsableAndWrongPhysicalSourceLinksAreRejected() = runBlocking {
        imports.bind(card(), true)
        val old = pipeline.process(allFiles(), "safe-before-condition")
        val new = pipeline.process(sixFiles(), "corrupt-condition")
        val source = imports.get(new.id)!!.specificConditions!!
        rejected<android.database.sqlite.SQLiteException> {
            db.cardSpecificConditionDao().insertSource(DriverImportSpecificConditionSourceEntity(old.id, "wrong-physical-card", source.id))
        }
        assertNull(imports.get(old.id)!!.specificConditions)
        db.openHelper.writableDatabase.execSQL("UPDATE card_specific_condition_imports SET bytes = ? WHERE id = ?", arrayOf(ByteArray(4), source.id))
        assertEquals(2, pipeline.history().size); assertNotNull(imports.get(old.id)!!.events)
        rejected<IllegalStateException> { imports.get(new.id) }
    }

    @Test fun corruptControlSourceDoesNotPreventHistoryOrOtherImportsFromOpening() = runBlocking {
        imports.bind(card(), true)
        val old = pipeline.process(read(), "without-control")
        val run = pipeline.process(read().copy(controls = controlFile()), "corrupt-control")
        val id = imports.get(run.id)!!.controls!!.id
        db.openHelper.writableDatabase.execSQL("UPDATE card_control_imports SET bytes = ? WHERE id = ?", arrayOf(ByteArray(45), id))
        assertEquals(2, pipeline.history().size); assertNotNull(imports.get(old.id)!!.activity)
        rejected<IllegalStateException> { imports.get(run.id) }
    }

    private fun gnssRead(time: Int = 1790000015): CardReadResult {
        val positions = ByteArray(CardGnssAccumulatedDrivingParser.fileLength(CardGeneration.GEN2V2, 336))
        ByteBuffer.wrap(positions).putShort(0).putInt(time - 7).putInt(time).put(10.toByte())
            .put(byteArrayOf(0, 0, 1, 0, 0, 2, 0, 0, 3))
        val auth = ByteArray(CardGnssAuthenticationParser.fileLength(CardGeneration.GEN2V2))
        ByteBuffer.wrap(auth).putShort(0).putInt(time - 7).put(1.toByte())
        return sixFiles().copy(gnssPositions = CardGnssAccumulatedDrivingFile(CardGeneration.GEN2V2, 336, positions),
            gnssAuthentication = CardGnssAuthenticationFile(CardGeneration.GEN2V2, auth))
    }

    @Test fun gnssPersistsExactSelectedSourcesAcrossReopenWithoutHistoricalFallback() = runBlocking {
        imports.bind(card(), true)
        val old = pipeline.process(sixFiles(), "before-gnss")
        val fresh = pipeline.process(gnssRead(), "with-gnss")
        assertEquals(2, fresh.newSourceCount); assertEquals(8, fresh.files.size)
        val saved = imports.get(fresh.id)!!
        assertEquals(1790000015L, saved.gnssPositions!!.records.single().positionTimeRaw)
        assertEquals(1790000008L, saved.gnssAuthentication!!.records.single().accumulatedDrivingTimeRaw)
        db.close(); open()
        assertArrayEquals(saved.gnssPositions!!.file.bytes, imports.get(fresh.id)!!.gnssPositions!!.file.bytes)
        assertArrayEquals(saved.gnssAuthentication!!.file.bytes, imports.get(fresh.id)!!.gnssAuthentication!!.file.bytes)
        assertNull(imports.get(old.id)!!.gnssPositions); assertNull(imports.get(old.id)!!.gnssAuthentication)
        assertEquals(old, pipeline.process(sixFiles(), old.id))
    }

    @Test fun gnssRetriesDeduplicateWhileChangedAndReplacementSourcesRemainSeparate() = runBlocking {
        imports.bind(card(), true)
        val first = pipeline.process(gnssRead(), "gnss-first")
        assertEquals(first, pipeline.process(gnssRead(), first.id))
        val same = pipeline.process(gnssRead(), "gnss-same")
        assertEquals(0, same.newSourceCount); assertEquals(2, count("card_gnss_imports"))
        rejected<IllegalArgumentException> { pipeline.process(gnssRead(1790000020), first.id) }
        val changed = pipeline.process(gnssRead(1790000020), "gnss-changed")
        assertEquals(2, changed.newSourceCount)
        val replacement = pipeline.process(gnssRead().copy(card = card("replacement-gnss")), "gnss-replacement")
        assertEquals(8, replacement.newSourceCount)
        assertNotEquals(first.driverCardId, replacement.driverCardId)
        assertNotEquals(imports.get(first.id)!!.gnssPositions!!.id, imports.get(replacement.id)!!.gnssPositions!!.id)
        assertEquals(1790000015L, imports.get(first.id)!!.gnssPositions!!.records.single().positionTimeRaw)
    }

    @Test fun gnssFailureIsPartialButValidEmptyFileIsStoredEvenWithoutOtherData() = runBlocking {
        imports.bind(card(), true)
        val partial = pipeline.process(gnssRead().copy(gnssPositions = null, gnssPositionReadFailed = true), "partial-gnss")
        assertEquals(CardImportRunStatus.PARTIAL, partial.status)
        assertEquals(CardImportFileStatus.READ_FAILED, partial.file(CardImportFileKind.GNSS_POSITIONS).status)
        assertNotNull(imports.get(partial.id)!!.gnssAuthentication)
        val invalid = pipeline.process(gnssRead().copy(gnssPositions =
            CardGnssAccumulatedDrivingFile(CardGeneration.GEN2, 336, ByteArray(6050))), "invalid-generation-gnss")
        assertEquals(CardImportFileStatus.INVALID, invalid.file(CardImportFileKind.GNSS_POSITIONS).status)
        assertNull(imports.get(invalid.id)!!.gnssPositions); assertNotNull(imports.get(invalid.id)!!.gnssAuthentication)
        val authFailed = pipeline.process(gnssRead().copy(gnssAuthentication = null,
            gnssAuthenticationReadFailed = true), "authentication-failed-gnss")
        assertEquals(CardImportFileStatus.READ_FAILED, authFailed.file(CardImportFileKind.GNSS_AUTHENTICATION).status)
        assertNotNull(imports.get(authFailed.id)!!.gnssPositions); assertNull(imports.get(authFailed.id)!!.gnssAuthentication)
        val only = read().copy(activity = null, vehicles = null, gnssPositions =
            CardGnssAccumulatedDrivingFile(CardGeneration.GEN2V2, 336, ByteArray(6050)))
        val empty = pipeline.process(only, "empty-gnss")
        assertNotNull(empty.sessionId); assertEquals(1, empty.newSourceCount)
        assertEquals(CardImportFileStatus.STORED, empty.file(CardImportFileKind.GNSS_POSITIONS).status)
        assertEquals(0, empty.file(CardImportFileKind.GNSS_POSITIONS).decodedCount)
        assertTrue(imports.get(empty.id)!!.gnssPositions!!.records.isEmpty())
    }

    @Test fun gnssForeignUnconfirmedAndUnclearReadsCannotPersist() = runBlocking {
        rejected<CardBindingRequired> { pipeline.process(gnssRead(), "unconfirmed-gnss") }
        imports.bind(card(), true)
        val foreign = card("foreign-gnss", DriverIdentity("BETA", "OTHER", LocalDate.of(1980, 1, 1), "en"))
        rejected<ForeignCardRejected> { pipeline.process(gnssRead().copy(card = foreign), "foreign-gnss") }
        rejected<CardBindingRequired> { pipeline.process(gnssRead().copy(card = card("unclear-gnss", card().holder.copy(birthDate = null))), "unclear-gnss") }
        assertEquals(0, count("card_gnss_imports")); assertEquals(0, count("driver_import_gnss_sources"))
        assertEquals(0, count("card_import_runs"))
    }

    @Test fun gnssWriteFailureRollsBackAllSelectedSourcesAndSession() = runBlocking {
        imports.bind(card(), true)
        db.openHelper.writableDatabase.execSQL("CREATE TRIGGER fixture_gnss_fail BEFORE UPDATE ON card_import_runs WHEN NEW.status = 'SAVED' BEGIN SELECT RAISE(ABORT, 'fixture'); END")
        val failed = pipeline.process(gnssRead(), "rollback-gnss")
        assertEquals(CardImportRunStatus.FAILED, failed.status); assertNull(failed.sessionId)
        assertEquals(0, count("card_gnss_imports")); assertEquals(0, count("driver_import_gnss_sources"))
        assertEquals(0, count("import_sessions")); assertEquals(0, count("card_activity_imports"))
        assertEquals(0, count("card_specific_condition_imports"))
    }

    @Test fun schemaElevenUpgradePreservesExactOldReceiptsWithoutGnssBackfill() = runBlocking {
        imports.bind(card(), true)
        val old = pipeline.process(sixFiles(), "schema-eleven-gnss")
        val row = db.cardImportRunDao().run(old.id); val files = db.cardImportRunDao().files(old.id)
        val bytes = imports.get(old.id)!!.specificConditions!!.file.bytes
        db.openHelper.writableDatabase.apply {
            execSQL("DROP TABLE driver_import_gnss_sources"); execSQL("DROP TABLE card_gnss_imports")
            execSQL("DELETE FROM room_master_table"); version = 11
        }
        db.close(); open()
        assertEquals(AppDatabase.SCHEMA_VERSION, db.openHelper.readableDatabase.version)
        assertEquals(row, db.cardImportRunDao().run(old.id)); assertEquals(files, db.cardImportRunDao().files(old.id))
        assertArrayEquals(bytes, imports.get(old.id)!!.specificConditions!!.file.bytes)
        assertNull(imports.get(old.id)!!.gnssPositions); assertEquals(old, pipeline.process(sixFiles(), old.id))
        assertEquals(0, count("card_gnss_imports"))
        assertEquals(2, pipeline.process(gnssRead(), "after-upgrade-gnss").newSourceCount)
    }

    @Test fun gnssWrongPhysicalLinksAndCorruptedBytesAreRejectedWithoutBreakingHistory() = runBlocking {
        imports.bind(card(), true)
        val old = pipeline.process(sixFiles(), "old-safe-gnss")
        val fresh = pipeline.process(gnssRead(), "corrupted-gnss")
        val source = imports.get(fresh.id)!!.gnssPositions!!
        rejected<android.database.sqlite.SQLiteException> {
            db.cardGnssDao().insertSource(DriverImportGnssSourceEntity(old.id, CardGnssFileKind.POSITIONS.name, "wrong-card", source.id))
        }
        db.openHelper.writableDatabase.execSQL("UPDATE card_gnss_imports SET bytes = ? WHERE id = ?", arrayOf(ByteArray(4), source.id))
        assertEquals(2, pipeline.history().size); assertNotNull(imports.get(old.id)!!.specificConditions)
        rejected<IllegalStateException> { imports.get(fresh.id) }
    }

    private fun placesRead(time: Int = 1790000015): CardReadResult {
        val bytes = ByteArray(2354)
        ByteBuffer.wrap(bytes).putShort(0).putInt(time).put(6.toByte()).put(13.toByte()).put(0.toByte())
            .put(byteArrayOf(0, 0, 3)).putInt(time + 7).put(10.toByte()).put(byteArrayOf(0, 0, 1, 0, 0, 2))
        val auth = ByteArray(562)
        ByteBuffer.wrap(auth).putShort(0).putInt(time).put(1.toByte())
        return gnssRead().copy(places = CardPlacesFile(CardGeneration.GEN2V2, 112, bytes),
            placesAuthentication = CardPlacesAuthenticationFile(CardGeneration.GEN2V2, auth))
    }

    @Test fun placesPersistsExactSelectedSourcesAcrossReopenWithoutHistoricalFallback() = runBlocking {
        imports.bind(card(), true)
        val old = pipeline.process(gnssRead(), "before-places")
        val fresh = pipeline.process(placesRead(), "with-places")
        assertEquals(2, fresh.newSourceCount); assertEquals(10, fresh.files.size)
        val saved = imports.get(fresh.id)!!
        assertEquals(1790000015L, saved.places!!.records.single().entryTimeRaw)
        assertEquals(1790000015L, saved.placesAuthentication!!.records.single().entryTimeRaw)
        db.close(); open()
        assertArrayEquals(saved.places!!.file.bytes, imports.get(fresh.id)!!.places!!.file.bytes)
        assertArrayEquals(saved.placesAuthentication!!.file.bytes, imports.get(fresh.id)!!.placesAuthentication!!.file.bytes)
        assertNull(imports.get(old.id)!!.places); assertNull(imports.get(old.id)!!.placesAuthentication)
        assertEquals(old, pipeline.process(gnssRead(), old.id))
    }

    @Test fun placesRetriesDeduplicateWhileChangedAndReplacementSourcesRemainSeparate() = runBlocking {
        imports.bind(card(), true)
        val first = pipeline.process(placesRead(), "places-first")
        assertEquals(first, pipeline.process(placesRead(), first.id))
        val same = pipeline.process(placesRead(), "places-same")
        assertEquals(0, same.newSourceCount); assertEquals(2, count("card_place_imports"))
        rejected<IllegalArgumentException> { pipeline.process(placesRead(1790000020), first.id) }
        val changed = pipeline.process(placesRead(1790000020), "places-changed")
        assertEquals(2, changed.newSourceCount)
        val replacement = pipeline.process(placesRead().copy(card = card("replacement-places")), "places-replacement")
        assertEquals(10, replacement.newSourceCount)
        assertNotEquals(first.driverCardId, replacement.driverCardId)
        assertNotEquals(imports.get(first.id)!!.places!!.id, imports.get(replacement.id)!!.places!!.id)
        assertEquals(1790000015L, imports.get(first.id)!!.places!!.records.single().entryTimeRaw)
    }

    @Test fun placesFailureIsPartialButValidEmptyFileIsStoredEvenWithoutOtherData() = runBlocking {
        imports.bind(card(), true)
        val partial = pipeline.process(placesRead().copy(places = null, placeReadFailed = true), "partial-places")
        assertEquals(CardImportRunStatus.PARTIAL, partial.status)
        assertEquals(CardImportFileStatus.READ_FAILED, partial.file(CardImportFileKind.WORK_PERIOD_PLACES).status)
        assertNotNull(imports.get(partial.id)!!.placesAuthentication)
        val invalid = pipeline.process(placesRead().copy(places =
            CardPlacesFile(CardGeneration.GEN2, 112, ByteArray(2354))), "invalid-generation-places")
        assertEquals(CardImportFileStatus.INVALID, invalid.file(CardImportFileKind.WORK_PERIOD_PLACES).status)
        assertNull(imports.get(invalid.id)!!.places); assertNotNull(imports.get(invalid.id)!!.placesAuthentication)
        val authFailed = pipeline.process(placesRead().copy(placesAuthentication = null,
            placesAuthenticationReadFailed = true), "authentication-failed-places")
        assertEquals(CardImportFileStatus.READ_FAILED, authFailed.file(CardImportFileKind.WORK_PERIOD_PLACES_AUTHENTICATION).status)
        assertNotNull(imports.get(authFailed.id)!!.places); assertNull(imports.get(authFailed.id)!!.placesAuthentication)
        val only = read().copy(activity = null, vehicles = null, places =
            CardPlacesFile(CardGeneration.GEN2V2, 112, ByteArray(2354)))
        val empty = pipeline.process(only, "empty-places")
        assertNotNull(empty.sessionId); assertEquals(1, empty.newSourceCount)
        assertEquals(CardImportFileStatus.STORED, empty.file(CardImportFileKind.WORK_PERIOD_PLACES).status)
        assertEquals(0, empty.file(CardImportFileKind.WORK_PERIOD_PLACES).decodedCount)
        assertTrue(imports.get(empty.id)!!.places!!.records.isEmpty())
    }

    @Test fun placesForeignUnconfirmedAndUnclearReadsCannotPersist() = runBlocking {
        rejected<CardBindingRequired> { pipeline.process(placesRead(), "unconfirmed-places") }
        imports.bind(card(), true)
        val foreign = card("foreign-places", DriverIdentity("BETA", "OTHER", LocalDate.of(1980, 1, 1), "en"))
        rejected<ForeignCardRejected> { pipeline.process(placesRead().copy(card = foreign), "foreign-places") }
        rejected<CardBindingRequired> { pipeline.process(placesRead().copy(card = card("unclear-places", card().holder.copy(birthDate = null))), "unclear-places") }
        assertEquals(0, count("card_place_imports")); assertEquals(0, count("driver_import_place_sources"))
        assertEquals(0, count("card_import_runs"))
    }

    @Test fun placesWriteFailureRollsBackAllSelectedSourcesAndSession() = runBlocking {
        imports.bind(card(), true)
        db.openHelper.writableDatabase.execSQL("CREATE TRIGGER fixture_places_fail BEFORE UPDATE ON card_import_runs WHEN NEW.status = 'SAVED' BEGIN SELECT RAISE(ABORT, 'fixture'); END")
        val failed = pipeline.process(placesRead(), "rollback-places")
        assertEquals(CardImportRunStatus.FAILED, failed.status); assertNull(failed.sessionId)
        assertEquals(0, count("card_place_imports")); assertEquals(0, count("driver_import_place_sources"))
        assertEquals(0, count("import_sessions")); assertEquals(0, count("card_activity_imports"))
        assertEquals(0, count("card_specific_condition_imports"))
    }

    @Test fun schemaTwelveUpgradePreservesExactOldReceiptsWithoutPlacesBackfill() = runBlocking {
        imports.bind(card(), true)
        val old = pipeline.process(gnssRead(), "schema-eleven-places")
        val row = db.cardImportRunDao().run(old.id); val files = db.cardImportRunDao().files(old.id)
        val bytes = imports.get(old.id)!!.specificConditions!!.file.bytes
        db.openHelper.writableDatabase.apply {
            execSQL("DROP TABLE driver_import_place_sources"); execSQL("DROP TABLE card_place_imports")
            execSQL("DELETE FROM room_master_table"); version = 12
        }
        db.close(); open()
        assertEquals(AppDatabase.SCHEMA_VERSION, db.openHelper.readableDatabase.version)
        assertEquals(row, db.cardImportRunDao().run(old.id)); assertEquals(files, db.cardImportRunDao().files(old.id))
        assertArrayEquals(bytes, imports.get(old.id)!!.specificConditions!!.file.bytes)
        assertNull(imports.get(old.id)!!.places); assertEquals(old, pipeline.process(gnssRead(), old.id))
        assertEquals(0, count("card_place_imports"))
        assertEquals(2, pipeline.process(placesRead(), "after-upgrade-places").newSourceCount)
    }

    @Test fun placesWrongPhysicalLinksAndCorruptedBytesAreRejectedWithoutBreakingHistory() = runBlocking {
        imports.bind(card(), true)
        val old = pipeline.process(gnssRead(), "old-safe-places")
        val fresh = pipeline.process(placesRead(), "corrupted-places")
        val source = imports.get(fresh.id)!!.places!!
        rejected<android.database.sqlite.SQLiteException> {
            db.cardPlacesDao().insertSource(DriverImportPlacesSourceEntity(old.id, CardPlacesFileKind.PLACES.name, "wrong-card", source.id))
        }
        db.openHelper.writableDatabase.execSQL("UPDATE card_place_imports SET bytes = ? WHERE id = ?", arrayOf(ByteArray(4), source.id))
        assertEquals(2, pipeline.history().size); assertNotNull(imports.get(old.id)!!.specificConditions)
        rejected<IllegalStateException> { imports.get(fresh.id) }
    }

    private fun borderRead(time: Int = 1790000030): CardReadResult {
        val bytes = ByteArray(19042)
        ByteBuffer.wrap(bytes).putShort(0).put(13.toByte()).put(1.toByte()).putInt(time).put(10.toByte())
            .put(byteArrayOf(0, 0, 1, 0, 0, 2)).put(1.toByte()).put(byteArrayOf(0, 0, 3))
        return placesRead().copy(borderCrossings = CardBorderCrossingsFile(CardGeneration.GEN2V2, 1120, bytes))
    }

    @Test fun bordersPersistExactOwnCardSessionAcrossEncryptedReopenWithoutHistoricalFallback() = runBlocking {
        imports.bind(card(), true)
        val old = pipeline.process(placesRead(), "before-borders")
        val fresh = pipeline.process(borderRead(), "with-borders")
        assertEquals(1, fresh.newSourceCount); assertEquals(11, fresh.files.size)
        val source = imports.get(fresh.id)!!.borderCrossings!!
        assertEquals(fresh.driverCardId, source.driverCardId)
        assertEquals(1790000030L, source.records.single().positionTimeRaw)
        db.close(); open()
        assertArrayEquals(source.file.bytes, imports.get(fresh.id)!!.borderCrossings!!.file.bytes)
        assertNull(imports.get(old.id)!!.borderCrossings)
        assertEquals(old, pipeline.process(placesRead(), old.id))
    }
    @Test fun bordersDeduplicateButChangedRequestsAndPhysicalReplacementCardsStayDistinct() = runBlocking {
        imports.bind(card(), true)
        val first = pipeline.process(borderRead(), "borders-first")
        assertEquals(first, pipeline.process(borderRead(), first.id))
        assertEquals(0, pipeline.process(borderRead(), "borders-repeat").newSourceCount)
        assertEquals(1, count("card_border_crossing_imports"))
        rejected<IllegalArgumentException> { pipeline.process(borderRead(1790000035), first.id) }
        rejected<IllegalArgumentException> { pipeline.process(placesRead(), first.id) }
        val changed = pipeline.process(borderRead(1790000035), "borders-changed")
        assertEquals(1, changed.newSourceCount)
        val replacement = pipeline.process(borderRead().copy(card = card("replacement-borders")), "borders-replacement")
        assertEquals(11, replacement.newSourceCount)
        assertNotEquals(first.driverCardId, replacement.driverCardId)
        assertNotEquals(imports.get(first.id)!!.borderCrossings!!.id, imports.get(replacement.id)!!.borderCrossings!!.id)
        assertEquals(1790000030L, imports.get(first.id)!!.borderCrossings!!.records.single().positionTimeRaw)
    }
    @Test fun borderFileFailuresAndInvalidPointerAreIndependentAndValidEmptySourceCanStandAlone() = runBlocking {
        imports.bind(card(), true)
        val partial = pipeline.process(borderRead().copy(borderCrossings = null, borderCrossingsReadFailed = true), "borders-partial")
        assertEquals(CardImportRunStatus.PARTIAL, partial.status)
        assertEquals(CardImportFileStatus.READ_FAILED, partial.file(CardImportFileKind.BORDER_CROSSINGS).status)
        assertNotNull(imports.get(partial.id)!!.places)
        val invalidBytes = ByteArray(19042).also { it[0] = 4; it[1] = 96 }
        val invalid = pipeline.process(borderRead().copy(borderCrossings = CardBorderCrossingsFile(CardGeneration.GEN2V2, 1120, invalidBytes)), "borders-invalid")
        assertEquals(CardImportFileStatus.INVALID, invalid.file(CardImportFileKind.BORDER_CROSSINGS).status)
        assertNull(imports.get(invalid.id)!!.borderCrossings); assertNotNull(imports.get(invalid.id)!!.places)
        val only = read().copy(activity = null, vehicles = null,
            borderCrossings = CardBorderCrossingsFile(CardGeneration.GEN2V2, 1120, ByteArray(19042)))
        val empty = pipeline.process(only, "borders-empty")
        assertNotNull(empty.sessionId); assertEquals(1, empty.newSourceCount)
        assertEquals(CardImportFileStatus.STORED, empty.file(CardImportFileKind.BORDER_CROSSINGS).status)
        assertEquals(0, empty.file(CardImportFileKind.BORDER_CROSSINGS).decodedCount)
        assertTrue(imports.get(empty.id)!!.borderCrossings!!.records.isEmpty())
    }
    @Test fun bordersForeignUnconfirmedAndUnclearCardsCannotCreatePersistentSources() = runBlocking {
        rejected<CardBindingRequired> { pipeline.process(borderRead(), "borders-unconfirmed") }
        imports.bind(card(), true)
        val foreign = card("foreign-borders", DriverIdentity("BETA", "OTHER", LocalDate.of(1980, 1, 1), "en"))
        rejected<ForeignCardRejected> { pipeline.process(borderRead().copy(card = foreign), "borders-foreign") }
        rejected<CardBindingRequired> { pipeline.process(borderRead().copy(card = card("unclear-borders", card().holder.copy(birthDate = null))), "borders-unclear") }
        assertEquals(0, count("card_border_crossing_imports")); assertEquals(0, count("driver_import_border_crossing_sources"))
        assertEquals(0, count("card_import_runs"))
    }
    @Test fun borderSourceWriteFailureRollsBackTheEntireOwnImport() = runBlocking {
        imports.bind(card(), true)
        db.openHelper.writableDatabase.execSQL("CREATE TRIGGER fixture_border_fail BEFORE INSERT ON card_border_crossing_imports BEGIN SELECT RAISE(ABORT, 'fixture'); END")
        val failed = pipeline.process(borderRead(), "borders-rollback")
        assertEquals(CardImportRunStatus.FAILED, failed.status); assertNull(failed.sessionId)
        for (table in listOf("card_border_crossing_imports", "driver_import_border_crossing_sources", "card_place_imports",
            "card_gnss_imports", "import_sessions", "card_activity_imports", "card_specific_condition_imports"))
            assertEquals(0, count(table))
    }
    @Test fun schemaThirteenUpgradeKeepsOldReceiptsAndSourcesWithoutBorderBackfill() = runBlocking {
        imports.bind(card(), true)
        val old = pipeline.process(placesRead(), "schema-thirteen-borders")
        val row = db.cardImportRunDao().run(old.id); val files = db.cardImportRunDao().files(old.id)
        val bytes = imports.get(old.id)!!.places!!.file.bytes
        db.openHelper.writableDatabase.apply {
            execSQL("DROP TABLE driver_import_border_crossing_sources"); execSQL("DROP TABLE card_border_crossing_imports")
            execSQL("DELETE FROM room_master_table"); version = 13
        }
        db.close(); open()
        assertEquals(AppDatabase.SCHEMA_VERSION, db.openHelper.readableDatabase.version)
        assertEquals(row, db.cardImportRunDao().run(old.id)); assertEquals(files, db.cardImportRunDao().files(old.id))
        assertArrayEquals(bytes, imports.get(old.id)!!.places!!.file.bytes)
        assertNull(imports.get(old.id)!!.borderCrossings); assertEquals(old, pipeline.process(placesRead(), old.id))
        assertEquals(0, count("card_border_crossing_imports"))
        assertEquals(1, pipeline.process(borderRead(), "borders-after-upgrade").newSourceCount)
    }
    @Test fun borderWrongCardLinksAndAlteredBytesCannotLeakIntoAnotherSession() = runBlocking {
        imports.bind(card(), true)
        val old = pipeline.process(placesRead(), "borders-safe-old")
        val fresh = pipeline.process(borderRead(), "borders-corrupt-new")
        val source = imports.get(fresh.id)!!.borderCrossings!!
        rejected<android.database.sqlite.SQLiteException> {
            db.cardBorderCrossingsDao().insertSource(DriverImportBorderCrossingsSourceEntity(old.id, "wrong-card", source.id))
        }
        val altered = source.file.bytes.also { it[3] = 7 }
        db.openHelper.writableDatabase.execSQL("UPDATE card_border_crossing_imports SET bytes = ? WHERE id = ?", arrayOf(altered, source.id))
        assertEquals(2, pipeline.history().size); assertNull(imports.get(old.id)!!.borderCrossings)
        rejected<IllegalStateException> { imports.get(fresh.id) }
    }

    private fun loadRead(time: Int = 1790000030): CardReadResult {
        val bytes = ByteArray(32482)
        ByteBuffer.wrap(bytes).putShort(0).putInt(time - 10).put(1.toByte()).putInt(time).put(10.toByte())
            .put(byteArrayOf(0, 0, 1, 0, 0, 2)).put(1.toByte()).put(byteArrayOf(0, 0, 3))
        return borderRead().copy(loadUnload = CardLoadUnloadFile(CardGeneration.GEN2V2, 1624, bytes))
    }

    @Test fun loadsPersistExactOwnCardSessionAcrossEncryptedReopenWithoutHistoricalFallback() = runBlocking {
        imports.bind(card(), true)
        val old = pipeline.process(borderRead(), "before-loads")
        val fresh = pipeline.process(loadRead(), "with-loads")
        assertEquals(1, fresh.newSourceCount); assertEquals(12, fresh.files.size)
        val source = imports.get(fresh.id)!!.loadUnload!!
        assertEquals(fresh.driverCardId, source.driverCardId)
        assertEquals(1790000020L, source.records.single().operationTimeRaw)
        db.close(); open()
        assertArrayEquals(source.file.bytes, imports.get(fresh.id)!!.loadUnload!!.file.bytes)
        assertNull(imports.get(old.id)!!.loadUnload)
        assertEquals(old, pipeline.process(borderRead(), old.id))
    }
    @Test fun loadsDeduplicateButChangedRequestsAndPhysicalReplacementCardsStayDistinct() = runBlocking {
        imports.bind(card(), true)
        val first = pipeline.process(loadRead(), "loads-first")
        assertEquals(first, pipeline.process(loadRead(), first.id))
        assertEquals(0, pipeline.process(loadRead(), "loads-repeat").newSourceCount)
        assertEquals(1, count("card_load_unload_imports"))
        rejected<IllegalArgumentException> { pipeline.process(loadRead(1790000035), first.id) }
        rejected<IllegalArgumentException> { pipeline.process(borderRead(), first.id) }
        val changed = pipeline.process(loadRead(1790000035), "loads-changed")
        assertEquals(1, changed.newSourceCount)
        val replacement = pipeline.process(loadRead().copy(card = card("replacement-loads")), "loads-replacement")
        assertEquals(12, replacement.newSourceCount)
        assertNotEquals(first.driverCardId, replacement.driverCardId)
        assertNotEquals(imports.get(first.id)!!.loadUnload!!.id, imports.get(replacement.id)!!.loadUnload!!.id)
        assertEquals(1790000030L, imports.get(first.id)!!.loadUnload!!.records.single().positionTimeRaw)
    }
    @Test fun loadFileFailuresAndInvalidPointerAreIndependentAndValidEmptySourceCanStandAlone() = runBlocking {
        imports.bind(card(), true)
        val partial = pipeline.process(loadRead().copy(loadUnload = null, loadUnloadReadFailed = true), "loads-partial")
        assertEquals(CardImportRunStatus.PARTIAL, partial.status)
        assertEquals(CardImportFileStatus.READ_FAILED, partial.file(CardImportFileKind.LOAD_UNLOAD_OPERATIONS).status)
        assertNotNull(imports.get(partial.id)!!.borderCrossings)
        val invalidBytes = ByteArray(32482).also { it[0] = 6; it[1] = 88 }
        val invalid = pipeline.process(loadRead().copy(loadUnload = CardLoadUnloadFile(CardGeneration.GEN2V2, 1624, invalidBytes)), "loads-invalid")
        assertEquals(CardImportFileStatus.INVALID, invalid.file(CardImportFileKind.LOAD_UNLOAD_OPERATIONS).status)
        assertNull(imports.get(invalid.id)!!.loadUnload); assertNotNull(imports.get(invalid.id)!!.borderCrossings)
        val only = read().copy(activity = null, vehicles = null,
            loadUnload = CardLoadUnloadFile(CardGeneration.GEN2V2, 1624, ByteArray(32482)))
        val empty = pipeline.process(only, "loads-empty")
        assertNotNull(empty.sessionId); assertEquals(1, empty.newSourceCount)
        assertEquals(CardImportFileStatus.STORED, empty.file(CardImportFileKind.LOAD_UNLOAD_OPERATIONS).status)
        assertEquals(0, empty.file(CardImportFileKind.LOAD_UNLOAD_OPERATIONS).decodedCount)
        assertTrue(imports.get(empty.id)!!.loadUnload!!.records.isEmpty())
    }
    @Test fun loadsForeignUnconfirmedAndUnclearCardsCannotCreatePersistentSources() = runBlocking {
        rejected<CardBindingRequired> { pipeline.process(loadRead(), "loads-unconfirmed") }
        imports.bind(card(), true)
        val foreign = card("foreign-loads", DriverIdentity("BETA", "OTHER", LocalDate.of(1980, 1, 1), "en"))
        rejected<ForeignCardRejected> { pipeline.process(loadRead().copy(card = foreign), "loads-foreign") }
        rejected<CardBindingRequired> { pipeline.process(loadRead().copy(card = card("unclear-loads", card().holder.copy(birthDate = null))), "loads-unclear") }
        assertEquals(0, count("card_load_unload_imports")); assertEquals(0, count("driver_import_load_unload_sources"))
        assertEquals(0, count("card_import_runs"))
    }
    @Test fun loadSourceWriteFailureRollsBackTheEntireOwnImport() = runBlocking {
        imports.bind(card(), true)
        db.openHelper.writableDatabase.execSQL("CREATE TRIGGER fixture_load_fail BEFORE INSERT ON card_load_unload_imports BEGIN SELECT RAISE(ABORT, 'fixture'); END")
        val failed = pipeline.process(loadRead(), "loads-rollback")
        assertEquals(CardImportRunStatus.FAILED, failed.status); assertNull(failed.sessionId)
        for (table in listOf("card_load_unload_imports", "driver_import_load_unload_sources", "card_place_imports",
            "card_gnss_imports", "import_sessions", "card_activity_imports", "card_specific_condition_imports"))
            assertEquals(0, count(table))
    }
    @Test fun schemaFourteenUpgradeKeepsOldReceiptsAndSourcesWithoutLoadBackfill() = runBlocking {
        imports.bind(card(), true)
        val old = pipeline.process(borderRead(), "schema-fourteen-loads")
        val row = db.cardImportRunDao().run(old.id); val files = db.cardImportRunDao().files(old.id)
        val bytes = imports.get(old.id)!!.places!!.file.bytes
        db.openHelper.writableDatabase.apply {
            execSQL("DROP TABLE driver_import_load_type_sources"); execSQL("DROP TABLE card_load_type_imports")
            execSQL("DROP TABLE driver_import_load_unload_sources"); execSQL("DROP TABLE card_load_unload_imports")
            execSQL("DELETE FROM room_master_table"); version = 14
        }
        db.close(); open()
        assertEquals(AppDatabase.SCHEMA_VERSION, db.openHelper.readableDatabase.version)
        assertEquals(row, db.cardImportRunDao().run(old.id)); assertEquals(files, db.cardImportRunDao().files(old.id))
        assertArrayEquals(bytes, imports.get(old.id)!!.places!!.file.bytes)
        assertNull(imports.get(old.id)!!.loadUnload); assertEquals(old, pipeline.process(borderRead(), old.id))
        assertEquals(0, count("card_load_unload_imports"))
        assertEquals(1, pipeline.process(loadRead(), "loads-after-upgrade").newSourceCount)
    }
    @Test fun loadWrongCardLinksAndAlteredBytesCannotLeakIntoAnotherSession() = runBlocking {
        imports.bind(card(), true)
        val old = pipeline.process(borderRead(), "loads-safe-old")
        val fresh = pipeline.process(loadRead(), "loads-corrupt-new")
        val source = imports.get(fresh.id)!!.loadUnload!!
        rejected<android.database.sqlite.SQLiteException> {
            db.cardLoadUnloadDao().insertSource(DriverImportLoadUnloadSourceEntity(old.id, "wrong-card", source.id))
        }
        val altered = source.file.bytes.also { it[3] = 7 }
        db.openHelper.writableDatabase.execSQL("UPDATE card_load_unload_imports SET bytes = ? WHERE id = ?", arrayOf(altered, source.id))
        assertEquals(2, pipeline.history().size); assertNull(imports.get(old.id)!!.loadUnload)
        rejected<IllegalStateException> { imports.get(fresh.id) }
    }

    private fun typeRead(time: Int = 1790000030): CardReadResult {
        val bytes = ByteArray(1682)
        ByteBuffer.wrap(bytes).putShort(0).putInt(time).put(1.toByte())
        return loadRead().copy(loadType = CardLoadTypeFile(CardGeneration.GEN2V2, 336, bytes))
    }

    @Test fun typesPersistExactOwnCardSessionAcrossEncryptedReopenWithoutHistoricalFallback() = runBlocking {
        imports.bind(card(), true)
        val old = pipeline.process(loadRead(), "before-types")
        val fresh = pipeline.process(typeRead(), "with-types")
        assertEquals(1, fresh.newSourceCount); assertEquals(13, fresh.files.size)
        val source = imports.get(fresh.id)!!.loadType!!
        assertEquals(fresh.driverCardId, source.driverCardId)
        assertEquals(1790000030L, source.records.single().entryTimeRaw)
        db.close(); open()
        assertArrayEquals(source.file.bytes, imports.get(fresh.id)!!.loadType!!.file.bytes)
        assertNull(imports.get(old.id)!!.loadType)
        assertEquals(old, pipeline.process(loadRead(), old.id))
    }
    @Test fun typesDeduplicateButChangedRequestsAndPhysicalReplacementCardsStayDistinct() = runBlocking {
        imports.bind(card(), true)
        val first = pipeline.process(typeRead(), "types-first")
        assertEquals(first, pipeline.process(typeRead(), first.id))
        assertEquals(0, pipeline.process(typeRead(), "types-repeat").newSourceCount)
        assertEquals(1, count("card_load_type_imports"))
        rejected<IllegalArgumentException> { pipeline.process(typeRead(1790000035), first.id) }
        rejected<IllegalArgumentException> { pipeline.process(loadRead(), first.id) }
        val changed = pipeline.process(typeRead(1790000035), "types-changed")
        assertEquals(1, changed.newSourceCount)
        val replacement = pipeline.process(typeRead().copy(card = card("replacement-types")), "types-replacement")
        assertEquals(13, replacement.newSourceCount)
        assertNotEquals(first.driverCardId, replacement.driverCardId)
        assertNotEquals(imports.get(first.id)!!.loadType!!.id, imports.get(replacement.id)!!.loadType!!.id)
        assertEquals(1790000030L, imports.get(first.id)!!.loadType!!.records.single().entryTimeRaw)
    }
    @Test fun typeFileFailuresAndInvalidPointerAreIndependentAndValidEmptySourceCanStandAlone() = runBlocking {
        imports.bind(card(), true)
        val partial = pipeline.process(typeRead().copy(loadType = null, loadTypeReadFailed = true), "types-partial")
        assertEquals(CardImportRunStatus.PARTIAL, partial.status)
        assertEquals(CardImportFileStatus.READ_FAILED, partial.file(CardImportFileKind.LOAD_TYPE_ENTRIES).status)
        assertNotNull(imports.get(partial.id)!!.loadUnload)
        val invalidBytes = ByteArray(1682).also { it[0] = 1; it[1] = 80 }
        val invalid = pipeline.process(typeRead().copy(loadType = CardLoadTypeFile(CardGeneration.GEN2V2, 336, invalidBytes)), "types-invalid")
        assertEquals(CardImportFileStatus.INVALID, invalid.file(CardImportFileKind.LOAD_TYPE_ENTRIES).status)
        assertNull(imports.get(invalid.id)!!.loadType); assertNotNull(imports.get(invalid.id)!!.loadUnload)
        val only = read().copy(activity = null, vehicles = null,
            loadType = CardLoadTypeFile(CardGeneration.GEN2V2, 336, ByteArray(1682)))
        val empty = pipeline.process(only, "types-empty")
        assertNotNull(empty.sessionId); assertEquals(1, empty.newSourceCount)
        assertEquals(CardImportFileStatus.STORED, empty.file(CardImportFileKind.LOAD_TYPE_ENTRIES).status)
        assertEquals(0, empty.file(CardImportFileKind.LOAD_TYPE_ENTRIES).decodedCount)
        assertTrue(imports.get(empty.id)!!.loadType!!.records.isEmpty())
    }
    @Test fun typesForeignUnconfirmedAndUnclearCardsCannotCreatePersistentSources() = runBlocking {
        rejected<CardBindingRequired> { pipeline.process(typeRead(), "types-unconfirmed") }
        imports.bind(card(), true)
        val foreign = card("foreign-types", DriverIdentity("BETA", "OTHER", LocalDate.of(1980, 1, 1), "en"))
        rejected<ForeignCardRejected> { pipeline.process(typeRead().copy(card = foreign), "types-foreign") }
        rejected<CardBindingRequired> { pipeline.process(typeRead().copy(card = card("unclear-types", card().holder.copy(birthDate = null))), "types-unclear") }
        assertEquals(0, count("card_load_type_imports")); assertEquals(0, count("driver_import_load_type_sources"))
        assertEquals(0, count("card_import_runs"))
    }
    @Test fun typeSourceWriteFailureRollsBackTheEntireOwnImport() = runBlocking {
        imports.bind(card(), true)
        db.openHelper.writableDatabase.execSQL("CREATE TRIGGER fixture_type_fail BEFORE INSERT ON card_load_type_imports BEGIN SELECT RAISE(ABORT, 'fixture'); END")
        val failed = pipeline.process(typeRead(), "types-rollback")
        assertEquals(CardImportRunStatus.FAILED, failed.status); assertNull(failed.sessionId)
        for (table in listOf("card_load_type_imports", "driver_import_load_type_sources", "card_load_unload_imports", "driver_import_load_unload_sources", "card_place_imports",
            "card_gnss_imports", "import_sessions", "card_activity_imports", "card_specific_condition_imports"))
            assertEquals(0, count(table))
    }
    @Test fun schemaFifteenUpgradeKeepsOldReceiptsAndSourcesWithoutLoadTypeBackfill() = runBlocking {
        imports.bind(card(), true)
        val old = pipeline.process(loadRead(), "schema-fifteen-types")
        val row = db.cardImportRunDao().run(old.id); val files = db.cardImportRunDao().files(old.id)
        val bytes = imports.get(old.id)!!.places!!.file.bytes
        db.openHelper.writableDatabase.apply {
            execSQL("DROP TABLE driver_import_load_type_sources"); execSQL("DROP TABLE card_load_type_imports")
            execSQL("DELETE FROM room_master_table"); version = 15
        }
        db.close(); open()
        assertEquals(AppDatabase.SCHEMA_VERSION, db.openHelper.readableDatabase.version)
        assertEquals(row, db.cardImportRunDao().run(old.id)); assertEquals(files, db.cardImportRunDao().files(old.id))
        assertArrayEquals(bytes, imports.get(old.id)!!.places!!.file.bytes)
        assertNull(imports.get(old.id)!!.loadType); assertEquals(old, pipeline.process(loadRead(), old.id))
        assertEquals(0, count("card_load_type_imports"))
        assertEquals(1, pipeline.process(typeRead(), "types-after-upgrade").newSourceCount)
    }
    @Test fun typeWrongCardLinksAndAlteredBytesCannotLeakIntoAnotherSession() = runBlocking {
        imports.bind(card(), true)
        val old = pipeline.process(loadRead(), "types-safe-old")
        val fresh = pipeline.process(typeRead(), "types-corrupt-new")
        val source = imports.get(fresh.id)!!.loadType!!
        rejected<android.database.sqlite.SQLiteException> {
            db.cardLoadTypeDao().insertSource(DriverImportLoadTypeSourceEntity(old.id, "wrong-card", source.id))
        }
        val altered = source.file.bytes.also { it[3] = 7 }
        db.openHelper.writableDatabase.execSQL("UPDATE card_load_type_imports SET bytes = ? WHERE id = ?", arrayOf(altered, source.id))
        assertEquals(2, pipeline.history().size); assertNull(imports.get(old.id)!!.loadType)
        rejected<IllegalStateException> { imports.get(fresh.id) }
    }

    @Test fun loadTypeCoverageRetainsUnknownEntryTimeWithoutBorrowingOtherSourceTimes() = runBlocking {
        imports.bind(card(), true)
        val result = pipeline.process(typeRead(0), "type-unknown-time")
        val receipt = result.file(CardImportFileKind.LOAD_TYPE_ENTRIES)
        assertEquals(CardImportFileStatus.STORED, receipt.status); assertEquals(1, receipt.decodedCount)
        assertNull(receipt.firstUtcEpochSeconds); assertNull(receipt.lastUtcEpochSeconds)
        val row = imports.get(result.id)!!.loadType!!.records.single()
        assertNull(row.entryTimeUtc); assertEquals(CardRecordedLoadType.GOODS, row.loadType)
        assertNotNull(imports.get(result.id)!!.loadUnload!!.records.single().operationTimeUtc)
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
