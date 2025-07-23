package io.sam43.gitfolio.data.repository

import io.sam43.gitfolio.data.remote.ApiService
import io.sam43.gitfolio.domain.model.Repo
import io.sam43.gitfolio.domain.model.User
import io.sam43.gitfolio.domain.model.UserDetail
import io.sam43.gitfolio.domain.repository.UserRepository
import io.sam43.gitfolio.data.helper.AppException
import io.sam43.gitfolio.data.helper.ErrorHandler
import io.sam43.gitfolio.data.helper.ErrorType
import io.sam43.gitfolio.data.helper.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val apiService: ApiService
) : UserRepository {
    override suspend fun getUsers(): Flow<Result<List<User>>> = flow {
        val response = apiService.getUsers()
        if (response.isSuccessful) {
            response.body()?.let { users ->
                emit(Result.Success(users))
            } ?: emit(Result.Error(ErrorHandler.handleError(AppException.EmptyBodyError())))
        } else {
            emit(Result.Error(ErrorHandler.handleError(AppException.ApiError("API Error: ${response.code()}"))))
        }
    }.catch { e ->
        emit(Result.Error(ErrorHandler.handleError(e as? Exception ?: Exception(e.message, e))))
    }

    override suspend fun searchUsers(query: String): Flow<Result<List<User>>> = flow {
        if (query.isEmpty()) {
            emit(Result.Error(ErrorType.SearchQueryError))
            return@flow
        }

        val response = apiService.searchUsers(query)
        if (response.isSuccessful) {
            response.body()?.let { users ->
                emit(Result.Success(users))
            } ?: emit(Result.Error(ErrorHandler.handleError(AppException.EmptyBodyError())))
        } else {
            emit(
                Result.Error(
                    ErrorHandler.handleError(
                        AppException.ApiError(
                            "API Error: ${response.code()}"
                        )
                    )
                )
            )
        }
    }.catch { e ->
        emit(Result.Error(ErrorHandler.handleError(e as? Exception ?: Exception(e.message, e))))
    }

    override suspend fun getUserDetails(username: String): Flow<Result<UserDetail>> = flow {
        val response = apiService.getUser(username)
        if (response.isSuccessful) {
            response.body()?.let { userDetail ->
                emit(Result.Success(userDetail))
            } ?: emit(Result.Error(ErrorHandler.handleError(AppException.EmptyBodyError())))
        } else {
            emit(Result.Error(ErrorHandler.handleError(AppException.ApiError("API Error: ${response.code()}"))))
        }
    }.catch { e ->
        emit(Result.Error(ErrorHandler.handleError(e as? Exception ?: Exception(e.message, e))))
    }

    override suspend fun getUserRepositories(username: String): Flow<Result<List<Repo>>> = flow {
        val response = apiService.getUserRepos(username)
        if (response.isSuccessful) {
            response.body()?.let { repos ->
                emit(Result.Success(repos))
            } ?: emit(Result.Error(ErrorHandler.handleError(AppException.EmptyBodyError())))
        } else {
            emit(Result.Error(ErrorHandler.handleError(AppException.ApiError("API Error: ${response.code()}"))))
        }
    }.catch { e ->
        emit(Result.Error(ErrorHandler.handleError(e as? Exception ?: Exception(e.message, e))))
    }
}