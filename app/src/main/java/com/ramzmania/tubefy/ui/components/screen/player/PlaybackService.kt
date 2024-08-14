package com.ramzmania.tubefy.ui.components.screen.player


import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaLibraryService
import androidx.media3.session.MediaSession

@UnstableApi
class PlaybackService : MediaLibraryService() {
    private lateinit var mediaLibrarySessionCallback: MediaLibrarySessionCallback
    private var mediaSession: MediaLibrarySession? = null
    var path:String=""

    override fun onCreate() {
        super.onCreate()

        val player = ExoPlayer.Builder(this).build().apply {
            // Adding the error listener to the ExoPlayer instance
            addListener(object : Player.Listener {
                override fun onPlayerError(error: PlaybackException) {
                    handlePlaybackError(error,currentMediaItem)
                }
            })
        }

        mediaLibrarySessionCallback = MediaLibrarySessionCallback()
        mediaSession = MediaLibrarySession.Builder(this, player, mediaLibrarySessionCallback).build()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo) = mediaSession

    override fun onDestroy() {
        mediaSession?.run {
            player.release()
            release()
            mediaSession = null
        }
        super.onDestroy()
    }
    private fun handlePlaybackError(error: PlaybackException, currentMediaItem: MediaItem?) {
        // Handle playback error
        // You can log the error or notify the user
        // For example:
//        val currentMediaItem = playe.currentMediaItem
        val currentUri = currentMediaItem?.localConfiguration?.uri
        if (currentUri != null) {
            android.util.Log.i("PlaybackService", "Current media URI: $currentUri")
        } else {
            android.util.Log.i("PlaybackService", "No current media URI found")
        }
        android.util.Log.e("PlaybackService", "Playback error occurred: ${error.message}")
    }
}