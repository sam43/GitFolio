package io.sam43.gitfolio.presentation.state

import io.sam43.gitfolio.data.helper.ErrorType

data class ListState<T>(
    val items: List<T> = emptyList(),
    val isLoading: Boolean = false,
    val error: ErrorType? = null
)