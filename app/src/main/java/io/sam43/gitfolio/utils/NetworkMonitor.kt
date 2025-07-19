package io.sam43.gitfolio.utils

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

sealed class NetworkStatus {
    data object Available : NetworkStatus()
    data object Unavailable : NetworkStatus()
    data object IncapableOfInternetConnection : NetworkStatus()
}

@Singleton
class NetworkMonitor@Inject constructor(@ApplicationContext private val application: Application) {
    private val connectivityManager =
        application.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    val networkStatus: Flow<NetworkStatus> = callbackFlow {
        val networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                super.onAvailable(network)
                launch { send(NetworkStatus.Available) }
            }

            override fun onLost(network: Network) {
                super.onLost(network)
                launch { send(NetworkStatus.Unavailable) }
            }

            override fun onUnavailable() {
                super.onUnavailable()
                launch { send(NetworkStatus.Unavailable) }
            }
        }
        val currentNetwork = connectivityManager.activeNetwork
        if (currentNetwork == null) {
            launch { send(NetworkStatus.Unavailable) }
        } else {
            val capabilities = connectivityManager.getNetworkCapabilities(currentNetwork)
            if (capabilities != null && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)) {
                launch { send(NetworkStatus.Available) }
            } else {
                launch { send(NetworkStatus.IncapableOfInternetConnection) }
            }
        }

        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
            .build()

        connectivityManager.registerNetworkCallback(networkRequest, networkCallback)

        awaitClose {
            connectivityManager.unregisterNetworkCallback(networkCallback)
        }
    }.distinctUntilChanged() // Only emit when the status actually changes
}
