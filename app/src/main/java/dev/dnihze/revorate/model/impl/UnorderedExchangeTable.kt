package dev.dnihze.revorate.model.impl

import androidx.collection.SparseArrayCompat
import dev.dnihze.revorate.model.Currency
import dev.dnihze.revorate.model.ExchangeRate
import dev.dnihze.revorate.model.ExchangeTable
import java.lang.IllegalArgumentException

class UnorderedExchangeTable(
    rates: Set<ExchangeRate>
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

        val rateForThisCurrency = get(currency) ?: return null

        val newRates = map { rate ->
            if (rate == rateForThisCurrency) {
                rate.invert()
            } else {
                rate.invertRateWithNewBase(rateForThisCurrency)
            }
        }.toSet()

        return UnorderedExchangeTable(newRates)
    }

    override fun iterator(): Iterator<ExchangeRate> {
        return UnorderedExchangeTableIterator(exchangeRateStorage)
    }

    private class UnorderedExchangeTableIterator(
        private val exchangeRateStorage: SparseArrayCompat<ExchangeRate>
    ): Iterator<ExchangeRate> {
        private var currentIndex = 0
        override fun hasNext(): Boolean = currentIndex < exchangeRateStorage.size()

        override fun next(): ExchangeRate {
            return exchangeRateStorage.valueAt(currentIndex++)
        }
    }
}