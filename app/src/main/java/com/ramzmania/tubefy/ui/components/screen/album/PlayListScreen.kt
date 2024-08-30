package com.ramzmania.tubefy.ui.components.screen.album

import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.palette.graphics.Palette
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.ramzmania.tubefy.R
import com.ramzmania.tubefy.core.YoutubeCoreConstant
import com.ramzmania.tubefy.core.YoutubeCoreConstant.decodeThumpUrl
import com.ramzmania.tubefy.data.Resource
import com.ramzmania.tubefy.data.dto.base.searchformat.StreamUrlData
import com.ramzmania.tubefy.data.dto.base.searchformat.TubeFyCoreTypeData
import com.ramzmania.tubefy.ui.components.NavigationItem
import com.ramzmania.tubefy.ui.components.screen.library.PlayListDialogViewer
import com.ramzmania.tubefy.ui.components.screen.player.LoadBitmapAndExtractPalette
import com.ramzmania.tubefy.utils.LocalNavController
import com.ramzmania.tubefy.viewmodel.TubeFyViewModel
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.Calendar

@Composable
fun AlbumScreen(viewModel: TubeFyViewModel = hiltViewModel()) {
    val playListData by viewModel.youTubePlayListData.observeAsState()
    val streamUrlData by viewModel.streamUrlData.observeAsState()
    val searchPlayListName by viewModel.youTubeSearchData.observeAsState()
    var isLoading by remember { mutableStateOf(true) }  // Track loading state
    var videoAudioItems by remember { mutableStateOf<List<TubeFyCoreTypeData?>>(emptyList()) }
    val navController = LocalNavController.current
    val navBackStackEntry = navController.currentBackStackEntry
    val context = LocalContext.current
    var palette by remember { mutableStateOf<Palette?>(null) }

    Log.d("sadakku", ">>>>>")
    var isDefaultDataLoaded by rememberSaveable { mutableStateOf(false) }

//    val playlistId = navBackStackEntry?.arguments?.getString("playlistId")
    LaunchedEffect(Unit) {
        val playlistId = navBackStackEntry?.arguments?.getString("playlistId")
        Log.d("sadakku", ">>2222>>>" + playlistId)
        if (!isDefaultDataLoaded) {
            playlistId?.let {
                if (playlistId.startsWith("PLAYLIST-ID-")) {
                    if (playlistId.startsWith("PLAYLIST-ID-YT")) {
                        Log.d("incomming<>", "<>music_songs")

                        viewModel.searchNewPipePage(
                            playlistId.replace(
                                "PLAYLIST-ID-",
                                ""
                            ) + " songs of " + Calendar.getInstance()
                                .get(Calendar.YEAR),
                            mutableListOf()
                        )
                    } else {
                        viewModel.searchNewPipePage(
                            playlistId.replace("PLAYLIST-ID-", ""),
                            mutableListOf("music_songs")
                        )
                    }

                } else {
                    viewModel.loadPlayList(playlistId!!)
                }
            }
        }
    }
    LoadBitmapAndExtractPalette(
        URLDecoder.decode(
            navBackStackEntry?.arguments?.getString("playlistImage"),
            StandardCharsets.UTF_8.toString()
        )
    ) { extractedPalette ->
        palette = extractedPalette
    }
    val dominantColor = palette?.dominantSwatch?.rgb?.let { Color(it) } ?: Color.Black
    val gradientBrush = Brush.linearGradient(
        colors = listOf(Color.Transparent, Color.Black),
        start = Offset(0f, 0f),
        end = Offset(0f, Float.POSITIVE_INFINITY),
        tileMode = TileMode.Clamp
    )
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(dominantColor)
    ) {
        Row(modifier = Modifier.background(gradientBrush)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                navBackStackEntry?.arguments?.getString("playlistName")?.let {
                    AlbumHeader(
                        imageUrl = URLDecoder.decode(
                            navBackStackEntry?.arguments?.getString("playlistImage"),
                            StandardCharsets.UTF_8.toString()
                        ), // Replace with your image URL
                        title = it, finalItems = videoAudioItems

                    )
                }
                if (isLoading) {
                    // Show progress indicator while loading
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = colorResource(id = R.color.tubefyred))
                    }
                } else {
                    // Show track list when data is loaded
                    AlbumTrackList(tracks = videoAudioItems)
                }
            }
        }
