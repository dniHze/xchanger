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
import dev.dnihze.revorate.ui.main.navigation.NetworkSettingsScreen
import dev.dnihze.revorate.utils.ext.retryWithExponentialBackoff
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.functions.Function
import io.reactivex.schedulers.Schedulers
import ru.terrakok.cicerone.Router
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class MainScreenStateMachine @Inject constructor(
    private val networkDataSource: NetworkDataSource,
    private val localDataSource: LocalDataSource,
    private val mainScreenListFactory: MainScreenListFactory,
    private val connectionWatcher: ConnectionWatcher,
    private val appStateObserver: AppStateObserver,
    private val router: Router
) {

    private val tag: String
        get() = this::class.java.simpleName

    val input: Relay<MainScreenAction> = PublishRelay.create()

    val state: Observable<MainScreenState> = input
        .doOnNext {
            Timber.tag(tag).i("New input action -> $it")
        }
        .reduxStore(
            initialState = MainScreenState.LoadingState(),
            reducer = ::reducer,
            sideEffects = listOf(
                ::networkLoadSideEffect,
                ::requestDBUpdatesSideEffect,
                ::initHeartBeatSideEffect,
                ::initConnectionWatcherSideEffect,
                ::connectivitySideEffect,
                ::currencyChangedSideEffect,
                ::retrySideEffect,
                ::newInputSideEffect,
                ::openNetworkSettingSideEffect
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
                    .filter { table -> !table.isEmpty() }
                    .map { table -> MainScreenAction.LocalDBTableLoaded(table) as MainScreenAction }
                    .onErrorResumeNext(Function { t -> Observable.just(MainScreenAction.Error(t)) })
            }
            .startWith(MainScreenAction.LoadNetworkTable)
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
                            MainScreenAction.LoadNetworkTable as MainScreenAction
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
                    .flatMap { loadedExchangeTable ->
                        getExchangeTable(state)
                            .map { localTable -> localTable to loadedExchangeTable }
                    }
                    .flatMap { (localTable, networkTable) ->
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
                    .onErrorResumeNext { t -> Single.just(MainScreenAction.Error(t)) }
            }
    }

    private fun currencyChangedSideEffect(
        actions: Observable<MainScreenAction>,
        state: StateAccessor<MainScreenState>
    ): Observable<MainScreenAction> {
        return actions.ofType(MainScreenAction.NewAmount::class.java)
            .distinctUntilChanged()
            .switchMapMaybe { currencySelected ->
                localDataSource.getSingleExchangeTable()
                    .subscribeOn(Schedulers.io())
                    .map { table -> table to currencySelected.amount }
                    .filter { (table, amount) -> !table.isEmpty() && table.baseCurrency != amount.currency }
                    .flatMapSingleElement { (table, amount) ->
                        localDataSource.saveExchangeTable(
                            table.newTableFor(amount.currency)
                                ?: throw IllegalStateException("Can't be created.")
                        ).andThen(Single.fromCallable {
                            if (state().isErrorState()) {
                                MainScreenAction.Retry
                            } else {
                                MainScreenAction.LoadNetworkTable
                            }
                        })
                    }
                    .onErrorResumeNext(Function { t -> Maybe.just(MainScreenAction.Error(t))})
            }
    }

    @Suppress("UNUSED_PARAMETER")
    private fun retrySideEffect(
        actions: Observable<MainScreenAction>,
        state: StateAccessor<MainScreenState>
    ): Observable<MainScreenAction> {
        return actions.ofType(MainScreenAction.Retry::class.java)
            .map { MainScreenAction.LoadNetworkTable }
    }

    @Suppress("UNUSED_PARAMETER")
    private fun newInputSideEffect(
        actions: Observable<MainScreenAction>,
        state: StateAccessor<MainScreenState>
    ): Observable<MainScreenAction> {
        return actions.ofType(MainScreenAction.NewInput::class.java)
            .map { input ->
                val inputNumber = input.input.toDoubleOrNull() ?: 0.0
                inputNumber to input.previousAmount
            }
            .filter { (inputNumber, previousAmount) ->
                inputNumber != previousAmount.amount && previousAmount.currency == state().getCurrentCurrency()
            }
            .map { (inputNumber, previousAmount) ->
                MainScreenAction.NewAmount(CurrencyAmount(inputNumber, previousAmount.currency))
            }
    }

    @Suppress("UNUSED_PARAMETER")
    private fun openNetworkSettingSideEffect(
        actions: Observable<MainScreenAction>,
        state: StateAccessor<MainScreenState>
    ): Observable<MainScreenAction> {
        return actions.ofType(MainScreenAction.NetworkSettings::class.java)
            .switchMap {
                router.navigateTo(NetworkSettingsScreen())
                Observable.never<MainScreenAction>()
            }
    }

    private fun reducer(state: MainScreenState, action: MainScreenAction): MainScreenState {
        Timber.tag(tag).i("State: $state;\nAction: $action;")
        val newState = when (state) {
            // Loading
            is MainScreenState.LoadingState -> {
                when (action) {
                    is MainScreenAction.LoadNetworkTable -> {
                        state.copy(loaded = true)
                    }
                    is MainScreenAction.LocalDBTableLoaded -> {
                        val table = action.exchangeTable.order()
                        val currency = table.baseCurrency
                        if (currency != null) {
                            val amount = CurrencyAmount(1.0, currency)
                            MainScreenState.DisplayState(
                                currentAmount = amount,
                                exchangeTable = table,
                                displayItems = mainScreenListFactory.create(
                                    table, amount, null
                                ),
                                scrollToFirst = false,
                                loading = !state.loaded,
                                error = null
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
                    else -> state
                }
            }
            // Error
            is MainScreenState.ErrorState -> {
                when (action) {
                    is MainScreenAction.Retry -> MainScreenState.LoadingState()
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
                    is MainScreenAction.ConnectivityChanged -> {
                        if (!action.connection.isAvailable()) {
                            state.toErrorState(MainScreenError.NetworkConnectionError)
                        } else {
                            state
                        }
                    }
                    is MainScreenAction.LocalDBTableLoaded -> {
                        val amount = state.currentAmount
                        val table = action.exchangeTable.order().newTableFor(
                            amount.currency
                        ) ?: return state.toErrorState(
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
                                table, amount, state
                            ),
                            scrollToFirst = false
                        )
                    }
                    is MainScreenAction.Retry -> state.toLoadingState()
                    is MainScreenAction.NewInput -> {
                        if (state.getFreeInput() != action.input &&
                            state.currentAmount.currency == action.previousAmount.currency) {
                            state.copy(
                                displayItems = state.displayItems.mapIndexed { index, currencyDisplayItem ->
                                    if (index == 0) {
                                        currencyDisplayItem.copy(freeInput = action.input)
                                    } else {
                                        currencyDisplayItem
                                    }
                                },
                                scrollToFirst = false
                            )
                        } else {
                            state
                        }
                    }
                    is MainScreenAction.LoadNetworkTable -> state.toLoadingState()
                    is MainScreenAction.NetworkTableLoaded -> state.toDisplayState()
                    is MainScreenAction.Error -> {
                        val error = action.throwable.toError()
                        if (error is MainScreenError.Unknown) {
                            MainScreenState.ErrorState(error)
                        } else {
                            state.toErrorState(action.throwable.toError())
                        }
                    }
                    is MainScreenAction.NewAmount -> {
                        if (action.amount.currency == state.currentAmount.currency) {
                            // Currency is same, just recalculate
                            state.copy(
                                currentAmount = action.amount,
                                exchangeTable = state.exchangeTable,
                                displayItems = mainScreenListFactory.create(
                                    state.exchangeTable, action.amount, state
                                ),
                                scrollToFirst = false
                            )
                        } else {
                            val table = state.exchangeTable.newTableFor(action.amount.currency)
                                ?: return state.toErrorState(
                                    MainScreenError.Unknown(
                                        IllegalStateException("Error on converting table")
                                    )
                                )
                            state.copy(
                                currentAmount = action.amount,
                                exchangeTable = table,
                                displayItems = mainScreenListFactory.create(
                                    table, action.amount, null
                                ),
                                scrollToFirst = true
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