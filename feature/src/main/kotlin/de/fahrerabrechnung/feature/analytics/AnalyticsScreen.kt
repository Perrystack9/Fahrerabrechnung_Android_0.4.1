package de.fahrerabrechnung.feature.analytics

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.fahrerabrechnung.core.design.PreviewDivider
import de.fahrerabrechnung.core.design.PreviewMetric
import de.fahrerabrechnung.core.design.SectionCard
import de.fahrerabrechnung.feature.common.ScreenContent

@Composable
fun AnalyticsScreen(padding: PaddingValues) {
    ScreenContent(padding) {
        Text("Monatsauswertung", style = MaterialTheme.typography.titleLarge)
        Text("Vorschau · Noch keine belegten Daten oder Berechnungen.",
            style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        SectionCard(title = "Zeiten") {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                PreviewMetric("Arbeitszeit", Modifier.weight(1f))
                PreviewMetric("Lenkzeit", Modifier.weight(1f))
            }
            PreviewDivider()
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                PreviewMetric("Nachtstunden", Modifier.weight(1f))
                PreviewMetric("Abwesenheitstage", Modifier.weight(1f))
            }
        }
        SectionCard(title = "Spesen & Einsatztage") {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                PreviewMetric("Spesen", Modifier.weight(1f))
                PreviewMetric("Übernachtungen", Modifier.weight(1f))
            }
            PreviewDivider()
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                PreviewMetric("Samstag", Modifier.weight(1f))
                PreviewMetric("Sonntag", Modifier.weight(1f))
            }
        }
        SectionCard(title = "Fahrerkarte & Tachograph") {
            PreviewMetric("Kilometer aus Quelldaten")
            Text("Hinweise und offene Prüfungen erscheinen erst mit belegten Daten.",
                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
