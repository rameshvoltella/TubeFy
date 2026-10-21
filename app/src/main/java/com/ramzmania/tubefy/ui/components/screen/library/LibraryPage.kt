package com.ramzmania.tubefy.ui.components.screen.library

import androidx.compose.foundation.background
import android.content.res.Configuration
import androidx.compose.ui.platform.LocalConfiguration
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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.DragIndicator
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.ramzmania.tubefy.R
import com.ramzmania.tubefy.core.YoutubeCoreConstant
import com.ramzmania.tubefy.core.download.Downloads
import com.ramzmania.tubefy.data.Resource
import com.ramzmania.tubefy.data.dto.base.searchformat.TubeFyCoreTypeData
import com.ramzmania.tubefy.ui.components.NavigationItem
import com.ramzmania.tubefy.ui.theme.DiscoverAccent
import com.ramzmania.tubefy.ui.theme.DiscoverAccentContainer
import com.ramzmania.tubefy.ui.theme.DiscoverBackground
import com.ramzmania.tubefy.ui.theme.DiscoverBorder
import com.ramzmania.tubefy.ui.theme.DiscoverSurface
import com.ramzmania.tubefy.ui.theme.DiscoverTextSecondary
import com.ramzmania.tubefy.utils.LocalNavController
import com.ramzmania.tubefy.viewmodel.TubeFyViewModel
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

private const val FAVORITES_PLAYLIST = "TubeFy-Favorites"
private val DeleteRed = Color(0xFFFF3B3B)

@Composable
fun LibraryDetailPage(viewModel: TubeFyViewModel = hiltViewModel()) {
    val gettingPlayListSongs by viewModel.gettingSongPlayListOperation.observeAsState()
    val navController = LocalNavController.current
    val playlistName = remember {
        navController.currentBackStackEntry?.arguments?.getString("playerListName").orEmpty()
    }
    val isFavorites = playlistName == FAVORITES_PLAYLIST
    val isDownloads = playlistName == Downloads.PLAYLIST
    val playListItems = when (val result = gettingPlayListSongs) {
        is Resource.Success -> result.data.orEmpty()
        else -> emptyList()
    }
    var searchActive by rememberSaveable { mutableStateOf(false) }
    var query by rememberSaveable { mutableStateOf("") }
    var showMenu by remember { mutableStateOf(false) }
    var confirmDeletePlaylist by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (isFavorites) viewModel.getFavorites() else viewModel.getSpecificPlayList(playlistName)
    }

    if (confirmDeletePlaylist) {
        DeleteSongConfirmationDialog(
            title = when { isFavorites -> "Clear favorites"; isDownloads -> "Delete all downloads"; else -> "Delete playlist" },
            message = when {
                isFavorites -> "Remove all songs from your favorites?"
                isDownloads -> "Delete all downloaded songs from this device?"
                else -> "Delete \"$playlistName\" and all its songs?"
            },
            onConfirm = {
                confirmDeletePlaylist = false
                deletePlayList(playlistName, viewModel)
                navController.popBackStack()
            },
            onDismiss = { confirmDeletePlaylist = false }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DiscoverBackground)
    ) {
        // Header: back, title + count, search, more
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 16.dp)
        ) {
            CircleIconButton(
                icon = Icons.AutoMirrored.Rounded.ArrowBack,
                description = "Back",
                tint = DiscoverAccent,
                background = DiscoverAccentContainer.copy(alpha = 0.55f),
                onClick = { navController.popBackStack() }
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp)
            ) {
                Text(
                    text = when {
                        isFavorites -> "Favorites"
                        isDownloads -> "Downloads"
                        else -> playlistName.trim()
                    },
                    color = Color.White,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = if (playListItems.size == 1) "1 song" else "${playListItems.size} songs",
                    color = DiscoverTextSecondary,
                    fontSize = 15.sp
                )
            }
            CircleIconButton(
                icon = if (searchActive) Icons.Rounded.Close else Icons.Rounded.Search,
                description = if (searchActive) "Close search" else "Search in playlist",
                onClick = {
                    searchActive = !searchActive
                    if (!searchActive) query = ""
                }
            )
            Spacer(modifier = Modifier.width(10.dp))
            Box {
                CircleIconButton(icon = Icons.Rounded.MoreVert, description = "More options", onClick = { showMenu = true })
                DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                    DropdownMenuItem(
                        text = { Text(when { isFavorites -> "Clear favorites"; isDownloads -> "Delete all downloads"; else -> "Delete playlist" }) },
                        leadingIcon = { Icon(Icons.Rounded.Delete, contentDescription = null) },
                        onClick = {
                            showMenu = false
                            confirmDeletePlaylist = true
                        }
                    )
                }
            }
        }

        if (searchActive) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                placeholder = { Text("Search in this playlist", color = DiscoverTextSecondary) },
                singleLine = true,
                textStyle = TextStyle(color = Color.White, fontSize = 16.sp),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = DiscoverAccent,
                    unfocusedBorderColor = DiscoverAccent.copy(alpha = 0.6f),
                    focusedContainerColor = DiscoverSurface,
                    unfocusedContainerColor = DiscoverSurface,
                    cursorColor = DiscoverAccent
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, top = 14.dp)
            )
        }

        if (playListItems.isNotEmpty()) {
            LibraryListBaseView(playListItems, playlistName, query, viewModel)
        } else if (gettingPlayListSongs != null) {
            Text(
                text = "No songs in this playlist yet",
                color = DiscoverTextSecondary,
                fontSize = 16.sp,
                modifier = Modifier.padding(20.dp)
            )
        }
    }
}

