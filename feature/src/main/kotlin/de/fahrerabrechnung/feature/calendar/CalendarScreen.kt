package de.fahrerabrechnung.feature.calendar

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.fahrerabrechnung.core.design.SectionCard
import de.fahrerabrechnung.feature.common.ScreenContent

@Composable
fun CalendarScreen(padding: PaddingValues, onOpenExampleDay: () -> Unit) {
    ScreenContent(padding) {
        Text("September 2026 · Strukturvorschau", style = MaterialTheme.typography.titleLarge)
        Text("Eine ruhige Ansicht für belegte Tage. Noch sind keine Tageszustände berechnet.",
            style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        SectionCard(title = "Tage im Überblick") {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                listOf("20", "21", "22", "23", "24").forEach { day ->
                    if (day == "22") {
                        OutlinedButton(
                            onClick = onOpenExampleDay,
                            modifier = Modifier.size(48.dp),
                            shape = CircleShape,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary),
                            contentPadding = PaddingValues(0.dp),
                        ) { Text(day, maxLines = 1, softWrap = false) }
                    } else {
                        Box(Modifier.size(48.dp), contentAlignment = Alignment.Center) {
                            Text(day, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
            Text("22 · Beispieldetail öffnen", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
        }
        Text("Später: Arbeitstag · Abwesenheitstag · Hinweis · Unklar",
            style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
