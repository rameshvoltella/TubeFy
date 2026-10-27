package com.ramzmania.tubefy.player

import com.ramzmania.tubefy.core.analytics.Analytics
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import androidx.media3.common.C
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
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
        if (isCar(browser)) Analytics.carSession("browse")
        return Futures.immediateFuture(LibraryResult.ofItem(carLibrary.root(), params))
    }

    /** Android Auto (phone projection) or a car's own Android (Automotive). */
    private fun isCar(controller: MediaSession.ControllerInfo) =
        controller.packageName.contains("gearhead") || controller.packageName.contains("automotive")

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
                val pageItems = pageOf(children, page, pageSize)
                Log.d(TAG, "Browse $parentId page $page/$pageSize: ${pageItems.size} of ${children.size} items in ${System.currentTimeMillis() - started} ms")
                future.set(LibraryResult.ofItemList(ImmutableList.copyOf(pageItems), params))
            }
        }
        return future
    }

    /** Car search: runs the search, then tells the car how many results there are. */
    override fun onSearch(
        session: MediaLibraryService.MediaLibrarySession,
        browser: MediaSession.ControllerInfo,
        query: String,
        params: MediaLibraryService.LibraryParams?
    ): ListenableFuture<LibraryResult<Void>> {
        if (isCar(browser)) Analytics.carSession("search")
        scope.launch {
            val results = searchSafely(query)
            searchResults[query] = results
            withContext(Dispatchers.Main) { session.notifySearchResultChanged(browser, query, results.size, params) }
        }
        return Futures.immediateFuture(LibraryResult.ofVoid())
    }

    override fun onGetSearchResult(
        session: MediaLibraryService.MediaLibrarySession,
        browser: MediaSession.ControllerInfo,
        query: String,
        page: Int,
        pageSize: Int,
        params: MediaLibraryService.LibraryParams?
    ): ListenableFuture<LibraryResult<ImmutableList<MediaItem>>> {
        val cached = searchResults[query]
        if (cached != null) {
            return Futures.immediateFuture(LibraryResult.ofItemList(ImmutableList.copyOf(pageOf(cached, page, pageSize)), params))
        }
        val future = SettableFuture.create<LibraryResult<ImmutableList<MediaItem>>>()
        scope.launch {
            val results = searchSafely(query).also { searchResults[query] = it }
            future.set(LibraryResult.ofItemList(ImmutableList.copyOf(pageOf(results, page, pageSize)), params))
        }
        return future
    }

    /** Search results by query (only the latest few are kept). */
    private val searchResults = object : LinkedHashMap<String, List<MediaItem>>() {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, List<MediaItem>>?) = size > 5
    }.let { java.util.Collections.synchronizedMap(it) }

    private suspend fun searchSafely(query: String): List<MediaItem> = try {
        val searching = scope.async { carLibrary.search(query) }
        withTimeoutOrNull(BROWSE_TIMEOUT_MS) { searching.await() }
            ?: emptyList<MediaItem>().also { searching.cancel() }
    } catch (e: Throwable) {
        Log.e(TAG, "Search \"$query\" failed", e)
        emptyList()
    }.also { Log.d(TAG, "Search \"$query\": ${it.size} songs") }

    /** Cars ask long lists page by page; answer only the requested page. */
    private fun pageOf(items: List<MediaItem>, page: Int, pageSize: Int): List<MediaItem> {
        if (pageSize !in 1 until Int.MAX_VALUE) return items
        val from = (page.coerceAtLeast(0).toLong() * pageSize).coerceAtMost(Int.MAX_VALUE.toLong()).toInt()
        return items.drop(from).take(pageSize)
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
        // Voice: "play <song> on TubeFy" arrives as an item with only a search query
        val voiceQuery = mediaItems.singleOrNull()
            ?.takeIf { it.mediaId.isEmpty() }
            ?.requestMetadata?.searchQuery?.trim()
        val fromCar = isCar(controller)
        if (fromCar) Analytics.pendingSource = "car"
        if (!voiceQuery.isNullOrEmpty()) {
            if (fromCar) Analytics.carSession("voice")
            val future = SettableFuture.create<MediaSession.MediaItemsWithStartPosition>()
            scope.launch {
                val found = searchSafely(voiceQuery)
                val (items, start, _) = carLibrary.resolveQueue(found.take(1), 0)
                withContext(Dispatchers.Main) { onBrowserQueue(false) }
                future.set(MediaSession.MediaItemsWithStartPosition(items, start, C.TIME_UNSET))
            }
            return future
        }
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