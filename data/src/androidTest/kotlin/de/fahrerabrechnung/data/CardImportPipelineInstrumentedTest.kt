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
    /** Synthetic complete EF inputs; this helper is never used to backfill stored sources. */
    private fun coreRead(activityBytes: ByteArray? = null, vehicleBytes: ByteArray? = null): CardReadResult {
        val value = read()
        val activity = activityBytes ?: ByteBuffer.allocate(18).putShort(0).putShort(0)
            .put(value.activity!!.dailyRecords).array()
        val vehicle = vehicleBytes ?: ByteBuffer.allocate(50).putShort(0).put(value.vehicles!!.records).array()
        return value.copy(coreReads = mapOf(
            CardCoreEf.ACTIVITY to CardCoreRead(CardCoreEf.ACTIVITY, CardCoreReadStatus.COMPLETE,
                CardCoreSnapshot(CardCoreEf.ACTIVITY, value.generation, activity)),
            CardCoreEf.VEHICLES to CardCoreRead(CardCoreEf.VEHICLES, CardCoreReadStatus.COMPLETE,
                CardCoreSnapshot(CardCoreEf.VEHICLES, value.generation, vehicle))))
    }

    private fun signedCoreRead(salt: Int = 7, input: CardReadResult = coreRead()): CardReadResult {
        val certificate = CardCertificateFile(CardCertificateKind.SIGNING, input.generation, certificateFixture())
        return input.copy(certificates = mapOf(CardCertificateKind.SIGNING to CardCertificateRead(certificate)),
            coreSignatures = input.coreReads.mapValues { (kind, source) ->
                CardCoreSignature(kind, input.generation, CardSignatureStatus.CAPTURED_UNVERIFIED,
                    CardSignaturePolicy.NIST_P256, CardSignaturePhase.SIGN, statusWord = 0x9000,
                    hashStatusWord = 0x9000, signStatusWord = 0x9000, actualLength = 64,
                    original = source.snapshot, certificate = certificate, bytes = ByteArray(64) { (salt + it).toByte() })
            })
    }

    @Test fun newSignaturesSurviveReopenWithoutBackfillingUnsignedHistoryOrChangingReplay() = runBlocking {
        imports.bind(card(), true)
        val old = pipeline.process(coreRead(), "signature-old")
        val oldFingerprint = db.cardImportRunDao().run(old.id)!!.requestFingerprint
        val firstInput = signedCoreRead()
        val first = pipeline.process(firstInput, "signature-new")
        assertEquals(CardImportRunStatus.SAVED, first.status)
        assertEquals(CardImportFileStatus.STORED, first.file(CardImportFileKind.ACTIVITY_SIGNATURE).status)
        assertEquals(first, pipeline.process(firstInput, first.id))
        val second = pipeline.process(signedCoreRead(13), "signature-next")
        assertEquals(2, second.newSourceCount) // only the two new session-owned signatures; raw sources deduplicate
        assertEquals(4, count("card_core_signature_receipts"))
        rejected<IllegalArgumentException> { pipeline.process(signedCoreRead(13), first.id) }
        rejected<IllegalArgumentException> { pipeline.process(firstInput, old.id) }
        rejected<IllegalStateException> { imports.save(firstInput, old.id, CardImportSelection.ALL_AVAILABLE) }
        db.close(); open()
        assertTrue(imports.get(old.id)!!.coreSignatures.isEmpty())
        assertEquals(oldFingerprint, db.cardImportRunDao().run(old.id)!!.requestFingerprint)
        assertEquals(old, pipeline.process(coreRead(), old.id))
        for (kind in CardCoreEf.entries) {
            val saved = imports.get(first.id)!!.coreSignatures.getValue(kind)
            assertArrayEquals(firstInput.coreSignatures.getValue(kind).bytes, saved.bytes)
            assertArrayEquals(firstInput.coreReads.getValue(kind).snapshot!!.bytes, saved.original!!.bytes)
            assertEquals(CardSignaturePolicy.NIST_P256, saved.policy)
            saved.bytes!!.fill(0)
            assertEquals(7, imports.get(first.id)!!.coreSignatures.getValue(kind).bytes!![0].toInt())
            assertEquals(13, imports.get(second.id)!!.coreSignatures.getValue(kind).bytes!![0].toInt())
        }
    }

    @Test fun signatureReceiptsPersistEmptyInvalidAndFailedRawSourcesIndependently() = runBlocking {
        imports.bind(card(), true)
        for ((id, input) in listOf("empty" to coreRead(ByteArray(68), ByteArray(50)),
            "invalid" to coreRead(ByteArray(68).also { it[0] = -1 }, ByteArray(50).also { it[0] = -1 }))) {
            val run = pipeline.process(signedCoreRead(input = input), "signed-$id")
            val saved = imports.get(run.id)!!
            assertNull(saved.activity); assertNull(saved.vehicles)
            assertEquals(2, saved.coreSignatures.size)
            assertTrue(saved.coreSignatures.values.all { it.status == CardSignatureStatus.CAPTURED_UNVERIFIED })
        }
        val raw = coreRead()
        val failed = raw.copy(coreSignatures = raw.coreReads.mapValues { (kind, source) ->
            CardCoreSignature(kind, raw.generation, CardSignatureStatus.FAILED,
                phase = CardSignaturePhase.HASH, reason = CardSignatureReason.CARD_STATUS,
                statusWord = 0x6985, hashStatusWord = 0x6985, original = source.snapshot)
        })
        val run = pipeline.process(failed, "signature-hash-failure")
        assertEquals(CardImportRunStatus.PARTIAL, run.status)
        db.close(); open()
        val saved = imports.get(run.id)!!
        assertEquals(2, saved.coreSnapshots.size)
        assertTrue(saved.coreSignatures.values.all { it.statusWord == 0x6985 && it.bytes == null })
        val missing = raw.copy(coreReads = raw.coreReads + (CardCoreEf.ACTIVITY to CardCoreRead(CardCoreEf.ACTIVITY, CardCoreReadStatus.ABSENT)),
            coreSignatures = mapOf(CardCoreEf.ACTIVITY to CardCoreSignature(CardCoreEf.ACTIVITY, raw.generation,
                CardSignatureStatus.NOT_ATTEMPTED, phase = CardSignaturePhase.SELECT, reason = CardSignatureReason.EF_ABSENT)))
        val absent = pipeline.process(missing, "signature-absent")
        assertEquals(CardSignatureReason.EF_ABSENT, imports.get(absent.id)!!.coreSignatures.getValue(CardCoreEf.ACTIVITY).reason)
    }

    @Test fun excludingOriginalsFromPartialSelectionDoesNotPersistDetachedSignatures() = runBlocking {
        imports.bind(card(), true)
        val saved = imports.save(signedCoreRead(), "signature-partial-selection", CardImportSelection.ACTIVITY)
        assertTrue(saved.coreSnapshots.isEmpty()); assertTrue(saved.coreSignatures.isEmpty())
        assertEquals(0, count("card_core_signature_receipts"))
    }

    @Test fun contradictoryRawOrCertificateBytesCannotCreateAnySignatureRun() = runBlocking {
        imports.bind(card(), true)
        val signed = signedCoreRead()
        val corrupt = coreRead(activityBytes = ByteArray(68)).copy(certificates = signed.certificates, coreSignatures = signed.coreSignatures)
        rejected<IllegalArgumentException> { pipeline.process(corrupt, "signature-wrong-original") }
        val cert = CardCertificateFile(CardCertificateKind.SIGNING, signed.generation, certificateFixture(role = 1))
        rejected<IllegalArgumentException> { pipeline.process(signed.copy(certificates = mapOf(CardCertificateKind.SIGNING to
            CardCertificateRead(cert))), "signature-wrong-certificate") }
        assertEquals(0, count("card_import_runs")); assertEquals(0, count("card_core_signature_receipts"))
    }

    @Test fun sameSessionForeignKeysRejectDetachedSourcesAndCorruptionIsDetectedOnLoad() = runBlocking {
        imports.bind(card(), true)
        val old = pipeline.process(coreRead(), "signature-fk-old")
        val run = pipeline.process(signedCoreRead(), "signature-fk-new")
        val row = db.cardCoreSignatureDao().receipts(run.id).first()
        rejected<android.database.sqlite.SQLiteException> {
            db.cardCoreSignatureDao().insert(row.copy(sessionId = old.id))
        }
        rejected<android.database.sqlite.SQLiteException> {
            db.cardCoreSignatureDao().insert(row.copy(kind = "CERTIFICATE_CA"))
        }
        db.openHelper.writableDatabase.execSQL("UPDATE card_core_signature_receipts SET bytes = zeroblob(64) WHERE sessionId = ?", arrayOf(run.id))
        rejected<IllegalStateException> { imports.get(run.id) }
    }

    @Test fun signatureStoreFailureRollsBackSourcesNormalizationAndSessionAtomically() = runBlocking {
        imports.bind(card(), true)
        val old = pipeline.process(coreRead(), "signature-rollback-old")
        db.openHelper.writableDatabase.execSQL("""CREATE TRIGGER reject_signature BEFORE INSERT ON card_core_signature_receipts
            WHEN NEW.kind = 'ORIGINAL_EF_0505' BEGIN SELECT RAISE(ABORT, 'synthetic signature rollback'); END""")
        val run = pipeline.process(signedCoreRead(), "signature-rollback")
        assertEquals("STORAGE_FAILED", run.failureCode); assertNull(run.sessionId)
        assertEquals(0, count("card_core_signature_receipts")); assertEquals(1, count("import_sessions"))
        assertEquals(2, count("card_metadata_imports")); assertEquals(1, count("driver_activity_days"))
        assertTrue(imports.get(old.id)!!.coreSignatures.isEmpty())
    }

    @Test fun encryptedMigrationNineteenToTwentyOneKeepsAllOldSourcesUnsigned() = runBlocking {
        imports.bind(card(), true)
        val old = pipeline.process(coreRead(), "signature-upgrade-old")
        val profile = imports.profile(); val request = db.cardImportRunDao().run(old.id)
        db.openHelper.writableDatabase.apply {
            execSQL("DROP TABLE card_identity_signature_receipts")
            execSQL("DROP TABLE card_core_signature_receipts")
            execSQL("DROP INDEX index_driver_import_metadata_sources_sessionId_driverCardId_kind_sourceId")
            execSQL("DELETE FROM room_master_table"); version = 19
        }
        db.close(); open()
        assertEquals(21, db.openHelper.readableDatabase.version)
        assertEquals(profile, imports.profile()); assertEquals(request, db.cardImportRunDao().run(old.id))
        assertTrue(imports.get(old.id)!!.coreSignatures.isEmpty())
        assertEquals(2, imports.get(old.id)!!.coreSnapshots.size)
        assertNotNull(pipeline.process(signedCoreRead(), "signature-upgrade-new").sessionId)
    }

    @Test fun coreOriginalsPersistPointersUnusedTailAndSeparateProjectionsAcrossReopen() = runBlocking {
        imports.bind(card(), true)
        val input = coreRead()
        val run = pipeline.process(input, "core-exact")
        assertEquals(CardImportRunStatus.SAVED, run.status); assertEquals(4, run.newSourceCount)
        assertEquals(CardImportFileStatus.STORED, run.file(CardImportFileKind.ACTIVITY_ORIGINAL).status)
        assertEquals(CardImportFileStatus.STORED, run.file(CardImportFileKind.VEHICLES_ORIGINAL).status)
        val before = imports.get(run.id)!!
        assertEquals(1, before.activity!!.days.size); assertEquals(1, before.vehicles!!.uses.size)
        assertEquals(2, before.coreSnapshots.size)
        for (kind in CardCoreEf.entries) assertArrayEquals(input.coreReads.getValue(kind).snapshot!!.bytes,
            before.coreSnapshots.getValue(kind).snapshot.bytes)
        before.coreSnapshots.getValue(CardCoreEf.ACTIVITY).snapshot.bytes.fill(1)
        db.close(); open()
        for (kind in CardCoreEf.entries) assertArrayEquals(input.coreReads.getValue(kind).snapshot!!.bytes,
            imports.get(run.id)!!.coreSnapshots.getValue(kind).snapshot.bytes)
        assertEquals(run, pipeline.history().single())
    }

    @Test fun bothUnusedFilesKeepOriginalsWithoutInventingAnyActivityOrVehicle() = runBlocking {
        imports.bind(card(), true)
        val vehicles = ByteArray(50).also { it[17] = 1; it[18] = 32 }
        val run = pipeline.process(coreRead(ByteArray(68), vehicles), "core-unused")
        assertEquals(CardImportRunStatus.SAVED, run.status); assertEquals(2, run.newSourceCount)
        assertEquals(CardImportFileStatus.EMPTY, run.file(CardImportFileKind.ACTIVITIES).status)
        assertEquals(CardImportFileStatus.EMPTY, run.file(CardImportFileKind.VEHICLES).status)
        val saved = imports.get(run.id)!!
        assertNull(saved.activity); assertNull(saved.vehicles); assertNull(saved.pair)
        assertArrayEquals(vehicles, saved.coreSnapshots.getValue(CardCoreEf.VEHICLES).snapshot.bytes)
        assertEquals(0, count("driver_activity_days")); assertEquals(0, count("driver_vehicle_uses"))
        assertEquals(0, pipeline.process(coreRead(ByteArray(68), vehicles), "core-unused-again").newSourceCount)
    }

    @Test fun coreParseFailureRetainsCompleteRawBytesWithoutUsableProjectionOrBorrowedHistory() = runBlocking {
        imports.bind(card(), true); pipeline.process(coreRead(), "core-valid-old")
        val pointer = ByteArray(68).also { it[0] = -1; it[1] = -1 }
        val record = ByteArray(50).also { it[15] = 1 } // nonempty timing prefix, invalid record
        val run = pipeline.process(coreRead(pointer, record), "core-invalid")
        assertEquals(CardImportRunStatus.PARTIAL, run.status)
        assertEquals(CardImportFileStatus.INVALID, run.file(CardImportFileKind.ACTIVITIES).status)
        assertEquals(CardImportFileStatus.INVALID, run.file(CardImportFileKind.VEHICLES).status)
        assertEquals(CardImportFileStatus.STORED, run.file(CardImportFileKind.ACTIVITY_ORIGINAL).status)
        assertEquals(CardImportFileStatus.STORED, run.file(CardImportFileKind.VEHICLES_ORIGINAL).status)
        val saved = imports.get(run.id)!!
        assertNull(saved.activity); assertNull(saved.vehicles); assertNull(saved.pair)
        assertArrayEquals(pointer, saved.coreSnapshots.getValue(CardCoreEf.ACTIVITY).snapshot.bytes)
        assertArrayEquals(record, saved.coreSnapshots.getValue(CardCoreEf.VEHICLES).snapshot.bytes)
        assertEquals(1, count("driver_activity_days")); assertEquals(1, count("driver_vehicle_uses"))
    }

    @Test fun unknownUnusedVehiclePointerKeepsOriginalButIsNotReportedAsValidEmpty() = runBlocking {
        imports.bind(card(), true)
        val bytes = ByteArray(98).also { it[1] = 1 }
        val run = pipeline.process(coreRead(ByteArray(68), bytes), "core-unknown-unused-pointer")
        assertEquals(CardImportFileStatus.INVALID, run.file(CardImportFileKind.VEHICLES).status)
        assertEquals(CardImportFileStatus.STORED, run.file(CardImportFileKind.VEHICLES_ORIGINAL).status)
        assertArrayEquals(bytes, imports.get(run.id)!!.coreSnapshots.getValue(CardCoreEf.VEHICLES).snapshot.bytes)
        assertNull(imports.get(run.id)!!.vehicles)
    }

    @Test fun coreReceiptsDistinguishCapacityAbsenceAndFailedAttemptWithoutRawBackfill() = runBlocking {
        imports.bind(card(), true); pipeline.process(coreRead(), "core-before-failures")
        for (capture in listOf(CardCoreRead(CardCoreEf.VEHICLES, CardCoreReadStatus.NOT_ATTEMPTED),
            CardCoreRead(CardCoreEf.VEHICLES, CardCoreReadStatus.ABSENT),
            CardCoreRead(CardCoreEf.VEHICLES, CardCoreReadStatus.READ_FAILED, failure = ReaderFailure.TIMEOUT))) {
            val input = coreRead().copy(coreReads = coreRead().coreReads + (CardCoreEf.VEHICLES to capture))
            val run = pipeline.process(input, "core-${capture.status}")
            assertEquals(capture.result().status, run.file(CardImportFileKind.VEHICLES_ORIGINAL).status)
            assertEquals(capture.result().failureCode, run.file(CardImportFileKind.VEHICLES_ORIGINAL).failureCode)
            assertEquals(capture.result().status, run.file(CardImportFileKind.VEHICLES).status)
            val saved = imports.get(run.id)!!
            assertNotNull(saved.activity); assertNull(saved.vehicles)
            assertEquals(setOf(CardCoreEf.ACTIVITY), saved.coreSnapshots.keys)
        }
    }

    @Test fun newCoreReadsNeverReconstructOlderSourcesOrChangeTheirRequestFingerprints() = runBlocking {
        imports.bind(card(), true)
        val oldInput = read()
        val old = pipeline.process(oldInput, "core-legacy")
        val fingerprint = db.cardImportRunDao().run(old.id)!!.requestFingerprint
        val next = pipeline.process(coreRead(), "core-new")
        assertEquals(2, next.newSourceCount)
        assertTrue(imports.get(old.id)!!.coreSnapshots.isEmpty())
        assertEquals(fingerprint, db.cardImportRunDao().run(old.id)!!.requestFingerprint)
        assertEquals(old, pipeline.process(oldInput, old.id))
        rejected<IllegalArgumentException> { pipeline.process(coreRead(), old.id) }
        assertEquals(old.files, pipeline.history().single { it.id == old.id }.files)
        db.close(); open()
        assertTrue(imports.get(old.id)!!.coreSnapshots.isEmpty())
        assertEquals(2, imports.get(next.id)!!.coreSnapshots.size)
    }

    @Test fun unusedCoreByteChangesAreIndependentSourcesAndCannotReuseExistingRequest() = runBlocking {
        imports.bind(card(), true)
        val padded = coreRead().coreReads.getValue(CardCoreEf.ACTIVITY).snapshot!!.bytes.copyOf(68)
        val firstInput = coreRead(activityBytes = padded)
        val first = pipeline.process(firstInput, "core-tail-first")
        val bytes = firstInput.coreReads.getValue(CardCoreEf.ACTIVITY).snapshot!!.bytes.also { it[67] = 42 }
        val nextInput = coreRead(activityBytes = bytes)
        rejected<IllegalArgumentException> { pipeline.process(nextInput, first.id) }
        val next = pipeline.process(nextInput, "core-tail-next")
        assertEquals(2, next.newSourceCount) // original and legacy ring projection; normalized day is unchanged
        assertEquals(1, count("driver_activity_days")); assertEquals(1, count("driver_vehicle_uses"))
        assertEquals(0, imports.get(first.id)!!.coreSnapshots.getValue(CardCoreEf.ACTIVITY).snapshot.bytes[67].toInt())
        assertEquals(42, imports.get(next.id)!!.coreSnapshots.getValue(CardCoreEf.ACTIVITY).snapshot.bytes[67].toInt())
    }

    @Test fun failingCoreStorageRollsBackOriginalsProjectionsAndSessionTogether() = runBlocking {
        imports.bind(card(), true)
        db.openHelper.writableDatabase.execSQL("""CREATE TRIGGER reject_core BEFORE INSERT ON card_metadata_imports
            WHEN NEW.kind = 'ORIGINAL_EF_0505' BEGIN SELECT RAISE(ABORT, 'synthetic core rollback'); END""")
        val run = pipeline.process(coreRead(), "core-rollback")
        assertEquals(CardImportRunStatus.FAILED, run.status); assertEquals("STORAGE_FAILED", run.failureCode)
        assertNull(run.sessionId); assertNull(imports.get(run.id))
        assertTrue(run.files.all { it.status == CardImportFileStatus.NOT_STORED })
        for (table in listOf("card_metadata_imports", "driver_import_metadata_sources", "import_sessions",
            "card_activity_imports", "card_vehicle_imports", "driver_activity_days", "driver_vehicle_uses"))
            assertEquals(table, 0, count(table))
    }

    @Test fun coreCardKindForeignKeysAndOriginalFingerprintRejectWrongOrCorruptSources() = runBlocking {
        imports.bind(card(), true)
        val old = pipeline.process(read(), "core-fk-old")
        val fresh = pipeline.process(coreRead(), "core-fk-current")
        val activity = imports.get(fresh.id)!!.coreSnapshots.getValue(CardCoreEf.ACTIVITY)
        rejected<Exception> { db.cardMetadataDao().insertSource(DriverImportMetadataSourceEntity(old.id,
            CardCoreEf.VEHICLES.storageKind, activity.driverCardId, activity.id)) }
        rejected<Exception> { db.cardMetadataDao().insertSource(DriverImportMetadataSourceEntity(old.id,
            CardCoreEf.ACTIVITY.storageKind, "wrong-card", activity.id)) }
        val corrupt = activity.snapshot.bytes.also { it[it.lastIndex] = 42 }
        db.openHelper.writableDatabase.execSQL("UPDATE card_metadata_imports SET bytes = ? WHERE id = ?", arrayOf(corrupt, activity.id))
        rejected<Exception> { imports.get(fresh.id) }
        assertTrue(imports.get(old.id)!!.coreSnapshots.isEmpty())
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
            execSQL("DROP TABLE driver_import_metadata_sources"); execSQL("DROP TABLE card_metadata_imports")
            execSQL("DROP TABLE driver_import_vehicle_units_sources"); execSQL("DROP TABLE card_vehicle_units_imports")
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
            execSQL("DROP TABLE driver_import_metadata_sources"); execSQL("DROP TABLE card_metadata_imports")
            execSQL("DROP TABLE driver_import_vehicle_units_sources"); execSQL("DROP TABLE card_vehicle_units_imports")
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
        val replacement = pipeline.process(typeRead().copy(card = card("valid-other-type-card")), "types-other-card")
        val other = imports.get(replacement.id)!!.loadType!!
        db.cardLoadTypeDao().insertSource(DriverImportLoadTypeSourceEntity(old.id, other.driverCardId, other.id))
        assertEquals(3, pipeline.history().size)
        rejected<IllegalStateException> { imports.get(old.id) }
    }

    private fun unitRead(time: Int = 1790000030): CardReadResult {
        val bytes = ByteArray(2002)
        ByteBuffer.wrap(bytes).putShort(0).putInt(time).put(253.toByte()).put(254.toByte()).put("A.01".toByteArray())
        return typeRead().copy(vehicleUnits = CardVehicleUnitsFile(CardGeneration.GEN2V2, 200, bytes))
    }

    @Test fun unitsPersistExactOwnCardSessionAcrossEncryptedReopenWithoutHistoricalFallback() = runBlocking {
        imports.bind(card(), true)
        val old = pipeline.process(typeRead(), "before-units")
        val fresh = pipeline.process(unitRead(), "with-units")
        assertEquals(1, fresh.newSourceCount); assertEquals(14, fresh.files.size)
        val source = imports.get(fresh.id)!!.vehicleUnits!!
        assertEquals(fresh.driverCardId, source.driverCardId)
        assertEquals(1790000030L, source.records.single().useBeginRaw)
        db.close(); open()
        assertArrayEquals(source.file.bytes, imports.get(fresh.id)!!.vehicleUnits!!.file.bytes)
        assertNull(imports.get(old.id)!!.vehicleUnits)
        assertEquals(old, pipeline.process(typeRead(), old.id))
    }
    @Test fun unitsDeduplicateButChangedRequestsAndPhysicalReplacementCardsStayDistinct() = runBlocking {
        imports.bind(card(), true)
        val first = pipeline.process(unitRead(), "units-first")
        assertEquals(first, pipeline.process(unitRead(), first.id))
        assertEquals(0, pipeline.process(unitRead(), "units-repeat").newSourceCount)
        assertEquals(1, count("card_vehicle_units_imports"))
        rejected<IllegalArgumentException> { pipeline.process(unitRead(1790000035), first.id) }
        rejected<IllegalArgumentException> { pipeline.process(typeRead(), first.id) }
        val changed = pipeline.process(unitRead(1790000035), "units-changed")
        assertEquals(1, changed.newSourceCount)
        val replacement = pipeline.process(unitRead().copy(card = card("replacement-units")), "units-replacement")
        assertEquals(14, replacement.newSourceCount)
        assertNotEquals(first.driverCardId, replacement.driverCardId)
        assertNotEquals(imports.get(first.id)!!.vehicleUnits!!.id, imports.get(replacement.id)!!.vehicleUnits!!.id)
        assertEquals(1790000030L, imports.get(first.id)!!.vehicleUnits!!.records.single().useBeginRaw)
    }
    @Test fun unitFileFailuresAndInvalidPointerAreIndependentAndValidEmptySourceCanStandAlone() = runBlocking {
        imports.bind(card(), true)
        val partial = pipeline.process(unitRead().copy(vehicleUnits = null, vehicleUnitsReadFailed = true), "units-partial")
        assertEquals(CardImportRunStatus.PARTIAL, partial.status)
        assertEquals(CardImportFileStatus.READ_FAILED, partial.file(CardImportFileKind.VEHICLE_UNITS_USED).status)
        assertNotNull(imports.get(partial.id)!!.loadUnload)
        val invalidBytes = ByteArray(2002).also { it[1] = 200.toByte() }
        val invalid = pipeline.process(unitRead().copy(vehicleUnits = CardVehicleUnitsFile(CardGeneration.GEN2V2, 200, invalidBytes)), "units-invalid")
        assertEquals(CardImportFileStatus.INVALID, invalid.file(CardImportFileKind.VEHICLE_UNITS_USED).status)
        assertNull(imports.get(invalid.id)!!.vehicleUnits); assertNotNull(imports.get(invalid.id)!!.loadUnload)
        val only = read().copy(activity = null, vehicles = null,
            vehicleUnits = CardVehicleUnitsFile(CardGeneration.GEN2V2, 200, ByteArray(2002)))
        val empty = pipeline.process(only, "units-empty")
        assertNotNull(empty.sessionId); assertEquals(1, empty.newSourceCount)
        assertEquals(CardImportFileStatus.STORED, empty.file(CardImportFileKind.VEHICLE_UNITS_USED).status)
        assertEquals(0, empty.file(CardImportFileKind.VEHICLE_UNITS_USED).decodedCount)
        assertTrue(imports.get(empty.id)!!.vehicleUnits!!.records.isEmpty())
    }
    @Test fun unitsForeignUnconfirmedAndUnclearCardsCannotCreatePersistentSources() = runBlocking {
        rejected<CardBindingRequired> { pipeline.process(unitRead(), "units-unconfirmed") }
        imports.bind(card(), true)
        val foreign = card("foreign-units", DriverIdentity("BETA", "OTHER", LocalDate.of(1980, 1, 1), "en"))
        rejected<ForeignCardRejected> { pipeline.process(unitRead().copy(card = foreign), "units-foreign") }
        rejected<CardBindingRequired> { pipeline.process(unitRead().copy(card = card("unclear-units", card().holder.copy(birthDate = null))), "units-unclear") }
        assertEquals(0, count("card_vehicle_units_imports")); assertEquals(0, count("driver_import_vehicle_units_sources"))
        assertEquals(0, count("card_import_runs"))
    }
    @Test fun unitSourceWriteFailureRollsBackTheEntireOwnImport() = runBlocking {
        imports.bind(card(), true)
        db.openHelper.writableDatabase.execSQL("CREATE TRIGGER fixture_unit_fail BEFORE INSERT ON card_vehicle_units_imports BEGIN SELECT RAISE(ABORT, 'fixture'); END")
        val failed = pipeline.process(unitRead(), "units-rollback")
        assertEquals(CardImportRunStatus.FAILED, failed.status); assertNull(failed.sessionId)
        for (table in listOf("card_vehicle_units_imports", "driver_import_vehicle_units_sources",
            "card_load_unload_imports", "driver_import_load_unload_sources", "card_load_type_imports", "driver_import_load_type_sources",
            "card_place_imports", "driver_import_place_sources", "card_gnss_imports", "driver_import_gnss_sources",
            "import_sessions", "driver_import_sources", "card_activity_imports", "card_vehicle_imports", "card_source_pairs",
            "card_control_imports", "driver_import_control_sources", "card_event_fault_imports", "driver_import_event_fault_sources",
            "card_specific_condition_imports", "driver_import_specific_condition_sources", "card_border_crossing_imports", "driver_import_border_crossing_sources"))
            assertEquals(0, count(table))
    }
    @Test fun schemaSixteenUpgradeKeepsOldReceiptsAndSourcesWithoutVehicleUnitsBackfill() = runBlocking {
        imports.bind(card(), true)
        val old = pipeline.process(typeRead(), "schema-sixteen-units")
        val row = db.cardImportRunDao().run(old.id); val files = db.cardImportRunDao().files(old.id)
        val bytes = imports.get(old.id)!!.places!!.file.bytes
        val typeBytes = imports.get(old.id)!!.loadType!!.file.bytes
        db.openHelper.writableDatabase.apply {
            execSQL("DROP TABLE driver_import_metadata_sources"); execSQL("DROP TABLE card_metadata_imports")
            execSQL("DROP TABLE driver_import_vehicle_units_sources"); execSQL("DROP TABLE card_vehicle_units_imports")
            execSQL("DELETE FROM room_master_table"); version = 16
        }
        db.close(); open()
        assertEquals(AppDatabase.SCHEMA_VERSION, db.openHelper.readableDatabase.version)
        assertEquals(row, db.cardImportRunDao().run(old.id)); assertEquals(files, db.cardImportRunDao().files(old.id))
        assertArrayEquals(bytes, imports.get(old.id)!!.places!!.file.bytes)
        assertArrayEquals(typeBytes, imports.get(old.id)!!.loadType!!.file.bytes)
        assertNull(imports.get(old.id)!!.vehicleUnits); assertEquals(old, pipeline.process(typeRead(), old.id))
        assertEquals(0, count("card_vehicle_units_imports"))
        assertEquals(1, pipeline.process(unitRead(), "units-after-upgrade").newSourceCount)
    }
    @Test fun unitWrongCardLinksAndAlteredBytesCannotLeakIntoAnotherSession() = runBlocking {
        imports.bind(card(), true)
        val old = pipeline.process(typeRead(), "units-safe-old")
        val fresh = pipeline.process(unitRead(), "units-corrupt-new")
        val source = imports.get(fresh.id)!!.vehicleUnits!!
        rejected<android.database.sqlite.SQLiteException> {
            db.cardVehicleUnitsDao().insertSource(DriverImportVehicleUnitsSourceEntity(old.id, "wrong-card", source.id))
        }
        val altered = source.file.bytes.also { it[3] = 7 }
        db.openHelper.writableDatabase.execSQL("UPDATE card_vehicle_units_imports SET bytes = ? WHERE id = ?", arrayOf(altered, source.id))
        assertEquals(2, pipeline.history().size); assertNull(imports.get(old.id)!!.vehicleUnits)
        rejected<IllegalStateException> { imports.get(fresh.id) }
        val replacement = pipeline.process(unitRead().copy(card = card("valid-other-unit-card")), "units-other-card")
        val other = imports.get(replacement.id)!!.vehicleUnits!!
        db.cardVehicleUnitsDao().insertSource(DriverImportVehicleUnitsSourceEntity(old.id, other.driverCardId, other.id))
        assertEquals(3, pipeline.history().size)
        rejected<IllegalStateException> { imports.get(old.id) }
    }

    @Test fun vehicleUnitCoverageKeepsAnUnknownBeginningWithoutBorrowingOtherSourceTimes() = runBlocking {
        imports.bind(card(), true)
        val result = pipeline.process(unitRead(0), "unit-unknown-time")
        val receipt = result.file(CardImportFileKind.VEHICLE_UNITS_USED)
        assertEquals(CardImportFileStatus.STORED, receipt.status); assertEquals(1, receipt.decodedCount)
        assertNull(receipt.firstUtcEpochSeconds); assertNull(receipt.lastUtcEpochSeconds)
        val row = imports.get(result.id)!!.vehicleUnits!!.records.single()
        assertNull(row.useBeginUtc); assertEquals(253, row.manufacturerCodeRaw); assertEquals("A.01", row.softwareVersionText)
        assertNotNull(imports.get(result.id)!!.loadType!!.records.single().entryTimeUtc)
    }

    @Test fun generationTwoVehicleUnitsPersistTheirOwnMinimumCapacityAndRawVersion() = runBlocking {
        val smartCard = card().copy(generation = CardGeneration.GEN2)
        imports.bind(smartCard, true)
        val bytes = ByteArray(842).also { ByteBuffer.wrap(it).putShort(0).putInt(1790000030)
            .put(253.toByte()).put(254.toByte()).put("A.01".toByteArray()) }
        val read = CardReadResult(CardRecognition.DRIVER, CardGeneration.GEN2, smartCard,
            vehicleUnits = CardVehicleUnitsFile(CardGeneration.GEN2, 84, bytes))
        val result = pipeline.process(read, "gen2-units-only")
        assertEquals(1, result.newSourceCount)
        assertEquals(CardImportFileStatus.STORED, result.file(CardImportFileKind.VEHICLE_UNITS_USED).status)
        db.close(); open()
        val source = imports.get(result.id)!!.vehicleUnits!!
        assertEquals(CardGeneration.GEN2, source.file.generation); assertEquals(84, source.file.recordCount)
        assertArrayEquals(bytes, source.file.bytes); assertEquals("A.01", source.records.single().softwareVersionText)
    }

    private fun metadataRead(download: Int = 1790000200): CardReadResult {
        val generation = CardGeneration.GEN2V2
        val licence = ByteArray(53).also {
            it[0] = 1; "TEST AUTHORITY".toByteArray().copyInto(it, 1); it[36] = 13
            "TEST-LICENCE-01".toByteArray().copyInto(it, 37)
        }
        val current = ByteArray(19).also {
            ByteBuffer.wrap(it).putInt(1790000100).put(13.toByte()).put(1.toByte())
            "TEST OWN".toByteArray().copyInto(it, 6)
        }
        return unitRead().copy(metadata = mapOf(
            CardMetadataKind.DRIVING_LICENCE to CardMetadataRead(CardMetadataFile(CardMetadataKind.DRIVING_LICENCE, generation, licence)),
            CardMetadataKind.CURRENT_USAGE to CardMetadataRead(CardMetadataFile(CardMetadataKind.CURRENT_USAGE, generation, current)),
            CardMetadataKind.LAST_CARD_DOWNLOAD to CardMetadataRead(CardMetadataFile(CardMetadataKind.LAST_CARD_DOWNLOAD, generation,
                ByteBuffer.allocate(4).putInt(download).array()))))
    }

    @Test fun metadataPersistsExactOwnCardSessionAcrossEncryptedReopenWithoutHistoricalFallback() = runBlocking {
        imports.bind(card(), true)
        val old = pipeline.process(unitRead(), "before-metadata")
        val fresh = pipeline.process(metadataRead(), "with-metadata")
        assertEquals(3, fresh.newSourceCount); assertEquals(17, fresh.files.size)
        val sources = imports.get(fresh.id)!!.metadata
        assertEquals(CardMetadataKind.entries.filter { !it.inMasterFile }.toSet(), sources.keys)
        assertTrue(sources.values.all { it.driverCardId == fresh.driverCardId })
        db.close(); open()
        for ((kind, source) in sources) assertArrayEquals(source.file.bytes, imports.get(fresh.id)!!.metadata.getValue(kind).file.bytes)
        assertTrue(imports.get(old.id)!!.metadata.isEmpty())
        assertEquals(old, pipeline.process(unitRead(), old.id)); assertEquals(14, pipeline.history().first { it.id == old.id }.files.size)
    }
    @Test fun metadataDeduplicatesAndMapOrderIsStableWhileChangedRequestsAndBytesStayDistinct() = runBlocking {
        imports.bind(card(), true)
        val first = pipeline.process(metadataRead(), "metadata-first")
        assertEquals(first, pipeline.process(metadataRead().copy(metadata = metadataRead().metadata.entries.reversed().associate { it.toPair() }), first.id))
        assertEquals(0, pipeline.process(metadataRead(), "metadata-repeat").newSourceCount)
        assertEquals(3, count("card_metadata_imports"))
        rejected<IllegalArgumentException> { pipeline.process(metadataRead(1790000210), first.id) }
        rejected<IllegalArgumentException> { pipeline.process(unitRead(), first.id) }
        assertEquals(1, pipeline.process(metadataRead(1790000210), "metadata-changed").newSourceCount)
        assertEquals(1790000200L, (imports.get(first.id)!!.metadata.getValue(CardMetadataKind.LAST_CARD_DOWNLOAD).record as CardDownloadRecord).lastDownloadTimeRaw)
    }
    @Test fun eachMetadataFailureAndInvalidKeyPreservesTheOtherSourcesAndValidEmptyFiles() = runBlocking {
        imports.bind(card(), true)
        for (kind in CardMetadataKind.entries.filter { !it.inMasterFile }) {
            val values = metadataRead().metadata.toMutableMap()
            values[kind] = CardMetadataRead(readFailed = true)
            val partial = pipeline.process(metadataRead().copy(metadata = values), "metadata-failed-${kind.name}")
            assertEquals(CardImportRunStatus.PARTIAL, partial.status)
            assertEquals(CardImportFileStatus.READ_FAILED, partial.file(kind.importFileKind()).status)
            assertFalse(imports.get(partial.id)!!.metadata.containsKey(kind)); assertNotNull(imports.get(partial.id)!!.vehicleUnits)
            assertEquals(2, imports.get(partial.id)!!.metadata.size)
        }
        val wrong = metadataRead().metadata.toMutableMap().also {
            it[CardMetadataKind.DRIVING_LICENCE] = it.getValue(CardMetadataKind.CURRENT_USAGE)
        }
        val invalid = pipeline.process(metadataRead().copy(metadata = wrong), "metadata-invalid-key")
        assertEquals(CardImportFileStatus.INVALID, invalid.file(CardImportFileKind.DRIVING_LICENCE_INFO).status)
        assertEquals(2, imports.get(invalid.id)!!.metadata.size)
        val empty = metadataRead().copy(activity = null, vehicles = null, vehicleUnits = null,
            metadata = CardMetadataKind.entries.filter { !it.inMasterFile }.associateWith { CardMetadataRead(CardMetadataFile(it, CardGeneration.GEN2V2, ByteArray(it.fileLength))) },
            controls = null, events = null, faults = null, specificConditions = null, gnssPositions = null,
            gnssAuthentication = null, places = null, placesAuthentication = null, borderCrossings = null, loadUnload = null, loadType = null)
        val result = pipeline.process(empty, "metadata-empty-only")
        assertNotNull(result.sessionId); assertEquals(3, result.newSourceCount)
        for (kind in CardMetadataKind.entries.filter { !it.inMasterFile }) {
            assertEquals(CardImportFileStatus.STORED, result.file(kind.importFileKind()).status)
            assertEquals(0, result.file(kind.importFileKind()).decodedCount)
            assertNull(imports.get(result.id)!!.metadata.getValue(kind).record)
        }
    }
    @Test fun metadataForeignUnconfirmedAndUnclearCardsCannotWriteAnything() = runBlocking {
        rejected<CardBindingRequired> { pipeline.process(metadataRead(), "metadata-unconfirmed") }
        imports.bind(card(), true)
        val foreign = card("foreign-metadata", DriverIdentity("BETA", "OTHER", LocalDate.of(1980, 1, 1), "en"))
        rejected<ForeignCardRejected> { pipeline.process(metadataRead().copy(card = foreign), "metadata-foreign") }
        rejected<CardBindingRequired> { pipeline.process(metadataRead().copy(card = card("unclear-metadata", card().holder.copy(birthDate = null))), "metadata-unclear") }
        assertEquals(0, count("card_metadata_imports")); assertEquals(0, count("driver_import_metadata_sources"))
        assertEquals(0, count("card_import_runs"))
    }
    @Test fun metadataPhysicalReplacementWithSameNumberAndBytesHasItsOwnSources() = runBlocking {
        imports.bind(card(), true)
        val only = CardReadResult(CardRecognition.DRIVER, card().generation, card(), metadata = metadataRead().metadata)
        val first = pipeline.process(only, "metadata-original")
        val physical = card().copy(card = card().card.copy(issued = Instant.ofEpochSecond(1)))
        val next = pipeline.process(only.copy(card = physical), "metadata-physical-replacement")
        assertNotEquals(first.driverCardId, next.driverCardId); assertEquals(3, next.newSourceCount)
        for (kind in CardMetadataKind.entries.filter { !it.inMasterFile }) assertNotEquals(imports.get(first.id)!!.metadata.getValue(kind).id,
            imports.get(next.id)!!.metadata.getValue(kind).id)
        assertEquals(6, count("card_metadata_imports")); assertEquals(6, count("driver_import_metadata_sources"))
    }
    @Test fun metadataStorageFailureRollsBackEverySourceAndSessionTogether() = runBlocking {
        imports.bind(card(), true)
        db.openHelper.writableDatabase.execSQL("CREATE TRIGGER fixture_metadata_fail BEFORE INSERT ON card_metadata_imports WHEN NEW.kind = 'LAST_CARD_DOWNLOAD' BEGIN SELECT RAISE(ABORT, 'fixture'); END")
        val result = pipeline.process(metadataRead(), "metadata-rollback")
        assertEquals(CardImportRunStatus.FAILED, result.status); assertNull(result.sessionId)
        for (table in listOf("card_metadata_imports", "driver_import_metadata_sources", "card_vehicle_units_imports", "driver_import_vehicle_units_sources",
            "card_load_type_imports", "driver_import_load_type_sources", "card_load_unload_imports", "driver_import_load_unload_sources",
            "card_place_imports", "driver_import_place_sources", "card_gnss_imports", "driver_import_gnss_sources",
            "import_sessions", "driver_import_sources", "card_activity_imports", "card_vehicle_imports", "card_source_pairs",
            "card_control_imports", "driver_import_control_sources", "card_event_fault_imports", "driver_import_event_fault_sources",
            "card_specific_condition_imports", "driver_import_specific_condition_sources", "card_border_crossing_imports", "driver_import_border_crossing_sources"))
            assertEquals(0, count(table))
        assertTrue(result.files.filter { it.status == CardImportFileStatus.NOT_STORED }.isNotEmpty())
    }
    @Test fun schemaSeventeenUpgradeRetainsOriginalBytesAndReceiptsWithoutMetadataBackfill() = runBlocking {
        imports.bind(card(), true)
        val old = pipeline.process(unitRead(), "schema-seventeen-metadata")
        val row = db.cardImportRunDao().run(old.id); val files = db.cardImportRunDao().files(old.id)
        val bytes = imports.get(old.id)!!.vehicleUnits!!.file.bytes
        val licenceAbsent = imports.get(old.id)!!.metadata
        db.openHelper.writableDatabase.apply {
            execSQL("DROP TABLE driver_import_metadata_sources"); execSQL("DROP TABLE card_metadata_imports")
            execSQL("DELETE FROM room_master_table"); version = 17
        }
        db.close(); open()
        assertEquals(AppDatabase.SCHEMA_VERSION, db.openHelper.readableDatabase.version)
        assertEquals(row, db.cardImportRunDao().run(old.id)); assertEquals(files, db.cardImportRunDao().files(old.id))
        assertArrayEquals(bytes, imports.get(old.id)!!.vehicleUnits!!.file.bytes)
        assertEquals(licenceAbsent, imports.get(old.id)!!.metadata); assertEquals(old, pipeline.process(unitRead(), old.id))
        assertEquals(0, count("card_metadata_imports")); assertEquals(3, pipeline.process(metadataRead(), "metadata-after-upgrade").newSourceCount)
    }
    @Test fun metadataWrongKindAndCardLinksAndCorruptBytesCannotLeakIntoOtherSessions() = runBlocking {
        imports.bind(card(), true)
        val old = pipeline.process(unitRead(), "metadata-safe-old")
        val fresh = pipeline.process(metadataRead(), "metadata-corrupt-new")
        val source = imports.get(fresh.id)!!.metadata.getValue(CardMetadataKind.DRIVING_LICENCE)
        rejected<android.database.sqlite.SQLiteException> {
            db.cardMetadataDao().insertSource(DriverImportMetadataSourceEntity(old.id, CardMetadataKind.DRIVING_LICENCE.name, "wrong-card", source.id))
        }
        rejected<android.database.sqlite.SQLiteException> {
            db.cardMetadataDao().insertSource(DriverImportMetadataSourceEntity(old.id, CardMetadataKind.CURRENT_USAGE.name, source.driverCardId, source.id))
        }
        val altered = source.file.bytes.also { it[37] = 88 }
        db.openHelper.writableDatabase.execSQL("UPDATE card_metadata_imports SET bytes = ? WHERE id = ?", arrayOf(altered, source.id))
        assertTrue(imports.get(old.id)!!.metadata.isEmpty()); rejected<IllegalStateException> { imports.get(fresh.id) }
        val replacement = pipeline.process(metadataRead().copy(card = card("valid-other-metadata")), "metadata-other-card")
        val other = imports.get(replacement.id)!!.metadata.getValue(CardMetadataKind.DRIVING_LICENCE)
        db.cardMetadataDao().insertSource(DriverImportMetadataSourceEntity(old.id, CardMetadataKind.DRIVING_LICENCE.name, other.driverCardId, other.id))
        assertEquals(3, pipeline.history().size); rejected<IllegalStateException> { imports.get(old.id) }
    }
    @Test fun metadataTimesRemainUnknownAndDoNotBorrowActivityOrImportTimes() = runBlocking {
        imports.bind(card(), true)
        val values = metadataRead().metadata.toMutableMap()
        values[CardMetadataKind.CURRENT_USAGE] = CardMetadataRead(CardMetadataFile(CardMetadataKind.CURRENT_USAGE, CardGeneration.GEN2V2,
            values.getValue(CardMetadataKind.CURRENT_USAGE).file!!.bytes.also { ByteBuffer.wrap(it).putInt(0) }))
        val result = pipeline.process(metadataRead(0).copy(metadata = values), "metadata-unknown-time")
        val licence = result.file(CardImportFileKind.DRIVING_LICENCE_INFO)
        val usage = result.file(CardImportFileKind.CURRENT_USAGE)
        assertEquals(1, licence.decodedCount); assertNull(licence.firstUtcEpochSeconds); assertNull(licence.lastUtcEpochSeconds)
        assertEquals(1, usage.decodedCount); assertNull(usage.firstUtcEpochSeconds); assertNull(usage.lastUtcEpochSeconds)
        assertNotNull(imports.get(result.id)!!.activity)
        assertEquals(1790000200L, result.file(CardImportFileKind.LAST_CARD_DOWNLOAD).firstUtcEpochSeconds)
    }
    @Test fun eachFixedMetadataKindCanBeTheOnlySourceOnGenerationOneAndTwo() = runBlocking {
        for (generation in listOf(CardGeneration.GEN1, CardGeneration.GEN2)) {
            val own = card("metadata-${generation.name}").copy(generation = generation)
            imports.bind(own, true)
            for (kind in CardMetadataKind.entries.filter { !it.inMasterFile }) {
                val file = CardMetadataFile(kind, generation, metadataRead().metadata.getValue(kind).file!!.bytes)
                val result = pipeline.process(CardReadResult(CardRecognition.DRIVER, generation, own,
                    metadata = mapOf(kind to CardMetadataRead(file))), "metadata-only-${generation.name}-${kind.name}")
                assertEquals(1, result.newSourceCount); assertEquals(CardImportFileStatus.STORED, result.file(kind.importFileKind()).status)
                assertEquals(setOf(kind), imports.get(result.id)!!.metadata.keys)
            }
        }
        db.close(); open()
        assertEquals(6, pipeline.history().size); assertEquals(6, count("card_metadata_imports"))
    }

    private fun masterRead(serial: Int = 0x80000001.toInt()): CardReadResult {
        val icc = ByteBuffer.allocate(25).put(253.toByte()).putInt(serial).put(byteArrayOf(0x10, 0x26))
            .put(254.toByte()).put(253.toByte()).put("AB-01234".toByteArray()).put(252.toByte())
            .put("DE".toByteArray()).put(251.toByte()).put(byteArrayOf(255.toByte(), 128.toByte(), 0, 127)).array()
        val ic = byteArrayOf(0, 127, 128.toByte(), 255.toByte(), 222.toByte(), 173.toByte(), 190.toByte(), 239.toByte())
        return metadataRead().copy(metadata = metadataRead().metadata + mapOf(
            CardMetadataKind.ICC_IDENTIFICATION to CardMetadataRead(CardMetadataFile(CardMetadataKind.ICC_IDENTIFICATION, CardGeneration.GEN2V2, icc)),
            CardMetadataKind.IC_IDENTIFICATION to CardMetadataRead(CardMetadataFile(CardMetadataKind.IC_IDENTIFICATION, CardGeneration.GEN2V2, ic))))
    }
    @Test fun masterSourcesPersistExactOwnCardSessionWithoutBackfillOrFallback() = runBlocking {
        imports.bind(card(), true)
        val old = pipeline.process(metadataRead(), "before-master")
        val oldRow = db.cardImportRunDao().run(old.id); val oldFiles = db.cardImportRunDao().files(old.id)
        val fresh = pipeline.process(masterRead(), "with-master")
        assertEquals(2, fresh.newSourceCount); assertEquals(19, fresh.files.size)
        val sources = imports.get(fresh.id)!!.metadata
        assertEquals(CardMetadataKind.entries.toSet(), sources.keys)
        assertEquals(0x80000001L, (sources.getValue(CardMetadataKind.ICC_IDENTIFICATION).record as CardIccIdentificationRecord).serialNumberRaw)
        assertEquals("00 7F 80 FF DE AD BE EF", (sources.getValue(CardMetadataKind.IC_IDENTIFICATION).record as CardChipIdentificationRecord).identifierHex)
        db.close(); open()
        for (kind in CardMetadataKind.entries.filter { it.inMasterFile }) assertArrayEquals(sources.getValue(kind).file.bytes,
            imports.get(fresh.id)!!.metadata.getValue(kind).file.bytes)
        assertEquals(oldRow, db.cardImportRunDao().run(old.id)); assertEquals(oldFiles, db.cardImportRunDao().files(old.id))
        assertEquals(3, imports.get(old.id)!!.metadata.size); assertEquals(old, pipeline.process(metadataRead(), old.id))
        assertEquals(AppDatabase.SCHEMA_VERSION, db.openHelper.readableDatabase.version)
    }
    @Test fun masterSourcesDeduplicateAndChangedRequestsCannotReuseTheirId() = runBlocking {
        imports.bind(card(), true)
        val first = pipeline.process(masterRead(), "master-first")
        assertEquals(first, pipeline.process(masterRead(), first.id))
        assertEquals(0, pipeline.process(masterRead(), "master-repeat").newSourceCount)
        assertEquals(5, count("card_metadata_imports"))
        rejected<IllegalArgumentException> { pipeline.process(masterRead(4), first.id) }
        rejected<IllegalArgumentException> { pipeline.process(metadataRead(), first.id) }
        assertEquals(1, pipeline.process(masterRead(4), "master-changed").newSourceCount)
        assertEquals(0x80000001L, (imports.get(first.id)!!.metadata.getValue(CardMetadataKind.ICC_IDENTIFICATION).record as CardIccIdentificationRecord).serialNumberRaw)
    }
    @Test fun failedOrWrongGenerationMasterFilePreservesEveryOtherSourceAndValidEmpty() = runBlocking {
        imports.bind(card(), true)
        for (kind in CardMetadataKind.entries.filter { it.inMasterFile }) {
            val values = masterRead().metadata.toMutableMap(); values[kind] = CardMetadataRead(readFailed = true)
            val partial = pipeline.process(masterRead().copy(metadata = values), "master-failed-${kind.name}")
            assertEquals(CardImportFileStatus.READ_FAILED, partial.file(kind.importFileKind()).status)
            assertEquals(4, imports.get(partial.id)!!.metadata.size); assertNotNull(imports.get(partial.id)!!.vehicleUnits)
            values[kind] = CardMetadataRead(CardMetadataFile(kind, CardGeneration.GEN1, masterRead().metadata.getValue(kind).file!!.bytes))
            val invalid = pipeline.process(masterRead().copy(metadata = values), "master-wrong-generation-${kind.name}")
            assertEquals(CardImportFileStatus.INVALID, invalid.file(kind.importFileKind()).status)
            assertEquals(4, imports.get(invalid.id)!!.metadata.size)
        }
        val empty = CardReadResult(CardRecognition.DRIVER, card().generation, card(),
            metadata = CardMetadataKind.entries.filter { it.inMasterFile }.associateWith {
                CardMetadataRead(CardMetadataFile(it, CardGeneration.GEN2V2, ByteArray(it.fileLength))) })
        val result = pipeline.process(empty, "master-empty-only")
        assertNotNull(result.sessionId); assertEquals(2, result.newSourceCount)
        for (kind in CardMetadataKind.entries.filter { it.inMasterFile }) {
            assertEquals(CardImportFileStatus.STORED, result.file(kind.importFileKind()).status)
            assertEquals(0, result.file(kind.importFileKind()).decodedCount)
            assertNull(imports.get(result.id)!!.metadata.getValue(kind).record)
        }
    }
    @Test fun masterForeignUnconfirmedAndUnclearCardsHaveNoPersistentSources() = runBlocking {
        rejected<CardBindingRequired> { pipeline.process(masterRead(), "master-unconfirmed") }
        imports.bind(card(), true)
        val foreign = card("foreign-master", DriverIdentity("BETA", "OTHER", LocalDate.of(1980, 1, 1), "en"))
        rejected<ForeignCardRejected> { pipeline.process(masterRead().copy(card = foreign), "master-foreign") }
        rejected<CardBindingRequired> { pipeline.process(masterRead().copy(card = card("unclear-master", card().holder.copy(birthDate = null))), "master-unclear") }
        assertEquals(0, count("card_metadata_imports")); assertEquals(0, count("driver_import_metadata_sources"))
        assertEquals(0, count("card_import_runs"))
    }
    @Test fun sameNumberPhysicalReplacementKeepsDistinctMasterProvenanceWithoutChangingBindingRules() = runBlocking {
        imports.bind(card(), true)
        val only = CardReadResult(CardRecognition.DRIVER, card().generation, card(), metadata = masterRead().metadata.filterKeys { it.inMasterFile })
        val first = pipeline.process(only, "master-original")
        val physical = card().copy(card = card().card.copy(issued = Instant.ofEpochSecond(1)))
        val next = pipeline.process(only.copy(card = physical), "master-physical-replacement")
        assertNotEquals(first.driverCardId, next.driverCardId); assertEquals(2, next.newSourceCount)
        for (kind in CardMetadataKind.entries.filter { it.inMasterFile }) {
            val old = imports.get(first.id)!!.metadata.getValue(kind); val fresh = imports.get(next.id)!!.metadata.getValue(kind)
            assertNotEquals(old.id, fresh.id); assertArrayEquals(old.file.bytes, fresh.file.bytes)
        }
        assertEquals(2, count("driver_cards")); assertEquals(4, count("card_metadata_imports"))
    }
    @Test fun masterSourceFailureRollsBackAllNineteenSourcesAndSession() = runBlocking {
        imports.bind(card(), true)
        db.openHelper.writableDatabase.execSQL("CREATE TRIGGER fixture_master_fail BEFORE INSERT ON card_metadata_imports WHEN NEW.kind = 'IC_IDENTIFICATION' BEGIN SELECT RAISE(ABORT, 'fixture'); END")
        val result = pipeline.process(masterRead(), "master-rollback")
        assertEquals(CardImportRunStatus.FAILED, result.status); assertNull(result.sessionId)
        for (table in listOf("card_metadata_imports", "driver_import_metadata_sources", "card_vehicle_units_imports", "driver_import_vehicle_units_sources",
            "card_load_type_imports", "driver_import_load_type_sources", "card_load_unload_imports", "driver_import_load_unload_sources",
            "card_place_imports", "driver_import_place_sources", "card_gnss_imports", "driver_import_gnss_sources",
            "import_sessions", "driver_import_sources", "card_activity_imports", "card_vehicle_imports", "card_source_pairs",
            "card_control_imports", "driver_import_control_sources", "card_event_fault_imports", "driver_import_event_fault_sources",
            "card_specific_condition_imports", "driver_import_specific_condition_sources", "card_border_crossing_imports", "driver_import_border_crossing_sources"))
            assertEquals(0, count(table))
        assertEquals(19, result.files.size)
    }
    @Test fun wrongCardAndKindLinksAndTamperedMasterBytesCannotLeakIntoOtherSessions() = runBlocking {
        imports.bind(card(), true)
        val old = pipeline.process(metadataRead(), "master-safe-old")
        val fresh = pipeline.process(masterRead(), "master-corrupt-new")
        val source = imports.get(fresh.id)!!.metadata.getValue(CardMetadataKind.ICC_IDENTIFICATION)
        rejected<android.database.sqlite.SQLiteException> {
            db.cardMetadataDao().insertSource(DriverImportMetadataSourceEntity(old.id, CardMetadataKind.ICC_IDENTIFICATION.name, "wrong-card", source.id))
        }
        rejected<android.database.sqlite.SQLiteException> {
            db.cardMetadataDao().insertSource(DriverImportMetadataSourceEntity(old.id, CardMetadataKind.IC_IDENTIFICATION.name, source.driverCardId, source.id))
        }
        val altered = source.file.bytes.also { it[1] = 1 }
        db.openHelper.writableDatabase.execSQL("UPDATE card_metadata_imports SET bytes = ? WHERE id = ?", arrayOf(altered, source.id))
        assertEquals(3, imports.get(old.id)!!.metadata.size); rejected<IllegalStateException> { imports.get(fresh.id) }
        val replacement = pipeline.process(masterRead().copy(card = card("valid-other-master")), "master-other-card")
        val other = imports.get(replacement.id)!!.metadata.getValue(CardMetadataKind.ICC_IDENTIFICATION)
        db.cardMetadataDao().insertSource(DriverImportMetadataSourceEntity(old.id, CardMetadataKind.ICC_IDENTIFICATION.name, other.driverCardId, other.id))
        assertEquals(3, pipeline.history().size); rejected<IllegalStateException> { imports.get(old.id) }
    }
    @Test fun masterIdentityHasNoTimeCoverageAndDoesNotBorrowAnotherSourceTime() = runBlocking {
        imports.bind(card(), true)
        val result = pipeline.process(masterRead(), "master-no-time")
        for (kind in CardMetadataKind.entries.filter { it.inMasterFile }) {
            val receipt = result.file(kind.importFileKind())
            assertEquals(1, receipt.decodedCount); assertNull(receipt.firstUtcEpochSeconds); assertNull(receipt.lastUtcEpochSeconds)
            assertNull(imports.get(result.id)!!.metadata.getValue(kind).record!!.timeUtc)
        }
        assertNotNull(result.file(CardImportFileKind.LAST_CARD_DOWNLOAD).firstUtcEpochSeconds)
        assertNotNull(imports.get(result.id)!!.activity)
    }
    @Test fun eachMasterFileCanBeTheOnlySourceInAllSupportedGenerations() = runBlocking {
        for (generation in listOf(CardGeneration.GEN1, CardGeneration.GEN2, CardGeneration.GEN2V2)) {
            val own = card("master-${generation.name}").copy(generation = generation)
            imports.bind(own, true)
            for (kind in CardMetadataKind.entries.filter { it.inMasterFile }) {
                val file = CardMetadataFile(kind, generation, masterRead().metadata.getValue(kind).file!!.bytes)
                val result = pipeline.process(CardReadResult(CardRecognition.DRIVER, generation, own,
                    metadata = mapOf(kind to CardMetadataRead(file))), "master-only-${generation.name}-${kind.name}")
                assertEquals(1, result.newSourceCount); assertEquals(setOf(kind), imports.get(result.id)!!.metadata.keys)
                assertEquals(CardImportFileStatus.STORED, result.file(kind.importFileKind()).status)
            }
        }
        db.close(); open(); assertEquals(6, pipeline.history().size); assertEquals(6, count("card_metadata_imports"))
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

    private fun originalIdentityBytes(own: DriverCard = card()): ByteArray {
        val result = ByteBuffer.allocate(143).put(own.card.issuingState.toByte())
            .put(own.card.number.padEnd(16).toByteArray(Charsets.ISO_8859_1))
        fun name(value: String) { result.put(1); result.put(value.padEnd(35).toByteArray(Charsets.ISO_8859_1)) }
        name(own.card.issuingAuthority)
        result.putInt(own.card.issued.epochSecond.toInt()).putInt(own.card.validFrom.epochSecond.toInt()).putInt(own.card.expires.epochSecond.toInt())
        name(own.holder.surname); name(own.holder.givenNames)
        result.put(own.holder.birthDate?.toString()?.replace("-", "")?.chunked(2)?.map { it.toInt(16).toByte() }?.toByteArray() ?: ByteArray(4))
        result.put(own.holder.language.toByteArray()); return result.array()
    }
    private fun originalApplicationBytes(generation: CardGeneration = CardGeneration.GEN2V2, vehicles: Int = 2): ByteArray {
        val size = if (generation == CardGeneration.GEN1) 10 else 17
        return ByteBuffer.allocate(size).put(1).put(1).put(if (generation == CardGeneration.GEN2V2) 1.toByte() else 0.toByte())
            .put(12).put(24).putShort(16).putShort(vehicles.toShort()).apply {
                if (generation == CardGeneration.GEN1) put(112) else putShort(112).putShort(336).putShort(112).putShort(200)
            }.array()
    }
    private fun originalSources(own: DriverCard = card(), vehicles: Int = 2) = CardIdentitySourceKind.entries
        .filter { it.supports(own.generation) }.associateWith { kind ->
            val bytes = when (kind) {
                CardIdentitySourceKind.APPLICATION -> originalApplicationBytes(own.generation, vehicles)
                CardIdentitySourceKind.IDENTIFICATION -> originalIdentityBytes(own)
                CardIdentitySourceKind.APPLICATION_V2 -> ByteBuffer.allocate(10).putShort(8).putShort(112).putShort(336).putShort(84).putShort(0).array()
            }
            CardIdentitySourceRead(CardIdentitySourceFile(kind, own.generation, bytes))
        }
    private fun originalRead(own: DriverCard = card(), vehicles: Int = 2) = certificateRead().copy(card = own,
        identitySources = originalSources(own, vehicles))

    @Test fun completeOriginalSourcesExtendTheNewRunTo26WithoutBackfilling19Or23SourceHistory() = runBlocking {
        imports.bind(card(), true)
        val old = pipeline.process(masterRead(), "old-19-originals")
        val cert = pipeline.process(certificateRead(), "old-23-originals")
        val full = pipeline.process(originalRead(), "new-26-originals")
        assertEquals(19, old.files.size); assertEquals(23, cert.files.size); assertEquals(26, full.files.size)
        assertEquals(3, full.newSourceCount); assertTrue(full.files.all { it.status == CardImportFileStatus.STORED })
        assertTrue(imports.get(old.id)!!.identitySources.isEmpty()); assertTrue(imports.get(cert.id)!!.identitySources.isEmpty())
        val sources = imports.get(full.id)!!.identitySources
        assertEquals(CardIdentitySourceKind.entries.toSet(), sources.keys)
        for (kind in CardIdentitySourceKind.entries) assertArrayEquals(originalRead().identitySources.getValue(kind).file!!.bytes, sources.getValue(kind).file.bytes)
        assertEquals(12, count("card_metadata_imports")); assertEquals(5 + 9 + 12, count("driver_import_metadata_sources"))
        assertEquals(old, pipeline.process(masterRead(), old.id)); assertEquals(cert, pipeline.process(certificateRead(), cert.id))
    }
    @Test fun originalRetryAndChangedApplicationPreserveExactBytesAndDeduplicateEachSource() = runBlocking {
        imports.bind(card(), true); val first = pipeline.process(originalRead(), "original-retry")
        assertEquals(26, first.newSourceCount); assertEquals(first, pipeline.process(originalRead(), first.id))
        val same = pipeline.process(originalRead(), "original-unchanged"); assertEquals(0, same.newSourceCount)
        val changed = pipeline.process(originalRead(vehicles = 3), "original-capacity-changed"); assertEquals(1, changed.newSourceCount)
        rejected<IllegalArgumentException> { pipeline.process(originalRead(vehicles = 4), first.id) }
        val before = imports.get(first.id)!!.identitySources; val after = imports.get(changed.id)!!.identitySources
        assertEquals(2, (before.getValue(CardIdentitySourceKind.APPLICATION).record as CardApplicationIdentityRecord).vehicleCapacityRaw)
        assertEquals(3, (after.getValue(CardIdentitySourceKind.APPLICATION).record as CardApplicationIdentityRecord).vehicleCapacityRaw)
        assertEquals(before.getValue(CardIdentitySourceKind.IDENTIFICATION).id, after.getValue(CardIdentitySourceKind.IDENTIFICATION).id)
        assertEquals(13, count("card_metadata_imports")); assertEquals(3, pipeline.history().size)
    }
    @Test fun aBlankCompleteV2SourcePersistsItsOriginalBytesWithZeroDecodedRecords() = runBlocking {
        imports.bind(card(), true)
        for (fill in listOf(0, 255)) {
            val source = CardIdentitySourceFile(CardIdentitySourceKind.APPLICATION_V2, card().generation, ByteArray(10) { fill.toByte() })
            val run = pipeline.process(CardReadResult(CardRecognition.DRIVER, card().generation, card(),
                identitySources = mapOf(source.kind to CardIdentitySourceRead(source))), "blank-original-v2-$fill")
            assertEquals(0, run.file(source.kind.importFileKind()).decodedCount)
            assertEquals(CardImportFileStatus.STORED, run.file(source.kind.importFileKind()).status)
            val saved = imports.get(run.id)!!.identitySources.getValue(source.kind)
            assertNull(saved.record); assertArrayEquals(source.bytes, saved.file.bytes)
        }
        assertEquals(2, count("card_metadata_imports")); assertEquals(0, count("driver_activity_days"))
    }
    @Test fun missingFailedWrongKindGenerationAndMalformedOriginalSourceFailIndependently() = runBlocking {
        imports.bind(card(), true)
        val kind = CardIdentitySourceKind.APPLICATION_V2
        val attempts = listOf(
            CardIdentitySourceRead() to CardImportFileStatus.MISSING,
            CardIdentitySourceRead(readFailed = true) to CardImportFileStatus.READ_FAILED,
            originalSources().getValue(CardIdentitySourceKind.IDENTIFICATION) to CardImportFileStatus.INVALID,
            CardIdentitySourceRead(CardIdentitySourceFile(kind, card().generation, ByteArray(10).also { it[1] = 7 })) to CardImportFileStatus.INVALID,
            CardIdentitySourceRead(CardIdentitySourceFile(CardIdentitySourceKind.APPLICATION, CardGeneration.GEN1,
                originalApplicationBytes(CardGeneration.GEN1))) to CardImportFileStatus.INVALID)
        for ((index, attempt) in attempts.withIndex()) {
            val read = originalRead().copy(identitySources = originalSources() + (kind to attempt.first))
            val run = pipeline.process(read, "original-partial-$index")
            assertEquals(CardImportRunStatus.PARTIAL, run.status); assertEquals(attempt.second, run.file(kind.importFileKind()).status)
            assertEquals(setOf(CardIdentitySourceKind.APPLICATION, CardIdentitySourceKind.IDENTIFICATION), imports.get(run.id)!!.identitySources.keys)
            assertNotNull(imports.get(run.id)!!.activity); assertEquals(4, imports.get(run.id)!!.certificates.size)
        }
    }
    @Test fun originalIdentityFromAnotherHolderCannotBeSavedUnderTheRecognizedOwnCard() = runBlocking {
        imports.bind(card(), true)
        val altered = card().copy(holder = card().holder.copy(surname = "OTHER"))
        val read = originalRead().copy(identitySources = originalSources() +
            (CardIdentitySourceKind.IDENTIFICATION to originalSources(altered).getValue(CardIdentitySourceKind.IDENTIFICATION)))
        val run = pipeline.process(read, "mismatched-original-identity")
        assertEquals(CardImportFileStatus.INVALID, run.file(CardImportFileKind.DRIVER_IDENTIFICATION).status)
        assertFalse(imports.get(run.id)!!.identitySources.containsKey(CardIdentitySourceKind.IDENTIFICATION))
        rejected<IllegalArgumentException> { imports.save(read, "direct-mismatched-identity", CardImportSelection.ALL_AVAILABLE) }
        assertEquals(1, count("import_sessions")); assertEquals(1, count("driver_cards"))
    }
    @Test fun unconfirmedAndForeignOriginalSourcesWriteNoProfileCardRunOrSourceRows() = runBlocking {
        rejected<CardBindingRequired> { pipeline.process(originalRead(), "unconfirmed-originals") }
        for (table in listOf("driver_cards", "card_import_runs", "import_sessions", "card_metadata_imports")) assertEquals(0, count(table))
        imports.bind(card(), true)
        val foreign = card("foreign-original", DriverIdentity("FOREIGN", "OTHER", LocalDate.of(2001, 3, 4), "de"))
        rejected<ForeignCardRejected> { pipeline.process(originalRead(foreign), "foreign-originals") }
        assertEquals(1, count("driver_cards")); assertEquals(0, count("card_metadata_imports")); assertEquals(0, count("card_import_runs"))
    }
    @Test fun sameNumberReplacementKeepsAllOriginalSourcesOnItsOwnPhysicalCard() = runBlocking {
        imports.bind(card(), true); val before = pipeline.process(originalRead(), "original-physical-card")
        val replacement = card().copy(card = card().card.copy(issued = Instant.ofEpochSecond(1)))
        val after = pipeline.process(originalRead(replacement), "replacement-original-card")
        assertNotEquals(before.driverCardId, after.driverCardId)
        for (kind in CardIdentitySourceKind.entries) {
            val old = imports.get(before.id)!!.identitySources.getValue(kind); val next = imports.get(after.id)!!.identitySources.getValue(kind)
            assertNotEquals(old.id, next.id); assertNotEquals(old.driverCardId, next.driverCardId)
        }
        assertEquals(0L, (imports.get(before.id)!!.identitySources.getValue(CardIdentitySourceKind.IDENTIFICATION).record as CardOriginalIdentityRecord).card.card.issued.epochSecond)
        assertEquals(1L, (imports.get(after.id)!!.identitySources.getValue(CardIdentitySourceKind.IDENTIFICATION).record as CardOriginalIdentityRecord).card.card.issued.epochSecond)
    }
    @Test fun receiptFailureRollsBackNewOriginalSourcesTogetherWithTheAcceptedImport() = runBlocking {
        imports.bind(card(), true); val old = pipeline.process(certificateRead(), "before-original-rollback")
        db.openHelper.writableDatabase.execSQL("CREATE TRIGGER reject_original_receipt BEFORE UPDATE ON card_import_runs WHEN NEW.status = 'SAVED' BEGIN SELECT RAISE(ABORT, 'fixture'); END")
        val failed = pipeline.process(originalRead(), "rollback-originals")
        assertEquals(CardImportRunStatus.FAILED, failed.status); assertNull(failed.sessionId)
        assertTrue(failed.files.all { it.status == CardImportFileStatus.NOT_STORED })
        assertEquals(9, count("card_metadata_imports")); assertEquals(9, count("driver_import_metadata_sources"))
        assertEquals(1, count("import_sessions")); assertTrue(imports.get(old.id)!!.identitySources.isEmpty())
    }
    @Test fun encryptedReopeningRestoresOnlySelectedOriginalBytesAndMissingHistoricalSources() = runBlocking {
        imports.bind(card(), true); val old = pipeline.process(certificateRead(), "old-no-originals")
        val first = pipeline.process(originalRead(), "old-original-capacity")
        val latest = pipeline.process(originalRead(vehicles = 3), "new-original-capacity")
        val original = imports.get(first.id)!!.identitySources
        db.close(); assertFalse(context.getDatabasePath(AppDatabase.DATABASE_NAME).readBytes().take(16).toByteArray().contentEquals("SQLite format 3\u0000".toByteArray()))
        open(); assertTrue(imports.get(old.id)!!.identitySources.isEmpty())
        for (kind in CardIdentitySourceKind.entries) assertArrayEquals(original.getValue(kind).file.bytes, imports.get(first.id)!!.identitySources.getValue(kind).file.bytes)
        assertEquals(3, (imports.get(latest.id)!!.identitySources.getValue(CardIdentitySourceKind.APPLICATION).record as CardApplicationIdentityRecord).vehicleCapacityRaw)
        assertEquals(3, pipeline.history().size)
    }
    @Test fun originalForeignKeysRejectWrongKindAndPhysicalCardAndTamperedBytesFailOnlyTheirSession() = runBlocking {
        imports.bind(card(), true); val old = pipeline.process(certificateRead(), "safe-before-original-corruption")
        val fresh = pipeline.process(originalRead(), "original-corrupt-session")
        val source = imports.get(fresh.id)!!.identitySources.getValue(CardIdentitySourceKind.APPLICATION)
        rejected<Exception> { db.cardMetadataDao().insertSource(DriverImportMetadataSourceEntity(old.id, CardIdentitySourceKind.IDENTIFICATION.storageKind, source.driverCardId, source.id)) }
        rejected<Exception> { db.cardMetadataDao().insertSource(DriverImportMetadataSourceEntity(old.id, source.file.kind.storageKind, "wrong-card", source.id)) }
        val changed = source.file.bytes.also { it[8] = 3 }
        db.openHelper.writableDatabase.execSQL("UPDATE card_metadata_imports SET bytes = ? WHERE id = ?", arrayOf(changed, source.id))
        rejected<IllegalStateException> { imports.get(fresh.id) }
        assertTrue(imports.get(old.id)!!.identitySources.isEmpty()); assertEquals(2, pipeline.history().size)
    }
    @Test fun eachSupportedOriginalSourceCanBeTheOnlySourceAcrossAllGenerations() = runBlocking {
        for (generation in listOf(CardGeneration.GEN1, CardGeneration.GEN2, CardGeneration.GEN2V2)) {
            val own = card("id-${generation.name}").copy(generation = generation); imports.bind(own, true)
            for ((kind, file) in originalSources(own)) {
                val run = pipeline.process(CardReadResult(CardRecognition.DRIVER, generation, own, identitySources = mapOf(kind to file)), "only-id-${generation.name}-${kind.name}")
                assertEquals(1, run.newSourceCount); assertEquals(setOf(kind), imports.get(run.id)!!.identitySources.keys)
                assertEquals(CardImportFileStatus.STORED, run.file(kind.importFileKind()).status)
                assertTrue(imports.get(run.id)!!.metadata.isEmpty()); assertTrue(imports.get(run.id)!!.certificates.isEmpty())
            }
        }
        db.close(); open(); assertEquals(7, pipeline.history().size); assertEquals(7, count("card_metadata_imports"))
    }
    @Test fun originalValidityAndCapacityDoNotBecomeActivityCoverageOrImportTimes() = runBlocking {
        imports.bind(card(), true); val run = pipeline.process(originalRead(), "original-no-time-coverage")
        for (kind in CardIdentitySourceKind.entries) {
            val file = run.file(kind.importFileKind()); assertNull(file.firstUtcEpochSeconds); assertNull(file.lastUtcEpochSeconds)
            assertEquals(1, file.decodedCount)
        }
        assertNotNull(run.file(CardImportFileKind.ACTIVITIES).firstUtcEpochSeconds)
    }
    @Test fun directOriginalSaveRetryRequiresTheExactUnionOfAllMixedSources() = runBlocking {
        imports.bind(card(), true); val saved = imports.save(originalRead(), "direct-original", CardImportSelection.ALL_AVAILABLE)
        assertEquals(saved.id, imports.save(originalRead(), saved.id, CardImportSelection.ALL_AVAILABLE).id)
        rejected<IllegalStateException> { imports.save(certificateRead(), saved.id, CardImportSelection.ALL_AVAILABLE) }
        rejected<IllegalStateException> { imports.save(originalRead(vehicles = 3), saved.id, CardImportSelection.ALL_AVAILABLE) }
        assertEquals(1, imports.all().size); assertEquals(3, imports.get(saved.id)!!.identitySources.size)
    }
    @Test fun concurrentIdenticalOriginalImportsShareSourcesButRetainBothExactSessions() = runBlocking {
        imports.bind(card(), true)
        val first = async { pipeline.process(originalRead(), "concurrent-original-1") }
        val second = async { pipeline.process(originalRead(), "concurrent-original-2") }
        val results = listOf(first.await(), second.await())
        assertEquals(listOf(0, 26), results.map { it.newSourceCount }.sorted())
        assertEquals(12, count("card_metadata_imports")); assertEquals(24, count("driver_import_metadata_sources"))
        assertEquals(2, count("import_sessions")); assertTrue(results.all { imports.get(it.id)!!.identitySources.size == 3 })
    }

    private fun directoryBytes() = byteArrayOf(0x61, 8, 0x4F, 6, 0xFF.toByte(), 0x54, 0x41, 0x43, 0x48, 0x4F,
        0x61, 8, 0x4F, 6, 0xFF.toByte(), 0x53, 0x4D, 0x52, 0x44, 0x54)
    private fun directoryRead(own: DriverCard = card(), raw: ByteArray = directoryBytes()) = originalRead(own).copy(
        directory = CardDirectoryFile(own.generation, raw))

    @Test fun masterDirectoryExtendsOnlyNewRunsTo27AndPreservesAllEarlierRequestFingerprints() = runBlocking {
        imports.bind(card(), true)
        val old19 = pipeline.process(masterRead(), "directory-old19")
        val old23 = pipeline.process(certificateRead(), "directory-old23")
        val old26 = pipeline.process(originalRead(), "directory-old26")
        val fresh = pipeline.process(directoryRead(), "directory-new27")
        assertEquals(27, fresh.files.size); assertEquals(1, fresh.newSourceCount)
        assertEquals(CardImportRunStatus.SAVED, fresh.status)
        assertEquals(2, fresh.file(CardImportFileKind.MF_DIRECTORY).decodedCount)
        assertNull(fresh.file(CardImportFileKind.MF_DIRECTORY).firstUtcEpochSeconds)
        assertNull(fresh.file(CardImportFileKind.MF_DIRECTORY).lastUtcEpochSeconds)
        for (old in listOf(old19, old23, old26)) assertNull(imports.get(old.id)!!.directory)
        assertEquals(old19, pipeline.process(masterRead(), old19.id))
        assertEquals(old23, pipeline.process(certificateRead(), old23.id))
        assertEquals(old26, pipeline.process(originalRead(), old26.id))
        assertEquals(13, count("card_metadata_imports")); assertEquals(39, count("driver_import_metadata_sources"))
        val saved = imports.get(fresh.id)!!
        assertArrayEquals(directoryBytes(), saved.directory!!.file.bytes)
        assertEquals(saved.driverCardId, saved.directory!!.driverCardId)
        assertEquals(5, saved.metadata.size); assertEquals(4, saved.certificates.size); assertEquals(3, saved.identitySources.size)
    }
    @Test fun directoryDeduplicationRetainsSeparateSessionsAndRejectsChangedOrOmittedAttempts() = runBlocking {
        imports.bind(card(), true); val first = pipeline.process(directoryRead(), "directory-retry")
        assertEquals(27, first.newSourceCount); assertEquals(first, pipeline.process(directoryRead(), first.id))
        val same = pipeline.process(directoryRead(), "directory-same"); assertEquals(0, same.newSourceCount)
        assertEquals(imports.get(first.id)!!.directory!!.id, imports.get(same.id)!!.directory!!.id)
        rejected<IllegalArgumentException> { pipeline.process(originalRead(), first.id) }
        rejected<IllegalArgumentException> { pipeline.process(directoryRead(raw = ByteArray(20)), first.id) }
        val blank = pipeline.process(directoryRead(raw = ByteArray(20)), "directory-now-blank")
        assertEquals(1, blank.newSourceCount); assertNull(imports.get(blank.id)!!.directory!!.record)
        assertNotNull(imports.get(first.id)!!.directory!!.record); assertEquals(3, pipeline.history().size)
    }
    @Test fun malformedFailedAndWrongGenerationDirectoryLeaveAll26OtherSourcesAvailable() = runBlocking {
        imports.bind(card(), true)
        val attempts = listOf(
            directoryRead().copy(directoryReadFailed = true) to CardImportFileStatus.READ_FAILED,
            originalRead().copy(directoryReadFailed = true) to CardImportFileStatus.READ_FAILED,
            directoryRead(raw = directoryBytes().also { it[3] = 5 }) to CardImportFileStatus.INVALID,
            originalRead().copy(directory = CardDirectoryFile(CardGeneration.GEN2, directoryBytes())) to CardImportFileStatus.INVALID)
        for ((index, attempt) in attempts.withIndex()) {
            val run = pipeline.process(attempt.first, "directory-bad-$index")
            assertEquals(CardImportRunStatus.PARTIAL, run.status)
            assertEquals(attempt.second, run.file(CardImportFileKind.MF_DIRECTORY).status)
            assertEquals(26, run.files.count { it.status == CardImportFileStatus.STORED })
            val saved = imports.get(run.id)!!; assertNull(saved.directory)
            assertEquals(3, saved.identitySources.size); assertEquals(4, saved.certificates.size); assertEquals(5, saved.metadata.size)
        }
        assertEquals(12, count("card_metadata_imports"))
    }
    @Test fun completeBlankDirectoriesRemainSourcesAndHaveNoActivityCoverage() = runBlocking {
        imports.bind(card(), true)
        for (fill in listOf(0, 255)) {
            val raw = ByteArray(20) { fill.toByte() }
            val run = pipeline.process(CardReadResult(CardRecognition.DRIVER, card().generation, card(),
                directory = CardDirectoryFile(card().generation, raw)), "directory-blank-$fill")
            assertEquals(1, run.newSourceCount); assertEquals(0, run.file(CardImportFileKind.MF_DIRECTORY).decodedCount)
            assertEquals(CardImportFileStatus.STORED, run.file(CardImportFileKind.MF_DIRECTORY).status)
            assertNull(run.file(CardImportFileKind.MF_DIRECTORY).firstUtcEpochSeconds)
            val saved = imports.get(run.id)!!; assertNull(saved.directory!!.record)
            assertArrayEquals(raw, saved.directory!!.file.bytes)
            assertTrue(saved.metadata.isEmpty()); assertTrue(saved.certificates.isEmpty()); assertTrue(saved.identitySources.isEmpty())
        }
        assertEquals(2, count("card_metadata_imports")); assertEquals(0, count("driver_activity_days")); assertEquals(0, count("driver_vehicle_uses"))
    }
    @Test fun aDirectoryCanBeTheOnlySavedSourceForEitherSupportedGeneration() = runBlocking {
        for ((index, generation) in listOf(CardGeneration.GEN2, CardGeneration.GEN2V2).withIndex()) {
            val own = card().copy(generation = generation, card = card().card.copy(issued = Instant.ofEpochSecond(index.toLong())))
            imports.bind(own, true)
            val run = pipeline.process(CardReadResult(CardRecognition.DRIVER, generation, own,
                directory = CardDirectoryFile(generation, directoryBytes())), "only-directory-${generation.name}")
            assertEquals(1, run.newSourceCount); assertEquals(3, run.files.size)
            val saved = imports.get(run.id)!!; assertEquals(generation, saved.directory!!.file.generation)
            assertNull(saved.activity); assertNull(saved.vehicles); assertTrue(saved.metadata.isEmpty())
        }
        assertEquals(2, count("card_metadata_imports")); assertEquals(2, count("driver_cards"))
    }
    @Test fun unconfirmedAndForeignDirectoriesNeverCreatePersistentSourcesOrReceipts() = runBlocking {
        rejected<CardBindingRequired> { pipeline.process(directoryRead(), "directory-unconfirmed") }
        assertNull(imports.profile()); assertEquals(0, count("card_metadata_imports")); assertEquals(0, count("card_import_runs"))
        imports.bind(card(), true)
        val foreign = card("foreign-dir", DriverIdentity("FOREIGN", "OTHER", LocalDate.of(2001, 3, 4), "de"))
        rejected<ForeignCardRejected> { pipeline.process(directoryRead(foreign), "directory-foreign") }
        val uncertain = card("uncertain-dir", card().holder.copy(surname = "OTHER"))
        rejected<CardBindingRequired> { pipeline.process(directoryRead(uncertain), "directory-uncertain") }
        assertEquals(1, count("driver_cards")); assertEquals(0, count("import_sessions")); assertEquals(0, count("card_metadata_imports"))
        assertEquals(0, count("card_import_runs"))
    }
    @Test fun sameNumberReplacementKeepsIdenticalDirectoryBytesBoundToItsPhysicalCard() = runBlocking {
        imports.bind(card(), true); val first = pipeline.process(directoryRead(), "directory-physical-old")
        val replacement = card().copy(card = card().card.copy(expires = card().card.expires.plusSeconds(86400)))
        val next = pipeline.process(directoryRead(replacement), "directory-physical-new")
        val before = imports.get(first.id)!!.directory!!; val after = imports.get(next.id)!!.directory!!
        assertNotEquals(first.driverCardId, next.driverCardId); assertNotEquals(before.id, after.id)
        assertArrayEquals(before.file.bytes, after.file.bytes)
        assertEquals(first.driverCardId, before.driverCardId); assertEquals(next.driverCardId, after.driverCardId)
    }
    @Test fun finalReceiptFailureRollsBackTheDirectoryAndAllSessionLinksTogether() = runBlocking {
        imports.bind(card(), true); val old = pipeline.process(originalRead(), "before-directory-rollback")
        db.openHelper.writableDatabase.execSQL("CREATE TRIGGER reject_directory_receipt BEFORE UPDATE ON card_import_runs WHEN NEW.status = 'SAVED' BEGIN SELECT RAISE(ABORT, 'fixture'); END")
        val failed = pipeline.process(directoryRead(), "directory-rollback")
        assertEquals(CardImportRunStatus.FAILED, failed.status); assertNull(failed.sessionId)
        assertTrue(failed.files.all { it.status == CardImportFileStatus.NOT_STORED })
        assertEquals(12, count("card_metadata_imports")); assertEquals(12, count("driver_import_metadata_sources"))
        assertEquals(1, count("import_sessions")); assertNull(imports.get(old.id)!!.directory)
    }
    @Test fun encryptedReopeningRestoresOnlyTheSelectedDirectoryIncludingEmptyAndMissingHistory() = runBlocking {
        imports.bind(card(), true); val old = pipeline.process(originalRead(), "directory-before")
        val first = pipeline.process(directoryRead(), "directory-full")
        val last = pipeline.process(directoryRead(raw = ByteArray(20)), "directory-empty")
        val before = imports.get(first.id)!!.directory!!
        db.close(); assertFalse(context.getDatabasePath(AppDatabase.DATABASE_NAME).readBytes().take(16).toByteArray().contentEquals("SQLite format 3\u0000".toByteArray()))
        open(); assertNull(imports.get(old.id)!!.directory)
        assertEquals(before.id, imports.get(first.id)!!.directory!!.id)
        assertArrayEquals(directoryBytes(), imports.get(first.id)!!.directory!!.file.bytes)
        assertNull(imports.get(last.id)!!.directory!!.record); assertEquals(3, pipeline.history().size)
    }
    @Test fun directoryForeignKeysRejectWrongKindOrPhysicalCardAndTamperingFailsOnlyItsSession() = runBlocking {
        imports.bind(card(), true); val old = pipeline.process(read(), "directory-safe-old")
        val fresh = pipeline.process(directoryRead(), "directory-corruption")
        val source = imports.get(fresh.id)!!.directory!!
        rejected<Exception> { db.cardMetadataDao().insertSource(DriverImportMetadataSourceEntity(old.id, CardMetadataKind.CURRENT_USAGE.name, source.driverCardId, source.id)) }
        rejected<Exception> { db.cardMetadataDao().insertSource(DriverImportMetadataSourceEntity(old.id, CardDirectoryStore.STORAGE_KIND, "wrong-card", source.id)) }
        db.openHelper.writableDatabase.execSQL("UPDATE card_metadata_imports SET bytes = ? WHERE id = ?", arrayOf(directoryBytes().also { it[8] = 0 }, source.id))
        rejected<IllegalStateException> { imports.get(fresh.id) }
        assertNull(imports.get(old.id)!!.directory); assertEquals(2, pipeline.history().size)
    }
    @Test fun directSaveRetryRequiresTheEntireUnionAndManualActivitySelectionDoesNotSaveDirectory() = runBlocking {
        imports.bind(card(), true)
        val saved = imports.save(directoryRead(), "directory-direct", CardImportSelection.ALL_AVAILABLE)
        assertEquals(saved.id, imports.save(directoryRead(), saved.id, CardImportSelection.ALL_AVAILABLE).id)
        rejected<IllegalStateException> { imports.save(originalRead(), saved.id, CardImportSelection.ALL_AVAILABLE) }
        rejected<IllegalStateException> { imports.save(directoryRead(raw = ByteArray(20)), saved.id, CardImportSelection.ALL_AVAILABLE) }
        val onlyActivity = imports.save(directoryRead(), "directory-manual-activity", CardImportSelection.ACTIVITY)
        assertNotNull(onlyActivity.activity); assertNull(onlyActivity.directory); assertEquals(2, imports.all().size)
    }

    private fun certificateFixture(role: Int = 17, generation: CardGeneration = CardGeneration.GEN2V2): ByteArray {
        if (generation == CardGeneration.GEN1) return ByteArray(194) { (it + role).toByte() }
        fun tlv(tag: Int, bytes: ByteArray): ByteArray {
            val prefix = if (tag > 255) byteArrayOf((tag ushr 8).toByte(), tag.toByte()) else byteArrayOf(tag.toByte())
            val size = if (bytes.size < 128) byteArrayOf(bytes.size.toByte()) else byteArrayOf(0x81.toByte(), bytes.size.toByte())
            return prefix + size + bytes
        }
        val oid = byteArrayOf(0x2A, 0x86.toByte(), 0x48, 0xCE.toByte(), 0x3D, 3, 1, 7)
        val body = tlv(0x5F29, byteArrayOf(0)) + tlv(0x42, ByteArray(8) { 1 }) +
            tlv(0x5F4C, DriverCardFiles.gen2Aid + role.toByte()) +
            tlv(0x7F49, tlv(6, oid) + tlv(0x86, ByteArray(65) { if (it == 0) 4 else 7 })) +
            tlv(0x5F20, ByteArray(8) { 9 }) + tlv(0x5F25, ByteBuffer.allocate(4).putInt(1790000000).array()) +
            tlv(0x5F24, ByteBuffer.allocate(4).putInt(1890000000).array())
        return tlv(0x7F21, tlv(0x7F4E, body) + tlv(0x5F37, ByteArray(64) { 3 }))
    }
    private fun certificateRead(role: Int = 17) = masterRead().copy(certificates = CardCertificateKind.entries.associateWith {
        CardCertificateRead(CardCertificateFile(it, CardGeneration.GEN2V2, certificateFixture(role))) })

    @Test fun certificateSourcesPersistExactOwnSessionWithoutChangingNineteenSourceHistory() = runBlocking {
        imports.bind(card(), true)
        val old = pipeline.process(masterRead(), "before-certificates")
        val oldRow = db.cardImportRunDao().run(old.id); val oldFiles = db.cardImportRunDao().files(old.id)
        val fresh = pipeline.process(certificateRead(), "with-certificates")
        assertEquals(CardImportRunStatus.SAVED, fresh.status); assertEquals(23, fresh.files.size); assertEquals(4, fresh.newSourceCount)
        val saved = imports.get(fresh.id)!!
        assertEquals(CardCertificateKind.entries.toSet(), saved.certificates.keys); assertEquals(5, saved.metadata.size)
        for (kind in CardCertificateKind.entries) assertArrayEquals(certificateFixture(), saved.certificates.getValue(kind).file.bytes)
        assertTrue(imports.get(old.id)!!.certificates.isEmpty()); assertEquals(19, pipeline.history().first { it.id == old.id }.files.size)
        assertEquals(oldRow, db.cardImportRunDao().run(old.id)); assertEquals(oldFiles, db.cardImportRunDao().files(old.id))
        assertEquals(old, pipeline.process(masterRead(), old.id)); assertEquals(AppDatabase.SCHEMA_VERSION, db.openHelper.readableDatabase.version)
    }
    @Test fun certificateKindsDeduplicateIndependentlyAndChangedAttemptCannotReuseId() = runBlocking {
        imports.bind(card(), true)
        val first = pipeline.process(certificateRead(), "certificates-first")
        assertEquals(first, pipeline.process(certificateRead(), first.id))
        assertEquals(0, pipeline.process(certificateRead(), "certificates-repeat").newSourceCount)
        assertEquals(9, count("card_metadata_imports"))
        assertEquals(4, imports.get(first.id)!!.certificates.values.map { it.id }.toSet().size)
        rejected<IllegalArgumentException> { pipeline.process(certificateRead(18), first.id) }
        rejected<IllegalArgumentException> { pipeline.process(masterRead(), first.id) }
        val changed = pipeline.process(certificateRead(18), "certificates-changed")
        assertEquals(4, changed.newSourceCount)
        assertEquals(17, (imports.get(first.id)!!.certificates.getValue(CardCertificateKind.CARD).record as CardEccCertificateRecord).equipmentTypeRaw)
        assertEquals(18, (imports.get(changed.id)!!.certificates.getValue(CardCertificateKind.CARD).record as CardEccCertificateRecord).equipmentTypeRaw)
    }
    @Test fun failedMissingWrongKindWrongGenerationAndMalformedCertificateRemainIndependent() = runBlocking {
        imports.bind(card(), true)
        for (kind in CardCertificateKind.entries) for (mode in 0..4) {
            val sources = certificateRead().certificates.toMutableMap()
            sources[kind] = when (mode) {
                0 -> CardCertificateRead(readFailed = true)
                1 -> CardCertificateRead()
                2 -> CardCertificateRead(CardCertificateFile(CardCertificateKind.entries.first { it != kind }, CardGeneration.GEN2V2, certificateFixture()))
                3 -> CardCertificateRead(CardCertificateFile(kind, CardGeneration.GEN2, certificateFixture()))
                else -> CardCertificateRead(CardCertificateFile(kind, CardGeneration.GEN2V2, certificateFixture().also { it[1] = 0x22 }))
            }
            val run = pipeline.process(certificateRead().copy(certificates = sources), "cert-${kind.name}-$mode")
            assertEquals(CardImportRunStatus.PARTIAL, run.status)
            assertEquals(when (mode) { 0 -> CardImportFileStatus.READ_FAILED; 1 -> CardImportFileStatus.MISSING
                else -> CardImportFileStatus.INVALID }, run.file(kind.importFileKind()).status)
            val saved = imports.get(run.id)!!
            assertEquals(3, saved.certificates.size); assertEquals(5, saved.metadata.size)
            assertNotNull(saved.activity); assertNotNull(saved.vehicles); assertNotNull(saved.vehicleUnits)
        }
    }
    @Test fun blankCertificatesAreStoredWithTheirActualLengthAndZeroDecodedRecords() = runBlocking {
        imports.bind(card(), true)
        for (fill in listOf(0, 255)) {
            val sources = CardCertificateKind.entries.associateWith {
                CardCertificateRead(CardCertificateFile(it, CardGeneration.GEN2V2, ByteArray(341) { fill.toByte() })) }
            val run = pipeline.process(certificateRead().copy(certificates = sources), "blank-certs-$fill")
            for (kind in CardCertificateKind.entries) {
                assertEquals(CardImportFileStatus.STORED, run.file(kind.importFileKind()).status)
                assertEquals(0, run.file(kind.importFileKind()).decodedCount)
                val saved = imports.get(run.id)!!.certificates.getValue(kind)
                assertNull(saved.record); assertEquals(341, saved.file.bytes.size)
                assertTrue(saved.file.bytes.all { (it.toInt() and 255) == fill })
            }
        }
    }
    @Test fun unconfirmedAndForeignCertificatesNeverCreatePersistentCardOrSourceRows() = runBlocking {
        rejected<CardBindingRequired> { pipeline.process(certificateRead(), "unconfirmed-certs") }
        assertEquals(0, count("driver_cards")); assertEquals(0, count("card_import_runs")); assertEquals(0, count("card_metadata_imports"))
        imports.bind(card(), true)
        val foreign = card("foreign-certs", DriverIdentity("FOREIGN", "OTHER", LocalDate.of(2001, 3, 4), "de"))
        rejected<ForeignCardRejected> { pipeline.process(certificateRead().copy(card = foreign), "foreign-certs") }
        assertEquals(1, count("driver_cards")); assertEquals(0, count("card_import_runs")); assertEquals(0, count("card_metadata_imports"))
    }
    @Test fun sameNumberReplacementKeepsCertificateBytesBoundToItsPhysicalCard() = runBlocking {
        imports.bind(card(), true)
        val first = pipeline.process(certificateRead(), "original-certs")
        val replacement = card().copy(card = card().card.copy(issued = Instant.ofEpochSecond(1)))
        val next = pipeline.process(certificateRead().copy(card = replacement), "replacement-certs")
        assertNotEquals(first.driverCardId, next.driverCardId)
        for (kind in CardCertificateKind.entries) {
            val before = imports.get(first.id)!!.certificates.getValue(kind); val after = imports.get(next.id)!!.certificates.getValue(kind)
            assertNotEquals(before.id, after.id); assertNotEquals(before.driverCardId, after.driverCardId)
            assertArrayEquals(before.file.bytes, after.file.bytes)
        }
        assertEquals(18, count("card_metadata_imports"))
    }
    @Test fun receiptFailureRollsBackCertificatesAndAllNewSourcesTogether() = runBlocking {
        imports.bind(card(), true); val old = pipeline.process(masterRead(), "old-before-cert-rollback")
        db.openHelper.writableDatabase.execSQL("CREATE TRIGGER reject_certificate_receipt BEFORE UPDATE ON card_import_runs WHEN NEW.status = 'SAVED' BEGIN SELECT RAISE(ABORT, 'fixture'); END")
        val failed = pipeline.process(certificateRead(), "rollback-certificates")
        assertEquals(CardImportRunStatus.FAILED, failed.status); assertNull(failed.sessionId); assertEquals("STORAGE_FAILED", failed.failureCode)
        assertTrue(failed.files.all { it.status == CardImportFileStatus.NOT_STORED })
        assertEquals(5, count("card_metadata_imports")); assertEquals(5, count("driver_import_metadata_sources"))
        assertEquals(1, count("import_sessions")); assertTrue(imports.get(old.id)!!.certificates.isEmpty())
        assertEquals(2, pipeline.history().size)
    }
    @Test fun encryptedReopeningRestoresOnlyTheExactSelectedCertificateSnapshot() = runBlocking {
        imports.bind(card(), true)
        val old = pipeline.process(certificateRead(17), "saved-certs-old")
        val next = pipeline.process(certificateRead(18), "saved-certs-new")
        val before = imports.get(old.id)!!.certificates
        db.close()
        assertFalse(context.getDatabasePath(AppDatabase.DATABASE_NAME).readBytes().take(16).toByteArray()
            .contentEquals("SQLite format 3\u0000".toByteArray()))
        open()
        for (kind in CardCertificateKind.entries) {
            assertArrayEquals(before.getValue(kind).file.bytes, imports.get(old.id)!!.certificates.getValue(kind).file.bytes)
            assertEquals(before.getValue(kind).id, imports.get(old.id)!!.certificates.getValue(kind).id)
            assertEquals(18, (imports.get(next.id)!!.certificates.getValue(kind).record as CardEccCertificateRecord).equipmentTypeRaw)
        }
        assertEquals(2, pipeline.history().size)
    }
    @Test fun certificateForeignKeysRejectWrongKindAndWrongPhysicalCardLinks() = runBlocking {
        imports.bind(card(), true); val old = pipeline.process(masterRead(), "no-certs-link-target")
        val own = pipeline.process(certificateRead(), "own-certs-link-source")
        val source = imports.get(own.id)!!.certificates.getValue(CardCertificateKind.CARD)
        rejected<Exception> { db.cardMetadataDao().insertSource(DriverImportMetadataSourceEntity(old.id,
            CardCertificateKind.CA.storageKind, source.driverCardId, source.id)) }
        rejected<Exception> { db.cardMetadataDao().insertSource(DriverImportMetadataSourceEntity(old.id,
            CardCertificateKind.CARD.storageKind, "wrong-card", source.id)) }
        assertTrue(imports.get(old.id)!!.certificates.isEmpty()); assertEquals(2, pipeline.history().size)
    }
    @Test fun corruptedCertificateBytesRejectThatSessionWithoutHidingOtherHistory() = runBlocking {
        imports.bind(card(), true); val old = pipeline.process(masterRead(), "safe-before-corrupt-cert")
        val fresh = pipeline.process(certificateRead(), "corrupt-cert-session")
        val source = imports.get(fresh.id)!!.certificates.getValue(CardCertificateKind.CARD)
        db.openHelper.writableDatabase.execSQL("UPDATE card_metadata_imports SET bytes = ? WHERE id = ?", arrayOf(ByteArray(204), source.id))
        rejected<IllegalStateException> { imports.get(fresh.id) }
        assertTrue(imports.get(old.id)!!.certificates.isEmpty()); assertEquals(2, pipeline.history().size)
    }
    @Test fun eachSupportedCertificateCanBeTheOnlySavedSourceAcrossAllGenerations() = runBlocking {
        for (generation in listOf(CardGeneration.GEN1, CardGeneration.GEN2, CardGeneration.GEN2V2)) {
            val own = card("cert-${generation.name}").copy(generation = generation); imports.bind(own, true)
            for (kind in CardCertificateKind.entries.filter { it.supports(generation) }) {
                val file = CardCertificateFile(kind, generation, certificateFixture(generation = generation))
                val run = pipeline.process(CardReadResult(CardRecognition.DRIVER, generation, own,
                    certificates = mapOf(kind to CardCertificateRead(file))), "only-cert-${generation.name}-${kind.name}")
                assertEquals(1, run.newSourceCount); assertEquals(setOf(kind), imports.get(run.id)!!.certificates.keys)
                assertEquals(CardImportFileStatus.STORED, run.file(kind.importFileKind()).status)
                assertTrue(imports.get(run.id)!!.metadata.isEmpty())
            }
        }
        db.close(); open(); assertEquals(10, pipeline.history().size); assertEquals(10, count("card_metadata_imports"))
    }
    @Test fun certificatePeriodsDoNotBecomeImportCoverageOrActivityTimes() = runBlocking {
        imports.bind(card(), true); val run = pipeline.process(certificateRead(), "cert-no-coverage-time")
        for (kind in CardCertificateKind.entries) {
            val result = run.file(kind.importFileKind())
            assertNull(result.firstUtcEpochSeconds); assertNull(result.lastUtcEpochSeconds); assertEquals(1, result.decodedCount)
            assertEquals(1790000000L, (imports.get(run.id)!!.certificates.getValue(kind).record as CardEccCertificateRecord).effectiveTimeRaw)
        }
        assertNotNull(run.file(CardImportFileKind.ACTIVITIES).firstUtcEpochSeconds)
    }
    @Test fun directCertificateSaveRetriesVerifyTheEntireExactMixedSourceSelection() = runBlocking {
        imports.bind(card(), true)
        val saved = imports.save(certificateRead(), "direct-certificate", CardImportSelection.ALL_AVAILABLE)
        assertEquals(saved.id, imports.save(certificateRead(), saved.id, CardImportSelection.ALL_AVAILABLE).id)
        rejected<IllegalStateException> { imports.save(masterRead(), saved.id, CardImportSelection.ALL_AVAILABLE) }
        rejected<IllegalStateException> { imports.save(certificateRead(18), saved.id, CardImportSelection.ALL_AVAILABLE) }
        assertEquals(1, imports.all().size); assertEquals(4, imports.get(saved.id)!!.certificates.size)
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
    private fun communicationBytes(kind: CardCommunicationKind): ByteArray = when (kind) {
        CardCommunicationKind.ATR_INFO -> byteArrayOf(0x7F, 0x66, 4, 2, 2, 1, 0)
        CardCommunicationKind.EXTENDED_LENGTH -> byteArrayOf(2, 1, 0x11)
    }
    private fun communicationRead(driver: DriverCard = card()) = directoryRead(driver).copy(
        communication = CardCommunicationKind.entries.associateWith { kind -> CardCommunicationRead(
            CardCommunicationFile(kind, driver.generation, communicationBytes(kind))) })

    @Test fun all29SourcesStayIndependentAndHistorical19To27SourceRequestsStayUnchanged() = runBlocking {
        imports.bind(card(), true)
        val attempts = listOf(masterRead(), certificateRead(), originalRead(), directoryRead())
        val old = attempts.mapIndexed { index, attempt -> pipeline.process(attempt, "communication-old-$index") }
        val fresh = pipeline.process(communicationRead(), "communication-complete")
        assertEquals(CardImportRunStatus.SAVED, fresh.status); assertEquals(29, fresh.files.size)
        assertEquals(29, fresh.files.count { it.status == CardImportFileStatus.STORED }); assertEquals(2, fresh.newSourceCount)
        old.forEachIndexed { index, run ->
            assertTrue(imports.get(run.id)!!.communication.isEmpty())
            assertEquals(run, pipeline.process(attempts[index], run.id))
        }
        val saved = imports.get(fresh.id)!!
        assertEquals(2, saved.communication.size); assertEquals(5, saved.metadata.size)
        assertEquals(4, saved.certificates.size); assertEquals(3, saved.identitySources.size); assertNotNull(saved.directory)
        for (kind in CardCommunicationKind.entries) {
            val source = saved.communication.getValue(kind)
            assertEquals(saved.driverCardId, source.driverCardId); assertArrayEquals(communicationBytes(kind), source.file.bytes)
        }
    }
    @Test fun communicationDeduplicatesPerPhysicalCardWhileSessionAndRetryContentRemainExact() = runBlocking {
        imports.bind(card(), true); val first = pipeline.process(communicationRead(), "communication-first")
        assertEquals(29, first.newSourceCount); assertEquals(first, pipeline.process(communicationRead(), first.id))
        val second = pipeline.process(communicationRead(), "communication-second"); assertEquals(0, second.newSourceCount)
        val before = imports.get(first.id)!!; val after = imports.get(second.id)!!
        assertNotEquals(before.id, after.id)
        assertEquals(before.communication.mapValues { it.value.id }, after.communication.mapValues { it.value.id })
        val changed = communicationRead().copy(communication = mapOf(CardCommunicationKind.EXTENDED_LENGTH to CardCommunicationRead(
            CardCommunicationFile(CardCommunicationKind.EXTENDED_LENGTH, card().generation, byteArrayOf(2, 1, 1)))))
        rejected<IllegalArgumentException> { pipeline.process(changed, first.id) }
        rejected<IllegalArgumentException> { pipeline.process(directoryRead(), first.id) }
        val last = pipeline.process(changed, "communication-changed"); assertEquals(1, last.newSourceCount)
        assertEquals(0x11, (imports.get(first.id)!!.communication.getValue(CardCommunicationKind.EXTENDED_LENGTH).record as CardExtendedLengthRecord).protocolCodeRaw)
        assertEquals(1, (imports.get(last.id)!!.communication.getValue(CardCommunicationKind.EXTENDED_LENGTH).record as CardExtendedLengthRecord).protocolCodeRaw)
        assertEquals(3, pipeline.history().size)
    }
    @Test fun failedInvalidAndWrongGenerationCommunicationNeverDiscardsTheOther28Sources() = runBlocking {
        imports.bind(card(), true)
        for (kind in CardCommunicationKind.entries) {
            val original = communicationRead()
            val bad = communicationBytes(kind).also { it[0] = 3 }
            val cases = listOf(CardCommunicationRead(readFailed = true) to CardImportFileStatus.READ_FAILED,
                original.communication.getValue(kind).copy(readFailed = true) to CardImportFileStatus.READ_FAILED,
                CardCommunicationRead(CardCommunicationFile(kind, card().generation, bad)) to CardImportFileStatus.INVALID,
                CardCommunicationRead(CardCommunicationFile(kind, CardGeneration.GEN2, communicationBytes(kind))) to CardImportFileStatus.INVALID)
            cases.forEachIndexed { index, (value, expected) ->
                val attempt = original.copy(communication = original.communication + (kind to value))
                val run = pipeline.process(attempt, "communication-invalid-${kind.name}-$index")
                assertEquals(CardImportRunStatus.PARTIAL, run.status)
                val fileKind = if (kind == CardCommunicationKind.ATR_INFO) CardImportFileKind.MF_ATR_INFO else CardImportFileKind.MF_EXTENDED_LENGTH
                assertEquals(expected, run.file(fileKind).status); assertEquals(28, run.files.count { it.status == CardImportFileStatus.STORED })
                val saved = imports.get(run.id)!!
                assertFalse(kind in saved.communication); assertEquals(1, saved.communication.size)
                assertNotNull(saved.directory); assertEquals(3, saved.identitySources.size); assertEquals(4, saved.certificates.size)
            }
        }
    }
    @Test fun completeDefaultCommunicationIsStoredWithoutRecordedTimeCoverageOrInventedProtocols() = runBlocking {
        imports.bind(card(), true)
        for (fill in listOf(0, 255)) {
            val attempt = communicationRead().copy(activity = null, vehicles = null,
                communication = CardCommunicationKind.entries.associateWith { kind -> CardCommunicationRead(
                    CardCommunicationFile(kind, card().generation, ByteArray(kind.minimumLength) { fill.toByte() })) })
            val run = pipeline.process(attempt, "communication-empty-$fill")
            for (kind in listOf(CardImportFileKind.MF_ATR_INFO, CardImportFileKind.MF_EXTENDED_LENGTH)) {
                val result = run.file(kind); assertEquals(CardImportFileStatus.STORED, result.status)
                assertEquals(0, result.decodedCount); assertNull(result.firstUtcEpochSeconds); assertNull(result.lastUtcEpochSeconds)
            }
            assertTrue(imports.get(run.id)!!.communication.values.all { it.record == null })
        }
    }
    @Test fun communicationOnlyOwnImportHasNoActivityVehicleOrIdentityTimeCoverage() = runBlocking {
        imports.bind(card(), true)
        val attempt = CardReadResult(CardRecognition.DRIVER, card().generation, card(), communication = communicationRead().communication)
        val run = pipeline.process(attempt, "communication-only")
        assertEquals(CardImportRunStatus.PARTIAL, run.status); assertEquals(2, run.newSourceCount)
        val saved = imports.get(run.id)!!; assertNull(saved.activity); assertNull(saved.vehicles); assertNull(saved.directory)
        assertEquals(2, saved.communication.size)
        assertTrue(run.files.filter { it.status == CardImportFileStatus.STORED }.all { it.firstUtcEpochSeconds == null && it.lastUtcEpochSeconds == null })
    }
    @Test fun communicationTransactionRollbackLeavesNoSourcesSessionOrNormalizedRows() = runBlocking {
        imports.bind(card(), true)
        db.openHelper.writableDatabase.execSQL("CREATE TRIGGER reject_communication BEFORE INSERT ON driver_import_metadata_sources WHEN NEW.kind = 'MASTER_COMMUNICATION_EXTENDED_LENGTH' BEGIN SELECT RAISE(ABORT, 'synthetic'); END")
        val failed = pipeline.process(communicationRead(), "communication-rollback")
        assertEquals(CardImportRunStatus.FAILED, failed.status)
        assertEquals(0, count("card_metadata_imports")); assertEquals(0, count("driver_import_metadata_sources"))
        assertEquals(0, count("import_sessions")); assertEquals(0, count("driver_import_sources"))
        assertEquals(CardImportRunStatus.FAILED, pipeline.history().single().status)
        assertTrue(pipeline.history().single().files.none { it.status == CardImportFileStatus.STORED })
    }
    @Test fun foreignAndUnconfirmedCommunicationImportsWriteNoProfileOrSourceRows() = runBlocking {
        rejected<CardBindingRequired> { pipeline.process(communicationRead(), "communication-unconfirmed") }
        assertNull(imports.profile()); assertEquals(0, count("card_import_runs")); assertEquals(0, count("card_metadata_imports"))
        imports.bind(card(), true)
        val foreign = card("foreign-comm", DriverIdentity("FOREIGN", "OTHER", LocalDate.of(2001, 3, 4), "de"))
        rejected<ForeignCardRejected> { pipeline.process(communicationRead(foreign), "communication-foreign") }
        val uncertain = card("uncertain-comm", card().holder.copy(surname = "OTHER"))
        rejected<CardBindingRequired> { pipeline.process(communicationRead(uncertain), "communication-uncertain") }
        assertEquals(1, count("driver_cards")); assertEquals(0, count("import_sessions")); assertEquals(0, count("card_metadata_imports"))
        assertEquals(0, count("card_import_runs"))
    }
    @Test fun sameNumberPhysicalReplacementGetsIndependentCommunicationFingerprints() = runBlocking {
        imports.bind(card(), true); val first = pipeline.process(communicationRead(), "communication-physical-old")
        val replacement = card().copy(card = card().card.copy(validFrom = card().card.validFrom.plusSeconds(1)))
        val next = pipeline.process(communicationRead(replacement), "communication-physical-next")
        val old = imports.get(first.id)!!; val fresh = imports.get(next.id)!!
        assertNotEquals(old.driverCardId, fresh.driverCardId)
        for (kind in CardCommunicationKind.entries) {
            assertNotEquals(old.communication.getValue(kind).id, fresh.communication.getValue(kind).id)
            assertArrayEquals(old.communication.getValue(kind).file.bytes, fresh.communication.getValue(kind).file.bytes)
        }
    }
    @Test fun encryptedReopeningKeepsOldMissingCurrentAndCompleteEmptyCommunicationSeparate() = runBlocking {
        imports.bind(card(), true); val old = pipeline.process(directoryRead(), "communication-reopen-old")
        val current = pipeline.process(communicationRead(), "communication-reopen-current")
        val empty = pipeline.process(communicationRead().copy(communication = CardCommunicationKind.entries.associateWith { kind ->
            CardCommunicationRead(CardCommunicationFile(kind, card().generation, ByteArray(kind.minimumLength))) }), "communication-reopen-empty")
        val before = imports.get(current.id)!!.communication.mapValues { it.value.id }
        db.close(); assertFalse(context.getDatabasePath(AppDatabase.DATABASE_NAME).readBytes().take(16).toByteArray().contentEquals("SQLite format 3\u0000".toByteArray()))
        open(); assertTrue(imports.get(old.id)!!.communication.isEmpty())
        assertEquals(before, imports.get(current.id)!!.communication.mapValues { it.value.id })
        assertTrue(imports.get(empty.id)!!.communication.values.all { it.record == null })
        assertEquals(3, pipeline.history().size)
    }
    @Test fun communicationCombinedKindAndCardForeignKeysAreNotMaskedByDuplicateKeys() = runBlocking {
        imports.bind(card(), true); val old = pipeline.process(read(), "communication-fk-old")
        val fresh = pipeline.process(communicationRead(), "communication-fk-fresh")
        val source = imports.get(fresh.id)!!.communication.getValue(CardCommunicationKind.EXTENDED_LENGTH)
        rejected<Exception> { db.cardMetadataDao().insertSource(DriverImportMetadataSourceEntity(old.id, CardCommunicationKind.ATR_INFO.storageKind, source.driverCardId, source.id)) }
        rejected<Exception> { db.cardMetadataDao().insertSource(DriverImportMetadataSourceEntity(old.id, source.file.kind.storageKind, "wrong-card", source.id)) }
        assertEquals(0, db.cardMetadataDao().sources(old.id).size)
        assertEquals(2, imports.get(fresh.id)!!.communication.size)
    }
    @Test fun corruptedCommunicationOnlyBreaksItsExactSessionAndNeverFallsBackToNewerSource() = runBlocking {
        imports.bind(card(), true); val old = pipeline.process(directoryRead(), "communication-corrupt-old")
        val fresh = pipeline.process(communicationRead(), "communication-corrupt-source")
        val source = imports.get(fresh.id)!!.communication.getValue(CardCommunicationKind.EXTENDED_LENGTH)
        db.openHelper.writableDatabase.execSQL("UPDATE card_metadata_imports SET bytes = ? WHERE id = ?", arrayOf(byteArrayOf(2, 1, 1), source.id))
        rejected<Exception> { imports.get(fresh.id) }
        assertTrue(imports.get(old.id)!!.communication.isEmpty())
    }

}
