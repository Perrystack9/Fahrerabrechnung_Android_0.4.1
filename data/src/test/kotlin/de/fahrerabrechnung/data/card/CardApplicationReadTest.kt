package de.fahrerabrechnung.data.card

import java.io.ByteArrayOutputStream
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.async
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.yield
import org.junit.Assert.*
import org.junit.Test

/** Synthetic two-DF transport. A SELECT never supplies bytes from the previous DF. */
private class DualApplicationTransport(
    val generations: Map<CardApplication, CardGeneration> = mapOf(CardApplication.GEN1 to CardGeneration.GEN1,
        CardApplication.GEN2 to CardGeneration.GEN2V2),
    val identities: Map<CardApplication, ByteArray> = emptyMap(),
    val missing: Set<Pair<CardApplication, Int>> = emptySet(),
    val errors: Map<Pair<CardApplication, Int>, Exception> = emptyMap(),
    val selectionErrors: Map<CardApplication, Exception> = emptyMap(),
    val malformedApplication: CardApplication? = null,
    val oversizedIdentity: CardApplication? = null,
) : CardReaderTransport {
    var opens = 0; var powers = 0; var offs = 0; var closes = 0
    val commands = mutableListOf<ByteArray>()
    val reads = mutableListOf<Triple<CardApplication?, Int, Int>>()
    var selected: CardApplication? = null
    private var file = 0
    override suspend fun availability() = SourceAvailability.AVAILABLE
    override suspend fun open(): ReaderState { opens++; return ReaderState.CardPresent }
    override suspend fun powerOn(): ByteArray { powers++; selected = null; file = 0; return byteArrayOf(0x3B, 0) }
    override suspend fun powerOff() { offs++ }
    override fun close() { closes++ }
    override suspend fun exchange(apdu: ByteArray): ByteArray {
        commands += apdu.copyOf()
        val ins = apdu[1].toInt() and 255
        if (ins == 0xA4 && apdu[2].toInt() == 4) {
            val application = CardApplication.entries.single { it.aid.contentEquals(apdu.copyOfRange(5, apdu.size)) }
            selected = null; file = 0
            selectionErrors[application]?.let { throw it }
            if (application !in generations) return status(0x6A82)
            selected = application
            return status(0x9000)
        }
        if (ins == 0xA4) {
            file = (apdu[5].toInt() and 255) shl 8 or (apdu[6].toInt() and 255)
            if (selected?.let { it to file in missing } == true) return status(0x6A82)
            if (bytes() == null) return status(0x6A82)
            return status(0x9000)
        }
        if (ins == 0x2A) return status(0x6D00) // synthetic legacy reader: signatures unsupported
        check(ins == 0xB0)
        selected?.let { errors[it to file]?.let { error -> throw error } }
        val offset = (apdu[2].toInt() and 255) shl 8 or (apdu[3].toInt() and 255)
        reads += Triple(selected, file, offset)
        val raw = checkNotNull(bytes())
        if (offset >= raw.size) return status(0x6B00)
        val le = (apdu[4].toInt() and 255).let { if (it == 0) 256 else it }
        // Small chunks exercise independent capacity-based bounded reads in both DFs.
        val length = minOf(le, 7, raw.size - offset)
        return raw.copyOfRange(offset, offset + length) + status(0x9000)
    }
    private fun status(word: Int) = byteArrayOf((word ushr 8).toByte(), word.toByte())
    private fun bytes(): ByteArray? {
        val application = selected ?: return when (file) {
            0x0002 -> ByteArray(25)
            0x0005 -> ByteArray(8)
            0x2F00 -> DirectoryFixtures.bytes()
            else -> null
        }
        val generation = generations.getValue(application)
        val gen1 = application == CardApplication.GEN1
        val capacity = if (gen1) 1 else 2
        return when (file) {
            DriverCardFiles.APPLICATION -> ByteArray(if (gen1) 10 else 17).also {
                it[0] = if (application == malformedApplication) 2 else 1
                it[1] = 1; it[2] = if (generation == CardGeneration.GEN2V2) 1 else 0
                it[3] = 1; it[4] = 1; it[6] = if (gen1) 12 else 16; it[8] = capacity.toByte()
                if (gen1) it[9] = 1 else { it[10] = 1; it[12] = 1; it[14] = 1; it[16] = 1 }
            }
            DriverCardFiles.IDENTIFICATION -> (identities[application] ?: dualIdentity()).let {
                if (application == oversizedIdentity) it + byteArrayOf(1) else it
            }
            DriverCardFiles.DRIVER_ACTIVITY -> ByteArray(4 + if (gen1) 12 else 16)
            DriverCardFiles.VEHICLES_USED -> ByteArray(2 + capacity * if (gen1) 31 else 48)
            DriverCardFiles.CONTROL_ACTIVITY -> ByteArray(CardControlParser.FILE_LENGTH)
            CardEventFaultKind.EVENTS.fileId -> ByteArray(CardEventFaultParser.fileLength(CardEventFaultKind.EVENTS, generation, 1))
            CardEventFaultKind.FAULTS.fileId -> ByteArray(CardEventFaultParser.fileLength(CardEventFaultKind.FAULTS, generation, 1))
            DriverCardFiles.SPECIFIC_CONDITIONS -> ByteArray(CardSpecificConditionParser.fileLength(generation, if (gen1) 56 else 1))
            DriverCardFiles.PLACES -> ByteArray(CardPlacesParser.fileLength(generation, 1))
            DriverCardFiles.APPLICATION_V2 -> if (generation == CardGeneration.GEN2V2) ByteArray(10).also {
                it[1] = 8; it[3] = 1; it[5] = 1; it[7] = 1
            } else null
            else -> CardMetadataKind.entries.firstOrNull { it.fileId == file && !it.inMasterFile }?.let {
                ByteArray(it.fileLength)
            } ?: CardCertificateKind.entries.firstOrNull { it.fileId == file && it.supports(generation) }?.let {
                // Full blank certificates remain real independent sources, with no trust assertion.
                ByteArray(if (gen1) 194 else 204) { if (gen1) 0 else -1 }
            }
        }
    }
}

