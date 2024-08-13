package com.ramzmania.tubefy.ui.components.screen.album

import android.content.Intent
import android.net.Uri
import android.util.Log
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.ramzmania.tubefy.R
import com.ramzmania.tubefy.core.YoutubeCoreConstant
import com.ramzmania.tubefy.core.YoutubeCoreConstant.decodeThumpUrl
import com.ramzmania.tubefy.data.Resource
import com.ramzmania.tubefy.data.dto.searchformat.StreamUrlData
import com.ramzmania.tubefy.data.dto.searchformat.TubeFyCoreTypeData
import com.ramzmania.tubefy.ui.components.NavigationItem
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
    var finalItems by remember { mutableStateOf<List<TubeFyCoreTypeData?>>(emptyList()) }
    val navController= LocalNavController.current
    val navBackStackEntry = navController.currentBackStackEntry
    val context = LocalContext.current
    Log.d("sadakku",">>>>>")

//    val playlistId = navBackStackEntry?.arguments?.getString("playlistId")
    LaunchedEffect(Unit) {
        val playlistId = navBackStackEntry?.arguments?.getString("playlistId")
        Log.d("sadakku",">>2222>>>"+playlistId)
        if(playlistId!!.startsWith("PLAYLIST-ID-")) {
            if(playlistId!!.startsWith("PLAYLIST-ID-YT"))
            {
                Log.d("incomming<>","<>music_songs")

                viewModel.searchNewPipePage(playlistId.replace("PLAYLIST-ID-","")+" songs of "+Calendar.getInstance().get(Calendar.YEAR),
                    mutableListOf()
                )
            }else
            {
                viewModel.searchNewPipePage(playlistId.replace("PLAYLIST-ID-",""),
                    mutableListOf("music_songs")
                )
            }

        }else {
            viewModel.loadPlayList(playlistId!!)
        }
    }
    Column(modifier = Modifier
        .fillMaxSize()
        .background(Color.Black)) {
        navBackStackEntry?.arguments?.getString("playlistName")?.let {
            AlbumHeader(
                imageUrl = URLDecoder.decode(navBackStackEntry?.arguments?.getString("playlistImage"), StandardCharsets.UTF_8.toString())
            , // Replace with your image URL
                title = it,

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
            AlbumTrackList(tracks = finalItems)
        }
//        AlbumTrackList(tracks =finalItems)
    }
    LaunchedEffect(key1 = playListData) {
        if (playListData is Resource.Success) {
//            val items = (streamUrlData as Resource.Success<StreamUrlData>).data
            // Prepend new data to the existing list
            Log.d("datata", ">>VADAAA"+playListData!!.data!!.playListVideoList?.get(0)?.videoId)
            finalItems=playListData!!.data!!.playListVideoList!!
            isLoading=false
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
            Log.d("datata","first")
            Log.d("datata", ">>VADAAACAME"+searchPlayListName!!.data!!.youtubeSortedData.youtubeSortedList!!.size)
            finalItems=searchPlayListName!!.data!!.youtubeSortedData.youtubeSortedList!!
            isLoading = false
        }
    }

}

@Composable
fun AlbumHeader(imageUrl: String, title: String) {
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
    Text(text = title,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        fontWeight = FontWeight.Medium,
        color = colorResource(id = R.color.tubefyred),
        textAlign = TextAlign.Left,
        maxLines = 2,
        fontSize = 16.sp)

}
}

@Composable
fun AlbumTrackList(tracks:List<TubeFyCoreTypeData?>) {
    LazyColumn {
        items(tracks!!) { track ->
            TrackItem(trackName = track!!)
        }
    }
}

@Composable
fun TrackItem(trackName: TubeFyCoreTypeData,viewModel: TubeFyViewModel = hiltViewModel()) {
    val newNav=  LocalNavController.current

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
                val encodedVideoId =
                    URLEncoder.encode(trackName.videoId, StandardCharsets.UTF_8.toString())
               val videoTitle= URLEncoder.encode(trackName.videoTitle, StandardCharsets.UTF_8.toString())
//                LocalNavController.current
                newNav!!.navigate(
                    NavigationItem.AudioPlayer.createRoute(
                        encodedVideoId,
                        encodedVideoThumpUrl,videoTitle,videoTitle,videoTitle
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

                Text(
                    text = trackName.videoTitle!!,
                    fontWeight = FontWeight.Thin,
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
//        Text(text = trackName.videoTitle,  color = Color.Black)
//        Divider(modifier = Modifier.padding(vertical = 4.dp), color = Color.Gray)
    }
}

@Preview
@Composable
fun PreviewAlbumScreen() {
//    AlbumScreen()
}
