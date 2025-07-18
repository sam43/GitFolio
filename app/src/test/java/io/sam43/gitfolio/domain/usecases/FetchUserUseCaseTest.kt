package io.sam43.gitfolio.domain.usecases

import io.mockk.coEvery
import io.mockk.mockk
import io.sam43.gitfolio.domain.model.User
import io.sam43.gitfolio.domain.repository.UserRepository
import io.sam43.gitfolio.utils.Result
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue

class FetchUserUseCaseTest {

    private val mockUserRepository = mockk<UserRepository>()
    private lateinit var fetchUserUseCase: FetchUserUseCase

    @Before
    fun setUp() {
        fetchUserUseCase = FetchUserUseCase(mockUserRepository)
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
        val result = fetchUserUseCase(query).toList()

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
        val result = fetchUserUseCase(query).toList()

        // Then
        assertEquals(1, result.size)
        assertTrue(result[0] is Result.Success)
        assertEquals(expectedUsers, (result[0] as Result.Success).data)
    }

    @Test
    fun `when searchUsers fails, should return error result`() = runTest {
        // Given
        val query = "invalid"
        val exception = Exception("Network error")
        
        coEvery { mockUserRepository.searchUsers(query) } returns flowOf(Result.Error(exception))

        // When
        val result = fetchUserUseCase(query).toList()

        // Then
        assertEquals(1, result.size)
        assertTrue(result[0] is Result.Error)
        assertEquals(exception, (result[0] as Result.Error).exception)
    }

    @Test
    fun `when searchUsers is loading, should return loading result`() = runTest {
        // Given
        val query = "octocat"
        
        coEvery { mockUserRepository.searchUsers(query) } returns flowOf(Result.Loading)

        // When
        val result = fetchUserUseCase(query).toList()

        // Then
        assertEquals(1, result.size)
        assertTrue(result[0] is Result.Loading)
    }
}
