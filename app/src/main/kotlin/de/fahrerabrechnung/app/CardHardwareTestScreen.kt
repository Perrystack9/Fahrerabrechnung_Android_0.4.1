package de.fahrerabrechnung.app

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import de.fahrerabrechnung.core.design.SectionCard
import de.fahrerabrechnung.data.card.AndroidCcidTransport
import de.fahrerabrechnung.data.card.CardDiagnostic
import de.fahrerabrechnung.data.card.CardDiagnosticProbe
import de.fahrerabrechnung.data.card.CardActivityParser
import de.fahrerabrechnung.data.card.CardActivityImportRepository
import de.fahrerabrechnung.data.card.CardVehicleImportRepository
import de.fahrerabrechnung.data.card.CardVehicleUse
import de.fahrerabrechnung.data.card.CardVehiclesParser
import de.fahrerabrechnung.data.card.CardReadResult
import de.fahrerabrechnung.data.card.CardDayTimeline
import de.fahrerabrechnung.data.card.CardDayVehicles
import de.fahrerabrechnung.data.card.LocalCardDay
import de.fahrerabrechnung.data.card.CardDaySegment
import de.fahrerabrechnung.data.card.DriverActivity
import de.fahrerabrechnung.data.card.ParseError
import de.fahrerabrechnung.data.card.CardGeneration
import de.fahrerabrechnung.data.card.DriverCardReader
import de.fahrerabrechnung.data.card.ReaderException
import de.fahrerabrechnung.data.card.ReaderState
import de.fahrerabrechnung.feature.common.ScreenContent
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.ZoneId
import java.time.format.DateTimeFormatter

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

