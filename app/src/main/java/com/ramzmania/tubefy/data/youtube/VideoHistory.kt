package com.ramzmania.tubefy.data.youtube

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

/** A video played on the video page, newest first. */
data class PlayedVideo(
    val id: String,
    val title: String,
    val channel: String,
    val thumbnail: String,
    val durationSeconds: Long,
    val playedAt: Long
)

/** Videos played from the YouTube tab, for its "Recent" list. Stored on the device only. */
@Singleton
class VideoHistory @Inject constructor(@ApplicationContext context: Context) {

    private val prefs = context.getSharedPreferences("video_history", Context.MODE_PRIVATE)
    private val _videos = MutableStateFlow(load())
    val videos: StateFlow<List<PlayedVideo>> = _videos.asStateFlow()

    /** Adds (or moves to the top) [id]; blank fields keep what was stored before. */
    fun record(id: String, title: String, channel: String, thumbnail: String, durationSeconds: Long = -1) {
        if (id.isBlank()) return
        val old = _videos.value.firstOrNull { it.id == id }
        val video = PlayedVideo(
            id = id,
            title = title.ifBlank { old?.title.orEmpty() },
            channel = channel.ifBlank { old?.channel.orEmpty() },
            thumbnail = thumbnail.ifBlank { old?.thumbnail ?: "https://i.ytimg.com/vi/$id/hq720.jpg" },
            durationSeconds = if (durationSeconds > 0) durationSeconds else old?.durationSeconds ?: -1,
            playedAt = System.currentTimeMillis()
        )
        save(listOf(video) + _videos.value.filter { it.id != id })
    }

    fun remove(id: String) = save(_videos.value.filter { it.id != id })

    fun clear() = save(emptyList())

    private fun save(list: List<PlayedVideo>) {
        val trimmed = list.take(MAX_ENTRIES)
        _videos.value = trimmed
        prefs.edit().putString(KEY, JSONArray().apply {
            trimmed.forEach {
                put(
                    JSONObject()
                        .put("id", it.id)
                        .put("title", it.title)
                        .put("channel", it.channel)
                        .put("thumbnail", it.thumbnail)
                        .put("duration", it.durationSeconds)
                        .put("playedAt", it.playedAt)
                )
            }
        }.toString()).apply()
    }

    private fun load(): List<PlayedVideo> = try {
        val array = JSONArray(prefs.getString(KEY, "[]"))
        (0 until array.length()).map { index ->
            val json = array.getJSONObject(index)
            PlayedVideo(
                id = json.getString("id"),
                title = json.optString("title"),
                channel = json.optString("channel"),
                thumbnail = json.optString("thumbnail"),
                durationSeconds = json.optLong("duration", -1),
                playedAt = json.optLong("playedAt")
            )
        }
    } catch (e: Exception) {
        emptyList()
    }

    private companion object {
        const val KEY = "videos"
        const val MAX_ENTRIES = 200
    }
}
