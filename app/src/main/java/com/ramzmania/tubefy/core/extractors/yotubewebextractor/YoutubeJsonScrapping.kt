package com.ramzmania.tubefy.core.extractors.yotubewebextractor

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.util.Log
import android.webkit.WebView
import android.webkit.WebViewClient
import com.ramzmania.tubefy.core.YoutubeCoreConstant
import com.ramzmania.tubefy.data.dto.youtubemusic.category.YtMusicCategoryBase
import com.ramzmania.tubefy.data.dto.youtubemusic.category.YtMusicCategoryContent
import com.ramzmania.tubefy.data.dto.youtubemusic.playlist.YoutubeMusicPlayListContent
import com.ramzmania.tubefy.data.dto.youtubestripper.ApiResponse
import com.ramzmania.tubefy.data.dto.youtubestripper.MusicHomeResponse2
import com.ramzmania.tubefy.utils.parseJson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class YoutubeJsonScrapping constructor(val webView: WebView, val context: Context) {
    private val sharedJsonContentPrivate = MutableSharedFlow<ApiResponse?>()
    val sharedJsonContent: SharedFlow<ApiResponse?> = sharedJsonContentPrivate
    private val sharedJsonMusicHomeContentPrivate = MutableSharedFlow<MusicHomeResponse2?>()
    val sharedJsonMusicHomeContent: SharedFlow<MusicHomeResponse2?> =
        sharedJsonMusicHomeContentPrivate
    private val sharedJsonMusicHomePlayListContentPrivate =
        MutableSharedFlow<YoutubeMusicPlayListContent?>()
    val sharedJsonMusicHomePlayListContent: SharedFlow<YoutubeMusicPlayListContent?> =
        sharedJsonMusicHomePlayListContentPrivate
    private val sharedJsonMusicCategoryPlayListContentPrivate =
        MutableSharedFlow<YtMusicCategoryBase?>()
    val sharedJsonMusicCategoryPlayListContent: SharedFlow<YtMusicCategoryBase?> =
        sharedJsonMusicCategoryPlayListContentPrivate

    var alreadyEvaluated = false;

    fun fetchPageSource(url: String, type: YoutubeScrapType) {
//        val webView=WebView(context)
//        var url="https://music.youtube.com"
        CoroutineScope(Dispatchers.IO).launch {
            try {
                withContext(Dispatchers.Main) {
                    webView.settings.javaScriptEnabled = true

                    webView.webViewClient = object : WebViewClient() {
                        override fun onPageFinished(view: WebView?, url: String?) {
                            super.onPageFinished(view, url)
//                            CoroutineScope(Dispatchers.IO).launch {
                            if (type == YoutubeScrapType.YOUTUBE_MUSIC) {
                                getMusicHomeHtmlContent(webView)
                            } else if (type == YoutubeScrapType.YOUTUBE_PLAYLIST) {
                                getMusicPlayListHtmlContent(webView)
                            } else if (type == YoutubeScrapType.YOUTUBE_HOME) {
                                getYoutubeContent(webView)
                            } else if (type == YoutubeScrapType.YOUTUBE_MUSIC_CATEGORY) {
                                getMusicCategoryHtmlContent(webView)
                            }
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

    private fun getYoutubeContent(webView: WebView) {
        if (!alreadyEvaluated) {
            alreadyEvaluated = true
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
                    if (result.startsWith("{")&&result.endsWith("}")) {

                        passYtVideoHomeData(parseJson(result))
                    }

                }
            }
        } else {
            alreadyEvaluated = false
        }

    }

    private fun getMusicSearchHtmlContent(webView: WebView) {
        if (!alreadyEvaluated) {
            alreadyEvaluated = true
            webView.evaluateJavascript("(function() { return document.documentElement.outerHTML; })();") { html ->
                val cleanHtml = html.replace("\\u003C", "<").replace("\\u003E", ">")
                var result = decodeHexString(
                    extractDataBetween(
                        cleanHtml,
                        "initialData.push({path: '\\\\/search', params",
                        "'});ytcfg.set"
                    ) + ""
                )
                result = getDataSubstring(result)
                result = result.replace("\\\\\\\\\"", "")
                val clipboard =
                    context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("label", result)
                clipboard.setPrimaryClip(clip)
//                getDataSubstring
                result = result.replaceFirst("= '{", "{").replaceFirst("';", "")
                    .replace("\\\\\\\\\"", "")
////                webViewModel.setHtmlContent(result)
//                CoroutineScope(Dispatchers.IO).launch {
//
//                    passDatas(parseJson(result))
//
//                }
            }
        } else {
            alreadyEvaluated = false
        }

    }

    private fun getMusicHomeHtmlContent(webView: WebView) {
        if (!alreadyEvaluated) {
            alreadyEvaluated = true
            webView.evaluateJavascript("(function() { return document.documentElement.outerHTML; })();") { html ->
                val cleanHtml = html.replace("\\u003C", "<").replace("\\u003E", ">")
                var result = decodeHexString(
                    extractDataBetween(
                        cleanHtml,
                        "initialData.push({path: '\\\\/browse', params",
                        "'});ytcfg.set"
                    ) + ""
                )
                result = getDataSubstring(result)
                result = result.replace("\\\\\\\\\"", "")
//                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
//                val clip = ClipData.newPlainText("label", result)
//                clipboard.setPrimaryClip(clip)
////                getDataSubstring
//                result = result.replaceFirst("= '{", "{").replaceFirst("';", "")
//                    .replace("\\\\\\\\\"", "")
////                webViewModel.setHtmlContent(result)
                CoroutineScope(Dispatchers.IO).launch {
                    if (result.startsWith("{")&&result.endsWith("}")) {

                        passYtMusicHomeData(parseJson(result))
                    }

                }
            }
        } else {
            alreadyEvaluated = false
        }

    }

    private fun getMusicPlayListHtmlContent(webView: WebView) {
        try {
            if (!alreadyEvaluated) {
                alreadyEvaluated = true
                webView.evaluateJavascript("(function() { return document.documentElement.outerHTML; })();") { html ->
                    val cleanHtml = html.replace("\\u003C", "<").replace("\\u003E", ">")
                    var result = decodeHexString(
                        extractDataBetween(
                            cleanHtml,
                            "initialData.push({path: '\\\\/browse', params",
                            "'});ytcfg.set"
                        ) + ""
                    )
                    result = getDataSubstring(result)
                    result = result.replace("\\\\\\\\\"", "")
//                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
//                val clip = ClipData.newPlainText("label", result)
//                clipboard.setPrimaryClip(clip)
////                getDataSubstring
//                result = result.replaceFirst("= '{", "{").replaceFirst("';", "")
//                    .replace("\\\\\\\\\"", "")
////                webViewModel.setHtmlContent(result)
                    CoroutineScope(Dispatchers.IO).launch {
                        if (result.startsWith("{")&&result.endsWith("}")) {
                            passYTMusicPlayListData(parseJson(result))
                        }

                    }
                }
            } else {
                alreadyEvaluated = false
            }
        }catch (ex:Exception)
        {

        }
    }

    /* private  fun getMusicPlayListHtmlContent(webView: WebView) {
         if (!alreadyEvaluated) {
             alreadyEvaluated = true
             webView.evaluateJavascript("(function() { return document.documentElement.outerHTML; })();") { html ->
                 val cleanHtml = html.replace("\\u003C", "<").replace("\\u003E", ">")
                 var result = decodeHexString(
                     extractDataBetween(
                         cleanHtml,
                         "initialData.push({path: '\\\\/browse', params",
                         "'});ytcfg.set"
                     ) + ""
                 )
                 result=getDataSubstring(result)
                 result=result.replace("\\\\\\\\\"", "")
                 val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                 val clip = ClipData.newPlainText("label", result)
                 clipboard.setPrimaryClip(clip)
 //                getDataSubstring
                 result = result.replaceFirst("= '{", "{").replaceFirst("';", "")
                     .replace("\\\\\\\\\"", "")
 ////                webViewModel.setHtmlContent(result)
 //                CoroutineScope(Dispatchers.IO).launch {
 //
 //                    passDatas(parseJson(result))
 //
 //                }
             }
         } else {
             alreadyEvaluated = false
         }

     }*/


    private fun getMusicCategoryHtmlContent(webView: WebView) {
        try {
            if (!alreadyEvaluated) {
                alreadyEvaluated = true
                webView.evaluateJavascript("(function() { return document.documentElement.outerHTML; })();") { html ->
                    val cleanHtml = html.replace("\\u003C", "<").replace("\\u003E", ">")
                    var result = decodeHexString(
                        extractDataBetween(
                            cleanHtml,
                            "initialData.push({path: '\\\\/browse', params",
                            "'});ytcfg.set"
                        ) + ""
                    )
                    result = getDataSubstring(result)
                    result = result.replace("\\\\\\\\\"", "")

//                getDataSubstring
                    result = result.replaceFirst("= '{", "{").replaceFirst("';", "")
                        .replace("\\\\\\\\\"", "")
                    result = decodeHexString(result)
//                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
//                val clip = ClipData.newPlainText("label", result)
//                clipboard.setPrimaryClip(clip)
                    CoroutineScope(Dispatchers.IO).launch {
                            if (result.startsWith("{")&&result.endsWith("}")) {

                                passYTMusicPlayCategory(parseJson(result))
                            }


                    }
////                webViewModel.setHtmlContent(result)
//                CoroutineScope(Dispatchers.IO).launch {
//
//                    passDatas(parseJson(result))
//
//                }
                }
            } else {
                alreadyEvaluated = false
            }
        } catch (ex: Exception) {

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
            .replace("\\x3d", "=")

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

    private suspend fun passYtVideoHomeData(data: ApiResponse?) {
        sharedJsonContentPrivate.emit(data)
    }

    private suspend fun passYtMusicHomeData(data: MusicHomeResponse2?) {
        sharedJsonMusicHomeContentPrivate.emit(data)
    }

    private suspend fun passYTMusicPlayListData(data: YoutubeMusicPlayListContent?) {
        sharedJsonMusicHomePlayListContentPrivate.emit(data)
    }

    private suspend fun passYTMusicPlayCategory(data: YtMusicCategoryBase?) {
        sharedJsonMusicCategoryPlayListContentPrivate.emit(data)
    }


    /*    inline fun <reified T> parseJsons(jsonString: String): T? {
            val moshi = Moshi.Builder()
                .add(KotlinJsonAdapterFactory())
                .build()
            val jsonAdapter = moshi.adapter(T::class.java)
            return jsonAdapter.fromJson(jsonString)
        }*/
    fun getDataSubstring(jsonString: String): String {
        val key = "data: '"
        val startIndex = jsonString.indexOf(key)
        return if (startIndex != -1) {
            jsonString.substring(startIndex + key.length)
        } else {
            ""
        }
    }
}