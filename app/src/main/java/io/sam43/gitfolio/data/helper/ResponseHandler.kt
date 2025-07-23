package io.sam43.gitfolio.data.helper

import io.sam43.gitfolio.presentation.state.DataState
import io.sam43.gitfolio.presentation.state.ListState
import kotlinx.coroutines.flow.MutableStateFlow

fun <T> MutableStateFlow<ListState<T>>.handleListResult(result: Result<List<T>>) {
    value = when (result) {
        is Result.Loading -> ListState(isLoading = true)
        is Result.Success -> ListState(items = result.data, isLoading = false)
        is Result.Error -> ListState(error = result.errorType, isLoading = false)
    }
}

fun <T> MutableStateFlow<DataState<T>>.handleDataResult(result: Result<T>) {
    value = when (result) {
        is Result.Loading -> DataState(isLoading = true)
        is Result.Success -> DataState(data = result.data, isLoading = false)
        is Result.Error -> DataState(error = result.errorType, isLoading = false)
    }
}