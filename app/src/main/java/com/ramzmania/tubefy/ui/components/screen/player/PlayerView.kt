package com.ramzmania.tubefy.ui.components.screen.player

import android.content.Context
import android.content.Intent
import android.widget.Toast
import android.os.Bundle
import android.content.res.Configuration
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.core.os.bundleOf
import androidx.media3.session.SessionCommand
import com.ramzmania.tubefy.player.SleepTimer
import com.ramzmania.tubefy.player.SmartShuffle
import androidx.compose.material.icons.rounded.AutoAwesome
import com.ramzmania.tubefy.ui.theme.DiscoverAccent
import androidx.annotation.OptIn
import androidx.compose.animation.Crossfade
import androidx.compose.ui.draw.rotate
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.PlaylistAdd
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.DownloadDone
import androidx.compose.runtime.State
import androidx.compose.runtime.produceState
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.ramzmania.tubefy.core.download.DownloadWorker
import com.ramzmania.tubefy.core.download.Downloads
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Repeat
import androidx.compose.material.icons.rounded.RepeatOne
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaController
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import androidx.palette.graphics.Palette
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.ramzmania.tubefy.R
import com.ramzmania.tubefy.core.YoutubeCoreConstant
import com.ramzmania.tubefy.data.dto.base.searchformat.TubeFyCoreTypeData
import com.ramzmania.tubefy.database.FavoritePlaylist
import com.ramzmania.tubefy.player.PlaybackService
import com.ramzmania.tubefy.ui.components.screen.library.PlayListDialogViewer
import com.ramzmania.tubefy.utils.LocalNavController
import com.ramzmania.tubefy.viewmodel.TubeFyViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import kotlin.math.abs
import kotlin.math.roundToInt

private enum class PlayerMode { Song, Video }

private val FavouriteRed = Color(0xFFFF4D4D)
private val SurfaceTint = Color.White.copy(alpha = 0.08f)
private val SecondaryText = Color.White.copy(alpha = 0.62f)

