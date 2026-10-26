package com.ramzmania.tubefy.ui.components.screen.youtube

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.ActivityInfo
import android.media.AudioManager
import android.view.WindowManager
import android.content.res.Configuration
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.PlaylistAdd
import androidx.compose.material.icons.automirrored.rounded.VolumeDown
import androidx.compose.material.icons.automirrored.rounded.VolumeOff
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.BrightnessHigh
import androidx.compose.material.icons.rounded.BrightnessLow
import androidx.compose.material.icons.rounded.BrightnessMedium
import androidx.compose.material.icons.rounded.ClosedCaption
import androidx.compose.material.icons.rounded.ClosedCaptionDisabled
import androidx.compose.material.icons.rounded.Translate
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material.icons.rounded.BookmarkBorder
import androidx.compose.material.icons.rounded.Forward10
import androidx.compose.material.icons.rounded.Fullscreen
import androidx.compose.material.icons.rounded.FullscreenExit
import androidx.compose.material.icons.rounded.Headphones
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Replay
import androidx.compose.material.icons.rounded.Replay10
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.material.icons.rounded.FastRewind
import androidx.compose.material.icons.rounded.FastForward
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.TrackSelectionParameters
import androidx.media3.common.Tracks
import androidx.media3.session.MediaController
import coil.compose.AsyncImage
import com.ramzmania.tubefy.R
import com.ramzmania.tubefy.player.PlaybackService
import com.ramzmania.tubefy.data.dto.base.searchformat.TubeFyCoreTypeData
import com.ramzmania.tubefy.data.youtube.YouTubeVideoDetails
import com.ramzmania.tubefy.ui.components.NavigationItem
import com.ramzmania.tubefy.ui.components.screen.library.PlayListDialogViewer
import com.ramzmania.tubefy.ui.components.screen.player.MediaControllerComposable
import com.ramzmania.tubefy.ui.components.screen.player.SessionVideoSurface
import com.ramzmania.tubefy.ui.components.screen.player.startFetchSongService
import com.ramzmania.tubefy.ui.theme.DiscoverAccent
import com.ramzmania.tubefy.ui.theme.DiscoverBackground
import com.ramzmania.tubefy.ui.theme.DiscoverBorder
import com.ramzmania.tubefy.ui.theme.DiscoverSurface
import com.ramzmania.tubefy.ui.theme.DiscoverTextSecondary
import com.ramzmania.tubefy.utils.LocalNavController
import com.ramzmania.tubefy.viewmodel.TubeFyViewModel
import com.ramzmania.tubefy.viewmodel.VideoViewModel
import androidx.compose.material.icons.rounded.DownloadDone
import androidx.compose.material.icons.rounded.Download
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.runtime.produceState
import androidx.work.WorkManager
import androidx.work.WorkInfo
import com.ramzmania.tubefy.core.download.VideoDownloads
import com.ramzmania.tubefy.core.download.VideoDownloadWorker
import com.ramzmania.tubefy.core.download.VideoDownloadOption
import com.ramzmania.tubefy.core.download.PhoneDownloads
import kotlinx.coroutines.delay
import kotlin.math.abs
import kotlin.math.roundToInt
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

/**
 * The YouTube tab's video page: the video with its own controls (±10 s, seek, full screen), then
 * details, "Play in background", "Save" (to the "YouTube Videos" playlist), "Play as audio" and
 * related videos. Playback runs in the app's music service, so audio carries on when leaving.
 */
