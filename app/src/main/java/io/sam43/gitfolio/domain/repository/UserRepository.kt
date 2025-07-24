package io.sam43.gitfolio.domain.repository

import io.sam43.gitfolio.data.helper.ErrorType
import io.sam43.gitfolio.domain.model.User
import io.sam43.gitfolio.domain.model.UserDetail
import io.sam43.gitfolio.domain.model.Repo
import io.sam43.gitfolio.data.helper.Result
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    fun getUsers(): Flow<Result<List<User>, ErrorType>>
    fun searchUsers(query: String): Flow<Result<List<User>, ErrorType>>
    fun getUserDetails(username: String): Flow<Result<UserDetail, ErrorType>>
    fun getUserRepositories(username: String): Flow<Result<List<Repo>, ErrorType>>
}
