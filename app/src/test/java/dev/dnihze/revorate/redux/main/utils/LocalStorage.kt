package dev.dnihze.revorate.redux.main.utils

import com.jakewharton.rxrelay2.BehaviorRelay
import dev.dnihze.revorate.data.local.LocalDataSource
import dev.dnihze.revorate.model.ExchangeTable
import dev.dnihze.revorate.model.impl.OrderedExchangeTable
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import java.util.concurrent.TimeUnit

class LocalStorage(
): LocalDataSource {

    private var exchangeTable: ExchangeTable = OrderedExchangeTable(emptyList())

    private val relay = BehaviorRelay.create<ExchangeTable>()

    override fun getLocalExchangeTable(): Observable<ExchangeTable> {
        return relay
    }

    override fun getSingleExchangeTable(): Single<ExchangeTable> {
        return Single.fromCallable { exchangeTable }
    }

    override fun saveExchangeTable(exchangeTable: ExchangeTable): Completable {
        this.exchangeTable = exchangeTable
        relay.accept(exchangeTable)
        return Completable.timer(50L, TimeUnit.MILLISECONDS)
    }

}