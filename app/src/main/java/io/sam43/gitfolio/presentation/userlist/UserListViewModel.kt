package io.sam43.gitfolio.presentation.userlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.sam43.gitfolio.data.helper.ErrorHandler
import io.sam43.gitfolio.data.helper.Result
import io.sam43.gitfolio.domain.usecases.GetUserListUseCase
import io.sam43.gitfolio.presentation.userlist.UserListContract.Effect
import io.sam43.gitfolio.presentation.userlist.UserListContract.Event
import io.sam43.gitfolio.presentation.userlist.UserListContract.State
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

@HiltViewModel
class UserListViewModel @Inject constructor(
    private val useCase: GetUserListUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(State(users = emptyList(), isLoading = false, error = null, canLoadMore = true))
    val state = _state.asStateFlow()

    private val _effect = Channel<Effect>()
    val effect = _effect.receiveAsFlow()

    private var loadingJob: Job? = null

    init { setEvent(Event.FetchUsers) }

    fun setEvent(event: Event) {
        when (event) {
            Event.FetchUsers -> fetchUsers(isLoadMore = false)
            Event.LoadMoreUsers -> fetchUsers(isLoadMore = true)
            Event.RefreshUsers -> fetchUsers(isLoadMore = false)
        }
    }

    private fun fetchUsers(isLoadMore: Boolean) {
        loadingJob?.cancel()

        if (isLoadMore && !_state.value.canLoadMore) return

        loadingJob = viewModelScope.launch {
            _state.update { currentState ->
                if (!isLoadMore) {
                    currentState.copy(isLoading = true, error = null, users = emptyList(), canLoadMore = true)
                } else {
                    currentState.copy(isLoading = true, error = null)
                }
            }

            try {
                val sinceId = if (isLoadMore && _state.value.users.isNotEmpty()) {
                    _state.value.users.last().id
                } else {
                    0
                }

                useCase(since = sinceId, perPage = 20)
                    .flowOn(Dispatchers.IO)
                    .catch { exception ->
                        _state.update {
                            it.copy(
                                isLoading = false,
                                error = ErrorHandler.handleError(exception)
                            )
                        }
                        _effect.send(Effect.ShowErrorToast(ErrorHandler.getErrorMessage(ErrorHandler.handleError(exception))))
                    }
                    .collectLatest { result ->
                        when (result) {
                            is Result.Success -> {
                                val newUsers = result.data
                                _state.update { currentState ->
                                    val updatedList = if (isLoadMore) {
                                        currentState.users + newUsers
                                    } else {
                                        newUsers
                                    }
                                    currentState.copy(
                                        users = updatedList,
                                        isLoading = false,
                                        error = null,
                                        canLoadMore = newUsers.size >= 20
                                    )
                                }
                            }
                            is Result.Error -> {
                                _state.update { currentState ->
                                    currentState.copy(
                                        isLoading = false,
                                        error = result.error
                                    )
                                }
                                _effect.send(Effect.ShowErrorToast(ErrorHandler.getErrorMessage(result.error)))
                            }
                            Result.Loading -> {
                                if (!_state.value.isLoading) {
                                    _state.update { it.copy(isLoading = true, error = null) }
                                }
                            }
                        }
                    }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = ErrorHandler.handleError(e)
                    )
                }
                _effect.send(Effect.ShowErrorToast(ErrorHandler.getErrorMessage(ErrorHandler.handleError(e))))
            }
        }
    }
}