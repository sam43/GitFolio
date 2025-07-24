package io.sam43.gitfolio.presentation.viewmodels

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.sam43.gitfolio.data.helper.ErrorType
import io.sam43.gitfolio.data.helper.NetworkMonitor
import io.sam43.gitfolio.data.helper.handleDataResult
import io.sam43.gitfolio.data.helper.handleListResult
import io.sam43.gitfolio.domain.model.Repo
import io.sam43.gitfolio.domain.model.UserDetail
import io.sam43.gitfolio.domain.usecases.GetUserDetailsUseCase
import io.sam43.gitfolio.domain.usecases.GetUserRepositoriesUseCase
import io.sam43.gitfolio.presentation.state.DataState
import io.sam43.gitfolio.presentation.state.ListState
import io.sam43.gitfolio.presentation.state.UserProfileState
import kotlinx.coroutines.async
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
    private val getRepositoryUseCase: GetUserRepositoriesUseCase,
    networkMonitor: NetworkMonitor
) : ParentViewModel<UserDetail>(networkMonitor) {
    private val _combinedError = MutableStateFlow<ErrorType?>(ErrorType.UnknownError())
    private val _userState = MutableStateFlow(DataState<UserDetail>())
    private val _repositoriesState = MutableStateFlow(ListState<Repo>())

    val state: StateFlow<UserProfileState> = combine(
        _userState,
        _repositoriesState
    ) { userState, repoState ->
        _combinedError.update { userState.error ?: repoState.error }
        UserProfileState(
            userState = userState,
            repositoriesState = repoState,
            errorCombined = _combinedError.value
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), UserProfileState())

    private fun fetchUserProfileDetails(userName: String) {
        viewModelScope.launch {
            try {
                val profileDeferred = async {
                    getProfileUseCase.invoke(userName).collectLatest { result ->
                        _userState.handleDataResult(result)
                    }
                }
                val reposDeferred = async {
                    getRepositoryUseCase.invoke(userName).collectLatest { result ->
                        _repositoriesState.handleListResult(result)
                    }
                }

                profileDeferred.await()
                reposDeferred.await()

            } catch (e: Exception) {
                _combinedError.value = ErrorType.UnknownError(e.message.toString().plus("during catch"))
            }
        }
    }

    fun fetchUserProfileByUsername(username: String? = null) {
        fetchUserProfileDetails(username ?: "")
    }
}