package de.fahrerabrechnung.feature.payroll

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import de.fahrerabrechnung.core.design.LabelValueRow
import de.fahrerabrechnung.core.design.PreviewDivider
import de.fahrerabrechnung.core.design.SectionCard
import de.fahrerabrechnung.feature.common.ScreenContent

@Composable
fun PayrollScreen(padding: PaddingValues) {
    ScreenContent(padding) {
        Text("Abrechnungsvergleich · Beispielstruktur", style = MaterialTheme.typography.titleLarge)
        Text("Vorschau zum späteren neutralen Abgleich mit erfassten Abrechnungswerten.",
            style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        SectionCard(title = "Bezahlte Zeit") {
            LabelValueRow("FahrerTagebuch", "—")
            PreviewDivider()
            LabelValueRow("Erfasster Wert", "—")
            PreviewDivider()
            LabelValueRow("Abweichung", "—")
        }
        SectionCard(title = "Spesen") {
            LabelValueRow("FahrerTagebuch", "—")
            PreviewDivider()
            LabelValueRow("Erfasster Wert", "—")
            PreviewDivider()
            LabelValueRow("Abweichung", "—")
        }
        Text("Noch keine Daten oder Berechnungen. Der Vergleich bewertet niemanden.",
            style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
