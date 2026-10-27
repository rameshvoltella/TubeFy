package com.ramzmania.tubefy.core.download

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.DocumentsContract
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.ramzmania.tubefy.R
import com.ramzmania.tubefy.core.youtube.YouTubeStreams
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.schabi.newpipe.extractor.MediaFormat
import java.io.IOException

/**
 * "Save to phone storage": the song's audio only, as a file in a folder the user picked with Android's
 * folder picker (so any music app can play it). Separate from the in-app [Downloads].
 */
object PhoneDownloads {

    private const val PREFS = "phone_downloads"
    private const val KEY_FOLDER = "folder"
    private const val KEY_LYRICS = "songbook_lyrics"
    private const val WORK_TAG = "tubefy-phone-download"

    fun workName(videoId: String) = "phone-download-${Downloads.plainId(videoId)}"

    /** The chosen folder, if it's still accessible. */
    fun folder(context: Context): Uri? {
        val saved = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY_FOLDER, null) ?: return null
        val uri = Uri.parse(saved)
        val stillAllowed = context.contentResolver.persistedUriPermissions.any { it.uri == uri && it.isWritePermission }
        return uri.takeIf { stillAllowed }
    }

    /** Keeps access to the folder picked in the system picker (survives restarts). */
    fun setFolder(context: Context, uri: Uri) {
        val flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
        runCatching { context.contentResolver.takePersistableUriPermission(uri, flags) }
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putString(KEY_FOLDER, uri.toString()).apply()
    }

    /** A readable name for the folder, e.g. "Music/TubeFy". */
    fun folderLabel(uri: Uri): String {
        val id = runCatching { DocumentsContract.getTreeDocumentId(uri) }.getOrNull() ?: return "Selected folder"
        // "primary:Music/TubeFy" → "Music/TubeFy"; SD cards keep their volume name
        val path = id.substringAfter(':', id)
        return when {
            path.isBlank() -> if (id.startsWith("primary")) "Phone storage" else id.substringBefore(':')
            id.startsWith("primary:") -> path
            else -> "${id.substringBefore(':')}/$path"
        }
    }

    /** Which lyrics go into the Song Book for songs in another script. */
    enum class LyricsChoice { Original, English, Both }

    fun lyricsChoice(context: Context): LyricsChoice =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY_LYRICS, null)
            ?.let { saved -> LyricsChoice.entries.firstOrNull { it.name == saved } } ?: LyricsChoice.Original

    fun setLyricsChoice(context: Context, choice: LyricsChoice) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putString(KEY_LYRICS, choice.name).apply()
    }

    /**
     * The song's file if it's already in the chosen folder (same name, either audio type).
     * Blocking: call off the main thread.
     */
    fun findExisting(context: Context, title: String, artist: String): String? {
        val folder = folder(context) ?: return null
        val base = fileName(title, artist)
        val wanted = setOf("$base.m4a", "$base.webm")
        return childNames(context, folder).firstOrNull { it in wanted }
    }

    /** The display names of the files in [folder]. Blocking. */
    internal fun childNames(context: Context, folder: Uri): List<String> {
        val children = DocumentsContract.buildChildDocumentsUriUsingTree(folder, DocumentsContract.getTreeDocumentId(folder))
        return context.contentResolver.query(children, arrayOf(DocumentsContract.Document.COLUMN_DISPLAY_NAME), null, null, null)
            ?.use { cursor -> buildList { while (cursor.moveToNext()) add(cursor.getString(0).orEmpty()) } }
            .orEmpty()
    }

    /** [replace]: overwrite a song already saved under the same name (otherwise Android adds "(1)"). */
    fun enqueue(context: Context, videoId: String, title: String, artist: String, replace: Boolean = false) {
        com.ramzmania.tubefy.core.analytics.Analytics.downloadStart("phone")
        val folder = folder(context) ?: return
        val request = OneTimeWorkRequestBuilder<PhoneDownloadWorker>()
            .addTag(WORK_TAG)
            .setInputData(
                workDataOf(
                    PhoneDownloadWorker.KEY_VIDEO_ID to Downloads.plainId(videoId),
                    PhoneDownloadWorker.KEY_TITLE to title,
                    PhoneDownloadWorker.KEY_ARTIST to artist,
                    PhoneDownloadWorker.KEY_FOLDER to folder.toString(),
                    PhoneDownloadWorker.KEY_REPLACE to replace,
                    PhoneDownloadWorker.KEY_LYRICS to lyricsChoice(context).name
                )
            )
            .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
            .build()
        WorkManager.getInstance(context).enqueueUniqueWork(workName(videoId), ExistingWorkPolicy.REPLACE, request)
    }

    /** "Song - Artist" without characters file systems refuse. */
    internal fun fileName(title: String, artist: String): String {
        val base = listOf(title.trim(), artist.trim().takeUnless { it.isEmpty() || title.contains(it, ignoreCase = true) })
            .filterNotNull().filter { it.isNotEmpty() }.joinToString(" - ").ifEmpty { "TubeFy song" }
        return base.replace("\"", "").replace(Regex("[\\\\/:*?<>|\\p{Cntrl}]"), " ").replace(Regex("\\s+"), " ").trim().take(120)
    }
}

