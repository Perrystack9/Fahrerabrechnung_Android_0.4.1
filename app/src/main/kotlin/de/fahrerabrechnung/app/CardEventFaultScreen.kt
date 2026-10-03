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

/** Recorded technical facts from the exact selected session; no violation or duration calculation. */
@Composable
internal fun CardEventFaultScreen(padding: PaddingValues, events: SavedCardEventFault?, faults: SavedCardEventFault?) {
    val zone = remember { ZoneId.systemDefault() }
    val format = remember { DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss") }
    var visibleEvents by remember(events?.id) { mutableIntStateOf(20) }
    var visibleFaults by remember(faults?.id) { mutableIntStateOf(20) }
    ScreenContent(padding) {
        Text("Ereignisse und Störungen", style = MaterialTheme.typography.titleLarge)
        Text("Aufzeichnungen des Fahrtenschreibers aus der ausgewählten Übernahme.")
        for ((label, source, visible) in listOf(Triple("Ereignisse", events, visibleEvents), Triple("Störungen", faults, visibleFaults))) {
            Text(label, style = MaterialTheme.typography.titleMedium)
            if (source == null) Text("Für diese Übernahme sind keine gespeicherten ${if (label == "Ereignisse") "Ereignisdaten" else "Störungsdaten"} verfügbar.")
            else if (source.records.isEmpty()) Text("In dieser Kartendatei sind keine ${label} aufgezeichnet.")
            else {
                // Slot order is evidence, not a claimed chronology. Unknown/inverted times are not repaired.
                source.records.take(visible).forEach { record ->
                    SectionCard {
                        Text(eventFaultLabel(source.file.generation, record.type), style = MaterialTheme.typography.titleMedium)
                        Text("Beginn: ${record.beginUtc?.atZone(zone)?.format(format) ?: "nicht angegeben"}")
                        Text("Ende: ${record.endUtc?.atZone(zone)?.format(format) ?: "nicht angegeben"}")
                        record.registrationNumber?.let { Text("Fahrzeug: $it") }
                        record.registrationNation?.let { Text("Zulassungsstaat: $it") }
                        if (source.file.kind == CardEventFaultKind.EVENTS && record.type == 0x03)
                            Text("Die Zeitpunkte beziehen sich auf Kartenentnahme im vorherigen Fahrzeug und Einlegen im aktuellen Fahrzeug.",
                                style = MaterialTheme.typography.bodySmall)
                        else if (source.file.kind == CardEventFaultKind.EVENTS && record.type == 0x06)
                            Text("Die Zeitpunkte beziehen sich auf das Einlegen in der früheren und in der aktuellen Kartensitzung.",
                                style = MaterialTheme.typography.bodySmall)
                    }
                }
                if (visible < source.records.size) OutlinedButton(onClick = {
                    if (label == "Ereignisse") visibleEvents += 20 else visibleFaults += 20
                }) { Text("Weitere $label anzeigen") }
            }
        }
        Text("Ortszeit (${zone.id}). Nicht angegebene Zeiten bleiben offen. Die Ansicht bewertet die Meldungen nicht.",
            style = MaterialTheme.typography.bodySmall)
    }
}

/** Current Appendix 1 2.70: Gen2 codes follow the 2018/502 amendment, V2 the 2021/1228 additions. */
internal fun eventFaultLabel(generation: CardGeneration, type: Int): String {
    val common = when (type) {
        0x00 -> "Ereignis ohne nähere Angaben"
        0x01 -> "Ungültige Karte eingelegt"
        0x02 -> "Kartenkonflikt"
        0x03 -> "Zeitüberschneidung"
        0x04 -> "Fahrt ohne geeignete Karte aufgezeichnet"
        0x05 -> "Karte während der Fahrt eingelegt"
        0x06 -> "Vorherige Kartensitzung nicht korrekt abgeschlossen"
        0x07 -> "Geschwindigkeitsüberschreitung aufgezeichnet"
        0x08 -> "Unterbrechung der Stromversorgung"
        0x09 -> "Fehler der Bewegungsdaten"
        0x0A -> "Bewegungsdatenkonflikt"
        0x10 -> "Sicherheitsmeldung des Fahrzeuggeräts ohne nähere Angaben"
        0x11 -> "Authentifizierung des Bewegungssensors fehlgeschlagen"
        0x12 -> "Authentifizierung der Fahrtenschreiberkarte fehlgeschlagen"
        0x13 -> "Sensorwechsel als nicht autorisiert aufgezeichnet"
        0x14 -> "Integritätsfehler bei Kartendaten"
        0x15 -> "Integritätsfehler bei gespeicherten Daten"
        0x16 -> "Fehler der internen Datenübertragung"
        0x17 -> "Gehäuseöffnung als nicht autorisiert aufgezeichnet"
        0x18 -> "Hardware-Manipulationsmeldung"
        0x20 -> "Sicherheitsmeldung des Sensors ohne nähere Angaben"
        0x21 -> "Authentifizierung des Sensors fehlgeschlagen"
        0x22 -> "Integritätsfehler bei Sensordaten"
        0x23 -> "Fehler der internen Sensorübertragung"
        0x24 -> "Sensoröffnung als nicht autorisiert aufgezeichnet"
        0x25 -> "Sensor-Manipulationsmeldung"
        0x30 -> "Fahrtenschreiberstörung ohne nähere Angaben"
        0x31 -> "Interne Störung des Fahrzeuggeräts"
        0x32 -> "Druckerstörung"
        0x33 -> "Anzeigestörung"
        0x34 -> "Störung beim Herunterladen"
        0x35 -> "Sensorstörung"
        0x40 -> "Kartenstörung ohne nähere Angaben"
        else -> null
    }
    if (common != null) return common
    if (generation != CardGeneration.GEN1) {
        val smart = when (type) {
            0x0B -> "Zeitkonflikt zwischen GNSS und Fahrtenschreiber"
            0x0C -> "Fehler der Fernkommunikation"
            0x0D -> "Keine GNSS-Positionsinformation"
            0x0E -> "Kommunikationsfehler der externen GNSS-Einheit"
            0x19 -> "GNSS-Manipulationsmeldung"
            0x1A -> "Authentifizierung der externen GNSS-Einheit fehlgeschlagen"
            0x1B -> "Zertifikat der externen GNSS-Einheit abgelaufen"
            0x36 -> "Störung des internen GNSS-Empfängers"
            0x37 -> "Störung der externen GNSS-Einheit"
            0x38 -> "Störung der Fernkommunikation"
            0x39 -> "Störung der ITS-Schnittstelle"
            else -> null
        }
        if (smart != null) return smart
    }
    if (generation == CardGeneration.GEN2V2) when (type) {
        0x0F -> return "GNSS-Anomalie"
        0x1C -> return "Widerspruch zwischen Bewegungs- und Tätigkeitsdaten"
        0x3A -> return "Störung des internen Sensors"
    }
    return "Nicht zugeordnete Meldung (Code ${type.toString(16).uppercase().padStart(2, '0')})"
}
