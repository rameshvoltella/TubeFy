package com.ramzmania.tubefy.data.remote

import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/** A podcast show; [playlistId] is the YouTube playlist holding its episodes. */
data class PodcastShow(
    val playlistId: String,
    val title: String,
    val author: String,
    val thumbnail: String
)

/**
 * Finds podcast shows with YouTube Music's search, restricted to podcasts (the same "Podcasts"
 * filter as in the YouTube Music app). Used for the per-language lists on the Podcasts page.
 */
@Singleton
class PodcastSearch @Inject constructor() {

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .build()

    /** Podcast shows for [query]; empty if none. Throws on network errors. */
    suspend fun search(query: String): List<PodcastShow> = withContext(Dispatchers.IO) {
        val body = """{"context":{"client":{"clientName":"WEB_REMIX","clientVersion":"$CLIENT_VERSION","hl":"en","gl":"IN"}},""" +
            """"query":${quote(query)},"params":"$PODCASTS_FILTER"}"""
        val request = Request.Builder()
            .url("https://music.youtube.com/youtubei/v1/search?prettyPrint=false")
            .header("User-Agent", "Mozilla/5.0")
            .post(body.toRequestBody("application/json".toMediaType()))
            .build()
        client.newCall(request).execute().use { response ->
            check(response.isSuccessful) { "HTTP ${response.code}" }
            parseShows(response.body?.string().orEmpty())
        }
    }

    companion object {
        private const val CLIENT_VERSION = "1.20240729.01.00"

        /** YouTube Music's search filter code for "Podcasts". */
        private const val PODCASTS_FILTER = "EgWKAQJQAWoMEA4QChADEAQQCRAF"

        private val mapAdapter = Moshi.Builder().build().adapter<Map<String, Any?>>(
            Types.newParameterizedType(Map::class.java, String::class.java, Any::class.java)
        )

        /** Extracts podcast shows (results that open an "MPSP<playlistId>" page) from a search response. */
        fun parseShows(json: String): List<PodcastShow> {
            val root = mapAdapter.fromJson(json) ?: return emptyList()
            val shows = mutableListOf<PodcastShow>()
            collectItems(root) { item ->
                val browseId = item.path("navigationEndpoint", "browseEndpoint", "browseId") as? String
                if (browseId == null || !browseId.startsWith("MPSP")) return@collectItems
                val columns = (item["flexColumns"] as? List<*>).orEmpty().map { column ->
                    ((column as? Map<*, *>)?.path("musicResponsiveListItemFlexColumnRenderer", "text", "runs") as? List<*>)
                        .orEmpty()
                        .mapNotNull { (it as? Map<*, *>)?.get("text") as? String }
                }
                val title = columns.getOrNull(0)?.joinToString("")?.trim().orEmpty()
                val author = columns.drop(1).flatten()
                    .filter { it.isNotBlank() && it.trim() != "•" && !it.equals("Podcast", ignoreCase = true) }
                    .joinToString(" ")
                    .trim()
                val thumbnail = ((item.path("thumbnail", "musicThumbnailRenderer", "thumbnail", "thumbnails") as? List<*>)
                    ?.lastOrNull() as? Map<*, *>)?.get("url") as? String
                if (title.isNotEmpty()) {
                    shows += PodcastShow(browseId.removePrefix("MPSP"), title, author, thumbnail.orEmpty())
                }
            }
            return shows.distinctBy { it.playlistId }
        }

        private fun collectItems(node: Any?, onItem: (Map<*, *>) -> Unit) {
            when (node) {
                is Map<*, *> -> {
                    (node["musicResponsiveListItemRenderer"] as? Map<*, *>)?.let(onItem)
                    node.values.forEach { collectItems(it, onItem) }
                }
                is List<*> -> node.forEach { collectItems(it, onItem) }
            }
        }

        private fun Map<*, *>.path(vararg keys: String): Any? {
            var current: Any? = this
            for (key in keys) current = (current as? Map<*, *>)?.get(key) ?: return null
            return current
        }

        private fun quote(text: String) =
            "\"" + text.replace("\\", "\\\\").replace("\"", "\\\"") + "\""
    }
}
