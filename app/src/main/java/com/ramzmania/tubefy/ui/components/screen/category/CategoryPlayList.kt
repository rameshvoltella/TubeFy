package com.ramzmania.tubefy.ui.components.screen.category

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.ramzmania.tubefy.core.extractors.yotubewebextractor.YoutubeScrapType
import com.ramzmania.tubefy.data.Resource
import com.ramzmania.tubefy.data.dto.base.playlist.PlayListCategory
import com.ramzmania.tubefy.viewmodel.TubeFyViewModel
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.TextButton
import androidx.compose.ui.text.style.TextOverflow
import com.ramzmania.tubefy.ui.components.screen.home.ArtworkCard
import com.ramzmania.tubefy.ui.components.screen.home.SectionHeader
import com.ramzmania.tubefy.ui.theme.DiscoverAccent
import com.ramzmania.tubefy.ui.theme.DiscoverBackground

import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.ramzmania.tubefy.R
import com.ramzmania.tubefy.core.YoutubeCoreConstant
import com.ramzmania.tubefy.data.dto.base.BaseContentData
import com.ramzmania.tubefy.data.dto.youtubemusic.category.MusicCategoryPlayList
import com.ramzmania.tubefy.data.dto.youtubemusic.category.MusicCategoryPlayListBase
import com.ramzmania.tubefy.ui.components.NavigationItem
import com.ramzmania.tubefy.utils.LocalNavController
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Composable
fun CategoryPlaylistView(viewModel: TubeFyViewModel = hiltViewModel()) {
    val navController = LocalNavController.current
    val youTubeCategoryPlayListData by viewModel.youTubeCategoryPlayList.observeAsState()
    var isDefaultDataLoaded by rememberSaveable { mutableStateOf(false) }
    var categoryName by rememberSaveable { mutableStateOf("Category") }
    var musicCategoryPlayListBaseList by remember { mutableStateOf<List<MusicCategoryPlayListBase?>>(emptyList()) }
    val navBackStackEntry = navController.currentBackStackEntry

    LaunchedEffect(Unit) {
        if(!isDefaultDataLoaded)
        {
            val browserId = navBackStackEntry?.arguments?.getString("browserId")!!
            val playListId = navBackStackEntry?.arguments?.getString("playerListId")!!
             categoryName = navBackStackEntry?.arguments?.getString("categoryName")!!

            viewModel.callCategoryPlayList(browserId,playListId)
        }

    }

    LaunchedEffect(key1 = youTubeCategoryPlayListData) {
        if (youTubeCategoryPlayListData is Resource.Success) {
            musicCategoryPlayListBaseList = youTubeCategoryPlayListData?.data!!
            isDefaultDataLoaded=true
        }
    }
    val title = URLDecoder.decode(categoryName, StandardCharsets.UTF_8.toString())
    val sections = musicCategoryPlayListBaseList.filterNotNull().filter { it.musicCategoryPlayList.isNotEmpty() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DiscoverBackground)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 8.dp, end = 20.dp, top = 8.dp, bottom = 4.dp)
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Text(
                text = title,
                color = Color.White,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(start = 4.dp)
            )
        }

        when {
            sections.isNotEmpty() -> LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(sections) { section ->
                    SectionHeader(section.plaListBaseName)
                    HorizontalPlayList(section.musicCategoryPlayList)
                }
                item { Spacer(modifier = Modifier.height(24.dp)) }
            }

            youTubeCategoryPlayListData is Resource.DataError -> Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 48.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Couldn't load $title", color = Color.White, fontSize = 16.sp)
                TextButton(onClick = {
                    viewModel.callCategoryPlayList(
                        navBackStackEntry?.arguments?.getString("browserId").orEmpty(),
                        navBackStackEntry?.arguments?.getString("playerListId").orEmpty()
                    )
                }) {
                    Text("Retry", color = DiscoverAccent, fontSize = 16.sp)
                }
            }

            else -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = DiscoverAccent)
            }
        }
    }
}

@Composable
fun HorizontalPlayList(playLists: List<MusicCategoryPlayList>) {
    val navController = LocalNavController.current

    LazyRow(
        contentPadding = PaddingValues(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        items(playLists) { item ->
            ArtworkCard(
                imageUrl = item.playListThump,
                title = item.playListName,
                onClick = { openCategoryPlaylistItem(navController, item) }
            )
        }
    }
}

/** Opens a category playlist entry: single songs go to the player, playlists to the playlist screen. */
fun openCategoryPlaylistItem(navController: NavController, selectedItem: MusicCategoryPlayList) {
    if (selectedItem.videoId.length == 11) {
        val encodedVideoThumpUrl = URLEncoder.encode(
            YoutubeCoreConstant.decodeThumpUrl(selectedItem.playListThump),
            StandardCharsets.UTF_8.toString()
        )
        val encodedVideoId = URLEncoder.encode(selectedItem.videoId, StandardCharsets.UTF_8.toString())
        val videoTitle = URLEncoder.encode(selectedItem.playListName, StandardCharsets.UTF_8.toString())
        navController.navigate(
            NavigationItem.AudioPlayer.createRoute(
                encodedVideoId,
                encodedVideoThumpUrl, videoTitle, videoTitle, videoTitle
            )
        ) {
            navController.graph.route?.let { route ->
                popUpTo(route) {
                    saveState = true
                }
            }
            launchSingleTop = true
            restoreState = true
        }
    } else {
        navController.navigate(
            NavigationItem.PlayList.createRoute(
                selectedItem.playListId,
                selectedItem.playListName, URLEncoder.encode(
                    YoutubeCoreConstant.decodeThumpUrl(selectedItem.playListThump.takeIf { it.isNotEmpty() }
                        ?: "nourl"),
                    StandardCharsets.UTF_8.toString()
                )
            )
        ) {
            navController.graph.route?.let { route ->
                popUpTo(route) {
                    saveState = true
                }
            }
            launchSingleTop = true
            restoreState = true
        }
    }
}
