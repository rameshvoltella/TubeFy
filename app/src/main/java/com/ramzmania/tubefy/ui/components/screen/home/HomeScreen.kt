package com.ramzmania.tubefy.ui.components.screen.home

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
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material.icons.automirrored.rounded.QueueMusic
import androidx.compose.material.icons.rounded.RemoveCircleOutline
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import com.ramzmania.tubefy.ui.theme.DiscoverAccent
import com.ramzmania.tubefy.ui.theme.DiscoverTextSecondary
import androidx.compose.material.icons.rounded.DownloadDone
import androidx.compose.material.icons.rounded.Star
import androidx.compose.ui.graphics.Brush
import com.ramzmania.tubefy.core.download.Downloads
import com.ramzmania.tubefy.data.local.PlaylistVisit
import com.ramzmania.tubefy.ui.components.screen.album.openTrack
import com.ramzmania.tubefy.data.local.PlayedSong
import kotlin.random.Random
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.ramzmania.tubefy.R
import com.ramzmania.tubefy.core.YoutubeCoreConstant
import com.ramzmania.tubefy.data.Resource
import com.ramzmania.tubefy.data.dto.base.BaseContentData
import com.ramzmania.tubefy.data.dto.base.searchformat.StreamUrlData
import com.ramzmania.tubefy.data.dto.base.searchformat.TubeFyCoreTypeData
import com.ramzmania.tubefy.ui.components.screen.library.PlayListDialogViewer
import com.ramzmania.tubefy.data.dto.home.CellType
import com.ramzmania.tubefy.data.dto.home.HomePageResponse
import com.ramzmania.tubefy.ui.components.NavigationItem
import com.ramzmania.tubefy.ui.theme.DiscoverAccent
import com.ramzmania.tubefy.ui.theme.DiscoverBorder
import com.ramzmania.tubefy.ui.theme.DiscoverSurface
import com.ramzmania.tubefy.viewmodel.TubeFyViewModel
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun HomePageContentList(
    homePageResponses: List<HomePageResponse?>,
    viewModel: TubeFyViewModel = hiltViewModel(), navController: NavController?, onRefresh: () -> Unit,
    header: (@Composable () -> Unit)? = null,
    /** Extra rows placed between the sections: (1-based position in the list, content). */
    inlineBlocks: List<Pair<Int, @Composable () -> Unit>> = emptyList()
) {
    val streamUrlData by viewModel.streamUrlData.observeAsState()
    val playListData by viewModel.youTubePlayListData.observeAsState()
    val context = LocalContext.current
    val lazyListState = rememberLazyListState()
    val isLoading = viewModel.loadMoreHomeData.collectAsState()
    val loadMoreHomePageEnded = viewModel.loadMoreHomePageEnded.collectAsState()
    val isRefreshing = viewModel.pullToRefreshPage.collectAsState()
    val showPlayListDialog by viewModel.showPlayListDialog.collectAsState()
    val selectedTrack by viewModel.selectedTrack.collectAsState()

    selectedTrack?.let { track ->
        if (showPlayListDialog) {
            PlayListDialogViewer(
                viewModel = viewModel,
                onDismiss = { viewModel.showPlayListDialog(false) },
                track
            )
        }
    }

    val pullRefreshState = rememberPullRefreshState(
        refreshing = isRefreshing.value,
        onRefresh = {
            viewModel.pullToRefreshHome(true)
            onRefresh()
        }
    )
    LaunchedEffect(lazyListState) {
        snapshotFlow { lazyListState.layoutInfo }
            .collect { layoutInfo ->
                if (layoutInfo.visibleItemsInfo.isNotEmpty()) {
                    val lastVisibleItem = layoutInfo.visibleItemsInfo.last()
                    val totalItems = layoutInfo.totalItemsCount
                    // Load the next page once the end of the list is reached
                    if (lastVisibleItem.index >= totalItems - 1 && !isLoading.value && !loadMoreHomePageEnded.value) {
                        if (homePageResponses.size > 1) {
                            viewModel.setHomePageLoadMoreState(true)
                        }
                    }
                }
            }
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .pullRefresh(pullRefreshState)
    ) {
        LazyColumn(state = lazyListState) {
            header?.let { item { it() } }
            // Each inline block (e.g. "Jump back in", "Recommended for today") becomes the item at its
            // position, or follows the last section when there are fewer. take/drop never go out of range.
            val blocks = inlineBlocks.sortedBy { it.first }
            val splits = inlineSplitIndices(blocks.map { it.first }, hasHeader = header != null, sectionCount = homePageResponses.size)
            var start = 0
            blocks.forEachIndexed { index, (_, content) ->
                val split = splits[index]
                items(homePageResponses.drop(start).take(split - start)) { response -> HomeSection(response, navController) }
                item { content() }
                start = split
            }
            items(homePageResponses.drop(start)) { response -> HomeSection(response, navController) }
            if (isLoading.value) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = DiscoverAccent)
                    }
                }
            }
            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
        PullRefreshIndicator(
            refreshing = isRefreshing.value,
            state = pullRefreshState,
            modifier = Modifier.align(Alignment.TopCenter)
        )
    }
    LaunchedEffect(key1 = playListData) {
        if (playListData is Resource.Success) {
            if (playListData!!.data!!.playListVideoList?.get(0)?.videoId != null) {
                viewModel.getStreamUrl(playListData!!.data!!.playListVideoList?.get(0)?.videoId!!)
            }
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
}

/**
 * How many sections come before an inline block that should be item number [inlinePosition]
 * (1-based, counting the header). Always within 0..[sectionCount].
 */
internal fun inlineSplitIndex(inlinePosition: Int, hasHeader: Boolean, sectionCount: Int): Int =
    (inlinePosition - 1 - (if (hasHeader) 1 else 0)).coerceIn(0, maxOf(sectionCount, 0))

/**
 * For inline blocks at 1-based [positions] (ascending; counting the header and earlier blocks),
 * how many sections come before each one. Non-decreasing and always within 0..[sectionCount].
 */
internal fun inlineSplitIndices(positions: List<Int>, hasHeader: Boolean, sectionCount: Int): List<Int> {
    var previous = 0
    return positions.mapIndexed { blocksBefore, position ->
        val split = inlineSplitIndex(position - blocksBefore, hasHeader, sectionCount).coerceAtLeast(previous)
        previous = split
        split
    }
}

@Composable
private fun HomeSection(response: HomePageResponse?, navController: NavController?) {
    response ?: return
    when (response.cellType) {
        CellType.LIST -> GridContentList(
            heading = response.heading,
            contentData = response.contentData,
            navController = navController
        )

        CellType.HORIZONTAL_LIST, CellType.PLAYLIST_ONLY -> HorizontalContentList(
            heading = response.heading,
            contentData = response.contentData,
            navController = navController
        )

        CellType.THREE_TYPE_CELL -> VerticalContentList(
            heading = response.heading,
            contentData = response.contentData,
            navController = navController
        )

        CellType.SINGLE_CELL -> SingleContentCell(
            contentData = response.contentData,
            heading = response.heading,
        )
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        fontWeight = FontWeight.Bold,
        color = Color.White,
        fontSize = 22.sp,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 26.dp, bottom = 14.dp)
    )
}

