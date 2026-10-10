package de.fahrerabrechnung.data.db

import androidx.room.*

@Entity(tableName = "card_border_crossing_imports", foreignKeys = [
    ForeignKey(entity = DriverCardEntity::class, parentColumns = ["id"],
        childColumns = ["driverCardId"], onDelete = ForeignKey.RESTRICT),
], indices = [Index("driverCardId"), Index(value = ["id", "driverCardId"], unique = true)])
data class CardBorderCrossingsImportEntity(
    @PrimaryKey val id: String, val driverCardId: String, val generation: String,
    val recordCount: Int, val bytes: ByteArray, val importedAtEpochMillis: Long,
)

@Entity(tableName = "driver_import_border_crossing_sources", primaryKeys = ["sessionId"], foreignKeys = [
    ForeignKey(entity = DriverImportSourcesEntity::class, parentColumns = ["sessionId"],
        childColumns = ["sessionId"], onDelete = ForeignKey.RESTRICT),
    ForeignKey(entity = CardBorderCrossingsImportEntity::class, parentColumns = ["id", "driverCardId"],
        childColumns = ["sourceId", "driverCardId"], onDelete = ForeignKey.RESTRICT),
], indices = [Index(value = ["sourceId", "driverCardId"])])
data class DriverImportBorderCrossingsSourceEntity(
    val sessionId: String, val driverCardId: String, val sourceId: String,
)

@Dao
interface CardBorderCrossingsDao {
    @Query("SELECT * FROM card_border_crossing_imports WHERE id = :id")
    suspend fun get(id: String): CardBorderCrossingsImportEntity?
    @Query("SELECT * FROM driver_import_border_crossing_sources WHERE sessionId = :sessionId")
    suspend fun source(sessionId: String): DriverImportBorderCrossingsSourceEntity?
    @Insert(onConflict = OnConflictStrategy.IGNORE) suspend fun insert(entity: CardBorderCrossingsImportEntity)
    @Insert suspend fun insertSource(entity: DriverImportBorderCrossingsSourceEntity)
}
