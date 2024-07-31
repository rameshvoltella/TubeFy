package com.ramzmania.tubefy.di

import android.content.Context
import android.webkit.WebView
import com.ramzmania.tubefy.data.ContextModule
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class AppModule {

    @Provides
    @Singleton
    fun provideContext(@ApplicationContext context: Context):ContextModule{
       return ContextModule(context)
    }

//    @Binds
//    @Singleton
//    fun provideWebView(@ApplicationContext context: Context):WebView{
//        return WebView(context)
//    }

}