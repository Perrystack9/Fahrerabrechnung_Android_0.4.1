package de.fahrerabrechnung.app

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import de.fahrerabrechnung.core.design.SectionCard
import de.fahrerabrechnung.data.card.*
import de.fahrerabrechnung.feature.common.ScreenContent
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/** Original tachograph-unit records from the exact selected own-card session. */
@Composable
internal fun CardVehicleUnitsScreen(padding: PaddingValues, source: SavedCardVehicleUnits?) {
    val zone = remember { ZoneId.systemDefault() }
    val format = remember { DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss XXX") }
    var visibleCount by remember(source?.id) { mutableStateOf(20) }
    ScreenContent(padding) {
        Text("Fahrtenschreiber-Nutzungen", style = MaterialTheme.typography.titleLarge)
        Text("Aufgezeichnete Fahrtenschreiber-Einträge der ausgewählten Fahrerkarte.")
        when {
            source == null -> Text("Für diese Übernahme ist keine gespeicherte Fahrtenschreiber-Datei verfügbar.")
            source.records.isEmpty() -> Text("In dieser Fahrtenschreiber-Datei sind keine Nutzungen aufgezeichnet.")
            else -> source.records.take(visibleCount).forEach { record -> SectionCard {
                Text("Aufgezeichneter Fahrtenschreiber", style = MaterialTheme.typography.titleMedium)
                Text(record.useBeginUtc?.atZone(zone)?.format(format)?.let { "Nutzungsbeginn: $it" }
                    ?: "Nutzungsbeginn: Zeitpunkt nicht angegeben")
                Text("Herstellercode des Fahrtenschreibers: Kartenwert ${record.manufacturerCodeRaw}")
                Text("Geräte-Typcode: Kartenwert ${record.deviceIdRaw}")
                Text(record.softwareVersionText?.let { "Softwareversion: $it" }
                    ?: "Softwareversion: nicht lesbar (Rohwert ${record.softwareVersionHex})")
            } }
        }
        if (source != null && visibleCount < source.records.size)
            OutlinedButton(onClick = { visibleCount += 20 }) { Text("Weitere Einträge anzeigen") }
        Text("Ortszeit (${zone.id}).", style = MaterialTheme.typography.bodySmall)
    }
}
