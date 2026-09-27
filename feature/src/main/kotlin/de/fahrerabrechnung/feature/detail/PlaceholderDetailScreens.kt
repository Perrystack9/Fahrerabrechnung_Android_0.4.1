package de.fahrerabrechnung.feature.detail

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import de.fahrerabrechnung.core.design.SectionCard
import de.fahrerabrechnung.feature.common.ScreenContent

@Composable
fun CardImportPlaceholderScreen(padding: PaddingValues) = Placeholder(padding, "Fahrerkarte einlesen", "Noch keine Fahrerkarte eingelesen. Das Einlesen mit einem Kartenleser folgt in einer späteren Phase.")

@Composable
fun VehicleDetailPlaceholderScreen(padding: PaddingValues) = Placeholder(padding, "Fahrzeugdetail", "Fahrzeugdaten werden nach einem späteren Kartenimport angezeigt.")

@Composable
fun SettingsSubpagePlaceholderScreen(padding: PaddingValues, title: String) = Placeholder(padding, title, "Dieser Bereich wird in einer späteren Phase eingerichtet.")

@Composable
private fun Placeholder(padding: PaddingValues, title: String, body: String) {
    ScreenContent(padding) {
        Text(title, style = MaterialTheme.typography.titleLarge)
        SectionCard {
            Text("FahrerTagebuch · Vorschau", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.secondary)
            Text(body, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
