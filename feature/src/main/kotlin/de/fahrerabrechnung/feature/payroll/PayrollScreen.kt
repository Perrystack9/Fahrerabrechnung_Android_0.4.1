package de.fahrerabrechnung.feature.payroll

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import de.fahrerabrechnung.core.design.LabelValueRow
import de.fahrerabrechnung.core.design.SectionCard
import de.fahrerabrechnung.core.design.StatusChip
import de.fahrerabrechnung.core.design.StatusTone
import de.fahrerabrechnung.feature.common.ScreenContent

@Composable
fun PayrollScreen(padding: PaddingValues) {
    ScreenContent(padding) {
        Text("Abrechnungsvergleich · Beispielstruktur", style = MaterialTheme.typography.titleLarge)
        SectionCard(title = "Bezahlte Zeit") {
            LabelValueRow("App-Wert", "—")
            LabelValueRow("Abrechnungswert", "nicht erfasst")
            LabelValueRow("Differenz (Ist − App)", "—")
        }
        SectionCard(title = "Spesen") {
            LabelValueRow("App-Wert", "—")
            LabelValueRow("Abrechnungswert", "nicht erfasst")
            LabelValueRow("Differenz", "—")
        }
        StatusChip("Neutraler Vergleich – keine Bewertung des Arbeitgebers", StatusTone.INFO)
        Text("Die Maske ist nur UI. Ist-Werte lösen keine Soll-Neuberechnung aus.", style = MaterialTheme.typography.bodySmall)
    }
}
