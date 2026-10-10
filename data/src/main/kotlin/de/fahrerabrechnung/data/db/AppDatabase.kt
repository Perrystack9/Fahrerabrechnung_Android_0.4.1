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
        LocalDriverProfileEntity::class,
        DriverCardIdentityEntity::class,
        DriverImportSourcesEntity::class,
        DriverActivityDayEntity::class,
        DriverActivityChangeEntity::class,
        DriverActivityDaySourceEntity::class,
        DriverVehicleUseEntity::class,
        DriverVehicleUseSourceEntity::class,
        CardImportRunEntity::class,
        CardImportFileResultEntity::class,
        CardControlImportEntity::class,
        DriverImportControlSourceEntity::class,
        CardEventFaultImportEntity::class,
        DriverImportEventFaultSourceEntity::class,
        CardSpecificConditionImportEntity::class,
        DriverImportSpecificConditionSourceEntity::class,
        CardGnssImportEntity::class,
        DriverImportGnssSourceEntity::class,
        CardPlacesImportEntity::class,
        DriverImportPlacesSourceEntity::class,
        CardBorderCrossingsImportEntity::class,
        DriverImportBorderCrossingsSourceEntity::class,
        CardLoadUnloadImportEntity::class,
        DriverImportLoadUnloadSourceEntity::class,
        CardLoadTypeImportEntity::class,
        DriverImportLoadTypeSourceEntity::class,
    ],
    version = AppDatabase.SCHEMA_VERSION,
    exportSchema = true,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun profileDao(): ProfileDao
    abstract fun cardActivityImportDao(): CardActivityImportDao
    abstract fun cardVehicleImportDao(): CardVehicleImportDao

    abstract fun cardSourcePairDao(): CardSourcePairDao
    abstract fun driverProfileDao(): DriverProfileDao
    abstract fun driverActivityDao(): DriverActivityDao
    abstract fun driverVehicleUseDao(): DriverVehicleUseDao
    abstract fun cardImportRunDao(): CardImportRunDao
    abstract fun cardControlDao(): CardControlDao
    abstract fun cardEventFaultDao(): CardEventFaultDao
    abstract fun cardSpecificConditionDao(): CardSpecificConditionDao

    abstract fun cardGnssDao(): CardGnssDao
    abstract fun cardPlacesDao(): CardPlacesDao
    abstract fun cardBorderCrossingsDao(): CardBorderCrossingsDao
    abstract fun cardLoadUnloadDao(): CardLoadUnloadDao
    abstract fun cardLoadTypeDao(): CardLoadTypeDao

    companion object {
        const val SCHEMA_VERSION = 16
        const val DATABASE_NAME = "fahrerabrechnung.db"
    }
}
