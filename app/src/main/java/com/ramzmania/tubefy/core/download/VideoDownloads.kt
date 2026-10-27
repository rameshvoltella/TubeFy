package com.ramzmania.tubefy.core.download

import android.content.Context
import android.net.Uri
import android.os.Handler
import android.os.HandlerThread
import android.provider.DocumentsContract
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.transformer.Composition
import androidx.media3.transformer.EditedMediaItem
import androidx.media3.transformer.EditedMediaItemSequence
import androidx.media3.transformer.ExportException
import androidx.media3.transformer.ExportResult
import androidx.media3.transformer.ProgressHolder
import androidx.media3.transformer.Transformer
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.ramzmania.tubefy.core.youtube.YouTubeAccess
import com.ramzmania.tubefy.core.youtube.YouTubeStreams
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import org.schabi.newpipe.extractor.MediaFormat
import org.schabi.newpipe.extractor.stream.AudioStream
import org.schabi.newpipe.extractor.stream.AudioTrackType
import org.schabi.newpipe.extractor.stream.DeliveryMethod
import org.schabi.newpipe.extractor.stream.StreamInfo
import org.schabi.newpipe.extractor.stream.VideoStream
import java.io.File
import java.io.IOException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/** One choice in the video page's Download sheet. */
data class VideoDownloadOption(
    val audioOnly: Boolean,
    /** "1080p60", "M4A". */
    val label: String,
    /** "MP4", "128 kbps · English (original)". */
    val detail: String,
    /** Estimated file size; 0 when unknown. */
    val sizeBytes: Long,
    val videoItag: Int = -1,
    /** True for videos that come with their sound already (no joining needed). */
    val withSound: Boolean = false,
    val audioItag: Int = -1,
    val audioTrackId: String? = null
)

/**
 * Downloads from the YouTube video page into the phone-storage folder: any video quality or audio
 * format, like NewPipe. HD video comes without sound on YouTube, so its video and audio are
 * downloaded separately and joined into one MP4 on the phone (without re-encoding).
 */
object VideoDownloads {

    fun workName(videoId: String) = "video-download-${Downloads.plainId(videoId)}"

    /** Video-page downloads go into this subfolder of the chosen folder (songs stay in the folder itself). */
    const val SUBFOLDER = "YouTube"

    /** "Movies/TubeFy/YouTube". */
    fun folderLabel(folder: Uri) = "${PhoneDownloads.folderLabel(folder)}/$SUBFOLDER"

    /**
     * The "YouTube" subfolder of [folder] as a document, created if it isn't there yet. Synchronized,
     * so two downloads starting together don't create it twice. Blocking.
     */
    @Synchronized
    fun youTubeFolder(context: Context, folder: Uri): Uri {
        val resolver = context.contentResolver
        val rootId = DocumentsContract.getTreeDocumentId(folder)
        resolver.query(
            DocumentsContract.buildChildDocumentsUriUsingTree(folder, rootId),
            arrayOf(
                DocumentsContract.Document.COLUMN_DOCUMENT_ID,
                DocumentsContract.Document.COLUMN_DISPLAY_NAME,
                DocumentsContract.Document.COLUMN_MIME_TYPE
            ),
            null, null, null
        )?.use { cursor ->
            while (cursor.moveToNext()) {
                if (cursor.getString(1) == SUBFOLDER && cursor.getString(2) == DocumentsContract.Document.MIME_TYPE_DIR) {
                    return DocumentsContract.buildDocumentUriUsingTree(folder, cursor.getString(0))
                }
            }
        }
        return DocumentsContract.createDocument(
            resolver,
            DocumentsContract.buildDocumentUriUsingTree(folder, rootId),
            DocumentsContract.Document.MIME_TYPE_DIR,
            SUBFOLDER
        ) ?: throw IOException("Couldn't create the $SUBFOLDER folder")
    }

