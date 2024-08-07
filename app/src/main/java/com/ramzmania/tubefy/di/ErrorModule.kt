package com.ramzmania.tubefy.di



import com.ramzmania.tubefy.errors.ErrorManager
import com.ramzmania.tubefy.errors.ErrorUseCase
import com.ramzmania.tubefy.errors.mapper.ErrorMapper
import com.ramzmania.tubefy.errors.mapper.ErrorMapperSource
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

// with @Module we Telling Dagger that, this is a Dagger module
@Module
@InstallIn(SingletonComponent::class)
abstract class ErrorModule {

    @Binds
    @Singleton
    abstract fun provideErrorFactoryImpl(errorManager: ErrorManager): ErrorUseCase

    @Binds
    @Singleton
    abstract fun provideErrorMapper(errorMapper: ErrorMapper): ErrorMapperSource
}