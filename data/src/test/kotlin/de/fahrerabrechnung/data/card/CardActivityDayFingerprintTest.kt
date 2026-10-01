package de.fahrerabrechnung.data.card

import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class CardActivityDayFingerprintTest {
    private val first = ActivityChange(0, DriverActivity.REST, CardSlot.DRIVER, false, true)
    private val change = ActivityChange(90, DriverActivity.DRIVING, CardSlot.DRIVER, false, true)
    private val day = CardActivityDay(0, Instant.parse("2026-03-29T00:00:00Z"), "0001", 37,
        listOf(first, change))
    private fun id(value: CardActivityDay) = CardActivityDayFingerprint.id("synthetic-card-id", value)

    @Test fun shiftingTheCyclicFileDoesNotCreateAnotherDayContent() {
        assertEquals(id(day), id(day.copy(ringOffset = 729)))
    }

    @Test fun separatePhysicalCardsNeverShareDayIdentity() {
        assertNotEquals(id(day), CardActivityDayFingerprint.id("synthetic-replacement-id", day))
    }

    @Test fun changedMetadataAndEveryActivityFlagCreateAnotherVersion() {
        val alternatives = listOf(
            day.copy(recordDateUtc = day.recordDateUtc.plusSeconds(86400)),
            day.copy(dailyPresenceCounter = "0002"),
            day.copy(distanceKm = 38),
        ) + listOf(
            change.copy(minuteOfDay = 91), change.copy(activity = DriverActivity.WORK),
            change.copy(slot = CardSlot.CO_DRIVER), change.copy(crewDriving = true),
            change.copy(cardInserted = false),
        ).map { day.copy(changes = listOf(first, it)) }
        alternatives.forEach { assertNotEquals(id(day), id(it)) }
    }

    @Test fun changesAtTheSameMinuteKeepTheirSourceOrder() {
        val other = change.copy(activity = DriverActivity.WORK, cardInserted = false)
        assertNotEquals(id(day.copy(changes = listOf(first, change, other))),
            id(day.copy(changes = listOf(first, other, change))))
    }
}
