package de.fahrerabrechnung.feature.settings

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.clickable
import de.fahrerabrechnung.core.design.LabelValueRow
import de.fahrerabrechnung.core.design.SectionCard
import de.fahrerabrechnung.core.model.ThemeMode
import de.fahrerabrechnung.feature.common.ScreenContent

@Composable
fun SettingsScreen(
    padding: PaddingValues,
    themeMode: ThemeMode,
    onThemeModeChanged: (ThemeMode) -> Unit,
    onOpenSubpage: (String) -> Unit,
) {
    ScreenContent(padding) {
        Text("Einstellungen", style = MaterialTheme.typography.titleLarge)
        SectionCard(title = "Darstellung") {
            ThemeMode.entries.forEach { mode ->
                androidx.compose.foundation.layout.Row(
                    Modifier.clickable { onThemeModeChanged(mode) },
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                ) {
                    RadioButton(selected = themeMode == mode, onClick = { onThemeModeChanged(mode) })
                    Text(when (mode) { ThemeMode.SYSTEM -> "System"; ThemeMode.LIGHT -> "Hell"; ThemeMode.DARK -> "Dunkel" })
                }
            }
        }
        SettingsGroup("Profil", onOpenSubpage)
        SettingsGroup("Standort / GPS", onOpenSubpage)
        SettingsGroup("Spesen / Übernachtung", onOpenSubpage)
        SettingsGroup("Bezahlte Zeit / Zuschläge", onOpenSubpage)
        SettingsGroup("Fahrerkarte / Erinnerungen", onOpenSubpage)
        SettingsGroup("Berichte", onOpenSubpage)
        SettingsGroup("Daten / Datenschutz", onOpenSubpage)
    }
}

@Composable
private fun SettingsGroup(title: String, onOpenSubpage: (String) -> Unit) {
    SectionCard(modifier = Modifier.clickable { onOpenSubpage(title) }) {
        LabelValueRow(title, "Öffnen")
    }
}
