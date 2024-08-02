package com.ramzmania.tubefy.di

import android.content.Context
import android.webkit.WebView
import com.ramzmania.tubefy.core.newpipeextractor.PipeDownloader
import com.ramzmania.tubefy.core.yotubewebscrapper.scrapping.YoutubeJsonScrapping
import com.ramzmania.tubefy.data.ContextModule
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit
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

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient = OkHttpClient.Builder()
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    @Provides
    @Singleton
    fun provideDownloader(client: OkHttpClient): PipeDownloader
            = PipeDownloader(client)

}