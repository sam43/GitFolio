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
    fun `when getUsers is called, should return success with list of users`() = runTest {
        // Given
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
        
        coEvery { mockUserRepository.getUsers(any(), any()) } returns flowOf(Result.Success(expectedUsers))

        // When
        val result = getUserListUseCase(0, 20).toList()

        // Then
        assertEquals(1, result.size)
        assertTrue(result[0] is Result.Success)
        assertEquals(expectedUsers, (result[0] as Result.Success).data)
    }

    @Test
    fun `when getUsers is called with empty list, should return empty list`() = runTest {
        // Given
        val expectedUsers = emptyList<User>()
        
        coEvery { mockUserRepository.getUsers(any(), any()) } returns flowOf(Result.Success(expectedUsers))

        // When
        val result = getUserListUseCase(0, 20).toList()
        println("Emitted results: $result")

        // Then
        assertEquals(1, result.size)
        assertTrue(result[0] is Result.Success)
        assertEquals(expectedUsers, (result[0] as Result.Success).data)
    }

    @Test
    fun `when getUsers fails, should return error result`() = runTest {
        // Given
        coEvery { mockUserRepository.getUsers(any(), any()) } returns flowOf(Result.Error(ErrorType.NetworkError))

        // When
        val result = getUserListUseCase(0, 20).toList()

        // Then
        assertEquals(1, result.size)
        assertTrue(result[0] is Result.Error)
        assertEquals(ErrorType.NetworkError, (result[0] as Result.Error).error)
    }

    @Test
    fun `when getUsers is loading, should return loading result`() = runTest {
        // Given
        coEvery { mockUserRepository.getUsers(any(), any()) } returns flowOf(Result.Loading)

        // When
        val result = getUserListUseCase(0, 20).toList()

        // Then
        assertEquals(1, result.size)
        assertTrue(result[0] is Result.Loading)
    }
}