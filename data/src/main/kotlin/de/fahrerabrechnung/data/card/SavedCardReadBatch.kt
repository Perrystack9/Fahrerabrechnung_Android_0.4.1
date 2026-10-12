package de.fahrerabrechnung.data.card

data class SavedCardApplication(val application: CardApplication, val status: CardApplicationReadStatus,
    val generation: CardGeneration?, val failure: ReaderFailure?, val run: CardImportRun?)

class SavedCardMasterFile(val fileId: Int, bytes: ByteArray?, val readFailed: Boolean) {
    private val original = bytes?.copyOf()
    val bytes: ByteArray? get() = original?.copyOf()
}

data class SavedCardReadBatch(val id: String, val driverCardId: String, val savedAtEpochMillis: Long,
    val primaryApplication: CardApplication, val applications: List<SavedCardApplication>,
    val masterFiles: List<SavedCardMasterFile>) {
    val primaryRun: CardImportRun get() = checkNotNull(applications.single { it.application == primaryApplication }.run)
}
