package dev.dnihze.revorate.model.network

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.math.BigDecimal

@JsonClass(generateAdapter = true)
data class CurrencyRatesResponse(
    @Json(name = "baseCurrency") val baseCurrency: String,
    @Json(name = "rates") val exchangeRates: Map<String, Double>
)