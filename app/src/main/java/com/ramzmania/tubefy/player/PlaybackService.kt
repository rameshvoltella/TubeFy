package com.ramzmania.tubefy.player


import android.app.PendingIntent
import android.content.Intent
import android.net.Uri
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.MimeTypes
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.Timeline
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.ResolvingDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.session.MediaLibraryService
import androidx.media3.session.MediaSession
import androidx.core.os.bundleOf
import com.ramzmania.tubefy.core.YoutubeCoreConstant
import com.ramzmania.tubefy.core.download.Downloads
import com.ramzmania.tubefy.core.recommendation.DailyRecommendations
import com.ramzmania.tubefy.data.local.SongHistory
import com.ramzmania.tubefy.data.Resource
import com.ramzmania.tubefy.data.database.DatabaseRepositorySource
import com.ramzmania.tubefy.data.dto.base.searchformat.TubeFyCoreTypeData
import com.ramzmania.tubefy.data.local.LocalRepositorySource
import com.ramzmania.tubefy.data.remote.RemoteRepositorySource
import com.ramzmania.tubefy.database.QuePlaylist
import com.ramzmania.tubefy.ui.components.HomeActivity
import com.ramzmania.tubefy.utils.PreferenceManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AndroidEntryPoint
@UnstableApi
class PlaybackService(
) : MediaLibraryService() {
    @Inject
    lateinit var localRepositorySource: LocalRepositorySource

    @Inject
    lateinit var remoteRepositorySource: RemoteRepositorySource

    @Inject
    lateinit var preferenceManager: PreferenceManager

    @Inject
    lateinit var dataBaseRepositorySource: DatabaseRepositorySource

    @Inject
    lateinit var songHistory: SongHistory

    @Inject
    lateinit var dailyRecommendations: DailyRecommendations
    private lateinit var mediaLibrarySessionCallback: MediaLibrarySessionCallback
    /** Background failures are logged instead of crashing the app (and stopping the music). */
    private val crashGuard = CoroutineExceptionHandler { _, error ->
        Log.e("PlaybackService", "Background task failed", error)
    }
    private val mainScope = CoroutineScope(Dispatchers.Main + SupervisorJob() + crashGuard)
    private lateinit var sleepTimer: SleepTimer
    private lateinit var smartShuffle: SmartShuffle
    private lateinit var notificationButtons: NotificationButtons
    private val upcomingShuffle = UpcomingShuffle()
    private val mainHandler = Handler(Looper.getMainLooper())

    /** Session extras shared by the sleep timer and Smart Shuffle (read by the player screen). */
    private val sessionExtras = Bundle()

    private fun loadErrorMessage(): String {
        val connectivity = getSystemService(ConnectivityManager::class.java)
        val online = connectivity?.getNetworkCapabilities(connectivity.activeNetwork)
            ?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
        return if (online) "Couldn't load this song" else "No internet connection"
    }

    private fun publishSessionExtras(values: Bundle) {
        // Only push when something actually changed: every push wakes all controllers / the notification
        @Suppress("DEPRECATION")
        val changed = values.keySet().any { !sessionExtras.containsKey(it) || sessionExtras.get(it) != values.get(it) }
        if (!changed) return
        sessionExtras.putAll(values)
        mediaSession?.setSessionExtras(Bundle(sessionExtras))
    }
    private var mediaSession: MediaLibrarySession? = null
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob() + crashGuard)
    private var removeExistingPlayList: Boolean = false
    private var fetchRecentPlaylistQueue: Boolean = false

    /** True while playing downloaded files: no network refetch and no auto-appended queue songs. */
    private var offlineMode: Boolean = false

    private val streamUrlRefresher = StreamUrlRefresher()

    /** Songs already retried once with a fresh link after a playback error. */
    private val linkRetriedIds = mutableSetOf<String>()

    /** The running "what plays next" refill (Mix or cache), so it isn't started twice. */
    private var autoQueueJob: Job? = null
    var path: String = ""
    private var apiPlaListBulkCallJob: Job? = null
