package com.ramzmania.tubefy.ui.tv

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ramzmania.tubefy.data.youtube.YouTubeFeed
import com.ramzmania.tubefy.data.youtube.YouTubeFeedSource
import com.ramzmania.tubefy.data.youtube.YouTubeItem
import com.ramzmania.tubefy.ui.components.screen.youtube.LoadMoreWhenNearEnd
import com.ramzmania.tubefy.ui.components.screen.youtube.StatePickerDialog
import com.ramzmania.tubefy.ui.components.screen.youtube.chipLabel
import com.ramzmania.tubefy.ui.theme.DiscoverAccent
import com.ramzmania.tubefy.ui.theme.DiscoverBackground
import com.ramzmania.tubefy.ui.theme.DiscoverTextSecondary
import com.ramzmania.tubefy.viewmodel.FeedState
import com.ramzmania.tubefy.viewmodel.YouTubeViewModel

/** What the side menu has open: a chip's feed, search, or the recent videos. */
private sealed interface TvSection {
    data class Feed(val feed: YouTubeFeed) : TvSection
    data object Search : TvSection
    data object Recent : TvSection
    data class Playlist(val feed: YouTubeFeed.Playlist) : TvSection
}

/**
 * The TV home: a side menu (Search, Recent, then the YouTube chips) and a grid of video cards that
 * loads more as the remote moves down. Moving through the menu switches the grid; OK on a card plays it.
 */
