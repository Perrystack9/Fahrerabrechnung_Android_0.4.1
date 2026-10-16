package de.fahrerabrechnung.data.violation

import de.fahrerabrechnung.domain.violation.driving.DrivingCalculation
import de.fahrerabrechnung.domain.violation.driving.DrivingCalculator
import de.fahrerabrechnung.domain.violation.rest.DailyRestCalculation
import de.fahrerabrechnung.domain.violation.rest.DailyRestCalculator
import de.fahrerabrechnung.domain.violation.weekly.WeeklyRestCalculation
import de.fahrerabrechnung.domain.violation.weekly.WeeklyRestCalculator
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.withContext

/** Preparation states, never extra user-facing infringement statuses. M5 owns presentation. */
sealed interface DrivingCheckState {
    data object Preparing : DrivingCheckState
    data object NoProfile : DrivingCheckState
    data class Ready(val calculation: DrivingCalculation, val dailyRest: DailyRestCalculation,
        val weeklyRest: WeeklyRestCalculation) : DrivingCheckState
    data class Failed(val reason: String = "TIME_CALCULATION_FAILED") : DrivingCheckState
}

/** Automatic read-only recalculation after M1 publishes an entire committed import/context. */
class DrivingCheckRepository @Inject constructor(
    private val inputs: ViolationInputRepository,
    private val adapter: DriverCardInputAdapter,
) {
    fun observe(): Flow<DrivingCheckState> = channelFlow {
        inputs.observe().collectLatest { state ->
            send(DrivingCheckState.Preparing)
            when (state) {
                ViolationInputState.Preparing -> Unit
                ViolationInputState.NoProfile -> send(DrivingCheckState.NoProfile)
                is ViolationInputState.Failed -> send(DrivingCheckState.Failed("INPUT_SNAPSHOT_FAILED"))
                is ViolationInputState.Ready -> try {
                    val result = withContext(Dispatchers.Default) {
                        val context = currentCoroutineContext()
                        val driving = DrivingCalculator.calculate(state.snapshot, checkCancellation = { context.ensureActive() })
                        val rest = DailyRestCalculator.calculate(state.snapshot, driving,
                            checkCancellation = { context.ensureActive() })
                        val weekly = WeeklyRestCalculator.calculate(state.snapshot, driving, rest,
                            checkCancellation = { context.ensureActive() })
                        DrivingCheckState.Ready(driving, weekly.dailyRest, weekly)
                    }
                    currentCoroutineContext().ensureActive()
                    // A newer outer commit may win while the arithmetic runs. Never expose the old result.
                    adapter.publishIfCurrent(state.snapshot) { trySend(result).getOrThrow() }
                } catch (cancelled: CancellationException) {
                    throw cancelled
                } catch (_: Exception) {
                    currentCoroutineContext().ensureActive()
                    send(DrivingCheckState.Failed()) // No raw exception, card data, APDU or account identifiers.
                }
            }
        }
    }.buffer(Channel.CONFLATED)
}
