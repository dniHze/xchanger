package dev.dnihze.revorate.data.mapper

import dev.dnihze.revorate.model.Currency
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class CurrencyISONameMapperTest {

    private lateinit var mapper: CurrencyISONameMapper

    @Before
    fun setup() {
        mapper = CurrencyISONameMapper()
    }

    @Test
    fun simple() {
        Currency.values().forEach { currency ->
            assertEquals(currency, mapper.map(currency.isoName))
        }
    }

    @Test
    fun lowerCaseWorks() {
        Currency.values().forEach { currency ->
            assertEquals(currency, mapper.map(currency.isoName.toLowerCase()))
        }
    }

    @Test
    fun nonCurrencyReturnsNull() {
        assertNull(mapper.map(""))
        assertNull(mapper.map("hello world"))
    }
}