package dev.dnihze.revorate.model

import io.mockk.spyk
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Test

class CurrencyAmountTest {

    @Test
    fun roundingOfSourceAmountWorks() {
        assertEquals(.0, CurrencyAmount(.0, Currency.EUR).amount, .0)
        assertEquals(1.0, CurrencyAmount(1.0, Currency.EUR).amount, .0)
        assertEquals(1.46, CurrencyAmount(1.46, Currency.EUR).amount, .0)
        assertEquals(1.47, CurrencyAmount(1.4689, Currency.EUR).amount, .0)
        assertEquals(1.46, CurrencyAmount(1.4612, Currency.EUR).amount, .0)
        assertEquals(.0, CurrencyAmount(0.00000001, Currency.EUR).amount, .0)
    }

    @Test
    fun confirmTimesDelegatesToExchangeRate() {
        val exchangeRate = spyk(ExchangeRate(
            rate = 2.0,
            ofCurrency = Currency.USD,
            forCurrency = Currency.EUR
        ))

        val nonEmptyWhole = CurrencyAmount(2.0, Currency.EUR)
        val convertedWhole = nonEmptyWhole * exchangeRate

        assertEquals(4.0, convertedWhole.amount, 0.0)
        assertEquals(Currency.USD, convertedWhole.currency)

        // exchangeRate.times() supposed to be tested elsewhere
        verify { exchangeRate.times(nonEmptyWhole) }
    }

    @Test(expected = AssertionError::class)
    fun negativeCurrencyAmountFails() {
        CurrencyAmount(-1.0, currency = Currency.EUR)
    }
}