package com.ramzmania.tubefy.ui.components.screen.youtube

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.Movie
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ramzmania.tubefy.core.download.VideoDownloadOption
import com.ramzmania.tubefy.core.download.VideoDownloads
import com.ramzmania.tubefy.ui.theme.DiscoverAccent
import com.ramzmania.tubefy.ui.theme.DiscoverTextSecondary
import com.ramzmania.tubefy.viewmodel.VideoViewModel

private val SheetColor = Color(0xFF161314)
private val RowColor = Color(0xFF221D1F)

/**
 * The video page's Download sheet: Video (every quality) or Audio only (every format), the folder it
 * saves to, and a Download button. HD videos are joined with their sound on the phone.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun VideoDownloadSheet(
    title: String,
    options: VideoViewModel.DownloadOptions,
    folder: String?,
    onChangeFolder: () -> Unit,
    onDownload: (VideoDownloadOption) -> Unit,
    onDismiss: () -> Unit
) {
    var audioTab by remember { mutableStateOf(false) }
    var chosen by remember { mutableStateOf<VideoDownloadOption?>(null) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = SheetColor
    ) {
        Column(Modifier.navigationBarsPadding().padding(start = 20.dp, end = 20.dp, bottom = 16.dp)) {
            Text("Download", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Text(title, color = DiscoverTextSecondary, fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)

            // Video | Audio only
            Row(
                modifier = Modifier
                    .padding(top = 16.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(50))
                    .background(Color.White.copy(alpha = 0.06f))
                    .padding(4.dp)
            ) {
                listOf(false to "Video", true to "Audio only").forEach { (audio, label) ->
                    val selected = audio == audioTab
                    Text(
                        label,
                        color = if (selected) Color.White else DiscoverTextSecondary,
                        fontSize = 15.sp,
                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(50))
                            .background(if (selected) DiscoverAccent else Color.Transparent)
                            .clickable {
                                audioTab = audio
                                chosen = null
                            }
                            .padding(vertical = 10.dp)
                    )
                }
            }

            when (options) {
                VideoViewModel.DownloadOptions.Loading -> Box(Modifier.fillMaxWidth().height(160.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = DiscoverAccent)
                }

                VideoViewModel.DownloadOptions.Error -> Text(
                    com.ramzmania.tubefy.core.youtube.YouTubeAccess.messageOr("Couldn't load the download options"),
                    color = DiscoverTextSecondary,
                    fontSize = 15.sp,
                    modifier = Modifier.padding(vertical = 40.dp)
                )

                is VideoViewModel.DownloadOptions.Loaded -> {
                    val list = if (audioTab) options.audio else options.video
                    if (list.isEmpty()) {
                        Text("Nothing to download here", color = DiscoverTextSecondary, fontSize = 15.sp, modifier = Modifier.padding(vertical = 40.dp))
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(top = 14.dp).heightIn(max = 340.dp)
                        ) {
                            items(list, key = { "${it.audioOnly}|${it.videoItag}|${it.audioItag}|${it.audioTrackId}|${it.withSound}" }) { option ->
                                OptionRow(option, selected = option == chosen, onClick = { chosen = option })
                            }
                        }
                        if (!audioTab) Text(
                            "HD videos are joined with their sound on your phone after downloading.",
                            color = DiscoverTextSecondary,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            }

            // Where it's saved
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .padding(top = 14.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White.copy(alpha = 0.06f))
                    .padding(start = 12.dp)
            ) {
                Icon(Icons.Rounded.Folder, contentDescription = null, tint = DiscoverTextSecondary, modifier = Modifier.size(18.dp))
                Text(
                    folder ?: "You'll choose a folder",
                    color = if (folder != null) Color.White else DiscoverTextSecondary,
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f).padding(start = 8.dp)
                )
                TextButton(onClick = onChangeFolder) {
                    Text(if (folder != null) "Change" else "Choose", color = DiscoverAccent, fontSize = 13.sp)
                }
            }

            val pick = chosen
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .padding(top = 14.dp)
                    .fillMaxWidth()
                    .height(52.dp)
                    .clip(RoundedCornerShape(26.dp))
                    .background(if (pick != null) DiscoverAccent else Color.White.copy(alpha = 0.08f))
                    .clickable(enabled = pick != null) { pick?.let(onDownload) }
            ) {
                Text(
                    pick?.let { "Download ${it.label}${VideoDownloads.sizeLabel(it.sizeBytes).let { size -> if (size.isNotEmpty()) " · $size" else "" }}" }
                        ?: "Choose a ${if (audioTab) "format" else "quality"}",
                    color = if (pick != null) Color.White else DiscoverTextSecondary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun OptionRow(option: VideoDownloadOption, selected: Boolean, onClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(RowColor)
            .border(1.5.dp, if (selected) DiscoverAccent else Color.Transparent, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(38.dp).clip(CircleShape).background(DiscoverAccent.copy(alpha = 0.15f))
        ) {
            Icon(if (option.audioOnly) Icons.Rounded.GraphicEq else Icons.Rounded.Movie, contentDescription = null, tint = DiscoverAccent, modifier = Modifier.size(20.dp))
        }
        Column(Modifier.weight(1f).padding(start = 12.dp)) {
            Text(option.label, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            if (option.detail.isNotBlank()) Text(option.detail, color = DiscoverTextSecondary, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        Text(VideoDownloads.sizeLabel(option.sizeBytes), color = DiscoverTextSecondary, fontSize = 13.sp)
        // Radio dot
        Box(
            Modifier
                .padding(start = 12.dp)
                .size(20.dp)
                .clip(CircleShape)
                .border(2.dp, if (selected) DiscoverAccent else DiscoverTextSecondary, CircleShape)
                .padding(4.dp)
                .clip(CircleShape)
                .background(if (selected) DiscoverAccent else Color.Transparent)
        )
    }
}
