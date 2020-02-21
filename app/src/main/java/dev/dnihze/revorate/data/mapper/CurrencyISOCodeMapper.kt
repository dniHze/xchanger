package dev.dnihze.revorate.data.mapper

import androidx.collection.LruCache
import dev.dnihze.revorate.utils.common.Mapper
import dev.dnihze.revorate.model.Currency
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CurrencyISOCodeMapper @Inject constructor(): Mapper<Int, Currency?> {

    private var currencyCache: LruCache<Int, Currency?>? = null

    override fun map(from: Int): Currency? {
        return currencyCache?.get(from) ?: getCurrencyForTag(from)
    }

    private fun getCurrencyForTag(isoCode: Int): Currency? {
        assert(isoCode > 0) {
            "Only positive ISO currency codes exists. Given code: $isoCode."
        }

        val currency = Currency.values().find {
            it.isoCode == isoCode
        }

        if (currency != null) {
            synchronized(this) {
                if (currencyCache == null) {
                    currencyCache = LruCache(Currency.values().size)
                }
            }
            currencyCache?.put(isoCode, currency)
        }

        return currency
    }
}