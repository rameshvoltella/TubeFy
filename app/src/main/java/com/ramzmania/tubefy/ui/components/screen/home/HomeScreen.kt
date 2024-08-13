package com.ramzmania.tubefy.ui.components.screen.home

import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.ramzmania.tubefy.R
import com.ramzmania.tubefy.core.YoutubeCoreConstant
import com.ramzmania.tubefy.data.Resource
import com.ramzmania.tubefy.data.dto.base.BaseContentData
import com.ramzmania.tubefy.data.dto.home.CellType
import com.ramzmania.tubefy.data.dto.home.HomePageResponse
import com.ramzmania.tubefy.data.dto.searchformat.StreamUrlData
import com.ramzmania.tubefy.ui.components.NavigationItem
import com.ramzmania.tubefy.utils.LocalNavController
import com.ramzmania.tubefy.viewmodel.TubeFyViewModel
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Composable
fun HomePageContentList(
    homePageResponses: List<HomePageResponse?>,
    viewModel: TubeFyViewModel = hiltViewModel(), navController: NavController?
) {
    val streamUrlData by viewModel.streamUrlData.observeAsState()
    val playListData by viewModel.youTubePlayListData.observeAsState()
//    val navController = rememberNavController()
    val context = LocalContext.current

    LazyColumn {
        items(homePageResponses) { response ->

            response?.let {
                when (it.cellType) {
                    CellType.LIST -> GridContentList(
                        contentData = it.contentData,
                        navController = navController
                    )

                    CellType.HORIZONTAL_LIST -> HorizontalContentList(
                        contentData = it.contentData,
                        navController = navController
                    )

                    CellType.THREE_TYPE_CELL -> VerticalContentList(
                        contentData = it.contentData,
                        navController = navController
                    )

                    CellType.SINGLE_CELL -> SingleContentCell(contentData = it.contentData)
                    CellType.PLAYLIST_ONLY -> HorizontalContentList(
                        contentData = it.contentData,
                        navController = navController
                    )
                }
            }
        }
    }

    LaunchedEffect(key1 = playListData) {
        if (playListData is Resource.Success) {
//            val items = (streamUrlData as Resource.Success<StreamUrlData>).data
            // Prepend new data to the existing list
            Log.d("datata", ">>VADAAA" + playListData!!.data!!.playListVideoList?.get(0)?.videoId)
            if (playListData!!.data!!.playListVideoList?.get(0)?.videoId != null) {
                viewModel.getStreamUrl(playListData!!.data!!.playListVideoList?.get(0)?.videoId!!)
            }
            // Log.d("datata", "palyislisis>>" + (playListData!!.data!!.playListVideoList?.get(0)?.videoId ?: ))
//            if (streamUrlData!!.data!!.streamUrl.isNotEmpty()) {
//                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(streamUrlData!!.data!!.streamUrl))
//                context.startActivity(intent)
//            }
        }
    }
    LaunchedEffect(key1 = streamUrlData) {
        if (streamUrlData is Resource.Success) {
            val items = (streamUrlData as Resource.Success<StreamUrlData>).data
            // Prepend new data to the existing list
            Log.d("datata", ">>VADAAA")

            Log.d("datata", ">>" + streamUrlData!!.data!!.streamUrl)
            if (streamUrlData!!.data!!.streamUrl.isNotEmpty()) {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(streamUrlData!!.data!!.streamUrl))
                context.startActivity(intent)
            }
        }
    }


}


