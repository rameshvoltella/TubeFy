package com.ramzmania.tubefy.player

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.LibraryResult
import androidx.media3.session.MediaLibraryService
import androidx.media3.session.MediaSession
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionResult
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.SettableFuture
import com.google.common.collect.ImmutableList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

@UnstableApi
class MediaLibrarySessionCallback(
    private val customCommands: List<SessionCommand>,
    private val carLibrary: CarLibrary,
    private val scope: CoroutineScope,
    /** Called when a car/browser starts a queue; true when it's offline (Downloads) playback. */
    private val onBrowserQueue: (offline: Boolean) -> Unit,
    private val onCustomCommand: (action: String, args: Bundle) -> Unit
) : MediaLibraryService.MediaLibrarySession.Callback {

    override fun onConnect(
        session: MediaSession,
        controller: MediaSession.ControllerInfo
    ): MediaSession.ConnectionResult {
        // Allow controllers (the player screen) to drive the sleep timer and Smart Shuffle
        val commands = MediaSession.ConnectionResult.DEFAULT_SESSION_AND_LIBRARY_COMMANDS.buildUpon()
            .apply { customCommands.forEach { add(it) } }
            .build()
        return MediaSession.ConnectionResult.AcceptedResultBuilder(session)
            .setAvailableSessionCommands(commands)
            .build()
    }

    override fun onCustomCommand(
        session: MediaSession,
        controller: MediaSession.ControllerInfo,
        customCommand: SessionCommand,
        args: Bundle
    ): ListenableFuture<SessionResult> {
        onCustomCommand(customCommand.customAction, args)
        return Futures.immediateFuture(SessionResult(SessionResult.RESULT_SUCCESS))
    }


    override fun onGetLibraryRoot(
        session: MediaLibraryService.MediaLibrarySession,
        browser: MediaSession.ControllerInfo,
        params: MediaLibraryService.LibraryParams?
    ): ListenableFuture<LibraryResult<MediaItem>> {
        return Futures.immediateFuture(LibraryResult.ofItem(carLibrary.root(), params))
    }

    override fun onGetChildren(
        session: MediaLibraryService.MediaLibrarySession,
        browser: MediaSession.ControllerInfo,
        parentId: String,
        page: Int,
        pageSize: Int,
        params: MediaLibraryService.LibraryParams?
    ): ListenableFuture<LibraryResult<ImmutableList<MediaItem>>> {
        val future = SettableFuture.create<LibraryResult<ImmutableList<MediaItem>>>()
        scope.launch {
            val started = System.currentTimeMillis()
            var children = emptyList<MediaItem>()
            try {
                // A car waits on this: never let a slow network keep it spinning forever. Loaded in its
                // own job because network calls block and can't be interrupted by the timeout itself.
                val loading = scope.async { carLibrary.children(parentId) }
                children = withTimeoutOrNull(BROWSE_TIMEOUT_MS) { loading.await() }
                    ?: emptyList<MediaItem>().also {
                        loading.cancel()
                        Log.w(TAG, "Browsing $parentId timed out")
                    }
            } catch (e: Throwable) {
                Log.e(TAG, "Browsing $parentId failed", e)
            } finally {
                // Cars ask long lists page by page; answer only the requested page
                val pageItems = if (pageSize in 1 until Int.MAX_VALUE) {
                    children.drop((page.toLong() * pageSize).coerceAtMost(Int.MAX_VALUE.toLong()).toInt()).take(pageSize)
                } else children
                Log.d(TAG, "Browse $parentId page $page/$pageSize: ${pageItems.size} of ${children.size} items in ${System.currentTimeMillis() - started} ms")
                future.set(LibraryResult.ofItemList(ImmutableList.copyOf(pageItems), params))
            }
        }
        return future
    }

    /** Some cars look items up by id (e.g. to resume); answer instead of leaving it pending. */
    override fun onGetItem(
        session: MediaLibraryService.MediaLibrarySession,
        browser: MediaSession.ControllerInfo,
        mediaId: String
    ): ListenableFuture<LibraryResult<MediaItem>> {
        val item = carLibrary.find(mediaId)
        return Futures.immediateFuture(
            if (item != null) LibraryResult.ofItem(item, null) else LibraryResult.ofError(LibraryResult.RESULT_ERROR_BAD_VALUE)
        )
    }

    /** A song picked in the car: queue it with the rest of its folder, ready to play. */
    override fun onSetMediaItems(
        mediaSession: MediaSession,
        controller: MediaSession.ControllerInfo,
        mediaItems: MutableList<MediaItem>,
        startIndex: Int,
        startPositionMs: Long
    ): ListenableFuture<MediaSession.MediaItemsWithStartPosition> {
        val (items, start, offline) = carLibrary.resolveQueue(mediaItems, startIndex)
        onBrowserQueue(offline)
        return Futures.immediateFuture(
            MediaSession.MediaItemsWithStartPosition(items, start, startPositionMs)
        )
    }

    override fun onAddMediaItems(
        mediaSession: MediaSession,
        controller: MediaSession.ControllerInfo,
        mediaItems: MutableList<MediaItem>
    ): ListenableFuture<MutableList<MediaItem>> =
        Futures.immediateFuture(mediaItems.map(carLibrary::playable).toMutableList())


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
        private const val TAG = "CarBrowse"
        private const val BROWSE_TIMEOUT_MS = 15_000L
        private const val Root = "root"
        private const val Playlist = "playlist"
    }
}