@Composable
fun VideoScreen(viewModel: VideoViewModel = hiltViewModel()) {
    val navController = LocalNavController.current
    val context = LocalContext.current
    val navArgs = remember { navController?.currentBackStackEntry?.arguments }
    fun arg(key: String) = URLDecoder.decode(navArgs?.getString(key).orEmpty(), StandardCharsets.UTF_8.toString())

    // The page follows the video that's playing (a related video, or the next one in the queue)
    var videoId by rememberSaveable { mutableStateOf(arg("videoId")) }
    var fallbackTitle by rememberSaveable { mutableStateOf(arg("title")) }
    var fallbackThumb by rememberSaveable { mutableStateOf(arg("thumb").ifBlank { "https://i.ytimg.com/vi/$videoId/hq720.jpg" }) }
    var fallbackChannel by rememberSaveable { mutableStateOf(arg("channel")) }
    // Survive rotation: only start playback once, and only follow the queue once our video started
    var playbackRequested by rememberSaveable { mutableStateOf(false) }
    // The new video's own picture is on screen (its first frame was drawn, or it has been playing a moment)
    var pictureReady by remember { mutableStateOf(false) }
    var started by rememberSaveable { mutableStateOf(false) }

    var controller by remember { mutableStateOf<MediaController?>(null) }
    var isPlaying by remember { mutableStateOf(false) }
    var isBuffering by remember { mutableStateOf(true) }
    var positionMs by remember { mutableLongStateOf(0L) }
    var durationMs by remember { mutableLongStateOf(0L) }
    var seekFraction by remember { mutableStateOf<Float?>(null) }
    var controlsVisible by remember { mutableStateOf(true) }
    var lastTouch by remember { mutableLongStateOf(0L) }
    // Full screen: pinch out fills the screen (crops the edges), pinch in fits the whole video again
    var zoomed by rememberSaveable { mutableStateOf(false) }
    // Swipe on the left half: this page's screen brightness (-1 = the phone's own; the setting isn't changed)
    var brightness by rememberSaveable { mutableStateOf(-1f) }
    var tracks by remember { mutableStateOf(Tracks.EMPTY) }
    // Height of the picture actually playing (what "Auto" picked right now)
    var videoHeight by remember { mutableStateOf(0) }
    var trackParams by remember { mutableStateOf<TrackSelectionParameters?>(null) }
    var openSheet by remember { mutableStateOf<TrackSheet?>(null) }

    /** The video "Add to playlist" was picked for (this one or a related one). */
    var playlistTarget by remember { mutableStateOf<TubeFyCoreTypeData?>(null) }

    val details by viewModel.details.collectAsState()
    val saved by viewModel.saved.collectAsState()
    val loaded = (details as? VideoViewModel.DetailsState.Loaded)?.details
    val title = loaded?.title?.ifBlank { null } ?: fallbackTitle
    val isLive = loaded?.isLive == true
    val fullscreen = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
    val activity = remember(context) { context.findActivity() }
    val listState = rememberLazyListState()

    LaunchedEffect(videoId) { viewModel.load(videoId, fallbackTitle, fallbackThumb, fallbackChannel) }

    // Autoplay: videos already played on this page (so it never bounces back to one)
    var playedIds by rememberSaveable { mutableStateOf(ArrayList<String>()) }
    val autoplay by viewModel.autoplay.collectAsState()
    // Autoplay off: the video stopped at its end (the play button becomes Replay)
    var atEnd by remember(videoId) { mutableStateOf(false) }
    val liveNow by rememberUpdatedState(isLive)

    fun play(id: String, itemTitle: String, thumb: String) {
        started = false
        pictureReady = false
        isBuffering = true
        positionMs = 0L
        durationMs = 0L
        startFetchSongService(context, id, thumb, itemTitle, video = true)
    }

    MediaControllerComposable(
        onPlayingChanged = { playing -> isPlaying = playing },
        onMediaControllerInitialized = { c ->
            controller = c
            isPlaying = c.isPlaying
            setVideoTrackEnabled(c, true)
            if (!playbackRequested) {
                playbackRequested = true
                if (c.currentMediaItem?.mediaId == videoId) {
                    started = true // already playing (e.g. came back from the mini player)
                    pictureReady = true
                } else {
                    play(videoId, fallbackTitle, fallbackThumb)
                }
            } else if (c.currentMediaItem?.mediaId == videoId) {
                pictureReady = true // screen recreated (rotation): the video is already showing, even if paused
            }
        },
        onMetaDataChangedValue = { }
    )

    // When the video ends (or the player would loop it / move on to a song), play the next related video
    val onVideoEnded by rememberUpdatedState {
        val next = loaded?.related?.let { nextRelated(it, playedIds + videoId) }
        if (!started || atEnd) {
            // already handled
        } else if (!autoplay || next == null) {
            controller?.pause()
            atEnd = true
            controlsVisible = true
        } else {
            controller?.pause()
            playedIds = ArrayList(playedIds + videoId)
            videoId = next.id
            fallbackTitle = next.title
            fallbackThumb = next.thumbnail
            fallbackChannel = next.channel
            controlsVisible = true
            play(next.id, next.title, next.thumbnail)
        }
    }

    // Player state: buffering, duration, and which video is playing
    DisposableEffect(controller) {
        val c = controller ?: return@DisposableEffect onDispose {}
        fun sync(player: Player) {
            isBuffering = player.playbackState == Player.STATE_BUFFERING ||
                (player.playbackState == Player.STATE_IDLE && player.playWhenReady)
            if (player.duration > 0) durationMs = player.duration
            positionMs = player.currentPosition
            tracks = player.currentTracks
            videoHeight = player.videoSize.height
            trackParams = player.trackSelectionParameters
            val current = player.currentMediaItem?.mediaId.orEmpty()
            if (current == videoId) started = true
            if (started && player.isPlaying && player.currentPosition > 800) pictureReady = true
            else if (started && current.length == 11) {
                // The queue moved on (next song / Mix): show that video's page
                videoId = current
                fallbackTitle = player.mediaMetadata.title?.toString().orEmpty()
                fallbackThumb = player.mediaMetadata.artworkUri?.toString() ?: "https://i.ytimg.com/vi/$current/hq720.jpg"
                fallbackChannel = ""
            }
        }
        val listener = object : Player.Listener {
            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                if (reason == Player.MEDIA_ITEM_TRANSITION_REASON_AUTO || reason == Player.MEDIA_ITEM_TRANSITION_REASON_REPEAT) onVideoEnded()
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_ENDED) onVideoEnded()
            }

            override fun onRenderedFirstFrame() {
                if (started) pictureReady = true
            }

            override fun onEvents(player: Player, events: Player.Events) = sync(player)
        }
        c.addListener(listener)
        sync(c)
        onDispose { c.removeListener(listener) }
    }

    // The service reports when the video couldn't be loaded (e.g. no internet)
    var loadError by remember { mutableStateOf("") }
    LaunchedEffect(controller) {
        val c = controller ?: return@LaunchedEffect
        while (true) {
            loadError = c.sessionExtras.getString(PlaybackService.EXTRA_LOAD_ERROR).orEmpty()
            delay(1_000L)
        }
    }

    // Position ticker; checks often near the end to act before the player jumps to its next item
    LaunchedEffect(controller, isPlaying) {
        val c = controller ?: return@LaunchedEffect
        do {
            positionMs = c.currentPosition
            if (c.duration > 0) durationMs = c.duration
            if (atEnd && durationMs - positionMs > END_WATCH_MS) atEnd = false // seeked back
            if (isPlaying && started && isAtEnd(positionMs, durationMs, liveNow)) onVideoEnded()
            delay(if (durationMs - positionMs < END_WATCH_MS) 100L else 500L)
        } while (isPlaying)
    }

    // Controls fade out after a few seconds of playback without touches
    LaunchedEffect(controlsVisible, isPlaying, lastTouch, seekFraction) {
        if (controlsVisible && isPlaying && seekFraction == null) {
            delay(3_000L)
            controlsVisible = false
        }
    }

    // Full screen = landscape, with the system bars hidden
    val view = LocalView.current
    DisposableEffect(fullscreen) {
        val window = activity?.window
        if (window != null) {
            val insets = WindowCompat.getInsetsController(window, view)
            if (fullscreen) {
                insets.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                insets.hide(WindowInsetsCompat.Type.systemBars())
            } else {
                insets.show(WindowInsetsCompat.Type.systemBars())
            }
        }
        onDispose {}
    }
    DisposableEffect(Unit) {
        onDispose {
            // Leaving the page (not just rotating, which recreates the screen): rotate freely again
            if (activity?.isChangingConfigurations == true) return@onDispose
            setVideoTrackEnabled(controller, false) // audio carries on without downloading video
            activity?.window?.let { it.attributes = it.attributes.apply { screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE } }
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            activity?.window?.let { WindowCompat.getInsetsController(it, view).show(WindowInsetsCompat.Type.systemBars()) }
        }
    }
    fun setFullscreen(on: Boolean) {
        activity?.requestedOrientation =
            if (on) ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE else ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT
    }
    BackHandler(enabled = fullscreen) { setFullscreen(false) }
    // Re-applied after rotation (a new window starts at the phone's brightness)
    LaunchedEffect(brightness, activity) {
        activity?.window?.let { it.attributes = it.attributes.apply { screenBrightness = brightness } }
    }

    fun applyTracks(change: (TrackSelectionParameters) -> TrackSelectionParameters?) {
        val c = controller ?: return
        change(c.trackSelectionParameters)?.let { c.trackSelectionParameters = it }
        trackParams = c.trackSelectionParameters
    }
    val params = trackParams
    val qualities = if (params != null) qualityOptions(tracks, params, videoHeight) else emptyList()
    val languages = audioOptions(tracks)
    val captions = if (params != null) captionOptions(tracks, params) else emptyList()
    openSheet?.let { sheet ->
        val options = when (sheet) {
            TrackSheet.Quality -> qualities
            TrackSheet.Audio -> languages
            TrackSheet.Captions -> captions
        }
        TrackSheetView(
            sheet = sheet,
            options = options,
            onPick = { option ->
                applyTracks(option.apply)
                openSheet = null
            },
            onDismiss = { openSheet = null }
        )
    }

    // Download sheet (to phone storage) and its progress
    var showDownloadSheet by remember { mutableStateOf(false) }
    var downloadFolder by remember { mutableStateOf(PhoneDownloads.folder(context)) }
    var pendingDownload by remember { mutableStateOf<VideoDownloadOption?>(null) }
    val downloadOptions by viewModel.downloadOptions.collectAsState()
    val downloadState by produceState<VideoDownloadState>(VideoDownloadState.Idle, videoId) {
        WorkManager.getInstance(context).getWorkInfosForUniqueWorkFlow(VideoDownloads.workName(videoId)).collect { infos ->
            val info = infos.firstOrNull()
            value = when (info?.state) {
                WorkInfo.State.RUNNING -> VideoDownloadState.Running(info.progress.getInt(VideoDownloadWorker.KEY_PROGRESS, 0))
                WorkInfo.State.ENQUEUED, WorkInfo.State.BLOCKED -> VideoDownloadState.Running(0)
                WorkInfo.State.SUCCEEDED -> VideoDownloadState.Saved
                else -> VideoDownloadState.Idle
            }
        }
    }
    fun startDownload(option: VideoDownloadOption) {
        VideoDownloads.enqueue(context, videoId, title, option)
        downloadFolder?.let { Toast.makeText(context, "Downloading to ${VideoDownloads.folderLabel(it)}…", Toast.LENGTH_SHORT).show() }
        showDownloadSheet = false
    }
    // Android's folder picker (the same folder as songs saved to phone storage)
    val folderPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
        if (uri != null) {
            PhoneDownloads.setFolder(context, uri)
            downloadFolder = PhoneDownloads.folder(context)
            pendingDownload?.let(::startDownload)
        }
        pendingDownload = null
    }
    if (showDownloadSheet) {
        VideoDownloadSheet(
            title = title,
            options = downloadOptions,
            folder = downloadFolder?.let(VideoDownloads::folderLabel),
            onChangeFolder = {
                pendingDownload = null
                folderPicker.launch(downloadFolder)
            },
            onDownload = { option ->
                if (downloadFolder == null) {
                    pendingDownload = option
                    folderPicker.launch(null)
                } else startDownload(option)
            },
            onDismiss = { showDownloadSheet = false }
        )
    }

    playlistTarget?.let { target ->
        val tubeFyViewModel: TubeFyViewModel = hiltViewModel()
        PlayListDialogViewer(
            viewModel = tubeFyViewModel,
            onDismiss = { playlistTarget = null },
            currentSongDetails = target
        )
    }

    val player: @Composable (Modifier) -> Unit = { modifier ->
        VideoPlayerBox(
            controller = controller,
            isPlaying = isPlaying,
            isBuffering = isBuffering,
            isLive = isLive,
            positionMs = positionMs,
            durationMs = durationMs,
            seekFraction = seekFraction,
            controlsVisible = controlsVisible,
            fullscreen = fullscreen,
            onToggleControls = {
                controlsVisible = !controlsVisible
                lastTouch = System.currentTimeMillis()
            },
            onTouch = { lastTouch = System.currentTimeMillis() },
            onBack = { if (fullscreen) setFullscreen(false) else navController?.popBackStack() },
            onSeekChange = { seekFraction = it },
            onSeekFinished = {
                seekFraction?.let { fraction -> controller?.seekTo((fraction * durationMs).toLong()) }
                seekFraction = null
            },
            onFullscreen = { setFullscreen(!fullscreen) },
            loadError = loadError,
            onRetry = { play(videoId, title, fallbackThumb) },
            zoomed = zoomed,
            onZoomChange = { zoomed = it },
            brightness = brightness,
            onBrightnessChange = { brightness = it },
            // Until this video's own picture plays, show its thumbnail (not the old video / a song's art)
            coverThumbnail = fallbackThumb.takeIf { !started || !pictureReady },
            atEnd = atEnd,
            onReplay = {
                atEnd = false
                controller?.seekTo(0)
                controller?.play()
            },
            menu = listOfNotNull(
                qualities.currentLabel()?.let { PlayerMenuEntry("Quality", it) { openSheet = TrackSheet.Quality } },
                languages.currentLabel()?.let { PlayerMenuEntry("Audio language", it) { openSheet = TrackSheet.Audio } },
                captions.currentLabel()?.let { PlayerMenuEntry("Captions", it) { openSheet = TrackSheet.Captions } }
            ),
            captionsOn = captions.isNotEmpty() && captions.first().selected.not(),
            hasCaptions = captions.isNotEmpty(),
            onToggleCaptions = { if (params != null) applyTracks { toggleCaptions(tracks, it) } },
            onAddToPlaylist = {
                playlistTarget = TubeFyCoreTypeData(videoId = videoId, videoTitle = title, videoImage = fallbackThumb, artist = loaded?.channel.orEmpty())
            },
            modifier = modifier
        )
    }

    if (fullscreen) {
        player(Modifier.fillMaxSize())
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DiscoverBackground)
            .statusBarsPadding()
    ) {
        player(Modifier.fillMaxWidth().aspectRatio(16f / 9f))

        LazyColumn(
            state = listState,
            contentPadding = PaddingValues(bottom = 32.dp),
            modifier = Modifier.weight(1f)
        ) {
            item(key = "info") {
                VideoInfo(
                    title = title,
                    details = loaded,
                    fallbackChannel = fallbackChannel,
                    saved = saved,
                    onPlayInBackground = {
                        controller?.play()
                        Toast.makeText(context, "Playing in background", Toast.LENGTH_SHORT).show()
                        navController?.popBackStack()
                    },
                    onSave = {
                        viewModel.toggleSave(videoId, title, fallbackThumb)
                        Toast.makeText(
                            context,
                            if (saved) "Removed from ${VideoViewModel.SAVED_PLAYLIST}" else "Saved to ${VideoViewModel.SAVED_PLAYLIST}",
                            Toast.LENGTH_SHORT
                        ).show()
                    },
                    onPlayAsAudio = {
                        fun enc(value: String) = URLEncoder.encode(value, StandardCharsets.UTF_8.toString())
                        navController?.navigate(
                            NavigationItem.AudioPlayer.createRoute(enc(videoId), enc(fallbackThumb), enc(title), enc(title), enc(loaded?.channel ?: fallbackChannel))
                        )
                    },
                    thumbnail = fallbackThumb,
                    isLive = isLive,
                    autoplay = autoplay,
                    onAutoplayChange = viewModel::setAutoplay,
                    downloadState = downloadState,
                    onDownload = {
                        if (downloadState is VideoDownloadState.Running) {
                            Toast.makeText(context, "Downloading…", Toast.LENGTH_SHORT).show()
                        } else {
                            downloadFolder = PhoneDownloads.folder(context)
                            viewModel.loadDownloadOptions(videoId)
                            showDownloadSheet = true
                        }
                    },
                    onOpenChannel = { channelUrl, name ->
                        fun enc(value: String) = URLEncoder.encode(value, StandardCharsets.UTF_8.toString())
                        navController?.navigate(NavigationItem.Channel.createRoute(enc(channelUrl), enc(name)))
                    }
                )
            }

            when (val state = details) {
                VideoViewModel.DetailsState.Loading -> item(key = "loading") {
                    Box(Modifier.fillMaxWidth().padding(top = 32.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = DiscoverAccent, strokeWidth = 2.dp, modifier = Modifier.size(28.dp))
                    }
                }

                VideoViewModel.DetailsState.Error -> item(key = "error") {
                    Column(Modifier.fillMaxWidth().padding(top = 24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(com.ramzmania.tubefy.core.youtube.YouTubeAccess.messageOr("Couldn't load related videos"), color = DiscoverTextSecondary, fontSize = 15.sp)
                        TextButton(onClick = { viewModel.load(videoId, title, fallbackThumb, fallbackChannel, force = true) }) { Text("Retry", color = DiscoverAccent) }
                    }
                }

                is VideoViewModel.DetailsState.Loaded -> {
                    if (state.details.related.isNotEmpty()) item(key = "relatedHeader") {
                        Text(
                            "Related videos",
                            color = Color.White,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 18.dp, bottom = 6.dp)
                        )
                    }
                    items(state.details.related, key = { "related|${it.id}" }) { item ->
                        VideoRow(
                            item = item,
                            onClick = {
                                if (item.isPlaylist) {
                                    fun enc(value: String) = URLEncoder.encode(value, StandardCharsets.UTF_8.toString())
                                    navController?.navigate(NavigationItem.PlayList.createRoute(item.id, enc(item.title), enc(item.thumbnail.ifBlank { "nourl" })))
                                } else {
                                    // Plays here: the page switches to the related video
                                    videoId = item.id
                                    fallbackTitle = item.title
                                    fallbackThumb = item.thumbnail
                                    fallbackChannel = item.channel
                                    controlsVisible = true
                                    play(item.id, item.title, item.thumbnail)
                                }
                            },
                            onPlayAudio = {
                                fun enc(value: String) = URLEncoder.encode(value, StandardCharsets.UTF_8.toString())
                                navController?.navigate(
                                    NavigationItem.AudioPlayer.createRoute(enc(item.id), enc(item.thumbnail), enc(item.title), enc(item.title), enc(item.channel))
                                )
                            },
                            onAddToPlaylist = {
                                playlistTarget = TubeFyCoreTypeData(videoId = item.id, videoTitle = item.title, videoImage = item.thumbnail, artist = item.channel)
                            }
                        )
                    }
                }
            }
        }
    }

    // A related video opens at the top of the page
    LaunchedEffect(videoId) { listState.scrollToItem(0) }
}

