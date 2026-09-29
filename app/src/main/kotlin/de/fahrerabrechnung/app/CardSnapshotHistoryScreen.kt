package de.fahrerabrechnung.app

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import de.fahrerabrechnung.core.design.SectionCard
import de.fahrerabrechnung.data.card.CardActivityImportRepository
import de.fahrerabrechnung.data.card.CardVehicleImportRepository
import de.fahrerabrechnung.data.card.SavedCardActivity
import de.fahrerabrechnung.data.card.SavedCardVehicles
import de.fahrerabrechnung.feature.common.ScreenContent
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/** Read-only list of explicitly saved partial source files, not a completed import log. */
@Composable
internal fun CardSnapshotHistoryScreen(padding: PaddingValues,
                                       activities: CardActivityImportRepository?,
                                       vehicles: CardVehicleImportRepository?) {
    var snapshots by remember { mutableStateOf<List<SavedCardActivity>>(emptyList()) }
    var status by remember { mutableStateOf("Lade gespeicherte Aktivitätsdateien …") }
    var vehicleSnapshots by remember { mutableStateOf<List<SavedCardVehicles>>(emptyList()) }
    var vehicleStatus by remember { mutableStateOf("Lade gespeicherte Fahrzeugdateien …") }
    val zone = remember { ZoneId.systemDefault() }
    val formatter = remember { DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm") }
    LaunchedEffect(activities) {
        try {
            snapshots = activities?.all().orEmpty()
            status = if (snapshots.isEmpty()) "Noch keine Aktivitätsdatei gespeichert."
                else "${snapshots.size} ausdrücklich gespeicherte Aktivitäts-Snapshots."
        } catch (_: Exception) {
            snapshots = emptyList()
            status = "Gespeicherte Aktivitätsdaten konnten nicht geöffnet werden."
        }
    }
    LaunchedEffect(vehicles) {
        try {
            vehicleSnapshots = vehicles?.all().orEmpty().sortedByDescending { it.importedAtEpochMillis }
            vehicleStatus = if (vehicleSnapshots.isEmpty()) "Noch keine Fahrzeugdatei gespeichert."
                else "${vehicleSnapshots.size} ausdrücklich gespeicherte Fahrzeug-Snapshots."
        } catch (_: Exception) {
            vehicleSnapshots = emptyList()
            vehicleStatus = "Gespeicherte Fahrzeugdaten konnten nicht geöffnet werden."
        }
    }
    ScreenContent(padding) {
        Text("Gespeicherte Teilimporte · Vorschau", style = MaterialTheme.typography.titleLarge)
        Text("Hier stehen nur ausdrücklich gespeicherte Fahrerkarten-Dateien. Der Lesetest allein speichert nichts; ein vollständiger Kartenimport ist noch nicht vorhanden.",
            style = MaterialTheme.typography.bodyMedium)
        Text("Aktivitätsdateien", style = MaterialTheme.typography.titleMedium)
        Text(status)
        snapshots.forEach { saved ->
            SectionCard {
                Text("Aktivitätsdatei · ${saved.generation.name}",
                    style = MaterialTheme.typography.titleMedium)
                Text("Gespeichert: ${Instant.ofEpochMilli(saved.importedAtEpochMillis).atZone(zone).format(formatter)}")
                Text("${saved.dayCount} Tagesdatensätze")
                Text("Fahrerkarte: ${maskCardNumber(saved.cardNumber)}",
                    style = MaterialTheme.typography.bodySmall)
            }
        }
        Text("Fahrzeugdateien", style = MaterialTheme.typography.titleMedium)
        Text(vehicleStatus)
        vehicleSnapshots.forEach { saved ->
            SectionCard {
                Text("Fahrzeugdatei · ${saved.file.generation.name}",
                    style = MaterialTheme.typography.titleMedium)
                Text("Gespeichert: ${Instant.ofEpochMilli(saved.importedAtEpochMillis).atZone(zone).format(formatter)}")
                Text("${saved.uses.size} Nutzungsdatensätze")
                Text("Fahrerkarte: ${maskCardNumber(saved.cardNumber)}",
                    style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

internal fun maskCardNumber(number: String): String =
    "••••${number.takeLast(4)}"