/** Reader diagnostics and explicit, separately saved card source files. */
@Composable
fun CardHardwareTestScreen(padding: PaddingValues, imports: CardActivityImportRepository? = null,
                           vehicleImports: CardVehicleImportRepository? = null,
                           onOpenVehicleHistory: () -> Unit = {}) {
    val context = LocalContext.current
    val transport = remember(context) { AndroidCcidTransport(context) }
    val scope = rememberCoroutineScope()
    var diagnostic by remember { mutableStateOf<CardDiagnostic?>(null) }
    var message by remember { mutableStateOf("Kartenleser anschließen und Fahrerkarte einlegen.") }
    var busy by remember { mutableStateOf(false) }
    var pendingFullRead by remember { mutableStateOf(false) }
    var activityStatus by remember { mutableStateOf<String?>(null) }
    var localDays by remember { mutableStateOf<List<LocalCardDay>>(emptyList()) }
    var selectedDay by remember { mutableStateOf(0) }
    var displayZone by remember { mutableStateOf(ZoneId.systemDefault()) }
    var pendingRead by remember { mutableStateOf<CardReadResult?>(null) }
    var savedStatus by remember { mutableStateOf<String?>(null) }
    var vehicleStatus by remember { mutableStateOf<String?>(null) }
    var savedVehicleStatus by remember { mutableStateOf<String?>(null) }
    var vehicleUses by remember { mutableStateOf<List<CardVehicleUse>>(emptyList()) }
    var activityCardNumber by remember { mutableStateOf<String?>(null) }
    var vehicleCardNumber by remember { mutableStateOf<String?>(null) }
    var activitySavedForRead by remember { mutableStateOf(false) }
    var vehicleSavedForRead by remember { mutableStateOf(false) }

    LaunchedEffect(imports) {
        if (imports != null) try {
            imports.latest()?.let { saved ->
                displayZone = ZoneId.systemDefault()
                localDays = CardDayTimeline.build(CardActivityParser.parse(saved.file), displayZone)
                activityCardNumber = saved.cardNumber
                selectedDay = localDays.lastIndex.coerceAtLeast(0)
                savedStatus = "Gespeicherter Aktivitäts-Teilimport: ${saved.dayCount} Tagesdatensätze (${saved.generation.label()})."
            }
        } catch (_: Exception) {
            savedStatus = "Gespeicherte Aktivitätsdaten konnten nicht geöffnet werden."
        }
    }

    LaunchedEffect(vehicleImports) {
        if (vehicleImports != null) try {
            vehicleImports.latest()?.let { saved ->
                vehicleUses = saved.uses
                vehicleCardNumber = saved.cardNumber
                savedVehicleStatus = "Gespeicherter Fahrzeug-Teilimport: ${saved.uses.size} Nutzungsdatensätze."
            }
        } catch (_: Exception) {
            savedVehicleStatus = "Gespeicherte Fahrzeugdaten konnten nicht geöffnet werden."
        }
    }

    fun inspect(fullRead: Boolean) {
        if (busy) return
        busy = true
        scope.launch {
            try {
                if (fullRead) {
                    val result = DriverCardReader(transport).read()
                    activityStatus = result.activity?.let {
                        val days = CardActivityParser.parse(it)
                        pendingRead = if (result.card != null && days.isNotEmpty()) result else null
                        activityCardNumber = result.card?.card?.number
                        val vehicleFile = result.vehicles
                        var vehicleDecodeError: ParseError? = null
                        vehicleUses = try {
                            vehicleFile?.let(CardVehiclesParser::parse).orEmpty()
                        } catch (e: ParseError) {
                            vehicleDecodeError = e
                            emptyList()
                        }
                        vehicleCardNumber = if (vehicleFile != null && vehicleDecodeError == null)
                            result.card?.card?.number else null
                        vehicleStatus = when {
                            result.vehicleReadFailed -> "Fahrzeugdatei nicht gelesen; Aktivitätsdaten bleiben nutzbar."
                            vehicleFile == null -> "Keine Fahrzeugdatei verfügbar."
                            vehicleDecodeError != null -> "Fahrzeugdatei gelesen, aber nicht dekodierbar (${vehicleDecodeError.field}, Offset ${vehicleDecodeError.offset}); Aktivitätsdaten bleiben nutzbar."
                            vehicleUses.isEmpty() -> "Fahrzeugdatei gelesen; keine auswertbaren Nutzungen."
                            else -> "Fahrzeugdatei gelesen: ${vehicleFile.records.size} Bytes; ${vehicleUses.size} Nutzungen dekodiert."
                        }
                        displayZone = ZoneId.systemDefault()
                        localDays = CardDayTimeline.build(days, displayZone)
                        selectedDay = localDays.lastIndex.coerceAtLeast(0)
                        "Aktivitätsdatei gelesen: ${it.dailyRecords.size} Bytes (${result.generation.label()}); ${days.size} Tagesdatensätze dekodiert."
                    } ?: "Aktivitätsdatei nicht gelesen: Kartentyp oder Dateistruktur nicht erkannt."
                    message = "Lesetest abgeschlossen."
                } else {
                    diagnostic = CardDiagnosticProbe(transport).inspect()
                    message = "Prüfung abgeschlossen."
                }
            } catch (e: ParseError) {
                pendingRead = null
                localDays = emptyList()
                activityStatus = "Rohdatei gelesen, Tagesdaten nicht dekodierbar (${e.field})."
                vehicleStatus = null
            } catch (e: ReaderException) {
                pendingRead = null
                if (fullRead) localDays = emptyList()
                if (fullRead) activityStatus = "Aktivitätsdatei nicht gelesen. Fehlercode: ${e.failure.name}"
                if (fullRead) vehicleStatus = null
                else message = "Diagnose fehlgeschlagen. Fehlercode: ${e.failure.name}"
            } catch (_: Exception) {
                pendingRead = null
                if (fullRead) localDays = emptyList()
                if (fullRead) activityStatus = "Aktivitätsdatei nicht gelesen. Lesetest fehlgeschlagen."
                if (fullRead) vehicleStatus = null
                else message = "Diagnose fehlgeschlagen. Verbindung trennen und erneut versuchen."
            } finally {
                busy = false
            }
        }
    }

    fun startCheck(fullRead: Boolean) {
        pendingFullRead = fullRead
        if (fullRead) {
            pendingRead = null
            activitySavedForRead = false
            vehicleSavedForRead = false
            activityStatus = null
            vehicleStatus = null
            localDays = emptyList()
        } else diagnostic = null
        if (transport.devices().any { it.permission } || transport.devices().isEmpty()) inspect(fullRead)
        else {
            val intent = Intent(context, CardPermissionReceiver::class.java)
            val result = PendingIntent.getBroadcast(context, 0, intent,
                PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
            if (transport.requestPermission(result)) message = "USB-Freigabe am Gerät bestätigen."
            else inspect(fullRead)
        }
    }

    DisposableEffect(context, transport) {
        CardPermissionReceiver.callback = { device, granted ->
            if (device != null) transport.onPermissionResult(device, granted)
            if (granted) inspect(pendingFullRead) else {
                diagnostic = null
                message = "USB-Freigabe abgelehnt. Erneut prüfen, um sie anzufragen."
            }
        }
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(receiverContext: Context, intent: Intent) {
                if (intent.action == UsbManager.ACTION_USB_DEVICE_DETACHED) {
                    transport.close()
                    diagnostic = null
                    activityStatus = null
                    pendingRead = null
                    activitySavedForRead = false
                    vehicleSavedForRead = false
                    localDays = emptyList()
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
            Text("Der Kartendatentest liest Aktivitäts- und Fahrzeugdatei zunächst im Arbeitsspeicher. Jede Datei wird nur nach ausdrücklicher Auswahl verschlüsselt lokal gespeichert. Weitere Kartendateien fehlen noch.",
                style = MaterialTheme.typography.bodyMedium)
            Button(enabled = !busy, onClick = { startCheck(false) }) {
                Text(if (busy) "Prüfung läuft …" else "Kartenleser prüfen")
            }
            Button(enabled = !busy, onClick = { startCheck(true) }) {
                Text(if (busy) "Lesen läuft …" else "Kartendaten testen")
            }
            if (pendingRead != null && imports != null && !activitySavedForRead) {
                Button(enabled = !busy, onClick = {
                    val read = pendingRead ?: return@Button
                    busy = true
                    scope.launch {
                        try {
                            val inserted = imports.save(read)
                            savedStatus = if (inserted) "Aktivitäts-Teilimport verschlüsselt gespeichert."
                                else "Diese Aktivitätsdatei ist bereits gespeichert."
                            activitySavedForRead = true
                            if (vehicleSavedForRead || vehicleUses.isEmpty()) pendingRead = null
                        } catch (_: Exception) {
                            savedStatus = "Speichern fehlgeschlagen. Bitte erneut versuchen."
                        } finally {
                            busy = false
                        }
                    }
                }) { Text("Aktivitätsdaten lokal speichern") }
            }
            if (pendingRead?.vehicles != null && vehicleUses.isNotEmpty() &&
                vehicleImports != null && !vehicleSavedForRead) {
                Button(enabled = !busy, onClick = {
                    val read = pendingRead ?: return@Button
                    busy = true
                    scope.launch {
                        try {
                            val inserted = vehicleImports.save(read)
                            savedVehicleStatus = if (inserted) "Fahrzeug-Teilimport verschlüsselt gespeichert."
                                else "Diese Fahrzeugdatei ist bereits gespeichert."
                            vehicleSavedForRead = true
                            if (activitySavedForRead) pendingRead = null
                        } catch (_: Exception) {
                            savedVehicleStatus = "Fahrzeugdaten konnten nicht gespeichert werden."
                        } finally {
                            busy = false
                        }
                    }
                }) { Text("Fahrzeugdaten lokal speichern") }
            }
            activityStatus?.let { Text(it, style = MaterialTheme.typography.bodyMedium) }
            vehicleStatus?.let { Text(it, style = MaterialTheme.typography.bodyMedium) }
            savedStatus?.let { Text(it, style = MaterialTheme.typography.bodyMedium) }
            savedVehicleStatus?.let { Text(it, style = MaterialTheme.typography.bodyMedium) }
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
        if (vehicleUses.isNotEmpty()) SectionCard {
            Text("Fahrzeugnutzungen · Vorschau", style = MaterialTheme.typography.titleMedium)
            OutlinedButton(onClick = onOpenVehicleHistory) { Text("Fahrzeughistorie öffnen") }
            Text("Quelle: Fahrerkarte. Zeitpunkte in Ortszeit des Geräts; Kilometerstände sind Quellwerte, keine berechnete Tagesstrecke.")
            val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")
            vehicleUses.takeLast(5).asReversed().forEach { use ->
                Text("${use.registrationNumber.ifBlank { "Kennzeichen unbekannt" }} · Staat ${use.registrationNation}")
                val start = use.firstUseUtc.atZone(displayZone).format(formatter)
                val end = use.lastUseUtc?.atZone(displayZone)?.format(formatter) ?: "offen"
                Text("$start – $end · Tacho ${use.odometerBeginKm} → ${use.odometerEndKm?.toString() ?: "offen"} km")
            }
            if (vehicleUses.size > 5) Text("Die letzten 5 von ${vehicleUses.size} Nutzungen werden gezeigt.")
        }
        if (localDays.isNotEmpty()) {
            val day = localDays[selectedDay.coerceIn(localDays.indices)]
            val dayVehicles = if (activityCardNumber != null &&
                activityCardNumber == vehicleCardNumber)
                CardDayVehicles.forDate(day.date, displayZone, vehicleUses) else emptyList()
            SectionCard {
                Text("Kartentag · Vorschau", style = MaterialTheme.typography.titleMedium)
                Text("Ortszeit des Geräts (${displayZone.id}). Einträge ohne eingelegte Karte werden getrennt gekennzeichnet. Fehlende Kartentage bleiben offen; daraus folgt keine bestätigte Ruhe oder Heimkehr.",
                    style = MaterialTheme.typography.bodyMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = { selectedDay-- }, enabled = selectedDay > 0) {
                        Text("‹")
                    }
                    OutlinedButton(onClick = { selectedDay++ }, enabled = selectedDay < localDays.lastIndex) {
                        Text("›")
                    }
                }
                Text("${day.date.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))} · ${selectedDay + 1} / ${localDays.size}",
                    style = MaterialTheme.typography.titleMedium)
                CardTimelinePreview(day, displayZone, dayVehicles)
                if (vehicleUses.isNotEmpty() && activityCardNumber != vehicleCardNumber)
                    Text("Fahrzeugdaten stammen aus einem anderen Karten-Snapshot und werden hier nicht zugeordnet.",
                        style = MaterialTheme.typography.bodySmall)
                var showList by remember(day.date) { mutableStateOf(false) }
                OutlinedButton(onClick = { showList = !showList }) {
                    Text(if (showList) "Abschnittsliste ausblenden" else "Abschnittsliste anzeigen")
                }
                if (showList) day.segments.forEach { segment ->
                    CardActivityRow(segment, displayZone)
                }
            }
        }
    }
}

@Composable
private fun CardActivityRow(segment: CardDaySegment, zone: ZoneId) {
    val (label, color) = if (!segment.cardInserted) {
        "○ Karte nicht eingelegt · Karteneintrag: ${segment.activity.label()}" to Color(0xFF59636E)
    } else when (segment.activity) {
        DriverActivity.DRIVING -> "▶ Lenken" to Color(0xFF145A94)
        DriverActivity.WORK -> "◆ Andere Arbeit" to Color(0xFF875100)
        DriverActivity.AVAILABILITY -> "◷ Bereitschaft" to Color(0xFF6843A1)
        DriverActivity.REST -> "☾ Pause/Ruhe" to Color(0xFF16765A)
    }
    val formatter = DateTimeFormatter.ofPattern("HH:mm XXX")
    val start = segment.start.atZone(zone).format(formatter)
    val endTime = segment.end.atZone(zone)
    val end = endTime.format(formatter) +
        if (endTime.toLocalDate().isAfter(segment.start.atZone(zone).toLocalDate())) " (Folgetag)" else ""
    val minutes = Duration.between(segment.start, segment.end).toMinutes()
    Column {
        Text(label, color = color, style = MaterialTheme.typography.titleSmall)
        Text("$start – $end · ${minutes / 60} h ${(minutes % 60).toString().padStart(2, '0')} min",
            style = MaterialTheme.typography.bodyMedium)
    }
}

private fun DriverActivity.label() = when (this) {
    DriverActivity.DRIVING -> "Lenken"
    DriverActivity.WORK -> "Andere Arbeit"
    DriverActivity.AVAILABILITY -> "Bereitschaft"
    DriverActivity.REST -> "Pause/Ruhe"
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
