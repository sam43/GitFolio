package io.sam43.retrofitcache.annotation

/**
 * Annotation to control caching behavior for Retrofit API calls.
 * 
 * Usage:
 * ```
 * @CacheControl(maxAge = 60 * 5) // Cache for 5 minutes
 * @GET("/users")
 * suspend fun getUsers(): Response<List<User>>
 * ```
 * 
 * @param maxAge Maximum age of cached data in seconds. Default is 300 seconds (5 minutes).
 * @param maxSize Maximum number of entries to keep in cache for this specific endpoint. 
 *                If not specified, uses the global cache size limit.
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class CacheControl(
    val maxAge: Long = 300, // 5 minutes default
    val maxSize: Int = -1   // -1 means use global cache size
)