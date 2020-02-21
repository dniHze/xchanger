package dev.dnihze.revorate.data.lifecycle

import androidx.lifecycle.LifecycleObserver
import dev.dnihze.revorate.model.lifecycle.AppState
import io.reactivex.Observable

interface AppStateObserver: LifecycleObserver {
    fun observe(): Observable<AppState>
}