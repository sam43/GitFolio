package io.sam43.gitfolio.presentation.viewmodels

import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.sam43.gitfolio.domain.model.Repo
import io.sam43.gitfolio.domain.model.UserDetail
import io.sam43.gitfolio.domain.usecases.GetUserDetailsUseCase
import io.sam43.gitfolio.domain.usecases.GetUserRepositoriesUseCase
import io.sam43.gitfolio.data.helper.ErrorType
import io.sam43.gitfolio.data.helper.Result
import io.sam43.gitfolio.presentation.userprofile.UserProfileContract
import io.sam43.gitfolio.presentation.userprofile.UserProfileViewModel
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


class UserProfileViewModelTest {

    private lateinit var viewModel: UserProfileViewModel
    private val getProfileUseCase = mockk<GetUserDetailsUseCase>()
    private val getRepositoryUseCase = mockk<GetUserRepositoriesUseCase>()

    private val testDispatcher = UnconfinedTestDispatcher()
    private val testScope = TestScope(testDispatcher)

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        viewModel = UserProfileViewModel(
            getUserDetailsUseCase = getProfileUseCase,
            getUserRepositoriesUseCase = getRepositoryUseCase
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        clearAllMocks()
    }

    @Test
    fun `fetchUserProfile should update both states with success data`() = testScope.runTest {
        // Given
        val userName = "testuser"
        val mockUser = mockk<UserDetail>()
        val mockRepos = listOf(mockk<Repo>(), mockk<Repo>())

        coEvery { getProfileUseCase.invoke(userName) } returns flowOf(Result.Success(mockUser))
        coEvery { getRepositoryUseCase.invoke(userName) } returns flowOf(Result.Success(mockRepos))

        // When
        viewModel.setEvent(UserProfileContract.Event.FetchUserProfile(userName))
        advanceUntilIdle()

        // Then
        val finalState = viewModel.state.value
        assertEquals(mockUser, finalState.user)
        assertEquals(mockRepos, finalState.repositories)
        assertEquals(false, finalState.isLoading)
        assertEquals(null, finalState.error)
        assertEquals(true, finalState.hasLoaded)

        coVerify { getProfileUseCase.invoke(userName) }
        coVerify { getRepositoryUseCase.invoke(userName) }
    }

    @Test
    fun `fetchUserProfile should handle profile success and repository error`() = testScope.runTest {
        // Given
        val userName = "testuser"
        val mockUser = mockk<UserDetail>()
        val repoError = ErrorType.NetworkError

        coEvery { getProfileUseCase.invoke(userName) } returns flowOf(Result.Success(mockUser))
        coEvery { getRepositoryUseCase.invoke(userName) } returns flowOf(Result.Error(repoError))

        // When
        viewModel.setEvent(UserProfileContract.Event.FetchUserProfile(userName))
        advanceUntilIdle()

        // Then
        val finalState = viewModel.state.value
        assertEquals(mockUser, finalState.user)
        assertEquals(emptyList<Repo>(), finalState.repositories)
        assertEquals(false, finalState.isLoading)
        assertEquals(repoError, finalState.error) // Should return the repository error
        assertEquals(true, finalState.hasLoaded)
    }

    @Test
    fun `fetchUserProfile should handle profile error and repository success`() = testScope.runTest {
        // Given
        val userName = "testuser"
        val profileError = ErrorType.NetworkError
        val mockRepos = listOf(mockk<Repo>())

        coEvery { getProfileUseCase.invoke(userName) } returns flowOf(Result.Error(profileError))
        coEvery { getRepositoryUseCase.invoke(userName) } returns flowOf(Result.Success(mockRepos))

        // When
        viewModel.setEvent(UserProfileContract.Event.FetchUserProfile(userName))
        advanceUntilIdle()

        // Then
        val finalState = viewModel.state.value
        assertEquals(null, finalState.user)
        assertEquals(mockRepos, finalState.repositories)
        assertEquals(false, finalState.isLoading)
        assertEquals(profileError, finalState.error) // Should return the profile error
        assertEquals(true, finalState.hasLoaded)
    }


