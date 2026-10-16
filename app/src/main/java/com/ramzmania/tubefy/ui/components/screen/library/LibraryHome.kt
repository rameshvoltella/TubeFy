package com.ramzmania.tubefy.ui.components.screen.library

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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.ramzmania.tubefy.R
import com.ramzmania.tubefy.core.YoutubeCoreConstant
import com.ramzmania.tubefy.data.Resource
import com.ramzmania.tubefy.data.dto.database.PlaylistNameWithUrl
import com.ramzmania.tubefy.ui.components.ChipRow
import com.ramzmania.tubefy.ui.components.NavigationItem
import com.ramzmania.tubefy.ui.theme.DiscoverAccent
import com.ramzmania.tubefy.ui.theme.DiscoverAccentContainer
import com.ramzmania.tubefy.ui.theme.DiscoverBackground
import com.ramzmania.tubefy.ui.theme.DiscoverBorder
import com.ramzmania.tubefy.ui.theme.DiscoverSurface
import com.ramzmania.tubefy.ui.theme.DiscoverTextSecondary
import com.ramzmania.tubefy.utils.LocalNavController
import com.ramzmania.tubefy.viewmodel.TubeFyViewModel
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

private const val FAVORITES_PLAYLIST = "TubeFy-Favorites"
private const val TAB_PLAYLISTS = "Playlists"
private const val TAB_FAVORITES = "Favorites"

@Composable
fun MyLibraryPage(viewModel: TubeFyViewModel = hiltViewModel()) {
    val gettingMyPlayList by viewModel.gettingPrivatePlayList.observeAsState()
    val reloadAllPlayList by viewModel.reloadAllPlayList.collectAsState()
    val favoriteSongs by viewModel.gettingSongPlayListOperation.observeAsState()
    var selectedTab by rememberSaveable { mutableStateOf(TAB_PLAYLISTS) }
    val playListItems = (gettingMyPlayList as? Resource.Success)?.data.orEmpty()

    LaunchedEffect(Unit) {
        viewModel.getAllSavedPlayList()
    }
    LaunchedEffect(reloadAllPlayList) {
        if (reloadAllPlayList) {
            viewModel.reloadAllCustomPlayListData(false)
            viewModel.getAllSavedPlayList()
        }
    }
    LaunchedEffect(selectedTab) {
        if (selectedTab == TAB_FAVORITES) viewModel.getFavorites()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DiscoverBackground)
    ) {
        Text(
            text = "Saved library",
            color = Color.White,
            fontSize = 38.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 20.dp)
        )
        Text(
            text = "Your playlists and favorites",
            color = DiscoverTextSecondary,
            fontSize = 16.sp,
            modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 8.dp)
        )
        ChipRow(
            chips = listOf(TAB_PLAYLISTS, TAB_FAVORITES),
            selected = selectedTab,
            onSelect = { selectedTab = it }
        )

        if (selectedTab == TAB_PLAYLISTS) {
            LibraryHomeView(playListItems.filterNotNull(), viewModel)
        } else {
            val songs = (favoriteSongs as? Resource.Success)?.data.orEmpty()
            if (songs.isNotEmpty()) {
                LibraryListBaseView(songs, FAVORITES_PLAYLIST)
            } else {
                Text(
                    text = "No liked songs yet. Tap ♥ on any song to save it here.",
                    color = DiscoverTextSecondary,
                    fontSize = 15.sp,
                    modifier = Modifier.padding(20.dp)
                )
            }
        }
    }
}