/**
 * Songs of a saved playlist (or favorites) with Play All, drag-and-drop reordering via the
 * handle, and delete. Reordering is disabled while a search [filter] is applied.
 */
@Composable
fun LibraryListBaseView(
    playListItems: List<TubeFyCoreTypeData?>,
    playlistName: String,
    filter: String = "",
    viewModel: TubeFyViewModel = hiltViewModel()
) {
    val newNav = LocalNavController.current
    val gettingActivePlayList by viewModel.addToActiveDatabase.observeAsState()
    var doLoadData by remember { mutableStateOf(false) }
    var clickedPosition by remember { mutableIntStateOf(0) }
    var songToDelete by remember { mutableStateOf<TubeFyCoreTypeData?>(null) }
    var songs by remember(playListItems) {
        mutableStateOf(playListItems.filterNotNull().distinctBy { it.videoId })
    }
    val visibleSongs = if (filter.isBlank()) songs else songs.filter { it.videoTitle.contains(filter.trim(), ignoreCase = true) }

    val lazyListState = rememberLazyListState()
    val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
    val reorderState = rememberReorderableLazyListState(lazyListState) { from, to ->
        songs = songs.toMutableList().apply { add(to.index, removeAt(from.index)) }
    }

    LaunchedEffect(key1 = gettingActivePlayList) {
        if (gettingActivePlayList is Resource.Success && doLoadData) {
            doLoadData = false
            songs.getOrNull(clickedPosition)?.let { song ->
                newNav.navigate(
                    NavigationItem.AudioPlayer.createRoute(
                        URLEncoder.encode(song.videoId, StandardCharsets.UTF_8.toString()),
                        URLEncoder.encode(YoutubeCoreConstant.decodeThumpUrl(song.videoImage), StandardCharsets.UTF_8.toString()),
                        URLEncoder.encode(song.videoTitle, StandardCharsets.UTF_8.toString()),
                        URLEncoder.encode(song.videoTitle, StandardCharsets.UTF_8.toString()),
                        URLEncoder.encode(song.videoTitle, StandardCharsets.UTF_8.toString()),
                        isBulk = true,
                        // Songs started from Downloads play offline, queueing only downloaded songs
                        offline = playlistName == Downloads.PLAYLIST
                    )
                ) {
                    newNav.graph.route?.let { route ->
                        popUpTo(route) {
                            saveState = true
                        }
                    }
                    launchSingleTop = true
                    restoreState = true
                }
            }
        }
    }

    fun play(index: Int) {
        if (songs.isEmpty()) return
        // Saved YouTube videos open in the video page
        if (playlistName == Downloads.YOUTUBE_VIDEOS) {
            val video = songs.getOrNull(index) ?: return
            fun enc(value: String) = URLEncoder.encode(value, StandardCharsets.UTF_8.toString())
            newNav.navigate(
                NavigationItem.Video.createRoute(
                    enc(YoutubeCoreConstant.extractYoutubeVideoId(video.videoId) ?: video.videoId),
                    enc(video.videoTitle),
                    enc(YoutubeCoreConstant.decodeThumpUrl(video.videoImage)),
                    enc(video.artist)
                )
            )
            return
        }
        // Counts towards Home's top playlists grid (own playlists, Favorites and Downloads)
        viewModel.recordCustomPlaylistPlayed(playlistName, songs.first().videoImage)
        clickedPosition = index
        doLoadData = true
        viewModel.setActiveSongsList(songs, index)
    }

    songToDelete?.let { song ->
        DeleteSongConfirmationDialog(
            onConfirm = {
                deleteSongFromPlayList(viewModel, playlistName, song.videoId)
                songToDelete = null
            },
            onDismiss = { songToDelete = null }
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Play All
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = if (isLandscape) 8.dp else 18.dp)
                .fillMaxWidth()
                .height(if (isLandscape) 46.dp else 58.dp)
                .shadow(14.dp, RoundedCornerShape(50), spotColor = Color(0xFFFF2D55))
                .clip(RoundedCornerShape(50))
                .background(Brush.horizontalGradient(listOf(Color(0xFFFF3B3B), Color(0xFFFF1F6E))))
                .clickable { play(0) }
        ) {
            Icon(Icons.Rounded.PlayArrow, contentDescription = null, tint = Color.White, modifier = Modifier.size(32.dp))
            Spacer(modifier = Modifier.width(10.dp))
            Text("Play All", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
        }

        LazyColumn(
            state = lazyListState,
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(visibleSongs, key = { it.videoId }) { song ->
                ReorderableItem(reorderState, key = song.videoId) { isDragging ->
                    LibraryItem(
                        trackName = song,
                        isDragging = isDragging,
                        reorderEnabled = filter.isBlank(),
                        dragHandleModifier = Modifier.draggableHandle(
                            onDragStopped = { viewModel.savePlaylistOrder(playlistName, songs) }
                        ),
                        onClick = { play(songs.indexOf(song).coerceAtLeast(0)) },
                        onDeleteClick = { songToDelete = it }
                    )
                }
            }
        }
    }
}

