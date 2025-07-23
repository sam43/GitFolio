package io.sam43.gitfolio.presentation.state

import io.sam43.gitfolio.domain.model.Repo
import io.sam43.gitfolio.domain.model.UserDetail
import io.sam43.gitfolio.data.helper.ErrorType

data class UserProfileState(
    val userState: DataState<UserDetail> = DataState(),
    val repositoriesState: ListState<Repo> = ListState(),
    val errorCombined: ErrorType? = null
) {
    // Convenience properties for easier access
    val user: UserDetail? get() = userState.data
    val repositories: List<Repo> get() = repositoriesState.items
    val isLoading: Boolean get() = userState.isLoading || repositoriesState.isLoading
    val error: ErrorType? get() = errorCombined
}