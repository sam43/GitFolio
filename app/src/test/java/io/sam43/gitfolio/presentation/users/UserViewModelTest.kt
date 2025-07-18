package io.sam43.gitfolio.presentation.users

import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.coVerify
import io.sam43.gitfolio.domain.model.User
import io.sam43.gitfolio.domain.model.UserDetail
import io.sam43.gitfolio.domain.model.Repo
import io.sam43.gitfolio.domain.usecases.FetchUserUseCase
import io.sam43.gitfolio.domain.usecases.GetUserDetailsUseCase
import io.sam43.gitfolio.domain.usecases.GetUserRepositoriesUseCase
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
    private lateinit var fetchUserUseCase: FetchUserUseCase
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
        following = 0,
        createdAt = "2008-01-14T04:33:35Z",
        updatedAt = "2008-01-14T04:33:35Z"
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
        fork = false,
        createdAt = "2011-01-26T19:01:12Z",
        updatedAt = "2011-01-26T19:14:43Z",
        pushedAt = "2011-01-26T19:06:43Z"
    )
    
    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        
        fetchUserUseCase = mockk()
        getUserDetailsUseCase = mockk()
        getUserRepositoriesUseCase = mockk()
        
        userViewModel = UserViewModel(
            fetchUserUseCase,
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
        coEvery { fetchUserUseCase() } returns flowOf(Result.Success(expectedUsers))
        
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
        coEvery { fetchUserUseCase() } returns flowOf(Result.Loading)
        
        // When
        userViewModel.getAllUsers()
        
        // Then
        assertEquals(true, userViewModel.isLoading.value)
        assertEquals(null, userViewModel.error.value)
    }
    
    @Test
    fun `getAllUsers with error should update error state`() = runTest {
        // Given
        val errorMessage = "Network error"
        val exception = RuntimeException(errorMessage)
        coEvery { fetchUserUseCase() } returns flowOf(Result.Error(exception))
        
        // When
        userViewModel.getAllUsers()
        
        // Then
        assertEquals(false, userViewModel.isLoading.value)
        assertEquals(errorMessage, userViewModel.error.value)
    }

    @Test
    fun `searchUsers with valid query should update users state on success`() = runTest {
        // Given
        val query = "octocat"
        val expectedUsers = listOf(testUser)
        coEvery { fetchUserUseCase(query) } returns flowOf(Result.Success(expectedUsers))
        
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
        coEvery { fetchUserUseCase(query) } returns flowOf(Result.Loading)
        
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
        val errorMessage = "Network error"
        val exception = RuntimeException(errorMessage)
        coEvery { fetchUserUseCase(query) } returns flowOf(Result.Error(exception))
        
        // When
        userViewModel.searchUsers(query)
        
        // Then
        assertEquals(false, userViewModel.isLoading.value)
        assertEquals(errorMessage, userViewModel.error.value)
    }
    
    @Test
    fun `searchUsers with empty query should call use case`() = runTest {
        // Given
        val query = ""
        coEvery { fetchUserUseCase(query) } returns flowOf(Result.Success(emptyList()))
        
        // When
        userViewModel.searchUsers(query)
        
        // Then
        coVerify { fetchUserUseCase(query) }
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
        val errorMessage = "User not found"
        val exception = RuntimeException(errorMessage)
        coEvery { getUserDetailsUseCase(username) } returns flowOf(Result.Error(exception))
        
        // When
        userViewModel.getUserDetails(username)
        
        // Then
        assertEquals(false, userViewModel.isLoading.value)
        assertEquals(errorMessage, userViewModel.error.value)
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
        val errorMessage = "Failed to fetch repositories"
        val exception = RuntimeException(errorMessage)
        coEvery { getUserRepositoriesUseCase(username) } returns flowOf(Result.Error(exception))
        
        // When
        userViewModel.getUserRepos(username)
        
        // Then
        assertEquals(false, userViewModel.isLoading.value)
        assertEquals(errorMessage, userViewModel.error.value)
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