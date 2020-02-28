package dev.dnihze.revorate.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.jakewharton.rxrelay2.PublishRelay
import com.jakewharton.rxrelay2.Relay
import dev.dnihze.revorate.model.CurrencyAmount
import dev.dnihze.revorate.redux.main.MainScreenAction
import dev.dnihze.revorate.redux.main.MainScreenState
import dev.dnihze.revorate.redux.main.MainScreenStateMachine
import dev.dnihze.revorate.ui.main.delegate.AdapterActionsDelegate
import dev.dnihze.revorate.utils.viewmodel.AssistedViewModelFactory
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.Consumer
import javax.inject.Inject
import javax.inject.Provider

class MainViewModel(
    stateMachine: MainScreenStateMachine,
    private val savedStateHandle: SavedStateHandle
): ViewModel(), AdapterActionsDelegate {

    private val inputRelay: Relay<MainScreenAction> = PublishRelay.create()
    private val mutableState = MutableLiveData<MainScreenState>()
    private val disposables = CompositeDisposable()

    val input: Consumer<MainScreenAction> = inputRelay
    val state: LiveData<MainScreenState> = mutableState

    init {
        disposables.add(inputRelay.subscribe(stateMachine.input))

        disposables.add(
            stateMachine.state
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { state -> mutableState.value = state }
        )
    }

    override fun onCleared() {
        super.onCleared()
        disposables.clear()
    }

    override fun onNewInput(input: String, previousAmount: CurrencyAmount) {
        this.input.accept(MainScreenAction.NewInput(input, previousAmount))
    }

    override fun onNewCurrency(currencyAmount: CurrencyAmount) {
        input.accept(MainScreenAction.NewAmount(currencyAmount))
    }

    class Factory @Inject constructor(
        private val stateMachine: Provider<MainScreenStateMachine>
    ): AssistedViewModelFactory<MainViewModel> {
        override fun create(savedStateHandle: SavedStateHandle): MainViewModel {
            return MainViewModel(stateMachine.get(), savedStateHandle)
        }
    }
}