    /** Every video quality (best first) and every audio format for the Download sheet. */
    fun options(info: StreamInfo): Pair<List<VideoDownloadOption>, List<VideoDownloadOption>> {
        val seconds = info.duration.coerceAtLeast(0)
        val audios = info.audioStreams.orEmpty().filter { usable(it.deliveryMethod, it.content) }
        val soundtrack = bestAudio(audios)
        val soundSize = soundtrack?.let { size(it.itagItem?.contentLength, it.averageBitrate * 1000L, seconds) } ?: 0L

        val videoOnly = info.videoOnlyStreams.orEmpty().filter { usable(it.deliveryMethod, it.content) && it.height > 0 }
            .map { stream ->
                VideoDownloadOption(
                    audioOnly = false,
                    label = stream.resolution.ifBlank { "${stream.height}p" },
                    detail = formatName(stream.format),
                    sizeBytes = size(stream.itagItem?.contentLength, stream.bitrate.toLong(), seconds).let { if (it > 0) it + soundSize else 0 },
                    videoItag = stream.itag
                )
            }
        val muxed = info.videoStreams.orEmpty().filter { usable(it.deliveryMethod, it.content) && it.height > 0 }
            .map { stream ->
                VideoDownloadOption(
                    audioOnly = false,
                    label = stream.resolution.ifBlank { "${stream.height}p" },
                    detail = formatName(stream.format),
                    sizeBytes = size(stream.itagItem?.contentLength, stream.bitrate.toLong(), seconds),
                    videoItag = stream.itag,
                    withSound = true
                )
            }
        // HD needs a soundtrack to join with; same quality + format offered once (the ready-made one first)
        val video = (muxed + videoOnly.takeIf { soundtrack != null }.orEmpty())
            .distinctBy { it.label + it.detail }
            .sortedWith(compareByDescending<VideoDownloadOption> { heightOf(it.label) }.thenBy { if (it.detail == "MP4") 0 else 1 })

        val tracks = audios.mapNotNull { it.audioTrackName }.distinct()
        fun isOriginal(stream: AudioStream) = stream.audioTrackType == null || stream.audioTrackType == AudioTrackType.ORIGINAL
        // Every format of the original sound; for dubbed languages just the best M4A (else the list is huge)
        val dubbed = audios.filterNot(::isOriginal).groupBy { it.audioTrackId }.values.mapNotNull { bestAudio(it) ?: it.maxByOrNull { s -> s.averageBitrate } }
        val audio = (audios.filter(::isOriginal) + dubbed)
            .sortedWith(compareByDescending<AudioStream> { it.audioTrackType == null || it.audioTrackType == AudioTrackType.ORIGINAL }
                .thenBy { it.audioTrackName.orEmpty() }
                .thenBy { if (it.format == MediaFormat.M4A) 0 else 1 }
                .thenByDescending { it.averageBitrate })
            .map { stream ->
                VideoDownloadOption(
                    audioOnly = true,
                    label = formatName(stream.format),
                    detail = listOfNotNull(
                        stream.averageBitrate.takeIf { it > 0 }?.let { "$it kbps" },
                        stream.audioTrackName?.takeIf { tracks.size > 1 }
                    ).joinToString(" · "),
                    sizeBytes = size(stream.itagItem?.contentLength, stream.averageBitrate * 1000L, seconds),
                    audioItag = stream.itag,
                    audioTrackId = stream.audioTrackId
                )
            }
            .distinctBy { it.label + it.detail }
        return video to audio
    }

    const val WORK_TAG = "tubefy-video-download"

    /** [retryOf]: the Downloads-list entry being retried (it's updated instead of listed again). */
    fun enqueue(context: Context, videoId: String, title: String, option: VideoDownloadOption, retryOf: String? = null) {
        val folder = PhoneDownloads.folder(context) ?: return
        com.ramzmania.tubefy.core.analytics.Analytics.downloadStart(if (option.audioOnly) "video_audio" else "video", option.label)
        val request = OneTimeWorkRequestBuilder<VideoDownloadWorker>()
            .addTag(WORK_TAG)
            .setInputData(
                workDataOf(
                    VideoDownloadWorker.KEY_VIDEO_ID to Downloads.plainId(videoId),
                    VideoDownloadWorker.KEY_TITLE to title,
                    VideoDownloadWorker.KEY_FOLDER to folder.toString(),
                    VideoDownloadWorker.KEY_AUDIO_ONLY to option.audioOnly,
                    VideoDownloadWorker.KEY_VIDEO_ITAG to option.videoItag,
                    VideoDownloadWorker.KEY_WITH_SOUND to option.withSound,
                    VideoDownloadWorker.KEY_AUDIO_ITAG to option.audioItag,
                    VideoDownloadWorker.KEY_AUDIO_TRACK to option.audioTrackId,
                    VideoDownloadWorker.KEY_LABEL to option.label
                )
            )
            .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
            .build()
        WorkManager.getInstance(context).enqueueUniqueWork(workName(videoId), ExistingWorkPolicy.REPLACE, request)
        // Settings → Downloads
        val entry = VideoDownloadEntry(
            workId = request.id.toString(), videoId = Downloads.plainId(videoId), title = title,
            label = option.label, detail = option.detail, audioOnly = option.audioOnly, sizeBytes = option.sizeBytes,
            videoItag = option.videoItag, withSound = option.withSound, audioItag = option.audioItag, audioTrackId = option.audioTrackId
        )
        if (retryOf != null) VideoDownloadLog.remove(context, retryOf)
        VideoDownloadLog.add(context, entry)
    }

