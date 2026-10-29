package com.ramzmania.tubefy.core.recognition

import android.content.Context
import android.net.Uri
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.ramzmania.tubefy.core.youtube.YouTubeAccess
import com.ramzmania.tubefy.data.Resource
import com.ramzmania.tubefy.data.remote.RemoteRepositorySource
import com.squareup.moshi.Moshi
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.io.File
import java.util.concurrent.TimeUnit

/** A song from the user's own Shazam history (imported from Shazam's CSV export), for Library → Shazam → Shazam. */
data class ImportedSong(
    val title: String,
    val artist: String,
    /** When it was last Shazamed (the CSV can list a song several times). */
    val shazamedAt: Long,
    val timesShazamed: Int,
    val status: Status = Status.Waiting,
    /** The YouTube Music match, once found. */
    val videoId: String? = null,
    val coverUrl: String? = null
) {
    enum class Status { Waiting, Found, NotFound }

    val key get() = keyOf(title, artist)

    companion object {
        fun keyOf(title: String, artist: String) = "${title.trim().lowercase()}|${artist.trim().lowercase()}"
    }
}

/**
 * Shazam history imported from its CSV export. Matching every song with YouTube Music at once would
 * fire hundreds of searches and get TubeFy blocked by YouTube, so matches are fetched slowly in the
 * background ([BATCH_SIZE] songs every 15 minutes, newest first, paused while YouTube limits us); a song
 * the user taps is fetched right away. Location columns in the export are ignored, never stored.
 */
object ShazamImport {

    const val BATCH_SIZE = 5
    const val BATCH_MINUTES = 15L
    private const val FILE = "shazam_import.json"
    private const val WORK = "shazam_import_sync"
    /** Gap between the searches of one batch, so they don't arrive as a burst. */
    private const val GAP_MS = 3_000L

    private val json = Moshi.Builder().build().adapter(Any::class.java)
    private val _songs = MutableStateFlow<List<ImportedSong>?>(null)
    private val _syncStopped = MutableStateFlow<Boolean?>(null)

    /** The user stopped the background sync (remembered); tapping a song or Play all still look songs up. */
    fun syncStopped(context: Context): StateFlow<Boolean?> {
        if (_syncStopped.value == null) _syncStopped.value = prefs(context).getBoolean(KEY_STOPPED, false)
        return _syncStopped.asStateFlow()
    }

    fun setSyncStopped(context: Context, stopped: Boolean) {
        _syncStopped.value = stopped
        prefs(context).edit().putBoolean(KEY_STOPPED, stopped).apply()
        if (stopped) WorkManager.getInstance(context).cancelUniqueWork(WORK) else startSync(context)
    }

    private fun prefs(context: Context) = context.getSharedPreferences("shazam_import", Context.MODE_PRIVATE)
    private const val KEY_STOPPED = "sync_stopped"

    fun find(context: Context, key: String): ImportedSong? = current(context).firstOrNull { it.key == key }

    fun songs(context: Context): StateFlow<List<ImportedSong>?> {
        if (_songs.value == null) synchronized(this) { if (_songs.value == null) _songs.value = load(context) }
        return _songs.asStateFlow()
    }

    class ImportResult(val rows: Int, val songs: Int, val alreadyFound: Int)

