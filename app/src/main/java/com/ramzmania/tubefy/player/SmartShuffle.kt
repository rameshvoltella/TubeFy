package com.ramzmania.tubefy.player

import android.net.Uri
import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.SessionCommand
import com.ramzmania.tubefy.core.YoutubeCoreConstant
import com.ramzmania.tubefy.data.Resource
import com.ramzmania.tubefy.data.dto.base.searchformat.TubeFyCoreTypeData
import com.ramzmania.tubefy.data.remote.RemoteRepositorySource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.ConcurrentHashMap
import kotlin.random.Random

/**
 * Smart Shuffle: reorders the upcoming queue so songs by the same artist tend to follow each
 * other, and every [INSERT_EVERY] songs slips in a recommended song similar to the one playing
 * (from its YouTube Mix: same language / mood / artist). Recommendations are fetched one at a time
 * just before they're needed, and unplayed ones are removed when Smart Shuffle is turned off.
 * Must be driven from the main thread (it touches the player).
 */
@UnstableApi
class SmartShuffle(
    private val scope: CoroutineScope,
    private val playerProvider: () -> Player?,
    private val remote: RemoteRepositorySource,
    private val publish: (Bundle) -> Unit,
    private val isOffline: () -> Boolean
) {
    var enabled = false
        private set

    private var songsSinceInsert = 0
    private var insertJob: Job? = null
    private val mixCache = ConcurrentHashMap<String, List<TubeFyCoreTypeData>>()

    fun handleCommand(action: String, args: Bundle) {
        if (action == CMD_SET) setEnabled(args.getBoolean(KEY_ENABLED))
    }

    fun setEnabled(on: Boolean) {
        val player = playerProvider() ?: return
        enabled = on
        if (on) {
            player.shuffleModeEnabled = false // the queue order itself becomes the smart shuffle
            reorderUpcoming(player)
            songsSinceInsert = INSERT_EVERY // first recommendation right away
            maybeInsertRecommendation()
        } else {
            insertJob?.cancel()
            removeUpcomingRecommendations(player)
        }
        publish(bundleOf(EXTRA_ENABLED to enabled))
    }

    /** Call on every song change. */
    fun onSongChanged() {
        if (!enabled) return
        songsSinceInsert++
        maybeInsertRecommendation()
    }

    /** Random order, but a song by the same artist as the previous one is preferred. */
    private fun reorderUpcoming(player: Player) {
        val start = player.currentMediaItemIndex + 1
        val count = player.mediaItemCount
        if (start >= count - 1) return
        val remaining = (start until count).map { player.getMediaItemAt(it) }.shuffled().toMutableList()
        val ordered = mutableListOf<MediaItem>()
        var previousArtist = artistOf(player.currentMediaItem)
        while (remaining.isNotEmpty()) {
            val sameArtist = if (previousArtist.isEmpty()) -1 else remaining.indexOfFirst { artistOf(it) == previousArtist }
            val pick = if (sameArtist >= 0 && Random.nextFloat() < SAME_ARTIST_CHANCE) remaining.removeAt(sameArtist) else remaining.removeAt(0)
            ordered += pick
            previousArtist = artistOf(pick)
        }
        player.removeMediaItems(start, count)
        player.addMediaItems(start, ordered)
    }

    private fun maybeInsertRecommendation() {
        if (isOffline() || songsSinceInsert < INSERT_EVERY || insertJob?.isActive == true) return
        val player = playerProvider() ?: return
        val seedId = player.currentMediaItem?.mediaId?.takeIf { it.isNotBlank() } ?: return
        val nextIndex = player.nextMediaItemIndex
        if (nextIndex != C.INDEX_UNSET && isRecommendation(player.getMediaItemAt(nextIndex))) return
        val queuedIds = (0 until player.mediaItemCount).map { player.getMediaItemAt(it).mediaId }.toSet()

        insertJob = scope.launch {
            val candidates = mixCache[seedId] ?: run {
                val mix = (remote.getMixQueue(seedId).first() as? Resource.Success)?.data.orEmpty()
                if (mix.isNotEmpty()) mixCache[seedId] = mix
                mix
            }
            val pick = candidates.firstOrNull { plainId(it.videoId) !in queuedIds } ?: return@launch
            val id = plainId(pick.videoId)
            val stream = (remote.getStreamUrl(id).first() as? Resource.Success)?.data ?: return@launch
            val item = MediaItem.Builder()
                .setMediaId(id)
                .setUri(Uri.parse(stream.streamUrl))
                .setCustomCacheKey(id)
                .setMediaMetadata(
                    MediaMetadata.Builder()
                        .setTitle(pick.videoTitle)
                        .setArtist(stream.artist.ifBlank { pick.artist })
                        .setArtworkUri(Uri.parse("https://i.ytimg.com/vi/$id/hq720.jpg"))
                        .setIsBrowsable(false)
                        .setIsPlayable(true)
                        .setMediaType(MediaMetadata.MEDIA_TYPE_MUSIC)
                        .setExtras(bundleOf(EXTRA_RECOMMENDATION to true))
                        .build()
                )
                .build()
            withContext(Dispatchers.Main) {
                val player = playerProvider() ?: return@withContext
                if (!enabled || player.mediaItemCount == 0) return@withContext
                player.addMediaItem(player.currentMediaItemIndex + 1, item)
                songsSinceInsert = 0
            }
        }
    }

    private fun removeUpcomingRecommendations(player: Player) {
        for (index in player.mediaItemCount - 1 downTo player.currentMediaItemIndex + 1) {
            if (isRecommendation(player.getMediaItemAt(index))) player.removeMediaItem(index)
        }
    }

    private fun isRecommendation(item: MediaItem) =
        item.mediaMetadata.extras?.getBoolean(EXTRA_RECOMMENDATION) == true

    private fun artistOf(item: MediaItem?): String {
        val artist = item?.mediaMetadata?.artist?.toString().orEmpty()
        // "Artist :<title>" is the placeholder used when the real artist is unknown
        return if (artist.startsWith("Artist :")) "" else artist.trim().lowercase()
    }

    private fun plainId(videoId: String) = YoutubeCoreConstant.extractYoutubeVideoId(videoId) ?: videoId

    companion object {
        const val CMD_SET = "com.ramzmania.tubefy.SMART_SHUFFLE_SET"
        const val KEY_ENABLED = "enabled"
        const val EXTRA_ENABLED = "com.ramzmania.tubefy.SMART_SHUFFLE"
        const val EXTRA_RECOMMENDATION = "com.ramzmania.tubefy.SMART_PICK"
        private const val INSERT_EVERY = 2
        private const val SAME_ARTIST_CHANCE = 0.6f

        val COMMANDS = listOf(SessionCommand(CMD_SET, Bundle.EMPTY))
    }
}
