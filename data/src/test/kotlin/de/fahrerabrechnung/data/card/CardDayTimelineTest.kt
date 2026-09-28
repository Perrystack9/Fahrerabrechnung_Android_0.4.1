package de.fahrerabrechnung.data.card

import org.junit.Assert.*
import org.junit.Test
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class CardDayTimelineTest {
    private val berlin = ZoneId.of("Europe/Berlin")

    private fun record(date: String, vararg changes: ActivityChange) = CardActivityDay(
        ringOffset = 0,
        recordDateUtc = Instant.parse("${date}T00:00:00Z"),
        dailyPresenceCounter = "0001",
        distanceKm = 0,
        changes = changes.toList()
    )

    private fun change(minute: Int, activity: DriverActivity, inserted: Boolean = true) =
        ActivityChange(minute, activity, CardSlot.DRIVER, false, inserted)

    @Test fun splitsAtLocalMidnightAndKeepsUtcInstants() {
        val days = CardDayTimeline.build(listOf(record("2026-09-28",
            change(0, DriverActivity.DRIVING),
            change(23 * 60, DriverActivity.REST, false))), berlin)
        assertEquals(listOf(LocalDate.parse("2026-09-28"), LocalDate.parse("2026-09-29")),
            days.map { it.date })
        assertEquals(Instant.parse("2026-09-28T22:00:00Z"), days[0].segments.last().end)
        assertEquals(Instant.parse("2026-09-28T22:00:00Z"), days[1].segments.first().start)
        assertEquals(Instant.parse("2026-09-29T00:00:00Z"), days[1].segments.last().end)
        assertFalse(days[1].segments.last().cardInserted)
        assertEquals(DriverActivity.REST, days[1].segments.last().activity)
    }

    @Test fun daylightSavingBoundaryUsesActualLocalMidnight() {
        val days = CardDayTimeline.build(listOf(record("2026-03-29",
            change(0, DriverActivity.WORK))), berlin)
        assertEquals(2, days.size)
        assertEquals(Instant.parse("2026-03-29T22:00:00Z"), days[0].segments.single().end)
        assertEquals(Instant.parse("2026-03-29T22:00:00Z"), days[1].segments.single().start)
    }

    @Test fun missingRecordsDoNotExtendPreviousActivity() {
        val days = CardDayTimeline.build(listOf(
            record("2026-09-27", change(0, DriverActivity.DRIVING)),
            record("2026-09-29", change(0, DriverActivity.WORK))
        ), ZoneId.of("UTC"))
        assertEquals(listOf(LocalDate.parse("2026-09-27"), LocalDate.parse("2026-09-29")),
            days.map { it.date })
        assertEquals(Instant.parse("2026-09-28T00:00:00Z"), days[0].segments.last().end)
    }
}
