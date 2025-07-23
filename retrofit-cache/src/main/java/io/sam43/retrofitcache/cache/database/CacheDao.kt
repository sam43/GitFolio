package io.sam43.retrofitcache.cache.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface CacheDao {
    @Query("SELECT * FROM cache_entries WHERE `key` = :key")
    suspend fun get(key: String): CacheEntry?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: CacheEntry)

    @Query("DELETE FROM cache_entries WHERE `key` = :key")
    suspend fun delete(key: String)

    @Query("DELETE FROM cache_entries")
    suspend fun clear()

    @Query("SELECT COUNT(*) FROM cache_entries")
    suspend fun count(): Int

    @Query("DELETE FROM cache_entries WHERE timestamp < :timestamp")
    suspend fun deleteOlderThan(timestamp: Long)
}