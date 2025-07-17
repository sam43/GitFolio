package io.sam43.gitfolio.presentation.users.contract

import io.sam43.gitfolio.domain.model.User
import io.sam43.gitfolio.domain.model.UserDetail

/**
 * Defines the contract for the User feature, encompassing all possible
 * Intents, States, and Side Effects.
 */
interface UserContract {

    /**
     * Represents all possible user actions or system events that the ViewModel can process.
     * Use a sealed interface for exhaustiveness in 'when' expressions.
     */
    sealed interface UserIntent {
        // User List Intents
        data object LoadUsers : UserIntent // Initial load or refresh
        data class SearchUsers(val query: String) : UserIntent // User enters search query
        data object ClearSearch : UserIntent // User clears search input
        data class SelectUser(val username: String) : UserIntent // User clicks on a user from the list

        // User Detail Intents (if applicable, or can be in a separate contract)
        data class LoadUserDetail(val username: String) : UserIntent // Load details for a specific user
        data object RefreshUserDetail : UserIntent // Refresh current user's details
    }

    /**
     * Represents the immutable UI state of the User feature at any given moment.
     * Use a data class for easy copying and state updates.
     */
    data class UserState(
        val isLoading: Boolean = false,
        val users: List<User> = emptyList(), // For user list screen
        val userDetail: UserDetail? = null, // For user detail screen
        val searchQuery: String = "", // Current search query
        val error: Throwable? = null, // General error for either list or detail
        val isSearching: Boolean = false // Indicates if a search is active (loading for search results)
    )

    /**
     * Represents one-time events or side effects that the UI should react to,
     * but which do not represent a persistent UI state.
     * These are typically consumed once by the UI.
     * Use a sealed interface for exhaustiveness.
     */
    sealed interface UserSideEffect {
        data class ShowSnackbar(val message: String) : UserSideEffect // Show a transient message
        data class NavigateToUserDetail(val username: String) : UserSideEffect // Navigate to user detail screen
        data object NavigateBack : UserSideEffect // General back navigation
        data object ShowNetworkErrorDialog : UserSideEffect // Show a specific network error dialog
    }
}