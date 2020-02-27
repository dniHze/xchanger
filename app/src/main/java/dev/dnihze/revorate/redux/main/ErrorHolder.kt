package dev.dnihze.revorate.redux.main

data class ErrorHolder(
    val error: MainScreenError,
    val isKnowIssue: Boolean = false
)