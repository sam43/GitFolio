
package io.sam43.gitfolio.utils

import java.io.IOException

sealed class CustomException(message: String) : IOException(message) {
    class ApiError(val statusCode: Int, message: String) : CustomException(message)
    class NetworkError(message: String) : CustomException(message)
    class EmptyBodyError(message: String) : CustomException(message)
    class SearchQueryError(message: String) : CustomException(message)
}
