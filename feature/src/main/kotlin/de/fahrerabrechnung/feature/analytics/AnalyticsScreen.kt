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
import de.fahrerabrechnung.core.design.LabelValueRow
import de.fahrerabrechnung.core.design.MetricCard
import de.fahrerabrechnung.core.design.SectionCard
import de.fahrerabrechnung.core.design.StatusChip
import de.fahrerabrechnung.core.design.StatusTone
import de.fahrerabrechnung.feature.common.ScreenContent

@Composable
fun AnalyticsScreen(padding: PaddingValues) {
    ScreenContent(padding) {
        Text("Monatsauswertung · UI-Platzhalter", style = MaterialTheme.typography.titleLarge)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            MetricCard("Bezahlte Zeit", "—", Modifier.weight(1f))
            MetricCard("Lenkzeit", "—", Modifier.weight(1f))
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            MetricCard("Nachtzeit", "—", Modifier.weight(1f))
            MetricCard("Übernachtungen", "—", Modifier.weight(1f))
        }
        SectionCard(title = "Zuschläge") {
            LabelValueRow("Nacht", "nicht berechnet")
            LabelValueRow("Samstag", "nicht berechnet")
            LabelValueRow("Sonntag", "nicht berechnet")
            LabelValueRow("Feiertag", "nicht berechnet")
        }
        SectionCard(title = "Kilometer") {
            LabelValueRow("Karte / Tacho", "—")
            LabelValueRow("App-GPS", "—")
            LabelValueRow("Vergleich", "nicht verfügbar")
        }
        StatusChip("Keine produktive Fachlogik in dieser Phase", StatusTone.NEUTRAL)
    }
}
