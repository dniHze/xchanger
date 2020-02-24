package dev.dnihze.revorate.model

import dev.dnihze.revorate.model.impl.OrderedExchangeTable
import java.lang.NullPointerException

interface ExchangeTable : Iterable<ExchangeRate> {

    // Values
    val baseCurrency: Currency?
    // Methods
    fun newTableFor(currency: Currency): ExchangeTable?
    fun isEmpty(): Boolean
    fun orderWith(other: ExchangeTable): ExchangeTable
    // Operator methods
    operator fun get(currency: Currency): ExchangeRate?
    operator fun contains(currency: Currency): Boolean

    // Default methods
    fun isOrdered() = this is OrderedExchangeTable

    fun order(): ExchangeTable {
        if (isOrdered()) {
            return this
        }
        return OrderedExchangeTable(sortedBy { it.forCurrency.isoName })
    }

    fun getValue(currency: Currency): ExchangeRate = get(currency)
        ?: throw NullPointerException("No such currency exchange rate inside table.")
}