@Composable
fun VerticalContentList(
    navController: NavController?,
    contentData: List<BaseContentData>?,
    viewModel: TubeFyViewModel = hiltViewModel()
) {
    contentData?.let {
        Column {
            it.forEach { data ->
                ContentItemList(data = data) { selectedItem ->
                    // Handle item click here
                    Log.d("ItemClicked", "Clicked item: ${selectedItem.videoId}")
//                    viewModel.getStreamUrl(selectedItem.videoId!!)
                    val encodedVideoUrl = URLEncoder.encode(
                        YoutubeCoreConstant.decodeThumpUrl(
                            selectedItem.thumbnail!!
                        ), StandardCharsets.UTF_8.toString()
                    )
                    val encodedVideoId =
                        URLEncoder.encode(selectedItem.videoId, StandardCharsets.UTF_8.toString())
                    val encodedVideoTitle =
                        URLEncoder.encode(selectedItem.title, StandardCharsets.UTF_8.toString())

                    navController!!.navigate(
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
            }
        }
    }
}

@Composable
fun GridContentList(
    navController: NavController?,
    contentData: List<BaseContentData>?,
    viewModel: TubeFyViewModel = hiltViewModel()
) {
    val gridContent = contentData?.size?.div(2)
    val newheight = 70 * gridContent!!
    contentData?.let {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2), modifier = Modifier
                .fillMaxSize()
                .height(newheight.dp)
                .padding(vertical = 10.dp)
        ) {
            items(it) { data ->
                ContentItemList(data = data) { selectedItem ->
                    // Handle item click here
                    Log.d("ItemClicked", "Clicked item: ${selectedItem.videoId}")

                    val encodedVideoUrl = URLEncoder.encode(
                        YoutubeCoreConstant.decodeThumpUrl(
                            selectedItem.thumbnail!!
                        ), StandardCharsets.UTF_8.toString()
                    )
                    val encodedVideoId = URLEncoder.encode(
                        selectedItem.videoId,
                        StandardCharsets.UTF_8.toString()
                    )
                    val encodedVideoTitle =
                        URLEncoder.encode(selectedItem.title, StandardCharsets.UTF_8.toString())

                    navController!!.navigate(
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
            }
        }
    }
}

@Composable
fun HorizontalContentList(
    navController: NavController?,
    contentData: List<BaseContentData>?,
    viewModel: TubeFyViewModel = hiltViewModel()
) {
    contentData?.let {
        Column (Modifier.background(Color.Black)){

            Text(  text = "TOP MIX",
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(10.dp,30.dp,10.dp),
                textAlign = TextAlign.Left,
                maxLines = 2,
                fontSize = 20.sp)

            LazyRow {
                items(it) { data ->
                    ContentItem(data = data) { selectedItem ->
                        // Handle item click here
                        Log.d(
                            "ItemClicked",
                            "Clicked item horizontalFIRST: ${selectedItem.playlistId}"
                        )
                        if (selectedItem.videoId?.length!! > 11 || selectedItem.playlistId!!.startsWith(
                                "PLAYLIST-ID-"
                            )
                        ) {
                            Log.d(
                                "ItemClicked",
                                "Clicked item playlistId: ${selectedItem.playlistId}"+"<>"+" ${selectedItem.title}"+"<>"+" ${selectedItem.thumbnail}"
                            )

//                        viewModel.loadPlayList(selectedItem.playlistId!!)
//                        navController!!.navigate(NavigationItem.PlayList.route) {
//                          /*  navController.graph.startDestinationRoute?.let { route ->
//                                popUpTo(route) {
//                                    saveState = true
//                                }
//                            }
//                            launchSingleTop = true
//                            restoreState = true*/
//                            // Avoid recreating the composable
//                            launchSingleTop = true
//                            // Restore state
//                            restoreState = true
//                        }
                            viewModel.setHomeScreenReload(false)
                            navController!!.navigate(
                                NavigationItem.PlayList.createRoute(
                                    selectedItem.playlistId!!,
                                    selectedItem.title!!,URLEncoder.encode(
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
    contentData: List<BaseContentData>?,
    viewModel: TubeFyViewModel = hiltViewModel()
) {
    contentData?.firstOrNull()?.let { data ->
        ContentItem(data = data) { selectedItem ->
            // Handle item click here
            Log.d("ItemClicked", "Clicked item: ${selectedItem.videoId}")
            viewModel.getStreamUrl(selectedItem.videoId!!)
        }
    }
}

@Composable
fun ContentItem(data: BaseContentData, onClick: (BaseContentData) -> Unit) {
    Column(
        modifier = Modifier
            .padding(2.dp)
            .wrapContentWidth()
            .clickable { onClick(data) }
    ) {
//        Log.d("incoming", "" + data.thumbnail)

        data.thumbnail?.let { thumbnailUrl ->
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
            text = data.title!!,
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

@Composable
fun GridItem(item: String) {
    Text(text = item)
}


@Composable
fun ContentItemList(data: BaseContentData, onClick: (BaseContentData) -> Unit) {
    Card(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth()
            .clickable { onClick(data) },
        elevation = CardDefaults.cardElevation(4.dp), // Use CardDefaults.cardElevation for elevation
        colors = CardDefaults.cardColors(containerColor = Color.DarkGray), // Set background color
        shape = RoundedCornerShape(8.dp) // Adjust the corner radius for a rounded effect
    ) {
        Row(
            modifier = Modifier
//                .padding(8.dp)
                .fillMaxWidth()
        ) {
            Log.d("incoming", "" + data.title)

            data.thumbnail?.let { thumbnailUrl ->
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(YoutubeCoreConstant.decodeThumpUrl(thumbnailUrl))
                        .crossfade(true)
                        .placeholder(R.drawable.placeholder)
                        .error(R.drawable.placeholder)
                        .build(),
                    contentDescription = "Drawable Image",
                    modifier = Modifier
                        .height(50.dp)
                        .width(50.dp),
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
                    .height(50.dp)
                    .width(50.dp),
                contentScale = ContentScale.Crop
            )

            Text(
                text = data.title!!,
                fontWeight = FontWeight.Medium,
                color = Color.White,
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .fillMaxWidth()
                    .padding(horizontal = 5.dp),
                textAlign = TextAlign.Left,
                maxLines = 1,
                fontSize = 16.sp
            )
        }
    }
}


@Composable
fun Content2Item(data: BaseContentData) {
    Column(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth()
    ) {
        Log.d("incomming", "" + data.thumbnail)
        data.thumbnail?.let {
//            Image(
//                painter = rememberImagePainter(data = it),
//                contentDescription = data.title,
//                modifier = Modifier
//                    .height(150.dp)
//                    .fillMaxWidth()
//            )
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(YoutubeCoreConstant.decodeThumpUrl(it))
                    .crossfade(true)
                    .build(),
                contentDescription = "Drawable Image",
                modifier = Modifier
                    .padding(20.dp)
                    .height(200.dp)
                    .width(200.dp),
                contentScale = ContentScale.Crop
            )
        }
        data.title?.let {
            Text(
                text = it,
                modifier = Modifier.padding(top = 8.dp),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = colorResource(id = R.color.purple_700),
//                style = MaterialTheme.typography.h6
            )
        }
    }
}

//@Preview(showBackground = true)
@Composable
fun HomePageContentListPreview() {
    val videoSortedList = mutableListOf<HomePageResponse>()

    val mockContentData = listOf(
        BaseContentData(
            playlistId = "",
            videoId = "",
            title = "Song Title 1",
            thumbnail = "https://via.placeholder.com/150"
        ),
        BaseContentData(
            playlistId = "",
            videoId = "",
            title = "Song Title 1",
            thumbnail = "https://via.placeholder.com/150"
        ),
        BaseContentData(
            playlistId = "",
            videoId = "",
            title = "Song Title 1",
            thumbnail = "https://via.placeholder.com/150"
        )
    )
    videoSortedList.add(HomePageResponse(CellType.HORIZONTAL_LIST, mockContentData))
    videoSortedList.add(HomePageResponse(CellType.HORIZONTAL_LIST, mockContentData))

//    HomePageContentList(homePageResponses = videoSortedList)
}

//@Preview
@Composable
fun LisrtContent() {
    ContentItemList(
        BaseContentData(
            "ekjsndflknqwek;fj;kqwasdasjek;fjq;kwejfk;qwej;kfjkq;wjfkjakwsjfklasjkfnjaks",
            "esadfasdfasasvsavhasljvhasdhvjhadvasdhk",
            "e",
            "unda",
            false
        ), onClick = {})
}

@Preview(showBackground = true)
@Composable
fun HorizontalContentListPreview() {
    val navController = rememberNavController()
    val mockViewModel = MockTubeFyViewModel()
    val mockData = listOf(
        BaseContentData(
            videoId = "video1",
            playlistId = "playlist1",
            title = "Title 1",
            thumbnail = "https://via.placeholder.com/150"
        ),
        BaseContentData(
            videoId = "video2",
            playlistId = "playlist2",
            title = "Title 2",
            thumbnail = "https://via.placeholder.com/150"
        ),
        // Add more mock items as needed
    )

    HorizontalContentList(
        navController = navController,
        contentData = mockData,
//        viewModel = mockViewModel
    )
}

class MockTubeFyViewModel : ViewModel() {
    fun getStreamUrl(videoId: String?) {
        // Mock implementation
    }

    fun setHomeScreenReload(reload: Boolean) {
        // Mock implementation
    }
}