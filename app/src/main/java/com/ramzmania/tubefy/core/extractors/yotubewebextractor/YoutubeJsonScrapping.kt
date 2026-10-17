package com.ramzmania.tubefy.core.extractors.yotubewebextractor

import android.util.Log
import com.ramzmania.tubefy.core.YoutubeCoreConstant
import com.ramzmania.tubefy.data.dto.youtubemusic.category.YtMusicCategoryBase
import com.ramzmania.tubefy.data.dto.youtubemusic.playlist.YoutubeMusicPlayListContent
import com.ramzmania.tubefy.data.dto.youtubestripper.ApiResponse
import com.ramzmania.tubefy.data.dto.youtubestripper.MusicHomeResponse2
import com.ramzmania.tubefy.utils.parseJson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

/**
 * Scrapes the JSON that YouTube / YouTube Music embed in their server-rendered HTML.
 *
 * The page is fetched directly with OkHttp (no WebView), so concurrent requests can't cancel
 * each other and the result doesn't depend on WebView page-load callbacks.
 */
class YoutubeJsonScrapping(
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .build()
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

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

    fun fetchPageSource(url: String, type: YoutubeScrapType) {
        scope.launch {
            val json = try {
                val html = downloadPage(url)
                if (type == YoutubeScrapType.YOUTUBE_HOME) extractYtInitialData(html)
                else extractYtMusicInitialData(html)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to scrape $url", e)
                null
            }

            when (type) {
                YoutubeScrapType.YOUTUBE_HOME ->
                    json?.let { safeParse<ApiResponse>(it) }?.let { sharedJsonContentPrivate.emit(it) }

                YoutubeScrapType.YOUTUBE_MUSIC ->
                    json?.let { safeParse<MusicHomeResponse2>(it) }?.let { sharedJsonMusicHomeContentPrivate.emit(it) }

                YoutubeScrapType.YOUTUBE_PLAYLIST ->
                    json?.let { safeParse<YoutubeMusicPlayListContent>(it) }?.let { sharedJsonMusicHomePlayListContentPrivate.emit(it) }

                // Always emit for categories, so the UI can show an error instead of spinning forever
                YoutubeScrapType.YOUTUBE_MUSIC_CATEGORY ->
                    sharedJsonMusicCategoryPlayListContentPrivate.emit(json?.let { safeParse<YtMusicCategoryBase>(it) })
            }
        }
    }

    private fun downloadPage(url: String): String {
        val request = Request.Builder()
            .url(url)
            .header("User-Agent", USER_AGENT)
            .header("Accept-Language", "en-US,en;q=0.9")
            .build()
        client.newCall(request).execute().use { response ->
            check(response.isSuccessful) { "HTTP ${response.code}" }
            return response.body?.string().orEmpty()
        }
    }

    private inline fun <reified T> safeParse(json: String): T? = try {
        parseJson<T>(json)
    } catch (e: Exception) {
        Log.e(TAG, "Failed to parse ${T::class.java.simpleName}", e)
        null
    }

    companion object {
        private const val TAG = "YoutubeJsonScrapping"

        // A desktop Chrome UA: music.youtube.com serves its full page (with initial data) to it
        private const val USER_AGENT =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/130.0.0.0 Safari/537.36"

        /**
         * YouTube Music pages embed their data as
         * `initialData.push({path: '\/browse', params: ..., data: '<escaped JSON>'});`
         * A page can push several entries (e.g. the guide); this returns the decoded JSON of the
         * entry for [path], or null if the page doesn't contain it.
         */
        fun extractYtMusicInitialData(html: String, path: String = "/browse"): String? {
            val pathMarker = "path: '" + path.replace("/", "\\/") + "'"
            var pushIndex = html.indexOf("initialData.push(")
            while (pushIndex != -1) {
                val nextPush = html.indexOf("initialData.push(", pushIndex + 1)
                val entryEnd = if (nextPush == -1) html.length else nextPush
                val pathIndex = html.indexOf(pathMarker, pushIndex)
                if (pathIndex != -1 && pathIndex < entryEnd) {
                    val dataIndex = html.indexOf("data: '", pathIndex)
                    if (dataIndex == -1 || dataIndex > entryEnd) return null
                    return readJsStringLiteral(html, dataIndex + "data: ".length)
                        ?.takeIf { it.startsWith("{") }
                }
                pushIndex = nextPush
            }
            return null
        }

        /** www.youtube.com embeds `var ytInitialData = {...};` or `var ytInitialData = '<escaped JSON>';` */
        fun extractYtInitialData(html: String): String? {
            val start = html.indexOf(YoutubeCoreConstant.START_TAG)
            if (start == -1) return null
            val valueStart = html.indexOf('=', start) + 1
            var i = valueStart
            while (i < html.length && html[i].isWhitespace()) i++
            if (i >= html.length) return null
            return when (html[i]) {
                '\'', '"' -> readJsStringLiteral(html, i)
                '{' -> html.indexOf(";</script>", i).takeIf { it != -1 }?.let { html.substring(i, it) }
                else -> null
            }
        }

        /** Reads the JavaScript string literal whose opening quote is at [quoteIndex] and unescapes it. */
        fun readJsStringLiteral(source: String, quoteIndex: Int): String? {
            val quote = source.getOrNull(quoteIndex)?.takeIf { it == '\'' || it == '"' } ?: return null
            val out = StringBuilder()
            var i = quoteIndex + 1
            while (i < source.length) {
                val c = source[i]
                when {
                    c == quote -> return out.toString()
                    c == '\\' && i + 1 < source.length -> {
                        val e = source[i + 1]
                        i += 2
                        when (e) {
                            'x' -> {
                                out.append(source.substring(i, i + 2).toInt(16).toChar())
                                i += 2
                            }
                            'u' -> {
                                out.append(source.substring(i, i + 4).toInt(16).toChar())
                                i += 4
                            }
                            'n' -> out.append('\n')
                            'r' -> out.append('\r')
                            't' -> out.append('\t')
                            'b' -> out.append('\b')
                            'f' -> out.append('\u000C')
                            'v' -> out.append('\u000B')
                            '0' -> out.append('\u0000')
                            else -> out.append(e) // \\ \' \" \/ and any other escaped char
                        }
                        continue
                    }
                    else -> out.append(c)
                }
                i++
            }
            return null // unterminated literal
        }
    }
}
