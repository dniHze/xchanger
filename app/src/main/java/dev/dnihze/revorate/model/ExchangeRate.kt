package dev.dnihze.revorate.model

import java.math.BigDecimal


data class ExchangeRate(
    val rate: BigDecimal,
    val forCurrency: Currency,
    val ofCurrency: Currency
) {

    init {
        // Pre-requirements
        assert(forCurrency != ofCurrency || rate == BigDecimal.ONE) {
            "Same currency exchange rate is always supposed to be 1.0. Created exchange rate: $this."
        }

        assert(rate > BigDecimal.ZERO) {
            "Exchange rate can't be 0 or less than 0. Created exchange rate: $this."
        }
    }

    override fun toString(): String {
        return "ExchangeRate(1 $forCurrency = $rate $ofCurrency)"
    }


    operator fun times(amount: CurrencyAmount): CurrencyAmount {
        assert(amount.currency == forCurrency) {
            "Rate can calculate exchange amount only for same base currency"
        }

        return CurrencyAmount(
            sourceAmount = rate * amount.amount,
            currency = ofCurrency
        )
    }

    /**
     * This method produces exchange rate based on basic double math for
     * same base currency.
     * For example:
     * `this` rate:         1 USD = 2 EUR
     * `anotherRate` rate:  1 USD = 100 RUB
     * Produced rate:       1 RUB = 2 / 100 EUR -> 1 RUB = 0.02 EUR
     *
     * If the base currency is different, than code throws IllegalArgumentException
     * if
     */
    fun invertRateWithNewBase(anotherRate: ExchangeRate): ExchangeRate {
        assert(anotherRate.forCurrency == this.forCurrency) {
            """
                Base currencies are different:
                    * this:       $this,
                    * anotherRate: $anotherRate
            """.trimIndent()
        }

        return if (anotherRate == this) {
            ExchangeRate(BigDecimal.ONE, anotherRate.ofCurrency, anotherRate.ofCurrency)
        } else {
            ExchangeRate(
                rate = this.rate / anotherRate.rate,
                forCurrency = anotherRate.ofCurrency,
                ofCurrency = this.ofCurrency
            )
        }
    }

    /**
     * This method inverts current exchange rate.
     *
     * For example:
     * `this` rate:         1 USD = 2 EUR
     * Produced rate:       1 EUR = 1 / 2 USD -> 1 EUR = 0.5 USD
     */
    fun invert(): ExchangeRate {
        return ExchangeRate(
            rate = BigDecimal.ONE / this.rate,
            forCurrency = this.ofCurrency,
            ofCurrency = this.forCurrency
        )
    }
}
