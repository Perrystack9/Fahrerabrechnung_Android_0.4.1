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
import java.util.Locale

/** Read-only original records from the exact selected own-card session. */
@Composable
internal fun CardLoadUnloadScreen(padding: PaddingValues, source: SavedCardLoadUnload?) {
    val zone = remember { ZoneId.systemDefault() }
    val format = remember { DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss XXX") }
    var visibleCount by remember(source?.id) { mutableStateOf(20) }
    ScreenContent(padding) {
        Text("Be- und Entladeeinträge", style = MaterialTheme.typography.titleLarge)
        Text("Aufgezeichnete Vorgänge der ausgewählten Fahrerkarte.")
        when {
            source == null -> Text("Für diese Übernahme ist keine gespeicherte Be-/Entladedatei verfügbar.")
            source.records.isEmpty() -> Text("In dieser Be-/Entladedatei sind keine Vorgänge aufgezeichnet.")
            else -> source.records.take(visibleCount).forEach { record -> SectionCard {
                Text("Aufgezeichneter Vorgang", style = MaterialTheme.typography.titleMedium)
                Text(when (record.operation) {
                    CardRecordedLoadUnloadOperation.LOAD -> "Art: Beladen"
                    CardRecordedLoadUnloadOperation.UNLOAD -> "Art: Entladen"
                    CardRecordedLoadUnloadOperation.SIMULTANEOUS_LOAD_UNLOAD -> "Art: gleichzeitiges Be- und Entladen"
                    null -> "Art: unbekannt (Kartenwert ${record.operationTypeRaw})"
                })
                Text(record.operationTimeUtc?.atZone(zone)?.format(format)?.let { "Beginn des Vorgangs: $it" }
                    ?: "Beginn des Vorgangs: nicht angegeben")
                Text(record.positionTimeUtc?.atZone(zone)?.format(format)?.let { "Positionszeit: $it" }
                    ?: "Positionszeit: nicht angegeben")
                val point = record.position
                Text(if (point == null) "Position: nicht verfügbar" else
                    String.format(Locale.ROOT, "Breite %.6f · Länge %.6f", point.latitude, point.longitude))
                Text(record.accuracyHdop?.let { "HDOP: $it" } ?: "HDOP: nicht angegeben")
                Text(when (record.recordedStatus) {
                    CardRecordedGnssAuthenticationStatus.AUTHENTICATED -> "Fahrtenschreiberstatus: authentisiert"
                    CardRecordedGnssAuthenticationStatus.NOT_AUTHENTICATED -> "Fahrtenschreiberstatus: nicht authentisiert"
                    null -> "Fahrtenschreiberstatus: unbekannt (${record.authenticationStatusRaw})"
                })
                Text(record.odometerKm?.let { "Kilometerstand (Kartenwert): $it km" } ?: "Kilometerstand: nicht angegeben")
            } }
        }
        if (source != null && visibleCount < source.records.size)
            OutlinedButton(onClick = { visibleCount += 20 }) { Text("Weitere Vorgänge anzeigen") }
        Text("Beginn und Positionszeit sind die getrennten Zeitangaben der Karte.", style = MaterialTheme.typography.bodySmall)
        Text("Ortszeit (${zone.id}).", style = MaterialTheme.typography.bodySmall)
    }
}
