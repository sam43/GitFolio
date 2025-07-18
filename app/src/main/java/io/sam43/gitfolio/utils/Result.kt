package io.sam43.gitfolio.utils

sealed class Result<out T> {
    data class Success<out T>(val data: T) : Result<T>()
    data class Error(val errorType: ErrorType) : Result<Nothing>()
    data object Loading : Result<Nothing>()
}

inline fun <T> Result<T>.onSuccess(action: (value: T) -> Unit): Result<T> {
    if (this is Result.Success) action(data)
    return this
}

inline fun <T> Result<T>.onError(action: (errorType: ErrorType) -> Unit): Result<T> {
    if (this is Result.Error) action(errorType)
    return this
}

inline fun <T> Result<T>.onLoading(action: () -> Unit): Result<T> {
    if (this is Result.Loading) action()
    return this
}
