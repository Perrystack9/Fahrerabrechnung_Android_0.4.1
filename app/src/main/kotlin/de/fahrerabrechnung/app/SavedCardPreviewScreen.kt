package de.fahrerabrechnung.app

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.unit.dp
import de.fahrerabrechnung.core.design.SectionCard
import de.fahrerabrechnung.data.card.*
import de.fahrerabrechnung.feature.common.ScreenContent
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/** Pure saved-source preview: no reader operations, saving or guessed pairing. */
@Composable
internal fun SavedCardPreviewScreen(padding: PaddingValues, activity: SavedCardActivity?, vehicles: SavedCardVehicles?,
                                    onOpenVehicles: (String?, Long?, Int?) -> Unit, archive: Boolean = false,
                                    onOpenDetails: (() -> Unit)? = null,
                                    driverCardId: String? = null,
                                    events: SavedCardEventFault? = null, faults: SavedCardEventFault? = null,
                                    specificConditions: SavedCardSpecificConditions? = null,
                                    receiptId: String? = null, applicationGeneration: CardGeneration? = null,
                                    coreSnapshots: Map<CardCoreEf, SavedCardCoreSnapshot> = emptyMap(),
                                    coreSignatures: Map<CardCoreEf, CardCoreSignature> = emptyMap()) {
    val zone = remember { ZoneId.systemDefault() }
    val days = remember(activity, zone) { activity?.let { CardDayTimeline.build(it.days, zone) }.orEmpty() }
    var selected by remember(activity, receiptId) { mutableStateOf(days.lastIndex.coerceAtLeast(0)) }
    val uses = vehicles?.uses.orEmpty()
    ScreenContent(padding) {
        Text(if (archive) "Gespeicherte Dateivorschau" else "Gespeicherte Kartendaten", style = MaterialTheme.typography.titleLarge)
        applicationGeneration?.let { Text("Ausgewählte Anwendung: ${it.name}") }
        if (archive) Text("Diese Dateivorschau ordnet ältere Daten keinem Fahrerprofil zu.")
        if (!archive && onOpenDetails != null)
            OutlinedButton(onClick = onOpenDetails) { Text("Fahrerkarten-Details") }
        if (!archive) SectionCard {
            Text("Gespeicherte Originaldateien", style = MaterialTheme.typography.titleMedium)
            CardCoreEf.entries.forEach { kind ->
                val label = if (kind == CardCoreEf.ACTIVITY) "Tätigkeiten" else "Fahrzeuge"
                val original = coreSnapshots[kind]?.snapshot
                Text(if (original == null) "$label: keine Originaldatei in dieser Übernahme"
                    else "$label: ${original.bytes.size} Bytes · ${original.generation.name}")
            }
        }
        if (!archive) SectionCard {
            Text("Kartensignaturen", style = MaterialTheme.typography.titleMedium)
            CardCoreEf.entries.forEach { kind ->
                val label = if (kind == CardCoreEf.ACTIVITY) "Tätigkeiten" else "Fahrzeuge"
                val signature = coreSignatures[kind]
                Text(when (signature?.status) {
                    CardSignatureStatus.CAPTURED_UNVERIFIED -> "$label: ${signature.actualLength} Bytes · aufgenommen · ungeprüft"
                    CardSignatureStatus.FAILED -> "$label: Aufnahme fehlgeschlagen · ${signature.reason.signatureMessage()}"
                    CardSignatureStatus.NOT_ATTEMPTED -> "$label: nicht versucht · ${signature.reason.signatureMessage()}"
                    null -> "$label: Signatur in diesem Import nicht aufgenommen"
                })
            }
        }
        if (uses.isNotEmpty()) SectionCard {
            Text("Fahrzeugnutzungen", style = MaterialTheme.typography.titleMedium)
            OutlinedButton(onClick = { onOpenVehicles(null, null, null) }) { Text("Fahrzeughistorie") }
            val format = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")
            uses.takeLast(5).asReversed().forEach {
                Text("${it.registrationNumber.ifBlank { "Kennzeichen unbekannt" }} · Staat ${it.registrationNation}")
                Text("${it.firstUseUtc.atZone(zone).format(format)} – ${it.lastUseUtc?.atZone(zone)?.format(format) ?: "offen"}")
                Text("Tacho ${it.odometerBeginKm} → ${it.odometerEndKm?.toString() ?: "offen"} km")
            }
            Text("Kilometerstände aus der Karte; keine berechnete Tagesstrecke.", style = MaterialTheme.typography.bodySmall)
        }
        if (days.isNotEmpty()) {
            val day = days[selected.coerceIn(days.indices)]
            val dayUses = if (activity?.cardNumber == vehicles?.cardNumber)
                CardDayVehicles.forDate(day.date, zone, uses) else emptyList()
            val dayIssues = remember(day.date, zone, archive, driverCardId, events, faults) {
                if (archive) emptyList() else CardDayEventFaultMarkers.forDate(day.date, zone, driverCardId, events, faults)
            }
            val dayConditions = remember(day.date, zone, archive, driverCardId, specificConditions) {
                if (archive) emptyList() else CardDaySpecificConditionMarkers.forDate(day.date, zone, driverCardId, specificConditions)
            }
            SectionCard {
                Text(day.date.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")), style = MaterialTheme.typography.titleMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(enabled = selected > 0, onClick = { selected-- }) { Text("‹") }
                    OutlinedButton(enabled = selected < days.lastIndex, onClick = { selected++ }) { Text("›") }
                }
                CardTimelinePreview(day, zone, dayUses, onOpenVehicles, eventFaultMarkers = dayIssues,
                    specificConditionMarkers = dayConditions)
                CardDaySourceTimes(day, zone)
                Text("Ortszeit (${zone.id}). Fehlende Zeiten bleiben offen und belegen keine Ruhe oder Heimkehr.",
                    style = MaterialTheme.typography.bodySmall)
            }
        }
        if (days.isEmpty() && uses.isEmpty()) Text("Für diese Auswahl sind keine auswertbaren Kartendaten verfügbar.")
    }
}

internal fun CardSignatureReason?.signatureMessage(): String = when (this) {
    CardSignatureReason.CAPACITY_UNAVAILABLE -> "Dateigröße unbekannt"
    CardSignatureReason.EF_ABSENT -> "Datei nicht vorhanden"
    CardSignatureReason.SIGNING_CERTIFICATE_UNAVAILABLE -> "passendes Signaturzertifikat fehlt"
    CardSignatureReason.BOUNDARY_UNSUPPORTED -> "Dateiende nicht prüfbar"
    CardSignatureReason.BOUNDARY_UNPROVEN -> "Dateiende nicht bestätigt"
    CardSignatureReason.INCOMPLETE_READ -> "Datei nicht vollständig gelesen"
    CardSignatureReason.HASH_RESPONSE -> "Dateihash nicht bestätigt"
    CardSignatureReason.SIGNATURE_LENGTH -> "unerwartete Signaturlänge"
    CardSignatureReason.BLANK_SIGNATURE -> "leere Signaturantwort"
    CardSignatureReason.CARD_STATUS -> "Karte meldet einen Fehler"
    CardSignatureReason.READER_FAILURE -> "Leserfehler"
    CardSignatureReason.UNSUPPORTED_FORMAT -> "Dateiformat nicht unterstützt"
    null -> "kein Beleg"
}
