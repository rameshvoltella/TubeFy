package com.ramzmania.tubefy.ui.components.screen.album

import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.QueueMusic
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.palette.graphics.Palette
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.ramzmania.tubefy.R
import com.ramzmania.tubefy.core.YoutubeCoreConstant
import com.ramzmania.tubefy.core.YoutubeCoreConstant.decodeThumpUrl
import com.ramzmania.tubefy.data.Resource
import com.ramzmania.tubefy.data.dto.base.searchformat.TubeFyCoreTypeData
import com.ramzmania.tubefy.ui.components.NavigationItem
import com.ramzmania.tubefy.ui.components.screen.library.PlayListDialogViewer
import com.ramzmania.tubefy.ui.components.screen.player.LoadBitmapAndExtractPalette
import com.ramzmania.tubefy.ui.theme.DiscoverBackground
import com.ramzmania.tubefy.ui.theme.DiscoverBorder
import com.ramzmania.tubefy.ui.theme.DiscoverSurface
import com.ramzmania.tubefy.ui.theme.DiscoverTextSecondary
import com.ramzmania.tubefy.utils.LocalNavController
import com.ramzmania.tubefy.viewmodel.TubeFyViewModel
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.Calendar

@Composable
fun AlbumScreen(viewModel: TubeFyViewModel = hiltViewModel()) {
    val playListData by viewModel.youTubePlayListData.observeAsState()
    val streamUrlData by viewModel.streamUrlData.observeAsState()
    val searchPlayListName by viewModel.youTubeSearchData.observeAsState()
    var isLoading by remember { mutableStateOf(true) }
    var videoAudioItems by remember { mutableStateOf<List<TubeFyCoreTypeData?>>(emptyList()) }
    val navController = LocalNavController.current
    val navBackStackEntry = remember { navController.currentBackStackEntry }
    val context = LocalContext.current
    var palette by remember { mutableStateOf<Palette?>(null) }
    var isDefaultDataLoaded by rememberSaveable { mutableStateOf(false) }

    val imageUrl = remember {
        URLDecoder.decode(
            navBackStackEntry?.arguments?.getString("playlistImage").orEmpty(),
            StandardCharsets.UTF_8.toString()
        )
    }
    // Titles can carry stray newlines from the feed; the title is also the saved playlist's name
    val title = remember { decodeArg(navBackStackEntry?.arguments?.getString("playlistName").orEmpty()).trim() }

    LaunchedEffect(Unit) {
        val playlistId = navBackStackEntry?.arguments?.getString("playlistId")
        if (!isDefaultDataLoaded) {
            playlistId?.let {
                if (playlistId.startsWith("PLAYLIST-ID-")) {
                    if (playlistId.startsWith("PLAYLIST-ID-YT")) {
                        viewModel.searchNewPipePage(
                            playlistId.replace("PLAYLIST-ID-", "") + " songs of " +
                                Calendar.getInstance().get(Calendar.YEAR),
                            mutableListOf()
                        )
                    } else {
                        viewModel.searchNewPipePage(
                            playlistId.replace("PLAYLIST-ID-", ""),
                            mutableListOf("music_songs")
                        )
                    }
                } else {
                    viewModel.loadPlayList(playlistId)
                }
            }
        }
        viewModel.checkAlbumSaved(title)
    }
    LoadBitmapAndExtractPalette(imageUrl) { palette = it }
    val tint = palette?.darkMutedSwatch?.rgb?.let { Color(it) }
        ?: palette?.dominantSwatch?.rgb?.let { Color(it) }
        ?: Color(0xFF1E2A33)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DiscoverBackground)
    ) {
        // Blurred artwork backdrop fading into black (blur applies on Android 12+)
        AsyncImage(
            model = decodeThumpUrl(imageUrl),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .height(560.dp)
                .blur(70.dp)
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(560.dp)
                .background(
                    Brush.verticalGradient(
                        listOf(tint.copy(alpha = 0.55f), DiscoverBackground.copy(alpha = 0.85f), DiscoverBackground)
                    )
                )
        )

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            item {
                AlbumHeader(
                    imageUrl = imageUrl,
                    title = title,
                    finalItems = videoAudioItems,
                    viewModel = viewModel
                )
            }
            if (isLoading) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = colorResource(id = R.color.tubefyred))
                    }
                }
            } else {
                items(videoAudioItems) { track ->
                    track?.let { TrackItem(trackName = it, viewModel = viewModel) }
                }
            }
            item { Spacer(modifier = Modifier.height(170.dp)) }
        }

        // Back button floats over the artwork
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .statusBarsPadding()
                .padding(start = 16.dp, top = 8.dp)
                .size(44.dp)
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.35f))
                .clickable { navController.popBackStack() }
        ) {
            Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back", tint = Color.White)
        }
    }

    TrackOptionsDialogHost(viewModel)

    LaunchedEffect(key1 = playListData) {
        if (playListData is Resource.Success) {
            videoAudioItems = playListData!!.data!!.playListVideoList!!
            isLoading = false
            isDefaultDataLoaded = true
            viewModel.setActiveSongsList(videoAudioItems)
        }
    }
    LaunchedEffect(key1 = streamUrlData) {
        if (streamUrlData is Resource.Success) {
            if (streamUrlData!!.data!!.streamUrl.isNotEmpty()) {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(streamUrlData!!.data!!.streamUrl))
                context.startActivity(intent)
            }
        }
    }
    LaunchedEffect(key1 = searchPlayListName) {
        if (searchPlayListName is Resource.Success) {
            videoAudioItems = searchPlayListName!!.data!!.youtubeSortedData.youtubeSortedList!!
            isLoading = false
            viewModel.setActiveSongsList(videoAudioItems)
        }
    }
}

