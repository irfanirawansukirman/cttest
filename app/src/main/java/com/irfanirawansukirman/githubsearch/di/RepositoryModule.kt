package com.irfanirawansukirman.githubsearch.di

import com.irfanirawansukirman.githubsearch.data.remote.service.GithubService
import com.irfanirawansukirman.githubsearch.data.repository.main.MainRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.android.scopes.ActivityRetainedScoped

@Module
@InstallIn(ActivityRetainedComponent::class)
object RepositoryModule {

    @Provides
    @ActivityRetainedScoped
    fun provideMainRepositoryImpl(
        githubService: GithubService
    ): MainRepositoryImpl = MainRepositoryImpl(githubService)
}