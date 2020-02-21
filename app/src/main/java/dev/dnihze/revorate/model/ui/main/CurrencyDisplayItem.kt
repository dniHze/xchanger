package dev.dnihze.revorate.model.ui.main

import androidx.annotation.StringRes
import dev.dnihze.revorate.model.CurrencyAmount

data class CurrencyDisplayItem(
    val displayAmount: CharSequence,
    val freeInput: CharSequence?,
    @field:StringRes @param:StringRes @get:StringRes val currencyFullNameId: Int,
    @field:StringRes @param:StringRes @get:StringRes val currencyFlagEmojiId: Int,
    val amount: CurrencyAmount,
    val inputEnabled: Boolean
) {

    val id: Long
        get() = amount.currency.isoCode.toLong()
}