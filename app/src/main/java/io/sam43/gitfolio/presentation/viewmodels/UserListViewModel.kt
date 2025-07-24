package io.sam43.gitfolio.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.sam43.gitfolio.data.helper.ErrorType
import io.sam43.gitfolio.data.helper.NetworkMonitor
import io.sam43.gitfolio.domain.model.User
import io.sam43.gitfolio.domain.usecases.GetUserListUseCase
import io.sam43.gitfolio.presentation.state.ListUiState
import io.sam43.gitfolio.presentation.state.updateWithListResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserListViewModel @Inject constructor(
    private val useCase: GetUserListUseCase
) : ViewModel() {
    val state = MutableStateFlow(ListUiState<User>())

    init { fetchUsers() }
    
    fun fetchUsers() {
        viewModelScope.launch {
            state.update { it.copy(isLoading = true, error = null) }
            try {
                launch {
                    useCase.invoke().collectLatest { result ->
                        state.updateWithListResult(result)
                    }
                }
            } catch (e: Exception) {
                state.update { ListUiState(error = ErrorType.UnknownError(e.message.toString())) }
            }
        }
    }

    @Deprecated("Not used as the user item filtration has been handled locally")
    fun searchUsers(q: String) {
        viewModelScope.launch {
            try {
                launch {
                    useCase.invoke(q).collectLatest { result ->
                        state.updateWithListResult(result)
                    }
                }
            } catch (e: Exception) {
                state.update { ListUiState(error = ErrorType.UnknownError(e.message.toString())) }
            }
        }
    }
}