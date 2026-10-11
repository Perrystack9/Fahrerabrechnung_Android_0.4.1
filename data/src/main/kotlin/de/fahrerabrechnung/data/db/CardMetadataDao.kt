package de.fahrerabrechnung.data.db

import androidx.room.*

@Entity(tableName = "card_metadata_imports", foreignKeys = [
    ForeignKey(entity = DriverCardEntity::class, parentColumns = ["id"],
        childColumns = ["driverCardId"], onDelete = ForeignKey.RESTRICT),
], indices = [Index("driverCardId"), Index(value = ["id", "driverCardId", "kind"], unique = true)])
data class CardMetadataImportEntity(
    @PrimaryKey val id: String, val driverCardId: String, val kind: String,
    val generation: String, val bytes: ByteArray, val importedAtEpochMillis: Long,
)

@Entity(tableName = "driver_import_metadata_sources", primaryKeys = ["sessionId", "kind"], foreignKeys = [
    ForeignKey(entity = DriverImportSourcesEntity::class, parentColumns = ["sessionId"],
        childColumns = ["sessionId"], onDelete = ForeignKey.RESTRICT),
    ForeignKey(entity = CardMetadataImportEntity::class, parentColumns = ["id", "driverCardId", "kind"],
        childColumns = ["sourceId", "driverCardId", "kind"], onDelete = ForeignKey.RESTRICT),
], indices = [Index(value = ["sourceId", "driverCardId", "kind"])])
data class DriverImportMetadataSourceEntity(
    val sessionId: String, val kind: String, val driverCardId: String, val sourceId: String,
)

@Dao
interface CardMetadataDao {
    @Query("SELECT * FROM card_metadata_imports WHERE id = :id")
    suspend fun get(id: String): CardMetadataImportEntity?
    @Query("SELECT * FROM driver_import_metadata_sources WHERE sessionId = :sessionId")
    suspend fun sources(sessionId: String): List<DriverImportMetadataSourceEntity>
    @Insert(onConflict = OnConflictStrategy.IGNORE) suspend fun insert(entity: CardMetadataImportEntity)
    @Insert suspend fun insertSource(entity: DriverImportMetadataSourceEntity)
}