    /** "12.4 MB". */
    fun sizeLabel(bytes: Long): String = when {
        bytes <= 0 -> ""
        bytes >= 1_000_000_000 -> String.format(java.util.Locale.US, "%.1f GB", bytes / 1e9)
        bytes >= 1_000_000 -> String.format(java.util.Locale.US, "%.1f MB", bytes / 1e6)
        else -> "${bytes / 1000} KB"
    }

    /** The soundtrack HD video is joined with: original-language AAC, best quality. */
    internal fun bestAudio(audios: List<AudioStream>): AudioStream? = audios
        .filter { it.format == MediaFormat.M4A }
        .sortedWith(compareByDescending<AudioStream> { it.audioTrackType == null || it.audioTrackType == AudioTrackType.ORIGINAL }
            .thenByDescending { it.averageBitrate })
        .firstOrNull()

    internal fun heightOf(label: String): Int = Regex("^(\\d+)p").find(label)?.groupValues?.get(1)?.toIntOrNull() ?: 0

    private fun usable(method: DeliveryMethod?, url: String?) = method == DeliveryMethod.PROGRESSIVE_HTTP && !url.isNullOrBlank()

    private fun size(contentLength: Long?, bitsPerSecond: Long, seconds: Long): Long =
        contentLength?.takeIf { it > 0 } ?: if (bitsPerSecond > 0 && seconds > 0) bitsPerSecond * seconds / 8 else 0L

    private fun formatName(format: MediaFormat?): String = when (format) {
        MediaFormat.MPEG_4 -> "MP4"
        MediaFormat.WEBM -> "WebM"
        MediaFormat.v3GPP -> "3GP"
        MediaFormat.M4A -> "M4A"
        MediaFormat.WEBMA, MediaFormat.WEBMA_OPUS -> "Opus"
        MediaFormat.MP3 -> "MP3"
        else -> format?.getName() ?: "Video"
    }
}

