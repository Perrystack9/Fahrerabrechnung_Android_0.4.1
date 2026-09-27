package de.fahrerabrechnung.feature.start

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CreditCard
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.fahrerabrechnung.core.design.PreviewDivider
import de.fahrerabrechnung.core.design.PreviewMetric
import de.fahrerabrechnung.core.design.SectionCard
import de.fahrerabrechnung.feature.common.ScreenContent

@Composable
fun StartScreen(padding: PaddingValues, onOpenCardImport: () -> Unit, onOpenOnboarding: () -> Unit) {
    ScreenContent(padding) {
        Text("Dein Fahrertag im Überblick", style = MaterialTheme.typography.headlineSmall)
        Text("Touren. Zeiten. Übersicht.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)

        SectionCard(title = "Fahrerkarte") {
            Text("Noch keine Fahrerkarte eingelesen", style = MaterialTheme.typography.titleSmall)
            Text("Für die spätere Auswertung werden Daten aus der Fahrerkarte benötigt.",
                style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Button(onClick = onOpenCardImport) {
                Icon(Icons.Rounded.CreditCard, contentDescription = null)
                Text("  Fahrerkarte ansehen")
            }
        }
        TextButton(onClick = onOpenOnboarding) { Text("Einrichtung ansehen") }

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Monatsübersicht", style = MaterialTheme.typography.titleLarge)
            Text("Vorschau", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.secondary)
        }
        SectionCard {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                PreviewMetric("Arbeitszeit", Modifier.weight(1f))
                PreviewMetric("Lenkzeit", Modifier.weight(1f))
            }
            PreviewDivider()
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                PreviewMetric("Nachtstunden", Modifier.weight(1f))
                PreviewMetric("Spesen", Modifier.weight(1f))
            }
            PreviewDivider()
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                PreviewMetric("Abwesenheitstage", Modifier.weight(1f))
                PreviewMetric("Karte / Tacho km", Modifier.weight(1f))
            }
            Text("Noch keine Auswertungsdaten. Es werden keine Werte berechnet.",
                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        Text("Hinweise", style = MaterialTheme.typography.titleLarge)
        Text("Aktuell liegen keine Hinweise vor.",
            style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