@kotlin.OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun VideoPlayerBox(
    controller: MediaController?,
    isPlaying: Boolean,
    isBuffering: Boolean,
    isLive: Boolean,
    positionMs: Long,
    durationMs: Long,
    seekFraction: Float?,
    controlsVisible: Boolean,
    fullscreen: Boolean,
    onToggleControls: () -> Unit,
    onTouch: () -> Unit,
    onBack: () -> Unit,
    onSeekChange: (Float) -> Unit,
    onSeekFinished: () -> Unit,
    onFullscreen: () -> Unit,
    loadError: String,
    onRetry: () -> Unit,
    zoomed: Boolean,
    onZoomChange: (Boolean) -> Unit,
    brightness: Float,
    onBrightnessChange: (Float) -> Unit,
    coverThumbnail: String?,
    atEnd: Boolean,
    onReplay: () -> Unit,
    menu: List<PlayerMenuEntry>,
    hasCaptions: Boolean,
    captionsOn: Boolean,
    onToggleCaptions: () -> Unit,
    onAddToPlaylist: () -> Unit,
    modifier: Modifier = Modifier
) {
    var menuOpen by remember { mutableStateOf(false) }
    val fraction = seekFraction ?: if (durationMs > 0) (positionMs.toFloat() / durationMs).coerceIn(0f, 1f) else 0f

    // Briefly says "Zoomed to fill" / "Original size" after a pinch
    var zoomHint by remember { mutableStateOf<String?>(null) }

    // Brightness / volume swipes
    val context = LocalContext.current
    val audio = remember(context) { context.getSystemService(AudioManager::class.java) }
    val maxVolume = remember(audio) { audio?.getStreamMaxVolume(AudioManager.STREAM_MUSIC)?.coerceAtLeast(1) ?: 1 }
    val currentBrightness by rememberUpdatedState(brightness)
    var swipeHud by remember { mutableStateOf<Pair<SwipeKind, Float>?>(null) }
    var swipeEndedAt by remember { mutableStateOf(0L) }
    LaunchedEffect(swipeEndedAt) {
        if (swipeEndedAt > 0) {
            delay(900L)
            swipeHud = null
        }
    }
    fun currentLevel(kind: SwipeKind): Float = when (kind) {
        SwipeKind.Brightness -> currentBrightness.takeIf { it >= 0f } ?: systemBrightness(context)
        SwipeKind.Volume -> (audio?.getStreamVolume(AudioManager.STREAM_MUSIC) ?: 0) / maxVolume.toFloat()
    }
    fun applyLevel(kind: SwipeKind, level: Float) {
        when (kind) {
            SwipeKind.Brightness -> onBrightnessChange(level.coerceAtLeast(0.01f))
            SwipeKind.Volume -> {
                val target = (level * maxVolume).roundToInt()
                if (target != audio?.getStreamVolume(AudioManager.STREAM_MUSIC)) {
                    audio?.setStreamVolume(AudioManager.STREAM_MUSIC, target, 0)
                }
            }
        }
    }
    // Double tap: left half −10 s, right half +10 s; quick repeats add up ("30 seconds")
    var doubleTapSeek by remember { mutableStateOf<Pair<Boolean, Int>?>(null) }
    var doubleTapAt by remember { mutableStateOf(0L) }
    LaunchedEffect(doubleTapAt) {
        if (doubleTapAt > 0) {
            delay(800L)
            doubleTapSeek = null
        }
    }
    // The ±10 s buttons spin a full turn (back: anticlockwise) whenever the video jumps 10 s
    val spinScope = rememberCoroutineScope()
    val backSpin = remember { Animatable(0f) }
    val forwardSpin = remember { Animatable(0f) }
    fun jump10(forward: Boolean) {
        val c = controller ?: return
        val target = c.currentPosition + if (forward) 10_000 else -10_000
        c.seekTo(if (forward && c.duration > 0) target.coerceAtMost(c.duration) else target.coerceAtLeast(0))
        val spin = if (forward) forwardSpin else backSpin
        spinScope.launch {
            spin.snapTo(0f)
            spin.animateTo(if (forward) 360f else -360f, tween(450, easing = FastOutSlowInEasing))
            spin.snapTo(0f)
        }
    }
    fun seekBy(forward: Boolean) {
        jump10(forward)
        val previous = doubleTapSeek?.takeIf { it.first == forward && System.currentTimeMillis() - doubleTapAt < 800L }
        doubleTapSeek = forward to (previous?.second ?: 0) + 10
        doubleTapAt = System.currentTimeMillis()
        onTouch()
    }
    LaunchedEffect(zoomHint) {
        if (zoomHint != null) {
            delay(1_200L)
            zoomHint = null
        }
    }

    Box(
        modifier = modifier
            .background(Color.Black)
            // Gestures: swipe up/down on the left half = brightness, on the right half = volume;
            // pinch (full screen) = zoom. Single taps pass through to toggle the controls.
            .pointerInput(fullscreen, zoomed) {
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    // Not right at the edges, where Android's back / home gestures live
                    val x = down.position.x / size.width
                    val y = down.position.y / size.height
                    val side = when {
                        y > 0.94f -> null
                        x in 0.05f..0.45f -> SwipeKind.Brightness
                        x in 0.55f..0.95f -> SwipeKind.Volume
                        else -> null
                    }
                    var mode = GestureMode.Undecided
                    var scale = 1f
                    var dragX = 0f
                    var dragY = 0f
                    var startLevel = 0f
                    var startY = 0f
                    do {
                        val event = awaitPointerEvent()
                        val pressed = event.changes.count { it.pressed }
                        if (pressed >= 2 && fullscreen && mode != GestureMode.Swipe) {
                            mode = GestureMode.Pinch
                            scale *= event.calculateZoom()
                            event.changes.forEach { it.consume() }
                        } else if (pressed == 1 && (mode == GestureMode.Undecided || mode == GestureMode.Swipe)) {
                            val change = event.changes.firstOrNull { it.id == down.id } ?: event.changes.first()
                            if (mode == GestureMode.Undecided) {
                                dragX += change.position.x - change.previousPosition.x
                                dragY += change.position.y - change.previousPosition.y
                                val slop = viewConfiguration.touchSlop
                                if (side != null && abs(dragY) > slop && abs(dragY) > abs(dragX) * 1.5f) {
                                    mode = GestureMode.Swipe
                                    startY = change.position.y
                                    startLevel = currentLevel(side)
                                } else if (abs(dragX) > slop * 2) {
                                    mode = GestureMode.Ignore
                                }
                            }
                            if (mode == GestureMode.Swipe && side != null) {
                                // A swipe across most of the video's height goes from 0 to 100 %
                                val level = (startLevel - (change.position.y - startY) / (size.height * 0.85f)).coerceIn(0f, 1f)
                                applyLevel(side, level)
                                swipeHud = side to level
                                change.consume()
                            }
                        }
                    } while (event.changes.any { it.pressed })
                    if (mode == GestureMode.Swipe) swipeEndedAt = System.currentTimeMillis()
                    if (mode == GestureMode.Pinch) when {
                        scale > 1.12f && !zoomed -> {
                            onZoomChange(true)
                            zoomHint = "Zoomed to fill"
                        }
                        scale < 0.9f && zoomed -> {
                            onZoomChange(false)
                            zoomHint = "Original size"
                        }
                    }
                }
            }
            // Tap toggles the controls; double tap seeks (not for live streams)
            .pointerInput(isLive) {
                detectTapGestures(
                    onTap = { onToggleControls() },
                    onDoubleTap = { offset ->
                        if (isLive) onToggleControls() else seekBy(forward = offset.x > size.width / 2)
                    }
                )
            }
    ) {
        SessionVideoSurface(controller = controller, playing = isPlaying, zoomed = fullscreen && zoomed, modifier = Modifier.fillMaxSize())

        coverThumbnail?.let { thumb ->
            Box(Modifier.fillMaxSize().background(Color.Black)) {
                AsyncImage(
                    model = thumb,
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize()
                )
                Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.35f)))
                if (loadError.isEmpty()) CircularProgressIndicator(
                    color = Color.White,
                    strokeWidth = 3.dp,
                    modifier = Modifier.align(Alignment.Center).size(46.dp)
                )
            }
        }

        swipeHud?.let { (kind, level) -> SwipeLevelHud(kind, level, Modifier.align(Alignment.Center)) }

        // Keeps the last hint while it fades out
        var lastSeek by remember { mutableStateOf(true to 10) }
        doubleTapSeek?.let { lastSeek = it }
        AnimatedVisibility(
            visible = doubleTapSeek != null,
            enter = fadeIn(tween(120)),
            exit = fadeOut(tween(300)),
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(0.38f)
                .align(if (lastSeek.first) Alignment.CenterEnd else Alignment.CenterStart)
        ) {
            DoubleTapSeekHint(lastSeek.first, lastSeek.second, tapKey = doubleTapAt)
        }

        zoomHint?.let {
            Text(
                it,
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 28.dp)
                    .clip(RoundedCornerShape(50))
                    .background(Color.Black.copy(alpha = 0.6f))
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }

        if (coverThumbnail == null && isBuffering && loadError.isEmpty() && !(controlsVisible && !isPlaying)) {
            CircularProgressIndicator(color = Color.White, strokeWidth = 3.dp, modifier = Modifier.align(Alignment.Center).size(46.dp))
        }

        AnimatedVisibility(visible = controlsVisible, enter = fadeIn(), exit = fadeOut(), modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Black.copy(alpha = 0.55f), Color.Black.copy(alpha = 0.2f), Color.Black.copy(alpha = 0.65f))
                        )
                    )
            ) {
                // Top: back and menu
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 2.dp).align(Alignment.TopCenter)
                ) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                    Spacer(Modifier.weight(1f))
                    if (hasCaptions) IconButton(onClick = {
                        onTouch()
                        onToggleCaptions()
                    }) {
                        Icon(
                            if (captionsOn) Icons.Rounded.ClosedCaption else Icons.Rounded.ClosedCaptionDisabled,
                            contentDescription = if (captionsOn) "Turn captions off" else "Turn captions on",
                            tint = if (captionsOn) Color.White else Color.White.copy(alpha = 0.75f)
                        )
                    }
                    Box {
                        IconButton(onClick = {
                            onTouch()
                            menuOpen = true
                        }) {
                            Icon(Icons.Rounded.MoreVert, contentDescription = "More options", tint = Color.White)
                        }
                        DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                            menu.forEach { entry ->
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Text(entry.title)
                                            Text(entry.value, fontSize = 12.sp, color = DiscoverTextSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        }
                                    },
                                    leadingIcon = { Icon(entry.icon, null) },
                                    onClick = {
                                        menuOpen = false
                                        entry.onClick()
                                    }
                                )
                            }
                            DropdownMenuItem(
                                text = { Text("Add to playlist") },
                                leadingIcon = { Icon(Icons.AutoMirrored.Rounded.PlaylistAdd, null) },
                                onClick = {
                                    menuOpen = false
                                    onAddToPlaylist()
                                }
                            )
                        }
                    }
                }

                // Centre: −10 s, play / pause, +10 s
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(if (fullscreen) 56.dp else 28.dp),
                    modifier = Modifier.align(Alignment.Center)
                ) {
                    if (!isLive) RoundControl(Icons.Rounded.Replay10, "Back 10 seconds", 52.dp, iconRotation = backSpin.value) {
                        onTouch()
                        jump10(forward = false)
                    }
                    if (isBuffering && !isPlaying) {
                        Box(Modifier.size(68.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = Color.White, strokeWidth = 3.dp, modifier = Modifier.size(46.dp))
                        }
                    } else {
                        if (atEnd && !isPlaying) {
                            RoundControl(Icons.Rounded.Replay, "Replay", 68.dp) {
                                onTouch()
                                onReplay()
                            }
                        } else RoundControl(if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow, if (isPlaying) "Pause" else "Play", 68.dp) {
                            onTouch()
                            controller?.let { if (it.isPlaying) it.pause() else it.play() }
                        }
                    }
                    if (!isLive) RoundControl(Icons.Rounded.Forward10, "Forward 10 seconds", 52.dp, iconRotation = forwardSpin.value) {
                        onTouch()
                        jump10(forward = true)
                    }
                }

                // Bottom: time, full screen, seek bar
                // Kept clear of the bottom edge (and the gesture bar in full screen) so it's easy to grab
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .padding(start = 12.dp, end = 12.dp, bottom = if (fullscreen) 22.dp else 10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        if (isLive) {
                            Text(
                                "● LIVE",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color(0xFFE5202E))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        } else {
                            val shownMs = seekFraction?.let { (it * durationMs).toLong() } ?: positionMs
                            Text(
                                "${clock(shownMs)} / ${clock(durationMs)}",
                                color = Color.White,
                                fontSize = 13.sp
                            )
                        }
                        Spacer(Modifier.weight(1f))
                        IconButton(onClick = onFullscreen) {
                            Icon(
                                if (fullscreen) Icons.Rounded.FullscreenExit else Icons.Rounded.Fullscreen,
                                contentDescription = if (fullscreen) "Exit full screen" else "Full screen",
                                tint = Color.White,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                    if (!isLive) Slider(
                        value = fraction,
                        onValueChange = {
                            onTouch()
                            onSeekChange(it)
                        },
                        onValueChangeFinished = onSeekFinished,
                        colors = SliderDefaults.colors(
                            thumbColor = DiscoverAccent,
                            activeTrackColor = DiscoverAccent,
                            inactiveTrackColor = Color.White.copy(alpha = 0.3f)
                        ),
                        thumb = {
                            Box(Modifier.size(14.dp).clip(CircleShape).background(DiscoverAccent))
                        },
                        track = { state ->
                            SliderDefaults.Track(
                                sliderState = state,
                                colors = SliderDefaults.colors(activeTrackColor = DiscoverAccent, inactiveTrackColor = Color.White.copy(alpha = 0.3f)),
                                thumbTrackGapSize = 0.dp,
                                drawStopIndicator = null,
                                modifier = Modifier.height(3.dp)
                            )
                        },
                        modifier = Modifier.fillMaxWidth().height(32.dp)
                    )
                }
            }
        }

        if (loadError.isNotEmpty() && !isPlaying) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.8f))
            ) {
                Text(loadError, color = Color.White, fontSize = 14.sp, modifier = Modifier.padding(horizontal = 24.dp))
                TextButton(onClick = onRetry) { Text("Retry", color = DiscoverAccent, fontSize = 16.sp) }
            }
        }

        // With the controls hidden, a thin progress line stays visible
        if (!controlsVisible && !isLive && !fullscreen) {
            Box(
                Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth(fraction)
                    .height(2.dp)
                    .background(DiscoverAccent)
            )
        }
    }
}

