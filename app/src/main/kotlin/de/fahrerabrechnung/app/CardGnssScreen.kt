package de.fahrerabrechnung.app

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import de.fahrerabrechnung.core.design.SectionCard
import de.fahrerabrechnung.data.card.*
import de.fahrerabrechnung.feature.common.ScreenContent
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/** Only the exact selected session; authentication records are displayed independently. */
@Composable
internal fun CardGnssScreen(padding: PaddingValues, positions: SavedCardGnssPositions?,
                            authentication: SavedCardGnssAuthentication?) {
    val zone = remember { ZoneId.systemDefault() }
    val format = remember { DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss XXX") }
    ScreenContent(padding) {
        Text("GNSS-Aufzeichnungen", style = MaterialTheme.typography.titleLarge)
        Text("Einzelpunkte des Fahrtenschreibers aus der ausgewählten Übernahme. Keine durchgehende Strecke.")
        Text("GNSS-Punkte", style = MaterialTheme.typography.titleMedium)
        when {
            positions == null -> Text("Für diese Übernahme ist keine gespeicherte GNSS-Punktdatei verfügbar.")
            positions.records.isEmpty() -> Text("In dieser GNSS-Punktdatei sind keine Aufzeichnungen enthalten.")
            else -> positions.records.forEach { record -> SectionCard {
                Text(record.positionTimeUtc?.atZone(zone)?.format(format) ?: "Positionszeit: nicht angegeben")
                val point = record.position
                Text(if (point == null) "Position: nicht verfügbar" else
                    String.format(Locale.ROOT, "Breite %.6f · Länge %.6f", point.latitude, point.longitude))
                Text(record.accuracyHdop?.let { "HDOP: $it" } ?: "HDOP: nicht angegeben")
            } }
        }
        Text("Aufgezeichneter Authentisierungsstatus", style = MaterialTheme.typography.titleMedium)
        when {
            authentication == null -> Text("Für diese Übernahme ist keine gespeicherte GNSS-Authentisierungsdatei verfügbar.")
            authentication.records.isEmpty() -> Text("In dieser GNSS-Authentisierungsdatei sind keine Aufzeichnungen enthalten.")
            else -> authentication.records.forEach { record -> SectionCard {
                Text(record.accumulatedDrivingTimeUtc?.atZone(zone)?.format(format) ?: "Aufzeichnungszeit: nicht angegeben")
                Text(when (record.recordedStatus) {
                    CardRecordedGnssAuthenticationStatus.AUTHENTICATED -> "Fahrtenschreiberstatus: authentisiert"
                    CardRecordedGnssAuthenticationStatus.NOT_AUTHENTICATED -> "Fahrtenschreiberstatus: nicht authentisiert"
                    null -> "Fahrtenschreiberstatus: unbekannt (${record.authenticationStatusRaw})"
                })
            } }
        }
        Text("Die Statusdatei wird separat angezeigt. Die App ordnet sie keinem Einzelpunkt zu und führt keine eigene kryptografische Prüfung durch.",
            style = MaterialTheme.typography.bodySmall)
        Text("Ortszeit (${zone.id}).", style = MaterialTheme.typography.bodySmall)
    }
}
