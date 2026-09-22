package de.fahrerabrechnung.data.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import de.fahrerabrechnung.data.db.AppDatabase
import de.fahrerabrechnung.data.db.ProfileDao
import de.fahrerabrechnung.data.security.DatabasePassphraseStore
import de.fahrerabrechnung.data.security.EncryptedDatabaseFactory
import java.time.Clock
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataModule {
    @Provides @Singleton
    fun provideClock(): Clock = Clock.systemUTC()

    @Provides @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context,
        passphraseStore: DatabasePassphraseStore,
    ): AppDatabase = EncryptedDatabaseFactory.create(context, passphraseStore.getOrCreate())

    @Provides
    fun provideProfileDao(database: AppDatabase): ProfileDao = database.profileDao()
}
