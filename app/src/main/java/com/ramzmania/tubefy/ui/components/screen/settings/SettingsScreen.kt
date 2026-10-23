package com.ramzmania.tubefy.ui.components.screen.settings

import android.widget.Toast
import androidx.compose.material.icons.rounded.Download
import com.ramzmania.tubefy.ui.components.NavigationItem
import com.ramzmania.tubefy.utils.LocalNavController
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.PlaylistRemove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ramzmania.tubefy.ui.theme.DiscoverAccent
import com.ramzmania.tubefy.ui.theme.DiscoverAccentContainer
import com.ramzmania.tubefy.ui.theme.DiscoverBackground
import com.ramzmania.tubefy.ui.theme.DiscoverBorder
import com.ramzmania.tubefy.ui.theme.DiscoverSurface
import com.ramzmania.tubefy.ui.theme.DiscoverTextSecondary
import com.ramzmania.tubefy.viewmodel.TubeFyViewModel

@Composable
fun SettingsScreen(viewModel: TubeFyViewModel = hiltViewModel()) {
    val navController = LocalNavController.current
    val context = LocalContext.current
    var confirmClear by remember { mutableStateOf(false) }

    if (confirmClear) {
        AlertDialog(
            onDismissRequest = { confirmClear = false },
            title = { Text("Clear automated playlist?") },
            text = {
                Text(
                    "All the songs that play automatically after the song you selected will be cleared. " +
                        "You'll get new ones based on what you play next."
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    confirmClear = false
                    viewModel.clearAutomatedPlaylist {
                        Toast.makeText(context, "Automated playlist cleared", Toast.LENGTH_SHORT).show()
                    }
                }) { Text("Clear", color = DiscoverAccent) }
            },
            dismissButton = {
                TextButton(onClick = { confirmClear = false }) { Text("Cancel", color = DiscoverTextSecondary) }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DiscoverBackground)
            .verticalScroll(rememberScrollState())
            .padding(bottom = 24.dp)
    ) {
        Text(
            text = "Settings",
            color = Color.White,
            fontSize = 38.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 16.dp)
        )

        SectionLabel("Playback")
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(DiscoverSurface)
                .border(1.dp, DiscoverBorder, RoundedCornerShape(18.dp))
                .clickable { confirmClear = true }
                .padding(14.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(DiscoverAccentContainer)
            ) {
                Icon(Icons.Rounded.PlaylistRemove, contentDescription = null, tint = DiscoverAccent, modifier = Modifier.size(26.dp))
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 14.dp)
            ) {
                Text("Clear automated playlist", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                Text(
                    "Remove the songs that play automatically after your selected song",
                    color = DiscoverTextSecondary,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
            Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, contentDescription = null, tint = DiscoverTextSecondary)
        }

        SectionLabel("Downloads")
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(DiscoverSurface)
                .border(1.dp, DiscoverBorder, RoundedCornerShape(18.dp))
                .clickable { navController?.navigate(NavigationItem.VideoDownloads.route) }
                .padding(14.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(DiscoverAccentContainer)
            ) {
                Icon(Icons.Rounded.Download, contentDescription = null, tint = DiscoverAccent, modifier = Modifier.size(26.dp))
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 14.dp)
            ) {
                Text("Downloads", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                Text(
                    "Videos and audio downloaded from the YouTube tab: progress, and open them in any app",
                    color = DiscoverTextSecondary,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
            Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, contentDescription = null, tint = DiscoverTextSecondary)
        }

        SectionLabel("About")
        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(DiscoverSurface)
                .border(1.dp, DiscoverBorder, RoundedCornerShape(18.dp))
                .padding(18.dp)
        ) {
            Text("TubeFy", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Text(
                text = ABOUT_TEXT,
                color = DiscoverTextSecondary,
                fontSize = 15.sp,
                lineHeight = 22.sp,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        color = DiscoverTextSecondary,
        fontSize = 13.sp,
        fontWeight = FontWeight.Medium,
        modifier = Modifier.padding(start = 24.dp, top = 18.dp, bottom = 8.dp)
    )
}

private const val ABOUT_TEXT = "Discover Your Favorite Music with Ease!\n" +
    "\n" +
    "Transform your YouTube experience into a seamless music streaming journey with our app. Just like Spotify, our app lets you play YouTube videos as songs, giving you access to your favorite tracks without interruption. Whether you're into the latest hits or classic tunes, you can enjoy them all in a Spotify-like interface.\n" +
    "\n" +
    "Key Features:\n" +
    "\n" +
    "• Play YouTube Videos as Songs: Enjoy your favorite YouTube music videos as if they were on a streaming service, with audio-only playback for a better listening experience.\n" +
    "• Curated Playlists: Create and manage playlists with your favorite tracks, just like you would on Spotify.\n" +
    "• Discover New Music: Explore new music and discover trending songs easily with personalized recommendations.\n" +
    "• Seamless Playback: Enjoy uninterrupted music playback with background support, so you can listen while using other apps.\n" +
    "• User-Friendly Interface: Navigate your music library and playlists with an intuitive and sleek interface designed for ease of use.\n" +
    "\n" +
    "Elevate your music experience and enjoy YouTube's vast music library like never before. Download now and start listening!"
