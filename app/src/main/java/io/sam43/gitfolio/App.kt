package io.sam43.gitfolio

import android.app.Application
import android.widget.Toast
import dagger.hilt.android.HiltAndroidApp
import io.sam43.gitfolio.utils.NetworkMonitor
import io.sam43.gitfolio.utils.NetworkStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltAndroidApp
class App : Application() {
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    @Inject lateinit var networkMonitor: NetworkMonitor
    override fun onCreate() {
        super.onCreate()
        startListeningToNetworkState()
    }
    private fun startListeningToNetworkState() {
        networkMonitor.networkStatus
            .onEach { status ->
                when (status) {
                    NetworkStatus.Unavailable ->
                        Toast.makeText(this, "Internet was disconnected! \nPlease check your connection.", Toast.LENGTH_SHORT).show()
                    NetworkStatus.IncapableOfInternetConnection ->
                        Toast.makeText(this, "The device is Incapable of Internet Connection", Toast.LENGTH_SHORT).show()
                    else ->
                        Toast.makeText(this, "Connected to the Internet!", Toast.LENGTH_SHORT).show()
                }
            }
            .launchIn(applicationScope)
    }
}