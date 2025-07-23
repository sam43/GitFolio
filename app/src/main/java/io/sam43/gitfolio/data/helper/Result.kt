package io.sam43.gitfolio.data.helper

sealed class Result<out T> {
    data class Success<out T>(val data: T) : Result<T>()
    data class Error(val errorType: ErrorType) : Result<Nothing>()
    data object Loading : Result<Nothing>()
}
