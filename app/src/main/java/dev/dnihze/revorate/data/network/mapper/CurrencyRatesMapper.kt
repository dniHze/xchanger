package dev.dnihze.revorate.data.network.mapper

import dev.dnihze.revorate.data.mapper.CurrencyISONameMapper
import dev.dnihze.revorate.model.ExchangeRate
import dev.dnihze.revorate.model.ExchangeTable
import dev.dnihze.revorate.model.impl.UnorderedExchangeTable
import dev.dnihze.revorate.model.network.CurrencyRatesResponse
import dev.dnihze.revorate.utils.common.Mapper
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CurrencyRatesMapper @Inject constructor(
    private val currencyISONameMapper: CurrencyISONameMapper
) : Mapper<CurrencyRatesResponse, ExchangeTable> {

    override fun map(from: CurrencyRatesResponse): ExchangeTable {
        val baseCurrency = currencyISONameMapper.map(from.baseCurrency)
            ?: throw IllegalArgumentException("Unknown base currency '${from.baseCurrency}' from response '$from'.")

        val exchangeRatesSet = mutableSetOf<ExchangeRate>()
        from.exchangeRates.forEach { (tag, rate) ->
            val targetCurrency = currencyISONameMapper.map(tag)
            if (targetCurrency != null) {
                exchangeRatesSet += ExchangeRate(
                    rate = rate,
                    forCurrency = baseCurrency,
                    ofCurrency = targetCurrency
                )
            }
        }

        return UnorderedExchangeTable(exchangeRatesSet)
    }
}