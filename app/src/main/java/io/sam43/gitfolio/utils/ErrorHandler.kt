package io.sam43.gitfolio.utils

import java.io.IOException
import retrofit2.HttpException

sealed class ErrorType {
    data class ApiError(val statusCode: Int, val message: String) : ErrorType()
    data object NetworkError : ErrorType()
    data class UnknownError(val message: String) : ErrorType()
    data object EmptyBodyError : ErrorType()
    data object SearchQueryError : ErrorType()
}

object ErrorHandler {
    fun handleError(throwable: Throwable): ErrorType {
        return when (throwable) {
            is HttpException -> ErrorType.ApiError(throwable.code(), throwable.message())
            is IOException -> ErrorType.NetworkError
            is CustomException.ApiError -> ErrorType.ApiError(throwable.statusCode, throwable.message ?: "API Error")
            is CustomException.NetworkError -> ErrorType.NetworkError
            is CustomException.EmptyBodyError -> ErrorType.EmptyBodyError
            is CustomException.SearchQueryError -> ErrorType.SearchQueryError
            else -> ErrorType.UnknownError(throwable.message ?: "An unknown error occurred")
        }
    }

    fun getErrorMessage(errorType: ErrorType): String {
        return when (errorType) {
            is ErrorType.ApiError -> "API Error: ${errorType.statusCode} - ${errorType.message}"
            is ErrorType.NetworkError -> "Network error. Please check your internet connection."
            is ErrorType.UnknownError -> errorType.message
            is ErrorType.EmptyBodyError -> "Received empty response from server."
            is ErrorType.SearchQueryError -> "Search query cannot be empty."
        }
    }
}