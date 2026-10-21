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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountCircle
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
import androidx.compose.runtime.rememberUpdatedState
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
import androidx.compose.ui.input.key.onKeyEvent
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
import com.ramzmania.tubefy.ui.components.screen.youtube.END_WATCH_MS
import com.ramzmania.tubefy.ui.components.screen.youtube.UP_NEXT_LEAD_SECONDS
import com.ramzmania.tubefy.ui.components.screen.youtube.isAtEnd
import com.ramzmania.tubefy.ui.components.screen.youtube.nextRelated
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
fun TvVideoPlayer(
    args: Bundle?,
    onExit: () -> Unit,
    onOpenChannel: (url: String, name: String) -> Unit,
    viewModel: VideoViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    fun arg(key: String) = URLDecoder.decode(args?.getString(key).orEmpty(), StandardCharsets.UTF_8.toString())

    var videoId by rememberSaveable { mutableStateOf(arg("videoId")) }
    var fallbackTitle by rememberSaveable { mutableStateOf(arg("title")) }
    var fallbackThumb by rememberSaveable { mutableStateOf(arg("thumb").ifBlank { "https://i.ytimg.com/vi/$videoId/hq720.jpg" }) }
    var fallbackChannel by rememberSaveable { mutableStateOf(arg("channel")) }
    var playbackRequested by rememberSaveable { mutableStateOf(false) }
    // The new video's own picture is on screen (its first frame was drawn, or it has been playing a moment)
    var pictureReady by remember { mutableStateOf(false) }
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
    // Related videos slide up only on ▼ from the buttons (like YouTube), not with every OK
    var relatedOpen by remember { mutableStateOf(false) }
    val firstRelated = remember { FocusRequester() }

    val details by viewModel.details.collectAsState()
    val loaded = (details as? VideoViewModel.DetailsState.Loaded)?.details
    val title = loaded?.title?.ifBlank { null } ?: fallbackTitle
    val channel = loaded?.channel?.ifBlank { null } ?: fallbackChannel
    val isLive = loaded?.isLive == true

    val playFocus = remember { FocusRequester() }
    val rootFocus = remember { FocusRequester() }

    LaunchedEffect(videoId) { viewModel.load(videoId, fallbackTitle, fallbackThumb, fallbackChannel) }

    // Autoplay: played videos (never bounce back), and "Back" on the Up next card cancels it for this video
    var playedIds by rememberSaveable { mutableStateOf(ArrayList<String>()) }
    var autoplayCancelled by remember(videoId) { mutableStateOf(false) }
    // Autoplay cancelled with Back: stopped at the end (OK replays)
    var atEnd by remember(videoId) { mutableStateOf(false) }
    val liveNow by rememberUpdatedState(isLive)
    val nextVideo = loaded?.related?.let { nextRelated(it, playedIds + videoId) }
    val remainingMs = durationMs - positionMs
    val showUpNext = nextVideo != null && !autoplayCancelled && !isLive && started &&
        durationMs > 30_000 && positionMs > 0 && remainingMs in 1..(UP_NEXT_LEAD_SECONDS * 1000L)

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
            if (!playbackRequested) {
                playbackRequested = true
                if (c.currentMediaItem?.mediaId == videoId) {
                    started = true
                    pictureReady = true
                } else play(videoId, fallbackTitle, fallbackThumb)
            } else if (c.currentMediaItem?.mediaId == videoId) {
                // Back from the channel page: the video is still there, carry on
                pictureReady = true
                c.play()
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
            if (started && player.isPlaying && player.currentPosition > 800) pictureReady = true
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
        if (!controlsVisible) relatedOpen = false
        runCatching { if (controlsVisible) playFocus.requestFocus() else rootFocus.requestFocus() }
    }
    LaunchedEffect(relatedOpen) {
        if (relatedOpen) {
            delay(150L) // let the row appear
            runCatching { firstRelated.requestFocus() }
        }
    }
    LaunchedEffect(seekHint) {
        if (seekHint != null) {
            delay(900L)
            seekHint = null
        }
    }

    fun playNext(next: com.ramzmania.tubefy.data.youtube.YouTubeItem) {
        controller?.pause()
        playedIds = ArrayList(playedIds + videoId)
        videoId = next.id
        fallbackTitle = next.title
        fallbackThumb = next.thumbnail
        fallbackChannel = next.channel
        controlsVisible = false
        play(next.id, next.title, next.thumbnail)
    }

    // The video ended (or the player would loop it / move on to a song): next related video, unless cancelled
    val onVideoEnded by rememberUpdatedState {
        val next = nextVideo
        if (started && !atEnd) {
            if (next != null && !autoplayCancelled) playNext(next)
            else {
                controller?.pause()
                atEnd = true
            }
        }
    }
    DisposableEffect(controller) {
        val c = controller ?: return@DisposableEffect onDispose {}
        val listener = object : Player.Listener {
            override fun onMediaItemTransition(mediaItem: androidx.media3.common.MediaItem?, reason: Int) {
                if (reason == Player.MEDIA_ITEM_TRANSITION_REASON_AUTO || reason == Player.MEDIA_ITEM_TRANSITION_REASON_REPEAT) onVideoEnded()
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_ENDED) onVideoEnded()
            }

            override fun onRenderedFirstFrame() {
                if (started) pictureReady = true
            }
        }
        c.addListener(listener)
        onDispose { c.removeListener(listener) }
    }

    // Position ticker; checks often near the end to switch before the player jumps to its next item
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

    // Seek bar scrubbing: ◀ ▶ move a preview position, applied after a short pause or on OK
    var scrubMs by remember { mutableStateOf<Long?>(null) }
    fun commitScrub() {
        scrubMs?.let { target -> controller?.seekTo(target) }
        scrubMs = null
    }
    fun scrub(direction: Int, repeatCount: Int) {
        if (durationMs <= 0) return
        val base = scrubMs ?: positionMs
        scrubMs = (base + direction * scrubStepMs(repeatCount, durationMs)).coerceIn(0L, (durationMs - 1_000).coerceAtLeast(0))
        lastKey = System.currentTimeMillis()
    }
    LaunchedEffect(scrubMs, lastKey) {
        if (scrubMs != null) {
            delay(800L)
            commitScrub()
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

    BackHandler(enabled = !controlsVisible) { onExit() }
    BackHandler(enabled = controlsVisible) { controlsVisible = false }
    // Registered last, so it wins: Back on the Up next card only cancels autoplay
    BackHandler(enabled = showUpNext && !controlsVisible) { autoplayCancelled = true }
    BackHandler(enabled = relatedOpen) {
        relatedOpen = false
        runCatching { playFocus.requestFocus() }
    }

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
                if (showUpNext && (event.key == Key.DirectionCenter || event.key == Key.Enter || event.key == Key.NumPadEnter)) {
                    nextVideo?.let(::playNext)
                    return@onPreviewKeyEvent true
                }
                when (event.key) {
                    Key.DirectionLeft -> { if (!isLive) seekBy(-10_000); true }
                    Key.DirectionRight -> { if (!isLive) seekBy(10_000); true }
                    Key.DirectionCenter, Key.Enter, Key.NumPadEnter -> {
                        controller?.let {
                            if (atEnd) {
                                atEnd = false
                                it.seekTo(0)
                                it.play()
                            } else if (it.isPlaying) it.pause() else it.play()
                        }
                        controlsVisible = true
                        true
                    }
                    Key.DirectionUp, Key.DirectionDown, Key.Menu -> { controlsVisible = true; true }
                    else -> false
                }
            }
    ) {
        SessionVideoSurface(controller = controller, playing = isPlaying, modifier = Modifier.fillMaxSize())

        // Until this video's own picture plays, show its thumbnail (not the old video / a song's art)
        if (!started || !pictureReady) {
            Box(Modifier.fillMaxSize().background(Color.Black)) {
                coil.compose.AsyncImage(
                    model = fallbackThumb,
                    contentDescription = null,
                    contentScale = androidx.compose.ui.layout.ContentScale.Fit,
                    modifier = Modifier.fillMaxSize()
                )
                Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.35f)))
            }
        }

        if ((isBuffering || !started || !pictureReady) && loadError.isEmpty()) {
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

        // Up next: the last seconds of the video show what plays next, YouTube style
        AnimatedVisibility(
            visible = showUpNext && !controlsVisible,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.BottomEnd).padding(end = 48.dp, bottom = 44.dp)
        ) {
            nextVideo?.let { next ->
                UpNextCard(next, secondsLeft = ((remainingMs + 999) / 1000).toInt(), fraction = (remainingMs / (UP_NEXT_LEAD_SECONDS * 1000f)).coerceIn(0f, 1f))
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
                        TvSeekBar(
                            positionMs = positionMs,
                            durationMs = durationMs,
                            scrubMs = scrubMs,
                            onScrub = ::scrub,
                            onCommit = ::commitScrub
                        )
                    }

                    val related = loaded?.related.orEmpty().filter { !it.isPlaylist }
                    // Buttons (▼ opens the related videos)
                    Row(
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(22.dp),
                        modifier = Modifier
                            .padding(top = 14.dp)
                            .onPreviewKeyEvent { event ->
                                if (event.type == KeyEventType.KeyDown && event.key == Key.DirectionDown && related.isNotEmpty()) {
                                    relatedOpen = true
                                    true
                                } else false
                            }
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
                        // The channel's page (pauses this video)
                        loaded?.channelUrl?.let { channelUrl ->
                            TvRoundButton(
                                Icons.Rounded.AccountCircle,
                                "Open channel",
                                onClick = { onOpenChannel(channelUrl, channel) },
                                label = channel.take(18)
                            )
                        }
                    }

                    // Related videos: a hint, and the row itself once opened with ▼ (▲ closes it)
                    if (related.isNotEmpty() && !relatedOpen) {
                        Text("▼  Related videos", color = Color.White.copy(alpha = 0.7f), fontSize = 14.sp, modifier = Modifier.padding(top = 14.dp))
                    }
                    AnimatedVisibility(
                        visible = relatedOpen && related.isNotEmpty(),
                        enter = androidx.compose.animation.slideInVertically { it / 2 } + fadeIn(),
                        exit = androidx.compose.animation.slideOutVertically { it / 2 } + fadeOut()
                    ) {
                      Column {
                        Text("Related videos", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 18.dp, bottom = 6.dp))
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(end = 24.dp),
                            modifier = Modifier.onPreviewKeyEvent { event ->
                                if (event.type == KeyEventType.KeyDown && event.key == Key.DirectionUp) {
                                    relatedOpen = false
                                    runCatching { playFocus.requestFocus() }
                                    true
                                } else false
                            }
                        ) {
                            itemsIndexed(related, key = { _, item -> item.id }) { index, item ->
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
                                    modifier = Modifier
                                        .width(240.dp)
                                        .then(if (index == 0) Modifier.focusRequester(firstRelated) else Modifier)
                                )
                            }
                        }
                      }
                    }
                }
            }
        }
    }
}

