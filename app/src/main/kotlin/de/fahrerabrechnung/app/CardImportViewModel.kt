package de.fahrerabrechnung.app

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import de.fahrerabrechnung.data.card.*
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** Separate explicit result receiver so the retained technical test cannot consume this request. */
class AutomaticCardPermissionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ACTION) return
        @Suppress("DEPRECATION")
        val device = intent.getParcelableExtra<UsbDevice>(UsbManager.EXTRA_DEVICE) ?: return
        callback?.invoke(device, intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false))
    }
    companion object {
        const val ACTION = "de.fahrerabrechnung.app.AUTOMATIC_CARD_USB_PERMISSION"
        var callback: ((UsbDevice, Boolean) -> Unit)? = null
    }
}

@HiltViewModel
class CardImportViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    imports: DriverCardImportRepository,
    private val pipeline: DriverCardImportPipeline,
) : ViewModel() {
    private val transport = AndroidCcidTransport(context)
    private val coordinator = CardImportCoordinator(
        { DriverCardReader(transport).read() }, imports::review,
        { imports.bind(it, confirmedOwnCard = true); Unit }, pipeline::process,
    )
    val state = coordinator.state
    private var foreground = false
    private var currentDevice: Int? = null
    private var requestedPermission: Int? = null
    private var deniedPermission: Int? = null
    private var blockedDevice: Int? = null
    private var action: Job? = null
    private var observedDetach = false

    /** A single resumed Activity owns probing and reads. No background job/service. */
    suspend fun monitor() {
        foreground = true
        try {
            coordinator.restoreLastRun(pipeline.history().firstOrNull { it.sessionId != null })
            while (currentCoroutineContext().isActive) {
                if (observedDetach) {
                    coordinator.observe(CardReaderObservation.NoReader)
                    observedDetach = false
                }
                val devices = transport.devices()
                val device = devices.firstOrNull { it.deviceId == currentDevice && it.permission }
                    ?: devices.firstOrNull { it.permission }
                    ?: devices.firstOrNull { it.deviceId == currentDevice } ?: devices.firstOrNull()
                val key = device?.deviceId
                if (key != currentDevice) {
                    currentDevice = key
                    requestedPermission = null
                    deniedPermission = null
                    blockedDevice = null
                }
                if (device != null && !device.permission && key == deniedPermission)
                    coordinator.readerFailed(ReaderFailure.PERMISSION)
                if (key == null || key != blockedDevice) {
                    coordinator.poll {
                        transport.selectDevice(key)
                        if (device == null) {
                            if (transport.availability() == SourceAvailability.UNKNOWN)
                                throw ReaderException(ReaderFailure.UNSUPPORTED_READER)
                            CardReaderObservation.NoReader
                        } else if (!device.permission) CardReaderObservation.PermissionRequired(device.deviceId)
                        else {
                            try {
                                when (transport.open()) {
                                    ReaderState.Open, ReaderState.CardPresent -> when (transport.slotStatus()) {
                                        ReaderState.Empty -> CardReaderObservation.Empty(device.deviceId)
                                        else -> CardReaderObservation.Present(device.deviceId)
                                    }
                                    ReaderState.PermissionRequired, ReaderState.PermissionDenied -> CardReaderObservation.PermissionRequired(device.deviceId)
                                    ReaderState.Empty -> CardReaderObservation.Empty(device.deviceId)
                                    else -> CardReaderObservation.NoReader
                                }
                            } catch (failure: ReaderException) {
                                blockedDevice = device.deviceId
                                throw failure
                            } finally { transport.close() }
                        }
                    }
                    if (state.value.phase == CardImportPhase.USB_PERMISSION_REQUIRED && key != null &&
                        requestedPermission != key && deniedPermission != key) {
                        requestedPermission = key
                        val intent = Intent(context, AutomaticCardPermissionReceiver::class.java)
                            .setAction(AutomaticCardPermissionReceiver.ACTION)
                        val pending = PendingIntent.getBroadcast(context, key, intent,
                            PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
                        if (!transport.requestPermission(pending)) coordinator.readerFailed(ReaderFailure.PERMISSION)
                    }
                }
                delay(2_000)
            }
        } catch (cancelled: CancellationException) { throw cancelled }
        catch (_: Exception) { coordinator.readerFailed(ReaderFailure.IO) }
        finally {
            foreground = false
            transport.close()
            withContext(NonCancellable) {
                action?.cancelAndJoin()
                coordinator.pause()
            }
        }
    }

    fun permissionResult(device: UsbDevice, granted: Boolean) {
        if (device.deviceId != requestedPermission || transport.devices().none { it.deviceId == device.deviceId }) return
        transport.onPermissionResult(device, granted)
        if (granted) { deniedPermission = null; blockedDevice = null }
        else {
            deniedPermission = device.deviceId
            blockedDevice = device.deviceId
            launchAction { coordinator.readerFailed(ReaderFailure.PERMISSION) }
        }
    }

    fun detached(device: UsbDevice) {
        if (device.deviceId == currentDevice && transport.devices().none { it.deviceId == device.deviceId }) {
            observedDetach = true
            requestedPermission = null; deniedPermission = null; blockedDevice = null
            transport.close()
        }
    }

    fun confirm(token: String) = launchAction { coordinator.confirm(token) }
    fun decline(token: String) = launchAction { coordinator.decline(token) }
    fun retry() = launchAction {
        requestedPermission = null; deniedPermission = null; blockedDevice = null
        coordinator.retry()
    }

    private fun launchAction(block: suspend () -> Unit) {
        if (!foreground || action?.isActive == true) return
        action = viewModelScope.launch { block() }
    }

    override fun onCleared() { transport.close(); super.onCleared() }
}

@Composable
internal fun CardImportObserver(model: CardImportViewModel, enabled: Boolean) {
    val owner = LocalLifecycleOwner.current
    val context = androidx.compose.ui.platform.LocalContext.current
    DisposableEffect(model, context, enabled) {
        if (enabled) AutomaticCardPermissionReceiver.callback = model::permissionResult
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(receiverContext: Context, intent: Intent) {
                if (intent.action != UsbManager.ACTION_USB_DEVICE_DETACHED) return
                @Suppress("DEPRECATION")
                val device = intent.getParcelableExtra<UsbDevice>(UsbManager.EXTRA_DEVICE) ?: return
                model.detached(device)
            }
        }
        if (enabled) ContextCompat.registerReceiver(context, receiver,
            IntentFilter(UsbManager.ACTION_USB_DEVICE_DETACHED), ContextCompat.RECEIVER_EXPORTED)
        onDispose {
            if (enabled) { AutomaticCardPermissionReceiver.callback = null; context.unregisterReceiver(receiver) }
        }
    }
    LaunchedEffect(model, owner, enabled) {
        if (enabled) owner.lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) { model.monitor() }
    }
}
