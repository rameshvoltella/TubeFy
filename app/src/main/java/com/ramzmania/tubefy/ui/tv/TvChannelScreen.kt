package com.ramzmania.tubefy.ui.tv

import android.os.Bundle
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.ramzmania.tubefy.R
import com.ramzmania.tubefy.data.youtube.YouTubeItem
import com.ramzmania.tubefy.ui.components.screen.youtube.LoadMoreWhenNearEnd
import com.ramzmania.tubefy.ui.components.screen.youtube.formatCount
import com.ramzmania.tubefy.ui.theme.DiscoverAccent
import com.ramzmania.tubefy.ui.theme.DiscoverBackground
import com.ramzmania.tubefy.ui.theme.DiscoverSurface
import com.ramzmania.tubefy.ui.theme.DiscoverTextSecondary
import com.ramzmania.tubefy.viewmodel.ChannelViewModel
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

/** TV channel page: banner header, then a grid of the channel's videos that loads more as you go down. */
@Composable
fun TvChannelScreen(args: Bundle?, onOpenVideo: (YouTubeItem) -> Unit, viewModel: ChannelViewModel = hiltViewModel()) {
    fun arg(key: String) = URLDecoder.decode(args?.getString(key).orEmpty(), StandardCharsets.UTF_8.toString())
    val url = remember { arg("url") }
    val fallbackName = remember { arg("name") }
    val channel by viewModel.channel.collectAsState()
    val videos by viewModel.videos.collectAsState()
    val gridState = rememberLazyGridState()
    val firstCard = remember { FocusRequester() }

    LaunchedEffect(url) { viewModel.load(url, fallbackName) }
    LoadMoreWhenNearEnd(gridState, videos) { viewModel.loadMore() }
    // Focus lands on the first video once they're loaded
    LaunchedEffect(videos.items.isNotEmpty()) {
        if (videos.items.isNotEmpty()) runCatching { firstCard.requestFocus() }
    }

    LazyVerticalGrid(
        state = gridState,
        columns = GridCells.Fixed(4),
        contentPadding = PaddingValues(start = 48.dp, end = 48.dp, bottom = 48.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxSize().background(DiscoverBackground)
    ) {
        item(span = { GridItemSpan(maxLineSpan) }) {
            Box(Modifier.fillMaxWidth().height(260.dp)) {
                channel?.banner?.let {
                    AsyncImage(model = it, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                }
                Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Black.copy(alpha = 0.2f), DiscoverBackground))))
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.align(Alignment.BottomStart).padding(start = 8.dp, bottom = 16.dp)) {
                    AsyncImage(
                        model = channel?.avatar ?: R.drawable.placeholder,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.size(96.dp).clip(CircleShape).background(DiscoverSurface)
                    )
                    Column(Modifier.padding(start = 20.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(channel?.name ?: fallbackName, color = Color.White, fontSize = 34.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            if (channel?.verified == true) Icon(Icons.Rounded.CheckCircle, "Verified", tint = DiscoverTextSecondary, modifier = Modifier.padding(start = 10.dp).size(24.dp))
                        }
                        channel?.subscribers?.takeIf { it > 0 }?.let {
                            Text("${formatCount(it)} subscribers", color = DiscoverTextSecondary, fontSize = 18.sp, modifier = Modifier.padding(top = 4.dp))
                        }
                    }
                }
            }
        }
        item(span = { GridItemSpan(maxLineSpan) }) {
            Text("Videos", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 6.dp))
        }
        when {
            videos.items.isEmpty() && videos.loading -> item(span = { GridItemSpan(maxLineSpan) }) {
                Box(Modifier.fillMaxWidth().padding(top = 40.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = DiscoverAccent) }
            }
            videos.items.isEmpty() && videos.error -> item(span = { GridItemSpan(maxLineSpan) }) {
                Column(Modifier.fillMaxWidth().padding(top = 40.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(com.ramzmania.tubefy.core.youtube.YouTubeAccess.messageOr("Couldn't load this channel"), color = DiscoverTextSecondary, fontSize = 18.sp)
                    TextButton(onClick = { viewModel.load(url, fallbackName, force = true) }) { Text("Retry", color = DiscoverAccent, fontSize = 18.sp) }
                }
            }
            videos.items.isEmpty() -> item(span = { GridItemSpan(maxLineSpan) }) {
                Text("No videos yet", color = DiscoverTextSecondary, fontSize = 18.sp, modifier = Modifier.padding(start = 6.dp))
            }
            else -> {
                itemsIndexed(videos.items, key = { _, item -> item.id }) { index, item ->
                    TvVideoCard(item = item, onClick = { onOpenVideo(item) }, modifier = if (index == 0) Modifier.focusRequester(firstCard) else Modifier)
                }
                if (videos.loadingMore) item(span = { GridItemSpan(maxLineSpan) }) {
                    Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = DiscoverAccent, strokeWidth = 3.dp, modifier = Modifier.size(32.dp))
                    }
                }
            }
        }
    }
}
