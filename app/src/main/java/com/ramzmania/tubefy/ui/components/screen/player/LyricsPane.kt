package com.ramzmania.tubefy.ui.components.screen.player

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.collectIsDraggedAsState
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.EditNote
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.Movie
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ramzmania.tubefy.core.lyrics.Lyrics
import com.ramzmania.tubefy.core.lyrics.SongDetails
import com.ramzmania.tubefy.core.youtube.YouTubeAccess
import com.ramzmania.tubefy.ui.theme.DiscoverAccent
import com.ramzmania.tubefy.viewmodel.LyricsViewModel

/**
 * The music player's Lyrics tab: a card with the song's details (singers, movie, lyricist, music,
 * year), then the lyrics. Timed lyrics follow the song (current line highlighted, auto-scrolling;
 * tap a line to jump there); otherwise they're shown as plain text.
 */
@Composable
internal fun LyricsPane(
    state: LyricsViewModel.State,
    positionMs: Long,
    onSeek: (Long) -> Unit,
    onRetry: () -> Unit,
    englishLetters: Boolean,
    onEnglishLettersChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(Color.White.copy(alpha = 0.06f))
    ) {
        when (state) {
            LyricsViewModel.State.Idle, LyricsViewModel.State.Loading -> CircularProgressIndicator(
                color = Color.White,
                strokeWidth = 3.dp,
                modifier = Modifier.align(Alignment.Center).size(40.dp)
            )

            LyricsViewModel.State.Error -> Column(Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(YouTubeAccess.messageOr("Couldn't load lyrics"), color = Color.White.copy(alpha = 0.7f), fontSize = 15.sp, modifier = Modifier.padding(horizontal = 24.dp))
                TextButton(onClick = onRetry) { Text("Retry", color = DiscoverAccent) }
            }

            is LyricsViewModel.State.Loaded -> {
                val english = state.info.englishLetters
                LyricsList(
                    details = state.info.details,
                    lyrics = if (englishLetters && english != null) english else state.info.lyrics,
                    // The switch only when there's another script to switch from
                    scriptSwitch = if (english != null && state.info.lyrics != null) {
                        { ScriptSwitch(scriptName(state.info.lyrics), englishLetters, onEnglishLettersChange) }
                    } else null,
                    positionMs = positionMs,
                    onSeek = onSeek
                )
            }
        }
    }
}

@Composable
private fun LyricsList(
    details: SongDetails,
    lyrics: Lyrics?,
    scriptSwitch: (@Composable () -> Unit)?,
    positionMs: Long,
    onSeek: (Long) -> Unit
) {
    val listState = rememberLazyListState()
    val dragged by listState.interactionSource.collectIsDraggedAsState()
    var lastUserScroll by remember { mutableLongStateOf(0L) }
    if (dragged) LaunchedEffect(Unit) { lastUserScroll = System.currentTimeMillis() }

    // The line being sung: the last one that has started (a little early, so it lights up on time)
    val current = if (lyrics?.synced == true) {
        lyrics.lines.indexOfLast { (it.timeMs ?: Long.MAX_VALUE) <= positionMs + 250 }
    } else -1

    // Follow the song, unless the user scrolled in the last few seconds
    LaunchedEffect(current) {
        if (current < 0 || dragged || System.currentTimeMillis() - lastUserScroll < 4_000) return@LaunchedEffect
        // Item 0 is the details card; keep the current line about a third of the way down
        listState.animateScrollToItem((current + 1 - 2).coerceAtLeast(0))
    }

    LazyColumn(
        state = listState,
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 120.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item(key = "details") { DetailsCard(details) }
        scriptSwitch?.let { switch -> item(key = "script") { switch() } }

        if (lyrics == null || lyrics.lines.all { it.text.isBlank() }) {
            item(key = "none") {
                Text(
                    "Lyrics not available for this song",
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 16.sp,
                    modifier = Modifier.padding(top = 28.dp).fillMaxWidth()
                )
            }
        } else {
            itemsIndexed(lyrics.lines) { index, line ->
                if (line.text.isBlank()) {
                    Spacer(Modifier.height(14.dp))
                } else if (lyrics.synced) {
                    SyncedLine(line.text, state = when {
                        index == current -> LineState.Current
                        index < current -> LineState.Sung
                        else -> LineState.Upcoming
                    }, onClick = { line.timeMs?.let(onSeek) })
                } else {
                    Text(
                        line.text,
                        color = Color.White.copy(alpha = 0.88f),
                        fontSize = 18.sp,
                        lineHeight = 26.sp,
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                }
            }
            item(key = "source") {
                if (lyrics.source.isNotBlank()) Text(
                    "Lyrics by ${lyrics.source}",
                    color = Color.White.copy(alpha = 0.45f),
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 24.dp)
                )
            }
        }
    }
}

