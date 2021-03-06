package dev.dnihze.revorate.model.impl

import androidx.collection.SparseArrayCompat
import dev.dnihze.revorate.model.Currency
import dev.dnihze.revorate.model.ExchangeRate
import dev.dnihze.revorate.model.ExchangeTable

abstract class AbstractExchangeTable : ExchangeTable {

    override var baseCurrency: Currency? = null
        protected set

    protected val exchangeRateStorage = SparseArrayCompat<ExchangeRate>()

    override fun toString(): String {
        val ratesString = sortedBy { it.ofCurrency.isoName }
            .joinToString(separator = "\n") { "|        $it" }
        return """
            |${javaClass.simpleName}[
            |    baseCurrency: $baseCurrency,
            |    rates: [
                $ratesString
            |    ]
            |]
        """.trimMargin()
    }

    override operator fun contains(currency: Currency): Boolean {
        return currency == baseCurrency || exchangeRateStorage.containsKey(currency.isoCode)
    }

    override operator fun get(currency: Currency): ExchangeRate? {
        if (currency == baseCurrency && !exchangeRateStorage.containsKey(currency.isoCode)) {
            return ExchangeRate(1.0, currency, currency)
        }

        return if (currency in this) {
            exchangeRateStorage[currency.isoCode]
        } else {
            null
        }
    }

    override fun isEmpty(): Boolean = exchangeRateStorage.isEmpty

    override fun orderWith(other: ExchangeTable): ExchangeTable {
        assert(other.isOrdered()) {
            "Order table must be ordered"
        }

        if (isOrdered()) return this

        if (isEmpty()) {
            return OrderedExchangeTable(listOf())
        }

        if (other.isEmpty()) {
            return OrderedExchangeTable(toList())
        }

        assert(this.baseCurrency == other.baseCurrency) {
            "Base currencies should be same."
        }

        val orderMap = other.mapIndexed { index, exchangeRate -> exchangeRate.ofCurrency to index }
            .toMap()

        val (canBeOrdered, cantBeOrdered) = partition { rate -> orderMap.containsKey(rate.ofCurrency) }

        val newOrderedTableItems = mutableListOf<ExchangeRate>()

        newOrderedTableItems += canBeOrdered.sortedBy { rate -> orderMap[rate.ofCurrency] ?: 0 }
        newOrderedTableItems += cantBeOrdered.sortedBy { it.ofCurrency.isoName }

        return OrderedExchangeTable(newOrderedTableItems)
    }
}