@kotlin.OptIn(ExperimentalFoundationApi::class)
@Composable
fun PlayerBaseView(viewModel: TubeFyViewModel = hiltViewModel()) {
    val navController = LocalNavController.current
    val context = LocalContext.current
    val navArgs = remember { navController.currentBackStackEntry?.arguments }
    fun navArg(key: String) =
        URLDecoder.decode(navArgs?.getString(key) ?: "", StandardCharsets.UTF_8.toString())

    val isBulk = remember { navArgs?.getString("isBulk") == "true" }
    // Opened from the Downloads playlist: play the downloaded files only
    val startOffline = remember { navArgs?.getString("isBulk") == "offline" }
    var playingOffline by remember { mutableStateOf(startOffline) }
    var videoId by remember { mutableStateOf(navArg("videoId")) }
    var albumArt by remember {
        mutableStateOf("https://i.ytimg.com/vi/${YoutubeCoreConstant.extractYoutubeVideoId(videoId)}/hq720.jpg")
    }
    var title by remember { mutableStateOf(navArg("playerHeader")) }
    var artist by remember { mutableStateOf(cleanArtist(navArg("playerBottomSub"), title)) }

    var mediaController by remember { mutableStateOf<MediaController?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var isPlaying by remember { mutableStateOf(false) }
    var positionMs by remember { mutableLongStateOf(0L) }
    var durationMs by remember { mutableLongStateOf(0L) }
    var seekFraction by remember { mutableStateOf<Float?>(null) } // non-null while the user drags the slider
    var shuffleOn by remember { mutableStateOf(false) }
    var repeatMode by remember { mutableIntStateOf(Player.REPEAT_MODE_OFF) }
    var nextItem by remember { mutableStateOf<MediaItem?>(null) }
    var playerMode by rememberSaveable { mutableStateOf(PlayerMode.Song) }
    var showPlayListDialog by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }
    var palette by remember { mutableStateOf<Palette?>(null) }
    val isFavouriteSong by viewModel.isFavouriteState.collectAsState()
    val downloadState by rememberDownloadState(videoId)
    val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
    var showSleepSheet by remember { mutableStateOf(false) }
    var sleepEndAt by remember { mutableLongStateOf(0L) }
    var sleepEndOfSong by remember { mutableStateOf(false) }
    var smartShuffleOn by remember { mutableStateOf(false) }
    var nowMs by remember { mutableLongStateOf(System.currentTimeMillis()) }
    val sleepRemainingMs = if (sleepEndAt > 0) (sleepEndAt - nowMs).coerceAtLeast(0L) else 0L
    val sleepActive = sleepEndOfSong || sleepRemainingMs > 0

    LoadBitmapAndExtractPalette(albumArt) { palette = it }
    val tint by animateColorAsState(
        palette?.darkMutedSwatch?.rgb?.let { Color(it) }
            ?: palette?.dominantSwatch?.rgb?.let { Color(it) }
            ?: Color(0xFF2A2A2A),
        label = "tint"
    )

    LaunchedEffect(videoId) {
        YoutubeCoreConstant.extractYoutubeVideoId(videoId)?.let { viewModel.isFavourite(it) }
    }
    LaunchedEffect(playingOffline) {
        if (playingOffline) playerMode = PlayerMode.Song
    }

    // Position ticker
    LaunchedEffect(mediaController, isPlaying) {
        val controller = mediaController ?: return@LaunchedEffect
        do {
            positionMs = controller.currentPosition
            if (controller.duration > 0) durationMs = controller.duration
            delay(500L)
        } while (isPlaying)
    }

    // Sleep timer state is published by the playback service as session extras
    LaunchedEffect(mediaController) {
        val controller = mediaController ?: return@LaunchedEffect
        while (true) {
            val extras = controller.sessionExtras
            sleepEndAt = extras.getLong(SleepTimer.EXTRA_END_AT, 0L)
            sleepEndOfSong = extras.getBoolean(SleepTimer.EXTRA_END_OF_SONG, false)
            smartShuffleOn = extras.getBoolean(SmartShuffle.EXTRA_ENABLED, false)
            nowMs = System.currentTimeMillis()
            delay(1000L)
        }
    }

    fun sendSleepCommand(action: String, args: Bundle = Bundle.EMPTY) {
        mediaController?.sendCustomCommand(SessionCommand(action, Bundle.EMPTY), args)
        showSleepSheet = false
    }

    if (showSleepSheet) {
        SleepTimerSheet(
            active = sleepActive,
            status = when {
                sleepEndOfSong -> "Stopping at the end of this song"
                sleepRemainingMs > 0 -> "Stopping in ${formatTime(sleepRemainingMs, 0)}"
                else -> null
            },
            onMinutes = { minutes -> sendSleepCommand(SleepTimer.CMD_SET, bundleOf(SleepTimer.KEY_MINUTES to minutes)) },
            onEndOfSong = { sendSleepCommand(SleepTimer.CMD_SET, bundleOf(SleepTimer.KEY_END_OF_SONG to true)) },
            onExtend = { sendSleepCommand(SleepTimer.CMD_EXTEND, bundleOf(SleepTimer.KEY_MINUTES to 5)) },
            onCancel = { sendSleepCommand(SleepTimer.CMD_CANCEL) },
            onDismiss = { showSleepSheet = false }
        )
    }

    // Keep shuffle / repeat / up next in sync with the session player
    DisposableEffect(mediaController) {
        val controller = mediaController ?: return@DisposableEffect onDispose {}
        fun sync(player: Player) {
            if (player.currentMediaItem != null) {
                playingOffline = player.mediaMetadata.extras?.getBoolean(Downloads.OFFLINE_EXTRA) == true
            }
            shuffleOn = player.shuffleModeEnabled
            repeatMode = player.repeatMode
            nextItem = if (player.hasNextMediaItem()) player.getMediaItemAt(player.nextMediaItemIndex) else null
            if (player.duration > 0) durationMs = player.duration
            positionMs = player.currentPosition
        }
        val listener = object : Player.Listener {
            override fun onEvents(player: Player, events: Player.Events) = sync(player)
        }
        controller.addListener(listener)
        sync(controller)
        onDispose { controller.removeListener(listener) }
    }

    MediaControllerComposable(
        onPlayingChanged = { playing ->
            isPlaying = playing
            if (playing) isLoading = false
        },
        onMediaControllerInitialized = { controller ->
            mediaController = controller
            isPlaying = controller.isPlaying
            val currentId = controller.currentMediaItem?.mediaId
            if (startOffline) {
                startPlayDownloadsService(context)
            } else if (isBulk) {
                startFetchPlayListService(context)
            } else if (controller.isConnected && currentId != null &&
                currentId == YoutubeCoreConstant.extractYoutubeVideoId(videoId)
            ) {
                // Already playing this song: just reflect its state
                isLoading = false
                controller.mediaMetadata.let { meta ->
                    title = meta.title?.toString() ?: title
                    artist = cleanArtist(meta.artist?.toString(), title)
                }
            } else {
                isLoading = true
                startFetchSongService(context, videoId, albumArt, title)
            }
        },
        onMetaDataChangedValue = { meta: MediaMetadata ->
            meta.artworkUri?.toString()?.let { albumArt = it }
            mediaController?.currentMediaItem?.mediaId?.let { videoId = it }
            title = meta.title?.toString() ?: title
            artist = cleanArtist(meta.artist?.toString(), title)
        }
    )

    if (showPlayListDialog) {
        val current = mediaController?.currentMediaItem
        if (current != null) {
            PlayListDialogViewer(
                viewModel = viewModel,
                onDismiss = { showPlayListDialog = false },
                TubeFyCoreTypeData(videoId = current.mediaId, videoImage = albumArt, videoTitle = title)
            )
        } else {
            showPlayListDialog = false
        }
    }

    fun toggleFavourite() {
        val id = YoutubeCoreConstant.extractYoutubeVideoId(videoId) ?: return
        if (isFavouriteSong) {
            viewModel.removeFromFavorites(id)
        } else {
            viewModel.addToFavorites(FavoritePlaylist(videoId = id, videoThump = albumArt, videoName = title))
        }
    }


    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Blurred artwork backdrop (blur is applied on Android 12+, the scrim keeps older versions readable)
        AsyncImage(
            model = YoutubeCoreConstant.decodeThumpUrl(albumArt),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .blur(80.dp)
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            tint.copy(alpha = 0.75f),
                            Color.Black.copy(alpha = 0.82f),
                            Color.Black.copy(alpha = 0.94f)
                        )
                    )
                )
        )

        // Pieces shared by the portrait and landscape layouts
        val topBar: @Composable () -> Unit = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.Rounded.KeyboardArrowDown, "Close player", tint = Color.White, modifier = Modifier.size(30.dp))
                }
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    if (playingOffline) {
                        OfflineBadge()
                    } else {
                        SongVideoToggle(
                            selected = playerMode,
                            onSelect = { mode ->
                                playerMode = mode
                                if (mode == PlayerMode.Video) mediaController?.play()
                            }
                        )
                    }
                }
                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Rounded.MoreVert, "More options", tint = Color.White)
                    }
                    DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                        DropdownMenuItem(
                            text = { Text("Add to playlist") },
                            leadingIcon = { Icon(Icons.AutoMirrored.Rounded.PlaylistAdd, null) },
                            onClick = {
                                showMenu = false
                                showPlayListDialog = true
                            }
                        )
                    }
                }
            }
        }
        val artwork: @Composable (Modifier) -> Unit = { boxModifier ->
            Box(modifier = boxModifier, contentAlignment = Alignment.Center) {
                Crossfade(targetState = playerMode, label = "playerMode") { mode ->
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        when (mode) {
                            PlayerMode.Song -> AsyncImage(
                                model = ImageRequest.Builder(context)
                                    .data(YoutubeCoreConstant.decodeThumpUrl(albumArt))
                                    .crossfade(true)
                                    .placeholder(R.drawable.placeholder)
                                    .error(R.drawable.placeholder)
                                    .build(),
                                contentDescription = "Album art",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .aspectRatio(1f, matchHeightConstraintsFirst = true)
                                    .clip(RoundedCornerShape(24.dp))
                            )

                            PlayerMode.Video -> SessionVideoSurface(
                                controller = mediaController,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(16f / 9f)
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(Color.Black)
                            )
                        }
                    }
                }
            }
        }
        val details: @Composable () -> Unit = {
            // Title, artist, favourite
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        color = Color.White,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        modifier = Modifier.basicMarquee()
                    )
                    if (artist.isNotEmpty()) {
                        Text(
                            text = artist,
                            color = SecondaryText,
                            fontSize = 18.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
                IconButton(onClick = ::toggleFavourite) {
                    Icon(
                        imageVector = if (isFavouriteSong) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                        contentDescription = if (isFavouriteSong) "Remove from favourites" else "Add to favourites",
                        tint = if (isFavouriteSong) FavouriteRed else Color.White,
                        modifier = Modifier.size(30.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Seek bar
            val shownFraction = seekFraction
                ?: if (durationMs > 0) (positionMs.toFloat() / durationMs).coerceIn(0f, 1f) else 0f
            SeekBar(
                fraction = shownFraction,
                onSeek = { seekFraction = it },
                onSeekFinished = {
                    val target = seekFraction
                    val controller = mediaController
                    if (target != null && controller != null && controller.duration > 0) {
                        val newPosition = (target * controller.duration).toLong()
                        controller.seekTo(newPosition)
                        positionMs = newPosition
                    }
                    seekFraction = null
                }
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val labelPosition = if (shownFraction > 0f && durationMs > 0) (shownFraction * durationMs).toLong() else positionMs
                Text(formatTime(labelPosition, durationMs), color = SecondaryText, fontSize = 13.sp)
                Text(formatTime(durationMs, durationMs), color = SecondaryText, fontSize = 13.sp)
            }

            // Transport controls
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Off -> Shuffle -> Smart Shuffle -> Off
                ShuffleButton(
                    shuffleOn = shuffleOn,
                    smartOn = smartShuffleOn,
                    onClick = {
                        val controller = mediaController ?: return@ShuffleButton
                        when {
                            smartShuffleOn -> {
                                smartShuffleOn = false
                                controller.sendCustomCommand(
                                    SessionCommand(SmartShuffle.CMD_SET, Bundle.EMPTY),
                                    bundleOf(SmartShuffle.KEY_ENABLED to false)
                                )
                                controller.shuffleModeEnabled = false
                                Toast.makeText(context, "Shuffle off", Toast.LENGTH_SHORT).show()
                            }
                            shuffleOn -> {
                                smartShuffleOn = true
                                controller.sendCustomCommand(
                                    SessionCommand(SmartShuffle.CMD_SET, Bundle.EMPTY),
                                    bundleOf(SmartShuffle.KEY_ENABLED to true)
                                )
                                Toast.makeText(context, "Smart Shuffle: similar songs mixed in", Toast.LENGTH_SHORT).show()
                            }
                            else -> {
                                controller.shuffleModeEnabled = true
                                Toast.makeText(context, "Shuffle on", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                )
                IconButton(onClick = { mediaController?.seekToPrevious() }, modifier = Modifier.size(56.dp)) {
                    Icon(Icons.Rounded.SkipPrevious, "Previous", tint = Color.White, modifier = Modifier.size(42.dp))
                }
                Box(
                    modifier = Modifier
                        .size(84.dp)
                        .clip(CircleShape)
                        .border(1.5.dp, Color.White.copy(alpha = 0.7f), CircleShape)
                        .clickable(enabled = !isLoading) {
                            mediaController?.takeIf { it.isConnected }?.let {
                                if (isPlaying) it.pause() else it.play()
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp, modifier = Modifier.size(34.dp))
                    } else {
                        Icon(
                            imageVector = if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                            contentDescription = if (isPlaying) "Pause" else "Play",
                            tint = Color.White,
                            modifier = Modifier.size(46.dp)
                        )
                    }
                }
                IconButton(
                    onClick = { mediaController?.takeIf { it.hasNextMediaItem() }?.seekToNextMediaItem() },
                    modifier = Modifier.size(56.dp)
                ) {
                    Icon(Icons.Rounded.SkipNext, "Next", tint = Color.White, modifier = Modifier.size(42.dp))
                }
                ModeButton(
                    icon = if (repeatMode == Player.REPEAT_MODE_ONE) Icons.Rounded.RepeatOne else Icons.Rounded.Repeat,
                    description = "Repeat",
                    active = repeatMode != Player.REPEAT_MODE_OFF,
                    onClick = {
                        mediaController?.let {
                            it.repeatMode = when (it.repeatMode) {
                                Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ALL
                                Player.REPEAT_MODE_ALL -> Player.REPEAT_MODE_ONE
                                else -> Player.REPEAT_MODE_OFF
                            }
                        }
                    }
                )
            }

            // Quick actions
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                ActionTile(
                    icon = if (isFavouriteSong) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                    label = "Like",
                    iconTint = if (isFavouriteSong) FavouriteRed else Color.White,
                    onClick = ::toggleFavourite
                )
                DownloadTile(
                    state = downloadState,
                    onClick = {
                        when (downloadState) {
                            DownloadState.Done -> Toast.makeText(context, "Already downloaded", Toast.LENGTH_SHORT).show()
                            is DownloadState.Downloading -> Toast.makeText(context, "Downloading…", Toast.LENGTH_SHORT).show()
                            else -> {
                                Downloads.enqueue(context, videoId, title, albumArt)
                                Toast.makeText(context, "Download started", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                )
                ActionTile(Icons.AutoMirrored.Rounded.PlaylistAdd, "Playlist", onClick = { showPlayListDialog = true })
                ActionTile(
                    icon = Icons.Rounded.Timer,
                    label = when {
                        sleepEndOfSong -> "End of song"
                        sleepRemainingMs > 0 -> formatTime(sleepRemainingMs, 0)
                        else -> "Sleep timer"
                    },
                    onClick = { showSleepSheet = true },
                    iconTint = if (sleepActive) DiscoverAccent else Color.White
                )
            }
        }

        Column(modifier = Modifier.fillMaxSize()) {
            if (isLandscape) {
                // Landscape: artwork / video on the left, scrollable details on the right
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal))
                        .padding(horizontal = 20.dp)
                ) {
                    artwork(
                        Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .padding(vertical = 16.dp)
                    )
                    Spacer(modifier = Modifier.width(24.dp))
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .verticalScroll(rememberScrollState())
                    ) {
                        topBar()
                        details()
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 20.dp)
                ) {
                    topBar()
                    artwork(
                        Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(vertical = 20.dp)
                    )
                    details()
                }
            }

            UpNextBar(
                nextItem = nextItem,
                onSwipe = { mediaController?.takeIf { it.hasNextMediaItem() }?.seekToNextMediaItem() }
            )
        }
    }
}

@Composable
private fun SongVideoToggle(selected: PlayerMode, onSelect: (PlayerMode) -> Unit) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(SurfaceTint)
            .padding(4.dp)
    ) {
        PlayerMode.entries.forEach { mode ->
            val isSelected = mode == selected
            val background by animateColorAsState(
                if (isSelected) Color.White.copy(alpha = 0.18f) else Color.Transparent, label = "toggleBg"
            )
            Text(
                text = mode.name,
                color = if (isSelected) Color.White else SecondaryText,
                fontSize = 15.sp,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(background)
                    .clickable { onSelect(mode) }
                    .padding(horizontal = 22.dp, vertical = 8.dp)
            )
        }
    }
}

/**
 * Renders the video track of the song already playing in [PlaybackService], so switching
 * between Song and Video never restarts or rebuffers playback.
 */
@OptIn(UnstableApi::class)
@Composable
private fun SessionVideoSurface(controller: MediaController?, modifier: Modifier = Modifier) {
    AndroidView(
        factory = { ctx ->
            PlayerView(ctx).apply {
                useController = false
                resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                setShutterBackgroundColor(android.graphics.Color.BLACK)
                setKeepContentOnPlayerReset(true)
                // Screen stays awake while the Video tab is showing; released when this view goes away
                keepScreenOn = true
            }
        },
        update = { it.player = controller },
        onRelease = { it.player = null },
        modifier = modifier
    )
}

@kotlin.OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SeekBar(fraction: Float, onSeek: (Float) -> Unit, onSeekFinished: () -> Unit) {
    Slider(
        value = fraction,
        onValueChange = onSeek,
        onValueChangeFinished = onSeekFinished,
        colors = SliderDefaults.colors(thumbColor = Color.White),
        thumb = {
            Box(
                modifier = Modifier
                    .size(14.dp)
                    .background(Color.White, CircleShape)
            )
        },
        track = { state ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(50))
                    .background(Color.White.copy(alpha = 0.2f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(state.value.coerceIn(0f, 1f))
                        .fillMaxHeight()
                        .background(Color.White)
                )
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(28.dp)
    )
}

@Composable
private fun ShuffleButton(shuffleOn: Boolean, smartOn: Boolean, onClick: () -> Unit) {
    val active = shuffleOn || smartOn
    IconButton(onClick = onClick) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = Icons.Rounded.Shuffle,
                contentDescription = when {
                    smartOn -> "Smart Shuffle on"
                    shuffleOn -> "Shuffle on"
                    else -> "Shuffle off"
                },
                tint = when {
                    smartOn -> DiscoverAccent
                    shuffleOn -> Color.White
                    else -> Color.White.copy(alpha = 0.5f)
                },
                modifier = Modifier.size(26.dp)
            )
            if (smartOn) {
                Icon(
                    imageVector = Icons.Rounded.AutoAwesome,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier
                        .offset(x = 12.dp, y = (-11).dp)
                        .size(12.dp)
                )
            }
            if (active) {
                Box(
                    modifier = Modifier
                        .offset(y = 18.dp)
                        .size(4.dp)
                        .background(if (smartOn) DiscoverAccent else Color.White, CircleShape)
                )
            }
        }
    }
}

