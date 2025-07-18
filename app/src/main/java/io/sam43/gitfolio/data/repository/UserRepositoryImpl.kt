package io.sam43.gitfolio.data.repository

import io.sam43.gitfolio.data.remote.ApiService
import io.sam43.gitfolio.domain.model.Repo
import io.sam43.gitfolio.domain.model.User
import io.sam43.gitfolio.domain.model.UserDetail
import io.sam43.gitfolio.domain.repository.UserRepository
import io.sam43.gitfolio.utils.Result
import io.sam43.retrofitcache.RetrofitCacheManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
    private val cacheManager: RetrofitCacheManager
) : UserRepository {
    override suspend fun getUsers(): Flow<Result<List<User>>> = flow {
        try {
            val response = apiService.getUsers()
            if (response.isSuccessful) {
                response.body()?.let { users ->
                    emit(Result.Success(users))
                } ?: emit(Result.Error(Exception("Empty response body")))
            } else {
                emit(Result.Error(Exception("API Error: ${response.code()}")))
            }
        } catch (e: Exception) {
            emit(Result.Error(e))
        }
    }

    override suspend fun searchUsers(query: String): Flow<Result<List<User>>> = flow {
        if (query.isEmpty()) {
            emit(Result.Error(Exception("Search query cannot be empty")))
            return@flow
        }
        try {
            val response = apiService.searchUsers(query)
            if (response.isSuccessful) {
                response.body()?.let { users ->
                    emit(Result.Success(users))
                } ?: emit(Result.Error(Exception("Empty response body")))
            } else {
                emit(Result.Error(Exception("API Error: ${response.code()}")))
            }
        } catch (e: Exception) {
            emit(Result.Error(e))
        }
    }

    override suspend fun getUserDetails(username: String): Flow<Result<UserDetail>> = flow {
        try {
            val response = apiService.getUser(username)
            if (response.isSuccessful) {
                response.body()?.let { userDetail ->
                    emit(Result.Success(userDetail))
                } ?: emit(Result.Error(Exception("Empty response body")))
            } else {
                emit(Result.Error(Exception("API Error: ${response.code()}")))
            }
        } catch (e: Exception) {
            emit(Result.Error(e))
        }
    }

    override suspend fun getUserRepositories(username: String): Flow<Result<List<Repo>>> = flow {
        try {
            val response = apiService.getUserRepos(username)
            if (response.isSuccessful) {
                response.body()?.let { repos ->
                    emit(Result.Success(repos))
                } ?: emit(Result.Error(Exception("Empty response body")))
            } else {
                emit(Result.Error(Exception("API Error: ${response.code()}")))
            }
        } catch (e: Exception) {
            emit(Result.Error(e))
        }
    }
    
    /**
     * Clear all cached data. Useful for refresh operations or logout.
     */
    suspend fun clearCache() {
        cacheManager.clearAllCache()
    }
    
    /**
     * Clean up expired cache entries to free up memory.
     */
    suspend fun cleanupExpiredCache() {
        cacheManager.cleanupExpiredCache()
    }
    
    /**
     * Get current cache size for debugging or monitoring purposes.
     */
    fun getCacheSize(): Int {
        return cacheManager.getCacheSize()
    }
    
    /**
     * Get cache statistics for monitoring.
     */
    fun getCacheStats() = cacheManager.getCacheStats()
}
