package com.ramzmania.tubefy.ui.components.screen.youtube

import android.Manifest
import android.content.pm.PackageManager
import android.content.res.Configuration
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.PlaylistAdd
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Headphones
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.PlaylistPlay
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.ramzmania.tubefy.R
import com.ramzmania.tubefy.data.dto.base.searchformat.TubeFyCoreTypeData
import com.ramzmania.tubefy.data.youtube.StateLanguages
import com.ramzmania.tubefy.data.youtube.YouTubeFeed
import com.ramzmania.tubefy.data.youtube.YouTubeFeedSource
import com.ramzmania.tubefy.data.youtube.YouTubeItem
import com.ramzmania.tubefy.ui.components.NavigationItem
import com.ramzmania.tubefy.ui.components.screen.library.PlayListDialogViewer
import com.ramzmania.tubefy.ui.theme.DiscoverAccent
import com.ramzmania.tubefy.ui.theme.DiscoverBackground
import com.ramzmania.tubefy.ui.theme.DiscoverBorder
import com.ramzmania.tubefy.ui.theme.DiscoverOnAccent
import com.ramzmania.tubefy.ui.theme.DiscoverSurface
import com.ramzmania.tubefy.ui.theme.DiscoverTextSecondary
import com.ramzmania.tubefy.utils.LocalNavController
import com.ramzmania.tubefy.viewmodel.FeedState
import com.ramzmania.tubefy.viewmodel.TubeFyViewModel
import com.ramzmania.tubefy.viewmodel.YouTubeViewModel
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

/**
 * The YouTube tab: a search box, then chips (Trending in <state>, Trending, Music, Mixes, Live, Gaming)
 * with a list of videos that loads more as you scroll. Searching replaces the chips' list until cleared.
 */
