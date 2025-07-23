package io.sam43.retrofitcache

import io.sam43.retrofitcache.cache.LruCacheManager
import io.sam43.retrofitcache.interceptor.CacheInterceptor
import android.content.Context
import kotlinx.coroutines.runBlocking

class RetrofitCacheManager(
    private val cacheInterceptor: CacheInterceptor,
    private val lruCacheManager: LruCacheManager
) {

    fun getInterceptor(): CacheInterceptor = cacheInterceptor

    fun clearAllCache() {
        runBlocking { lruCacheManager.clear() }
    }

    fun cleanupExpiredCache(): Int {
        // This functionality is now handled internally by LruCacheManager
        return 0
    }

    fun getCacheSize(): Int {
        return runBlocking { lruCacheManager.getStats().totalEntries }
    }

    fun getCacheStats(): LruCacheManager.CacheStats {
        return runBlocking { lruCacheManager.getStats() }
    }

    fun isCached(request: okhttp3.Request): Boolean {
        val cacheKey = "${request.method}_${request.url}"
        return runBlocking { lruCacheManager.contains(cacheKey) }
    }

    fun removeCachedEntry(request: okhttp3.Request): Boolean {
        val cacheKey = "${request.method}_${request.url}"
        runBlocking { lruCacheManager.remove(cacheKey) }
        return true
    }

    class Builder(private val context: Context) {
        private var maxCacheSize: Int = 100
        private var enableDebugHeaders: Boolean = true

        fun maxCacheSize(size: Int): Builder {
            require(size > 0) { "Cache size must be positive, got: $size" }
            this.maxCacheSize = size
            return this
        }

        fun enableDebugHeaders(enabled: Boolean): Builder {
            this.enableDebugHeaders = enabled
            return this
        }

        fun build(): RetrofitCacheManager {
            val lruCacheManager = LruCacheManager(context, maxCacheSize)
            val cacheInterceptor = CacheInterceptor(lruCacheManager, enableDebugHeaders)
            return RetrofitCacheManager(cacheInterceptor, lruCacheManager)
        }
    }

    companion object {
        fun builder(context: Context): Builder = Builder(context)

        fun createDefault(context: Context): RetrofitCacheManager = builder(context).build()
    }
}