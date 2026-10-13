package de.fahrerabrechnung.data

import android.content.Context
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
            for (table in listOf("card_batch_master_files", "card_application_attempts", "card_master_sources", "card_read_batches"))
                execSQL("DROP TABLE $table")
            execSQL("DROP INDEX index_card_import_runs_id_driverCardId")
            execSQL("DROP INDEX index_driver_import_sources_sessionId_driverCardId")
            execSQL("DELETE FROM room_master_table")
            version = 18
        }
        db.close(); open()
        assertEquals(19, db.openHelper.writableDatabase.version)
        assertEquals(old, pipeline.process(oldRead, "before-upgrade"))
        assertArrayEquals(bytes, imports.get(old.sessionId!!)!!.identitySources.getValue(CardIdentitySourceKind.IDENTIFICATION).file.bytes)
        assertEquals(0, count("card_read_batches")); assertEquals(0, count("card_application_attempts"))
        assertNull(imports.get(old.sessionId!!)!!.batchId)
        pipeline.processApplications(batch(), "after-upgrade")
        assertEquals(1, count("card_read_batches")); assertEquals(2, pipeline.history().size)
    }
}