@Composable
fun YouTubeScreen(viewModel: YouTubeViewModel = hiltViewModel()) {
    val navController = LocalNavController.current
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE

    val region by viewModel.region.collectAsState()
    val selected by viewModel.selected.collectAsState()
    val search by viewModel.search.collectAsState()
    val feeds by viewModel.feeds.collectAsState()
    val showPrompt by viewModel.showStatePrompt.collectAsState()
    val detecting by viewModel.detecting.collectAsState()
    val detectFailed by viewModel.detectFailed.collectAsState()
    val recent by viewModel.recent.collectAsState()
    val showRecent by viewModel.showRecent.collectAsState()
    var confirmClearRecent by remember { mutableStateOf(false) }

    var query by rememberSaveable { mutableStateOf("") }
    var showStatePicker by remember { mutableStateOf(false) }
    var addToPlaylist by remember { mutableStateOf<YouTubeItem?>(null) }

    val activeFeed: YouTubeFeed = search ?: selected
    val state = feeds[activeFeed.key] ?: FeedState(loading = true)

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) viewModel.detectState() else showStatePicker = true
    }
    fun detectOrAsk() {
        val granted = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED
        if (granted) viewModel.detectState() else permissionLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION)
    }

    LaunchedEffect(detectFailed) {
        if (detectFailed) {
            viewModel.consumeDetectFailed()
            showStatePicker = true
        }
    }

    BackHandler(enabled = showRecent) { viewModel.setShowRecent(false) }
    BackHandler(enabled = search != null && !showRecent) {
        query = ""
        viewModel.clearSearch()
    }

    if (confirmClearRecent) {
        AlertDialog(
            onDismissRequest = { confirmClearRecent = false },
            containerColor = DialogColor,
            title = { Text("Clear recent videos?", color = Color.White) },
            text = { Text("This removes all videos from your Recent list.", color = DiscoverTextSecondary) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.clearRecent()
                    confirmClearRecent = false
                }) { Text("Clear", color = DiscoverAccent) }
            },
            dismissButton = { TextButton(onClick = { confirmClearRecent = false }) { Text("Cancel", color = DiscoverTextSecondary) } }
        )
    }

    if (showStatePicker) {
        StatePickerDialog(
            current = region.state,
            detecting = detecting,
            onDetect = {
                showStatePicker = false
                detectOrAsk()
            },
            onPick = {
                showStatePicker = false
                viewModel.pickState(it)
            },
            onDismiss = { showStatePicker = false }
        )
    }

    addToPlaylist?.let { item ->
        val tubeFyViewModel: TubeFyViewModel = hiltViewModel()
        PlayListDialogViewer(
            viewModel = tubeFyViewModel,
            onDismiss = { addToPlaylist = null },
            currentSongDetails = TubeFyCoreTypeData(
                videoId = item.id,
                videoTitle = item.title,
                videoImage = item.thumbnail,
                artist = item.channel
            )
        )
    }

    fun openItem(item: YouTubeItem, audioOnly: Boolean = false) {
        val nav = navController ?: return
        fun enc(value: String) = URLEncoder.encode(value, StandardCharsets.UTF_8.toString())
        if (item.isPlaylist) {
            nav.navigate(NavigationItem.PlayList.createRoute(item.id, enc(item.title), enc(item.thumbnail.ifBlank { "nourl" })))
        } else if (audioOnly) {
            nav.navigate(
                NavigationItem.AudioPlayer.createRoute(enc(item.id), enc(item.thumbnail), enc(item.title), enc(item.title), enc(item.channel))
            )
        } else {
            nav.navigate(NavigationItem.Video.createRoute(enc(item.id), enc(item.title), enc(item.thumbnail), enc(item.channel)))
        }
    }

    val gridState = key(activeFeed.key) { rememberLazyGridState() }
    LoadMoreWhenNearEnd(gridState, state) { if (!showRecent) viewModel.loadMore(activeFeed) }

    val pullState = rememberPullToRefreshState()
    PullToRefreshBox(
        isRefreshing = state.refreshing,
        onRefresh = { viewModel.refresh(activeFeed) },
        state = pullState,
        indicator = {
            PullToRefreshDefaults.Indicator(
                state = pullState,
                isRefreshing = state.refreshing,
                containerColor = DiscoverSurface,
                color = DiscoverAccent,
                modifier = Modifier.align(Alignment.TopCenter)
            )
        },
        modifier = Modifier
            .fillMaxSize()
            .background(DiscoverBackground)
    ) {
        LazyVerticalGrid(
            state = gridState,
            columns = GridCells.Fixed(if (isLandscape) 2 else 1),
            contentPadding = PaddingValues(bottom = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            val fullLine: (androidx.compose.foundation.lazy.grid.LazyGridItemSpanScope.() -> GridItemSpan) = { GridItemSpan(maxLineSpan) }

            if (!isLandscape) item(key = "title", span = fullLine) {
                Text(
                    text = "YouTube",
                    color = Color.White,
                    fontSize = 34.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 4.dp)
                )
            }

            item(key = "search", span = fullLine) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 4.dp)
                ) {
                    SearchBox(
                        query = query,
                        searching = search != null,
                        onQueryChange = { query = it },
                        onSearch = {
                            focusManager.clearFocus()
                            viewModel.submitSearch(query)
                        },
                        onClear = {
                            query = ""
                            viewModel.clearSearch()
                        },
                        modifier = Modifier.weight(1f)
                    )
                    RecentButton(selected = showRecent, onClick = {
                        focusManager.clearFocus()
                        viewModel.setShowRecent(!showRecent)
                    })
                }
            }

            if (showRecent) {
                item(key = "recentHeader", span = fullLine) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(start = 20.dp, end = 8.dp, top = 10.dp, bottom = 2.dp)
                    ) {
                        Text(
                            "Recently played",
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f)
                        )
                        if (recent.isNotEmpty()) TextButton(onClick = { confirmClearRecent = true }) {
                            Text("Clear all", color = DiscoverAccent)
                        }
                    }
                }
                if (recent.isEmpty()) item(key = "recentEmpty", span = fullLine) {
                    Message("Videos you play will show up here")
                }
                items(recent, key = { "recent|${it.id}" }) { video ->
                    val item = YouTubeItem(
                        id = video.id,
                        url = "https://www.youtube.com/watch?v=${video.id}",
                        title = video.title,
                        channel = video.channel,
                        thumbnail = video.thumbnail,
                        durationSeconds = video.durationSeconds,
                        uploadedAgo = "Watched " + YouTubeFeedSource.timeAgo(video.playedAt, System.currentTimeMillis())
                    )
                    VideoRow(
                        item = item,
                        onClick = { openItem(item) },
                        onPlayAudio = { openItem(item, audioOnly = true) },
                        onAddToPlaylist = { addToPlaylist = item },
                        onRemove = { viewModel.removeRecent(video.id) }
                    )
                }
                return@LazyVerticalGrid
            }

            if (search == null) {
                // Pinned: the chips stay at the top while the list scrolls under them
                stickyHeader(key = "chips") {
                    ChipRow(
                        chips = viewModel.chips(region),
                        selected = selected,
                        showSetState = region.state == null && !showPrompt,
                        onSelect = { feed ->
                            if (feed is YouTubeFeed.InState && feed == selected) showStatePicker = true
                            else viewModel.select(feed)
                        },
                        onSetState = { showStatePicker = true }
                    )
                }
                if (showPrompt) item(key = "prompt", span = fullLine) {
                    StatePromptCard(
                        detecting = detecting,
                        onAllow = ::detectOrAsk,
                        onPick = { showStatePicker = true },
                        onDismiss = viewModel::dismissStatePrompt
                    )
                }
            } else {
                item(key = "searchHeader", span = fullLine) {
                    Text(
                        text = "Results for \"${search?.query}\"",
                        color = DiscoverTextSecondary,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 6.dp, bottom = 4.dp)
                    )
                }
            }

            when {
                state.items.isEmpty() && state.loading -> item(key = "loading", span = fullLine) {
                    Box(Modifier.fillMaxWidth().padding(top = 64.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = DiscoverAccent)
                    }
                }

                state.items.isEmpty() && state.error -> item(key = "error", span = fullLine) {
                    Message("Couldn't load videos. Check your connection.", action = "Retry") {
                        viewModel.load(activeFeed, force = true)
                    }
                }

                state.items.isEmpty() -> item(key = "empty", span = fullLine) {
                    Message(if (search != null) "No videos found" else "Nothing here right now")
                }

                else -> {
                    items(state.items, key = { "${activeFeed.key}|${it.id}" }) { item ->
                        VideoRow(
                            item = item,
                            onClick = { openItem(item) },
                            onPlayAudio = { openItem(item, audioOnly = true) },
                            onAddToPlaylist = { addToPlaylist = item }
                        )
                    }
                    item(key = "footer", span = fullLine) {
                        when {
                            state.loadingMore -> Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(color = DiscoverAccent, strokeWidth = 2.dp, modifier = Modifier.size(26.dp))
                            }
                            state.error -> Message("Couldn't load more", action = "Retry") { viewModel.loadMore(activeFeed) }
                            state.ended -> Text(
                                "You're all caught up",
                                color = DiscoverTextSecondary,
                                fontSize = 13.sp,
                                modifier = Modifier.fillMaxWidth().padding(20.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            else -> Spacer(Modifier.height(1.dp))
                        }
                    }
                }
            }
        }
    }
}

