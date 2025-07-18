package io.sam43.gitfolio.presentation.users

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.sam43.gitfolio.domain.model.User
import io.sam43.gitfolio.domain.model.UserDetail
import io.sam43.gitfolio.domain.model.Repo
import io.sam43.gitfolio.domain.usecases.FetchUserUseCase
import io.sam43.gitfolio.domain.usecases.GetUserDetailsUseCase
import io.sam43.gitfolio.domain.usecases.GetUserRepositoriesUseCase
import io.sam43.gitfolio.utils.Result
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserViewModel @Inject constructor(
    private val fetchUserUseCase: FetchUserUseCase,
    private val getUserDetailsUseCase: GetUserDetailsUseCase,
    private val getUserRepositoriesUseCase: GetUserRepositoriesUseCase
) : ViewModel() {

    private val _users = MutableStateFlow<List<User>>(emptyList())
    val users: StateFlow<List<User>> = _users.asStateFlow()

    private val _userDetail = MutableStateFlow<UserDetail?>(null)
    val userDetail: StateFlow<UserDetail?> = _userDetail.asStateFlow()

    private val _userRepos = MutableStateFlow<List<Repo>>(emptyList())
    val userRepos: StateFlow<List<Repo>> = _userRepos.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun getAllUsers() {
        viewModelScope.launch {
            fetchUserUseCase().collect { result ->
                when (result) {
                    is Result.Loading -> {
                        _isLoading.value = true
                        _error.value = null
                    }
                    is Result.Success -> {
                        _isLoading.value = false
                        _users.value = result.data
                        _error.value = null
                    }
                    is Result.Error -> {
                        _isLoading.value = false
                        _error.value = result.exception.message
                    }
                }
            }
        }
    }

    fun searchUsers(query: String) {
        viewModelScope.launch {
            fetchUserUseCase(query).collect { result ->
                when (result) {
                    is Result.Loading -> {
                        _isLoading.value = true
                        _error.value = null
                    }
                    is Result.Success -> {
                        _isLoading.value = false
                        _users.value = result.data
                        _error.value = null
                    }
                    is Result.Error -> {
                        _isLoading.value = false
                        _error.value = result.exception.message
                    }
                }
            }
        }
    }

    fun getUserDetails(username: String) {
        viewModelScope.launch {
            getUserDetailsUseCase(username).collect { result ->
                when (result) {
                    is Result.Loading -> {
                        _isLoading.value = true
                        _error.value = null
                    }
                    is Result.Success -> {
                        _isLoading.value = false
                        _userDetail.value = result.data
                        _error.value = null
                    }
                    is Result.Error -> {
                        _isLoading.value = false
                        _error.value = result.exception.message
                    }
                }
            }
        }
    }

    fun getUserRepos(username: String) {
        viewModelScope.launch {
            getUserRepositoriesUseCase(username).collect { result ->
                when (result) {
                    is Result.Loading -> {
                        _isLoading.value = true
                        _error.value = null
                    }
                    is Result.Success -> {
                        _isLoading.value = false
                        // filtering out forked repositories; only show non-fork public repositories
                        _userRepos.value = result.data.filter { !it.fork }
                        _error.value = null
                    }
                    is Result.Error -> {
                        _isLoading.value = false
                        _error.value = result.exception.message
                    }
                }
            }
        }
    }
}
