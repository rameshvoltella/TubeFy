package com.ramzmania.tubefy.di


import android.content.Context
import com.ramzmania.tubefy.data.Network
import com.ramzmania.tubefy.data.NetworkConnectivity
import com.ramzmania.tubefy.data.database.DatabaseRepository
import com.ramzmania.tubefy.data.database.DatabaseRepositorySource
import com.ramzmania.tubefy.data.local.LocalRepository
import com.ramzmania.tubefy.data.local.LocalRepositorySource
import com.ramzmania.tubefy.data.remote.RemoteRepository
import com.ramzmania.tubefy.data.remote.RemoteRepositorySource
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

// Tells Dagger this is a Dagger module
@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {

    @Binds
    @Singleton
    abstract fun provideLocalRepository(dataRepository: LocalRepository): LocalRepositorySource



    @Binds
    @Singleton
    abstract fun provideRemoteRepository(remoteRepository: RemoteRepository): RemoteRepositorySource

    @Binds
    @Singleton
    abstract fun provideDatabaseRepository(remoteRepository: DatabaseRepository): DatabaseRepositorySource

}