/** Asks for the next page once the user is within a few rows of the end. */
@Composable
internal fun LoadMoreWhenNearEnd(gridState: LazyGridState, state: FeedState, loadMore: () -> Unit) {
    val nearEnd by remember(gridState) {
        derivedStateOf {
            val info = gridState.layoutInfo
            val last = info.visibleItemsInfo.lastOrNull()?.index ?: return@derivedStateOf false
            info.totalItemsCount > 0 && last >= info.totalItemsCount - 5
        }
    }
    LaunchedEffect(nearEnd, state.items.size, state.loadingMore, state.error) {
        if (nearEnd && state.items.isNotEmpty() && !state.loadingMore && !state.error && !state.ended) loadMore()
    }
}

@Composable
private fun SearchBox(
    query: String,
    searching: Boolean,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = { Text("Search YouTube videos", color = DiscoverTextSecondary, fontSize = 17.sp, maxLines = 1) },
        leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null, tint = DiscoverTextSecondary, modifier = Modifier.size(24.dp)) },
        trailingIcon = {
            if (query.isNotEmpty() || searching) IconButton(onClick = onClear) {
                Icon(Icons.Rounded.Close, contentDescription = "Clear search", tint = Color.White.copy(alpha = 0.8f))
            }
        },
        singleLine = true,
        textStyle = TextStyle(color = Color.White, fontSize = 18.sp),
        keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { onSearch() }),
        shape = RoundedCornerShape(28.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = DiscoverAccent,
            unfocusedBorderColor = DiscoverAccent.copy(alpha = 0.75f),
            focusedContainerColor = FieldFill,
            unfocusedContainerColor = FieldFill,
            cursorColor = DiscoverAccent
        ),
        modifier = modifier.height(58.dp)
    )
}

