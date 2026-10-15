package de.fahrerabrechnung.data

import android.content.Context
import androidx.room.withTransaction
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import de.fahrerabrechnung.data.card.*
import de.fahrerabrechnung.data.db.*
import de.fahrerabrechnung.data.security.EncryptedDatabaseFactory
import java.io.ByteArrayOutputStream
import java.security.SecureRandom
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import kotlinx.coroutines.runBlocking
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CardReadBatchInstrumentedTest {
    private lateinit var context: Context
    private lateinit var db: AppDatabase
    private lateinit var key: ByteArray
    private lateinit var imports: DriverCardImportRepository
    private lateinit var pipeline: DriverCardImportPipeline
    private val clock = Clock.fixed(Instant.parse("2026-10-12T07:00:00Z"), ZoneOffset.UTC)
    @Before fun before() {
        context = ApplicationProvider.getApplicationContext()
        context.deleteDatabase(AppDatabase.DATABASE_NAME)
        key = ByteArray(32).also(SecureRandom()::nextBytes); open()
    }
    private fun open() {
        db = EncryptedDatabaseFactory.create(context, key)
        val activities = CardActivityImportRepository(db.cardActivityImportDao(), clock)
        val vehicles = CardVehicleImportRepository(db.cardVehicleImportDao(), clock)
        val pairs = CardSourcePairRepository(db, activities, vehicles, clock)
        imports = DriverCardImportRepository(db, activities, vehicles, pairs, clock)
        pipeline = DriverCardImportPipeline(db, imports, activities, vehicles, clock)
    }
    @After fun after() { db.close(); context.deleteDatabase(AppDatabase.DATABASE_NAME) }
    private fun identity(number: String = "TESTCARD12345678", surname: String = "MUSTER",
                         birthDate: ByteArray = byteArrayOf(0x20, 0, 0x02, 0x29)) = ByteArrayOutputStream().apply {
        fun name(value: String) { write(1); write(value.padEnd(35).toByteArray()) }
        write(1); write(number.toByteArray()); name("TEST AUTHORITY")
        write(byteArrayOf(0x65, 0x53, 0xF1.toByte(), 0)); write(byteArrayOf(0x65, 0x53, 0xF1.toByte(), 0))
        write(byteArrayOf(0x75, 0x53, 0xF1.toByte(), 0)); name(surname); name("ERIKA")
        write(birthDate); write("DE".toByteArray())
    }.toByteArray()
    private fun read(generation: CardGeneration, raw: ByteArray = identity()): CardReadResult {
        val card = DriverCardDecoder.identity(raw, generation)
        val application = ByteArray(if (generation == CardGeneration.GEN1) 10 else 15).also {
            it[0] = 1; it[1] = 1; if (generation == CardGeneration.GEN2V2) it[2] = 1
        }
        return CardReadResult(CardRecognition.DRIVER, generation, card,
            identitySources = mapOf(
                CardIdentitySourceKind.IDENTIFICATION to CardIdentitySourceRead(CardIdentitySourceFile(CardIdentitySourceKind.IDENTIFICATION, generation, raw), false),
                CardIdentitySourceKind.APPLICATION to CardIdentitySourceRead(CardIdentitySourceFile(CardIdentitySourceKind.APPLICATION, generation, application), false)))
    }
    private fun batch(second: CardApplicationAttempt = CardApplicationAttempt.read(CardApplication.GEN2, read(CardGeneration.GEN2)),
                      master: ByteArray = ByteArray(8)) = CardApplicationReadBatch(listOf(
        CardApplicationAttempt.read(CardApplication.GEN1, read(CardGeneration.GEN1)), second),
        listOf(CardMasterFileCapture(0x0005, master, false)))
    private fun signedRead(generation: CardGeneration): CardReadResult {
        val raw = read(generation)
        val originals = CardCoreEf.entries.associateWith { kind ->
            CardCoreRead(kind, CardCoreReadStatus.COMPLETE, CardCoreSnapshot(kind, generation,
                ByteArray(if (kind == CardCoreEf.ACTIVITY) 68 else if (generation == CardGeneration.GEN1) 33 else 50)))
        }
        fun tlv(tag: Int, bytes: ByteArray): ByteArray {
            val prefix = if (tag > 255) byteArrayOf((tag ushr 8).toByte(), tag.toByte()) else byteArrayOf(tag.toByte())
            val size = if (bytes.size < 128) byteArrayOf(bytes.size.toByte()) else byteArrayOf(0x81.toByte(), bytes.size.toByte())
            return prefix + size + bytes
        }
        val certificate = if (generation == CardGeneration.GEN1) null else {
            val body = tlv(0x5F29, byteArrayOf(0)) + tlv(0x42, ByteArray(8) { 1 }) +
                tlv(0x5F4C, DriverCardFiles.gen2Aid + byteArrayOf(17)) +
                tlv(0x7F49, tlv(6, byteArrayOf(0x2A, 0x86.toByte(), 0x48, 0xCE.toByte(), 0x3D, 3, 1, 7)) +
                    tlv(0x86, ByteArray(65) { if (it == 0) 4 else 7 })) +
                tlv(0x5F20, ByteArray(8) { 9 }) + tlv(0x5F25, ByteArray(4)) + tlv(0x5F24, ByteArray(4))
            CardCertificateFile(CardCertificateKind.SIGNING, generation, tlv(0x7F21,
                tlv(0x7F4E, body) + tlv(0x5F37, ByteArray(64) { 3 })))
        }
        val policy = if (generation == CardGeneration.GEN1) CardSignaturePolicy.RSA_SHA1 else CardSignaturePolicy.NIST_P256
        return raw.copy(coreReads = originals,
            certificates = certificate?.let { mapOf(CardCertificateKind.SIGNING to CardCertificateRead(it)) }.orEmpty(),
            coreSignatures = originals.mapValues { (kind, source) ->
                CardCoreSignature(kind, generation, CardSignatureStatus.CAPTURED_UNVERIFIED, policy, CardSignaturePhase.SIGN,
                    statusWord = 0x9000, hashStatusWord = 0x9000, signStatusWord = 0x9000, actualLength = policy.length,
                    original = source.snapshot, certificate = certificate, bytes = ByteArray(policy.length) { 7 })
            })
    }

    private fun signedIdentityRead(generation: CardGeneration): CardReadResult {
        val input = signedRead(generation)
        val sources = input.identitySources.toMutableMap()
        if (generation == CardGeneration.GEN2V2) {
            sources[CardIdentitySourceKind.APPLICATION] = CardIdentitySourceRead(CardIdentitySourceFile(
                CardIdentitySourceKind.APPLICATION, generation, sources.getValue(CardIdentitySourceKind.APPLICATION).file!!.bytes + byteArrayOf(0, 0)))
            sources[CardIdentitySourceKind.APPLICATION_V2] = CardIdentitySourceRead(CardIdentitySourceFile(
                CardIdentitySourceKind.APPLICATION_V2, generation, byteArrayOf(0, 8, 0, 0, 0, 0, 0, 0, 0, 0)))
        }
        val policy = if (generation == CardGeneration.GEN1) CardSignaturePolicy.RSA_SHA1 else CardSignaturePolicy.NIST_P256
        val certificate = input.certificates[CardCertificateKind.SIGNING]?.file
        return input.copy(identitySources = sources, identitySignatures = sources.mapValues { (kind, source) ->
            CardIdentitySignature(kind, generation, CardSignatureStatus.CAPTURED_UNVERIFIED, policy, CardSignaturePhase.SIGN,
                statusWord = 0x9000, hashStatusWord = 0x9000, signStatusWord = 0x9000, actualLength = policy.length,
                original = source.file, certificate = certificate, bytes = ByteArray(policy.length) { 9 })
        })
    }
    private fun identityBatch() = CardApplicationReadBatch(listOf(
        CardApplicationAttempt.read(CardApplication.GEN1, signedIdentityRead(CardGeneration.GEN1)),
        CardApplicationAttempt.read(CardApplication.GEN2, signedIdentityRead(CardGeneration.GEN2V2))),
        listOf(CardMasterFileCapture(0x0005, ByteArray(8), false)))

    private fun eventRead(generation: CardGeneration, salt: Int = 7, unsupported: Boolean = false): CardReadResult {
        val input = signedIdentityRead(generation)
        val app = input.identitySources.getValue(CardIdentitySourceKind.APPLICATION).file!!.bytes.also { it[3] = 12; it[4] = 24 }
        val sources = input.identitySources + (CardIdentitySourceKind.APPLICATION to CardIdentitySourceRead(
            CardIdentitySourceFile(CardIdentitySourceKind.APPLICATION, generation, app)))
        val policy = if (generation == CardGeneration.GEN1) CardSignaturePolicy.RSA_SHA1 else CardSignaturePolicy.NIST_P256
        val certificate = input.certificates[CardCertificateKind.SIGNING]?.file
        val identities = input.identitySignatures + (CardIdentitySourceKind.APPLICATION to CardIdentitySignature(
            CardIdentitySourceKind.APPLICATION, generation, CardSignatureStatus.CAPTURED_UNVERIFIED, policy, CardSignaturePhase.SIGN,
            statusWord = 0x9000, hashStatusWord = 0x9000, signStatusWord = 0x9000, actualLength = policy.length,
            original = sources.getValue(CardIdentitySourceKind.APPLICATION).file, certificate = certificate, bytes = ByteArray(policy.length) { 7 }))
        val originals = CardEventFaultKind.entries.associateWith { kind ->
            val count = if (kind == CardEventFaultKind.EVENTS) 12 else 24
            CardEventFaultFile(kind, generation, count, ByteArray(CardEventFaultParser.fileLength(kind, generation, count)).also {
                if (unsupported && kind == CardEventFaultKind.EVENTS) { it[0] = 1; it[10] = 99; it[11] = 65 }
            })
        }
        return input.copy(identitySources = sources, identitySignatures = identities,
            events = originals.getValue(CardEventFaultKind.EVENTS), faults = originals.getValue(CardEventFaultKind.FAULTS),
            eventFaultSignatures = originals.mapValues { (kind, file) ->
                if (unsupported && kind == CardEventFaultKind.EVENTS) CardEventFaultSignature(kind, generation,
                    CardSignatureStatus.NOT_ATTEMPTED, policy, CardSignaturePhase.BOUNDARY, CardSignatureReason.UNSUPPORTED_FORMAT,
                    statusWord = 0x6700, hashStatusWord = 0x9000, original = file, certificate = certificate)
                else CardEventFaultSignature(kind, generation, CardSignatureStatus.CAPTURED_UNVERIFIED, policy, CardSignaturePhase.SIGN,
                    statusWord = 0x9000, hashStatusWord = 0x9000, signStatusWord = 0x9000, actualLength = policy.length,
                    original = file, certificate = certificate, bytes = ByteArray(policy.length) { salt.toByte() })
            })
    }
    private fun eventBatch(salt: Int = 7) = CardApplicationReadBatch(listOf(
        CardApplicationAttempt.read(CardApplication.GEN1, eventRead(CardGeneration.GEN1, salt)),
        CardApplicationAttempt.read(CardApplication.GEN2, eventRead(CardGeneration.GEN2V2, salt))), emptyList())

    @Test fun eventReceiptsBindFreshRawAndCertificatesToExactChildrenAndReopen() = runBlocking {
        val old = pipeline.processApplications(identityBatch(), "before-event-signatures", true)
        val saved = pipeline.processApplications(eventBatch(), "events-signed")
        assertEquals(4, count("card_event_fault_signature_receipts"))
        for (attempt in saved.applications) {
            val child = imports.get(attempt.run!!.sessionId!!)!!
            assertTrue(child.events!!.records.isEmpty()); assertTrue(child.faults!!.records.isEmpty())
            assertEquals(2, child.coreSignatures.size)
            assertEquals(if (child.application == CardApplication.GEN1) 2 else 3, child.identitySignatures.size)
            for (receipt in db.cardEventFaultSignatureDao().receipts(child.id)) {
                val signature = child.eventFaultSignatures.values.single { it.kind.originalStorageKind == receipt.kind }
                assertArrayEquals(signature.original!!.bytes, db.cardMetadataDao().get(receipt.originalSourceId!!)!!.bytes)
                assertTrue(db.cardMetadataDao().sources(child.id).any { it.sourceId == receipt.originalSourceId && it.kind == receipt.kind })
                assertEquals(child.driverCardId, receipt.driverCardId); assertEquals(child.application!!.name, receipt.application)
                assertEquals(if (child.application == CardApplication.GEN1) 128 else 64, signature.actualLength)
            }
        }
        db.close(); open()
        for (attempt in old.applications) assertTrue(imports.get(attempt.run!!.sessionId!!)!!.eventFaultSignatures.isEmpty())
        assertBatchContent(saved, pipeline.processApplications(eventBatch(), "events-signed"))
        val next = pipeline.processApplications(eventBatch(9), "events-signed-again")
        assertEquals(8, count("card_event_fault_signature_receipts"))
        for (attempt in next.applications) {
            val nextRows = db.cardEventFaultSignatureDao().receipts(attempt.run!!.sessionId!!)
            val previousSession = saved.applications.single { it.application == attempt.application }.run!!.sessionId!!
            val previousRows = db.cardEventFaultSignatureDao().receipts(previousSession)
            assertEquals(previousRows.associate { it.kind to it.originalSourceId }, nextRows.associate { it.kind to it.originalSourceId })
            assertTrue(nextRows.none { row -> previousRows.any { it.fingerprint == row.fingerprint } })
        }
        for (attempt in next.applications) assertEquals(9, imports.get(attempt.run!!.sessionId!!)!!.eventFaultSignatures.values.first().bytes!![0].toInt())
    }
    @Test fun unsupportedCompleteEventsRetainRawWithInvalidProjectionAndNoPositiveSignature() = runBlocking {
        val input = eventRead(CardGeneration.GEN1, unsupported = true)
        imports.bind(input.card!!, true)
        val run = pipeline.process(input, "event-invalid-projection")
        assertEquals(CardImportFileStatus.INVALID, run.files.single { it.kind == CardImportFileKind.EVENTS }.status)
        val saved = imports.get(run.sessionId!!)!!
        assertNull(saved.events); assertTrue(saved.faults!!.records.isEmpty())
        val signature = saved.eventFaultSignatures.getValue(CardEventFaultKind.EVENTS)
        assertEquals(CardSignatureReason.UNSUPPORTED_FORMAT, signature.reason); assertNull(signature.bytes)
        assertArrayEquals(input.events!!.bytes, signature.original!!.bytes)
        db.close(); open(); assertArrayEquals(input.events!!.bytes, imports.get(saved.id)!!.eventFaultSignatures.getValue(CardEventFaultKind.EVENTS).original!!.bytes)
    }
    @Test fun foreignSessionAndReceiptTamperAreRejectedWithoutChangingGoodImports() = runBlocking {
        val saved = pipeline.processApplications(eventBatch(), "event-tamper", true)
        val g1 = saved.applications.first().run!!.sessionId!!; val g2 = saved.applications.last().run!!.sessionId!!
        val row = db.cardEventFaultSignatureDao().receipts(g2).first()
        val unsigned = pipeline.processApplications(identityBatch(), "event-receipt-foreign-session")
        val unsignedG2 = unsigned.applications.last().run!!.sessionId!!
        assertTrue(db.cardEventFaultSignatureDao().receipts(unsignedG2).isEmpty())
        // Same card, generation and kind, but this exact child never captured the event original.
        rejected { db.cardEventFaultSignatureDao().insert(row.copy(sessionId = unsignedG2)) }
        assertTrue(db.cardEventFaultSignatureDao().receipts(unsignedG2).isEmpty())
        for (update in listOf("bytes = zeroblob(64)", "status = 'NOT_ATTEMPTED'", "fingerprint = 'damaged'",
            "application = 'GEN1'", "generation = 'GEN1'", "driverCardId = 'foreign'", "originalSourceId = NULL")) {
            rejected { db.withTransaction {
                db.openHelper.writableDatabase.execSQL("UPDATE card_event_fault_signature_receipts SET $update WHERE sessionId = ?", arrayOf(g2))
                imports.get(g2)
            } }
            assertEquals(2, imports.get(g2)!!.eventFaultSignatures.size)
        }
        val rawId = checkNotNull(row.originalSourceId)
        val corrupted = checkNotNull(db.cardMetadataDao().get(rawId)).bytes.copyOf().also { it[0] = 1 }
        rejected { db.withTransaction {
            db.openHelper.writableDatabase.execSQL("UPDATE card_metadata_imports SET bytes = ? WHERE id = ?", arrayOf(corrupted, rawId))
            imports.get(g2)
        } }
        assertEquals(2, imports.get(g2)!!.eventFaultSignatures.size)
        val another = pipeline.processApplications(eventBatch(9), "event-tamper-other")
        val foreign = db.cardEventFaultSignatureDao().receipts(another.primaryRun.sessionId!!).first()
        rejected { db.withTransaction {
            db.openHelper.writableDatabase.execSQL("UPDATE card_event_fault_signature_receipts SET certificateSourceId = ? WHERE sessionId = ? AND kind = ?",
                arrayOf(foreign.originalSourceId, g2, row.kind))
            imports.get(g2)
        } }
    }
    @Test fun newReceiptsCannotBeAttachedToOldRequestOrBatchEvenForIdenticalRawBytes() = runBlocking {
        val input = eventRead(CardGeneration.GEN1)
        val unsigned = input.copy(eventFaultSignatures = emptyMap())
        imports.bind(input.card!!, true)
        val old = pipeline.process(unsigned, "event-old-request")
        rejected { pipeline.process(input, "event-old-request") }
        assertTrue(imports.get(old.sessionId!!)!!.eventFaultSignatures.isEmpty())
        val legacy = CardApplicationReadBatch(listOf(CardApplicationAttempt.read(CardApplication.GEN1, unsigned),
            CardApplicationAttempt.absent(CardApplication.GEN2)), emptyList())
        pipeline.processApplications(legacy, "event-old-batch")
        val signed = CardApplicationReadBatch(listOf(CardApplicationAttempt.read(CardApplication.GEN1, input),
            CardApplicationAttempt.absent(CardApplication.GEN2)), emptyList())
        rejected { pipeline.processApplications(signed, "event-old-batch") }
        assertEquals(0, count("card_event_fault_signature_receipts"))
    }
    @Test fun secondChildEventReceiptFailureRollsBackEntireNewGroupAndKeepsPriorImports() = runBlocking {
        val old = pipeline.processApplications(identityBatch(), "before-event-rollback", true)
        val oldProfile = imports.profile()
        val counts = listOf("card_read_batches", "card_import_runs", "import_sessions", "card_metadata_imports",
            "driver_import_metadata_sources", "card_core_signature_receipts", "card_identity_signature_receipts").associateWith { count(it) }
        db.openHelper.writableDatabase.execSQL("""CREATE TRIGGER reject_event_g2 BEFORE INSERT ON card_event_fault_signature_receipts
            WHEN NEW.application = 'GEN2' BEGIN SELECT RAISE(ABORT, 'synthetic second child'); END""")
        rejected { pipeline.processApplications(eventBatch(), "event-rollback") }
        for ((table, before) in counts) assertEquals(table, before, count(table))
        assertEquals(0, count("card_event_fault_signature_receipts")); assertEquals(oldProfile, imports.profile())
        assertTrue(imports.get(old.primaryRun.sessionId!!)!!.eventFaultSignatures.isEmpty())
    }
    @Test fun firstEventGroupFailureAlsoRollsBackNewOwnCardBinding() = runBlocking {
        db.openHelper.writableDatabase.execSQL("""CREATE TRIGGER reject_first_event_g2 BEFORE INSERT ON card_event_fault_signature_receipts
            WHEN NEW.application = 'GEN2' BEGIN SELECT RAISE(ABORT, 'synthetic first group'); END""")
        rejected { pipeline.processApplications(eventBatch(), "first-event-rollback", true) }
        assertNull(imports.profile())
        for (table in listOf("profiles", "driver_cards", "card_read_batches", "card_application_attempts", "card_import_runs",
            "import_sessions", "driver_import_sources", "card_metadata_imports", "driver_import_metadata_sources",
            "card_core_signature_receipts", "card_identity_signature_receipts", "card_event_fault_signature_receipts"))
            assertEquals(table, 0, count(table))
    }

    @Test fun encryptedTwentyOneToTwentyTwoPreservesExactExistingRequestsSourcesAndSignatures() = runBlocking {
        val old = pipeline.processApplications(identityBatch(), "before-event-upgrade", true)
        val runs = old.applications.map { db.cardImportRunDao().run(it.run!!.id) }
        val profile = imports.profile()
        val sources = old.applications.associate { attempt ->
            val id = checkNotNull(attempt.run).sessionId!!; id to db.cardMetadataDao().sources(id)
        }
        val receipts = old.applications.associate { attempt ->
            val id = checkNotNull(attempt.run).sessionId!!; id to db.cardIdentitySignatureDao().receipts(id).map { row -> row.fingerprint }
        }
        db.openHelper.writableDatabase.apply {
            execSQL("DROP TABLE violation_context_revisions") // Empty M1 relation is absent in the historical fixture.
            execSQL("DROP TABLE card_event_fault_signature_receipts"); execSQL("DELETE FROM room_master_table"); version = 21
        }
        db.close(); open()
        assertEquals(AppDatabase.SCHEMA_VERSION, db.openHelper.writableDatabase.version); assertEquals(profile, imports.profile())
        assertEquals(runs, old.applications.map { db.cardImportRunDao().run(it.run!!.id) })
        assertEquals(0, count("card_event_fault_signature_receipts"))
        assertBatchContent(old, pipeline.processApplications(identityBatch(), "before-event-upgrade"))
        for ((id, original) in sources) { assertEquals(original, db.cardMetadataDao().sources(id)); assertTrue(imports.get(id)!!.eventFaultSignatures.isEmpty()) }
        for ((id, original) in receipts) assertEquals(original, db.cardIdentitySignatureDao().receipts(id).map { it.fingerprint })
        pipeline.processApplications(eventBatch(), "after-event-upgrade")
        db.close(); open(); assertEquals(4, count("card_event_fault_signature_receipts"))
        for (attempt in old.applications) assertTrue(imports.get(attempt.run!!.sessionId!!)!!.eventFaultSignatures.isEmpty())
    }

    @Test fun fiveIdentityReceiptsKeepExactChildSourcesAcrossDeduplicationHistoryAndReopen() = runBlocking {
        val old = pipeline.processApplications(batch(), "identity-old", true)
        val first = pipeline.processApplications(identityBatch(), "identity-first")
        val next = pipeline.processApplications(identityBatch(), "identity-next")
        assertEquals(10, count("card_identity_signature_receipts")); assertEquals(8, count("card_core_signature_receipts"))
        for (attempt in first.applications) {
            val child = imports.get(attempt.run!!.sessionId!!)!!
            val second = imports.get(next.applications.single { it.application == attempt.application }.run!!.sessionId!!)!!
            assertEquals(if (attempt.application == CardApplication.GEN1) 2 else 3, child.identitySignatures.size)
            assertEquals(2, child.coreSignatures.size)
            val refs = db.cardMetadataDao().sources(child.id)
            for (row in db.cardIdentitySignatureDao().receipts(child.id)) {
                assertEquals(child.driverCardId, row.driverCardId); assertEquals(attempt.application.name, row.application)
                assertTrue(refs.any { it.kind == row.kind && it.sourceId == row.originalSourceId && it.driverCardId == row.driverCardId })
                if (row.certificateSourceId != null) assertTrue(refs.any { it.kind == row.certificateKind &&
                    it.sourceId == row.certificateSourceId && it.driverCardId == row.driverCardId })
                val kind = CardIdentitySourceKind.entries.single { "IDENTITY_SOURCE_${it.name}" == row.kind }
                assertEquals(child.identitySources.getValue(kind).id, second.identitySources.getValue(kind).id)
                assertArrayEquals(child.identitySources.getValue(kind).file.bytes, child.identitySignatures.getValue(kind).original!!.bytes)
            }
            assertTrue(child.identitySignatures.values.all { it.status == CardSignatureStatus.CAPTURED_UNVERIFIED })
        }
        rejected { pipeline.processApplications(identityBatch(), "identity-old") }
        assertBatchContent(first, pipeline.processApplications(identityBatch(), "identity-first"))
        db.close(); open()
        for (attempt in first.applications) assertFalse(imports.get(attempt.run!!.sessionId!!)!!.identitySignatures.isEmpty())
        for (attempt in old.applications) assertTrue(imports.get(attempt.run!!.sessionId!!)!!.identitySignatures.isEmpty())
        assertEquals(10, count("card_identity_signature_receipts"))
    }
    @Test fun identitySignaturesAloneCannotChangeAnyExistingSingleOrBatchRequest() = runBlocking {
        val signed = signedIdentityRead(CardGeneration.GEN1)
        val unsigned = signed.copy(identitySignatures = emptyMap())
        imports.bind(unsigned.card!!, true)
        val old = pipeline.process(unsigned, "identity-single-old")
        val request = db.cardImportRunDao().run(old.id)
        rejected { pipeline.process(signed, old.id) }
        rejected { imports.save(signed, old.id, CardImportSelection.ALL_AVAILABLE) }
        assertEquals(request, db.cardImportRunDao().run(old.id))
        assertTrue(imports.get(old.id)!!.identitySignatures.isEmpty())
        assertEquals(old, pipeline.process(unsigned.copy(identitySignatures = emptyMap()), old.id))
        val signedBatch = identityBatch()
        val oldBatch = CardApplicationReadBatch(signedBatch.applications.map {
            CardApplicationAttempt.read(it.application, it.result!!.copy(identitySignatures = emptyMap()))
        }, signedBatch.masterFiles)
        val saved = pipeline.processApplications(oldBatch, "identity-batch-old")
        rejected { pipeline.processApplications(signedBatch, saved.id) }
        assertBatchContent(saved, pipeline.processApplications(oldBatch, saved.id))
        assertEquals(0, count("card_identity_signature_receipts"))
        assertEquals(3, count("import_sessions"))
    }
    @Test fun secondIdentityReceiptFailureRollsBackNewBindingParentBothChildrenAndAllKindsOfReceipt() = runBlocking {
        db.openHelper.writableDatabase.execSQL("""CREATE TRIGGER reject_identity_g2 BEFORE INSERT ON card_identity_signature_receipts
            WHEN NEW.generation = 'GEN2V2' BEGIN SELECT RAISE(ABORT, 'synthetic identity second-child failure'); END""")
        rejected { pipeline.processApplications(identityBatch(), "identity-rollback-new", true) }
        for (table in listOf("profiles", "driver_cards", "card_read_batches", "card_application_attempts", "card_import_runs",
            "import_sessions", "driver_import_sources", "card_metadata_imports", "driver_import_metadata_sources",
            "card_core_signature_receipts", "card_identity_signature_receipts")) assertEquals(table, 0, count(table))
    }
    @Test fun failedSecondIdentityReceiptLeavesPreviousSuccessfulImportsByteForByteIntact() = runBlocking {
        val previous = pipeline.processApplications(identityBatch(), "identity-previous", true)
        val rows = previous.applications.flatMap { db.cardIdentitySignatureDao().receipts(it.run!!.sessionId!!) }
        val fingerprints = rows.map { it.fingerprint }
        val profile = imports.profile()
        db.openHelper.writableDatabase.execSQL("""CREATE TRIGGER reject_later_identity BEFORE INSERT ON card_identity_signature_receipts
            WHEN NEW.generation = 'GEN2V2' BEGIN SELECT RAISE(ABORT, 'synthetic later receipt failure'); END""")
        rejected { pipeline.processApplications(identityBatch(), "identity-rollback-later") }
        assertEquals(profile, imports.profile()); assertEquals(1, count("card_read_batches"))
        assertEquals(2, count("import_sessions")); assertEquals(5, count("card_identity_signature_receipts"))
        assertEquals(4, count("card_core_signature_receipts"))
        assertBatchContent(previous, pipeline.applicationBatch(previous.id))
        assertEquals(fingerprints, previous.applications.flatMap { db.cardIdentitySignatureDao().receipts(it.run!!.sessionId!!) }.map { it.fingerprint })
    }
    @Test fun failedFreshIdentityReadKeepsOnlyIndependentUnsignedSourceAndNullReceiptReference() = runBlocking {
        val input = signedIdentityRead(CardGeneration.GEN1)
        val kind = CardIdentitySourceKind.IDENTIFICATION
        val failed = input.copy(identitySignatures = mapOf(kind to CardIdentitySignature(kind, input.generation,
            CardSignatureStatus.FAILED, CardSignaturePolicy.RSA_SHA1, CardSignaturePhase.READ,
            CardSignatureReason.INCOMPLETE_READ, statusWord = 0x6700, hashStatusWord = 0x9000)))
        val batch = CardApplicationReadBatch(listOf(CardApplicationAttempt.read(CardApplication.GEN1, failed),
            CardApplicationAttempt.absent(CardApplication.GEN2)), emptyList())
        val saved = pipeline.processApplications(batch, "identity-null-original", true)
        val child = imports.get(saved.applications.first().run!!.sessionId!!)!!
        assertNotNull(child.identitySources[kind]); assertNull(child.identitySignatures.getValue(kind).original)
        assertNull(db.cardIdentitySignatureDao().receipts(child.id).single().originalSourceId)
        db.close(); open()
        assertNull(imports.get(child.id)!!.identitySignatures.getValue(kind).original)
    }
    @Test fun identityReceiptForeignSessionAndEveryTamperAreRejected() = runBlocking {
        val saved = pipeline.processApplications(identityBatch(), "identity-tamper", true)
        val g1 = saved.applications.first().run!!.sessionId!!
        val g2 = saved.applications.last().run!!.sessionId!!
        val row = db.cardIdentitySignatureDao().receipts(g2).first()
        // This G2 original/certificate tuple does not exist in the G1 child.
        rejected { db.cardIdentitySignatureDao().insert(row.copy(sessionId = g1, kind = "IDENTITY_SOURCE_APPLICATION_V2")) }
        for (update in listOf("bytes = zeroblob(64)", "status = 'NOT_ATTEMPTED'", "fingerprint = 'damaged'",
            "application = 'GEN1'", "generation = 'GEN1'", "driverCardId = 'foreign'")) {
            rejected {
                db.withTransaction {
                    db.openHelper.writableDatabase.execSQL("UPDATE card_identity_signature_receipts SET $update WHERE sessionId = ?", arrayOf(g2))
                    imports.get(g2)
                }
            }
            assertEquals(3, imports.get(g2)!!.identitySignatures.size)
        }
    }
    @Test fun encryptedTwentyToTwentyOneRetainsAllLegacyIdentityCoreAndRequestBytesWithoutBackfill() = runBlocking {
        val input = CardApplicationReadBatch(listOf(
            CardApplicationAttempt.read(CardApplication.GEN1, signedRead(CardGeneration.GEN1)),
            CardApplicationAttempt.read(CardApplication.GEN2, signedRead(CardGeneration.GEN2V2))), emptyList())
        val old = pipeline.processApplications(input, "before-identity-upgrade", true)
        val profile = imports.profile()
        val sources = old.applications.associate { attempt ->
            val id = attempt.run!!.sessionId!!
            id to db.cardMetadataDao().sources(id).map { it.sourceId }
        }
        val core = old.applications.flatMap { db.cardCoreSignatureDao().receipts(it.run!!.sessionId!!) }.map { it.fingerprint }
        db.openHelper.writableDatabase.apply {
            execSQL("DROP TABLE violation_context_revisions") // Empty M1 relation is absent in the historical fixture.
            execSQL("DROP TABLE card_identity_signature_receipts")
            execSQL("DROP TABLE IF EXISTS card_event_fault_signature_receipts")
            execSQL("DELETE FROM room_master_table"); version = 20
        }
        db.close(); open()
        assertEquals(AppDatabase.SCHEMA_VERSION, db.openHelper.writableDatabase.version); assertEquals(profile, imports.profile())
        assertBatchContent(old, pipeline.processApplications(input, "before-identity-upgrade"))
        assertEquals(core, old.applications.flatMap { db.cardCoreSignatureDao().receipts(it.run!!.sessionId!!) }.map { it.fingerprint })
        for ((id, rawIds) in sources) {
            assertEquals(rawIds, db.cardMetadataDao().sources(id).map { it.sourceId })
            val child = imports.get(id)!!
            assertTrue(child.identitySignatures.isEmpty()); assertEquals(2, child.coreSignatures.size)
            assertEquals(15.takeIf { child.application == CardApplication.GEN2 } ?: 10,
                child.identitySources.getValue(CardIdentitySourceKind.APPLICATION).file.bytes.size)
        }
        assertEquals(0, count("card_identity_signature_receipts"))
        pipeline.processApplications(identityBatch(), "after-identity-upgrade")
        db.close(); open()
        assertEquals(5, count("card_identity_signature_receipts"))
        for (attempt in old.applications) assertTrue(imports.get(attempt.run!!.sessionId!!)!!.identitySignatures.isEmpty())
    }

    @Test fun signaturesBindOnlyToFreshGen1AndGen2V2ChildrenAndSurviveReopen() = runBlocking {
        val old = pipeline.processApplications(batch(), "signature-group-old", true)
        val input = CardApplicationReadBatch(listOf(
            CardApplicationAttempt.read(CardApplication.GEN1, signedRead(CardGeneration.GEN1)),
            CardApplicationAttempt.read(CardApplication.GEN2, signedRead(CardGeneration.GEN2V2))),
            listOf(CardMasterFileCapture(0x0005, ByteArray(8), false)))
        val saved = pipeline.processApplications(input, "signature-group-new")
        for (attempt in saved.applications) {
            val child = imports.get(attempt.run!!.sessionId!!)!!
            assertEquals(attempt.application, child.application)
            assertEquals(2, child.coreSignatures.size)
            assertTrue(child.coreSignatures.values.all { it.generation == attempt.generation &&
                it.bytes!!.size == if (attempt.application == CardApplication.GEN1) 128 else 64 })
        }
        for (attempt in old.applications) assertTrue(imports.get(attempt.run!!.sessionId!!)!!.coreSignatures.isEmpty())
        assertEquals(4, db.cardCoreSignatureDao().receipts(saved.applications[0].run!!.sessionId!!).size +
            db.cardCoreSignatureDao().receipts(saved.applications[1].run!!.sessionId!!).size)
        db.close(); open()
        assertBatchContent(saved, pipeline.applicationBatch(saved.id))
        assertBatchContent(saved, pipeline.processApplications(input, saved.id))
    }

    @Test fun secondChildSignatureFailureRollsBackTheEntireNewGroup() = runBlocking {
        val old = pipeline.processApplications(batch(), "signature-group-before", true)
        db.openHelper.writableDatabase.execSQL("""CREATE TRIGGER reject_second_signature BEFORE INSERT ON card_core_signature_receipts
            WHEN NEW.generation = 'GEN2V2' BEGIN SELECT RAISE(ABORT, 'synthetic second signature rollback'); END""")
        val input = CardApplicationReadBatch(listOf(
            CardApplicationAttempt.read(CardApplication.GEN1, signedRead(CardGeneration.GEN1)),
            CardApplicationAttempt.read(CardApplication.GEN2, signedRead(CardGeneration.GEN2V2))), emptyList())
        try { pipeline.processApplications(input, "signature-group-failed"); fail() } catch (_: IllegalStateException) { }
        assertNull(pipeline.applicationBatch("signature-group-failed"))
        assertBatchContent(old, pipeline.applicationBatch(old.id))
        db.openHelper.readableDatabase.query("SELECT COUNT(*) FROM card_core_signature_receipts").use {
            it.moveToFirst(); assertEquals(0, it.getInt(0))
        }
    }

    @Test fun coreOriginalsBindToExactGen1AndGen2V2ChildrenWithoutMfOrHistoricalBackfill() = runBlocking {
        val old = pipeline.processApplications(batch(), "core-group-old", true)
        fun value(generation: CardGeneration): CardReadResult {
            val raw = read(generation)
            val width = if (generation == CardGeneration.GEN1) 31 else 48
            return raw.copy(coreReads = mapOf(
                CardCoreEf.ACTIVITY to CardCoreRead(CardCoreEf.ACTIVITY, CardCoreReadStatus.COMPLETE,
                    CardCoreSnapshot(CardCoreEf.ACTIVITY, generation, ByteArray(68))),
                CardCoreEf.VEHICLES to CardCoreRead(CardCoreEf.VEHICLES, CardCoreReadStatus.COMPLETE,
                    CardCoreSnapshot(CardCoreEf.VEHICLES, generation, ByteArray(2 + width)))))
        }
        val input = CardApplicationReadBatch(listOf(
            CardApplicationAttempt.read(CardApplication.GEN1, value(CardGeneration.GEN1)),
            CardApplicationAttempt.read(CardApplication.GEN2, value(CardGeneration.GEN2V2))),
            listOf(CardMasterFileCapture(0x0005, ByteArray(8), false)))
        val saved = pipeline.processApplications(input, "core-group-new")
        val sources = saved.applications.map { application ->
            val imported = imports.get(application.run!!.sessionId!!)!!
            assertEquals(saved.driverCardId, imported.driverCardId)
            assertEquals(saved.id, imported.batchId); assertEquals(application.application, imported.application)
            assertEquals(2, imported.coreSnapshots.size)
            assertTrue(imported.coreSnapshots.values.all { it.snapshot.generation == application.generation })
            assertTrue(db.cardMetadataDao().sources(imported.id).all { it.kind != "IC_IDENTIFICATION" })
            imported.coreSnapshots.getValue(CardCoreEf.ACTIVITY).id
        }
        assertNotEquals(sources[0], sources[1])
        assertTrue(old.applications.all { imports.get(it.run!!.sessionId!!)!!.coreSnapshots.isEmpty() })
        assertEquals(1, saved.masterFiles.size); assertEquals(1, count("card_master_sources"))
        assertEquals(saved.id, imports.latest()!!.batchId)
        db.close(); open()
        for (application in saved.applications) assertEquals(2, imports.get(application.run!!.sessionId!!)!!.coreSnapshots.size)
        assertTrue(old.applications.all { imports.get(it.run!!.sessionId!!)!!.coreSnapshots.isEmpty() })
    }

    @Test fun coreFailureInSecondChildRollsBackParentBindingAndFirstChildOriginals() = runBlocking {
        fun value(generation: CardGeneration) = read(generation).copy(coreReads = mapOf(
            CardCoreEf.ACTIVITY to CardCoreRead(CardCoreEf.ACTIVITY, CardCoreReadStatus.COMPLETE,
                CardCoreSnapshot(CardCoreEf.ACTIVITY, generation, ByteArray(68)))))
        val input = CardApplicationReadBatch(listOf(
            CardApplicationAttempt.read(CardApplication.GEN1, value(CardGeneration.GEN1)),
            CardApplicationAttempt.read(CardApplication.GEN2, value(CardGeneration.GEN2))), emptyList())
        db.openHelper.writableDatabase.execSQL("""CREATE TRIGGER fail_core_g2 BEFORE INSERT ON card_metadata_imports
            WHEN NEW.generation = 'GEN2' AND NEW.kind = 'ORIGINAL_EF_0504'
            BEGIN SELECT RAISE(ABORT, 'synthetic core second-child failure'); END""")
        rejected { pipeline.processApplications(input, "core-group-rollback", true) }
        for (table in listOf("profiles", "driver_cards", "card_read_batches", "card_application_attempts",
            "card_import_runs", "import_sessions", "driver_import_sources", "card_metadata_imports"))
            assertEquals(table, 0, count(table))
    }

    private fun assertBatchContent(expected: SavedCardReadBatch, actual: SavedCardReadBatch?) {
        assertNotNull(actual)
        val saved = checkNotNull(actual)
        // Master-file wrappers intentionally use object identity; compare their exact contents separately.
        assertEquals(expected.copy(masterFiles = emptyList()), saved.copy(masterFiles = emptyList()))
        assertEquals(expected.masterFiles.size, saved.masterFiles.size)
        for ((before, after) in expected.masterFiles.zip(saved.masterFiles)) {
            assertEquals(before.fileId, after.fileId)
            assertEquals(before.readFailed, after.readFailed)
            assertArrayEquals(before.bytes, after.bytes)
        }
    }

    private fun count(table: String) = db.openHelper.readableDatabase.query("SELECT COUNT(*) FROM $table").use { it.moveToFirst(); it.getInt(0) }
    private suspend fun rejected(action: suspend () -> Unit) {
        try { action() } catch (_: Exception) { return }; fail("Expected rejection")
    }

    @Test fun bothApplicationsShareOneCardAndReopenExactSourcesAndGeneration() = runBlocking {
        val saved = pipeline.processApplications(batch(), "both", true)
        assertEquals(1, count("driver_cards")); assertEquals(2, count("import_sessions"))
        assertEquals(1, count("card_read_batches")); assertEquals(1, count("card_master_sources"))
        assertEquals(setOf(CardGeneration.GEN1, CardGeneration.GEN2), saved.applications.mapNotNull { it.generation }.toSet())
        assertEquals(CardApplication.GEN2, saved.primaryApplication)
        val g1 = saved.applications.single { it.application == CardApplication.GEN1 }.run!!
        val g2 = saved.primaryRun
        assertEquals(g1.driverCardId, g2.driverCardId)
        assertEquals(CardGeneration.GEN1, g1.generation); assertEquals(CardGeneration.GEN2, g2.generation)
        assertEquals(10, imports.get(g1.sessionId!!)!!.identitySources.getValue(CardIdentitySourceKind.APPLICATION).file.bytes.size)
        assertEquals(15, imports.get(g2.sessionId!!)!!.identitySources.getValue(CardIdentitySourceKind.APPLICATION).file.bytes.size)
        assertEquals(g2.sessionId, imports.latest()!!.id); assertEquals(1, pipeline.history().size)
        assertEquals(1, imports.all().size)
        db.close(); open()
        val reopened = pipeline.applicationBatch("both")!!
        assertEquals(g1, reopened.applications.single { it.application == CardApplication.GEN1 }.run)
        assertEquals(g2, reopened.primaryRun)
        assertArrayEquals(ByteArray(8), reopened.masterFiles.single().bytes)
        assertFalse(context.getDatabasePath(AppDatabase.DATABASE_NAME).readBytes().take(16).toByteArray()
            .contentEquals("SQLite format 3\u0000".toByteArray()))
    }
    @Test fun retryDeduplicatesAndChangedMfOrAttemptStatusCannotRefillRequest() = runBlocking {
        pipeline.processApplications(batch(), "retry", true)
        pipeline.processApplications(batch(), "retry")
        assertEquals(2, count("import_sessions")); assertEquals(1, count("card_read_batches"))
        rejected { pipeline.processApplications(batch(master = ByteArray(8) { 1 }), "retry") }
        rejected { pipeline.processApplications(batch(CardApplicationAttempt.absent(CardApplication.GEN2)), "retry") }
        assertArrayEquals(ByteArray(8), pipeline.applicationBatch("retry")!!.masterFiles.single().bytes)
        pipeline.processApplications(batch(), "separate")
        assertEquals(4, count("import_sessions")); assertEquals(1, count("card_master_sources"))
        assertEquals(4, count("card_metadata_imports")) // Two independent generations, two original EF kinds.
    }
    @Test fun differentCertificateOriginalsOfTheSameKindKeepTheirOwnApplicationSession() = runBlocking {
        val attempts = listOf(CardApplication.GEN1 to CardGeneration.GEN1, CardApplication.GEN2 to CardGeneration.GEN2).map { (aid, generation) ->
            val raw = ByteArray(if (generation == CardGeneration.GEN1) 194 else 204)
            CardApplicationAttempt.read(aid, read(generation).copy(certificates = mapOf(CardCertificateKind.CARD to
                CardCertificateRead(CardCertificateFile(CardCertificateKind.CARD, generation, raw)))))
        }
        val saved = pipeline.processApplications(CardApplicationReadBatch(attempts, emptyList()), "certificates", true)
        val g1 = imports.get(saved.applications.single { it.application == CardApplication.GEN1 }.run!!.sessionId!!)!!
        val g2 = imports.get(saved.primaryRun.sessionId!!)!!
        val first = g1.certificates.getValue(CardCertificateKind.CARD); val second = g2.certificates.getValue(CardCertificateKind.CARD)
        assertNotEquals(first.id, second.id); assertEquals(first.driverCardId, second.driverCardId)
        assertArrayEquals(ByteArray(194), first.file.bytes); assertArrayEquals(ByteArray(204), second.file.bytes)
        db.close(); open()
        assertArrayEquals(ByteArray(194), imports.get(g1.id)!!.certificates.getValue(CardCertificateKind.CARD).file.bytes)
        assertArrayEquals(ByteArray(204), imports.get(g2.id)!!.certificates.getValue(CardCertificateKind.CARD).file.bytes)
    }
    @Test fun unconfirmedConflictingAndForeignGroupsWriteNothing() = runBlocking {
        rejected { pipeline.processApplications(batch(), "unconfirmed") }
        assertNull(imports.profile()); assertEquals(0, count("card_read_batches"))
        val conflict = batch(CardApplicationAttempt.read(CardApplication.GEN2, read(CardGeneration.GEN2, identity(surname = "OTHER"))))
        rejected { pipeline.processApplications(conflict, "conflict", true) }
        assertNull(imports.profile()); assertEquals(0, count("driver_cards"))
        pipeline.processApplications(batch(), "own", true)
        val foreignRaw = identity("OTHERCRD12345678", "FOREIGN", byteArrayOf(0x19, 0x80.toByte(), 0x01, 0x01))
        val foreign = CardApplicationReadBatch(listOf(
            CardApplicationAttempt.read(CardApplication.GEN1, read(CardGeneration.GEN1, foreignRaw)),
            CardApplicationAttempt.read(CardApplication.GEN2, read(CardGeneration.GEN2, foreignRaw))), emptyList())
        assertEquals(CardBindingKind.FOREIGN_CARD, imports.review(foreign.applications.first().result!!.card!!).kind)
        rejected { pipeline.processApplications(foreign, "foreign", true) }
        assertEquals(1, count("driver_cards")); assertEquals(1, count("card_read_batches"))
    }
    @Test fun legacyManualChildRequestCannotBeAdoptedOrBackfilledIntoNewGroup() = runBlocking {
        imports.bind(read(CardGeneration.GEN2).card!!, true)
        val id = CardBatchRequests.child("collision", CardApplication.GEN1)
        val old = imports.save(read(CardGeneration.GEN1), id, CardImportSelection.ALL_AVAILABLE)
        rejected { pipeline.processApplications(batch(), "collision") }
        assertEquals(0, count("card_read_batches")); assertEquals(0, count("card_import_runs"))
        assertEquals(1, count("import_sessions")); assertNull(imports.get(old.id)!!.batchId)
        assertEquals(old.identitySources.keys, imports.get(old.id)!!.identitySources.keys)
    }
    @Test fun failureAfterFirstChildRollsBackBindingParentEverySourceAndRun() = runBlocking {
        db.openHelper.writableDatabase.execSQL("""CREATE TRIGGER fail_gen2 BEFORE INSERT ON card_metadata_imports
            WHEN NEW.generation = 'GEN2' BEGIN SELECT RAISE(ABORT, 'synthetic second-child storage failure'); END""")
        rejected { pipeline.processApplications(batch(), "atomic", true) }
        for (table in listOf("profiles", "driver_cards", "card_read_batches", "card_application_attempts",
            "card_import_runs", "import_sessions", "driver_import_sources", "card_metadata_imports", "card_master_sources"))
            assertEquals(table, 0, count(table))
        db.openHelper.writableDatabase.execSQL("DROP TRIGGER fail_gen2")
        assertNotNull(pipeline.processApplications(batch(), "atomic", true))
    }
    @Test fun failedOrAbsentSecondApplicationHasNoBorrowedSessionAndOldRequestsStayExact() = runBlocking {
        val ownRead = read(CardGeneration.GEN2)
        imports.bind(ownRead.card!!, true)
        val old = pipeline.process(ownRead, "legacy")
        val saved = pipeline.processApplications(batch(CardApplicationAttempt.failed(CardApplication.GEN2, ReaderFailure.IO)), "one")
        assertEquals(CardApplication.GEN1, saved.primaryApplication)
        assertNull(saved.applications.single { it.application == CardApplication.GEN2 }.run)
        assertEquals(CardGeneration.GEN1, saved.primaryRun.generation)
        assertEquals(old, pipeline.process(ownRead, "legacy"))
        assertNull(imports.get("legacy")!!.batchId)
        val absent = pipeline.processApplications(batch(CardApplicationAttempt.absent(CardApplication.GEN2)), "absent")
        assertEquals(CardApplicationReadStatus.ABSENT, absent.applications.single { it.application == CardApplication.GEN2 }.status)
    }
    @Test fun compositeForeignKeysRejectRunAndMasterSourcesFromAnotherCard() = runBlocking {
        val saved = pipeline.processApplications(batch(), "fk", true)
        val other = imports.bind(read(CardGeneration.GEN2, identity("NEXTCARD12345678")).card!!)
        val dao = db.cardReadBatchDao()
        dao.insertBatch(CardReadBatchEntity("wrong-card", other, "synthetic", clock.millis(), "GEN2"))
        rejected { dao.insertAttempts(listOf(CardApplicationAttemptEntity("wrong-card", "GEN2", other,
            "READ", null, "GEN2", saved.primaryRun.id))) }
        val original = dao.masterFiles("fk").single()
        rejected { dao.insertMasterFiles(listOf(CardBatchMasterFileEntity("wrong-card", original.fileId, other, original.sourceId, false))) }
        assertTrue(dao.attempts("wrong-card").isEmpty()); assertTrue(dao.masterFiles("wrong-card").isEmpty())
        val foreignSession = pipeline.process(read(CardGeneration.GEN2, identity("NEXTCARD12345678")), "other-session").sessionId!!
        val ownRun = pipeline.process(read(CardGeneration.GEN2), "own-unassigned")
        dao.insertBatch(CardReadBatchEntity("wrong-session", saved.driverCardId, "synthetic", clock.millis(), "GEN2"))
        rejected { dao.insertAttempts(listOf(CardApplicationAttemptEntity("wrong-session", "GEN2", saved.driverCardId,
            "READ", null, "GEN2", ownRun.id, foreignSession))) }
        assertTrue(dao.attempts("wrong-session").isEmpty())
    }
    @Test fun additiveEncryptedSchema18UpgradePreservesLegacyRequestAndSources() = runBlocking {
        val oldRead = read(CardGeneration.GEN2)
        imports.bind(oldRead.card!!, true)
        val old = pipeline.process(oldRead, "before-upgrade")
        val bytes = imports.get(old.sessionId!!)!!.identitySources.getValue(CardIdentitySourceKind.IDENTIFICATION).file.bytes
        // Remove only the new, empty additive relations to recreate the real encrypted v18 schema.
        db.openHelper.writableDatabase.apply {
            execSQL("DROP TABLE violation_context_revisions") // Empty M1 relation is absent in the historical fixture.
            for (table in listOf("card_identity_signature_receipts", "card_core_signature_receipts", "card_batch_master_files", "card_application_attempts", "card_master_sources", "card_read_batches"))
                execSQL("DROP TABLE $table")
            execSQL("DROP INDEX index_card_import_runs_id_driverCardId")
            execSQL("DROP INDEX index_driver_import_sources_sessionId_driverCardId")
            execSQL("DROP INDEX index_driver_import_metadata_sources_sessionId_driverCardId_kind_sourceId")
            execSQL("DROP TABLE IF EXISTS card_event_fault_signature_receipts")
            execSQL("DELETE FROM room_master_table")
            version = 18
        }
        db.close(); open()
        assertEquals(AppDatabase.SCHEMA_VERSION, db.openHelper.writableDatabase.version)
        assertEquals(old, pipeline.process(oldRead, "before-upgrade"))
        assertArrayEquals(bytes, imports.get(old.sessionId!!)!!.identitySources.getValue(CardIdentitySourceKind.IDENTIFICATION).file.bytes)
        assertEquals(0, count("card_read_batches")); assertEquals(0, count("card_application_attempts"))
        assertEquals(0, count("card_core_signature_receipts"))
        assertTrue(imports.get(old.sessionId!!)!!.coreSignatures.isEmpty())
        assertNull(imports.get(old.sessionId!!)!!.batchId)
        pipeline.processApplications(batch(), "after-upgrade")
        assertEquals(1, count("card_read_batches")); assertEquals(2, pipeline.history().size)
    }
}
