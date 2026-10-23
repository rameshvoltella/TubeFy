package com.ramzmania.tubefy.core.lyrics

import android.content.Context
import com.squareup.moshi.Moshi
import java.io.File

/**
 * Details and lyrics (original and English letters) of songs downloaded inside the app, kept in the
 * app's private storage so the Lyrics tab works offline. One small JSON file per song.
 */
object OfflineLyrics {

    private val json = Moshi.Builder().build().adapter(Any::class.java)

    private fun file(context: Context, videoId: String) =
        File(File(context.filesDir, "lyrics").apply { mkdirs() }, "${videoId.replace(Regex("[^A-Za-z0-9_-]"), "")}.json")

    fun save(context: Context, videoId: String, info: SongInfo) {
        val target = file(context, videoId)
        val temp = File(target.parentFile, "${target.name}.tmp")
        temp.writeText(json.toJson(toMap(info)))
        temp.renameTo(target)
    }

    fun load(context: Context, videoId: String): SongInfo? = runCatching {
        val target = file(context, videoId)
        if (!target.exists()) return null
        fromMap(json.fromJson(target.readText()) as Map<*, *>)
    }.getOrNull()

    fun delete(context: Context, videoId: String) {
        file(context, videoId).delete()
    }

    internal fun toMap(info: SongInfo): Map<String, Any?> = mapOf(
        "details" to info.details.let {
            mapOf(
                "title" to it.title, "singers" to it.singers, "movie" to it.movie,
                "lyricist" to it.lyricist, "composer" to it.composer, "year" to it.year
            )
        },
        "lyrics" to info.lyrics?.let(::lyricsToMap),
        "englishLetters" to info.englishLetters?.let(::lyricsToMap)
    )

    internal fun fromMap(map: Map<*, *>): SongInfo {
        val details = map["details"] as? Map<*, *> ?: emptyMap<String, Any>()
        fun text(key: String) = details[key] as? String ?: ""
        return SongInfo(
            details = SongDetails(text("title"), text("singers"), text("movie"), text("lyricist"), text("composer"), text("year")),
            lyrics = (map["lyrics"] as? Map<*, *>)?.let(::lyricsFromMap),
            englishLetters = (map["englishLetters"] as? Map<*, *>)?.let(::lyricsFromMap)
        )
    }

    private fun lyricsToMap(lyrics: Lyrics): Map<String, Any?> = mapOf(
        "synced" to lyrics.synced,
        "source" to lyrics.source,
        "lines" to lyrics.lines.map { mapOf("time" to it.timeMs, "text" to it.text) }
    )

    private fun lyricsFromMap(map: Map<*, *>): Lyrics = Lyrics(
        lines = (map["lines"] as? List<*>).orEmpty().filterIsInstance<Map<*, *>>().map { line ->
            LyricLine((line["time"] as? Number)?.toLong(), line["text"] as? String ?: "")
        },
        synced = map["synced"] as? Boolean ?: false,
        source = map["source"] as? String ?: ""
    )
}
