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
}
