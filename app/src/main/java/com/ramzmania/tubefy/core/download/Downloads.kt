package com.ramzmania.tubefy.core.download

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.ramzmania.tubefy.core.YoutubeCoreConstant
import com.ramzmania.tubefy.database.CustomPlaylist
import com.ramzmania.tubefy.database.PlaylistDao
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * Offline downloads: songs are saved as `filesDir/downloaded/<videoId>.mp4` and listed in the
 * reserved [PLAYLIST] custom playlist (shown as "Downloads").
 */
object Downloads {
    const val PLAYLIST = "TubeFy-Downloads"

    /** MediaMetadata extra set on items played from local files. */
    const val OFFLINE_EXTRA = "com.ramzmania.tubefy.OFFLINE"

    /** The YouTube tab's "Save" button fills this playlist; its songs open in the video page. */
    const val YOUTUBE_VIDEOS = "YouTube Videos"

    private val RESERVED_NAMES = setOf("download", "downloads", PLAYLIST.lowercase(), YOUTUBE_VIDEOS.lowercase())

    /** Names users can't give their own playlists (Downloads and the YouTube "Save" list). */
    fun isReservedName(name: String) = name.trim().lowercase() in RESERVED_NAMES

    fun directory(context: Context) = File(context.filesDir, "downloaded").apply { mkdirs() }

    fun file(context: Context, videoId: String) =
        File(directory(context), "${plainId(videoId)}.mp4")

    fun isDownloaded(context: Context, videoId: String) =
        file(context, videoId).let { it.exists() && it.length() > 0 }

    /** Deletes the song's file (and any half-finished download) and cancels a pending download. */
    fun delete(context: Context, videoId: String) {
        WorkManager.getInstance(context).cancelUniqueWork(workName(videoId))
        val target = file(context, videoId)
        File(target.parentFile, "${target.name}.part").delete()
        target.delete()
    }

    fun deleteAll(context: Context) {
        WorkManager.getInstance(context).cancelAllWorkByTag(WORK_TAG)
        directory(context).listFiles()?.forEach { it.delete() }
    }

    private const val WORK_TAG = "tubefy-download"

    fun workName(videoId: String) = "download-${plainId(videoId)}"

    fun plainId(videoId: String) = YoutubeCoreConstant.extractYoutubeVideoId(videoId) ?: videoId

    fun enqueue(context: Context, videoId: String, title: String, thumbnail: String) {
        val request = OneTimeWorkRequestBuilder<DownloadWorker>()
            .addTag(WORK_TAG)
            .setInputData(
                workDataOf(
                    DownloadWorker.KEY_VIDEO_ID to plainId(videoId),
                    DownloadWorker.KEY_TITLE to title,
                    DownloadWorker.KEY_THUMBNAIL to thumbnail
                )
            )
            .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
            .build()
        WorkManager.getInstance(context)
            .enqueueUniqueWork(workName(videoId), ExistingWorkPolicy.KEEP, request)
    }
}

@EntryPoint
@InstallIn(SingletonComponent::class)
interface DownloadEntryPoint {
    fun playlistDao(): PlaylistDao
    fun songInfoSource(): com.ramzmania.tubefy.core.lyrics.SongInfoSource
}

/**
 * Resolves a fresh progressive (audio + video) MP4 stream for the song, downloads it in byte-range
 * chunks (YouTube throttles single long requests), then adds it to the Downloads playlist.
 */
class DownloadWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val videoId = inputData.getString(KEY_VIDEO_ID) ?: return@withContext Result.failure()
        val title = inputData.getString(KEY_TITLE).orEmpty()
        val thumbnail = inputData.getString(KEY_THUMBNAIL).orEmpty()
        val target = Downloads.file(applicationContext, videoId)
        val partial = File(target.parentFile, "${target.name}.part")

        try {
            if (!Downloads.isDownloaded(applicationContext, videoId)) {
                val url = resolveStreamUrl(videoId)
                partial.delete()
                partial.outputStream().use { output ->
                    downloadInChunks(url, output) { setProgress(workDataOf(KEY_PROGRESS to it)) }
                }
                if (!partial.renameTo(target)) throw IOException("Could not save download")
            }
            EntryPointAccessors.fromApplication(applicationContext, DownloadEntryPoint::class.java)
                .playlistDao()
                .insertCustomPlaylistEntryIfNotExists(
                    CustomPlaylist(
                        playlistName = Downloads.PLAYLIST,
                        videoId = videoId,
                        videoThump = thumbnail,
                        videoName = title
                    )
                )
            Result.success()
        } catch (e: Exception) {
            partial.delete()
            if (runAttemptCount < MAX_ATTEMPTS) Result.retry() else Result.failure()
        }
    }

    private fun resolveStreamUrl(videoId: String): String =
        com.ramzmania.tubefy.core.youtube.YouTubeStreams.info(videoId).videoStreams.orEmpty().firstOrNull()?.content
            ?: throw IOException("No downloadable stream for $videoId")

    companion object {
        const val KEY_VIDEO_ID = "videoId"
        const val KEY_TITLE = "title"
        const val KEY_THUMBNAIL = "thumbnail"
        const val KEY_PROGRESS = "progress"
        private const val MAX_ATTEMPTS = 3
    }
}

private const val CHUNK_SIZE = 4L * 1024 * 1024

private val downloadClient = OkHttpClient.Builder()
    .connectTimeout(20, TimeUnit.SECONDS)
    .readTimeout(60, TimeUnit.SECONDS)
    .build()

/**
 * Downloads [url] into [output] in byte-range chunks (YouTube throttles single long requests),
 * reporting progress as a percentage.
 */
internal suspend fun downloadInChunks(url: String, output: java.io.OutputStream, onProgress: suspend (Int) -> Unit) {
    var start = 0L
    var total = -1L
    while (total < 0 || start < total) {
        val request = Request.Builder()
            .url(url)
            .header("Range", "bytes=$start-${start + CHUNK_SIZE - 1}")
            .build()
        downloadClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw IOException("HTTP ${response.code}")
            val bytes = response.body?.bytes() ?: throw IOException("Empty response")
            if (total < 0) {
                total = response.header("Content-Range")?.substringAfter('/')?.toLongOrNull()
                    ?: bytes.size.toLong()
            }
            if (bytes.isEmpty()) throw IOException("Download stalled")
            output.write(bytes)
            start += bytes.size
        }
        onProgress((start * 100 / total).toInt())
    }
}
