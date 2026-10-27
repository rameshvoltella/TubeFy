package com.ramzmania.tubefy.ui.components.screen.youtube

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.ramzmania.tubefy.R
import com.ramzmania.tubefy.data.dto.base.searchformat.TubeFyCoreTypeData
import com.ramzmania.tubefy.data.youtube.YouTubeItem
import com.ramzmania.tubefy.ui.components.NavigationItem
import com.ramzmania.tubefy.ui.components.screen.library.PlayListDialogViewer
import com.ramzmania.tubefy.ui.theme.DiscoverAccent
import com.ramzmania.tubefy.ui.theme.DiscoverBackground
import com.ramzmania.tubefy.ui.theme.DiscoverSurface
import com.ramzmania.tubefy.ui.theme.DiscoverTextSecondary
import com.ramzmania.tubefy.utils.LocalNavController
import com.ramzmania.tubefy.viewmodel.ChannelViewModel
import com.ramzmania.tubefy.viewmodel.TubeFyViewModel
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

/** A YouTube channel: banner, avatar, name, subscribers, description, then its videos (newest first). */
@Composable
fun ChannelScreen(viewModel: ChannelViewModel = hiltViewModel()) {
    val navController = LocalNavController.current
    val args = remember { navController?.currentBackStackEntry?.arguments }
    fun arg(key: String) = URLDecoder.decode(args?.getString(key).orEmpty(), StandardCharsets.UTF_8.toString())
    val url = remember { arg("url") }
    val fallbackName = remember { arg("name") }

    val channel by viewModel.channel.collectAsState()
    val videos by viewModel.videos.collectAsState()
    var descriptionOpen by rememberSaveable { mutableStateOf(false) }
    var addToPlaylist by remember { mutableStateOf<YouTubeItem?>(null) }
    val listState = rememberLazyListState()

    LaunchedEffect(url) { viewModel.load(url, fallbackName) }

    // More videos when near the end of the list
    val nearEnd by remember {
        derivedStateOf {
            val info = listState.layoutInfo
            val last = info.visibleItemsInfo.lastOrNull()?.index ?: return@derivedStateOf false
            info.totalItemsCount > 0 && last >= info.totalItemsCount - 5
        }
    }
    LaunchedEffect(nearEnd, videos.items.size, videos.loadingMore) {
        if (nearEnd && videos.items.isNotEmpty() && !videos.loadingMore && !videos.error && !videos.ended) viewModel.loadMore()
    }

    addToPlaylist?.let { item ->
        val tubeFyViewModel: TubeFyViewModel = hiltViewModel()
        PlayListDialogViewer(
            viewModel = tubeFyViewModel,
            onDismiss = { addToPlaylist = null },
            currentSongDetails = TubeFyCoreTypeData(videoId = item.id, videoTitle = item.title, videoImage = item.thumbnail, artist = item.channel)
        )
    }

    fun enc(value: String) = URLEncoder.encode(value, StandardCharsets.UTF_8.toString())
    fun open(item: YouTubeItem, audioOnly: Boolean = false) {
        val nav = navController ?: return
        com.ramzmania.tubefy.core.analytics.Analytics.pendingSource = "channel"
        when {
            item.isPlaylist -> nav.navigate(NavigationItem.PlayList.createRoute(item.id, enc(item.title), enc(item.thumbnail.ifBlank { "nourl" })))
            audioOnly -> nav.navigate(NavigationItem.AudioPlayer.createRoute(enc(item.id), enc(item.thumbnail), enc(item.title), enc(item.title), enc(item.channel)))
            else -> nav.navigate(NavigationItem.Video.createRoute(enc(item.id), enc(item.title), enc(item.thumbnail), enc(item.channel)))
        }
    }

    Box(Modifier.fillMaxSize().background(DiscoverBackground)) {
        LazyColumn(state = listState, contentPadding = PaddingValues(bottom = 24.dp), modifier = Modifier.fillMaxSize()) {
            item(key = "header") {
                Column {
                    // Back + name bar
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)) {
                        IconButton(onClick = { navController?.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back", tint = Color.White)
                        }
                        Text(
                            channel?.name ?: fallbackName,
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    channel?.banner?.let { banner ->
                        AsyncImage(
                            model = banner,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                                .aspectRatio(5.5f)
                                .clip(RoundedCornerShape(14.dp))
                                .background(DiscoverSurface)
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp)) {
                        AsyncImage(
                            model = channel?.avatar ?: R.drawable.placeholder,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.size(72.dp).clip(CircleShape).background(DiscoverSurface)
                        )
                        Column(Modifier.weight(1f).padding(start = 14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    channel?.name ?: fallbackName,
                                    color = Color.White,
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f, fill = false)
                                )
                                if (channel?.verified == true) Icon(
                                    Icons.Rounded.CheckCircle,
                                    contentDescription = "Verified",
                                    tint = DiscoverTextSecondary,
                                    modifier = Modifier.padding(start = 6.dp).size(18.dp)
                                )
                            }
                            channel?.subscribers?.takeIf { it > 0 }?.let {
                                Text("${formatCount(it)} subscribers", color = DiscoverTextSecondary, fontSize = 14.sp, modifier = Modifier.padding(top = 2.dp))
                            }
                        }
                    }
                    channel?.description?.takeIf { it.isNotBlank() }?.let { description ->
                        Text(
                            description,
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 14.sp,
                            lineHeight = 19.sp,
                            maxLines = if (descriptionOpen) Int.MAX_VALUE else 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier
                                .padding(start = 16.dp, end = 16.dp, top = 12.dp)
                                .clickable { descriptionOpen = !descriptionOpen }
                        )
                    }
                    Text(
                        "Videos",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 4.dp)
                    )
                }
            }

            when {
                videos.items.isEmpty() && videos.loading -> item(key = "loading") {
                    Box(Modifier.fillMaxWidth().padding(top = 48.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = DiscoverAccent)
                    }
                }

                videos.items.isEmpty() && videos.error -> item(key = "error") {
                    Column(Modifier.fillMaxWidth().padding(top = 40.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(com.ramzmania.tubefy.core.youtube.YouTubeAccess.messageOr("Couldn't load this channel"), color = DiscoverTextSecondary, fontSize = 15.sp)
                        TextButton(onClick = { viewModel.load(url, fallbackName, force = true) }) { Text("Retry", color = DiscoverAccent) }
                    }
                }

                videos.items.isEmpty() -> item(key = "empty") {
                    Text("No videos yet", color = DiscoverTextSecondary, fontSize = 15.sp, modifier = Modifier.padding(16.dp))
                }

                else -> {
                    items(videos.items, key = { "channel|${it.id}" }) { item ->
                        // Already on this channel: no "Go to channel" in the menu
                        VideoRow(
                            item = item.copy(channelUrl = null),
                            onClick = { open(item) },
                            onPlayAudio = { open(item, audioOnly = true) },
                            onAddToPlaylist = { addToPlaylist = item }
                        )
                    }
                    item(key = "footer") {
                        when {
                            videos.loadingMore -> Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(color = DiscoverAccent, strokeWidth = 2.dp, modifier = Modifier.size(26.dp))
                            }
                            videos.error -> TextButton(onClick = viewModel::loadMore, modifier = Modifier.fillMaxWidth()) {
                                Text(com.ramzmania.tubefy.core.youtube.YouTubeAccess.messageOr("Couldn't load more") + " · Retry", color = DiscoverAccent)
                            }
                            videos.ended -> Text(
                                "You're all caught up",
                                color = DiscoverTextSecondary,
                                fontSize = 13.sp,
                                modifier = Modifier.fillMaxWidth().padding(20.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}
