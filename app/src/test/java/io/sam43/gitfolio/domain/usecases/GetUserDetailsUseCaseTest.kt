package io.sam43.gitfolio.domain.usecases

import io.mockk.coEvery
import io.mockk.mockk
import io.sam43.gitfolio.domain.model.UserDetail
import io.sam43.gitfolio.domain.repository.UserRepository
import io.sam43.gitfolio.utils.Result
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue

class GetUserDetailsUseCaseTest {

    private val mockUserRepository = mockk<UserRepository>()
    private lateinit var getUserDetailsUseCase: GetUserDetailsUseCase

    @Before
    fun setUp() {
        getUserDetailsUseCase = GetUserDetailsUseCase(mockUserRepository)
    }

    @Test
    fun `when getUserDetails is called with valid username, should return user details`() = runTest {
        // Given
        val username = "octocat"
        val expectedUserDetail = UserDetail(
            id = 1,
            login = "octocat",
            avatarUrl = "https://github.com/images/error/octocat_happy.gif",
            htmlUrl = "https://github.com/octocat",
            name = "The Octocat",
            company = "GitHub",
            blog = "https://github.com/blog",
            location = "San Francisco",
            email = "octocat@github.com",
            bio = "There once was...",
            publicRepos = 2,
            followers = 20,
            following = 0,
            createdAt = "2008-01-14T04:33:35Z",
            updatedAt = "2008-01-14T04:33:35Z"
        )
        
        coEvery { mockUserRepository.getUserDetails(username) } returns flowOf(Result.Success(expectedUserDetail))

        // When
        val result = getUserDetailsUseCase(username).toList()

        // Then
        assertEquals(1, result.size)
        assertTrue(result[0] is Result.Success)
        assertEquals(expectedUserDetail, (result[0] as Result.Success).data)
    }

    @Test
    fun `when getUserDetails is called with invalid username, should return error result`() = runTest {
        // Given
        val username = "nonexistentuser"
        val exception = Exception("User not found")
        
        coEvery { mockUserRepository.getUserDetails(username) } returns flowOf(Result.Error(exception))

        // When
        val result = getUserDetailsUseCase(username).toList()

        // Then
        assertEquals(1, result.size)
        assertTrue(result[0] is Result.Error)
        assertEquals(exception, (result[0] as Result.Error).exception)
    }

    @Test
    fun `when getUserDetails is loading, should return loading result`() = runTest {
        // Given
        val username = "octocat"
        
        coEvery { mockUserRepository.getUserDetails(username) } returns flowOf(Result.Loading)

        // When
        val result = getUserDetailsUseCase(username).toList()

        // Then
        assertEquals(1, result.size)
        assertTrue(result[0] is Result.Loading)
    }

    @Test
    fun `when getUserDetails is called with empty username, should return error result`() = runTest {
        // Given
        val username = ""
        val exception = Exception("Username cannot be empty")
        
        coEvery { mockUserRepository.getUserDetails(username) } returns flowOf(Result.Error(exception))

        // When
        val result = getUserDetailsUseCase(username).toList()

        // Then
        assertEquals(1, result.size)
        assertTrue(result[0] is Result.Error)
        assertEquals(exception, (result[0] as Result.Error).exception)
    }
}
