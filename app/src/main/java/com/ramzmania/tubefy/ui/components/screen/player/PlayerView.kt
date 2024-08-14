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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
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
import androidx.compose.ui.draw.clip
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
import com.ramzmania.tubefy.utils.LocalNavController
import com.ramzmania.tubefy.viewmodel.TubeFyViewModel
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

@Composable
fun PlayerBaseView(
    viewModel: TubeFyViewModel = hiltViewModel()
) {
    var isLoading by remember { mutableStateOf(true) }  // Track loading state
    var mediaController by remember { mutableStateOf<MediaController?>(null) }
    var isPlaying by remember { mutableStateOf(false) }
    var progress by remember { mutableStateOf(0f) }  // Track progress
    val streamUrlData by viewModel.streamUrlData.observeAsState()
    val youTubePlayListBulkData by viewModel.youTubePlayListBulkData.observeAsState()
    val navController = LocalNavController.current
    val context = LocalContext.current
    var currentTime by remember { mutableStateOf("00:00") }
    var totalTime by remember { mutableStateOf("00:00") }
    var playerHeader by remember { mutableStateOf("") }
    var playerBottomHeader by remember { mutableStateOf("") }
    var playerBottomSub by remember { mutableStateOf("") }
    var albumArt by remember { mutableStateOf("") }
    var videoId by remember { mutableStateOf("") }
    var isBulk by remember { mutableStateOf("false") }  // Track loading state


    var mediaUri = ""
    val navBackStackEntry = navController.currentBackStackEntry


    LaunchedEffect(Unit) {
        isBulk = navBackStackEntry?.arguments?.getString("isBulk")!!
        Log.d("isBulk", "isBulk>>>>" + isBulk)
        videoId = URLDecoder.decode(
            navBackStackEntry?.arguments?.getString("videoId"),
            StandardCharsets.UTF_8.toString()
        )
        albumArt = "https://i.ytimg.com/vi/${
            YoutubeCoreConstant.extractYoutubeVideoId(
                URLDecoder.decode(
                    navBackStackEntry?.arguments?.getString("videoId"),
                    StandardCharsets.UTF_8.toString()
                )
            )
        }/hq720.jpg"

        playerHeader = URLDecoder.decode(
            navBackStackEntry?.arguments?.getString("playerHeader"),
            StandardCharsets.UTF_8.toString()
        )
        playerBottomHeader = URLDecoder.decode(
            navBackStackEntry?.arguments?.getString("playerBottomHeader"),
            StandardCharsets.UTF_8.toString()
        )
        playerBottomSub = URLDecoder.decode(
            navBackStackEntry?.arguments?.getString("playerBottomSub"),
            StandardCharsets.UTF_8.toString()
        )
        if (isBulk.equals("true")) {
            viewModel.getStreamUrl(videoId!!)
            viewModel.getBulkStreamUrl()
        } else {
            viewModel.getStreamUrl(videoId!!)
        }

    }
    LaunchedEffect(key1 = youTubePlayListBulkData) {
        if (youTubePlayListBulkData is Resource.Success) {

            playAudioList(mediaController!!, youTubePlayListBulkData?.data!!)

        }
    }

    LaunchedEffect(key1 = streamUrlData) {
        if (streamUrlData is Resource.Success) {
//            val items = (streamUrlData as Resource.Success<StreamUrlData>).data

            if (mediaController != null && mediaController!!.isConnected) {
                Log.d("checker", "connected")

                if (mediaController!!.currentMediaItem != null && mediaController!!.currentMediaItem?.mediaMetadata?.title!!.equals(
                        playerHeader
                    )
                ) {
                    Log.d("checker", "onsucessexisting video is playing")
                } else {
                    Log.d("checker", "playinggg iffff")

                    if (streamUrlData!!.data!!.streamUrl.isNotEmpty()) {
                        mediaUri = streamUrlData!!.data!!.streamUrl
                        playAudio(mediaController!!, mediaUri, albumArt, playerHeader)
                    }
                }
            } else {
                Log.d("checker", "playinggg else")

                if (streamUrlData!!.data!!.streamUrl.isNotEmpty()) {
                    mediaUri = streamUrlData!!.data!!.streamUrl
                    playAudio(mediaController!!, mediaUri, albumArt, playerHeader)
                }
            }

        }
    }

    LaunchedEffect(!isLoading) {
        while (true) {
            if (mediaController?.isConnected == true && isPlaying) {
                progress =
                    (mediaController!!.currentPosition * 1.0f / mediaController!!.duration).coerceIn(
                        0f,
                        1f
                    )
//                currentTime="%02d:%02d:%02d".format(milliSec / 1000 / 60 / 60, milliSec / 1000 / 60 % 60, milliSec / 1000 % 60)
                currentTime =
                    if (mediaController!!.duration >= 3600000) "%02d:%02d:%02d".format(
                        mediaController!!.currentPosition / 1000 / 60 / 60,
                        mediaController!!.currentPosition / 1000 / 60 % 60,
                        mediaController!!.currentPosition / 1000 % 60
                    ) else "%02d:%02d".format(
                        mediaController!!.currentPosition / 1000 / 60 % 60,
                        mediaController!!.currentPosition / 1000 % 60
                    )

                // Update progress every second
                kotlinx.coroutines.delay(1000L)
            } else {
                // Stop updating if not playing
                kotlinx.coroutines.delay(1000L)
//                    break
                if (mediaController?.isPlaying != true) {
                    break
                }
            }

        }
    }

    MediaControllerComposable(
        onPlayingChanged = { playing ->
            isPlaying = playing
            if (playing) {
                isLoading = false
            }
            totalTime = if (mediaController!!.duration >= 3600000) "%02d:%02d:%02d".format(
                mediaController!!.duration / 1000 / 60 / 60,
                mediaController!!.duration / 1000 / 60 % 60,
                mediaController!!.duration / 1000 % 60
            ) else "%02d:%02d".format(
                mediaController!!.duration / 1000 / 60 % 60,
                mediaController!!.duration / 1000 % 60
            )
//            totalTime="%02d:%02d:%02d".format(milliSec / 1000 / 60 / 60, milliSec / 1000 / 60 % 60, milliSec / 1000 % 60)
            // Update progress every second
        },
        onMediaControllerInitialized = { controller ->
            mediaController = controller
            isPlaying = mediaController!!.isPlaying
            if (mediaController!!.currentMediaItem != null && mediaController!!.currentMediaItem?.mediaMetadata?.title!!.equals(
                    playerHeader
                )
            ) {
                isLoading = false
            } else {
                isLoading = true


            }

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
                model = ImageRequest.Builder(context)
                    .data(YoutubeCoreConstant.decodeThumpUrl(albumArt))
                    .crossfade(true)
                    .placeholder(R.drawable.placeholder)
                    .error(R.drawable.placeholder)
                    .build(),
                contentDescription = "Drawable Image",
                modifier = Modifier
                    .padding(horizontal = 30.dp, vertical = 10.dp)
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .align(Alignment.CenterHorizontally)
                    .clip(RoundedCornerShape(16.dp)), // Add this line to apply rounded corners,
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
                value = progress * 100f,
                onValueChange = { newValue ->
                    progress = newValue / 100f
                    mediaController?.seekTo((progress * mediaController?.duration!!).toLong())
                },
                colors = SliderDefaults.colors(
                    activeTrackColor = Color.White,
                    inactiveTrackColor = Color.Gray,
                    thumbColor = Color.Red
                ),
                valueRange = 0f..100f,
                modifier = Modifier.fillMaxWidth()
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = currentTime, color = Color.Gray, fontSize = 12.sp)
                Text(text = totalTime, color = Color.Gray, fontSize = 12.sp)
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.CenterHorizontally),
                horizontalArrangement = Arrangement.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_player_previous),
                    contentDescription = "Previous",
                    modifier = Modifier.size(30.dp)
                )
                Spacer(modifier = Modifier.width(40.dp))
                if (isLoading) {
                    CircularProgressIndicator(
                        color = colorResource(id = R.color.tubefyred),
                        modifier = Modifier.size(30.dp)
                    )
                } else {
                    Image(
                        painter = painterResource(id = if (isPlaying) R.drawable.ic_player_pause else R.drawable.ic_player_play),
                        contentDescription = "Play/Pause",
                        modifier = Modifier
                            .size(30.dp)
                            .clickable {
                                if (mediaController != null && mediaController!!.isConnected) {
                                    if (isPlaying) {
                                        mediaController?.pause()
                                    } else {
                                        mediaController?.play()
                                    }
                                }
                            }
                    )
                }
                Spacer(modifier = Modifier.width(40.dp))
                Image(
                    painter = painterResource(id = R.drawable.ic_player_next),
                    contentDescription = "Next",
                    modifier = Modifier.size(30.dp)
                )
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun PlayerPreview() {
    PlayerBaseView()
}

fun playAudioList(
    mediaController: MediaController?,
    mediaItems: List<MediaItem>
) {
    mediaController?.let {
        if (it.isConnected) {
            Log.d("click", "connected")

            // Set the media items to the MediaController
            if(it.currentMediaItem==null) {
                it.setMediaItems(mediaItems)
                it.playWhenReady = true
                it.prepare()
            }else
            {
                val mutableMediaItems = mediaItems.toMutableList()
                mutableMediaItems.removeAt(0)
                it.addMediaItems(mutableMediaItems)

            }
        }
    }
}

fun playAudio(
    mediaController: MediaController,
    mediaUri: String,
    videoThumpUrl: String,
    videoTitle: String
) {
    if (mediaController?.isConnected!!) {
        Log.d("click", "connected")

//                val drawableUri: Uri =
//                    Uri.parse("android.resource://${BuildConfig.APPLICATION_ID}/${com.ramzmania.tubefy.R.drawable.tubefy_icon}")

        fun mediaItemData() = MediaItem.Builder()
            .setMediaId("TubeFy")
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







