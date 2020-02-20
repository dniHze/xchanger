package dev.dnihze.revorate.model.platform

enum class NetworkConnection {
    AVAILABLE,
    UNAVAILABLE,
    LOSING,
    LOST;

    fun isAvailable() = this == AVAILABLE
}