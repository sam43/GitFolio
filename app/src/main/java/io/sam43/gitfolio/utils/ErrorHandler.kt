package io.sam43.gitfolio.utils

import java.io.IOException
import retrofit2.HttpException

const val EMPTY_BODY_MESSAGE = "Received empty response from server."
const val SEARCH_QUERY_ERROR_MESSAGE = "Search query cannot be empty."
const val UNKNOWN_ERROR_MESSAGE = "An unknown error occurred."
const val NETWORK_ERROR_MESSAGE = "Network error. Please check your internet connection."
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
            is AppException.ApiError -> ErrorType.ApiError(throwable.statusCode, throwable.message ?: "API Error")
            is AppException.NetworkError -> ErrorType.NetworkError
            is AppException.EmptyBodyError -> ErrorType.EmptyBodyError
            is AppException.SearchQueryError -> ErrorType.SearchQueryError
            is HttpException -> ErrorType.ApiError(throwable.code(), throwable.message())
            is IOException -> ErrorType.NetworkError
            else -> ErrorType.UnknownError(throwable.message ?: UNKNOWN_ERROR_MESSAGE)
        }
    }

    fun getErrorMessage(errorType: ErrorType): String {
        return when (errorType) {
            is ErrorType.ApiError -> "API Error: ${errorType.statusCode} - ${errorType.message}"
            is ErrorType.NetworkError -> NETWORK_ERROR_MESSAGE
            is ErrorType.UnknownError -> errorType.message
            is ErrorType.EmptyBodyError -> EMPTY_BODY_MESSAGE
            is ErrorType.SearchQueryError -> SEARCH_QUERY_ERROR_MESSAGE
        }
    }
}