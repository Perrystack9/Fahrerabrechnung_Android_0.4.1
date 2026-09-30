package de.fahrerabrechnung.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface CardActivityImportDao {
    @Query("SELECT * FROM card_activity_imports WHERE id = :id")
    suspend fun getById(id: String): CardActivityImportEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(snapshot: CardActivityImportEntity): Long

    @Query("SELECT * FROM card_activity_imports ORDER BY importedAtEpochMillis DESC, id DESC LIMIT 1")
    suspend fun latest(): CardActivityImportEntity?

    @Query("SELECT * FROM card_activity_imports ORDER BY importedAtEpochMillis DESC, id DESC")
    suspend fun all(): List<CardActivityImportEntity>
}
