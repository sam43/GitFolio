package io.sam43.gitfolio.data.repository

import android.util.Log
import io.sam43.gitfolio.data.remote.ApiService
import io.sam43.gitfolio.domain.model.Repo
import io.sam43.gitfolio.domain.model.User
import io.sam43.gitfolio.domain.model.UserDetail
import io.sam43.gitfolio.domain.repository.UserRepository
import io.sam43.gitfolio.data.helper.AppException
import io.sam43.gitfolio.data.helper.ErrorHandler
import io.sam43.gitfolio.data.helper.ErrorType
import io.sam43.gitfolio.data.helper.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.io.IOException
import java.net.SocketTimeoutException
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val apiService: ApiService
) : UserRepository {
    override fun getUsers(since: Int, perPage: Int): Flow<Result<List<User>, ErrorType>> = flow {
        emit(Result.Loading) // Optional: emit loading state

        val response = apiService.getUsers(since, perPage)

        when {
            response.isSuccessful -> {
                val users = response.body()
                if (!users.isNullOrEmpty()) {
                    Log.d("UserRepository", "Success - From cache: ${response.raw().cacheResponse != null}")
                    emit(Result.Success(users))
                } else {
                    emit(Result.Error(ErrorHandler.handleError(AppException.EmptyBodyError())))
                }
            }
            response.code() == 504 -> {
                // Gateway timeout - likely offline with no cache
                Log.w("UserRepository", "Offline with no cached data")
                emit(Result.Error(ErrorHandler.handleError(AppException.NetworkError("No cached data available"))))
            }
            else -> {
                Log.e("UserRepository", "HTTP Error: ${response.code()}")
                emit(Result.Error(ErrorHandler.handleError(AppException.ApiError("HTTP ${response.code()}"))))
            }
        }
    }.flowOn(Dispatchers.IO).catch { e ->
        emit(Result.Error(ErrorHandler.handleError(e)))
    }

    override fun searchUsers(query: String): Flow<Result<List<User>, ErrorType>> = flow {
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

    override fun getUserDetails(username: String): Flow<Result<UserDetail, ErrorType>> = flow {
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

    override fun getUserRepositories(username: String): Flow<Result<List<Repo>, ErrorType>> = flow {
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