package com.ramzmania.tubefy.ui.components.screen.player

import android.content.ComponentName
import android.util.Log
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.Tracks
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.MoreExecutors
import com.ramzmania.tubefy.player.PlaybackService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@androidx.annotation.OptIn(UnstableApi::class)
@Composable
fun MediaControllerComposable(
    onPlayingChanged: (Boolean) -> Unit,
    onMediaControllerInitialized: (MediaController) -> Unit,
    onMetaDataChangedValue: (MediaMetadata) -> Unit,
) {
    val context = LocalContext.current
    var mediaController by remember { mutableStateOf<MediaController?>(null) }

    LaunchedEffect(context) {
        try {
            // Create and initialize MediaController asynchronously
            val sessionToken = SessionToken(context, ComponentName(context, PlaybackService::class.java))
            val controllerFuture = MediaController.Builder(context, sessionToken).buildAsync()

            withContext(Dispatchers.IO) {
                controllerFuture.addListener(
                    {
                        try {
                            val controller = controllerFuture.get()
                            mediaController = controller.apply {
                                addListener(object : Player.Listener {
                                    override fun onIsPlayingChanged(isPlaying: Boolean) {
                                        onPlayingChanged(isPlaying)
                                    }

                                    override fun onTracksChanged(tracks: Tracks) {
                                        super.onTracksChanged(tracks)

                                    }

                                    override fun onMediaMetadataChanged(mediaMetadata: MediaMetadata) {
                                        super.onMediaMetadataChanged(mediaMetadata)
                                        Log.d("tagggg","chagessss")
                                        onMetaDataChangedValue(mediaMetadata)

                                    }

                                })
                                onMediaControllerInitialized(this)
                            }
                        } catch (ex: Exception) {
                            // Handle the exception
                            ex.printStackTrace()
                        }
                    },
                    MoreExecutors.directExecutor()
                )
            }
        } catch (ex: Exception) {
            // Handle the exception
            ex.printStackTrace()
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            mediaController?.release()
            mediaController = null
        }
    }
}
