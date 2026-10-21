package com.ramzmania.tubefy.ui.tv

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.derivedStateOf
import com.ramzmania.tubefy.data.youtube.YouTubeItem
import com.ramzmania.tubefy.viewmodel.FeedState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Backspace
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.SpaceBar
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.key.utf16CodePoint
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ramzmania.tubefy.ui.theme.DiscoverAccent
import com.ramzmania.tubefy.ui.theme.DiscoverTextSecondary

/** The on-screen keyboard's letter and number rows (7 keys each), like YouTube on TV. */
internal val TV_KEY_ROWS = listOf(
    "abcdefg",
    "hijklmn",
    "opqrstu",
    "vwxyz'-",
    "1234567",
    "890.,&?"
)

private val KeySize = 34.dp
private val KeyGap = 5.dp
private val KeyColor = Color(0xFF1E1A1C)

/**
 * TV search, YouTube style: what's typed and the suggestions on the left, a compact keyboard on the
 * right, and a row of videos along the bottom (recently played until you search, then the results).
 * The keyboard stays, so you can keep refining the search. A physical keyboard types too.
 */
@Composable
internal fun TvSearchScreen(
    query: String,
    onQueryChange: (String) -> Unit,
    suggestions: List<String>,
    onSearch: (String) -> Unit,
    rowTitle: String,
    rowState: FeedState,
    emptyText: String,
    onLoadMore: () -> Unit,
    onOpen: (YouTubeItem) -> Unit
) {
    val firstKey = remember { FocusRequester() }
    LaunchedEffect(Unit) { runCatching { firstKey.requestFocus() } }

    Column(
        modifier = Modifier
            .fillMaxSize()
            // Typing on a real keyboard (or a remote with one) works as well
            .onPreviewKeyEvent { event ->
                if (event.type != KeyEventType.KeyDown) return@onPreviewKeyEvent false
                when {
                    event.key == Key.Backspace -> { onQueryChange(query.dropLast(1)); true }
                    event.utf16CodePoint > 0 -> {
                        val char = event.utf16CodePoint.toChar()
                        if (char.isLetterOrDigit() || char in " '-.,&?") {
                            onQueryChange(query + char.lowercaseChar())
                            true
                        } else false
                    }
                    else -> false
                }
            }
    ) {
        Row(Modifier.padding(start = 28.dp, end = 28.dp, top = 22.dp)) {
            // Left: what's typed, then suggestions
            Column(Modifier.weight(1f).padding(end = 24.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color(0xFF161314))
                        .border(2.dp, DiscoverAccent.copy(alpha = 0.7f), RoundedCornerShape(24.dp))
                        .padding(horizontal = 16.dp)
                ) {
                    Icon(Icons.Rounded.Search, null, tint = DiscoverTextSecondary, modifier = Modifier.size(24.dp))
                    Text(
                        if (query.isEmpty()) "Search YouTube" else "$query|",
                        color = if (query.isEmpty()) DiscoverTextSecondary else Color.White,
                        fontSize = 19.sp,
                        maxLines = 1,
                        overflow = TextOverflow.StartEllipsis,
                        modifier = Modifier.padding(start = 12.dp)
                    )
                }
                if (suggestions.isNotEmpty()) {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                        modifier = Modifier.padding(top = 8.dp).height(KeySize * 7 + KeyGap * 6 - 56.dp)
                    ) {
                        items(suggestions, key = { it }) { suggestion ->
                            TvMenuItem(
                                label = suggestion,
                                icon = Icons.Rounded.Search,
                                selected = false,
                                onFocus = {},
                                onClick = { onSearch(suggestion) },
                                compact = true
                            )
                        }
                    }
                } else {
                    Text(
                        if (query.isBlank()) "Type with the keyboard on the right. Suggestions show up here." else "",
                        color = DiscoverTextSecondary,
                        fontSize = 15.sp,
                        modifier = Modifier.padding(start = 6.dp, top = 14.dp)
                    )
                }
            }

            // Right: the keyboard
            Column(verticalArrangement = Arrangement.spacedBy(KeyGap)) {
                TV_KEY_ROWS.forEachIndexed { rowIndex, row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(KeyGap)) {
                        row.forEachIndexed { index, char ->
                            TvKey(
                                label = char.toString(),
                                onClick = { onQueryChange(query + char) },
                                modifier = if (rowIndex == 0 && index == 0) Modifier.focusRequester(firstKey) else Modifier
                            )
                        }
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(KeyGap)) {
                    TvKey(icon = Icons.Rounded.SpaceBar, description = "Space", span = 2, onClick = {
                        if (query.isNotEmpty() && !query.endsWith(" ")) onQueryChange("$query ")
                    })
                    TvKey(icon = Icons.AutoMirrored.Rounded.Backspace, description = "Delete", onClick = { onQueryChange(query.dropLast(1)) })
                    TvKey(label = "Clear", span = 2, onClick = { onQueryChange("") })
                    TvKey(icon = Icons.Rounded.Search, description = "Search", span = 2, accent = true, onClick = { onSearch(query.trim()) })
                }
            }
        }

        // Bottom: recently played, or the results of the last search
        Text(
            rowTitle,
            color = Color.White,
            fontSize = 19.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(start = 34.dp, top = 14.dp, bottom = 4.dp)
        )
        val rowListState = rememberLazyListState()
        val nearEnd by remember {
            derivedStateOf {
                val info = rowListState.layoutInfo
                val last = info.visibleItemsInfo.lastOrNull()?.index ?: return@derivedStateOf false
                info.totalItemsCount > 0 && last >= info.totalItemsCount - 4
            }
        }
        LaunchedEffect(nearEnd, rowState.items.size, rowState.loadingMore) {
            if (nearEnd && rowState.items.isNotEmpty() && !rowState.loadingMore && !rowState.error && !rowState.ended) onLoadMore()
        }
        when {
            rowState.items.isEmpty() && rowState.loading -> Box(Modifier.fillMaxWidth().height(120.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = DiscoverAccent)
            }
            rowState.items.isEmpty() -> Text(
                if (rowState.error) "Couldn't load videos. Check your connection." else emptyText,
                color = DiscoverTextSecondary,
                fontSize = 15.sp,
                modifier = Modifier.padding(start = 34.dp, top = 8.dp)
            )
            else -> LazyRow(
                state = rowListState,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(start = 28.dp, end = 28.dp, bottom = 16.dp)
            ) {
                items(rowState.items, key = { it.id }) { item ->
                    TvVideoCard(item = item, onClick = { onOpen(item) }, modifier = Modifier.width(210.dp))
                }
                if (rowState.loadingMore) item(key = "more") {
                    Box(Modifier.width(80.dp).height(118.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = DiscoverAccent, strokeWidth = 3.dp, modifier = Modifier.size(30.dp))
                    }
                }
            }
        }
    }
}

