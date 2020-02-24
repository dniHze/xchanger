package dev.dnihze.revorate.data.ui

import dev.dnihze.revorate.data.ui.mapper.DisplayAmountMapper
import dev.dnihze.revorate.data.ui.mapper.DisplayCurrencyFlagMapper
import dev.dnihze.revorate.data.ui.mapper.DisplayCurrencyNameMapper
import dev.dnihze.revorate.model.Currency
import dev.dnihze.revorate.model.CurrencyAmount
import dev.dnihze.revorate.model.ExchangeRate
import dev.dnihze.revorate.model.impl.OrderedExchangeTable
import dev.dnihze.revorate.model.impl.UnorderedExchangeTable
import dev.dnihze.revorate.redux.main.MainScreenState
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class MainScreenListFactoryTest {

    private lateinit var factory: MainScreenListFactory

    @Before
    fun setup() {
        factory = MainScreenListFactory(
            displayAmountMapper = DisplayAmountMapper(),
            flagMapper = DisplayCurrencyFlagMapper(),
            nameMapper = DisplayCurrencyNameMapper()
        )
    }

    @Test
    fun emptyTableMapsToNothing() {
        val list = factory.create(
            OrderedExchangeTable(emptyList()),
            CurrencyAmount(.0, Currency.USD),
            null
        )
        assertTrue(list.isEmpty())
    }

    @Test(expected = AssertionError::class)
    fun differentBaseFails() {
        val rateCAD = ExchangeRate(2.0, Currency.EUR, Currency.CAD)
        val rateUSD = ExchangeRate(3.0, Currency.EUR, Currency.USD)

        val table = OrderedExchangeTable(listOf(rateUSD, rateCAD))
        factory.create(
            table,
            CurrencyAmount(.0, Currency.USD),
            null
        )
    }

    @Test(expected = AssertionError::class)
    fun unorderedTableFails() {
        val rateCAD = ExchangeRate(2.0, Currency.EUR, Currency.CAD)
        val rateUSD = ExchangeRate(3.0, Currency.EUR, Currency.USD)

        val table = UnorderedExchangeTable(setOf(rateUSD, rateCAD))
        factory.create(
            table,
            CurrencyAmount(.0, Currency.USD),
            null
        )
    }

    @Test
    fun simple() {
        val rateCAD = ExchangeRate(2.0, Currency.EUR, Currency.CAD)
        val rateUSD = ExchangeRate(3.0, Currency.EUR, Currency.USD)
        val tableOrder = listOf(rateUSD, rateCAD)
        val table = OrderedExchangeTable(listOf(rateUSD, rateCAD))

        val displayList = factory.create(
            table,
            CurrencyAmount(.0, Currency.EUR),
            null
        )
        assertEquals(3, displayList.size)

        displayList.forEachIndexed { index, item ->
            assertEquals("0", item.displayAmount)
            assertEquals(0.0, item.amount.amount, 0.0)
            assertEquals(index == 0, item.inputEnabled)
            if (index > 0) {
                assertEquals(tableOrder[index - 1].ofCurrency, item.amount.currency)
            } else {
                assertEquals(table.baseCurrency, item.amount.currency)
            }
        }
    }

    @Test
    fun simpleCopyingState() {
        val rateCAD = ExchangeRate(2.0, Currency.EUR, Currency.CAD)
        val rateUSD = ExchangeRate(3.0, Currency.EUR, Currency.USD)
        val tableOrder = listOf(rateUSD, rateCAD)
        val table = OrderedExchangeTable(listOf(rateUSD, rateCAD))

        val state = mockk<MainScreenState.DisplayState>()
        every { state.getFreeInput() } returns  "0.00"

        val displayList = factory.create(
            table,
            CurrencyAmount(.0, Currency.EUR),
            state
        )
        assertEquals(3, displayList.size)

        displayList.forEachIndexed { index, item ->
            assertEquals("0", item.displayAmount)
            assertEquals(0.0, item.amount.amount, 0.0)
            assertEquals(index == 0, item.inputEnabled)
            if (index > 0) {
                assertEquals(tableOrder[index - 1].ofCurrency, item.amount.currency)
            } else {
                assertEquals("0.00", item.freeInput)
                assertEquals(table.baseCurrency, item.amount.currency)
            }
        }
    }
}