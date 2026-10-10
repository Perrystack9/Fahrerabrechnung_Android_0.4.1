package de.fahrerabrechnung.data.card

import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import java.io.ByteArrayOutputStream
import java.time.LocalDate

class DriverCardFoundationTest {
    private fun bytes(vararg numbers: Int) = numbers.map(Int::toByte).toByteArray()
    private fun failure(block: () -> Unit): ParseError {
        try { block() } catch (e: ParseError) { return e }
        throw AssertionError("ParseError expected")
    }

    @Test fun apduCasesAndStatus() {
        assertArrayEquals(bytes(0, 0xB0, 0, 0, 0), CardCommands.readBinary(0, 256).encode())
        assertArrayEquals(bytes(0, 0xA4, 2, 12, 2, 5, 1), CardCommands.selectFile(0x0501).encode())
        assertEquals("file not found", ResponseApdu.decode(bytes(0x6A, 0x82)).meaning)
        assertEquals("security status not satisfied", ResponseApdu.decode(bytes(0x69, 0x82)).meaning)
        assertEquals("conditions of use not satisfied", ResponseApdu.decode(bytes(0x69, 0x85)).meaning)
        assertEquals(0x9000, ResponseApdu.decode(bytes(1, 2, 0x90, 0)).statusWord)
    }

    @Test fun getResponseAndCorrectedLength() = runBlocking {
        val fake = ScriptedTransport(listOf(bytes(0x6C, 3), bytes(1, 0x61, 2), bytes(2, 3, 0x90, 0)))
        val response = ApduSession(fake).send(CardCommands.readBinary(0, 8))
        assertArrayEquals(bytes(1, 2, 3), response.data)
        assertEquals(3, fake.commands.size)
        assertEquals(3, fake.commands[1].last().toInt() and 255)
        assertEquals(0xC0, fake.commands[2][1].toInt() and 255)
    }

    @Test fun binaryChecks() {
        val r = BinaryReader(bytes(0x12, 0x34, 0x56, 0x78, 0x99))
        assertEquals(0x1234, r.u16be("a"))
        assertEquals(0x7856, r.u16le("b"))
        assertEquals("99", r.bcd(2, "c"))
        assertEquals(5, failure { r.u8("last") }.offset)
        assertEquals(0, failure { BinaryReader(bytes(0xFA)).bcd(2, "bcd") }.offset)
        assertEquals(2, failure { BinaryReader(bytes(0x82, 0x01)).tlv("tlv") }.offset)
        assertEquals(4L, BinaryReader(bytes(0, 0, 0, 4)).epochSeconds("time").epochSecond)
    }

    @Test fun ccidFrameAndAtrValidation() {
        val request = CcidProtocol.command(CcidProtocol.XFR_BLOCK, 0, 7, bytes(1, 2))
        assertEquals(12, request.size)
        assertEquals(2, request[1].toInt())
        val reply = bytes(0x80, 2, 0, 0, 0, 0, 7, 0, 0, 0, 0x90, 0)
        assertArrayEquals(bytes(0x90, 0), CcidProtocol.response(reply, 0x80, 0, 7).data)
        try { CcidProtocol.response(reply, 0x80, 0, 6); fail() }
        catch (e: ReaderException) { assertEquals(ReaderFailure.MALFORMED_RESPONSE, e.failure) }
        assertTrue(AtrDecoder.parse(bytes(0x3B, 0)).protocols.contains(0))
        assertEquals(setOf(1), AtrDecoder.parse(bytes(0x3B, 0x80, 0x01, 0x81)).protocols)
        try { AtrDecoder.parse(bytes(0x3B, 0x01)); fail() }
        catch (e: ReaderException) { assertEquals(ReaderFailure.MALFORMED_RESPONSE, e.failure) }
    }

    @Test fun ccidDescriptorMatchesInterfaceAndRejectsUnsupportedExchange() {
        val iface = bytes(9, 4, 2, 0, 2, 0x0B, 0, 0, 0)
        val ccid = ByteArray(54).also {
            it[0] = 54; it[1] = 0x21; it[4] = 1 // two slots
            it[40] = 0; it[41] = 0; it[42] = 4 // extended APDU exchange
            it[44] = 0x10; it[45] = 1 // 272-byte CCID message
        }
        val raw = iface + ccid
        assertNull(CcidDescriptor.parse(raw, 1))
        val descriptor = CcidDescriptor.parse(raw, 2)!!
        assertEquals(2, descriptor.slots)
        assertEquals(272, descriptor.maxMessage)
        assertTrue(descriptor.apduExchange)
        assertTrue(descriptor.extendedApdu)
        ccid[42] = 0
        assertFalse(CcidDescriptor.parse(iface + ccid, 2)!!.apduExchange)
        assertNull(CcidDescriptor.parse(raw.copyOf(raw.size - 1), 2))
    }

    @Test fun ccidResponseChainIsOrderedAndBounded() {
        fun response(chain: Int, vararg data: Int) = CcidProtocol.Response(0x80, 1, 0, 0, 0,
            chain, bytes(*data))
        val chain = CcidResponseChain(4)
        assertNull(chain.add(response(1, 1)))
        assertNull(chain.add(response(3, 2)))
        assertArrayEquals(bytes(1, 2, 3), chain.add(response(2, 3)))
        try { chain.add(response(0)); fail() }
        catch (e: ReaderException) { assertEquals(ReaderFailure.MALFORMED_RESPONSE, e.failure) }
        try { CcidResponseChain().add(response(2, 1)); fail() }
        catch (e: ReaderException) { assertEquals(ReaderFailure.MALFORMED_RESPONSE, e.failure) }
        try { CcidResponseChain(1).add(response(0, 1, 2)); fail() }
        catch (e: ReaderException) { assertEquals(ReaderFailure.MALFORMED_RESPONSE, e.failure) }
        assertEquals(0x10, CcidProtocol.command(CcidProtocol.XFR_BLOCK, 1, 7, level = 0x10)[8].toInt())
        assertEquals(2, CcidProtocol.command(CcidProtocol.XFR_BLOCK, 1, 7, bytes(1), level = 2)[8].toInt())
    }

    @Test fun ccidCommandBlocksRespectAdvertisedMessageSize() {
        val apdu = ByteArray(14) { it.toByte() }
        val blocks = CcidCommandBlocks.split(apdu, 16, true)
        assertEquals(listOf(1, 3, 2), blocks.map { it.level })
        assertArrayEquals(apdu, blocks.flatMap { it.data.toList() }.toByteArray())
        assertEquals(0, CcidCommandBlocks.split(apdu, 24, false).single().level)
        try { CcidCommandBlocks.split(apdu, 16, false); fail() }
        catch (e: ReaderException) { assertEquals(ReaderFailure.UNSUPPORTED_READER, e.failure) }
        try { CcidCommandBlocks.split(apdu, 10, true); fail() }
        catch (e: ReaderException) { assertEquals(ReaderFailure.UNSUPPORTED_READER, e.failure) }
    }