@Composable
fun LibraryItem(
    trackName: TubeFyCoreTypeData,
    isDragging: Boolean,
    reorderEnabled: Boolean,
    dragHandleModifier: Modifier,
    onClick: (TubeFyCoreTypeData) -> Unit,
    onDeleteClick: (TubeFyCoreTypeData) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .height(84.dp)
            .shadow(if (isDragging) 12.dp else 0.dp, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .background(if (isDragging) DiscoverAccentContainer else DiscoverSurface)
            .border(1.dp, DiscoverBorder, RoundedCornerShape(16.dp))
            .clickable { onClick(trackName) }
            .padding(end = 6.dp)
    ) {
        if (reorderEnabled) {
            Icon(
                Icons.Rounded.DragIndicator,
                contentDescription = "Drag to reorder",
                tint = DiscoverTextSecondary,
                modifier = dragHandleModifier
                    .padding(start = 8.dp, end = 4.dp)
                    .size(26.dp)
            )
        } else {
            Spacer(modifier = Modifier.width(12.dp))
        }
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(YoutubeCoreConstant.decodeThumpUrl(trackName.videoImage))
                .crossfade(true)
                .placeholder(R.drawable.placeholder)
                .error(R.drawable.placeholder)
                .build(),
            contentDescription = trackName.videoTitle,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(66.dp)
                .clip(RoundedCornerShape(10.dp))
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 14.dp)
        ) {
            Text(
                text = trackName.videoTitle,
                color = Color.White,
                fontSize = 16.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (trackName.artist.isNotBlank()) {
                Text(
                    text = trackName.artist,
                    color = DiscoverAccent.copy(alpha = 0.75f),
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 3.dp)
                )
            }
        }
        if (trackName.durationSeconds > 0) {
            Text(
                text = com.ramzmania.tubefy.ui.components.screen.album.formatDuration(trackName.durationSeconds),
                color = DiscoverTextSecondary,
                fontSize = 14.sp,
                modifier = Modifier.padding(end = 6.dp)
            )
        }
        Icon(
            Icons.Rounded.Delete,
            contentDescription = "Remove from playlist",
            tint = DeleteRed,
            modifier = Modifier
                .clip(CircleShape)
                .clickable { onDeleteClick(trackName) }
                .padding(10.dp)
                .size(24.dp)
        )
    }
}

@Composable
private fun CircleIconButton(
    icon: ImageVector,
    description: String,
    onClick: () -> Unit,
    tint: Color = Color.White,
    background: Color = DiscoverSurface
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(background)
            .clickable(onClick = onClick)
    ) {
        Icon(icon, contentDescription = description, tint = tint, modifier = Modifier.size(24.dp))
    }
}

@Composable
fun DeleteSongConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    title: String = "Delete Song",
    message: String = "Are you sure you want to delete this song from the playlist?"
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = title) },
        text = { Text(text = message) },
        confirmButton = {
            TextButton(onClick = onConfirm) { Text("Delete", color = DeleteRed) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = DiscoverTextSecondary) }
        }
    )
}

fun deleteSongFromPlayList(viewModel: TubeFyViewModel, playListName: String, videoId: String) {
    viewModel.removeSongFromSavedList(playListName, videoId)
}