private fun dualIdentity(number: String = "TESTCARD12345678", surname: String = "MUSTER"): ByteArray =
    ByteArrayOutputStream().apply {
        fun name(value: String) { write(1); write(value.padEnd(35, ' ').toByteArray()) }
        write(1); write(number.toByteArray()); name("TEST AUTHORITY")
        write(byteArrayOf(0x65, 0x53, 0xF1.toByte(), 0)); write(byteArrayOf(0x65, 0x53, 0xF1.toByte(), 0))
        write(byteArrayOf(0x75, 0x53, 0xF1.toByte(), 0)); name(surname); name("ERIKA")
        write(byteArrayOf(0x20, 0, 0x02, 0x29)); write("DE".toByteArray())
    }.toByteArray()

class CardApplicationReaderTest {
    @Test fun poweredSessionsCannotInterleaveSelectionsOrResetAnotherCapture() = runBlocking {
        val delegate = DualApplicationTransport()
        val entered = CompletableDeferred<Unit>()
        val release = CompletableDeferred<Unit>()
        var first = true
        val transport = object : CardReaderTransport by delegate {
            override suspend fun exchange(apdu: ByteArray): ByteArray {
                if (first) { first = false; entered.complete(Unit); release.await() }
                return delegate.exchange(apdu)
            }
        }
        val reader = DriverCardReader(transport)
        val a = async { reader.readApplications() }
        entered.await()
        val b = async { reader.readApplications() }
        yield()
        assertEquals(1, delegate.opens); assertEquals(1, delegate.powers)
        release.complete(Unit)
        assertEquals(CardApplicationIdentityAgreement.AGREED, a.await().identityAgreement)
        assertEquals(CardApplicationIdentityAgreement.AGREED, b.await().identityAgreement)
        assertEquals(2, delegate.opens); assertEquals(2, delegate.offs); assertEquals(2, delegate.closes)
    }

