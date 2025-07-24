@file:OptIn(ExperimentalCoroutinesApi::class)

package io.sam43.gitfolio.presentation.viewmodels

import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.sam43.gitfolio.domain.model.Repo
import io.sam43.gitfolio.domain.model.UserDetail
import io.sam43.gitfolio.domain.usecases.GetUserDetailsUseCase
import io.sam43.gitfolio.domain.usecases.GetUserRepositoriesUseCase
import io.sam43.gitfolio.data.helper.ErrorType
import io.sam43.gitfolio.data.helper.NetworkMonitor
import io.sam43.gitfolio.data.helper.NetworkStatus
import io.sam43.gitfolio.data.helper.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test


class UserProfileDetailsViewModelTest {

    private lateinit var viewModel: UserProfileDetailsViewModel
    private val getProfileUseCase = mockk<GetUserDetailsUseCase>()
    private val getRepositoryUseCase = mockk<GetUserRepositoriesUseCase>()
    private val networkMonitor = mockk<NetworkMonitor>(relaxed = true)

    private val testDispatcher = UnconfinedTestDispatcher()
    private val testScope = TestScope(testDispatcher)

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        every { networkMonitor.networkStatus } returns flowOf(NetworkStatus.Available)

        viewModel = UserProfileDetailsViewModel(
            getProfileUseCase = getProfileUseCase,
            getRepositoryUseCase = getRepositoryUseCase
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        clearAllMocks()
    }

    @Test
    fun `fetchUserProfileByUsername should update both states with success data`() = testScope.runTest {
        // Given
        val userName = "testuser"
        val mockUser = mockk<UserDetail>()
        val mockRepos = listOf(mockk<Repo>(), mockk<Repo>())

        coEvery { getProfileUseCase.invoke(userName) } returns flowOf(Result.Success(mockUser))
        coEvery { getRepositoryUseCase.invoke(userName) } returns flowOf(Result.Success(mockRepos))

        // When
        viewModel.fetchUserProfileByUsername(userName)
        advanceUntilIdle()

        // Then
        val finalState = viewModel.state.value
        assertEquals(mockUser, finalState.user)
        assertEquals(mockRepos, finalState.repositories)
        assertEquals(false, finalState.isLoading)
        assertEquals(null, finalState.error)

        // Verify individual states
        assertEquals(mockUser, finalState.userState.data)
        assertEquals(false, finalState.userState.isLoading)
        assertEquals(null, finalState.userState.error)

        assertEquals(mockRepos, finalState.repositoriesState.items)
        assertEquals(false, finalState.repositoriesState.isLoading)
        assertEquals(null, finalState.repositoriesState.error)

        coVerify { getProfileUseCase.invoke(userName) }
        coVerify { getRepositoryUseCase.invoke(userName) }
    }

    @Test
    fun `fetchUserProfileByUsername should handle profile success and repository error`() = testScope.runTest {
        // Given
        val userName = "testuser"
        val mockUser = mockk<UserDetail>()
        val repoError = ErrorType.NetworkError

        coEvery { getProfileUseCase.invoke(userName) } returns flowOf(Result.Success(mockUser))
        coEvery { getRepositoryUseCase.invoke(userName) } returns flowOf(Result.Error(repoError))

        // When
        viewModel.fetchUserProfileByUsername(userName)
        advanceUntilIdle()

        // Then
        val finalState = viewModel.state.value
        assertEquals(mockUser, finalState.user)
        assertEquals(emptyList<Repo>(), finalState.repositories)
        assertEquals(false, finalState.isLoading)
        assertEquals(repoError, finalState.error) // Should return the repository error

        // Verify individual states
        assertEquals(mockUser, finalState.userState.data)
        assertEquals(false, finalState.userState.isLoading)
        assertEquals(null, finalState.userState.error)

        assertEquals(emptyList<Repo>(), finalState.repositoriesState.items)
        assertEquals(false, finalState.repositoriesState.isLoading)
        assertEquals(repoError, finalState.repositoriesState.error)
    }

    @Test
    fun `fetchUserProfileByUsername should handle profile error and repository success`() = testScope.runTest {
        // Given
        val userName = "testuser"
        val profileError = ErrorType.NetworkError
        val mockRepos = listOf(mockk<Repo>())

        coEvery { getProfileUseCase.invoke(userName) } returns flowOf(Result.Error(profileError))
        coEvery { getRepositoryUseCase.invoke(userName) } returns flowOf(Result.Success(mockRepos))

        // When
        viewModel.fetchUserProfileByUsername(userName)
        advanceUntilIdle()

        // Then
        val finalState = viewModel.state.value
        assertEquals(null, finalState.user)
        assertEquals(mockRepos, finalState.repositories)
        assertEquals(false, finalState.isLoading)
        assertEquals(profileError, finalState.error) // Should return the profile error

        // Verify individual states
        assertEquals(null, finalState.userState.data)
        assertEquals(false, finalState.userState.isLoading)
        assertEquals(profileError, finalState.userState.error)

        assertEquals(mockRepos, finalState.repositoriesState.items)
        assertEquals(false, finalState.repositoriesState.isLoading)
        assertEquals(null, finalState.repositoriesState.error)
    }