/** Round "Recent" button next to the search box; red when the Recent list is showing. */
@Composable
private fun RecentButton(selected: Boolean, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .padding(start = 10.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 4.dp, vertical = 2.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(if (selected) DiscoverAccent else FieldFill)
                .border(1.dp, if (selected) Color.Transparent else DiscoverAccent.copy(alpha = 0.6f), CircleShape)
        ) {
            Icon(
                Icons.Rounded.History,
                contentDescription = if (selected) "Hide recent videos" else "Recent videos",
                tint = if (selected) DiscoverOnAccent else Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
        Text("Recent", color = if (selected) DiscoverAccent else DiscoverTextSecondary, fontSize = 11.sp, modifier = Modifier.padding(top = 2.dp))
    }
}

@Composable
private fun ChipRow(
    chips: List<YouTubeFeed>,
    selected: YouTubeFeed,
    showSetState: Boolean,
    onSelect: (YouTubeFeed) -> Unit,
    onSetState: () -> Unit
) {
    LazyRow(
        // Opaque, so videos scrolling under the pinned chips don't show through
        modifier = Modifier
            .fillMaxWidth()
            .background(DiscoverBackground),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(chips, key = { it.key }) { feed ->
            val isSelected = feed.key == selected.key
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(if (isSelected) DiscoverAccent else DiscoverSurface)
                    .border(1.dp, if (isSelected) Color.Transparent else DiscoverBorder, RoundedCornerShape(50))
                    .clickable { onSelect(feed) }
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                if (feed is YouTubeFeed.InState) {
                    Icon(
                        Icons.Rounded.LocationOn,
                        contentDescription = null,
                        tint = if (isSelected) DiscoverOnAccent else DiscoverAccent,
                        modifier = Modifier.size(16.dp).padding(end = 2.dp)
                    )
                }
                Text(
                    text = chipLabel(feed),
                    color = if (isSelected) DiscoverOnAccent else Color.White.copy(alpha = 0.85f),
                    fontSize = 14.sp,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                )
            }
        }
        if (showSetState) item(key = "setState") {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .border(1.dp, DiscoverAccent.copy(alpha = 0.6f), RoundedCornerShape(50))
                    .clickable(onClick = onSetState)
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Icon(Icons.Rounded.LocationOn, contentDescription = null, tint = DiscoverAccent, modifier = Modifier.size(16.dp))
                Text("Your state", color = DiscoverAccent, fontSize = 14.sp, modifier = Modifier.padding(start = 4.dp))
            }
        }
    }
}

