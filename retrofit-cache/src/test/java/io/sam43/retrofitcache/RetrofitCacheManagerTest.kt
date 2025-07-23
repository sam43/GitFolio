package io.sam43.retrofitcache

import android.content.Context
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory

class RetrofitCacheManagerTest {

    private lateinit var server: MockWebServer
    private lateinit var apiService: TestApiService
    private lateinit var cacheManager: RetrofitCacheManager
    private lateinit var mockContext: Context

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()

        mockContext = mockk<Context>()
        every { mockContext.cacheDir } returns createTempDir()

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
            .build()

        apiService = retrofit.create(TestApiService::class.java)
    }

    @After
    fun tearDown() {
        server.shutdown()
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