    /**
     * Reads a Shazam CSV export, merges repeated songs (newest date, count), keeps matches found in an
     * earlier import, and starts the slow sync. Throws [IllegalArgumentException] for a non-Shazam file.
     */
    suspend fun import(context: Context, uri: Uri): ImportResult = withContext(Dispatchers.IO) {
        val text = context.contentResolver.openInputStream(uri)?.use { it.readBytes().toString(Charsets.UTF_8) }
            ?: throw IllegalArgumentException("Couldn't read the file")
        val rows = parseCsv(text.removePrefix("﻿"))
        val header = rows.firstOrNull()?.map { it.trim().lowercase() } ?: throw IllegalArgumentException("The file is empty")
        val titleAt = header.indexOf("title")
        val artistAt = header.indexOf("artist")
        val dateAt = header.indexOf("date")
        if (titleAt < 0 || artistAt < 0) throw IllegalArgumentException("This isn't a Shazam export (no title / artist columns)")

        val entries = rows.drop(1).mapNotNull { row ->
            val title = row.getOrNull(titleAt)?.trim().orEmpty()
            if (title.isEmpty()) return@mapNotNull null
            Triple(title, row.getOrNull(artistAt)?.trim().orEmpty(), parseDate(row.getOrNull(dateAt)))
        }
        val previous = current(context).associateBy { it.key }
        val merged = entries.groupBy { ImportedSong.keyOf(it.first, it.second) }.map { (key, group) ->
            val newest = group.maxBy { it.third }
            val earlier = previous[key]
            ImportedSong(
                title = newest.first,
                artist = newest.second,
                shazamedAt = newest.third,
                timesShazamed = group.size,
                status = earlier?.status ?: ImportedSong.Status.Waiting,
                videoId = earlier?.videoId,
                coverUrl = earlier?.coverUrl
            )
        }.sortedByDescending { it.shazamedAt }
        save(context, merged)
        startSync(context)
        ImportResult(rows = entries.size, songs = merged.size, alreadyFound = merged.count { it.status == ImportedSong.Status.Found })
    }

    /** The user tapped a song that isn't matched yet: look it up now (one search). */
    suspend fun fetchNow(context: Context, song: ImportedSong): ImportedSong = match(context, song).also { update(context, it) }

    @Synchronized
    fun remove(context: Context, song: ImportedSong) = save(context, current(context).filterNot { it.key == song.key })

    @Synchronized
    fun clear(context: Context) {
        save(context, emptyList())
        WorkManager.getInstance(context).cancelUniqueWork(WORK)
    }

    fun waitingCount(songs: List<ImportedSong>) = songs.count { it.status == ImportedSong.Status.Waiting }

    /** Roughly how long until every waiting song is matched, in minutes. */
    fun minutesLeft(waiting: Int) = ((waiting + BATCH_SIZE - 1) / BATCH_SIZE - 1).coerceAtLeast(0) * BATCH_MINUTES

    // --- Background sync ---

    private fun startSync(context: Context) {
        if (waitingCount(current(context)) == 0 || syncStopped(context).value == true) return
        val request = PeriodicWorkRequestBuilder<ShazamSyncWorker>(BATCH_MINUTES, TimeUnit.MINUTES)
            .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
            .build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(WORK, ExistingPeriodicWorkPolicy.KEEP, request)
    }

