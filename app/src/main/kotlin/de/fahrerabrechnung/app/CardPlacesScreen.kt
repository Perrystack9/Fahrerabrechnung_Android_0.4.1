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

/** Exact selected source; the separate status file is not joined to place records. */
@Composable
internal fun CardPlacesScreen(padding: PaddingValues, places: SavedCardPlaces?,
                              authentication: SavedCardPlacesAuthentication?) {
    val zone = remember { ZoneId.systemDefault() }
    val format = remember { DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss XXX") }
    ScreenContent(padding) {
        Text("Beginn-/Ende-Orte", style = MaterialTheme.typography.titleLarge)
        Text("Orts- und Ländereinträge der täglichen Arbeitsperiode aus der ausgewählten Übernahme.")
        when {
            places == null -> Text("Für diese Übernahme ist keine gespeicherte Ortsdatei verfügbar.")
            places.records.isEmpty() -> Text("In dieser Ortsdatei sind keine Aufzeichnungen enthalten.")
            else -> places.records.forEach { record -> SectionCard {
                Text(when (record.entry) {
                    CardWorkPeriodEntry.BEGIN -> "Beginn der täglichen Arbeitsperiode"
                    CardWorkPeriodEntry.END -> "Ende der täglichen Arbeitsperiode"
                    null -> "Eintragsart: unbekannt (${record.entryTypeRaw})"
                }, style = MaterialTheme.typography.titleMedium)
                Text(record.entryTimeUtc?.atZone(zone)?.format(format) ?: "Eintragszeit: nicht angegeben")
                Text(if (record.countryRaw in setOf(0, 255)) "Land: nicht angegeben" else "Land: Kartenwert ${record.countryRaw}")
                Text(if (record.regionRaw in setOf(0, 255)) "Region: nicht angegeben" else "Region: Kartenwert ${record.regionRaw}")
                Text(record.odometerKm?.let { "Kilometerstand: $it km" } ?: "Kilometerstand: nicht angegeben")
                if (record.generation != CardGeneration.GEN1) {
                    Text(record.positionTimeUtc?.atZone(zone)?.format(format)?.let { "Positionszeit: $it" }
                        ?: "Positionszeit: nicht angegeben")
                    val point = record.position
                    Text(if (point == null) "Position: nicht verfügbar" else
                        String.format(Locale.ROOT, "Breite %.6f · Länge %.6f", point.latitude, point.longitude))
                    Text(record.accuracyHdop?.let { "HDOP: $it" } ?: "HDOP: nicht angegeben")
                }
            } }
        }
        Text("Aufgezeichneter Authentisierungsstatus", style = MaterialTheme.typography.titleMedium)
        when {
            authentication == null -> Text("Für diese Übernahme ist keine gespeicherte Orts-Authentisierungsdatei verfügbar.")
            authentication.records.isEmpty() -> Text("In dieser Orts-Authentisierungsdatei sind keine Aufzeichnungen enthalten.")
            else -> authentication.records.forEach { record -> SectionCard {
                Text(record.entryTimeUtc?.atZone(zone)?.format(format) ?: "Aufzeichnungszeit: nicht angegeben")
                Text(when (record.recordedStatus) {
                    CardRecordedGnssAuthenticationStatus.AUTHENTICATED -> "Fahrtenschreiberstatus: authentisiert"
                    CardRecordedGnssAuthenticationStatus.NOT_AUTHENTICATED -> "Fahrtenschreiberstatus: nicht authentisiert"
                    null -> "Fahrtenschreiberstatus: unbekannt (${record.authenticationStatusRaw})"
                })
            } }
        }
        Text("Die Statusdatei bleibt separat. Aus den Einträgen werden keine Arbeitszeit, Heimkehr oder Fahrstrecke berechnet.",
            style = MaterialTheme.typography.bodySmall)
        Text("Ortszeit (${zone.id}).", style = MaterialTheme.typography.bodySmall)
    }
}
