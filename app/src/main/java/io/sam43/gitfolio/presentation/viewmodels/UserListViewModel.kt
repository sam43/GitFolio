package io.sam43.gitfolio.presentation.viewmodels

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.sam43.gitfolio.domain.usecases.GetUserListUseCase
import io.sam43.gitfolio.presentation.state.UserListState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import io.sam43.gitfolio.utils.Result
import io.sam43.gitfolio.utils.NetworkMonitor
import kotlinx.coroutines.flow.collectLatest

@HiltViewModel
class UserListViewModel @Inject constructor(
    private val useCase: GetUserListUseCase,
    networkMonitor: NetworkMonitor
) : ParentViewModel(networkMonitor) {
    private val _state = MutableStateFlow(UserListState(isLoading = true))
    val state: StateFlow<UserListState> = _state.asStateFlow()

    init {
        monitorNetworkChanges(
            onConnectedAction = ::fetchUsers,
            onDisconnectedAction = {/* do nothing for now */}
        )
    }
    
    fun fetchUsers() {
        viewModelScope.launch {
            useCase().collectLatest { result ->
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
                            error = result.errorType,
                            isLoading = false
                        )
                    }
                }
            }
        }
    }

    fun searchUsers(q: String) {
        viewModelScope.launch {
            useCase.invoke(q).collectLatest { result ->
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
                            error = result.errorType,
                            isLoading = false
                        )
                    }
                }
            }
        }
    }
}