package de.fahrerabrechnung.data.card

import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

internal object OriginalFileFixtures {
    fun application(generation: CardGeneration, legacy: Boolean = false): ByteArray =
        ByteArray(if (generation == CardGeneration.GEN1) 10 else if (legacy) 15 else 17).also {
            it[0] = 1; it[1] = 1; it[2] = if (generation == CardGeneration.GEN2V2) 1 else 0
            it[3] = 255.toByte(); it[4] = 128.toByte(); it[5] = 255.toByte(); it[6] = 255.toByte()
            it[7] = 128.toByte(); it[8] = 1; it[9] = 255.toByte()
            if (it.size > 10) { it[10] = 254.toByte(); it[11] = 128.toByte(); it[12] = 2; it[13] = 255.toByte(); it[14] = 255.toByte() }
            if (it.size == 17) { it[15] = 128.toByte(); it[16] = 3 }
        }
    fun extension() = byteArrayOf(0, 8, 255.toByte(), 255.toByte(), 128.toByte(), 1, 0, 0, 255.toByte(), 254.toByte())
    fun identity(): ByteArray = ByteArrayOutputStream().apply {
        fun name(value: String) { write(1); write(value.padEnd(35).toByteArray(Charsets.ISO_8859_1)) }
        write(13); write("TESTCARD12345678".toByteArray()); name("Fixture")
        write(ByteBuffer.allocate(12).putInt(0).putInt(0).putInt(1893456000).array())
        name("MUSTER"); name("TEST PERSON"); write(byteArrayOf(0x20, 0, 2, 0x29)); write("de".toByteArray())
    }.toByteArray()
}

class CardOriginalFileParserTest {
    private fun parsed(generation: CardGeneration, legacy: Boolean = false) = CardOriginalFileParser.parse(
        CardOriginalFile(CardOriginalFileKind.APPLICATION_IDENTIFICATION, generation, OriginalFileFixtures.application(generation, legacy))) as CardApplicationIdentificationRecord
    private fun invalid(block: () -> Unit) { try { block(); fail("Expected ParseError") } catch (_: ParseError) { } }

