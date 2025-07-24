package io.sam43.gitfolio.presentation.userlist

import io.sam43.gitfolio.data.helper.ErrorType
import io.sam43.gitfolio.domain.model.User
import io.sam43.gitfolio.presentation.mvi.UiEffect
import io.sam43.gitfolio.presentation.mvi.UiEvent
import io.sam43.gitfolio.presentation.mvi.UiState

class UserListContract {

    sealed class Event : UiEvent {
        data object FetchUsers : Event()
        data object LoadMoreUsers : Event()
        data object RefreshUsers : Event()
    }

    data class State(
        val users: List<User>,
        val isLoading: Boolean,
        val error: ErrorType?,
        val canLoadMore: Boolean
    ) : UiState

    sealed class Effect : UiEffect {
        data class ShowErrorToast(val message: String) : Effect()
    }
}