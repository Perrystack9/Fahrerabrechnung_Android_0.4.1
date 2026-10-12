package de.fahrerabrechnung.app

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import de.fahrerabrechnung.core.design.SectionCard
import de.fahrerabrechnung.data.card.CardAtrInfoRecord
import de.fahrerabrechnung.data.card.CardCommunicationKind
import de.fahrerabrechnung.data.card.CardExtendedLengthRecord
import de.fahrerabrechnung.data.card.SavedCardCommunication
import de.fahrerabrechnung.feature.common.ScreenContent

@Composable
internal fun CardCommunicationScreen(padding: PaddingValues, sources: Map<CardCommunicationKind, SavedCardCommunication>) {
    ScreenContent(padding) {
        Text("Kommunikationsangaben", style = MaterialTheme.typography.titleLarge)
        Text("Kartenangaben aus der ausgewählten gespeicherten Übernahme.")
        for (kind in CardCommunicationKind.entries) SectionCard {
            Text(if (kind == CardCommunicationKind.ATR_INFO) "Pufferinformation" else "Protokollunterstützung",
                style = MaterialTheme.typography.titleMedium)
            val source = sources[kind]
            if (source == null) Text("Für diese Übernahme ist diese bedingte Kartenangabe nicht gespeichert.")
            else {
                Text("Dateigröße: ${source.file.bytes.size} Bytes")
                when (val record = source.record) {
                    null -> Text("Diese gespeicherte Kartendatei ist leer.")
                    is CardAtrInfoRecord -> {
                        Text("Die Karte hat Pufferinformationen aufgezeichnet.")
                        Text("Die Angaben wurden unverändert gespeichert; Puffergrößen werden noch nicht ausgewertet.",
                            style = MaterialTheme.typography.bodySmall)
                    }
                    is CardExtendedLengthRecord -> {
                        fun label(value: Boolean?) = when (value) { true -> "aufgezeichnet"; false -> "nicht aufgezeichnet"; null -> "nicht zugeordnet" }
                        Text("Erweiterte Länge für T=0: ${label(record.supportsT0)}")
                        Text("Erweiterte Länge für T=1: ${label(record.supportsT1)}")
                        if (record.supportsT0 == null) Text("Unbekannter Protokollcode: Kartenwert ${record.protocolCodeRaw}")
                    }
                }
            }
        }
        Text("Diese Kartenangaben bestätigen keine Verbindung oder vollständige Datenübernahme.",
            style = MaterialTheme.typography.bodySmall)
    }
}
