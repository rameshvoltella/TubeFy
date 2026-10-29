package com.ramzmania.tubefy.ui.components.screen.library

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.FileUpload
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Sync
import androidx.compose.material.icons.rounded.SyncDisabled
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
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
import com.ramzmania.tubefy.core.analytics.Analytics
import com.ramzmania.tubefy.core.recognition.ImportedSong
import com.ramzmania.tubefy.core.recognition.ShazamImport
import com.ramzmania.tubefy.data.dto.base.searchformat.TubeFyCoreTypeData
import com.ramzmania.tubefy.ui.components.NavigationItem
import com.ramzmania.tubefy.ui.components.navigateToTab
import com.ramzmania.tubefy.ui.components.screen.album.openTrack
import com.ramzmania.tubefy.ui.components.screen.search.PendingSearch
import com.ramzmania.tubefy.ui.theme.DiscoverAccent
import com.ramzmania.tubefy.ui.theme.DiscoverAccentContainer
import com.ramzmania.tubefy.ui.theme.DiscoverBorder
import com.ramzmania.tubefy.ui.theme.DiscoverSurface
import com.ramzmania.tubefy.ui.theme.DiscoverTextSecondary
import com.ramzmania.tubefy.utils.LocalNavController
import kotlinx.coroutines.launch
import java.text.DateFormat
import java.util.Date

private const val SUB_TUBEFY = "TubeFy"
private const val SUB_SHAZAM = "Shazam"

/**
 * Library → Shazam. Before an import: the songs identified in TubeFy, with an "Import from Shazam"
 * card on top. After one: two sub-tabs, TubeFy (identified here) and Shazam (the imported history,
 * matched with YouTube Music a few songs at a time, see [ShazamImport]).
 */
@Composable
fun ShazamLibraryTab() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val imported by remember { ShazamImport.songs(context) }.collectAsState()
    val hasImport = !imported.isNullOrEmpty()
    var sub by rememberSaveable { mutableStateOf(SUB_TUBEFY) }
    var importing by remember { mutableStateOf(false) }
    var result by remember { mutableStateOf<ShazamImport.ImportResult?>(null) }

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri ?: return@rememberLauncherForActivityResult
        importing = true
        scope.launch {
            runCatching { ShazamImport.import(context, uri) }
                .onSuccess {
                    result = it
                    sub = SUB_SHAZAM
                    Analytics.playerFeature("shazam_import", "${it.songs}")
                }
                .onFailure { Toast.makeText(context, it.message ?: "Couldn't import the file", Toast.LENGTH_LONG).show() }
            importing = false
        }
    }
    val pickCsv = { picker.launch(arrayOf("text/csv", "text/comma-separated-values", "application/csv", "text/plain", "application/vnd.ms-excel")) }

    result?.let { done ->
        AlertDialog(
            onDismissRequest = { result = null },
            containerColor = Color(0xFF161314),
            title = { Text("Imported ${done.songs} songs", color = Color.White) },
            text = {
                val waiting = done.songs - done.alreadyFound
                val hours = ShazamImport.minutesLeft(waiting) / 60.0
                Text(
                    buildString {
                        if (done.rows > done.songs) append("${done.rows} Shazams, repeated songs merged.\n\n")
                        append("Looking up all songs at once could get TubeFy blocked by YouTube, so details are fetched slowly: ")
                        append("${ShazamImport.BATCH_SIZE} songs every ${ShazamImport.BATCH_MINUTES} minutes")
                        if (waiting > ShazamImport.BATCH_SIZE) append(" (about ${"%.0f".format(hours.coerceAtLeast(1.0))} h for all)")
                        append(".\n\nTap any song to fetch it right away.")
                    },
                    color = DiscoverTextSecondary
                )
            },
            confirmButton = { TextButton(onClick = { result = null }) { Text("OK", color = DiscoverAccent) } }
        )
    }

    Column(Modifier.fillMaxSize()) {
        if (hasImport) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(start = 16.dp, bottom = 10.dp)) {
                listOf(SUB_TUBEFY, SUB_SHAZAM).forEach { tab ->
                    val selected = sub == tab
                    Text(
                        tab,
                        color = if (selected) Color.White else DiscoverTextSecondary,
                        fontSize = 14.sp,
                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                        modifier = Modifier
                            .clip(RoundedCornerShape(18.dp))
                            .background(if (selected) DiscoverAccent else DiscoverSurface)
                            .border(1.dp, if (selected) Color.Transparent else DiscoverBorder, RoundedCornerShape(18.dp))
                            .clickable { sub = tab }
                            .padding(horizontal = 16.dp, vertical = 7.dp)
                    )
                }
            }
        }
        if (!hasImport || sub == SUB_TUBEFY) {
            if (!hasImport) ImportCard(importing, onImport = pickCsv, modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 10.dp))
            Box(Modifier.weight(1f)) { IdentifiedSongsList() }
        } else {
            ImportedSongsList(imported.orEmpty(), importing, onImportAgain = pickCsv)
        }
    }
}

