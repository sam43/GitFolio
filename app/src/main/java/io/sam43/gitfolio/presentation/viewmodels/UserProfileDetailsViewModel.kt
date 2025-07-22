package io.sam43.gitfolio.presentation.viewmodels

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.sam43.gitfolio.domain.model.Repo
import io.sam43.gitfolio.domain.model.UserDetail
import io.sam43.gitfolio.domain.usecases.GetUserDetailsUseCase
import io.sam43.gitfolio.domain.usecases.GetUserRepositoriesUseCase
import io.sam43.gitfolio.utils.ErrorType
import io.sam43.gitfolio.utils.NETWORK_ERROR_MESSAGE
import io.sam43.gitfolio.utils.NetworkMonitor
import io.sam43.gitfolio.utils.Result
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class UserProfileState(
    val repositories: List<Repo> = emptyList(),
    val user: UserDetail? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class UserProfileDetailsViewModel @Inject constructor(
    private val getProfileUseCase: GetUserDetailsUseCase,
    private val getRepositoryUseCase: GetUserRepositoriesUseCase,
    networkMonitor: NetworkMonitor
) : ParentViewModel(networkMonitor) {
    private val _state = MutableStateFlow(UserProfileState(isLoading = true))
    val state: StateFlow<UserProfileState> = _state.asStateFlow()

    init {
        monitorNetworkChanges(
            onConnectedAction = {},
            onDisconnectedAction = {
                _state.update { it.copy(error = NETWORK_ERROR_MESSAGE, isLoading = false) }
            }
        )
    }

    private fun fetchUserProfileDetails(userName: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                val profileDeferred = async { getProfileUseCase.invoke(userName).first() }
                val reposDeferred = async { getRepositoryUseCase.invoke(userName).first() }

                val profileResult: Result<UserDetail> = profileDeferred.await()
                val reposResult: Result<List<Repo>> = reposDeferred.await()
                var finalUser: UserDetail? = null
                var finalRepos: List<Repo> = emptyList()
                val errors = mutableListOf<ErrorType>()
                
                when(profileResult) {
                    is Result.Success -> finalUser = profileResult.data
                    is Result.Error -> profileResult.errorType.let { errors.add(it) }
                    else -> {}
                }

                when(reposResult) {
                    is Result.Success -> finalRepos = reposResult.data
                    is Result.Error -> reposResult.errorType.let { errors.add(it) }
                    else -> {}
                }

                if (errors.isEmpty()) {
                    _state.update {
                        it.copy(
                            user = finalUser,
                            repositories = finalRepos,
                            isLoading = false,
                            error = null
                        )
                    }
                } else {
                    _state.update {
                        it.copy(
                            user = finalUser,
                            repositories = finalRepos,
                            isLoading = false,
                            error = errors.joinToString("; ") { error -> error.toString() }
                        )
                    }
                }

            } catch (e: Exception) {
                _state.value = UserProfileState(
                    error = e.message,
                    isLoading = false
                )
            }
        }
    }

    fun fetchUserProfileByUsername(username: String) {
        fetchUserProfileDetails(username)
    }
}