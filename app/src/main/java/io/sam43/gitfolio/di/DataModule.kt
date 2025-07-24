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
                var request = chain.request()
                request = request.newBuilder()
                    .addHeader("Authorization", "token ${BuildConfig.GITHUB_API_TOKEN}")
                    .addHeader("Accept", "application/vnd.github.v3+json")
                    .addHeader("X-GitHub-Api-Version", "2022-11-28")
                    .build()

                if (!context.isOnline()) {
                    request = request.newBuilder()
                        .cacheControl(CacheControl.Builder()
                            .onlyIfCached()
                            .maxStale(1, TimeUnit.DAYS)
                            .build())
                        .build()
                }

                chain.proceed(request)
            }
            // Network interceptor for online caching
            .addNetworkInterceptor { chain ->
                val response = chain.proceed(chain.request())
                if (response.isSuccessful) {
                    val cacheControl = CacheControl.Builder()
                        .maxAge(5, TimeUnit.MINUTES) // Cache for 5 minutes when online
                        .build()

                    response.newBuilder()
                        .removeHeader("Pragma") // Remove any conflicting headers
                        .removeHeader("Cache-Control")
                        .header("Cache-Control", cacheControl.toString())
                        .build()
                } else {
                    response
                }
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
            .baseUrl("https://api.github.com/")
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

