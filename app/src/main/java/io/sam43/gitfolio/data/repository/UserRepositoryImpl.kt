package io.sam43.gitfolio.data.repository

import io.sam43.gitfolio.data.remote.ApiService
import io.sam43.gitfolio.domain.model.Repo
import io.sam43.gitfolio.domain.model.User
import io.sam43.gitfolio.domain.model.UserDetail
import io.sam43.gitfolio.domain.repository.UserRepository
import io.sam43.gitfolio.utils.AppException
import io.sam43.gitfolio.utils.ErrorHandler
import io.sam43.gitfolio.utils.ErrorType
import io.sam43.gitfolio.utils.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val apiService: ApiService
) : UserRepository {
    override suspend fun getUsers(): Flow<Result<List<User>>> = flow {
        try {
            val response = apiService.getUsers()
            if (response.isSuccessful) {
                response.body()?.let { users ->
                    emit(Result.Success(users))
                } ?: emit(Result.Error(ErrorHandler.handleError(AppException.EmptyBodyError())))
            } else {
                emit(Result.Error(ErrorHandler.handleError(AppException.ApiError(response.code(), "API Error: ${response.code()}"))))
            }
        } catch (e: Exception) {
            emit(Result.Error(ErrorHandler.handleError(e)))
        }
    }

    override suspend fun searchUsers(query: String): Flow<Result<List<User>>> = flow {
        if (query.isEmpty()) {
            emit(Result.Error(ErrorType.SearchQueryError))
            return@flow
        }
        try {
            val response = apiService.searchUsers(query)
            if (response.isSuccessful) {
                response.body()?.let { users ->
                    emit(Result.Success(users))
                } ?: emit(Result.Error(ErrorHandler.handleError(AppException.EmptyBodyError())))
            } else {
                emit(Result.Error(ErrorHandler.handleError(AppException.ApiError(response.code(), "API Error: ${response.code()}"))))
            }
        } catch (e: Exception) {
            emit(Result.Error(ErrorHandler.handleError(e)))
        }
    }

    override suspend fun getUserDetails(username: String): Flow<Result<UserDetail>> = flow {
        try {
            val response = apiService.getUser(username)
            if (response.isSuccessful) {
                response.body()?.let { userDetail ->
                    emit(Result.Success(userDetail))
                } ?: emit(Result.Error(ErrorHandler.handleError(AppException.EmptyBodyError())))
            } else {
                emit(Result.Error(ErrorHandler.handleError(AppException.ApiError(response.code(), "API Error: ${response.code()}"))))
            }
        } catch (e: Exception) {
            emit(Result.Error(ErrorHandler.handleError(e)))
        }
    }

    override suspend fun getUserRepositories(username: String): Flow<Result<List<Repo>>> = flow {
        try {
            val response = apiService.getUserRepos(username)
            if (response.isSuccessful) {
                response.body()?.let { repos ->
                    emit(Result.Success(repos))
                } ?: emit(Result.Error(ErrorHandler.handleError(AppException.EmptyBodyError())))
            } else {
                emit(Result.Error(ErrorHandler.handleError(AppException.ApiError(response.code(), "API Error: ${response.code()}"))))
            }
        } catch (e: Exception) {
            emit(Result.Error(ErrorHandler.handleError(e)))
        }
    }
}