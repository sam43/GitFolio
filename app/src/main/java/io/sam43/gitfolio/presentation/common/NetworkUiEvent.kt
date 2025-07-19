package io.sam43.gitfolio.presentation.common

sealed class NetworkUiEvent {
    data class ShowToast(val message: String) : NetworkUiEvent()
}