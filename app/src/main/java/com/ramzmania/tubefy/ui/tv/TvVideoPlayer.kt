package com.ramzmania.tubefy.ui.tv

import android.os.Bundle
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.ClosedCaption
import androidx.compose.material.icons.rounded.ClosedCaptionDisabled
import androidx.compose.material.icons.rounded.Forward10
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Replay10
import androidx.compose.material.icons.rounded.Translate
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.common.Player
import androidx.media3.common.TrackSelectionParameters
import androidx.media3.common.Tracks
import androidx.media3.session.MediaController
import com.ramzmania.tubefy.player.PlaybackService
import com.ramzmania.tubefy.ui.components.screen.player.MediaControllerComposable
import com.ramzmania.tubefy.ui.components.screen.player.SessionVideoSurface
import com.ramzmania.tubefy.ui.components.screen.player.startFetchSongService
import com.ramzmania.tubefy.ui.components.screen.youtube.TrackOption
import com.ramzmania.tubefy.ui.components.screen.youtube.TrackSheet
import com.ramzmania.tubefy.ui.components.screen.youtube.audioOptions
import com.ramzmania.tubefy.ui.components.screen.youtube.captionOptions
import com.ramzmania.tubefy.ui.components.screen.youtube.currentLabel
import com.ramzmania.tubefy.ui.components.screen.youtube.formatCount
import com.ramzmania.tubefy.ui.components.screen.youtube.formatDuration
import com.ramzmania.tubefy.ui.components.screen.youtube.qualityOptions
import com.ramzmania.tubefy.ui.theme.DiscoverAccent
import com.ramzmania.tubefy.ui.theme.DiscoverTextSecondary
import com.ramzmania.tubefy.viewmodel.VideoViewModel
import kotlinx.coroutines.delay
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

/**
 * Full-screen TV player. With the controls hidden: OK plays / pauses, ◀ ▶ seek 10 s, ▲ ▼ show the
 * controls. With them showing, the remote moves between the buttons (±10 s, play, captions, quality,
 * audio language) and, below them, the related videos. Back hides the controls, then leaves.
 */
