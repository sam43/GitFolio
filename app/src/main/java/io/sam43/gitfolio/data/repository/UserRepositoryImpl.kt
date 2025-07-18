package io.sam43.gitfolio.data.repository

import io.sam43.gitfolio.domain.model.Repo
import io.sam43.gitfolio.domain.model.User
import io.sam43.gitfolio.domain.model.UserDetail
import io.sam43.gitfolio.domain.repository.UserRepository
import io.sam43.gitfolio.utils.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    // Add your dependencies here (ApiService, UserDao, etc.)
) : UserRepository {

    override suspend fun searchUsers(query: String): Flow<Result<List<User>>> {
        // TODO: Implement actual API call
        return flowOf(Result.Success(emptyList()))
    }

    override suspend fun getUserDetails(username: String): Flow<Result<UserDetail>> {
        // TODO: Implement actual API call
        return flowOf(Result.Error(Exception("Not implemented yet")))
    }

    override suspend fun getUserRepositories(username: String): Flow<Result<List<Repo>>> {
        // TODO: Implement actual API call
        return flowOf(Result.Success(emptyList()))
    }
}