    @Test fun generationIsBoundToApplicationBytes() {
        val g1 = ByteArray(10).also { it[0] = 1 }
        val g2 = ByteArray(15).also { it[0] = 1; it[1] = 1 }
        assertEquals(CardGeneration.GEN1, DriverCardDecoder.generation(g1, null).second)
        assertEquals(CardGeneration.GEN2, DriverCardDecoder.generation(null, g2).second)
        g2[2] = 1
        assertEquals(CardGeneration.GEN2V2, DriverCardDecoder.generation(null, g2).second)
        g2[2] = 9
        assertEquals(CardGeneration.UNKNOWN, DriverCardDecoder.generation(null, g2).second)
        g2[0] = 2
        assertEquals(CardRecognition.OTHER_CARD, DriverCardDecoder.generation(null, g2).first)
        assertEquals(17, DriverCardFiles.files.first { it.generation == CardGeneration.GEN2 }.expectedLength)
        assertEquals(10, DriverCardFiles.files.first { it.generation == CardGeneration.GEN2V2 }.expectedLength)
        assertTrue(CardGeneration.GEN2V2.capabilities.contains(CardCapability.V2_FILES))
    }

    @Test fun syntheticIdentityAndTruncation() {
        val data = identityFixture()
        assertEquals(143, data.size)
        for (generation in listOf(CardGeneration.GEN1, CardGeneration.GEN2, CardGeneration.GEN2V2)) {
            val result = DriverCardDecoder.identity(data, generation)
            assertEquals("MUSTER", result.holder.surname)
            assertEquals("TESTCARD12345678", result.card.number)
            assertEquals(LocalDate.of(2000, 2, 29), result.holder.birthDate)
            assertEquals("DE", result.holder.language)
            assertEquals(1968435456L, result.card.expires.epochSecond)
        }
        assertEquals(0, failure { DriverCardDecoder.identity(data.copyOf(142), CardGeneration.GEN1) }.offset)
    }

    @Test fun missingBirthDateKeepsIdentificationAligned() {
        for (generation in listOf(CardGeneration.GEN1, CardGeneration.GEN2, CardGeneration.GEN2V2)) {
            val result = DriverCardDecoder.identity(identityFixture(ByteArray(4)), generation)
            assertNull(result.holder.birthDate)
            assertEquals("DE", result.holder.language)
            assertEquals(1968435456L, result.card.expires.epochSecond)
        }
    }

    @Test fun invalidBirthDateIsRejectedForEveryGeneration() {
        for (generation in listOf(CardGeneration.GEN1, CardGeneration.GEN2, CardGeneration.GEN2V2)) {
            for (date in listOf(bytes(0x20, 0x01, 0x02, 0x29), bytes(0x20, 0x00, 0x0A, 0x29))) {
                val error = failure { DriverCardDecoder.identity(identityFixture(date), generation) }
                assertEquals("birthDate", error.field)
                assertEquals(137, error.offset)
            }
        }
    }

    @Test fun completeReadWithSyntheticGenerationsAndUnknownCard() = runBlocking {
        for (expected in listOf(CardGeneration.GEN1, CardGeneration.GEN2, CardGeneration.GEN2V2)) {
            val fake = CardFixtureTransport(expected, identityFixture())
            val result = DriverCardReader(fake).read()
            assertEquals(CardRecognition.DRIVER, result.recognition)
            val card = result.card!!
            assertEquals(expected, card.generation)
            assertEquals(LocalDate.of(2000, 2, 29), card.holder.birthDate)
            assertEquals(16, result.activity!!.dailyRecords.size)
            assertEquals(4, result.activity!!.newestDayOffset)
            assertNotNull(result.vehicles)
            assertFalse(result.activityReadFailed)
            assertFalse(result.vehicleReadFailed)
            assertTrue(fake.closed)
        }
        val unknown = DriverCardReader(CardFixtureTransport(CardGeneration.UNKNOWN, identityFixture())).read()
        assertEquals(CardRecognition.UNKNOWN_CARD, unknown.recognition)
        assertNull(unknown.card)
    }

    @Test fun missingBirthDateDoesNotDiscardCompleteRead() = runBlocking {
        for (generation in listOf(CardGeneration.GEN1, CardGeneration.GEN2, CardGeneration.GEN2V2)) {
            val fake = CardFixtureTransport(generation, identityFixture(ByteArray(4)))
            val result = DriverCardReader(fake).read()
            assertEquals(CardRecognition.DRIVER, result.recognition)
            val card = result.card!!
            assertNull(card.holder.birthDate)
            assertEquals("DE", card.holder.language)
            assertNotNull(result.activity)
            assertNotNull(result.vehicles)
            assertFalse(result.vehicleReadFailed)
            assertTrue(fake.closed)
        }
    }

    @Test fun invalidBirthDateDoesNotReturnPartialCard() = runBlocking {
        for (generation in listOf(CardGeneration.GEN1, CardGeneration.GEN2, CardGeneration.GEN2V2)) {
            val fake = CardFixtureTransport(generation, identityFixture(bytes(0x20, 0x01, 0x02, 0x29)))
            val result = DriverCardReader(fake).read()
            assertEquals(CardRecognition.INCONSISTENT, result.recognition)
            assertEquals(CardGeneration.UNKNOWN, result.generation)
            assertNull(result.card)
            assertNull(result.activity)
            assertNull(result.vehicles)
            assertFalse(fake.readFiles.contains(DriverCardFiles.DRIVER_ACTIVITY))
            assertFalse(fake.readFiles.contains(DriverCardFiles.VEHICLES_USED))
            assertTrue(fake.closed)
        }
    }

    @Test fun activityLengthAndRingPointersAreValidated() {
        val app = ByteArray(15).also { it[5] = 0; it[6] = 16 }
        assertEquals(16, DriverCardDecoder.activityLength(app, CardGeneration.GEN2V2))
        val ring = ByteArray(20).also { it[2] = 0; it[3] = 4 }
        assertEquals(4, DriverCardDecoder.activity(ring, 16).newestDayOffset)
        ring[3] = 16
        assertEquals("Driver_Activity_Data",
            failure { DriverCardDecoder.activity(ring, 16) }.field)
        assertEquals("Driver_Activity_Data",
            failure { DriverCardDecoder.activity(ring.copyOf(19), 16) }.field)
        app[5] = 0x7F
        app[6] = 0xFD.toByte()
        assertEquals("activityStructureLength",
            failure { DriverCardDecoder.activityLength(app, CardGeneration.GEN2) }.field)
    }

    @Test fun truncatedActivityReturnsOnlyCompleteVehiclesForEveryGeneration() = runBlocking {
        for (generation in supportedGenerations) {
            val fake = CardFixtureTransport(generation, identityFixture(), truncateActivity = true)
            val result = DriverCardReader(fake).read()
            assertEquals(CardRecognition.DRIVER, result.recognition)
            assertEquals(generation, result.card!!.generation)
            assertNull(result.activity); assertTrue(result.activityReadFailed)
            assertNotNull(result.vehicles); assertFalse(result.vehicleReadFailed)
            assertTrue(fake.readFiles.contains(DriverCardFiles.VEHICLES_USED)); assertTrue(fake.closed)
        }
    }

