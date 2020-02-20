package dev.dnihze.revorate.redux.main

import dev.dnihze.revorate.model.network.exception.ApiException

sealed class MainScreenError {
    object NetworkConnectionError: MainScreenError()
    data class ApiError(val exception: ApiException): MainScreenError()
    data class Unknown(val throwable: Throwable): MainScreenError()
}