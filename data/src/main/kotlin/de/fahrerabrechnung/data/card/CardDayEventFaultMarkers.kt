package de.fahrerabrechnung.data.card

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

enum class CardEventFaultBoundary { BEGIN, END, BEGIN_AND_END }

/** A recorded point, never an inferred incident interval or activity assignment. */
data class CardDayEventFaultMarker(
    val sourceId: String,
    val generation: CardGeneration,
    val kind: CardEventFaultKind,
    val record: CardEventFaultRecord,
    val time: Instant,
    val boundary: CardEventFaultBoundary,
) {
    val id: String get() = "$sourceId:${record.groupIndex}:${record.slotIndex}:${boundary.name}"
}

/** Use only the exact selected session's sources, with its immutable physical card identity. */
object CardDayEventFaultMarkers {
    fun forDate(date: LocalDate, zone: ZoneId, driverCardId: String?,
                events: SavedCardEventFault?, faults: SavedCardEventFault?): List<CardDayEventFaultMarker> {
        if (driverCardId.isNullOrBlank()) return emptyList()
        val start = date.atStartOfDay(zone).toInstant()
        val end = date.plusDays(1).atStartOfDay(zone).toInstant()
        return buildList {
            for ((kind, source) in listOf(CardEventFaultKind.EVENTS to events, CardEventFaultKind.FAULTS to faults)) {
                if (source == null || source.driverCardId != driverCardId || source.file.kind != kind) continue
                source.records.forEach { record ->
                    fun point(time: Instant?, boundary: CardEventFaultBoundary) {
                        if (time != null && time >= start && time < end)
                            add(CardDayEventFaultMarker(source.id, source.file.generation, kind, record, time, boundary))
                    }
                    if (record.beginUtc != null && record.beginUtc == record.endUtc)
                        point(record.beginUtc, CardEventFaultBoundary.BEGIN_AND_END)
                    else {
                        point(record.beginUtc, CardEventFaultBoundary.BEGIN)
                        point(record.endUtc, CardEventFaultBoundary.END)
                    }
                }
            }
        }.sortedWith(compareBy<CardDayEventFaultMarker> { it.time }.thenBy { it.kind.ordinal }
            .thenBy { it.record.groupIndex }.thenBy { it.record.slotIndex }.thenBy { it.boundary.ordinal })
    }

    /** Same real elapsed-time scale as the activity track, including DST and a zoomed viewport. */
    fun position(marker: CardDayEventFaultMarker, visibleStartMillis: Long, visibleMillis: Double): Double? {
        if (!visibleMillis.isFinite() || visibleMillis <= 0) return null
        val elapsed = marker.time.toEpochMilli() - visibleStartMillis
        if (elapsed < 0 || elapsed.toDouble() >= visibleMillis) return null
        return elapsed / visibleMillis
    }
}