@Composable
fun VerticalContentList(
    heading: String?,
    navController: NavController?,
    contentData: List<BaseContentData>?,
    viewModel: TubeFyViewModel = hiltViewModel()
) {
    contentData?.let {
        Column {
            heading?.let { title -> SectionHeader(title) }
            Column(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                it.forEach { data ->
                    QuickPickCard(
                        data = data,
                        modifier = Modifier.fillMaxWidth(),
                        onMoreClick = { viewModel.showPlayListDialog(true, data.toTrack()) }
                    ) { selectedItem ->
                        navController?.let { nav -> openSong(nav, selectedItem) }
                    }
                }
            }
        }
    }
}

/** "Quick picks": two-column grid of song cards. */
@Composable
fun GridContentList(
    heading: String?,
    navController: NavController?,
    contentData: List<BaseContentData>?,
    viewModel: TubeFyViewModel = hiltViewModel()
) {
    contentData?.let {
        Column {
            SectionHeader(heading ?: "Quick picks")
            Column(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                it.chunked(2).forEach { rowItems ->
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        rowItems.forEach { data ->
                            QuickPickCard(
                                data = data,
                                modifier = Modifier.weight(1f),
                                onMoreClick = { viewModel.showPlayListDialog(true, data.toTrack()) }
                            ) { selectedItem ->
                                navController?.let { nav -> openSong(nav, selectedItem) }
                            }
                        }
                        if (rowItems.size == 1) Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

/** Horizontal carousel of playlists / albums ("Made for your mood", "India's biggest hits"). */
@Composable
fun HorizontalContentList(
    heading: String?,
    navController: NavController?,
    contentData: List<BaseContentData>?,
    viewModel: TubeFyViewModel = hiltViewModel()
) {
    contentData?.let {
        Column {
            SectionHeader(heading ?: "Top Mix")
            LazyRow(
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(it) { data ->
                    ContentItem(data = data) { selectedItem ->
                        val nav = navController ?: return@ContentItem
                        if (selectedItem.videoId?.length!! > 11 || selectedItem.playlistId!!.startsWith("PLAYLIST-ID-")) {
                            viewModel.setHomeScreenReload(false)
                            openPlaylist(nav, selectedItem)
                        } else if (selectedItem.playlistId.length > 1) {
                            openPlaylist(nav, selectedItem)
                        } else {
                            viewModel.getStreamUrl(selectedItem.videoId)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SingleContentCell(
    heading: String?,
    contentData: List<BaseContentData>?,
    viewModel: TubeFyViewModel = hiltViewModel()
) {
    contentData?.firstOrNull()?.let { data ->
        Column {
            heading?.let { SectionHeader(it) }
            ContentItem(data = data, modifier = Modifier.padding(horizontal = 20.dp), size = 280.dp) { selectedItem ->
                viewModel.getStreamUrl(selectedItem.videoId!!)
            }
        }
    }
}

/** Square artwork card with a title underneath. */
@Composable
fun ContentItem(
    data: BaseContentData,
    modifier: Modifier = Modifier,
    size: Dp = 168.dp,
    onClick: (BaseContentData) -> Unit
) {
    ArtworkCard(
        imageUrl = data.thumbnail,
        title = data.title.orEmpty(),
        size = size,
        modifier = modifier,
        onClick = { onClick(data) }
    )
}

@Composable
fun ArtworkCard(
    imageUrl: String?,
    title: String,
    modifier: Modifier = Modifier,
    size: Dp = 168.dp,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier
            .width(size)
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(imageUrl?.let { YoutubeCoreConstant.decodeThumpUrl(it) } ?: R.drawable.placeholder)
                .crossfade(true)
                .placeholder(R.drawable.placeholder)
                .error(R.drawable.placeholder)
                .build(),
            contentDescription = title,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(14.dp))
        )
        Text(
            text = title,
            color = Color.White,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = 10.dp, bottom = 4.dp)
        )
    }
}

/** Compact song card used in "Quick picks". */
@Composable
fun QuickPickCard(
    data: BaseContentData,
    modifier: Modifier = Modifier,
    onMoreClick: (() -> Unit)? = null,
    onClick: (BaseContentData) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .height(66.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(DiscoverSurface)
            .border(1.dp, DiscoverBorder, RoundedCornerShape(12.dp))
            .clickable { onClick(data) }
            .padding(6.dp)
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(data.thumbnail?.let { YoutubeCoreConstant.decodeThumpUrl(it) } ?: R.drawable.placeholder)
                .crossfade(true)
                .placeholder(R.drawable.placeholder)
                .error(R.drawable.placeholder)
                .build(),
            contentDescription = data.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(54.dp)
                .clip(RoundedCornerShape(8.dp))
        )
        Text(
            text = data.title.orEmpty(),
            color = Color.White,
            fontSize = 12.5.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            lineHeight = 16.sp,
            modifier = Modifier
                .weight(1f)
                .padding(start = 10.dp, end = 2.dp)
        )
        if (onMoreClick != null) {
            Icon(
                Icons.Rounded.MoreVert,
                contentDescription = "More options",
                tint = Color.White.copy(alpha = 0.8f),
                modifier = Modifier
                    .clip(CircleShape)
                    .clickable(onClick = onMoreClick)
                    .padding(vertical = 8.dp)
                    .size(18.dp)
            )
        }
    }
}

private fun BaseContentData.toTrack() = TubeFyCoreTypeData(
    videoId = videoId.orEmpty(),
    videoTitle = title.orEmpty(),
    videoImage = thumbnail?.let { YoutubeCoreConstant.decodeThumpUrl(it) }.orEmpty()
)

private fun openSong(navController: NavController, selectedItem: BaseContentData) {
    val encodedVideoUrl = URLEncoder.encode(
        YoutubeCoreConstant.decodeThumpUrl(selectedItem.thumbnail!!), StandardCharsets.UTF_8.toString()
    )
    val encodedVideoId = URLEncoder.encode(selectedItem.videoId, StandardCharsets.UTF_8.toString())
    val encodedVideoTitle = URLEncoder.encode(selectedItem.title, StandardCharsets.UTF_8.toString())

    navController.navigate(
        NavigationItem.AudioPlayer.createRoute(
            encodedVideoId,
            encodedVideoUrl,
            encodedVideoTitle,
            encodedVideoTitle,
            encodedVideoTitle
        )
    ) {
        navController.graph.startDestinationRoute?.let { route ->
            popUpTo(route) {
                saveState = true
            }
        }
        launchSingleTop = true
        restoreState = true
    }
}

private fun openPlaylist(navController: NavController, selectedItem: BaseContentData) {
    navController.navigate(
        NavigationItem.PlayList.createRoute(
            selectedItem.playlistId!!,
            selectedItem.title!!, URLEncoder.encode(
                YoutubeCoreConstant.decodeThumpUrl(selectedItem.thumbnail?.takeIf { it.isNotEmpty() } ?: "nourl"),
                StandardCharsets.UTF_8.toString()
            )
        )
    ) {
        navController.graph.startDestinationRoute?.let { route ->
            popUpTo(route) {
                saveState = true
            }
        }
        launchSingleTop = true
        restoreState = true
    }
}

/**
 * Home's first item: the user's top playlists as a compact 2-column grid.
 * Long-press a card for options (open / remove from this list).
 */
@Composable
fun TopPlaylistsGrid(
    visits: List<PlaylistVisit>,
    onOpen: (PlaylistVisit) -> Unit,
    onRemove: (PlaylistVisit) -> Unit
) {
    var optionsFor by remember { mutableStateOf<PlaylistVisit?>(null) }

    optionsFor?.let { visit ->
        TopPlaylistOptionsSheet(
            visit = visit,
            onOpen = {
                optionsFor = null
                onOpen(visit)
            },
            onRemove = {
                optionsFor = null
                onRemove(visit)
            },
            onDismiss = { optionsFor = null }
        )
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 4.dp)
    ) {
        visits.chunked(2).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                row.forEach { visit ->
                    TopPlaylistCard(
                        visit = visit,
                        modifier = Modifier.weight(1f),
                        onClick = { onOpen(visit) },
                        onLongClick = { optionsFor = visit }
                    )
                }
                if (row.size == 1) Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun TopPlaylistCard(
    visit: PlaylistVisit,
    modifier: Modifier,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    val haptics = LocalHapticFeedback.current
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .height(60.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(DiscoverSurface)
            .border(1.dp, DiscoverBorder, RoundedCornerShape(12.dp))
            .combinedClickable(
                onClick = onClick,
                onLongClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    onLongClick()
                }
            )
            .padding(6.dp)
    ) {
        PlaylistVisitArt(visit, size = 48.dp)
        Text(
            text = displayName(visit),
            color = Color.White,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            lineHeight = 16.sp,
            modifier = Modifier
                .weight(1f)
                .padding(start = 10.dp, end = 4.dp)
        )
    }
}

/** Long-press options for a top playlist, in the app's bottom sheet style. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopPlaylistOptionsSheet(
    visit: PlaylistVisit,
    onOpen: () -> Unit,
    onRemove: () -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = Color(0xFF121011),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 10.dp, bottom = 6.dp)
                    .size(width = 40.dp, height = 4.dp)
                    .clip(RoundedCornerShape(50))
                    .background(Color.White.copy(alpha = 0.25f))
            )
        }
    ) {
        Column(
            modifier = Modifier
                .navigationBarsPadding()
                .padding(bottom = 12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
            ) {
                PlaylistVisitArt(visit, size = 56.dp)
                Column(modifier = Modifier.padding(start = 14.dp)) {
                    Text(
                        text = displayName(visit),
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = if (visit.isCustom) "Your playlist" else "Playlist",
                        color = DiscoverTextSecondary,
                        fontSize = 14.sp
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            SheetOption(
                icon = Icons.AutoMirrored.Rounded.QueueMusic,
                title = "Open playlist",
                onClick = onOpen
            )
            SheetOption(
                icon = Icons.Rounded.RemoveCircleOutline,
                title = "Remove from this list",
                subtitle = "The playlist itself won't be deleted",
                tint = DiscoverAccent,
                onClick = onRemove
            )
        }
    }
}

@Composable
private fun SheetOption(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit,
    subtitle: String? = null,
    tint: Color = Color.White
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 14.dp)
    ) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(24.dp))
        Column(modifier = Modifier.padding(start = 18.dp)) {
            Text(title, color = tint, fontSize = 16.sp, fontWeight = FontWeight.Medium)
            subtitle?.let { Text(it, color = DiscoverTextSecondary, fontSize = 13.sp) }
        }
    }
}

@Composable
private fun PlaylistVisitArt(visit: PlaylistVisit, size: Dp) {
    when (visit.id) {
        FAVORITES_PLAYLIST_NAME -> PlaylistTile(Brush.linearGradient(listOf(Color(0xFFD6334A), Color(0xFF3A1219))), size) {
            Icon(Icons.Rounded.Star, contentDescription = null, tint = Color(0xFFFFD54F), modifier = Modifier.size(size * 0.55f))
        }
        Downloads.PLAYLIST -> PlaylistTile(Brush.linearGradient(listOf(Color(0xFF27AE60), Color(0xFF0E3B22))), size) {
            Icon(Icons.Rounded.DownloadDone, contentDescription = null, tint = Color.White, modifier = Modifier.size(size * 0.5f))
        }
        else -> AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(visit.thumbnail.takeIf { it.isNotBlank() }?.let { YoutubeCoreConstant.decodeThumpUrl(it) } ?: R.drawable.placeholder)
                .crossfade(true)
                .placeholder(R.drawable.placeholder)
                .error(R.drawable.placeholder)
                .build(),
            contentDescription = visit.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(size)
                .clip(RoundedCornerShape(8.dp))
        )
    }
}

/** An entry of "Jump back in": a song or a playlist the user chose to play. */
sealed interface JumpBackInItem {
    val key: String
    val lastPlayedAt: Long

    data class Song(val song: PlayedSong) : JumpBackInItem {
        override val key get() = "song:${song.videoId}"
        override val lastPlayedAt get() = song.playedAt
    }

    data class Playlist(val visit: PlaylistVisit) : JumpBackInItem {
        override val key get() = visit.key
        override val lastPlayedAt get() = visit.lastPlayedAt
    }
}

/** Fixed per process, so the order changes on each app launch but stays put while it runs. */
private val launchSeed = Random.nextLong()

/**
 * The [maxCount] most recent songs and playlists, shuffled per launch;
 * empty until there are at least [minCount].
 */
fun jumpBackInItems(
    songs: List<PlayedSong>,
    visits: List<PlaylistVisit>,
    minCount: Int = 5,
    maxCount: Int = 15
): List<JumpBackInItem> {
    val recent = (songs.map { JumpBackInItem.Song(it) } + visits.map { JumpBackInItem.Playlist(it) })
        .sortedByDescending { it.lastPlayedAt }
        .take(maxCount)
    return if (recent.size < minCount) emptyList() else recent.shuffled(Random(launchSeed))
}

@Composable
fun JumpBackInRow(items: List<JumpBackInItem>, navController: NavController?) {
    Column {
        SectionHeader("Jump back in")
        LazyRow(
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            items(items, key = { it.key }) { item ->
                JumpBackInCard(item) {
                    val nav = navController ?: return@JumpBackInCard
                    when (item) {
                        is JumpBackInItem.Song -> openSong(
                            nav,
                            BaseContentData(
                                videoId = item.song.videoId,
                                playlistId = "",
                                thumbnail = item.song.thumbnail.ifBlank { "https://i.ytimg.com/vi/${item.song.videoId}/hq720.jpg" },
                                title = item.song.title
                            )
                        )
                        is JumpBackInItem.Playlist -> openPlaylistVisit(nav, item.visit)
                    }
                }
            }
        }
    }
}

@Composable
private fun JumpBackInCard(item: JumpBackInItem, onClick: () -> Unit) {
    val cardSize = 150.dp
    Column(
        modifier = Modifier
            .width(cardSize)
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
    ) {
        when (item) {
            is JumpBackInItem.Song -> AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(item.song.thumbnail.takeIf { it.isNotBlank() }?.let { YoutubeCoreConstant.decodeThumpUrl(it) } ?: R.drawable.placeholder)
                    .crossfade(true)
                    .placeholder(R.drawable.placeholder)
                    .error(R.drawable.placeholder)
                    .build(),
                contentDescription = item.song.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(cardSize)
                    .clip(RoundedCornerShape(14.dp))
            )
            is JumpBackInItem.Playlist -> Box(modifier = Modifier.clip(RoundedCornerShape(14.dp))) {
                PlaylistVisitArt(item.visit, size = cardSize)
            }
        }
        Text(
            text = when (item) {
                is JumpBackInItem.Song -> item.song.title
                is JumpBackInItem.Playlist -> displayName(item.visit)
            },
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = 8.dp)
        )
        Text(
            text = if (item is JumpBackInItem.Song) "Song" else "Playlist",
            color = DiscoverTextSecondary,
            fontSize = 12.sp,
            modifier = Modifier.padding(top = 2.dp, bottom = 4.dp)
        )
    }
}

private fun displayName(visit: PlaylistVisit) = when (visit.id) {
    FAVORITES_PLAYLIST_NAME -> "Favorites"
    Downloads.PLAYLIST -> "Downloads"
    else -> visit.title
}

@Composable
private fun PlaylistTile(background: Brush, size: Dp = 48.dp, icon: @Composable () -> Unit) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(size)
            .clip(RoundedCornerShape(8.dp))
            .background(background)
    ) { icon() }
}

/** Opens a top playlist: the user's own playlists in the library page, YouTube ones in the album screen. */
fun openPlaylistVisit(navController: NavController, visit: PlaylistVisit) {
    val thumbnail = URLEncoder.encode(
        YoutubeCoreConstant.decodeThumpUrl(visit.thumbnail.takeIf { it.isNotEmpty() } ?: "nourl"),
        StandardCharsets.UTF_8.toString()
    )
    val route = if (visit.isCustom) {
        NavigationItem.MyPlayList.createRoute(thumbnail, visit.id)
    } else {
        NavigationItem.PlayList.createRoute(
            visit.id,
            URLEncoder.encode(visit.title, StandardCharsets.UTF_8.toString()),
            thumbnail
        )
    }
    navController.navigate(route) {
        navController.graph.startDestinationRoute?.let { startRoute ->
            popUpTo(startRoute) {
                saveState = true
            }
        }
        launchSingleTop = true
        restoreState = true
    }
}

private const val FAVORITES_PLAYLIST_NAME = "TubeFy-Favorites"

/** Home's "Recommended for today": songs and artist playlists picked for the user's taste. */
@Composable
fun RecommendedTodayRow(items: List<TubeFyCoreTypeData>, navController: NavController?) {
    Column {
        SectionHeader("Recommended for today")
        LazyRow(
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            items(items, key = { it.videoId.ifEmpty { it.plaListUrl.orEmpty() } }) { item ->
                val isPlaylist = item.videoId.isEmpty()
                val cardSize = 150.dp
                Column(
                    modifier = Modifier
                        .width(cardSize)
                        .clip(RoundedCornerShape(14.dp))
                        .clickable { navController?.let { openTrack(it, item) } }
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(item.videoImage.takeIf { it.isNotBlank() }?.let { YoutubeCoreConstant.decodeThumpUrl(it) } ?: R.drawable.placeholder)
                            .crossfade(true)
                            .placeholder(R.drawable.placeholder)
                            .error(R.drawable.placeholder)
                            .build(),
                        contentDescription = item.videoTitle,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(cardSize)
                            .clip(RoundedCornerShape(14.dp))
                    )
                    Text(
                        text = item.videoTitle,
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                    Text(
                        text = when {
                            isPlaylist -> "Playlist"
                            item.artist.isNotBlank() -> item.artist
                            else -> "Song"
                        },
                        color = DiscoverTextSecondary,
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 2.dp, bottom = 4.dp)
                    )
                }
            }
        }
    }
}
