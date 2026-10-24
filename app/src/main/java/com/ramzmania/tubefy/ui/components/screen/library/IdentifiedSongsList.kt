package com.ramzmania.tubefy.ui.components.screen.library

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.ramzmania.tubefy.R
import com.ramzmania.tubefy.core.recognition.IdentifiedSong
import com.ramzmania.tubefy.core.recognition.IdentifiedSongs
import com.ramzmania.tubefy.data.dto.base.searchformat.TubeFyCoreTypeData
import com.ramzmania.tubefy.data.youtube.YouTubeFeedSource
import com.ramzmania.tubefy.ui.components.NavigationItem
import com.ramzmania.tubefy.ui.components.navigateToTab
import com.ramzmania.tubefy.ui.components.screen.album.openTrack
import com.ramzmania.tubefy.ui.components.screen.search.PendingSearch
import com.ramzmania.tubefy.ui.theme.DiscoverAccent
import com.ramzmania.tubefy.ui.theme.DiscoverBorder
import com.ramzmania.tubefy.ui.theme.DiscoverSurface
import com.ramzmania.tubefy.ui.theme.DiscoverTextSecondary
import com.ramzmania.tubefy.utils.LocalNavController

/**
 * Library → Shazam: songs identified with 🎤, newest first. Tap plays it (the YouTube Music match
 * found when it was identified, or a search); ⋮ has Play, Search and Remove.
 */
@Composable
fun IdentifiedSongsList() {
    val context = LocalContext.current
    val navController = LocalNavController.current
    val songs by remember { IdentifiedSongs.songs(context) }.collectAsState()
    var confirmClear by remember { mutableStateOf(false) }
    val list = songs.orEmpty()

    fun search(song: IdentifiedSong) {
        PendingSearch.query.value = listOf(song.title, song.artist.substringBefore(" & ").substringBefore(",")).filter { it.isNotBlank() }.joinToString(" ")
        navController?.navigateToTab(NavigationItem.Search.route)
    }

    fun play(song: IdentifiedSong) {
        val id = song.videoId
        if (id.isNullOrBlank()) {
            search(song) // no match was found when it was identified: let the user pick
        } else navController?.let {
            openTrack(it, TubeFyCoreTypeData(videoId = id, videoTitle = song.title, videoImage = "https://i.ytimg.com/vi/$id/hq720.jpg", artist = song.artist))
        }
    }

    if (confirmClear) {
        AlertDialog(
            onDismissRequest = { confirmClear = false },
            containerColor = Color(0xFF161314),
            title = { Text("Clear identified songs?", color = Color.White) },
            text = { Text("This removes all songs from your Shazam list.", color = DiscoverTextSecondary) },
            confirmButton = {
                TextButton(onClick = {
                    IdentifiedSongs.clear(context)
                    confirmClear = false
                }) { Text("Clear", color = DiscoverAccent) }
            },
            dismissButton = { TextButton(onClick = { confirmClear = false }) { Text("Cancel", color = DiscoverTextSecondary) } }
        )
    }

    if (list.isEmpty()) {
        EmptyLibraryState(
            title = "No identified songs yet",
            message = "Tap 🎤 on Home or Search while a song plays\nand it'll appear here"
        )
        return
    }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 24.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item(key = "header") {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(start = 4.dp)) {
                Text(
                    "${list.size} ${if (list.size == 1) "song" else "songs"} identified",
                    color = DiscoverTextSecondary,
                    fontSize = 14.sp,
                    modifier = Modifier.weight(1f)
                )
                TextButton(onClick = { confirmClear = true }) { Text("Clear all", color = DiscoverAccent) }
            }
        }
        items(list, key = { it.key }) { song ->
            IdentifiedSongRow(
                song = song,
                onPlay = { play(song) },
                onSearch = { search(song) },
                onRemove = { IdentifiedSongs.remove(context, song) }
            )
        }
    }
}

@Composable
private fun IdentifiedSongRow(song: IdentifiedSong, onPlay: () -> Unit, onSearch: () -> Unit, onRemove: () -> Unit) {
    var menuOpen by remember { mutableStateOf(false) }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(DiscoverSurface)
            .border(1.dp, DiscoverBorder, RoundedCornerShape(16.dp))
            .clickable(onClick = onPlay)
            .padding(start = 10.dp, top = 10.dp, bottom = 10.dp)
    ) {
        AsyncImage(
            model = song.coverUrl ?: R.drawable.placeholder,
            contentDescription = song.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier.size(58.dp).clip(RoundedCornerShape(12.dp))
        )
        Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
            Text(song.title, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            if (song.artist.isNotBlank()) Text(song.artist, color = Color.White.copy(alpha = 0.8f), fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            val extra = listOf(song.album, song.year, YouTubeFeedSource.timeAgo(song.identifiedAt, System.currentTimeMillis()))
                .filter { it.isNotBlank() }.joinToString(" · ")
            Text(extra, color = DiscoverTextSecondary, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        Box {
            IconButton(onClick = { menuOpen = true }) {
                Icon(Icons.Rounded.MoreVert, contentDescription = "More options", tint = DiscoverTextSecondary)
            }
            DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                DropdownMenuItem(text = { Text("Play") }, leadingIcon = { Icon(Icons.Rounded.PlayArrow, null) }, onClick = { menuOpen = false; onPlay() })
                DropdownMenuItem(text = { Text("Search for other versions") }, leadingIcon = { Icon(Icons.Rounded.Search, null) }, onClick = { menuOpen = false; onSearch() })
                DropdownMenuItem(text = { Text("Remove") }, leadingIcon = { Icon(Icons.Rounded.Delete, null) }, onClick = { menuOpen = false; onRemove() })
            }
        }
    }
}
