package com.ramzmania.tubefy.ui.components.screen.player

import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.annotation.OptIn
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.text.BasicText
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaController
import androidx.navigation.NavController
import com.ramzmania.tubefy.data.Resource
import com.ramzmania.tubefy.data.dto.searchformat.StreamUrlData
import com.ramzmania.tubefy.viewmodel.TubeFyViewModel
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

@OptIn(UnstableApi::class)
@Composable
fun MediaPlayerScreen(viewModel: TubeFyViewModel = hiltViewModel(), navController: NavController) {
    var mediaController by remember { mutableStateOf<MediaController?>(null) }
    var isPlaying by remember { mutableStateOf(false) }
    val streamUrlData by viewModel.streamUrlData.observeAsState()

    // Replace this URL with your desired media URL
    var mediaUri = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"
val context=LocalContext.current
    val navBackStackEntry = navController.currentBackStackEntry
    var albumArt by remember { mutableStateOf("") }
//Log.d("thiump url",videoThumpUrl)
    LaunchedEffect(Unit) {
//        val videoId = navBackStackEntry?.arguments?.getString("videoId")
        val videoId = URLDecoder.decode(navBackStackEntry?.arguments?.getString("videoId"), StandardCharsets.UTF_8.toString())
        albumArt = URLDecoder.decode(navBackStackEntry?.arguments?.getString("videoUrl"), StandardCharsets.UTF_8.toString())

        viewModel.getStreamUrl(videoId!!)
    }
    LaunchedEffect(key1 = streamUrlData) {
        if (streamUrlData is Resource.Success) {
            val items = (streamUrlData as Resource.Success<StreamUrlData>).data
            // Prepend new data to the existing list
            Log.d("datata", ">>VADAAA")

            Log.d("datata", ">>" + streamUrlData!!.data!!.streamUrl)
            if (streamUrlData!!.data!!.streamUrl.isNotEmpty()) {
                mediaUri=streamUrlData!!.data!!.streamUrl
                playAudio(mediaController!!,mediaUri,albumArt)

            }
        }
    }
    MediaControllerComposable(
        onPlayingChanged = { playing ->
            isPlaying = playing
        },
        onMediaControllerInitialized = { controller ->
            Log.d("konana","yoooo"+controller)
            mediaController = controller
        }
    )

    Column {
        Button(onClick = {
            Log.d("click","yp")

//            val mediaController = mediaController ?: return



        }) {
            Text("Play Audio")
        }

        Button(onClick = {
            mediaController?.pause()
        }) {
            Text("Pause")
        }

        Text(text = if (isPlaying) "Playing" else "Paused")
    }
}

fun playAudio(mediaController: MediaController,mediaUri:String,videoThumpUrl:String) {
    if (mediaController?.isConnected!!) {
        Log.d("click","connected")

//                val drawableUri: Uri =
//                    Uri.parse("android.resource://${BuildConfig.APPLICATION_ID}/${com.ramzmania.tubefy.R.drawable.tubefy_icon}")

        fun mediaItemData() = MediaItem.Builder()
            .setMediaId("id")
            .setUri(
                Uri.parse(mediaUri)
            )
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle("ghhhj")
                    .setArtist("Reading Page No : " )
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
