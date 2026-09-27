package de.fahrerabrechnung.core.model

/**
 * Result container for the driver card read pipeline.
 * Raw card dumps and sensitive values are intentionally not stored here.
 */
data class CardReadResult(
    val success: Boolean,
    val generation: CardGeneration = CardGeneration.UNKNOWN,
    val identity: CardIdentity? = null,
    val warnings: List<String> = emptyList(),
    val error: String? = null
)

enum class CardGeneration {
    GEN1,
    GEN2,
    GEN2_V2,
    UNKNOWN
}

data class CardIdentity(
    val cardNumberMasked: String? = null,
    val holderNameAvailable: Boolean = false,
    val validityAvailable: Boolean = false
)
