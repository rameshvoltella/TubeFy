package com.ramzmania.tubefy.data.youtube

import com.ramzmania.tubefy.core.dataformatter.newpipe.bestThumbnailUrl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.schabi.newpipe.extractor.InfoItem
import org.schabi.newpipe.extractor.Page
import org.schabi.newpipe.extractor.ServiceList
import org.schabi.newpipe.extractor.localization.ContentCountry
import org.schabi.newpipe.extractor.playlist.PlaylistInfoItem
import org.schabi.newpipe.extractor.search.SearchInfo
import org.schabi.newpipe.extractor.stream.Description
import org.schabi.newpipe.extractor.stream.StreamInfo
import org.schabi.newpipe.extractor.stream.StreamInfoItem
import org.schabi.newpipe.extractor.stream.StreamType
import javax.inject.Inject
import javax.inject.Singleton

/** A video (or playlist) row on the YouTube tab. */
data class YouTubeItem(
    val id: String,
    val url: String,
    val title: String,
    val channel: String,
    val thumbnail: String,
    /** Seconds; -1 when unknown (live streams, playlists). */
    val durationSeconds: Long = -1,
    /** -1 when YouTube doesn't say (e.g. music charts). */
    val views: Long = -1,
    val uploadedAgo: String? = null,
    val isLive: Boolean = false,
    val isPlaylist: Boolean = false,
    /** Number of videos, for playlists; -1 when unknown. */
    val videoCount: Long = -1
)

/** The chips on the YouTube tab, plus search. */
sealed interface YouTubeFeed {
    val key: String

    /** Popular recent videos in the language of the user's state (YouTube has no per-state trending). */
    data class InState(val state: String, val language: String) : YouTubeFeed {
        override val key get() = "state:$state"
    }
    data object Trending : YouTubeFeed { override val key = "trending" }
    data object Music : YouTubeFeed { override val key = "music" }
    data class Mixes(val language: String?) : YouTubeFeed { override val key = "mixes" }
    data object Live : YouTubeFeed { override val key = "live" }
    data object Gaming : YouTubeFeed { override val key = "gaming" }
    data class Search(val query: String) : YouTubeFeed { override val key = "search:$query" }
}

/** Where the next page of a feed comes from. A feed can switch source (chart → search) when one ends. */
sealed interface YouTubeCursor {
    /** Charts/kiosks, then [then] once they run out. */
    data class Kiosks(val ids: List<String>, val country: String, val then: YouTubeCursor?) : YouTubeCursor
    data class Kiosk(val id: String, val country: String, val page: Page, val then: YouTubeCursor?) : YouTubeCursor
    data class SearchQuery(val query: String, val filter: String, val page: Page?) : YouTubeCursor
    /** Several sources loaded together and interleaved; each keeps its own paging. */
    data class Mixed(val parts: List<YouTubeCursor>) : YouTubeCursor
}

class YouTubePage(val items: List<YouTubeItem>, val next: YouTubeCursor?)

/** Everything the video page shows besides the video itself. */
data class YouTubeVideoDetails(
    val id: String,
    val title: String,
    val channel: String,
    val channelAvatar: String?,
    /** -1 when hidden / unknown. */
    val subscribers: Long,
    val views: Long,
    val uploadedAgo: String?,
    /** Plain text (links and formatting removed). */
    val description: String,
    val tags: List<String>,
    val isLive: Boolean,
    /** Seconds; 0 for live. */
    val durationSeconds: Long,
    val related: List<YouTubeItem>
)

/**
 * Loads YouTube lists through NewPipe. YouTube removed its Trending page, so "Trending" is built from
 * the charts that still exist (music, trailers, gaming); charts are a single fixed page, so lists go on
 * with matching search results ("load more" never just stops).
 */
@Singleton
class YouTubeFeedSource @Inject constructor() {

    private val youtube get() = ServiceList.YouTube

