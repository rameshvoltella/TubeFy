package com.ramzmania.tubefy.ui.components.screen.category

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
import com.ramzmania.tubefy.core.extractors.yotubewebextractor.YoutubeScrapType
import com.ramzmania.tubefy.data.Resource
import com.ramzmania.tubefy.data.dto.base.playlist.PlayListCategory
import com.ramzmania.tubefy.viewmodel.TubeFyViewModel
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow

import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
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
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Composable
fun CategoryPlaylistView(viewModel: TubeFyViewModel = hiltViewModel()) {
    val youTubeCategoryPlayListData by viewModel.youTubeCategoryPlayList.observeAsState()
    var isDefaultDataLoaded by rememberSaveable { mutableStateOf(false) }
    var musicCategoryPlayListBaseList by remember { mutableStateOf<List<MusicCategoryPlayListBase?>>(emptyList()) }

    LaunchedEffect(Unit) {
        if(!isDefaultDataLoaded)
        {
            viewModel.callCategoryPlayList("FEmusic_moods_and_genres_category","ggMPOg1uX1JOQWZFeDByc2Jm")
        }

    }

    LaunchedEffect(key1 = youTubeCategoryPlayListData) {
        if (youTubeCategoryPlayListData is Resource.Success) {
            musicCategoryPlayListBaseList = youTubeCategoryPlayListData?.data!!
            isDefaultDataLoaded=true
        }
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(16.dp)
    ) {
        if (musicCategoryPlayListBaseList.isNotEmpty()) {

            LazyColumn {
                items(musicCategoryPlayListBaseList.size) { rowIndex ->
                    val rowData = musicCategoryPlayListBaseList[rowIndex]

                    rowData?.let {
                        Text(
                            text = it.plaListBaseName,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        HorizontalPlayList(it.musicCategoryPlayList)
                    }


                }
            }

        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
    }
}
@Composable
fun HorizontalPlayList(playLists: List<MusicCategoryPlayList>) {
    val navController = LocalNavController.current

    LazyRow(
        contentPadding = PaddingValues(horizontal = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(playLists.size) { rowIndex ->
            val rowData = playLists[rowIndex]

            ContentPlayListItem(rowData) { selectedItem ->
                navController!!.navigate(
                    NavigationItem.PlayList.createRoute(
                        selectedItem.playListId!!,
                        selectedItem.playListName!!, URLEncoder.encode(
                            YoutubeCoreConstant.decodeThumpUrl(selectedItem.playListThump?.takeIf { it.isNotEmpty() } ?: "nourl"),
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
        }
    }
}
@Composable
fun ContentPlayListItem(data: MusicCategoryPlayList, onClick: (MusicCategoryPlayList) -> Unit) {
    Column(
        modifier = Modifier
            .padding(2.dp)
            .wrapContentWidth()
            .clickable { onClick(data) }
    ) {
//        Log.d("incoming", "" + data.thumbnail)

        data.playListThump?.let { thumbnailUrl ->
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(YoutubeCoreConstant.decodeThumpUrl(thumbnailUrl))
                    .crossfade(true)
                    .placeholder(R.drawable.placeholder)
                    .error(R.drawable.placeholder)
                    .build(),
                contentDescription = "Drawable Image",
                modifier = Modifier
                    .padding(10.dp)
                    .height(200.dp)
                    .width(200.dp),
                contentScale = ContentScale.Crop
            )
        } ?: AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(R.drawable.images)
                .crossfade(true)
                .placeholder(R.drawable.placeholder)
                .error(R.drawable.placeholder)
                .build(),
            contentDescription = "Placeholder Image",
            modifier = Modifier
                .padding(20.dp)
                .height(200.dp)
                .width(200.dp),
            contentScale = ContentScale.Crop
        )
        Text(
            text = data.playListName!!,
            fontWeight = FontWeight.Thin,
            color = Color.White,
            modifier = Modifier
                .padding(horizontal = 10.dp)
                .width(200.dp),
            textAlign = TextAlign.Left,
            maxLines = 2,
            fontSize = 16.sp
        )

    }
}
