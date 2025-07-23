package io.sam43.gitfolio.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.sam43.gitfolio.utils.NetworkMonitor
import io.sam43.gitfolio.utils.NetworkStatus
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
open class ParentViewModel @Inject constructor(
    private val networkMonitor: NetworkMonitor
) : ViewModel() {

    private var wasOffline = false

    protected fun monitorNetworkChanges(onConnectedAction: () -> Unit, onDisconnectedAction: () -> Unit) {
        viewModelScope.launch {
            // Trigger initial data load if network is available
            networkMonitor.networkStatus.collectLatest {
                if (it is NetworkStatus.Available) {
                    onConnectedAction()
                } else {
                    onDisconnectedAction()
                }
            }
            networkMonitor.networkStatus.collectLatest { status ->
                when (status) {
                    is NetworkStatus.Available -> {
                        if (wasOffline) {
                            onConnectedAction()
                            wasOffline = false
                        }
                    }
                    is NetworkStatus.Unavailable,
                    is NetworkStatus.IncapableOfInternetConnection -> {
                        onDisconnectedAction()
                        wasOffline = true
                    }
                }
            }
        }
    }
}