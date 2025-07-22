package io.sam43.gitfolio.presentation.users

import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.coVerify
import io.sam43.gitfolio.domain.model.User
import io.sam43.gitfolio.domain.model.UserDetail
import io.sam43.gitfolio.domain.model.Repo
import io.sam43.gitfolio.domain.usecases.GetUserListUseCase
import io.sam43.gitfolio.domain.usecases.GetUserDetailsUseCase
import io.sam43.gitfolio.domain.usecases.GetUserRepositoriesUseCase
import io.sam43.gitfolio.utils.ErrorType
import io.sam43.gitfolio.utils.ErrorHandler
import io.sam43.gitfolio.utils.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*

@OptIn(ExperimentalCoroutinesApi::class)
class UserViewModelTest {
    
    private lateinit var userViewModel: UserViewModel
    private lateinit var getUserListUseCase: GetUserListUseCase
    private lateinit var getUserDetailsUseCase: GetUserDetailsUseCase
    private lateinit var getUserRepositoriesUseCase: GetUserRepositoriesUseCase

    // Test Fixture - test user data
    private val testUser = User(
        id = 1,
        login = "octocat",
        avatarUrl = "https://github.com/images/octocat.png",
        htmlUrl = "https://github.com/octocat",
        type = "User"
    )
    // Test Fixture - test user detail data
    private val testUserDetail = UserDetail(
        id = 1,
        login = "octocat",
        avatarUrl = "https://github.com/images/octocat.png",
        htmlUrl = "https://github.com/octocat",
        name = "The Octocat",
        company = "GitHub",
        blog = "https://github.blog",
        location = "San Francisco",
        email = "octocat@github.com",
        bio = "There once was...",
        publicRepos = 8,
        followers = 20,
        following = 0
    )
    // Test Fixture - test user repository data
    private val testRepo = Repo(
        id = 1,
        name = "Hello-World",
        fullName = "octocat/Hello-World",
        description = "This your first repo!",
        htmlUrl = "https://github.com/octocat/Hello-World",
        language = "C",
        stargazersCount = 80,
        forksCount = 9,
        openIssuesCount = 0,
        fork = false
    )
    
    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        
        getUserListUseCase = mockk()
        getUserDetailsUseCase = mockk()
        getUserRepositoriesUseCase = mockk()
        