    @Test fun unavailableActivitiesDoNotPreventCompleteVehicleRead() = runBlocking {
        for (generation in supportedGenerations) {
            val fake = CardFixtureTransport(generation, identityFixture(), missingFiles = setOf(DriverCardFiles.DRIVER_ACTIVITY))
            val result = DriverCardReader(fake).read()
            assertEquals(CardRecognition.DRIVER, result.recognition)
            assertEquals(generation, result.generation)
            assertNull(result.activity); assertTrue(result.activityReadFailed)
            assertNotNull(result.vehicles); assertFalse(result.vehicleReadFailed)
            assertTrue(fake.closed)
        }
    }

    @Test fun invalidActivityLengthAndPointersLeaveVehiclesReadable() = runBlocking {
        for (generation in supportedGenerations) {
            for (badLength in listOf(false, true)) {
                val fake = CardFixtureTransport(generation, identityFixture(), invalidActivityLength = badLength,
                    invalidActivityPointer = !badLength)
                val result = DriverCardReader(fake).read()
                assertEquals(CardRecognition.DRIVER, result.recognition)
                assertNull(result.activity); assertTrue(result.activityReadFailed)
                assertNotNull(result.vehicles); assertFalse(result.vehicleReadFailed)
                assertTrue(fake.closed)
            }
        }
    }

    @Test fun unavailableVehiclesKeepCompleteActivities() = runBlocking {
        for (generation in supportedGenerations) {
            val fake = CardFixtureTransport(generation, identityFixture(), missingFiles = setOf(DriverCardFiles.VEHICLES_USED))
            val result = DriverCardReader(fake).read()
            assertNotNull(result.activity); assertFalse(result.activityReadFailed)
            assertNull(result.vehicles); assertTrue(result.vehicleReadFailed)
            assertEquals(CardRecognition.DRIVER, result.recognition); assertTrue(fake.closed)
        }
    }

    @Test fun twoUnavailableFilesReturnNoRawDataAndTwoExplicitFailures() = runBlocking {
        for (generation in supportedGenerations) {
            val fake = CardFixtureTransport(generation, identityFixture(), missingFiles =
                setOf(DriverCardFiles.DRIVER_ACTIVITY, DriverCardFiles.VEHICLES_USED))
            val result = DriverCardReader(fake).read()
            assertEquals(CardRecognition.DRIVER, result.recognition); assertNotNull(result.card)
            assertNull(result.activity); assertNull(result.vehicles)
            assertTrue(result.activityReadFailed); assertTrue(result.vehicleReadFailed); assertTrue(fake.closed)
        }
    }

    @Test fun activityTransportFailureDoesNotDiscardReadableVehicles() = runBlocking {
        for (generation in supportedGenerations) {
            val fake = CardFixtureTransport(generation, identityFixture(), activityFailure = ReaderException(ReaderFailure.TIMEOUT))
            val result = DriverCardReader(fake).read()
            assertNull(result.activity); assertTrue(result.activityReadFailed)
            assertNotNull(result.vehicles); assertFalse(result.vehicleReadFailed); assertTrue(fake.closed)
        }
    }

    @Test fun cancellationDuringActivityDoesNotReadVehiclesOrReturnASuccess() = runBlocking {
        for (generation in supportedGenerations) {
            val fake = CardFixtureTransport(generation, identityFixture(), activityFailure = kotlinx.coroutines.CancellationException("fixture"))
            try { DriverCardReader(fake).read(); fail("Cancellation expected") }
            catch (_: kotlinx.coroutines.CancellationException) { }
            assertFalse(fake.readFiles.contains(DriverCardFiles.VEHICLES_USED)); assertTrue(fake.closed)
        }
    }

    private val supportedGenerations = listOf(CardGeneration.GEN1, CardGeneration.GEN2, CardGeneration.GEN2V2)

    @Test fun readerErrorsCloseSessionWithoutReturningPartialIdentity() = runBlocking {
        for (failure in listOf(ReaderFailure.TIMEOUT, ReaderFailure.CARD_REMOVED, ReaderFailure.DISCONNECTED)) {
            val transport = object : CardReaderTransport {
                var closed = false
                override suspend fun availability() = SourceAvailability.AVAILABLE
                override suspend fun open() = ReaderState.Open
                override suspend fun powerOn() = byteArrayOf(0x3B, 0)
                override suspend fun exchange(apdu: ByteArray): ByteArray = throw ReaderException(failure)
                override suspend fun powerOff() = Unit
                override fun close() { closed = true }
            }
            try { DriverCardReader(transport).read(); fail("Expected $failure") }
            catch (e: ReaderException) { assertEquals(failure, e.failure) }
            assertTrue(transport.closed)
        }
    }

    private fun identityFixture(birthDate: ByteArray = bytes(0x20, 0x00, 0x02, 0x29)) = ByteArrayOutputStream().apply {
            write(1)
            write("TESTCARD12345678".toByteArray())
            write(name("TEST AUTHORITY"))
            write(bytes(0x65, 0x53, 0xF1, 0))
            write(bytes(0x65, 0x53, 0xF1, 0))
            write(bytes(0x75, 0x53, 0xF1, 0))
            write(name("MUSTER"))
            write(name("ERIKA"))
            write(birthDate)
            write("DE".toByteArray())
    }.toByteArray()

    private fun name(value: String) = byteArrayOf(1) + value.toByteArray().copyOf(35).also {
        for (i in value.length until it.size) it[i] = 32
    }

    private class ScriptedTransport(private val replies: List<ByteArray>) : CardReaderTransport {
        val commands = mutableListOf<ByteArray>()
        override suspend fun availability() = SourceAvailability.AVAILABLE
        override suspend fun open() = ReaderState.Open
        override suspend fun powerOn() = bytesStatic(0x3B, 0)
        override suspend fun exchange(apdu: ByteArray): ByteArray {
            commands += apdu
            return replies[commands.size - 1]
        }
        override suspend fun powerOff() = Unit
        override fun close() = Unit
        companion object { fun bytesStatic(vararg n: Int) = n.map(Int::toByte).toByteArray() }
    }

    @Test fun controlsAreReadIndependentlyForEverySupportedGeneration() = runBlocking {
        for (generation in supportedGenerations) {
            val raw = ByteArray(46).also { it[1] = 1; it[24] = 1 }
            val fake = CardFixtureTransport(generation, identityFixture(),
                missingFiles = setOf(DriverCardFiles.DRIVER_ACTIVITY, DriverCardFiles.VEHICLES_USED), controlBytes = raw)
            val result = DriverCardReader(fake).read()
            assertEquals(CardRecognition.DRIVER, result.recognition)
            assertNull(result.activity); assertNull(result.vehicles)
            assertTrue(result.activityReadFailed); assertTrue(result.vehicleReadFailed)
            assertArrayEquals(raw, result.controls!!.bytes); assertFalse(result.controlReadFailed)
            assertNotNull(CardControlParser.parse(result.controls!!)); assertTrue(fake.closed)
        }
    }

    @Test fun unavailableTruncatedOrInvalidControlsDoNotDiscardOtherFiles() = runBlocking {
        for (generation in supportedGenerations) for (kind in 0..3) {
            val fake = CardFixtureTransport(generation, identityFixture(),
                missingFiles = if (kind == 0) setOf(DriverCardFiles.CONTROL_ACTIVITY) else emptySet(),
                controlBytes = when (kind) { 1 -> ByteArray(45); 2 -> ByteArray(46).also { it[1] = 1; it[24] = 127 }; else -> ByteArray(46) },
                controlFailure = if (kind == 3) ReaderException(ReaderFailure.TIMEOUT) else null)
            val result = DriverCardReader(fake).read()
            assertNotNull(result.activity); assertNotNull(result.vehicles)
            assertNull(result.controls); assertTrue(result.controlReadFailed); assertTrue(fake.closed)
        }
    }

