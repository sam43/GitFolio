package io.sam43.retrofitcache

import io.sam43.retrofitcache.cache.LruCacheManager
import io.sam43.retrofitcache.interceptor.CacheInterceptor

/**
 * Main entry point for the Retrofit Cache library.
 * 
 * This class provides a convenient way to create and manage cache components
 * for Retrofit networking. It encapsulates the cache manager and interceptor
 * setup, making it easy to integrate caching into your application.
 * 
 * Usage:
 * ```
 * val cacheManager = RetrofitCacheManager.Builder()
 *     .maxCacheSize(200)
 *     .enableDebugHeaders(true)
 *     .build()
 * 
 * val okHttpClient = OkHttpClient.Builder()
 *     .addInterceptor(cacheManager.getInterceptor())
 *     .build()
 * ```
 * 
 * @param cacheInterceptor The cache interceptor instance
 */
class RetrofitCacheManager private constructor(
    private val cacheInterceptor: CacheInterceptor
) {
    
    /**
     * Get the cache interceptor to add to OkHttpClient.
     */
    fun getInterceptor(): CacheInterceptor = cacheInterceptor
    
    /**
     * Clear all cached data.
     */
    fun clearAllCache() {
        cacheInterceptor.clearCache()
    }
    
    /**
     * Clean up expired cache entries.
     * 
     * @return Number of expired entries removed
     */
    fun cleanupExpiredCache(): Int {
        return cacheInterceptor.cleanupExpiredCache()
    }
    
    /**
     * Get current cache size.
     */
    fun getCacheSize(): Int {
        return cacheInterceptor.getCacheSize()
    }
    
    /**
     * Get cache statistics for monitoring and debugging.
     */
    fun getCacheStats(): LruCacheManager.CacheStats {
        return cacheInterceptor.getCacheStats()
    }
    
    /**
     * Check if a specific request would be cached.
     */
    fun isCached(request: okhttp3.Request): Boolean {
        return cacheInterceptor.isCached(request)
    }
    
    /**
     * Remove a specific cached entry.
     */
    fun removeCachedEntry(request: okhttp3.Request): Boolean {
        return cacheInterceptor.removeCachedEntry(request)
    }
    
    /**
     * Builder class for creating RetrofitCacheManager instances.
     */
    class Builder {
        private var maxCacheSize: Int = 100
        private var enableDebugHeaders: Boolean = true
        
        /**
         * Set the maximum cache size (number of entries).
         * 
         * @param size Maximum number of cache entries (default: 100)
         */
        fun maxCacheSize(size: Int): Builder {
            require(size > 0) { "Cache size must be positive, got: $size" }
            this.maxCacheSize = size
            return this
        }
        
        /**
         * Enable or disable debug headers in cached responses.
         * 
         * @param enabled Whether to add debug headers (default: true)
         */
        fun enableDebugHeaders(enabled: Boolean): Builder {
            this.enableDebugHeaders = enabled
            return this
        }
        
        /**
         * Build the RetrofitCacheManager instance.
         */
        fun build(): RetrofitCacheManager {
            val lruCacheManager = LruCacheManager(maxCacheSize)
            val cacheInterceptor = CacheInterceptor(lruCacheManager, enableDebugHeaders)
            return RetrofitCacheManager(cacheInterceptor)
        }
    }
    
    companion object {
        /**
         * Create a new builder instance.
         */
        fun builder(): Builder = Builder()
        
        /**
         * Create a default RetrofitCacheManager instance.
         */
        fun createDefault(): RetrofitCacheManager = builder().build()
    }
}