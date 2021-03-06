package dev.dnihze.revorate.redux.main

import dev.dnihze.revorate.model.Currency
import dev.dnihze.revorate.model.CurrencyAmount
import dev.dnihze.revorate.model.ExchangeTable
import dev.dnihze.revorate.model.ui.main.CurrencyDisplayItem

sealed class MainScreenState {

    data class LoadingState(
        val loaded: Boolean = false,
        val restoreFreeInput: CharSequence?
    ) : MainScreenState() {
        override fun getFreeInput() = restoreFreeInput
    }

    data class DisplayState(
        val currentAmount: CurrencyAmount,
        val exchangeTable: ExchangeTable,
        val displayItems: List<CurrencyDisplayItem>,
        val scrollToFirst: Boolean,
        val loading: Boolean,
        val error: ErrorHolder?
    ) : MainScreenState() {

        fun toErrorState(error: MainScreenError): DisplayState {
            val currentError = this.error?.error
            return if (error != currentError) {
                copy(
                    scrollToFirst = false,
                    loading = false,
                    error = ErrorHolder(error)
                )
            } else {
                copy(
                    scrollToFirst = false,
                    loading = false,
                    error = dropDownErrorState()
                )
            }
        }

        fun toLoadingState(): DisplayState {
            return copy(
                scrollToFirst = false,
                loading = true,
                error = null
            )
        }

        fun toDisplayState(): DisplayState {
            return copy(
                scrollToFirst = false,
                loading = false,
                error = null
            )
        }

        fun dropDownErrorState(): ErrorHolder? {
            return error?.copy(isKnowIssue = true)
        }

        override fun getFreeInput(): CharSequence? = displayItems.firstOrNull()?.freeInput

        override fun toString(): String {
            return "DisplayState(currentAmount: $currentAmount; loading: $loading; error: $error)"
        }
    }

    data class ErrorState(
        var error: MainScreenError,
        val restoreFreeInput: CharSequence?
    ) : MainScreenState() {
        override fun toString(): String {
            return "ErrorState(error: $error)"
        }

        override fun getFreeInput() = restoreFreeInput
    }


    fun isErrorState(): Boolean = this is ErrorState || (this is DisplayState && error != null)
    fun isLoadingState(): Boolean = this is LoadingState || (this is DisplayState && loading)

    fun getCurrentCurrency(): Currency? {
        return when (this) {
            is DisplayState -> currentAmount.currency
            else -> null
        }
    }

    fun exchangeTable(): ExchangeTable? {
        return when (this) {
            is DisplayState -> exchangeTable
            else -> null
        }
    }

    abstract fun getFreeInput(): CharSequence?
}