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

internal fun CardMetadataKind.metadataTitle(): String = when (this) {
    CardMetadataKind.DRIVING_LICENCE -> "Führerschein"
    CardMetadataKind.CURRENT_USAGE -> "Karten-Nutzungseintrag"
    CardMetadataKind.LAST_CARD_DOWNLOAD -> "Download-Zeitpunkt"
    CardMetadataKind.ICC_IDENTIFICATION -> "Kartenkennung"
    CardMetadataKind.IC_IDENTIFICATION -> "Chipkennung"
}

/** Read-only snapshot from the exact selected own-card session, with no inferred facts. */
@Composable
internal fun CardMetadataScreen(padding: PaddingValues, kind: CardMetadataKind, source: SavedCardMetadata?) {
    val zone = remember { ZoneId.systemDefault() }
    val format = remember { DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss XXX") }
    var showNumber by remember(kind, source?.id) { mutableStateOf(false) }
    ScreenContent(padding) {
        Text(kind.metadataTitle(), style = MaterialTheme.typography.titleLarge)
        Text("Kartenstand der ausgewählten gespeicherten Übernahme.")
        when {
            source == null -> Text("Für diese Übernahme ist diese Kartendatei nicht gespeichert.")
            source.record == null -> Text("In dieser Kartendatei ist kein Eintrag aufgezeichnet.")
            else -> SectionCard {
                when (val record = source.record) {
                    is CardDrivingLicenceRecord -> {
                        Text(record.authority.text?.let { "Ausstellende Stelle: $it" }
                            ?: if (record.authority.isBlank) "Ausstellende Stelle: nicht angegeben"
                            else "Ausstellende Stelle: Text nicht lesbar (Zeichensatzcode ${record.authority.codePageRaw})")
                        Text("Ausstellungsstaat: Kartenwert ${record.issuingNationRaw}")
                        when {
                            record.numberIsBlank -> Text("Führerscheinnummer: nicht angegeben")
                            !showNumber -> Text("Führerscheinnummer: verborgen")
                            else -> Text(record.numberText?.let { "Führerscheinnummer: $it" }
                                ?: "Führerscheinnummer: nicht lesbar (Rohwert ${record.numberHex})")
                        }
                        if (!record.numberIsBlank) OutlinedButton(onClick = { showNumber = !showNumber }) {
                            Text(if (showNumber) "Nummer verbergen" else "Nummer anzeigen")
                        }
                        Text("Diese Kartendatei enthält keine Führerscheinklassen oder Gültigkeitsprüfung.",
                            style = MaterialTheme.typography.bodySmall)
                    }
                    is CardCurrentUsageRecord -> {
                        Text(record.timeUtc?.atZone(zone)?.format(format)?.let { "Aufgezeichneter Beginn: $it" }
                            ?: "Aufgezeichneter Beginn: Zeitpunkt nicht angegeben")
                        Text(record.vehicleRegistration.text?.let { "Fahrzeugkennzeichen: $it" }
                            ?: if (record.vehicleRegistration.isBlank) "Fahrzeugkennzeichen: nicht angegeben"
                            else "Fahrzeugkennzeichen: Text nicht lesbar (Zeichensatzcode ${record.vehicleRegistration.codePageRaw})")
                        Text("Zulassungsstaat: Kartenwert ${record.vehicleNationRaw}")
                        Text("Dieser gespeicherte Eintrag zeigt keinen Live-Fahrzustand und enthält keinen Nutzungsabschluss.",
                            style = MaterialTheme.typography.bodySmall)
                    }
                    is CardDownloadRecord -> {
                        Text(record.timeUtc?.atZone(zone)?.format(format)?.let { "Download-Zeitpunkt auf der Karte: $it" }
                            ?: "Download-Zeitpunkt auf der Karte: nicht angegeben")
                        Text("Der Kartenwert ist kein Nachweis eines Downloads durch diese App.",
                            style = MaterialTheme.typography.bodySmall)
                    }
                    is CardIccIdentificationRecord -> {
                        Text(if (showNumber) "Kartenseriennummer: ${record.serialNumberRaw}" else "Kartenseriennummer: verborgen")
                        OutlinedButton(onClick = { showNumber = !showNumber }) {
                            Text(if (showNumber) "Kennung verbergen" else "Kennung anzeigen")
                        }
                        Text("Taktstopp-Code: Kartenwert ${record.clockStopRaw}")
                        Text("Herstellercode der Karte: Kartenwert ${record.manufacturerCodeRaw}")
                        Text("Kartentyp-Code: Kartenwert ${record.equipmentTypeRaw}")
                        Text(record.monthYearText?.let { "Monat/Jahr-Code: $it (Jahr zweistellig)" }
                            ?: "Monat/Jahr-Code: nicht lesbar (Rohwert ${record.monthYearRaw.joinToString(" ") { "%02X".format(it) }})")
                        Text(record.approvalNumberText?.let { "Bauartgenehmigung: $it" }
                            ?: "Bauartgenehmigung: nicht angegeben oder nicht lesbar")
                        Text("Personalisierer-Code: Kartenwert ${record.personaliserIdRaw}")
                        Text("Einbettungsland-Rohwert: ${record.embedderCountryRaw.joinToString(" ") { "%02X".format(it) }}")
                        Text("Modul-Einbetter-Code: Kartenwert ${record.moduleEmbedderRaw}")
                        Text("Herstellerinformation-Rohwert: ${record.manufacturerInformationRaw.joinToString(" ") { "%02X".format(it) }}")
                        Text("IC-Kennung-Rohwert: ${record.icIdentifierRaw.joinToString(" ") { "%02X".format(it) }}")
                        Text("Der Monat/Jahr-Code bezeichnet Herstellung oder Serienvergabe; er legt kein vollständiges Datum fest.",
                            style = MaterialTheme.typography.bodySmall)
                    }
                    is CardChipIdentificationRecord -> {
                        Text(if (showNumber) "Chipkennung: ${record.identifierHex}" else "Chipkennung: verborgen")
                        OutlinedButton(onClick = { showNumber = !showNumber }) {
                            Text(if (showNumber) "Kennung verbergen" else "Kennung anzeigen")
                        }
                        Text("Die Chipkennung besteht aus Serienbytes und Herstellerreferenz.", style = MaterialTheme.typography.bodySmall)
                    }
                    null -> Unit
                }
            }
        }
        if (kind.inMasterFile) Text("Diese Kartenwerte sind kein kryptografischer Echtheitsnachweis.", style = MaterialTheme.typography.bodySmall)
        if (kind in setOf(CardMetadataKind.CURRENT_USAGE, CardMetadataKind.LAST_CARD_DOWNLOAD))
            Text("Ortszeit (${zone.id}).", style = MaterialTheme.typography.bodySmall)
    }
}