/** "Up next" card: the next video's thumbnail and title, a countdown, and how to act on it. */
@Composable
private fun UpNextCard(next: com.ramzmania.tubefy.data.youtube.YouTubeItem, secondsLeft: Int, fraction: Float) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .width(520.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(Color(0xE6161314))
            .padding(14.dp)
    ) {
        Box(
            Modifier
                .width(200.dp)
                .height(112.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.DarkGray)
        ) {
            coil.compose.AsyncImage(
                model = next.thumbnail,
                contentDescription = next.title,
                contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            if (next.durationSeconds > 0) Text(
                formatDuration(next.durationSeconds),
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(6.dp)
                    .clip(RoundedCornerShape(5.dp))
                    .background(Color.Black.copy(alpha = 0.8f))
                    .padding(horizontal = 5.dp, vertical = 1.dp)
            )
            // Shrinking bar: time left before it plays
            Box(
                Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth(fraction)
                    .height(4.dp)
                    .background(DiscoverAccent)
            )
        }
        Column(Modifier.weight(1f).padding(start = 16.dp)) {
            Text("Up next in $secondsLeft", color = DiscoverAccent, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            Text(
                next.title,
                color = Color.White,
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 4.dp)
            )
            if (next.channel.isNotBlank()) Text(next.channel, color = DiscoverTextSecondary, fontSize = 14.sp, maxLines = 1, modifier = Modifier.padding(top = 2.dp))
            Text("OK  Play now     Back  Cancel", color = Color.White.copy(alpha = 0.7f), fontSize = 13.sp, modifier = Modifier.padding(top = 8.dp))
        }
    }
}

