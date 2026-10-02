package de.fahrerabrechnung.data.db

import androidx.room.Dao
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query

/** Immutable content of a UTC card day. Different versions of one date remain separate. */
@Entity(tableName = "driver_activity_days", foreignKeys = [
    ForeignKey(entity = DriverCardEntity::class, parentColumns = ["id"],
        childColumns = ["driverCardId"], onDelete = ForeignKey.RESTRICT),
], indices = [Index(value = ["driverCardId", "recordDateUtcEpochSeconds"]),
    Index(value = ["id", "driverCardId"], unique = true)])
data class DriverActivityDayEntity(
    @PrimaryKey val id: String,
    val driverCardId: String,
    val recordDateUtcEpochSeconds: Long,
    val dailyPresenceCounter: String,
    val distanceKm: Int,
    val changeCount: Int,
)

@Entity(tableName = "driver_activity_changes", primaryKeys = ["dayId", "changeIndex"],
    foreignKeys = [ForeignKey(entity = DriverActivityDayEntity::class,
        parentColumns = ["id"], childColumns = ["dayId"], onDelete = ForeignKey.RESTRICT)])
data class DriverActivityChangeEntity(
    val dayId: String,
    val changeIndex: Int,
    val minuteOfDay: Int,
    val activity: String,
    val slot: String,
    val crewDriving: Boolean,
    val cardInserted: Boolean,
)

/** Exact source order and ring position, scoped to the separately bound physical card. */
@Entity(tableName = "driver_activity_day_sources",
    primaryKeys = ["driverCardId", "activityId", "recordIndex"], foreignKeys = [
        ForeignKey(entity = DriverActivityDayEntity::class, parentColumns = ["id", "driverCardId"],
            childColumns = ["dayId", "driverCardId"], onDelete = ForeignKey.RESTRICT),
        ForeignKey(entity = CardActivityImportEntity::class, parentColumns = ["id"],
            childColumns = ["activityId"], onDelete = ForeignKey.RESTRICT),
    ], indices = [Index("activityId"), Index(value = ["dayId", "driverCardId"])])
data class DriverActivityDaySourceEntity(
    val driverCardId: String,
    val activityId: String,
    val recordIndex: Int,
    val dayId: String,
    val ringOffset: Int,
)

data class StoredActivitySourceDay(
    @Embedded val day: DriverActivityDayEntity,
    val recordIndex: Int,
    val ringOffset: Int,
)

@Dao
interface DriverActivityDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertDay(day: DriverActivityDayEntity): Long

    @Insert suspend fun insertChanges(changes: List<DriverActivityChangeEntity>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertSource(source: DriverActivityDaySourceEntity): Long

    @Query("SELECT d.*, s.recordIndex, s.ringOffset FROM driver_activity_day_sources s JOIN driver_activity_days d ON d.id = s.dayId AND d.driverCardId = s.driverCardId WHERE s.driverCardId = :cardId AND s.activityId = :activityId ORDER BY s.recordIndex")
    suspend fun sourceDays(cardId: String, activityId: String): List<StoredActivitySourceDay>

    @Query("SELECT * FROM driver_activity_changes WHERE dayId IN (SELECT dayId FROM driver_activity_day_sources WHERE driverCardId = :cardId AND activityId = :activityId) ORDER BY dayId, changeIndex")
    suspend fun sourceChanges(cardId: String, activityId: String): List<DriverActivityChangeEntity>
}
