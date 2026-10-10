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

/** Only the exact selected session's complete source; insertion entries stay neutral. */
@Composable
internal fun CardBorderCrossingsScreen(padding: PaddingValues, source: SavedCardBorderCrossings?) {
    val zone = remember { ZoneId.systemDefault() }
    val format = remember { DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss XXX") }
    var visibleCount by remember(source?.id) { mutableStateOf(20) }
    ScreenContent(padding) {
        Text("Grenz- und Ländereinträge", style = MaterialTheme.typography.titleLarge)
        Text("Aufzeichnungen der Fahrerkarte. Diese Datei enthält auch Ländereinträge beim Einlegen der Karte.")
        when {
            source == null -> Text("Für diese Übernahme ist keine gespeicherte Grenz-/Länderdatei verfügbar.")
            source.records.isEmpty() -> Text("In dieser Grenz-/Länderdatei sind keine Aufzeichnungen enthalten.")
            else -> source.records.take(visibleCount).forEach { record -> SectionCard {
                Text("Ländereintrag", style = MaterialTheme.typography.titleMedium)
                Text("Land verlassen: ${borderCountry(record.countryLeftRaw)}")
                Text("Land eingereist / bei Karteneinlage: ${borderCountry(record.countryEnteredRaw)}")
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
            OutlinedButton(onClick = { visibleCount += 20 }) { Text("Weitere Ländereinträge anzeigen") }
        Text("Aus diesen Einträgen werden keine Fahrstrecke, Kilometerdifferenz oder Spesen berechnet.",
            style = MaterialTheme.typography.bodySmall)
        Text("Ortszeit (${zone.id}).", style = MaterialTheme.typography.bodySmall)
    }
}

private fun borderCountry(raw: Int): String = when (raw) {
    0 -> "nicht angegeben"
    255 -> "Rest der Welt (Kartenwert 255)"
    else -> "Kartenwert $raw"
}
