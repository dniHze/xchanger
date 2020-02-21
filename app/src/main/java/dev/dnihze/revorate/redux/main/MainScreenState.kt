package dev.dnihze.revorate.redux.main

import dev.dnihze.revorate.model.Currency
import dev.dnihze.revorate.model.CurrencyAmount
import dev.dnihze.revorate.model.ExchangeTable
import dev.dnihze.revorate.model.ui.main.CurrencyDisplayItem

sealed class MainScreenState {

    object LoadingState : MainScreenState() {
        override fun toString(): String {
            return "LoadingState"
        }
    }

    data class DisplayState(
        val currentAmount: CurrencyAmount,
        val exchangeTable: ExchangeTable,
        val displayItems: List<CurrencyDisplayItem>
    ) : MainScreenState() {

        fun toErrorAndDisplayState(error: MainScreenError): ErrorAndDisplayState {
            return ErrorAndDisplayState(
                currentAmount,
                exchangeTable,
                displayItems,
                error
            )
        }

        fun toLoadingAndDisplayState(): LoadAndDisplayState {
            return LoadAndDisplayState(
                currentAmount,
                exchangeTable,
                displayItems
            )
        }

        override fun toString(): String {
            return "DisplayState(currentAmount: $currentAmount)"
        }
    }

    data class LoadAndDisplayState(
        val currentAmount: CurrencyAmount,
        val exchangeTable: ExchangeTable,
        val displayItems: List<CurrencyDisplayItem>
    ) : MainScreenState() {

        fun toErrorAndDisplayState(error: MainScreenError): ErrorAndDisplayState {
            return ErrorAndDisplayState(
                currentAmount,
                exchangeTable,
                displayItems,
                error
            )
        }

        fun toDisplayState(): DisplayState {
            return DisplayState(
                currentAmount,
                exchangeTable,
                displayItems
            )
        }

        override fun toString(): String {
            return "LoadAndDisplayState(currentAmount: $currentAmount)"
        }
    }

    data class ErrorState(
        var error: MainScreenError
    ) : MainScreenState() {
        override fun toString(): String {
            return "ErrorState(error: $error)"
        }
    }

    data class ErrorAndDisplayState(
        val currentAmount: CurrencyAmount,
        val exchangeTable: ExchangeTable,
        val displayItems: List<CurrencyDisplayItem>,
        val error: MainScreenError
    ) : MainScreenState() {

        fun toLoadingAndDisplayState(): LoadAndDisplayState {
            return LoadAndDisplayState(
                currentAmount,
                exchangeTable,
                displayItems
            )
        }

        override fun toString(): String {
            return "ErrorAndDisplayState(currentAmount: $currentAmount, error: $error)"
        }
    }

    fun isErrorState(): Boolean = this is ErrorState || this is ErrorAndDisplayState
    fun isLoadingState(): Boolean = this is LoadingState || this is LoadAndDisplayState
    fun isDisplayState(): Boolean =
        this is DisplayState || this is LoadAndDisplayState || this is ErrorAndDisplayState

    fun getCurrentCurrency(): Currency? {
        return when (this) {
            is DisplayState -> currentAmount.currency
            is LoadAndDisplayState -> currentAmount.currency
            is ErrorAndDisplayState -> currentAmount.currency
            else -> null
        }
    }

    fun exchangeTable(): ExchangeTable? {
        return when (this) {
            is DisplayState -> exchangeTable
            is LoadAndDisplayState -> exchangeTable
            is ErrorAndDisplayState -> exchangeTable
            else -> null
        }
    }
}