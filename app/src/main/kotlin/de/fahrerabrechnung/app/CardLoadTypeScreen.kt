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

/** Original discrete load-type entries from the exact selected own-card session. */
@Composable
internal fun CardLoadTypeScreen(padding: PaddingValues, source: SavedCardLoadType?) {
    val zone = remember { ZoneId.systemDefault() }
    val format = remember { DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss XXX") }
    var visibleCount by remember(source?.id) { mutableStateOf(20) }
    ScreenContent(padding) {
        Text("Ladungsart-Einträge", style = MaterialTheme.typography.titleLarge)
        Text("Aufgezeichnete Einträge der ausgewählten Fahrerkarte.")
        when {
            source == null -> Text("Für diese Übernahme ist keine gespeicherte Ladungsart-Datei verfügbar.")
            source.records.isEmpty() -> Text("In dieser Ladungsart-Datei sind keine Einträge aufgezeichnet.")
            else -> source.records.take(visibleCount).forEach { record -> SectionCard {
                Text("Aufgezeichneter Eintrag", style = MaterialTheme.typography.titleMedium)
                Text(when (record.loadType) {
                    CardRecordedLoadType.UNDEFINED -> "Art: nicht festgelegt"
                    CardRecordedLoadType.GOODS -> "Art: Güter"
                    CardRecordedLoadType.PASSENGERS -> "Art: Fahrgäste"
                    null -> "Art: unbekannt (Kartenwert ${record.loadTypeRaw})"
                })
                Text(record.entryTimeUtc?.atZone(zone)?.format(format)?.let { "Eingetragen: $it" }
                    ?: "Eingetragen: Zeitpunkt nicht angegeben")
            } }
        }
        if (source != null && visibleCount < source.records.size)
            OutlinedButton(onClick = { visibleCount += 20 }) { Text("Weitere Einträge anzeigen") }
        Text("Ortszeit (${zone.id}).", style = MaterialTheme.typography.bodySmall)
    }
}
