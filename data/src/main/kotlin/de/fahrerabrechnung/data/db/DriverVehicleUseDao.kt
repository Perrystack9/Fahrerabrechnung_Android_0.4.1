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

/** Immutable source use, scoped to a physical card; this is not a resolved vehicle entity. */
@Entity(tableName = "driver_vehicle_uses", foreignKeys = [
    ForeignKey(entity = DriverCardEntity::class, parentColumns = ["id"],
        childColumns = ["driverCardId"], onDelete = ForeignKey.RESTRICT),
], indices = [Index(value = ["driverCardId", "firstUseUtcEpochSeconds"]),
    Index(value = ["id", "driverCardId"], unique = true)])
data class DriverVehicleUseEntity(
    @PrimaryKey val id: String,
    val driverCardId: String,
    val firstUseUtcEpochSeconds: Long,
    val lastUseUtcEpochSeconds: Long?,
    val registrationNation: Int,
    val registrationNumber: String,
    val vin: String?,
    val odometerBeginKm: Int,
    val odometerEndKm: Int?,
    val vuDataBlockCounter: Int,
)

/** Exact decoded order and original ring slot, without guessing an assignment for old files. */
@Entity(tableName = "driver_vehicle_use_sources",
    primaryKeys = ["driverCardId", "vehicleId", "recordIndex"], foreignKeys = [
        ForeignKey(entity = DriverVehicleUseEntity::class, parentColumns = ["id", "driverCardId"],
            childColumns = ["useId", "driverCardId"], onDelete = ForeignKey.RESTRICT),
        ForeignKey(entity = CardVehicleImportEntity::class, parentColumns = ["id"],
            childColumns = ["vehicleId"], onDelete = ForeignKey.RESTRICT),
    ], indices = [Index("vehicleId"), Index(value = ["useId", "driverCardId"])])
data class DriverVehicleUseSourceEntity(
    val driverCardId: String,
    val vehicleId: String,
    val recordIndex: Int,
    val useId: String,
    val ringIndex: Int,
)

data class StoredVehicleSourceUse(
    @Embedded val use: DriverVehicleUseEntity,
    val recordIndex: Int,
    val ringIndex: Int,
)

@Dao
interface DriverVehicleUseDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertUse(use: DriverVehicleUseEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertSource(source: DriverVehicleUseSourceEntity): Long

    @Query("SELECT u.*, s.recordIndex, s.ringIndex FROM driver_vehicle_use_sources s JOIN driver_vehicle_uses u ON u.id = s.useId AND u.driverCardId = s.driverCardId WHERE s.driverCardId = :cardId AND s.vehicleId = :vehicleId ORDER BY s.recordIndex")
    suspend fun sourceUses(cardId: String, vehicleId: String): List<StoredVehicleSourceUse>
}
