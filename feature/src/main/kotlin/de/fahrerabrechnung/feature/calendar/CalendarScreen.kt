package de.fahrerabrechnung.feature.calendar

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.fahrerabrechnung.core.design.SectionCard
import de.fahrerabrechnung.core.design.StatusChip
import de.fahrerabrechnung.core.design.StatusTone
import de.fahrerabrechnung.feature.common.ScreenContent

@Composable
fun CalendarScreen(padding: PaddingValues, onOpenExampleDay: () -> Unit) {
    ScreenContent(padding) {
        Text("September 2026 · Strukturvorschau", style = MaterialTheme.typography.titleLarge)
        SectionCard {
            Text("Kalendertage können später höchstens zwei kompakte Marker zeigen. Noch werden keine Arbeitstage aus Mockdaten abgeleitet.")
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("20", "21", "22", "23", "24").forEach { day ->
                    OutlinedButton(
                        onClick = if (day == "22") onOpenExampleDay else ({}),
                        modifier = Modifier.weight(1f).sizeIn(minHeight = 48.dp),
                    ) { Text(day) }
                }
            }
            StatusChip("22 · Beispieltag öffnen", StatusTone.INFO)
        }
        SectionCard(title = "Legende") {
            Text("Marker werden später aus belegten Tagesdaten abgeleitet. GPS ohne Kartenimport erzeugt keinen fertigen Arbeitstag.")
        }
    }
}
