package io.sam43.gitfolio.data.remote

import io.sam43.gitfolio.domain.model.User
import io.sam43.gitfolio.domain.model.UserDetail
import io.sam43.gitfolio.domain.model.Repo
import io.sam43.retrofitcache.annotation.CacheControl
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    @CacheControl(maxAge = 60 * 5) // Cache for 5 minutes
    @GET("/users")
    suspend fun getUsers(): Response<List<User>>

    // Search results cached for 2 minutes (search results change frequently)
    @CacheControl(maxAge = 60 * 2)
    @GET("/search/users")
    suspend fun searchUsers(@Query("q") query: String): Response<List<User>>

    // User details cached for 10 minutes (user info doesn't change often)
    @CacheControl(maxAge = 60 * 10)
    @GET("/users/{username}")
    suspend fun getUser(@Path("username") username: String): Response<UserDetail>

    // User repos cached for 15 minutes
    @CacheControl(maxAge = 60 * 15)
    @GET("/users/{username}/repos")
    suspend fun getUserRepos(
        @Path("username") username: String
    ): Response<List<Repo>>
}
