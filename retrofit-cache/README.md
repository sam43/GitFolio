# Retrofit Cache Library

A lightweight, annotation-based caching library for Retrofit2 that implements LRU (Least Recently Used) caching algorithm.

## Features

- 🚀 **Annotation-based**: Simple `@CacheControl` annotation to enable caching
- 🧠 **LRU Algorithm**: Efficient memory management with Least Recently Used eviction
- 🔒 **Thread-safe**: Concurrent operations supported
- ⚡ **Lightweight**: Minimal overhead and dependencies
- 🎯 **Flexible**: Configurable cache size and expiration per endpoint
- 🐛 **Debug-friendly**: Optional debug headers for cache hit/miss tracking
- 📊 **Monitoring**: Built-in cache statistics and monitoring

## Installation

### Gradle (Module-level)

```kotlin
dependencies {
    implementation project(':retrofit-cache')
}
```

### Add to settings.gradle.kts

```kotlin
include(":retrofit-cache")
```

## Quick Start

### 1. Setup the Cache Manager

```kotlin
val cacheManager = RetrofitCacheManager.builder()
    .maxCacheSize(200)           // Maximum 200 cached entries
    .enableDebugHeaders(true)    // Add debug headers
    .build()

val okHttpClient = OkHttpClient.Builder()
    .addInterceptor(cacheManager.getInterceptor())
    .build()

val retrofit = Retrofit.Builder()
    .baseUrl("https://api.example.com")
    .client(okHttpClient)
    .addConverterFactory(GsonConverterFactory.create())
    .build()
```

### 2. Annotate Your API Methods

```kotlin
interface ApiService {
    
    @CacheControl(maxAge = 60 * 5) // Cache for 5 minutes
    @GET("/users")
    suspend fun getUsers(): Response<List<User>>
    
    @CacheControl(maxAge = 60 * 2) // Cache for 2 minutes
    @GET("/search/users")
    suspend fun searchUsers(@Query("q") query: String): Response<List<User>>
    
    @CacheControl(maxAge = 60 * 10) // Cache for 10 minutes
    @GET("/users/{id}")
    suspend fun getUser(@Path("id") userId: String): Response<User>
}
```

### 3. Use Your API as Normal

```kotlin
class UserRepository @Inject constructor(
    private val apiService: ApiService,
    private val cacheManager: RetrofitCacheManager
) {
    
    suspend fun getUsers(): List<User> {
        val response = apiService.getUsers() // Automatically cached!
        return response.body() ?: emptyList()
    }
    
    fun clearCache() {
        cacheManager.clearAllCache()
    }
}
```

## Configuration Options

### @CacheControl Annotation

```kotlin
@CacheControl(
    maxAge = 300,    // Cache duration in seconds (default: 300 = 5 minutes)
    maxSize = -1     // Max entries for this endpoint (-1 = use global limit)
)
```

### RetrofitCacheManager Builder

```kotlin
val cacheManager = RetrofitCacheManager.builder()
    .maxCacheSize(100)           // Global max cache entries (default: 100)
    .enableDebugHeaders(true)    // Add X-Cache headers (default: true)
    .build()
```

## Cache Management

### Manual Cache Operations

```kotlin
// Clear all cached data
cacheManager.clearAllCache()

// Clean up expired entries
val removedCount = cacheManager.cleanupExpiredCache()

// Get cache statistics
val stats = cacheManager.getCacheStats()
println("Cache utilization: ${stats.utilizationPercentage}%")

// Check if specific request is cached
val isCached = cacheManager.isCached(request)

// Remove specific cached entry
cacheManager.removeCachedEntry(request)
```

### Cache Statistics

```kotlin
val stats = cacheManager.getCacheStats()
println("""
    Total entries: ${stats.totalEntries}
    Valid entries: ${stats.validEntries}
    Expired entries: ${stats.expiredEntries}
    Max size: ${stats.maxSize}
    Utilization: ${stats.utilizationPercentage}%
""")
```

## Debug Headers

When debug headers are enabled, cached responses include:

- `X-Cache: HIT` - Response served from cache
- `X-Cache: MISS` - Response fetched from network
- `X-Cache-TTL: 300` - Time to live in seconds

## How It Works

1. **Annotation Detection**: The interceptor detects `@CacheControl` annotations on Retrofit methods
2. **Cache Key Generation**: Creates unique keys based on HTTP method, URL, and parameters
3. **Cache Lookup**: Checks if valid cached data exists
4. **Network Fallback**: Makes network request if cache miss or expired
5. **LRU Eviction**: Removes least recently used entries when cache is full
6. **Automatic Cleanup**: Expired entries are removed on access

## Thread Safety

All cache operations are thread-safe using:
- `ConcurrentHashMap` for the main cache storage
- `ReentrantReadWriteLock` for access order management
- Atomic operations for statistics

## Best Practices

### Cache Duration Guidelines

```kotlin
// Static data - longer cache
@CacheControl(maxAge = 60 * 60) // 1 hour
@GET("/app/config")
suspend fun getAppConfig(): Response<Config>

// User data - medium cache
@CacheControl(maxAge = 60 * 10) // 10 minutes
@GET("/user/profile")
suspend fun getUserProfile(): Response<Profile>

// Search results - short cache
@CacheControl(maxAge = 60 * 2) // 2 minutes
@GET("/search")
suspend fun search(@Query("q") query: String): Response<SearchResults>

// Real-time data - very short cache
@CacheControl(maxAge = 30) // 30 seconds
@GET("/notifications")
suspend fun getNotifications(): Response<List<Notification>>
```

### Memory Management

```kotlin
// Periodic cleanup in background
class CacheMaintenanceService {
    
    @Scheduled(fixedRate = 300000) // Every 5 minutes
    fun cleanupExpiredCache() {
        val removed = cacheManager.cleanupExpiredCache()
        if (removed > 0) {
            Log.d("Cache", "Cleaned up $removed expired entries")
        }
    }
}
```

### Error Handling

```kotlin
suspend fun getUsers(): Result<List<User>> {
    return try {
        val response = apiService.getUsers()
        if (response.isSuccessful) {
            Result.Success(response.body() ?: emptyList())
        } else {
            Result.Error("API Error: ${response.code()}")
        }
    } catch (e: Exception) {
        // Cache might still serve stale data in offline scenarios
        Result.Error("Network error: ${e.message}")
    }
}
```

## Testing

The library includes comprehensive unit tests. Run them with:

```bash
./gradlew :retrofit-cache:test
```

## Requirements

- Android API 21+
- Kotlin 1.8+
- Retrofit 2.9+
- OkHttp 4.9+

## License

```
Copyright 2024 Sam43

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```