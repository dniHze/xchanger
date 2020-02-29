package dev.dnihze.revorate.ui.main

import android.os.Bundle
import androidx.lifecycle.*
import androidx.savedstate.SavedStateRegistryOwner
import com.jakewharton.rxrelay2.PublishRelay
import com.jakewharton.rxrelay2.Relay
import dev.dnihze.revorate.model.CurrencyAmount
import dev.dnihze.revorate.redux.main.MainScreenAction
import dev.dnihze.revorate.redux.main.MainScreenState
import dev.dnihze.revorate.redux.main.MainScreenStateMachine
import dev.dnihze.revorate.ui.main.delegate.AdapterActionsDelegate
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.Consumer
import java.lang.IllegalArgumentException
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
                .subscribe { state ->
                    mutableState.value = state
                    saveDataToHandleIfNeeded(state)
                }
        )

        restoreInputStateIfAble()
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

    private fun saveDataToHandleIfNeeded(state: MainScreenState) {
        val input = state.getFreeInput()?.toString() ?: return
        savedStateHandle[KEY_FREE_INPUT] = input
    }

    private fun restoreInputStateIfAble() {
        if (savedStateHandle.contains(KEY_FREE_INPUT)) {
            val freeInput: String = savedStateHandle[KEY_FREE_INPUT] ?: return
            input.accept(MainScreenAction.RestoreInput(freeInput))
        }
    }

    companion object {
        private const val KEY_FREE_INPUT = "free_input"
    }

    class Factory @Inject constructor(
        private val stateMachine: Provider<MainScreenStateMachine>
    ) {
        fun create(
            owner: SavedStateRegistryOwner, defaultArgs: Bundle? = null
        ) = object : AbstractSavedStateViewModelFactory(owner, defaultArgs) {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel?> create(
                key: String,
                modelClass: Class<T>,
                handle: SavedStateHandle
            ): T {
                if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
                    return MainViewModel(stateMachine.get(), handle) as T
                } else {
                    throw IllegalArgumentException("Unknown ViewModel class $modelClass")
                }
            }
        }
    }
}