private enum class GestureMode { Undecided, Swipe, Pinch, Ignore }

internal enum class SwipeKind { Brightness, Volume }

/** The phone's own brightness (0..1), used as the starting point before the first swipe. */
private fun systemBrightness(context: Context): Float = runCatching {
    android.provider.Settings.System.getInt(context.contentResolver, android.provider.Settings.System.SCREEN_BRIGHTNESS) / 255f
}.getOrDefault(0.5f).coerceIn(0f, 1f)

/** The level shown while swiping: an icon, a bar and the percentage. */
@Composable
private fun SwipeLevelHud(kind: SwipeKind, level: Float, modifier: Modifier = Modifier) {
    val icon = when {
        kind == SwipeKind.Brightness -> if (level < 0.3f) Icons.Rounded.BrightnessLow else if (level < 0.7f) Icons.Rounded.BrightnessMedium else Icons.Rounded.BrightnessHigh
        level <= 0f -> Icons.AutoMirrored.Rounded.VolumeOff
        level < 0.5f -> Icons.AutoMirrored.Rounded.VolumeDown
        else -> Icons.AutoMirrored.Rounded.VolumeUp
    }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(Color.Black.copy(alpha = 0.65f))
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Icon(icon, contentDescription = if (kind == SwipeKind.Brightness) "Brightness" else "Volume", tint = Color.White, modifier = Modifier.size(22.dp))
        Box(
            Modifier
                .padding(horizontal = 12.dp)
                .width(110.dp)
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(Color.White.copy(alpha = 0.3f))
        ) {
            Box(Modifier.fillMaxWidth(level).height(4.dp).background(if (kind == SwipeKind.Brightness) Color.White else DiscoverAccent))
        }
        Text("${(level * 100).roundToInt()}%", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Medium)
    }
}

