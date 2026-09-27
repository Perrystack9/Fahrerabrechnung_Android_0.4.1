package de.fahrerabrechnung.data.di

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import de.fahrerabrechnung.data.backup.BackupProvider
import de.fahrerabrechnung.data.backup.NoOpBackupProvider
import de.fahrerabrechnung.data.card.CardReaderTransport
import de.fahrerabrechnung.data.card.AndroidCcidTransport
import de.fahrerabrechnung.data.vehicle.NoOpVinDecoder
import de.fahrerabrechnung.data.vehicle.VinDecoder
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class FutureSourcesModule {
    @Binds @Singleton abstract fun bindVinDecoder(impl: NoOpVinDecoder): VinDecoder
    @Binds @Singleton abstract fun bindBackupProvider(impl: NoOpBackupProvider): BackupProvider

    companion object {
        @Provides @Singleton fun cardReader(@ApplicationContext context: Context): CardReaderTransport =
            AndroidCcidTransport(context)
    }
}
