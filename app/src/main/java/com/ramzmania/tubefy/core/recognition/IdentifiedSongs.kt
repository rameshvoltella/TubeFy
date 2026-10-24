package com.ramzmania.tubefy.core.recognition

import android.content.Context
import com.squareup.moshi.Moshi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File

/** A song recognised with Identify song, for Library → Shazam. */
data class IdentifiedSong(
    val title: String,
    val artist: String,
    val album: String,
    val year: String,
    val coverUrl: String?,
    /** The YouTube Music match used for Play, if one was found. */
    val videoId: String?,
    val identifiedAt: Long
) {
    val key get() = "${title.lowercase()}|${artist.lowercase()}"
}

/** Every identified song, newest first (identifying it again moves it to the top). Private app storage. */
object IdentifiedSongs {

    private const val FILE = "identified_songs.json"
    private const val MAX = 300
    private val json = Moshi.Builder().build().adapter(Any::class.java)
    private val _songs = MutableStateFlow<List<IdentifiedSong>?>(null)

    fun songs(context: Context): StateFlow<List<IdentifiedSong>?> {
        if (_songs.value == null) synchronized(this) { if (_songs.value == null) _songs.value = load(context) }
        return _songs.asStateFlow()
    }

    @Synchronized
    fun record(context: Context, song: IdentifiedSong) =
        save(context, (listOf(song) + current(context).filterNot { it.key == song.key }).take(MAX))

    @Synchronized
    fun remove(context: Context, song: IdentifiedSong) = save(context, current(context).filterNot { it.key == song.key })

    @Synchronized
    fun clear(context: Context) = save(context, emptyList())

    private fun current(context: Context) = songs(context).value.orEmpty()

    private fun save(context: Context, songs: List<IdentifiedSong>) {
        _songs.value = songs
        val temp = File(context.filesDir, "$FILE.tmp")
        temp.writeText(json.toJson(songs.map(::toMap)))
        temp.renameTo(File(context.filesDir, FILE))
    }

    private fun load(context: Context): List<IdentifiedSong> = runCatching {
        (json.fromJson(File(context.filesDir, FILE).readText()) as? List<*>).orEmpty().filterIsInstance<Map<*, *>>().map(::fromMap)
    }.getOrDefault(emptyList())

    internal fun toMap(s: IdentifiedSong): Map<String, Any?> = mapOf(
        "title" to s.title, "artist" to s.artist, "album" to s.album, "year" to s.year,
        "coverUrl" to s.coverUrl, "videoId" to s.videoId, "identifiedAt" to s.identifiedAt
    )

    internal fun fromMap(m: Map<*, *>) = IdentifiedSong(
        title = m["title"] as? String ?: "",
        artist = m["artist"] as? String ?: "",
        album = m["album"] as? String ?: "",
        year = m["year"] as? String ?: "",
        coverUrl = m["coverUrl"] as? String,
        videoId = m["videoId"] as? String,
        identifiedAt = (m["identifiedAt"] as? Number)?.toLong() ?: 0L
    )
}