@Composable
fun AlbumHeader(
    imageUrl: String,
    title: String,
    viewModel: TubeFyViewModel = hiltViewModel(),
    finalItems: List<TubeFyCoreTypeData?>
) {
    val newNav = LocalNavController.current
    val isSaved by viewModel.isAlbumSaved.collectAsState()
    val songs = finalItems.filterNotNull().filter { it.videoId.isNotEmpty() }

    Column(modifier = Modifier.fillMaxWidth()) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(decodeThumpUrl(imageUrl))
                .crossfade(true)
                .placeholder(R.drawable.placeholder)
                .error(R.drawable.placeholder)
                .build(),
            contentDescription = title,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .statusBarsPadding()
                .padding(top = 12.dp)
                .size(220.dp)
                .align(Alignment.CenterHorizontally)
                .shadow(24.dp, RoundedCornerShape(18.dp))
                .clip(RoundedCornerShape(18.dp))
        )
        Text(
            text = title,
            color = Color.White,
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold,
            lineHeight = 36.sp,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 24.dp)
        )
        albumSubtitle(songs)?.let {
            Text(
                text = it,
                color = DiscoverTextSecondary,
                fontSize = 16.sp,
                modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 6.dp)
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 22.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .weight(1f)
                    .height(54.dp)
                    .shadow(16.dp, RoundedCornerShape(50), spotColor = PlayRed)
                    .clip(RoundedCornerShape(50))
                    .background(PlayRed)
                    .clickable(enabled = finalItems.isNotEmpty()) { playAll(newNav, finalItems) }
            ) {
                Icon(Icons.Rounded.PlayArrow, contentDescription = null, tint = Color.White, modifier = Modifier.size(28.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Play All", color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
            }
            // Saves the whole album as a playlist in "Your Library" (share & download come later)
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(DiscoverSurface)
                    .border(1.dp, DiscoverBorder, CircleShape)
                    .clickable(enabled = songs.isNotEmpty()) { viewModel.toggleAlbumSaved(title, songs) }
            ) {
                Icon(
                    imageVector = if (isSaved) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                    contentDescription = if (isSaved) "Remove from Your Library" else "Save to Your Library",
                    tint = if (isSaved) PlayRed else Color.White,
                    modifier = Modifier.size(26.dp)
                )
            }
        }
    }
}

/** Hosts the "Add to playlist" dialog opened from a track's ⋮ button. */
@Composable
fun TrackOptionsDialogHost(viewModel: TubeFyViewModel) {
    val showPlayListDialog by viewModel.showPlayListDialog.collectAsState()
    val currentSongItemSelected by viewModel.selectedTrack.collectAsState()
    val selected = currentSongItemSelected
    if (showPlayListDialog && selected != null) {
        PlayListDialogViewer(
            viewModel = viewModel,
            onDismiss = { viewModel.showPlayListDialog(false) },
            selected
        )
    }
}

@Composable
fun AlbumTrackList(
    tracks: List<TubeFyCoreTypeData?>,
    viewModel: TubeFyViewModel = hiltViewModel()
) {
    TrackOptionsDialogHost(viewModel)
    LazyColumn {
        items(tracks) { track ->
            track?.let { TrackItem(trackName = it, viewModel = viewModel) }
        }
        item {
            Spacer(modifier = Modifier.height(140.dp))
        }
    }
}

