package de.fahrerabrechnung.data.card

import org.junit.Assert.*
import org.junit.Test
import java.time.Instant

class CardVehicleHistoryTest {
    private fun use(index: Int, first: Long, end: Long?, plate: String, vin: String? = null) =
        CardVehicleUse(index, Instant.ofEpochSecond(first), end?.let(Instant::ofEpochSecond),
            13, plate, vin, 100, end?.let { 200 }, 0)

    private fun snapshot(time: Long, vararg uses: CardVehicleUse) = SavedCardVehicles(
        CardVehiclesFile(0, 1, CardGeneration.GEN2V2, ByteArray(48)),
        uses.toList(), time, "card-$time")

    @Test fun repeatedSnapshotsCollapseTheSameUseAndKeepCompletedObservation() {
        val open = use(0, 1_700_000_000, null, "NÖ NR 6600", "WDB12345678901234")
        val complete = use(1, 1_700_000_000, 1_700_001_000, "NÖ NR 6600", open.vin)
        val next = use(2, 1_700_086_400, 1_700_087_400, "NÖ NR 6600", open.vin)
        val history = CardVehicleHistory.build(listOf(snapshot(1, open),
            snapshot(2, complete, next), snapshot(3, open)))
        assertEquals(1, history.size)
        assertEquals(2, history.single().uses.size)
        assertEquals(complete.lastUseUtc, history.single().uses.last().lastUseUtc)
        assertEquals(next.lastUseUtc, history.single().lastObservedUtc)
    }

    @Test fun matchingVinGroupsPlateChangesButUnknownVinUsesNationAndPlate() {
        val vin = "WDB12345678901234"
        val withVin = CardVehicleHistory.build(listOf(snapshot(1,
            use(0, 1_700_000_000, 1_700_000_300, "AB 123", vin),
            use(1, 1_700_086_400, 1_700_086_700, "CD 456", vin))))
        assertEquals(1, withVin.size)
        val plates = CardVehicleHistory.build(listOf(snapshot(1,
            use(0, 1_700_000_000, 1_700_000_300, "AB 123"),
            use(1, 1_700_086_400, 1_700_086_700, "CD 456"))))
        assertEquals(2, plates.size)
    }
}
