package dev.dnihze.revorate.model


data class ExchangeRate(
    val rate: Double,
    val forCurrency: Currency,
    val ofCurrency: Currency
) {

    init {
        // Pre-requirements
        assert(forCurrency != ofCurrency || rate == 1.0) {
            "Same currency exchange rate is always supposed to be 1.0. Created exchange rate: $this."
        }

        assert(rate > 0) {
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
     * If the base currency is different, than code throws IllegalArgumentException.
     */
    fun invertRateWithNewBase(anotherRate: ExchangeRate): ExchangeRate {
        assert(anotherRate.forCurrency == this.forCurrency) {
            """
                |Base currencies are different:
                |    * this:       $this,
                |    * anotherRate: $anotherRate
            """.trimMargin()
        }

        return if (anotherRate.ofCurrency == ofCurrency && anotherRate.forCurrency == forCurrency) {
            if (anotherRate.rate == rate) {
                ExchangeRate(1.0, anotherRate.ofCurrency, anotherRate.ofCurrency)
            } else {
                throw IllegalArgumentException(
                    """
                        |Exchange rates currencies are the same, while the exchange rate are different:
                        |    * this:       $this,
                        |    * anotherRate: $anotherRate
                    """
                )
            }
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
            rate = 1.0 / this.rate,
            forCurrency = this.ofCurrency,
            ofCurrency = this.forCurrency
        )
    }
}
