package com.ramzmania.tubefy.ui.components.screen.settings

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.DocumentsContract
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Movie
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.RemoveCircleOutline
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.ramzmania.tubefy.core.download.PhoneDownloads
import com.ramzmania.tubefy.core.download.VideoDownloadEntry
import com.ramzmania.tubefy.core.download.VideoDownloadLog
import com.ramzmania.tubefy.core.download.VideoDownloadWorker
import com.ramzmania.tubefy.core.download.VideoDownloads
import com.ramzmania.tubefy.ui.theme.DiscoverAccent
import com.ramzmania.tubefy.ui.theme.DiscoverBackground
import com.ramzmania.tubefy.ui.theme.DiscoverBorder
import com.ramzmania.tubefy.ui.theme.DiscoverSurface
import com.ramzmania.tubefy.ui.theme.DiscoverTextSecondary
import com.ramzmania.tubefy.utils.LocalNavController
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

private val DoneGreen = Color(0xFF2ECC71)

/** What a row shows: from the list itself once finished, from WorkManager while running. */
private sealed interface RowState {
    data class Running(val percent: Int) : RowState
    data object Waiting : RowState
    data object Done : RowState
    data object Failed : RowState
}

/**
 * Settings → Downloads: everything downloaded from the YouTube video page. Running downloads show
 * their progress; finished ones open in any app ("Open with"); failed ones can be retried.
 */
@Composable
fun VideoDownloadsScreen() {
    val context = LocalContext.current
    val navController = LocalNavController.current
    val entries by remember { VideoDownloadLog.entries(context) }.collectAsState()
    val work by remember { WorkManager.getInstance(context).getWorkInfosByTagFlow(VideoDownloads.WORK_TAG) }.collectAsState(emptyList())
    val workById = work.associateBy { it.id.toString() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DiscoverBackground)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(start = 4.dp, top = 8.dp, end = 16.dp)) {
            IconButton(onClick = { navController?.popBackStack() }) {
                Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Text("Downloads", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
        }
        Text(
            "Videos and audio downloaded from the YouTube tab",
            color = DiscoverTextSecondary,
            fontSize = 14.sp,
            modifier = Modifier.padding(start = 20.dp, end = 20.dp, bottom = 12.dp)
        )

        val list = entries.orEmpty()
        if (list.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    "Nothing downloaded yet.\nUse Download on a YouTube video's page.",
                    color = DiscoverTextSecondary,
                    fontSize = 15.sp,
                    lineHeight = 22.sp,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp)
            ) {
                items(list, key = { it.workId }) { entry ->
                    DownloadRow(entry, rowState(entry, workById[entry.workId]))
                }
            }
        }
    }
}

private fun rowState(entry: VideoDownloadEntry, info: WorkInfo?): RowState = when (entry.status) {
    VideoDownloadEntry.Status.Done -> RowState.Done
    VideoDownloadEntry.Status.Failed -> RowState.Failed
    VideoDownloadEntry.Status.Queued -> when (info?.state) {
        WorkInfo.State.RUNNING -> RowState.Running(info.progress.getInt(VideoDownloadWorker.KEY_PROGRESS, 0))
        WorkInfo.State.ENQUEUED, WorkInfo.State.BLOCKED -> RowState.Waiting
        WorkInfo.State.SUCCEEDED -> RowState.Done
        // Failed, cancelled, or the app was closed mid-way and the work is gone
        else -> RowState.Failed
    }
}

