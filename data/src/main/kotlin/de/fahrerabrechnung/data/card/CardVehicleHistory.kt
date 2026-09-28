package de.fahrerabrechnung.data.card

import java.time.Instant
import java.util.Locale

data class CardVehicleHistoryEntry(
    val registrationNation: Int,
    val registrationNumber: String,
    val vin: String?,
    val firstUseUtc: Instant,
    val lastObservedUtc: Instant,
    val uses: List<CardVehicleUse>,
)

/** Read-only view across saved source snapshots, without summing odometers. */
object CardVehicleHistory {
    fun build(snapshots: List<SavedCardVehicles>): List<CardVehicleHistoryEntry> {
        val unique = linkedMapOf<Pair<String, Instant>, CardVehicleUse>()
        snapshots.sortedBy { it.importedAtEpochMillis }.forEach { snapshot ->
            snapshot.uses.forEach { use ->
                val key = identity(use) to use.firstUseUtc
                val previous = unique[key]
                // An earlier completed observation must not be replaced by a later open record.
                if (previous == null || use.lastUseUtc != null || previous.lastUseUtc == null)
                    unique[key] = use
            }
        }
        return unique.values.groupBy(::identity).values.map { records ->
            val chronological = records.sortedBy { it.firstUseUtc }
            val latest = chronological.last()
            CardVehicleHistoryEntry(latest.registrationNation, latest.registrationNumber,
                validVin(latest.vin), chronological.first().firstUseUtc,
                chronological.maxOf { it.lastUseUtc ?: it.firstUseUtc },
                chronological.asReversed())
        }.sortedByDescending { it.lastObservedUtc }
    }

    private fun identity(use: CardVehicleUse): String = validVin(use.vin)?.let { "vin:$it" }
        ?: use.registrationNumber.trim().uppercase(Locale.ROOT).takeIf { it.isNotEmpty() }
            ?.let { "plate:${use.registrationNation}:$it" }
        ?: "unverified-vin:${use.vin?.trim()}:${use.firstUseUtc}"

    private fun validVin(vin: String?): String? = vin?.trim()?.uppercase(Locale.ROOT)
        ?.takeIf { it.matches(Regex("[A-HJ-NPR-Z0-9]{17}")) }
}
