package io.sam43.gitfolio.data.repository

import io.sam43.gitfolio.domain.model.User
import io.sam43.gitfolio.utils.Result
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue

class UserRepositoryImplTest {
    
        private lateinit var userRepository: UserRepositoryImpl
    
        @Before
        fun setUp() {
                userRepository = UserRepositoryImpl()
            }
    
        @Test
        fun `when getUsers is called, should return success with empty list`() = runTest {
                // When
                val result = userRepository.getUsers().toList()
        
                // Then
                assertEquals(1, result.size)
                assertTrue(result[0] is Result.Success)
                assertEquals(emptyList<User>(), (result[0] as Result.Success).data)
            }

        @Test
        fun `when getUsers is called multiple times, should return consistent results`() = runTest {
                // When
                val result1 = userRepository.getUsers().toList()
                val result2 = userRepository.getUsers().toList()
        
                // Then
                assertEquals(result1.size, result2.size)
                assertEquals(result1[0]::class, result2[0]::class)
                if (result1[0] is Result.Success && result2[0] is Result.Success) {
                    assertEquals((result1[0] as Result.Success).data, (result2[0] as Result.Success).data)
                }
            }
    
        @Test
        fun `when searchUsers is called with valid query, should return success with empty list`() = runTest {
                // Given
                val query = "octocat"
        
                // When
                val result = userRepository.searchUsers(query).toList()
        
                // Then
                assertEquals(1, result.size)
                assertTrue(result[0] is Result.Success)
                assertEquals(emptyList<User>(), (result[0] as Result.Success).data)
            }
    
        @Test
        fun `when searchUsers is called with empty query, should return success with empty list`() = runTest {
                // Given
                val query = ""
        
                // When
                val result = userRepository.searchUsers(query).toList()
        
                // Then
                assertEquals(1, result.size)
                assertTrue(result[0] is Result.Success)
                assertEquals(emptyList<User>(), (result[0] as Result.Success).data)
            }
    
        @Test
        fun `when searchUsers is called with long query, should return success with empty list`() = runTest {
                // Given
                val query = "a".repeat(1000)
        
                // When
                val result = userRepository.searchUsers(query).toList()
        
                // Then
                assertEquals(1, result.size)
                assertTrue(result[0] is Result.Success)
                assertEquals(emptyList<User>(), (result[0] as Result.Success).data)
            }
    
        @Test
        fun `when searchUsers is called with special characters, should return success with empty list`() = runTest {
                // Given
                val query = "!@#$%^&*()"
        
                // When
                val result = userRepository.searchUsers(query).toList()
        
                // Then
                assertEquals(1, result.size)
                assertTrue(result[0] is Result.Success)
                assertEquals(emptyList<User>(), (result[0] as Result.Success).data)
            }
    
        @Test
        fun `when searchUsers is called with whitespace query, should return success with empty list`() = runTest {
                // Given
                val query = "   "
        
                // When
                val result = userRepository.searchUsers(query).toList()
        
                // Then
                assertEquals(1, result.size)
                assertTrue(result[0] is Result.Success)
                assertEquals(emptyList<User>(), (result[0] as Result.Success).data)
            }
    }