@Composable
private fun ModeButton(icon: ImageVector, description: String, active: Boolean, onClick: () -> Unit) {
    IconButton(onClick = onClick) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = description,
                tint = if (active) Color.White else Color.White.copy(alpha = 0.5f),
                modifier = Modifier.size(26.dp)
            )
            if (active) {
                Box(
                    modifier = Modifier
                        .offset(y = 18.dp)
                        .size(4.dp)
                        .background(Color.White, CircleShape)
                )
            }
        }
    }
}

/** Sleep timer options: presets and end-of-song when idle; extend or turn off while running. */
@kotlin.OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SleepTimerSheet(
    active: Boolean,
    status: String?,
    onMinutes: (Int) -> Unit,
    onEndOfSong: () -> Unit,
    onExtend: () -> Unit,
    onCancel: () -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = Color(0xFF121011)
    ) {
        Column(
            modifier = Modifier
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 16.dp)
        ) {
            Text(
                text = "Sleep timer",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 24.dp)
            )
            Text(
                text = status ?: "Music fades out and pauses when the timer ends",
                color = if (active) DiscoverAccent else SecondaryText,
                fontSize = 14.sp,
                modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 4.dp, bottom = 12.dp)
            )
            if (active) {
                SleepOption("+5 minutes", onClick = onExtend)
                SleepOption("Turn off timer", color = DiscoverAccent, onClick = onCancel)
            } else {
                listOf(15, 30, 45, 60).forEach { minutes ->
                    SleepOption(if (minutes == 60) "1 hour" else "$minutes minutes") { onMinutes(minutes) }
                }
                SleepOption("End of this song", onClick = onEndOfSong)
            }
        }
    }
}

