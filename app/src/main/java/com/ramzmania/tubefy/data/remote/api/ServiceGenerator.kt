package com.ramzmania.tubefy.data.remote.api

import android.os.Build
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.ramzmania.tubefy.core.YoutubeCoreConstant.YOUTUBE_V3API_KEY
import com.ramzmania.tubefy.core.YoutubeCoreConstant.YOUTUBE_V3_BASE_URL
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton
private const val timeoutRead = 30   //In seconds
private const val contentType = "Content-Type"
private const val contentTypeValue = "application/json"
private const val timeoutConnect = 30   //In seconds


@Singleton
class ServiceGenerator @Inject constructor(){

    private val okHttpBuilder: OkHttpClient.Builder = OkHttpClient.Builder()
    private var retrofit: Retrofit


    private var headerInterceptor = Interceptor { chain ->
        val original = chain.request()

        val request = original.newBuilder()
//                .header(contentType, contentTypeValue)
            //   .method(original.method, original.body)
            .build()

        chain.proceed(request)
    }

    private val logger: HttpLoggingInterceptor
        get() {
            val loggingInterceptor = HttpLoggingInterceptor()
                loggingInterceptor.apply { level = HttpLoggingInterceptor.Level.BODY }

            return loggingInterceptor
        }

    init {
//        okHttpBuilder.addInterceptor(headerInterceptor)
        okHttpBuilder.addNetworkInterceptor(UserAgentInterceptor())
        okHttpBuilder.addInterceptor(logger)
        okHttpBuilder.addInterceptor { chain ->
            val url = chain
                .request()
                .url
                .newBuilder()
                .addQueryParameter("key", YOUTUBE_V3API_KEY)
                .build()
            chain.proceed(chain.request().newBuilder().url(url).build())
        }
        okHttpBuilder.connectTimeout(timeoutConnect.toLong(), TimeUnit.SECONDS)
        okHttpBuilder.readTimeout(timeoutRead.toLong(), TimeUnit.SECONDS)
        val client = okHttpBuilder.build()
        var gson: Gson? = GsonBuilder()
            .setLenient()
            .create()
        retrofit = Retrofit.Builder()
            .baseUrl(YOUTUBE_V3_BASE_URL).client(client)
            .addConverterFactory(MoshiConverterFactory.create(getMoshi()).asLenient())
            .build()

    }

    fun <S> createService(serviceClass: Class<S>): S {
        return retrofit.create(serviceClass)
    }



    private fun getMoshi(): Moshi {
        return    Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
    }


    fun getDefaultUserAgent(): String? {
        return try {
            val result = StringBuilder(64)
            result.append("Dalvik/")
            result.append(System.getProperty("java.vm.version")) // such as 1.1.0
            result.append(" (Linux; U; Android ")
            val version = Build.VERSION.RELEASE // "1.0" or "3.4b5"
            result.append(if (version.length > 0) version else "1.0")

            // add the model for the release build
            if ("REL" == Build.VERSION.CODENAME) {
                val model = Build.MODEL
                if (model.length > 0) {
                    result.append("; ")
                    result.append(model)
                }
            }
            val id = Build.ID // "MASTER" or "M4-rc20"
            if (id.length > 0) {
                result.append(" Build/")
                result.append(id)
            }
            result.append(")")
            result.toString()
        } catch (e: Exception) {
            String.format(
                Locale.US,
                "%s/%s (Android %s; %s; %s %s; %s)",
                Build.VERSION.CODENAME,
                Build.ID,
                Build.VERSION.RELEASE,
                Build.MODEL,
                Build.BRAND,
                Build.DEVICE,
                Locale.getDefault().language
            )
        }
    }





}