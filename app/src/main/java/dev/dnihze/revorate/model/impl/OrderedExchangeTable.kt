package dev.dnihze.revorate.model.impl

import dev.dnihze.revorate.model.Currency
import dev.dnihze.revorate.model.ExchangeRate
import dev.dnihze.revorate.model.ExchangeTable
import java.lang.IllegalArgumentException

class OrderedExchangeTable(
    private val rates: List<ExchangeRate>
) : AbstractExchangeTable() {

    init {
        val baseCurrencies = rates.map { it.forCurrency }.toSet()

        baseCurrency = when {
            baseCurrencies.size > 1 -> {
                throw IllegalArgumentException(
                    "Illegal exchange rates provided to Exchange table." +
                            "Different base currencies: ${baseCurrencies.joinToString()}. " +
                            "Exchange rates: ${rates.joinToString()}"
                )
            }
            baseCurrencies.isEmpty() -> {
                null
            }
            else -> {
                baseCurrencies.first()
            }
        }

        assert(rates.map { it.ofCurrency }.toSet().size == rates.size) {
            "Only unique target currency exchange rates allowed. Exchange rates: $rates"
        }

        rates.forEach { rate ->
            exchangeRateStorage.put(rate.ofCurrency.isoCode, rate)
        }
    }

    override fun newTableFor(currency: Currency): ExchangeTable? {
        if (baseCurrency == currency) {
            return this
        }

        val rateForNewCurrency = get(currency) ?: return null
        val newRates = mutableListOf(rateForNewCurrency.invert())

        filter { rate -> rate != rateForNewCurrency }
            .mapTo(newRates) { rate ->
                rate.invertRateWithNewBase(rateForNewCurrency)
            }

        return OrderedExchangeTable(newRates)
    }

    override fun iterator(): Iterator<ExchangeRate> {
        return rates.iterator()
    }
}