@Composable
private fun SleepOption(label: String, color: Color = Color.White, onClick: () -> Unit) {
    Text(
        text = label,
        color = color,
        fontSize = 17.sp,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 15.dp)
    )
}

private sealed interface DownloadState {
    data object Idle : DownloadState
    data object Failed : DownloadState
    data class Downloading(val progress: Int) : DownloadState
    data object Done : DownloadState
}

private val DownloadedGreen = Color(0xFF2ECC71)

/** Tracks the song's file and its download work, so the button reflects progress and completion. */
@Composable
private fun rememberDownloadState(videoId: String): State<DownloadState> {
    val context = LocalContext.current
    return produceState<DownloadState>(
        initialValue = if (videoId.isNotBlank() && Downloads.isDownloaded(context, videoId)) DownloadState.Done else DownloadState.Idle,
        key1 = videoId
    ) {
        if (videoId.isBlank()) return@produceState
        WorkManager.getInstance(context)
            .getWorkInfosForUniqueWorkFlow(Downloads.workName(videoId))
            .collect { infos ->
                val info = infos.firstOrNull()
                value = when {
                    info?.state == WorkInfo.State.RUNNING ->
                        DownloadState.Downloading(info.progress.getInt(DownloadWorker.KEY_PROGRESS, 0))
                    info?.state == WorkInfo.State.ENQUEUED || info?.state == WorkInfo.State.BLOCKED ->
                        DownloadState.Downloading(0)
                    Downloads.isDownloaded(context, videoId) -> DownloadState.Done
                    info?.state == WorkInfo.State.FAILED -> DownloadState.Failed
                    else -> DownloadState.Idle
                }
            }
    }
}

