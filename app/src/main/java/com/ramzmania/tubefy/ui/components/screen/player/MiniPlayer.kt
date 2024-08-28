package com.ramzmania.tubefy.ui.components.screen.player

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.session.MediaController
import androidx.palette.graphics.Palette
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.ramzmania.tubefy.R
import com.ramzmania.tubefy.core.YoutubeCoreConstant
import com.ramzmania.tubefy.database.FavoritePlaylist
import com.ramzmania.tubefy.ui.components.NavigationItem
import com.ramzmania.tubefy.utils.LocalNavController
import com.ramzmania.tubefy.viewmodel.TubeFyViewModel
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Composable
fun MiniPlayerView(viewModel: TubeFyViewModel = hiltViewModel()) {
    var mediaController by remember { mutableStateOf<MediaController?>(null) }
    var albumArt by remember { mutableStateOf("") }
    var songName by remember { mutableStateOf("") }
    var isFavouriteSong =viewModel.isFavouriteState.collectAsState()
    var isPlaying by remember { mutableStateOf(false) }
    var videoId by remember { mutableStateOf("") }
    var palette by remember { mutableStateOf<Palette?>(null) }
    val newNav = LocalNavController.current

    LoadBitmapAndExtractPalette(albumArt) { extractedPalette ->
        palette = extractedPalette
    }
    MediaControllerComposable(
        onPlayingChanged = { playing ->
            isPlaying = playing
        },
        onMediaControllerInitialized = { controller ->
            mediaController = controller
            if(mediaController!=null&&mediaController?.currentMediaItem!=null) {
                isPlaying = mediaController?.isPlaying!!
                albumArt =
                    mediaController?.currentMediaItem?.mediaMetadata?.artworkUri?.toString()!!

                songName = mediaController?.currentMediaItem?.mediaMetadata?.title.toString()
                videoId=mediaController?.currentMediaItem?.mediaId!!
            }

        }, onMetaDataChangedValue = {

            if (it != null) {
                albumArt =
                    it.artworkUri?.toString()!!

                songName = it.title.toString()

            }
        }
    )
    val dominantColor = palette?.dominantSwatch?.rgb?.let { Color(it) } ?: Color.DarkGray
    Column {
        // Top card item
        Card(
            modifier = Modifier
//                .padding(8.dp)
                .height(50.dp)
                .fillMaxWidth()
                .clickable {
                    val encodedVideoId =
                        URLEncoder.encode(videoId, StandardCharsets.UTF_8.toString())
                    newNav!!.navigate(
                        NavigationItem.AudioPlayer.createRoute(
                            encodedVideoId,
                            URLEncoder.encode(albumArt, StandardCharsets.UTF_8.toString()),
                            URLEncoder.encode(songName, StandardCharsets.UTF_8.toString()),
                            URLEncoder.encode(songName, StandardCharsets.UTF_8.toString()),
                            URLEncoder.encode(songName, StandardCharsets.UTF_8.toString())
                        )
                    ) {
                        newNav.graph.route?.let { route ->
                            popUpTo(route) {
                                saveState = true
                            }
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
            elevation = CardDefaults.cardElevation(4.dp), // Use CardDefaults.cardElevation for elevation
            colors = CardDefaults.cardColors(containerColor = colorResource(id = R.color.colorPrimary)), // Set background color
            shape = RoundedCornerShape(8.dp) // Adjust the corner radius for a rounded effect
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .background(dominantColor),
            ) {
                // Left ImageView
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(YoutubeCoreConstant.decodeThumpUrl(albumArt))
                        .crossfade(true)
                        .placeholder(R.drawable.placeholder)
                        .error(R.drawable.placeholder)
                        .build(),
                    contentDescription = "Drawable Image",
                    modifier = Modifier
                        .height(50.dp)
                        .width(50.dp),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(6.dp))
                // Text overlapping the right image
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .align(Alignment.CenterVertically),// Ensures the Box takes up remaining space
                    contentAlignment = Alignment.CenterStart // Centers content within the Box
                ) {
                    Text(
                        text = songName,
                        fontSize = 16.sp,
                        color = Color.White,
                        maxLines = 1,
                        textAlign = TextAlign.Left, // Centers text within its own bounds
                        modifier = Modifier
                            .align(Alignment.TopStart) // Aligns the Text within the Box
                            .padding(4.dp)
                    )
                }
                // Right ImageViews with Box to manage padding
                Row(
                    modifier = Modifier
                        .fillMaxHeight()
                        .padding(end = 8.dp), // Padding for the entire Row
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // First Image with padding
                    Box(modifier = Modifier.padding(end = 4.dp)) {
                        Image(
                            painter = painterResource(id = if (isFavouriteSong.value==true) R.drawable.ic_unfav else R.drawable.ic_fav), // Replace with your image resource
                            contentDescription = null,
                            modifier = Modifier
                                .width(20.dp)
                                .height(50.dp)
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
                                                )!!, videoThump = albumArt, videoName = songName
                                            )
                                        )

                                    }

                                } // Ensure it matches the height of the Row
                        )
                    }
                    // Second Image
                    Image(
                        painter = painterResource(id = if (isPlaying) R.drawable.ic_player_pause else R.drawable.ic_player_play),
                        contentDescription = null,
                        modifier = Modifier
                            .width(20.dp)
                            .height(50.dp)
                            .clickable {
                                if (mediaController != null && mediaController!!.isConnected) {
                                    if (isPlaying) {
                                        mediaController?.pause()
                                    } else {
                                        mediaController?.play()
                                    }
                                }
                            } // Ensure it matches the height of the Row
                    )
                }
            }
        }
    }
}





@Preview(showBackground = true)
@Composable
fun BottomNavigationBarPreview() {
    MiniPlayerView()
}