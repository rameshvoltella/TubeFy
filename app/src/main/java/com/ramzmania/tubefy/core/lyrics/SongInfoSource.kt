package com.ramzmania.tubefy.core.lyrics

import com.ramzmania.tubefy.core.youtube.YouTubeAccess
import com.ramzmania.tubefy.core.youtube.YouTubeStreams
import com.squareup.moshi.Moshi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.Collections
import javax.inject.Inject
import javax.inject.Singleton

/** A song's credits for the Lyrics tab; blank fields weren't found. */
data class SongDetails(
    val title: String,
    val singers: String,
    val movie: String,
    val lyricist: String,
    val composer: String,
    val year: String
)

/** One lyrics line; [timeMs] is set for timed ("synced") lyrics. */
data class LyricLine(val timeMs: Long?, val text: String)

/**
 * [offsetMs]: how much later than their timestamps timed lines are sung in the version playing
 * (e.g. a music video with an intro before the song starts); added to every line's time.
 */
data class Lyrics(val lines: List<LyricLine>, val synced: Boolean, val source: String, val offsetMs: Long = 0)

/**
 * [lyrics] is shown first; for songs in an Indian script, [englishLetters] is the same song written in
 * English letters (the source's own version, or converted on the phone), for people who can't read the script.
 */
/** A song's album on YouTube Music: [playlistId] opens in the album screen. */
data class SongAlbum(val playlistId: String, val title: String, val thumbnail: String)

data class SongInfo(val details: SongDetails, val lyrics: Lyrics?, val englishLetters: Lyrics? = null)

/**
 * Song details and lyrics for the player's Lyrics tab. Details come from YouTube Music (singers,
 * album = movie for film songs, year) and the video description already fetched for playback
 * (lyricist, composer). Lyrics: timed ones from LRCLIB when they match the song's length, otherwise
 * YouTube Music's (Musixmatch / LyricFind), otherwise LRCLIB's plain text. Loaded on demand, remembered.
 */
