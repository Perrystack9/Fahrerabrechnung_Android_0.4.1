package de.fahrerabrechnung.app

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import de.fahrerabrechnung.core.design.SectionCard
import de.fahrerabrechnung.data.card.*
import de.fahrerabrechnung.feature.common.ScreenContent

internal fun CardOriginalFileKind.originalTitle(): String = when (this) {
    CardOriginalFileKind.APPLICATION_IDENTIFICATION -> "Anwendungsdatei"
    CardOriginalFileKind.APPLICATION_IDENTIFICATION_V2 -> "Anwendungsdatei Version 2"
    CardOriginalFileKind.IDENTIFICATION -> "Identifikationsdatei"
}

/** Only the selected stored receipt; never borrow a file from another card or import. */
@Composable
internal fun CardOriginalFilesScreen(padding: PaddingValues, sources: Map<CardOriginalFileKind, SavedCardOriginalFile>, receiptId: String? = null) {
    var kind by remember { mutableStateOf(CardOriginalFileKind.APPLICATION_IDENTIFICATION) }
    val source = sources[kind]
    var showNumber by remember(kind, source?.id, receiptId) { mutableStateOf(false) }
    ScreenContent(padding) {
        Text("Anwendungs- und Identifikationsdateien", style = MaterialTheme.typography.titleLarge)
        Text("Originaldateien der ausgewählten gespeicherten Übernahme.")
        for (option in CardOriginalFileKind.entries) OutlinedButton(onClick = { kind = option }) {
            Text("${option.originalTitle()} öffnen")
        }
        Text(kind.originalTitle(), style = MaterialTheme.typography.titleMedium)
        if (source == null) Text("Für diese Übernahme ist diese Originaldatei nicht gespeichert.")
        else SectionCard {
            Text("Dateigröße: ${source.file.bytes.size} Bytes")
            Text("Kartengeneration: ${source.file.generation.originalGenerationLabel()}")
            when (val record = source.record) {
                is CardApplicationIdentificationRecord -> {
                    Text("Strukturversion laut Datei: ${record.structureVersionBytes.joinToString(".")}")
                    Text("Ereignisspeicher je Typ: ${record.eventsPerTypeRaw}")
                    Text("Störungsspeicher je Typ: ${record.faultsPerTypeRaw}")
                    Text("Aktivitätsspeicher: ${record.activityBytesRaw} Bytes")
                    Text("Fahrzeugplätze: ${record.vehicleRecordsRaw}")
                    Text("Ortsplätze: ${record.placeRecordsRaw}")
                    record.gnssRecordsRaw?.let { Text("GNSS-Plätze: $it") }
                    record.specificConditionRecordsRaw?.let { Text("Plätze für besondere Bedingungen: $it") }
                    Text(record.vehicleUnitRecordsRaw?.let { "Fahrtenschreiberplätze: $it" }
                        ?: "Fahrtenschreiberplätze: in diesem Dateiformat nicht angegeben")
                    Text("Speicherkapazitäten laut Datei; keine Anzahl tatsächlicher Einträge.", style = MaterialTheme.typography.bodySmall)
                }
                is CardApplicationIdentificationV2Record -> {
                    Text("Grenzübertrittsplätze: ${record.borderRecordsRaw}")
                    Text("Be- und Entladeplätze: ${record.loadUnloadRecordsRaw}")
                    Text("Ladungsartplätze: ${record.loadTypeRecordsRaw}")
                    Text("Speicher für Fahrtenschreiberkonfiguration: ${record.vehicleUnitConfigurationBytesRaw} Bytes")
                    Text("Speicherkapazitäten laut Datei; keine Anzahl tatsächlicher Einträge.", style = MaterialTheme.typography.bodySmall)
                }
                is CardIdentificationRecord -> {
                    Text("Karteninhaber: ${record.card.holder.givenNames} ${record.card.holder.surname}")
                    Text(if (showNumber) "Kartennummer: ${record.card.card.number}"
                        else "Kartennummer: ${maskCardNumber(record.card.card.number)}")
                    Text("Ausgabestaat laut Datei: ${record.card.card.issuingState}")
                    Text("Ausgabestelle: ${record.card.card.issuingAuthority}")
                    OutlinedButton(onClick = { showNumber = !showNumber }) {
                        Text(if (showNumber) "Kartennummer verbergen" else "Kartennummer anzeigen")
                    }
                }
            }
        }
    }
}

private fun CardGeneration.originalGenerationLabel(): String = when (this) {
    CardGeneration.GEN1 -> "Generation 1"
    CardGeneration.GEN2 -> "Generation 2"
    CardGeneration.GEN2V2 -> "Generation 2, Version 2"
    else -> "Unbekannt"
}