    @Test fun unusedControlIsACompleteFileRatherThanAReadFailure() = runBlocking {
        for (generation in supportedGenerations) {
            val fake = CardFixtureTransport(generation, identityFixture())
            val result = DriverCardReader(fake).read()
            assertNotNull(result.controls); assertFalse(result.controlReadFailed)
            assertNull(CardControlParser.parse(result.controls!!)); assertTrue(fake.closed)
        }
    }

    @Test fun cancellationDuringControlReadPropagatesAndClosesTheReader() = runBlocking {
        for (generation in supportedGenerations) {
            val fake = CardFixtureTransport(generation, identityFixture(),
                controlFailure = kotlinx.coroutines.CancellationException("fixture"))
            try { DriverCardReader(fake).read(); throw AssertionError("Expected cancellation") }
            catch (_: kotlinx.coroutines.CancellationException) { assertTrue(fake.closed) }
        }
    }

    @Test fun eventAndFaultFilesReadCompletelyForAllGenerationsAndCoreFileFailures() = runBlocking {
        for (generation in supportedGenerations) {
            val fake = CardFixtureTransport(generation, identityFixture(),
                missingFiles = setOf(DriverCardFiles.DRIVER_ACTIVITY, DriverCardFiles.VEHICLES_USED, DriverCardFiles.CONTROL_ACTIVITY))
            val result = DriverCardReader(fake).read()
            assertEquals(CardRecognition.DRIVER, result.recognition)
            assertEquals(CardEventFaultParser.fileLength(CardEventFaultKind.EVENTS, generation, 12), result.events!!.bytes.size)
            assertEquals(1152, result.faults!!.bytes.size)
            assertTrue(CardEventFaultParser.parse(result.events!!).isEmpty())
            assertTrue(CardEventFaultParser.parse(result.faults!!).isEmpty())
            assertFalse(result.eventReadFailed); assertFalse(result.faultReadFailed); assertTrue(fake.closed)
        }
    }

    @Test fun unavailableTruncatedInvalidAndTimedOutNewFilesNeverDiscardOtherCompleteFiles() = runBlocking {
        for (generation in supportedGenerations) for (kind in CardEventFaultKind.entries) for (failure in 0..3) {
            val length = CardEventFaultParser.fileLength(kind, generation, if (kind == CardEventFaultKind.EVENTS) 12 else 24)
            val raw = when (failure) {
                1 -> ByteArray(length - 1)
                2 -> ByteArray(length).also { it[0] = 3; it[10] = 127; it[11] = 65 }
                else -> null
            }
            val fake = CardFixtureTransport(generation, identityFixture(),
                missingFiles = if (failure == 0) setOf(kind.fileId) else emptySet(),
                eventBytes = raw.takeIf { kind == CardEventFaultKind.EVENTS },
                faultBytes = raw.takeIf { kind == CardEventFaultKind.FAULTS },
                eventFailure = if (kind == CardEventFaultKind.EVENTS && failure == 3) ReaderException(ReaderFailure.TIMEOUT) else null,
                faultFailure = if (kind == CardEventFaultKind.FAULTS && failure == 3) ReaderException(ReaderFailure.TIMEOUT) else null)
            val result = DriverCardReader(fake).read()
            assertNotNull(result.activity); assertNotNull(result.vehicles); assertNotNull(result.controls)
            if (kind == CardEventFaultKind.EVENTS) {
                assertNull(result.events); assertTrue(result.eventReadFailed)
                assertNotNull(result.faults); assertFalse(result.faultReadFailed)
            } else {
                assertNull(result.faults); assertTrue(result.faultReadFailed)
                assertNotNull(result.events); assertFalse(result.eventReadFailed)
            }
            assertTrue(fake.closed)
        }
    }

    @Test fun invalidEventCountDoesNotPreventFaultOrExistingFileReads() = runBlocking {
        for (generation in supportedGenerations) {
            val fake = CardFixtureTransport(generation, identityFixture(), invalidEventCount = true)
            val result = DriverCardReader(fake).read()
            assertNull(result.events); assertTrue(result.eventReadFailed)
            assertNotNull(result.faults); assertFalse(result.faultReadFailed)
            assertNotNull(result.activity); assertNotNull(result.vehicles); assertNotNull(result.controls)
            assertFalse(CardEventFaultKind.EVENTS.fileId in fake.readFiles); assertTrue(fake.closed)
        }
    }

    @Test fun cancellationDuringEitherNewFilePropagatesAndClosesTheReader() = runBlocking {
        for (generation in supportedGenerations) for (kind in CardEventFaultKind.entries) {
            val cancellation = kotlinx.coroutines.CancellationException("fixture")
            val fake = CardFixtureTransport(generation, identityFixture(),
                eventFailure = cancellation.takeIf { kind == CardEventFaultKind.EVENTS },
                faultFailure = cancellation.takeIf { kind == CardEventFaultKind.FAULTS })
            try { DriverCardReader(fake).read(); throw AssertionError("Expected cancellation") }
            catch (_: kotlinx.coroutines.CancellationException) { assertTrue(fake.closed) }
        }
    }

    @Test fun invalidIdentityDoesNotAttemptEventsOrFaults() = runBlocking {
        for (generation in supportedGenerations) {
            val fake = CardFixtureTransport(generation, identityFixture(bytes(0x20, 0x01, 0x02, 0x29)))
            val result = DriverCardReader(fake).read()
            assertEquals(CardRecognition.INCONSISTENT, result.recognition)
            assertNull(result.events); assertNull(result.faults)
            assertFalse(CardEventFaultKind.EVENTS.fileId in fake.readFiles)
            assertFalse(CardEventFaultKind.FAULTS.fileId in fake.readFiles); assertTrue(fake.closed)
        }
    }

    @Test fun specificConditionsAreReadCompletelyEvenWhenAllOtherDataFilesFail() = runBlocking {
        for (generation in supportedGenerations) {
            val count = if (generation == CardGeneration.GEN1) 56 else 112
            val raw = ByteArray(CardSpecificConditionParser.fileLength(generation, count)).also {
                val offset = (if (generation == CardGeneration.GEN1) 0 else 2) + (count - 1) * 5
                java.nio.ByteBuffer.wrap(it).apply { position(offset); putInt(1790000001); put(1.toByte()) }
            }
            val fake = CardFixtureTransport(generation, identityFixture(), missingFiles = setOf(
                DriverCardFiles.DRIVER_ACTIVITY, DriverCardFiles.VEHICLES_USED, DriverCardFiles.CONTROL_ACTIVITY,
                CardEventFaultKind.EVENTS.fileId, CardEventFaultKind.FAULTS.fileId), specificConditionBytes = raw)
            val result = DriverCardReader(fake).read()
            assertEquals(CardRecognition.DRIVER, result.recognition)
            assertArrayEquals(raw, result.specificConditions!!.bytes)
            assertFalse(result.specificConditionReadFailed)
            assertEquals(count - 1, CardSpecificConditionParser.parse(result.specificConditions!!).single().slotIndex)
            assertTrue(fake.readFiles.contains(DriverCardFiles.SPECIFIC_CONDITIONS)); assertTrue(fake.closed)
        }
    }

