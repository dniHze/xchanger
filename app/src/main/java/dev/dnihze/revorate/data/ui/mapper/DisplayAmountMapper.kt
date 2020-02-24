package dev.dnihze.revorate.data.ui.mapper

import androidx.collection.SparseArrayCompat
import dev.dnihze.revorate.utils.common.Mapper
import dev.dnihze.revorate.model.CurrencyAmount
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DisplayAmountMapper @Inject constructor(): Mapper<CurrencyAmount, CharSequence> {

    private val patternCache by lazy { SparseArrayCompat<String>() }

    override fun map(from: CurrencyAmount): CharSequence {
        val formatPattern = getPattern(from)
        val decimalFormat = NumberFormat.getNumberInstance(Locale.getDefault()) as DecimalFormat
        decimalFormat.roundingMode = RoundingMode.DOWN
        decimalFormat.applyPattern(formatPattern)
        return decimalFormat.format(from.amount)
    }

    private fun getPattern(amount: CurrencyAmount): String {
        val currency = amount.currency
        val digitsAfterSeparator = currency.digitsAfterSeparator.takeUnless {count ->
            count > 0 && amount.amount.toLong().toDouble() == amount.amount
        } ?: 0

        val pattern = patternCache[digitsAfterSeparator]
        if (pattern != null) {
            return pattern
        }

        val newPattern = buildString {
            append("##0")
            if (digitsAfterSeparator > 0) {
                append('.')
                repeat(digitsAfterSeparator) {
                    append('0')
                }
            }
        }
        patternCache.put(digitsAfterSeparator, newPattern)
        return newPattern
    }
}