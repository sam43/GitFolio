package io.sam43.gitfolio.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.sam43.gitfolio.BuildConfig
import io.sam43.gitfolio.data.remote.ApiService
import io.sam43.gitfolio.data.repository.UserRepositoryImpl
import io.sam43.gitfolio.domain.repository.UserRepository
import io.sam43.gitfolio.utils.isOnline
import okhttp3.Cache
import okhttp3.CacheControl
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.io.File
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataModule {

    @Provides
    @Singleton
    fun provideHttpLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
                } else {
                    HttpLoggingInterceptor.Level.BASIC
                }
        }
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(
        @ApplicationContext context: Context,
        loggingInterceptor: HttpLoggingInterceptor
    ): OkHttpClient {
        val cacheSize = 10 * 1024 * 1024L // Fixed: Use Long directly
        val cacheDirectory = File(context.cacheDir, "app-http-cache")
        val cache = Cache(cacheDirectory, cacheSize)

        return OkHttpClient.Builder()
            .cache(cache)
            .addInterceptor { chain ->
                val originalRequest = chain.request()
                // Add required headers
                val requestWithHeaders = originalRequest.newBuilder()
                    .addHeader("Authorization", "token ${BuildConfig.GITHUB_API_TOKEN}")
                    .addHeader("Accept", "application/vnd.github.v3+json")
                    .addHeader("X-GitHub-Api-Version", "2022-11-28")
                    .build()

                // Modify cache control based on network availability
                val request = if (context.isOnline()) {
                    requestWithHeaders.newBuilder()
                        .cacheControl(CacheControl.Builder().maxAge(5, TimeUnit.HOURS).build())
                        .build()
                } else {
                    requestWithHeaders.newBuilder()
                        .cacheControl(
                            CacheControl.Builder()
                                .onlyIfCached()
                                .maxStale(7, TimeUnit.DAYS)
                                .build()
                        )
                        .build()
                }
                println("🔍 REQUEST: ${request.url}")
                println("🔍 Cache-Control: ${request.header("Cache-Control")}")
                chain.proceed(request)
            }
            .addInterceptor { chain ->
                val response = chain.proceed(chain.request())
                println("🔍 RESPONSE CODE: ${response.code}")
                println("🔍 From Cache: ${response.cacheResponse != null}")
                println("🔍 From Network: ${response.networkResponse != null}")
                println("🔍 Response Cache-Control: ${response.header("Cache-Control")}")
                // Override server cache headers to enable caching
                response.newBuilder()
                    .header("Cache-Control", "public, max-age=${TimeUnit.HOURS.toSeconds(5)}")
                    .removeHeader("Pragma")
                    .build()
            }
            .apply {
                if (BuildConfig.DEBUG) addInterceptor(loggingInterceptor)
            }
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BuildConfig.GIHUB_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideApiService(retrofit: Retrofit): ApiService {
        return retrofit.create(ApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideUserRepository(userRepositoryImpl: UserRepositoryImpl): UserRepository {
        return userRepositoryImpl
    }
}

