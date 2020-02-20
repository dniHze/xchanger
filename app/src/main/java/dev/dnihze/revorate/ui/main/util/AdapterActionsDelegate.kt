package dev.dnihze.revorate.ui.main.util

import dev.dnihze.revorate.model.CurrencyAmount

interface AdapterActionsDelegate {
    fun onNewCurrency(currencyAmount: CurrencyAmount)
}