/** One key: [span] keys wide; white when focused; [accent] for the Search key. */
@Composable
private fun TvKey(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    icon: ImageVector? = null,
    description: String? = label,
    span: Int = 1,
    accent: Boolean = false
) {
    val interaction = remember { MutableInteractionSource() }
    val focused by interaction.collectIsFocusedAsState()
    val scale by animateFloatAsState(if (focused) 1.08f else 1f, label = "keyScale")
    val width: Dp = KeySize * span + KeyGap * (span - 1)
    val content = when {
        focused -> Color.Black
        else -> Color.White
    }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = modifier
            .scale(scale)
            .width(width)
            .height(KeySize)
            .clip(RoundedCornerShape(10.dp))
            .background(
                when {
                    focused -> Color.White
                    accent -> DiscoverAccent
                    else -> KeyColor
                }
            )
            .clickable(interactionSource = interaction, indication = null, onClick = onClick)
    ) {
        icon?.let { Icon(it, contentDescription = description, tint = content, modifier = Modifier.size(22.dp)) }
        label?.let {
            Text(
                it,
                color = content,
                fontSize = if (it.length == 1) 20.sp else 16.sp,
                fontWeight = if (accent) FontWeight.Bold else FontWeight.Medium,
                modifier = Modifier.padding(start = if (icon != null) 8.dp else 0.dp)
            )
        }
    }
}
