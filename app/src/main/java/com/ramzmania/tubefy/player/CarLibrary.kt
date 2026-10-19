package com.ramzmania.tubefy.player

import android.content.Context
import android.net.Uri
import androidx.core.os.bundleOf
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.util.UnstableApi
import com.ramzmania.tubefy.core.YoutubeCoreConstant
import com.ramzmania.tubefy.core.download.Downloads
import com.ramzmania.tubefy.core.recommendation.DailyRecommendations
import com.ramzmania.tubefy.data.Resource
import com.ramzmania.tubefy.data.database.DatabaseRepositorySource
import com.ramzmania.tubefy.data.dto.base.searchformat.TubeFyCoreTypeData
import com.ramzmania.tubefy.data.local.SongHistory
import com.ramzmania.tubefy.data.remote.RemoteRepositorySource
import kotlinx.coroutines.flow.first
import java.util.concurrent.ConcurrentHashMap

/**
 * The library shown by Android Auto (and other media browsers):
 *
 * ```
 * root
 * ├── Recently played      songs the user started themselves
 * ├── Recommended          today's picks (songs + artist playlists)
 * └── Library              Favorites, Downloads and the user's playlists
 * ```
 *
 * Streamed songs are queued as `tubefy://video/<videoId>`; [StreamUrlRefresher] fetches the real link
 * just before each song plays, so a whole folder can be queued instantly. Downloads play from file.
 */
