package de.fahrerabrechnung.data.db

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object DatabaseMigrations {
    val MIGRATION_3_4 = object : Migration(3, 4) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("""CREATE TABLE IF NOT EXISTS `card_source_pairs` (`id` TEXT NOT NULL, `activityId` TEXT NOT NULL, `vehicleId` TEXT NOT NULL, `savedAtEpochMillis` INTEGER NOT NULL, PRIMARY KEY(`id`), FOREIGN KEY(`activityId`) REFERENCES `card_activity_imports`(`id`) ON UPDATE NO ACTION ON DELETE RESTRICT, FOREIGN KEY(`vehicleId`) REFERENCES `card_vehicle_imports`(`id`) ON UPDATE NO ACTION ON DELETE RESTRICT)""")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_card_source_pairs_activityId` ON `card_source_pairs` (`activityId`)")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_card_source_pairs_vehicleId` ON `card_source_pairs` (`vehicleId`)")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_card_source_pairs_savedAtEpochMillis` ON `card_source_pairs` (`savedAtEpochMillis`)")
        }
    }
    val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("""CREATE TABLE IF NOT EXISTS `card_activity_imports` (`id` TEXT NOT NULL, `cardNumber` TEXT NOT NULL, `generation` TEXT NOT NULL, `oldestDayOffset` INTEGER NOT NULL, `newestDayOffset` INTEGER NOT NULL, `dailyRecords` BLOB NOT NULL, `dayCount` INTEGER NOT NULL, `importedAtEpochMillis` INTEGER NOT NULL, PRIMARY KEY(`id`))""")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_card_activity_imports_importedAtEpochMillis` ON `card_activity_imports` (`importedAtEpochMillis`)")
        }
    }
    val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("""CREATE TABLE IF NOT EXISTS `card_vehicle_imports` (`id` TEXT NOT NULL, `cardNumber` TEXT NOT NULL, `generation` TEXT NOT NULL, `newestRecordIndex` INTEGER NOT NULL, `recordCount` INTEGER NOT NULL, `records` BLOB NOT NULL, `useCount` INTEGER NOT NULL, `importedAtEpochMillis` INTEGER NOT NULL, PRIMARY KEY(`id`))""")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_card_vehicle_imports_importedAtEpochMillis` ON `card_vehicle_imports` (`importedAtEpochMillis`)")
        }
    }
}
