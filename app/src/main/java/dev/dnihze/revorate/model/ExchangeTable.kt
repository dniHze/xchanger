package dev.dnihze.revorate.model

import dev.dnihze.revorate.model.impl.OrderedExchangeTable
import java.lang.NullPointerException

interface ExchangeTable : Iterable<ExchangeRate> {

    val baseCurrency: Currency?

    fun newTableFor(currency: Currency): ExchangeTable?
    fun getValue(currency: Currency): ExchangeRate = get(currency)
        ?: throw NullPointerException("No such currency exchange rate inside table.")

    operator fun get(currency: Currency): ExchangeRate?
    operator fun contains(currency: Currency): Boolean
    fun isEmpty(): Boolean

    fun isOrdered() = this is OrderedExchangeTable
    fun order(): ExchangeTable {
        if (isOrdered()) {
            return this
        }
        return OrderedExchangeTable(sortedBy { it.forCurrency.isoName })
    }

    fun orderWith(other: ExchangeTable): ExchangeTable
}