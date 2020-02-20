package dev.dnihze.revorate.redux.main

import dev.dnihze.revorate.model.Currency
import dev.dnihze.revorate.model.CurrencyAmount
import dev.dnihze.revorate.model.ExchangeTable
import dev.dnihze.revorate.model.platform.NetworkConnection

sealed class MainScreenAction {
    object InitScreen: MainScreenAction()
    data class NewAmount(val amount: CurrencyAmount): MainScreenAction()
    data class ConnectivityChanged(val connection: NetworkConnection): MainScreenAction()
    object NetworkTableLoaded: MainScreenAction()
    data class LocalDBTableLoaded(val exchangeTable: ExchangeTable): MainScreenAction()
    data class LoadNetworkTable(val currency: Currency?): MainScreenAction()
    data class Error(val throwable: Throwable): MainScreenAction()
    object Retry: MainScreenAction()
    data class ApiHeartbeat(val timeInMillis: Long): MainScreenAction()
}