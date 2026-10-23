package com.ramzmania.tubefy.probe

import com.ramzmania.tubefy.core.extractors.newpipeextractor.PipeDownloader
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import com.squareup.moshi.Moshi
import org.junit.Assume.assumeTrue
import org.junit.Test
import org.schabi.newpipe.extractor.NewPipe
import org.schabi.newpipe.extractor.ServiceList
import org.schabi.newpipe.extractor.search.SearchInfo
import org.schabi.newpipe.extractor.stream.StreamInfoItem
import java.net.URLEncoder

/** Manual: song details + lyrics coverage; runs only with env TUBEFY_PROBE=true. */
class LyricsProbe {
    private val http = OkHttpClient()
    private val json = "application/json".toMediaType()

    private val any = Moshi.Builder().build().adapter(Any::class.java)

    private fun music(endpoint: String, fields: String): Any? {
        val body = """{"context":{"client":{"clientName":"WEB_REMIX","clientVersion":"1.20240729.01.00","hl":"en","gl":"IN"}},$fields}"""
        val request = Request.Builder()
            .url("https://music.youtube.com/youtubei/v1/$endpoint?prettyPrint=false")
            .header("User-Agent", "Mozilla/5.0").header("Origin", "https://music.youtube.com")
            .post(body.toRequestBody(json)).build()
        return http.newCall(request).execute().use { any.fromJson(it.body!!.string()) }
    }

    /** Every value of [key] anywhere in the tree. */
    private fun find(node: Any?, key: String, out: MutableList<Any?> = mutableListOf()): MutableList<Any?> {
        when (node) {
            is Map<*, *> -> node.forEach { (k, v) -> if (k == key) out += v; find(v, key, out) }
            is List<*> -> node.forEach { find(it, key, out) }
        }
        return out
    }

    private fun runs(obj: Any?): String =
        ((obj as? Map<*, *>)?.get("runs") as? List<*>)?.joinToString("") { ((it as? Map<*, *>)?.get("text") as? String).orEmpty() }.orEmpty()

    @Test
    fun probe() {
        assumeTrue(System.getenv("TUBEFY_PROBE") == "true")
        NewPipe.init(PipeDownloader(http))
        val yt = ServiceList.YouTube
        for (q in listOf("Malare Premam", "Jimikki Kammal", "Kannaana Kanney Viswasam", "Kesariya Brahmastra", "Tum Hi Ho Aashiqui 2")) {
            val song = SearchInfo.getInfo(yt, yt.searchQHFactory.fromQuery(q, listOf("music_songs"), "")).relatedItems.firstOrNull() as? StreamInfoItem
            if (song == null) { println("SONG '$q': not found"); continue }
            val id = Regex("v=([A-Za-z0-9_-]{11})").find(song.url)!!.groupValues[1]
            val next = music("next", "\"videoId\":\"$id\",\"isAudioOnly\":true")
            val panel = find(next, "playlistPanelVideoRenderer").firstOrNull() as? Map<*, *>
            val byline = runs(panel?.get("longBylineText"))
            val lyricsBrowse = find(next, "browseId").map { it.toString() }.firstOrNull { it.startsWith("MPLY") }
            var lyrics = "-"; var source = ""
            if (lyricsBrowse != null) {
                val page = music("browse", "\"browseId\":\"$lyricsBrowse\"")
                val shelf = find(page, "musicDescriptionShelfRenderer").firstOrNull() as? Map<*, *>
                lyrics = runs(shelf?.get("description")).ifBlank { "(empty)" }
                source = runs(shelf?.get("footer"))
            }
            val lrc = http.newCall(Request.Builder().url("https://lrclib.net/api/search?q=" + URLEncoder.encode("${song.name} ${song.uploaderName}", "UTF-8"))
                .header("User-Agent", "TubeFy probe").build()).execute().use { any.fromJson(it.body!!.string()) as? List<*> }.orEmpty()
            val lrcFirst = lrc.firstOrNull() as? Map<*, *>
            println("SONG '$q' -> '${song.name}' by ${song.uploaderName} [$id]")
            println("   BYLINE: $byline")
            println("   YTM LYRICS: ${if (lyricsBrowse == null) "no lyrics tab" else lyrics.take(90).replace("\n", " / ")}  $source")
            println("   LRCLIB: ${lrc.size} hits; first='${lrcFirst?.get("trackName")}' by '${lrcFirst?.get("artistName")}' synced=${!(lrcFirst?.get("syncedLyrics") as? String).isNullOrBlank()} text='${(lrcFirst?.get("plainLyrics") as? String)?.take(60)?.replace("\n"," / ")}'")
        }
    }
}