    @Test fun bothDfsUseOnePoweredInsertionWithIndependentCapacitiesAndSources() = runBlocking {
        val fake = DualApplicationTransport(); val batch = DriverCardReader(fake).readApplications()
        assertEquals(CardApplicationIdentityAgreement.AGREED, batch.identityAgreement)
        assertEquals(listOf(CardApplication.GEN1, CardApplication.GEN2), batch.applications.map { it.application })
        val g1 = batch.applications[0].result!!; val g2 = batch.applications[1].result!!
        assertEquals(CardGeneration.GEN1, g1.generation); assertEquals(CardGeneration.GEN2V2, g2.generation)
        assertEquals(g1.card!!.card, g2.card!!.card); assertEquals(g1.card!!.holder, g2.card!!.holder)
        assertEquals(12, g1.activity!!.dailyRecords.size); assertEquals(16, g2.activity!!.dailyRecords.size)
        assertEquals(1, g1.vehicles!!.recordCount); assertEquals(2, g2.vehicles!!.recordCount)
        assertEquals(31, g1.vehicles!!.records.size); assertEquals(96, g2.vehicles!!.records.size)
        for (read in listOf(g1, g2)) {
            assertEquals(CardCoreEf.entries.toSet(), read.coreReads.keys)
            assertTrue(read.coreReads.values.all { it.status == CardCoreReadStatus.COMPLETE })
            assertEquals(read.generation, read.coreReads.getValue(CardCoreEf.ACTIVITY).snapshot!!.generation)
            assertEquals(read.activity!!.dailyRecords.size + 4,
                read.coreReads.getValue(CardCoreEf.ACTIVITY).snapshot!!.bytes.size)
        }
        assertEquals(10, g1.identitySources.getValue(CardIdentitySourceKind.APPLICATION).file!!.bytes.size)
        assertEquals(17, g2.identitySources.getValue(CardIdentitySourceKind.APPLICATION).file!!.bytes.size)
        assertEquals(194, g1.certificates.getValue(CardCertificateKind.CARD).file!!.bytes.size)
        assertEquals(204, g2.certificates.getValue(CardCertificateKind.CARD).file!!.bytes.size)
        assertEquals(1, fake.opens); assertEquals(1, fake.powers); assertEquals(1, fake.offs); assertEquals(1, fake.closes)
        assertTrue(fake.commands.all { (it[1].toInt() and 255) in setOf(0xA4, 0xB0) ||
            it.contentEquals(CardCommands.performHashOfFile().encode()) })
        assertFalse(fake.reads.any { it.second == 0x0540 })
    }
    @Test fun masterCapturesBelongToEnvelopeAndNeverAppearInEitherApplicationMap() = runBlocking {
        val fake = DualApplicationTransport(); val batch = DriverCardReader(fake).readApplications()
        assertEquals(setOf(2, 5, 0x2F00), batch.masterFiles.map { it.fileId }.toSet())
        assertArrayEquals(DirectoryFixtures.bytes(), batch.masterFiles.single { it.fileId == 0x2F00 }.bytes)
        batch.applications.forEach {
            assertTrue(it.result!!.metadata.keys.none { kind -> kind.inMasterFile })
            assertNull(it.result!!.directory); assertFalse(it.result!!.directoryReadFailed)
            assertTrue(it.result!!.communication.isEmpty())
        }
        assertTrue(fake.reads.filter { it.second in setOf(2, 5, 0x2F00) }.all { it.first == null })
    }
    @Test fun absentApplicationDoesNotBorrowPreviousDfAndSiblingRemainsReadable() = runBlocking {
        for (present in CardApplication.entries) {
            val generation = if (present == CardApplication.GEN1) CardGeneration.GEN1 else CardGeneration.GEN2
            val fake = DualApplicationTransport(generations = mapOf(present to generation))
            val batch = DriverCardReader(fake).readApplications()
            assertEquals(CardApplicationReadStatus.ABSENT, batch.applications.single { it.application != present }.status)
            assertNull(batch.applications.single { it.application != present }.result)
            assertEquals(generation, batch.applications.single { it.application == present }.result!!.generation)
            assertEquals(CardApplicationIdentityAgreement.AGREED, batch.identityAgreement)
        }
    }
    @Test fun neitherAidPresentHasNoDriverIdentityOrSuccessfulApplicationClaim() = runBlocking {
        val batch = DriverCardReader(DualApplicationTransport(generations = emptyMap())).readApplications()
        assertEquals(CardApplicationIdentityAgreement.NO_DRIVER, batch.identityAgreement)
        assertTrue(batch.applications.all { it.status == CardApplicationReadStatus.ABSENT && it.result == null })
    }
    @Test fun oneApplicationFileErrorDoesNotDiscardOtherFilesOrTheOtherDf() = runBlocking {
        for (application in CardApplication.entries) {
            val fake = DualApplicationTransport(missing = setOf(application to DriverCardFiles.DRIVER_ACTIVITY))
            val batch = DriverCardReader(fake).readApplications()
            val damaged = batch.applications.single { it.application == application }.result!!
            assertNull(damaged.activity); assertTrue(damaged.activityReadFailed); assertNotNull(damaged.vehicles)
            assertNotNull(batch.applications.single { it.application != application }.result!!.activity)
            assertEquals(CardApplicationIdentityAgreement.AGREED, batch.identityAgreement)
        }
    }
    @Test fun applicationIdentificationFailureIsSeparateFromAbsenceAndSiblingSources() = runBlocking {
        for (application in CardApplication.entries) {
            val fake = DualApplicationTransport(missing = setOf(application to DriverCardFiles.APPLICATION))
            val batch = DriverCardReader(fake).readApplications()
            val failed = batch.applications.single { it.application == application }
            assertEquals(CardApplicationReadStatus.READ_FAILED, failed.status); assertEquals(ReaderFailure.IO, failed.failure)
            assertNull(failed.result); assertNotNull(batch.applications.single { it.application != application }.result!!.card)
        }
    }
    @Test fun differentHolderOrPhysicalCardNeverPassesIdentityAgreement() = runBlocking {
        for (identity in listOf(dualIdentity(surname = "OTHER"), dualIdentity(number = "DIFFCARD12345678"))) {
            val fake = DualApplicationTransport(identities = mapOf(CardApplication.GEN2 to identity))
            val batch = DriverCardReader(fake).readApplications()
            assertEquals(CardApplicationIdentityAgreement.CONFLICT, batch.identityAgreement)
        }
    }
    @Test fun nonDriverSecondDfBlocksAgreementRatherThanDisappearingAsMissing() = runBlocking {
        val batch = DriverCardReader(DualApplicationTransport(malformedApplication = CardApplication.GEN2)).readApplications()
        assertEquals(CardRecognition.OTHER_CARD, batch.applications[1].result!!.recognition)
        assertEquals(CardApplicationIdentityAgreement.CONFLICT, batch.identityAgreement)
    }
    @Test fun unprovenIdentityBoundaryIsUnverifiedAndNeverUsesSiblingOriginal() = runBlocking {
        val batch = DriverCardReader(DualApplicationTransport(oversizedIdentity = CardApplication.GEN2)).readApplications()
        assertNull(batch.applications[1].result!!.identitySources.getValue(CardIdentitySourceKind.IDENTIFICATION).file)
        assertNotNull(batch.applications[0].result!!.identitySources.getValue(CardIdentitySourceKind.IDENTIFICATION).file)
        assertEquals(CardApplicationIdentityAgreement.UNVERIFIED, batch.identityAgreement)
    }
    @Test fun fatalTransportLossDuringEitherDfClosesEverythingAndReturnsNoBatch() = runBlocking {
        for (application in CardApplication.entries) for (failure in listOf(ReaderFailure.CARD_REMOVED,
            ReaderFailure.DISCONNECTED, ReaderFailure.TIMEOUT)) {
            val fake = DualApplicationTransport(errors = mapOf((application to DriverCardFiles.DRIVER_ACTIVITY) to ReaderException(failure)))
            try { DriverCardReader(fake).readApplications(); fail("$application/$failure") }
            catch (error: ReaderException) { assertEquals(failure, error.failure) }
            assertEquals(1, fake.offs); assertEquals(1, fake.closes)
            if (application == CardApplication.GEN1) assertFalse(fake.reads.any { it.first == CardApplication.GEN2 })
        }
    }
    @Test fun cancellationBetweenApplicationsCannotTurnFirstApplicationIntoBatchSuccess() = runBlocking {
        val fake = DualApplicationTransport(selectionErrors = mapOf(CardApplication.GEN2 to CancellationException("synthetic")))
        try { DriverCardReader(fake).readApplications(); fail() } catch (_: CancellationException) { }
        assertTrue(fake.reads.any { it.first == CardApplication.GEN1 })
        assertFalse(fake.reads.any { it.first == CardApplication.GEN2 })
        assertEquals(1, fake.offs); assertEquals(1, fake.closes)
    }
    @Test fun legacyReaderStillPrefersGen2AndDoesNotAttemptSecondDf() = runBlocking {
        val fake = DualApplicationTransport(); val legacy = DriverCardReader(fake).read()
        assertEquals(CardGeneration.GEN2V2, legacy.generation)
        assertFalse(fake.reads.any { it.first == CardApplication.GEN1 })
        assertNotNull(legacy.directory)
        assertTrue(legacy.metadata.keys.any { it.inMasterFile })
    }
    @Test fun returnedActivityAndMapsCannotMutateStoredApplicationSnapshot() = runBlocking {
        val batch = DriverCardReader(DualApplicationTransport()).readApplications()
        val attempt = batch.applications[0]; val first = attempt.result!!
        first.activity!!.dailyRecords.fill(127); first.vehicles!!.records.fill(127)
        (first.certificates as MutableMap).clear()
        val second = attempt.result!!
        assertTrue(second.activity!!.dailyRecords.all { it == 0.toByte() })
        assertTrue(second.vehicles!!.records.all { it == 0.toByte() })
        assertFalse(second.certificates.isEmpty())
        assertEquals(CardApplicationIdentityAgreement.AGREED, batch.identityAgreement)
    }
}

