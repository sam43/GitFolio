package io.sam43.gitfolio.presentation.state

import androidx.compose.animation.core.copy
import androidx.compose.ui.semantics.error
import io.sam43.gitfolio.data.helper.ErrorType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import io.sam43.gitfolio.data.helper.Result

interface UiState<T> {
    val data: T?
    val isLoading: Boolean
    val error: ErrorType?
}
data class ListUiState<T>(
    override val data: List<T> = emptyList(),
    override val isLoading: Boolean = false,
    override val error: ErrorType? = null
) : UiState<List<T>> {
    val items: List<T> get() = data
}

data class DataUiState<D>(
    override val data: D? = null,
    override val isLoading: Boolean = false,
    override val error: ErrorType? = null
) : UiState<D>

/**
 * Updates a MutableStateFlow holding a ListUiState based on a Result.
 * Preserves existing items on error if the list is not empty (for offline cache).
 *
 * @param result The Result of the operation.
 * @param T The type of items in the list.
 * @param E The type of the error.
 */
fun <T, E : ErrorType> MutableStateFlow<ListUiState<T>>.updateWithListResult(
    result: Result<List<T>, E>
) {
    when (result) {
        is Result.Success -> {
            update {currentState ->
                currentState.copy(
                    data = result.data,
                    isLoading = false,
                    error = null
                )
            }
        }
        is Result.Error -> {
            update { currentState ->
                currentState.copy(
                    isLoading = false,
                    error = result.error,
                    data = currentState.data.ifEmpty { emptyList() }
                )
            }
        }

        Result.Loading -> update { currentState ->
            currentState.copy(
                isLoading = true,
                error = null,
                data = currentState.data.ifEmpty { emptyList() }
            )
        }
    }
}

/**
 * Updates a MutableStateFlow holding a ListUiState based on a Result.
 * Preserves existing items on error if the list is not empty (for offline cache).
 *
 * @param result The Result of the operation.
 * @param D The type of data.
 * @param E The type of the error.
 */
fun <D, E : ErrorType> MutableStateFlow<DataUiState<D>>.updateWithDataResult(
    result: Result<D, E>
) {
    when (result) {
        is Result.Success -> {
            update {currentState ->
                currentState.copy(
                    data = result.data,
                    isLoading = false,
                    error = null
                )
            }
        }
        is Result.Error -> {
            update { currentState ->
                currentState.copy(
                    isLoading = false,
                    error = result.error,
                    data = currentState.data.takeIf { currentState.data != null }
                )
            }
        }

        Result.Loading -> update { currentState ->
            currentState.copy(
                isLoading = true,
                error = null,
                data = currentState.data.takeIf { currentState.data != null }
            )
        }
    }
}
