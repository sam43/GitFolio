package io.sam43.gitfolio.data.helper

sealed class Result<out T, out E> {
    data class Success<out T>(val data: T) : Result<T, Nothing>()
    data class Error<out E>(val error: E) : Result<Nothing, E>()
    data object Loading : Result<Nothing, Nothing>()
}
