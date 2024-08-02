package com.ramzmania.tubefy.core.yotubewebscrapper.scrapping

import android.webkit.WebView
import android.webkit.WebViewClient
import com.ramzmania.tubefy.core.YoutubeCoreConstant
import com.ramzmania.tubefy.data.dto.youtubestripper.ApiResponse
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class YoutubeJsonScrapping constructor(val webView: WebView) {
 private val sharedJsonContentPrivate= MutableSharedFlow<ApiResponse?>()
    val sharedJsonContent:SharedFlow<ApiResponse?>  = sharedJsonContentPrivate

    fun fetchPageSource(url: String) {
//        val webView=WebView(context)
        CoroutineScope(Dispatchers.IO).launch {
            try {

                withContext(Dispatchers.Main) {
                    webView.settings.javaScriptEnabled = true

                    webView.webViewClient = object : WebViewClient() {
                        override fun onPageFinished(view: WebView?, url: String?) {
                            super.onPageFinished(view, url)
//                            CoroutineScope(Dispatchers.IO).launch {
                                getHtmlContent(webView)
//                            }
                        }
                    }
                    webView.loadUrl(url)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private  fun getHtmlContent(webView: WebView) {

            webView.evaluateJavascript("(function() { return document.documentElement.outerHTML; })();") { html ->
                val cleanHtml = html.replace("\\u003C", "<").replace("\\u003E", ">")
                var result = decodeHexString(
                    extractDataBetween(
                        cleanHtml,
                        YoutubeCoreConstant.START_TAG,
                        YoutubeCoreConstant.END_TAG
                    ) + ""
                )
                result = result.replaceFirst("= '{", "{").replaceFirst("';", "")
                    .replace("\\\\\\\\\"", "")
//                webViewModel.setHtmlContent(result)
                CoroutineScope(Dispatchers.IO).launch {

                        passDatas(parseJson(result))

                }
        }
    }




    fun decodeHexString(input: String): String {
        // Regular expression to find \xHH patterns
        /*val hexPattern = Regex("""\\\\x([0-9A-Fa-f]{2})""")
//            val hexPattern = Regex("""\\x([0-9A-Fa-f]{2})""")
        // Replace each match with the corresponding character
        return hexPattern.replace(input) { matchResult ->
            val hexValue = matchResult.groupValues[1]
            val charValue = Hex.decodeHex(hexValue)[0]
            charValue.toChar().toString()
        }*/
        return input.replace("\\\\x22", "\"")
            .replace("\\\\x20", " ")
            .replace("\\\\x21", "!")
            .replace("\\\\x26", "&")
            .replace("\\\\x28", "(")
            .replace("\\\\x29", ")")
            .replace("\\\\x2c", ",")
            .replace("\\\\x3a", ":")
            .replace("\\\\x7b", "{")
            .replace("\\\\x7d", "}")
            .replace("\\\\x5b", "[")
            .replace("\\\\x5d", "]")

    }

    fun extractDataBetween(input: String, start: String, end: String): String? {
        val startIndex = input.indexOf(start)
        val endIndex = input.indexOf(end, startIndex + start.length)

        return if (startIndex != -1 && endIndex != -1) {
            input.substring(startIndex + start.length, endIndex).trim()
        } else {
            null
        }
    }
    private suspend fun passDatas(data:ApiResponse?) {
        sharedJsonContentPrivate.emit(data)
    }

    fun parseJson(jsonString: String): ApiResponse? {
        val moshi=Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
        val jsonAdapter = moshi.adapter(ApiResponse::class.java)
        return jsonAdapter.fromJson(jsonString)
    }
}