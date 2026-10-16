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
    viewModel: TubeFyViewModel = hiltViewModel(), navController: NavController?, onRefresh: () -> Unit
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
            items(homePageResponses) { response ->
                response?.let {
                    when (it.cellType) {
                        CellType.LIST -> GridContentList(
                            heading = it.heading,
                            contentData = it.contentData,
                            navController = navController
                        )

                        CellType.HORIZONTAL_LIST, CellType.PLAYLIST_ONLY -> HorizontalContentList(
                            heading = it.heading,
                            contentData = it.contentData,
                            navController = navController
                        )

                        CellType.THREE_TYPE_CELL -> VerticalContentList(
                            heading = it.heading,
                            contentData = it.contentData,
                            navController = navController
                        )

                        CellType.SINGLE_CELL -> SingleContentCell(
                            contentData = it.contentData,
                            heading = it.heading,
                        )
                    }
                }
            }
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
                .data(imageUrl?.let { YoutubeCoreConstant.decodeThumpUrl(it) } ?: R.drawable.images)
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
                .data(data.thumbnail?.let { YoutubeCoreConstant.decodeThumpUrl(it) } ?: R.drawable.images)
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