/** How far one ◀ / ▶ press moves the seek bar: 10 s, faster while the key is held, bigger on long videos. */
internal fun scrubStepMs(repeatCount: Int, durationMs: Long): Long = when {
    repeatCount < 4 -> 10_000L
    repeatCount < 12 -> 30_000L
    else -> maxOf(60_000L, durationMs / 40)
}

/**
 * The seek bar on TV: focusable (▲ from the buttons). Focused, it grows and shows a handle; ◀ ▶ preview
 * a new time (held, it speeds up) and OK jumps there straight away.
 */
@Composable
private fun TvSeekBar(
    positionMs: Long,
    durationMs: Long,
    scrubMs: Long?,
    onScrub: (direction: Int, repeatCount: Int) -> Unit,
    onCommit: () -> Unit
) {
    val interaction = remember { MutableInteractionSource() }
    val focused by interaction.collectIsFocusedAsState()
    val shownMs = scrubMs ?: positionMs
    val fraction = if (durationMs > 0) (shownMs.toFloat() / durationMs).coerceIn(0f, 1f) else 0f
    val played = if (durationMs > 0) (positionMs.toFloat() / durationMs).coerceIn(0f, 1f) else 0f

    Column(Modifier.fillMaxWidth()) {
        androidx.compose.foundation.layout.BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .height(28.dp)
                .onKeyEvent { event ->
                    if (event.type != KeyEventType.KeyDown) return@onKeyEvent false
                    when (event.key) {
                        Key.DirectionLeft -> { onScrub(-1, event.nativeKeyEvent.repeatCount); true }
                        Key.DirectionRight -> { onScrub(1, event.nativeKeyEvent.repeatCount); true }
                        Key.DirectionCenter, Key.Enter, Key.NumPadEnter -> { onCommit(); true }
                        else -> false
                    }
                }
                .focusable(interactionSource = interaction)
        ) {
            val barHeight = if (focused) 8.dp else 5.dp
            val width = maxWidth
            // Track, what's played, and (while scrubbing) the preview position
            Box(
                Modifier
                    .align(Alignment.CenterStart)
                    .fillMaxWidth()
                    .height(barHeight)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color.White.copy(alpha = 0.3f))
            ) {
                if (scrubMs != null) Box(Modifier.fillMaxWidth(fraction).height(barHeight).background(Color.White.copy(alpha = 0.55f)))
                Box(Modifier.fillMaxWidth(if (scrubMs != null) minOf(played, fraction) else played).height(barHeight).background(DiscoverAccent))
            }
            if (focused) Box(
                Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = (width - 22.dp) * fraction)
                    .size(22.dp)
                    .clip(androidx.compose.foundation.shape.CircleShape)
                    .background(if (scrubMs != null) Color.White else DiscoverAccent)
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 6.dp)) {
            Text(
                "${clock(shownMs)} / ${clock(durationMs)}",
                color = if (scrubMs != null) DiscoverAccent else Color.White,
                fontSize = if (scrubMs != null) 20.sp else 15.sp,
                fontWeight = if (scrubMs != null) FontWeight.Bold else FontWeight.Normal
            )
            if (focused) Text(
                if (scrubMs != null) "   OK to jump here" else "   ◀ ▶ to seek",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 13.sp
            )
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
