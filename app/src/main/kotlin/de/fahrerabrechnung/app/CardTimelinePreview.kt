package de.fahrerabrechnung.app

import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import de.fahrerabrechnung.data.card.CardDaySegment
import de.fahrerabrechnung.data.card.CardDayVehicle
import de.fahrerabrechnung.data.card.CardDayVehicles
import de.fahrerabrechnung.data.card.CardDayEventFaultMarker
import de.fahrerabrechnung.data.card.CardDaySpecificConditionMarker
import de.fahrerabrechnung.data.card.DriverActivity
import de.fahrerabrechnung.data.card.LocalCardDay
import java.time.Duration
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.math.roundToLong

/** Read-only preview; missing source intervals remain visibly blank. */
@Composable
internal fun CardTimelinePreview(day: LocalCardDay, zone: ZoneId,
                                 vehicles: List<CardDayVehicle> = emptyList(),
                                 onOpenVehicleHistory: (String?, Long?, Int?) -> Unit = { _, _, _ -> },
                                 allowHistoryNavigation: Boolean = true,
                                 eventFaultMarkers: List<CardDayEventFaultMarker> = emptyList(),
                                 specificConditionMarkers: List<CardDaySpecificConditionMarker> = emptyList()) {
    val start = day.date.atStartOfDay(zone).toInstant()
    val end = day.date.plusDays(1).atStartOfDay(zone).toInstant()
    val totalMillis = Duration.between(start, end).toMillis()
    var zoom by remember(day.date, zone) { mutableFloatStateOf(1f) }
    var windowStart by remember(day.date, zone) { mutableFloatStateOf(0f) }
    var cursor by remember(day.date, zone) { mutableFloatStateOf(0.5f) }
    var selected by remember(day.date, zone) { mutableStateOf<CardDaySegment?>(null) }
    var selectedIssues by remember(day.date, zone, eventFaultMarkers) { mutableStateOf(emptyList<CardDayEventFaultMarker>()) }
    var selectedConditions by remember(day.date, zone, specificConditionMarkers) { mutableStateOf(emptyList<CardDaySpecificConditionMarker>()) }

    fun select(fraction: Float) {
        cursor = fraction.coerceIn(0f, 1f)
        val point = start.plusMillis((cursor * totalMillis).roundToLong()
            .coerceIn(0L, totalMillis - 1))
        selected = day.segments.lastOrNull { !point.isBefore(it.start) && point.isBefore(it.end) }
    }
    fun centerOnCursor() {
        windowStart = (cursor - 0.5f / zoom).coerceIn(0f, 1f - 1f / zoom)
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (vehicles.isNotEmpty()) {
            Text("Fahrzeugnutzung laut Karte", style = MaterialTheme.typography.titleSmall)
            Canvas(Modifier.fillMaxWidth().height(28.dp)) {
                drawVehicleSpans(vehicles, start.toEpochMilli(), totalMillis.toDouble())
            }
            Text("▰ belegtes Zeitfenster · ▲ Nutzungsbeginn · offenes Ende nur als Zeitpunkt",
                style = MaterialTheme.typography.bodySmall)
        }
        Text("24-Stunden-Übersicht", style = MaterialTheme.typography.titleSmall)
        if (specificConditionMarkers.isNotEmpty()) {
            CardSpecificConditionMarkerTrack(specificConditionMarkers, start.toEpochMilli(), totalMillis.toDouble(),
                "timeline_condition_overview", onSelect = { selectedConditions = it })
            Text("↔ Fähre/Zug · ⊘ Out of Scope · ? unbekannte Sonderbedingung · Zeitpunkte antippen",
                style = MaterialTheme.typography.bodySmall)
        }
        if (eventFaultMarkers.isNotEmpty()) {
            CardEventFaultMarkerTrack(eventFaultMarkers, start.toEpochMilli(), totalMillis.toDouble(),
                "timeline_issue_overview", onSelect = { selectedIssues = it })
            Text("● Ereignis · ◆ Störung · aufgezeichnete Zeitpunkte antippen",
                style = MaterialTheme.typography.bodySmall)
        }
        Canvas(Modifier.fillMaxWidth().height(44.dp).pointerInput(day.date, zone) {
            detectTapGestures { offset ->
                select(offset.x / size.width)
                zoom = maxOf(zoom, 4f)
                centerOnCursor()
            }
        }) {
            drawSegments(day.segments, start.toEpochMilli(), totalMillis.toDouble())
            drawLine(Color(0xFF102F4C), Offset(cursor * size.width, 0f),
                Offset(cursor * size.width, size.height), strokeWidth = 2.dp.toPx())
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            val marks = if (totalMillis == 86_400_000L) listOf("00", "06", "12", "18", "24")
                else listOf("00", "Zeitumstellung", "24")
            marks.forEach { Text(it, style = MaterialTheme.typography.labelSmall) }
        }
        Column {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                listOf("▶ Lenken" to Color(0xFF145A94),
                    "◆ Arbeit" to Color(0xFF875100),
                    "◷ Bereitschaft" to Color(0xFF6843A1)).forEach { (label, color) ->
                    Text(label, color = color, style = MaterialTheme.typography.labelSmall)
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("☾ Pause/Ruhe", color = Color(0xFF16765A), style = MaterialTheme.typography.labelSmall)
                Text("○ Karte nicht eingelegt", color = Color(0xFF59636E),
                    style = MaterialTheme.typography.labelSmall)
            }
        }
        Text("Detail · mit zwei Fingern zoomen, seitlich verschieben oder +/− tippen",
            style = MaterialTheme.typography.bodySmall)
        if (vehicles.isNotEmpty()) Canvas(Modifier.fillMaxWidth().height(28.dp)) {
            drawVehicleSpans(vehicles,
                start.toEpochMilli() + (windowStart * totalMillis).roundToLong(),
                totalMillis / zoom.toDouble())
        }
        if (eventFaultMarkers.isNotEmpty()) CardEventFaultMarkerTrack(eventFaultMarkers,
            start.toEpochMilli() + (windowStart * totalMillis).roundToLong(), totalMillis / zoom.toDouble(),
            "timeline_issue_detail", onSelect = { selectedIssues = it })
        if (specificConditionMarkers.isNotEmpty()) CardSpecificConditionMarkerTrack(specificConditionMarkers,
            start.toEpochMilli() + (windowStart * totalMillis).roundToLong(), totalMillis / zoom.toDouble(),
            "timeline_condition_detail", onSelect = { selectedConditions = it })
        Canvas(Modifier.fillMaxWidth().height(92.dp)
            .pointerInput(day.date, zone) {
                detectTransformGestures { centroid, pan, gestureZoom, _ ->
                    val oldWindow = 1f / zoom
                    val nextZoom = (zoom * gestureZoom).coerceIn(1f, 1440f)
                    val nextWindow = 1f / nextZoom
                    val focal = windowStart + centroid.x / size.width * oldWindow
                    zoom = nextZoom
                    windowStart = (focal - centroid.x / size.width * nextWindow -
                        pan.x / size.width * nextWindow).coerceIn(0f, 1f - nextWindow)
                }
            }
            .pointerInput(day.date, zone, zoom, windowStart) {
                detectTapGestures { offset ->
                    select(windowStart + offset.x / size.width / zoom)
                }
            }) {
            val visibleMillis = totalMillis / zoom.toDouble()
            drawSegments(day.segments,
                start.toEpochMilli() + (windowStart * totalMillis).roundToLong(), visibleMillis)
            val cursorX = (cursor - windowStart) * zoom * size.width
            if (cursorX in 0f..size.width)
                drawLine(Color(0xFF102F4C), Offset(cursorX, 0f),
                    Offset(cursorX, size.height), strokeWidth = 2.dp.toPx())
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = {
                zoom = (zoom / 2f).coerceAtLeast(1f); centerOnCursor()
            }, enabled = zoom > 1f) { Text("−") }
            Text("${zoom.toInt()}×", modifier = Modifier.padding(top = 14.dp))
            OutlinedButton(onClick = {
                zoom = (zoom * 2f).coerceAtMost(1440f); centerOnCursor()
            }, enabled = zoom < 1440f) { Text("+") }
        }
        Text("Zeitmarke verschieben", style = MaterialTheme.typography.labelMedium)
        Slider(value = cursor, onValueChange = { select(it); centerOnCursor() })
        val cursorMinute = (cursor * (totalMillis / 60_000.0)).roundToLong()
        val cursorTime = start.plusSeconds(cursorMinute * 60)
        Text("Zeitmarke: ${cursorTime.atZone(zone).format(DateTimeFormatter.ofPattern("dd.MM. HH:mm XXX"))}",
            style = MaterialTheme.typography.bodyMedium)
        if (vehicles.isNotEmpty()) {
            val point = start.plusMillis((cursor * totalMillis).roundToLong()
                .coerceIn(0L, totalMillis - 1))
            val use = CardDayVehicles.at(point, vehicles)
            if (use != null) {
                Text("Fahrzeug zur Zeitmarke: ${use.registrationNumber.ifBlank { "Kennzeichen unbekannt" }} · Staat ${use.registrationNation}",
                    style = MaterialTheme.typography.bodySmall)
                if (allowHistoryNavigation) OutlinedButton(onClick = {
                    val validVin = use.vin?.trim()?.takeIf {
                        it.matches(Regex("[A-HJ-NPR-Za-hj-npr-z0-9]{17}"))
                    }
                    onOpenVehicleHistory(validVin ?: use.registrationNumber,
                        use.firstUseUtc.epochSecond, use.registrationNation)
                }) { Text("Dieses Fahrzeug in der Historie öffnen") }
            } else Text("Fahrzeug zur Zeitmarke nicht eindeutig belegt.",
                style = MaterialTheme.typography.bodySmall)
            vehicles.filter { it.beginsHere }.forEach { vehicle ->
                Text("▲ ${vehicle.start.atZone(zone).format(DateTimeFormatter.ofPattern("HH:mm XXX"))} · " +
                    "${vehicle.use.registrationNumber.ifBlank { "Kennzeichen unbekannt" }} · Staat ${vehicle.use.registrationNation}",
                    style = MaterialTheme.typography.bodySmall)
            }
        }
        selected?.let { segment ->
            val formatter = DateTimeFormatter.ofPattern("dd.MM. HH:mm XXX")
            val title = if (segment.cardInserted) segment.activity.title()
                else "○ Karte nicht eingelegt · Karteneintrag: ${segment.activity.title()}"
            Text(title, color = segment.timelineColor(), style = MaterialTheme.typography.titleSmall)
            val duration = Duration.between(segment.start, segment.end).toMinutes()
            Text("${segment.start.atZone(zone).format(formatter)} – ${segment.end.atZone(zone).format(formatter)} · ${duration / 60} h ${(duration % 60).toString().padStart(2, '0')} min")
            if (!segment.cardInserted)
                Text("Kartenentnahme allein belegt keine Ruhe oder Heimkehr.",
                    style = MaterialTheme.typography.bodySmall)
        } ?: Text("Für die Zeitmarke liegt kein Kartenabschnitt vor.",
            style = MaterialTheme.typography.bodySmall)
        if (totalMillis != 86_400_000L)
            Text("Zeitumstellung: Dieser Ortskalendertag umfasst ${totalMillis / 3_600_000} reale Stunden.",
                style = MaterialTheme.typography.bodySmall)
        if (eventFaultMarkers.isNotEmpty()) OutlinedButton(
            modifier = Modifier.testTag("day_issue_list"), onClick = { selectedIssues = eventFaultMarkers }) {
            Text("Meldungen dieses Tages (${eventFaultMarkers.size} Zeitpunkte)")
        }
        if (specificConditionMarkers.isNotEmpty()) OutlinedButton(
            modifier = Modifier.testTag("day_condition_list"), onClick = { selectedConditions = specificConditionMarkers }) {
            Text("Sonderbedingungen dieses Tages (${specificConditionMarkers.size} Zeitpunkte)")
        }
    }
    CardEventFaultMarkerDialog(selectedIssues, zone, onChoose = { selectedIssues = listOf(it) },
        onDismiss = { selectedIssues = emptyList() })
    CardSpecificConditionMarkerDialog(selectedConditions, zone, onChoose = { selectedConditions = listOf(it) },
        onDismiss = { selectedConditions = emptyList() })
}

