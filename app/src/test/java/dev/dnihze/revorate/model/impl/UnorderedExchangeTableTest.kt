package dev.dnihze.revorate.model.impl

import dev.dnihze.revorate.model.Currency
import dev.dnihze.revorate.model.ExchangeRate
import org.junit.Assert.*
import org.junit.Test

class UnorderedExchangeTableTest {

    @Test
    fun emptyTableWorks() {
        val table = UnorderedExchangeTable(emptySet())
        assertEquals(null, table.baseCurrency)
        assertTrue(table.isEmpty())
    }

    @Test
    fun simple() {
        val rateCAD = ExchangeRate(2.0, Currency.EUR, Currency.CAD)
        val rateUSD = ExchangeRate(2.0, Currency.EUR, Currency.USD)

        val set = setOf(rateCAD, rateUSD)
        val table = UnorderedExchangeTable(setOf(rateCAD, rateUSD))

        assertEquals(Currency.EUR, table.baseCurrency)
        assertEquals(rateUSD, table[Currency.USD])
        assertEquals(rateCAD, table[Currency.CAD])
        assertEquals(ExchangeRate(1.0, Currency.EUR, Currency.EUR), table[Currency.EUR])

        assertEquals(null, table[Currency.RON])

        assertTrue(!table.isOrdered())
        assertTrue(!table.isEmpty())

        table.iterator().forEach { rate ->
            assertTrue(rate in set)
        }
    }

    @Test
    fun orderingWorks() {
        val rateCAD = ExchangeRate(2.0, Currency.EUR, Currency.CAD)
        val rateUSD = ExchangeRate(3.0, Currency.EUR, Currency.USD)
        val table = UnorderedExchangeTable(setOf(rateUSD, rateCAD))
        val orderedTable = table.order()
        assertTrue(orderedTable.isOrdered())
        val items = orderedTable.toList()
        assertEquals(Currency.CAD, items[0].ofCurrency)
        assertEquals(Currency.USD, items[1].ofCurrency)
    }

    @Test
    fun tableConvertWorks() {
        val rateCAD = ExchangeRate(2.0, Currency.EUR, Currency.CAD)
        val rateUSD = ExchangeRate(3.0, Currency.EUR, Currency.USD)

        val table = UnorderedExchangeTable(setOf(rateCAD, rateUSD))

        val tableForEUR = table.newTableFor(currency = Currency.EUR)
        assertTrue(tableForEUR === table)

        val tableForRON = table.newTableFor(currency = Currency.RON)
        assertNull(tableForRON)

        val tableForCAD = table.newTableFor(Currency.CAD)
        assertNotNull(tableForCAD)
        tableForCAD?.let { newTable ->
            assertTrue(!newTable.isEmpty())
            assertTrue(!newTable.isOrdered())

            val eur = newTable.getValue(Currency.EUR)
            assertEquals(0.5, eur.rate, 0.0)
            assertEquals(Currency.CAD, eur.forCurrency)
            assertEquals(Currency.EUR, eur.ofCurrency)

            val usd = newTable.getValue(Currency.USD)
            assertEquals(1.5, usd.rate, 0.0)
            assertEquals(Currency.CAD, usd.forCurrency)
            assertEquals(Currency.USD, usd.ofCurrency)
        }
    }

    @Test(expected = IllegalArgumentException::class)
    fun differentBaseFails() {
        val rateCAD = ExchangeRate(2.0, Currency.EUR, Currency.CAD)
        val rateUSD = ExchangeRate(2.0, Currency.RON, Currency.USD)

        UnorderedExchangeTable(setOf(rateCAD, rateUSD))
    }

    @Test(expected = AssertionError::class)
    fun duplicationFails() {
        val rateCAD = ExchangeRate(2.0, Currency.EUR, Currency.CAD)
        val rateUSD = ExchangeRate(2.0, Currency.EUR, Currency.USD)
        val rateUSD2 = ExchangeRate(1.5, Currency.EUR, Currency.USD)

        UnorderedExchangeTable(setOf(rateCAD, rateUSD, rateUSD2))
    }

    @Test(expected = NullPointerException::class)
    fun getValueForNonIncludedRateFails() {
        val rateCAD = ExchangeRate(2.0, Currency.EUR, Currency.CAD)
        val rateUSD = ExchangeRate(3.0, Currency.EUR, Currency.USD)
        val table = UnorderedExchangeTable(setOf(rateUSD, rateCAD))
        assertNull(table[Currency.RON])
        table.getValue(Currency.RON)
    }
}