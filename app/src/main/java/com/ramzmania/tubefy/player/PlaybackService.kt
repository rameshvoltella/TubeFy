package com.ramzmania.tubefy.player


import android.app.PendingIntent
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaLibraryService
import androidx.media3.session.MediaSession
import com.ramzmania.tubefy.core.YoutubeCoreConstant
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
    private lateinit var mediaLibrarySessionCallback: MediaLibrarySessionCallback
    private var mediaSession: MediaLibrarySession? = null
    private val serviceScope = CoroutineScope(Dispatchers.IO + Job())
    private var removeExistingPlayList: Boolean = false
    private var fetchRecentPlaylistQueue: Boolean = false
    var path: String = ""
    private var apiPlaListBulkCallJob: Job? = null
//    var currentPlayListQueue:Long=0L

    override fun onCreate() {
        super.onCreate()

        val player = ExoPlayer.Builder(this).build().apply {
            // Adding the error listener to the ExoPlayer instance
            addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(playbackState: Int) {
                    super.onPlaybackStateChanged(playbackState)
                    if (playbackState == Player.STATE_READY && playWhenReady) {
                        // Playback has started
                        // Add your logic here for when playback starts
                        if (fetchRecentPlaylistQueue) {
                            fetchRecentPlaylistQueue = false
                            handleMediaItemChange()
                        }
                    }

                }

                override fun onPlayerError(error: PlaybackException) {
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


        mediaLibrarySessionCallback = MediaLibrarySessionCallback()
        mediaSession =
            MediaLibrarySession.Builder(this, player, mediaLibrarySessionCallback)
                .setSessionActivity(pendingIntent).build()
    }

    private fun handleMediaItemChange() {
        val player = mediaSession?.player

        if (player != null) {

            if (!player.hasNextMediaItem()) {

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
//            else {
//                // Logic when there is no next media item
//            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        intent?.let {
            when (it.action) {
                ACTION_FETCH_PLAYLIST -> {
                    apiPlaListBulkCallJob?.cancel()
                    preferenceManager.putLong("queue", System.currentTimeMillis())
                    removeExistingPlayList = true
                    fetchFromActiveDb()

                }

                ACTION_FETCH_SONG -> {
                    apiPlaListBulkCallJob?.cancel()
                    preferenceManager.putLong("queue", System.currentTimeMillis())
                    removeExistingPlayList = true
                    fetchRecentPlaylistQueue = true
                    it.extras?.getString(VIDEO_ID)?.let { it1 ->
                        getStreamUrl(
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
                                    .setMediaMetadata(
                                        MediaMetadata.Builder()
                                            .setTitle(playerHeader)
                                            .setArtist("Artist :${playerHeader}")
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
        const val VIDEO_ID = "com.ramzmania.tubefy.VIDEO_ID"
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