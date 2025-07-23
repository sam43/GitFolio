package io.sam43.retrofitcache.cache.database

import androidx.room.Database
import androidx.room.RoomDatabase


@Database(entities = [CacheEntry::class], version = 1)
abstract class CacheDatabase : RoomDatabase() {
    abstract fun cacheDao(): CacheDao
}