package dev.dnihze.revorate.data.local.mapper

import dev.dnihze.revorate.common.Mapper
import dev.dnihze.revorate.data.mapper.CurrencyISOCodeMapper
import dev.dnihze.revorate.model.ExchangeRate
import dev.dnihze.revorate.model.ExchangeTable
import dev.dnihze.revorate.model.impl.OrderedExchangeTable
import dev.dnihze.revorate.model.local.ExchangeRateDBEntity
import java.math.BigDecimal
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExchangeTableLocalMapper @Inject constructor(
    private val currencyISOCodeMapper: CurrencyISOCodeMapper
): Mapper<List<ExchangeRateDBEntity>, ExchangeTable> {

    override fun map(from: List<ExchangeRateDBEntity>): ExchangeTable {
        val rates = from.mapNotNull { entity ->
            val forCurrency = currencyISOCodeMapper.map(entity.forCurrency)
            val ofCurrency = currencyISOCodeMapper.map(entity.ofCurrency)
            if (forCurrency != null && ofCurrency != null) {
                ExchangeRate(
                    forCurrency = forCurrency,
                    rate = BigDecimal(entity.rate),
                    ofCurrency = ofCurrency
                )
            } else {
                null
            }
        }
        return OrderedExchangeTable(rates)
    }
}