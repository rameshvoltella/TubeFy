package com.ramzmania.tubefy.di

import android.content.Context
import com.ramzmania.tubefy.data.ContextModule
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class AppModule {

    @Binds
    @Singleton
    fun provideContext(@ApplicationContext context: Context):ContextModule{
       return ContextModule(context)
    }

}