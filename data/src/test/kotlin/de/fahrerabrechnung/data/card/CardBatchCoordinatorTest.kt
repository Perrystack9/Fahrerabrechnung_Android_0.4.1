package de.fahrerabrechnung.data.card

import java.io.ByteArrayOutputStream
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class CardBatchCoordinatorTest {
    private fun batch(marker: Int, number: String = "TESTCARD12345678"): CardApplicationReadBatch {
        val identity = ByteArrayOutputStream().apply {
            fun name(value: String) { write(1); write(value.padEnd(35).toByteArray()) }
            write(1); write(number.toByteArray()); name("TEST AUTHORITY")
            write(byteArrayOf(0x65, 0x53, 0xF1.toByte(), 0)); write(byteArrayOf(0x65, 0x53, 0xF1.toByte(), 0))
            write(byteArrayOf(0x75, 0x53, 0xF1.toByte(), 0)); name("MUSTER"); name("ERIKA")
            write(byteArrayOf(0x20, 0, 0x02, 0x29)); write("DE".toByteArray())
        }.toByteArray()
        val attempts = CardApplication.entries.map { aid ->
            val generation = if (aid == CardApplication.GEN1) CardGeneration.GEN1 else CardGeneration.GEN2
            CardApplicationAttempt.read(aid, CardReadResult(CardRecognition.DRIVER, generation,
                DriverCardDecoder.identity(identity, generation), identitySources = mapOf(
                    CardIdentitySourceKind.IDENTIFICATION to CardIdentitySourceRead(CardIdentitySourceFile(CardIdentitySourceKind.IDENTIFICATION, generation, identity)))))
        }
        return CardApplicationReadBatch(attempts, listOf(CardMasterFileCapture(0x0005, ByteArray(8) { marker.toByte() }, false)))
    }
    private fun saved(input: CardApplicationReadBatch, id: String): SavedCardReadBatch {
        val applications = input.applications.map { attempt ->
            val read = attempt.result!!
            val child = CardBatchRequests.child(id, attempt.application)
            SavedCardApplication(attempt.application, attempt.status, read.generation, null,
                CardImportRun(child, "own", read.card!!.card.number, read.generation, CardImportRunStatus.PARTIAL,
                    0, 0, child, 0, null, emptyList(), id, attempt.application))
        }
        return SavedCardReadBatch(id, "own", 0, CardApplication.GEN2, applications, emptyList())
    }
    @Test fun confirmedOwnCardIsBoundOnlyThroughFreshAtomicBatchSave() = runBlocking {
        var reads = 0; var writes = 0
        val coordinator = CardImportCoordinator({ error("single read") },
            { CardBindingReview(CardBindingKind.FIRST_CARD, null) }, { error("binding must be atomic with group") },
            { _, _ -> error("single save") }, { batch(++reads) }, { value, id, confirmed ->
                assertTrue(confirmed); assertEquals(2, value.masterFiles.single().bytes!![0].toInt())
                writes++; saved(value, id)
            })
        coordinator.observe(CardReaderObservation.Present(1))
        assertEquals(CardImportPhase.CONFIRM_FIRST_CARD, coordinator.state.value.phase); assertEquals(0, writes)
        coordinator.confirm(coordinator.state.value.confirmationToken!!)
        assertEquals(2, reads); assertEquals(1, writes)
        assertEquals(CardApplication.GEN2, coordinator.state.value.lastRun!!.application)
    }
    @Test fun changedPhysicalCardRequiresNewConfirmationAndDecliningWritesNothing() = runBlocking {
        var reads = 0; var writes = 0
        val coordinator = CardImportCoordinator({ error("single") }, { CardBindingReview(CardBindingKind.FIRST_CARD, null) },
            { error("single bind") }, { _, _ -> error("single save") },
            { if (++reads == 1) batch(1) else batch(2, "OTHERCRD12345678") },
            { value, id, _ -> writes++; saved(value, id) })
        coordinator.observe(CardReaderObservation.Present(1)); val token = coordinator.state.value.confirmationToken!!
        coordinator.confirm(token)
        assertEquals(0, writes); assertEquals(CardImportPhase.CONFIRM_FIRST_CARD, coordinator.state.value.phase)
        assertNotEquals(token, coordinator.state.value.confirmationToken)
        coordinator.confirm(token); assertEquals(2, reads)
        coordinator.decline(coordinator.state.value.confirmationToken!!)
        assertEquals(CardImportPhase.CANCELLED, coordinator.state.value.phase); assertEquals(0, writes)
    }
    @Test fun knownOwnCardReadsBothDfsOncePerInsertionAndForeignCardNeverSaves() = runBlocking {
        var reads = 0; var writes = 0; var foreign = false
        val coordinator = CardImportCoordinator({ error("single") },
            { CardBindingReview(if (foreign) CardBindingKind.FOREIGN_CARD else CardBindingKind.KNOWN_CARD, null) },
            { error("single bind") }, { _, _ -> error("single save") }, { batch(++reads) },
            { value, id, confirmed -> assertFalse(confirmed); writes++; saved(value, id) })
        repeat(4) { coordinator.observe(CardReaderObservation.Present(1)) }
        assertEquals(1, reads); assertEquals(1, writes)
        foreign = true
        coordinator.observe(CardReaderObservation.Empty(1)); coordinator.observe(CardReaderObservation.Present(1))
        assertEquals(CardImportPhase.FOREIGN_CARD, coordinator.state.value.phase)
        assertEquals(2, reads); assertEquals(1, writes)
    }
}
