package dev.dnihze.revorate.utils.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel

interface AssistedViewModelFactory<out T: ViewModel> {
    fun create(savedStateHandle: SavedStateHandle): T
}