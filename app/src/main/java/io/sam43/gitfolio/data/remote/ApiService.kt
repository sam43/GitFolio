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
    @GET("users")
    @CacheControl(maxAge = 60 * 5) // Cache for 5 minutes
    suspend fun getUsers(
        @Query("since") since: Int = 1,
        @Query("per_page") perPage: Int = 50
    ): Response<List<User>>

    @GET("search/users")
    @CacheControl(maxAge = 60 * 2)
    suspend fun searchUsers(@Query("q") query: String): Response<List<User>>

    @GET("users/{username}")
    @CacheControl(maxAge = 60 * 10)
    suspend fun getUser(@Path("username") username: String): Response<UserDetail>

    @GET("users/{username}/repos")
    @CacheControl(maxAge = 60 * 15)
    suspend fun getUserRepos(
        @Path("username") username: String
    ): Response<List<Repo>>
}