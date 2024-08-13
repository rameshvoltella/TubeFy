package com.ramzmania.tubefy.ui.components.screen.player

import android.net.Uri
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.session.MediaController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.ramzmania.tubefy.R
import com.ramzmania.tubefy.core.YoutubeCoreConstant
import com.ramzmania.tubefy.data.Resource
import com.ramzmania.tubefy.data.dto.searchformat.StreamUrlData
import com.ramzmania.tubefy.utils.LocalNavController
import com.ramzmania.tubefy.viewmodel.TubeFyViewModel
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

@Composable
fun PlayerBaseView(
    viewModel: TubeFyViewModel = hiltViewModel()
) {
    var isLoading by remember { mutableStateOf(false) }  // Track loading state
    var mediaController by remember { mutableStateOf<MediaController?>(null) }
    var isPlaying by remember { mutableStateOf(false) }
    val streamUrlData by viewModel.streamUrlData.observeAsState()
    val navController = LocalNavController.current

    // Replace this URL with your desired media URL
    var mediaUri =
        ""
    val navBackStackEntry = navController.currentBackStackEntry
//    val albumArt = "https://i.ytimg.com/vi/${
//        YoutubeCoreConstant.extractYoutubeVideoId(
//            URLDecoder.decode(
//                navBackStackEntry?.arguments?.getString("videoId"),
//                StandardCharsets.UTF_8.toString()
//            )
//        )
//    }/maxresdefault.jpg"
        val albumArt = "https://i.ytimg.com/vi/${
        YoutubeCoreConstant.extractYoutubeVideoId(
            URLDecoder.decode(
                navBackStackEntry?.arguments?.getString("videoId"),
                StandardCharsets.UTF_8.toString()
            )
        )
    }/hq720.jpg"
//    https://i.ytimg.com/vi/VTw0wVvEmPw/hq720.jpg

    val playerHeader= URLDecoder.decode(
        navBackStackEntry?.arguments?.getString("playerHeader"),
        StandardCharsets.UTF_8.toString()
    )
    val playerBottomHeader= URLDecoder.decode(
        navBackStackEntry?.arguments?.getString("playerBottomHeader"),
        StandardCharsets.UTF_8.toString()
    )
    val playerBottomSub= URLDecoder.decode(
        navBackStackEntry?.arguments?.getString("playerBottomSub"),
        StandardCharsets.UTF_8.toString()
    )
//    val orginalThump= URLDecoder.decode(
//        navBackStackEntry?.arguments?.getString("encodedVideoThumpUrl"),
//        StandardCharsets.UTF_8.toString()
//    )
//    Log.d("ddddd",orginalThump)

    LaunchedEffect(Unit) {
        val videoId = URLDecoder.decode(
            navBackStackEntry?.arguments?.getString("videoId"),
            StandardCharsets.UTF_8.toString()
        )

        viewModel.getStreamUrl(videoId!!)
    }
    LaunchedEffect(key1 = streamUrlData) {
        if (streamUrlData is Resource.Success) {
            val items = (streamUrlData as Resource.Success<StreamUrlData>).data
            // Prepend new data to the existing list
            Log.d("datata", ">>VADAAA")

            Log.d("datata", ">>" + streamUrlData!!.data!!.streamUrl)
            if (streamUrlData!!.data!!.streamUrl.isNotEmpty()) {
                mediaUri = streamUrlData!!.data!!.streamUrl
                playAudio(mediaController!!, mediaUri, albumArt,playerHeader)

            }
        }
    }
    MediaControllerComposable(
        onPlayingChanged = { playing ->
            isPlaying = playing
        },
        onMediaControllerInitialized = { controller ->
//            Log.d("konana","yoooo"+controller)
            mediaController = controller
        }
    )
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(id = R.color.colorPrimary))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Playing Now",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp, start = 10.dp, end = 10.dp),
                fontWeight = FontWeight.Thin,
                color = Color.White,
                textAlign = TextAlign.Center,
                maxLines = 2,
                fontSize = 16.sp
            )
            Text(
                text = playerHeader,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 2.dp, start = 10.dp, end = 10.dp),
                fontWeight = FontWeight.Medium,
                color = Color.White,
                textAlign = TextAlign.Center,
                maxLines = 2,
                fontSize = 16.sp
            )

            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(YoutubeCoreConstant.decodeThumpUrl(albumArt))
                    .crossfade(true)
                    .placeholder(R.drawable.placeholder)
                    .error(R.drawable.placeholder)
                    .build(),
                contentDescription = "Drawable Image",
                modifier = Modifier
                    .padding(horizontal = 60.dp, vertical = 50.dp)
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .align(Alignment.CenterHorizontally),
                contentScale = ContentScale.Crop
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = playerBottomHeader,
                fontSize = 20.sp,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = playerBottomSub,
                fontSize = 14.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Slider(
                value = 1.19f,
                onValueChange = {},
                valueRange = 0f..5.23f,
                modifier = Modifier.fillMaxWidth()
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "1:19", color = Color.Gray, fontSize = 12.sp)
                Text(text = "5:23", color = Color.Gray, fontSize = 12.sp)
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.CenterHorizontally),
                horizontalArrangement = Arrangement.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_player_previous),
                    contentDescription = "Image 1",
                    modifier = Modifier.size(30.dp) // Adjust the size as needed
                )
                Spacer(modifier = Modifier.width(40.dp))
                // Optional: Adds spacing between images
                if (isLoading) {
                    CircularProgressIndicator(
                        color = colorResource(id = R.color.tubefyred),
                        modifier = Modifier.size(30.dp)
                    )

                } else {

                    Image(
                        painter = painterResource(id = if (isPlaying) R.drawable.ic_player_pause else R.drawable.ic_player_play),
                        contentDescription = "Image 2",
                        modifier = Modifier
                            .size(30.dp)
                            .clickable {

                                    if (mediaController != null && mediaController!!.isConnected) {
                                        if (isPlaying) {
                                            mediaController?.pause()
                                        }else
                                        {
                                            mediaController?.play()
                                        }
                                    }

                            }
                    )

                }
                Spacer(modifier = Modifier.width(40.dp)) // Optional: Adds spacing between images
                Image(
                    painter = painterResource(id = R.drawable.ic_player_next),
                    contentDescription = "Image 3",
                    modifier = Modifier.size(30.dp)
                )
            }
        }
    }
}


@Composable
fun MusicPlayerScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121212)),
        verticalArrangement = Arrangement.SpaceBetween
    ) {

    }
}


@Preview(showBackground = true)
@Composable
fun PlayerPreview() {
    PlayerBaseView()
}


fun playAudio(mediaController: MediaController,mediaUri:String,videoThumpUrl:String,videoTitle:String) {
    if (mediaController?.isConnected!!) {
        Log.d("click", "connected")

//                val drawableUri: Uri =
//                    Uri.parse("android.resource://${BuildConfig.APPLICATION_ID}/${com.ramzmania.tubefy.R.drawable.tubefy_icon}")

        fun mediaItemData() = MediaItem.Builder()
            .setMediaId("id")
            .setUri(
                Uri.parse(mediaUri)
            )
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(videoTitle)
                    .setArtist("Artist :${videoTitle}")
                    .setIsBrowsable(false)
                    .setIsPlayable(true)
                    .setArtworkUri(Uri.parse(videoThumpUrl))
                    .setMediaType(MediaMetadata.MEDIA_TYPE_MUSIC)
                    .build()
            )
            .build()

        mediaController?.setMediaItem(mediaItemData())
        mediaController?.playWhenReady = true
        mediaController?.prepare()

    }
}
