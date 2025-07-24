package io.sam43.gitfolio.data.repository

import io.mockk.*
import io.sam43.gitfolio.data.remote.ApiService
import io.sam43.gitfolio.domain.model.User
import io.sam43.gitfolio.data.helper.AppException
import io.sam43.gitfolio.data.helper.ErrorType
import io.sam43.gitfolio.data.helper.Result
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import retrofit2.Response

class UserRepositoryImplTest {

    private lateinit var userRepository: UserRepositoryImpl
    private lateinit var apiService: ApiService

    @Before
    fun setUp() {
        apiService = mockk()
        userRepository = UserRepositoryImpl(apiService)
    }

    @Test
    fun `when getUsers is called, should return success with list of users`() = runTest {
        // Given
        val users = listOf(
            User(id = 1, login = "user1", avatarUrl = "", htmlUrl = "", type = ""),
            User(id = 2, login = "user2", avatarUrl = "", htmlUrl = "", type = "")
        )
        coEvery { apiService.getUsers() } returns Response.success(users)

        // When
        val result = userRepository.getUsers().toList()

        // Then
        assertEquals(1, result.size)
        assertTrue(result[0] is Result.Success)
        assertEquals(users, (result[0] as Result.Success).data)
        coVerify { apiService.getUsers() }
    }

    @Test
    fun `when searchUsers is called with valid query, should return success with list of users`() = runTest {
        // Given
        val query = "octocat"
        val users = listOf(User(id = 1, login = "octocat", avatarUrl = "", htmlUrl = "", type = ""))
        coEvery { apiService.searchUsers(query) } returns Response.success(users)

        // When
        val result = userRepository.searchUsers(query).toList()

        // Then
        assertEquals(1, result.size)
        assertTrue(result[0] is Result.Success)
        assertEquals(users, (result[0] as Result.Success).data)
        coVerify { apiService.searchUsers(query) }
    }

    @Test
    fun `when searchUsers is called with empty query, should return error`() = runTest {
        // Given
        val query = ""

        // When
        val result = userRepository.searchUsers(query).toList()

        // Then
        assertEquals(1, result.size)
        assertTrue(result[0] is Result.Error)
        assertEquals(ErrorType.SearchQueryError, (result[0] as Result.Error).errorType)
    }

    @Test
    fun `when API call fails, should return error`() = runTest {
        // Given
        coEvery { apiService.getUsers() } throws AppException.NetworkError("Network error")

        // When
        val result = userRepository.getUsers().toList()

        // Then
        assertEquals(1, result.size)
        assertTrue(result[0] is Result.Error)
        assertEquals(ErrorType.NetworkError, (result[0] as Result.Error).errorType)
    }
}