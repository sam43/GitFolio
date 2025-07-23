package io.sam43.gitfolio.utils

import java.io.IOException
import retrofit2.HttpException

const val EMPTY_BODY_MESSAGE = "Received empty response from server."
const val SEARCH_QUERY_ERROR_MESSAGE = "Search query cannot be empty."
const val UNKNOWN_ERROR_MESSAGE = "An unknown error occurred."
const val NETWORK_ERROR_MESSAGE = "Network error. Please check your internet connection."

sealed class ErrorType {
    data class ApiError(val statusCode: Int, val message: String) : ErrorType() {
        override fun toString(): String = "ApiError($statusCode, $message)"
    }

    data object NetworkError : ErrorType() {
        override fun toString(): String = "NetworkError"
    }

    data class UnknownError(val message: String = UNKNOWN_ERROR_MESSAGE) : ErrorType() {
        override fun toString(): String = message
    }

    data object EmptyBodyError : ErrorType() {
        override fun toString(): String = "EmptyBodyError"
    }

    data object SearchQueryError : ErrorType() {
        override fun toString(): String = "SearchQueryError"
    }

    // HTTP 500-599 RANGE STATUS CODE RESULT
    data object ServerError : ErrorType() {
        override fun toString(): String = "ServerError"
    }
}

object ErrorHandler {
    fun handleError(throwable: Throwable): ErrorType {
        return when (throwable) {
            is AppException.NetworkError -> ErrorType.NetworkError
            is AppException.EmptyBodyError -> ErrorType.EmptyBodyError
            is AppException.SearchQueryError -> ErrorType.SearchQueryError
            is HttpException -> {
                when (throwable.code()) {
                    in 500..599 -> ErrorType.ServerError
                    else -> ErrorType.ApiError(throwable.code(), throwable.message())
                }
            }
            is IOException -> ErrorType.UnknownError(throwable.message ?: UNKNOWN_ERROR_MESSAGE)
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
            is ErrorType.ServerError -> "Server error occurred. Please try again later."
        }
    }
}