    fun start(feed: YouTubeFeed, country: String): YouTubeCursor {
        val countryName = countryName(country)
        return when (feed) {
            // No per-state trending on YouTube: popular + new songs + trailers in the state's language
            is YouTubeFeed.InState -> YouTubeCursor.Mixed(
                listOf("${feed.language} trending", "new ${feed.language} songs", "${feed.language} movie trailer")
                    .map { YouTubeCursor.SearchQuery(it, VIDEOS, null) }
            )
            YouTubeFeed.Trending -> YouTubeCursor.Kiosks(
                listOf(KIOSK_MUSIC, KIOSK_TRAILERS, KIOSK_GAMING), country,
                YouTubeCursor.SearchQuery("trending $countryName", VIDEOS, null)
            )
            YouTubeFeed.Music -> YouTubeCursor.Kiosks(
                listOf(KIOSK_MUSIC), country, YouTubeCursor.SearchQuery("new songs $countryName", VIDEOS, null)
            )
            is YouTubeFeed.Mixes -> YouTubeCursor.SearchQuery("${feed.language ?: countryName} songs mix", PLAYLISTS, null)
            YouTubeFeed.Live -> YouTubeCursor.Kiosks(listOf(KIOSK_LIVE), country, null)
            YouTubeFeed.Gaming -> YouTubeCursor.Kiosks(
                listOf(KIOSK_GAMING), country, YouTubeCursor.SearchQuery("gaming $countryName", VIDEOS, null)
            )
            is YouTubeFeed.Search -> YouTubeCursor.SearchQuery(feed.query, ALL, null)
        }
    }

    /** Loads one page from [cursor]; blocking network, so it runs on IO. */
    suspend fun load(cursor: YouTubeCursor): YouTubePage = withContext(Dispatchers.IO) { loadBlocking(cursor) }

    private fun loadBlocking(cursor: YouTubeCursor): YouTubePage = when (cursor) {
        is YouTubeCursor.Kiosks -> {
            // All charts at once, de-duplicated; a chart that fails is skipped
            val charts = mutableListOf<List<YouTubeItem>>()
            var next: YouTubeCursor? = cursor.then
            cursor.ids.forEach { id ->
                runCatching {
                    val extractor = youtube.kioskList.getExtractorById(id, null)
                    extractor.forceContentCountry(ContentCountry(cursor.country))
                    extractor.fetchPage()
                    val page = extractor.initialPage
                    charts += page.items.mapNotNull(::toItem)
                    // Only a single paged kiosk (Live) keeps its own next page
                    val nextPage = page.nextPage
                    if (cursor.ids.size == 1 && nextPage != null && Page.isValid(nextPage)) {
                        next = YouTubeCursor.Kiosk(id, cursor.country, nextPage, cursor.then)
                    }
                }
            }
            val items = interleave(charts).distinctBy { it.id }
            if (items.isEmpty() && cursor.then != null) loadBlocking(cursor.then)
            else YouTubePage(items, next)
        }

        is YouTubeCursor.Kiosk -> {
            val extractor = youtube.kioskList.getExtractorById(cursor.id, null)
            extractor.forceContentCountry(ContentCountry(cursor.country))
            val page = extractor.getPage(cursor.page)
            YouTubePage(
                page.items.mapNotNull(::toItem),
                page.nextPage?.takeIf { Page.isValid(it) }?.let { cursor.copy(page = it) } ?: cursor.then
            )
        }

        is YouTubeCursor.Mixed -> {
            val pages = cursor.parts.mapNotNull { part -> runCatching { loadBlocking(part) }.getOrNull() }
            if (pages.isEmpty()) throw java.io.IOException("No source loaded")
            val next = pages.mapNotNull { it.next }
            YouTubePage(
                interleave(pages.map { it.items }).distinctBy { it.id },
                when (next.size) {
                    0 -> null
                    1 -> next.single()
                    else -> YouTubeCursor.Mixed(next)
                }
            )
        }

        is YouTubeCursor.SearchQuery -> {
            val handler = youtube.searchQHFactory.fromQuery(cursor.query, listOf(cursor.filter), "")
            val (items, nextPage) = if (cursor.page == null) {
                val info = SearchInfo.getInfo(youtube, handler)
                info.relatedItems to info.nextPage
            } else {
                val more = SearchInfo.getMoreItems(youtube, handler, cursor.page)
                more.items to more.nextPage
            }
            YouTubePage(
                items.mapNotNull(::toItem),
                if (Page.isValid(nextPage)) cursor.copy(page = nextPage) else null
            )
        }
    }

