package com.ramzmania.tubefy.ui.components.screen.search

import androidx.compose.animation.core.RepeatMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.draw.shadow
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.Image
import androidx.compose.foundation.Canvas
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.StartOffset
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.ramzmania.tubefy.R
import com.ramzmania.tubefy.ui.theme.DiscoverAccent
import com.ramzmania.tubefy.ui.theme.DiscoverTextSecondary
import com.ramzmania.tubefy.viewmodel.RecognitionViewModel

private const val LISTEN_SECONDS = 12

/** "Identify song": listening, then the recognised song with Play / Search, or Try again. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun IdentifySongSheet(
    state: RecognitionViewModel.State,
    onPlay: () -> Unit,
    onSearch: () -> Unit,
    onRetry: () -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = Color(0xFF161314)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(start = 24.dp, end = 24.dp, bottom = 28.dp)
        ) {
            when (state) {
                RecognitionViewModel.State.Idle, is RecognitionViewModel.State.Listening -> {
                    val seconds = (state as? RecognitionViewModel.State.Listening)?.seconds ?: 0
                    ListeningPulse(progress = seconds / LISTEN_SECONDS.toFloat())
                    Text("Listening…", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 12.dp))
                    EqualizerBars(Modifier.padding(top = 10.dp))
                    Text(
                        "Play the song near your phone",
                        color = DiscoverTextSecondary,
                        fontSize = 15.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                    Text(
                        "${(LISTEN_SECONDS - seconds).coerceAtLeast(0)} s",
                        color = DiscoverTextSecondary,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(top = 14.dp)
                    )
                }

                is RecognitionViewModel.State.Found -> {
                    AsyncImage(
                        model = state.song.coverUrl ?: R.drawable.placeholder,
                        contentDescription = state.song.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.size(180.dp).clip(RoundedCornerShape(20.dp)).background(Color.White.copy(alpha = 0.06f))
                    )
                    Text(
                        state.song.title,
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 18.dp)
                    )
                    if (state.song.artist.isNotBlank()) Text(state.song.artist, color = Color.White.copy(alpha = 0.8f), fontSize = 16.sp, textAlign = TextAlign.Center, modifier = Modifier.padding(top = 4.dp))
                    val extra = listOf(state.song.album, state.song.year).filter { it.isNotBlank() }.joinToString(" · ")
                    if (extra.isNotEmpty()) Text(extra, color = DiscoverTextSecondary, fontSize = 14.sp, textAlign = TextAlign.Center, modifier = Modifier.padding(top = 2.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.padding(top = 22.dp).fillMaxWidth()) {
                        SheetButton(
                            label = when {
                                state.findingTrack -> "Finding…"
                                state.track != null -> "Play"
                                else -> "Not on YouTube"
                            },
                            icon = Icons.Rounded.PlayArrow,
                            primary = true,
                            enabled = state.track != null,
                            busy = state.findingTrack,
                            onClick = onPlay,
                            modifier = Modifier.weight(1f)
                        )
                        SheetButton("Search", Icons.Rounded.Search, primary = false, enabled = true, busy = false, onClick = onSearch, modifier = Modifier.weight(1f))
                    }
                }

                RecognitionViewModel.State.NotFound -> Outcome(
                    title = "Couldn't recognise this song",
                    text = "Try again with the music a little louder or closer to the phone.",
                    onRetry = onRetry
                )

                is RecognitionViewModel.State.Failed -> Outcome(title = "Couldn't identify", text = state.message, onRetry = onRetry)
            }
        }
    }
}

/**
 * Listening: ripple rings spread out from the identify symbol like sound waves, the centre gently
 * "breathes", and a thin ring around it fills up over the listening time ([progress] 0–1).
 */
