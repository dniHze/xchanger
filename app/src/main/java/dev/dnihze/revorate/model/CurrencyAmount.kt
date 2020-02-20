package dev.dnihze.revorate.model

import java.math.BigDecimal
import java.math.RoundingMode

data class CurrencyAmount(
    val sourceAmount: BigDecimal,
    val currency: Currency
) {

    val amount = sourceAmount.setScale(2, RoundingMode.UP)

    init {
        assert(sourceAmount >= BigDecimal.ZERO) {
            "Amount supposed to be positive only."
        }
    }

    override fun toString(): String {
        return "CurrencyAmount($amount $currency)"
    }

    operator fun times(rate: ExchangeRate): CurrencyAmount {
        return rate.times(this)
    }
}