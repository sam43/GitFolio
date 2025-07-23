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
    suspend fun getUsers(
        @Query("since") since: Int = 1,
        @Query("per_page") perPage: Int = 50
    ): Response<List<User>>

    @CacheControl(maxAge = 60 * 2)
    @GET("/search/users")
    suspend fun searchUsers(@Query("q") query: String): Response<List<User>>

    @CacheControl(maxAge = 60 * 10)
    @GET("/users/{username}")
    suspend fun getUser(@Path("username") username: String): Response<UserDetail>

    @CacheControl(maxAge = 60 * 15)
    @GET("/users/{username}/repos")
    suspend fun getUserRepos(
        @Path("username") username: String
    ): Response<List<Repo>>
}