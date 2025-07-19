
package io.sam43.gitfolio.utils

import java.io.IOException

sealed class AppException(message: String) : IOException(message) {
    class ApiError(val statusCode: Int, message: String) : AppException(message)
    class NetworkError(message: String = NETWORK_ERROR_MESSAGE) : AppException(message)
    class EmptyBodyError(message: String = EMPTY_BODY_MESSAGE) : AppException(message)
    class SearchQueryError(message: String = SEARCH_QUERY_ERROR_MESSAGE) : AppException(message)
}