internal fun chipLabel(feed: YouTubeFeed): String = when (feed) {
    is YouTubeFeed.InState -> feed.state
    YouTubeFeed.Trending -> "Trending"
    YouTubeFeed.Music -> "Music"
    is YouTubeFeed.Mixes -> "Mixes"
    YouTubeFeed.Live -> "Live"
    YouTubeFeed.Gaming -> "Gaming"
    is YouTubeFeed.Search -> feed.query
    is YouTubeFeed.Playlist -> feed.title
}

@Composable
private fun StatePromptCard(detecting: Boolean, onAllow: () -> Unit, onPick: () -> Unit, onDismiss: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(DiscoverSurface)
            .border(1.dp, DiscoverAccent.copy(alpha = 0.35f), RoundedCornerShape(18.dp))
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(DiscoverAccent.copy(alpha = 0.18f))
            ) {
                Icon(Icons.Rounded.LocationOn, contentDescription = null, tint = DiscoverAccent, modifier = Modifier.size(20.dp))
            }
            Column(modifier = Modifier.weight(1f).padding(start = 12.dp)) {
                Text("See what's trending in your state", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                Text(
                    "We use your approximate location once to find your state.",
                    color = DiscoverTextSecondary,
                    fontSize = 13.sp
                )
            }
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End,
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
        ) {
            TextButton(onClick = onDismiss) { Text("Not now", color = DiscoverTextSecondary) }
            TextButton(onClick = onPick) { Text("Choose state", color = Color.White) }
            if (detecting) {
                CircularProgressIndicator(color = DiscoverAccent, strokeWidth = 2.dp, modifier = Modifier.padding(horizontal = 18.dp).size(20.dp))
            } else {
                TextButton(onClick = onAllow) { Text("Allow", color = DiscoverAccent, fontWeight = FontWeight.SemiBold) }
            }
        }
    }
}

