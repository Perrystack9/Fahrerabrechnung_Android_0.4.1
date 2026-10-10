package de.fahrerabrechnung.data.db

import androidx.room.*

@Entity(tableName = "card_vehicle_units_imports", foreignKeys = [
    ForeignKey(entity = DriverCardEntity::class, parentColumns = ["id"],
        childColumns = ["driverCardId"], onDelete = ForeignKey.RESTRICT),
], indices = [Index("driverCardId"), Index(value = ["id", "driverCardId"], unique = true)])
data class CardVehicleUnitsImportEntity(
    @PrimaryKey val id: String, val driverCardId: String, val generation: String,
    val recordCount: Int, val bytes: ByteArray, val importedAtEpochMillis: Long,
)

@Entity(tableName = "driver_import_vehicle_units_sources", primaryKeys = ["sessionId"], foreignKeys = [
    ForeignKey(entity = DriverImportSourcesEntity::class, parentColumns = ["sessionId"],
        childColumns = ["sessionId"], onDelete = ForeignKey.RESTRICT),
    ForeignKey(entity = CardVehicleUnitsImportEntity::class, parentColumns = ["id", "driverCardId"],
        childColumns = ["sourceId", "driverCardId"], onDelete = ForeignKey.RESTRICT),
], indices = [Index(value = ["sourceId", "driverCardId"])])
data class DriverImportVehicleUnitsSourceEntity(
    val sessionId: String, val driverCardId: String, val sourceId: String,
)

@Dao
interface CardVehicleUnitsDao {
    @Query("SELECT * FROM card_vehicle_units_imports WHERE id = :id")
    suspend fun get(id: String): CardVehicleUnitsImportEntity?
    @Query("SELECT * FROM driver_import_vehicle_units_sources WHERE sessionId = :sessionId")
    suspend fun source(sessionId: String): DriverImportVehicleUnitsSourceEntity?
    @Insert(onConflict = OnConflictStrategy.IGNORE) suspend fun insert(entity: CardVehicleUnitsImportEntity)
    @Insert suspend fun insertSource(entity: DriverImportVehicleUnitsSourceEntity)
}
