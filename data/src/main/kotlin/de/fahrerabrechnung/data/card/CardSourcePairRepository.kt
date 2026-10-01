package de.fahrerabrechnung.data.card

import androidx.room.withTransaction
import de.fahrerabrechnung.data.db.AppDatabase
import de.fahrerabrechnung.data.db.CardSourcePairEntity
import java.time.Clock
import javax.inject.Inject

data class SavedCardSourcePair(val id: String, val activity: SavedCardActivity,
                              val vehicles: SavedCardVehicles, val savedAtEpochMillis: Long)

/** A partial import only. Existing standalone snapshots are not retrospectively paired. */
class CardSourcePairRepository @Inject constructor(
    private val database: AppDatabase,
    private val activities: CardActivityImportRepository,
    private val vehicles: CardVehicleImportRepository,
    private val clock: Clock,
) {
    suspend fun save(result: CardReadResult): Boolean {
        require(!result.vehicleReadFailed && result.card?.generation == result.generation)
        val now = clock.millis()
        // Validate and copy both complete files before the first database mutation.
        val activity = activities.prepare(result, now)
        val vehicle = vehicles.prepare(result, now)
        return storePrepared(activity, vehicle, now)
    }

    internal suspend fun storePrepared(activity: de.fahrerabrechnung.data.db.CardActivityImportEntity,
                                       vehicle: de.fahrerabrechnung.data.db.CardVehicleImportEntity,
                                       savedAt: Long): Boolean = database.withTransaction {
            val pair = CardSourcePairEntity("${activity.id}:${vehicle.id}", activity.id, vehicle.id, savedAt)
            database.cardActivityImportDao().insert(activity)
            database.cardVehicleImportDao().insert(vehicle)
            database.cardSourcePairDao().insert(pair) != -1L
    }

    suspend fun all(): List<SavedCardSourcePair> = database.withTransaction {
        database.cardSourcePairDao().all().map { pair ->
            val activity = activities.decode(checkNotNull(database.cardActivityImportDao().getById(pair.activityId)))
            val vehicle = vehicles.decode(checkNotNull(database.cardVehicleImportDao().getById(pair.vehicleId)))
            check(activity.cardNumber == vehicle.cardNumber && activity.generation == vehicle.file.generation)
            SavedCardSourcePair(pair.id, activity, vehicle, pair.savedAtEpochMillis)
        }
    }
}
