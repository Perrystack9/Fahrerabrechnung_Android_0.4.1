package de.fahrerabrechnung.data.card

import java.util.UUID
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

enum class CardImportPhase {
    WAITING_FOR_READER, USB_PERMISSION_REQUIRED, WAITING_FOR_CARD, READING,
    CONFIRM_FIRST_CARD, CONFIRM_REPLACEMENT, SAVING, IMPORTED, INCOMPLETE,
    FOREIGN_CARD, CANCELLED, ERROR,
}

data class CardImportUiState(
    val phase: CardImportPhase = CardImportPhase.WAITING_FOR_READER,
    val confirmationToken: String? = null,
    val confirmationCard: DriverCard? = null,
    val lastRun: CardImportRun? = null,
    val readerFailure: ReaderFailure? = null,
)

sealed interface CardReaderObservation {
    data object NoReader : CardReaderObservation
    data class PermissionRequired(val deviceKey: Int) : CardReaderObservation
    data class Empty(val deviceKey: Int) : CardReaderObservation
    data class Present(val deviceKey: Int) : CardReaderObservation
}

/** One read per observed insertion. Native USB/lifecycle/permission handling stays outside. */
class CardImportCoordinator(
    private val readCard: suspend () -> CardReadResult,
    private val reviewCard: suspend (DriverCard) -> CardBindingReview,
    private val confirmOwnCard: suspend (DriverCard) -> Unit,
    private val saveRead: suspend (CardReadResult, String) -> CardImportRun,
) {
    private val operations = Mutex()
    private val mutableState = MutableStateFlow(CardImportUiState())
    val state: StateFlow<CardImportUiState> = mutableState.asStateFlow()
    private var latchedDevice: Int? = null
    private var pending: CardReadResult? = null

    suspend fun observe(observation: CardReaderObservation) = operations.withLock {
        observeInside(observation)
    }

    suspend fun poll(probe: suspend () -> CardReaderObservation) = operations.withLock {
        try { observeInside(probe()) }
        catch (cancelled: CancellationException) { reset(CardImportPhase.WAITING_FOR_READER); throw cancelled }
        catch (failure: ReaderException) { pending = null; change(CardImportPhase.ERROR, failure = failure.failure) }
    }

    private suspend fun observeInside(observation: CardReaderObservation) {
        when (observation) {
            CardReaderObservation.NoReader -> reset(CardImportPhase.WAITING_FOR_READER)
            is CardReaderObservation.Empty -> reset(CardImportPhase.WAITING_FOR_CARD)
            is CardReaderObservation.PermissionRequired -> reset(CardImportPhase.USB_PERMISSION_REQUIRED)
            is CardReaderObservation.Present -> {
                if (latchedDevice == observation.deviceKey) return
                latchedDevice = observation.deviceKey
                perform { accept(readCard()) }
            }
        }
    }

    /** The live card is re-read before binding; a stale dialog can never bind a swapped card. */
    suspend fun confirm(token: String) = operations.withLock {
        val expected = pending?.card ?: return@withLock
        if (mutableState.value.confirmationToken != token) return@withLock
        perform {
            val current = freeze(readCard())
            if (current.card != expected || current.recognition != CardRecognition.DRIVER) {
                accept(current)
            } else {
                confirmOwnCard(expected)
                pending = null
                save(current)
            }
        }
    }

    suspend fun decline(token: String) = operations.withLock {
        if (mutableState.value.confirmationToken == token) {
            pending = null
            change(CardImportPhase.CANCELLED)
        }
    }

    suspend fun retry() = operations.withLock { reset(CardImportPhase.WAITING_FOR_READER) }
    suspend fun pause() = operations.withLock {
        latchedDevice = null; pending = null
        if (mutableState.value.phase in listOf(CardImportPhase.READING, CardImportPhase.SAVING,
                CardImportPhase.CONFIRM_FIRST_CARD, CardImportPhase.CONFIRM_REPLACEMENT))
            change(CardImportPhase.WAITING_FOR_READER)
    }
    suspend fun readerFailed(failure: ReaderFailure) = operations.withLock {
        pending = null
        change(CardImportPhase.ERROR, failure = failure)
    }

    suspend fun restoreLastRun(run: CardImportRun?) = operations.withLock {
        if (mutableState.value.lastRun == null) mutableState.value = mutableState.value.copy(lastRun = run)
    }

    private suspend fun accept(result: CardReadResult) {
        pending = null
        if (result.recognition != CardRecognition.DRIVER || result.card == null) {
            change(CardImportPhase.ERROR)
            return
        }
        val read = freeze(result)
        val review = reviewCard(checkNotNull(read.card))
        when (review.kind) {
            CardBindingKind.FOREIGN_CARD -> change(CardImportPhase.FOREIGN_CARD)
            CardBindingKind.FIRST_CARD, CardBindingKind.CONFIRM_REPLACEMENT -> {
                pending = read
                val phase = if (review.kind == CardBindingKind.FIRST_CARD)
                    CardImportPhase.CONFIRM_FIRST_CARD else CardImportPhase.CONFIRM_REPLACEMENT
                change(phase, UUID.randomUUID().toString(), read.card)
            }
            CardBindingKind.KNOWN_CARD, CardBindingKind.SAME_HOLDER -> save(read)
        }
    }

    private suspend fun save(result: CardReadResult) {
        change(CardImportPhase.SAVING)
        val run = saveRead(result, UUID.randomUUID().toString())
        val phase = when (run.status) {
            CardImportRunStatus.SAVED -> CardImportPhase.IMPORTED
            CardImportRunStatus.PARTIAL -> CardImportPhase.INCOMPLETE
            else -> CardImportPhase.ERROR
        }
        mutableState.value = CardImportUiState(phase = phase, lastRun = run)
    }

    private suspend fun perform(action: suspend () -> Unit) {
        change(CardImportPhase.READING)
        try { action() }
        catch (cancelled: CancellationException) { reset(CardImportPhase.WAITING_FOR_READER); throw cancelled }
        catch (failure: ReaderException) { pending = null; change(CardImportPhase.ERROR, failure = failure.failure) }
        catch (_: ForeignCardRejected) { pending = null; change(CardImportPhase.FOREIGN_CARD) }
        catch (_: Exception) { pending = null; change(CardImportPhase.ERROR) }
    }

    private fun reset(phase: CardImportPhase) {
        latchedDevice = null
        pending = null
        change(phase)
    }

    private fun change(phase: CardImportPhase, token: String? = null, card: DriverCard? = null,
                       failure: ReaderFailure? = null) {
        mutableState.value = CardImportUiState(phase, token, card, mutableState.value.lastRun, failure)
    }

    private fun freeze(result: CardReadResult) = result.copy(
        activity = result.activity?.let { CardActivityFile(it.oldestDayOffset, it.newestDayOffset, it.dailyRecords.copyOf()) },
        vehicles = result.vehicles?.let { CardVehiclesFile(it.newestRecordIndex, it.recordCount, it.generation, it.records.copyOf()) },
        controls = result.controls?.let { CardControlFile(it.generation, it.bytes) },
        events = result.events?.let { CardEventFaultFile(it.kind, it.generation, it.recordsPerType, it.bytes) },
        faults = result.faults?.let { CardEventFaultFile(it.kind, it.generation, it.recordsPerType, it.bytes) },
        specificConditions = result.specificConditions?.let { CardSpecificConditionFile(it.generation, it.recordCount, it.bytes) },
        gnssPositions = result.gnssPositions?.let { CardGnssAccumulatedDrivingFile(it.generation, it.recordCount, it.bytes) },
        gnssAuthentication = result.gnssAuthentication?.let { CardGnssAuthenticationFile(it.generation, it.bytes) },
        places = result.places?.let { CardPlacesFile(it.generation, it.recordCount, it.bytes) },
        placesAuthentication = result.placesAuthentication?.let { CardPlacesAuthenticationFile(it.generation, it.bytes) },
    )
}