    @Test
    fun `fetchUserProfile should handle both profile and repository errors`() = testScope.runTest {
        // Given
        val userName = "testuser"
        val profileError = ErrorType.NetworkError
        val repoError = ErrorType.ServerError

        coEvery { getProfileUseCase.invoke(userName) } returns flowOf(Result.Error(profileError))
        coEvery { getRepositoryUseCase.invoke(userName) } returns flowOf(Result.Error(repoError))

        // When
        viewModel.setEvent(UserProfileContract.Event.FetchUserProfile(userName))
        advanceUntilIdle()

        // Then
        val finalState = viewModel.state.value
        assertEquals(null, finalState.user)
        assertEquals(emptyList<Repo>(), finalState.repositories)
        assertEquals(false, finalState.isLoading)
        assertEquals(profileError, finalState.error)
        assertEquals(true, finalState.hasLoaded)

        coVerify { getProfileUseCase.invoke(userName) }
        coVerify { getRepositoryUseCase.invoke(userName) }
    }

    @Test
    fun `fetchUserProfile should handle loading states`() = testScope.runTest {
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
        viewModel.setEvent(UserProfileContract.Event.FetchUserProfile(userName))
        advanceUntilIdle()

        // Then - After completion, should not be loading
        val finalState = viewModel.state.value
        assertEquals(mockUser, finalState.user)
        assertEquals(mockRepos, finalState.repositories)
        assertEquals(false, finalState.isLoading)
        assertEquals(null, finalState.error)
        assertEquals(true, finalState.hasLoaded)
    }

    @Test
    fun `fetchUserProfile should handle exception and update user state with error`() = testScope.runTest {
        // Given
        val userName = "testuser"
        val exceptionMessage = "Network timeout"
        val exception = RuntimeException(exceptionMessage)

        coEvery { getProfileUseCase.invoke(userName) } throws exception
        coEvery { getRepositoryUseCase.invoke(userName) } returns flowOf(Result.Success(emptyList()))


        // When
        viewModel.setEvent(UserProfileContract.Event.FetchUserProfile(userName))
        advanceUntilIdle()

        // Then
        val finalState = viewModel.state.value
        assertEquals(null, finalState.user)
        assertEquals(emptyList<Repo>(), finalState.repositories)

        // The exception should update the user state with error
        assertEquals(ErrorType.UnknownError(exceptionMessage), finalState.error)
        assertEquals(false, finalState.isLoading)
        assertEquals(true, finalState.hasLoaded)
    }

    @Test
    fun `fetchUserProfile should handle repository use case exception`() = testScope.runTest {
        // Given
        val userName = "testuser"
        val mockUser = mockk<UserDetail>()
        val exceptionMessage = "Repository fetch failed"
        val exception = RuntimeException(exceptionMessage)

        coEvery { getProfileUseCase.invoke(userName) } returns flowOf(Result.Success(mockUser))
        coEvery { getRepositoryUseCase.invoke(userName) } throws exception

        // When
        viewModel.setEvent(UserProfileContract.Event.FetchUserProfile(userName))
        advanceUntilIdle()

        // Then
        val finalState = viewModel.state.value

        // Profile should succeed
        assertEquals(mockUser, finalState.user)
        assertEquals(false, finalState.isLoading)
        assertEquals(null, finalState.error)

        // But overall state should show the exception in user state (as per catch block)
        assertEquals(ErrorType.UnknownError(exceptionMessage), finalState.error)
        assertEquals(true, finalState.hasLoaded)
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
        viewModel.setEvent(UserProfileContract.Event.FetchUserProfile(userName))
        advanceUntilIdle()

        // Then - Test convenience properties
        val state = viewModel.state.value
        assertEquals(mockUser, state.user)
        assertEquals(mockRepos, state.repositories)
        assertEquals(false, state.isLoading)
        assertEquals(null, state.error)
        assertEquals(true, state.hasLoaded)
    }
}