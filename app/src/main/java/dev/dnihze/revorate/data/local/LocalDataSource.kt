package dev.dnihze.revorate.data.local

import dev.dnihze.revorate.model.ExchangeTable
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single

interface LocalDataSource {
    fun getLocalExchangeTable(): Observable<ExchangeTable>
    fun getSingleExchangeTable(): Single<ExchangeTable>
    fun saveExchangeTable(exchangeTable: ExchangeTable): Completable
}