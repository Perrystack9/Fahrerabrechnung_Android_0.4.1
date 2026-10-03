package de.fahrerabrechnung.data.db

import androidx.room.*

@Entity(tableName = "card_specific_condition_imports", foreignKeys = [
    ForeignKey(entity = DriverCardEntity::class, parentColumns = ["id"],
        childColumns = ["driverCardId"], onDelete = ForeignKey.RESTRICT),
], indices = [Index("driverCardId"), Index(value = ["id", "driverCardId"], unique = true)])
data class CardSpecificConditionImportEntity(
    @PrimaryKey val id: String, val driverCardId: String, val generation: String,
    val recordCount: Int, val bytes: ByteArray, val importedAtEpochMillis: Long,
)

@Entity(tableName = "driver_import_specific_condition_sources", primaryKeys = ["sessionId"], foreignKeys = [
    ForeignKey(entity = DriverImportSourcesEntity::class, parentColumns = ["sessionId"],
        childColumns = ["sessionId"], onDelete = ForeignKey.RESTRICT),
    ForeignKey(entity = CardSpecificConditionImportEntity::class, parentColumns = ["id", "driverCardId"],
        childColumns = ["sourceId", "driverCardId"], onDelete = ForeignKey.RESTRICT),
], indices = [Index(value = ["sourceId", "driverCardId"])])
data class DriverImportSpecificConditionSourceEntity(
    val sessionId: String, val driverCardId: String, val sourceId: String,
)

@Dao
interface CardSpecificConditionDao {
    @Query("SELECT * FROM card_specific_condition_imports WHERE id = :id")
    suspend fun get(id: String): CardSpecificConditionImportEntity?
    @Query("SELECT * FROM driver_import_specific_condition_sources WHERE sessionId = :sessionId")
    suspend fun source(sessionId: String): DriverImportSpecificConditionSourceEntity?
    @Insert(onConflict = OnConflictStrategy.IGNORE) suspend fun insert(entity: CardSpecificConditionImportEntity)
    @Insert suspend fun insertSource(entity: DriverImportSpecificConditionSourceEntity)
}
