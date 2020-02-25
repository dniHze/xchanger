package dev.dnihze.revorate.ui.main.navigation

import ru.terrakok.cicerone.Screen

class NetworkSettingsScreen: Screen() {
    override fun equals(other: Any?): Boolean {
        return other is NetworkSettingsScreen
    }

    override fun hashCode(): Int {
        return javaClass.hashCode()
    }
}