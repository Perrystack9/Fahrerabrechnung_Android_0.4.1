package de.fahrerabrechnung.data.violation

import de.fahrerabrechnung.data.db.AppDatabase
import de.fahrerabrechnung.domain.violation.input.InputSnapshotBuilder
import de.fahrerabrechnung.domain.violation.input.ViolationInputSnapshot
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

/** Input preparation states, not violation statuses or a legal all-clear. */
sealed interface ViolationInputState {
    data object Preparing : ViolationInputState
    data object NoProfile : ViolationInputState
    data class Ready(val snapshot: ViolationInputSnapshot) : ViolationInputState
    data class Failed(val reason: String = "INPUT_SNAPSHOT_FAILED") : ViolationInputState
}

/**
 * The downstream engine subscribes to this flow. Room invalidates only after the outer import or
 * context transaction commits, including both applications. M1 deliberately starts no M2 rule run.
 */
class ViolationInputRepository @Inject constructor(private val db: AppDatabase, private val adapter: DriverCardInputAdapter) {
    fun observe(): Flow<ViolationInputState> = channelFlow {
        db.invalidationTracker.createFlow(*OBSERVED_TABLES.toTypedArray()).collectLatest {
            send(ViolationInputState.Preparing)
            try {
                while (true) {
                    currentCoroutineContext().ensureActive()
                    val captured = adapter.capture()
                    val result = if (captured.sources == null) ViolationInputState.NoProfile else {
                        val context = checkNotNull(captured.context)
                        withContext(Dispatchers.Default) {
                            val jobContext = currentCoroutineContext()
                            ViolationInputState.Ready(InputSnapshotBuilder.build(captured.sources, context,
                                checkCancellation = { jobContext.ensureActive() }))
                        }
                    }
                    currentCoroutineContext().ensureActive()
                    if (adapter.publishIfCurrent(captured.token) { trySend(result).getOrThrow() }) break
                    // A committed import/context revision won the race. Never publish the old result.
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                currentCoroutineContext().ensureActive()
                send(ViolationInputState.Failed()) // No raw exception or sensitive card content in diagnostics.
            }
        }
    }.buffer(Channel.CONFLATED)

    companion object {
        /** Existing card tables plus the isolated context; no settings/display-zone dependency. */
        internal val OBSERVED_TABLES = setOf(
            "profiles", "local_driver_profile", "driver_cards", "driver_card_identities", "import_sessions",
            "driver_import_sources", "card_activity_imports", "driver_activity_days", "driver_activity_changes",
            "driver_activity_day_sources", "card_vehicle_imports", "driver_vehicle_uses", "driver_vehicle_use_sources",
            "card_source_pairs", "card_import_runs", "card_import_file_results", "card_read_batches", "card_application_attempts",
            "card_master_sources", "card_batch_master_files", "card_metadata_imports", "driver_import_metadata_sources",
            "card_core_signature_receipts", "card_identity_signature_receipts", "card_event_fault_signature_receipts",
            "card_control_imports", "driver_import_control_sources", "card_event_fault_imports", "driver_import_event_fault_sources",
            "card_specific_condition_imports", "driver_import_specific_condition_sources", "card_gnss_imports", "driver_import_gnss_sources",
            "card_place_imports", "driver_import_place_sources", "card_border_crossing_imports", "driver_import_border_crossing_sources",
            "card_load_unload_imports", "driver_import_load_unload_sources", "card_load_type_imports", "driver_import_load_type_sources",
            "card_vehicle_units_imports", "driver_import_vehicle_units_sources", "violation_context_revisions",
        )
    }
}
