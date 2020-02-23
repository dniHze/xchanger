package dev.dnihze.revorate.ui.main.navigation

import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import ru.terrakok.cicerone.Navigator
import ru.terrakok.cicerone.commands.Command
import ru.terrakok.cicerone.commands.Forward


class ActivityNavigator constructor(private val activity: AppCompatActivity): Navigator {
    override fun applyCommands(commands: Array<out Command>) {
        for (command in commands) {
            if (command is Forward) {
                val screen = command.screen
                if (screen is NetworkSettingsScreen) {
                    // Open network settings here
                    val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        Intent(Settings.Panel.ACTION_INTERNET_CONNECTIVITY)
                    } else {
                        Intent(Settings.ACTION_WIRELESS_SETTINGS)
                    }
                    activity.startActivity(intent)

                }
            }
        }
    }
}