    @Test fun missingTruncatedOrTimedOutSpecificConditionsKeepAllOtherCompleteFiles() = runBlocking {
        for (generation in supportedGenerations) for (failure in 0..2) {
            val length = if (generation == CardGeneration.GEN1) 280 else 562
            val fake = CardFixtureTransport(generation, identityFixture(),
                missingFiles = if (failure == 0) setOf(DriverCardFiles.SPECIFIC_CONDITIONS) else emptySet(),
                specificConditionBytes = if (failure == 1) ByteArray(length - 1) else null,
                specificConditionFailure = if (failure == 2) ReaderException(ReaderFailure.TIMEOUT) else null)
            val result = DriverCardReader(fake).read()
            assertNull(result.specificConditions); assertTrue(result.specificConditionReadFailed)
            assertNotNull(result.activity); assertNotNull(result.vehicles); assertNotNull(result.controls)
            assertNotNull(result.events); assertNotNull(result.faults); assertTrue(fake.closed)
        }
    }

    @Test fun invalidSpecificConditionCapacityOrRingPointerDoesNotDiscardOtherSources() = runBlocking {
        for (generation in listOf(CardGeneration.GEN2, CardGeneration.GEN2V2)) for (invalidCount in listOf(true, false)) {
            val fake = CardFixtureTransport(generation, identityFixture(), invalidSpecificConditionCount = invalidCount,
                specificConditionBytes = ByteArray(562).also { it[1] = 112 })
            val result = DriverCardReader(fake).read()
            assertNull(result.specificConditions); assertTrue(result.specificConditionReadFailed)
            assertEquals(!invalidCount, fake.readFiles.contains(DriverCardFiles.SPECIFIC_CONDITIONS))
            assertNotNull(result.activity); assertNotNull(result.vehicles); assertNotNull(result.events); assertNotNull(result.faults)
            assertTrue(fake.closed)
        }
    }

    @Test fun cancellationDuringSpecificConditionReadPropagatesAndClosesReader() = runBlocking {
        for (generation in supportedGenerations) {
            val fake = CardFixtureTransport(generation, identityFixture(),
                specificConditionFailure = kotlinx.coroutines.CancellationException("fixture"))
            try { DriverCardReader(fake).read(); throw AssertionError("Cancellation expected") }
            catch (_: kotlinx.coroutines.CancellationException) { assertTrue(fake.closed) }
        }
    }

    @Test fun readerAttemptsOnlyGenerationSupportedGnssFiles() = runBlocking {
        for (generation in listOf(CardGeneration.GEN1, CardGeneration.GEN2, CardGeneration.GEN2V2)) {
            val fake = CardFixtureTransport(generation, identityFixture())
            val result = DriverCardReader(fake).read()
            assertEquals(generation != CardGeneration.GEN1, result.gnssPositions != null)
            assertEquals(generation == CardGeneration.GEN2V2, result.gnssAuthentication != null)
            assertFalse(result.gnssPositionReadFailed); assertFalse(result.gnssAuthenticationReadFailed)
            assertEquals(generation != CardGeneration.GEN1, DriverCardFiles.GNSS_PLACES in fake.readFiles)
            assertEquals(generation == CardGeneration.GEN2V2, DriverCardFiles.GNSS_AUTHENTICATION in fake.readFiles)
            assertTrue(fake.closed)
        }
    }

    @Test fun gnssReadFailuresAreIndependentAndNeverDiscardOtherSources() = runBlocking {
        for (failedId in listOf(DriverCardFiles.GNSS_PLACES, DriverCardFiles.GNSS_AUTHENTICATION)) {
            val fake = CardFixtureTransport(CardGeneration.GEN2V2, identityFixture(), missingFiles = setOf(failedId))
            val result = DriverCardReader(fake).read()
            assertNotNull(result.activity); assertNotNull(result.vehicles); assertNotNull(result.specificConditions)
            assertEquals(failedId == DriverCardFiles.GNSS_PLACES, result.gnssPositionReadFailed)
            assertEquals(failedId == DriverCardFiles.GNSS_AUTHENTICATION, result.gnssAuthenticationReadFailed)
            assertEquals(failedId != DriverCardFiles.GNSS_PLACES, result.gnssPositions != null)
            assertEquals(failedId != DriverCardFiles.GNSS_AUTHENTICATION, result.gnssAuthentication != null)
        }
    }

    @Test fun gnssInvalidCapacityAndPointerLeaveAuthenticationUsable() = runBlocking {
        for (badCount in listOf(true, false)) {
            val bytes = ByteArray(CardGnssAccumulatedDrivingParser.fileLength(CardGeneration.GEN2V2, 336))
            bytes[0] = 2
            val result = DriverCardReader(CardFixtureTransport(CardGeneration.GEN2V2, identityFixture(),
                invalidGnssCount = badCount, gnssBytes = bytes)).read()
            assertNull(result.gnssPositions); assertTrue(result.gnssPositionReadFailed)
            assertNotNull(result.gnssAuthentication); assertNotNull(result.events)
        }
    }

    @Test fun gnssCancellationPropagatesAndClosesReader() = runBlocking {
        for (positions in listOf(true, false)) {
            val fake = CardFixtureTransport(CardGeneration.GEN2V2, identityFixture(),
                gnssFailure = if (positions) kotlinx.coroutines.CancellationException("fixture") else null,
                authenticationFailure = if (!positions) kotlinx.coroutines.CancellationException("fixture") else null)
            try { DriverCardReader(fake).read(); fail("Expected cancellation") }
            catch (_: kotlinx.coroutines.CancellationException) { assertTrue(fake.closed) }
        }
    }

    @Test fun gnssCapacityUsesFullBaseApplicationNotV2Extension() {
        val application = ByteArray(17).also { it[11] = 1; it[12] = 80 }
        assertEquals(336, DriverCardDecoder.gnssCount(application, CardGeneration.GEN2V2))
        application[11] = 0; application[12] = 252.toByte()
        assertEquals(252, DriverCardDecoder.gnssCount(application, CardGeneration.GEN2))
        failure { DriverCardDecoder.gnssCount(application, CardGeneration.GEN2V2) }
        failure { DriverCardDecoder.gnssCount(ByteArray(15), CardGeneration.GEN2) }
    }