    /** One batch: the next [BATCH_SIZE] waiting songs, newest first. */
    internal suspend fun syncBatch(context: Context) {
        // YouTube is limiting us: skip this round rather than make it worse
        if (YouTubeAccess.recentlyBlocked) return
        if (syncStopped(context).value == true) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK)
            return
        }
        val batch = current(context).filter { it.status == ImportedSong.Status.Waiting }.take(BATCH_SIZE)
        if (batch.isEmpty()) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK)
            return
        }
        batch.forEachIndexed { index, song ->
            if (index > 0) delay(GAP_MS)
            update(context, match(context, song))
            if (YouTubeAccess.recentlyBlocked) return
        }
    }

    /** Finds the song on YouTube Music ("title artist", like Identify song does). Errors leave it Waiting. */
    private suspend fun match(context: Context, song: ImportedSong): ImportedSong {
        val remote = EntryPointAccessors.fromApplication(context.applicationContext, ShazamImportEntryPoint::class.java).remote()
        val query = listOf(song.title, song.artist.substringBefore(" & ").substringBefore(","))
            .filter { it.isNotBlank() }.joinToString(" ")
        val result = runCatching { remote.getNewPipePageSearch(0, query, listOf("music_songs"), "").first() }.getOrNull()
        val tracks = (result as? Resource.Success)?.data?.youtubeSortedData?.youtubeSortedList ?: return song
        val track = tracks.firstOrNull { it != null && it.videoId.isNotBlank() }
            ?: return song.copy(status = ImportedSong.Status.NotFound)
        return song.copy(
            status = ImportedSong.Status.Found,
            videoId = track.videoId,
            coverUrl = track.videoImage.takeIf { it.isNotBlank() } ?: "https://i.ytimg.com/vi/${track.videoId}/hq720.jpg"
        )
    }

    // --- Storage ---

    private fun current(context: Context) = songs(context).value.orEmpty()

    @Synchronized
    private fun update(context: Context, song: ImportedSong) =
        save(context, current(context).map { if (it.key == song.key) song else it })

    @Synchronized
    private fun save(context: Context, songs: List<ImportedSong>) {
        _songs.value = songs
        val temp = File(context.filesDir, "$FILE.tmp")
        temp.writeText(json.toJson(songs.map(::toMap)))
        temp.renameTo(File(context.filesDir, FILE))
    }

    private fun load(context: Context): List<ImportedSong> = runCatching {
        (json.fromJson(File(context.filesDir, FILE).readText()) as? List<*>).orEmpty().filterIsInstance<Map<*, *>>().map(::fromMap)
    }.getOrDefault(emptyList())

    private fun toMap(s: ImportedSong): Map<String, Any?> = mapOf(
        "title" to s.title, "artist" to s.artist, "shazamedAt" to s.shazamedAt, "times" to s.timesShazamed,
        "status" to s.status.name, "videoId" to s.videoId, "coverUrl" to s.coverUrl
    )

    private fun fromMap(m: Map<*, *>) = ImportedSong(
        title = m["title"] as? String ?: "",
        artist = m["artist"] as? String ?: "",
        shazamedAt = (m["shazamedAt"] as? Number)?.toLong() ?: 0L,
        timesShazamed = (m["times"] as? Number)?.toInt() ?: 1,
        status = runCatching { ImportedSong.Status.valueOf(m["status"] as String) }.getOrDefault(ImportedSong.Status.Waiting),
        videoId = m["videoId"] as? String,
        coverUrl = m["coverUrl"] as? String
    )

    // --- CSV ---

    private fun parseDate(value: String?): Long =
        value?.let { runCatching { java.time.Instant.parse(it.trim()).toEpochMilli() }.getOrNull() } ?: 0L

    /** RFC 4180 CSV: quoted fields, "" as an escaped quote, commas and line breaks inside quotes. */
    internal fun parseCsv(text: String): List<List<String>> {
        val rows = mutableListOf<List<String>>()
        var row = mutableListOf<String>()
        val field = StringBuilder()
        var quoted = false
        var i = 0
        while (i < text.length) {
            val c = text[i]
            when {
                quoted && c == '"' && text.getOrNull(i + 1) == '"' -> { field.append('"'); i++ }
                c == '"' -> quoted = !quoted
                !quoted && c == ',' -> { row.add(field.toString()); field.clear() }
                !quoted && (c == '\n' || c == '\r') -> {
                    if (c == '\r' && text.getOrNull(i + 1) == '\n') i++
                    row.add(field.toString()); field.clear()
                    if (row.any { it.isNotEmpty() }) rows.add(row)
                    row = mutableListOf()
                }
                else -> field.append(c)
            }
            i++
        }
        if (field.isNotEmpty() || row.isNotEmpty()) {
            row.add(field.toString())
            if (row.any { it.isNotEmpty() }) rows.add(row)
        }
        return rows
    }
}

@EntryPoint
@InstallIn(SingletonComponent::class)
interface ShazamImportEntryPoint {
    fun remote(): RemoteRepositorySource
}

/** Every 15 minutes: matches the next few imported Shazam songs with YouTube Music. */
class ShazamSyncWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        runCatching { ShazamImport.syncBatch(applicationContext) }
        return Result.success()
    }
}