@Composable
fun LibraryHomeView(tracks: List<PlaylistNameWithUrl>, viewModel: TubeFyViewModel = hiltViewModel()) {
    val navController = LocalNavController.current
    var playlistToDelete by remember { mutableStateOf<String?>(null) }

    playlistToDelete?.let { name ->
        DeleteSongConfirmationDialog(
            onConfirm = {
                deletePlayList(name, viewModel)
                playlistToDelete = null
            },
            onDismiss = { playlistToDelete = null }
        )
    }

    LazyColumn(
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(tracks) { track ->
            LibraryHomeItem(
                playListItem = track,
                onClick = { selectedItem ->
                    navController.navigate(
                        NavigationItem.MyPlayList.createRoute(
                            URLEncoder.encode(
                                YoutubeCoreConstant.decodeThumpUrl(selectedItem.videoThump),
                                StandardCharsets.UTF_8.toString()
                            ), selectedItem.playlistName
                        )
                    ) {
                        navController.graph.route?.let { route ->
                            popUpTo(route) {
                                saveState = true
                            }
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onDeleteClick = { playlistToDelete = it.playlistName }
            )
        }
    }
}

@Composable
fun LibraryHomeItem(
    playListItem: PlaylistNameWithUrl,
    onClick: (PlaylistNameWithUrl) -> Unit,
    onDeleteClick: (PlaylistNameWithUrl) -> Unit
) {
    val isFavorites = playListItem.playlistName == FAVORITES_PLAYLIST
    var showMenu by remember { mutableStateOf(false) }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(DiscoverSurface)
            .border(1.dp, DiscoverBorder, RoundedCornerShape(18.dp))
            .clickable { onClick(playListItem) }
            .padding(12.dp)
    ) {
        if (isFavorites) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(84.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Brush.linearGradient(listOf(Color(0xFFD6334A), Color(0xFF3A1219))))
            ) {
                Icon(Icons.Rounded.Star, contentDescription = null, tint = Color(0xFFFFD54F), modifier = Modifier.size(44.dp))
            }
        } else {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(YoutubeCoreConstant.decodeThumpUrl(playListItem.videoThump))
                    .crossfade(true)
                    .placeholder(R.drawable.placeholder)
                    .error(R.drawable.placeholder)
                    .build(),
                contentDescription = playListItem.playlistName,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(84.dp)
                    .clip(RoundedCornerShape(14.dp))
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 16.dp, end = 8.dp)
        ) {
            Text(
                text = if (isFavorites) "Favorite" else playListItem.playlistName.trim().replaceFirstChar { it.uppercase() },
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = if (isFavorites) "All your liked songs" else "Your playlist",
                color = DiscoverTextSecondary,
                fontSize = 14.sp,
                maxLines = 1,
                modifier = Modifier.padding(top = 4.dp)
            )
            Text(
                text = if (playListItem.songCount == 1) "1 song" else "${playListItem.songCount} songs",
                color = DiscoverTextSecondary,
                fontSize = 13.sp,
                modifier = Modifier.padding(top = 6.dp)
            )
        }

        if (!isFavorites) {
            Box {
                Icon(
                    Icons.Rounded.MoreVert,
                    contentDescription = "More options",
                    tint = Color.White.copy(alpha = 0.85f),
                    modifier = Modifier
                        .clip(CircleShape)
                        .clickable { showMenu = true }
                        .padding(8.dp)
                        .size(22.dp)
                )
                DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                    DropdownMenuItem(
                        text = { Text("Delete playlist") },
                        leadingIcon = { Icon(Icons.Rounded.Delete, contentDescription = null) },
                        onClick = {
                            showMenu = false
                            onDeleteClick(playListItem)
                        }
                    )
                }
            }
        }
        Spacer(modifier = Modifier.size(6.dp))
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(DiscoverAccentContainer.copy(alpha = 0.6f))
        ) {
            Icon(
                Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                contentDescription = null,
                tint = DiscoverAccent,
                modifier = Modifier.size(26.dp)
            )
        }
    }
}

fun deletePlayList(playlistName: String, viewModel: TubeFyViewModel) {
    // The library list reloads once the delete has finished (see the ViewModel)
    if (playlistName == FAVORITES_PLAYLIST) {
        viewModel.deleteAllFavorites()
    } else {
        viewModel.deleteSpecificPlayList(playlistName)
    }
}
