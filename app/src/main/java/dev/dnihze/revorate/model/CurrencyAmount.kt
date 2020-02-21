package dev.dnihze.revorate.model

import java.math.RoundingMode

data class CurrencyAmount(
    val sourceAmount: Double,
    val currency: Currency
) {

    val amount = sourceAmount.toBigDecimal().setScale(2, RoundingMode.HALF_EVEN).toDouble()

    init {
        assert(sourceAmount >= 0) {
            "Amount supposed to be positive only."
        }
    }

    override fun toString(): String {
        return "CurrencyAmount($amount ($sourceAmount) $currency)"
    }

    operator fun times(rate: ExchangeRate): CurrencyAmount {
        return rate.times(this)
    }
}