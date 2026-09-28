package de.fahrerabrechnung.data.card

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

/** A source interval projected onto one local calendar date. End is exclusive. */
data class CardDaySegment(
    val start: Instant,
    val end: Instant,
    val activity: DriverActivity,
    val cardInserted: Boolean,
    val slot: CardSlot,
    val crewDriving: Boolean,
    val sourceDateUtc: Instant
)

data class LocalCardDay(val date: LocalDate, val segments: List<CardDaySegment>)

/**
 * A read-only preview of recorded card changes. Each UTC card record is bounded
 * by its own midnight; no activity is carried across a missing record. The card
 * status and activity are retained separately: a not-inserted entry may be a
 * manual entry, but it is never evidence of rest merely because the card is out.
 */
object CardDayTimeline {
    fun build(records: List<CardActivityDay>, zone: ZoneId): List<LocalCardDay> {
        val byDate = sortedMapOf<LocalDate, MutableList<CardDaySegment>>()
        records.forEach { record ->
            val endOfRecord = record.recordDateUtc.plusSeconds(86_400)
            record.changes.forEachIndexed { index, change ->
                var cursor = record.recordDateUtc.plusSeconds(change.minuteOfDay * 60L)
                val next = record.changes.getOrNull(index + 1)
                val end = next?.let { record.recordDateUtc.plusSeconds(it.minuteOfDay * 60L) }
                    ?: endOfRecord
                while (cursor.isBefore(end)) {
                    val localDate = cursor.atZone(zone).toLocalDate()
                    val nextMidnight = localDate.plusDays(1).atStartOfDay(zone).toInstant()
                    val sliceEnd = minOf(end, nextMidnight)
                    byDate.getOrPut(localDate) { mutableListOf() } += CardDaySegment(
                        cursor, sliceEnd, change.activity, change.cardInserted,
                        change.slot, change.crewDriving, record.recordDateUtc
                    )
                    cursor = sliceEnd
                }
            }
        }
        return byDate.map { (date, segments) ->
            LocalCardDay(date, segments.sortedBy { it.start })
        }
    }
}
