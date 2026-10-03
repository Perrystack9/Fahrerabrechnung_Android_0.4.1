package de.fahrerabrechnung.data.card

import org.junit.Assert.*
import org.junit.Test
import java.nio.ByteBuffer
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class CardDaySpecificConditionMarkersTest {
    private val zone = ZoneId.of("Europe/Berlin")
    private val date = LocalDate.of(2026, 9, 28)
    private fun source(vararg entries: Pair<String?, Int>, cardId: String = "own", id: String = "fixture-source",
                       generation: CardGeneration = CardGeneration.GEN2V2): SavedCardSpecificConditions {
        val count = if (generation == CardGeneration.GEN1) 56 else 112
        val header = if (generation == CardGeneration.GEN1) 0 else 2
        val bytes = ByteArray(CardSpecificConditionParser.fileLength(generation, count))
        entries.forEachIndexed { slot, (time, type) ->
            ByteBuffer.wrap(bytes).apply {
                position(header + slot * 5)
                putInt(time?.let { Instant.parse(it).epochSecond.toInt() } ?: 0); put(type.toByte())
            }
        }
        val file = CardSpecificConditionFile(generation, count, bytes)
        return SavedCardSpecificConditions(id, cardId, file, CardSpecificConditionParser.parse(file), 0)
    }
    private fun points(source: SavedCardSpecificConditions?) = CardDaySpecificConditionMarkers.forDate(date, zone, "own", source)

    @Test fun localMidnightUsesHalfOpenDayAndDoesNotAssignUnknownTimes() {
        val source = source("2026-09-27T21:59:59Z" to 1, "2026-09-27T22:00:00Z" to 2,
            "2026-09-28T21:59:59Z" to 3, "2026-09-28T22:00:00Z" to 4, null to 1)
        val points = points(source)
        assertEquals(listOf(1, 2), points.map { it.record.slotIndex })
        assertEquals(listOf(2, 3), points.map { it.record.type })
        assertEquals(Instant.parse("2026-09-27T22:00:00Z"), points.first().time)
        assertEquals(5, source.records.size)
        assertNull(source.records.single { it.slotIndex == 4 }.timeUtc)
    }

    @Test fun anEndAloneAndInvertedSourceOrderStayIndependentExactSeconds() {
        val source = source("2026-09-28T11:00:15Z" to 1, "2026-09-28T10:00:03Z" to 2,
            "2026-09-28T12:00:01Z" to 4)
        val original = source.records.toList()
        val points = points(source)
        assertEquals(listOf(2, 1, 4), points.map { it.record.type })
        assertEquals(Instant.parse("2026-09-28T10:00:03Z"), points.first().time)
        assertEquals(original, source.records)
        assertTrue(points.all { it.record in original && it.time == it.record.timeUtc })
        assertEquals(3, points.size)
    }

    @Test fun coincidentSlotsHaveDistinctStableIdsAndAnotherSourceKeepsItsOwnIdentity() {
        val same = "2026-09-28T10:00:00Z"
        val original = source(same to 1, same to 1, same to 3)
        val projected = points(original)
        assertEquals(listOf(0, 1, 2), projected.map { it.record.slotIndex })
        assertEquals(3, projected.map { it.id }.distinct().size)
        assertEquals(projected, points(original))
        val next = points(source(same to 1, id = "fixture-next-source")).single()
        assertNotEquals(projected.first().id, next.id)
    }

    @Test fun repeatedAutumnClockTimeKeepsTwoDistinctPositionsAndOffsets() {
        val dst = LocalDate.of(2026, 10, 25)
        val source = source("2026-10-25T00:30:00Z" to 3, "2026-10-25T01:30:00Z" to 4)
        val points = CardDaySpecificConditionMarkers.forDate(dst, zone, "own", source)
        assertEquals(points[0].time.atZone(zone).toLocalTime(), points[1].time.atZone(zone).toLocalTime())
        assertNotEquals(points[0].time.atZone(zone).offset, points[1].time.atZone(zone).offset)
        val start = dst.atStartOfDay(zone).toInstant().toEpochMilli()
        val millis = Duration.between(dst.atStartOfDay(zone), dst.plusDays(1).atStartOfDay(zone)).toMillis().toDouble()
        assertEquals(25 * 3_600_000.0, millis, 0.0)
        assertEquals(2.5 / 25, CardDaySpecificConditionMarkers.position(points[0], start, millis)!!, 1e-10)
        assertEquals(3.5 / 25, CardDaySpecificConditionMarkers.position(points[1], start, millis)!!, 1e-10)
    }

    @Test fun springUses23RealHoursAndNextMidnightStaysOnTheNextDay() {
        val dst = LocalDate.of(2026, 3, 29)
        val start = dst.atStartOfDay(zone).toInstant()
        val end = dst.plusDays(1).atStartOfDay(zone).toInstant()
        val source = source(start.toString() to 3, end.toString() to 4)
        val points = CardDaySpecificConditionMarkers.forDate(dst, zone, "own", source)
        assertEquals(1, points.size)
        assertEquals(3, points.single().record.type)
        assertEquals(23 * 3600L, end.epochSecond - start.epochSecond)
        assertEquals(0.0, CardDaySpecificConditionMarkers.position(points.single(), start.toEpochMilli(), 23 * 3_600_000.0)!!, 0.0)
        assertEquals(4, CardDaySpecificConditionMarkers.forDate(dst.plusDays(1), zone, "own", source).single().record.type)
    }

    @Test fun gen1FerryAndReservedCodesKeepTheirSourceMeaningWithoutInventedBoundaries() {
        val source = source("2026-09-28T10:00:00Z" to 3, "2026-09-28T10:01:00Z" to 4,
            "2026-09-28T10:02:00Z" to 0x70, generation = CardGeneration.GEN1)
        val points = points(source)
        assertEquals(listOf(3, 4, 0x70), points.map { it.record.type })
        assertTrue(points.all { it.generation == CardGeneration.GEN1 })
        assertEquals("Fähre/Zug", CardSpecificConditionParser.description(points[0].generation, points[0].record.type))
        assertEquals("Unbekannte Sonderbedingung (Code 04)", CardSpecificConditionParser.description(points[1].generation, points[1].record.type))
        assertEquals(3, points.size)
    }

    @Test fun missingSourceEmptyFileMissingBindingAndAnotherPhysicalCardStayEmpty() {
        val own = source("2026-09-28T10:00:00Z" to 1)
        assertTrue(points(null).isEmpty())
        assertTrue(points(source()).isEmpty())
        assertTrue(points(source(null to 1)).isEmpty())
        for (binding in listOf(null, "", " ", "foreign", "replacement"))
            assertTrue(CardDaySpecificConditionMarkers.forDate(date, zone, binding, own).isEmpty())
        assertTrue(points(source("2026-09-28T10:00:00Z" to 1, cardId = "replacement")).isEmpty())
        assertEquals(1, points(own).size)
    }

    @Test fun zoomUsesExactSecondsAndExcludesOffscreenPointsAndInvalidViewports() {
        val marker = points(source("2026-09-28T10:00:15Z" to 1)).single()
        val start = Instant.parse("2026-09-28T10:00:00Z").toEpochMilli()
        assertEquals(0.25, CardDaySpecificConditionMarkers.position(marker, start, 60_000.0)!!, 0.0)
        assertNull(CardDaySpecificConditionMarkers.position(marker, start, 15_000.0))
        assertNull(CardDaySpecificConditionMarkers.position(marker, start + 16_000, 60_000.0))
        for (millis in listOf(0.0, -1.0, Double.NaN, Double.POSITIVE_INFINITY))
            assertNull(CardDaySpecificConditionMarkers.position(marker, start, millis))
    }
}
