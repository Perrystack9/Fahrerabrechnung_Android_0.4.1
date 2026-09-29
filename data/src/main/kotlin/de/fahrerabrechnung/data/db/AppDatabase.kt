package de.fahrerabrechnung.data.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        ProfileEntity::class,
        ConfigurationVersionEntity::class,
        DriverCardEntity::class,
        VehicleEntity::class,
        ImportSessionEntity::class,
        CardActivityImportEntity::class,
        CardVehicleImportEntity::class,
        CardSourcePairEntity::class,
    ],
    version = AppDatabase.SCHEMA_VERSION,
    exportSchema = true,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun profileDao(): ProfileDao
    abstract fun cardActivityImportDao(): CardActivityImportDao
    abstract fun cardVehicleImportDao(): CardVehicleImportDao

    abstract fun cardSourcePairDao(): CardSourcePairDao

    companion object {
        const val SCHEMA_VERSION = 4
        const val DATABASE_NAME = "fahrerabrechnung.db"
    }
}
