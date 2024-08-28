package com.ramzmania.tubefy.ui.components.screen.player

import VideoPlayerView
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.annotation.OptIn
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.ramzmania.tubefy.R
import com.ramzmania.tubefy.core.YoutubeCoreConstant
import com.ramzmania.tubefy.data.dto.base.searchformat.TubeFyCoreTypeData
import com.ramzmania.tubefy.database.CustomPlaylist
import com.ramzmania.tubefy.database.FavoritePlaylist
import com.ramzmania.tubefy.player.PlaybackService
import com.ramzmania.tubefy.ui.components.screen.library.PlayListDialogViewer
import com.ramzmania.tubefy.utils.LocalNavController
import com.ramzmania.tubefy.viewmodel.TubeFyViewModel
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

@kotlin.OptIn(ExperimentalFoundationApi::class)
@Composable
fun PlayerBaseView(viewModel: TubeFyViewModel= hiltViewModel()
) {
    var isLoading by remember { mutableStateOf(true) }  // Track loading state
    var mediaController by remember { mutableStateOf<MediaController?>(null) }
    var isPlaying by remember { mutableStateOf(false) }
    var progress by remember { mutableStateOf(0f) }  // Track progress
    val navController = LocalNavController.current
    val context = LocalContext.current
    var currentTime by remember { mutableStateOf("00:00") }
    var totalTime by remember { mutableStateOf("00:00") }
    var playerHeader by remember { mutableStateOf("") }
    var playerBottomHeader by remember { mutableStateOf("") }
    var playerBottomSub by remember { mutableStateOf("") }
    var albumArt by remember { mutableStateOf("") }
    var videoId by remember { mutableStateOf("") }
    var isBulk by remember { mutableStateOf("false") }  // Track loading state
    var videoUrl by remember { mutableStateOf("") }
    var showVideoPlayer by remember { mutableStateOf(false) }
    var showPlaListDialog by remember {
        mutableStateOf(false)
    }

    var isFavouriteSong =viewModel.isFavouriteState.collectAsState()
    val navBackStackEntry = navController.currentBackStackEntry


    LaunchedEffect(Unit) {
        isBulk = navBackStackEntry?.arguments?.getString("isBulk")!!
        Log.d("isBulk", "isBulk>>>>" + isBulk)
        videoId = URLDecoder.decode(
            navBackStackEntry?.arguments?.getString("videoId"),
            StandardCharsets.UTF_8.toString()
        )
        albumArt = "https://i.ytimg.com/vi/${
            YoutubeCoreConstant.extractYoutubeVideoId(
                URLDecoder.decode(
                    navBackStackEntry?.arguments?.getString("videoId"),
                    StandardCharsets.UTF_8.toString()
                )
            )
        }/hq720.jpg"

        playerHeader = URLDecoder.decode(
            navBackStackEntry?.arguments?.getString("playerHeader"),
            StandardCharsets.UTF_8.toString()
        )
        playerBottomHeader = URLDecoder.decode(
            navBackStackEntry?.arguments?.getString("playerBottomHeader"),
            StandardCharsets.UTF_8.toString()
        )
        playerBottomSub = URLDecoder.decode(
            navBackStackEntry?.arguments?.getString("playerBottomSub"),
            StandardCharsets.UTF_8.toString()
        )

        viewModel.isFavourite(YoutubeCoreConstant.extractYoutubeVideoId(videoId)!!)


    }

    LaunchedEffect(mediaController?.isPlaying) {
        Log.d("corortine", "loading called" + isLoading)
        while (true) {
            Log.d("timer", "yessssss")
            if (mediaController?.isConnected == true && isPlaying) {
                progress =
                    (mediaController!!.currentPosition * 1.0f / mediaController!!.duration).coerceIn(
                        0f,
                        1f
                    )
//                currentTime="%02d:%02d:%02d".format(milliSec / 1000 / 60 / 60, milliSec / 1000 / 60 % 60, milliSec / 1000 % 60)
                currentTime =
                    if (mediaController!!.duration >= 3600000) "%02d:%02d:%02d".format(
                        mediaController!!.currentPosition / 1000 / 60 / 60,
                        mediaController!!.currentPosition / 1000 / 60 % 60,
                        mediaController!!.currentPosition / 1000 % 60
                    ) else "%02d:%02d".format(
                        mediaController!!.currentPosition / 1000 / 60 % 60,
                        mediaController!!.currentPosition / 1000 % 60
                    )

                // Update progress every second
                kotlinx.coroutines.delay(1000L)
            } else {
                // Stop updating if not playing
                kotlinx.coroutines.delay(1000L)
//                    break
                if (mediaController?.isPlaying != true) {
                    break
                }
            }

        }
    }

    MediaControllerComposable(
        onPlayingChanged = { playing ->
            isPlaying = playing
            if (playing) {
                isLoading = false
            }
            totalTime = if (mediaController!!.duration >= 3600000) "%02d:%02d:%02d".format(
                mediaController!!.duration / 1000 / 60 / 60,
                mediaController!!.duration / 1000 / 60 % 60,
                mediaController!!.duration / 1000 % 60
            ) else "%02d:%02d".format(
                mediaController!!.duration / 1000 / 60 % 60,
                mediaController!!.duration / 1000 % 60
            )
//            totalTime="%02d:%02d:%02d".format(milliSec / 1000 / 60 / 60, milliSec / 1000 / 60 % 60, milliSec / 1000 % 60)
            // Update progress every second
        },
        onMediaControllerInitialized = { controller ->
            mediaController = controller
            isPlaying = mediaController!!.isPlaying
            if (!isBulk.equals("true")) {
                if (mediaController!!.currentMediaItem != null && mediaController!!.currentMediaItem?.mediaMetadata?.title!!.equals(
                        playerHeader
                    )
                ) {
                    isLoading = false
                } else {
                    isLoading = true


                }
            }
            if (isBulk.equals("true")) {
//            viewModel.getStreamUrl(videoId!!)
//                viewModel.getBulkStreamUrl()
                startFetchPlayListService(context)
            } else {

                var loadService=true
                if(mediaController!=null&&mediaController!!.isConnected&&mediaController!!.currentMediaItem!=null&&mediaController!!.currentMediaItem?.mediaId.equals(YoutubeCoreConstant.extractYoutubeVideoId(videoId)))
                {
                    loadService=false
                }

                if(loadService)
                {
                    startFetchSongService(context, videoId, albumArt, playerHeader!!)

                }else{
                    totalTime = if (mediaController!!.duration >= 3600000) "%02d:%02d:%02d".format(
                        mediaController!!.duration / 1000 / 60 / 60,
                        mediaController!!.duration / 1000 / 60 % 60,
                        mediaController!!.duration / 1000 % 60
                    ) else "%02d:%02d".format(
                        mediaController!!.duration / 1000 / 60 % 60,
                        mediaController!!.duration / 1000 % 60
                    )
                }
//                viewModel.getStreamUrl(videoId!!)
            }

        }, onMetaDataChangedValue = {

            if (it != null) {
                albumArt =
                    it.artworkUri?.toString()!!
                videoId=mediaController?.currentMediaItem?.mediaId!!

                playerHeader = it.title.toString()

                playerBottomHeader = it.title.toString()
                playerBottomSub = it.title.toString()
                Log.d(
                    "album",
                    albumArt + "<>" + it.artworkUri?.path + "<>" + it.artworkUri?.scheme + "<>" + it.artworkUri?.host
                )
            }
        }
    )
    if (showPlaListDialog) {
        if(mediaController?.currentMediaItem!=null) {
            PlayListDialogViewer(
                viewModel = viewModel,
                onDismiss = { showPlaListDialog = false },
                TubeFyCoreTypeData(
                    videoId = mediaController?.currentMediaItem!!.mediaId,
                    videoImage = albumArt,
                    videoTitle = playerHeader
                )
                // Reset the state when the dialog is dismissed
            )
        }else
        {
            showPlaListDialog=false
        }
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(id = R.color.colorPrimary))
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Playing Now",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp, start = 10.dp, end = 10.dp),
                fontWeight = FontWeight.Thin,
                color = Color.White,
                textAlign = TextAlign.Center,
                maxLines = 2,
                fontSize = 16.sp
            )
            Text(
                text = playerHeader,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 2.dp, start = 10.dp, end = 10.dp),
                fontWeight = FontWeight.Medium,
                color = Color.White,
                textAlign = TextAlign.Center,
                maxLines = 2,
                fontSize = 16.sp
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 30.dp, vertical = 10.dp)
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(YoutubeCoreConstant.decodeThumpUrl(albumArt))
                        .crossfade(true)
                        .placeholder(R.drawable.placeholder)
                        .error(R.drawable.placeholder)
                        .build(),
                    contentDescription = "Drawable Image",
                    modifier = Modifier
                        .fillMaxSize() // Match the size of the Box
                        .clip(RoundedCornerShape(16.dp))
                        .clickable {
                            mediaController?.pause()
                            val currentMediaItem = mediaController?.currentMediaItem
//                             currentMediaItem?.playbackProperties?.uri?.toString()
                            videoUrl =
                                currentMediaItem?.localConfiguration?.uri
                                    ?.toString()
                                    .toString() // Replace with actual video URL
                            showVideoPlayer = true
                        },
                    contentScale = ContentScale.Crop
                )
                if (showVideoPlayer) {
                    VideoPlayerView(videoUrl)
                }
            }
            /* AsyncImage(
                 model = ImageRequest.Builder(context)
                     .data(YoutubeCoreConstant.decodeThumpUrl(albumArt))
                     .crossfade(true)
                     .placeholder(R.drawable.placeholder)
                     .error(R.drawable.placeholder)
                     .build(),
                 contentDescription = "Drawable Image",
                 modifier = Modifier
                     .padding(horizontal = 30.dp, vertical = 10.dp)
                     .fillMaxWidth()
                     .fillMaxHeight()
                     .align(Alignment.CenterHorizontally)
                     .clip(RoundedCornerShape(16.dp)), // Add this line to apply rounded corners,
                 contentScale = ContentScale.Crop
             )*/
        }

        Spacer(modifier = Modifier.height(8.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = playerBottomHeader,
                fontSize = 20.sp,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                maxLines = 2,
                minLines = 2,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = playerBottomSub.replace("\n", ""),
                fontSize = 14.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .basicMarquee(),

                overflow = TextOverflow.Ellipsis,

                )
            Spacer(modifier = Modifier.height(16.dp))
            Slider(
                value = progress * 100f,
                onValueChange = { newValue ->
                    progress = newValue / 100f
                    mediaController?.seekTo((progress * mediaController?.duration!!).toLong())
                },
                colors = SliderDefaults.colors(
                    activeTrackColor = Color.White,
                    inactiveTrackColor = Color.Gray,
                    thumbColor = Color.Red
                ),
                valueRange = 0f..100f,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)

            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = currentTime, color = Color.Gray, fontSize = 12.sp)
                Text(text = totalTime, color = Color.Gray, fontSize = 12.sp)
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_add_playlist),
                    contentDescription = "Previous",
                    modifier = Modifier
                        .size(30.dp)
                        .clickable { showPlaListDialog = true }

                )
                Row(
                    modifier = Modifier
                        .wrapContentWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_player_previous),
                        contentDescription = "Previous",
                        modifier = Modifier
                            .size(30.dp)
                            .clickable {
                                mediaController?.let {
                                    if (it.hasPreviousMediaItem()) {
                                        it.seekToPreviousMediaItem()
                                    } else {
                                        Log.d("PlaybackService", "No prev media item available")
                                    }
                                } ?: Log.d("PlaybackService", "MediaController is null")
                            }

                    )
                    Spacer(modifier = Modifier.width(40.dp))
                    if (isLoading) {
                        CircularProgressIndicator(
                            color = colorResource(id = R.color.tubefyred),
                            modifier = Modifier.size(30.dp)
                        )
                    } else {

                        Image(
                            painter = painterResource(id = if (isPlaying) R.drawable.ic_player_pause else R.drawable.ic_player_play),
                            contentDescription = "Play/Pause",
                            modifier = Modifier
                                .size(30.dp)
                                .clickable {

                                    if (mediaController != null && mediaController!!.isConnected) {
                                        if (isPlaying) {
                                            mediaController?.pause()
                                        } else {
                                            mediaController?.play()
                                        }
                                    }
                                    if (showVideoPlayer) {
                                        showVideoPlayer = false
                                        Log.d("loading", "<<<<" + isLoading + "<>")
                                    }
                                }

                        )
                    }
                    Spacer(modifier = Modifier.width(40.dp))
                    Image(
                        painter = painterResource(id = R.drawable.ic_player_next),
                        contentDescription = "Next",
                        modifier = Modifier
                            .size(30.dp)
                            .clickable {
                                mediaController?.let {
                                    if (it.hasNextMediaItem()) {
                                        it.seekToNextMediaItem()
                                    } else {
                                        Log.d("PlaybackService", "No next media item available")
                                    }
                                } ?: Log.d("PlaybackService", "MediaController is null")
                            }
                    )
                }
                Image(
                    painter = painterResource(id = if (isFavouriteSong.value==true) R.drawable.ic_unfav else R.drawable.ic_fav),
                    contentDescription = "Previous",
                    modifier = Modifier
                        .size(30.dp)
                        .clickable {
                            if (isFavouriteSong.value) {
                                viewModel.removeFromFavorites(
                                    YoutubeCoreConstant.extractYoutubeVideoId(
                                        videoId
                                    )!!
                                )
                            } else {
                                viewModel.addToFavorites(
                                    FavoritePlaylist(
                                        videoId = YoutubeCoreConstant.extractYoutubeVideoId(
                                            videoId
                                        )!!, videoThump = albumArt, videoName = playerHeader
                                    )
                                )

                            }

                        }

                )
            }
            /*   Row(
                   modifier = Modifier
                       .fillMaxWidth()
                       .align(Alignment.CenterHorizontally),
                   horizontalArrangement = Arrangement.Center
               ) {
                   Image(
                       painter = painterResource(id = R.drawable.ic_player_previous),
                       contentDescription = "Previous",
                       modifier = Modifier
                           .size(30.dp)
                           .clickable {
                               mediaController?.let {
                                   if (it.hasPreviousMediaItem()) {
                                       it.seekToPreviousMediaItem()
                                   } else {
                                       Log.d("PlaybackService", "No prev media item available")
                                   }
                               } ?: Log.d("PlaybackService", "MediaController is null")
                           }
                   )
                   Spacer(modifier = Modifier.width(40.dp))
                   if (isLoading) {
                       CircularProgressIndicator(
                           color = colorResource(id = R.color.tubefyred),
                           modifier = Modifier.size(30.dp)
                       )
                   } else {

                       Image(
                           painter = painterResource(id = if (isPlaying) R.drawable.ic_player_pause else R.drawable.ic_player_play),
                           contentDescription = "Play/Pause",
                           modifier = Modifier
                               .size(30.dp)
                               .clickable {

                                   if (mediaController != null && mediaController!!.isConnected) {
                                       if (isPlaying) {
                                           mediaController?.pause()
                                       } else {
                                           mediaController?.play()
                                       }
                                   }
                                   if(showVideoPlayer)
                                   {
                                       showVideoPlayer=false
                                       Log.d("loading","<<<<"+isLoading+"<>")
                                   }
                               }
                       )
                   }
                   Spacer(modifier = Modifier.width(40.dp))
                   Image(
                       painter = painterResource(id = R.drawable.ic_player_next),
                       contentDescription = "Next",
                       modifier = Modifier.size(30.dp).clickable { mediaController?.let {
                           if (it.hasNextMediaItem()) {
                               it.seekToNextMediaItem()
                           } else {
                               Log.d("PlaybackService", "No next media item available")
                           }
                       } ?: Log.d("PlaybackService", "MediaController is null") }
                   )
               }*/
        }
    }
}


@Preview(showBackground = true)
@Composable
fun PlayerPreview() {
    PlayerBaseView()
}


@OptIn(UnstableApi::class)
fun startFetchPlayListService(context: Context) {
    val intent = Intent(context, PlaybackService::class.java).apply {
        action = PlaybackService.ACTION_FETCH_PLAYLIST
    }
    context.startService(intent)
}

@OptIn(UnstableApi::class)
fun startFetchSongService(context: Context, videoId: String, albumArt: String, title: String) {
    val intent = Intent(context, PlaybackService::class.java).apply {
        action = PlaybackService.ACTION_FETCH_SONG
        putExtra(PlaybackService.VIDEO_ID, videoId)
        putExtra(PlaybackService.ALBUM_ART, albumArt)
        putExtra(PlaybackService.VIDEO_TITLE, title)
    }
    context.startService(intent)
}