@Composable
fun TvVideoPlayer(args: Bundle?, onExit: () -> Unit, viewModel: VideoViewModel = hiltViewModel()) {
    val context = LocalContext.current
    fun arg(key: String) = URLDecoder.decode(args?.getString(key).orEmpty(), StandardCharsets.UTF_8.toString())

    var videoId by rememberSaveable { mutableStateOf(arg("videoId")) }
    var fallbackTitle by rememberSaveable { mutableStateOf(arg("title")) }
    var fallbackThumb by rememberSaveable { mutableStateOf(arg("thumb").ifBlank { "https://i.ytimg.com/vi/$videoId/hq720.jpg" }) }
    var fallbackChannel by rememberSaveable { mutableStateOf(arg("channel")) }
    var playbackRequested by rememberSaveable { mutableStateOf(false) }
    var started by rememberSaveable { mutableStateOf(false) }

    var controller by remember { mutableStateOf<MediaController?>(null) }
    var isPlaying by remember { mutableStateOf(false) }
    var isBuffering by remember { mutableStateOf(true) }
    var positionMs by remember { mutableLongStateOf(0L) }
    var durationMs by remember { mutableLongStateOf(0L) }
    var tracks by remember { mutableStateOf(Tracks.EMPTY) }
    var trackParams by remember { mutableStateOf<TrackSelectionParameters?>(null) }
    var loadError by remember { mutableStateOf("") }

    var controlsVisible by remember { mutableStateOf(true) }
    var lastKey by remember { mutableLongStateOf(0L) }
    var seekHint by remember { mutableStateOf<String?>(null) }
    var openSheet by remember { mutableStateOf<TrackSheet?>(null) }

    val details by viewModel.details.collectAsState()
    val loaded = (details as? VideoViewModel.DetailsState.Loaded)?.details
    val title = loaded?.title?.ifBlank { null } ?: fallbackTitle
    val channel = loaded?.channel?.ifBlank { null } ?: fallbackChannel
    val isLive = loaded?.isLive == true

    val playFocus = remember { FocusRequester() }
    val rootFocus = remember { FocusRequester() }

    LaunchedEffect(videoId) { viewModel.load(videoId, fallbackTitle, fallbackThumb, fallbackChannel) }

    fun play(id: String, itemTitle: String, thumb: String) {
        started = false
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
            if (!playbackRequested) {
                playbackRequested = true
                if (c.currentMediaItem?.mediaId == videoId) started = true else play(videoId, fallbackTitle, fallbackThumb)
            }
        },
        onMetaDataChangedValue = { }
    )

    DisposableEffect(controller) {
        val c = controller ?: return@DisposableEffect onDispose {}
        fun sync(player: Player) {
            isBuffering = player.playbackState == Player.STATE_BUFFERING ||
                (player.playbackState == Player.STATE_IDLE && player.playWhenReady)
            if (player.duration > 0) durationMs = player.duration
            positionMs = player.currentPosition
            tracks = player.currentTracks
            trackParams = player.trackSelectionParameters
            val current = player.currentMediaItem?.mediaId.orEmpty()
            if (current == videoId) started = true
            else if (started && current.length == 11) {
                videoId = current
                fallbackTitle = player.mediaMetadata.title?.toString().orEmpty()
                fallbackThumb = player.mediaMetadata.artworkUri?.toString() ?: "https://i.ytimg.com/vi/$current/hq720.jpg"
                fallbackChannel = ""
            }
        }
        val listener = object : Player.Listener {
            override fun onEvents(player: Player, events: Player.Events) = sync(player)
        }
        c.addListener(listener)
        sync(c)
        // Leaving the player stops the video (a TV has no "background" listening here)
        onDispose {
            c.removeListener(listener)
            c.pause()
        }
    }

    LaunchedEffect(controller, isPlaying) {
        val c = controller ?: return@LaunchedEffect
        do {
            positionMs = c.currentPosition
            if (c.duration > 0) durationMs = c.duration
            delay(500L)
        } while (isPlaying)
    }
    LaunchedEffect(controller) {
        val c = controller ?: return@LaunchedEffect
        while (true) {
            loadError = c.sessionExtras.getString(PlaybackService.EXTRA_LOAD_ERROR).orEmpty()
            delay(1_000L)
        }
    }

    // Controls hide after a few seconds without key presses while playing
    LaunchedEffect(controlsVisible, isPlaying, lastKey, openSheet) {
        if (controlsVisible && isPlaying && openSheet == null) {
            delay(5_000L)
            controlsVisible = false
        }
    }
    LaunchedEffect(controlsVisible) {
        runCatching { if (controlsVisible) playFocus.requestFocus() else rootFocus.requestFocus() }
    }
    LaunchedEffect(seekHint) {
        if (seekHint != null) {
            delay(900L)
            seekHint = null
        }
    }

    fun seekBy(deltaMs: Long) {
        val c = controller ?: return
        val target = (c.currentPosition + deltaMs).coerceAtLeast(0)
        c.seekTo(if (c.duration > 0) target.coerceAtMost(c.duration) else target)
        seekHint = if (deltaMs < 0) "−10 s" else "+10 s"
    }
    fun applyTracks(change: (TrackSelectionParameters) -> TrackSelectionParameters?) {
        val c = controller ?: return
        change(c.trackSelectionParameters)?.let { c.trackSelectionParameters = it }
        trackParams = c.trackSelectionParameters
    }

    BackHandler(enabled = controlsVisible) { controlsVisible = false }
    BackHandler(enabled = !controlsVisible) { onExit() }

    val params = trackParams
    val qualities = if (params != null) qualityOptions(tracks, params) else emptyList()
    val languages = audioOptions(tracks)
    val captions = if (params != null) captionOptions(tracks, params) else emptyList()

    openSheet?.let { sheet ->
        TvChoiceDialog(
            title = sheet.title,
            options = when (sheet) {
                TrackSheet.Quality -> qualities
                TrackSheet.Audio -> languages
                TrackSheet.Captions -> captions
            },
            onPick = {
                applyTracks(it.apply)
                openSheet = null
                lastKey = System.currentTimeMillis()
            },
            onDismiss = { openSheet = null }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .focusRequester(rootFocus)
            .focusable()
            .onPreviewKeyEvent { event ->
                if (event.type != KeyEventType.KeyDown) return@onPreviewKeyEvent false
                lastKey = System.currentTimeMillis()
                if (controlsVisible) return@onPreviewKeyEvent false // the buttons handle it
                when (event.key) {
                    Key.DirectionLeft -> { if (!isLive) seekBy(-10_000); true }
                    Key.DirectionRight -> { if (!isLive) seekBy(10_000); true }
                    Key.DirectionCenter, Key.Enter, Key.NumPadEnter -> {
                        controller?.let { if (it.isPlaying) it.pause() else it.play() }
                        controlsVisible = true
                        true
                    }
                    Key.DirectionUp, Key.DirectionDown, Key.Menu -> { controlsVisible = true; true }
                    else -> false
                }
            }
    ) {
        SessionVideoSurface(controller = controller, playing = isPlaying, modifier = Modifier.fillMaxSize())

        if (isBuffering && loadError.isEmpty()) {
            CircularProgressIndicator(color = Color.White, strokeWidth = 4.dp, modifier = Modifier.align(Alignment.Center).size(64.dp))
        }

        seekHint?.let {
            Text(
                it,
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .align(Alignment.Center)
                    .clip(RoundedCornerShape(50))
                    .background(Color.Black.copy(alpha = 0.6f))
                    .padding(horizontal = 28.dp, vertical = 12.dp)
            )
        }

        if (loadError.isNotEmpty() && !isPlaying) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.85f))
            ) {
                Text(loadError, color = Color.White, fontSize = 20.sp)
                TextButton(onClick = { play(videoId, title, fallbackThumb) }) { Text("Retry", color = DiscoverAccent, fontSize = 20.sp) }
            }
        }

        AnimatedVisibility(visible = controlsVisible, enter = fadeIn(), exit = fadeOut(), modifier = Modifier.fillMaxSize()) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(Brush.verticalGradient(listOf(Color.Black.copy(alpha = 0.7f), Color.Transparent, Color.Black.copy(alpha = 0.9f))))
            ) {
                // Title and channel
                Column(Modifier.align(Alignment.TopStart).padding(start = 56.dp, top = 40.dp, end = 56.dp)) {
                    Text(title, color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    val meta = listOfNotNull(
                        channel.ifBlank { null },
                        loaded?.views?.takeIf { it >= 0 }?.let { formatCount(it) + if (isLive) " watching" else " views" },
                        loaded?.uploadedAgo
                    ).joinToString(" • ")
                    if (meta.isNotBlank()) Text(meta, color = DiscoverTextSecondary, fontSize = 17.sp, modifier = Modifier.padding(top = 6.dp))
                }

                Column(Modifier.align(Alignment.BottomStart).fillMaxWidth().padding(horizontal = 56.dp, vertical = 32.dp)) {
                    // Progress
                    if (isLive) {
                        Text(
                            "● LIVE",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.clip(RoundedCornerShape(4.dp)).background(TvLiveRed).padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    } else {
                        val fraction = if (durationMs > 0) (positionMs.toFloat() / durationMs).coerceIn(0f, 1f) else 0f
                        Box(Modifier.fillMaxWidth().height(5.dp).clip(RoundedCornerShape(3.dp)).background(Color.White.copy(alpha = 0.3f))) {
                            Box(Modifier.fillMaxWidth(fraction).height(5.dp).background(DiscoverAccent))
                        }
                        Text(
                            "${clock(positionMs)} / ${clock(durationMs)}",
                            color = Color.White,
                            fontSize = 15.sp,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }

                    // Buttons
                    Row(
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(22.dp),
                        modifier = Modifier.padding(top = 14.dp)
                    ) {
                        if (!isLive) TvRoundButton(Icons.Rounded.Replay10, "Back 10 seconds", onClick = { seekBy(-10_000) })
                        TvRoundButton(
                            if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                            if (isPlaying) "Pause" else "Play",
                            onClick = { controller?.let { if (it.isPlaying) it.pause() else it.play() } },
                            big = true,
                            modifier = Modifier.focusRequester(playFocus)
                        )
                        if (!isLive) TvRoundButton(Icons.Rounded.Forward10, "Forward 10 seconds", onClick = { seekBy(10_000) })
                        Spacer(Modifier.width(24.dp))
                        if (captions.isNotEmpty()) {
                            val on = !captions.first().selected
                            TvRoundButton(
                                if (on) Icons.Rounded.ClosedCaption else Icons.Rounded.ClosedCaptionDisabled,
                                "Captions",
                                onClick = { openSheet = TrackSheet.Captions },
                                label = captions.currentLabel()
                            )
                        }
                        qualities.currentLabel()?.let {
                            TvRoundButton(Icons.Rounded.Tune, "Quality", onClick = { openSheet = TrackSheet.Quality }, label = it)
                        }
                        languages.currentLabel()?.let {
                            TvRoundButton(Icons.Rounded.Translate, "Audio language", onClick = { openSheet = TrackSheet.Audio }, label = it)
                        }
                    }

                    // Related videos
                    val related = loaded?.related.orEmpty().filter { !it.isPlaylist }
                    if (related.isNotEmpty()) {
                        Text("Related videos", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 22.dp, bottom = 6.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(end = 24.dp)) {
                            items(related, key = { it.id }) { item ->
                                TvVideoCard(
                                    item = item,
                                    onClick = {
                                        videoId = item.id
                                        fallbackTitle = item.title
                                        fallbackThumb = item.thumbnail
                                        fallbackChannel = item.channel
                                        play(item.id, item.title, item.thumbnail)
                                        controlsVisible = false
                                    },
                                    modifier = Modifier.width(240.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/** A remote-friendly list dialog (quality / audio language / captions). */
@Composable
private fun TvChoiceDialog(title: String, options: List<TrackOption>, onPick: (TrackOption) -> Unit, onDismiss: () -> Unit) {
    val first = remember { FocusRequester() }
    Dialog(onDismissRequest = onDismiss) {
        Column(
            Modifier
                .width(420.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFF161314))
                .padding(vertical = 20.dp)
        ) {
            Text(title, color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 28.dp, bottom = 10.dp))
            LazyColumn(Modifier.heightIn(max = 460.dp)) {
                items(options.size) { index ->
                    val option = options[index]
                    val interaction = remember { MutableInteractionSource() }
                    val focused by interaction.collectIsFocusedAsState()
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (focused) Color.White else Color.Transparent)
                            .then(if (option.selected) Modifier.focusRequester(first) else Modifier)
                            .clickable(interactionSource = interaction, indication = null) { onPick(option) }
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        if (option.selected) Icon(Icons.Rounded.Check, null, tint = if (focused) Color.Black else DiscoverAccent, modifier = Modifier.size(22.dp))
                        else Spacer(Modifier.width(22.dp))
                        Text(
                            option.label,
                            color = if (focused) Color.Black else Color.White,
                            fontSize = 18.sp,
                            fontWeight = if (option.selected) FontWeight.SemiBold else FontWeight.Normal,
                            modifier = Modifier.padding(start = 14.dp)
                        )
                    }
                }
            }
        }
    }
    LaunchedEffect(Unit) { runCatching { first.requestFocus() } }
}

private fun clock(ms: Long): String = if (ms <= 0) "0:00" else formatDuration(ms / 1000)
