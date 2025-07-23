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
import io.sam43.gitfolio.utils.ErrorType
import io.sam43.gitfolio.utils.NetworkMonitor
import io.sam43.gitfolio.utils.NetworkStatus
import io.sam43.gitfolio.utils.Result
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

    private lateinit var userDetailsViewModel: UserProfileDetailsViewModel
    private val getProfileUseCase = mockk<GetUserDetailsUseCase>()
    private val getRepositoryUseCase = mockk<GetUserRepositoriesUseCase>()
    private val networkMonitor = mockk<NetworkMonitor>(relaxed = true)

    private val testDispatcher = UnconfinedTestDispatcher()
    private val testScope = TestScope(testDispatcher)
    
    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        setUpMockedNetworkStatus(NetworkStatus.Available)

        userDetailsViewModel = UserProfileDetailsViewModel(
            getProfileUseCase = getProfileUseCase,
            getRepositoryUseCase = getRepositoryUseCase,
            networkMonitor = networkMonitor
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        clearAllMocks()
    }

    private fun setUpMockedNetworkStatus(status: NetworkStatus) =
        every { networkMonitor.networkStatus } returns flowOf(status)

    @Test
    fun `initial state should be loading`() {
        // Given - userDetailsViewModel is created in setup

        // Then
        assertEquals(true, userDetailsViewModel.state.value.isLoading)
        assertEquals(null, userDetailsViewModel.state.value.user)
        assertEquals(emptyList<Repo>(), userDetailsViewModel.state.value.repositories)
        assertEquals(null, userDetailsViewModel.state.value.error)
    }

    @Test
    fun `fetchUserProfileDetails should update state with success data`() = testScope.runTest {
        // Given
        val userName = "testuser"
        val mockUser = mockk<UserDetail>()
        val mockRepos = listOf(mockk<Repo>(), mockk<Repo>())

        coEvery { getProfileUseCase.invoke(userName) } returns flowOf(Result.Success(mockUser))
        coEvery { getRepositoryUseCase.invoke(userName) } returns flowOf(Result.Success(mockRepos))

        // When
        userDetailsViewModel.fetchUserProfileDetails(userName)

        // Then
        val finalState = userDetailsViewModel.state.value
        assertEquals(false, finalState.isLoading)
        assertEquals(mockUser, finalState.user)
        assertEquals(mockRepos, finalState.repositories)
        assertEquals(null, finalState.error)

        coVerify { getProfileUseCase.invoke(userName) }
        coVerify { getRepositoryUseCase.invoke(userName) }
    }

    @Test
    fun `fetchUserProfileDetails should handle profile error only`() = testScope.runTest {
        // Given
        val userName = "testuser"
        val mockRepos = listOf(mockk<Repo>())
        val profileError = ErrorType.NetworkError

        coEvery { getProfileUseCase.invoke(userName) } returns flowOf(Result.Error(profileError))
        coEvery { getRepositoryUseCase.invoke(userName) } returns flowOf(Result.Success(mockRepos))

        // When
        userDetailsViewModel.fetchUserProfileDetails(userName)

        // Then
        val finalState = userDetailsViewModel.state.value
        assertEquals(false, finalState.isLoading)
        assertEquals(null, finalState.user)
        assertEquals(mockRepos, finalState.repositories)
        assertEquals(ErrorType.UnknownError("NetworkError"), finalState.error)
    }

    @Test
    fun `fetchUserProfileDetails should handle repository error only`() = testScope.runTest {
        // Given
        val userName = "testuser"
        val mockUser = mockk<UserDetail>()
        val repoError = ErrorType.NetworkError

        coEvery { getProfileUseCase.invoke(userName) } returns flowOf(Result.Success(mockUser))
        coEvery { getRepositoryUseCase.invoke(userName) } returns flowOf(Result.Error(repoError))

        // When
        userDetailsViewModel.fetchUserProfileDetails(userName)

        // Then
        val finalState = userDetailsViewModel.state.value
        assertEquals(false, finalState.isLoading)
        assertEquals(mockUser, finalState.user)
        assertEquals(emptyList<Repo>(), finalState.repositories)
        assertEquals(ErrorType.UnknownError("NetworkError"), finalState.error)
    }

    @Test
    fun `fetchUserProfileDetails should handle both profile and repository errors`() = testScope.runTest {
        // Given
        val userName = "testuser"
        val profileError = ErrorType.NetworkError
        val repoError = ErrorType.EmptyBodyError

        coEvery { getProfileUseCase.invoke(userName) } returns flowOf(Result.Error(profileError))
        coEvery { getRepositoryUseCase.invoke(userName) } returns flowOf(Result.Error(repoError))

        // When
        userDetailsViewModel.fetchUserProfileDetails(userName)

        // Then
        val finalState = userDetailsViewModel.state.value
        assertEquals(false, finalState.isLoading)
        assertEquals(null, finalState.user)
        assertEquals(emptyList<Repo>(), finalState.repositories)
        assertEquals(ErrorType.UnknownError("NetworkError; EmptyBodyError"), finalState.error)
    }

    @Test(expected = RuntimeException::class)
    fun `fetchUserProfileDetails should handle exception`() = testScope.runTest {
        // Given
        val userName = "testuser"
        val exceptionMessage = "Network timeout"

        coEvery { getProfileUseCase.invoke(userName) } throws RuntimeException(exceptionMessage)

        // When
        userDetailsViewModel.fetchUserProfileDetails(userName)

        // Then
        val finalState = userDetailsViewModel.state.value
        assertEquals(false, finalState.isLoading)
        assertEquals(null, finalState.user)
        assertEquals(emptyList<Repo>(), finalState.repositories)
        assertEquals(ErrorType.UnknownError(exceptionMessage), finalState.error)
    }

    @Test
    fun `fetchUserProfileByUsername should call fetchUserProfileDetails`() = testScope.runTest {
        // Given
        val userName = "testuser"
        val mockUser = mockk<UserDetail>()
        val mockRepos = listOf(mockk<Repo>())

        coEvery { getProfileUseCase.invoke(userName) } returns flowOf(Result.Success(mockUser))
        coEvery { getRepositoryUseCase.invoke(userName) } returns flowOf(Result.Success(mockRepos))

        // When
        userDetailsViewModel.fetchUserProfileByUsername(userName)

        // Then
        coVerify { getProfileUseCase.invoke(userName) }
        coVerify { getRepositoryUseCase.invoke(userName) }

        val finalState = userDetailsViewModel.state.value
        assertEquals(false, finalState.isLoading)
        assertEquals(mockUser, finalState.user)
        assertEquals(mockRepos, finalState.repositories)
    }


    @Test
    fun `state should be updated to loading when fetchUserProfileDetails starts`() = testScope.runTest {
        // Given
        val userName = "testuser"
        coEvery { getProfileUseCase.invoke(userName) } returns flowOf(Result.Success(mockk()))
        coEvery { getRepositoryUseCase.invoke(userName) } returns flowOf(Result.Success(emptyList()))

        // Initially set state to not loading
        userDetailsViewModel.fetchUserProfileDetails(userName)
        advanceUntilIdle()

        // When
        userDetailsViewModel.fetchUserProfileDetails(userName)

        // Then - state should be loading before completion
        advanceUntilIdle()
        assertEquals(false, userDetailsViewModel.state.value.isLoading)
    }
}