package dev.dnihze.revorate.data.network

import dev.dnihze.revorate.model.Currency
import dev.dnihze.revorate.model.ExchangeTable
import io.reactivex.Single

interface NetworkDataSource {
    fun getExchangeTable(baseCurrency: Currency): Single<ExchangeTable>
}