//    var currentPlayListQueue:Long=0L

    override fun onCreate() {
        super.onCreate()
        // Video page: only use VP9 where it's hardware-decoded (decided once per process)
        VideoManifest.allowVp9 = VideoManifest.hasHardwareVp9()


        // Links of queued songs are refreshed just before use if they have expired (long pauses)
        val dataSourceFactory = ResolvingDataSource.Factory(DefaultDataSource.Factory(this), streamUrlRefresher)
        val player = ExoPlayer.Builder(this)
            .setMediaSourceFactory(DefaultMediaSourceFactory(dataSourceFactory))
            // Pause when the audio output goes away (Bluetooth/car disconnects, headphones unplugged)
            .setHandleAudioBecomingNoisy(true)
            // Take audio focus as music: pause for calls / other players, duck for short sounds
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(C.USAGE_MEDIA)
                    .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                    .build(),
                /* handleAudioFocus = */ true
            )
            .build().apply {
            // Adding the error listener to the ExoPlayer instance
            addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(playbackState: Int) {
                    super.onPlaybackStateChanged(playbackState)
                    if (playbackState == Player.STATE_READY) {
                        currentMediaItem?.mediaId?.let { linkRetriedIds.remove(it) }
                    }
                    if (playbackState == Player.STATE_READY && playWhenReady) {
                        // Playback has started
                        // Add your logic here for when playback starts
                        if (fetchRecentPlaylistQueue) {
                            fetchRecentPlaylistQueue = false
                            handleMediaItemChange()
                        }
                    }

                }

                override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
                    if (shuffleModeEnabled) {
                        // Shuffle only what's still to come, so the current song keeps a "next"
                        upcomingShuffle.onSongStarted(currentMediaItem?.mediaId)
                        upcomingShuffle.apply(this@apply, reshuffle = true)
                        handleMediaItemChange() // nothing queued yet: start the auto-queue refill
                    }
                    // Keep the notification's shuffle button in sync (player screen, other controllers)
                    if (::notificationButtons.isInitialized) notificationButtons.onShuffleChanged()
                }

                override fun onTimelineChanged(timeline: Timeline, reason: Int) {
                    // Songs added while shuffling (auto-queue, Mix, playlist loading) go after the current one
                    if (reason == Player.TIMELINE_CHANGE_REASON_PLAYLIST_CHANGED && shuffleModeEnabled) {
                        mainHandler.post { upcomingShuffle.apply(this@apply) }
                    }
                }

                override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
                    if (!playWhenReady && reason == Player.PLAY_WHEN_READY_CHANGE_REASON_END_OF_MEDIA_ITEM) {
                        sleepTimer.onPausedAtEndOfSong()
                    }
                }

                override fun onPlayerError(error: PlaybackException) {
                    Log.w("PlaybackService", "Playback error ${error.errorCodeName} on ${currentMediaItem?.mediaId}", error)
                    // A dead link (expired / rejected): retry the same song once with a fresh link,
                    // keeping its position, before falling back to the skip logic below
                    val failedId = currentMediaItem?.mediaId
                    if (!offlineMode && failedId != null &&
                        error.errorCode == PlaybackException.ERROR_CODE_IO_BAD_HTTP_STATUS &&
                        linkRetriedIds.add(failedId)
                    ) {
                        streamUrlRefresher.forceRefresh(failedId)
                        playWhenReady = true
                        prepare()
                        return
                    }
                    if (offlineMode) {
                        // A local file failed (e.g. deleted): just move on to the next download
                        if (hasNextMediaItem()) {
                            seekToNextMediaItem()
                            prepare()
                            playWhenReady = true
                        }
                        return
                    }
                    val videoId = currentMediaItem?.mediaId // Assuming mediaId is the videoId
                    val albumArt = currentMediaItem?.mediaMetadata?.artworkUri.toString()
                    val playerHeader = currentMediaItem?.mediaMetadata?.title.toString()


                    if (retryCount <= maxRetries) {
                        getStreamUrl(videoId!!, albumArt, playerHeader, currentMediaItemIndex)
                    }
                    if (hasNextMediaItem()) {

                        retryCount = 0
                        seekToNextMediaItem()
                        playWhenReady = true
                        prepare()

                    } else {

                        retryCount++
                        playWhenReady = true
                        prepare()
                        pause()
                    }

//                    handlePlaybackError(error, currentMediaItem)
//                    quickRetryLauchMediaPlayer()

//                    mediaSession?.player?.seekToNextMediaItem()
                }

                override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
