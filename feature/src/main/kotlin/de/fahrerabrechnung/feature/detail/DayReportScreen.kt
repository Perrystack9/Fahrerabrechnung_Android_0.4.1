package de.fahrerabrechnung.feature.detail

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import de.fahrerabrechnung.core.design.LabelValueRow
import de.fahrerabrechnung.core.design.SectionCard
import de.fahrerabrechnung.feature.common.ScreenContent

@Composable
fun DayReportScreen(padding: PaddingValues) {
    ScreenContent(padding) {
        Text("22. September 2026 · Beispieldetail", style = MaterialTheme.typography.titleLarge)
        Text("Vorschau · Noch keine belegten Tagesdaten.",
            style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        SectionCard(title = "Zeiten") {
            LabelValueRow("Bezahlte Zeit", "—")
            LabelValueRow("Lenkzeit", "—")
            LabelValueRow("Sonstige Arbeit", "—")
            LabelValueRow("Bereitschaft", "—")
            LabelValueRow("Pause", "—")
        }
        SectionCard(title = "Quelldaten") {
            Text("Fahrerkarte und Tachograph können später punktuelle Positionsnachweise enthalten.",
                style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            LabelValueRow("Kilometer aus Quelldaten", "—")
        }
    }
}
