package io.sam43.gitfolio.data.helper

import java.io.IOException

sealed class AppException(message: String) : IOException(message) {
    class ApiError(message: String) : AppException(message)
    class NetworkError(message: String = NETWORK_ERROR_MESSAGE) : AppException(message)
    class EmptyBodyError(message: String = EMPTY_BODY_MESSAGE) : AppException(message)
    class SearchQueryError(message: String = SEARCH_QUERY_ERROR_MESSAGE) : AppException(message)
}
