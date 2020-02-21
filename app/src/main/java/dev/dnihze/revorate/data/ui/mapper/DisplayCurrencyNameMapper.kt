package dev.dnihze.revorate.data.ui.mapper

import androidx.annotation.StringRes
import dev.dnihze.revorate.R
import dev.dnihze.revorate.utils.common.Mapper
import dev.dnihze.revorate.model.Currency
import javax.inject.Inject


class DisplayCurrencyNameMapper @Inject constructor() : Mapper<Currency, Int> {

    @StringRes
    override fun map(from: Currency): Int {
        return when (from) {
            Currency.AUD -> R.string.currency_name_aud
            Currency.BGN -> R.string.currency_name_bgn
            Currency.BRL -> R.string.currency_name_brl
            Currency.CAD -> R.string.currency_name_cad
            Currency.CHF -> R.string.currency_name_chf
            Currency.CNY -> R.string.currency_name_cny
            Currency.CZK -> R.string.currency_name_czk
            Currency.DKK -> R.string.currency_name_dkk
            Currency.EUR -> R.string.currency_name_eur
            Currency.GBP -> R.string.currency_name_gbp
            Currency.HKD -> R.string.currency_name_hkd
            Currency.HRK -> R.string.currency_name_hrk
            Currency.HUF -> R.string.currency_name_huf
            Currency.IDR -> R.string.currency_name_idr
            Currency.ILS -> R.string.currency_name_ils
            Currency.INR -> R.string.currency_name_inr
            Currency.ISK -> R.string.currency_name_isk
            Currency.JPY -> R.string.currency_name_jpy
            Currency.KRW -> R.string.currency_name_krw
            Currency.MXN -> R.string.currency_name_mxn
            Currency.MYR -> R.string.currency_name_myr
            Currency.NOK -> R.string.currency_name_nok
            Currency.NZD -> R.string.currency_name_nzd
            Currency.PHP -> R.string.currency_name_php
            Currency.PLN -> R.string.currency_name_pln
            Currency.RON -> R.string.currency_name_ron
            Currency.RUB -> R.string.currency_name_rub
            Currency.SEK -> R.string.currency_name_sek
            Currency.SGD -> R.string.currency_name_sgd
            Currency.THB -> R.string.currency_name_thb
            Currency.USD -> R.string.currency_name_usd
            Currency.ZAR -> R.string.currency_name_zar
        }
    }
}