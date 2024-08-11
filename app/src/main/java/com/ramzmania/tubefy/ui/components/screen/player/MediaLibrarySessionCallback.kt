package com.ramzmania.tubefy.ui.components.screen.player

import android.net.Uri
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

    /*    override fun onGetChildren(
            session: MediaLibraryService.MediaLibrarySession,
            browser: MediaSession.ControllerInfo,
            parentId: String,
            page: Int,
            pageSize: Int,
            params: MediaLibraryService.LibraryParams?
        ): ListenableFuture<LibraryResult<ImmutableList<MediaItem>>> {
            val items = when(parentId) {
                Root -> listOf(browsableItem(Playlist))
                Playlist -> PlaybackService.testMediaItems()
                else -> null
            }

            return if(items != null) {
                Futures.immediateFuture(LibraryResult.ofItemList(items, params))
            } else {
                Futures.immediateFuture(LibraryResult.ofError(LibraryResult.RESULT_ERROR_BAD_VALUE))
            }
        }*/

    /*   override fun onGetItem(
           session: MediaLibraryService.MediaLibrarySession,
           browser: MediaSession.ControllerInfo,
           mediaId: String
       ): ListenableFuture<LibraryResult<MediaItem>> {
           val item = when(mediaId) {
               Root -> browsableItem(Root)
               Playlist -> PlaybackService.testMediaItem()
               else -> null
           }

           return if(item != null) {
               Futures.immediateFuture(LibraryResult.ofItem(item, null))
           } else {
               Futures.immediateFuture(LibraryResult.ofError(LibraryResult.RESULT_ERROR_BAD_VALUE))
           }
       }*/

    /*  override fun onAddMediaItems(
          mediaSession: MediaSession,
          controller: MediaSession.ControllerInfo,
          mediaItems: MutableList<MediaItem>
      ): ListenableFuture<MutableList<MediaItem>> {
          val items = mediaItems.map {
              getItem(it.mediaId) ?: it
          }.toMutableList()

          return Futures.immediateFuture(items)
      }*/

    /* private fun getItem(mediaId: String): MediaItem? {
         val playlist = PlaybackService.testMediaItems()
         return playlist.firstOrNull { mediaItem ->
             mediaItem.mediaId == mediaId
         }
     }*/

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