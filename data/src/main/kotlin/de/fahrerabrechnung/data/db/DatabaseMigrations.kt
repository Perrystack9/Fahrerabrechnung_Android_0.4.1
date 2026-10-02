package de.fahrerabrechnung.data.db

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object DatabaseMigrations {
    val MIGRATION_7_8 = object : Migration(7, 8) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("""CREATE TABLE IF NOT EXISTS `card_import_runs` (`id` TEXT NOT NULL, `driverCardId` TEXT NOT NULL, `requestFingerprint` TEXT NOT NULL, `status` TEXT NOT NULL, `startedAtEpochMillis` INTEGER NOT NULL, `finishedAtEpochMillis` INTEGER, `sessionId` TEXT, `newSourceCount` INTEGER NOT NULL, `failureCode` TEXT, PRIMARY KEY(`id`), FOREIGN KEY(`driverCardId`) REFERENCES `driver_cards`(`id`) ON UPDATE NO ACTION ON DELETE RESTRICT, FOREIGN KEY(`sessionId`) REFERENCES `import_sessions`(`id`) ON UPDATE NO ACTION ON DELETE RESTRICT)""")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_card_import_runs_driverCardId` ON `card_import_runs` (`driverCardId`)")
            db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_card_import_runs_sessionId` ON `card_import_runs` (`sessionId`)")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_card_import_runs_startedAtEpochMillis` ON `card_import_runs` (`startedAtEpochMillis`)")
            db.execSQL("""CREATE TABLE IF NOT EXISTS `card_import_file_results` (`runId` TEXT NOT NULL, `kind` TEXT NOT NULL, `status` TEXT NOT NULL, `decodedCount` INTEGER NOT NULL, `firstUtcEpochSeconds` INTEGER, `lastUtcEpochSeconds` INTEGER, `failureCode` TEXT, PRIMARY KEY(`runId`, `kind`), FOREIGN KEY(`runId`) REFERENCES `card_import_runs`(`id`) ON UPDATE NO ACTION ON DELETE RESTRICT)""")
        }
    }
    val MIGRATION_6_7 = object : Migration(6, 7) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("""CREATE TABLE IF NOT EXISTS `driver_vehicle_uses` (`id` TEXT NOT NULL, `driverCardId` TEXT NOT NULL, `firstUseUtcEpochSeconds` INTEGER NOT NULL, `lastUseUtcEpochSeconds` INTEGER, `registrationNation` INTEGER NOT NULL, `registrationNumber` TEXT NOT NULL, `vin` TEXT, `odometerBeginKm` INTEGER NOT NULL, `odometerEndKm` INTEGER, `vuDataBlockCounter` INTEGER NOT NULL, PRIMARY KEY(`id`), FOREIGN KEY(`driverCardId`) REFERENCES `driver_cards`(`id`) ON UPDATE NO ACTION ON DELETE RESTRICT)""")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_driver_vehicle_uses_driverCardId_firstUseUtcEpochSeconds` ON `driver_vehicle_uses` (`driverCardId`, `firstUseUtcEpochSeconds`)")
            db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_driver_vehicle_uses_id_driverCardId` ON `driver_vehicle_uses` (`id`, `driverCardId`)")
            db.execSQL("""CREATE TABLE IF NOT EXISTS `driver_vehicle_use_sources` (`driverCardId` TEXT NOT NULL, `vehicleId` TEXT NOT NULL, `recordIndex` INTEGER NOT NULL, `useId` TEXT NOT NULL, `ringIndex` INTEGER NOT NULL, PRIMARY KEY(`driverCardId`, `vehicleId`, `recordIndex`), FOREIGN KEY(`useId`, `driverCardId`) REFERENCES `driver_vehicle_uses`(`id`, `driverCardId`) ON UPDATE NO ACTION ON DELETE RESTRICT, FOREIGN KEY(`vehicleId`) REFERENCES `card_vehicle_imports`(`id`) ON UPDATE NO ACTION ON DELETE RESTRICT)""")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_driver_vehicle_use_sources_vehicleId` ON `driver_vehicle_use_sources` (`vehicleId`)")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_driver_vehicle_use_sources_useId_driverCardId` ON `driver_vehicle_use_sources` (`useId`, `driverCardId`)")
        }
    }
    val MIGRATION_5_6 = object : Migration(5, 6) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("""CREATE TABLE IF NOT EXISTS `driver_activity_days` (`id` TEXT NOT NULL, `driverCardId` TEXT NOT NULL, `recordDateUtcEpochSeconds` INTEGER NOT NULL, `dailyPresenceCounter` TEXT NOT NULL, `distanceKm` INTEGER NOT NULL, `changeCount` INTEGER NOT NULL, PRIMARY KEY(`id`), FOREIGN KEY(`driverCardId`) REFERENCES `driver_cards`(`id`) ON UPDATE NO ACTION ON DELETE RESTRICT)""")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_driver_activity_days_driverCardId_recordDateUtcEpochSeconds` ON `driver_activity_days` (`driverCardId`, `recordDateUtcEpochSeconds`)")
            db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_driver_activity_days_id_driverCardId` ON `driver_activity_days` (`id`, `driverCardId`)")
            db.execSQL("""CREATE TABLE IF NOT EXISTS `driver_activity_changes` (`dayId` TEXT NOT NULL, `changeIndex` INTEGER NOT NULL, `minuteOfDay` INTEGER NOT NULL, `activity` TEXT NOT NULL, `slot` TEXT NOT NULL, `crewDriving` INTEGER NOT NULL, `cardInserted` INTEGER NOT NULL, PRIMARY KEY(`dayId`, `changeIndex`), FOREIGN KEY(`dayId`) REFERENCES `driver_activity_days`(`id`) ON UPDATE NO ACTION ON DELETE RESTRICT)""")
            db.execSQL("""CREATE TABLE IF NOT EXISTS `driver_activity_day_sources` (`driverCardId` TEXT NOT NULL, `activityId` TEXT NOT NULL, `recordIndex` INTEGER NOT NULL, `dayId` TEXT NOT NULL, `ringOffset` INTEGER NOT NULL, PRIMARY KEY(`driverCardId`, `activityId`, `recordIndex`), FOREIGN KEY(`dayId`, `driverCardId`) REFERENCES `driver_activity_days`(`id`, `driverCardId`) ON UPDATE NO ACTION ON DELETE RESTRICT, FOREIGN KEY(`activityId`) REFERENCES `card_activity_imports`(`id`) ON UPDATE NO ACTION ON DELETE RESTRICT)""")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_driver_activity_day_sources_activityId` ON `driver_activity_day_sources` (`activityId`)")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_driver_activity_day_sources_dayId_driverCardId` ON `driver_activity_day_sources` (`dayId`, `driverCardId`)")
        }
    }
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
