package de.fahrerabrechnung.data.backup

/** Provider boundary only. No cloud account, upload or server is selected in the project-start phase. */
interface BackupProvider {
    suspend fun availability(): BackupAvailability
}

enum class BackupAvailability { NOT_CONFIGURED, AVAILABLE, UNAVAILABLE }

class NoOpBackupProvider @javax.inject.Inject constructor() : BackupProvider {
    override suspend fun availability() = BackupAvailability.NOT_CONFIGURED
}