        userViewModel = UserViewModel(
            getUserListUseCase,
            getUserDetailsUseCase,
            getUserRepositoriesUseCase
        )
    }
    
    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }
    
    @Test
    fun `getAllUsers should update users state on success`() = runTest {
        // Given
        val expectedUsers = listOf(testUser)
        coEvery { getUserListUseCase() } returns flowOf(Result.Success(expectedUsers))
        
        // When
        userViewModel.getAllUsers()
        
        // Then
        assertEquals(expectedUsers, userViewModel.users.value)
        assertEquals(false, userViewModel.isLoading.value)
        assertEquals(null, userViewModel.error.value)
    }
    
    @Test
    fun `getAllUsers with loading state should update loading state`() = runTest {
        // Given
        coEvery { getUserListUseCase() } returns flowOf(Result.Loading)
        
        // When
        userViewModel.getAllUsers()
        
        // Then
        assertEquals(true, userViewModel.isLoading.value)
        assertEquals(null, userViewModel.error.value)
    }
    
    @Test
    fun `getAllUsers with error should update error state`() = runTest {
        // Given
        val errorType = ErrorType.NetworkError
        coEvery { getUserListUseCase() } returns flowOf(Result.Error(errorType))
        
        // When
        userViewModel.getAllUsers()
        
        // Then
        assertEquals(false, userViewModel.isLoading.value)
        assertEquals(ErrorHandler.getErrorMessage(errorType), userViewModel.error.value)
    }

    @Test
    fun `searchUsers with valid query should update users state on success`() = runTest {
        // Given
        val query = "octocat"
        val expectedUsers = listOf(testUser)
        coEvery { getUserListUseCase(query) } returns flowOf(Result.Success(expectedUsers))
        
        // When
        userViewModel.searchUsers(query)
        
        // Then
        assertEquals(expectedUsers, userViewModel.users.value)
        assertEquals(false, userViewModel.isLoading.value)
        assertEquals(null, userViewModel.error.value)
    }
    
    @Test
    fun `searchUsers with loading state should update loading state`() = runTest {
        // Given
        val query = "octocat"
        coEvery { getUserListUseCase(query) } returns flowOf(Result.Loading)
        
        // When
        userViewModel.searchUsers(query)
        
        // Then
        assertEquals(true, userViewModel.isLoading.value)
        assertEquals(null, userViewModel.error.value)
    }
    
    @Test
    fun `searchUsers with error should update error state`() = runTest {
        // Given
        val query = "octocat"
        val errorType = ErrorType.NetworkError
        coEvery { getUserListUseCase(query) } returns flowOf(Result.Error(errorType))
        
        // When
        userViewModel.searchUsers(query)
        
        // Then
        assertEquals(false, userViewModel.isLoading.value)
        assertEquals(ErrorHandler.getErrorMessage(errorType), userViewModel.error.value)
    }
    
    @Test
    fun `searchUsers with empty query should call use case`() = runTest {
        // Given
        val query = ""
        coEvery { getUserListUseCase(query) } returns flowOf(Result.Success(emptyList()))
        
        // When
        userViewModel.searchUsers(query)
        
        // Then
        coVerify { getUserListUseCase(query) }
    }
    
    @Test
    fun `getUserDetails with valid username should update userDetail state on success`() = runTest {
        // Given
        val username = "octocat"
        coEvery { getUserDetailsUseCase(username) } returns flowOf(Result.Success(testUserDetail))
        
        // When
        userViewModel.getUserDetails(username)
        
        // Then
        assertEquals(testUserDetail, userViewModel.userDetail.value)
        assertEquals(false, userViewModel.isLoading.value)
        assertEquals(null, userViewModel.error.value)
    }
    
    @Test
    fun `getUserDetails with loading state should update loading state`() = runTest {
        // Given
        val username = "octocat"
        coEvery { getUserDetailsUseCase(username) } returns flowOf(Result.Loading)
        
        // When
        userViewModel.getUserDetails(username)
        
        // Then
        assertEquals(true, userViewModel.isLoading.value)
        assertEquals(null, userViewModel.error.value)
    }
    
    @Test
    fun `getUserDetails with error should update error state`() = runTest {
        // Given
        val username = "octocat"
        val errorType = ErrorType.ApiError(404, "User not found")
        coEvery { getUserDetailsUseCase(username) } returns flowOf(Result.Error(errorType))
        
        // When
        userViewModel.getUserDetails(username)
        
        // Then
        assertEquals(false, userViewModel.isLoading.value)
        assertEquals(ErrorHandler.getErrorMessage(errorType), userViewModel.error.value)
    }
    
    @Test
    fun `getUserRepos with valid username should update userRepos state on success`() = runTest {
        // Given
        val username = "octocat"
        val expectedRepos = listOf(testRepo)
        coEvery { getUserRepositoriesUseCase(username) } returns flowOf(Result.Success(expectedRepos))
        
        // When
        userViewModel.getUserRepos(username)
        
        // Then
        assertEquals(expectedRepos, userViewModel.userRepos.value)
        assertEquals(false, userViewModel.isLoading.value)
        assertEquals(null, userViewModel.error.value)
    }
    
    @Test
    fun `getUserRepos with loading state should update loading state`() = runTest {
        // Given
        val username = "octocat"
        coEvery { getUserRepositoriesUseCase(username) } returns flowOf(Result.Loading)
        
        // When
        userViewModel.getUserRepos(username)
        
        // Then
        assertEquals(true, userViewModel.isLoading.value)
        assertEquals(null, userViewModel.error.value)
    }
    
    @Test
    fun `getUserRepos with error should update error state`() = runTest {
        // Given
        val username = "octocat"
        val errorType = ErrorType.NetworkError
        coEvery { getUserRepositoriesUseCase(username) } returns flowOf(Result.Error(errorType))
        
        // When
        userViewModel.getUserRepos(username)
        
        // Then
        assertEquals(false, userViewModel.isLoading.value)
        assertEquals(ErrorHandler.getErrorMessage(errorType), userViewModel.error.value)
    }
    
    @Test
    fun `initial state should have correct default values`() {
        // Then
        assertEquals(emptyList<User>(), userViewModel.users.value)
        assertEquals(null, userViewModel.userDetail.value)
        assertEquals(emptyList<Repo>(), userViewModel.userRepos.value)
        assertEquals(false, userViewModel.isLoading.value)
        assertEquals(null, userViewModel.error.value)
    }
}