package com.ramzmania.tubefy.player

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.core.os.bundleOf
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import com.ramzmania.tubefy.core.download.Downloads
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

/**
 * The play queue and position, kept on disk so they survive Android closing TubeFy during a long pause.
 * On restart the service puts them back (paused); expired links are refreshed by [StreamUrlRefresher]
 * when the song loads, by the song id each item carries.
 */
class QueueStore(context: Context) {

    class Saved(val items: List<MediaItem>, val index: Int, val positionMs: Long, val offline: Boolean)

    private val file = File(context.filesDir, "play_queue.json")

    fun save(player: Player, offline: Boolean) {
        val count = player.mediaItemCount
        if (count == 0) {
            file.delete()
            return
        }
        val items = JSONArray()
        // The window around the current song, so a huge queue stays a small file
        val index = player.currentMediaItemIndex
        val from = (index - KEEP_BEFORE).coerceAtLeast(0)
        val to = (from + MAX_ITEMS).coerceAtMost(count)
        for (i in from until to) {
            val item = player.getMediaItemAt(i)
            val local = item.localConfiguration ?: continue
            val meta = item.mediaMetadata
            items.put(
                JSONObject()
                    .put("id", item.mediaId)
                    .put("uri", local.uri.toString())
                    .put("key", local.customCacheKey)
                    .put("mime", local.mimeType)
                    .put("title", meta.title?.toString())
                    .put("artist", meta.artist?.toString())
                    .put("art", meta.artworkUri?.toString())
                    .put("type", meta.mediaType ?: MediaMetadata.MEDIA_TYPE_MUSIC)
                    .put("offline", meta.extras?.getBoolean(Downloads.OFFLINE_EXTRA) == true)
            )
        }
        val json = JSONObject()
            .put("items", items)
            .put("index", index - from)
            .put("position", player.currentPosition.coerceAtLeast(0))
            .put("offline", offline)
        runCatching { file.writeText(json.toString()) }.onFailure { Log.w(TAG, "Couldn't save the queue", it) }
    }

    fun load(): Saved? = runCatching {
        if (!file.exists()) return null
        val json = JSONObject(file.readText())
        val array = json.getJSONArray("items")
        val items = (0 until array.length()).map { i ->
            val o = array.getJSONObject(i)
            MediaItem.Builder()
                .setMediaId(o.getString("id"))
                .setUri(Uri.parse(o.getString("uri")))
                .setCustomCacheKey(o.optString("key").ifEmpty { null })
                .setMimeType(o.optString("mime").ifEmpty { null })
                .setMediaMetadata(
                    MediaMetadata.Builder()
                        .setTitle(o.optString("title").ifEmpty { null })
                        .setArtist(o.optString("artist").ifEmpty { null })
                        .setArtworkUri(o.optString("art").ifEmpty { null }?.let(Uri::parse))
                        .setMediaType(o.optInt("type", MediaMetadata.MEDIA_TYPE_MUSIC))
                        .setIsBrowsable(false)
                        .setIsPlayable(true)
                        .apply { if (o.optBoolean("offline")) setExtras(bundleOf(Downloads.OFFLINE_EXTRA to true)) }
                        .build()
                )
                .build()
        }
        if (items.isEmpty()) return null
        Saved(items, json.optInt("index").coerceIn(0, items.lastIndex), json.optLong("position"), json.optBoolean("offline"))
    }.onFailure { Log.w(TAG, "Couldn't restore the queue", it) }.getOrNull()

    private companion object {
        const val TAG = "QueueStore"
        const val MAX_ITEMS = 200
        const val KEEP_BEFORE = 50
    }
}
