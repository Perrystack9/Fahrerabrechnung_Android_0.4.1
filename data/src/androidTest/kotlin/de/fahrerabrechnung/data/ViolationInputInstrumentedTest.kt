package de.fahrerabrechnung.data

import android.content.Context
import androidx.room.withTransaction
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import de.fahrerabrechnung.data.card.*
import de.fahrerabrechnung.data.db.*
import de.fahrerabrechnung.data.security.EncryptedDatabaseFactory
import de.fahrerabrechnung.data.violation.*
import de.fahrerabrechnung.domain.model.ActivityType
import de.fahrerabrechnung.domain.violation.input.*
import java.security.SecureRandom
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.ZoneOffset
import java.util.TimeZone
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collect
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ViolationInputInstrumentedTest {
    private lateinit var context: Context
    private lateinit var db: AppDatabase
    private lateinit var passphrase: ByteArray
    private lateinit var activities: CardActivityImportRepository
    private lateinit var imports: DriverCardImportRepository
    private lateinit var pipeline: DriverCardImportPipeline
    private lateinit var contexts: ViolationContextRepository
    private lateinit var adapter: DriverCardInputAdapter
    private val clock = Clock.fixed(Instant.parse("2026-10-15T07:00:00Z"), ZoneOffset.UTC)
    @Before fun before() {
        context = ApplicationProvider.getApplicationContext(); context.deleteDatabase(AppDatabase.DATABASE_NAME)
        passphrase = ByteArray(32).also(SecureRandom()::nextBytes); open()
    }
    private fun open() {
        db = EncryptedDatabaseFactory.create(context, passphrase)
        activities = CardActivityImportRepository(db.cardActivityImportDao(), clock)
        val vehicles = CardVehicleImportRepository(db.cardVehicleImportDao(), clock)
        imports = DriverCardImportRepository(db, activities, vehicles, CardSourcePairRepository(db, activities, vehicles, clock), clock)
        pipeline = DriverCardImportPipeline(db, imports, activities, vehicles, clock)
        contexts = ViolationContextRepository(db, clock); adapter = DriverCardInputAdapter(db, imports, contexts)
    }
    @After fun after() { db.close(); context.deleteDatabase(AppDatabase.DATABASE_NAME) }
    private suspend fun snapshot(): ViolationInputSnapshot {
        val captured = adapter.capture()
        return InputSnapshotBuilder.build(checkNotNull(captured.sources), checkNotNull(captured.context))
    }
    private fun minutes(snapshot: ViolationInputSnapshot, activity: ActivityType) = snapshot.intervals
        .filter { it.meaning.activity == activity }.sumOf { Duration.between(it.range.start, it.range.end).toMinutes() }

    @Test fun mi01_all32RawWordsPassThroughExistingParserStoreAndAdapterWithActualBitSemantics() = runBlocking {
        val words = mutableListOf(0)
        for (s in 0..1) for (c in 0..1) for (p in 0..1) for (aa in 0..3)
            words += s * 32768 + c * 16384 + p * 8192 + aa * 2048 + 360 + words.size - 1
        words += 600
        val read = M1InputFixture.read(CardGeneration.GEN1, *words.toIntArray())
        imports.bind(checkNotNull(read.card), true); pipeline.process(read, "all-raw-bits")
        val observations = snapshot().observations.drop(1).dropLast(1)
        assertEquals(32, observations.size)
        for ((index, observed) in observations.withIndex()) {
            val raw = words[index + 1]
            val aa = (raw ushr 11) and 3
            val p = raw and 8192 != 0; val c = raw and 16384 != 0
            val recorded = observed.rawChanges.single()
            assertEquals(360 + index, recorded.minuteOfDay)
            assertEquals(!p, recorded.cardInserted); assertEquals(c, recorded.rawC)
            assertEquals(raw and 32768 != 0, recorded.coDriverSlot)
            if (p && (!c || aa == 3)) assertEquals(InputQuality.UNKNOWN, observed.meaning.quality)
            else {
                assertEquals(InputQuality.KNOWN, observed.meaning.quality)
                assertEquals(listOf(ActivityType.PAUSE_RUHE, ActivityType.BEREITSCHAFT,
                    ActivityType.SONSTIGE_ARBEIT, ActivityType.LENKZEIT)[aa], observed.meaning.activity)
                assertEquals(p && c, observed.meaning.manualTachograph)
                assertEquals(if (p) CrewEvidence.NOT_RECORDED else if (c) CrewEvidence.CREW else CrewEvidence.SOLO, observed.meaning.crew)
            }
        }
    }

    @Test fun mi05_mi06_equalMinuteIndicesRemainExactInActualNormalizedQueriesAndAdapter() = runBlocking {
        pipeline.processApplications(M1InputFixture.batch(0, 0x11e0, 0x31e0, 0x1258), "equal-ambiguous", true)
        val snapshot = snapshot()
        assertEquals(120, minutes(snapshot, ActivityType.UNBEKANNT))
        val key = snapshot.sources.first().key
        val days = db.driverActivityDao().sourceDays(key.cardId, checkNotNull(key.sourceId))
        val changes = db.driverActivityDao().sourceChanges(key.cardId, checkNotNull(key.sourceId))
        assertEquals(listOf(0, 1, 2, 3), changes.map { it.changeIndex })
        assertEquals(listOf(0, 480, 480, 600), changes.map { it.minuteOfDay })
        assertEquals(listOf(true, true, false, true), changes.map { it.cardInserted })
        assertEquals(0, days.single().recordIndex)
        assertTrue(db.driverActivityDao().sourceDays("foreign-card", checkNotNull(key.sourceId)).isEmpty())
        assertTrue(db.driverActivityDao().sourceChanges(key.cardId, "foreign-source").isEmpty())
        assertEquals(listOf(1, 2), snapshot.observations.first { it.at == M1InputFixture.day.plusSeconds(28800) }.references.map { it.changeIndex })
        pipeline.processApplications(M1InputFixture.batch(0, 0x11e0, 0x11e0, 0x0258), "equal-compatible")
        val compatible = snapshot()
        assertEquals(120, minutes(compatible, ActivityType.SONSTIGE_ARBEIT))
        assertEquals(0, minutes(compatible, ActivityType.UNBEKANNT))
        val repeated = compatible.observations.first { it.at == M1InputFixture.day.plusSeconds(28800) &&
            it.meaning.quality == InputQuality.KNOWN && it.references.size == 2 }
        assertEquals(listOf(1, 2), repeated.references.map { it.changeIndex })
    }

    @Test fun mi10_actualCompleteChainCloses660MinutesUsingBothSourceRecordEnds() = runBlocking {
        val first = M1InputFixture.record(0x1000, 0x6528)
        val next = M1InputFixture.record(0x6000, 0x121c, date = M1InputFixture.day.plusSeconds(86400), previous = first.size)
        val original = CardCoreSnapshot(CardCoreEf.ACTIVITY, CardGeneration.GEN1,
            java.nio.ByteBuffer.allocate(4 + first.size + next.size).putShort(0).putShort(first.size.toShort()).put(first).put(next).array())
        val read = M1InputFixture.read().copy(coreReads = mapOf(CardCoreEf.ACTIVITY to CardCoreRead(CardCoreEf.ACTIVITY, CardCoreReadStatus.COMPLETE, original)))
        imports.bind(checkNotNull(read.card), true); pipeline.process(read, "midnight-chain")
        val snapshot = snapshot()
        assertEquals(660, minutes(snapshot, ActivityType.PAUSE_RUHE))
        val bridge = snapshot.intervals.single { it.range.start == M1InputFixture.day.plusSeconds(79200) }
        assertEquals(EndEvidenceKind.NEXT_UTC_DAY, bridge.support.single().kind)
        assertEquals(0, bridge.support.single().begin.single().recordIndex)
        assertEquals(1, bridge.support.single().end.single().recordIndex)
        assertEquals(M1InputFixture.day.plusSeconds(118800), snapshot.latestClosedEnd)
    }

    @Test fun mi09_real0507SessionBeginAndLaterAppImportAreNeverAnActivityEnd() = runBlocking {
        val usage = java.nio.ByteBuffer.allocate(19).putInt(M1InputFixture.day.plusSeconds(50400).epochSecond.toInt())
            .put(13).put(1).put("TEST OWN     ".toByteArray()).array()
        val read = M1InputFixture.read(CardGeneration.GEN1, 0, 0x1258).copy(metadata = mapOf(
            CardMetadataKind.CURRENT_USAGE to CardMetadataRead(CardMetadataFile(CardMetadataKind.CURRENT_USAGE, CardGeneration.GEN1, usage))))
        imports.bind(checkNotNull(read.card), true); pipeline.process(read, "usage-not-end")
        val snapshot = snapshot()
        assertEquals(M1InputFixture.day.plusSeconds(36000), snapshot.latestObservation)
        assertEquals(M1InputFixture.day.plusSeconds(36000), snapshot.latestClosedEnd)
        assertEquals(0, minutes(snapshot, ActivityType.SONSTIGE_ARBEIT))
        assertTrue(snapshot.sources.single().metadataDigests.keys.any { it.startsWith("metadata-CURRENT_USAGE-") })
        assertTrue(snapshot.issues.any { it.code == InputIssueCode.OPEN_END })
        assertEquals(FileAvailability.NOT_SUPPORTED, snapshot.sources.single().files.single { it.family == InputFamily.LOAD_TYPE }.availability)
    }

    @Test fun signatureEvidenceStaysBoundToExactReceiptWithoutNewMathOrTrustClaim() = runBlocking {
        val unsigned = M1InputFixture.read()
        val original = unsigned.coreReads.getValue(CardCoreEf.ACTIVITY).snapshot
        val signature = CardCoreSignature(CardCoreEf.ACTIVITY, CardGeneration.GEN1,
            CardSignatureStatus.CAPTURED_UNVERIFIED, CardSignaturePolicy.RSA_SHA1, CardSignaturePhase.SIGN,
            statusWord = 0x9000, hashStatusWord = 0x9000, signStatusWord = 0x9000, actualLength = 128,
            original = original, bytes = ByteArray(128) { 7 })
        imports.bind(checkNotNull(unsigned.card), true)
        pipeline.process(unsigned.copy(coreSignatures = mapOf(CardCoreEf.ACTIVITY to signature)), "signed-m1")
        val before = db.cardCoreSignatureDao().receipts("signed-m1")
        val source = snapshot().sources.single()
        val evidence = source.receipts.single()
        assertEquals(before.single().fingerprint, evidence.fingerprint)
        assertEquals(before.single().originalSourceId, evidence.originalSourceId)
        assertEquals(source.files.first().completeOriginalDigest, evidence.originalDigest)
        assertEquals("NOT_RUN_BY_M1", evidence.mathematicalStatus)
        assertEquals("NOT_EVALUATED", evidence.certificateTrust)
        val after = db.cardCoreSignatureDao().receipts("signed-m1")
        assertEquals(before.single().fingerprint, after.single().fingerprint)
        assertArrayEquals(before.single().bytes, after.single().bytes)
    }

    @Test fun mi14_bothApplicationsExactOriginalsAndReplaySurviveEncryptedReopenWithoutMutation() = runBlocking {
        pipeline.processApplications(M1InputFixture.batch(), "m1-both", true)
        val profile = checkNotNull(db.driverProfileDao().localProfile())
        val bindings = db.driverProfileDao().imports(profile.id)
        val rawBefore = db.cardActivityImportDao().all().associate { it.id to it.dailyRecords.toList() }
        val before = snapshot()
        assertEquals(2, before.sources.size)
        assertEquals(setOf("GEN1", "GEN2"), before.sources.map { it.key.application }.toSet())
        assertEquals(120, minutes(before, ActivityType.SONSTIGE_ARBEIT))
        assertEquals(setOf("m1-both"), before.sources.map { it.key.batchId }.toSet())
        assertEquals(bindings.map { it.sessionId }.toSet(), before.sources.map { it.key.sessionId }.toSet())
        for (source in before.sources) {
            val saved = checkNotNull(imports.get(source.key.sessionId))
            val original = saved.coreSnapshots.getValue(CardCoreEf.ACTIVITY)
            val evidence = source.files.single { it.family == InputFamily.ACTIVITIES }
            assertEquals(original.id, evidence.completeOriginalId)
            assertEquals(InputRevision.digest(original.snapshot.bytes), evidence.completeOriginalDigest)
        }
        pipeline.processApplications(M1InputFixture.batch(), "m1-both")
        assertEquals(before.revision, snapshot().revision)
        pipeline.processApplications(M1InputFixture.batch(), "m1-compatible-new-origin")
        val additional = snapshot()
        assertEquals(before.dataRevision, additional.dataRevision)
        assertNotEquals(before.provenanceRevision, additional.provenanceRevision)
        assertEquals(120, minutes(additional, ActivityType.SONSTIGE_ARBEIT))
        assertEquals(rawBefore, db.cardActivityImportDao().all().associate { it.id to it.dailyRecords.toList() })
        db.close(); open()
        assertEquals(additional.revision, snapshot().revision)
        assertEquals(rawBefore, db.cardActivityImportDao().all().associate { it.id to it.dailyRecords.toList() })
        assertTrue(db.violationContextDao().history(profile.id).isEmpty())
    }

    @Test fun mi02_mi03_manualReplacementKeepsUnknownOriginalAndDoesNotBecomeCrew() = runBlocking {
        pipeline.processApplications(M1InputFixture.batch(0x1000, 0x21e0, 0x1258), "old-unknown", true)
        val oldRaw = db.cardActivityImportDao().all().associate { it.id to it.dailyRecords.toList() }
        assertEquals(120, minutes(snapshot(), ActivityType.UNBEKANNT))
        pipeline.processApplications(M1InputFixture.batch(0x1000, 0x61e0, 0x1258), "known-manual")
        val after = snapshot()
        assertEquals(120, minutes(after, ActivityType.PAUSE_RUHE))
        assertEquals(0, minutes(after, ActivityType.UNBEKANNT))
        val last = after.intervals.last()
        assertTrue(last.meaning.manualTachograph); assertEquals(CrewEvidence.NOT_RECORDED, last.meaning.crew)
        val allRaw = db.cardActivityImportDao().all().associate { it.id to it.dailyRecords.toList() }
        oldRaw.forEach { (id, bytes) -> assertEquals(bytes, allRaw.getValue(id)) }
        assertEquals(4, last.support.flatMap { it.begin }.map { it.source.sessionId }.toSet().size)
    }

    @Test fun mi24_unboundArchiveExcludedBoundLegacyRawFallbackNoBackfillOrNewestWinner() = runBlocking {
        val read = M1InputFixture.read(fullOriginal = false)
        val cardId = imports.bind(checkNotNull(read.card), true)
        val profile = checkNotNull(db.driverProfileDao().localProfile())
        val source = activities.prepare(read)
        val unrelated = activities.prepare(M1InputFixture.read(CardGeneration.GEN1, 0, 0x19e0, 0x0258, number = "ARCHIVCARD123456"))
        db.cardActivityImportDao().insert(source); db.cardActivityImportDao().insert(unrelated)
        db.withTransaction {
            db.driverProfileDao().insertSession(ImportSessionEntity("bound-legacy", profile.id, "SAVED", 1, 1))
            db.driverProfileDao().insertSources(DriverImportSourcesEntity("bound-legacy", cardId, source.id, null, null, 1))
        }
        assertTrue(db.driverActivityDao().sourceDays(cardId, source.id).isEmpty())
        val first = snapshot()
        assertEquals(1, first.sources.size)
        assertEquals(120, minutes(first, ActivityType.SONSTIGE_ARBEIT))
        assertNull(first.sources.single().key.batchId)
        assertNull(first.sources.single().files.first().completeOriginalId)
        assertTrue(db.driverActivityDao().sourceDays(cardId, source.id).isEmpty())
        // A newer actual conflicting driving observation must not silently replace work.
        pipeline.processApplications(M1InputFixture.batch(0, 0x19e0, 0x0258), "conflicting-new")
        val conflict = snapshot()
        assertEquals(120, minutes(conflict, ActivityType.UNBEKANNT))
        assertEquals(InputQuality.CONFLICT, conflict.intervals.last().meaning.quality)
        assertTrue(conflict.sources.none { it.key.sourceId == unrelated.id })
        assertTrue(db.driverActivityDao().sourceDays(cardId, source.id).isEmpty())
        assertFalse(ContextKey.entries.any { it.name.contains("ARTICLE_12") || it.name.contains("COMPENSATION") })
    }

    @Test fun mi14_dayFingerprintIgnoresRingPositionButSeparatesCardBitsAndOrder() {
        val original = CardActivityParser.parse(M1InputFixture.read().activity!!).single()
        val id = CardActivityDayFingerprint.id("C1", original)
        assertEquals(id, CardActivityDayFingerprint.id("C1", original.copy(ringOffset = 600)))
        assertNotEquals(id, CardActivityDayFingerprint.id("C2", original))
        assertNotEquals(id, CardActivityDayFingerprint.id("C1", original.copy(changes = original.changes.mapIndexed { index, c ->
            if (index == 1) c.copy(crewDriving = !c.crewDriving) else c })))
        assertNotEquals(id, CardActivityDayFingerprint.id("C1", original.copy(changes = original.changes.mapIndexed { index, c ->
            if (index == 1) c.copy(cardInserted = !c.cardInserted) else c })))
        assertNotEquals(id, CardActivityDayFingerprint.id("C1", original.copy(changes = original.changes.reversed())))
    }

    @Test fun mi13_emptyOriginalAndActualZeroWordRemainDifferent() = runBlocking {
        val empty = M1InputFixture.read().let { read -> read.copy(coreReads = mapOf(CardCoreEf.ACTIVITY to
            CardCoreRead(CardCoreEf.ACTIVITY, CardCoreReadStatus.COMPLETE, CardCoreSnapshot(CardCoreEf.ACTIVITY, CardGeneration.GEN1, ByteArray(68))))) }
        imports.bind(checkNotNull(empty.card), true)
        pipeline.process(empty, "empty-original")
        val first = snapshot()
        assertEquals(FileAvailability.EMPTY, first.sources.single().files.single { it.family == InputFamily.ACTIVITIES }.availability)
        assertNull(first.sources.single().key.sourceId)
        assertTrue(first.observations.isEmpty()); assertTrue(first.intervals.isEmpty())
        pipeline.process(M1InputFixture.read(CardGeneration.GEN1, 0), "actual-zero")
        val second = snapshot()
        assertEquals(1, second.observations.size); assertTrue(second.intervals.isEmpty())
        assertNotEquals(first.revision, second.revision)
        assertNotEquals(first.dataRevision, second.dataRevision)
    }

    @Test fun mi18_mi21_actualConditionAndPassengerPointsRetainPhysicalRecordAndDoNotProveScope() = runBlocking {
        val read = M1InputFixture.read(CardGeneration.GEN2V2)
        val conditions = ByteArray(CardSpecificConditionParser.fileLength(CardGeneration.GEN2V2, 112)).also {
            java.nio.ByteBuffer.wrap(it).putShort(0).putInt(M1InputFixture.day.epochSecond.toInt()).put(1)
        }
        val load = ByteArray(CardLoadTypeParser.fileLength(CardGeneration.GEN2V2, 336)).also {
            java.nio.ByteBuffer.wrap(it).putShort(0).putInt(M1InputFixture.day.plusSeconds(120).epochSecond.toInt()).put(2)
        }
        imports.bind(checkNotNull(read.card), true)
        pipeline.process(read.copy(specificConditions = CardSpecificConditionFile(CardGeneration.GEN2V2, 112, conditions),
            loadType = CardLoadTypeFile(CardGeneration.GEN2V2, 336, load)), "points")
        val snapshot = snapshot()
        val oos = snapshot.points.single { it.family == InputFamily.SPECIAL_CONDITIONS }
        assertEquals(0, oos.recordIndex); assertEquals("1", oos.fields["typeRaw"])
        assertEquals(M1InputFixture.day, oos.timeUtc)
        val passengers = snapshot.points.single { it.family == InputFamily.LOAD_TYPE }
        assertEquals("2", passengers.fields["loadTypeRaw"]); assertEquals(0, passengers.recordIndex)
        assertTrue(snapshot.scopes.all { segment -> segment.decisions.all { it.status == ScopeStatus.UNKNOWN } })
        assertTrue(snapshot.contextNeeds.any { it.key == ContextKey.PASSENGER_SERVICE })
        assertTrue(snapshot.contextNeeds.none { it.key == ContextKey.FERRY_SLEEPING_FACILITY })
    }

    @Test fun readFailureInSecondApplicationStaysExplicitAndNeverAddsActivity() = runBlocking {
        val batch = CardApplicationReadBatch(listOf(CardApplicationAttempt.read(CardApplication.GEN1, M1InputFixture.read()),
            CardApplicationAttempt.failed(CardApplication.GEN2, ReaderFailure.IO)), emptyList())
        pipeline.processApplications(batch, "partial", true)
        val snapshot = snapshot()
        assertEquals(1, snapshot.sources.size)
        assertEquals(120, minutes(snapshot, ActivityType.SONSTIGE_ARBEIT))
        val issue = snapshot.issues.single { it.code == InputIssueCode.INCOMPLETE_APPLICATION }
        assertEquals("partial", issue.batchId); assertEquals("GEN2", issue.application)
        assertTrue(snapshot.manifest.containsKey("batch-partial-GEN2"))
    }

    @Test fun corruptExactSourceRejectsItWithoutBorrowingNewerOriginalOrHidingOtherValidImport() = runBlocking {
        pipeline.processApplications(M1InputFixture.batch(), "first", true)
        val firstBindings = db.driverProfileDao().imports(checkNotNull(db.driverProfileDao().localProfile()).id)
        val damaged = firstBindings.single { db.cardActivityImportDao().getById(it.activityId!!)?.generation == "GEN1" }
        db.openHelper.writableDatabase.execSQL("UPDATE card_activity_imports SET dailyRecords = X'01' WHERE id = ?", arrayOf(damaged.activityId))
        val result = snapshot()
        assertEquals(1, result.sources.size)
        assertEquals("GEN2V2", result.sources.single().key.generation)
        assertEquals(120, minutes(result, ActivityType.SONSTIGE_ARBEIT))
        assertTrue(result.issues.any { it.code == InputIssueCode.SOURCE_REJECTED })
    }

    @Test fun mi23_displayZoneAndDefaultClockNeverChangeInputRevision() = runBlocking {
        pipeline.processApplications(M1InputFixture.batch(), "zone", true)
        val originalZone = TimeZone.getDefault()
        try {
            val revisions = listOf("UTC", "Europe/Berlin", "America/New_York").map {
                TimeZone.setDefault(TimeZone.getTimeZone(it)); snapshot().revision
            }
            assertEquals(1, revisions.distinct().size)
        } finally { TimeZone.setDefault(originalZone) }
    }

    @Test fun publicationRejectsOldContextOrImportRevisionAndCancellationDoesNotDeleteImport() = runBlocking {
        pipeline.processApplications(M1InputFixture.batch(), "publication", true)
        val captured = adapter.capture()
        val profile = checkNotNull(captured.sources).profileId
        contexts.replace(profile, "none", listOf(ContextAssertion(ContextKey.MAX_MASS_KG, ContextValue.Number(40000),
            ContextSubject(ContextSubjectKind.PROFILE, profile), M1InputFixture.day, null, ContextOrigin.USER_CONTEXT, "test-profile", clock.instant())))
        var published = false
        assertFalse(adapter.publishIfCurrent(captured.token) { published = true }); assertFalse(published)
        val current = adapter.capture()
        assertTrue(adapter.publishIfCurrent(current.token) { published = true }); assertTrue(published)
        pipeline.processApplications(M1InputFixture.batch(), "publication-new-origin")
        assertFalse(adapter.publishIfCurrent(current.token) { fail("Old import revision published") })
        val job = launch { currentCoroutineContext().cancel(); adapter.capture() }
        job.join(); assertTrue(job.isCancelled)
        assertEquals(4, snapshot().sources.size)
    }

    @Test fun observerPublishesCompleteCommittedChildrenAndContextAndSurvivesRollback() = runBlocking {
        val events = Channel<ViolationInputState>(Channel.UNLIMITED)
        val repository = ViolationInputRepository(db, adapter)
        val observer = launch(Dispatchers.Default) { repository.observe().collect { events.send(it) } }
        suspend fun nextReady(): ViolationInputSnapshot = withTimeout(20000) {
            while (true) {
                val value = events.receive()
                if (value is ViolationInputState.Failed) fail("Input observer failed")
                if (value is ViolationInputState.Ready) return@withTimeout value.snapshot
            }
            error("unreachable")
        }
        try {
            withTimeout(20000) { while (events.receive() != ViolationInputState.NoProfile) { } }
            pipeline.processApplications(M1InputFixture.batch(), "observed", true)
            val first = nextReady()
            assertEquals(2, first.sources.size)
            val profile = first.profileId
            val revised = contexts.replace(profile, "none", listOf(ContextAssertion(ContextKey.MAX_MASS_KG, ContextValue.Number(40000),
                ContextSubject(ContextSubjectKind.PROFILE, profile), M1InputFixture.day, null, ContextOrigin.USER_CONTEXT, "test", clock.instant())))
            val second = nextReady()
            assertEquals(revised.revisionId, second.context.revisionId)
            assertNotEquals(first.revision, second.revision)
            try { db.withTransaction { contexts.replace(profile, revised.revisionId, emptyList()); error("synthetic rollback") } }
            catch (_: IllegalStateException) { }
            assertEquals(second.revision, snapshot().revision)
        } finally { observer.cancelAndJoin(); events.close() }
    }
}
