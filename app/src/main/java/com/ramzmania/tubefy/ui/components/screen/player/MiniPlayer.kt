package com.ramzmania.tubefy.ui.components.screen.player

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.layout.size
import com.ramzmania.tubefy.ui.theme.DiscoverAccent
import com.ramzmania.tubefy.ui.theme.DiscoverBorder
import com.ramzmania.tubefy.ui.theme.DiscoverSurface
import com.ramzmania.tubefy.ui.theme.DiscoverTextSecondary
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpSize
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
    var progress by remember { mutableStateOf(0f) }  // Track progress

    val newNav = LocalNavController.current
    LaunchedEffect(mediaController?.isPlaying) {
        while (true) {
            if (mediaController?.isConnected == true && isPlaying) {
                progress =
                    (mediaController!!.currentPosition * 1.0f / mediaController!!.duration).coerceIn(
                        0f,
                        1f
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
                viewModel.isFavourite(YoutubeCoreConstant.extractYoutubeVideoId(videoId)!!)

            }

        }, onMetaDataChangedValue = {

            if (it != null) {
                albumArt =
                    it.artworkUri?.toString()!!

                songName = it.title.toString()
                videoId=mediaController?.currentMediaItem?.mediaId!!
                viewModel.isFavourite(YoutubeCoreConstant.extractYoutubeVideoId(videoId)!!)


            }
        }
    )
    fun openPlayer() {
        val encodedVideoId = URLEncoder.encode(videoId, StandardCharsets.UTF_8.toString())
        newNav.navigate(
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
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .padding(horizontal = 10.dp, vertical = 6.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(DiscoverSurface)
            .border(1.dp, DiscoverBorder, RoundedCornerShape(22.dp))
            .clickable { openPlayer() }
            .padding(horizontal = 10.dp, vertical = 7.dp)
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(YoutubeCoreConstant.decodeThumpUrl(albumArt))
                .crossfade(true)
                .placeholder(R.drawable.placeholder)
                .error(R.drawable.placeholder)
                .build(),
            contentDescription = songName,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(9.dp))
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 14.dp)
        ) {
            Text(
                text = songName,
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = if (isPlaying) "Now playing" else "Paused",
                color = DiscoverTextSecondary,
                fontSize = 13.sp,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(5.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .clip(RoundedCornerShape(50))
                    .background(Color.White.copy(alpha = 0.15f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress)
                        .fillMaxHeight()
                        .background(DiscoverAccent)
                )
            }
        }
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .border(1.5.dp, DiscoverAccent, CircleShape)
                .clickable {
                    mediaController?.takeIf { it.isConnected }?.let {
                        if (isPlaying) it.pause() else it.play()
                    }
                }
        ) {
            Icon(
                imageVector = if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                contentDescription = if (isPlaying) "Pause" else "Play",
                tint = Color.White,
                modifier = Modifier.size(22.dp)
            )
        }
        IconButton(onClick = { mediaController?.takeIf { it.hasNextMediaItem() }?.seekToNextMediaItem() }) {
            Icon(Icons.Rounded.SkipNext, contentDescription = "Next", tint = Color.White, modifier = Modifier.size(26.dp))
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MiniSeekBar(progress: Float) {
    // Create an interaction source that is never interacted with
    val interactionSource = remember { MutableInteractionSource() }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(4.dp) // Set the height of the track without the extra padding
    ) {
        Slider(
            value = progress * 100f,
            thumb = {
                SliderDefaults.Thumb( //androidx.compose.material3.SliderDefaults
                    interactionSource = interactionSource,
                    thumbSize = DpSize(0.dp,0.dp)
                )
            },
            onValueChange = { newValue ->

            },
            colors = SliderDefaults.colors(
                activeTrackColor = Color.White,
                inactiveTrackColor = Color.Gray,
                thumbColor = Color.Red
            ),
            interactionSource = interactionSource,
            enabled = true, // Disables manual seeking,
            valueRange = 0f..100f,
            modifier = Modifier
                .fillMaxWidth()


        )
    }
}


@Preview(showBackground = true)
@Composable
fun BottomNavigationBarPreview() {
    MiniPlayerView()
}