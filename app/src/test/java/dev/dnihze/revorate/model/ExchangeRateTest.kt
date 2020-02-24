package dev.dnihze.revorate.model

import org.junit.Assert.assertEquals
import org.junit.Test

class ExchangeRateTest {

    @Test
    fun simpleUsageWorks() {
        ExchangeRate(2.0, Currency.EUR, Currency.USD)
        ExchangeRate(1.0, Currency.EUR, Currency.EUR)
        ExchangeRate(1.0, Currency.EUR, Currency.CAD)
        ExchangeRate(0.5, Currency.USD, Currency.EUR)
        ExchangeRate(0.000001, Currency.USD, Currency.EUR)
    }

    @Test(expected = AssertionError::class)
    fun sameCurrenciesFailsWithNonOneExchangeRate() {
        ExchangeRate(0.5, Currency.USD, Currency.USD)
    }

    @Test(expected = AssertionError::class)
    fun exchangeRateZeroFails() {
        ExchangeRate(.0, Currency.EUR, Currency.USD)
    }

    @Test(expected = AssertionError::class)
    fun negativeExchangeRateFails() {
        ExchangeRate(-1.0, Currency.EUR, Currency.USD)
    }

    @Test
    fun invertSelfWorks() {
        val simpleInverted = ExchangeRate(0.5, Currency.USD, Currency.EUR).invert()
        assertEquals(2.0, simpleInverted.rate, 0.0)
        assertEquals(Currency.EUR, simpleInverted.forCurrency)
        assertEquals(Currency.USD, simpleInverted.ofCurrency)

        val invertedSameBase = ExchangeRate(1.0, Currency.USD, Currency.USD).invert()
        assertEquals(1.0, invertedSameBase.rate, 0.0)
        assertEquals(Currency.USD, invertedSameBase.forCurrency)
        assertEquals(Currency.USD, invertedSameBase.ofCurrency)

        val invertedOneExchangeRate = ExchangeRate(1.0, Currency.USD, Currency.SEK).invert()
        assertEquals(1.0, invertedOneExchangeRate.rate, 0.0)
        assertEquals(Currency.SEK, invertedOneExchangeRate.forCurrency)
        assertEquals(Currency.USD, invertedOneExchangeRate.ofCurrency)
    }

    @Test
    fun invertWithDifferentExchangeRateWorks() {
        val different = ExchangeRate(2.0, Currency.USD, Currency.CAD)
        val simpleInverted = ExchangeRate(1.0, Currency.USD, Currency.EUR)
            .invertRateWithNewBase(different)
        assertEquals(0.5, simpleInverted.rate, 0.0)
        assertEquals(Currency.CAD, simpleInverted.forCurrency)
        assertEquals(Currency.EUR, simpleInverted.ofCurrency)

        val invertedSameBase = ExchangeRate(1.0, Currency.USD, Currency.USD)
            .invertRateWithNewBase(different)
        assertEquals(0.5, invertedSameBase.rate, 0.0)
        assertEquals(Currency.CAD, invertedSameBase.forCurrency)
        assertEquals(Currency.USD, invertedSameBase.ofCurrency)

        val invertedSameRate = different.copy()
            .invertRateWithNewBase(different)
        assertEquals(1.0, invertedSameRate.rate, 0.0)
        assertEquals(Currency.CAD, invertedSameRate.forCurrency)
        assertEquals(Currency.CAD, invertedSameRate.ofCurrency)
    }

    @Test(expected = AssertionError::class)
    fun differentBaseInvertFails() {
        val different = ExchangeRate(2.0, Currency.USD, Currency.CAD)
        ExchangeRate(1.0, Currency.EUR, Currency.RON).invertRateWithNewBase(different)
    }

    @Test(expected = IllegalArgumentException::class)
    fun differentSameCurrenciesDifferentRateFails() {
        val different = ExchangeRate(2.0, Currency.USD, Currency.CAD)
        ExchangeRate(1.0, Currency.USD, Currency.CAD).invertRateWithNewBase(different)
    }

    @Test
    fun convertingEmptyWithSameBaseWorks() {
        val exchangeRate = ExchangeRate(
            rate = 2.0,
            ofCurrency = Currency.USD,
            forCurrency = Currency.EUR
        )

        val empty = CurrencyAmount(.0, Currency.EUR)
        val convertedAmount = exchangeRate * empty

        assertEquals(.0, convertedAmount.amount, 0.0)
        assertEquals(Currency.USD, convertedAmount.currency)
    }

    @Test
    fun convertingNonEmptyWithSameBaseWorks() {
        val exchangeRate = ExchangeRate(
            rate = 2.0,
            ofCurrency = Currency.USD,
            forCurrency = Currency.EUR
        )

        val nonEmptyWhole = CurrencyAmount(2.0, Currency.EUR)
        val convertedWhole =  exchangeRate * nonEmptyWhole

        assertEquals(4.0, convertedWhole.amount, 0.0)
        assertEquals(Currency.USD, convertedWhole.currency)

        val nonEmptyWithoutRounding = CurrencyAmount(1.5, Currency.EUR)
        val convertedWithoutRounding =  exchangeRate * nonEmptyWithoutRounding

        assertEquals(3.0, convertedWithoutRounding.amount, 0.0)
        assertEquals(Currency.USD, convertedWithoutRounding.currency)

        val nonEmptyWithRounding = CurrencyAmount(1.5001, Currency.EUR)
        val convertedWithRounding =  exchangeRate * nonEmptyWithRounding

        assertEquals(3.0, convertedWithRounding.amount, 0.0)
        assertEquals(Currency.USD, convertedWithRounding.currency)

        val nonEmptyWithRounding2 = CurrencyAmount(1.509, Currency.EUR)
        val convertedWithRounding2 = exchangeRate * nonEmptyWithRounding2

        assertEquals(3.02, convertedWithRounding2.amount, 0.0)
        assertEquals(Currency.USD, convertedWithRounding2.currency)
    }

    @Test(expected = AssertionError::class)
    fun convertingWithDifferentBasesFails() {
        val exchangeRate = ExchangeRate(
            rate = 2.0,
            ofCurrency = Currency.USD,
            forCurrency = Currency.EUR
        )

        exchangeRate * CurrencyAmount(.0, Currency.CAD)
    }

}