private fun DrawScope.drawVehicleSpans(
    vehicles: List<CardDayVehicle>, visibleStartMillis: Long, visibleMillis: Double
) {
    drawRect(Color(0xFFE5E9ED))
    val visibleEnd = visibleStartMillis + visibleMillis
    vehicles.forEach { vehicle ->
        vehicle.end?.let { end ->
            val left = maxOf(vehicle.start.toEpochMilli().toDouble(), visibleStartMillis.toDouble())
            val right = minOf(end.toEpochMilli().toDouble(), visibleEnd)
            if (right > left) drawRect(Color(0xFF007D85),
                topLeft = Offset(((left - visibleStartMillis) / visibleMillis * size.width).toFloat(), 0f),
                size = Size(((right - left) / visibleMillis * size.width).toFloat(), size.height))
        }
        if (vehicle.beginsHere && vehicle.start.toEpochMilli() >= visibleStartMillis &&
            vehicle.start.toEpochMilli() < visibleEnd) {
            val x = ((vehicle.start.toEpochMilli() - visibleStartMillis) / visibleMillis * size.width).toFloat()
            drawLine(Color(0xFF102F4C), Offset(x, 0f), Offset(x, size.height), strokeWidth = 2.dp.toPx())
            drawContext.canvas.nativeCanvas.drawText("▲", x.coerceIn(8.dp.toPx(), size.width - 8.dp.toPx()),
                size.height - 2.dp.toPx(), Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = android.graphics.Color.WHITE
                    textAlign = Paint.Align.CENTER
                    textSize = 14.dp.toPx()
                })
        }
    }
}

