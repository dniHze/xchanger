package dev.dnihze.revorate.ui.main.delegate

import dev.dnihze.revorate.model.CurrencyAmount

interface AdapterActionsDelegate {
    fun onNewCurrency(currencyAmount: CurrencyAmount)
    fun onNewInput(input: String, previousAmount: CurrencyAmount)
}