//        AlbumTrackList(tracks =finalItems)
    }
    LaunchedEffect(key1 = playListData) {
        if (playListData is Resource.Success) {
//            val items = (streamUrlData as Resource.Success<StreamUrlData>).data
            // Prepend new data to the existing list
            Log.d("datata", ">>VADAAA" + playListData!!.data!!.playListVideoList?.get(0)?.videoId)
            videoAudioItems = playListData!!.data!!.playListVideoList!!
            isLoading = false
            isDefaultDataLoaded = true
            viewModel.setActiveSongsList(videoAudioItems)

//            if(playListData!!.data!!.playListVideoList?.get(0)?.videoId!=null)
//            {
//                viewModel.getStreamUrl(playListData!!.data!!.playListVideoList?.get(0)?.videoId!!)
//            }
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

    LaunchedEffect(key1 = searchPlayListName) {
        if (searchPlayListName is Resource.Success) {
//            val items = (streamUrlData as Resource.Success<StreamUrlData>).data
            // Prepend new data to the existing list
            Log.d("datata", "first")
            Log.d(
                "datata",
                ">>VADAAACAME" + searchPlayListName!!.data!!.youtubeSortedData.youtubeSortedList!!.size
            )
            videoAudioItems = searchPlayListName!!.data!!.youtubeSortedData.youtubeSortedList!!
            isLoading = false
            viewModel.setActiveSongsList(videoAudioItems)
        }
    }

}

@Composable
fun AlbumHeader(
    imageUrl: String,
    title: String,
    viewModel: TubeFyViewModel = hiltViewModel(),
    finalItems: List<TubeFyCoreTypeData?>
) {
    val newNav = LocalNavController.current

    Column(modifier = Modifier.fillMaxWidth()) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(YoutubeCoreConstant.decodeThumpUrl(imageUrl))
                .crossfade(true)
                .placeholder(R.drawable.placeholder)
                .error(R.drawable.placeholder)
                .build(),
            contentDescription = "Drawable Image",
            modifier = Modifier
                .padding(horizontal = 5.dp, vertical = 5.dp)
                .width(200.dp)
                .height(200.dp)
                .align(Alignment.CenterHorizontally)
                .fillMaxWidth(),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = title,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            fontWeight = FontWeight.Medium,
            color = colorResource(id = R.color.tubefyred),
            textAlign = TextAlign.Left,
            maxLines = 2,
            fontSize = 16.sp
        )

        Button(
            onClick = {
                if (finalItems.isNotEmpty()) {
                    val encodedVideoThumpUrl = URLEncoder.encode(
                        decodeThumpUrl(finalItems.get(0)!!.videoImage),
                        StandardCharsets.UTF_8.toString()
                    )
                    val encodedVideoId =
                        URLEncoder.encode(
                            finalItems.get(0)!!.videoId,
                            StandardCharsets.UTF_8.toString()
                        )
                    val videoTitle =
                        URLEncoder.encode(
                            finalItems.get(0)!!.videoTitle,
                            StandardCharsets.UTF_8.toString()
                        )
//                LocalNavController.current
                    newNav!!.navigate(
                        NavigationItem.AudioPlayer.createRoute(
                            encodedVideoId,
                            encodedVideoThumpUrl, videoTitle, videoTitle, videoTitle, true
                        )
                    ) {
                        newNav.graph.route?.let { route ->
                            popUpTo(route) {
                                saveState = true
                            }
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = colorResource(id = R.color.tubefyred), // Background color of the button
                contentColor = Color.White   // Text color of the button
            )
        ) { Text(text = "Play All") }


    }
}

@Composable
fun AlbumTrackList(
    tracks: List<TubeFyCoreTypeData?>,
    viewModel: TubeFyViewModel = hiltViewModel()
) {
    var showPlayListDialog = viewModel.showPlayListDialog.collectAsState()
    var currentSongItemSelected = viewModel.selectedTrack.collectAsState()

    if (showPlayListDialog.value && currentSongItemSelected.value != null) {
        PlayListDialogViewer(
            viewModel = viewModel,
            onDismiss = { viewModel.showPlayListDialog(false) },
            currentSongItemSelected.value!!
            // Reset the state when the dialog is dismissed
        )
    }

    LazyColumn {
        items(tracks!!) { track ->
            TrackItem(trackName = track!!)
        }
        item {
            Spacer(modifier = Modifier.height(140.dp))
        }
    }
}

@Composable
fun TrackItem(trackName: TubeFyCoreTypeData, viewModel: TubeFyViewModel = hiltViewModel()) {
    val newNav = LocalNavController.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 8.dp)
            .clickable {
//                val videoId = "someVideoId"
//                val videoName = "Mere Khwabon Mein | Lyrical Song"

// Encode videoName if it contains special characters
                val encodedVideoThumpUrl = URLEncoder.encode(
                    decodeThumpUrl(trackName.videoImage),
                    StandardCharsets.UTF_8.toString()
                )

                val videoTitle =
                    URLEncoder.encode(trackName.videoTitle, StandardCharsets.UTF_8.toString())

                Log.d("Sound track", "<><><" + trackName.videoId)
                if (trackName.videoId.length > 10) {
                    val encodedVideoId =
                        URLEncoder.encode(trackName.videoId, StandardCharsets.UTF_8.toString())
                    newNav!!.navigate(
                        NavigationItem.AudioPlayer.createRoute(
                            encodedVideoId,
                            encodedVideoThumpUrl, videoTitle, videoTitle, videoTitle
                        )
                    ) {
                        newNav.graph.route?.let { route ->
                            popUpTo(route) {
                                saveState = true
                            }
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                } else {
                    newNav!!.navigate(
                        NavigationItem.PlayList.createRoute(
                            URLEncoder.encode(
                                YoutubeCoreConstant.extractPlaylistId(trackName.plaListUrl!!),
                                StandardCharsets.UTF_8.toString()
                            ),
                            videoTitle,
                            encodedVideoThumpUrl

                        )
                    ) {
                        newNav.graph.route?.let { route ->
                            popUpTo(route) {
                                saveState = true
                            }
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }


//                LocalNavController.current

            }
    ) {
        Card(
            modifier = Modifier
//                .padding(8.dp)
                .fillMaxWidth(),
            elevation = CardDefaults.cardElevation(4.dp), // Use CardDefaults.cardElevation for elevation
            colors = CardDefaults.cardColors(containerColor = Color.DarkGray), // Set background color
            shape = RoundedCornerShape(8.dp) // Adjust the corner radius for a rounded effect
        ) {
            Row(
                modifier = Modifier
//                .padding(8.dp)
                    .fillMaxWidth()
            ) {

                trackName.videoImage?.let { thumbnailUrl ->
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(decodeThumpUrl(thumbnailUrl))
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
                // Spacer to push the text and right image apart
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = trackName.videoTitle!!,
                    fontWeight = FontWeight.Thin,
                    color = Color.White,
                    modifier = Modifier
                        .align(Alignment.CenterVertically)
                        .fillMaxWidth()
                        .padding(horizontal = 5.dp)
                        .weight(2f),
                    textAlign = TextAlign.Left,
                    maxLines = 1,
                    fontSize = 16.sp
                )
                if (trackName.videoId.isEmpty()) {
                    // Spacer to push the text and right image apart
                    Spacer(modifier = Modifier.width(2.dp))
                    Image(
                        painter = painterResource(id = R.drawable.ic_playlist),
                        contentDescription = "Right Image",
                        modifier = Modifier
                            .height(30.dp)
                            .align(Alignment.CenterVertically)
                            .width(30.dp)
                            .padding(horizontal = 5.dp, vertical = 5.dp),
                        contentScale = ContentScale.Crop
                    )
                }
                Spacer(modifier = Modifier.width(2.dp))
                Image(
                    painter = painterResource(id = R.drawable.ic_more),
                    contentDescription = "Right Image",
                    modifier = Modifier
                        .height(30.dp)
                        .align(Alignment.CenterVertically)
                        .width(30.dp)
                        .padding(horizontal = 5.dp, vertical = 5.dp)
                        .clickable {
                            viewModel.showPlayListDialog(true, trackName)
                        },
                    contentScale = ContentScale.Crop
                )
            }
        }
//        Text(text = trackName.videoTitle,  color = Color.Black)
//        Divider(modifier = Modifier.padding(vertical = 4.dp), color = Color.Gray)
    }
}

@Preview
@Composable
fun PreviewAlbumScreen() {
    TrackItem(
        TubeFyCoreTypeData(
            "",
            "sbksdnbcjhsabdjcaskjncdkadsnkcnsdkndckdsnvkcsnjndkfjndsknvksdnkvnskdc",
            "ascs",
            "dcs"
        )
    )
}
