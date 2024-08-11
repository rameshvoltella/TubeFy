package com.ramzmania.tubefy.ui.components.screen.player

import android.net.Uri
import android.util.Log
import androidx.annotation.OptIn
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.text.BasicText
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaController

@OptIn(UnstableApi::class)
@Composable
fun MediaPlayerScreen() {
    var mediaController by remember { mutableStateOf<MediaController?>(null) }
    var isPlaying by remember { mutableStateOf(false) }

    // Replace this URL with your desired media URL
    val mediaUri = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"
val context=LocalContext.current
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
//                            .setArtworkUri(drawableUri)
                            .setMediaType(MediaMetadata.MEDIA_TYPE_MUSIC)
                            .build()
                    )
                    .build()

                mediaController?.setMediaItem(mediaItemData())
                mediaController?.playWhenReady = true
                mediaController?.prepare()

            }



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
