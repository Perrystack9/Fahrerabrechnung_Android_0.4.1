package de.fahrerabrechnung.data.card

import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class CardVehicleUseFingerprintTest {
    private val use = CardVehicleUse(0, Instant.parse("2026-10-01T10:00:00Z"),
        Instant.parse("2026-10-01T12:00:00Z"), 13, "TEST OWN", "WDB12345678901234", 100, 137, 1)
    private fun id(value: CardVehicleUse) = CardVehicleUseFingerprint.id("synthetic-card-id", value)

    @Test fun movingToAnotherRingSlotKeepsTheSameUseContent() {
        assertEquals(id(use), id(use.copy(ringIndex = 173)))
    }

    @Test fun replacementCardsKeepSeparateContentIdentityEvenForIdenticalUses() {
        assertNotEquals(id(use), CardVehicleUseFingerprint.id("synthetic-replacement-id", use))
    }

    @Test fun everySourceFieldRetainsItsOwnContentVersion() {
        val alternatives = listOf(
            use.copy(firstUseUtc = use.firstUseUtc.plusSeconds(1)),
            use.copy(lastUseUtc = use.lastUseUtc!!.plusSeconds(1)),
            use.copy(registrationNation = 14),
            use.copy(registrationNumber = "TEST OTHER"),
            use.copy(vin = "WDB12345678901235"),
            use.copy(odometerBeginKm = 101),
            use.copy(odometerEndKm = 138),
            use.copy(vuDataBlockCounter = 2),
        )
        alternatives.forEach { assertNotEquals(id(use), id(it)) }
        assertEquals(alternatives.size, alternatives.map(::id).distinct().size)
    }

    @Test fun absentSourceValuesRemainDifferentFromPresentValues() {
        assertNotEquals(id(use), id(use.copy(lastUseUtc = null)))
        assertNotEquals(id(use), id(use.copy(odometerEndKm = null)))
        assertNotEquals(id(use.copy(odometerEndKm = null)), id(use.copy(odometerEndKm = 0)))
        assertNotEquals(id(use.copy(vin = null)), id(use.copy(vin = "")))
    }

    @Test fun decodedTextIsKeptExactlyWithoutGuessingOrNormalizingVehicleIdentity() {
        assertNotEquals(id(use), id(use.copy(registrationNumber = "test own")))
        assertNotEquals(id(use), id(use.copy(registrationNumber = "TESTOWN")))
        assertNotEquals(id(use.copy(registrationNumber = "TEST Ä")), id(use.copy(registrationNumber = "TEST A")))
    }
}
