package de.fahrerabrechnung.domain.model

import java.math.BigDecimal
import java.math.RoundingMode

data class Money private constructor(
    val amount: BigDecimal,
    val currencyCode: String,
) {
    init {
        require(currencyCode.matches(Regex("[A-Z]{3}"))) { "currencyCode must be an ISO-style 3-letter code" }
    }

    operator fun plus(other: Money): Money {
        require(currencyCode == other.currencyCode) { "Currencies must match" }
        return of(amount.add(other.amount), currencyCode)
    }

    fun rounded(scale: Int = 2): Money = of(amount.setScale(scale, RoundingMode.HALF_UP), currencyCode)

    companion object {
        fun of(amount: BigDecimal, currencyCode: String): Money =
            Money(amount.stripTrailingZeros(), currencyCode.uppercase())

        fun ofMinorUnits(minorUnits: Long, currencyCode: String, scale: Int = 2): Money =
            Money(BigDecimal.valueOf(minorUnits, scale), currencyCode.uppercase())
    }
}
