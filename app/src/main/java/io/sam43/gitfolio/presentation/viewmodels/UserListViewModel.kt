package io.sam43.gitfolio.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.sam43.gitfolio.domain.model.User
import io.sam43.gitfolio.domain.usecases.FetchUserUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import io.sam43.gitfolio.utils.Result
import io.sam43.gitfolio.utils.NetworkMonitor
import io.sam43.gitfolio.utils.NetworkStatus
import kotlinx.coroutines.flow.collectLatest

data class UserListState(
    val users: List<User> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class UserListViewModel @Inject constructor(
    private val fetchUserUseCase: FetchUserUseCase,
    private val networkMonitor: NetworkMonitor
) : ViewModel() {
    private val _state = MutableStateFlow(UserListState(isLoading = true))
    val state: StateFlow<UserListState> = _state.asStateFlow()
    
    private var wasOffline = false

    init {
        fetchUsers()
        monitorNetworkChanges()
    }
    
    private fun monitorNetworkChanges() {
        viewModelScope.launch {
            networkMonitor.networkStatus.collectLatest { status ->
                when (status) {
                    is NetworkStatus.Available -> {
                        if (wasOffline) {
                            fetchUsers()
                            wasOffline = false
                        }
                    }
                    is NetworkStatus.Unavailable, 
                    is NetworkStatus.IncapableOfInternetConnection -> {
                        wasOffline = true
                    }
                }
            }
        }
    }
    
    private fun fetchUsers() {
        viewModelScope.launch {
            fetchUserUseCase().collect { result ->
                when (result) {
                    is Result.Loading -> {
                        _state.value = UserListState(isLoading = true)
                    }
                    is Result.Success -> {
                        _state.value = UserListState(
                            users = result.data,
                            isLoading = false
                        )
                    }
                    is Result.Error -> {
                        _state.value = UserListState(
                            error = result.errorType.toString(),
                            isLoading = false
                        )
                    }
                }
            }
        }
    }

    private fun searchUsers(q: String) {
        viewModelScope.launch {
            fetchUserUseCase.invoke(q).collect { result ->
                when (result) {
                    is Result.Loading -> {
                        _state.value = UserListState(isLoading = true)
                    }

                    is Result.Success -> {
                        _state.value = UserListState(
                            users = result.data,
                            isLoading = false
                        )
                    }

                    is Result.Error -> {
                        _state.value = UserListState(
                            error = result.errorType.toString(),
                            isLoading = false
                        )
                    }
                }
            }
        }
    }
}