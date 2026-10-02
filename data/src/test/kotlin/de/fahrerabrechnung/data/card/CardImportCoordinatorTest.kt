package de.fahrerabrechnung.data.card

import java.time.Instant
import java.time.LocalDate
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class CardImportCoordinatorTest {
    private fun card(number: String = "fixture-own") = DriverCard(CardGeneration.GEN2V2,
        CardIdentity(number, 13, "Fixture", Instant.EPOCH, Instant.EPOCH, Instant.parse("2030-01-01T00:00:00Z")),
        DriverIdentity("ALPHA", "TEST PERSON", LocalDate.of(2000, 2, 29), "de"))
    private fun result(card: DriverCard = card()) = CardReadResult(CardRecognition.DRIVER, card.generation, card)
    private fun run(id: String, status: CardImportRunStatus = CardImportRunStatus.SAVED) = CardImportRun(id,
        "physical-card", "fixture-own", CardGeneration.GEN2V2, status, 1, 2, id, 0, null, emptyList())

    @Test fun knownCardImportsAutomaticallyOncePerObservedInsertion() = runBlocking {
        var reads = 0; val saves = mutableListOf<String>()
        val c = CardImportCoordinator({ reads++; result() }, { CardBindingReview(CardBindingKind.KNOWN_CARD, "Fixture") },
            { error("Known card needs no confirmation") }, { _, id -> saves.add(id); run(id) })
        c.observe(CardReaderObservation.Present(7))
        repeat(10) { c.observe(CardReaderObservation.Present(7)) }
        assertEquals(1, reads); assertEquals(1, saves.size)
        assertEquals(CardImportPhase.IMPORTED, c.state.value.phase)
        c.observe(CardReaderObservation.Empty(7)); c.observe(CardReaderObservation.Present(7))
        assertEquals(2, reads); assertEquals(2, saves.toSet().size)
        c.observe(CardReaderObservation.Present(8))
        assertEquals(3, reads)
    }

    @Test fun firstCardWritesNothingUntilConfirmationAndThenUsesFreshRead() = runBlocking {
        var reads = 0; var bound = false; var saved = 0
        val c = CardImportCoordinator({ reads++; result() }, {
            CardBindingReview(if (bound) CardBindingKind.KNOWN_CARD else CardBindingKind.FIRST_CARD, null)
        }, { bound = true }, { _, id -> assertTrue(bound); saved++; run(id) })
        c.observe(CardReaderObservation.Present(1))
        assertEquals(CardImportPhase.CONFIRM_FIRST_CARD, c.state.value.phase)
        assertFalse(bound); assertEquals(0, saved)
        val token = c.state.value.confirmationToken!!
        c.confirm(token)
        assertEquals(2, reads); assertTrue(bound); assertEquals(1, saved)
        c.confirm(token)
        assertEquals(2, reads); assertEquals(1, saved)
    }

    @Test fun uncertainReplacementRequiresExplicitConfirmation() = runBlocking {
        var confirmed = 0; var saved = 0
        val c = CardImportCoordinator({ result() }, { CardBindingReview(CardBindingKind.CONFIRM_REPLACEMENT, "Fixture") },
            { confirmed++ }, { _, id -> saved++; run(id) })
        c.observe(CardReaderObservation.Present(1))
        assertEquals(CardImportPhase.CONFIRM_REPLACEMENT, c.state.value.phase)
        assertEquals(0, confirmed); assertEquals(0, saved)
        c.confirm(c.state.value.confirmationToken!!)
        assertEquals(1, confirmed); assertEquals(1, saved)
    }

    @Test fun reliableReplacementUsesExistingMatchingWithoutExtraConfirmation() = runBlocking {
        var saved = 0
        val c = CardImportCoordinator({ result(card("fixture-next")) },
            { CardBindingReview(CardBindingKind.SAME_HOLDER, "Fixture") },
            { error("Strong match needs no confirmation") }, { _, id -> saved++; run(id) })
        c.observe(CardReaderObservation.Present(1))
        assertEquals(CardImportPhase.IMPORTED, c.state.value.phase); assertEquals(1, saved)
    }

    @Test fun decliningDoesNotPersistOrRepeatedlyAskForTheSameInsertion() = runBlocking {
        var reads = 0
        val c = CardImportCoordinator({ reads++; result() }, { CardBindingReview(CardBindingKind.FIRST_CARD, null) },
            { error("Declined") }, { _, _ -> error("Declined") })
        c.observe(CardReaderObservation.Present(1)); c.decline(c.state.value.confirmationToken!!)
        repeat(3) { c.observe(CardReaderObservation.Present(1)) }
        assertEquals(1, reads); assertEquals(CardImportPhase.CANCELLED, c.state.value.phase)
        assertNull(c.state.value.confirmationCard); assertNull(c.state.value.confirmationToken)
    }

    @Test fun swappedForeignCardCannotInheritConfirmationFromThePreviousCard() = runBlocking {
        var current = card(); var confirmations = 0; var saves = 0
        val c = CardImportCoordinator({ result(current) }, {
            CardBindingReview(if (it.card.number == "fixture-own") CardBindingKind.FIRST_CARD else CardBindingKind.FOREIGN_CARD, null)
        }, { confirmations++ }, { _, id -> saves++; run(id) })
        c.observe(CardReaderObservation.Present(1)); val oldToken = c.state.value.confirmationToken!!
        current = card("fixture-foreign")
        c.confirm(oldToken)
        assertEquals(CardImportPhase.FOREIGN_CARD, c.state.value.phase)
        assertEquals(0, confirmations); assertEquals(0, saves); assertNull(c.state.value.confirmationCard)
        c.confirm(oldToken)
        assertEquals(0, confirmations)
    }

    @Test fun swappedUnboundCardGetsANewConfirmationToken() = runBlocking {
        var current = card()
        val c = CardImportCoordinator({ result(current) }, { CardBindingReview(CardBindingKind.FIRST_CARD, null) },
            { error("Stale token") }, { _, _ -> error("Stale token") })
        c.observe(CardReaderObservation.Present(1)); val oldToken = c.state.value.confirmationToken!!
        current = card("fixture-other-unbound"); c.confirm(oldToken)
        assertNotEquals(oldToken, c.state.value.confirmationToken)
        c.confirm(oldToken)
        assertEquals("fixture-other-unbound", c.state.value.confirmationCard!!.card.number)
    }

    @Test fun removalWhileAwaitingConfirmationDiscardsIt() = runBlocking {
        val c = CardImportCoordinator({ result() }, { CardBindingReview(CardBindingKind.FIRST_CARD, null) },
            { error("Removed") }, { _, _ -> error("Removed") })
        c.observe(CardReaderObservation.Present(1)); val token = c.state.value.confirmationToken!!
        c.observe(CardReaderObservation.Empty(1)); c.confirm(token)
        assertNull(c.state.value.confirmationCard); assertEquals(CardImportPhase.WAITING_FOR_CARD, c.state.value.phase)
    }

    @Test fun readerErrorWaitsForRetryAndNeverReusesAnEarlierResult() = runBlocking {
        var failing = false; var reads = 0; var saves = 0
        val c = CardImportCoordinator({ reads++; if (failing) throw ReaderException(ReaderFailure.DISCONNECTED); result() },
            { CardBindingReview(CardBindingKind.KNOWN_CARD, "Fixture") }, {}, { _, id -> saves++; run(id) })
        c.observe(CardReaderObservation.Present(1)); c.observe(CardReaderObservation.Empty(1)); failing = true
        c.observe(CardReaderObservation.Present(1)); c.observe(CardReaderObservation.Present(1))
        assertEquals(2, reads); assertEquals(1, saves)
        assertEquals(ReaderFailure.DISCONNECTED, c.state.value.readerFailure)
        failing = false; c.retry(); c.observe(CardReaderObservation.Present(1))
        assertEquals(3, reads); assertEquals(2, saves)
    }

    @Test fun partialAndStorageFailureRemainVisibleWithoutASuccessClaim() = runBlocking {
        var status = CardImportRunStatus.PARTIAL
        val c = CardImportCoordinator({ result() }, { CardBindingReview(CardBindingKind.KNOWN_CARD, "Fixture") },
            {}, { _, id -> run(id, status) })
        c.observe(CardReaderObservation.Present(1))
        assertEquals(CardImportPhase.INCOMPLETE, c.state.value.phase)
        status = CardImportRunStatus.FAILED; c.observe(CardReaderObservation.Empty(1)); c.observe(CardReaderObservation.Present(1))
        assertEquals(CardImportPhase.ERROR, c.state.value.phase)
    }

    @Test fun cancellationIsRethrownAndDropsPendingDataForFreshForegroundRead() = runBlocking {
        val started = CompletableDeferred<Unit>(); val never = CompletableDeferred<CardReadResult>()
        var reads = 0
        val c = CardImportCoordinator({ reads++; started.complete(Unit); never.await() },
            { CardBindingReview(CardBindingKind.KNOWN_CARD, "Fixture") }, {}, { _, _ -> error("Cancelled") })
        val job = launch { c.observe(CardReaderObservation.Present(1)) }
        started.await(); job.cancelAndJoin()
        assertEquals(CardImportPhase.WAITING_FOR_READER, c.state.value.phase)
        assertNull(c.state.value.confirmationCard)
        val retry = launch { c.observe(CardReaderObservation.Present(1)) }
        kotlinx.coroutines.yield(); retry.cancelAndJoin()
        assertEquals(2, reads)
    }

    @Test fun permissionWaitingAndBackgroundPauseDoNotReadOrWrite() = runBlocking {
        var reads = 0
        val c = CardImportCoordinator({ reads++; result() }, { CardBindingReview(CardBindingKind.FIRST_CARD, null) },
            { error("Not confirmed") }, { _, _ -> error("Not confirmed") })
        c.observe(CardReaderObservation.PermissionRequired(1))
        assertEquals(CardImportPhase.USB_PERMISSION_REQUIRED, c.state.value.phase); assertEquals(0, reads)
        c.observe(CardReaderObservation.Present(1)); val token = c.state.value.confirmationToken!!
        c.pause(); c.confirm(token)
        assertNull(c.state.value.confirmationCard); assertEquals(1, reads)
        c.observe(CardReaderObservation.Present(1)); assertEquals(2, reads)
        assertNotEquals(token, c.state.value.confirmationToken)
    }
}
