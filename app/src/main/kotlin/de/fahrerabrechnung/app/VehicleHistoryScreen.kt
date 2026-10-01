package de.fahrerabrechnung.app

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import de.fahrerabrechnung.data.card.DriverCardImportRepository
import de.fahrerabrechnung.data.card.CardVehicleImportRepository
import de.fahrerabrechnung.data.card.SavedCardVehicles
import de.fahrerabrechnung.feature.common.ScreenContent
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/** Read-only history from explicitly saved card snapshots. */
@Composable
internal fun VehicleHistoryScreen(padding: PaddingValues, imports: CardVehicleImportRepository?,
                                  initialQuery: String = "", selectedUseStart: Long? = null,
                                  selectedNation: Int? = null,
                                  previewSnapshot: SavedCardVehicles? = null,
                                  driverImports: DriverCardImportRepository? = null) {
    var history by remember { mutableStateOf<List<CardVehicleHistoryEntry>>(emptyList()) }
    var status by remember { mutableStateOf("Lade gespeicherte Fahrzeugdaten …") }
    var query by remember(initialQuery) { mutableStateOf(initialQuery) }
    var selectedStart by remember(selectedUseStart) { mutableStateOf(selectedUseStart) }
    val zone = remember { ZoneId.systemDefault() }
    val formatter = remember { DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm") }
    LaunchedEffect(imports, previewSnapshot, driverImports) {
        try {
            val bound = driverImports?.profile() != null
            val sources = previewSnapshot?.let { listOf(it) } ?: if (bound) driverImports?.vehicleSources() else imports?.all()
            history = sources?.let(CardVehicleHistory::build).orEmpty()
            status = if (history.isEmpty()) "Noch keine Fahrzeugdaten gespeichert." else if (previewSnapshot != null)
                "${history.size} Fahrzeuge aus dieser gespeicherten Datei."
            else "${history.size} Fahrzeuge aus gespeicherten Karten-Snapshots."
        } catch (_: Exception) {
            history = emptyList()
            status = "Gespeicherte Fahrzeugdaten konnten nicht ausgewertet werden."
        }
    }
    ScreenContent(padding) {
        Text("Fahrzeughistorie · Vorschau", style = MaterialTheme.typography.titleLarge)
        Text(if (previewSnapshot != null)
            "Quelle: nur die ausgewählte, ausdrücklich gespeicherte Fahrzeugdatei. Kennzeichen ohne gültige VIN werden nach Staat und Kennzeichen gruppiert."
            else "Quelle: ausdrücklich gespeicherte Fahrerkarten-Dateien. Gleiche Nutzungen aus mehreren Snapshots werden nur einmal gezeigt. Kennzeichen ohne gültige VIN werden nach Staat und Kennzeichen gruppiert.",
            style = MaterialTheme.typography.bodyMedium)
        Text(status, style = MaterialTheme.typography.bodyMedium)
        if (history.isNotEmpty()) OutlinedTextField(
            value = query,
            onValueChange = { query = it; selectedStart = null },
            label = { Text("Kennzeichen oder VIN suchen") },
            singleLine = true
        )
        val filtered = history.filter { vehicle ->
            val search = query.trim()
            search.isEmpty() || vehicle.registrationNumber.contains(search, ignoreCase = true) ||
                vehicle.vin?.contains(search, ignoreCase = true) == true
        }
        if (selectedStart != null && history.isNotEmpty()) {
            val selectedUse = filtered.asSequence().flatMap { it.uses.asSequence() }
                .firstOrNull { it.firstUseUtc.epochSecond == selectedStart &&
                    it.registrationNation == selectedNation &&
                    (it.registrationNumber.equals(initialQuery, ignoreCase = true) ||
                        it.vin?.equals(initialQuery, ignoreCase = true) == true) }
            if (selectedUse != null) SectionCard {
                Text("Ausgewählte Kartennutzung", style = MaterialTheme.typography.titleMedium)
                Text("${selectedUse.registrationNumber.ifBlank { "Kennzeichen unbekannt" }} · Staat ${selectedUse.registrationNation}")
                val first = selectedUse.firstUseUtc.atZone(zone).format(formatter)
                val last = selectedUse.lastUseUtc?.atZone(zone)?.format(formatter) ?: "Ende offen"
                Text("$first – $last")
                Text("Tacho ${selectedUse.odometerBeginKm} → ${selectedUse.odometerEndKm?.toString() ?: "offen"} km")
                Text("Quellwerte der Fahrerkarte; keine berechnete Strecke.",
                    style = MaterialTheme.typography.bodySmall)
            } else Text("Die ausgewählte Nutzung ist in dieser gespeicherten Historie nicht vorhanden.",
                style = MaterialTheme.typography.bodySmall)
        }
        if (history.isNotEmpty() && filtered.isEmpty())
            Text("Kein Fahrzeug für diese Suche.", style = MaterialTheme.typography.bodyMedium)
        filtered.forEach { vehicle ->
            var expanded by remember(vehicle.vin, vehicle.registrationNation, vehicle.registrationNumber) {
                mutableStateOf(false)
            }
            var visibleUses by remember(vehicle.vin, vehicle.registrationNation, vehicle.registrationNumber) {
                mutableStateOf(10)
            }
            SectionCard {
                Text("${vehicle.registrationNumber.ifBlank { "Kennzeichen unbekannt" }} · Staat ${vehicle.registrationNation}",
                    style = MaterialTheme.typography.titleMedium)
                vehicle.vin?.let { Text("VIN: $it", style = MaterialTheme.typography.bodyMedium) }
                Text("Erste Nutzung: ${vehicle.firstUseUtc.atZone(zone).format(formatter)}")
                Text("Letzte belegte Zeit: ${vehicle.lastObservedUtc.atZone(zone).format(formatter)}")
                Text("${vehicle.uses.size} verschiedene Nutzungen")
                OutlinedButton(onClick = {
                    expanded = !expanded
                    if (!expanded) visibleUses = 10
                }) {
                    Text(if (expanded) "Nutzungen ausblenden" else "Letzte Nutzungen anzeigen")
                }
                if (expanded) Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    vehicle.uses.take(visibleUses).forEach { use ->
                        val start = use.firstUseUtc.atZone(zone).format(formatter)
                        val end = use.lastUseUtc?.atZone(zone)?.format(formatter) ?: "Ende offen"
                        Text("$start – $end · ${use.registrationNumber}")
                        Text("Tacho ${use.odometerBeginKm} → ${use.odometerEndKm?.toString() ?: "offen"} km",
                            style = MaterialTheme.typography.bodySmall)
                    }
                    if (vehicle.uses.size > visibleUses) OutlinedButton(
                        onClick = { visibleUses = (visibleUses + 10).coerceAtMost(vehicle.uses.size) }
                    ) {
                        Text("Weitere Nutzungen anzeigen (${visibleUses} von ${vehicle.uses.size})")
                    }
                }
            }
        }
        if (history.isNotEmpty())
            Text("Kilometerstände sind Quellwerte je Nutzung. Eine Gesamtstrecke, Arbeitstage und fahrzeugbezogene Arbeitszeit sind noch nicht berechnet.",
                style = MaterialTheme.typography.bodySmall)
    }
}
