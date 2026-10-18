package com.ramzmania.tubefy.data.local

import android.content.Context
import com.ramzmania.tubefy.core.YoutubeCoreConstant
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

/** A song the user chose to play themselves (not one queued automatically). */
data class PlayedSong(
    val videoId: String,
    val title: String,
    val thumbnail: String,
    val playedAt: Long
)

/** Songs the user started themselves, newest first. Stored on the device only. */
@Singleton
class SongHistory @Inject constructor(@ApplicationContext context: Context) {

    private val prefs = context.getSharedPreferences("song_history", Context.MODE_PRIVATE)
    private val songsPrivate = MutableStateFlow(load())
    val songs: StateFlow<List<PlayedSong>> = songsPrivate.asStateFlow()

    fun record(videoId: String, title: String, thumbnail: String) {
        val id = YoutubeCoreConstant.extractYoutubeVideoId(videoId) ?: videoId
        if (id.isBlank()) return
        val song = PlayedSong(id, title, thumbnail, System.currentTimeMillis())
        update(listOf(song) + songsPrivate.value.filter { it.videoId != id })
    }

    private fun update(list: List<PlayedSong>) {
        val trimmed = list.take(MAX_ENTRIES)
        songsPrivate.value = trimmed
        prefs.edit().putString(KEY, toJson(trimmed)).apply()
    }

    private fun load(): List<PlayedSong> = try {
        val array = JSONArray(prefs.getString(KEY, "[]"))
        (0 until array.length()).map { index ->
            val json = array.getJSONObject(index)
            PlayedSong(
                videoId = json.getString("videoId"),
                title = json.optString("title"),
                thumbnail = json.optString("thumbnail"),
                playedAt = json.optLong("playedAt")
            )
        }
    } catch (e: Exception) {
        emptyList()
    }

    private fun toJson(list: List<PlayedSong>): String = JSONArray().apply {
        list.forEach {
            put(
                JSONObject()
                    .put("videoId", it.videoId)
                    .put("title", it.title)
                    .put("thumbnail", it.thumbnail)
                    .put("playedAt", it.playedAt)
            )
        }
    }.toString()

    private companion object {
        const val KEY = "songs"
        const val MAX_ENTRIES = 30
    }
}
