package dev.dnihze.revorate.data.mapper

import androidx.collection.LruCache
import dev.dnihze.revorate.utils.common.Mapper
import dev.dnihze.revorate.model.Currency
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CurrencyISONameMapper @Inject constructor(): Mapper<String, Currency?> {

    private var currencyCache: LruCache<String, Currency?>? = null

    override fun map(from: String): Currency? {
        return currencyCache?.get(from) ?: getCurrencyForTag(from)
    }

    private fun getCurrencyForTag(isoName: String): Currency? {
        val currency = Currency.values().find {
            it.isoName.equals(isoName, ignoreCase = true)
        }

        if (currency != null) {
            synchronized(this) {
                if (currencyCache == null) {
                    currencyCache = LruCache(Currency.values().size)
                }
            }
            currencyCache?.put(isoName, currency)
        }

        return currency
    }
}