package dev.dnihze.revorate.data.local.db

import androidx.room.*
import dev.dnihze.revorate.model.local.ExchangeRateDBEntity
import io.reactivex.Observable
import io.reactivex.Single

@Dao
interface ExchangeRatesDao {
    @Query("SELECT * FROM rates ORDER BY item_order ASC;")
    fun getExchangeRates(): Observable<List<ExchangeRateDBEntity>>

    @Query("SELECT * FROM rates ORDER BY item_order ASC;")
    fun getExchangeRatesSingle(): Single<List<ExchangeRateDBEntity>>

    @Query("DELETE FROM rates;")
    fun deleteAll()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(entities: List<ExchangeRateDBEntity>)

    @Transaction
    fun updateWith(entities: List<ExchangeRateDBEntity>) {
        deleteAll()
        insertAll(entities)
    }
}