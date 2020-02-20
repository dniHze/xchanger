package dev.dnihze.revorate.data.platform

import dev.dnihze.revorate.model.platform.NetworkConnection
import io.reactivex.Observable

interface ConnectionWatcher {
    fun watch(): Observable<NetworkConnection>
}