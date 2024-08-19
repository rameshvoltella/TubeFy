package com.ramzmania.tubefy.utils

import com.ramzmania.tubefy.data.ContextModule
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AssetJsonReader  @Inject constructor(private val contextModule: ContextModule)  {
    inline fun <reified T> loadModelFromAsset(fileName: String): T? {
        return try {
            val json = loadJsonFromAsset(fileName)
            json?.let { parseJson(it) }
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

     fun loadJsonFromAsset(fileName: String): String? {
        return try {
            val inputStream = contextModule.context.assets.open(fileName)
            val size = inputStream.available()
            val buffer = ByteArray(size)
            inputStream.read(buffer)
            inputStream.close()
            String(buffer, Charsets.UTF_8)
        } catch (ex: IOException) {
            ex.printStackTrace()
            null
        }
    }
}