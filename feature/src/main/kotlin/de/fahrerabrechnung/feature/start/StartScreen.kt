package de.fahrerabrechnung.feature.start

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CreditCard
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
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
fun StartScreen(padding: PaddingValues, onOpenCardImport: () -> Unit) {
    ScreenContent(padding) {
        Text("Beispieldaten · UI-Prototyp", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        SectionCard(title = "Standortstatus") {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("GPS / Ort", style = MaterialTheme.typography.bodyLarge)
                StatusChip("Noch nicht angebunden", StatusTone.NEUTRAL)
            }
            Text("Produktive Standorterfassung beginnt erst nach dem 0.4.1-Zwischenstopp.", style = MaterialTheme.typography.bodySmall)
        }
        SectionCard(title = "Fahrerkarte") {
            LabelValueRow("Letzter erfolgreicher Import", "Noch keiner")
            Button(onClick = onOpenCardImport) {
                Icon(Icons.Rounded.CreditCard, contentDescription = null)
                Text("  Kartenimport öffnen")
            }
        }
        Text("September · Beispiel", style = MaterialTheme.typography.titleMedium)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            MetricCard("Bezahlte Zeit", "—", Modifier.weight(1f), "keine Berechnung")
            MetricCard("Lenkzeit", "—", Modifier.weight(1f), "keine Berechnung")
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            MetricCard("Nachtzeit", "—", Modifier.weight(1f), "keine Berechnung")
            MetricCard("Spesen", "—", Modifier.weight(1f), "keine Berechnung")
        }
        MetricCard("Übernachtungen", "—", Modifier.fillMaxWidth(), "keine Berechnung")
        SectionCard(title = "Hinweise") {
            Text("Prüfstand noch nicht vorhanden. Produktive Auswertung beginnt erst nach dem 0.4.1-Zwischenstopp.")
        }
    }
}
