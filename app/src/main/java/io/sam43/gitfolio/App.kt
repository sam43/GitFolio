package io.sam43.gitfolio

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import io.sam43.gitfolio.presentation.common.NetworkUiEvent
import io.sam43.gitfolio.data.helper.NetworkMonitor
import io.sam43.gitfolio.data.helper.NetworkStatus
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
        if (!::networkMonitor.isInitialized) return
        var isInitialLoad = true
        networkMonitor.networkStatus
            .onEach { status ->
                val message = when (status) {
                    is NetworkStatus.Available -> "Internet Connection was reestablished successfully"
                    is NetworkStatus.Unavailable -> "Disconnected from Internet! Please check your connection."
                    is NetworkStatus.IncapableOfInternetConnection -> "Unable to connect to internet."
                }
                // Only emit toast events after initial load
                if (!isInitialLoad) {
                    _networkUiEvents.emit(NetworkUiEvent.ShowToast(message))
                }
                isInitialLoad = false
            }
            .launchIn(applicationScope)
    }
}