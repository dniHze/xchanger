@file:Suppress("DEPRECATION")

package dev.dnihze.revorate.data.platform.impl

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkInfo
import android.net.wifi.WifiManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.content.getSystemService
import androidx.core.net.ConnectivityManagerCompat
import dev.dnihze.revorate.data.platform.ConnectionWatcher
import dev.dnihze.revorate.model.platform.NetworkConnection
import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import javax.inject.Inject
import javax.inject.Singleton

/**
 * File is using deprecation suppression as the NetworkInfo is deprecated,
 * but there is no alternative on API < N
 */
@Singleton
class ConnectionWatcherImpl @Inject constructor(
    private val appContext: Context
) : ConnectionWatcher {

    override fun watch(): Observable<NetworkConnection> {
        return Observable.create<NetworkConnection> { emitter ->
            val connectivityManager = appContext.getSystemService<ConnectivityManager>()
            if (connectivityManager == null) {
                emitter.onNext(NetworkConnection.UNAVAILABLE)
                emitter.onComplete()
            } else {
                createWatcher(connectivityManager, emitter)
            }
        }.distinctUntilChanged().share()
    }

    private val createWatcher: (
        connectivityManager: ConnectivityManager,
        emitter: ObservableEmitter<NetworkConnection>
    ) -> Unit = { connectivityManager, emitter ->
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            watcherApiN(connectivityManager, emitter)
        } else {
            watcherApiDefault(connectivityManager, emitter)
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private val watcherApiN: (
        connectivityManager: ConnectivityManager,
        emitter: ObservableEmitter<NetworkConnection>
    ) -> Unit = { connectivityManager, emitter ->
        val networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                emitter.onNext(NetworkConnection.AVAILABLE)
            }

            override fun onUnavailable() {
                emitter.onNext(NetworkConnection.UNAVAILABLE)
            }

            override fun onLosing(network: Network, maxMsToLive: Int) {
                emitter.onNext(NetworkConnection.LOSING)
            }

            override fun onLost(network: Network) {
                emitter.onNext(NetworkConnection.LOST)
            }
        }
        connectivityManager.registerDefaultNetworkCallback(networkCallback)

        emitter.setCancellable {
            connectivityManager.unregisterNetworkCallback(networkCallback)
        }
    }

    private val watcherApiDefault: (
        connectivityManager: ConnectivityManager,
        emitter: ObservableEmitter<NetworkConnection>
    ) -> Unit = { connectivityManager, emitter ->
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val info = ConnectivityManagerCompat.getNetworkInfoFromBroadcast(connectivityManager, intent)
                    ?: connectivityManager.activeNetworkInfo
                val active = info?.getNetworkConnection() ?: NetworkConnection.UNAVAILABLE
                emitter.onNext(active)
            }
        }

        val filter = IntentFilter()
        filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION)
        filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION)
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION)

        appContext.registerReceiver(receiver, filter)

        emitter.setCancellable {
            appContext.unregisterReceiver(receiver)
        }
    }

    private fun NetworkInfo.getNetworkConnection(): NetworkConnection {
        return when (state) {
            NetworkInfo.State.CONNECTED, NetworkInfo.State.CONNECTING -> NetworkConnection.AVAILABLE
            NetworkInfo.State.DISCONNECTING -> NetworkConnection.LOSING
            NetworkInfo.State.DISCONNECTED, NetworkInfo.State.SUSPENDED -> NetworkConnection.LOST
            else -> NetworkConnection.UNAVAILABLE

        }
    }
}