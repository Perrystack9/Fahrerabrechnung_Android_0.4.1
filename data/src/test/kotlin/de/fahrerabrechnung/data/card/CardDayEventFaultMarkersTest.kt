package de.fahrerabrechnung.data.card

import org.junit.Assert.*
import org.junit.Test
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class CardDayEventFaultMarkersTest {
    private val zone = ZoneId.of("Europe/Berlin")
    private val date = LocalDate.of(2026, 9, 28)
    private fun record(begin: String?, end: String?, slot: Int = 0) = CardEventFaultRecord(0, slot, 3,
        begin?.let(Instant::parse), end?.let(Instant::parse), 13, "TEST ONLY")
    private fun source(kind: CardEventFaultKind, vararg records: CardEventFaultRecord, cardId: String = "own") =
        SavedCardEventFault("fixture-${kind.name}", cardId,
            CardEventFaultFile(kind, CardGeneration.GEN2V2, if (kind == CardEventFaultKind.EVENTS) 12 else 24, byteArrayOf()),
            records.toList(), 0)

    @Test fun missingTimesAreNotInferredAndAnEndWithoutBeginIsAnEndPoint() {
        val event = source(CardEventFaultKind.EVENTS,
            record(null, null), record(null, "2026-09-28T10:00:15Z", 1),
            record("2026-09-28T11:00:01Z", null, 2))
        val markers = CardDayEventFaultMarkers.forDate(date, zone, "own", event, null)
        assertEquals(2, markers.size)
        assertEquals(CardEventFaultBoundary.END, markers[0].boundary)
        assertEquals(CardEventFaultBoundary.BEGIN, markers[1].boundary)
        assertEquals(Instant.parse("2026-09-28T10:00:15Z"), markers[0].time)
        assertNull(markers[0].record.beginUtc)
        assertNull(markers[1].record.endUtc)
    }

    @Test fun overnightRecordsKeepOnlyActualPointsOnEachLocalDay() {
        val event = source(CardEventFaultKind.EVENTS, record("2026-09-27T21:59:59Z", "2026-09-27T22:00:00Z"))
        val previous = CardDayEventFaultMarkers.forDate(date.minusDays(1), zone, "own", event, null).single()
        val today = CardDayEventFaultMarkers.forDate(date, zone, "own", event, null).single()
        assertEquals(CardEventFaultBoundary.BEGIN, previous.boundary)
        assertEquals(CardEventFaultBoundary.END, today.boundary)
        assertEquals(date.atStartOfDay(zone).toInstant(), today.time)
        assertTrue(CardDayEventFaultMarkers.forDate(date.plusDays(1), zone, "own", event, null).isEmpty())
    }

    @Test fun invertedTimesRetainTheirFieldMeaningAndDoNotBecomeAnInterval() {
        val raw = record("2026-09-28T11:00:00Z", "2026-09-28T10:00:00Z")
        val markers = CardDayEventFaultMarkers.forDate(date, zone, "own", source(CardEventFaultKind.EVENTS, raw), null)
        assertEquals(listOf(CardEventFaultBoundary.END, CardEventFaultBoundary.BEGIN), markers.map { it.boundary })
        assertTrue(markers.all { it.record == raw })
        assertEquals(raw.endUtc, markers.first().time)
        assertEquals(raw.beginUtc, markers.last().time)
    }

    @Test fun equalEndpointsShareAPointButDistinctSlotsAndKindsRemainDistinct() {
        val same = "2026-09-28T10:00:00Z"
        val events = source(CardEventFaultKind.EVENTS, record(same, same), record(same, same, 1))
        val faults = source(CardEventFaultKind.FAULTS, record(same, same))
        val markers = CardDayEventFaultMarkers.forDate(date, zone, "own", events, faults)
        assertEquals(3, markers.size)
        assertEquals(3, markers.map { it.id }.distinct().size)
        assertTrue(markers.all { it.boundary == CardEventFaultBoundary.BEGIN_AND_END })
        assertEquals(listOf(CardEventFaultKind.EVENTS, CardEventFaultKind.EVENTS, CardEventFaultKind.FAULTS), markers.map { it.kind })
    }

    @Test fun repeatedAutumnLocalHourHasTwoDistinctPositionsOnA25HourDay() {
        val dst = LocalDate.of(2026, 10, 25)
        val events = source(CardEventFaultKind.EVENTS,
            record("2026-10-25T00:30:00Z", null), record("2026-10-25T01:30:00Z", null, 1))
        val markers = CardDayEventFaultMarkers.forDate(dst, zone, "own", events, null)
        assertEquals(2, markers.size)
        assertEquals(markers[0].time.atZone(zone).toLocalTime(), markers[1].time.atZone(zone).toLocalTime())
        assertNotEquals(markers[0].time.atZone(zone).offset, markers[1].time.atZone(zone).offset)
        val start = dst.atStartOfDay(zone).toInstant().toEpochMilli()
        val millis = Duration.between(dst.atStartOfDay(zone), dst.plusDays(1).atStartOfDay(zone)).toMillis().toDouble()
        assertEquals(25 * 3_600_000.0, millis, 0.0)
        assertEquals(2.5 / 25, CardDayEventFaultMarkers.position(markers[0], start, millis)!!, 1e-10)
        assertEquals(3.5 / 25, CardDayEventFaultMarkers.position(markers[1], start, millis)!!, 1e-10)
    }

    @Test fun springDayUses23HoursAndNextMidnightBelongsToTheNextDay() {
        val dst = LocalDate.of(2026, 3, 29)
        val start = dst.atStartOfDay(zone).toInstant()
        val end = dst.plusDays(1).atStartOfDay(zone).toInstant()
        val events = source(CardEventFaultKind.EVENTS, record(start.toString(), end.toString()))
        val markers = CardDayEventFaultMarkers.forDate(dst, zone, "own", events, null)
        assertEquals(1, markers.size)
        assertEquals(CardEventFaultBoundary.BEGIN, markers.single().boundary)
        assertEquals(0.0, CardDayEventFaultMarkers.position(markers.single(), start.toEpochMilli(), 23 * 3_600_000.0)!!, 0.0)
        val next = CardDayEventFaultMarkers.forDate(dst.plusDays(1), zone, "own", events, null).single()
        assertEquals(CardEventFaultBoundary.END, next.boundary)
        assertEquals(23 * 3600L, end.epochSecond - start.epochSecond)
    }

    @Test fun physicalCardIdentityAndFileKindPreventMixingAndOldSessionsStayEmpty() {
        val raw = record("2026-09-28T10:00:00Z", null)
        val own = source(CardEventFaultKind.EVENTS, raw)
        val replacement = source(CardEventFaultKind.FAULTS, raw, cardId = "replacement")
        assertTrue(CardDayEventFaultMarkers.forDate(date, zone, null, own, null).isEmpty())
        assertTrue(CardDayEventFaultMarkers.forDate(date, zone, "own", null, null).isEmpty())
        assertTrue(CardDayEventFaultMarkers.forDate(date, zone, "foreign", own, replacement).isEmpty())
        assertTrue(CardDayEventFaultMarkers.forDate(date, zone, "own", replacement, own).isEmpty())
        val partial = CardDayEventFaultMarkers.forDate(date, zone, "own", own, replacement)
        assertEquals(1, partial.size)
        assertEquals(CardEventFaultKind.EVENTS, partial.single().kind)
        assertTrue(CardDayEventFaultMarkers.forDate(date, zone, "own", source(CardEventFaultKind.EVENTS), null).isEmpty())
    }

    @Test fun zoomViewportExcludesOffscreenPointsInsteadOfClampingThemToTheEdges() {
        val marker = CardDayEventFaultMarkers.forDate(date, zone, "own",
            source(CardEventFaultKind.EVENTS, record("2026-09-28T10:00:15Z", null)), null).single()
        val start = Instant.parse("2026-09-28T10:00:00Z").toEpochMilli()
        assertEquals(0.25, CardDayEventFaultMarkers.position(marker, start, 60_000.0)!!, 0.0)
        assertNull(CardDayEventFaultMarkers.position(marker, start, 15_000.0))
        assertNull(CardDayEventFaultMarkers.position(marker, start + 16_000, 60_000.0))
        assertNull(CardDayEventFaultMarkers.position(marker, start, 0.0))
        assertNull(CardDayEventFaultMarkers.position(marker, start, Double.NaN))
    }
}
