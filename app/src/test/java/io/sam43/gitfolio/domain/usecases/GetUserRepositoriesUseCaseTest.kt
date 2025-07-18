package io.sam43.gitfolio.domain.usecases

import io.mockk.coEvery
import io.mockk.mockk
import io.sam43.gitfolio.domain.model.Repo
import io.sam43.gitfolio.domain.repository.UserRepository
import io.sam43.gitfolio.utils.Result
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue

class GetUserRepositoriesUseCaseTest {

    private val mockUserRepository = mockk<UserRepository>()
    private lateinit var getUserRepositoriesUseCase: GetUserRepositoriesUseCase

    @Before
    fun setUp() {
        getUserRepositoriesUseCase = GetUserRepositoriesUseCase(mockUserRepository)
    }

    @Test
    fun `when getUserRepositories is called, should return non-forked repositories`() = runTest {
        // Given
        val username = "octocat"
        val expectedRepos = listOf(
            Repo(
                id = 1,
                name = "Hello-World",
                fullName = "octocat/Hello-World",
                description = "This your first repo!",
                htmlUrl = "https://github.com/octocat/Hello-World",
                language = "JavaScript",
                stargazersCount = 80,
                forksCount = 9,
                openIssuesCount = 0,
                fork = false,
                createdAt = "2011-01-26T19:06:43Z",
                updatedAt = "2011-01-26T19:06:43Z",
                pushedAt = "2011-01-26T19:06:43Z"
            )
        )
        
        coEvery { mockUserRepository.getUserRepositories(username) } returns flowOf(Result.Success(expectedRepos))

        // When
        val result = getUserRepositoriesUseCase(username).toList()

        // Then
        assertEquals(1, result.size)
        assertTrue(result[0] is Result.Success)
        assertEquals(expectedRepos, (result[0] as Result.Success).data)
    }

    @Test
    fun `when getUserRepositories fails, should return error result`() = runTest {
        // Given
        val username = "invalid"
        val exception = Exception("Network error")
        
        coEvery { mockUserRepository.getUserRepositories(username) } returns flowOf(Result.Error(exception))

        // When
        val result = getUserRepositoriesUseCase(username).toList()

        // Then
        assertEquals(1, result.size)
        assertTrue(result[0] is Result.Error)
        assertEquals(exception, (result[0] as Result.Error).exception)
    }

    @Test
    fun `when getUserRepositories is loading, should return loading result`() = runTest {
        // Given
        val username = "octocat"
        
        coEvery { mockUserRepository.getUserRepositories(username) } returns flowOf(Result.Loading)

        // When
        val result = getUserRepositoriesUseCase(username).toList()

        // Then
        assertEquals(1, result.size)
        assertTrue(result[0] is Result.Loading)
    }

    @Test
    fun `when getUserRepositories is called, should filter out forked repositories`() = runTest {
        // Given
        val username = "octocat"
        val reposWithForks = listOf(
            Repo(
                id = 1,
                name = "Hello-World",
                fullName = "octocat/Hello-World",
                description = "This your first repo!",
                htmlUrl = "https://github.com/octocat/Hello-World",
                language = "JavaScript",
                stargazersCount = 80,
                forksCount = 9,
                openIssuesCount = 0,
                fork = false,
                createdAt = "2011-01-26T19:06:43Z",
                updatedAt = "2011-01-26T19:06:43Z",
                pushedAt = "2011-01-26T19:06:43Z"
            ),
            Repo(
                id = 2,
                name = "Forked-Repo",
                fullName = "octocat/Forked-Repo",
                description = "This is a forked repo",
                htmlUrl = "https://github.com/octocat/Forked-Repo",
                language = "Python",
                stargazersCount = 5,
                forksCount = 0,
                openIssuesCount = 1,
                fork = true,
                createdAt = "2011-01-26T19:06:43Z",
                updatedAt = "2011-01-26T19:06:43Z",
                pushedAt = "2011-01-26T19:06:43Z"
            )
        )
        
        val expectedNonForkedRepos = listOf(
            Repo(
                id = 1,
                name = "Hello-World",
                fullName = "octocat/Hello-World",
                description = "This your first repo!",
                htmlUrl = "https://github.com/octocat/Hello-World",
                language = "JavaScript",
                stargazersCount = 80,
                forksCount = 9,
                openIssuesCount = 0,
                fork = false,
                createdAt = "2011-01-26T19:06:43Z",
                updatedAt = "2011-01-26T19:06:43Z",
                pushedAt = "2011-01-26T19:06:43Z"
            )
        )
        
        coEvery { mockUserRepository.getUserRepositories(username) } returns flowOf(Result.Success(reposWithForks))

        // When
        val result = getUserRepositoriesUseCase(username).toList()

        // Then
        assertEquals(1, result.size)
        assertTrue(result[0] is Result.Success)
        assertEquals(expectedNonForkedRepos, (result[0] as Result.Success).data)
        // Verify that the forked repo was filtered out
        assertEquals(1, (result[0] as Result.Success).data.size)
    }
}
