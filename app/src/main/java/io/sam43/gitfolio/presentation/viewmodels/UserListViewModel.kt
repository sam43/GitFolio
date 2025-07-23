package io.sam43.gitfolio.presentation.viewmodels

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.sam43.gitfolio.data.helper.ErrorType
import io.sam43.gitfolio.data.helper.NetworkMonitor
import io.sam43.gitfolio.data.helper.handleListResult
import io.sam43.gitfolio.domain.model.User
import io.sam43.gitfolio.domain.usecases.GetUserListUseCase
import io.sam43.gitfolio.presentation.state.ListState
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserListViewModel @Inject constructor(
    private val useCase: GetUserListUseCase,
    networkMonitor: NetworkMonitor
) : ParentViewModel<User>(networkMonitor) {
    val state = MutableStateFlow(ListState<User>())

    init {
        monitorNetworkChanges(
            onConnectedAction = ::fetchUsers,
            onDisconnectedAction = {
                state.update {
                    ListState(
                        error = ErrorType.NetworkError,
                        isLoading = false
                    )
                }
            }
        )
    }
    
    fun fetchUsers() {
        viewModelScope.launch {
            try {
                val userDeffrredList = async {
                    useCase.invoke().collectLatest { result ->
                        state.handleListResult(result)
                    }
                }
                userDeffrredList.await()
            } catch (e: Exception) {
                state.value = ListState(error = ErrorType.UnknownError(e.message.toString()))
            }
        }
    }

    fun searchUsers(q: String) {
        viewModelScope.launch {
            try {
                val userDeffrredList = async {
                    useCase.invoke(q).collectLatest { result ->
                        state.handleListResult(result)
                    }
                }
                userDeffrredList.await()
            } catch (e: Exception) {
                state.value = ListState(error = ErrorType.UnknownError(e.message.toString()))
            }
        }
    }
}