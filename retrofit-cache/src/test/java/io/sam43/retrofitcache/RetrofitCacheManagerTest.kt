package io.sam43.retrofitcache

import android.content.Context
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import io.sam43.retrofitcache.annotation.CacheControl
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Test
import retrofit2.Invocation.Factory
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.http.GET
import java.io.File

// Define a simple TestApiService for testing purposes
interface TestApiService {
    @CacheControl(maxAge = 60) // Cache for 60 seconds
    @GET("/cached-data")
    suspend fun getCachedData(): String
}

class RetrofitCacheManagerTest {

    private lateinit var server: MockWebServer
    private lateinit var apiService: TestApiService
    private lateinit var cacheManager: RetrofitCacheManager
    private lateinit var mockContext: Context
    private lateinit var tempCacheDir: File

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()

        mockContext = mockk<Context>()
        tempCacheDir = createTempDir()
        every { mockContext.cacheDir } returns tempCacheDir

        cacheManager = RetrofitCacheManager.Builder(mockContext)
            .maxCacheSize(10)
            .build()

        val client = OkHttpClient.Builder()
            .addInterceptor(cacheManager.getInterceptor())
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(server.url("/"))
            .client(client)
            .addConverterFactory(ScalarsConverterFactory.create())
            .addCallAdapterFactory(Invocation.Factory.create()) // Crucial for annotation passing
            .build()

        apiService = retrofit.create(TestApiService::class.java)
    }

    @After
    fun tearDown() {
        server.shutdown()
        tempCacheDir.deleteRecursively() // Clean up the temporary cache directory
    }

    @Test
    fun `when a request is made, response should be cached`() {
        // Given
        server.enqueue(MockResponse().setBody("response body"))

        // When
        runBlocking { apiService.getCachedData() }

        // Then
        assertThat(cacheManager.getCacheSize()).isEqualTo(1)
    }

    @Test
    fun `when a cached request is made, response should be served from cache`() {
        // Given
        server.enqueue(MockResponse().setBody("response body"))

        // When
        runBlocking {
            apiService.getCachedData() // First call to cache the response
            apiService.getCachedData() // Second call
        }

        // Then
        assertThat(server.requestCount).isEqualTo(1)
    }

    @Test
    fun `when clearAllCache is called, cache should be empty`() {
        // Given
        server.enqueue(MockResponse().setBody("response body"))
        runBlocking { apiService.getCachedData() }
        assertThat(cacheManager.getCacheSize()).isEqualTo(1)

        // When
        cacheManager.clearAllCache()

        // Then
        assertThat(cacheManager.getCacheSize()).isEqualTo(0)
    }

    @Test
    fun `isCached should return true for cached requests`() {
        // Given
        server.enqueue(MockResponse().setBody("response body"))
        runBlocking { apiService.getCachedData() }

        // When
        val isCached = cacheManager.isCached(okhttp3.Request.Builder().url(server.url("/cached-data")).build())

        // Then
        assertThat(isCached).isTrue()
    }

    @Test
    fun `removeCachedEntry should remove a specific cached entry`() {
        // Given
        server.enqueue(MockResponse().setBody("response body"))
        runBlocking { apiService.getCachedData() }
        assertThat(cacheManager.getCacheSize()).isEqualTo(1)

        // When
        val removed = cacheManager.removeCachedEntry(okhttp3.Request.Builder().url(server.url("/cached-data")).build())

        // Then
        assertThat(removed).isTrue()
        assertThat(cacheManager.getCacheSize()).isEqualTo(0)
    }
}