@Composable
fun TrackItem(trackName: TubeFyCoreTypeData, viewModel: TubeFyViewModel = hiltViewModel()) {
    val newNav = LocalNavController.current
    val isPlaylist = trackName.videoId.isEmpty()

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .fillMaxWidth()
            .height(76.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(DiscoverSurface)
            .border(1.dp, DiscoverBorder, RoundedCornerShape(14.dp))
            .clickable { openTrack(newNav, trackName) }
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(decodeThumpUrl(trackName.videoImage))
                .crossfade(true)
                .placeholder(R.drawable.placeholder)
                .error(R.drawable.placeholder)
                .build(),
            contentDescription = trackName.videoTitle,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .padding(4.dp)
                .size(68.dp)
                .clip(RoundedCornerShape(10.dp))
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 12.dp, end = 8.dp)
        ) {
            Text(
                text = trackName.videoTitle,
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            val subtitle = if (isPlaylist) "Playlist" else trackName.artist
            if (subtitle.isNotEmpty()) {
                Text(
                    text = subtitle,
                    color = DiscoverTextSecondary,
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 3.dp)
                )
            }
        }
        if (isPlaylist) {
            Icon(
                Icons.AutoMirrored.Rounded.QueueMusic,
                contentDescription = "Playlist",
                tint = DiscoverTextSecondary,
                modifier = Modifier.size(20.dp)
            )
        } else if (trackName.durationSeconds > 0) {
            Text(
                text = formatDuration(trackName.durationSeconds),
                color = DiscoverTextSecondary,
                fontSize = 13.sp
            )
        }
        Icon(
            Icons.Rounded.MoreVert,
            contentDescription = "More options",
            tint = Color.White.copy(alpha = 0.85f),
            modifier = Modifier
                .padding(start = 4.dp, end = 6.dp)
                .clip(CircleShape)
                .clickable { viewModel.showPlayListDialog(true, trackName) }
                .padding(8.dp)
                .size(20.dp)
        )
    }
}

private val PlayRed = Color(0xFFFF1F3D)

private fun playAll(navController: NavController, finalItems: List<TubeFyCoreTypeData?>) {
    val first = finalItems.firstOrNull() ?: return
    val encodedVideoThumpUrl = URLEncoder.encode(decodeThumpUrl(first.videoImage), StandardCharsets.UTF_8.toString())
    val encodedVideoId = URLEncoder.encode(first.videoId, StandardCharsets.UTF_8.toString())
    val videoTitle = URLEncoder.encode(first.videoTitle, StandardCharsets.UTF_8.toString())
    navController.navigate(
        NavigationItem.AudioPlayer.createRoute(
            encodedVideoId,
            encodedVideoThumpUrl, videoTitle, videoTitle, videoTitle, true
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
}

/** Opens a song in the player, or a playlist result in the album screen. */
fun openTrack(navController: NavController, trackName: TubeFyCoreTypeData) {
    val encodedVideoThumpUrl = URLEncoder.encode(decodeThumpUrl(trackName.videoImage), StandardCharsets.UTF_8.toString())
    val videoTitle = URLEncoder.encode(trackName.videoTitle, StandardCharsets.UTF_8.toString())

    if (trackName.videoId.length > 10) {
        val encodedVideoId = URLEncoder.encode(trackName.videoId, StandardCharsets.UTF_8.toString())
        navController.navigate(
            NavigationItem.AudioPlayer.createRoute(
                encodedVideoId,
                encodedVideoThumpUrl, videoTitle, videoTitle, videoTitle
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
    } else {
        navController.navigate(
            NavigationItem.PlayList.createRoute(
                URLEncoder.encode(
                    YoutubeCoreConstant.extractPlaylistId(trackName.plaListUrl!!),
                    StandardCharsets.UTF_8.toString()
                ),
                videoTitle,
                encodedVideoThumpUrl
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
    }
}

/** "50 songs • 2h 48m" (duration only when known). */
private fun albumSubtitle(songs: List<TubeFyCoreTypeData>): String? {
    if (songs.isEmpty()) return null
    val parts = mutableListOf(if (songs.size == 1) "1 song" else "${songs.size} songs")
    val totalSeconds = songs.sumOf { it.durationSeconds.coerceAtLeast(0) }
    if (totalSeconds > 0) {
        val hours = totalSeconds / 3600
        val minutes = totalSeconds / 60 % 60
        parts += if (hours > 0) "${hours}h ${minutes}m" else "${minutes}m"
    }
    return parts.joinToString(" • ")
}

fun formatDuration(seconds: Long): String {
    val hours = seconds / 3600
    val minutes = seconds / 60 % 60
    val secs = seconds % 60
    return if (hours > 0) "%d:%02d:%02d".format(hours, minutes, secs) else "%d:%02d".format(minutes, secs)
}

/** Some callers URL-encode the playlist name (spaces as '+'), others pass it raw. */
private fun decodeArg(value: String): String = try {
    URLDecoder.decode(value, StandardCharsets.UTF_8.toString())
} catch (e: IllegalArgumentException) {
    value
}

@Preview
@Composable
fun PreviewAlbumScreen() {
    TrackItem(
        TubeFyCoreTypeData(
            "",
            "sbksdnbcjhsabdjcaskjncdkadsnkcnsdkndckdsnvkcsnjndkfjndsknvksdnkvnskdc",
            "ascs",
            "dcs"
        )
    )
}
