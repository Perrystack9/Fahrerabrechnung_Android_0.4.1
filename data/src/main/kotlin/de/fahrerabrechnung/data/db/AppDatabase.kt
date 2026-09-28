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
    ],
    version = AppDatabase.SCHEMA_VERSION,
    exportSchema = true,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun profileDao(): ProfileDao
    abstract fun cardActivityImportDao(): CardActivityImportDao

    companion object {
        const val SCHEMA_VERSION = 2
        const val DATABASE_NAME = "fahrerabrechnung.db"
    }
}
