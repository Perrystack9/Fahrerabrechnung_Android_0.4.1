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
            .addMigrations(DatabaseMigrations.MIGRATION_1_2)
            .build()
    }
}
