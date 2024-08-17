package com.ramzmania.tubefy.ui.components.screen.player


import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaController
import androidx.media3.session.MediaLibraryService
import androidx.media3.session.MediaSession
import com.ramzmania.tubefy.core.YoutubeCoreConstant
import com.ramzmania.tubefy.data.Resource
import com.ramzmania.tubefy.data.local.LocalRepositorySource
import com.ramzmania.tubefy.data.remote.RemoteRepositorySource
import com.ramzmania.tubefy.player.PlayListSingleton
import com.ramzmania.tubefy.player.YoutubePlayerPlaylistListModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.schabi.newpipe.extractor.timeago.patterns.it
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
                    handlePlaybackError(error, currentMediaItem)
                }
            })
        }

        mediaLibrarySessionCallback = MediaLibrarySessionCallback()
        mediaSession =
            MediaLibrarySession.Builder(this, player, mediaLibrarySessionCallback).build()
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
                    it.extras?.getString("videoId")?.let { it1 -> getStreamUrl(it1,
                        it.extras?.getString("albumart")!!, it.extras?.getString("title")!!
                    ) }
                }

                else -> {}
            }
        }
        return START_STICKY
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

    fun fetchPlayList() {
        serviceScope.launch {
            Log.d("bulkmode", "new array" + PlayListSingleton.getDataList()?.playListData?.size)
            if (PlayListSingleton.getDataList()?.playListData?.size!! > 0) {
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

    fun getStreamUrl(videoId: String, albumArt: String, playerHeader: String) {
        serviceScope.launch {
            remoteRepositorySource.getStreamUrl(
                YoutubeCoreConstant.extractYoutubeVideoId(
                    videoId
                )!!
            ).collect { response ->
                if (response is Resource.Success) {
                    withContext(Dispatchers.Main) {
                        mediaSession?.player?.let {
                            fun mediaItemData() = MediaItem.Builder()
                                .setMediaId("TubeFy")
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

                            it.setMediaItem(mediaItemData())
                            it.playWhenReady = true
                            it.prepare()
                        }
                    }
                }
            }
        }
    }

    fun playAudioList(mediaItems: List<MediaItem>) {
        mediaSession?.player?.let {
            Log.d("click", "connected")

            if (it.currentMediaItem == null) {
                removeExistingPlayList = false
                it.setMediaItems(mediaItems)
                it.playWhenReady = true
                it.prepare()
            } else {
                val mutableMediaItems = mediaItems.toMutableList()
                if (removeExistingPlayList) {
                    removeExistingPlayList = false
                    it.setMediaItems(mutableMediaItems)

                } else {
                    it.addMediaItems(mutableMediaItems)

                }
            }
        }
    }

    /*    fun fetchPlayList()
        {
            serviceScope.launch {
                Log.d("bulkmode","new arra"+ PlayListSingleton.getDataList()?.playListData?.size)
                var list= PlayListSingleton.getDataList()?.playListData?.take(2)
                remoteRepositorySource.getStreamBulkUrl(YoutubePlayerPlaylistListModel(list!!)).collect {
                    if (it is Resource.Success) {
                        playAudioList(it.data!!)
                        var list= PlayListSingleton.getDataList()?.playListData?.drop(2)
                        PlayListSingleton.addData(list!!)
                        fetchPlayList()
                    }
                }
            }
        }

        fun playAudioList(
            mediaItems: List<MediaItem>
        ) {
            mediaSession?.player?.let {

                    Log.d("click", "connected")

                    // Set the media items to the MediaController
                    if(it.currentMediaItem==null) {
                        mediaSession?.player?.setMediaItems(mediaItems)
                        it.playWhenReady = true
                        it.prepare()
                    }else
                    {
                        val mutableMediaItems = mediaItems.toMutableList()
    //                mutableMediaItems.removeAt(0)
                        if(removedExistingPlayList) {
                            it.addMediaItems(mutableMediaItems)
                        }else
                        {
                            it.setMediaItems(mutableMediaItems)
                            removedExistingPlayList=false
                        }

                    }
                }

        }*/
    companion object {
        const val ACTION_FETCH_PLAYLIST = "com.ramzmania.tubefy.ACTION_FETCH_PLAYLIST"
        const val ACTION_FETCH_SONG = "com.ramzmania.tubefy.ACTION_FETCH_SONG"

    }
}