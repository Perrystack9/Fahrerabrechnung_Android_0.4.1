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
import de.fahrerabrechnung.data.card.CardDayEventFaultMarker
import de.fahrerabrechnung.data.card.CardDayEventFaultMarkers
import de.fahrerabrechnung.data.card.CardEventFaultBoundary
import de.fahrerabrechnung.data.card.CardEventFaultKind
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.math.abs

/** The point track shares the activity track's real-time scale; no connecting spans. */
@Composable
internal fun CardEventFaultMarkerTrack(markers: List<CardDayEventFaultMarker>, visibleStartMillis: Long,
                                      visibleMillis: Double, tag: String,
                                      onSelect: (List<CardDayEventFaultMarker>) -> Unit) {
    Canvas(Modifier.fillMaxWidth().height(40.dp).testTag(tag)
        .semantics { contentDescription = "Ereignis- und Störungsmarker. Einzelne Meldungen über die Tagesliste öffnen." }
        .pointerInput(markers, visibleStartMillis, visibleMillis) {
            detectTapGestures { offset ->
                val hit = markers.filter { marker ->
                    CardDayEventFaultMarkers.position(marker, visibleStartMillis, visibleMillis)?.let {
                        abs(it * size.width - offset.x) <= 24.dp.toPx()
                    } == true
                }
                if (hit.isNotEmpty()) onSelect(hit)
            }
        }) {
        val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textAlign = Paint.Align.CENTER
            textSize = 18.dp.toPx()
        }
        markers.forEach { marker ->
            val position = CardDayEventFaultMarkers.position(marker, visibleStartMillis, visibleMillis) ?: return@forEach
            val x = (position * size.width).toFloat()
            val event = marker.kind == CardEventFaultKind.EVENTS
            val color = if (event) Color(0xFF145A94) else Color(0xFF875100)
            drawLine(color, Offset(x, size.height * 0.65f), Offset(x, size.height), strokeWidth = 2.dp.toPx())
            labelPaint.color = if (event) android.graphics.Color.rgb(20, 90, 148) else android.graphics.Color.rgb(135, 81, 0)
            drawContext.canvas.nativeCanvas.drawText(if (event) "●" else "◆",
                x.coerceIn(8.dp.toPx(), size.width - 8.dp.toPx()), size.height * 0.6f, labelPaint)
        }
    }
}

@Composable
internal fun CardEventFaultMarkerDialog(markers: List<CardDayEventFaultMarker>, zone: ZoneId,
                                       onChoose: (CardDayEventFaultMarker) -> Unit, onDismiss: () -> Unit) {
    if (markers.isEmpty()) return
    val format = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss XXX")
    val single = markers.singleOrNull()
    AlertDialog(onDismissRequest = onDismiss,
        title = { Text(single?.let { "${it.kind.title()} · ${it.boundary.title()}" } ?: "Meldungen dieses Tages") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (single != null) {
                    CardEventFaultRecordDetails(single.generation, single.kind, single.record, zone, format)
                    Text("Ortszeit (${zone.id}). Aufzeichnung des Fahrtenschreibers; keine Bewertung.")
                } else markers.forEach { marker ->
                    OutlinedButton(modifier = Modifier.fillMaxWidth().testTag("choose_issue_${marker.id}"),
                        onClick = { onChoose(marker) }) {
                        Text("${marker.kind.symbol()} ${marker.kind.title()} · ${marker.boundary.title()}\n" +
                            "${marker.time.atZone(zone).format(format)}\n${eventFaultLabel(marker.generation, marker.record.type)}")
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Schließen") } })
}

internal fun CardEventFaultKind.title() = if (this == CardEventFaultKind.EVENTS) "Ereignis" else "Störung"
internal fun CardEventFaultKind.symbol() = if (this == CardEventFaultKind.EVENTS) "●" else "◆"
internal fun CardEventFaultBoundary.title() = when (this) {
    CardEventFaultBoundary.BEGIN -> "Beginn"
    CardEventFaultBoundary.END -> "Ende"
    CardEventFaultBoundary.BEGIN_AND_END -> "Beginn und Ende"
}
