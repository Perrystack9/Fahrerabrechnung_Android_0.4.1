package de.fahrerabrechnung.data.card

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

/** One recorded point, never a paired condition interval or an activity override. */
data class CardDaySpecificConditionMarker(
    val sourceId: String,
    val generation: CardGeneration,
    val record: CardSpecificConditionRecord,
    val time: Instant,
) {
    val id: String get() = "$sourceId:${record.slotIndex}"
}

/** Project only the selected session's exact source for its physical DriverCard. */
object CardDaySpecificConditionMarkers {
    fun forDate(date: LocalDate, zone: ZoneId, driverCardId: String?,
                source: SavedCardSpecificConditions?): List<CardDaySpecificConditionMarker> {
        if (driverCardId.isNullOrBlank() || source == null || source.driverCardId != driverCardId)
            return emptyList()
        val start = date.atStartOfDay(zone).toInstant()
        val end = date.plusDays(1).atStartOfDay(zone).toInstant()
        return source.records.mapNotNull { record ->
            val time = record.timeUtc ?: return@mapNotNull null
            if (time < start || time >= end) null
            else CardDaySpecificConditionMarker(source.id, source.file.generation, record, time)
        }.sortedWith(compareBy<CardDaySpecificConditionMarker> { it.time }.thenBy { it.record.slotIndex })
    }

    /** Real elapsed-time scale, including DST and zoom; offscreen points are not clamped. */
    fun position(marker: CardDaySpecificConditionMarker, visibleStartMillis: Long, visibleMillis: Double): Double? {
        if (!visibleMillis.isFinite() || visibleMillis <= 0) return null
        val elapsed = marker.time.toEpochMilli() - visibleStartMillis
        if (elapsed < 0 || elapsed.toDouble() >= visibleMillis) return null
        return elapsed / visibleMillis
    }
}
