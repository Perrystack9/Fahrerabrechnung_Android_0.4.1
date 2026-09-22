package de.fahrerabrechnung.data.vehicle

/** Interface only. No online provider, VIN guess or manual vehicle identity entry is implemented here. */
interface VinDecoder {
    suspend fun decode(vin: String): VinDecodeResult
}

sealed interface VinDecodeResult {
    data object NotConfigured : VinDecodeResult
    data class Available(val manufacturer: String?, val model: String?, val powerKw: Int?) : VinDecodeResult
}

class NoOpVinDecoder @javax.inject.Inject constructor() : VinDecoder {
    override suspend fun decode(vin: String): VinDecodeResult = VinDecodeResult.NotConfigured
}
