package de.fahrerabrechnung.feature.detail

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import de.fahrerabrechnung.core.design.LabelValueRow
import de.fahrerabrechnung.core.design.SectionCard
import de.fahrerabrechnung.core.design.StatusChip
import de.fahrerabrechnung.core.design.StatusTone
import de.fahrerabrechnung.domain.status.RouteStatus
import de.fahrerabrechnung.feature.common.ScreenContent

@Composable
fun DayReportScreen(padding: PaddingValues) {
    ScreenContent(padding) {
        Text("22. September 2026 · Beispieldetail", style = MaterialTheme.typography.titleLarge)
        StatusChip("Keine Daten", StatusTone.NEUTRAL)
        SectionCard(title = "Zeiten") {
            LabelValueRow("Bezahlte Zeit", "—")
            LabelValueRow("Lenkzeit", "—")
            LabelValueRow("Sonstige Arbeit", "—")
            LabelValueRow("Bereitschaft", "—")
            LabelValueRow("Pause", "—")
        }
        SectionCard(title = "Route") {
            LabelValueRow("Status", RouteStatus.KEINE_ORTSDATEN.name)
            Text("Keine Route wird aus Gen2-Ortsankern erzeugt. Die spätere Darstellung bleibt getrennt.")
        }
        SectionCard(title = "Kilometervergleich") {
            LabelValueRow("Gemeinsame Abdeckung", "—")
            LabelValueRow("Differenz GPS − Karte", "—")
        }
    }
}
