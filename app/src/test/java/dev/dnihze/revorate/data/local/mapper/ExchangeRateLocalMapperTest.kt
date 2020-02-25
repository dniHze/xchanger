package dev.dnihze.revorate.data.local.mapper

import dev.dnihze.revorate.model.Currency
import dev.dnihze.revorate.model.ExchangeRate
import dev.dnihze.revorate.model.impl.OrderedExchangeTable
import io.mockk.spyk
import io.mockk.verify
import org.junit.Before

import org.junit.Assert.*
import org.junit.Test

class ExchangeRateLocalMapperTest {

    private lateinit var mapper: ExchangeRateLocalMapper

    @Before
    fun setUp() {
        mapper = ExchangeRateLocalMapper()
    }

    @Test
    fun simple() {
        val rateCAD = ExchangeRate(2.0, Currency.EUR, Currency.CAD)
        val rateUSD = ExchangeRate(1.568943, Currency.EUR, Currency.USD)

        val list = listOf(rateCAD, rateUSD)
        val table = spyk(OrderedExchangeTable(list))

        val values = mapper.map(table)

        values.forEachIndexed { index, exchangeRateDBEntity ->
            assertEquals(index, exchangeRateDBEntity.order)
            val value = list[index]
            assertEquals(value.forCurrency.isoCode, exchangeRateDBEntity.forCurrency)
            assertEquals(value.ofCurrency.isoCode, exchangeRateDBEntity.ofCurrency)
            if (index == 0) {
                assertEquals("2.0000000000", exchangeRateDBEntity.rate)
            } else {
                assertEquals("1.5689430000", exchangeRateDBEntity.rate)
            }
        }

        verify { table.order() }

    }
}