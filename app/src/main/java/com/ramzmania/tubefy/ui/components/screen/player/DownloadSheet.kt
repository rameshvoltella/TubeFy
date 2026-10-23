package com.ramzmania.tubefy.ui.components.screen.player

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.LibraryMusic
import androidx.compose.material.icons.rounded.SaveAlt
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ramzmania.tubefy.core.download.PhoneDownloads
import com.ramzmania.tubefy.ui.theme.DiscoverAccent
import com.ramzmania.tubefy.ui.theme.DiscoverTextSecondary

private val SheetColor = Color(0xFF161314)
private val CardColor = Color(0xFF221D1F)
private val SavedGreen = Color(0xFF2ECC71)

/**
 * Where to download the song: inside TubeFy (offline playback, Downloads playlist) or to a folder on
 * the phone as an audio file. [phoneFolder] is the folder chosen before, shown with a "Change" option.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun DownloadOptionsSheet(
    songTitle: String,
    inAppDone: Boolean,
    inAppBusy: Boolean,
    phoneSaving: Boolean,
    phoneFolder: String?,
    lyricsChoice: PhoneDownloads.LyricsChoice,
    onLyricsChoiceChange: (PhoneDownloads.LyricsChoice) -> Unit,
    onInApp: () -> Unit,
    onPhone: () -> Unit,
    onChangeFolder: () -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = SheetColor
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .navigationBarsPadding()
                .padding(start = 20.dp, end = 20.dp, bottom = 20.dp)
        ) {
            Text("Download", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Text(
                songTitle,
                color = DiscoverTextSecondary,
                fontSize = 14.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(bottom = 4.dp)
            )

            OptionCard(
                icon = Icons.Rounded.LibraryMusic,
                title = "In TubeFy",
                subtitle = "Listen offline inside the app. Saved to your Downloads playlist.",
                status = when {
                    inAppDone -> "Already downloaded"
                    inAppBusy -> "Downloading…"
                    else -> null
                },
                enabled = !inAppDone && !inAppBusy,
                onClick = onInApp
            )

            OptionCard(
                icon = Icons.Rounded.SaveAlt,
                title = "Save to phone storage",
                subtitle = "Audio only (.m4a). Plays in any music app or file manager.",
                status = if (phoneSaving) "Saving…" else null,
                enabled = !phoneSaving,
                onClick = onPhone
            ) {
                // The folder it goes to, and a way to change it
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .padding(top = 10.dp)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = 0.06f))
                        .padding(start = 12.dp)
                ) {
                    Icon(Icons.Rounded.Folder, contentDescription = null, tint = DiscoverTextSecondary, modifier = Modifier.size(18.dp))
                    Text(
                        phoneFolder ?: "You'll choose a folder",
                        color = if (phoneFolder != null) Color.White else DiscoverTextSecondary,
                        fontSize = 13.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f).padding(start = 8.dp)
                    )
                    TextButton(onClick = onChangeFolder) {
                        Text(if (phoneFolder != null) "Change" else "Choose", color = DiscoverAccent, fontSize = 13.sp)
                    }
                }
                // Lyrics for the Song Book (songs in Malayalam, Hindi, … scripts)
                Text(
                    "Song Book lyrics",
                    color = DiscoverTextSecondary,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 12.dp, bottom = 6.dp)
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(50))
                        .background(Color.White.copy(alpha = 0.06f))
                        .padding(3.dp)
                ) {
                    listOf(
                        PhoneDownloads.LyricsChoice.Original to "Original",
                        PhoneDownloads.LyricsChoice.English to "English letters",
                        PhoneDownloads.LyricsChoice.Both to "Both"
                    ).forEach { (choice, label) ->
                        val selected = choice == lyricsChoice
                        Text(
                            label,
                            color = if (selected) Color.White else DiscoverTextSecondary,
                            fontSize = 13.sp,
                            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            maxLines = 1,
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(50))
                                .background(if (selected) DiscoverAccent else Color.Transparent)
                                .clickable { onLyricsChoiceChange(choice) }
                                .padding(vertical = 8.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun OptionCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    status: String?,
    enabled: Boolean,
    onClick: () -> Unit,
    extra: @Composable () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(CardColor)
            .border(1.dp, Color.White.copy(alpha = 0.06f), RoundedCornerShape(18.dp))
            .clickable(enabled = enabled, onClick = onClick)
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(DiscoverAccent.copy(alpha = if (enabled) 0.18f else 0.08f))
            ) {
                Icon(icon, contentDescription = null, tint = if (enabled) DiscoverAccent else DiscoverTextSecondary, modifier = Modifier.size(24.dp))
            }
            Column(Modifier.weight(1f).padding(start = 14.dp)) {
                Text(title, color = if (enabled) Color.White else Color.White.copy(alpha = 0.6f), fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
                Text(subtitle, color = DiscoverTextSecondary, fontSize = 13.sp, lineHeight = 17.sp, modifier = Modifier.padding(top = 2.dp))
            }
        }
        status?.let {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 10.dp, start = 58.dp)) {
                Icon(Icons.Rounded.CheckCircle, contentDescription = null, tint = SavedGreen, modifier = Modifier.size(16.dp))
                Text(it, color = SavedGreen, fontSize = 13.sp, fontWeight = FontWeight.Medium, modifier = Modifier.padding(start = 6.dp))
            }
        }
        extra()
    }
}
