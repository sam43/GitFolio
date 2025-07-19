package io.sam43.retrofitcache.cache

import android.content.Context
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.io.IOException
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write

/**
 * Thread-safe LRU Cache implementation with persistent storage for caching network responses.
 * 
 * This cache manager implements the Least Recently Used (LRU) algorithm to manage
 * cached data efficiently and persists data to disk for offline access.
 * 
 * Features:
 * - Thread-safe operations using ConcurrentHashMap and ReentrantReadWriteLock
 * - Persistent disk storage for offline access
 * - Automatic expiration based on maxAge
 * - LRU eviction policy
 * - Configurable maximum cache size
 * - Cleanup methods for expired entries
 * 
 * @param maxSize Maximum number of entries to keep in cache (default: 100)
 * @param context Android context for file operations
 */
class PersistentLruCacheManager(
    private val maxSize: Int = 100,
    private val context: Context
) {
    
    private val cache = ConcurrentHashMap<String, CacheEntry>()
    private val accessOrder = LinkedHashMap<String, Long>()
    private val lock = ReentrantReadWriteLock()
    private val cacheDir = File(context.cacheDir, "retrofit_cache")
    private val json = Json { ignoreUnknownKeys = true }
    
    init {
        // Ensure cache directory exists
        if (!cacheDir.exists()) {
            cacheDir.mkdirs()
        }
        // Load existing cache from disk
        loadCacheFromDisk()
    }
    
    /**
     * Serializable cache entry for disk persistence.
     */
    @Serializable
    data class CacheEntry(
        val data: String,
        val timestamp: Long,
        val maxAge: Long
    ) {
        /**
         * Checks if this cache entry has expired.
         */
        fun isExpired(): Boolean {
            return System.currentTimeMillis() - timestamp > maxAge * 1000
        }
        
        /**
         * Gets the remaining time to live in seconds.
         */
        fun getRemainingTtl(): Long {
            val elapsed = (System.currentTimeMillis() - timestamp) / 1000
            return maxOf(0, maxAge - elapsed)
        }
    }
    
    /**
     * Container for cache data and access order for disk persistence.
     */
    @Serializable
    private data class CacheContainer(
        val cacheEntries: Map<String, CacheEntry>,
        val accessOrder: Map<String, Long>
    )
    
    /**
     * Load cache from disk on initialization.
     */
    private fun loadCacheFromDisk() {
        lock.write {
            try {
                val cacheFile = File(cacheDir, "cache_data.json")
                if (cacheFile.exists()) {
                    val cacheData = cacheFile.readText()
                    val container = json.decodeFromString<CacheContainer>(cacheData)
                    
                    // Filter out expired entries during load
                    val validEntries = container.cacheEntries.filter { !it.value.isExpired() }
                    cache.clear()
                    cache.putAll(validEntries)
                    
                    accessOrder.clear()
                    accessOrder.putAll(container.accessOrder.filter { it.key in validEntries.keys })
                }
            } catch (e: Exception) {
                // If loading fails, start with empty cache
                cache.clear()
                accessOrder.clear()
            }
        }
    }
    
    /**
     * Save cache to disk.
     */
    private fun saveCacheToDisk() {
        try {
            val container = CacheContainer(
                cacheEntries = cache.toMap(),
                accessOrder = accessOrder.toMap()
            )
            val cacheFile = File(cacheDir, "cache_data.json")
            cacheFile.writeText(json.encodeToString(container))
        } catch (e: IOException) {
            // Silently handle file write errors
        }
    }
    
    /**
     * Get cached data if it exists and is not expired.
     * Falls back to expired cache if allowExpired is true (for offline scenarios).
     * 
     * @param key The cache key
     * @param allowExpired Whether to return expired cache entries (useful for offline mode)
     * @return The cached data if available, null otherwise
     */
    fun get(key: String, allowExpired: Boolean = false): String? {
        var entry: CacheEntry? = null
        var expired = false

        // Acquire read lock to get entry and check expiration
        lock.read {
            entry = cache[key]
            expired = entry?.isExpired() ?: false
        }

        if (entry != null && (!expired || allowExpired)) {
            // Acquire write lock to update access order
            lock.write {
                accessOrder[key] = System.currentTimeMillis()
            }
            return entry!!.data
        } else if (expired && !allowExpired) {
            // Acquire write lock to remove expired entry
            lock.write {
                cache.remove(key)
                accessOrder.remove(key)
                saveCacheToDisk()
            }
        }

        return null
    }
    
    /**
     * Put data into cache with specified max age.
     * 
     * @param key The cache key
     * @param data The data to cache
     * @param maxAge Maximum age in seconds
     */
    fun put(key: String, data: String, maxAge: Long) {
        lock.write {
            // Remove oldest entries if cache is full
            while (cache.size >= maxSize) {
                val oldestKey = accessOrder.keys.firstOrNull()
                if (oldestKey != null) {
                    cache.remove(oldestKey)
                    accessOrder.remove(oldestKey)
                } else {
                    break // Safety check
                }
            }
            
            val entry = CacheEntry(data, System.currentTimeMillis(), maxAge)
            cache[key] = entry
            accessOrder[key] = System.currentTimeMillis()
            
            // Save to disk
            saveCacheToDisk()
        }
    }
    
    /**
     * Check if a key exists in cache and is not expired.
     * 
     * @param key The cache key
     * @param allowExpired Whether to consider expired entries as existing
     * @return true if key exists and is valid, false otherwise
     */
    fun contains(key: String, allowExpired: Boolean = false): Boolean {
        return lock.read {
            val entry = cache[key]
            entry != null && (!entry.isExpired() || allowExpired)
        }
    }
    
    /**
     * Remove a specific key from cache.
     * 
     * @param key The cache key to remove
     * @return true if the key was removed, false if it didn't exist
     */
    fun remove(key: String): Boolean {
        return lock.write {
            val removed = cache.remove(key) != null
            accessOrder.remove(key)
            if (removed) {
                saveCacheToDisk()
            }
            removed
        }
    }
    
    /**
     * Clear all cached data.
     */
    fun clear() {
        lock.write {
            cache.clear()
            accessOrder.clear()
            saveCacheToDisk()
        }
    }
    
    /**
     * Remove expired entries from cache.
     * 
     * @return Number of expired entries removed
     */
    fun cleanupExpired(): Int {
        return lock.write {
            val expiredKeys = cache.entries
                .filter { it.value.isExpired() }
                .map { it.key }
            
            expiredKeys.forEach { key ->
                cache.remove(key)
                accessOrder.remove(key)
            }
            
            if (expiredKeys.isNotEmpty()) {
                saveCacheToDisk()
            }
            
            expiredKeys.size
        }
    }
    
    /**
     * Get current cache size.
     */
    fun size(): Int = cache.size
    
    /**
     * Get cache statistics for monitoring and debugging.
     */
    fun getStats(): CacheStats {
        return lock.read {
            val totalEntries = cache.size
            val expiredEntries = cache.values.count { it.isExpired() }
            val validEntries = totalEntries - expiredEntries
            
            CacheStats(
                totalEntries = totalEntries,
                validEntries = validEntries,
                expiredEntries = expiredEntries,
                maxSize = maxSize,
                utilizationPercentage = if (maxSize > 0) (totalEntries * 100.0 / maxSize) else 0.0
            )
        }
    }
    
    /**
     * Cache statistics data class.
     */
    data class CacheStats(
        val totalEntries: Int,
        val validEntries: Int,
        val expiredEntries: Int,
        val maxSize: Int,
        val utilizationPercentage: Double
    )
}
