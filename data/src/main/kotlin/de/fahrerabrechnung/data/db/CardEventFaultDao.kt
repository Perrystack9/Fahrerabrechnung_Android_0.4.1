package de.fahrerabrechnung.data.db

import androidx.room.*

/** Only the two event/fault EFs; existing source tables are unchanged. */
@Entity(tableName = "card_event_fault_imports", foreignKeys = [
    ForeignKey(entity = DriverCardEntity::class, parentColumns = ["id"],
        childColumns = ["driverCardId"], onDelete = ForeignKey.RESTRICT),
], indices = [Index("driverCardId"), Index(value = ["id", "driverCardId", "kind"], unique = true)])
data class CardEventFaultImportEntity(
    @PrimaryKey val id: String, val driverCardId: String, val kind: String,
    val generation: String, val recordsPerType: Int, val bytes: ByteArray,
    val importedAtEpochMillis: Long,
)

@Entity(tableName = "driver_import_event_fault_sources", primaryKeys = ["sessionId", "kind"], foreignKeys = [
    ForeignKey(entity = DriverImportSourcesEntity::class, parentColumns = ["sessionId"],
        childColumns = ["sessionId"], onDelete = ForeignKey.RESTRICT),
    ForeignKey(entity = CardEventFaultImportEntity::class, parentColumns = ["id", "driverCardId", "kind"],
        childColumns = ["sourceId", "driverCardId", "kind"], onDelete = ForeignKey.RESTRICT),
], indices = [Index(value = ["sourceId", "driverCardId", "kind"])])
data class DriverImportEventFaultSourceEntity(
    val sessionId: String, val kind: String, val driverCardId: String, val sourceId: String,
)

@Dao
interface CardEventFaultDao {
    @Query("SELECT * FROM card_event_fault_imports WHERE id = :id")
    suspend fun get(id: String): CardEventFaultImportEntity?
    @Query("SELECT * FROM driver_import_event_fault_sources WHERE sessionId = :id AND kind = :kind")
    suspend fun source(id: String, kind: String): DriverImportEventFaultSourceEntity?
    @Insert(onConflict = OnConflictStrategy.IGNORE) suspend fun insert(entity: CardEventFaultImportEntity)
    @Insert suspend fun insertSource(entity: DriverImportEventFaultSourceEntity)
}
