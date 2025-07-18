package io.sam43.retrofitcache.cache

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write

/**
 * Thread-safe LRU Cache implementation for caching network responses.
 * 
 * This cache manager implements the Least Recently Used (LRU) algorithm to manage
 * cached data efficiently. It automatically removes the least recently used items
 * when the cache reaches its maximum capacity.
 * 
 * Features:
 * - Thread-safe operations using ConcurrentHashMap and ReentrantReadWriteLock
 * - Automatic expiration based on maxAge
 * - LRU eviction policy
 * - Configurable maximum cache size
 * - Cleanup methods for expired entries
 * 
 * @param maxSize Maximum number of entries to keep in cache (default: 100)
 */
class LruCacheManager(private val maxSize: Int = 100) {
    
    private val cache = ConcurrentHashMap<String, CacheEntry>()
    private val accessOrder = LinkedHashMap<String, Long>()
    private val lock = ReentrantReadWriteLock()
    
    /**
     * Represents a cached entry with its data, timestamp, and expiration time.
     */
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
     * Get cached data if it exists and is not expired.
     * 
     * @param key The cache key
     * @return The cached data if available and valid, null otherwise
     */
    fun get(key: String): String? {
        return lock.read {
            val entry = cache[key]
            if (entry != null && !entry.isExpired()) {
                // Update access order
                lock.write {
                    accessOrder[key] = System.currentTimeMillis()
                }
                entry.data
            } else {
                // Remove expired entry
                if (entry != null) {
                    lock.write {
                        cache.remove(key)
                        accessOrder.remove(key)
                    }
                }
                null
            }
        }
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
        }
    }
    
    /**
     * Check if a key exists in cache and is not expired.
     * 
     * @param key The cache key
     * @return true if key exists and is valid, false otherwise
     */
    fun contains(key: String): Boolean {
        return lock.read {
            val entry = cache[key]
            entry != null && !entry.isExpired()
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