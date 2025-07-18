package io.sam43.retrofitcache

import com.google.common.truth.Truth.assertThat
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

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()

        cacheManager = RetrofitCacheManager.Builder()
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
        apiService.getCachedData().execute()

        // Then
        assertThat(cacheManager.getCacheSize()).isEqualTo(1)
    }

    @Test
    fun `when a cached request is made, response should be served from cache`() {
        // Given
        server.enqueue(MockResponse().setBody("response body"))

        // When
        apiService.getCachedData().execute() // First call to cache the response
        apiService.getCachedData().execute() // Second call

        // Then
        assertThat(server.requestCount).isEqualTo(1)
    }

    @Test
    fun `when clearAllCache is called, cache should be empty`() {
        // Given
        server.enqueue(MockResponse().setBody("response body"))
        apiService.getCachedData().execute()
        assertThat(cacheManager.getCacheSize()).isEqualTo(1)

        // When
        cacheManager.clearAllCache()

        // Then
        assertThat(cacheManager.getCacheSize()).isEqualTo(0)
    }
}