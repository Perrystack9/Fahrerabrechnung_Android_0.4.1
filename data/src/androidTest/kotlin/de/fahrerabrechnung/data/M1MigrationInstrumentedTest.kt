package de.fahrerabrechnung.data

import android.content.Context
import android.database.Cursor
import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import de.fahrerabrechnung.data.db.*
import de.fahrerabrechnung.data.security.EncryptedDatabaseFactory
import de.fahrerabrechnung.domain.violation.input.InputRevision
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream
import java.security.SecureRandom
import net.zetetic.database.sqlcipher.SupportOpenHelperFactory
import org.json.JSONObject
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class M1MigrationInstrumentedTest {
    @get:Rule val helper = MigrationTestHelper(InstrumentationRegistry.getInstrumentation(), AppDatabase::class.java)
    private val migrations = arrayOf(DatabaseMigrations.MIGRATION_1_2, DatabaseMigrations.MIGRATION_2_3,
        DatabaseMigrations.MIGRATION_3_4, DatabaseMigrations.MIGRATION_4_5, DatabaseMigrations.MIGRATION_5_6,
        DatabaseMigrations.MIGRATION_6_7, DatabaseMigrations.MIGRATION_7_8, DatabaseMigrations.MIGRATION_8_9,
        DatabaseMigrations.MIGRATION_9_10, DatabaseMigrations.MIGRATION_10_11, DatabaseMigrations.MIGRATION_11_12,
        DatabaseMigrations.MIGRATION_12_13, DatabaseMigrations.MIGRATION_13_14, DatabaseMigrations.MIGRATION_14_15,
        DatabaseMigrations.MIGRATION_15_16, DatabaseMigrations.MIGRATION_16_17, DatabaseMigrations.MIGRATION_17_18,
        DatabaseMigrations.MIGRATION_18_19, DatabaseMigrations.MIGRATION_19_20, DatabaseMigrations.MIGRATION_20_21,
        DatabaseMigrations.MIGRATION_21_22, DatabaseMigrations.MIGRATION_22_23)

    @Test fun fullMigrationChainFrom1AndDirect22ValidateActualCompilerSchema23() {
        for (version in listOf(1, 22)) {
            val name = "m1-schema-$version"
            helper.createDatabase(name, version).use { db ->
                db.execSQL("INSERT INTO profiles VALUES ('p', 'Synthetic retained profile', 1, 1)")
            }
            helper.runMigrationsAndValidate(name, 23, true, *migrations).use { db ->
                db.query("SELECT name FROM profiles WHERE id = 'p'").use { it.moveToFirst(); assertEquals("Synthetic retained profile", it.getString(0)) }
                db.query("SELECT COUNT(*) FROM violation_context_revisions").use { it.moveToFirst(); assertEquals(0, it.getInt(0)) }
                db.query("PRAGMA foreign_key_check").use { assertEquals(0, it.count) }
            }
        }
    }

    @Test fun encrypted18And22UpgradeReopenPreserveEveryOriginalRowAndDoNotBackfill() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val assets = InstrumentationRegistry.getInstrumentation().context.assets
        System.loadLibrary("sqlcipher")
        for (version in listOf(18, 22)) {
            context.deleteDatabase(AppDatabase.DATABASE_NAME)
            val key = ByteArray(32).also(SecureRandom()::nextBytes)
            val schema = JSONObject(assets.open("de.fahrerabrechnung.data.db.AppDatabase/$version.json").bufferedReader().use { it.readText() }).getJSONObject("database")
            val entities = schema.getJSONArray("entities")
            val tables = (0 until entities.length()).map { entities.getJSONObject(it).getString("tableName") }
            val old = SupportOpenHelperFactory(key).create(SupportSQLiteOpenHelper.Configuration.builder(context)
                .name(AppDatabase.DATABASE_NAME).callback(object : SupportSQLiteOpenHelper.Callback(version) {
                    override fun onConfigure(db: SupportSQLiteDatabase) { db.setForeignKeyConstraintsEnabled(true) }
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        for (i in 0 until entities.length()) {
                            val entity = entities.getJSONObject(i)
                            db.execSQL(entity.getString("createSql").replace("\${TABLE_NAME}", entity.getString("tableName")))
                            // Compiler exports omit indices for entities without declared indexes.
                            // A present malformed value still fails through the strict accessor.
                            if (entity.has("indices")) {
                                val indexes = entity.getJSONArray("indices")
                                for (j in 0 until indexes.length()) db.execSQL(indexes.getJSONObject(j).getString("createSql")
                                    .replace("\${TABLE_NAME}", entity.getString("tableName")))
                            }
                        }
                        val setup = schema.getJSONArray("setupQueries")
                        for (i in 0 until setup.length()) db.execSQL(setup.getString(i))
                    }
                    override fun onUpgrade(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) = error("Fixture never upgrades")
                }).build())
            val before: Map<String, String>
            old.use { handle ->
                val db = handle.writableDatabase
                db.execSQL("INSERT INTO profiles VALUES ('p', 'Synthetic protected profile', 1, 1)")
                db.execSQL("INSERT INTO local_driver_profile VALUES (1, 'p')")
                db.execSQL("INSERT INTO driver_cards VALUES ('c', 'p', 'synthetic', 'CONFIRMED', 1, 1)")
                db.execSQL("INSERT INTO import_sessions VALUES ('s', 'p', 'SAVED', 1, 1)")
                db.execSQL("INSERT INTO card_activity_imports VALUES ('a', 'synthetic', 'GEN1', 0, 0, X'000102030405', 1, 1)")
                db.execSQL("INSERT INTO driver_import_sources VALUES ('s', 'c', 'a', NULL, NULL, 1)")
                db.execSQL("INSERT INTO card_metadata_imports VALUES ('original', 'c', 'ORIGINAL_EF_0504', 'GEN1', X'0011223344556677', 1)")
                db.execSQL("INSERT INTO driver_import_metadata_sources VALUES ('s', 'ORIGINAL_EF_0504', 'c', 'original')")
                if (version == 22) db.execSQL("""INSERT INTO card_core_signature_receipts
                    (sessionId,driverCardId,kind,generation,application,originalSourceId,status,actualLength,bytes,fingerprint)
                    VALUES ('s','c','ORIGINAL_EF_0504','GEN1','GEN1','original','CAPTURED_UNVERIFIED',3,X'AABBCC','unchanged-receipt')""")
                before = tables.associateWith { tableDigest(db, it) }
            }
            val migrated = EncryptedDatabaseFactory.create(context, key)
            try {
                val db = migrated.openHelper.writableDatabase
                assertEquals(23, db.version)
                assertEquals(before, tables.associateWith { tableDigest(db, it) })
                db.query("SELECT COUNT(*) FROM violation_context_revisions").use { it.moveToFirst(); assertEquals(0, it.getInt(0)) }
                db.query("PRAGMA foreign_key_check").use { assertEquals(0, it.count) }
                db.query("PRAGMA integrity_check").use { it.moveToFirst(); assertEquals("ok", it.getString(0)) }
            } finally { migrated.close() }
            val reopened = EncryptedDatabaseFactory.create(context, key)
            try {
                assertEquals(before, tables.associateWith { tableDigest(reopened.openHelper.writableDatabase, it) })
            } finally { reopened.close() }
            val header = context.getDatabasePath(AppDatabase.DATABASE_NAME).inputStream().use { stream -> ByteArray(16).also { stream.read(it) } }
            assertFalse(header.toString(Charsets.US_ASCII).startsWith("SQLite format 3"))
            context.deleteDatabase(AppDatabase.DATABASE_NAME)
        }
    }

    private fun tableDigest(db: SupportSQLiteDatabase, table: String): String {
        require(table.matches(Regex("[a-z_]+")))
        val bytes = ByteArrayOutputStream()
        DataOutputStream(bytes).use { output ->
            db.query("SELECT * FROM `$table` ORDER BY rowid").use { cursor ->
                output.writeInt(cursor.columnCount); output.writeInt(cursor.count)
                while (cursor.moveToNext()) for (column in 0 until cursor.columnCount) {
                    val type = cursor.getType(column); output.writeInt(type)
                    when (type) {
                        Cursor.FIELD_TYPE_INTEGER -> output.writeLong(cursor.getLong(column))
                        Cursor.FIELD_TYPE_FLOAT -> output.writeDouble(cursor.getDouble(column))
                        Cursor.FIELD_TYPE_STRING -> { val value = cursor.getString(column).toByteArray(); output.writeInt(value.size); output.write(value) }
                        Cursor.FIELD_TYPE_BLOB -> { val value = cursor.getBlob(column); output.writeInt(value.size); output.write(value) }
                    }
                }
            }
        }
        return InputRevision.digest(bytes.toByteArray())
    }
}
