package de.fahrerabrechnung.data.card

/** Transport boundary only. No USB/CCID protocol or driver-card parser is implemented in 0.4.1 project start. */
interface CardReaderTransport {
    suspend fun availability(): SourceAvailability
}

enum class SourceAvailability { AVAILABLE, UNAVAILABLE, UNKNOWN }

class NoOpCardReaderTransport @javax.inject.Inject constructor() : CardReaderTransport {
    override suspend fun availability() = SourceAvailability.UNKNOWN
}
