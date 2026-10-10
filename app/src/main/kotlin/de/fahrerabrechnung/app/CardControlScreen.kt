package de.fahrerabrechnung.app

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import de.fahrerabrechnung.core.design.SectionCard
import de.fahrerabrechnung.data.card.CardGeneration
import de.fahrerabrechnung.data.card.SavedCardControl
import de.fahrerabrechnung.feature.common.ScreenContent
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
internal fun CardDetailsScreen(padding: PaddingValues, onControls: () -> Unit, onEventsFaults: () -> Unit,
                               onSpecificConditions: () -> Unit = {}, onGnss: () -> Unit = {},
                               onPlaces: () -> Unit = {},
                               onBorders: () -> Unit = {}, onLoadUnload: () -> Unit = {}, onLoadType: () -> Unit = {}, onVehicleUnits: () -> Unit = {}) {
    ScreenContent(padding) {
        Text("Fahrerkarte · Details", style = MaterialTheme.typography.titleLarge)
        OutlinedButton(onClick = onControls) { Text("Kontrollen") }
        OutlinedButton(onClick = onEventsFaults) { Text("Ereignisse und Störungen") }
        OutlinedButton(onClick = onSpecificConditions) { Text("Fähre/Zug und Out of Scope") }
        OutlinedButton(onClick = onGnss) { Text("GNSS-Aufzeichnungen") }
        OutlinedButton(onClick = onPlaces) { Text("Beginn-/Ende-Orte") }
        OutlinedButton(onClick = onBorders) { Text("Grenz- und Ländereinträge") }
        OutlinedButton(onClick = onLoadUnload) { Text("Be- und Entladeeinträge") }
        OutlinedButton(onClick = onLoadType) { Text("Ladungsart-Einträge") }
        OutlinedButton(onClick = onVehicleUnits) { Text("Fahrtenschreiber-Nutzungen") }
        Text("Angaben aus der ausgewählten gespeicherten Übernahme.", style = MaterialTheme.typography.bodySmall)
    }
}

/** Exact selected own source. Missing source and a valid unused EF are distinct. */
@Composable
internal fun CardControlScreen(padding: PaddingValues, saved: SavedCardControl?) {
    val zone = remember { ZoneId.systemDefault() }
    val format = remember { DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss") }
    var showNumber by remember(saved?.id) { mutableStateOf(false) }
    ScreenContent(padding) {
        Text("Kontrollen", style = MaterialTheme.typography.titleLarge)
        when {
            saved == null -> Text("Für diese Übernahme sind keine gespeicherten Kontrolldaten verfügbar.")
            saved.record == null -> Text("In dieser Kartendatei ist keine Kontrolle aufgezeichnet.")
            else -> {
                val control = checkNotNull(saved.record)
                SectionCard {
                    Text("Letzte aufgezeichnete Kontrolle", style = MaterialTheme.typography.titleMedium)
                    Text(control.timeUtc.atZone(zone).format(format))
                    Text("Ortszeit (${zone.id})", style = MaterialTheme.typography.bodySmall)
                    val labels = buildList {
                        if (control.controlType and 0x80 != 0) add("Fahrerkarte heruntergeladen")
                        if (control.controlType and 0x40 != 0) add("Fahrzeuggerät heruntergeladen")
                        if (control.controlType and 0x20 != 0) add("Ausdruck erstellt")
                        if (control.controlType and 0x10 != 0) add("Anzeige geprüft")
                        if (saved.file.generation != CardGeneration.GEN1 && control.controlType and 0x08 != 0)
                            add("Kalibrierungsparameter geprüft")
                    }
                    if (labels.isEmpty()) Text("Kontrollart nicht angegeben") else labels.forEach { Text(it) }
                    val knownBits = if (saved.file.generation == CardGeneration.GEN1) 0xF0 else 0xF8
                    if (control.controlType and knownBits.inv() != 0)
                        Text("Weitere Kontrollmerkmale sind nicht zugeordnet.", style = MaterialTheme.typography.bodySmall)
                    control.vehicleRegistrationNumber?.let {
                        Text("Fahrzeug: $it · Staat ${control.vehicleRegistrationNation}")
                    }
                    control.controlCardNumber?.let { number ->
                        Text("Kontrollkarte: ${if (showNumber) number else maskCardNumber(number)}")
                        Text("Ausstellungsstaat: ${control.controlCardIssuingState}")
                        OutlinedButton(onClick = { showNumber = !showNumber }) {
                            Text(if (showNumber) "Kartennummer verbergen" else "Kartennummer anzeigen")
                        }
                    }
                    control.downloadBeginUtc?.let { Text("Heruntergeladener Zeitraum ab: ${it.atZone(zone).format(format)}") }
                    control.downloadEndUtc?.let { Text("Heruntergeladener Zeitraum bis: ${it.atZone(zone).format(format)}") }
                    val begin = control.downloadBeginUtc
                    val end = control.downloadEndUtc
                    if (begin != null && end != null && end < begin)
                        Text("Die gespeicherten Zeitraumangaben widersprechen sich.")
                }
                Text("Die Fahrerkarte enthält hier nur den letzten Kontrollbeleg. Ältere Übernahmen behalten ihren damaligen Stand.",
                    style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
