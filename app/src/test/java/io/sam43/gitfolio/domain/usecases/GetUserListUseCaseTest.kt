package io.sam43.gitfolio.domain.usecases

import io.mockk.coEvery
import io.mockk.mockk
import io.sam43.gitfolio.domain.model.User
import io.sam43.gitfolio.domain.repository.UserRepository
import io.sam43.gitfolio.data.helper.ErrorType
import io.sam43.gitfolio.data.helper.Result
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue

class GetUserListUseCaseTest {

    private val mockUserRepository = mockk<UserRepository>(relaxed = true)
    private lateinit var getUserListUseCase: GetUserListUseCase

    @Before
    fun setUp() {
        getUserListUseCase = GetUserListUseCase(mockUserRepository)
    }

    @Test
    fun `when searchUsers is called with valid query, should return list of users`() = runTest {
        // Given
        val query = "octocat"
        val expectedUsers = listOf(
            User(
                id = 1,
                login = "octocat",
                avatarUrl = "https://github.com/images/error/octocat_happy.gif",
                htmlUrl = "https://github.com/octocat",
                type = "User"
            ),
            User(
                id = 2,
                login = "octocat2",
                avatarUrl = "https://github.com/images/error/octocat2_happy.gif",
                htmlUrl = "https://github.com/octocat2",
                type = "User"
            )
        )
        
        coEvery { mockUserRepository.searchUsers(query) } returns flowOf(Result.Success(expectedUsers))

        // When
        val result = getUserListUseCase(query).toList()

        // Then
        assertEquals(1, result.size)
        assertTrue(result[0] is Result.Success)
        assertEquals(expectedUsers, (result[0] as Result.Success).data)
    }

    @Test
    fun `when searchUsers is called with empty query, should return empty list`() = runTest {
        // Given
        val query = ""
        val expectedUsers = emptyList<User>()
        
        coEvery { mockUserRepository.searchUsers(query) } returns flowOf(Result.Success(expectedUsers))

        // When
        val result = getUserListUseCase(query).toList()
        println("Emitted results: $result")

        // Then
        assertEquals(0, result.size)
    }

    @Test
    fun `when searchUsers fails, should return error result`() = runTest {
        // Given
        val query = "invalid"
        
        coEvery { mockUserRepository.searchUsers(query) } returns flowOf(Result.Error(ErrorType.NetworkError))

        // When
        val result = getUserListUseCase(query).toList()

        // Then
        assertEquals(1, result.size)
        assertTrue(result[0] is Result.Error)
        assertEquals(ErrorType.NetworkError, (result[0] as Result.Error).errorType)
    }

    @Test
    fun `when searchUsers is loading, should return loading result`() = runTest {
        // Given
        val query = "octocat"
        
        coEvery { mockUserRepository.searchUsers(query) } returns flowOf(Result.Loading)

        // When
        val result = getUserListUseCase(query).toList()

        // Then
        assertEquals(1, result.size)
        assertTrue(result[0] is Result.Loading)
    }
}