@Composable
fun TvYouTubeHome(
    onOpenVideo: (id: String, title: String, thumb: String, channel: String) -> Unit,
    viewModel: YouTubeViewModel = hiltViewModel()
) {
    val region by viewModel.region.collectAsState()
    val selected by viewModel.selected.collectAsState()
    val search by viewModel.search.collectAsState()
    val feeds by viewModel.feeds.collectAsState()
    val recent by viewModel.recent.collectAsState()
    val detecting by viewModel.detecting.collectAsState()
    val suggestions by viewModel.suggestions.collectAsState()

    // Saveable, so coming back from a video returns to the same section (e.g. search results)
    var section by rememberSaveable(stateSaver = tvSectionSaver { key -> viewModel.chips(viewModel.region.value).firstOrNull { it.key == key } }) {
        mutableStateOf<TvSection>(TvSection.Feed(selected))
    }
    var query by rememberSaveable { mutableStateOf("") }
    var showStatePicker by remember { mutableStateOf(false) }
    val menuFocus = remember { FocusRequester() }

    LaunchedEffect(Unit) { runCatching { menuFocus.requestFocus() } }

    if (showStatePicker) {
        StatePickerDialog(
            current = region.state,
            detecting = detecting,
            // TVs rarely know their location: the list is the way to pick here
            onDetect = { showStatePicker = false },
            onPick = {
                showStatePicker = false
                viewModel.pickState(it)
                viewModel.chips(viewModel.region.value).firstOrNull()?.let { feed -> section = TvSection.Feed(feed) }
            },
            onDismiss = { showStatePicker = false }
        )
    }

    // Back from Search / Recent goes to the first chip before leaving the app
    BackHandler(enabled = section is TvSection.Search || section is TvSection.Recent) {
        section = TvSection.Feed(selected)
    }

    // Back from a playlist returns to where it was opened from
    var beforePlaylist by remember { mutableStateOf<TvSection?>(null) }
    BackHandler(enabled = section is TvSection.Playlist) {
        section = beforePlaylist ?: TvSection.Feed(selected)
    }

    fun open(item: YouTubeItem) {
        if (item.isPlaylist) {
            val feed = YouTubeFeed.Playlist(item.id, item.title)
            beforePlaylist = section
            section = TvSection.Playlist(feed)
            viewModel.load(feed)
        } else {
            onOpenVideo(item.id, item.title, item.thumbnail, item.channel)
        }
    }

    Row(Modifier.fillMaxSize().background(DiscoverBackground)) {
        // Side menu
        Column(
            modifier = Modifier
                .width(250.dp)
                .fillMaxHeight()
                .background(Color(0xFF0D0B0C))
                .padding(horizontal = 16.dp, vertical = 28.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                "TubeFy",
                color = DiscoverAccent,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 18.dp, bottom = 18.dp)
            )
            TvMenuItem("Search", Icons.Rounded.Search, section == TvSection.Search, onFocus = {}, onClick = {
                // A fresh search each time: the bottom row starts with recently played videos
                query = ""
                viewModel.clearSuggestions()
                viewModel.clearSearch()
                section = TvSection.Search
            })
            TvMenuItem("Recent", Icons.Rounded.History, section == TvSection.Recent, onFocus = {}, onClick = { section = TvSection.Recent })
            Spacer(Modifier.height(12.dp))
            Text("YouTube", color = DiscoverTextSecondary, fontSize = 13.sp, modifier = Modifier.padding(start = 18.dp, bottom = 4.dp))
            viewModel.chips(region).forEachIndexed { index, feed ->
                val isOpen = (section as? TvSection.Feed)?.feed?.key == feed.key
                TvMenuItem(
                    label = chipLabel(feed),
                    icon = if (feed is YouTubeFeed.InState) Icons.Rounded.LocationOn else null,
                    selected = isOpen,
                    // Moving through the chips switches the grid, like YouTube on TV
                    onFocus = {
                        section = TvSection.Feed(feed)
                        viewModel.select(feed)
                    },
                    onClick = {
                        if (feed is YouTubeFeed.InState) showStatePicker = true
                        else {
                            section = TvSection.Feed(feed)
                            viewModel.select(feed)
                        }
                    },
                    modifier = if (index == 0) Modifier.focusRequester(menuFocus) else Modifier
                )
            }
            if (region.state == null) TvMenuItem(
                "Your state",
                Icons.Rounded.LocationOn,
                selected = false,
                onFocus = {},
                onClick = { showStatePicker = true }
            )
        }

        // Content
        Box(Modifier.weight(1f).fillMaxHeight()) {
            when (val open = section) {
                is TvSection.Feed -> FeedGrid(
                    title = chipLabel(open.feed).let { if (open.feed is YouTubeFeed.InState) "Trending in $it" else it },
                    feedKey = open.feed.key,
                    state = feeds[open.feed.key] ?: FeedState(loading = true),
                    onLoadMore = { viewModel.loadMore(open.feed) },
                    onRetry = { viewModel.load(open.feed, force = true) },
                    onOpen = ::open
                )

                TvSection.Search -> {
                    val results = search
                    val recentItems = recent.map { video ->
                        YouTubeItem(
                            id = video.id,
                            url = "https://www.youtube.com/watch?v=${video.id}",
                            title = video.title,
                            channel = video.channel,
                            thumbnail = video.thumbnail,
                            durationSeconds = video.durationSeconds,
                            uploadedAgo = "Watched " + YouTubeFeedSource.timeAgo(video.playedAt, System.currentTimeMillis())
                        )
                    }
                    TvSearchScreen(
                        query = query,
                        onQueryChange = {
                            query = it
                            viewModel.onQueryChanged(it)
                        },
                        suggestions = if (query.isBlank()) emptyList() else suggestions,
                        onSearch = { text ->
                            if (text.isNotBlank()) {
                                query = text
                                viewModel.submitSearch(text)
                            }
                        },
                        rowTitle = if (results != null) "Results for \"${results.query}\"" else "Recently played",
                        rowState = if (results != null) feeds[results.key] ?: FeedState(loading = true) else FeedState(items = recentItems, ended = true),
                        emptyText = if (results != null) "No videos found" else "Videos you play will show up here",
                        onLoadMore = { results?.let(viewModel::loadMore) },
                        onOpen = ::open
                    )
                }

                is TvSection.Playlist -> FeedGrid(
                    title = open.feed.title,
                    feedKey = open.feed.key,
                    state = feeds[open.feed.key] ?: FeedState(loading = true),
                    onLoadMore = { viewModel.loadMore(open.feed) },
                    onRetry = { viewModel.load(open.feed, force = true) },
                    onOpen = ::open
                )

                TvSection.Recent -> {
                    val items = recent.map { video ->
                        YouTubeItem(
                            id = video.id,
                            url = "https://www.youtube.com/watch?v=${video.id}",
                            title = video.title,
                            channel = video.channel,
                            thumbnail = video.thumbnail,
                            durationSeconds = video.durationSeconds,
                            uploadedAgo = "Watched " + YouTubeFeedSource.timeAgo(video.playedAt, System.currentTimeMillis())
                        )
                    }
                    FeedGrid(
                        title = "Recently played",
                        feedKey = "recent",
                        state = FeedState(items = items, ended = true),
                        emptyText = "Videos you play will show up here",
                        onLoadMore = {},
                        onRetry = {},
                        onOpen = ::open
                    )
                }
            }
        }
    }
}