    @Test fun placesReaderSupportsAllGenerationsAndOnlyV2Authentication() = runBlocking {
        for (g in supportedGenerations) {
            val fake = CardFixtureTransport(g, identityFixture())
            val result = DriverCardReader(fake).read()
            assertNotNull(result.places); assertFalse(result.placeReadFailed)
            assertEquals(g == CardGeneration.GEN2V2, result.placesAuthentication != null)
            assertFalse(result.placesAuthenticationReadFailed)
            assertTrue(DriverCardFiles.PLACES in fake.readFiles)
            assertEquals(g == CardGeneration.GEN2V2, DriverCardFiles.PLACES_AUTHENTICATION in fake.readFiles)
            assertTrue(fake.closed)
        }
    }
    @Test fun placesFailuresNeverDiscardOtherSourcesOrAuthentication() = runBlocking {
        for (failure in 0..3) {
            val fake = CardFixtureTransport(CardGeneration.GEN2V2, identityFixture(),
                missingFiles = if (failure == 0) setOf(DriverCardFiles.PLACES) else emptySet(),
                placeBytes = if (failure == 1) ByteArray(2353) else if (failure == 2) ByteArray(2354).also { it[1] = 112 } else null,
                invalidPlaceCount = failure == 3)
            val result = DriverCardReader(fake).read()
            assertNull(result.places); assertTrue(result.placeReadFailed)
            assertNotNull(result.placesAuthentication); assertNotNull(result.gnssPositions)
            assertNotNull(result.activity); assertNotNull(result.events); assertTrue(fake.closed)
        }
        val result = DriverCardReader(CardFixtureTransport(CardGeneration.GEN2V2, identityFixture(),
            placeAuthFailure = ReaderException(ReaderFailure.TIMEOUT))).read()
        assertNotNull(result.places); assertNull(result.placesAuthentication); assertTrue(result.placesAuthenticationReadFailed)
    }
    @Test fun placesCancellationPropagatesAndClosesReader() = runBlocking {
        for (auth in listOf(false, true)) {
            val fake = CardFixtureTransport(CardGeneration.GEN2V2, identityFixture(),
                placeFailure = if (!auth) kotlinx.coroutines.CancellationException("fixture") else null,
                placeAuthFailure = if (auth) kotlinx.coroutines.CancellationException("fixture") else null)
            try { DriverCardReader(fake).read(); fail("Cancellation expected") }
            catch (_: kotlinx.coroutines.CancellationException) { assertTrue(fake.closed) }
        }
    }
    @Test fun placeCapacityUsesGenerationSpecificApplicationField() {
        assertEquals(84, DriverCardDecoder.placeCount(ByteArray(10).also { it[9] = 84 }, CardGeneration.GEN1))
        assertEquals(112, DriverCardDecoder.placeCount(ByteArray(17).also { it[10] = 112 }, CardGeneration.GEN2V2))
        failure { DriverCardDecoder.placeCount(ByteArray(10), CardGeneration.GEN1) }
        failure { DriverCardDecoder.placeCount(ByteArray(17).also { it[9] = 1 }, CardGeneration.GEN2V2) }
    }

    @Test fun borderReaderAttemptsOnlyGen2V2AndKeepsOtherSources() = runBlocking {
        for (g in supportedGenerations) {
            val fake = CardFixtureTransport(g, identityFixture())
            val result = DriverCardReader(fake).read()
            assertEquals(g == CardGeneration.GEN2V2, result.borderCrossings != null)
            assertEquals(g == CardGeneration.GEN2V2, DriverCardFiles.BORDER_CROSSINGS in fake.readFiles)
            assertFalse(result.borderCrossingsReadFailed); assertNotNull(result.places); assertTrue(fake.closed)
        }
    }
    @Test fun borderCapacityUsesOnlyValidatedV2Extension() {
        val extension = byteArrayOf(0, 8, 4, 96, 6, 88, 1, 80, 12, 0)
        assertEquals(1120, DriverCardDecoder.borderCrossingCount(extension, CardGeneration.GEN2V2))
        for (g in listOf(CardGeneration.GEN1, CardGeneration.GEN2, CardGeneration.UNKNOWN))
            failure { DriverCardDecoder.borderCrossingCount(extension, g) }
        for (length in listOf(0, 9, 11, 17))
            failure { DriverCardDecoder.borderCrossingCount(ByteArray(length), CardGeneration.GEN2V2) }
        for (offset in listOf(1, 2, 3)) {
            val invalid = extension.copyOf(); invalid[offset] = 0
            failure { DriverCardDecoder.borderCrossingCount(invalid, CardGeneration.GEN2V2) }
        }
    }
    @Test fun borderFailuresAndInvalidCapacityNeverDiscardOtherValidFiles() = runBlocking {
        for (problem in 0..4) {
            val fake = CardFixtureTransport(CardGeneration.GEN2V2, identityFixture(),
                missingFiles = if (problem == 0) setOf(DriverCardFiles.BORDER_CROSSINGS) else emptySet(),
                borderBytes = when (problem) {
                    1 -> ByteArray(19041)
                    2 -> ByteArray(19042).also { it[0] = 4; it[1] = 96 }
                    else -> null
                },
                extensionBytes = if (problem == 3) ByteArray(10) else null,
                borderFailure = if (problem == 4) ReaderException(ReaderFailure.TIMEOUT) else null)
            val result = DriverCardReader(fake).read()
            assertNull(result.borderCrossings); assertTrue(result.borderCrossingsReadFailed)
            assertNotNull(result.activity); assertNotNull(result.vehicles); assertNotNull(result.places)
            assertNotNull(result.placesAuthentication); assertNotNull(result.gnssPositions); assertNotNull(result.events)
            assertEquals(problem != 0 && problem != 3, DriverCardFiles.BORDER_CROSSINGS in fake.readFiles)
            assertTrue(fake.closed)
        }
    }
    @Test fun unavailableV2ExtensionOnlyFailsTheDependentBorderSource() = runBlocking {
        for (missing in listOf(true, false)) {
            val fake = CardFixtureTransport(CardGeneration.GEN2V2, identityFixture(),
                missingFiles = if (missing) setOf(DriverCardFiles.APPLICATION_V2) else emptySet(),
                extensionBytes = if (!missing) ByteArray(9) else null)
            val result = DriverCardReader(fake).read()
            assertEquals(CardRecognition.DRIVER, result.recognition)
            assertNull(result.borderCrossings); assertTrue(result.borderCrossingsReadFailed)
            assertNotNull(result.activity); assertNotNull(result.vehicles); assertNotNull(result.places)
            assertNotNull(result.gnssPositions); assertNotNull(result.gnssAuthentication)
            assertFalse(DriverCardFiles.BORDER_CROSSINGS in fake.readFiles); assertTrue(fake.closed)
        }
    }
    @Test fun earlierReadFailuresDoNotPreventTheBorderAttempt() = runBlocking {
        val result = DriverCardReader(CardFixtureTransport(CardGeneration.GEN2V2, identityFixture(),
            missingFiles = setOf(DriverCardFiles.PLACES, DriverCardFiles.GNSS_AUTHENTICATION,
                DriverCardFiles.DRIVER_ACTIVITY))).read()
        assertNotNull(result.borderCrossings); assertFalse(result.borderCrossingsReadFailed)
        assertTrue(result.placeReadFailed); assertTrue(result.activityReadFailed)
    }
    @Test fun borderCancellationPropagatesAndClosesReader() = runBlocking {
        val fake = CardFixtureTransport(CardGeneration.GEN2V2, identityFixture(),
            borderFailure = kotlinx.coroutines.CancellationException("fixture"))
        try { DriverCardReader(fake).read(); fail("Cancellation expected") }
        catch (_: kotlinx.coroutines.CancellationException) { assertTrue(fake.closed) }
    }

