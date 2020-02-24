package dev.dnihze.revorate.data.network.mapper

import dev.dnihze.revorate.data.mapper.CurrencyISONameMapper
import dev.dnihze.revorate.model.Currency
import dev.dnihze.revorate.model.ExchangeRate
import dev.dnihze.revorate.model.network.CurrencyRatesResponse
import org.junit.Before
import org.junit.Assert.*
import org.junit.Test
import java.lang.IllegalArgumentException

class CurrencyRatesMapperTest {

    private lateinit var mapper: CurrencyRatesMapper

    @Before
    fun setUp() {
        mapper = CurrencyRatesMapper(CurrencyISONameMapper())
    }

    @Test
    fun simple() {
        val response = CurrencyRatesResponse(
            baseCurrency = "EUR",
            exchangeRates = mapOf(
                "USD" to 1.1,
                "CAD" to 1.68
            )
        )
        val table = mapper.map(response)
        assertEquals(Currency.EUR, table.baseCurrency)
        assertEquals(ExchangeRate(1.1, Currency.EUR, Currency.USD), table[Currency.USD])
        assertEquals(ExchangeRate(1.68, Currency.EUR, Currency.CAD), table[Currency.CAD])
        assertNull(table[Currency.RON])

        assertTrue(!table.isOrdered())
    }

    @Test
    fun unknownCurrenciesSkipped() {
        val response = CurrencyRatesResponse(
            baseCurrency = "EUR",
            exchangeRates = mapOf(
                "USD" to 1.1,
                "CAD" to 1.68,
                "UAH" to 28.56
            )
        )
        val table = mapper.map(response)
        assertEquals(Currency.EUR, table.baseCurrency)
        assertEquals(ExchangeRate(1.1, Currency.EUR, Currency.USD), table[Currency.USD])
        assertEquals(ExchangeRate(1.68, Currency.EUR, Currency.CAD), table[Currency.CAD])

        assertTrue(!table.isOrdered())
        assertEquals(2, table.toList().size)
    }

    @Test(expected = IllegalArgumentException::class)
    fun unknownBaseFails() {
        val response = CurrencyRatesResponse(
            baseCurrency = "UAH",
            exchangeRates = mapOf(
                "USD" to 1.1,
                "CAD" to 1.68
            )
        )
        mapper.map(response)
    }
}