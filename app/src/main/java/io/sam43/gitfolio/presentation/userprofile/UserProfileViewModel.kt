package io.sam43.gitfolio.presentation.userprofile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.sam43.gitfolio.data.helper.ErrorHandler
import io.sam43.gitfolio.data.helper.Result
import io.sam43.gitfolio.domain.usecases.GetUserDetailsUseCase
import io.sam43.gitfolio.domain.usecases.GetUserRepositoriesUseCase
import io.sam43.gitfolio.presentation.userprofile.UserProfileContract.Effect
import io.sam43.gitfolio.presentation.userprofile.UserProfileContract.Event
import io.sam43.gitfolio.presentation.userprofile.UserProfileContract.State
import kotlinx.coroutines.Dispatchers
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

@HiltViewModel
class UserProfileViewModel @Inject constructor(
    private val getUserDetailsUseCase: GetUserDetailsUseCase,
    private val getUserRepositoriesUseCase: GetUserRepositoriesUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(State(user = null, repositories = emptyList(), isLoading = false, error = null, hasLoaded = false))
    val state = _state.asStateFlow()

    private val _effect = Channel<Effect>()
    val effect = _effect.receiveAsFlow()

    fun setEvent(event: Event) {
        when (event) {
            is Event.FetchUserProfile -> fetchUserProfile(event.username)
        }
    }

    private fun fetchUserProfile(username: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null, hasLoaded = false) }

            getUserDetailsUseCase(username)
                .flowOn(Dispatchers.IO)
                .catch { exception ->
                    _state.update { it.copy(isLoading = false, error = ErrorHandler.handleError(exception), hasLoaded = true) }
                    _effect.send(Effect.ShowErrorToast(ErrorHandler.getErrorMessage(ErrorHandler.handleError(exception))))
                }
                .collectLatest { result ->
                    when (result) {
                        is Result.Success -> {
                            _state.update { it.copy(user = result.data, isLoading = false, error = null, hasLoaded = true) }
                            fetchUserRepos(username)
                        }
                        is Result.Error -> {
                            _state.update { it.copy(isLoading = false, error = result.error, hasLoaded = true) }
                            _effect.send(Effect.ShowErrorToast(ErrorHandler.getErrorMessage(result.error)))
                        }
                        Result.Loading -> {
                            _state.update { it.copy(isLoading = true, error = null, hasLoaded = false) }
                        }
                    }
                }
        }
    }

    private fun fetchUserRepos(username: String) {
        viewModelScope.launch {
            getUserRepositoriesUseCase(username)
                .flowOn(Dispatchers.IO)
                .catch { exception ->
                    _state.update { it.copy(isLoading = false, error = ErrorHandler.handleError(exception), hasLoaded = true) }
                    _effect.send(Effect.ShowErrorToast(ErrorHandler.getErrorMessage(ErrorHandler.handleError(exception))))
                }
                .collectLatest { result ->
                    when (result) {
                        is Result.Success -> {
                            _state.update { it.copy(repositories = result.data, isLoading = false, error = null, hasLoaded = true) }
                        }
                        is Result.Error -> {
                            _state.update { it.copy(isLoading = false, error = result.error, hasLoaded = true) }
                            _effect.send(Effect.ShowErrorToast(ErrorHandler.getErrorMessage(result.error)))
                        }
                        Result.Loading -> {
                            _state.update { it.copy(isLoading = true, error = null, hasLoaded = false) }
                        }
                    }
                }
        }
    }
}