/** A ⋮ menu row in the player: "Quality / Auto (720p)", "Captions / Off", … */
class PlayerMenuEntry(val title: String, val value: String, val onClick: () -> Unit) {
    val icon: ImageVector
        get() = when (title) {
            "Quality" -> Icons.Rounded.Tune
            "Audio language" -> Icons.Rounded.Translate
            else -> Icons.Rounded.ClosedCaption
        }
}

@Composable
private fun RoundControl(
    icon: ImageVector,
    description: String,
    size: androidx.compose.ui.unit.Dp,
    iconRotation: Float = 0f,
    onClick: () -> Unit
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(Color.Black.copy(alpha = 0.35f))
            .clickable(onClick = onClick)
    ) {
        Icon(icon, contentDescription = description, tint = Color.White, modifier = Modifier
            .size(size * 0.58f)
            .rotate(iconRotation))
    }
}

@Composable
private fun VideoInfo(
    title: String,
    details: YouTubeVideoDetails?,
    fallbackChannel: String,
    saved: Boolean,
    thumbnail: String,
    isLive: Boolean,
    autoplay: Boolean,
    onAutoplayChange: (Boolean) -> Unit,
    downloadState: VideoDownloadState,
    onDownload: () -> Unit,
    onOpenChannel: (url: String, name: String) -> Unit,
    onPlayInBackground: () -> Unit,
    onSave: () -> Unit,
    onPlayAsAudio: () -> Unit
) {
    var expanded by rememberSaveable(details?.id) { mutableStateOf(false) }
    val chevron by animateFloatAsState(if (expanded) 180f else 0f, label = "descriptionChevron")
    val hasMore = !details?.description.isNullOrBlank()

    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        // Title + chevron (shows the description)
        Row(
            verticalAlignment = Alignment.Top,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 14.dp)
                .clickable(enabled = hasMore, interactionSource = remember { MutableInteractionSource() }, indication = null) { expanded = !expanded }
        ) {
            Text(
                text = title,
                color = Color.White,
                fontSize = 19.sp,
                fontWeight = FontWeight.SemiBold,
                lineHeight = 24.sp,
                maxLines = if (expanded) Int.MAX_VALUE else 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            if (hasMore) Icon(
                Icons.Rounded.KeyboardArrowDown,
                contentDescription = if (expanded) "Hide description" else "Show description",
                tint = DiscoverTextSecondary,
                modifier = Modifier.padding(start = 8.dp, top = 2.dp).size(26.dp).rotate(chevron)
            )
        }

        // Views · time · hashtags · …more
        val meta = listOfNotNull(
            details?.views?.takeIf { it >= 0 }?.let { formatCount(it) + if (isLive) " watching" else " views" },
            details?.uploadedAgo?.takeIf { it.isNotBlank() }
        ) + details?.tags.orEmpty().take(3).map { "#" + it.replace(" ", "") }
        if (meta.isNotEmpty() || hasMore) Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(top = 6.dp)
                .clickable(enabled = hasMore, interactionSource = remember { MutableInteractionSource() }, indication = null) { expanded = !expanded }
        ) {
            Text(
                meta.joinToString("   "),
                color = DiscoverTextSecondary,
                fontSize = 13.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f, fill = false)
            )
            if (hasMore && !expanded) Text("  ...more", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Medium)
        }
        if (expanded && hasMore) {
            Text(
                details?.description.orEmpty(),
                color = Color.White.copy(alpha = 0.85f),
                fontSize = 14.sp,
                lineHeight = 20.sp,
                modifier = Modifier
                    .padding(top = 10.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(DiscoverSurface)
                    .padding(12.dp)
            )
        }

        // Channel
        val channel = details?.channel?.ifBlank { null } ?: fallbackChannel
        // Tap to open the channel page
        if (channel.isNotBlank()) Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(top = 16.dp)
                .clip(RoundedCornerShape(28.dp))
                .clickable(enabled = details?.channelUrl != null) { details?.channelUrl?.let { onOpenChannel(it, channel) } }
                .padding(end = 10.dp)
        ) {
            AsyncImage(
                model = details?.channelAvatar ?: R.drawable.placeholder,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(42.dp).clip(CircleShape).background(DiscoverSurface)
            )
            Text(
                channel,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(start = 12.dp).weight(1f, fill = false)
            )
            details?.subscribers?.takeIf { it > 0 }?.let {
                Text("  ${formatCount(it)} subscribers", color = DiscoverTextSecondary, fontSize = 13.sp, maxLines = 1)
            }
        }

        // Play in background · Save · Download · Autoplay (scrolls sideways, like YouTube's action chips)
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
                .horizontalScroll(rememberScrollState())
        ) {
            Pill(onClick = onPlayInBackground) {
                Icon(Icons.Rounded.Headphones, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                Text(
                    "Play in background",
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    modifier = Modifier.padding(start = 6.dp)
                )
            }
            Pill(highlighted = saved, onClick = onSave) {
                Icon(
                    if (saved) Icons.Rounded.Bookmark else Icons.Rounded.BookmarkBorder,
                    contentDescription = null,
                    tint = if (saved) DiscoverAccent else Color.White,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    if (saved) "Saved" else "Save",
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }
            // Download: any quality or audio only, to phone storage
            Pill(highlighted = downloadState == VideoDownloadState.Saved, onClick = onDownload) {
                when (downloadState) {
                    is VideoDownloadState.Running -> CircularProgressIndicator(
                        progress = { downloadState.percent / 100f },
                        color = DiscoverAccent,
                        trackColor = Color.White.copy(alpha = 0.15f),
                        strokeWidth = 2.5.dp,
                        modifier = Modifier.size(18.dp)
                    )
                    VideoDownloadState.Saved -> Icon(Icons.Rounded.DownloadDone, contentDescription = null, tint = Color(0xFF2ECC71), modifier = Modifier.size(20.dp))
                    VideoDownloadState.Idle -> Icon(Icons.Rounded.Download, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                }
                Text(
                    when (downloadState) {
                        is VideoDownloadState.Running -> "${downloadState.percent}%"
                        VideoDownloadState.Saved -> "Saved"
                        VideoDownloadState.Idle -> "Download"
                    },
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    modifier = Modifier.padding(start = 6.dp)
                )
            }
            // Autoplay: when this video ends, play the next related one (off: stop at the end)
            Pill(highlighted = autoplay, onClick = { onAutoplayChange(!autoplay) }) {
                Text("Autoplay", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Medium, maxLines = 1)
                MiniSwitch(checked = autoplay, modifier = Modifier.padding(start = 6.dp))
            }
        }

        // Play as audio
        if (!isLive) Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 14.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(Brush.horizontalGradient(listOf(Color(0xFF1E1416), Color(0xFF2A1218))))
                .border(1.dp, DiscoverAccent.copy(alpha = 0.3f), RoundedCornerShape(18.dp))
                .clickable(onClick = onPlayAsAudio)
                .padding(12.dp)
        ) {
            AsyncImage(
                model = thumbnail,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(64.dp).clip(RoundedCornerShape(12.dp)).background(DiscoverSurface)
            )
            Column(modifier = Modifier.weight(1f).padding(horizontal = 12.dp)) {
                Text("Play as audio", color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
                Text(
                    "Listen to this video with the screen off. Audio will continue in background.",
                    color = DiscoverTextSecondary,
                    fontSize = 13.sp,
                    lineHeight = 17.sp,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(48.dp).clip(CircleShape).background(DiscoverAccent)
            ) {
                Icon(Icons.Rounded.PlayArrow, contentDescription = "Play as audio", tint = Color.White, modifier = Modifier.size(28.dp))
            }
        }
    }
}

@Composable
private fun Pill(
    modifier: Modifier = Modifier,
    highlighted: Boolean = false,
    onClick: () -> Unit,
    content: @Composable androidx.compose.foundation.layout.RowScope.() -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = modifier
            .height(54.dp)
            .clip(RoundedCornerShape(27.dp))
            .background(DiscoverSurface)
            .border(1.dp, if (highlighted) DiscoverAccent.copy(alpha = 0.6f) else DiscoverBorder, RoundedCornerShape(27.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp),
        content = content
    )
}

/** The video page's Download chip. */
internal sealed interface VideoDownloadState {
    data object Idle : VideoDownloadState
    data class Running(val percent: Int) : VideoDownloadState
    data object Saved : VideoDownloadState
}

/** A small on/off switch that fits inside a pill (the pill itself handles the tap). */
@Composable
private fun MiniSwitch(checked: Boolean, modifier: Modifier = Modifier) {
    val thumbOffset by androidx.compose.animation.core.animateDpAsState(if (checked) 16.dp else 2.dp, label = "miniSwitch")
    Box(
        modifier = modifier
            .size(width = 34.dp, height = 20.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(if (checked) DiscoverAccent else Color(0xFF3A3436))
    ) {
        Box(
            Modifier
                .padding(start = thumbOffset, top = 2.dp)
                .size(16.dp)
                .clip(CircleShape)
                .background(if (checked) Color.White else DiscoverTextSecondary)
        )
    }
}

/**
 * Turns the video track on or off. Off while only the audio is used (background / audio player), so
 * the separate video stream isn't downloaded for nothing.
 */
internal fun setVideoTrackEnabled(controller: MediaController?, enabled: Boolean) {
    val c = controller ?: return
    if (!c.isConnected) return
    val params = c.trackSelectionParameters
    if ((androidx.media3.common.C.TRACK_TYPE_VIDEO in params.disabledTrackTypes) == !enabled) return
    c.trackSelectionParameters = params.buildUpon().setTrackTypeDisabled(androidx.media3.common.C.TRACK_TYPE_VIDEO, !enabled).build()
}

private fun clock(ms: Long): String = if (ms <= 0) "0:00" else formatDuration(ms / 1000)

private fun Context.findActivity(): Activity? {
    var current: Context? = this
    while (current is ContextWrapper) {
        if (current is Activity) return current
        current = current.baseContext
    }
    return null
}

/**
 * The flash on the tapped side after a double-tap seek: a ripple from the middle of the side, three
 * arrows lighting up one after another (towards the seek direction), and the seconds popping in.
 * [tapKey] changes on every tap, restarting the ripple and the pop.
 */
@Composable
private fun DoubleTapSeekHint(forward: Boolean, seconds: Int, tapKey: Long, modifier: Modifier = Modifier) {
    val shape = if (forward) RoundedCornerShape(topStartPercent = 50, bottomStartPercent = 50)
    else RoundedCornerShape(topEndPercent = 50, bottomEndPercent = 50)
    val ripple = remember { Animatable(0f) }
    val pop = remember { Animatable(1f) }
    LaunchedEffect(tapKey) {
        launch {
            ripple.snapTo(0f)
            ripple.animateTo(1f, tween(550, easing = FastOutSlowInEasing))
        }
        pop.snapTo(1.3f)
        pop.animateTo(1f, spring(dampingRatio = 0.45f, stiffness = 500f))
    }
    val arrows = rememberInfiniteTransition(label = "seekArrows")
    val phase by arrows.animateFloat(0f, 3f, infiniteRepeatable(tween(750, easing = LinearEasing)), label = "phase")

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .fillMaxSize()
            .clip(shape)
            .background(Color.White.copy(alpha = 0.12f))
            .drawBehind {
                // Ripple
                val r = size.maxDimension * 0.75f * ripple.value
                drawCircle(Color.White.copy(alpha = 0.22f * (1f - ripple.value)), radius = r, center = center)
            }
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Row(horizontalArrangement = Arrangement.spacedBy((-6).dp)) {
                repeat(3) { i ->
                    // The lit arrow travels towards the seek direction
                    val step = if (forward) i else 2 - i
                    val lit = (1f - abs(phase - step - 0.5f).coerceAtMost(1.5f) / 1.5f).coerceIn(0f, 1f)
                    Icon(
                        Icons.Rounded.PlayArrow,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.3f + 0.7f * lit),
                        modifier = Modifier
                            .size(22.dp)
                            .rotate(if (forward) 0f else 180f)
                    )
                }
            }
            Text(
                "$seconds seconds",
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .padding(top = 4.dp)
                    .scale(pop.value)
            )
        }
    }
}
