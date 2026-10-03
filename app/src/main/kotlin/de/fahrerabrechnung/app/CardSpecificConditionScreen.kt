package de.fahrerabrechnung.app

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import de.fahrerabrechnung.core.design.SectionCard
import de.fahrerabrechnung.data.card.CardSpecificConditionParser
import de.fahrerabrechnung.data.card.SavedCardSpecificConditions
import de.fahrerabrechnung.feature.common.ScreenContent
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/** Exact selected own source, without pairing begin/end records or changing any activity. */
@Composable
internal fun CardSpecificConditionScreen(padding: PaddingValues, saved: SavedCardSpecificConditions?) {
    val zone = remember { ZoneId.systemDefault() }
    val format = remember { DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss XXX") }
    ScreenContent(padding) {
        Text("Fähre/Zug und Out of Scope", style = MaterialTheme.typography.titleLarge)
        when {
            saved == null -> Text("Für diese Übernahme sind keine gespeicherten Sonderbedingungen verfügbar.")
            saved.records.isEmpty() -> Text("In dieser Kartendatei sind keine Sonderbedingungen aufgezeichnet.")
            else -> {
                Text("Aufzeichnungen des Fahrtenschreibers aus der ausgewählten Übernahme.")
                saved.records.forEach { record ->
                    SectionCard {
                        Text(CardSpecificConditionParser.description(saved.file.generation, record.type),
                            style = MaterialTheme.typography.titleMedium)
                        Text(record.timeUtc?.atZone(zone)?.format(format) ?: "Zeitpunkt: nicht angegeben")
                    }
                }
                Text("Ortszeit (${zone.id}). Die Meldungen ändern keine Tätigkeit und belegen für sich allein keine Ruhezeit.",
                    style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
