package de.fahrerabrechnung.feature.detail

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import de.fahrerabrechnung.core.design.SectionCard
import de.fahrerabrechnung.feature.common.ScreenContent

@Composable
fun CardImportPlaceholderScreen(padding: PaddingValues) = Placeholder(padding, "Fahrerkarte einlesen", "Transport-/Parser-Schnittstelle vorbereitet. USB-CCID und Kartenparser sind am Zwischenstopp ausdrücklich noch nicht implementiert.")

@Composable
fun VehicleDetailPlaceholderScreen(padding: PaddingValues) = Placeholder(padding, "Fahrzeugdetail", "Fahrzeugdarstellung ist vorbereitet. VIN-Decoding bleibt eine Schnittstelle; es werden keine Hersteller-/Modelldaten geraten.")

@Composable
fun SettingsSubpagePlaceholderScreen(padding: PaddingValues, title: String) = Placeholder(padding, title, "Diese Unterseite ist als Navigationziel vorbereitet. Fachliche Eingaben werden erst in einer später geprüften Phase umgesetzt.")

@Composable
private fun Placeholder(padding: PaddingValues, title: String, body: String) {
    ScreenContent(padding) {
        Text(title, style = MaterialTheme.typography.titleLarge)
        SectionCard { Text(body) }
    }
}
