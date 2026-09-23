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
fun StartScreen(padding: PaddingValues, onOpenCardImport: () -> Unit, onOpenOnboarding: () -> Unit) {
    ScreenContent(padding) {
        Text("Dein Fahrertag im Überblick", style = MaterialTheme.typography.headlineSmall)
        Text("Touren und Zeiten an einem Ort.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        SectionCard(title = "Dein FahrerTagebuch") {
            Text("Lerne die Einrichtung in acht Schritten kennen.")
            androidx.compose.material3.OutlinedButton(onClick = onOpenOnboarding) { Text("Einrichtung ansehen") }
        }
        SectionCard(title = "Standortstatus") {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("GPS / Ort", style = MaterialTheme.typography.bodyLarge)
                StatusChip("Noch nicht angebunden", StatusTone.NEUTRAL)
            }
            Text("Die Standortfunktion steht noch nicht zur Verfügung.", style = MaterialTheme.typography.bodySmall)
        }
        SectionCard(title = "Fahrerkarte") {
            LabelValueRow("Letzter erfolgreicher Import", "Noch keiner")
            Button(onClick = onOpenCardImport) {
                Icon(Icons.Rounded.CreditCard, contentDescription = null)
                Text("  Kartenimport öffnen")
            }
        }
        Text("Monatsübersicht", style = MaterialTheme.typography.titleMedium)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            MetricCard("Arbeitszeit", "—", Modifier.weight(1f), "Noch keine Daten")
            MetricCard("Lenkzeit", "—", Modifier.weight(1f), "Noch keine Daten")
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            MetricCard("Nachtzeit", "—", Modifier.weight(1f), "Noch keine Daten")
            MetricCard("Spesen", "—", Modifier.weight(1f), "Noch keine Daten")
        }
        MetricCard("Übernachtungen", "—", Modifier.fillMaxWidth(), "Noch keine Daten")
        SectionCard(title = "Hinweise") {
            Text("Sobald Daten vorliegen, findest du hier deine Hinweise.")
        }
    }
}
