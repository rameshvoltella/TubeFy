package com.ramzmania.tubefy.core.recommendation

import android.content.Context
import com.ramzmania.tubefy.core.YoutubeCoreConstant
import com.ramzmania.tubefy.data.Resource
import com.ramzmania.tubefy.data.dto.base.searchformat.TubeFyCoreTypeData
import com.ramzmania.tubefy.data.local.SongHistory
import com.ramzmania.tubefy.data.remote.RemoteRepositorySource
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

/**
 * Home's "Recommended for today": songs from the YouTube Mixes of a few songs the user recently
 * chose to play, plus playlists of the artists those Mixes centre on. Built once per day (seeded
 * by the date) and cached, so the list is stable all day and changes the next day.
 */
@Singleton
class DailyRecommendations @Inject constructor(
    @ApplicationContext context: Context,
    private val remote: RemoteRepositorySource,
    private val songHistory: SongHistory
) {
    private val prefs = context.getSharedPreferences("daily_recommendations", Context.MODE_PRIVATE)

    /** Today's recommendations; empty until the user has started at least [MIN_HISTORY] songs. */
    suspend fun today(): List<TubeFyCoreTypeData> {
        val day = LocalDate.now().toString()
        if (prefs.getString(KEY_DAY, null) == day) {
            load().takeIf { it.isNotEmpty() }?.let { return it }
        }
        val history = songHistory.songs.value
        if (history.size < MIN_HISTORY) return emptyList()

        val random = Random(day.hashCode())
        val seeds = history.take(SEED_POOL).shuffled(random).take(SEED_COUNT)
        val list = coroutineScope {
            val mixes = seeds.map { seed ->
                async { (remote.getMixQueue(seed.videoId).first() as? Resource.Success)?.data.orEmpty() }
            }.awaitAll()
            val playlists = dominantArtists(mixes).take(ARTIST_COUNT).map { artist ->
                async {
                    (remote.getNewPipePageSearch(0, artist, listOf("music_playlists"), "").first() as? Resource.Success)
                        ?.data?.youtubeSortedData?.youtubeSortedList.orEmpty()
                        .filter { it.videoId.isEmpty() && !it.plaListUrl.isNullOrBlank() }
                        .take(PLAYLISTS_PER_ARTIST)
                }
            }.awaitAll().flatten()
            compose(mixes, playlists, played = history.map { it.videoId }.toSet())
        }
        if (list.isNotEmpty()) save(day, list)
        return list
    }

    private fun load(): List<TubeFyCoreTypeData> = try {
        val array = JSONArray(prefs.getString(KEY_ITEMS, "[]"))
        (0 until array.length()).map { index ->
            val json = array.getJSONObject(index)
            TubeFyCoreTypeData(
                videoId = json.optString("videoId"),
                videoTitle = json.optString("title"),
                videoImage = json.optString("image"),
                plaListUrl = json.optString("playlistUrl").ifEmpty { null },
                artist = json.optString("artist")
            )
        }
    } catch (e: Exception) {
        emptyList()
    }

    private fun save(day: String, list: List<TubeFyCoreTypeData>) {
        val array = JSONArray().apply {
            list.forEach {
                put(
                    JSONObject()
                        .put("videoId", it.videoId)
                        .put("title", it.videoTitle)
                        .put("image", it.videoImage)
                        .put("playlistUrl", it.plaListUrl.orEmpty())
                        .put("artist", it.artist)
                )
            }
        }
        prefs.edit().putString(KEY_DAY, day).putString(KEY_ITEMS, array.toString()).apply()
    }

    companion object {
        private const val KEY_DAY = "day"
        private const val KEY_ITEMS = "items"
        const val MIN_HISTORY = 3
        private const val SEED_POOL = 10
        private const val SEED_COUNT = 3
        private const val ARTIST_COUNT = 2
        private const val PLAYLISTS_PER_ARTIST = 2
        const val MAX_ITEMS = 12

        /** The artist each Mix is mostly made of (the user's taste), most common first. */
        fun dominantArtists(mixes: List<List<TubeFyCoreTypeData>>): List<String> =
            mixes.mapNotNull { mix ->
                mix.map { it.artist.trim() }
                    .filter { it.isNotEmpty() }
                    .groupingBy { it }
                    .eachCount()
                    .maxByOrNull { it.value }
                    ?.key
            }.distinct()

        /**
         * Songs taken in turn from each Mix (so no single seed dominates), skipping songs the user
         * already played and duplicates; every third card is a playlist. At most [maxItems].
         */
        fun compose(
            mixes: List<List<TubeFyCoreTypeData>>,
            playlists: List<TubeFyCoreTypeData>,
            played: Set<String>,
            maxItems: Int = MAX_ITEMS
        ): List<TubeFyCoreTypeData> {
            val seen = played.toMutableSet()
            val songs = mutableListOf<TubeFyCoreTypeData>()
            val longest = mixes.maxOfOrNull { it.size } ?: 0
            for (index in 0 until longest) {
                for (mix in mixes) {
                    val song = mix.getOrNull(index) ?: continue
                    val id = YoutubeCoreConstant.extractYoutubeVideoId(song.videoId) ?: song.videoId
                    if (id.isNotBlank() && seen.add(id)) songs += song
                }
            }
            val uniquePlaylists = playlists.distinctBy { it.plaListUrl }
            val result = mutableListOf<TubeFyCoreTypeData>()
            var songIndex = 0
            var playlistIndex = 0
            while (result.size < maxItems && (songIndex < songs.size || playlistIndex < uniquePlaylists.size)) {
                val playlistTurn = result.size % 3 == 2 && playlistIndex < uniquePlaylists.size
                if (playlistTurn || songIndex >= songs.size) {
                    result += uniquePlaylists[playlistIndex++]
                } else {
                    result += songs[songIndex++]
                }
            }
            return result
        }
    }
}