    @Test fun generationOneCapacitiesAreUnsignedAndHaveNoInventedGenerationTwoFields() {
        val record = parsed(CardGeneration.GEN1)
        assertEquals(255, record.eventsPerTypeRaw); assertEquals(128, record.faultsPerTypeRaw)
        assertEquals(65535, record.activityBytesRaw); assertEquals(32769, record.vehicleRecordsRaw)
        assertEquals(255, record.placeRecordsRaw); assertNull(record.gnssRecordsRaw)
        assertNull(record.specificConditionRecordsRaw); assertNull(record.vehicleUnitRecordsRaw)
    }
    @Test fun bothGenerationTwoLayoutsRetainOnlyPresentCapacityFields() {
        for (generation in listOf(CardGeneration.GEN2, CardGeneration.GEN2V2)) {
            val old = parsed(generation, true); val current = parsed(generation)
            assertEquals(65534, old.placeRecordsRaw); assertEquals(32770, old.gnssRecordsRaw)
            assertEquals(65535, old.specificConditionRecordsRaw); assertNull(old.vehicleUnitRecordsRaw)
            assertEquals(32771, current.vehicleUnitRecordsRaw)
            assertEquals(listOf(1, if (generation == CardGeneration.GEN2V2) 1 else 0), current.structureVersionBytes)
        }
    }
    @Test fun zeroAndMaximumCapacityValuesRemainRecordedWithoutExpandingReaderBounds() {
        val raw = OriginalFileFixtures.application(CardGeneration.GEN2).also { it[5] = 0; it[6] = 0 }
        val record = CardOriginalFileParser.parse(CardOriginalFile(CardOriginalFileKind.APPLICATION_IDENTIFICATION, CardGeneration.GEN2, raw)) as CardApplicationIdentificationRecord
        assertEquals(0, record.activityBytesRaw)
        invalid { DriverCardDecoder.activityLength(raw, CardGeneration.GEN2) }
    }
    @Test fun versionTwoExtensionPreservesUnknownCapacitiesAndConfigurationSize() {
        val record = CardOriginalFileParser.parse(CardOriginalFile(CardOriginalFileKind.APPLICATION_IDENTIFICATION_V2,
            CardGeneration.GEN2V2, OriginalFileFixtures.extension())) as CardApplicationIdentificationV2Record
        assertEquals(8, record.followingDataLengthRaw); assertEquals(65535, record.borderRecordsRaw)
        assertEquals(32769, record.loadUnloadRecordsRaw); assertEquals(0, record.loadTypeRecordsRaw)
        assertEquals(65534, record.vehicleUnitConfigurationBytesRaw)
    }
    @Test fun extensionRejectsAnInconsistentDeclaredLength() {
        for (length in listOf(0, 7, 9, 255)) invalid { CardOriginalFileParser.parse(CardOriginalFile(
            CardOriginalFileKind.APPLICATION_IDENTIFICATION_V2, CardGeneration.GEN2V2,
            OriginalFileFixtures.extension().also { it[1] = length.toByte() })) }
    }
    @Test fun identificationRetainsAllOriginalBytesAndUsesExistingIdentityDecoder() {
        for (generation in listOf(CardGeneration.GEN1, CardGeneration.GEN2, CardGeneration.GEN2V2)) {
            val raw = OriginalFileFixtures.identity(); val file = CardOriginalFile(CardOriginalFileKind.IDENTIFICATION, generation, raw)
            val record = CardOriginalFileParser.parse(file) as CardIdentificationRecord
            assertEquals(DriverCardDecoder.identity(raw, generation), record.card)
            assertEquals(1, record.authorityCodePageRaw); assertEquals(1, record.surnameCodePageRaw); assertEquals(1, record.givenNamesCodePageRaw)
            assertArrayEquals(raw, file.bytes)
        }
    }
    @Test fun sourceGenerationAndCardTypeMustMatchRecordedApplication() {
        invalid { CardOriginalFileParser.parse(CardOriginalFile(CardOriginalFileKind.APPLICATION_IDENTIFICATION,
            CardGeneration.GEN2, OriginalFileFixtures.application(CardGeneration.GEN2V2))) }
        invalid { CardOriginalFileParser.parse(CardOriginalFile(CardOriginalFileKind.APPLICATION_IDENTIFICATION,
            CardGeneration.GEN2, OriginalFileFixtures.application(CardGeneration.GEN2).also { it[0] = 2 })) }
    }
    @Test fun unsupportedKindsGenerationsAndLengthsAreRejected() {
        for (generation in listOf(CardGeneration.UNKNOWN, CardGeneration.UNSUPPORTED)) for (kind in CardOriginalFileKind.entries)
            invalid { CardOriginalFile(kind, generation, ByteArray(10)) }
        for (generation in listOf(CardGeneration.GEN1, CardGeneration.GEN2))
            invalid { CardOriginalFile(CardOriginalFileKind.APPLICATION_IDENTIFICATION_V2, generation, ByteArray(10)) }
        for (length in listOf(0, 9, 11, 14, 16, 18, 142, 144))
            invalid { CardOriginalFile(CardOriginalFileKind.IDENTIFICATION, CardGeneration.GEN2V2, ByteArray(length)) }
    }
    @Test fun originalBuffersAreDefensiveOnInputAndOutput() {
        val raw = OriginalFileFixtures.extension(); val expected = raw.copyOf()
        val file = CardOriginalFile(CardOriginalFileKind.APPLICATION_IDENTIFICATION_V2, CardGeneration.GEN2V2, raw)
        raw.fill(0); file.bytes.fill(0); assertArrayEquals(expected, file.bytes)
    }
    @Test fun identityValidationRejectsAnOriginalFromAnotherReportedCard() {
        val file = CardOriginalFile(CardOriginalFileKind.IDENTIFICATION, CardGeneration.GEN2V2, OriginalFileFixtures.identity())
        val card = (CardOriginalFileParser.parse(file) as CardIdentificationRecord).card
        CardOriginalFileParser.validateIdentity(file, card)
        try { CardOriginalFileParser.validateIdentity(file, card.copy(card = card.card.copy(number = "OTHER"))); fail() }
        catch (_: IllegalArgumentException) { }
    }
    private class ProbeTransport(private val response: ByteArray? = null, private val error: Exception? = null): CardReaderTransport {
        val commands = mutableListOf<ByteArray>()
        override suspend fun availability() = SourceAvailability.AVAILABLE
        override suspend fun open() = ReaderState.Open
        override suspend fun powerOn() = byteArrayOf(0x3B, 0)
        override suspend fun exchange(apdu: ByteArray): ByteArray { commands += apdu; error?.let { throw it }; return checkNotNull(response) }
        override suspend fun powerOff() = Unit
        override fun close() = Unit
    }
    @Test fun onlyExplicitEmptyEndOfFileResponsesRetainTheCompleteSource() = runBlocking {
        for (sw in listOf(0x6700, 0x6B00, 0x6282)) {
            val transport = ProbeTransport(byteArrayOf((sw shr 8).toByte(), sw.toByte()))
            val raw = OriginalFileFixtures.extension()
            val read = CardOriginalFileReader.retain(ApduSession(transport), CardOriginalFileKind.APPLICATION_IDENTIFICATION_V2, CardGeneration.GEN2V2, raw)
            assertFalse(read.readFailed); assertArrayEquals(raw, read.file!!.bytes)
            assertArrayEquals(CardCommands.readBinary(10, 1).encode(), transport.commands.single())
        }
    }
    @Test fun extraBytesEmptySuccessAndLengthCorrectionsDoNotProveCompleteFile() = runBlocking {
        for (reply in listOf(byteArrayOf(0x90.toByte(), 0), byteArrayOf(1, 0x90.toByte(), 0),
            byteArrayOf(1, 0x67, 0), byteArrayOf(0x6C, 0), byteArrayOf(0x6C, 1), byteArrayOf(0x69, 0x82.toByte()))) {
            val transport = ProbeTransport(reply)
            val read = CardOriginalFileReader.retain(ApduSession(transport), CardOriginalFileKind.IDENTIFICATION,
                CardGeneration.GEN2, OriginalFileFixtures.identity())
            assertTrue(read.readFailed); assertNull(read.file); assertEquals(1, transport.commands.size)
        }
    }
    @Test fun boundaryReadFailureIsIndependentAndCancellationPropagates() = runBlocking {
        val failed = CardOriginalFileReader.retain(ApduSession(ProbeTransport(error = ReaderException(ReaderFailure.TIMEOUT))),
            CardOriginalFileKind.IDENTIFICATION, CardGeneration.GEN2, OriginalFileFixtures.identity())
        assertTrue(failed.readFailed)
        try { CardOriginalFileReader.retain(ApduSession(ProbeTransport(error = CancellationException("fixture"))),
            CardOriginalFileKind.IDENTIFICATION, CardGeneration.GEN2, OriginalFileFixtures.identity()); fail() }
        catch (_: CancellationException) { }
        assertTrue(CardOriginalFileReader.retain(ApduSession(ProbeTransport()), CardOriginalFileKind.IDENTIFICATION,
            CardGeneration.GEN2, null).readFailed)
    }
}
