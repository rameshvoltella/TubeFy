package com.ramzmania.tubefy.di

import android.content.Context
import android.webkit.WebView
import com.ramzmania.tubefy.core.yotubesearch.scrapping.YoutubeJsonScrapping
import com.ramzmania.tubefy.data.ContextModule
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
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

//    @Provides
//    @Singleton
//    fun provideWebView(@ApplicationContext context: Context):WebView{
//        return WebView(context)
//    }

    @Provides
    @Singleton
    fun provideYoutubeJsonScrapping(@ApplicationContext context: Context):YoutubeJsonScrapping{
        return YoutubeJsonScrapping(WebView(context))
    }
//    @Provides
//    @Singleton
//    fun provideMoshi(): Moshi {
//        return Moshi.Builder()
//            .add(KotlinJsonAdapterFactory())
//            .build()
//    }

//    @Binds
//    @Singleton
//    fun provideWebView(@ApplicationContext context: Context):WebView{
//        return WebView(context)
//    }

}