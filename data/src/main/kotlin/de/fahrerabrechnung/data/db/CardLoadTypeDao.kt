package de.fahrerabrechnung.data.db

import androidx.room.*

@Entity(tableName = "card_load_type_imports", foreignKeys = [
    ForeignKey(entity = DriverCardEntity::class, parentColumns = ["id"],
        childColumns = ["driverCardId"], onDelete = ForeignKey.RESTRICT),
], indices = [Index("driverCardId"), Index(value = ["id", "driverCardId"], unique = true)])
data class CardLoadTypeImportEntity(
    @PrimaryKey val id: String, val driverCardId: String, val generation: String,
    val recordCount: Int, val bytes: ByteArray, val importedAtEpochMillis: Long,
)

@Entity(tableName = "driver_import_load_type_sources", primaryKeys = ["sessionId"], foreignKeys = [
    ForeignKey(entity = DriverImportSourcesEntity::class, parentColumns = ["sessionId"],
        childColumns = ["sessionId"], onDelete = ForeignKey.RESTRICT),
    ForeignKey(entity = CardLoadTypeImportEntity::class, parentColumns = ["id", "driverCardId"],
        childColumns = ["sourceId", "driverCardId"], onDelete = ForeignKey.RESTRICT),
], indices = [Index(value = ["sourceId", "driverCardId"])])
data class DriverImportLoadTypeSourceEntity(
    val sessionId: String, val driverCardId: String, val sourceId: String,
)

@Dao
interface CardLoadTypeDao {
    @Query("SELECT * FROM card_load_type_imports WHERE id = :id")
    suspend fun get(id: String): CardLoadTypeImportEntity?
    @Query("SELECT * FROM driver_import_load_type_sources WHERE sessionId = :sessionId")
    suspend fun source(sessionId: String): DriverImportLoadTypeSourceEntity?
    @Insert(onConflict = OnConflictStrategy.IGNORE) suspend fun insert(entity: CardLoadTypeImportEntity)
    @Insert suspend fun insertSource(entity: DriverImportLoadTypeSourceEntity)
}
