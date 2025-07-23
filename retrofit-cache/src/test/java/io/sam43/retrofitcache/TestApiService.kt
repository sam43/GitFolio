
package io.sam43.retrofitcache

import io.sam43.retrofitcache.annotation.CacheControl
import retrofit2.Call
import retrofit2.http.GET

interface TestApiService {
    @GET("/")
    @CacheControl(maxAge = 60)
    fun getCachedData(): Call<String>
}
