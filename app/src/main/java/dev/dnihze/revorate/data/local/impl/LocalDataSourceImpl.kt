package dev.dnihze.revorate.data.local.impl

import dev.dnihze.revorate.data.local.LocalDataSource
import dev.dnihze.revorate.data.local.db.AppDB
import dev.dnihze.revorate.data.local.mapper.ExchangeRateLocalMapper
import dev.dnihze.revorate.data.local.mapper.ExchangeTableLocalMapper
import dev.dnihze.revorate.model.ExchangeTable
import dev.dnihze.revorate.model.impl.OrderedExchangeTable
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import javax.inject.Inject

class LocalDataSourceImpl @Inject constructor(
    appDB: AppDB,
    private val tableMapper: ExchangeTableLocalMapper,
    private val localMapper: ExchangeRateLocalMapper
): LocalDataSource {

    private val dao = appDB.exchangeRatesDao()

    override fun getLocalExchangeTable(): Observable<ExchangeTable> {
        return dao.getExchangeRates()
            .map { items -> tableMapper.map(items) }
    }

    override fun getSingleExchangeTable(): Single<ExchangeTable> {
        return dao.getExchangeRatesSingle()
            .map { items -> tableMapper.map(items) }
    }

    override fun saveExchangeTable(exchangeTable: ExchangeTable): Completable {
        return Completable.fromCallable {
            dao.updateWith(localMapper.map(exchangeTable))
        }
    }
}