package dev.dnihze.revorate.redux.main.utils

import com.jakewharton.rxrelay2.BehaviorRelay
import dev.dnihze.revorate.data.platform.ConnectionWatcher
import dev.dnihze.revorate.model.platform.NetworkConnection
import io.reactivex.Observable

class MockConnectionWatcher: ConnectionWatcher {

    private val relay = BehaviorRelay.create<NetworkConnection>()

    init {
        relay.accept(NetworkConnection.AVAILABLE)
    }

    override fun watch(): Observable<NetworkConnection> {
        return relay.distinctUntilChanged().share()
    }

    fun setConnection(connection: NetworkConnection) {
        relay.accept(connection)
    }
}