@Singleton
class SongInfoSource @Inject constructor(
    private val http: OkHttpClient,
    @dagger.hilt.android.qualifiers.ApplicationContext private val context: android.content.Context
) {

    private val json = Moshi.Builder().build().adapter(Any::class.java)
    private val cache = Collections.synchronizedMap(object : LinkedHashMap<String, SongInfo>() {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, SongInfo>?) = size > 60
    })

    suspend fun load(videoId: String, fallbackTitle: String, fallbackArtist: String, durationMs: Long): SongInfo =
        cache[videoId] ?: withContext(Dispatchers.IO) {
            // Downloaded songs keep their lyrics on the phone: offline first
            OfflineLyrics.load(context, videoId)?.let { saved ->
                cache[videoId] = saved
                return@withContext saved
            }
            coroutineScope {
                val music = async { runCatching { musicTrack(videoId) }.getOrNull() }
                val track = music.await()
                val title = track?.title?.ifBlank { null } ?: fallbackTitle
                val singers = track?.artists?.ifBlank { null } ?: fallbackArtist
                val credits = YouTubeStreams.peek(videoId)?.description?.content?.let(::creditsFrom) ?: Credits()

                val ytmLyrics = async { track?.lyricsBrowseId?.let { runCatching { musicLyrics(it) }.getOrNull() } }
                val lrcLyrics = async { runCatching { lrclib(title, singers, durationMs) }.getOrNull() }
                val lrc = lrcLyrics.await()
                val ytm = ytmLyrics.await()
                val (lyrics, englishLetters) = versions(lrc, ytm, Romanizer::isLatin, Romanizer::romanize)

                SongInfo(
                    details = SongDetails(
                        title = title,
                        singers = singers,
                        movie = movieFrom(track?.album.orEmpty()),
                        lyricist = credits.lyricist,
                        composer = credits.composer,
                        year = track?.year.orEmpty()
                    ),
                    lyrics = lyrics,
                    englishLetters = englishLetters
                ).also { info ->
                    if (track != null || lyrics != null) {
                        cache[videoId] = info
                        // Downloaded inside the app (also songs from before lyrics were saved): keep them for offline
                        if (com.ramzmania.tubefy.core.download.Downloads.isDownloaded(context, videoId)) {
                            runCatching { OfflineLyrics.save(context, videoId, info) }
                        }
                    }
                }
            }
        }

    // --- YouTube Music ---

    private class MusicTrack(
        val title: String, val artists: String, val album: String, val year: String, val lyricsBrowseId: String?,
        val albumBrowseId: String? = null
    )

    private val albums = Collections.synchronizedMap(HashMap<String, SongAlbum>())

    /** The YouTube Music album the song belongs to (for the player's "Go to album"), or null. Remembered. */
    suspend fun album(videoId: String): SongAlbum? {
        albums[videoId]?.let { return it }
        return withContext(Dispatchers.IO) {
            val result = runCatching {
                val track = musicTrack(videoId) ?: return@runCatching null
                val browseId = track.albumBrowseId ?: return@runCatching null
                val page = music("browse", "\"browseId\":\"$browseId\"") ?: return@runCatching null
                // The album page names its playlist (OLAK5uy_…), which the album screen opens
                val playlistId = find(page, "playlistId").map { it.toString() }.firstOrNull { it.startsWith("OLAK5uy_") }
                    ?: return@runCatching null
                // The cover in the album's header (the page's first thumbnails can be another album's, in "More from")
                val header = (find(page, "musicResponsiveHeaderRenderer").firstOrNull()
                    ?: find(page, "musicDetailHeaderRenderer").firstOrNull()) as? Map<*, *>
                val thumbnail = (find(header?.get("thumbnail"), "thumbnails").firstOrNull() as? List<*>)
                    ?.filterIsInstance<Map<*, *>>()?.lastOrNull()?.get("url") as? String
                SongAlbum(playlistId, track.album, thumbnail.orEmpty())
            }
            // Only found albums are remembered, so a network hiccup can be retried
            result.getOrNull()?.also { albums[videoId] = it }
        }
    }

    private fun musicTrack(videoId: String): MusicTrack? {
        val next = music("next", "\"videoId\":\"$videoId\",\"isAudioOnly\":true") ?: return null
        val panel = find(next, "playlistPanelVideoRenderer").firstOrNull() as? Map<*, *>
        val title = runs(panel?.get("title"))
        val artists = mutableListOf<String>()
        var album = ""
        var albumBrowseId: String? = null
        var year = ""
        // "Vijay Yesudas • Premam • 2015": each part is tagged as artist / album by its link
        runList(panel?.get("longBylineText")).forEach { run ->
            val text = (run["text"] as? String).orEmpty().trim()
            val pageType = find(run["navigationEndpoint"], "pageType").firstOrNull() as? String
            when {
                text.isEmpty() || text == "•" || text == "," || text == "&" -> Unit
                pageType == "MUSIC_PAGE_TYPE_ARTIST" -> artists += text
                pageType == "MUSIC_PAGE_TYPE_ALBUM" -> {
                    album = text
                    albumBrowseId = find(run["navigationEndpoint"], "browseId").firstOrNull() as? String
                }
                Regex("^(19|20)\\d{2}$").matches(text) -> year = text
            }
        }
        val lyricsBrowseId = find(next, "browseId").map { it.toString() }.firstOrNull { it.startsWith("MPLY") }
        return MusicTrack(title, artists.joinToString(", "), album, year, lyricsBrowseId, albumBrowseId)
    }

    private fun musicLyrics(browseId: String): Lyrics? {
        val page = music("browse", "\"browseId\":\"$browseId\"") ?: return null
        val shelf = find(page, "musicDescriptionShelfRenderer").firstOrNull() as? Map<*, *> ?: return null
        val text = runs(shelf["description"]).trim()
        if (text.isEmpty()) return null
        val source = runs(shelf["footer"]).removePrefix("Source:").trim()
        return Lyrics(text.lines().map { LyricLine(null, it.trim()) }, synced = false, source = source)
    }

    private fun music(endpoint: String, fields: String): Any? {
        val body = """{"context":{"client":{"clientName":"WEB_REMIX","clientVersion":"$CLIENT_VERSION","hl":"en","gl":"IN"}},$fields}"""
        val request = Request.Builder()
            .url("https://music.youtube.com/youtubei/v1/$endpoint?prettyPrint=false")
            .header("Origin", "https://music.youtube.com")
            .post(body.toRequestBody(JSON))
            .build()
        return try {
            http.newCall(request).execute().use { response ->
                if (response.code == 429) YouTubeAccess.check(org.schabi.newpipe.extractor.exceptions.ReCaptchaException("rate limited", request.url.toString()))
                if (!response.isSuccessful) null else json.fromJson(response.body?.string().orEmpty())
            }
        } catch (e: Exception) {
            YouTubeAccess.check(e)
            throw e
        }
    }

    // --- LRCLIB ---

    private fun lrclib(title: String, artist: String, durationMs: Long): Lyrics? {
        val url = "https://lrclib.net/api/search".toHttpUrl().newBuilder()
            .addQueryParameter("track_name", cleanTitle(title))
            .addQueryParameter("artist_name", artist.substringBefore(",").trim())
            .build()
        val request = Request.Builder().url(url).header("User-Agent", "TubeFy (https://github.com/rameshvoltella/TubeFy)").build()
        val results = http.newCall(request).execute().use { if (it.isSuccessful) json.fromJson(it.body?.string().orEmpty()) as? List<*> else null }
            ?.filterIsInstance<Map<*, *>>().orEmpty()
        // The same song: a recording as long as the one playing, or shorter by up to an intro's length
        // (music videos often start with a scene / logo before the song); closest length first
        val matching = results
            .mapNotNull { result ->
                val trackMs = (result["duration"] as? Number)?.toDouble()?.times(1000)?.toLong()
                val offset = introOffset(trackMs, durationMs) ?: return@mapNotNull null
                result to offset
            }
            .sortedBy { (_, offset) -> offset }
        matching.firstNotNullOfOrNull { (result, offset) ->
            (result["syncedLyrics"] as? String)?.takeIf(String::isNotBlank)?.let { it to offset }
        }?.let { (lrc, offset) ->
            parseLrc(lrc).takeIf { it.isNotEmpty() }?.let { return Lyrics(it, synced = true, source = "LRCLIB", offsetMs = offset) }
        }
        return matching.firstNotNullOfOrNull { (result, _) -> (result["plainLyrics"] as? String)?.takeIf(String::isNotBlank) }?.let { plain ->
            Lyrics(plain.lines().map { LyricLine(null, it.trim()) }, synced = false, source = "LRCLIB")
        }
    }

    // --- JSON helpers ---

    /** Every value of [key] anywhere in the tree. */
    private fun find(node: Any?, key: String, out: MutableList<Any?> = mutableListOf()): MutableList<Any?> {
        when (node) {
            is Map<*, *> -> node.forEach { (k, v) -> if (k == key) out += v; find(v, key, out) }
            is List<*> -> node.forEach { find(it, key, out) }
        }
        return out
    }

    private fun runList(obj: Any?): List<Map<*, *>> =
        ((obj as? Map<*, *>)?.get("runs") as? List<*>)?.filterIsInstance<Map<*, *>>().orEmpty()

    private fun runs(obj: Any?): String = runList(obj).joinToString("") { (it["text"] as? String).orEmpty() }

    internal data class Credits(val lyricist: String = "", val composer: String = "")

    companion object {
        private const val CLIENT_VERSION = "1.20240729.01.00"
        private val JSON = "application/json".toMediaType()
        private const val DURATION_TOLERANCE_MS = 4_000L
        private const val MAX_INTRO_MS = 30_000L

        /**
         * How much later the song starts in the version playing than in the lyrics' recording, or null if
         * they aren't the same song. Same length (±4 s): no shift. Longer by up to 30 s: assumed to be an
         * intro (a music video's opening), so the lyrics move later by the difference. Unknown lengths: no shift.
         */
        internal fun introOffset(trackMs: Long?, playingMs: Long): Long? {
            if (playingMs <= 0 || trackMs == null || trackMs <= 0) return if (playingMs <= 0 || trackMs == null) 0L else null
            val extra = playingMs - trackMs
            return when {
                kotlin.math.abs(extra) <= DURATION_TOLERANCE_MS -> 0L
                extra in DURATION_TOLERANCE_MS..MAX_INTRO_MS -> extra
                else -> null
            }
        }

        /**
         * What to show: the lyrics in the song's own script first (timed if possible), and an English-letters
         * version. The English one keeps timing when it can: a timed English source, else the timed lyrics
         * converted, else the source's English text (reads most naturally), else a conversion.
         */
        internal fun versions(
            lrclib: Lyrics?,
            youtubeMusic: Lyrics?,
            isLatin: (Lyrics) -> Boolean,
            romanize: (Lyrics) -> Lyrics?
        ): Pair<Lyrics?, Lyrics?> {
            val all = listOfNotNull(lrclib, youtubeMusic)
            val native = all.filterNot(isLatin).maxByOrNull { if (it.synced) 1 else 0 }
                ?: return pickLyrics(lrclib, youtubeMusic) to null
            val latin = all.filter(isLatin).maxByOrNull { if (it.synced) 1 else 0 }
            val english = when {
                latin?.synced == true -> latin
                native.synced -> runCatching { romanize(native) }.getOrNull() ?: latin
                else -> latin ?: runCatching { romanize(native) }.getOrNull()
            }
            return native to english
        }

        /** Timed lyrics first; then YouTube Music's text; then any plain text. */
        internal fun pickLyrics(lrclib: Lyrics?, youtubeMusic: Lyrics?): Lyrics? = when {
            lrclib?.synced == true -> lrclib
            youtubeMusic != null -> youtubeMusic
            else -> lrclib
        }

        /** The film for a soundtrack album: 'Kesariya (From "Brahmastra")' → Brahmastra. */
        internal fun movieFrom(album: String): String {
            Regex("\\(From [\"“](.+?)[\"”]\\)", RegexOption.IGNORE_CASE).find(album)?.let { return it.groupValues[1].trim() }
            return album
                .replace(Regex("\\((Original )?(Motion Picture )?Soundtrack\\)|\\(OST\\)", RegexOption.IGNORE_CASE), "")
                .trim()
        }

        /** Lyricist and composer from a description ("Lyricist: …", "Music: …", "Composer: …"). */
        internal fun creditsFrom(description: String): Credits {
            var lyricist = ""
            var composer = ""
            // Descriptions come as HTML: lines are separated by <br>
            description.replace(Regex("(?i)<br\\s*/?>"), "\n").lines().forEach { raw ->
                val line = raw.trim().replace(Regex("<[^>]+>"), "")
                val match = Regex("^([A-Za-z ]{4,30})\\s*[:\\-–]\\s*(.{2,80})$").find(line) ?: return@forEach
                val key = match.groupValues[1].trim().lowercase()
                val value = match.groupValues[2].trim()
                when {
                    lyricist.isEmpty() && key in LYRICIST_KEYS -> lyricist = value
                    composer.isEmpty() && key in COMPOSER_KEYS -> composer = value
                }
            }
            return Credits(lyricist, composer)
        }

        private val LYRICIST_KEYS = setOf("lyricist", "lyrics", "lyrics by", "lyricist name", "written by", "writer", "song writer", "songwriter", "lyrics writer")
        private val COMPOSER_KEYS = setOf("composer", "music", "music by", "music director", "music composed by", "composed by", "music composer")

        /** "[01:23.45] line" → timed lines, in order. */
        internal fun parseLrc(lrc: String): List<LyricLine> = lrc.lines().flatMap { line ->
            val stamps = Regex("\\[(\\d{1,2}):(\\d{2})(?:[.:](\\d{1,3}))?]").findAll(line).toList()
            if (stamps.isEmpty()) return@flatMap emptyList()
            val text = line.substring(stamps.last().range.last + 1).trim()
            stamps.map { stamp ->
                val (m, s, fraction) = stamp.destructured
                val ms = fraction.padEnd(3, '0').take(3).toLongOrNull() ?: 0L
                LyricLine((m.toLong() * 60 + s.toLong()) * 1000 + ms, text)
            }
        }.sortedBy { it.timeMs }

        private fun cleanTitle(title: String): String =
            title.replace(Regex("\\s*\\((From|feat\\.|ft\\.).*?\\)", RegexOption.IGNORE_CASE), "").trim()
    }
}