class CardApplicationModelTest {
    @Test fun aidCopiesCannotChangeFixedApplicationSelectionAndV2HasNoThirdAid() {
        CardApplication.GEN1.aid.fill(0); CardApplication.GEN2.aid.fill(0)
        assertEquals(0xFF, CardApplication.GEN1.aid[0].toInt() and 255)
        assertEquals(0xFF, CardApplication.GEN2.aid[0].toInt() and 255)
        assertEquals(2, CardApplication.entries.size)
        assertTrue(CardApplication.GEN2.supports(CardGeneration.GEN2V2))
        assertFalse(CardApplication.GEN1.supports(CardGeneration.GEN2V2))
    }
    @Test fun masterBytesAreFrozenOnInputAndOutputAndAttemptCollectionIsCopied() {
        val raw = ByteArray(8) { 1 }; val master = CardMasterFileCapture(5, raw, false)
        raw.fill(2); master.bytes!!.fill(3)
        assertTrue(master.bytes!!.all { it == 1.toByte() })
        val attempts = CardApplication.entries.map { CardApplicationAttempt.absent(it) }.toMutableList()
        val batch = CardApplicationReadBatch(attempts, listOf(master)); attempts.clear()
        (batch.applications as MutableList).clear(); runCatching { (batch.masterFiles as MutableList).clear() }
        assertEquals(2, batch.applications.size); assertEquals(1, batch.masterFiles.size)
    }
    @Test fun incompleteDuplicateOrThirdApplicationEnvelopesAreRejected() {
        val absent = CardApplicationAttempt.absent(CardApplication.GEN1)
        for (attempts in listOf(emptyList(), listOf(absent), listOf(absent, absent), listOf(absent, absent, absent))) {
            try { CardApplicationReadBatch(attempts, emptyList()); fail() } catch (_: IllegalArgumentException) { }
        }
    }
    @Test fun duplicateMasterSourcesAndProtectedFileCannotEnterEnvelope() {
        val attempts = CardApplication.entries.map { CardApplicationAttempt.absent(it) }
        val mf = CardMasterFileCapture(5, ByteArray(8), false)
        try { CardApplicationReadBatch(attempts, listOf(mf, mf)); fail() } catch (_: IllegalArgumentException) { }
        try { CardMasterFileCapture(0x0540, ByteArray(8), false); fail() } catch (_: IllegalArgumentException) { }
    }
    @Test fun aDriverGenerationCannotBeAssignedToTheWrongDf() {
        val raw = dualIdentity(); val card = DriverCardDecoder.identity(raw, CardGeneration.GEN2V2)
        try { CardApplicationAttempt.read(CardApplication.GEN1,
            CardReadResult(CardRecognition.DRIVER, CardGeneration.GEN2V2, card)); fail() }
        catch (_: IllegalArgumentException) { }
    }
    @Test fun masterFilesCannotBeHiddenInsideAnApplicationSourceMap() {
        val card = DriverCardDecoder.identity(dualIdentity(), CardGeneration.GEN1)
        val result = CardReadResult(CardRecognition.DRIVER, CardGeneration.GEN1, card,
            metadata = mapOf(CardMetadataKind.IC_IDENTIFICATION to CardMetadataRead(
                CardMetadataFile(CardMetadataKind.IC_IDENTIFICATION, CardGeneration.GEN1, ByteArray(8)))))
        try { CardApplicationAttempt.read(CardApplication.GEN1, result); fail() } catch (_: IllegalArgumentException) { }
    }
    @Test fun fatalTransportFailureCannotBeDowngradedToAnIndependentFileError() {
        for (failure in ReaderFailure.entries.filter { it !in setOf(ReaderFailure.IO, ReaderFailure.MALFORMED_RESPONSE) }) {
            try { CardApplicationAttempt.failed(CardApplication.GEN1, failure); fail() } catch (_: IllegalArgumentException) { }
        }
    }
}
