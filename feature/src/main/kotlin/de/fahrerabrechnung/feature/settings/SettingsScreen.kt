package de.fahrerabrechnung.feature.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.fahrerabrechnung.core.design.PreviewDivider
import de.fahrerabrechnung.core.design.SectionCard
import de.fahrerabrechnung.core.model.ThemeMode
import de.fahrerabrechnung.feature.common.ScreenContent

@Composable
fun SettingsScreen(
    padding: PaddingValues,
    themeMode: ThemeMode,
    onThemeModeChanged: (ThemeMode) -> Unit,
    onOpenSubpage: (String) -> Unit,
    onOpenOnboarding: () -> Unit,
) {
    ScreenContent(padding) {
        Text("Deine App. Deine Übersicht.", style = MaterialTheme.typography.titleLarge)
        Text("Profil und Arbeitsalltag", style = MaterialTheme.typography.titleMedium)
        SectionCard {
            listOf("Profil", "Fahrerkarte", "Zuhause", "Spesen", "Arbeitszeit & Zuschläge", "Nachtzuschlags-Zeitfenster", "Erinnerungen").forEachIndexed { index, title ->
                if (index > 0) PreviewDivider()
                SettingRow(title) { onOpenSubpage(title) }
            }
        }
        Text("App & Daten", style = MaterialTheme.typography.titleMedium)
        SectionCard {
            Text("Darstellung", style = MaterialTheme.typography.titleMedium)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                ThemeMode.entries.forEach { mode ->
                    Row(
                        Modifier.clickable { onThemeModeChanged(mode) }.sizeIn(minHeight = 48.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(selected = themeMode == mode, onClick = { onThemeModeChanged(mode) })
                        Text(when (mode) { ThemeMode.SYSTEM -> "System"; ThemeMode.LIGHT -> "Hell"; ThemeMode.DARK -> "Dunkel" },
                            style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
            PreviewDivider()
            SettingRow("Backup") { onOpenSubpage("Backup") }
            Text("Lokale Datensicherung; verschlüsseltes Cloud-Backup für Gerätewechsel später optional.",
                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            PreviewDivider()
            SettingRow("Datenschutz") { onOpenSubpage("Datenschutz") }
        }
        TextButton(onClick = onOpenOnboarding) { Text("Einrichtung ansehen · 7 Schritte") }
        Text("FahrerTagebuch · 0.4.1", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun SettingRow(title: String, onClick: () -> Unit) {
    Row(Modifier.fillMaxWidth().clickable(onClick = onClick).sizeIn(minHeight = 48.dp),
        horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text(title, style = MaterialTheme.typography.bodyMedium)
        Text("›", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.secondary)
    }
}
