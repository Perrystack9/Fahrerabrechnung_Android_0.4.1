package de.fahrerabrechnung.data.db

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query

/** Immutable complete control EF per physical driver card, protected by SQLCipher. */
@Entity(tableName = "card_control_imports", foreignKeys = [
    ForeignKey(entity = DriverCardEntity::class, parentColumns = ["id"],
        childColumns = ["driverCardId"], onDelete = ForeignKey.RESTRICT),
], indices = [Index("driverCardId"), Index(value = ["id", "driverCardId"], unique = true)])
data class CardControlImportEntity(
    @PrimaryKey val id: String,
    val driverCardId: String,
    val generation: String,
    val bytes: ByteArray,
    val importedAtEpochMillis: Long,
)

/** Only a fresh accepted read may link this source to its exact own import session. */
@Entity(tableName = "driver_import_control_sources", foreignKeys = [
    ForeignKey(entity = DriverImportSourcesEntity::class, parentColumns = ["sessionId"],
        childColumns = ["sessionId"], onDelete = ForeignKey.RESTRICT),
    ForeignKey(entity = CardControlImportEntity::class, parentColumns = ["id", "driverCardId"],
        childColumns = ["controlId", "driverCardId"], onDelete = ForeignKey.RESTRICT),
], indices = [Index(value = ["controlId", "driverCardId"])])
data class DriverImportControlSourceEntity(
    @PrimaryKey val sessionId: String,
    val driverCardId: String,
    val controlId: String,
)

@Dao
interface CardControlDao {
    @Query("SELECT * FROM card_control_imports WHERE id = :id")
    suspend fun get(id: String): CardControlImportEntity?

    @Query("SELECT * FROM driver_import_control_sources WHERE sessionId = :id")
    suspend fun source(id: String): DriverImportControlSourceEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE) suspend fun insert(entity: CardControlImportEntity)
    @Insert suspend fun insertSource(entity: DriverImportControlSourceEntity)
}
