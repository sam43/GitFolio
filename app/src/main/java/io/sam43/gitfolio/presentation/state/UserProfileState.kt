package io.sam43.gitfolio.presentation.state

import io.sam43.gitfolio.domain.model.Repo
import io.sam43.gitfolio.domain.model.UserDetail
import io.sam43.gitfolio.utils.ErrorType

data class UserProfileState(
    val repositories: List<Repo> = emptyList(),
    val user: UserDetail? = null,
    val isLoading: Boolean = false,
    val error: ErrorType? = null
)