@UnstableApi
class CarLibrary(
    private val context: Context,
    private val songHistory: SongHistory,
    private val dailyRecommendations: DailyRecommendations,
    private val database: DatabaseRepositorySource,
    private val remote: RemoteRepositorySource
) {
    /** Last children listed per folder, so picking a song can queue the rest of its folder. */
    private val listed = ConcurrentHashMap<String, List<MediaItem>>()

    fun root(): MediaItem = folder(ROOT, "TubeFy")

    suspend fun children(parentId: String): List<MediaItem> {
        val children = when {
            parentId == ROOT -> listOf(
                folder(RECENT, "Recently played"),
                folder(RECOMMENDED, "Recommended"),
                folder(LIBRARY, "Library")
            )

            parentId == RECENT -> songHistory.songs.value.map { song(RECENT, it.videoId, it.title, "", it.thumbnail) }

            parentId == RECOMMENDED -> dailyRecommendations.today().map { item ->
                if (item.videoId.isEmpty()) {
                    val playlistId = YoutubeCoreConstant.extractPlaylistId(item.plaListUrl.orEmpty()).orEmpty()
                    folder("$YOUTUBE_PLAYLIST$playlistId", item.videoTitle, item.videoImage)
                } else {
                    song(RECOMMENDED, item.videoId, item.videoTitle, item.artist, item.videoImage)
                }
            }

            parentId == LIBRARY -> {
                val playlists = (database.getAllSavedPlayList().first() as? Resource.Success)?.data.orEmpty()
                    .filter { it.playlistName != FAVORITES_NAME && it.playlistName != Downloads.PLAYLIST }
                listOf(folder(FAVORITES, "Favorites"), folder(DOWNLOADS, "Downloads")) +
                    playlists.map { folder("$MY_PLAYLIST${it.playlistName}", it.playlistName.trim(), it.videoThump) }
            }

            parentId == FAVORITES -> songsOf(parentId, database.getFavorites().first())

            parentId == DOWNLOADS -> songsOf(parentId, database.getSpecificPlayList(Downloads.PLAYLIST).first())
                .filter { Downloads.isDownloaded(context, it.mediaId.substringAfter(SEPARATOR)) }

            parentId.startsWith(MY_PLAYLIST) ->
                songsOf(parentId, database.getSpecificPlayList(parentId.removePrefix(MY_PLAYLIST)).first())

            parentId.startsWith(YOUTUBE_PLAYLIST) -> {
                val playlistId = parentId.removePrefix(YOUTUBE_PLAYLIST)
                val result = remote.getPlayListInfo("https://music.youtube.com/playlist?list=$playlistId").first()
                (result as? Resource.Success)?.data?.playListVideoList.orEmpty().filterNotNull()
                    .filter { it.videoId.isNotEmpty() }
                    .map { song(parentId, it.videoId, it.videoTitle, it.artist, it.videoImage) }
            }

            else -> emptyList()
        }.take(MAX_CHILDREN)
        listed[parentId] = children
        return children
    }

    /**
     * Turns what a car picked into a playable queue: the picked song plus the rest of its folder.
     * Returns the items, the start index, and whether it's offline (Downloads) playback.
     */
    fun resolveQueue(picked: List<MediaItem>, startIndex: Int): Triple<List<MediaItem>, Int, Boolean> {
        val single = picked.singleOrNull()
        val parentId = single?.mediaId?.substringBefore(SEPARATOR, missingDelimiterValue = "")
        val folder = parentId?.let { listed[it] }
        val (items, start) = if (single != null && folder != null) {
            val index = folder.indexOfFirst { it.mediaId == single.mediaId }
            if (index >= 0) folder to index else picked to startIndex
        } else {
            picked to startIndex
        }
        val offline = items.isNotEmpty() && items.all { it.mediaId.startsWith("$DOWNLOADS$SEPARATOR") }
        return Triple(items.map(::playable), start.coerceIn(0, (items.size - 1).coerceAtLeast(0)), offline)
    }

    /** A browse item with a real source: a local file for downloads, a lazily resolved stream otherwise. */
    fun playable(item: MediaItem): MediaItem {
        // Cars may send just the id (no metadata), so only folders and non-library items are skipped
        if (!item.mediaId.contains(SEPARATOR) || item.mediaMetadata.isBrowsable == true) return item
        val videoId = item.mediaId.substringAfter(SEPARATOR)
        if (videoId.isBlank()) return item
        val offline = item.mediaId.startsWith("$DOWNLOADS$SEPARATOR") && Downloads.isDownloaded(context, videoId)
        val uri = if (offline) Uri.fromFile(Downloads.file(context, videoId)) else Uri.parse("$LAZY_SCHEME://video/$videoId")
        return MediaItem.Builder()
            .setMediaId(videoId)
            .setUri(uri)
            .setCustomCacheKey(videoId)
            .setMediaMetadata(
                item.mediaMetadata.buildUpon()
                    .setExtras(bundleOf(Downloads.OFFLINE_EXTRA to offline))
                    .build()
            )
            .build()
    }

    private fun songsOf(parentId: String, result: Resource<List<TubeFyCoreTypeData?>>): List<MediaItem> =
        (result as? Resource.Success)?.data.orEmpty().filterNotNull()
            .filter { it.videoId.isNotEmpty() }
            .map { song(parentId, it.videoId, it.videoTitle, it.artist, it.videoImage) }

    private fun folder(id: String, title: String, artwork: String? = null): MediaItem =
        MediaItem.Builder()
            .setMediaId(id)
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(title)
                    .setIsBrowsable(true)
                    .setIsPlayable(false)
                    .setMediaType(MediaMetadata.MEDIA_TYPE_FOLDER_PLAYLISTS)
                    .apply { artwork?.takeIf { it.isNotBlank() }?.let { setArtworkUri(Uri.parse(YoutubeCoreConstant.decodeThumpUrl(it))) } }
                    .build()
            )
            .build()

    private fun song(parentId: String, videoId: String, title: String, artist: String, artwork: String): MediaItem {
        val id = YoutubeCoreConstant.extractYoutubeVideoId(videoId) ?: videoId
        val art = artwork.takeIf { it.isNotBlank() }?.let { YoutubeCoreConstant.decodeThumpUrl(it) }
            ?: "https://i.ytimg.com/vi/$id/hq720.jpg"
        return MediaItem.Builder()
            .setMediaId("$parentId$SEPARATOR$id")
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(title)
                    .setArtist(artist.ifBlank { null })
                    .setArtworkUri(Uri.parse(art))
                    .setIsBrowsable(false)
                    .setIsPlayable(true)
                    .setMediaType(MediaMetadata.MEDIA_TYPE_MUSIC)
                    .build()
            )
            .build()
    }

    companion object {
        /** Streams queued from the car are resolved to a real link by [StreamUrlRefresher]. */
        const val LAZY_SCHEME = "tubefy"

        private const val ROOT = "root"
        private const val RECENT = "recent"
        private const val RECOMMENDED = "recommended"
        private const val LIBRARY = "library"
        private const val FAVORITES = "favorites"
        private const val DOWNLOADS = "downloads"
        private const val MY_PLAYLIST = "my:"
        private const val YOUTUBE_PLAYLIST = "yt:"
        private const val SEPARATOR = "|"
        private const val FAVORITES_NAME = "TubeFy-Favorites"
        private const val MAX_CHILDREN = 100
    }
}