@Composable
private fun ListeningPulse(progress: Float) {
    val transition = rememberInfiniteTransition(label = "listening")
    val ripples = (0 until 3).map { index ->
        transition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(tween(2400, easing = LinearEasing), initialStartOffset = StartOffset(index * 800)),
            label = "ripple$index"
        )
    }
    val breathe by transition.animateFloat(1f, 1.06f, infiniteRepeatable(tween(700, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "breathe")
    val shownProgress by animateFloatAsState(progress.coerceIn(0f, 1f), animationSpec = tween(400), label = "progress")

    Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(top = 8.dp).size(230.dp)) {
        ripples.forEach { ripple ->
            val value = ripple.value
            Box(
                Modifier
                    .size(104.dp)
                    .graphicsLayer {
                        scaleX = 1f + value * 1.15f
                        scaleY = 1f + value * 1.15f
                        alpha = (1f - value) * 0.7f
                    }
                    .border(2.dp, DiscoverAccent, CircleShape)
                    .background(DiscoverAccent.copy(alpha = 0.10f), CircleShape)
            )
        }
        // Countdown ring: fills up over the listening time
        Canvas(Modifier.size(124.dp)) {
            val stroke = 3.dp.toPx()
            drawCircle(Color.White.copy(alpha = 0.12f), style = Stroke(stroke))
            drawArc(
                color = Color.White,
                startAngle = -90f,
                sweepAngle = 360f * shownProgress,
                useCenter = false,
                style = Stroke(stroke, cap = StrokeCap.Round)
            )
        }
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(104.dp)
                .scale(breathe)
                .shadow(18.dp, CircleShape, ambientColor = DiscoverAccent, spotColor = DiscoverAccent)
                .clip(CircleShape)
                .background(Brush.linearGradient(listOf(Color(0xFFFF5A6A), DiscoverAccent)))
        ) {
            Image(painterResource(R.drawable.ic_identify_song_symbol), contentDescription = null, modifier = Modifier.size(60.dp))
        }
    }
}

/** Little bouncing bars under "Listening…". */
@Composable
private fun EqualizerBars(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "equalizer")
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.Bottom, modifier = modifier.height(22.dp)) {
        listOf(520, 380, 640, 450, 560).forEachIndexed { index, duration ->
            val level by transition.animateFloat(
                initialValue = 0.25f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(tween(duration, easing = FastOutSlowInEasing), RepeatMode.Reverse, StartOffset(index * 90)),
                label = "bar$index"
            )
            Box(
                Modifier
                    .width(4.dp)
                    .fillMaxHeight(level)
                    .clip(RoundedCornerShape(2.dp))
                    .background(DiscoverAccent)
            )
        }
    }
}

@Composable
private fun Outcome(title: String, text: String, onRetry: () -> Unit) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.padding(top = 24.dp).size(84.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.08f))
    ) {
        Image(
            painterResource(R.drawable.ic_identify_song_symbol),
            contentDescription = null,
            alpha = 0.6f,
            modifier = Modifier.size(48.dp)
        )
    }
    Text(title, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.padding(top = 18.dp))
    Text(text, color = DiscoverTextSecondary, fontSize = 15.sp, textAlign = TextAlign.Center, modifier = Modifier.padding(top = 6.dp))
    Spacer(Modifier.height(20.dp))
    SheetButton("Try again", Icons.Rounded.Mic, primary = true, enabled = true, busy = false, onClick = onRetry, modifier = Modifier.fillMaxWidth())
}

@Composable
private fun SheetButton(
    label: String,
    icon: ImageVector,
    primary: Boolean,
    enabled: Boolean,
    busy: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = modifier
            .height(52.dp)
            .clip(RoundedCornerShape(26.dp))
            .background(if (primary && enabled) DiscoverAccent else Color.White.copy(alpha = 0.08f))
            .border(1.dp, if (primary) Color.Transparent else Color.White.copy(alpha = 0.15f), RoundedCornerShape(26.dp))
            .clickable(enabled = enabled && !busy, onClick = onClick)
    ) {
        if (busy) CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp, modifier = Modifier.size(18.dp))
        else Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
        Spacer(Modifier.width(8.dp))
        Text(label, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
    }
}
