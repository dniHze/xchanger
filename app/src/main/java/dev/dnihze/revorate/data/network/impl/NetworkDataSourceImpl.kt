package dev.dnihze.revorate.data.network.impl

import dev.dnihze.revorate.data.network.NetworkDataSource
import dev.dnihze.revorate.data.network.exception.ApiExceptionFactory
import dev.dnihze.revorate.data.network.mapper.CurrencyRatesMapper
import dev.dnihze.revorate.model.Currency
import dev.dnihze.revorate.model.ExchangeTable
import io.reactivex.Single
import javax.inject.Inject

class NetworkDataSourceImpl @Inject constructor(
    private val apiService: ApiService,
    private val apiExceptionFactory: ApiExceptionFactory,
    private val currencyRatesMapper: CurrencyRatesMapper
): NetworkDataSource {

    override fun getExchangeTable(baseCurrency: Currency): Single<ExchangeTable> {
        return apiService.getExchangeRates(baseCurrency.isoName)
            .onErrorResumeNext { t -> Single.error(apiExceptionFactory.create(t)) }
            .map { response ->
                currencyRatesMapper.map(response)
            }
    }
}