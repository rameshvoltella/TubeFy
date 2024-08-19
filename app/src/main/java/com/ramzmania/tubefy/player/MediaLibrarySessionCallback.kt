package com.ramzmania.tubefy.player

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.view.KeyEvent
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.LibraryResult
import androidx.media3.session.MediaLibraryService
import androidx.media3.session.MediaSession
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture

@UnstableApi
class MediaLibrarySessionCallback: MediaLibraryService.MediaLibrarySession.Callback {

    override fun onGetLibraryRoot(
        session: MediaLibraryService.MediaLibrarySession,
        browser: MediaSession.ControllerInfo,
        params: MediaLibraryService.LibraryParams?
    ): ListenableFuture<LibraryResult<MediaItem>> {
        return Futures.immediateFuture(LibraryResult.ofItem(browsableItem(Root), params))
    }


    override fun onMediaButtonEvent(
        session: MediaSession,
        controllerInfo: MediaSession.ControllerInfo,
        mediaButtonEvent: Intent
    ): Boolean {
        val keyEvent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            mediaButtonEvent.getParcelableExtra(Intent.EXTRA_KEY_EVENT, KeyEvent::class.java)
        } else {
            @Suppress("DEPRECATION")
            mediaButtonEvent.getParcelableExtra(Intent.EXTRA_KEY_EVENT) as? KeyEvent
        }
        if (keyEvent != null && keyEvent.action == KeyEvent.ACTION_DOWN) {

            when (keyEvent.keyCode) {
                KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE -> {
                    // Handle play/pause
                    if(session!=null&&session.player!=null) {
                        if(session.player.isPlaying)
                        {
                            session.player.pause()
                        }else
                        {
                            session.player.play()
                        }
                    }
                    return true
                }
                KeyEvent.KEYCODE_MEDIA_NEXT -> {
//                    Log.d("caller","RECEVED NEXT"+keyEvent.keyCode)

                    if(session!=null&&session.player!=null&&session.player.hasNextMediaItem()) {
                        session.player.seekToNextMediaItem()
                    }
                    // Handle next
                    return true
                }
                KeyEvent.KEYCODE_MEDIA_PREVIOUS -> {
//                    Log.d("caller","RECEVED PREV"+keyEvent.keyCode+""+session.player.hasPreviousMediaItem())

                    // Handle previous
                    if(session!=null&&session.player!=null&&session.player.hasPreviousMediaItem()) {
                        session.player.seekToPreviousMediaItem()
                    }
                    return true
                }
            }
        }
        return super.onMediaButtonEvent(session, controllerInfo, mediaButtonEvent)
    }

    private fun browsableItem(title: String): MediaItem {
        val metadata = MediaMetadata.Builder()
            .setTitle(title)
            .setIsBrowsable(true)
            .setIsPlayable(false)
            .setMediaType(MediaMetadata.MEDIA_TYPE_FOLDER_MIXED)
            .build()

        return MediaItem.Builder()
            .setMediaId(title)
            .setMediaMetadata(metadata)
            .setSubtitleConfigurations(mutableListOf())
            .setUri(Uri.EMPTY)
            .build()
    }

    companion object {
        private const val Root = "root"
        private const val Playlist = "playlist"
    }
}