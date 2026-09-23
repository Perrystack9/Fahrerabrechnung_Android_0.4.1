package de.fahrerabrechnung.feature.detail

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import de.fahrerabrechnung.core.design.SectionCard
import de.fahrerabrechnung.feature.common.ScreenContent

@Composable
fun CardImportPlaceholderScreen(padding: PaddingValues) = Placeholder(padding, "Fahrerkarte einlesen", "Das Einlesen der Fahrerkarte ist noch nicht verfügbar.")

@Composable
fun VehicleDetailPlaceholderScreen(padding: PaddingValues) = Placeholder(padding, "Fahrzeugdetail", "Es liegen noch keine Fahrzeugdaten vor.")

@Composable
fun SettingsSubpagePlaceholderScreen(padding: PaddingValues, title: String) = Placeholder(padding, title, "Dieser Bereich wird später verfügbar.")

@Composable
private fun Placeholder(padding: PaddingValues, title: String, body: String) {
    ScreenContent(padding) {
        Text(title, style = MaterialTheme.typography.titleLarge)
        SectionCard { Text(body) }
    }
}
