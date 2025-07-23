package io.sam43.gitfolio.presentation.state

import io.sam43.gitfolio.data.helper.ErrorType

data class DataState<T>(
    val data: T? = null,
    val isLoading: Boolean = false,
    val error: ErrorType? = null
)