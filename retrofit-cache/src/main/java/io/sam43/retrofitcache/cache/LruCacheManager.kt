package io.sam43.retrofitcache.cache

import android.content.Context
import androidx.room.*
import io.sam43.retrofitcache.cache.database.CacheDao
import io.sam43.retrofitcache.cache.database.CacheDatabase
import io.sam43.retrofitcache.cache.database.CacheEntry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

class LruCacheManager(
    context: Context,
    private val maxSize: Int = 200, // Maximum number of entries in the cache
    private val defaultMaxAge: Long = TimeUnit.HOURS.toMillis(1)
) {
    private val database: CacheDatabase = Room.databaseBuilder(
        context.applicationContext,
        CacheDatabase::class.java,
        "cache_database"
    ).build()

    private val cacheDao: CacheDao = database.cacheDao()

    suspend fun get(key: String): ByteArray? = withContext(Dispatchers.IO) {
        val entry = cacheDao.get(key)
        if (entry != null && !entry.isExpired()) {
            entry.data
        } else {
            if (entry != null) {
                cacheDao.delete(key)
            }
            null
        }
    }

    suspend fun put(key: String, data: ByteArray, maxAge: Long = defaultMaxAge) = withContext(Dispatchers.IO) {
        val entry = CacheEntry(key, data, System.currentTimeMillis(), maxAge)
        cacheDao.insert(entry)
        cleanupIfNeeded()
    }

    suspend fun remove(key: String) = withContext(Dispatchers.IO) {
        cacheDao.delete(key)
    }

    suspend fun clear() = withContext(Dispatchers.IO) {
        cacheDao.clear()
    }

    suspend fun contains(key: String): Boolean = withContext(Dispatchers.IO) {
        val entry = cacheDao.get(key)
        entry != null && !entry.isExpired()
    }

    private suspend fun cleanupIfNeeded() {
        val count = cacheDao.count()
        if (count > maxSize) {
            val entriesToRemove = count - maxSize
            val cutoffTimestamp = System.currentTimeMillis() - TimeUnit.HOURS.toMillis(24)
            cacheDao.deleteOlderThan(cutoffTimestamp)
            // If still over limit, we'll handle in the next cleanup
        }
    }

    suspend fun getStats(): CacheStats = withContext(Dispatchers.IO) {
        val totalEntries = cacheDao.count()
        CacheStats(
            totalEntries = totalEntries,
            maxSize = maxSize
        )
    }

    data class CacheStats(
        val totalEntries: Int,
        val maxSize: Int
    )
}