@Composable
private fun ImportCard(importing: Boolean, onImport: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(DiscoverSurface)
            .border(1.dp, DiscoverBorder, RoundedCornerShape(16.dp))
            .clickable(enabled = !importing, onClick = onImport)
            .padding(12.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(44.dp).clip(CircleShape).background(DiscoverAccentContainer)
        ) {
            if (importing) CircularProgressIndicator(color = DiscoverAccent, strokeWidth = 2.dp, modifier = Modifier.size(20.dp))
            else Icon(Icons.Rounded.FileUpload, null, tint = DiscoverAccent)
        }
        Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
            Text("Import from Shazam (.csv)", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
            Text(
                "Bring in your Shazam history. In Shazam: Settings → export your data, then pick the CSV file here",
                color = DiscoverTextSecondary,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
private fun ImportedSongsList(songs: List<ImportedSong>, importing: Boolean, onImportAgain: () -> Unit) {
    val context = LocalContext.current
    val navController = LocalNavController.current
    val scope = rememberCoroutineScope()
    var fetching by remember { mutableStateOf<String?>(null) }
    var confirmClear by remember { mutableStateOf(false) }
    val found = songs.count { it.status == ImportedSong.Status.Found }
    val waiting = ShazamImport.waitingCount(songs)
    val syncStopped by remember { ShazamImport.syncStopped(context) }.collectAsState()
    val playable = songs.count { it.status != ImportedSong.Status.NotFound }

    // The whole list as the queue; songs not matched yet are looked up one by one as they come up
    fun playAll() {
        context.startService(
            android.content.Intent(context, com.ramzmania.tubefy.player.PlaybackService::class.java)
                .setAction(com.ramzmania.tubefy.player.PlaybackService.ACTION_PLAY_SHAZAM)
        )
        Analytics.playerFeature("shazam_play_all", "$playable")
        Toast.makeText(context, "Playing your $playable Shazam songs", Toast.LENGTH_SHORT).show()
    }

    fun search(song: ImportedSong) {
        PendingSearch.query.value = listOf(song.title, song.artist.substringBefore(" & ").substringBefore(","))
            .filter { it.isNotBlank() }.joinToString(" ")
        navController?.navigateToTab(NavigationItem.Search.route)
    }

    fun playFound(song: ImportedSong) {
        val id = song.videoId ?: return
        navController?.let {
            Analytics.pendingSource = "shazam"
            openTrack(it, TubeFyCoreTypeData(videoId = id, videoTitle = song.title, videoImage = song.coverUrl.orEmpty(), artist = song.artist))
        }
    }

    // Matched: play. Not yet: fetch just this one now, update the list, then play
    fun open(song: ImportedSong) {
        if (song.status == ImportedSong.Status.Found) return playFound(song)
        if (fetching != null) return
        fetching = song.key
        scope.launch {
            val updated = runCatching { ShazamImport.fetchNow(context, song) }.getOrNull()
            fetching = null
            when (updated?.status) {
                ImportedSong.Status.Found -> playFound(updated)
                ImportedSong.Status.NotFound -> {
                    Toast.makeText(context, "Not found on YouTube Music. Showing search results", Toast.LENGTH_SHORT).show()
                    search(song)
                }
                else -> Toast.makeText(
                    context,
                    com.ramzmania.tubefy.core.youtube.YouTubeAccess.messageOr("Couldn't look it up. Check your connection"),
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    if (confirmClear) {
        AlertDialog(
            onDismissRequest = { confirmClear = false },
            containerColor = Color(0xFF161314),
            title = { Text("Remove imported Shazam songs?", color = Color.White) },
            text = { Text("Your Shazam history is removed from TubeFy. Songs identified in TubeFy stay.", color = DiscoverTextSecondary) },
            confirmButton = {
                TextButton(onClick = {
                    ShazamImport.clear(context)
                    confirmClear = false
                }) { Text("Remove", color = DiscoverAccent) }
            },
            dismissButton = { TextButton(onClick = { confirmClear = false }) { Text("Cancel", color = DiscoverTextSecondary) } }
        )
    }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 24.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item(key = "status") {
            Column(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(DiscoverSurface)
                    .border(1.dp, DiscoverBorder, RoundedCornerShape(16.dp))
                    .padding(14.dp)
            ) {
                Text("${songs.size} songs from Shazam · $found ready to play", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                Text(
                    when {
                        waiting == 0 -> "All songs are looked up"
                        syncStopped == true -> "Background sync stopped · $waiting waiting. Tap a song, or Play all, to fetch songs as they come up"
                        else -> "Looking up ${ShazamImport.BATCH_SIZE} songs every ${ShazamImport.BATCH_MINUTES} min to keep YouTube happy · " +
                            "$waiting waiting${ShazamImport.minutesLeft(waiting).takeIf { it >= 60 }?.let { " (~${it / 60} h)" } ?: ""}. Tap a song to fetch it now"
                    },
                    color = DiscoverTextSecondary,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 2.dp)
                )
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 12.dp)) {
                    // Play all (filled) and Stop / Resume sync (outlined)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(DiscoverAccent)
                            .clickable(enabled = playable > 0, onClick = ::playAll)
                            .padding(start = 12.dp, end = 16.dp, top = 8.dp, bottom = 8.dp)
                    ) {
                        Icon(Icons.Rounded.PlayArrow, null, tint = Color.White, modifier = Modifier.size(20.dp))
                        Text("Play all", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(start = 4.dp))
                    }
                    if (waiting > 0) {
                        Spacer(Modifier.width(8.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .border(1.dp, DiscoverBorder, RoundedCornerShape(20.dp))
                                .clickable { ShazamImport.setSyncStopped(context, syncStopped != true) }
                                .padding(start = 12.dp, end = 16.dp, top = 8.dp, bottom = 8.dp)
                        ) {
                            Icon(
                                if (syncStopped == true) Icons.Rounded.Sync else Icons.Rounded.SyncDisabled,
                                null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                if (syncStopped == true) "Resume sync" else "Stop sync",
                                color = Color.White,
                                fontSize = 14.sp,
                                modifier = Modifier.padding(start = 6.dp)
                            )
                        }
                    }
                }
                Row(Modifier.padding(top = 4.dp)) {
                    TextButton(onClick = onImportAgain, enabled = !importing) { Text("Import again", color = DiscoverAccent) }
                    TextButton(onClick = { confirmClear = true }) { Text("Remove all", color = DiscoverTextSecondary) }
                }
            }
        }
        items(songs, key = { it.key }) { song ->
            ImportedSongRow(
                song = song,
                loading = fetching == song.key,
                onOpen = { open(song) },
                onSearch = { search(song) },
                onRemove = { ShazamImport.remove(context, song) }
            )
        }
    }
}

@Composable
private fun ImportedSongRow(song: ImportedSong, loading: Boolean, onOpen: () -> Unit, onSearch: () -> Unit, onRemove: () -> Unit) {
    var menuOpen by remember { mutableStateOf(false) }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(DiscoverSurface)
            .border(1.dp, DiscoverBorder, RoundedCornerShape(16.dp))
            .clickable(onClick = onOpen)
            .padding(start = 10.dp, top = 10.dp, bottom = 10.dp)
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(58.dp).clip(RoundedCornerShape(12.dp)).background(Color.White.copy(alpha = 0.06f))) {
            if (song.coverUrl != null) {
                AsyncImage(model = song.coverUrl, contentDescription = song.title, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
            } else {
                Icon(Icons.Rounded.MusicNote, null, tint = DiscoverTextSecondary)
            }
            if (loading) {
                Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.5f)), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp, modifier = Modifier.size(22.dp))
                }
            }
        }
        Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
            Text(song.title, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            if (song.artist.isNotBlank()) Text(song.artist, color = Color.White.copy(alpha = 0.8f), fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Row(verticalAlignment = Alignment.CenterVertically) {
                val date = if (song.shazamedAt > 0) DateFormat.getDateInstance(DateFormat.MEDIUM).format(Date(song.shazamedAt)) else ""
                Text(
                    listOf(date, if (song.timesShazamed > 1) "×${song.timesShazamed}" else "").filter { it.isNotBlank() }.joinToString(" · "),
                    color = DiscoverTextSecondary,
                    fontSize = 12.sp,
                    maxLines = 1
                )
                Spacer(Modifier.width(8.dp))
                when (song.status) {
                    ImportedSong.Status.Waiting -> StatusChip("Waiting", DiscoverTextSecondary)
                    ImportedSong.Status.NotFound -> StatusChip("Not found", DiscoverAccent)
                    ImportedSong.Status.Found -> Unit
                }
            }
        }
        Box {
            IconButton(onClick = { menuOpen = true }) {
                Icon(Icons.Rounded.MoreVert, contentDescription = "More options", tint = DiscoverTextSecondary)
            }
            DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                DropdownMenuItem(text = { Text("Play") }, leadingIcon = { Icon(Icons.Rounded.PlayArrow, null) }, onClick = { menuOpen = false; onOpen() })
                DropdownMenuItem(text = { Text("Search for other versions") }, leadingIcon = { Icon(Icons.Rounded.Search, null) }, onClick = { menuOpen = false; onSearch() })
                DropdownMenuItem(text = { Text("Remove") }, leadingIcon = { Icon(Icons.Rounded.Delete, null) }, onClick = { menuOpen = false; onRemove() })
            }
        }
    }
}

@Composable
private fun StatusChip(text: String, color: Color) {
    Text(
        text,
        color = color,
        fontSize = 11.sp,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .border(1.dp, color.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
            .padding(horizontal = 6.dp, vertical = 1.dp)
    )
}
