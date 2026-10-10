package de.fahrerabrechnung.data.db

import androidx.room.*

@Entity(tableName = "card_load_unload_imports", foreignKeys = [
    ForeignKey(entity = DriverCardEntity::class, parentColumns = ["id"],
        childColumns = ["driverCardId"], onDelete = ForeignKey.RESTRICT),
], indices = [Index("driverCardId"), Index(value = ["id", "driverCardId"], unique = true)])
data class CardLoadUnloadImportEntity(
    @PrimaryKey val id: String, val driverCardId: String, val generation: String,
    val recordCount: Int, val bytes: ByteArray, val importedAtEpochMillis: Long,
)

@Entity(tableName = "driver_import_load_unload_sources", primaryKeys = ["sessionId"], foreignKeys = [
    ForeignKey(entity = DriverImportSourcesEntity::class, parentColumns = ["sessionId"],
        childColumns = ["sessionId"], onDelete = ForeignKey.RESTRICT),
    ForeignKey(entity = CardLoadUnloadImportEntity::class, parentColumns = ["id", "driverCardId"],
        childColumns = ["sourceId", "driverCardId"], onDelete = ForeignKey.RESTRICT),
], indices = [Index(value = ["sourceId", "driverCardId"])])
data class DriverImportLoadUnloadSourceEntity(
    val sessionId: String, val driverCardId: String, val sourceId: String,
)

@Dao
interface CardLoadUnloadDao {
    @Query("SELECT * FROM card_load_unload_imports WHERE id = :id")
    suspend fun get(id: String): CardLoadUnloadImportEntity?
    @Query("SELECT * FROM driver_import_load_unload_sources WHERE sessionId = :sessionId")
    suspend fun source(sessionId: String): DriverImportLoadUnloadSourceEntity?
    @Insert(onConflict = OnConflictStrategy.IGNORE) suspend fun insert(entity: CardLoadUnloadImportEntity)
    @Insert suspend fun insertSource(entity: DriverImportLoadUnloadSourceEntity)
}
