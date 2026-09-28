package de.fahrerabrechnung.data.card

import org.junit.Assert.*
import org.junit.Test
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class CardDayVehiclesTest {
    private val zone = ZoneId.of("Europe/Berlin")
    private val date = LocalDate.of(2026, 9, 25)
    private fun use(index: Int, begin: String, end: String?) = CardVehicleUse(index,
        Instant.parse(begin), end?.let(Instant::parse), 13, "NÖ NR 6600", null, 100, 200, 0)

    @Test fun clipsOvernightRecordAndKeepsActualStartMarker() {
        val vehicle = use(1, "2026-09-24T16:37:00Z", "2026-09-25T00:00:00Z")
        val day = CardDayVehicles.forDate(date, zone, listOf(vehicle)).single()
        assertEquals(date.atStartOfDay(zone).toInstant(), day.start)
        assertFalse(day.beginsHere)
        assertEquals(Instant.parse("2026-09-25T00:00:00Z"), day.end)
        assertEquals(vehicle, CardDayVehicles.at(day.start, listOf(day)))
        assertNull(CardDayVehicles.at(day.end!!, listOf(day)))
    }

    @Test fun openEndIsOnlyAStartPointAndOverlapsRemainAmbiguous() {
        val open = use(1, "2026-09-25T10:00:00Z", null)
        val a = use(2, "2026-09-25T11:00:00Z", "2026-09-25T12:00:00Z")
        val b = use(3, "2026-09-25T11:30:00Z", "2026-09-25T12:30:00Z")
        val day = CardDayVehicles.forDate(date, zone, listOf(open, a, b))
        assertNull(day.first().end)
        assertNull(CardDayVehicles.at(open.firstUseUtc.plusSeconds(60), day))
        assertNull(CardDayVehicles.at(b.firstUseUtc, day))
        assertEquals(a, CardDayVehicles.at(a.firstUseUtc, day))
    }

    @Test fun daylightSavingUsesRealDayBoundaries() {
        val dst = LocalDate.of(2026, 10, 25)
        val vehicle = use(1, "2026-10-24T21:00:00Z", "2026-10-25T23:00:00Z")
        val day = CardDayVehicles.forDate(dst, zone, listOf(vehicle)).single()
        assertEquals(25 * 3600L, day.end!!.epochSecond - day.start.epochSecond)
    }
}
