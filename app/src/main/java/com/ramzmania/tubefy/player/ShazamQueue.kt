package com.ramzmania.tubefy.player

import android.net.Uri
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import com.ramzmania.tubefy.core.recognition.ImportedSong

/**
 * Library → Shazam → "Play all": the imported Shazam history as a play queue. Songs already matched
 * with YouTube Music play like any song; the others are queued as "find" items (`tubefy-find://`) and
 * looked up one at a time, just before they're needed (see PlaybackService.lookAheadShazam), so
 * playing the list never fires a burst of searches.
 */
object ShazamQueue {

    const val FIND_SCHEME = "tubefy-find"
    private const val FIND_PREFIX = "find:"

    /** Every song that can be played (not the ones already known to be missing from YouTube Music). */
    fun items(songs: List<ImportedSong>): List<MediaItem> =
        songs.filter { it.status != ImportedSong.Status.NotFound }.map { song ->
            if (song.status == ImportedSong.Status.Found && song.videoId != null) resolved(song) else pending(song)
        }

    fun isPending(item: MediaItem?) = item?.localConfiguration?.uri?.scheme == FIND_SCHEME

    /** The imported song's key, for a pending item. */
    fun keyOf(item: MediaItem): String? = item.localConfiguration?.uri?.getQueryParameter("key")

    /** A matched song: queued by its YouTube Music id, its link fetched when it's about to play. */
    fun resolved(song: ImportedSong): MediaItem {
        val id = song.videoId.orEmpty()
        return MediaItem.Builder()
            .setMediaId(id)
            .setUri(Uri.parse("${CarLibrary.LAZY_SCHEME}://video/$id"))
            .setCustomCacheKey(id)
            .setMediaMetadata(metadata(song))
            .build()
    }

    private fun pending(song: ImportedSong): MediaItem = MediaItem.Builder()
        .setMediaId(FIND_PREFIX + song.key)
        .setUri(Uri.Builder().scheme(FIND_SCHEME).authority("song").appendQueryParameter("key", song.key).build())
        .setMediaMetadata(metadata(song))
        .build()

    private fun metadata(song: ImportedSong) = MediaMetadata.Builder()
        .setTitle(song.title)
        .setArtist(song.artist)
        .setArtworkUri(song.coverUrl?.let(Uri::parse))
        .setIsBrowsable(false)
        .setIsPlayable(true)
        .setMediaType(MediaMetadata.MEDIA_TYPE_MUSIC)
        .build()
}
