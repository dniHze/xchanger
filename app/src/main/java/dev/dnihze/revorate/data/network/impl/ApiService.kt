package dev.dnihze.revorate.data.network.impl

import dev.dnihze.revorate.model.network.CurrencyRatesResponse
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {
    @GET("api/android/latest")
    fun getExchangeRates(
        @Query("base") baseCurrency: String
    ): Single<CurrencyRatesResponse>
}