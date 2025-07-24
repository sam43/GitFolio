package io.sam43.gitfolio.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.sam43.gitfolio.domain.model.Repo
import io.sam43.gitfolio.domain.model.UserDetail
import io.sam43.gitfolio.domain.usecases.GetUserDetailsUseCase
import io.sam43.gitfolio.domain.usecases.GetUserRepositoriesUseCase
import io.sam43.gitfolio.presentation.state.DataUiState
import io.sam43.gitfolio.presentation.state.ListUiState
import io.sam43.gitfolio.presentation.state.UserProfileState
import io.sam43.gitfolio.presentation.state.updateWithDataResult
import io.sam43.gitfolio.presentation.state.updateWithListResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class UserProfileDetailsViewModel @Inject constructor(
    private val getProfileUseCase: GetUserDetailsUseCase,
    private val getRepositoryUseCase: GetUserRepositoriesUseCase
) : ViewModel() {
    private val _userState = MutableStateFlow(DataUiState<UserDetail>())
    private val _repositoriesState = MutableStateFlow(ListUiState<Repo>())

    val state: StateFlow<UserProfileState> = combine(
        _userState,
        _repositoriesState
    ) { userState, repoState ->
        UserProfileState(
            userState = userState,
            repositoriesState = repoState,
            errorCombined = userState.error ?: repoState.error
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), UserProfileState())

    private fun fetchUserProfileDetails(userName: String) {
        viewModelScope.launch {
            _repositoriesState.update { it.copy(isLoading = true) }
            try {
                getProfileUseCase.invoke(userName).collectLatest { result ->
                    _userState.updateWithDataResult(result)
                }
                getRepositoryUseCase.invoke(userName).collectLatest { result ->
                    _repositoriesState.updateWithListResult(result)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _repositoriesState.update { currentState ->
                    currentState.copy(
                        isLoading = false,
                        error = currentState.error,
                        data = currentState.data.ifEmpty { emptyList() }
                    )
                }
            }
        }
    }

    fun fetchUserProfileByUsername(username: String) {
        fetchUserProfileDetails(username)
    }
}