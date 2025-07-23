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
import io.sam43.retrofitcache.RetrofitCacheManager
import io.sam43.retrofitcache.cache.LruCacheManager
import io.sam43.retrofitcache.interceptor.CacheInterceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataModule {


    @Provides
    @Singleton
    fun provideLruCacheManager(@ApplicationContext context: Context): LruCacheManager {
        return LruCacheManager(context, maxSize = 500, defaultMaxAge = TimeUnit.HOURS.toMillis(1))
    }

    @Provides
    @Singleton
    fun provideCacheInterceptor(lruCacheManager: LruCacheManager): CacheInterceptor {
        return CacheInterceptor(lruCacheManager)
    }

    @Provides
    @Singleton
    fun provideRetrofitCacheManager(interceptor: CacheInterceptor, cacheManager: LruCacheManager): RetrofitCacheManager =
        RetrofitCacheManager(interceptor,cacheManager)
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
        retrofitCacheManager: RetrofitCacheManager,
        loggingInterceptor: HttpLoggingInterceptor
    ): OkHttpClient {
        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .addHeader("Authorization", BuildConfig.GITHUB_API_TOKEN)
                    .addHeader("X-GitHub-Api-Version", "2022-11-28")
                    .build()
                chain.proceed(request)
            }
            .addInterceptor(retrofitCacheManager.getInterceptor())
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)

        if (BuildConfig.DEBUG) {
            okHttpClient.addInterceptor(loggingInterceptor)
        }

        return okHttpClient.build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://api.github.com")
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

