package de.fahrerabrechnung.data.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import de.fahrerabrechnung.data.backup.BackupProvider
import de.fahrerabrechnung.data.backup.NoOpBackupProvider
import de.fahrerabrechnung.data.card.CardReaderTransport
import de.fahrerabrechnung.data.card.NoOpCardReaderTransport
import de.fahrerabrechnung.data.location.LocationSource
import de.fahrerabrechnung.data.location.NoOpLocationSource
import de.fahrerabrechnung.data.vehicle.NoOpVinDecoder
import de.fahrerabrechnung.data.vehicle.VinDecoder
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class FutureSourcesModule {
    @Binds @Singleton abstract fun bindCardReader(impl: NoOpCardReaderTransport): CardReaderTransport
    @Binds @Singleton abstract fun bindLocationSource(impl: NoOpLocationSource): LocationSource
    @Binds @Singleton abstract fun bindVinDecoder(impl: NoOpVinDecoder): VinDecoder
    @Binds @Singleton abstract fun bindBackupProvider(impl: NoOpBackupProvider): BackupProvider
}
