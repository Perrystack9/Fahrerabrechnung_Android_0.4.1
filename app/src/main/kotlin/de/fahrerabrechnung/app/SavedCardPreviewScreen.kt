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
                                    onOpenVehicles: (String?, Long?, Int?) -> Unit, archive: Boolean = false) {
    val zone = remember { ZoneId.systemDefault() }
    val days = remember(activity, zone) { activity?.let { CardDayTimeline.build(it.days, zone) }.orEmpty() }
    var selected by remember(activity) { mutableStateOf(days.lastIndex.coerceAtLeast(0)) }
    val uses = vehicles?.uses.orEmpty()
    ScreenContent(padding) {
        Text(if (archive) "Gespeicherte Dateivorschau" else "Gespeicherte Kartendaten", style = MaterialTheme.typography.titleLarge)
        if (archive) Text("Diese Dateivorschau ordnet ältere Daten keinem Fahrerprofil zu.")
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
            SectionCard {
                Text(day.date.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")), style = MaterialTheme.typography.titleMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(enabled = selected > 0, onClick = { selected-- }) { Text("‹") }
                    OutlinedButton(enabled = selected < days.lastIndex, onClick = { selected++ }) { Text("›") }
                }
                CardTimelinePreview(day, zone, dayUses, onOpenVehicles)
                CardDaySourceTimes(day, zone)
                Text("Ortszeit (${zone.id}). Fehlende Zeiten bleiben offen und belegen keine Ruhe oder Heimkehr.",
                    style = MaterialTheme.typography.bodySmall)
            }
        }
        if (days.isEmpty() && uses.isEmpty()) Text("Für diese Auswahl sind keine auswertbaren Kartendaten verfügbar.")
    }
}