    /** Details and related videos for the video page. */
    suspend fun details(videoId: String): YouTubeVideoDetails = withContext(Dispatchers.IO) {
        val info = StreamInfo.getInfo(youtube, "https://www.youtube.com/watch?v=$videoId")
        YouTubeVideoDetails(
            id = videoId,
            title = info.name.orEmpty(),
            channel = info.uploaderName.orEmpty(),
            channelAvatar = bestThumbnailUrl(info.uploaderAvatars).ifBlank { null },
            subscribers = info.uploaderSubscriberCount,
            views = info.viewCount,
            uploadedAgo = info.uploadDate?.offsetDateTime()?.toInstant()?.let { timeAgo(it.toEpochMilli(), System.currentTimeMillis()) }
                ?: info.textualUploadDate,
            description = info.description?.content.orEmpty().let { text ->
                if (info.description?.type == Description.HTML) htmlToText(text) else text.trim()
            },
            tags = info.tags.orEmpty().filter { it.length <= MAX_TAG_LENGTH },
            isLive = info.streamType == StreamType.LIVE_STREAM || info.streamType == StreamType.AUDIO_LIVE_STREAM,
            durationSeconds = info.duration,
            related = info.relatedItems.orEmpty().mapNotNull(::toItem).filter { it.id != videoId }.distinctBy { it.id }
        )
    }

    private fun toItem(item: InfoItem): YouTubeItem? = when (item) {
        is StreamInfoItem -> {
            val id = videoId(item.url)
            if (id == null || item.streamType == StreamType.NONE) null
            else YouTubeItem(
                id = id,
                url = item.url,
                title = item.name.orEmpty(),
                channel = item.uploaderName.orEmpty(),
                thumbnail = bestThumbnailUrl(item.thumbnails).ifBlank { "https://i.ytimg.com/vi/$id/hqdefault.jpg" },
                durationSeconds = item.duration,
                views = item.viewCount,
                uploadedAgo = item.textualUploadDate,
                isLive = item.streamType == StreamType.LIVE_STREAM || item.streamType == StreamType.AUDIO_LIVE_STREAM
            )
        }

        is PlaylistInfoItem -> {
            val id = playlistId(item.url)
            if (id == null) null
            else YouTubeItem(
                id = id,
                url = item.url,
                title = item.name.orEmpty(),
                channel = item.uploaderName.orEmpty(),
                thumbnail = bestThumbnailUrl(item.thumbnails),
                isPlaylist = true,
                videoCount = item.streamCount
            )
        }

        else -> null // channels aren't shown
    }

    companion object {
        private const val KIOSK_MUSIC = "trending_music"
        private const val KIOSK_TRAILERS = "trending_movies_and_shows"
        private const val KIOSK_GAMING = "trending_gaming"
        private const val KIOSK_LIVE = "live"
        private const val VIDEOS = "videos"
        private const val PLAYLISTS = "playlists"
        private const val ALL = "all"

        /** One from each list in turn, so mixed charts don't come one block after another. */
        internal fun <T> interleave(lists: List<List<T>>): List<T> {
            val result = ArrayList<T>(lists.sumOf { it.size })
            val longest = lists.maxOfOrNull { it.size } ?: 0
            for (index in 0 until longest) lists.forEach { list -> list.getOrNull(index)?.let(result::add) }
            return result
        }

        /** YouTube descriptions come as simple HTML (links, line breaks): keep the text and line breaks. */
        internal fun htmlToText(html: String): String = html
            .replace(Regex("(?i)<br\\s*/?>"), "\n")
            .replace(Regex("<[^>]+>"), "")
            .replace("&amp;", "&").replace("&lt;", "<").replace("&gt;", ">")
            .replace("&quot;", "\"").replace("&#39;", "'").replace("&nbsp;", " ")
            .trim()

        private const val MAX_TAG_LENGTH = 20

        /** "3 hours ago", "2 years ago": the way YouTube writes it in lists. */
        internal fun timeAgo(thenMs: Long, nowMs: Long): String {
            val seconds = ((nowMs - thenMs) / 1000).coerceAtLeast(0)
            val (value, unit) = when {
                seconds < 60 -> return "just now"
                seconds < 3_600 -> seconds / 60 to "minute"
                seconds < 86_400 -> seconds / 3_600 to "hour"
                seconds < 7 * 86_400 -> seconds / 86_400 to "day"
                seconds < 30 * 86_400 -> seconds / (7 * 86_400) to "week"
                seconds < 365 * 86_400 -> seconds / (30 * 86_400) to "month"
                else -> seconds / (365 * 86_400) to "year"
            }
            return "$value $unit${if (value == 1L) "" else "s"} ago"
        }

        internal fun videoId(url: String): String? =
            Regex("(?:v=|youtu\\.be/|/shorts/|/live/)([A-Za-z0-9_-]{11})").find(url)?.groupValues?.get(1)

        internal fun playlistId(url: String): String? =
            Regex("[?&]list=([A-Za-z0-9_-]+)").find(url)?.groupValues?.get(1)

        private fun countryName(code: String): String =
            java.util.Locale("", code).getDisplayCountry(java.util.Locale.ENGLISH).ifBlank { code }
    }
}
