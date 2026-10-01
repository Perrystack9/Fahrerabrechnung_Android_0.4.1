package de.fahrerabrechnung.data.db

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object DatabaseMigrations {
    val MIGRATION_4_5 = object : Migration(4, 5) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("""CREATE TABLE IF NOT EXISTS `local_driver_profile` (`id` INTEGER NOT NULL, `profileId` TEXT NOT NULL, PRIMARY KEY(`id`), FOREIGN KEY(`profileId`) REFERENCES `profiles`(`id`) ON UPDATE NO ACTION ON DELETE RESTRICT)""")
            db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_local_driver_profile_profileId` ON `local_driver_profile` (`profileId`)")
            db.execSQL("""CREATE TABLE IF NOT EXISTS `driver_card_identities` (`cardId` TEXT NOT NULL, `cardKey` TEXT NOT NULL, `cardNumber` TEXT NOT NULL, `generation` TEXT NOT NULL, `issuingState` INTEGER NOT NULL, `issuedEpochSeconds` INTEGER NOT NULL, `validFromEpochSeconds` INTEGER NOT NULL, `expiresEpochSeconds` INTEGER NOT NULL, `surname` TEXT NOT NULL, `givenNames` TEXT NOT NULL, `birthDateIso` TEXT, PRIMARY KEY(`cardId`), FOREIGN KEY(`cardId`) REFERENCES `driver_cards`(`id`) ON UPDATE NO ACTION ON DELETE RESTRICT)""")
            db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_driver_card_identities_cardKey` ON `driver_card_identities` (`cardKey`)")
            db.execSQL("""CREATE TABLE IF NOT EXISTS `driver_import_sources` (`sessionId` TEXT NOT NULL, `driverCardId` TEXT NOT NULL, `activityId` TEXT, `vehicleId` TEXT, `pairId` TEXT, `newSourceCount` INTEGER NOT NULL, PRIMARY KEY(`sessionId`), FOREIGN KEY(`sessionId`) REFERENCES `import_sessions`(`id`) ON UPDATE NO ACTION ON DELETE RESTRICT, FOREIGN KEY(`driverCardId`) REFERENCES `driver_cards`(`id`) ON UPDATE NO ACTION ON DELETE RESTRICT, FOREIGN KEY(`activityId`) REFERENCES `card_activity_imports`(`id`) ON UPDATE NO ACTION ON DELETE RESTRICT, FOREIGN KEY(`vehicleId`) REFERENCES `card_vehicle_imports`(`id`) ON UPDATE NO ACTION ON DELETE RESTRICT, FOREIGN KEY(`pairId`) REFERENCES `card_source_pairs`(`id`) ON UPDATE NO ACTION ON DELETE RESTRICT)""")
            for (column in listOf("driverCardId", "activityId", "vehicleId", "pairId"))
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_driver_import_sources_$column` ON `driver_import_sources` (`$column`)")
        }
    }
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
