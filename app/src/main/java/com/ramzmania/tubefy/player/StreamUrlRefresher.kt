package com.ramzmania.tubefy.player

import android.net.Uri
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DataSpec
import androidx.media3.datasource.ResolvingDataSource
import com.ramzmania.tubefy.core.YoutubeCoreConstant
import org.schabi.newpipe.extractor.services.youtube.YoutubeService
import org.schabi.newpipe.extractor.services.youtube.extractors.YoutubeStreamExtractor
import java.io.IOException
import java.util.Collections
import java.util.concurrent.ConcurrentHashMap

/**
 * YouTube stream links expire (~6h, see their `expire=` parameter). Queued songs keep the link
 * fetched when they were queued, so after a long pause they would fail. Right before the player
 * reads a YouTube link, this swaps an expired (or force-refreshed) link for a fresh one fetched by
 * the song's id, which every queued item carries as its custom cache key. Valid links pass
 * through untouched, and local files (Downloads) are ignored.
 */
@UnstableApi
class StreamUrlRefresher : ResolvingDataSource.Resolver {

    private val refreshed = ConcurrentHashMap<String, Uri>()

    /** Video-page manifests by video id, as data: URIs (see [VideoManifest]). */
    private val manifests = ConcurrentHashMap<String, Uri>()

    /** Stores a freshly built manifest so the first load doesn't fetch it again. */
    fun putManifest(videoId: String, manifest: String) {
        manifests[videoId] = dataUri(manifest)
    }
    private val forced: MutableSet<String> = Collections.newSetFromMap(ConcurrentHashMap())

    /** Makes the next load of [videoId] fetch a new link even if the current one isn't expired. */
    fun forceRefresh(videoId: String) {
        refreshed.remove(videoId)
        manifests.remove(videoId)
        forced.add(videoId)
    }

    override fun resolveDataSpec(dataSpec: DataSpec): DataSpec {
        val uri = dataSpec.uri
        val videoId = dataSpec.key

        // Video page: a DASH manifest (all qualities / audio languages), rebuilt when its links expired
        if (uri.scheme == DASH_SCHEME) {
            val id = uri.lastPathSegment ?: throw IOException("No video id")
            if (forced.remove(id)) manifests.remove(id)
            val manifest = manifests[id]
                ?: VideoManifest.fetch(id)?.let { dataUri(it.manifest).also { built -> manifests[id] = built } }
                ?: throw IOException("No manifest for $id")
            return dataSpec.withUri(manifest)
        }

        // Songs queued without a link (e.g. from Android Auto): fetch one now, by song id
        if (uri.scheme == CarLibrary.LAZY_SCHEME) {
            val id = videoId?.takeIf { it.isNotBlank() } ?: uri.lastPathSegment ?: throw IOException("No song id")
            val link = refreshed[id]?.takeIf { !isExpiring(it) }
                ?: fetchFreshLink(id, itag = null)?.also { refreshed[id] = it }
                ?: throw IOException("No stream for $id")
            return dataSpec.withUri(link)
        }

        // Only links keyed by a song id (DASH segments are keyed by their own URL and refresh with the manifest)
        if (videoId.isNullOrBlank() || videoId.length != 11 || uri.host?.endsWith("googlevideo.com") != true) return dataSpec

        val force = forced.remove(videoId)
        val current = refreshed[videoId] ?: uri
        if (!force && !isExpiring(current)) {
            return if (current == uri) dataSpec else dataSpec.withUri(current)
        }
        val fresh = fetchFreshLink(videoId, uri.getQueryParameter("itag")) ?: return dataSpec
        refreshed[videoId] = fresh
        return dataSpec.withUri(fresh)
    }

    private fun isExpiring(uri: Uri): Boolean {
        val expireAt = uri.getQueryParameter("expire")?.toLongOrNull() ?: return false
        return expireAt - EXPIRY_MARGIN_SECONDS <= System.currentTimeMillis() / 1000
    }

    /** Same format (itag) as before, so a resumed position still points at the same bytes. */
    private fun fetchFreshLink(videoId: String, itag: String?): Uri? = try {
        val extractor = YoutubeService(0)
            .getStreamExtractor("${YoutubeCoreConstant.YOUTUBE_WATCH_URL}$videoId") as YoutubeStreamExtractor
        extractor.fetchPage()
        val streams = extractor.videoStreams
        val stream = streams.firstOrNull { it.itag.toString() == itag } ?: streams.firstOrNull()
        (stream?.content ?: extractor.hlsUrl.takeIf { it.isNotBlank() })?.let(Uri::parse)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }

    private fun dataUri(manifest: String): Uri = Uri.parse(
        "data:application/dash+xml;base64," +
            android.util.Base64.encodeToString(manifest.toByteArray(Charsets.UTF_8), android.util.Base64.NO_WRAP)
    )

    companion object {
        private const val EXPIRY_MARGIN_SECONDS = 60L

        /** `tubefy-dash://video/<id>`: a video-page item, resolved to its DASH manifest on load. */
        const val DASH_SCHEME = "tubefy-dash"
    }
}
