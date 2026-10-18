package com.ramzmania.tubefy.data.local

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.pow

/** A playlist the user played songs from: a YouTube playlist/album or one of their own. */
data class PlaylistVisit(
    val key: String,
    val isCustom: Boolean,
    /** YouTube playlist id, or the custom playlist's name. */
    val id: String,
    val title: String,
    val thumbnail: String,
    val lastPlayedAt: Long,
    val score: Double
)

/**
 * Remembers which playlists the user plays from. Each play adds a point and points halve every
 * [HALF_LIFE_MS], so [top] favours playlists used often *and* recently. Stored on the device only.
 */
@Singleton
class PlaylistHistory @Inject constructor(@ApplicationContext context: Context) {

    private val prefs = context.getSharedPreferences("playlist_history", Context.MODE_PRIVATE)
    private val visitsPrivate = MutableStateFlow(load())
    val visits: StateFlow<List<PlaylistVisit>> = visitsPrivate.asStateFlow()

    fun recordYoutubePlaylist(playlistId: String, title: String, thumbnail: String) =
        record("yt:$playlistId", isCustom = false, id = playlistId, title = title, thumbnail = thumbnail)

    fun recordCustomPlaylist(name: String, thumbnail: String) =
        record("my:$name", isCustom = true, id = name, title = name, thumbnail = thumbnail)

    /** Forget a playlist of the user's own (e.g. after it was deleted). */
    fun removeCustomPlaylist(name: String) = update(visitsPrivate.value.filter { it.key != "my:$name" })

    /**
     * The [count] top playlists, but only once the user has played from at least [count]
     * different playlists; otherwise empty.
     */
    fun top(visits: List<PlaylistVisit>, count: Int, now: Long = System.currentTimeMillis()): List<PlaylistVisit> {
        if (visits.size < count) return emptyList()
        return visits.sortedByDescending { decayed(it, now) }.take(count)
    }

    private fun record(key: String, isCustom: Boolean, id: String, title: String, thumbnail: String) {
        if (id.isBlank()) return
        val now = System.currentTimeMillis()
        val existing = visitsPrivate.value.firstOrNull { it.key == key }
        val visit = PlaylistVisit(
            key = key,
            isCustom = isCustom,
            id = id,
            title = title.ifBlank { existing?.title.orEmpty() },
            thumbnail = thumbnail.ifBlank { existing?.thumbnail.orEmpty() },
            lastPlayedAt = now,
            score = (existing?.let { decayed(it, now) } ?: 0.0) + 1.0
        )
        val others = visitsPrivate.value.filter { it.key != key }
        update((others + visit).sortedByDescending { decayed(it, now) }.take(MAX_ENTRIES))
    }

    private fun decayed(visit: PlaylistVisit, now: Long): Double {
        val age = (now - visit.lastPlayedAt).coerceAtLeast(0L).toDouble()
        return visit.score * 0.5.pow(age / HALF_LIFE_MS)
    }

    private fun update(list: List<PlaylistVisit>) {
        visitsPrivate.value = list
        prefs.edit().putString(KEY, toJson(list)).apply()
    }

    private fun load(): List<PlaylistVisit> = try {
        val array = JSONArray(prefs.getString(KEY, "[]"))
        (0 until array.length()).map { index ->
            val json = array.getJSONObject(index)
            PlaylistVisit(
                key = json.getString("key"),
                isCustom = json.optBoolean("custom"),
                id = json.optString("id"),
                title = json.optString("title"),
                thumbnail = json.optString("thumbnail"),
                lastPlayedAt = json.optLong("lastPlayedAt"),
                score = json.optDouble("score", 1.0)
            )
        }
    } catch (e: Exception) {
        emptyList()
    }

    private fun toJson(list: List<PlaylistVisit>): String = JSONArray().apply {
        list.forEach {
            put(
                JSONObject()
                    .put("key", it.key)
                    .put("custom", it.isCustom)
                    .put("id", it.id)
                    .put("title", it.title)
                    .put("thumbnail", it.thumbnail)
                    .put("lastPlayedAt", it.lastPlayedAt)
                    .put("score", it.score)
            )
        }
    }.toString()

    private companion object {
        const val KEY = "visits"
        const val MAX_ENTRIES = 30
        const val HALF_LIFE_MS = 7L * 24 * 60 * 60 * 1000
    }
}
