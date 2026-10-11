package de.fahrerabrechnung.app

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import de.fahrerabrechnung.core.design.SectionCard
import de.fahrerabrechnung.data.card.CardDirectoryApplication
import de.fahrerabrechnung.data.card.SavedCardDirectory
import de.fahrerabrechnung.feature.common.ScreenContent

@Composable
internal fun CardDirectoryScreen(padding: PaddingValues, source: SavedCardDirectory?) {
    ScreenContent(padding) {
        Text("Anwendungsverzeichnis", style = MaterialTheme.typography.titleLarge)
        Text("Originalverzeichnis aus der ausgewählten gespeicherten Übernahme.")
        if (source == null) Text("Für diese Übernahme ist kein Anwendungsverzeichnis gespeichert.")
        else SectionCard {
            Text("Dateigröße: ${source.file.bytes.size} Bytes")
            val record = source.record
            if (record == null) Text("Dieses gespeicherte Anwendungsverzeichnis ist leer.")
            else {
                Text("Aufgezeichnete Anwendungen", style = MaterialTheme.typography.titleMedium)
                for (application in record.applications) Text(when (application) {
                    CardDirectoryApplication.TACHOGRAPH_GEN1 -> "Fahrtenschreiber Generation 1"
                    CardDirectoryApplication.TACHOGRAPH_GEN2 -> "Fahrtenschreiber Generation 2"
                })
            }
            Text("Das Verzeichnis beschreibt die auf der Karte aufgezeichneten Anwendungen. Es zeigt keine übernommene Zeitspanne.",
                style = MaterialTheme.typography.bodySmall)
        }
    }
}
