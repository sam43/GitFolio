package io.sam43.gitfolio.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.sam43.gitfolio.domain.repository.UserRepository
import io.sam43.gitfolio.domain.usecases.FetchUserUseCase
import io.sam43.gitfolio.domain.usecases.GetUserDetailsUseCase
import io.sam43.gitfolio.domain.usecases.GetUserRepositoriesUseCase
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DomainModule {
    @Provides
    @Singleton
    fun provideFetchUserUseCase(repository: UserRepository): FetchUserUseCase = FetchUserUseCase(repository)

    @Provides
    @Singleton
    fun provideGetUserDetailsUseCase(repository: UserRepository): GetUserDetailsUseCase = GetUserDetailsUseCase(repository)

    @Provides
    @Singleton
    fun provideGetUserRepositoriesUseCase(repository: UserRepository): GetUserRepositoriesUseCase = GetUserRepositoriesUseCase(repository)
}