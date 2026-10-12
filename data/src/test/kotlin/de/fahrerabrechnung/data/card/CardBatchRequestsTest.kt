package de.fahrerabrechnung.data.card

import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class CardBatchRequestsTest {
    private fun batch(bytes: ByteArray = byteArrayOf(1, 2), reversed: Boolean = false,
                      failed: Boolean = false): CardApplicationReadBatch {
        val attempts = listOf(CardApplicationAttempt.absent(CardApplication.GEN1),
            if (failed) CardApplicationAttempt.failed(CardApplication.GEN2, ReaderFailure.IO)
            else CardApplicationAttempt.absent(CardApplication.GEN2))
        return CardApplicationReadBatch(if (reversed) attempts.reversed() else attempts,
            listOf(CardMasterFileCapture(0x0005, bytes, false)))
    }
    @Test fun childIdsUseCompleteApplicationAndParentWithoutTruncation() {
        assertNotEquals(CardBatchRequests.child("parent", CardApplication.GEN1), CardBatchRequests.child("parent", CardApplication.GEN2))
        assertNotEquals(CardBatchRequests.child("a\u0000b", CardApplication.GEN1), CardBatchRequests.child("a", CardApplication.GEN1))
        assertNotEquals(CardBatchRequests.child("a".repeat(127) + "x", CardApplication.GEN1),
            CardBatchRequests.child("a".repeat(127) + "y", CardApplication.GEN1))
        assertEquals(64, CardBatchRequests.child("parent", CardApplication.GEN1).length)
    }
    @Test fun fingerprintUsesCanonicalOrderExactMfBytesAndIndividualAttemptStatuses() {
        val original = CardBatchRequests.fingerprint("card", batch(), emptyMap())
        assertEquals(original, CardBatchRequests.fingerprint("card", batch(reversed = true), emptyMap()))
        assertNotEquals(original, CardBatchRequests.fingerprint("card", batch(byteArrayOf(1, 3)), emptyMap()))
        assertNotEquals(original, CardBatchRequests.fingerprint("card", batch(failed = true), emptyMap()))
        assertNotEquals(original, CardBatchRequests.fingerprint("other", batch(), emptyMap()))
    }
    @Test fun masterAndSnapshotsRemainIndependentOfCallerAndReturnedBuffers() {
        val raw = byteArrayOf(1, 2); val snapshot = batch(raw)
        val hash = CardBatchRequests.fingerprint("card", snapshot, emptyMap())
        raw[0] = 9; snapshot.masterFiles.single().bytes!![0] = 8
        assertEquals(hash, CardBatchRequests.fingerprint("card", snapshot, emptyMap()))
        assertNotEquals(CardBatchRequests.master("card", 2, raw), CardBatchRequests.master("card", 5, raw))
    }
    @Test fun absentAndFailedApplicationsCannotReachOwnCardReviewOrStorage() = runBlocking {
        var reviewed = false; var saved = false
        val coordinator = CardImportCoordinator({ error("single reader") }, { reviewed = true; error("review") },
            { error("bind") }, { _, _ -> error("legacy save") }, { batch(failed = true) },
            { _, _, _ -> saved = true; error("batch save") })
        coordinator.observe(CardReaderObservation.Present(1))
        assertEquals(CardImportPhase.ERROR, coordinator.state.value.phase)
        assertFalse(reviewed); assertFalse(saved)
    }
}
