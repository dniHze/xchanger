package dev.dnihze.revorate.data.ui.mapper

import dev.dnihze.revorate.model.Currency
import dev.dnihze.revorate.model.CurrencyAmount
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.util.*

class DisplayAmountMapperTest {

    private lateinit var mapper: DisplayAmountMapper

    @Before
    fun setup() {
        mapper = DisplayAmountMapper()
    }

    @Test
    fun simpleTwoSigns() {
        Locale.setDefault(Locale.US)
        assertEquals("0", mapper.map(CurrencyAmount(.0, Currency.USD)))
        assertEquals("0.01", mapper.map(CurrencyAmount(.01, Currency.USD)))
        assertEquals("0", mapper.map(CurrencyAmount(.001, Currency.USD)))
        assertEquals("0.69", mapper.map(CurrencyAmount(.6891, Currency.USD)))
        assertEquals("12", mapper.map(CurrencyAmount(12.0001, Currency.USD)))
        assertEquals("2000000000000", mapper.map(CurrencyAmount(2000000000000.0, Currency.USD)))
        assertEquals("2000000000000.13", mapper.map(CurrencyAmount(2000000000000.134, Currency.USD)))
    }

    @Test
    fun simpleTwoSignsLocaleGerman() {
        Locale.setDefault(Locale.GERMAN)

        assertEquals("0", mapper.map(CurrencyAmount(.0, Currency.USD)))
        assertEquals("0.01", mapper.map(CurrencyAmount(.01, Currency.USD)))
        assertEquals("0", mapper.map(CurrencyAmount(.001, Currency.USD)))
        assertEquals("0.69", mapper.map(CurrencyAmount(.6891, Currency.USD)))
        assertEquals("12", mapper.map(CurrencyAmount(12.0001, Currency.USD)))
        assertEquals("2000000000000", mapper.map(CurrencyAmount(2000000000000.0, Currency.USD)))
        assertEquals("2000000000000.13", mapper.map(CurrencyAmount(2000000000000.134, Currency.USD)))
    }

    @Test
    fun simpleZeroSigns() {
        assertEquals("0", mapper.map(CurrencyAmount(.0, Currency.JPY)))
        assertEquals("0", mapper.map(CurrencyAmount(.01, Currency.JPY)))
        assertEquals("0", mapper.map(CurrencyAmount(.001, Currency.JPY)))
        assertEquals("0", mapper.map(CurrencyAmount(.489, Currency.JPY)))
        assertEquals("0", mapper.map(CurrencyAmount(.698, Currency.JPY)))
        assertEquals("12", mapper.map(CurrencyAmount(12.0001, Currency.JPY)))
        assertEquals("2000000000000", mapper.map(CurrencyAmount(2000000000000.0, Currency.JPY)))
        assertEquals("2000000000000", mapper.map(CurrencyAmount(2000000000000.134, Currency.JPY)))
    }

    @After
    fun tearDown() {
        Locale.setDefault(Locale.US)
    }
}