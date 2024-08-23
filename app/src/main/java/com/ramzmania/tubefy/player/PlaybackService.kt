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
import com.ramzmania.tubefy.data.local.LocalRepositorySource
import com.ramzmania.tubefy.data.remote.RemoteRepositorySource
import com.ramzmania.tubefy.ui.components.HomeActivity
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
    private lateinit var mediaLibrarySessionCallback: MediaLibrarySessionCallback
    private var mediaSession: MediaLibrarySession? = null
    private val serviceScope = CoroutineScope(Dispatchers.IO + Job())
    private var removeExistingPlayList: Boolean = false
    var path: String = ""

    override fun onCreate() {
        super.onCreate()

        val player = ExoPlayer.Builder(this).build().apply {
            // Adding the error listener to the ExoPlayer instance
            addListener(object : Player.Listener {

                override fun onPlayerError(error: PlaybackException) {
                    val videoId = currentMediaItem?.mediaId // Assuming mediaId is the videoId
                    val albumArt = currentMediaItem?.mediaMetadata?.artworkUri.toString()
                    val playerHeader = currentMediaItem?.mediaMetadata?.title.toString()
                    if(retryCount<=maxRetries) {
                        getStreamUrl(videoId!!, albumArt, playerHeader, currentMediaItemIndex)
                    }
                    if(hasNextMediaItem()) {
                        retryCount=0
                        seekToNextMediaItem()
                        playWhenReady = true
                        prepare()


                    }else
                    {
                        retryCount++
                    }

//                    handlePlaybackError(error, currentMediaItem)
//                    quickRetryLauchMediaPlayer()

//                    mediaSession?.player?.seekToNextMediaItem()
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

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        intent?.let {
            when (it.action) {
                ACTION_FETCH_PLAYLIST -> {
                    removeExistingPlayList = true
                    fetchPlayList()
                }

                ACTION_FETCH_SONG -> {
                    removeExistingPlayList = true
                    it.extras?.getString(VIDEO_ID)?.let { it1 ->
                        getStreamUrl(
                            it1,
                            it.extras?.getString(ALBUM_ART)!!, it.extras?.getString(VIDEO_TITLE)!!
                        )
                    }
                }

                else -> {}
            }
        }
        return START_STICKY
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo) = mediaSession

    override fun onDestroy() {
        Log.d("ondestry called","WHY???")
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
        val currentIndex = mediaSession?.player?.currentMediaItemIndex ?: -1 // Get current media item index
//        val currentUri = currentMediaItem?.localConfiguration?.uri
//        if (currentUri != null) {
//            Log.i("PlaybackServiceCKER", "Current media URI: $currentUri")
//        } else {
//            Log.i("PlaybackServiceCKER", "No current media URI found")
//        }
//        Log.e("PlaybackServiceCKER", "Playback error occurred: ${error.message}")
        moveToNextMediaItem(currentIndex,currentMediaItem)

//        if (isNetworkError(error)) {
//            Log.e("PlaybackServiceCKER", "Network error occurred: ${error.message}")
//            // Handle network error (e.g., retrying the current media item, notifying the user, etc.)
//            retryCurrentMediaItem(currentIndex,currentMediaItem)
//        } else {
//            Log.e("PlaybackServiceCKER", "Non-network playback error occurred: ${error.message}")
//        }
    }

    fun fetchPlayList() {
        serviceScope.launch {
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
                            fetchPlayList()
                        }
                    }
            } else {
                Log.d("bulkmode", "new array overrrrr")

            }
        }
    }

    fun getStreamUrl(videoId: String, albumArt: String, playerHeader: String,mediaIndex:Int=-1) {
        if (videoId != null) {
            serviceScope.launch {
                remoteRepositorySource.getStreamUrl(
                    YoutubeCoreConstant.extractYoutubeVideoId(
                        videoId
                    )!!,mediaIndex
                ).collect { response ->
                    if (response is Resource.Success) {
                        withContext(Dispatchers.Main) {
                            mediaSession?.player?.let {

                                fun mediaItemData() = MediaItem.Builder()
                                    .setMediaId(""+YoutubeCoreConstant.extractYoutubeVideoId(videoId)!!)
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
                                playAudioList(listOf(mediaItemData()).toMutableList(),mediaIndex)

                            }
                        }
                    }
                }
            }
        }
    }

    fun playAudioList(mediaItems: List<MediaItem>,mediaIndex:Int=-1) {

        Log.d("Poda","<><><><<<>>>"+mediaIndex)
        mediaSession?.player?.let {

            if (it.currentMediaItem == null) {

                removeExistingPlayList = false
                it.setMediaItems(mediaItems)
                it.playWhenReady = true
                it.prepare()
            } else {

                val mutableMediaItems = mediaItems.toMutableList()
                if(mediaIndex!=-1)
                {
                    if(mediaIndex<it.mediaItemCount) {
                        it.removeMediaItem(mediaIndex)
                        it.addMediaItems(mediaIndex, mutableMediaItems)
                    }else
                    {
                        it.setMediaItems(mutableMediaItems)
                        it.playWhenReady = true
                        it.prepare()

                    }
                }
                else if (removeExistingPlayList) {

                    removeExistingPlayList = false
                    it.setMediaItems(mutableMediaItems)
                    it.playWhenReady = true
                    it.prepare()
                } else {

                    it.addMediaItems(mutableMediaItems)

                }
            }
        }
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


    private fun moveToNextMediaItem(currentIndex: Int,currentMediaItem: MediaItem?) {
        val player = mediaSession?.player
        if (player != null && currentIndex >= 0 && currentIndex < player.mediaItemCount - 1) {
            val videoId = currentMediaItem?.mediaId // Assuming mediaId is the videoId
            val albumArt = currentMediaItem?.mediaMetadata?.artworkUri.toString()
            val playerHeader = currentMediaItem?.mediaMetadata?.title.toString()

            // Fetch a new stream URL for the next media item
            getStreamUrl(videoId!!, albumArt, playerHeader,currentIndex)
//            player.seekToNextMediaItem()

            // Remove the problematic media item and move to the next
            player.removeMediaItem(currentIndex)

        } else {
            Log.i("PlaybackServiceCKER", "No more media items to play or invalid index.")
            retryCurrentMediaItem(currentIndex,currentMediaItem)
        }
    }

    private val maxRetries = 3
    private var retryCount = 0
    private fun retryCurrentMediaItem(currentIndex: Int, currentMediaItem: MediaItem?) {
        if (retryCount < maxRetries) {
            retryCount++
            Log.i("PlaybackServiceCKER", "Retrying media item. Attempt $retryCount of $maxRetries")

            currentMediaItem?.let {
                val videoId = it.mediaId // Assuming mediaId is the videoId
                val albumArt = it.mediaMetadata.artworkUri.toString()
                val playerHeader = it.mediaMetadata.title.toString()
                getStreamUrl(videoId!!, albumArt, playerHeader,currentIndex)

            }
        } else {
            Log.e("PlaybackServiceCKER", "Max retries reached. Moving to the next media item.")
            retryCount = 0
            moveToNextMediaItem(currentIndex,currentMediaItem)
        }
    }


    fun quickRetryLauchMediaPlayer()
    {
           /* mediaSession?.player?.let {

                fun mediaItemData() = MediaItem.Builder()
                    .setMediaId("id")
                    .setUri(
                        Uri.parse("https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4")
                    )
                    .setMediaMetadata(
                        MediaMetadata.Builder()
                            .setTitle("tadadada")
                            .setArtist("Reading Page No : ")
                            .setIsBrowsable(false)
                            .setIsPlayable(true)
                        .setArtworkUri(Uri.parse("https://www.cbc.ca/kids/images/weird_wonderful_bunnies_header_update_1140.jpg"))
                            .setMediaType(MediaMetadata.MEDIA_TYPE_MUSIC)
                            .build()
                    )
                    .build()

                it.setMediaItem(mediaItemData())
                it.playWhenReady = true
                it.prepare()
            }*/

        CoroutineScope(Dispatchers.Main).launch {
            try {
                val mediaItems = fetchMediaItemsAsync()
                // Do something with the media items


            } catch (e: Exception) {
                // Handle exceptions
                e.printStackTrace()
            }
        }

    }

    suspend fun fetchMediaItemsAsync() = withContext(Dispatchers.IO) {
//        val mediaItemCount = mediaSession?.player?.mediaItemCount
//
//        // Retrieve each media item by index
//        val mediaItems = mutableListOf<MediaItem>()
//        for (index in 0 until mediaItemCount!!) {
//            val mediaItem = player.getMediaItemAt(index)
//            mediaItems.add(mediaItem)
//        }
//        mediaItems
    }

}