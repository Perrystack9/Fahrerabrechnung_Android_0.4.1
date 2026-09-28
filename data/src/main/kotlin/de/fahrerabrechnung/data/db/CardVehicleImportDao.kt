package de.fahrerabrechnung.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface CardVehicleImportDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(snapshot: CardVehicleImportEntity): Long

    @Query("SELECT * FROM card_vehicle_imports ORDER BY importedAtEpochMillis DESC, id DESC LIMIT 1")
    suspend fun latest(): CardVehicleImportEntity?
}
