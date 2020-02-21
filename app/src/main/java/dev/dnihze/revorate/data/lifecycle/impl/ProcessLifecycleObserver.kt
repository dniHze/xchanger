package dev.dnihze.revorate.data.lifecycle.impl

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.jakewharton.rxrelay2.BehaviorRelay
import com.jakewharton.rxrelay2.Relay
import dev.dnihze.revorate.data.lifecycle.AppStateObserver
import dev.dnihze.revorate.model.lifecycle.AppState
import io.reactivex.Observable
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProcessLifecycleObserver @Inject constructor() : AppStateObserver {

    private val publisher: Relay<AppState> = BehaviorRelay.create()

    init {
        publisher.accept(AppState.FOREGROUND)
    }

    override fun observe(): Observable<AppState> {
        return publisher
            .distinctUntilChanged()
            .share()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun background() {
        publisher.accept(AppState.BACKGROUND)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun foreground() {
        publisher.accept(AppState.FOREGROUND)
    }
}