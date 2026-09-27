package de.fahrerabrechnung.app

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import de.fahrerabrechnung.core.design.SectionCard
import de.fahrerabrechnung.data.card.AndroidCcidTransport
import de.fahrerabrechnung.data.card.CardDiagnostic
import de.fahrerabrechnung.data.card.CardDiagnosticProbe
import de.fahrerabrechnung.data.card.CardGeneration
import de.fahrerabrechnung.data.card.ReaderState
import de.fahrerabrechnung.feature.common.ScreenContent
import kotlinx.coroutines.launch

/** Explicit PendingIntent target; Android's USB result needs a mutable PendingIntent. */
class CardPermissionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        @Suppress("DEPRECATION")
        val device = intent.getParcelableExtra<UsbDevice>(UsbManager.EXTRA_DEVICE)
        callback?.invoke(device, intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false))
    }

    companion object {
        var callback: ((UsbDevice?, Boolean) -> Unit)? = null
    }
}

/** Explicit diagnostic only. Card identities and raw card bytes are never displayed or stored. */
@Composable
fun CardHardwareTestScreen(padding: PaddingValues) {
    val context = LocalContext.current
    val transport = remember(context) { AndroidCcidTransport(context) }
    val scope = rememberCoroutineScope()
    var diagnostic by remember { mutableStateOf<CardDiagnostic?>(null) }
    var message by remember { mutableStateOf("Kartenleser anschließen und Fahrerkarte einlegen.") }
    var busy by remember { mutableStateOf(false) }

    fun inspect() {
        if (busy) return
        busy = true
        scope.launch {
            try {
                diagnostic = CardDiagnosticProbe(transport).inspect()
                message = "Prüfung abgeschlossen."
            } catch (_: Exception) {
                message = "Diagnose fehlgeschlagen. Verbindung trennen und erneut versuchen."
            } finally {
                busy = false
            }
        }
    }

    DisposableEffect(context, transport) {
        CardPermissionReceiver.callback = { device, granted ->
            if (device != null) transport.onPermissionResult(device, granted)
            if (granted) inspect() else {
                diagnostic = null
                message = "USB-Freigabe abgelehnt. Erneut prüfen, um sie anzufragen."
            }
        }
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(receiverContext: Context, intent: Intent) {
                if (intent.action == UsbManager.ACTION_USB_DEVICE_DETACHED) {
                    transport.close()
                    diagnostic = null
                    message = "Kartenleser entfernt."
                }
            }
        }
        ContextCompat.registerReceiver(context, receiver,
            IntentFilter(UsbManager.ACTION_USB_DEVICE_DETACHED), ContextCompat.RECEIVER_EXPORTED)
        onDispose {
            CardPermissionReceiver.callback = null
            context.unregisterReceiver(receiver)
            transport.close()
        }
    }

    ScreenContent(padding) {
        Text("Fahrerkarte einlesen", style = MaterialTheme.typography.titleLarge)
        SectionCard {
            Text("Technischer Lesertest", style = MaterialTheme.typography.titleMedium)
            Text("Dieser Test liest nur Erkennungsdaten im Arbeitsspeicher. Es wird noch kein Kartenimport gespeichert.",
                style = MaterialTheme.typography.bodyMedium)
            Button(enabled = !busy, onClick = {
                diagnostic = null
                if (transport.devices().any { it.permission } || transport.devices().isEmpty()) inspect()
                else {
                    val intent = Intent(context, CardPermissionReceiver::class.java)
                    val result = PendingIntent.getBroadcast(context, 0, intent,
                        PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
                    if (transport.requestPermission(result)) message = "USB-Freigabe am Gerät bestätigen."
                    else inspect()
                }
            }) { Text(if (busy) "Prüfung läuft …" else "Kartenleser prüfen") }
            Text(message, style = MaterialTheme.typography.bodyMedium)
            diagnostic?.let { result ->
                Text("Reader: ${result.reader.label()}")
                Text("USB: ${result.vendorId?.toString(16) ?: "–"}:${result.productId?.toString(16) ?: "–"}")
                Text("Freigabe: ${if (result.permission) "erteilt" else "fehlt"} · Slot: ${result.slot}")
                val atrText = if (result.atrProtocols.isEmpty()) "nicht gelesen"
                    else "T=" + result.atrProtocols.sorted().joinToString(", T=")
                Text("ATR: $atrText")
                Text("APDU: ${if (result.apduResponded) "Antwort erhalten" else "nicht bestätigt"}")
                Text("Kartengeneration: ${result.generation.label()}")
                result.failure?.let { Text("Fehlercode: ${it.name}") }
            }
        }
    }
}

private fun ReaderState.label() = when (this) {
    ReaderState.Absent -> "nicht gefunden"
    ReaderState.PermissionRequired -> "Freigabe nötig"
    ReaderState.PermissionDenied -> "Freigabe abgelehnt"
    ReaderState.Open -> "geöffnet"
    ReaderState.Empty -> "keine Karte"
    ReaderState.CardPresent -> "Karte vorhanden"
    ReaderState.Disconnected -> "Verbindung verloren"
}

private fun CardGeneration.label() = when (this) {
    CardGeneration.GEN1 -> "Gen1"
    CardGeneration.GEN2 -> "Gen2"
    CardGeneration.GEN2V2 -> "Gen2V2"
    CardGeneration.UNKNOWN -> "unklar"
    CardGeneration.UNSUPPORTED -> "nicht unterstützt"
}
