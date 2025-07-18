package io.sam43.gitfolio.domain.repository

import io.sam43.gitfolio.domain.model.User
import io.sam43.gitfolio.domain.model.UserDetail
import io.sam43.gitfolio.domain.model.Repo
import io.sam43.gitfolio.utils.Result
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    suspend fun getUsers(): Flow<Result<List<User>>>
    suspend fun searchUsers(query: String): Flow<Result<List<User>>>
    suspend fun getUserDetails(username: String): Flow<Result<UserDetail>>
    suspend fun getUserRepositories(username: String): Flow<Result<List<Repo>>>
}
