package io.sam43.retrofitcache.interceptor

import io.sam43.retrofitcache.annotation.CacheControl
import io.sam43.retrofitcache.cache.LruCacheManager
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import retrofit2.Invocation
import java.io.IOException
import kotlinx.coroutines.runBlocking

class CacheInterceptor(
    private val cacheManager: LruCacheManager,
    private val enableDebugHeaders: Boolean = true
) : Interceptor {

    companion object {
        private const val CACHE_HIT_HEADER = "X-Cache"
        private const val CACHE_TTL_HEADER = "X-Cache-TTL"
    }

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()

        // Get the annotation from the request
        val invocation = request.tag(Invocation::class.java)
        val cacheControl = invocation?.method()?.getAnnotation(CacheControl::class.java)

        // Add logging to check if invocation and cacheControl are null
        println("CacheInterceptor: Invocation tag: $invocation")
        println("CacheInterceptor: CacheControl annotation: $cacheControl")

        if (cacheControl != null) {
            val cacheKey = generateCacheKey(request)

            // Try to get from cache first
            val cachedResponse = runBlocking { cacheManager.get(cacheKey) }
            if (cachedResponse != null) {
                return createCachedResponse(request, cachedResponse, cacheControl)
            }

            // If not in cache, proceed with network request
            val response = chain.proceed(request)

            // Cache successful responses
            if (response.isSuccessful && response.body != null) {
                val responseBody = response.body!!
                val responseString = responseBody.string()

                // Store in cache
                runBlocking { cacheManager.put(cacheKey, responseString.toByteArray(), cacheControl.maxAge) }

                // Create new response with the same body content
                val newResponse = response.newBuilder()
                    .body(responseString.toResponseBody(responseBody.contentType()))

                if (enableDebugHeaders) {
                    newResponse.addHeader(CACHE_HIT_HEADER, "MISS")
                }

                return newResponse.build()
            }

            return response
        }

        // If no cache control annotation, proceed normally
        return chain.proceed(request)
    }

    private fun generateCacheKey(request: okhttp3.Request): String {
        return "${request.method}_${request.url}"
    }

    private fun createCachedResponse(
        request: okhttp3.Request,
        cachedData: ByteArray,
        cacheControl: CacheControl
    ): Response {
        val responseBuilder = Response.Builder()
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .code(200)
            .message("OK")
            .body(cachedData.toResponseBody("application/json".toMediaType()))

        if (enableDebugHeaders) {
            responseBuilder.addHeader(CACHE_HIT_HEADER, "HIT")
            responseBuilder.addHeader(CACHE_TTL_HEADER, cacheControl.maxAge.toString())
        }

        return responseBuilder.build()
    }
}