private enum class LineState { Sung, Current, Upcoming }

@Composable
private fun SyncedLine(text: String, state: LineState, onClick: () -> Unit) {
    val color by animateColorAsState(
        when (state) {
            LineState.Current -> Color.White
            LineState.Sung -> Color.White.copy(alpha = 0.55f)
            LineState.Upcoming -> Color.White.copy(alpha = 0.35f)
        },
        label = "lyricColor"
    )
    val size by animateFloatAsState(if (state == LineState.Current) 23f else 20f, label = "lyricSize")
    Text(
        text,
        color = color,
        fontSize = size.sp,
        lineHeight = (size + 8).sp,
        fontWeight = if (state == LineState.Current) FontWeight.Bold else FontWeight.SemiBold,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 6.dp)
    )
}

/** "Malayalam | English letters": which way to show the lyrics. */
@Composable
private fun ScriptSwitch(script: String, englishLetters: Boolean, onChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .padding(bottom = 18.dp)
            .clip(RoundedCornerShape(50))
            .background(Color.White.copy(alpha = 0.08f))
            .padding(4.dp)
    ) {
        listOf(false to script, true to "English letters").forEach { (english, label) ->
            val selected = english == englishLetters
            val background by animateColorAsState(if (selected) DiscoverAccent else Color.Transparent, label = "scriptBg")
            Text(
                label,
                color = if (selected) Color.White else Color.White.copy(alpha = 0.7f),
                fontSize = 14.sp,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(background)
                    .clickable { onChange(english) }
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }
    }
}

/** The script's name for the switch ("Malayalam", "Hindi", …), from the lyrics themselves. */
private fun scriptName(lyrics: Lyrics): String =
    com.ramzmania.tubefy.core.download.SongBook.languageOf(lyrics.lines.joinToString(" ") { it.text }).ifBlank { "Original" }

@Composable
private fun DetailsCard(details: SongDetails) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Color.White.copy(alpha = 0.08f))
            .padding(16.dp)
    ) {
        Text(
            details.title,
            color = Color.White,
            fontSize = 19.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        DetailRow(Icons.Rounded.Mic, "Singers", details.singers)
        DetailRow(Icons.Rounded.Movie, "Movie", details.movie)
        DetailRow(Icons.Rounded.EditNote, "Lyrics", details.lyricist)
        DetailRow(Icons.Rounded.MusicNote, "Music", details.composer)
        DetailRow(Icons.Rounded.CalendarMonth, "Year", details.year)
    }
    Spacer(Modifier.height(20.dp))
}

@Composable
private fun DetailRow(icon: ImageVector, label: String, value: String) {
    if (value.isBlank()) return
    Row(verticalAlignment = Alignment.Top) {
        Icon(icon, contentDescription = null, tint = DiscoverAccent, modifier = Modifier.padding(top = 1.dp).size(18.dp))
        Text(label, color = Color.White.copy(alpha = 0.55f), fontSize = 14.sp, modifier = Modifier.padding(start = 10.dp).width(64.dp))
        Text(value, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
    }
}
