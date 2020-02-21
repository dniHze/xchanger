package dev.dnihze.revorate.data.local.mapper

import dev.dnihze.revorate.utils.common.Mapper
import dev.dnihze.revorate.model.ExchangeTable
import dev.dnihze.revorate.model.local.ExchangeRateDBEntity
import java.math.RoundingMode
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExchangeRateLocalMapper @Inject constructor(): Mapper<ExchangeTable, List<ExchangeRateDBEntity>> {

    override fun map(from: ExchangeTable): List<ExchangeRateDBEntity> {
        return from.order().mapIndexed { index, rate ->
            ExchangeRateDBEntity(
                forCurrency = rate.forCurrency.isoCode,
                ofCurrency = rate.ofCurrency.isoCode,
                rate = rate.rate.toBigDecimal().setScale(10, RoundingMode.HALF_EVEN).toString(),
                order = index
            )
        }
    }
}