    @Test fun loadUnloadReaderAttemptsOnlyGen2V2AndKeepsOtherSources() = runBlocking {
        for (g in supportedGenerations) {
            val fake = CardFixtureTransport(g, identityFixture())
            val result = DriverCardReader(fake).read()
            assertEquals(g == CardGeneration.GEN2V2, result.loadUnload != null)
            assertEquals(g == CardGeneration.GEN2V2, DriverCardFiles.LOAD_UNLOAD in fake.readFiles)
            assertFalse(result.loadUnloadReadFailed); assertNotNull(result.places); assertTrue(fake.closed)
        }
    }
    @Test fun loadUnloadCapacityUsesOnlyValidatedV2Extension() {
        val extension = byteArrayOf(0, 8, 4, 96, 6, 88, 1, 80, 12, 0)
        assertEquals(1624, DriverCardDecoder.loadUnloadCount(extension, CardGeneration.GEN2V2))
        for (g in listOf(CardGeneration.GEN1, CardGeneration.GEN2, CardGeneration.UNKNOWN))
            failure { DriverCardDecoder.loadUnloadCount(extension, g) }
        for (length in listOf(0, 9, 11, 17))
            failure { DriverCardDecoder.loadUnloadCount(ByteArray(length), CardGeneration.GEN2V2) }
        for (offset in listOf(1, 4, 5)) {
            val invalid = extension.copyOf(); invalid[offset] = 0
            failure { DriverCardDecoder.loadUnloadCount(invalid, CardGeneration.GEN2V2) }
        }
    }
    @Test fun loadUnloadFailuresAndInvalidCapacityNeverDiscardOtherValidFiles() = runBlocking {
        for (problem in 0..4) {
            val fake = CardFixtureTransport(CardGeneration.GEN2V2, identityFixture(),
                missingFiles = if (problem == 0) setOf(DriverCardFiles.LOAD_UNLOAD) else emptySet(),
                loadUnloadBytes = when (problem) {
                    1 -> ByteArray(32481)
                    2 -> ByteArray(32482).also { it[0] = 6; it[1] = 88 }
                    else -> null
                },
                extensionBytes = if (problem == 3) ByteArray(10) else null,
                loadUnloadFailure = if (problem == 4) ReaderException(ReaderFailure.TIMEOUT) else null)
            val result = DriverCardReader(fake).read()
            assertNull(result.loadUnload); assertTrue(result.loadUnloadReadFailed)
            assertNotNull(result.activity); assertNotNull(result.vehicles); assertNotNull(result.places)
            assertNotNull(result.placesAuthentication); assertNotNull(result.gnssPositions); assertNotNull(result.events)
            assertEquals(problem != 0 && problem != 3, DriverCardFiles.LOAD_UNLOAD in fake.readFiles)
            assertTrue(fake.closed)
        }
    }
    @Test fun unavailableV2ExtensionOnlyFailsTheDependentLoadUnloadSource() = runBlocking {
        for (missing in listOf(true, false)) {
            val fake = CardFixtureTransport(CardGeneration.GEN2V2, identityFixture(),
                missingFiles = if (missing) setOf(DriverCardFiles.APPLICATION_V2) else emptySet(),
                extensionBytes = if (!missing) ByteArray(9) else null)
            val result = DriverCardReader(fake).read()
            assertEquals(CardRecognition.DRIVER, result.recognition)
            assertNull(result.loadUnload); assertTrue(result.loadUnloadReadFailed)
            assertNotNull(result.activity); assertNotNull(result.vehicles); assertNotNull(result.places)
            assertNotNull(result.gnssPositions); assertNotNull(result.gnssAuthentication)
            assertFalse(DriverCardFiles.LOAD_UNLOAD in fake.readFiles); assertTrue(fake.closed)
        }
    }
    @Test fun earlierReadFailuresDoNotPreventTheLoadUnloadAttempt() = runBlocking {
        val result = DriverCardReader(CardFixtureTransport(CardGeneration.GEN2V2, identityFixture(),
            missingFiles = setOf(DriverCardFiles.PLACES, DriverCardFiles.GNSS_AUTHENTICATION,
                DriverCardFiles.DRIVER_ACTIVITY))).read()
        assertNotNull(result.loadUnload); assertFalse(result.loadUnloadReadFailed)
        assertTrue(result.placeReadFailed); assertTrue(result.activityReadFailed)
    }
    @Test fun loadUnloadCancellationPropagatesAndClosesReader() = runBlocking {
        val fake = CardFixtureTransport(CardGeneration.GEN2V2, identityFixture(),
            loadUnloadFailure = kotlinx.coroutines.CancellationException("fixture"))
        try { DriverCardReader(fake).read(); fail("Cancellation expected") }
        catch (_: kotlinx.coroutines.CancellationException) { assertTrue(fake.closed) }
    }

    @Test fun loadUnloadReadsTheComplete32482ByteFileWithinTheBinaryOffsetLimit() = runBlocking {
        val fake = CardFixtureTransport(CardGeneration.GEN2V2, identityFixture())
        val result = DriverCardReader(fake).read()
        assertEquals(32482, result.loadUnload!!.bytes.size)
        assertEquals(127, fake.loadReads.size)
        assertEquals(0 to 256, fake.loadReads.first())
        assertEquals(32256 to 226, fake.loadReads.last())
        assertTrue(fake.loadReads.all { (offset, length) -> offset in 0..32767 && offset + length <= 32482 })
    }
    @Test fun invalidBorderCapacityDoesNotBlockTheIndependentLoadUnloadCapacity() = runBlocking {
        val extension = byteArrayOf(0, 8, 0, 0, 6, 88, 1, 80, 12, 0)
        val result = DriverCardReader(CardFixtureTransport(CardGeneration.GEN2V2, identityFixture(), extensionBytes = extension)).read()
        assertTrue(result.borderCrossingsReadFailed); assertNull(result.borderCrossings)
        assertNotNull(result.loadUnload); assertFalse(result.loadUnloadReadFailed)
    }
    @Test fun invalidLoadUnloadCapacityDoesNotBlockTheIndependentBorderCapacity() = runBlocking {
        val extension = byteArrayOf(0, 8, 4, 96, 0, 0, 1, 80, 12, 0)
        val result = DriverCardReader(CardFixtureTransport(CardGeneration.GEN2V2, identityFixture(), extensionBytes = extension)).read()
        assertTrue(result.loadUnloadReadFailed); assertNull(result.loadUnload)
        assertNotNull(result.borderCrossings); assertFalse(result.borderCrossingsReadFailed)
    }

