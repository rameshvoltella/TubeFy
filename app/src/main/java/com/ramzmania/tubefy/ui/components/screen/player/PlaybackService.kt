package com.ramzmania.tubefy.ui.components.screen.player


import android.net.Uri
import androidx.core.net.toUri
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaLibraryService
import androidx.media3.session.MediaSession

import java.io.File

@UnstableApi
class PlaybackService : MediaLibraryService() {
    private lateinit var mediaLibrarySessionCallback: MediaLibrarySessionCallback
    private var mediaSession: MediaLibrarySession? = null
    var path:String=""

    override fun onCreate() {
        super.onCreate()

        val player = ExoPlayer.Builder(this).build()

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

    fun setFilePath(pathNew:String)
    {
        path="file://"+pathNew
    }

    companion object {
        /* val drawableUri: Uri = Uri.parse("android.resource://${BuildConfig.APPLICATION_ID}/${R.drawable.logo}")
         fun testMediaItem() = MediaItem.Builder()
             .setMediaId("id")
             .setUri(File(
                 App.Companion.context.filesDir.absolutePath
                     + "/pdfAudio/audio.wav").absolutePath.toUri())
             .setMediaMetadata(
                 MediaMetadata.Builder()
                     .setTitle("Oliveboard")
                     .setArtist("Reading pdf.....")
                     .setIsBrowsable(false)
                     .setIsPlayable(true)
                     .setArtworkUri(drawableUri)
                     .setMediaType(MediaMetadata.MEDIA_TYPE_MUSIC)
                     .build()
             )
             .build()

         fun testMediaItems() = mutableListOf(testMediaItem())*/
    }
}