@Composable
internal fun StatePickerDialog(
    current: String?,
    detecting: Boolean,
    onDetect: () -> Unit,
    onPick: (String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DialogColor,
        title = { Text("Your state", color = Color.White) },
        text = {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable(enabled = !detecting, onClick = onDetect)
                        .padding(vertical = 12.dp, horizontal = 4.dp)
                ) {
                    Icon(Icons.Rounded.LocationOn, contentDescription = null, tint = DiscoverAccent)
                    Text(
                        if (detecting) "Detecting…" else "Detect my location",
                        color = DiscoverAccent,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(start = 10.dp)
                    )
                }
                LazyColumn(modifier = Modifier.heightIn(max = 360.dp)) {
                    items(StateLanguages.INDIA.keys.toList()) { state ->
                        val isCurrent = state == current
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { onPick(state) }
                                .padding(vertical = 11.dp, horizontal = 4.dp)
                        ) {
                            Text(
                                state,
                                color = if (isCurrent) DiscoverAccent else Color.White,
                                fontSize = 16.sp,
                                fontWeight = if (isCurrent) FontWeight.SemiBold else FontWeight.Normal,
                                modifier = Modifier.weight(1f)
                            )
                            Text(StateLanguages.INDIA[state].orEmpty(), color = DiscoverTextSecondary, fontSize = 13.sp)
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel", color = DiscoverTextSecondary) } }
    )
}

/** A video (or playlist) row: 16:9 thumbnail with a duration / LIVE badge, then title, channel and stats. */
@Composable
internal fun VideoRow(
    item: YouTubeItem,
    onClick: () -> Unit,
    onPlayAudio: () -> Unit,
    onAddToPlaylist: () -> Unit,
    /** Shows "Remove from recent" in the menu (the Recent list). */
    onRemove: (() -> Unit)? = null
) {
    var menuOpen by remember { mutableStateOf(false) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(start = 16.dp, end = 4.dp, top = 8.dp, bottom = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .width(150.dp)
                .aspectRatio(16f / 9f)
                .clip(RoundedCornerShape(12.dp))
                .background(DiscoverSurface)
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(item.thumbnail.ifBlank { null } ?: R.drawable.placeholder)
                    .crossfade(true)
                    .placeholder(R.drawable.placeholder)
                    .error(R.drawable.placeholder)
                    .build(),
                contentDescription = item.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            val badge = when {
                item.isLive -> "LIVE"
                item.isPlaylist -> if (item.videoCount > 0) "${item.videoCount} videos" else "Playlist"
                item.durationSeconds > 0 -> formatDuration(item.durationSeconds)
                else -> null
            }
            badge?.let {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(6.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (item.isLive) LiveRed else Color.Black.copy(alpha = 0.78f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    if (item.isPlaylist) {
                        Icon(Icons.Rounded.PlaylistPlay, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                    }
                    Text(it, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 12.dp, top = 2.dp)
        ) {
            Text(
                text = item.title,
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                lineHeight = 19.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            if (item.channel.isNotBlank()) Text(
                text = item.channel,
                color = DiscoverTextSecondary,
                fontSize = 13.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 4.dp)
            )
            stats(item)?.let {
                Text(
                    text = it,
                    color = DiscoverTextSecondary,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 1.dp)
                )
            }
        }
        if (!item.isPlaylist) Box {
            IconButton(onClick = { menuOpen = true }, modifier = Modifier.size(36.dp)) {
                Icon(Icons.Rounded.MoreVert, contentDescription = "More options", tint = DiscoverTextSecondary, modifier = Modifier.size(20.dp))
            }
            DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                if (!item.isLive) DropdownMenuItem(
                    text = { Text("Play as audio") },
                    leadingIcon = { Icon(Icons.Rounded.Headphones, null) },
                    onClick = {
                        menuOpen = false
                        onPlayAudio()
                    }
                )
                DropdownMenuItem(
                    text = { Text("Add to playlist") },
                    leadingIcon = { Icon(Icons.AutoMirrored.Rounded.PlaylistAdd, null) },
                    onClick = {
                        menuOpen = false
                        onAddToPlaylist()
                    }
                )
                onRemove?.let { remove ->
                    DropdownMenuItem(
                        text = { Text("Remove from recent") },
                        leadingIcon = { Icon(Icons.Rounded.Delete, null) },
                        onClick = {
                            menuOpen = false
                            remove()
                        }
                    )
                }
            }
        } else {
            Spacer(Modifier.width(12.dp))
        }
    }
}

@Composable
private fun Message(text: String, action: String? = null, onAction: () -> Unit = {}) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 48.dp, bottom = 16.dp)
    ) {
        Text(text, color = DiscoverTextSecondary, fontSize = 15.sp)
        action?.let { TextButton(onClick = onAction) { Text(it, color = DiscoverAccent, fontSize = 16.sp) } }
    }
}

/** "12M views • 2 weeks ago", "1.2K watching" for live; null when YouTube gave neither. */
internal fun stats(item: YouTubeItem): String? {
    val views = item.views.takeIf { it >= 0 }?.let { formatCount(it) + if (item.isLive) " watching" else " views" }
    return listOfNotNull(views, item.uploadedAgo?.takeIf { it.isNotBlank() }).joinToString(" • ").ifEmpty { null }
}

internal fun formatCount(count: Long): String = when {
    count >= 1_000_000_000 -> trim(count / 1_000_000_000.0) + "B"
    count >= 1_000_000 -> trim(count / 1_000_000.0) + "M"
    count >= 1_000 -> trim(count / 1_000.0) + "K"
    else -> count.toString()
}

private fun trim(value: Double): String =
    if (value >= 100 || value % 1.0 < 0.05) value.toLong().toString() else String.format(java.util.Locale.US, "%.1f", value)

internal fun formatDuration(seconds: Long): String {
    val h = seconds / 3600
    val m = (seconds % 3600) / 60
    val s = seconds % 60
    return if (h > 0) String.format(java.util.Locale.US, "%d:%02d:%02d", h, m, s)
    else String.format(java.util.Locale.US, "%d:%02d", m, s)
}

private val FieldFill = Color(0xFF161314)
private val DialogColor = Color(0xFF161314)
private val LiveRed = Color(0xFFE5202E)
