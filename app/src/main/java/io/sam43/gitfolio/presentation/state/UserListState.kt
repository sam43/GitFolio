package io.sam43.gitfolio.presentation.state

import io.sam43.gitfolio.domain.model.User
import io.sam43.gitfolio.utils.ErrorType

data class UserListState(
    val users: List<User> = emptyList(),
    val isLoading: Boolean = false,
    val error: ErrorType? = null
)