@Composable
private fun DownloadTile(state: DownloadState, onClick: () -> Unit) {
    when (state) {
        DownloadState.Done -> ActionTile(Icons.Rounded.DownloadDone, "Downloaded", onClick, iconTint = DownloadedGreen)
        DownloadState.Failed -> ActionTile(Icons.Rounded.Download, "Retry", onClick)
        DownloadState.Idle -> ActionTile(Icons.Rounded.Download, "Download", onClick)
        is DownloadState.Downloading -> ActionTile(
            icon = Icons.Rounded.Download,
            label = if (state.progress > 0) "${state.progress}%" else "Waiting…",
            onClick = onClick,
            content = {
                if (state.progress > 0) {
                    CircularProgressIndicator(
                        progress = { state.progress / 100f },
                        color = DownloadedGreen,
                        trackColor = Color.White.copy(alpha = 0.15f),
                        strokeWidth = 3.dp,
                        modifier = Modifier.size(30.dp)
                    )
                } else {
                    CircularProgressIndicator(color = DownloadedGreen, strokeWidth = 3.dp, modifier = Modifier.size(30.dp))
                }
            }
        )
    }
}

/** Shown instead of the Song/Video toggle while playing downloaded files. */
@Composable
private fun OfflineBadge() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(SurfaceTint)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Icon(Icons.Rounded.DownloadDone, contentDescription = null, tint = DownloadedGreen, modifier = Modifier.size(18.dp))
        Text("Offline", color = Color.White, fontSize = 15.sp, modifier = Modifier.padding(start = 6.dp))
    }
}

