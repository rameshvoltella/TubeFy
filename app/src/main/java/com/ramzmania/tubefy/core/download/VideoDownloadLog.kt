package com.ramzmania.tubefy.core.download

import android.content.Context
import com.squareup.moshi.Moshi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File

/** One download from the YouTube video page, as listed in Settings → Downloads. */
data class VideoDownloadEntry(
    /** The WorkManager request doing (or that did) the download: its progress while running. */
    val workId: String,
    val videoId: String,
    val title: String,
    /** "720p", "M4A". */
    val label: String,
    /** "MP4", "128 kbps · English". */
    val detail: String,
    val audioOnly: Boolean,
    val status: Status = Status.Queued,
    /** The saved file (a document in the chosen folder), once done. */
    val fileUri: String = "",
    val mimeType: String = "",
    val sizeBytes: Long = 0,
    val folderLabel: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    // To retry with the same choice
    val videoItag: Int = -1,
    val withSound: Boolean = false,
    val audioItag: Int = -1,
    val audioTrackId: String? = null
) {
    enum class Status { Queued, Done, Failed }

    fun option() = VideoDownloadOption(
        audioOnly = audioOnly, label = label, detail = detail, sizeBytes = sizeBytes,
        videoItag = videoItag, withSound = withSound, audioItag = audioItag, audioTrackId = audioTrackId
    )
}

/** The list behind Settings → Downloads, newest first; stored in the app's private files. */
object VideoDownloadLog {

    private const val FILE = "video_downloads.json"
    private val json = Moshi.Builder().build().adapter(Any::class.java)
    private val _entries = MutableStateFlow<List<VideoDownloadEntry>?>(null)

    fun entries(context: Context): StateFlow<List<VideoDownloadEntry>?> {
        if (_entries.value == null) synchronized(this) { if (_entries.value == null) _entries.value = load(context) }
        return _entries.asStateFlow()
    }

    @Synchronized
    fun add(context: Context, entry: VideoDownloadEntry) =
        save(context, listOf(entry) + current(context).filterNot { it.workId == entry.workId })

    @Synchronized
    fun update(context: Context, workId: String, change: (VideoDownloadEntry) -> VideoDownloadEntry) =
        save(context, current(context).map { if (it.workId == workId) change(it) else it })

    @Synchronized
    fun remove(context: Context, workId: String) = save(context, current(context).filterNot { it.workId == workId })

    private fun current(context: Context) = entries(context).value.orEmpty()

    private fun save(context: Context, entries: List<VideoDownloadEntry>) {
        _entries.value = entries
        val file = File(context.filesDir, FILE)
        val temp = File(context.filesDir, "$FILE.tmp")
        temp.writeText(json.toJson(entries.map(::toMap)))
        temp.renameTo(file)
    }

    private fun load(context: Context): List<VideoDownloadEntry> = runCatching {
        (json.fromJson(File(context.filesDir, FILE).readText()) as? List<*>).orEmpty()
            .filterIsInstance<Map<*, *>>().map(::fromMap)
    }.getOrDefault(emptyList())

    internal fun toMap(e: VideoDownloadEntry): Map<String, Any?> = mapOf(
        "workId" to e.workId, "videoId" to e.videoId, "title" to e.title, "label" to e.label, "detail" to e.detail,
        "audioOnly" to e.audioOnly, "status" to e.status.name, "fileUri" to e.fileUri, "mimeType" to e.mimeType,
        "sizeBytes" to e.sizeBytes, "folderLabel" to e.folderLabel, "createdAt" to e.createdAt,
        "videoItag" to e.videoItag, "withSound" to e.withSound, "audioItag" to e.audioItag, "audioTrackId" to e.audioTrackId
    )

    internal fun fromMap(m: Map<*, *>): VideoDownloadEntry {
        fun text(key: String) = m[key] as? String ?: ""
        fun number(key: String) = (m[key] as? Number)?.toLong()
        return VideoDownloadEntry(
            workId = text("workId"), videoId = text("videoId"), title = text("title"), label = text("label"),
            detail = text("detail"), audioOnly = m["audioOnly"] as? Boolean ?: false,
            status = VideoDownloadEntry.Status.entries.firstOrNull { it.name == text("status") } ?: VideoDownloadEntry.Status.Failed,
            fileUri = text("fileUri"), mimeType = text("mimeType"), sizeBytes = number("sizeBytes") ?: 0,
            folderLabel = text("folderLabel"), createdAt = number("createdAt") ?: 0,
            videoItag = number("videoItag")?.toInt() ?: -1, withSound = m["withSound"] as? Boolean ?: false,
            audioItag = number("audioItag")?.toInt() ?: -1, audioTrackId = m["audioTrackId"] as? String
        )
    }
}
