package de.fahrerabrechnung.data.db

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object DatabaseMigrations {
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
