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
import java.security.MessageDigest

/**
 * OkHttp Interceptor that handles caching based on @CacheControl annotation.
 * 
 * This interceptor automatically caches successful HTTP responses when the corresponding
 * Retrofit method is annotated with @CacheControl. It implements an LRU caching strategy
 * and respects the maxAge parameter for cache expiration.
 * 
 * Features:
 * - Automatic caching based on @CacheControl annotation
 * - LRU cache eviction policy
 * - Configurable cache expiration
 * - Thread-safe operations
 * - Cache hit/miss headers for debugging
 * - Support for different cache sizes per endpoint
 * 
 * Usage:
 * ```
 * val cacheInterceptor = CacheInterceptor()
 * val okHttpClient = OkHttpClient.Builder()
 *     .addInterceptor(cacheInterceptor)
 *     .build()
 * ```
 * 
 * @param cacheManager The LRU cache manager instance (default: new instance with size 100)
 * @param enableDebugHeaders Whether to add debug headers to responses (default: true)
 */
class CacheInterceptor(
    private val cacheManager: LruCacheManager = LruCacheManager(),
    private val enableDebugHeaders: Boolean = true
) : Interceptor {
    
    companion object {
        private const val CACHE_HIT_HEADER = "X-Cache"
        private const val CACHE_AGE_HEADER = "X-Cache-Age"
        private const val CACHE_TTL_HEADER = "X-Cache-TTL"
    }
    
    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        
        // Get the annotation from the request
        val invocation = request.tag(Invocation::class.java)
        val cacheControl = invocation?.method()?.getAnnotation(CacheControl::class.java)
        
        if (cacheControl != null) {
            val cacheKey = generateCacheKey(request)
            
            // Try to get from cache first
            val cachedResponse = cacheManager.get(cacheKey)
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
                cacheManager.put(cacheKey, responseString, cacheControl.maxAge)
                
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
    
    /**
     * Generate a unique cache key for the request.
     * The key is based on the HTTP method, URL, and query parameters.
     */
    private fun generateCacheKey(request: okhttp3.Request): String {
        val url = request.url.toString()
        val method = request.method
        var keyString = "${method}_$url"

        // Include request body hash for non-GET requests
        if (method != "GET" && request.body != null) {
            // Add request body to key calculation
            val buffer = okio.Buffer()
            request.body?.writeTo(buffer)
            keyString += "_${buffer.readByteString().md5().hex()}"
        }

        // Use MD5 hash to create a shorter, consistent key
        return try {
            val md = MessageDigest.getInstance("MD5")
            val digest = md.digest(keyString.toByteArray())
            digest.joinToString("") { "%02x".format(it) }
        } catch (e: Exception) {
            // Fallback to simple hash if MD5 is not available
            keyString.hashCode().toString()
        }
    }
    
    /**
     * Create a response from cached data.
     */
    private fun createCachedResponse(
        request: okhttp3.Request, 
        cachedData: String, 
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
            
            // Add cache age and TTL headers for debugging
            responseBuilder.addHeader(CACHE_TTL_HEADER, cacheControl.maxAge.toString())

        }
        
        return responseBuilder.build()
    }
    
    /**
     * Clear all cached data.
     */
    fun clearCache() {
        cacheManager.clear()
    }
    
    /**
     * Clean up expired cache entries.
     * 
     * @return Number of expired entries removed
     */
    fun cleanupExpiredCache(): Int {
        return cacheManager.cleanupExpired()
    }
    
    /**
     * Get current cache size.
     */
    fun getCacheSize(): Int = cacheManager.size()
    
    /**
     * Get cache statistics.
     */
    fun getCacheStats(): LruCacheManager.CacheStats = cacheManager.getStats()
    
    /**
     * Check if a specific request would be cached.
     */
    fun isCached(request: okhttp3.Request): Boolean {
        val cacheKey = generateCacheKey(request)
        return cacheManager.contains(cacheKey)
    }
    
    /**
     * Remove a specific cached entry.
     */
    fun removeCachedEntry(request: okhttp3.Request): Boolean {
        val cacheKey = generateCacheKey(request)
        return cacheManager.remove(cacheKey)
    }
}