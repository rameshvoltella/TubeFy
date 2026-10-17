package com.ramzmania.tubefy.data.local

import android.content.Context
import com.ramzmania.tubefy.core.YoutubeCoreConstant
import com.ramzmania.tubefy.data.dto.base.searchformat.TubeFyCoreTypeData
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Songs/playlists the user picked from search results, newest first (max [MAX_ITEMS]).
 * Shown when the search box is focused and empty.
 */
@Singleton
class RecentSearches @Inject constructor(@ApplicationContext context: Context) {

    private val prefs = context.getSharedPreferences("recent_searches", Context.MODE_PRIVATE)
    private val itemsPrivate = MutableStateFlow(load())
    val items: StateFlow<List<TubeFyCoreTypeData>> = itemsPrivate.asStateFlow()

    fun add(item: TubeFyCoreTypeData) {
        val key = keyOf(item)
        update(listOf(item) + itemsPrivate.value.filter { keyOf(it) != key })
    }

    fun remove(item: TubeFyCoreTypeData) {
        val key = keyOf(item)
        update(itemsPrivate.value.filter { keyOf(it) != key })
    }

    fun clear() = update(emptyList())

    private fun update(list: List<TubeFyCoreTypeData>) {
        val trimmed = list.take(MAX_ITEMS)
        itemsPrivate.value = trimmed
        prefs.edit().putString(KEY, toJson(trimmed)).apply()
    }

    /** Songs are matched by video id, playlist results by their playlist url. */
    private fun keyOf(item: TubeFyCoreTypeData) =
        if (item.videoId.isNotEmpty()) YoutubeCoreConstant.extractYoutubeVideoId(item.videoId) ?: item.videoId
        else item.plaListUrl.orEmpty()

    private fun load(): List<TubeFyCoreTypeData> = try {
        val array = JSONArray(prefs.getString(KEY, "[]"))
        (0 until array.length()).map { index ->
            val json = array.getJSONObject(index)
            TubeFyCoreTypeData(
                videoId = json.optString("videoId"),
                videoTitle = json.optString("title"),
                videoImage = json.optString("image"),
                plaListUrl = json.optString("playlistUrl").ifEmpty { null },
                artist = json.optString("artist"),
                durationSeconds = json.optLong("duration", -1),
                viewCount = json.optLong("views", -1)
            )
        }
    } catch (e: Exception) {
        emptyList()
    }

    private fun toJson(list: List<TubeFyCoreTypeData>): String = JSONArray().apply {
        list.forEach { item ->
            put(
                JSONObject()
                    .put("videoId", item.videoId)
                    .put("title", item.videoTitle)
                    .put("image", item.videoImage)
                    .put("playlistUrl", item.plaListUrl.orEmpty())
                    .put("artist", item.artist)
                    .put("duration", item.durationSeconds)
                    .put("views", item.viewCount)
            )
        }
    }.toString()

    private companion object {
        const val KEY = "items"
        const val MAX_ITEMS = 20
    }
}
