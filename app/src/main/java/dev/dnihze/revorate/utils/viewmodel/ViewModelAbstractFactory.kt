package dev.dnihze.revorate.utils.viewmodel

import android.os.Bundle
import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.savedstate.SavedStateRegistryOwner
import dagger.Reusable
import java.lang.Exception
import java.lang.IllegalArgumentException
import java.lang.RuntimeException
import javax.inject.Inject

class ViewModelAbstractFactory @Inject constructor(
    private val assistedFactories: @JvmSuppressWildcards Map<Class<out ViewModel>,  AssistedViewModelFactory<out ViewModel>>
) {

    fun create(owner: SavedStateRegistryOwner, defaultArgs: Bundle?) =
        object : AbstractSavedStateViewModelFactory(owner, defaultArgs) {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel?> create(
                key: String,
                modelClass: Class<T>,
                handle: SavedStateHandle
            ): T {
                assistedFactories[modelClass]?.let { assistedFactory ->
                    try {
                        return assistedFactory.create(handle) as T
                    } catch (e: Exception) {
                        throw RuntimeException(e)
                    }
                } ?: throw IllegalArgumentException("Unknown ViewModel class $modelClass")
            }
        }
}