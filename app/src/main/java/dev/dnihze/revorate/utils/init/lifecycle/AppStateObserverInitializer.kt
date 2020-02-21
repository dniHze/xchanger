package dev.dnihze.revorate.utils.init.lifecycle

import androidx.lifecycle.ProcessLifecycleOwner
import dev.dnihze.revorate.data.lifecycle.AppStateObserver
import dev.dnihze.revorate.utils.init.InitOnAppStart
import javax.inject.Inject

class AppStateObserverInitializer @Inject constructor(
    private val appStateObserver: AppStateObserver
): InitOnAppStart {

    override fun init() {
        ProcessLifecycleOwner.get()
            .lifecycle
            .addObserver(appStateObserver)
    }
}