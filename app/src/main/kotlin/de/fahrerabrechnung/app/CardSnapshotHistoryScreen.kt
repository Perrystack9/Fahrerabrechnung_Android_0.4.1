package de.fahrerabrechnung.app

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
import de.fahrerabrechnung.core.design.SectionCard
import de.fahrerabrechnung.data.card.CardActivityImportRepository
import de.fahrerabrechnung.data.card.CardSourcePairRepository
import de.fahrerabrechnung.data.card.SavedCardSourcePair
import de.fahrerabrechnung.data.card.CardVehicleImportRepository
import de.fahrerabrechnung.data.card.SavedCardActivity
import de.fahrerabrechnung.data.card.SavedCardVehicles
import de.fahrerabrechnung.data.card.DriverCardImportRepository
import de.fahrerabrechnung.data.card.SavedDriverImport
import de.fahrerabrechnung.feature.common.ScreenContent
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/** Read-only list of explicitly saved partial source files, not a completed import log. */
@Composable
internal fun CardSnapshotHistoryScreen(padding: PaddingValues,
                                       activities: CardActivityImportRepository?,
                                       vehicles: CardVehicleImportRepository?,
                                       pairs: CardSourcePairRepository? = null,
                                       onPreviewPair: (SavedCardSourcePair) -> Unit = {},
                                       onPreviewActivity: (SavedCardActivity) -> Unit = {},
                                       onPreviewVehicle: (SavedCardVehicles) -> Unit = {},
                                       onPreviewVehicleDay: (SavedCardVehicles) -> Unit = {},
                                       driverImports: DriverCardImportRepository? = null) {
    var ownImports by remember { mutableStateOf<List<SavedDriverImport>>(emptyList()) }
    var ownStatus by remember { mutableStateOf("Lade Fahrerprofil-Teilimporte …") }
    var sourcePairs by remember { mutableStateOf<List<SavedCardSourcePair>>(emptyList()) }
    var pairStatus by remember { mutableStateOf("Lade gemeinsam gespeicherte Dateien …") }
    var snapshots by remember { mutableStateOf<List<SavedCardActivity>>(emptyList()) }
    var status by remember { mutableStateOf("Lade gespeicherte Aktivitätsdateien …") }
    var vehicleSnapshots by remember { mutableStateOf<List<SavedCardVehicles>>(emptyList()) }
    var vehicleStatus by remember { mutableStateOf("Lade gespeicherte Fahrzeugdateien …") }
    val zone = remember { ZoneId.systemDefault() }
    val formatter = remember { DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm") }
    LaunchedEffect(driverImports) {
        try {
            ownImports = driverImports?.all().orEmpty()
            ownStatus = if (ownImports.isEmpty()) "Noch keine Teilimporte im Fahrerprofil gespeichert."
                else "${ownImports.size} ausdrücklich gespeicherte Teilimport-Sitzungen."
        } catch (_: Exception) { ownStatus = "Fahrerprofil-Teilimporte konnten nicht geladen werden." }
    }
    LaunchedEffect(pairs) {
        try {
            sourcePairs = pairs?.all().orEmpty()
            pairStatus = if (sourcePairs.isEmpty()) "Noch keine Dateien gemeinsam gespeichert."
                else "${sourcePairs.size} gemeinsam gespeicherte Dateipaare."
        } catch (_: Exception) {
            sourcePairs = emptyList()
            pairStatus = "Gemeinsam gespeicherte Dateien konnten nicht geöffnet werden."
        }
    }
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
        if (driverImports != null) {
            Text("Teilimporte im Fahrerprofil", style = MaterialTheme.typography.titleMedium)
            Text(ownStatus)
            ownImports.forEach { saved ->
                SectionCard {
                    Text("Teilimport · ${Instant.ofEpochMilli(saved.savedAtEpochMillis).atZone(zone).format(formatter)}")
                    Text("Neue Quelldateien: ${saved.newSourceCount}; kein vollständiger Kartenimport.")
                    saved.activity?.let { Text("${it.dayCount} Tagesdatensätze · Fahrerkarte: ${maskCardNumber(it.cardNumber)}") }
                    saved.vehicles?.let { Text("${it.uses.size} Fahrzeugnutzungen · Fahrerkarte: ${maskCardNumber(it.cardNumber)}") }
                    val pair = saved.pair
                    val activity = saved.activity
                    if (pair != null) OutlinedButton(onClick = { onPreviewPair(pair) }) { Text("Teilimport ansehen") }
                    else if (activity != null) OutlinedButton(onClick = { onPreviewActivity(activity) }) { Text("Kartentage des Teilimports ansehen") }
                    else saved.vehicles?.let { vehicles -> OutlinedButton(onClick = { onPreviewVehicle(vehicles) }) { Text("Fahrzeuge des Teilimports ansehen") } }
                }
            }
            Text("Quellarchiv und frühere Teilimporte", style = MaterialTheme.typography.titleMedium)
            Text("Die Auswahl einer älteren Datei erzeugt keine Profilzuordnung. Vorhandene Quellen bleiben unverändert erhalten.")
        }
        Text("Gemeinsam gespeicherte Dateien", style = MaterialTheme.typography.titleMedium)
        Text(pairStatus)
        Text("Nur ausdrücklich gemeinsam gespeicherte Dateien aus demselben Lesevorgang stehen hier. Ältere einzelne Dateien werden nicht nachträglich zugeordnet.",
            style = MaterialTheme.typography.bodySmall)
        sourcePairs.forEach { saved ->
            SectionCard {
                Text("Aktivität + Fahrzeuge · ${saved.activity.generation.name}",
                    style = MaterialTheme.typography.titleMedium)
                Text("Gemeinsam gespeichert: ${Instant.ofEpochMilli(saved.savedAtEpochMillis).atZone(zone).format(formatter)}")
                Text("${saved.activity.dayCount} Tagesdatensätze · ${saved.vehicles.uses.size} Fahrzeugnutzungen")
                Text("Fahrerkarte: ${maskCardNumber(saved.activity.cardNumber)}")
                OutlinedButton(onClick = { onPreviewPair(saved) }) { Text("Gemeinsame Vorschau öffnen") }
            }
        }
        Text("Einzeldateien bleiben auch unten verfügbar; identische Dateien werden nur einmal gespeichert.",
            style = MaterialTheme.typography.bodySmall)
        Text("Aktivitätsdateien", style = MaterialTheme.typography.titleMedium)
        Text(status)
        snapshots.forEach { saved ->
            SectionCard {
                Text("Aktivitätsdatei · ${saved.generation.name}",
                    style = MaterialTheme.typography.titleMedium)
                Text("Gespeichert: ${Instant.ofEpochMilli(saved.importedAtEpochMillis).atZone(zone).format(formatter)}")
                Text("${saved.dayCount} Tagesdatensätze")
                val recordDates = remember(saved) { saved.days.map { it.recordDateUtc } }
                if (recordDates.isNotEmpty()) {
                    Text("Kartentage: ${recordDates.min().atZone(zone).toLocalDate()} – ${recordDates.max().atZone(zone).toLocalDate()}")
                }
                Text("Fahrerkarte: ${maskCardNumber(saved.cardNumber)}",
                    style = MaterialTheme.typography.bodySmall)
                OutlinedButton(onClick = { onPreviewActivity(saved) }) {
                    Text("Kartentage als Vorschau öffnen")
                }
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
                if (saved.uses.isNotEmpty()) {
                    val first = saved.uses.minOf { it.firstUseUtc }.atZone(zone).format(formatter)
                    val last = saved.uses.maxOf { it.lastUseUtc ?: it.firstUseUtc }.atZone(zone).format(formatter)
                    Text("Belegte Fahrzeugnutzungen: $first – $last")
                    if (saved.uses.any { it.lastUseUtc == null })
                        Text("Mindestens ein Nutzungsende ist in der Quelle offen.",
                            style = MaterialTheme.typography.bodySmall)
                }
                Text("Fahrerkarte: ${maskCardNumber(saved.cardNumber)}",
                    style = MaterialTheme.typography.bodySmall)
                OutlinedButton(onClick = { onPreviewVehicle(saved) }) {
                    Text("Fahrzeuge dieser Datei ansehen")
                }
                OutlinedButton(onClick = { onPreviewVehicleDay(saved) }) {
                    Text("Nutzungen im Kartentag ansehen")
                }
            }
        }
    }
}

internal fun maskCardNumber(number: String): String =
    if (number.length > 4) "••••${number.takeLast(4)}" else "••••"