    private class CardFixtureTransport(private val generation: CardGeneration,
                                       private val identification: ByteArray,
                                       private val truncateActivity: Boolean = false,
                                       private val missingFiles: Set<Int> = emptySet(),
                                       private val invalidActivityLength: Boolean = false,
                                       private val invalidActivityPointer: Boolean = false,
                                       private val activityFailure: Exception? = null,
                                       private val controlBytes: ByteArray = ByteArray(46),
                                       private val controlFailure: Exception? = null,
                                       private val eventBytes: ByteArray? = null,
                                       private val faultBytes: ByteArray? = null,
                                       private val eventFailure: Exception? = null,
                                       private val faultFailure: Exception? = null,
                                       private val invalidEventCount: Boolean = false,
                                       private val specificConditionBytes: ByteArray? = null,
                                       private val specificConditionFailure: Exception? = null,
                                       private val invalidSpecificConditionCount: Boolean = false,
                                       private val gnssBytes: ByteArray? = null,
                                       private val authenticationBytes: ByteArray? = null,
                                       private val gnssFailure: Exception? = null,
                                       private val authenticationFailure: Exception? = null,
                                       private val invalidGnssCount: Boolean = false,
                                       private val placeBytes: ByteArray? = null,
                                       private val placeAuthBytes: ByteArray? = null,
                                       private val placeFailure: Exception? = null,
                                       private val placeAuthFailure: Exception? = null,
                                       private val invalidPlaceCount: Boolean = false,
                                       private val borderBytes: ByteArray? = null,
                                       private val borderFailure: Exception? = null,
                                       private val loadUnloadBytes: ByteArray? = null,
                                       private val loadUnloadFailure: Exception? = null,
                                       private val extensionBytes: ByteArray? = null) : CardReaderTransport {
        var closed = false
        val readFiles = mutableSetOf<Int>()
        val loadReads = mutableListOf<Pair<Int, Int>>()
        private var selected = false
        private var file = 0
        override suspend fun availability() = SourceAvailability.AVAILABLE
        override suspend fun open() = ReaderState.Open
        override suspend fun powerOn() = byteArrayOf(0x3B, 0)
        override suspend fun exchange(apdu: ByteArray): ByteArray {
            val ins = apdu[1].toInt() and 255
            if (ins == 0xA4 && apdu[2].toInt() == 4) {
                val aid = apdu.copyOfRange(5, apdu.size)
                selected = when {
                    generation == CardGeneration.UNKNOWN -> false
                    aid.contentEquals(DriverCardFiles.gen2Aid) -> generation != CardGeneration.GEN1
                    aid.contentEquals(DriverCardFiles.gen1Aid) -> generation == CardGeneration.GEN1
                    else -> false
                }
                return if (selected) byteArrayOf(0x90.toByte(), 0) else byteArrayOf(0x6A, 0x82.toByte())
            }
            if (ins == 0xA4 && selected) {
                file = (apdu[5].toInt() and 255) shl 8 or (apdu[6].toInt() and 255)
                if (file in missingFiles) return byteArrayOf(0x6A, 0x82.toByte())
                return byteArrayOf(0x90.toByte(), 0)
            }
            if (ins == 0xB0 && selected) {
                readFiles += file
                if (file == DriverCardFiles.DRIVER_ACTIVITY) activityFailure?.let { throw it }
                if (file == DriverCardFiles.CONTROL_ACTIVITY) controlFailure?.let { throw it }
                if (file == CardEventFaultKind.EVENTS.fileId) eventFailure?.let { throw it }
                if (file == CardEventFaultKind.FAULTS.fileId) faultFailure?.let { throw it }
                if (file == DriverCardFiles.SPECIFIC_CONDITIONS) specificConditionFailure?.let { throw it }
                if (file == DriverCardFiles.GNSS_PLACES) gnssFailure?.let { throw it }
                if (file == DriverCardFiles.GNSS_AUTHENTICATION) authenticationFailure?.let { throw it }
                if (file == DriverCardFiles.BORDER_CROSSINGS) borderFailure?.let { throw it }
                if (file == DriverCardFiles.LOAD_UNLOAD) loadUnloadFailure?.let { throw it }
                if (file == DriverCardFiles.PLACES) placeFailure?.let { throw it }
                if (file == DriverCardFiles.PLACES_AUTHENTICATION) placeAuthFailure?.let { throw it }
                val data = when (file) {
                    DriverCardFiles.PLACES -> placeBytes ?: ByteArray(CardPlacesParser.fileLength(generation, 112))
                    DriverCardFiles.PLACES_AUTHENTICATION -> placeAuthBytes ?: ByteArray(CardPlacesAuthenticationParser.fileLength(generation))
                    DriverCardFiles.IDENTIFICATION -> identification
                    DriverCardFiles.GNSS_PLACES -> gnssBytes ?: ByteArray(CardGnssAccumulatedDrivingParser.fileLength(generation, 336))
                    DriverCardFiles.GNSS_AUTHENTICATION -> authenticationBytes ?: ByteArray(CardGnssAuthenticationParser.fileLength(generation))
                    DriverCardFiles.BORDER_CROSSINGS -> borderBytes ?: ByteArray(19042)
                    DriverCardFiles.LOAD_UNLOAD -> loadUnloadBytes ?: ByteArray(32482)
                    DriverCardFiles.APPLICATION_V2 -> extensionBytes ?: ByteArray(10).also { it[1] = 8; it[2] = 4; it[3] = 96; it[4] = 6; it[5] = 88; it[6] = 1; it[7] = 80; it[8] = 12 }
                    DriverCardFiles.DRIVER_ACTIVITY -> ByteArray(if (truncateActivity) 19 else 20).also {
                        it[3] = if (invalidActivityPointer) 16 else 4
                    }
                    DriverCardFiles.VEHICLES_USED -> ByteArray(2 + 2 *
                        (if (generation == CardGeneration.GEN1) 31 else 48))
                    DriverCardFiles.CONTROL_ACTIVITY -> controlBytes
                    CardEventFaultKind.EVENTS.fileId -> eventBytes ?: ByteArray(CardEventFaultParser.fileLength(CardEventFaultKind.EVENTS, generation, 12))
                    CardEventFaultKind.FAULTS.fileId -> faultBytes ?: ByteArray(1152)
                    DriverCardFiles.SPECIFIC_CONDITIONS -> specificConditionBytes ?: ByteArray(
                        CardSpecificConditionParser.fileLength(generation, if (generation == CardGeneration.GEN1) 56 else 112))
                    else -> ByteArray(if (generation == CardGeneration.GEN1) 10 else 17).also {
                        it[0] = 1; it[1] = 1
                        if (generation == CardGeneration.GEN2V2) it[2] = 1
                        it[3] = if (invalidEventCount) 0 else 12; it[4] = 24
                        it[6] = if (invalidActivityLength) 0 else 16
                        it[8] = 2 // two vehicle record slots
                        it[if (generation == CardGeneration.GEN1) 9 else 10] = if (invalidPlaceCount) 0 else 112
                        if (it.size == 17) {
                            it[11] = if (invalidGnssCount) 0 else 1; it[12] = if (invalidGnssCount) 0 else 80
                            it[14] = if (invalidSpecificConditionCount) 0 else 112
                        }
                    }
                }
                val offset = (apdu[2].toInt() and 255) shl 8 or (apdu[3].toInt() and 255)
                val length = (apdu[4].toInt() and 255).let { if (it == 0) 256 else it }
                if (file == DriverCardFiles.LOAD_UNLOAD) loadReads += offset to length
                return data.copyOfRange(offset, minOf(offset + length, data.size)) + byteArrayOf(0x90.toByte(), 0)
            }
            throw AssertionError("Unexpected APDU instruction")
        }
        override suspend fun powerOff() = Unit
        override fun close() { closed = true }
    }
}