private fun DrawScope.drawSegments(
    segments: List<CardDaySegment>, visibleStartMillis: Long, visibleMillis: Double
) {
    drawRect(Color(0xFFE5E9ED))
    val visibleEnd = visibleStartMillis + visibleMillis
    val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.WHITE
        textAlign = Paint.Align.CENTER
        textSize = 15.dp.toPx()
    }
    segments.forEach { segment ->
        val left = maxOf(segment.start.toEpochMilli().toDouble(), visibleStartMillis.toDouble())
        val right = minOf(segment.end.toEpochMilli().toDouble(), visibleEnd)
        if (right <= left) return@forEach
        val x = ((left - visibleStartMillis) / visibleMillis * size.width).toFloat()
        val width = ((right - left) / visibleMillis * size.width).toFloat()
        drawRect(segment.timelineColor(), topLeft = Offset(x, 0f),
            size = Size(width, size.height))
        if (width > 20.dp.toPx())
            drawContext.canvas.nativeCanvas.drawText(segment.symbol(),
                x + width / 2f, size.height / 2f + labelPaint.textSize / 3f, labelPaint)
    }
}

private fun CardDaySegment.timelineColor(): Color = if (!cardInserted) Color(0xFF59636E)
    else when (activity) {
        DriverActivity.DRIVING -> Color(0xFF145A94)
        DriverActivity.WORK -> Color(0xFF875100)
        DriverActivity.AVAILABILITY -> Color(0xFF6843A1)
        DriverActivity.REST -> Color(0xFF16765A)
    }

private fun CardDaySegment.symbol(): String = if (!cardInserted) "○" else when (activity) {
    DriverActivity.DRIVING -> "▶"
    DriverActivity.WORK -> "◆"
    DriverActivity.AVAILABILITY -> "◷"
    DriverActivity.REST -> "☾"
}

private fun DriverActivity.title(): String = when (this) {
    DriverActivity.DRIVING -> "▶ Lenken"
    DriverActivity.WORK -> "◆ Andere Arbeit"
    DriverActivity.AVAILABILITY -> "◷ Bereitschaft"
    DriverActivity.REST -> "☾ Pause/Ruhe"
}
