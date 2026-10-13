package de.fahrerabrechnung.data

import androidx.room.testing.MigrationTestHelper
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import de.fahrerabrechnung.data.db.AppDatabase
import de.fahrerabrechnung.data.db.DatabaseMigrations
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MigrationTestBase {
    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        AppDatabase::class.java,
    )

    @Test fun migration_21_22_addsOnlyEmptyEventFaultReceiptsAndKeepsExistingTables() {
        val name = "event-fault-signature-migration-test"
        helper.createDatabase(name, 21).apply {
            execSQL("INSERT INTO profiles (id, name, createdAtEpochMillis, updatedAtEpochMillis) VALUES ('p', 'Schema21 profile', 1, 1)")
            close()
        }
        helper.runMigrationsAndValidate(name, 22, true, DatabaseMigrations.MIGRATION_21_22).use { db ->
            for (table in listOf("card_event_fault_signature_receipts", "card_identity_signature_receipts", "card_core_signature_receipts"))
                db.query("SELECT COUNT(*) FROM $table").use { it.moveToFirst(); assertEquals(0, it.getInt(0)) }
            db.query("SELECT name FROM profiles WHERE id = 'p'").use { it.moveToFirst(); assertEquals("Schema21 profile", it.getString(0)) }
            db.query("PRAGMA foreign_key_list(card_event_fault_signature_receipts)").use { assertEquals(10, it.count) }
        }
    }

    @Test fun migration_20_21_addsOnlyEmptyIdentityReceiptsAndKeepsSchema20() {
        val name = "identity-signature-migration-test"
        helper.createDatabase(name, 20).apply {
            execSQL("INSERT INTO profiles (id, name, createdAtEpochMillis, updatedAtEpochMillis) VALUES ('p', 'Schema20 profile', 1, 1)")
            close()
        }
        helper.runMigrationsAndValidate(name, 21, true, DatabaseMigrations.MIGRATION_20_21).use { db ->
            for (table in listOf("card_identity_signature_receipts", "card_core_signature_receipts"))
                db.query("SELECT COUNT(*) FROM $table").use { it.moveToFirst(); assertEquals(0, it.getInt(0)) }
            db.query("SELECT name FROM profiles WHERE id = 'p'").use { it.moveToFirst(); assertEquals("Schema20 profile", it.getString(0)) }
            db.query("PRAGMA foreign_key_list(card_identity_signature_receipts)").use { assertEquals(10, it.count) }
        }
    }

    @Test fun migration_19_20_addsEmptySessionBoundReceiptsWithoutBackfill() {
        val name = "core-signature-migration-test"
        helper.createDatabase(name, 19).apply {
            execSQL("INSERT INTO profiles (id, name, createdAtEpochMillis, updatedAtEpochMillis) VALUES ('p', 'Old profile', 1, 1)")
            close()
        }
        helper.runMigrationsAndValidate(name, 20, true, DatabaseMigrations.MIGRATION_19_20).use { db ->
            db.query("SELECT COUNT(*) FROM card_core_signature_receipts").use { it.moveToFirst(); assertEquals(0, it.getInt(0)) }
            db.query("SELECT name FROM profiles WHERE id = 'p'").use { it.moveToFirst(); assertEquals("Old profile", it.getString(0)) }
        }
    }

    @Test fun migration_1_2_keepsProfilesAndAddsActivitySnapshots() {
        val name = "activity-migration-test"
        helper.createDatabase(name, 1).apply {
            execSQL("INSERT INTO profiles (id, name, createdAtEpochMillis, updatedAtEpochMillis) VALUES ('p1', 'Fahrer', 1, 1)")
            close()
        }
        helper.runMigrationsAndValidate(name, 2, true, DatabaseMigrations.MIGRATION_1_2).use { db ->
            db.query("SELECT name FROM profiles WHERE id = 'p1'").use { cursor ->
                cursor.moveToFirst()
                assertEquals("Fahrer", cursor.getString(0))
            }
            db.query("SELECT count(*) FROM card_activity_imports").use { cursor ->
                cursor.moveToFirst()
                assertEquals(0, cursor.getInt(0))
            }
        }
    }

    @Test fun migration_2_3_preservesExistingActivitySnapshots() {
        val name = "vehicle-migration-test"
        helper.createDatabase(name, 2).apply {
            execSQL("INSERT INTO card_activity_imports (id, cardNumber, generation, oldestDayOffset, newestDayOffset, dailyRecords, dayCount, importedAtEpochMillis) VALUES ('a1', 'test-card', 'GEN2V2', 0, 0, X'0102', 1, 100)")
            close()
        }
        helper.runMigrationsAndValidate(name, 3, true, DatabaseMigrations.MIGRATION_2_3).use { db ->
            db.query("SELECT cardNumber FROM card_activity_imports WHERE id = 'a1'").use { cursor ->
                cursor.moveToFirst()
                assertEquals("test-card", cursor.getString(0))
            }
            db.query("SELECT count(*) FROM card_vehicle_imports").use { cursor ->
                cursor.moveToFirst()
                assertEquals(0, cursor.getInt(0))
            }
        }
    }
    @Test fun migration_11_12_preservesProfilesAndExistingSourcesWithoutBackfill() {
        val name = "gnss-migration-test"
        helper.createDatabase(name, 11).apply {
            execSQL("INSERT INTO profiles (id, name, createdAtEpochMillis, updatedAtEpochMillis) VALUES ('p1', 'Fahrer', 1, 1)")
            close()
        }
        helper.runMigrationsAndValidate(name, 12, true, DatabaseMigrations.MIGRATION_11_12).use { db ->
            db.query("SELECT name FROM profiles WHERE id = 'p1'").use { cursor ->
                cursor.moveToFirst(); assertEquals("Fahrer", cursor.getString(0))
            }
            for (table in listOf("card_gnss_imports", "driver_import_gnss_sources"))
                db.query("SELECT count(*) FROM $table").use { cursor ->
                    cursor.moveToFirst(); assertEquals(0, cursor.getInt(0))
                }
        }
    }
    @Test fun migration_12_13_preservesProfilesAndExistingSourcesWithoutBackfill() {
        val name = "places-migration-test"
        helper.createDatabase(name, 12).apply {
            execSQL("INSERT INTO profiles (id, name, createdAtEpochMillis, updatedAtEpochMillis) VALUES ('p1', 'Fahrer', 1, 1)")
            close()
        }
        helper.runMigrationsAndValidate(name, 13, true, DatabaseMigrations.MIGRATION_12_13).use { db ->
            db.query("SELECT name FROM profiles WHERE id = 'p1'").use { cursor ->
                cursor.moveToFirst(); assertEquals("Fahrer", cursor.getString(0))
            }
            for (table in listOf("card_place_imports", "driver_import_place_sources"))
                db.query("SELECT count(*) FROM $table").use { cursor ->
                    cursor.moveToFirst(); assertEquals(0, cursor.getInt(0))
                }
        }
    }
    @Test fun migration_13_14_preservesProfilesAndExistingSourcesWithoutBackfill() {
        val name = "borders-migration-test"
        helper.createDatabase(name, 13).apply {
            execSQL("INSERT INTO profiles (id, name, createdAtEpochMillis, updatedAtEpochMillis) VALUES ('p1', 'Fahrer', 1, 1)")
            close()
        }
        helper.runMigrationsAndValidate(name, 14, true, DatabaseMigrations.MIGRATION_13_14).use { db ->
            db.query("SELECT name FROM profiles WHERE id = 'p1'").use { cursor ->
                cursor.moveToFirst(); assertEquals("Fahrer", cursor.getString(0))
            }
            for (table in listOf("card_border_crossing_imports", "driver_import_border_crossing_sources"))
                db.query("SELECT count(*) FROM $table").use { cursor ->
                    cursor.moveToFirst(); assertEquals(0, cursor.getInt(0))
                }
        }
    }

    @Test fun migration_14_15_preservesProfilesAndExistingSourcesWithoutBackfill() {
        val name = "loads-migration-test"
        helper.createDatabase(name, 14).apply {
            execSQL("INSERT INTO profiles (id, name, createdAtEpochMillis, updatedAtEpochMillis) VALUES ('p1', 'Fahrer', 1, 1)")
            close()
        }
        helper.runMigrationsAndValidate(name, 15, true, DatabaseMigrations.MIGRATION_14_15).use { db ->
            db.query("SELECT name FROM profiles WHERE id = 'p1'").use { cursor ->
                cursor.moveToFirst(); assertEquals("Fahrer", cursor.getString(0))
            }
            for (table in listOf("card_load_unload_imports", "driver_import_load_unload_sources"))
                db.query("SELECT count(*) FROM $table").use { cursor ->
                    cursor.moveToFirst(); assertEquals(0, cursor.getInt(0))
                }
        }
    }

    @Test fun migration_15_16_preservesProfilesAndExistingSourcesWithoutBackfill() {
        val name = "types-migration-test"
        helper.createDatabase(name, 15).apply {
            execSQL("INSERT INTO profiles (id, name, createdAtEpochMillis, updatedAtEpochMillis) VALUES ('p1', 'Fahrer', 1, 1)")
            close()
        }
        helper.runMigrationsAndValidate(name, 16, true, DatabaseMigrations.MIGRATION_15_16).use { db ->
            db.query("SELECT name FROM profiles WHERE id = 'p1'").use { cursor ->
                cursor.moveToFirst(); assertEquals("Fahrer", cursor.getString(0))
            }
            for (table in listOf("card_load_type_imports", "driver_import_load_type_sources"))
                db.query("SELECT count(*) FROM $table").use { cursor ->
                    cursor.moveToFirst(); assertEquals(0, cursor.getInt(0))
                }
        }
    }

    @Test fun migration_16_17_preservesProfilesAndExistingSourcesWithoutBackfill() {
        val name = "units-migration-test"
        helper.createDatabase(name, 16).apply {
            execSQL("INSERT INTO profiles (id, name, createdAtEpochMillis, updatedAtEpochMillis) VALUES ('p1', 'Fahrer', 1, 1)")
            close()
        }
        helper.runMigrationsAndValidate(name, 17, true, DatabaseMigrations.MIGRATION_16_17).use { db ->
            db.query("SELECT name FROM profiles WHERE id = 'p1'").use { cursor ->
                cursor.moveToFirst(); assertEquals("Fahrer", cursor.getString(0))
            }
            for (table in listOf("card_vehicle_units_imports", "driver_import_vehicle_units_sources"))
                db.query("SELECT count(*) FROM $table").use { cursor ->
                    cursor.moveToFirst(); assertEquals(0, cursor.getInt(0))
                }
        }
    }

    @Test fun migration_17_18_preservesProfilesAndDoesNotBackfillMetadata() {
        val name = "metadata-migration-test"
        helper.createDatabase(name, 17).apply {
            execSQL("INSERT INTO profiles (id, name, createdAtEpochMillis, updatedAtEpochMillis) VALUES ('p1', 'Fahrer', 1, 1)")
            close()
        }
        helper.runMigrationsAndValidate(name, 18, true, DatabaseMigrations.MIGRATION_17_18).use { db ->
            db.query("SELECT name FROM profiles WHERE id = 'p1'").use { cursor ->
                cursor.moveToFirst(); assertEquals("Fahrer", cursor.getString(0))
            }
            for (table in listOf("card_metadata_imports", "driver_import_metadata_sources"))
                db.query("SELECT count(*) FROM $table").use { cursor ->
                    cursor.moveToFirst(); assertEquals(0, cursor.getInt(0))
                }
        }
    }


}
