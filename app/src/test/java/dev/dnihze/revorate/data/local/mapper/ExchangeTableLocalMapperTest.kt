package dev.dnihze.revorate.data.local.mapper

import dev.dnihze.revorate.data.mapper.CurrencyISOCodeMapper
import dev.dnihze.revorate.model.Currency
import dev.dnihze.revorate.model.local.ExchangeRateDBEntity
import org.junit.Before

import org.junit.Assert.*
import org.junit.Test
import java.lang.IllegalArgumentException

class ExchangeTableLocalMapperTest {

    private lateinit var mapper : ExchangeTableLocalMapper

    @Before
    fun setUp() {
        mapper = ExchangeTableLocalMapper(CurrencyISOCodeMapper())
    }

    @Test
    fun simple() {
        val usd = ExchangeRateDBEntity(null,
            Currency.EUR.isoCode, "2.0000000000", Currency.USD.isoCode, 0)
        val cad = ExchangeRateDBEntity(null,
            Currency.EUR.isoCode, "1.6548000000", Currency.CAD.isoCode, 1)

        val table = mapper.map(listOf(usd, cad))
        assertTrue(table.isOrdered())
        assertFalse(table.isEmpty())
        val values = table.toList()
        assertEquals(2, values.size)

        assertEquals(Currency.USD, values[0].ofCurrency)
        assertEquals(2.0, values[0].rate, 0.0)

        assertEquals(Currency.CAD, values[1].ofCurrency)
        assertEquals(1.6548, values[1].rate, 0.0)

        assertNull(table[Currency.RON])
    }

    @Test
    fun unknownSkipped() {
        val usd = ExchangeRateDBEntity(null,
            Currency.EUR.isoCode, "2.0000000000", Currency.USD.isoCode, 0)
        val cad = ExchangeRateDBEntity(null,
            Currency.EUR.isoCode, "1.6548000000", Currency.CAD.isoCode, 1)
        val uah = ExchangeRateDBEntity(null,
            Currency.EUR.isoCode, "28.0000000000", -1, 1)
        val unknown = ExchangeRateDBEntity(null,
            -2, "1.6548000000", Currency.CAD.isoCode, 1)

        val table = mapper.map(listOf(usd, cad, uah, unknown))
        assertTrue(table.isOrdered())
        assertFalse(table.isEmpty())
        val values = table.toList()
        assertEquals(2, values.size)
    }

    @Test(expected = IllegalArgumentException::class)
    fun differentBaseFails() {
        val usd = ExchangeRateDBEntity(null,
            Currency.RON.isoCode, "2.0000000000", Currency.USD.isoCode, 0)
        val cad = ExchangeRateDBEntity(null,
            Currency.EUR.isoCode, "1.6548000000", Currency.CAD.isoCode, 1)

        mapper.map(listOf(usd, cad))
    }
}