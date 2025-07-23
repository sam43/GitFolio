package io.sam43.gitfolio

import android.app.Application
import android.util.Log
import dagger.hilt.android.HiltAndroidApp
import io.sam43.gitfolio.presentation.common.NetworkUiEvent
import io.sam43.gitfolio.utils.NetworkMonitor
import io.sam43.gitfolio.utils.NetworkStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltAndroidApp
class App : Application() {
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    @Inject lateinit var networkMonitor: NetworkMonitor

    private val _networkUiEvents = MutableSharedFlow<NetworkUiEvent>(replay = 1)
    val networkUiEvents: SharedFlow<NetworkUiEvent> = _networkUiEvents.asSharedFlow()

    override fun onCreate() {
        super.onCreate()
        startBroadcastingNetworkState()
    }

    private fun startBroadcastingNetworkState() {
        if (!::networkMonitor.isInitialized) {
            Log.e("App", "NetworkMonitor not initialized in App class.")
            return
        }

        networkMonitor.networkStatus
            .onEach { status ->
                val message = when (status) {
                    is NetworkStatus.Available -> "Internet Connected"
                    is NetworkStatus.Unavailable -> "Internet Disconnected"
                    is NetworkStatus.IncapableOfInternetConnection -> "Unable to connect to internet."
                }
                Log.d("App", "Network Status Changed: $message")
                // Emit an event instead of showing a Toast directly
                _networkUiEvents.emit(NetworkUiEvent.ShowToast(message))
            }
            .launchIn(applicationScope)
    }
}