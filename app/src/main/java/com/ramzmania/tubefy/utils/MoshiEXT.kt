package com.ramzmania.tubefy.utils

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

inline fun <reified T> parseJson(jsonString: String): T? {
    val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()
    val jsonAdapter = moshi.adapter(T::class.java).lenient()
    return jsonAdapter.fromJson(jsonString)
}