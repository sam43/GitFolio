package io.sam43.gitfolio.presentation.userlist

import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.mockk
import io.sam43.gitfolio.domain.model.User
import io.sam43.gitfolio.domain.usecases.GetUserListUseCase
import io.sam43.gitfolio.data.helper.ErrorType
import io.sam43.gitfolio.data.helper.Result
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
        userListViewModel = UserListViewModel(getUserListUseCase)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        clearAllMocks()
    }

    @Test
    fun `fetchUsers should update users state on success`() = runTest {
        // Given
        val expectedUsers = listOf(testUser)
        coEvery { getUserListUseCase(any(), any()) } returns flowOf(Result.Success(expectedUsers))

        // When
        userListViewModel.setEvent(UserListContract.Event.FetchUsers)

        // Then
        assertEquals(expectedUsers, userListViewModel.state.value.users)
        assertEquals(false, userListViewModel.state.value.isLoading)
        assertEquals(null, userListViewModel.state.value.error)
    }

    @Test
    fun `fetchUsers with loading state should update loading state`() = runTest {
        // Given
        coEvery { getUserListUseCase(any(), any()) } returns flowOf(Result.Loading)

        // When
        userListViewModel.setEvent(UserListContract.Event.FetchUsers)

        // Then
        assertEquals(true, userListViewModel.state.value.isLoading)
        assertEquals(null, userListViewModel.state.value.error)
    }

    @Test
    fun `fetchUsers with error should update error state`() = runTest {
        // Given
        val errorType = ErrorType.NetworkError
        coEvery { getUserListUseCase(any(), any()) } returns flowOf(Result.Error(errorType))

        // When
        userListViewModel.setEvent(UserListContract.Event.FetchUsers)

        // Then
        assertEquals(false, userListViewModel.state.value.isLoading)
        assertEquals(errorType, userListViewModel.state.value.error)
    }

    @Test
    fun `loadMoreUsers should append users to existing list`() = runTest {
        // Given
        val initialUsers = listOf(testUser)
        val newUsers = listOf(testUser.copy(id = 2, login = "user2"))
        coEvery { getUserListUseCase(0, 20) } returns flowOf(Result.Success(initialUsers))
        coEvery { getUserListUseCase(1, 20) } returns flowOf(Result.Success(newUsers))

        userListViewModel.setEvent(UserListContract.Event.FetchUsers)
        assertEquals(initialUsers, userListViewModel.state.value.users)

        // When
        userListViewModel.setEvent(UserListContract.Event.LoadMoreUsers)

        // Then
        assertEquals(initialUsers + newUsers, userListViewModel.state.value.users)
        assertEquals(false, userListViewModel.state.value.isLoading)
        assertEquals(null, userListViewModel.state.value.error)
    }

    @Test
    fun `refreshUsers should clear existing users and fetch new ones`() = runTest {
        // Given
        val initialUsers = listOf(testUser)
        val refreshedUsers = listOf(testUser.copy(id = 3, login = "user3"))
        coEvery { getUserListUseCase(0, 20) } returnsMany listOf(
            flowOf(Result.Success(initialUsers)),
            flowOf(Result.Success(refreshedUsers))
        )

        userListViewModel.setEvent(UserListContract.Event.FetchUsers)
        assertEquals(initialUsers, userListViewModel.state.value.users)

        // When
        userListViewModel.setEvent(UserListContract.Event.RefreshUsers)

        // Then
        assertEquals(refreshedUsers, userListViewModel.state.value.users)
        assertEquals(false, userListViewModel.state.value.isLoading)
        assertEquals(null, userListViewModel.state.value.error)
    }
}