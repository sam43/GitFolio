@file:OptIn(ExperimentalCoroutinesApi::class)

package io.sam43.gitfolio.presentation.viewmodels

import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.sam43.gitfolio.domain.model.User
import io.sam43.gitfolio.domain.usecases.GetUserListUseCase
import io.sam43.gitfolio.utils.ErrorType
import io.sam43.gitfolio.utils.NetworkMonitor
import io.sam43.gitfolio.utils.NetworkStatus
import io.sam43.gitfolio.utils.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class UserListViewModelTest {
    private lateinit var userListViewModel: UserListViewModel
    private val networkMonitor: NetworkMonitor = mockk(relaxed = true)
    private val getUserListUseCase: GetUserListUseCase = mockk()

    // Test Fixture - test user data
    private val testUser = User(
        id = 1,
        login = "octocat",
        avatarUrl = "https://github.com/images/octocat.png",
        htmlUrl = "https://github.com/octocat",
        type = "User"
    )


    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        userListViewModel = UserListViewModel(getUserListUseCase, networkMonitor)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        clearAllMocks()
    }

    private fun setUpMockedNetworkStatus(status: NetworkStatus) =
        every { networkMonitor.networkStatus } returns flowOf(status)

    @Test
    fun `fetchUsers should update users state on success`() = runTest {
        setUpMockedNetworkStatus(NetworkStatus.Available)
        // Given
        val expectedUsers = listOf(testUser)
        coEvery { getUserListUseCase() } returns flowOf(Result.Success(expectedUsers))

        // When
        userListViewModel.fetchUsers()

        // Then
        assertEquals(expectedUsers, userListViewModel.state.value.users)
        assertEquals(false, userListViewModel.state.value.isLoading)
        assertEquals(null, userListViewModel.state.value.error)
    }

    @Test
    fun `fetchUsers with loading state should update loading state`() = runTest {
        setUpMockedNetworkStatus(NetworkStatus.Available)
        // Given
        coEvery { getUserListUseCase() } returns flowOf(Result.Loading)

        // When
        userListViewModel.fetchUsers()

        // Then
        assertEquals(true, userListViewModel.state.value.isLoading)
        assertEquals(null, userListViewModel.state.value.error)
    }

    @Test
    fun `fetchUsers with error should update error state`() = runTest {
        setUpMockedNetworkStatus(NetworkStatus.Unavailable)
        // Given
        val errorType = ErrorType.NetworkError
        coEvery { getUserListUseCase() } returns flowOf(Result.Error(errorType))

        // When
        userListViewModel.fetchUsers()

        // Then
        assertEquals(false, userListViewModel.state.value.isLoading)
        assertEquals(errorType, userListViewModel.state.value.error)
    }

    @Test
    fun `searchUsers with valid query should update users state on success`() = runTest {
        setUpMockedNetworkStatus(NetworkStatus.Available)
        // Given
        val query = "octocat"
        val expectedUsers = listOf(testUser)
        coEvery { getUserListUseCase(query) } returns flowOf(Result.Success(expectedUsers))

        // When
        userListViewModel.searchUsers(query)

        // Then
        assertEquals(expectedUsers, userListViewModel.state.value.users)
        assertEquals(false, userListViewModel.state.value.isLoading)
        assertEquals(null, userListViewModel.state.value.error)
    }

    @Test
    fun `searchUsers with loading state should update loading state`() = runTest {
        setUpMockedNetworkStatus(NetworkStatus.Available)
        // Given
        val query = "octocat"
        coEvery { getUserListUseCase(query) } returns flowOf(Result.Loading)

        // When
        userListViewModel.searchUsers(query)

        // Then
        assertEquals(true, userListViewModel.state.value.isLoading)
        assertEquals(null, userListViewModel.state.value.error)
    }

    @Test
    fun `searchUsers with error should update error state`() = runTest {
        setUpMockedNetworkStatus(NetworkStatus.Unavailable)
        // Given
        val query = "octocat"
        val errorType = ErrorType.NetworkError
        coEvery { getUserListUseCase(query) } returns flowOf(Result.Error(errorType))

        // When
        userListViewModel.searchUsers(query)

        // Then
        assertEquals(false, userListViewModel.state.value.isLoading)
        assertEquals(errorType, userListViewModel.state.value.error)
    }

    @Test
    fun `searchUsers with empty query should call use case`() = runTest {
        setUpMockedNetworkStatus(NetworkStatus.Available)
        // Given
        val query = "z"
        coEvery { getUserListUseCase(query) } returns flowOf(Result.Success(emptyList()))

        // When
        userListViewModel.searchUsers(query)

        // Then
        coVerify { getUserListUseCase(query) }
    }
}