@Composable
private fun FeedGrid(
    title: String,
    feedKey: String,
    state: FeedState,
    onLoadMore: () -> Unit,
    onRetry: () -> Unit,
    onOpen: (YouTubeItem) -> Unit,
    emptyText: String = "Nothing here right now"
) {
    val gridState = key(feedKey) { rememberLazyGridState() }
    LoadMoreWhenNearEnd(gridState, state, onLoadMore)

    LazyVerticalGrid(
        state = gridState,
        columns = GridCells.Fixed(4),
        contentPadding = PaddingValues(start = 28.dp, end = 28.dp, top = 20.dp, bottom = 48.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item(span = { GridItemSpan(maxLineSpan) }) {
            Text(title, color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 6.dp, bottom = 4.dp))
        }
        when {
            state.items.isEmpty() && state.loading -> item(span = { GridItemSpan(maxLineSpan) }) {
                Box(Modifier.fillMaxWidth().padding(top = 80.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = DiscoverAccent)
                }
            }

            state.items.isEmpty() && state.error -> item(span = { GridItemSpan(maxLineSpan) }) {
                Column(Modifier.fillMaxWidth().padding(top = 60.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Couldn't load videos. Check your connection.", color = DiscoverTextSecondary, fontSize = 18.sp)
                    TextButton(onClick = onRetry) { Text("Retry", color = DiscoverAccent, fontSize = 18.sp) }
                }
            }

            state.items.isEmpty() -> item(span = { GridItemSpan(maxLineSpan) }) {
                Text(emptyText, color = DiscoverTextSecondary, fontSize = 18.sp, modifier = Modifier.padding(start = 6.dp, top = 40.dp))
            }

            else -> {
                items(state.items, key = { "$feedKey|${it.id}" }) { item ->
                    TvVideoCard(item = item, onClick = { onOpen(item) })
                }
                if (state.loadingMore) item(span = { GridItemSpan(maxLineSpan) }) {
                    Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = DiscoverAccent, strokeWidth = 3.dp, modifier = Modifier.size(32.dp))
                    }
                }
            }
        }
    }
}

/** Saves the open section as text: "feed:<key>", "search", "recent" or "playlist:<id>|<title>". */
private fun tvSectionSaver(feedFor: (String) -> YouTubeFeed?) = androidx.compose.runtime.saveable.Saver<TvSection, String>(
    save = { section ->
        when (section) {
            is TvSection.Feed -> "feed:${section.feed.key}"
            TvSection.Search -> "search"
            TvSection.Recent -> "recent"
            is TvSection.Playlist -> "playlist:${section.feed.id}|${section.feed.title}"
        }
    },
    restore = { saved ->
        when {
            saved == "search" -> TvSection.Search
            saved == "recent" -> TvSection.Recent
            saved.startsWith("playlist:") -> saved.removePrefix("playlist:").split("|", limit = 2).let {
                TvSection.Playlist(YouTubeFeed.Playlist(it[0], it.getOrElse(1) { "" }))
            }
            saved.startsWith("feed:") -> feedFor(saved.removePrefix("feed:"))?.let { TvSection.Feed(it) }
            else -> null
        }
    }
)