@Composable
private fun DownloadRow(entry: VideoDownloadEntry, state: RowState) {
    val context = LocalContext.current
    var menuOpen by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(DiscoverSurface)
            .border(1.dp, DiscoverBorder, RoundedCornerShape(18.dp))
            .clickable(enabled = state == RowState.Done) { openWith(context, entry) }
            .padding(start = 14.dp, top = 12.dp, bottom = 12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(DiscoverAccent.copy(alpha = 0.15f))
            ) {
                Icon(
                    if (entry.audioOnly) Icons.Rounded.GraphicEq else Icons.Rounded.Movie,
                    contentDescription = null,
                    tint = DiscoverAccent,
                    modifier = Modifier.size(24.dp)
                )
            }
            Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
                Text(entry.title, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, maxLines = 2, overflow = TextOverflow.Ellipsis)
                val details = listOf(
                    entry.label, entry.detail, VideoDownloads.sizeLabel(entry.sizeBytes),
                    if (state == RowState.Done) entry.folderLabel else "",
                    if (state == RowState.Done) DATE.format(Date(entry.createdAt)) else ""
                ).filter { it.isNotBlank() }.joinToString(" · ")
                Text(details, color = DiscoverTextSecondary, fontSize = 12.sp, maxLines = 2, overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(top = 2.dp))
                when (state) {
                    is RowState.Running -> StatusLine("Downloading ${state.percent}%", DiscoverAccent)
                    RowState.Waiting -> StatusLine("Waiting…", DiscoverTextSecondary)
                    RowState.Done -> StatusLine("Tap to open", DoneGreen)
                    RowState.Failed -> StatusLine("Download failed", DiscoverAccent)
                }
            }
            Box {
                IconButton(onClick = { menuOpen = true }) {
                    Icon(Icons.Rounded.MoreVert, contentDescription = "More options", tint = DiscoverTextSecondary)
                }
                DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                    if (state == RowState.Done) {
                        DropdownMenuItem(
                            text = { Text("Open with") },
                            leadingIcon = { Icon(Icons.AutoMirrored.Rounded.OpenInNew, null) },
                            onClick = { menuOpen = false; openWith(context, entry) }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete file") },
                            leadingIcon = { Icon(Icons.Rounded.Delete, null) },
                            onClick = {
                                menuOpen = false
                                runCatching { DocumentsContract.deleteDocument(context.contentResolver, Uri.parse(entry.fileUri)) }
                                VideoDownloadLog.remove(context, entry.workId)
                                Toast.makeText(context, "Deleted", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                    if (state is RowState.Running || state == RowState.Waiting) DropdownMenuItem(
                        text = { Text("Cancel download") },
                        leadingIcon = { Icon(Icons.Rounded.Close, null) },
                        onClick = {
                            menuOpen = false
                            WorkManager.getInstance(context).cancelWorkById(UUID.fromString(entry.workId))
                            VideoDownloadLog.update(context, entry.workId) { it.copy(status = VideoDownloadEntry.Status.Failed) }
                        }
                    )
                    if (state == RowState.Failed) DropdownMenuItem(
                        text = { Text("Retry") },
                        leadingIcon = { Icon(Icons.Rounded.Refresh, null) },
                        onClick = { menuOpen = false; retry(context, entry) }
                    )
                    DropdownMenuItem(
                        text = { Text("Remove from list") },
                        leadingIcon = { Icon(Icons.Rounded.RemoveCircleOutline, null) },
                        onClick = { menuOpen = false; VideoDownloadLog.remove(context, entry.workId) }
                    )
                }
            }
        }
        if (state is RowState.Running || state == RowState.Waiting) {
            val percent = (state as? RowState.Running)?.percent ?: 0
            if (state == RowState.Waiting || percent == 0) LinearProgressIndicator(
                color = DiscoverAccent,
                trackColor = Color.White.copy(alpha = 0.1f),
                modifier = Modifier.fillMaxWidth().padding(top = 10.dp, end = 14.dp).height(4.dp).clip(RoundedCornerShape(2.dp))
            ) else LinearProgressIndicator(
                progress = { percent / 100f },
                color = DiscoverAccent,
                trackColor = Color.White.copy(alpha = 0.1f),
                modifier = Modifier.fillMaxWidth().padding(top = 10.dp, end = 14.dp).height(4.dp).clip(RoundedCornerShape(2.dp))
            )
        }
        if (state == RowState.Failed) TextButton(onClick = { retry(context, entry) }, contentPadding = PaddingValues(horizontal = 0.dp)) {
            Text("Retry", color = DiscoverAccent, modifier = Modifier.padding(start = 56.dp))
        }
    }
}

@Composable
private fun StatusLine(text: String, color: Color) {
    Text(text, color = color, fontSize = 12.sp, fontWeight = FontWeight.Medium, modifier = Modifier.padding(top = 3.dp))
}

/** Android's "Open with" for the saved file, so any player can open it. */
private fun openWith(context: Context, entry: VideoDownloadEntry) {
    val uri = Uri.parse(entry.fileUri)
    val exists = runCatching {
        context.contentResolver.query(uri, arrayOf(DocumentsContract.Document.COLUMN_DOCUMENT_ID), null, null, null)?.use { it.count > 0 } ?: false
    }.getOrDefault(false)
    if (!exists) {
        Toast.makeText(context, "The file was moved or deleted", Toast.LENGTH_LONG).show()
        return
    }
    val type = entry.mimeType.ifBlank { if (entry.audioOnly) "audio/*" else "video/*" }
    val view = Intent(Intent.ACTION_VIEW).setDataAndType(uri, type).addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    try {
        context.startActivity(Intent.createChooser(view, "Open with").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    } catch (e: ActivityNotFoundException) {
        Toast.makeText(context, "No app on this phone can open this file", Toast.LENGTH_LONG).show()
    }
}

private fun retry(context: Context, entry: VideoDownloadEntry) {
    if (PhoneDownloads.folder(context) == null) {
        Toast.makeText(context, "Choose a download folder from a video's Download button first", Toast.LENGTH_LONG).show()
        return
    }
    VideoDownloads.enqueue(context, entry.videoId, entry.title, entry.option(), retryOf = entry.workId)
}

private val DATE = SimpleDateFormat("d MMM", Locale.ENGLISH)
