package dev.dnihze.revorate.model.impl

import dev.dnihze.revorate.model.Currency
import dev.dnihze.revorate.model.ExchangeRate

import org.junit.Assert.*
import org.junit.Test
import java.lang.AssertionError

class AbstractExchangeTableTest {

    @Test
    fun reorderWorks() {
        val unorderedExchangeTable = UnorderedExchangeTable(
            setOf(
                ExchangeRate(1.1, Currency.EUR, Currency.USD),
                ExchangeRate(1.2, Currency.EUR, Currency.CAD)
            )
        )

        val orderedExchangeTable = OrderedExchangeTable(
            listOf(
                ExchangeRate(1.0, Currency.EUR, Currency.CAD),
                ExchangeRate(1.0, Currency.EUR, Currency.USD)
            )
        )

        val newOrdered = unorderedExchangeTable.orderWith(orderedExchangeTable)
        assertTrue(newOrdered.isOrdered())
        assertFalse(newOrdered.isEmpty())
        newOrdered.forEach { rate ->
            assertEquals(unorderedExchangeTable.getValue(rate.ofCurrency).rate, rate.rate, 0.0)
        }
        val values = newOrdered.toList()
        assertEquals(Currency.CAD, values.first().ofCurrency)
        assertEquals(Currency.USD, values.last().ofCurrency)
    }

    @Test
    fun unknownPutAtTheEndByName() {
        val unorderedExchangeTable = UnorderedExchangeTable(
            setOf(
                ExchangeRate(1.1, Currency.EUR, Currency.USD),
                ExchangeRate(1.5, Currency.EUR, Currency.AUD),
                ExchangeRate(100.0, Currency.EUR, Currency.JPY),
                ExchangeRate(1.2, Currency.EUR, Currency.CAD)
            )
        )

        val orderedExchangeTable = OrderedExchangeTable(
            listOf(
                ExchangeRate(1.0, Currency.EUR, Currency.CAD),
                ExchangeRate(1.0, Currency.EUR, Currency.USD)
            )
        )

        val newOrdered = unorderedExchangeTable.orderWith(orderedExchangeTable)
        assertTrue(newOrdered.isOrdered())
        assertFalse(newOrdered.isEmpty())
        newOrdered.forEach { rate ->
            assertEquals(unorderedExchangeTable.getValue(rate.ofCurrency).rate, rate.rate, 0.0)
        }
        val values = newOrdered.toList()
        assertEquals(Currency.CAD, values[0].ofCurrency)
        assertEquals(Currency.USD, values[1].ofCurrency)
        assertEquals(Currency.AUD, values[2].ofCurrency)
        assertEquals(Currency.JPY, values[3].ofCurrency)
    }

    @Test
    fun orderedWillNotBeReorder() {
        val oldOrdered = OrderedExchangeTable(
            listOf(
                ExchangeRate(1.1, Currency.EUR, Currency.USD),
                ExchangeRate(1.5, Currency.EUR, Currency.AUD),
                ExchangeRate(100.0, Currency.EUR, Currency.JPY),
                ExchangeRate(1.2, Currency.EUR, Currency.CAD)
            )
        )

        val orderedExchangeTable = OrderedExchangeTable(
            listOf(
                ExchangeRate(1.0, Currency.EUR, Currency.CAD),
                ExchangeRate(1.0, Currency.EUR, Currency.USD)
            )
        )

        val newOrdered = oldOrdered.orderWith(orderedExchangeTable)
        assertEquals(oldOrdered, newOrdered)
    }

    @Test
    fun emptyReturnsOrdered() {
        val unorderedExchangeTable = UnorderedExchangeTable(
            setOf(
                ExchangeRate(1.1, Currency.EUR, Currency.USD),
                ExchangeRate(1.2, Currency.EUR, Currency.CAD)
            )
        )

        val orderedExchangeTable = OrderedExchangeTable(
            emptyList()
        )

        val newOrdered = unorderedExchangeTable.orderWith(orderedExchangeTable)
        assertTrue(newOrdered.isOrdered())

        val emptyNonOrdered = UnorderedExchangeTable(
            setOf(
                ExchangeRate(1.1, Currency.EUR, Currency.USD),
                ExchangeRate(1.2, Currency.EUR, Currency.CAD)
            )
        )

        val orderedExchangeTableNonEmpty = OrderedExchangeTable(
            listOf(
                ExchangeRate(1.0, Currency.EUR, Currency.CAD),
                ExchangeRate(1.0, Currency.EUR, Currency.USD)
            )        )

        assertTrue(emptyNonOrdered.orderWith(orderedExchangeTableNonEmpty)
            .isOrdered())
    }

    @Test(expected = AssertionError::class)
    fun differentBaseFails() {
        val unorderedExchangeTable = UnorderedExchangeTable(
            setOf(
                ExchangeRate(1.1, Currency.EUR, Currency.USD),
                ExchangeRate(1.2, Currency.EUR, Currency.CAD)
            )
        )

        val orderedExchangeTable = OrderedExchangeTable(
            listOf(
                ExchangeRate(1.0, Currency.JPY, Currency.CAD),
                ExchangeRate(1.0, Currency.JPY, Currency.USD)
            )
        )

        unorderedExchangeTable.orderWith(orderedExchangeTable)
    }

    @Test(expected = AssertionError::class)
    fun unorderedOtherTableFails() {
        val unorderedExchangeTable = UnorderedExchangeTable(
            setOf(
                ExchangeRate(1.1, Currency.EUR, Currency.USD),
                ExchangeRate(1.2, Currency.EUR, Currency.CAD)
            )
        )

        val otherTable = UnorderedExchangeTable(
            setOf(
                ExchangeRate(1.0, Currency.EUR, Currency.CAD),
                ExchangeRate(1.0, Currency.EUR, Currency.USD)
            )
        )

        unorderedExchangeTable.orderWith(otherTable)
    }

}