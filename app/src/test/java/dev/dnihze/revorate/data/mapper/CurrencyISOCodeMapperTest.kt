package dev.dnihze.revorate.data.mapper

import dev.dnihze.revorate.model.Currency
import org.junit.Before

import org.junit.Assert.*
import org.junit.Test

class CurrencyISOCodeMapperTest {

    private lateinit var mapper: CurrencyISOCodeMapper

    @Before
    fun setUp() {
        mapper = CurrencyISOCodeMapper()
    }

    @Test
    fun simple() {
        Currency.values().forEach { currency ->
            assertEquals(currency, mapper.map(currency.isoCode))
        }
    }

    @Test
    fun nonCurrencyReturnsNull() {
        assertNull(mapper.map(1337))
        assertNull(mapper.map(0))
        assertNull(mapper.map(-12))
    }
}