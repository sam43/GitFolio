package io.sam43.gitfolio.presentation.userprofile

import io.sam43.gitfolio.data.helper.ErrorType
import io.sam43.gitfolio.domain.model.Repo
import io.sam43.gitfolio.domain.model.UserDetail
import io.sam43.gitfolio.presentation.mvi.UiEffect
import io.sam43.gitfolio.presentation.mvi.UiEvent
import io.sam43.gitfolio.presentation.mvi.UiState

class UserProfileContract {

    sealed class Event : UiEvent {
        data class FetchUserProfile(val username: String) : Event()
    }

    data class State(
        val user: UserDetail?,
        val repositories: List<Repo>,
        val isLoading: Boolean,
        val error: ErrorType?,
        val hasLoaded: Boolean
    ) : UiState

    sealed class Effect : UiEffect {
        data class ShowErrorToast(val message: String) : Effect()
    }
}