/** Downloads one Download-sheet choice into the phone-storage folder, joining HD video with its sound. */
@UnstableApi
class VideoDownloadWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val videoId = inputData.getString(KEY_VIDEO_ID) ?: return@withContext Result.failure()
        val title = inputData.getString(KEY_TITLE).orEmpty()
        val folder = inputData.getString(KEY_FOLDER)?.let(Uri::parse) ?: return@withContext Result.failure()
        val label = inputData.getString(KEY_LABEL).orEmpty()
        val workDir = File(applicationContext.cacheDir, "video-download-$videoId").apply { mkdirs() }
        var created: Uri? = null
        try {
            // Links from a lookup made just now (older ones may have expired)
            val info = YouTubeStreams.info(videoId, fresh = runAttemptCount > 0)
            val name = PhoneDownloads.fileName(title, "")
            if (inputData.getBoolean(KEY_AUDIO_ONLY, false)) {
                val audio = findAudio(info) ?: throw IOException("Audio format not available")
                val (mime, extension) = if (audio.format == MediaFormat.M4A) "audio/mp4" to "m4a" else "audio/webm" to "webm"
                created = createFile(folder, mime, "$name.$extension")
                write(created) { output -> downloadInChunks(audio.content, output) { progress(it) } }
            } else {
                val video = (info.videoStreams.orEmpty() + info.videoOnlyStreams.orEmpty())
                    .firstOrNull { it.itag == inputData.getInt(KEY_VIDEO_ITAG, -1) && it.isVideoOnly != inputData.getBoolean(KEY_WITH_SOUND, false) }
                    ?: throw IOException("Video quality not available")
                val fileName = "$name ($label).${if (video.format == MediaFormat.v3GPP) "3gp" else "mp4"}"
                if (!video.isVideoOnly) {
                    // Already has sound: a straight download
                    created = createFile(folder, video.format?.mimeType ?: "video/mp4", fileName)
                    write(created) { output -> downloadInChunks(video.content, output) { progress(it) } }
                } else {
                    val audio = VideoDownloads.bestAudio(info.audioStreams.orEmpty()) ?: throw IOException("No soundtrack")
                    val videoFile = File(workDir, "video")
                    val audioFile = File(workDir, "audio")
                    val joined = File(workDir, "joined.mp4")
                    // Rough shares of the progress bar: video 0–75 %, sound 75–90 %, joining 90–100 %
                    videoFile.outputStream().use { out -> downloadInChunks(video.content, out) { progress(it * 75 / 100) } }
                    audioFile.outputStream().use { out -> downloadInChunks(audio.content, out) { progress(75 + it * 15 / 100) } }
                    join(videoFile, audioFile, joined)
                    created = createFile(folder, "video/mp4", fileName)
                    write(created) { output -> joined.inputStream().use { it.copyTo(output) } }
                }
            }
            postSavedNotification(applicationContext, videoId, "Saved to ${VideoDownloads.folderLabel(folder)}", title)
            val file = created ?: throw IOException("No file")
            val (mime, size) = applicationContext.contentResolver.query(
                file, arrayOf(DocumentsContract.Document.COLUMN_MIME_TYPE, DocumentsContract.Document.COLUMN_SIZE), null, null, null
            )?.use { if (it.moveToFirst()) (it.getString(0).orEmpty() to it.getLong(1)) else null } ?: ("" to 0L)
            VideoDownloadLog.update(applicationContext, id.toString()) {
                it.copy(
                    status = VideoDownloadEntry.Status.Done, fileUri = file.toString(), mimeType = mime,
                    sizeBytes = if (size > 0) size else it.sizeBytes, folderLabel = VideoDownloads.folderLabel(folder)
                )
            }
            com.ramzmania.tubefy.core.analytics.Analytics.downloadResult("video", "done")
            Result.success()
        } catch (e: Exception) {
            YouTubeAccess.check(e)
            created?.let { runCatching { DocumentsContract.deleteDocument(applicationContext.contentResolver, it) } }
            if (runAttemptCount < MAX_ATTEMPTS && e !is SecurityException) Result.retry()
            else {
                postSavedNotification(applicationContext, videoId, "Couldn't download the video", title)
                VideoDownloadLog.update(applicationContext, id.toString()) { it.copy(status = VideoDownloadEntry.Status.Failed) }
                com.ramzmania.tubefy.core.analytics.Analytics.downloadResult("video", "failed")
                Result.failure()
            }
        } finally {
            workDir.deleteRecursively()
        }
    }

    private fun findAudio(info: StreamInfo): AudioStream? {
        val itag = inputData.getInt(KEY_AUDIO_ITAG, -1)
        val track = inputData.getString(KEY_AUDIO_TRACK)
        return info.audioStreams.orEmpty().firstOrNull { it.itag == itag && it.audioTrackId == track }
            ?: info.audioStreams.orEmpty().firstOrNull { it.itag == itag }
    }

    private suspend fun progress(percent: Int) = setProgress(workDataOf(KEY_PROGRESS to percent.coerceIn(0, 100)))

    /** Creates the file in the "YouTube" subfolder of the chosen folder (made the first time). */
    private fun createFile(folder: Uri, mime: String, name: String): Uri {
        val parent = VideoDownloads.youTubeFolder(applicationContext, folder)
        return DocumentsContract.createDocument(applicationContext.contentResolver, parent, mime, name)
            ?: throw IOException("Couldn't create the file")
    }

    private inline fun write(target: Uri, block: (java.io.OutputStream) -> Unit) {
        applicationContext.contentResolver.openOutputStream(target)?.use(block) ?: throw IOException("Couldn't write the file")
    }

    /** Joins the video and sound into one MP4, copying both as they are (no re-encoding). */
    private suspend fun join(video: File, audio: File, output: File) {
        val thread = HandlerThread("video-join").apply { start() }
        val handler = Handler(thread.looper)
        try {
            var transformer: Transformer? = null
            suspendCancellableCoroutine { continuation ->
                handler.post {
                    val composition = Composition.Builder(
                        EditedMediaItemSequence.Builder(
                            EditedMediaItem.Builder(MediaItem.fromUri(Uri.fromFile(video))).setRemoveAudio(true).build()
                        ).build(),
                        EditedMediaItemSequence.Builder(
                            EditedMediaItem.Builder(MediaItem.fromUri(Uri.fromFile(audio))).setRemoveVideo(true).build()
                        ).build()
                    ).setTransmuxVideo(true).setTransmuxAudio(true).build()
                    transformer = Transformer.Builder(applicationContext)
                        .setLooper(thread.looper)
                        .addListener(object : Transformer.Listener {
                            override fun onCompleted(composition: Composition, exportResult: ExportResult) {
                                if (continuation.isActive) continuation.resume(Unit)
                            }

                            override fun onError(composition: Composition, exportResult: ExportResult, exportException: ExportException) {
                                if (continuation.isActive) continuation.resumeWithException(exportException)
                            }
                        })
                        .build()
                        .also { it.start(composition, output.absolutePath) }
                }
                continuation.invokeOnCancellation { handler.post { transformer?.cancel() } }
            }
        } finally {
            thread.quitSafely()
        }
        progress(100)
        delay(50)
    }

    companion object {
        const val KEY_VIDEO_ID = "videoId"
        const val KEY_TITLE = "title"
        const val KEY_FOLDER = "folder"
        const val KEY_AUDIO_ONLY = "audioOnly"
        const val KEY_VIDEO_ITAG = "videoItag"
        const val KEY_WITH_SOUND = "withSound"
        const val KEY_AUDIO_ITAG = "audioItag"
        const val KEY_AUDIO_TRACK = "audioTrack"
        const val KEY_LABEL = "label"
        const val KEY_PROGRESS = "progress"
        private const val MAX_ATTEMPTS = 2
    }
}