/** Downloads the best audio-only stream into the chosen folder, then posts a "Saved" notification. */
class PhoneDownloadWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val videoId = inputData.getString(KEY_VIDEO_ID) ?: return@withContext Result.failure()
        val title = inputData.getString(KEY_TITLE).orEmpty()
        val artist = inputData.getString(KEY_ARTIST).orEmpty()
        val folder = inputData.getString(KEY_FOLDER)?.let(Uri::parse) ?: return@withContext Result.failure()
        val resolver = applicationContext.contentResolver
        var created: Uri? = null
        try {
            // Best audio: M4A (AAC) plays everywhere; Opus/WebM only if there's no M4A
            val audio = YouTubeStreams.info(videoId).audioStreams.orEmpty()
                .filter { !it.content.isNullOrBlank() }
                .sortedWith(compareByDescending<org.schabi.newpipe.extractor.stream.AudioStream> { it.format == MediaFormat.M4A }
                    .thenByDescending { it.audioTrackType == null || it.audioTrackType == org.schabi.newpipe.extractor.stream.AudioTrackType.ORIGINAL }
                    .thenByDescending { it.averageBitrate })
                .firstOrNull() ?: throw IOException("No audio stream for $videoId")
            val (mime, extension) = if (audio.format == MediaFormat.M4A) "audio/mp4" to "m4a" else "audio/webm" to "webm"

            val parent = DocumentsContract.buildDocumentUriUsingTree(folder, DocumentsContract.getTreeDocumentId(folder))
            val name = "${PhoneDownloads.fileName(title, artist)}.$extension"
            if (inputData.getBoolean(KEY_REPLACE, false)) deleteChild(folder, name)
            created = DocumentsContract.createDocument(resolver, parent, mime, name)
                ?: throw IOException("Couldn't create the file")
            resolver.openOutputStream(created)?.use { output ->
                downloadInChunks(audio.content, output) { setProgress(workDataOf(KEY_PROGRESS to it)) }
            } ?: throw IOException("Couldn't write the file")
            // The song's details and lyrics go into the folder's Song Book (a failure there doesn't undo the save)
            runCatching { addToSongBook(videoId, title, artist, folder, created) }
                .onFailure { android.util.Log.w("PhoneDownloads", "Song Book not updated for $videoId", it) }
            notify(videoId, "Saved to ${PhoneDownloads.folderLabel(folder)}", title)
            com.ramzmania.tubefy.core.analytics.Analytics.downloadResult("phone", "done")
            Result.success()
        } catch (e: Exception) {
            com.ramzmania.tubefy.core.youtube.YouTubeAccess.check(e)
            created?.let { runCatching { DocumentsContract.deleteDocument(resolver, it) } }
            if (runAttemptCount < MAX_ATTEMPTS && e !is SecurityException) Result.retry()
            else {
                notify(videoId, "Couldn't save to phone storage", title)
                com.ramzmania.tubefy.core.analytics.Analytics.downloadResult("phone", "failed")
                Result.failure()
            }
        }
    }

    /** Removes the file called [name] from [folder] (Replace). */
    private fun deleteChild(folder: Uri, name: String) {
        val resolver = applicationContext.contentResolver
        val children = DocumentsContract.buildChildDocumentsUriUsingTree(folder, DocumentsContract.getTreeDocumentId(folder))
        resolver.query(
            children,
            arrayOf(DocumentsContract.Document.COLUMN_DOCUMENT_ID, DocumentsContract.Document.COLUMN_DISPLAY_NAME),
            null, null, null
        )?.use { cursor ->
            while (cursor.moveToNext()) {
                if (cursor.getString(1) == name) {
                    runCatching { DocumentsContract.deleteDocument(resolver, DocumentsContract.buildDocumentUriUsingTree(folder, cursor.getString(0))) }
                }
            }
        }
    }

    private suspend fun addToSongBook(videoId: String, title: String, artist: String, folder: Uri, file: Uri) {
        val context = applicationContext
        val durationMs = YouTubeStreams.peek(videoId)?.duration?.times(1000) ?: 0L
        val info = dagger.hilt.android.EntryPointAccessors.fromApplication(context, DownloadEntryPoint::class.java)
            .songInfoSource()
            .load(videoId, title, artist, durationMs)
        fun text(lyrics: com.ramzmania.tubefy.core.lyrics.Lyrics?) = lyrics?.lines?.joinToString("\n") { it.text }.orEmpty()
        val original = text(info.lyrics)
        val english = text(info.englishLetters)
        // Songs in another script: the lyrics the user chose for the Song Book
        val choice = inputData.getString(KEY_LYRICS)?.let { saved -> PhoneDownloads.LyricsChoice.entries.firstOrNull { it.name == saved } }
            ?: PhoneDownloads.LyricsChoice.Original
        val (lyrics, lyricsEnglish) = when {
            english.isBlank() -> original to ""
            choice == PhoneDownloads.LyricsChoice.English -> english to ""
            choice == PhoneDownloads.LyricsChoice.Both -> original to english
            else -> original to ""
        }
        // The name Android actually gave the file (it adds "(1)" when the name was taken)
        val fileName = context.contentResolver.query(file, arrayOf(DocumentsContract.Document.COLUMN_DISPLAY_NAME), null, null, null)
            ?.use { if (it.moveToFirst()) it.getString(0) else null }.orEmpty()
        SongBook.record(
            context,
            SongBookEntry(
                videoId = videoId,
                title = info.details.title.ifBlank { title },
                movie = info.details.movie,
                year = info.details.year,
                language = SongBook.languageOf(original),
                singers = info.details.singers.ifBlank { artist },
                composer = info.details.composer,
                lyricist = info.details.lyricist,
                lyrics = lyrics,
                lyricsEnglish = lyricsEnglish,
                lyricsSource = info.lyrics?.source.orEmpty(),
                fileName = fileName,
                folder = folder.toString(),
                savedAt = System.currentTimeMillis()
            )
        )
    }

    private fun notify(videoId: String, heading: String, title: String) = postSavedNotification(applicationContext, videoId, heading, title)

    companion object {
        const val KEY_VIDEO_ID = "videoId"
        const val KEY_TITLE = "title"
        const val KEY_ARTIST = "artist"
        const val KEY_FOLDER = "folder"
        const val KEY_REPLACE = "replace"
        const val KEY_LYRICS = "lyrics"
        const val KEY_PROGRESS = "progress"
        private const val MAX_ATTEMPTS = 3
    }
}

private const val SAVED_CHANNEL = "tubefy_saved_songs"

/** "Saved to Music/TubeFy" (or a failure) for downloads saved to phone storage. */
internal fun postSavedNotification(context: Context, id: String, heading: String, text: String) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
        ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
    ) return
    val manager = context.getSystemService(NotificationManager::class.java)
    if (manager.getNotificationChannel(SAVED_CHANNEL) == null) {
        manager.createNotificationChannel(NotificationChannel(SAVED_CHANNEL, "Saved downloads", NotificationManager.IMPORTANCE_LOW))
    }
    val notification = NotificationCompat.Builder(context, SAVED_CHANNEL)
        .setSmallIcon(R.drawable.ic_notification_player_icon)
        .setContentTitle(heading)
        .setContentText(text)
        .setAutoCancel(true)
        .build()
    NotificationManagerCompat.from(context).notify(id.hashCode(), notification)
}