    @Test
    fun `fetchUserProfileByUsername should handle both profile and repository errors`() = testScope.runTest {
        // Given
        val userName = "testuser"
        val profileError = ErrorType.NetworkError
        val repoError = ErrorType.ServerError

        coEvery { getProfileUseCase.invoke(userName) } returns flowOf(Result.Error(profileError))
        coEvery { getRepositoryUseCase.invoke(userName) } returns flowOf(Result.Error(repoError))

        // When
        viewModel.fetchUserProfileByUsername(userName)
        advanceUntilIdle()

        // Then
        val finalState = viewModel.state.value
        assertEquals(null, finalState.user)
        assertEquals(emptyList<Repo>(), finalState.repositories)
        assertEquals(false, finalState.isLoading)
        assertEquals(profileError, finalState.error)

        // Verify individual states have their respective errors
        assertEquals(profileError, finalState.userState.error)
        assertEquals(repoError, finalState.repositoriesState.error)

        coVerify { getProfileUseCase.invoke(userName) }
        coVerify { getRepositoryUseCase.invoke(userName) }
    }

    @Test
    fun `fetchUserProfileByUsername should handle loading states`() = testScope.runTest {
        // Given
        val userName = "testuser"
        val mockUser = mockk<UserDetail>()
        val mockRepos = listOf(mockk<Repo>())

        coEvery { getProfileUseCase.invoke(userName) } returns flowOf(
            Result.Loading,
            Result.Success(mockUser)
        )
        coEvery { getRepositoryUseCase.invoke(userName) } returns flowOf(
            Result.Loading,
            Result.Success(mockRepos)
        )

        // When
        viewModel.fetchUserProfileByUsername(userName)
        advanceUntilIdle()

        // Then - After completion, should not be loading
        val finalState = viewModel.state.value
        assertEquals(mockUser, finalState.user)
        assertEquals(mockRepos, finalState.repositories)
        assertEquals(false, finalState.isLoading)
        assertEquals(null, finalState.error)
    }

    @Test
    fun `fetchUserProfileByUsername should handle exception and update user state with error`() = testScope.runTest {
        // Given
        val userName = "testuser"
        val exceptionMessage = "Network timeout"
        val exception = RuntimeException(exceptionMessage)

        coEvery { getProfileUseCase.invoke(userName) } throws exception
        coEvery { getRepositoryUseCase.invoke(userName) } returns flowOf(Result.Success(emptyList()))


        // When
        viewModel.fetchUserProfileByUsername(userName)
        advanceUntilIdle()

        // Then
        val finalState = viewModel.state.value
        assertEquals(null, finalState.user)
        assertEquals(emptyList<Repo>(), finalState.repositories)

        // The exception should update the user state with error
        assertEquals(ErrorType.UnknownError(exceptionMessage), finalState.userState.error)
        assertEquals(false, finalState.userState.isLoading)

        // Repository state should remain unchanged (default state)
        assertEquals(emptyList<Repo>(), finalState.repositoriesState.items)
        assertEquals(false, finalState.repositoriesState.isLoading)
        assertEquals(null, finalState.repositoriesState.error)
    }

    @Test
    fun `fetchUserProfileByUsername should handle repository use case exception`() = testScope.runTest {
        // Given
        val userName = "testuser"
        val mockUser = mockk<UserDetail>()
        val exceptionMessage = "Repository fetch failed"
        val exception = RuntimeException(exceptionMessage)

        coEvery { getProfileUseCase.invoke(userName) } returns flowOf(Result.Success(mockUser))
        coEvery { getRepositoryUseCase.invoke(userName) } throws exception

        // When
        viewModel.fetchUserProfileByUsername(userName)
        advanceUntilIdle()

        // Then
        val finalState = viewModel.state.value

        // Profile should succeed
        assertEquals(mockUser, finalState.userState.data)
        assertEquals(false, finalState.userState.isLoading)
        assertEquals(null, finalState.userState.error)

        // But overall state should show the exception in user state (as per catch block)
        assertEquals(ErrorType.UnknownError(exceptionMessage), finalState.repositoriesState.error)
    }

    @Test
    fun `state should provide convenient access properties`() = testScope.runTest {
        // Given
        val userName = "testuser"
        val mockUser = mockk<UserDetail>()
        val mockRepos = listOf(mockk<Repo>(), mockk<Repo>())

        coEvery { getProfileUseCase.invoke(userName) } returns flowOf(Result.Success(mockUser))
        coEvery { getRepositoryUseCase.invoke(userName) } returns flowOf(Result.Success(mockRepos))

        // When
        viewModel.fetchUserProfileByUsername(userName)
        advanceUntilIdle()

        // Then - Test convenience properties
        val state = viewModel.state.value
        assertEquals(mockUser, state.user)
        assertEquals(mockRepos, state.repositories)
        assertEquals(false, state.isLoading)
        assertEquals(null, state.error)
    }
}