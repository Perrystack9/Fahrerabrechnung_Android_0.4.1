package de.fahrerabrechnung.data.db

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query

/** Provenance for two explicitly saved files from one actual read, never inferred from dates. */
@Entity(tableName = "card_source_pairs", foreignKeys = [
    ForeignKey(entity = CardActivityImportEntity::class, parentColumns = ["id"],
        childColumns = ["activityId"], onDelete = ForeignKey.RESTRICT),
    ForeignKey(entity = CardVehicleImportEntity::class, parentColumns = ["id"],
        childColumns = ["vehicleId"], onDelete = ForeignKey.RESTRICT),
], indices = [Index("activityId"), Index("vehicleId"), Index("savedAtEpochMillis")])
data class CardSourcePairEntity(
    @PrimaryKey val id: String,
    val activityId: String,
    val vehicleId: String,
    val savedAtEpochMillis: Long,
)

@Dao
interface CardSourcePairDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(pair: CardSourcePairEntity): Long

    @Query("SELECT * FROM card_source_pairs ORDER BY savedAtEpochMillis DESC, id DESC")
    suspend fun all(): List<CardSourcePairEntity>

    @Query("SELECT * FROM card_source_pairs WHERE id = :id")
    suspend fun getById(id: String): CardSourcePairEntity?
}
