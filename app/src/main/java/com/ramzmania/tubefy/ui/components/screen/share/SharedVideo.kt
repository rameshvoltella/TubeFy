package com.ramzmania.tubefy.ui.components.screen.share

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.ramzmania.tubefy.ui.components.YouTubeIconFilled
import com.ramzmania.tubefy.ui.theme.DiscoverAccent
import com.ramzmania.tubefy.ui.theme.DiscoverAccentContainer
import com.ramzmania.tubefy.ui.theme.DiscoverSurface
import com.ramzmania.tubefy.ui.theme.DiscoverTextSecondary
import kotlinx.coroutines.flow.MutableStateFlow

/** A YouTube video shared to (or opened with) TubeFy. */
data class SharedVideo(val videoId: String, val title: String) {
    val thumbnail get() = "https://i.ytimg.com/vi/$videoId/hq720.jpg"

    companion object {
        /** Waiting for the user to choose Music or YouTube. */
        val pending = MutableStateFlow<SharedVideo?>(null)

        private val ID_PATTERNS = listOf(
            Regex("""youtu\.be/([A-Za-z0-9_-]{11})"""),
            Regex("""[?&]v=([A-Za-z0-9_-]{11})"""),
            Regex("""youtube\.com/(?:shorts|live|embed|v)/([A-Za-z0-9_-]{11})""")
        )

        /** The video in a Share (text) or a View (link) intent, if it is a YouTube link. */
        fun from(intent: Intent?): SharedVideo? {
            intent ?: return null
            val text = when (intent.action) {
                Intent.ACTION_SEND -> intent.getStringExtra(Intent.EXTRA_TEXT)
                Intent.ACTION_VIEW -> intent.dataString
                else -> null
            } ?: return null
            val id = ID_PATTERNS.firstNotNullOfOrNull { it.find(text)?.groupValues?.get(1) } ?: return null
            // YouTube puts the title in the subject (older versions as "Watch "…" on YouTube")
            val subject = intent.getStringExtra(Intent.EXTRA_SUBJECT).orEmpty()
            val title = Regex("""^Watch "(.*)" on YouTube$""").find(subject)?.groupValues?.get(1) ?: subject
            return SharedVideo(id, title.trim())
        }
    }
}

/** Asks how to open a shared video: as music in the player, or as a video in the YouTube player. */
@Composable
fun SharedVideoDialog(video: SharedVideo, onMusic: () -> Unit, onVideo: () -> Unit, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(24.dp), color = DiscoverSurface) {
            Column(Modifier.padding(20.dp)) {
                AsyncImage(
                    model = video.thumbnail,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color.White.copy(alpha = 0.06f))
                )
                Spacer(Modifier.height(14.dp))
                Text(
                    video.title.ifBlank { "Shared YouTube video" },
                    color = Color.White,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text("How do you want to open it?", color = DiscoverTextSecondary, fontSize = 13.sp)
                Spacer(Modifier.height(16.dp))
                OpenOption(Icons.Rounded.MusicNote, "Play as music", "In the music player, with lyrics", onMusic)
                Spacer(Modifier.height(10.dp))
                OpenOption(YouTubeIconFilled, "Watch video", "In the YouTube player", onVideo)
                Spacer(Modifier.height(6.dp))
                TextButton(onClick = onDismiss, modifier = Modifier.align(Alignment.End)) {
                    Text("Cancel", color = DiscoverTextSecondary)
                }
            }
        }
    }
}

@Composable
private fun OpenOption(icon: ImageVector, title: String, subtitle: String, onClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White.copy(alpha = 0.06f))
            .clickable(onClick = onClick)
            .padding(12.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(DiscoverAccentContainer)
        ) {
            Icon(icon, contentDescription = null, tint = DiscoverAccent, modifier = Modifier.size(22.dp))
        }
        Spacer(Modifier.width(14.dp))
        Column {
            Text(title, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
            Text(subtitle, color = DiscoverTextSecondary, fontSize = 12.sp)
        }
    }
}
