package io.sam43.retrofitcache.cache.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "cache_entries")
data class CacheEntry(
    @PrimaryKey val key: String,
    @ColumnInfo(typeAffinity = ColumnInfo.BLOB) val data: ByteArray,
    val timestamp: Long,
    val maxAge: Long
) {
    fun isExpired(): Boolean = System.currentTimeMillis() - timestamp > maxAge

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as CacheEntry
        return key == other.key && data.contentEquals(other.data)
    }

    override fun hashCode(): Int {
        var result = key.hashCode()
        result = 31 * result + data.contentHashCode()
        return result
    }
}
