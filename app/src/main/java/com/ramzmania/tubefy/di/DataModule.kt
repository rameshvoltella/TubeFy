package com.ramzmania.tubefy.di


import com.ramzmania.tubefy.data.local.LocalRepository
import com.ramzmania.tubefy.data.local.LocalRepositorySource
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

// Tells Dagger this is a Dagger module
@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {

    @Binds
    @Singleton
    abstract fun provideDataRepository(dataRepository: LocalRepository): LocalRepositorySource

}