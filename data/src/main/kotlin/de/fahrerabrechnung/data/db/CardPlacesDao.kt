package de.fahrerabrechnung.data.db

import androidx.room.*

/** Independent daily-work-period place and authentication sources, scoped to physical card and session. */
@Entity(tableName = "card_place_imports", foreignKeys = [
    ForeignKey(entity = DriverCardEntity::class, parentColumns = ["id"],
        childColumns = ["driverCardId"], onDelete = ForeignKey.RESTRICT),
], indices = [Index("driverCardId"), Index(value = ["id", "driverCardId", "kind"], unique = true)])
data class CardPlacesImportEntity(
    @PrimaryKey val id: String, val driverCardId: String, val kind: String,
    val generation: String, val recordCount: Int, val bytes: ByteArray,
    val importedAtEpochMillis: Long,
)

@Entity(tableName = "driver_import_place_sources", primaryKeys = ["sessionId", "kind"], foreignKeys = [
    ForeignKey(entity = DriverImportSourcesEntity::class, parentColumns = ["sessionId"],
        childColumns = ["sessionId"], onDelete = ForeignKey.RESTRICT),
    ForeignKey(entity = CardPlacesImportEntity::class, parentColumns = ["id", "driverCardId", "kind"],
        childColumns = ["sourceId", "driverCardId", "kind"], onDelete = ForeignKey.RESTRICT),
], indices = [Index(value = ["sourceId", "driverCardId", "kind"])])
data class DriverImportPlacesSourceEntity(
    val sessionId: String, val kind: String, val driverCardId: String, val sourceId: String,
)

@Dao
interface CardPlacesDao {
    @Query("SELECT * FROM card_place_imports WHERE id = :id")
    suspend fun get(id: String): CardPlacesImportEntity?
    @Query("SELECT * FROM driver_import_place_sources WHERE sessionId = :id AND kind = :kind")
    suspend fun source(id: String, kind: String): DriverImportPlacesSourceEntity?
    @Insert(onConflict = OnConflictStrategy.IGNORE) suspend fun insert(entity: CardPlacesImportEntity)
    @Insert suspend fun insertSource(entity: DriverImportPlacesSourceEntity)
}