//                    mediaItem?.let {
                    upcomingShuffle.onSongStarted(mediaItem?.mediaId)
                    smartShuffle.onSongChanged()
                    notificationButtons.refresh()
                    handleMediaItemChange()
//                    }
                }
            })
        }


        val activityIntent = Intent(this, HomeActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            activityIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )


        sleepTimer = SleepTimer(mainScope, { mediaSession }, ::publishSessionExtras)
        smartShuffle = SmartShuffle(
            scope = serviceScope,
            playerProvider = { mediaSession?.player },
            remote = remoteRepositorySource,
            publish = ::publishSessionExtras,
            isOffline = { offlineMode }
        )
        val carLibrary = CarLibrary(this, songHistory, dailyRecommendations, dataBaseRepositorySource, remoteRepositorySource)
        mediaLibrarySessionCallback = MediaLibrarySessionCallback(
            customCommands = SleepTimer.COMMANDS + SmartShuffle.COMMANDS + NotificationButtons.COMMANDS,
            carLibrary = carLibrary,
            scope = serviceScope,
            onBrowserQueue = { offline ->
                // Picked in the car: a fresh queue (Downloads play offline, others get the Mix later)
                apiPlaListBulkCallJob?.cancel()
                offlineMode = offline
                upcomingShuffle.reset()
            }
        ) { action, args ->
            when (action) {
                SmartShuffle.CMD_SET -> {
                    smartShuffle.handleCommand(action, args)
                    notificationButtons.onShuffleChanged()
                }
                NotificationButtons.CMD_TOGGLE, NotificationButtons.CMD_REFRESH, NotificationButtons.CMD_SHUFFLE ->
                    notificationButtons.handleCommand(action)
                else -> sleepTimer.handleCommand(action, args)
            }
        }
        mediaSession =
            MediaLibrarySession.Builder(this, player, mediaLibrarySessionCallback)
                .setSessionActivity(pendingIntent).build()
        notificationButtons = NotificationButtons(
            scope = serviceScope,
            sessionProvider = { mediaSession },
            database = dataBaseRepositorySource,
            isSmartShuffleOn = { smartShuffle.enabled },
            turnSmartShuffleOff = { smartShuffle.setEnabled(false) },
            publish = ::publishSessionExtras
        )
        notificationButtons.refresh()
    }

    /**
     * Keeps music going when the queue is about to run out: first YouTube's "Mix" for the
     * current song (similar language / genre / artist), and if that fails or adds nothing new,
     * the cached queue of songs from earlier playlists and searches ([queueFromCache]).
     */
    private fun handleMediaItemChange() {
        // Downloads play only the downloaded songs
        if (offlineMode) return
        val player = mediaSession?.player ?: return
        // More songs are still on the way (a playlist is loading) or a refill is already running
        if (player.hasNextMediaItem() || apiPlaListBulkCallJob?.isActive == true || autoQueueJob?.isActive == true) return

        val seedId = player.currentMediaItem?.mediaId
        val queuedIds = (0 until player.mediaItemCount).map { player.getMediaItemAt(it).mediaId }.toSet()
        autoQueueJob = serviceScope.launch {
            val mix = if (seedId.isNullOrBlank()) emptyList() else {
                (remoteRepositorySource.getMixQueue(seedId).first() as? Resource.Success)?.data.orEmpty()
            }.filter { YoutubeCoreConstant.extractYoutubeVideoId(it.videoId) !in queuedIds }

            if (mix.isNotEmpty()) {
                fetchFromQueue(mix.take(MIX_BATCH_SIZE))
            } else {
                withContext(Dispatchers.Main) { queueFromCache(player) }
            }
        }
    }

    /** The original auto-queue: songs cached from playlists/searches the user opened. */
    private fun queueFromCache(player: Player) {
        serviceScope.launch {
            dataBaseRepositorySource.getPlaylists().collect {
                if (it is Resource.Success) {
                    var mediaItemsList:List<MediaItem>?=null
                    withContext(Dispatchers.Main) {
                        mediaItemsList = (0 until player.mediaItemCount).map { player.getMediaItemAt(it) }

                        withContext(Dispatchers.IO)
                        {
                            val listFromQueue: ArrayList<TubeFyCoreTypeData?> = ArrayList()

                            for (data in it.data!!) {


                                var isAlreadyInList =false

                                if(mediaItemsList!=null)
                                {
                                    isAlreadyInList=mediaItemsList!!.any {

                                        it.mediaId ==YoutubeCoreConstant.extractYoutubeVideoId(data.videoId) }
                                }


                                if (!isAlreadyInList) {
                                    // If not already in the list, add the mediaItem
//                                        player.addMediaItem(mediaItem)
                                    listFromQueue.add(
                                        TubeFyCoreTypeData(
                                            videoId = data.videoId,
                                            videoTitle = data.videoName,
                                            videoImage = data.videoThumbnail
                                        )
                                    )
                                } else {
                                    // If it's already in the list, skip adding it
                                    Log.d("MediaItem", "The media item is already in the playlist.")
                                }

                            }

                            if (listFromQueue.size > 0) {
                                apiPlaListBulkCallJob?.cancel()
                                listFromQueue.shuffle()
                                if (listFromQueue.size > 20) {
                                    fetchFromQueue(listFromQueue.toMutableList().take(20))
                                } else {
                                    fetchFromQueue(listFromQueue.toMutableList())

                                }

                            }

                        }
                    }


                }
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        intent?.let {
            when (it.action) {
                ACTION_PLAY_DOWNLOADS -> {
                    upcomingShuffle.reset()
                    apiPlaListBulkCallJob?.cancel()
                    offlineMode = true
                    playDownloads()
                }

                ACTION_FETCH_PLAYLIST -> {
                    upcomingShuffle.reset()
                    offlineMode = false
                    apiPlaListBulkCallJob?.cancel()
                    preferenceManager.putLong("queue", System.currentTimeMillis())
                    removeExistingPlayList = true
                    fetchFromActiveDb()

                }

                ACTION_FETCH_SONG -> {
                    upcomingShuffle.reset()
                    offlineMode = false
                    publishSessionExtras(bundleOf(EXTRA_LOAD_ERROR to ""))
                    apiPlaListBulkCallJob?.cancel()
                    preferenceManager.putLong("queue", System.currentTimeMillis())
                    removeExistingPlayList = true
                    fetchRecentPlaylistQueue = true
                    it.extras?.getString(VIDEO_ID)?.let { it1 ->
                        if (it.getBooleanExtra(VIDEO_MODE, false)) {
                            // Video page: all qualities, audio languages and captions
                            getVideoStream(it1, it.extras?.getString(ALBUM_ART).orEmpty(), it.extras?.getString(VIDEO_TITLE).orEmpty())
                        } else getStreamUrl(
                            it1,
                            it.extras?.getString(ALBUM_ART)!!, it.extras?.getString(VIDEO_TITLE)!!
                        )

                        serviceScope.launch {
                            dataBaseRepositorySource.addSongToQueue(
                                QuePlaylist(
                                    videoId = it1,
                                    videoThumbnail = it.extras?.getString(ALBUM_ART)!!,
                                    videoName = it.extras?.getString(VIDEO_TITLE)!!
                                )
                            ).collect {}

                        }
                    }

                }

                else -> {}
            }
        }
        return START_STICKY
    }

    /** Queues the active playlist (set from the Downloads page) using the local files only. */
    private fun playDownloads() {
        serviceScope.launch {
            dataBaseRepositorySource.getAllActivePlaylists().collect { result ->
                if (result !is Resource.Success) return@collect
                val items = result.data.orEmpty().filterNotNull()
                    .filter { Downloads.isDownloaded(this@PlaybackService, it.videoId) }
                    .map { song ->
                        MediaItem.Builder()
                            .setMediaId(Downloads.plainId(song.videoId))
                            .setUri(Uri.fromFile(Downloads.file(this@PlaybackService, song.videoId)))
                            .setMediaMetadata(
                                MediaMetadata.Builder()
                                    .setTitle(song.videoTitle)
                                    .setArtworkUri(Uri.parse(song.videoImage))
                                    .setIsBrowsable(false)
                                    .setIsPlayable(true)
                                    .setMediaType(MediaMetadata.MEDIA_TYPE_MUSIC)
                                    .setExtras(bundleOf(Downloads.OFFLINE_EXTRA to true))
                                    .build()
                            )
                            .build()
                    }
                if (items.isEmpty()) return@collect
                withContext(Dispatchers.Main) {
                    mediaSession?.player?.apply {
                        setMediaItems(items)
                        playWhenReady = true
                        prepare()
                    }
                }
            }
        }
    }

    private fun fetchFromActiveDb() {
        serviceScope.launch {
            dataBaseRepositorySource.getAllActivePlaylists(

            ).collect {
                when(it)
                {
                    is Resource.Success->{

                        apiPlaListBulkCallJob?.cancel()
                        if(it.data!=null) {
                            if(it.data.isNotEmpty()) {
//                                for(item in it.data) {
//                                    Log.d("Got Active liat", "it" + item?.videoId)
//                                }
                                fetchFromQueue(it.data)
                            }
                        }


                    }
                    else -> {}
                }
            }
        }
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo) = mediaSession

    override fun onDestroy() {
        sleepTimer.release()
        mainScope.cancel()
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
        val currentIndex =
            mediaSession?.player?.currentMediaItemIndex ?: -1 // Get current media item index
//        val currentUri = currentMediaItem?.localConfiguration?.uri
//        if (currentUri != null) {
//            Log.i("PlaybackServiceCKER", "Current media URI: $currentUri")
//        } else {
//            Log.i("PlaybackServiceCKER", "No current media URI found")
//        }
//        Log.e("PlaybackServiceCKER", "Playback error occurred: ${error.message}")
        moveToNextMediaItem(currentIndex, currentMediaItem)

//        if (isNetworkError(error)) {
//            Log.e("PlaybackServiceCKER", "Network error occurred: ${error.message}")
//            // Handle network error (e.g., retrying the current media item, notifying the user, etc.)
//            retryCurrentMediaItem(currentIndex,currentMediaItem)
//        } else {
//            Log.e("PlaybackServiceCKER", "Non-network playback error occurred: ${error.message}")
//        }
    }


    fun fetchFromQueue(totalList: List<TubeFyCoreTypeData?>) {
        val listFromQueue = totalList.take(2)
        apiPlaListBulkCallJob = serviceScope.launch {
            remoteRepositorySource.getStreamBulkUrl(YoutubePlayerPlaylistListModel(listFromQueue!!))
                .collect {
                    if (it is Resource.Success) {
                        withContext(Dispatchers.Main) {
                            playAudioList(it.data!!)
                        }
                        val remainingList =
                            totalList.drop(2)
                        if (remainingList.size != 0) {
                            fetchFromQueue(remainingList)
                        }
                    }
                }
        }
    }

/*
    fun fetchPlayList(inseLoop: Boolean) {

        val list = PlayListSingleton.getDataList()?.playListData?.take(2)

//        apiPlaListBulkCallJob = serviceScope.launch {
//            remoteRepositorySource.getStreamBulkUrl(YoutubePlayerPlaylistListModel(list!!))
//                .collect { resource ->
//                    if (resource is Resource.Success) {
//                        withContext(Dispatchers.Main) {
//                            playAudioList(resource.data!!)
//                        }
//                        val remainingList =
//                            PlayListSingleton.getDataList()?.playListData?.drop(2)
//                        PlayListSingleton.addData(remainingList!!)
//                        fetchPlayList(true)
//                    }
//                }
//        }
        apiPlaListBulkCallJob = serviceScope.launch {
            Log.d("bulkmode", "new array" + PlayListSingleton.getDataList()?.playListData?.size)
            if (PlayListSingleton.getDataList() != null && PlayListSingleton.getDataList()?.playListData?.size!! > 0) {
                val list = PlayListSingleton.getDataList()?.playListData?.take(2)
                remoteRepositorySource.getStreamBulkUrl(YoutubePlayerPlaylistListModel(list!!))
                    .collect {
                        if (it is Resource.Success) {

                            withContext(Dispatchers.Main) {
                                playAudioList(it.data!!)
                            }
                            val remainingList =
                                PlayListSingleton.getDataList()?.playListData?.drop(2)
                            PlayListSingleton.addData(remainingList!!)
                            fetchPlayList(true)

                        }
                    }
            } else {
                Log.d("bulkmode", "new array overrrrr")

            }
        }
    }
*/

    fun getStreamUrl(
        videoId: String,
        albumArt: String,
        playerHeader: String,
        mediaIndex: Int = -1
    ) {
        if (videoId != null) {
            serviceScope.launch {
                remoteRepositorySource.getStreamUrl(
                    YoutubeCoreConstant.extractYoutubeVideoId(
                        videoId
                    )!!, mediaIndex
                ).collect { response ->
                    // Tell the player screen whether the song the user picked could be loaded
                    if (mediaIndex == -1) {
                        val error = if (response is Resource.Success) "" else loadErrorMessage()
                        withContext(Dispatchers.Main) {
                            publishSessionExtras(bundleOf(EXTRA_LOAD_ERROR to error))
                        }
                    }
                    if (response is Resource.Success) {
                        withContext(Dispatchers.Main) {
                            mediaSession?.player?.let {

                                fun mediaItemData() = MediaItem.Builder()
                                    .setMediaId(
                                        "" + YoutubeCoreConstant.extractYoutubeVideoId(
                                            videoId
                                        )!!
                                    )
                                    .setUri(
                                        Uri.parse(response.data?.streamUrl)
                                    )
                                    // Live streams are HLS manifests; say so in case the URL doesn't show it
                                    .apply { if (YoutubeCoreConstant.isHlsUrl(response.data?.streamUrl)) setMimeType(MimeTypes.APPLICATION_M3U8) }
                                    // Lets StreamUrlRefresher re-fetch the link by song id once it expires
                                    .setCustomCacheKey(YoutubeCoreConstant.extractYoutubeVideoId(videoId))
                                    .setMediaMetadata(
                                        MediaMetadata.Builder()
                                            .setTitle(playerHeader)
                                            .setArtist(response.data?.artist?.takeIf { it.isNotBlank() } ?: "Artist :${playerHeader}")
                                            .setIsBrowsable(false)
                                            .setIsPlayable(true)
                                            .setArtworkUri(Uri.parse(albumArt))
                                            .setMediaType(MediaMetadata.MEDIA_TYPE_MUSIC)
                                            .build()
                                    )
                                    .build()
                                playAudioList(listOf(mediaItemData()).toMutableList(), mediaIndex)

                            }
                        }
                    }
                }
            }
        }


    }

    /**
     * Plays a video for the video page from a DASH manifest (every quality and audio language, plus
     * captions). Falls back to the normal stream for live videos or when the manifest can't be built.
     */
    private fun getVideoStream(videoId: String, albumArt: String, title: String) {
        serviceScope.launch {
            val id = YoutubeCoreConstant.extractYoutubeVideoId(videoId) ?: videoId
            val result = runCatching { VideoManifest.fetch(id) }.onFailure { Log.w("PlaybackService", "No manifest for $id", it) }.getOrNull()
            if (result == null) {
                getStreamUrl(videoId, albumArt, title)
                return@launch
            }
            streamUrlRefresher.putManifest(id, result.manifest)
            val captions = result.subtitles.mapIndexed { index, sub ->
                MediaItem.SubtitleConfiguration.Builder(Uri.parse(sub.url))
                    .setId("cc$index")
                    .setMimeType(MimeTypes.APPLICATION_TTML)
                    .setLanguage(sub.language)
                    .setLabel(sub.label)
                    .build()
            }
            val item = MediaItem.Builder()
                .setMediaId(id)
                .setUri(Uri.parse("${StreamUrlRefresher.DASH_SCHEME}://video/$id"))
                .setMimeType(MimeTypes.APPLICATION_MPD)
                .setSubtitleConfigurations(captions)
                .setMediaMetadata(
                    MediaMetadata.Builder()
                        .setTitle(title.ifBlank { result.title })
                        .setArtist(result.uploader.ifBlank { null })
                        .setArtworkUri(Uri.parse(albumArt.ifBlank { "https://i.ytimg.com/vi/$id/hq720.jpg" }))
                        .setIsBrowsable(false)
                        .setIsPlayable(true)
                        .setMediaType(MediaMetadata.MEDIA_TYPE_VIDEO)
                        .build()
                )
                .build()
            withContext(Dispatchers.Main) {
                publishSessionExtras(bundleOf(EXTRA_LOAD_ERROR to ""))
                playAudioList(listOf(item), -1)
            }
        }
    }

    fun playAudioList(mediaItems: List<MediaItem>, mediaIndex: Int = -1) {

//        mediaSession?.player?.let {
        if (mediaSession != null && mediaSession?.player != null) {
            val it = mediaSession?.player
            if (it!!.currentMediaItem == null) {

                removeExistingPlayList = false
                it.setMediaItems(mediaItems)
                it.playWhenReady = true
                it.prepare()
            } else {

                val mutableMediaItems = mediaItems.toMutableList()
                if (mediaIndex != -1) {
                    if (mediaIndex < it.mediaItemCount) {
                        it.removeMediaItem(mediaIndex)
                        it.addMediaItems(mediaIndex, mutableMediaItems)
                        if (it.mediaItemCount == 1) {
                            it.playWhenReady = true
                            it.prepare()
                        }

                    } else {
                        it.setMediaItems(mutableMediaItems)
                        it.playWhenReady = true
                        it.prepare()


                    }
                } else if (removeExistingPlayList) {

                    removeExistingPlayList = false
                    it.setMediaItems(mutableMediaItems)
                    it.playWhenReady = true
                    it.prepare()

                } else {
                    it.addMediaItems(mutableMediaItems)

//                    if(!it.isPlaying)
//                    {
//                        it.playWhenReady = true
//                        it.prepare()
//                    }

                }
            }
        }
//        }
    }


    companion object {
        const val ACTION_FETCH_PLAYLIST = "com.ramzmania.tubefy.ACTION_FETCH_PLAYLIST"
        const val ACTION_FETCH_SONG = "com.ramzmania.tubefy.ACTION_FETCH_SONG"
        const val ACTION_PLAY_DOWNLOADS = "com.ramzmania.tubefy.ACTION_PLAY_DOWNLOADS"

        /** Session extra: why the selected song couldn't load ("" when it's fine). */
        const val EXTRA_LOAD_ERROR = "com.ramzmania.tubefy.LOAD_ERROR"
        /** How many Mix songs to queue at once; the next refill seeds from the song playing then. */
        private const val MIX_BATCH_SIZE = 10
        const val VIDEO_ID = "com.ramzmania.tubefy.VIDEO_ID"
        const val VIDEO_MODE = "com.ramzmania.tubefy.VIDEO_MODE"
        const val ALBUM_ART = "com.ramzmania.tubefy.ALBUM_ART"
        const val VIDEO_TITLE = "com.ramzmania.tubefy.VIDEO_TITLE"


    }

    private fun isNetworkError(error: PlaybackException): Boolean {
        return error.errorCode == PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_FAILED ||
                error.errorCode == PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_TIMEOUT
    }


    private fun moveToNextMediaItem(currentIndex: Int, currentMediaItem: MediaItem?) {
        val player = mediaSession?.player
        if (player != null && currentIndex >= 0 && currentIndex < player.mediaItemCount - 1) {
            val videoId = currentMediaItem?.mediaId // Assuming mediaId is the videoId
            val albumArt = currentMediaItem?.mediaMetadata?.artworkUri.toString()
            val playerHeader = currentMediaItem?.mediaMetadata?.title.toString()

            // Fetch a new stream URL for the next media item
            getStreamUrl(videoId!!, albumArt, playerHeader, currentIndex)
//            player.seekToNextMediaItem()

            // Remove the problematic media item and move to the next
            player.removeMediaItem(currentIndex)

        } else {
            Log.i("PlaybackServiceCKER", "No more media items to play or invalid index.")
            retryCurrentMediaItem(currentIndex, currentMediaItem)
        }
    }

    private val maxRetries = 20
    private var retryCount = 0
    private fun retryCurrentMediaItem(currentIndex: Int, currentMediaItem: MediaItem?) {
        if (retryCount < maxRetries) {
            retryCount++
            Log.i("PlaybackServiceCKER", "Retrying media item. Attempt $retryCount of $maxRetries")

            currentMediaItem?.let {
                val videoId = it.mediaId // Assuming mediaId is the videoId
                val albumArt = it.mediaMetadata.artworkUri.toString()
                val playerHeader = it.mediaMetadata.title.toString()
                getStreamUrl(videoId!!, albumArt, playerHeader, currentIndex)

            }
        } else {
            Log.e("PlaybackServiceCKER", "Max retries reached. Moving to the next media item.")
            retryCount = 0
            moveToNextMediaItem(currentIndex, currentMediaItem)
        }
    }



}