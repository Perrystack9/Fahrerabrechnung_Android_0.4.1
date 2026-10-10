package de.fahrerabrechnung.app

import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import de.fahrerabrechnung.data.card.CardDaySpecificConditionMarker
import de.fahrerabrechnung.data.card.CardDaySpecificConditionMarkers
import de.fahrerabrechnung.data.card.CardGeneration
import de.fahrerabrechnung.data.card.CardSpecificConditionParser
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.math.abs

/** Separate point track so coincident activity/event markers remain independently selectable. */
@Composable
internal fun CardSpecificConditionMarkerTrack(markers: List<CardDaySpecificConditionMarker>, visibleStartMillis: Long,
                                              visibleMillis: Double, tag: String,
                                              onSelect: (List<CardDaySpecificConditionMarker>) -> Unit) {
    Canvas(Modifier.fillMaxWidth().height(40.dp).testTag(tag)
        .semantics { contentDescription = "Fähre/Zug- und Out-of-Scope-Marker. Einzelne Meldungen über die Tagesliste öffnen." }
        .pointerInput(markers, visibleStartMillis, visibleMillis) {
            detectTapGestures { offset ->
                val hit = markers.filter { marker ->
                    CardDaySpecificConditionMarkers.position(marker, visibleStartMillis, visibleMillis)?.let {
                        abs(it * size.width - offset.x) <= 24.dp.toPx()
                    } == true
                }
                if (hit.isNotEmpty()) onSelect(hit)
            }
        }) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textAlign = Paint.Align.CENTER
            textSize = 18.dp.toPx()
        }
        markers.forEach { marker ->
            val position = CardDaySpecificConditionMarkers.position(marker, visibleStartMillis, visibleMillis) ?: return@forEach
            val x = (position * size.width).toFloat()
            paint.color = when (marker.symbol()) {
                "↔" -> android.graphics.Color.rgb(0, 109, 118)
                "⊘" -> android.graphics.Color.rgb(104, 67, 161)
                else -> android.graphics.Color.rgb(89, 99, 110)
            }
            drawLine(Color(paint.color), Offset(x, size.height * 0.65f), Offset(x, size.height), strokeWidth = 2.dp.toPx())
            drawContext.canvas.nativeCanvas.drawText(marker.symbol(),
                x.coerceIn(8.dp.toPx(), size.width - 8.dp.toPx()), size.height * 0.6f, paint)
        }
    }
}

@Composable
internal fun CardSpecificConditionMarkerDialog(markers: List<CardDaySpecificConditionMarker>, zone: ZoneId,
                                               onChoose: (CardDaySpecificConditionMarker) -> Unit, onDismiss: () -> Unit) {
    if (markers.isEmpty()) return
    val format = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss XXX")
    val single = markers.singleOrNull()
    AlertDialog(onDismissRequest = onDismiss,
        title = { Text(single?.description() ?: "Sonderbedingungen dieses Tages") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (single != null) {
                    Text("Zeitpunkt: ${single.time.atZone(zone).format(format)}")
                    Text("Ortszeit (${zone.id}). Aufzeichnung des Fahrtenschreibers. Die Meldung ändert keine Tätigkeit und belegt für sich allein keine Ruhezeit.")
                } else markers.forEach { marker ->
                    OutlinedButton(modifier = Modifier.fillMaxWidth().testTag("choose_condition_${marker.id}"),
                        onClick = { onChoose(marker) }) {
                        Text("${marker.symbol()} ${marker.description()}\n${marker.time.atZone(zone).format(format)}")
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Schließen") } })
}

private fun CardDaySpecificConditionMarker.description() = CardSpecificConditionParser.description(generation, record.type)
private fun CardDaySpecificConditionMarker.symbol() = when {
    record.type in 1..2 -> "⊘"
    record.type == 3 || (record.type == 4 && generation in setOf(CardGeneration.GEN2, CardGeneration.GEN2V2)) -> "↔"
    else -> "?"
}
