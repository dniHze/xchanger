package dev.dnihze.revorate.redux.main

import com.freeletics.rxredux.StateAccessor
import com.freeletics.rxredux.reduxStore
import com.jakewharton.rxrelay2.PublishRelay
import com.jakewharton.rxrelay2.Relay
import dev.dnihze.revorate.data.lifecycle.AppStateObserver
import dev.dnihze.revorate.data.local.LocalDataSource
import dev.dnihze.revorate.data.network.NetworkDataSource
import dev.dnihze.revorate.data.platform.ConnectionWatcher
import dev.dnihze.revorate.data.ui.MainScreenListFactory
import dev.dnihze.revorate.model.Currency
import dev.dnihze.revorate.model.CurrencyAmount
import dev.dnihze.revorate.model.ExchangeTable
import dev.dnihze.revorate.model.lifecycle.AppState
import dev.dnihze.revorate.model.network.exception.ApiException
import dev.dnihze.revorate.utils.ext.retryWithExponentialBackoff
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class MainScreenStateMachine @Inject constructor(
    private val networkDataSource: NetworkDataSource,
    private val localDataSource: LocalDataSource,
    private val mainScreenListFactory: MainScreenListFactory,
    private val connectionWatcher: ConnectionWatcher,
    private val appStateObserver: AppStateObserver
) {

    private val tag: String
        get() = this::class.java.simpleName

    val input: Relay<MainScreenAction> = PublishRelay.create()

    val state: Observable<MainScreenState> = input
        .doOnNext {
            Timber.tag(tag).i("New input action -> $it")
        }
        .reduxStore(
            initialState = MainScreenState.LoadingState,
            reducer = ::reducer,
            sideEffects = listOf(
                ::networkLoadSideEffect,
                ::requestDBUpdatesSideEffect,
                ::initHeartBeatSideEffect,
                ::initConnectionWatcherSideEffect,
                ::connectivitySideEffect,
                ::currencyChangedSideEffect,
                ::retrySideEffect
            )
        )
        .distinctUntilChanged()


    @Suppress("UNUSED_PARAMETER")
    private fun requestDBUpdatesSideEffect(
        actions: Observable<MainScreenAction>,
        state: StateAccessor<MainScreenState>
    ): Observable<MainScreenAction> {
        return actions.ofType(MainScreenAction.InitScreen::class.java)
            .switchMap {
                localDataSource.getLocalExchangeTable()
                    .subscribeOn(Schedulers.io())
            }
            .filter { table -> !table.isEmpty() }
            .map { table -> MainScreenAction.LocalDBTableLoaded(table) as MainScreenAction }
            .startWith(MainScreenAction.LoadNetworkTable(null))
    }

    private fun initHeartBeatSideEffect(
        actions: Observable<MainScreenAction>,
        state: StateAccessor<MainScreenState>
    ): Observable<MainScreenAction> {
        return actions.ofType(MainScreenAction.InitScreen::class.java)
            .switchMap {
                appStateObserver.observe()
            }
            .switchMap { appState ->
                if (appState == AppState.FOREGROUND) {
                    Observable.interval(1L, 1L, TimeUnit.SECONDS)
                        .subscribeOn(Schedulers.computation())
                        .filter {
                            val currentState = state()
                            !currentState.isErrorState() && !currentState.isLoadingState()
                        }
                        .map {
                            MainScreenAction.LoadNetworkTable(state().getCurrentCurrency()) as MainScreenAction
                        }
                        .doOnNext {
                            Timber.tag(tag).i("HeartBeat sent")
                        }
                } else {
                    Observable.never()
                }
            }
    }

    @Suppress("UNUSED_PARAMETER")
    private fun initConnectionWatcherSideEffect(
        actions: Observable<MainScreenAction>,
        state: StateAccessor<MainScreenState>
    ): Observable<MainScreenAction> {
        return actions.ofType(MainScreenAction.InitScreen::class.java)
            .switchMap {
                connectionWatcher.watch()
                    .distinctUntilChanged()
                    .doOnNext { Timber.tag(tag).i("New connection status -> $it") }
                    .subscribeOn(Schedulers.computation())
                    .map { networkConnection -> MainScreenAction.ConnectivityChanged(networkConnection) }
            }
    }

    private fun connectivitySideEffect(
        actions: Observable<MainScreenAction>,
        state: StateAccessor<MainScreenState>
    ): Observable<MainScreenAction> {
        return actions.ofType(MainScreenAction.ConnectivityChanged::class.java)
            .filter { connectivity ->
                connectivity.connection.isAvailable() && state().isErrorState()
            }
            .map { MainScreenAction.Retry as MainScreenAction }
    }

    private fun networkLoadSideEffect(
        actions: Observable<MainScreenAction>,
        state: StateAccessor<MainScreenState>
    ): Observable<MainScreenAction> {
        return actions.ofType(MainScreenAction.LoadNetworkTable::class.java)
            .doOnNext { Timber.tag(tag).i("New network load event -> $it") }
            .switchMapSingle {
                networkDataSource.getExchangeTable(Currency.EUR)
                    .subscribeOn(Schedulers.io())
                    .retryWithExponentialBackoff()
            }
            .switchMapSingle { loadedExchangeTable ->
                getExchangeTable(state)
                    .map { localTable -> localTable to loadedExchangeTable }
            }
            .switchMapSingle { (localTable, networkTable) ->
                if (localTable.isEmpty()) {
                    localDataSource.saveExchangeTable(networkTable.order())
                        .subscribeOn(Schedulers.io())
                } else {
                    val baseCurrency = localTable.baseCurrency
                        ?: throw IllegalStateException("Non empty table can have empty base currency")
                    val table = networkTable.newTableFor(baseCurrency)
                        ?: throw IllegalStateException("Table can't be created")
                    localDataSource.saveExchangeTable(table.orderWith(localTable))
                        .subscribeOn(Schedulers.io())
                }.andThen(Single.just(MainScreenAction.NetworkTableLoaded as MainScreenAction))
            }
            .onErrorReturn { t -> MainScreenAction.Error(t) }

    }

    private fun currencyChangedSideEffect(
        actions: Observable<MainScreenAction>,
        state: StateAccessor<MainScreenState>
    ): Observable<MainScreenAction> {
        return actions.ofType(MainScreenAction.NewAmount::class.java)
            .distinctUntilChanged()
            .switchMap { currencySelected ->
                localDataSource.getLocalExchangeTable()
                    .subscribeOn(Schedulers.io())
                    .map { table -> table to currencySelected.amount }
            }
            .filter { (table, amount) -> !table.isEmpty() && table.baseCurrency != amount.currency }
            .switchMapSingle { (table, amount) ->
                localDataSource.saveExchangeTable(
                    table.newTableFor(amount.currency)
                        ?: throw IllegalStateException("Can't be created.")
                )
                    .subscribeOn(Schedulers.io())
                    .andThen(Single.fromCallable {
                        if (state().isErrorState()) {
                            MainScreenAction.Retry
                        } else {
                            MainScreenAction.LoadNetworkTable(amount.currency)
                        }
                    })
            }
            .onErrorReturn { t -> MainScreenAction.Error(t) }
    }

    private fun retrySideEffect(
        actions: Observable<MainScreenAction>,
        state: StateAccessor<MainScreenState>
    ): Observable<MainScreenAction> {
        return actions.ofType(MainScreenAction.Retry::class.java)
            .filter {
                !state().isLoadingState()
            }
            .map { MainScreenAction.LoadNetworkTable(state().getCurrentCurrency()) }
    }


    private fun reducer(state: MainScreenState, action: MainScreenAction): MainScreenState {
        Timber.tag(tag).i("State: $state;\nAction: $action;")
        val newState = when (state) {
            // Loading
            is MainScreenState.LoadingState -> {
                when (action) {
                    is MainScreenAction.LoadNetworkTable -> {
                        state
                    }
                    is MainScreenAction.LocalDBTableLoaded -> {
                        val table = action.exchangeTable.order()
                        val currency = table.baseCurrency
                        if (currency != null) {
                            val amount = CurrencyAmount(1.0, currency)
                            MainScreenState.LoadAndDisplayState(
                                currentAmount = amount,
                                exchangeTable = table,
                                displayItems = mainScreenListFactory.create(
                                    table, amount
                                )
                            )
                        } else {
                            state
                        }
                    }
                    is MainScreenAction.Error -> MainScreenState.ErrorState(
                        action.throwable.toError()
                    )
                    is MainScreenAction.ConnectivityChanged -> {
                        if (action.connection.isAvailable()) {
                            state
                        } else {
                            MainScreenState.ErrorState(MainScreenError.NetworkConnectionError)
                        }
                    }
                    is MainScreenAction.InitScreen -> state
                    else -> state
                }
            }
            // Error
            is MainScreenState.ErrorState -> {
                when (action) {
                    is MainScreenAction.InitScreen -> MainScreenState.LoadingState
                    is MainScreenAction.Retry -> MainScreenState.LoadingState
                    is MainScreenAction.ConnectivityChanged -> {
                        if (!action.connection.isAvailable()) {
                            MainScreenState.ErrorState(MainScreenError.NetworkConnectionError)
                        } else {
                            state
                        }
                    }
                    else -> state
                }
            }
            // Display
            is MainScreenState.DisplayState -> {
                when (action) {
                    is MainScreenAction.InitScreen -> MainScreenState.LoadingState
                    is MainScreenAction.ConnectivityChanged -> {
                        if (!action.connection.isAvailable()) {
                            state.toErrorAndDisplayState(MainScreenError.NetworkConnectionError)
                        } else {
                            state
                        }
                    }
                    is MainScreenAction.LocalDBTableLoaded -> {
                        val amount = state.currentAmount
                        val table = action.exchangeTable.order().newTableFor(
                            amount.currency
                        ) ?: return state.toErrorAndDisplayState(
                            MainScreenError.Unknown(
                                IllegalStateException(
                                    "Error on converting table"
                                )
                            )
                        )
                        state.copy(
                            currentAmount = amount,
                            exchangeTable = table,
                            displayItems = mainScreenListFactory.create(
                                table, amount
                            )
                        )
                    }
                    is MainScreenAction.LoadNetworkTable -> state.toLoadingAndDisplayState()
                    is MainScreenAction.Error -> state.toErrorAndDisplayState(action.throwable.toError())
                    is MainScreenAction.NewAmount -> {
                        if (action.amount.currency == state.currentAmount.currency) {
                            // Currency is same, just recalculate
                            state.copy(
                                currentAmount = action.amount,
                                exchangeTable = state.exchangeTable,
                                displayItems = mainScreenListFactory.create(
                                    state.exchangeTable, action.amount
                                )
                            )
                        } else {
                            val table = state.exchangeTable.newTableFor(action.amount.currency)
                                ?: return state.toErrorAndDisplayState(
                                    MainScreenError.Unknown(
                                        IllegalStateException("Error on converting table")
                                    )
                                )
                            state.copy(
                                currentAmount = action.amount,
                                exchangeTable = table,
                                displayItems = mainScreenListFactory.create(
                                    table, action.amount
                                )
                            )
                        }
                    }
                    else -> state
                }
            }

            // Load and display
            is MainScreenState.LoadAndDisplayState -> {
                when (action) {
                    is MainScreenAction.InitScreen -> MainScreenState.LoadingState
                    is MainScreenAction.ConnectivityChanged -> {
                        if (!action.connection.isAvailable()) {
                            state.toErrorAndDisplayState(MainScreenError.NetworkConnectionError)
                        } else {
                            state
                        }
                    }
                    is MainScreenAction.LocalDBTableLoaded -> {
                        val amount = state.currentAmount
                        val table = action.exchangeTable.order().newTableFor(
                            amount.currency
                        ) ?: return state.toErrorAndDisplayState(
                            MainScreenError.Unknown(
                                IllegalStateException(
                                    "Error on converting table"
                                )
                            )
                        )
                        state.copy(
                            currentAmount = amount,
                            exchangeTable = table,
                            displayItems = mainScreenListFactory.create(
                                table, amount
                            )
                        )
                    }
                    is MainScreenAction.NetworkTableLoaded -> state.toDisplayState()
                    is MainScreenAction.Error -> state.toErrorAndDisplayState(action.throwable.toError())
                    is MainScreenAction.NewAmount -> {
                        if (action.amount.currency == state.currentAmount.currency) {
                            // Currency is same, just recalculate
                            state.copy(
                                currentAmount = action.amount,
                                exchangeTable = state.exchangeTable,
                                displayItems = mainScreenListFactory.create(
                                    state.exchangeTable, action.amount
                                )
                            )
                        } else {
                            val table = state.exchangeTable.newTableFor(action.amount.currency)
                                ?: return state.toErrorAndDisplayState(
                                    MainScreenError.Unknown(
                                        IllegalStateException("Error on converting table")
                                    )
                                )
                            state.copy(
                                currentAmount = action.amount,
                                exchangeTable = table,
                                displayItems = mainScreenListFactory.create(
                                    table, action.amount
                                )
                            )
                        }
                    }
                    else -> state
                }
            }

            // Error and display
            is MainScreenState.ErrorAndDisplayState -> {
                when (action) {
                    is MainScreenAction.InitScreen -> MainScreenState.LoadingState
                    is MainScreenAction.Retry -> state.toLoadingAndDisplayState()
                    is MainScreenAction.ConnectivityChanged -> {
                        if (!action.connection.isAvailable()) {
                            state.copy(error = MainScreenError.NetworkConnectionError)
                        } else {
                            state
                        }
                    }

                    is MainScreenAction.LocalDBTableLoaded -> {
                        val amount = state.currentAmount
                        val table = action.exchangeTable.order().newTableFor(
                            amount.currency
                        ) ?: return state.copy(
                            error = MainScreenError.Unknown(
                                IllegalStateException(
                                    "Error on converting table"
                                )
                            )
                        )
                        state.copy(
                            currentAmount = amount,
                            exchangeTable = table,
                            displayItems = mainScreenListFactory.create(
                                table, amount
                            )
                        )
                    }
                    is MainScreenAction.Error -> state.copy(error = action.throwable.toError())
                    is MainScreenAction.NewAmount -> {
                        if (action.amount.currency == state.currentAmount.currency) {
                            // Currency is same, just recalculate
                            state.copy(
                                currentAmount = action.amount,
                                exchangeTable = state.exchangeTable,
                                displayItems = mainScreenListFactory.create(
                                    state.exchangeTable, action.amount
                                )
                            )
                        } else {
                            val table = state.exchangeTable.newTableFor(action.amount.currency)
                                ?: return state.copy(
                                    error = MainScreenError.Unknown(
                                        IllegalStateException("Error on converting table")
                                    )
                                )
                            state.copy(
                                currentAmount = action.amount,
                                exchangeTable = table,
                                displayItems = mainScreenListFactory.create(
                                    table, action.amount
                                )
                            )
                        }
                    }
                    else -> state
                }
            }
        }

        Timber.tag(tag).i("New state: $newState;")
        return newState
    }

    private fun getExchangeTable(
        state: StateAccessor<MainScreenState>
    ): Single<ExchangeTable> {
        val currentExchangeTable = state().exchangeTable()
        return if (currentExchangeTable == null) {
            localDataSource.getSingleExchangeTable()
                .subscribeOn(Schedulers.io())
        } else {
            Single.just(currentExchangeTable)
        }
    }

    private fun Throwable.toError(): MainScreenError {
        return when (this) {
            is ApiException -> when {
                isHttpException() || isIOException() -> MainScreenError.ApiError(this)
                else -> MainScreenError.Unknown(this)
            }
            else -> MainScreenError.Unknown(this)
        }
    }
}