@Composable
private fun ActionTile(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    iconTint: Color = Color.White,
    content: (@Composable () -> Unit)? = null
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(76.dp)
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(SurfaceTint)
                .border(1.dp, Color.White.copy(alpha = 0.06f), RoundedCornerShape(18.dp))
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            if (content != null) content() else Icon(icon, contentDescription = label, tint = iconTint, modifier = Modifier.size(26.dp))
        }
        Text(
            text = label,
            color = Color.White.copy(alpha = 0.85f),
            fontSize = 13.sp,
            maxLines = 1,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}

/**
 * Bottom "Up next" panel, collapsed to a small "⌃ Up next" tab. Tapping it expands the next
 * song (swipe it sideways to play it); tapping again collapses it.
 */
@Composable
private fun UpNextBar(nextItem: MediaItem?, onSwipe: () -> Unit) {
    val scope = rememberCoroutineScope()
    val offsetX = remember { Animatable(0f) }
    var barWidth by remember { mutableFloatStateOf(1f) }
    val hasNext = nextItem != null
    var expanded by rememberSaveable { mutableStateOf(false) }
    val arrowRotation by animateFloatAsState(if (expanded) 180f else 0f, label = "upNextArrow")

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp)
            .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
            .background(SurfaceTint)
            .navigationBarsPadding()
            .animateContentSize()
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
                .padding(top = 6.dp, bottom = if (expanded) 4.dp else 10.dp)
        ) {
            Icon(
                Icons.Rounded.KeyboardArrowUp,
                contentDescription = if (expanded) "Hide up next" else "Show up next",
                tint = SecondaryText,
                modifier = Modifier
                    .size(20.dp)
                    .rotate(arrowRotation)
            )
            Text("Up next", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Medium)
        }

        if (expanded) Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 12.dp)
                .onSizeChanged { barWidth = it.width.toFloat() }
                .offset { IntOffset(offsetX.value.roundToInt(), 0) }
                .draggable(
                    enabled = hasNext,
                    orientation = Orientation.Horizontal,
                    state = rememberDraggableState { delta ->
                        scope.launch { offsetX.snapTo(offsetX.value + delta) }
                    },
                    onDragStopped = {
                        if (abs(offsetX.value) > barWidth * 0.3f) {
                            onSwipe()
                            offsetX.snapTo(0f)
                        } else {
                            offsetX.animateTo(0f)
                        }
                    }
                )
        ) {
            if (nextItem != null) {
                AsyncImage(
                    model = YoutubeCoreConstant.decodeThumpUrl(nextItem.mediaMetadata.artworkUri?.toString() ?: ""),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                )
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 12.dp)
                ) {
                    Text(
                        text = nextItem.mediaMetadata.title?.toString() ?: "",
                        color = Color.White,
                        fontSize = 14.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text("Swipe to play", color = SecondaryText, fontSize = 12.sp)
                }
            } else {
                Text(
                    text = "Nothing queued yet",
                    color = SecondaryText,
                    fontSize = 13.sp,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

/** The service falls back to "Artist :<title>" when it has no channel name; hide that placeholder. */
private fun cleanArtist(rawArtist: String?, title: String): String {
    val artist = rawArtist?.removePrefix("Artist :")?.replace("\n", " ")?.trim().orEmpty()
    return if (artist.isEmpty() || artist == title.trim()) "" else artist
}

private fun formatTime(ms: Long, durationMs: Long): String {
    if (ms <= 0L || ms == C.TIME_UNSET) return if (durationMs >= 3_600_000) "0:00:00" else "0:00"
    val totalSeconds = ms / 1000
    val hours = totalSeconds / 3600
    val minutes = totalSeconds / 60 % 60
    val seconds = totalSeconds % 60
    return if (durationMs >= 3_600_000) "%d:%02d:%02d".format(hours, minutes, seconds)
    else "%d:%02d".format(minutes, seconds)
}

@OptIn(UnstableApi::class)
fun startPlayDownloadsService(context: Context) {
    val intent = Intent(context, PlaybackService::class.java).apply {
        action = PlaybackService.ACTION_PLAY_DOWNLOADS
    }
    context.startService(intent)
}

@OptIn(UnstableApi::class)
fun startFetchPlayListService(context: Context) {
    val intent = Intent(context, PlaybackService::class.java).apply {
        action = PlaybackService.ACTION_FETCH_PLAYLIST
    }
    context.startService(intent)
}

@OptIn(UnstableApi::class)
fun startFetchSongService(context: Context, videoId: String, albumArt: String, title: String) {
    val intent = Intent(context, PlaybackService::class.java).apply {
        action = PlaybackService.ACTION_FETCH_SONG
        putExtra(PlaybackService.VIDEO_ID, videoId)
        putExtra(PlaybackService.ALBUM_ART, albumArt)
        putExtra(PlaybackService.VIDEO_TITLE, title)
    }
    context.startService(intent)
}
