package de.fahrerabrechnung.app

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import de.fahrerabrechnung.core.design.SectionCard
import de.fahrerabrechnung.data.card.CardVehicleHistory
import de.fahrerabrechnung.data.card.CardVehicleHistoryEntry
import de.fahrerabrechnung.data.card.CardVehicleImportRepository
import de.fahrerabrechnung.feature.common.ScreenContent
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/** Read-only history from explicitly saved card snapshots. */
@Composable
internal fun VehicleHistoryScreen(padding: PaddingValues, imports: CardVehicleImportRepository?) {
    var history by remember { mutableStateOf<List<CardVehicleHistoryEntry>>(emptyList()) }
    var status by remember { mutableStateOf("Lade gespeicherte Fahrzeugdaten …") }
    val zone = remember { ZoneId.systemDefault() }
    val formatter = remember { DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm") }
    LaunchedEffect(imports) {
        try {
            history = imports?.all()?.let(CardVehicleHistory::build).orEmpty()
            status = if (history.isEmpty()) "Noch keine Fahrzeugdaten gespeichert." else
                "${history.size} Fahrzeuge aus gespeicherten Karten-Snapshots."
        } catch (_: Exception) {
            history = emptyList()
            status = "Gespeicherte Fahrzeugdaten konnten nicht ausgewertet werden."
        }
    }
    ScreenContent(padding) {
        Text("Fahrzeughistorie · Vorschau", style = MaterialTheme.typography.titleLarge)
        Text("Quelle: ausdrücklich gespeicherte Fahrerkarten-Dateien. Gleiche Nutzungen aus mehreren Snapshots werden nur einmal gezeigt. Kennzeichen ohne gültige VIN werden nach Staat und Kennzeichen gruppiert.",
            style = MaterialTheme.typography.bodyMedium)
        Text(status, style = MaterialTheme.typography.bodyMedium)
        history.forEach { vehicle ->
            var expanded by remember(vehicle.vin, vehicle.registrationNation, vehicle.registrationNumber) {
                mutableStateOf(false)
            }
            SectionCard {
                Text("${vehicle.registrationNumber.ifBlank { "Kennzeichen unbekannt" }} · Staat ${vehicle.registrationNation}",
                    style = MaterialTheme.typography.titleMedium)
                vehicle.vin?.let { Text("VIN: $it", style = MaterialTheme.typography.bodyMedium) }
                Text("Erste Nutzung: ${vehicle.firstUseUtc.atZone(zone).format(formatter)}")
                Text("Letzte belegte Zeit: ${vehicle.lastObservedUtc.atZone(zone).format(formatter)}")
                Text("${vehicle.uses.size} verschiedene Nutzungen")
                OutlinedButton(onClick = { expanded = !expanded }) {
                    Text(if (expanded) "Nutzungen ausblenden" else "Letzte Nutzungen anzeigen")
                }
                if (expanded) Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    vehicle.uses.take(10).forEach { use ->
                        val start = use.firstUseUtc.atZone(zone).format(formatter)
                        val end = use.lastUseUtc?.atZone(zone)?.format(formatter) ?: "Ende offen"
                        Text("$start – $end · ${use.registrationNumber}")
                        Text("Tacho ${use.odometerBeginKm} → ${use.odometerEndKm?.toString() ?: "offen"} km",
                            style = MaterialTheme.typography.bodySmall)
                    }
                    if (vehicle.uses.size > 10)
                        Text("Die letzten 10 von ${vehicle.uses.size} Nutzungen werden gezeigt.")
                }
            }
        }
        if (history.isNotEmpty())
            Text("Kilometerstände sind Quellwerte je Nutzung. Eine Gesamtstrecke, Arbeitstage und fahrzeugbezogene Arbeitszeit sind noch nicht berechnet.",
                style = MaterialTheme.typography.bodySmall)
    }
}
