package dev.dnihze.revorate.data.ui.mapper

import androidx.annotation.StringRes
import dev.dnihze.revorate.R
import dev.dnihze.revorate.common.Mapper
import dev.dnihze.revorate.model.Currency
import javax.inject.Inject

class DisplayCurrencyFlagMapper @Inject constructor() : Mapper<Currency, Int> {

    @StringRes
    override fun map(from: Currency): Int {
        return when (from) {
            Currency.AUD -> R.string.currency_emoji_aud
            Currency.BGN -> R.string.currency_emoji_bgn
            Currency.BRL -> R.string.currency_emoji_brl
            Currency.CAD -> R.string.currency_emoji_cad
            Currency.CHF -> R.string.currency_emoji_chf
            Currency.CNY -> R.string.currency_emoji_cny
            Currency.CZK -> R.string.currency_emoji_czk
            Currency.DKK -> R.string.currency_emoji_dkk
            Currency.EUR -> R.string.currency_emoji_eur
            Currency.GBP -> R.string.currency_emoji_gbp
            Currency.HKD -> R.string.currency_emoji_hkd
            Currency.HRK -> R.string.currency_emoji_hrk
            Currency.HUF -> R.string.currency_emoji_huf
            Currency.IDR -> R.string.currency_emoji_idr
            Currency.ILS -> R.string.currency_emoji_ils
            Currency.INR -> R.string.currency_emoji_inr
            Currency.ISK -> R.string.currency_emoji_isk
            Currency.JPY -> R.string.currency_emoji_jpy
            Currency.KRW -> R.string.currency_emoji_krw
            Currency.MXN -> R.string.currency_emoji_mxn
            Currency.MYR -> R.string.currency_emoji_myr
            Currency.NOK -> R.string.currency_emoji_nok
            Currency.NZD -> R.string.currency_emoji_nzd
            Currency.PHP -> R.string.currency_emoji_php
            Currency.PLN -> R.string.currency_emoji_pln
            Currency.RON -> R.string.currency_emoji_ron
            Currency.RUB -> R.string.currency_emoji_rub
            Currency.SEK -> R.string.currency_emoji_sek
            Currency.SGD -> R.string.currency_emoji_sgd
            Currency.THB -> R.string.currency_emoji_thb
            Currency.USD -> R.string.currency_emoji_usd
            Currency.ZAR -> R.string.currency_emoji_zar
        }
    }
}