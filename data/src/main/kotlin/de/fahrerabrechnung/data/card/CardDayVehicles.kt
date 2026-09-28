package de.fahrerabrechnung.data.card

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

/** Vehicle source intervals clipped to the selected local day. Unknown ends stay points. */
data class CardDayVehicle(val use: CardVehicleUse, val start: Instant,
                          val end: Instant?, val beginsHere: Boolean)

object CardDayVehicles {
    fun forDate(date: LocalDate, zone: ZoneId, uses: List<CardVehicleUse>): List<CardDayVehicle> {
        val dayStart = date.atStartOfDay(zone).toInstant()
        val dayEnd = date.plusDays(1).atStartOfDay(zone).toInstant()
        return uses.mapNotNull { use ->
            val begin = use.firstUseUtc
            val end = use.lastUseUtc
            when {
                // Open ends are not proof that the vehicle remained in use beyond this point.
                end == null && begin >= dayStart && begin < dayEnd ->
                    CardDayVehicle(use, begin, null, true)
                end == null || end <= dayStart || begin >= dayEnd || end <= begin -> null
                else -> CardDayVehicle(use, maxOf(begin, dayStart), minOf(end, dayEnd),
                    begin >= dayStart)
            }
        }.sortedWith(compareBy<CardDayVehicle> { it.start }.thenBy { it.use.ringIndex })
    }

    fun at(time: Instant, vehicles: List<CardDayVehicle>): CardVehicleUse? =
        vehicles.filter { it.end != null && time >= it.start && time < it.end }
            .singleOrNull()?.use
}
