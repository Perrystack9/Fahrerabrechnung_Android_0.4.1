package de.fahrerabrechnung.data.security

import android.content.Context
import androidx.room.Room
import de.fahrerabrechnung.data.db.AppDatabase
import de.fahrerabrechnung.data.db.DatabaseMigrations
import net.zetetic.database.sqlcipher.SupportOpenHelperFactory

object EncryptedDatabaseFactory {
    fun create(context: Context, passphrase: ByteArray): AppDatabase {
        System.loadLibrary("sqlcipher")
        val sqlCipherFactory = SupportOpenHelperFactory(passphrase)
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            AppDatabase.DATABASE_NAME,
        )
            .openHelperFactory(sqlCipherFactory)
            .addMigrations(DatabaseMigrations.MIGRATION_1_2, DatabaseMigrations.MIGRATION_2_3,
                DatabaseMigrations.MIGRATION_3_4, DatabaseMigrations.MIGRATION_4_5,
                DatabaseMigrations.MIGRATION_5_6, DatabaseMigrations.MIGRATION_6_7,
                DatabaseMigrations.MIGRATION_7_8, DatabaseMigrations.MIGRATION_8_9,
                DatabaseMigrations.MIGRATION_9_10, DatabaseMigrations.MIGRATION_10_11, DatabaseMigrations.MIGRATION_11_12, DatabaseMigrations.MIGRATION_12_13, DatabaseMigrations.MIGRATION_13_14, DatabaseMigrations.MIGRATION_14_15)
            .build()
    }
}
