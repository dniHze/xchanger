package dev.dnihze.revorate.redux.main

import dev.dnihze.revorate.model.CurrencyAmount
import dev.dnihze.revorate.model.ExchangeTable
import dev.dnihze.revorate.model.platform.NetworkConnection

sealed class MainScreenAction {
    object InitScreen : MainScreenAction()

    data class NewInput(val input: String, val previousAmount: CurrencyAmount): MainScreenAction()

    data class NewAmount(val amount: CurrencyAmount) : MainScreenAction()

    data class ConnectivityChanged(val connection: NetworkConnection) : MainScreenAction()

    object NetworkTableLoaded : MainScreenAction()

    data class LocalDBTableLoaded(val exchangeTable: ExchangeTable) : MainScreenAction() {
        override fun toString(): String {
            return "LocalDBTableLoaded"
        }
    }

    object LoadNetworkTable : MainScreenAction()

    data class Error(val throwable: Throwable) : MainScreenAction()

    object Retry : MainScreenAction()

    object NetworkSettings: MainScreenAction()

    data class RestoreInput(val freeInput: CharSequence) : MainScreenAction()
}