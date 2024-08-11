package com.ramzmania.tubefy.ui.components.screen.album

import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.compose.rememberImagePainter
import coil.request.ImageRequest
import com.ramzmania.tubefy.R
import com.ramzmania.tubefy.core.YoutubeCoreConstant
import com.ramzmania.tubefy.core.extractors.yotubewebextractor.YoutubeScrapType
import com.ramzmania.tubefy.data.Resource
import com.ramzmania.tubefy.data.dto.home.HomePageResponse
import com.ramzmania.tubefy.data.dto.searchformat.StreamUrlData
import com.ramzmania.tubefy.data.dto.searchformat.TubeFyCoreTypeData
import com.ramzmania.tubefy.viewmodel.TubeFyViewModel

@Composable
fun AlbumScreen(viewModel: TubeFyViewModel = hiltViewModel(),navController: NavController) {
    val playListData by viewModel.youTubePlayListData.observeAsState()
    val streamUrlData by viewModel.streamUrlData.observeAsState()
    val searchPlayListName by viewModel.youTubeSearchData.observeAsState()

    var finalItems by remember { mutableStateOf<List<TubeFyCoreTypeData?>>(emptyList()) }
    val navBackStackEntry = navController.currentBackStackEntry
    val context = LocalContext.current

//    val playlistId = navBackStackEntry?.arguments?.getString("playlistId")
    LaunchedEffect(Unit) {
        val playlistId = navBackStackEntry?.arguments?.getString("playlistId")
        if(playlistId!!.startsWith("PLAYLIST-ID-")) {
            viewModel.searchNewPipePage(playlistId.replace("PLAYLIST-ID-",""))
        }else {
            viewModel.loadPlayList(playlistId!!)
        }
    }
    Column(modifier = Modifier.fillMaxSize()) {
        AlbumHeader(
            imageUrl = "https://www.udiscovermusic.com/wp-content/uploads/2015/10/100-Greatest-Album-Covers.jpg", // Replace with your image URL
            title = "Real Steel (Original Motion Picture Score)",
            artist = "Danny Elfman",
            year = "2011"
        )
        AlbumTrackList(tracks =finalItems)
    }
    LaunchedEffect(key1 = playListData) {
        if (playListData is Resource.Success) {
//            val items = (streamUrlData as Resource.Success<StreamUrlData>).data
            // Prepend new data to the existing list
            Log.d("datata", ">>VADAAA"+playListData!!.data!!.playListVideoList?.get(0)?.videoId)
            finalItems=playListData!!.data!!.playListVideoList!!

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

        }
    }

}

@Composable
fun AlbumHeader(imageUrl: String, title: String, artist: String, year: String) {
    Column(modifier = Modifier.padding(16.dp)) {
      /*  AsyncImage(
            painter = rememberImagePainter(data = imageUrl),
            contentDescription = null,
            modifier = Modifier
                .height(200.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
        )*/
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
                .height(200.dp)
                .fillMaxWidth(),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = title,  color = Color.White)
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = artist,color = Color.Gray)
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = year, color = Color.Gray)
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
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { viewModel.getStreamUrl(trackName.videoId) }
    ) {
        Text(text = trackName.videoTitle,  color = Color.Black)
        Divider(modifier = Modifier.padding(vertical = 4.dp), color = Color.Gray)
    }
}

@Preview
@Composable
fun PreviewAlbumScreen() {
//    AlbumScreen()
}
