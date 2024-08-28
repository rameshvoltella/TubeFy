package com.ramzmania.tubefy.ui.components.screen.library

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.ramzmania.tubefy.R
import com.ramzmania.tubefy.core.YoutubeCoreConstant
import com.ramzmania.tubefy.data.Resource
import com.ramzmania.tubefy.data.dto.base.searchformat.TubeFyCoreTypeData
import com.ramzmania.tubefy.ui.components.NavigationItem
import com.ramzmania.tubefy.utils.LocalNavController
import com.ramzmania.tubefy.viewmodel.TubeFyViewModel
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Composable
fun LibraryDetailPage(viewModel: TubeFyViewModel = hiltViewModel()) {
    var doLoadData by remember { mutableStateOf(true) }  // Track loading state
    val gettingPlayListSongs by viewModel.gettingSongPlayListOperation.observeAsState()
    var playListItems by rememberSaveable { mutableStateOf<List<TubeFyCoreTypeData?>>(emptyList()) }
    val navController = LocalNavController.current
    val navBackStackEntry = navController.currentBackStackEntry
    var plaListName by remember { mutableStateOf("Songs") }  // Track loading state

    LaunchedEffect(key1 = gettingPlayListSongs) {
        if (gettingPlayListSongs is Resource.Success) {
            doLoadData = false
            if (gettingPlayListSongs?.data != null && gettingPlayListSongs?.data!!.isNotEmpty()) {
                playListItems = gettingPlayListSongs?.data!!
            }else{
                playListItems= emptyList()
            }

        }
       else if (gettingPlayListSongs is Resource.DataError) {
            if (gettingPlayListSongs?.data != null && gettingPlayListSongs?.data!!.isNotEmpty()) {
                playListItems = gettingPlayListSongs?.data!!
            }else{
                playListItems= emptyList()
            }

        }

    }

    LaunchedEffect(key1 = Unit) {
        if (doLoadData) {
            plaListName = navBackStackEntry?.arguments?.getString("playerListName")!!
            if (plaListName == "TubeFy-Favorites") {
                viewModel.getFavorites()
            } else {
                viewModel.getSpecificPlayList(plaListName)
            }


        }

    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(id = R.color.colorPrimary))
    ) {
        Text(
            text = if (plaListName == "TubeFy-Favorites") "Favorites" else plaListName,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(10.dp, 30.dp, 10.dp, 10.dp),
            textAlign = TextAlign.Left,
            maxLines = 2,
            fontSize = 40.sp
        )
        if (playListItems != null && playListItems.isNotEmpty()) {
            LibraryListBaseView(playListItems)
        }
        else{
            Text(
                text = "No songs listed in this playlist",
                fontWeight = FontWeight.Medium,
                color = Color.White,
                modifier = Modifier.padding(10.dp, 30.dp, 10.dp, 10.dp),
                textAlign = TextAlign.Left,
                maxLines = 2,
                fontSize = 20.sp
            )
        }

    }

}

@Composable
fun LibraryListBaseView(
    playListItems: List<TubeFyCoreTypeData?>,
    viewModel: TubeFyViewModel = hiltViewModel()
) {
    val newNav = LocalNavController.current
    val gettingActivePlayList by viewModel.addToActiveDatabase.observeAsState()
    var doLoadData by remember { mutableStateOf(false) }  // Track loading state
    var clickedPosition by remember { mutableStateOf(0) }  // Track loading state
    var showDialog by remember { mutableStateOf(false) }
    var tubeFyCoreItem by remember { mutableStateOf(TubeFyCoreTypeData(videoId ="", videoImage = "", videoTitle = "")) }
    LaunchedEffect(key1 = gettingActivePlayList) {
        if (gettingActivePlayList is Resource.Success) {

            if (doLoadData) {
                doLoadData = false
                if (playListItems.isNotEmpty()) {
                    doLoadData = true
//                    viewModel.setActiveSongsList(playListItems)
                    val encodedVideoThumpUrl = URLEncoder.encode(
                        YoutubeCoreConstant.decodeThumpUrl(playListItems.get(clickedPosition)!!.videoImage),
                        StandardCharsets.UTF_8.toString()
                    )
                    val encodedVideoId =
                        URLEncoder.encode(
                            playListItems.get(clickedPosition)!!.videoId,
                            StandardCharsets.UTF_8.toString()
                        )
                    val videoTitle =
                        URLEncoder.encode(
                            playListItems.get(clickedPosition)!!.videoTitle,
                            StandardCharsets.UTF_8.toString()
                        )
//                LocalNavController.current
                    newNav!!.navigate(
                        NavigationItem.AudioPlayer.createRoute(
                            encodedVideoId,
                            encodedVideoThumpUrl, videoTitle, videoTitle, videoTitle, true
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
            }
        }
    }

    if (showDialog) {
        DeleteSongConfirmationDialog(
            onConfirm = {
                doLoadData=true
                deleteSongFromPlayList(viewModel,tubeFyCoreItem.playListName,tubeFyCoreItem.videoId)
                showDialog = false // Close the dialog after confirming
            },
            onDismiss = {
                showDialog = false // Close the dialog without confirming
            }
        )
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(id = R.color.colorPrimary))
    ) {

        Button(
            onClick = {

                if (playListItems.isNotEmpty()) {
                    doLoadData = true
                    clickedPosition = 0
                    viewModel.setActiveSongsList(playListItems)

                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),

            ) { Text(text = "Play All") }
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 16.dp) // Padding for the whole LazyColumn
        ) {

            if (playListItems != null) {
                itemsIndexed(playListItems!!) { index, playListData ->
//                    LibraryItem(trackName = playListData!!) {
//                        clickedPosition = index
//                        doLoadData = true
//                        viewModel.setActiveSongsList(playListItems, index)
//
//
//                    }
                    LibraryItem(
                        trackName = playListData!!,
                        onClick = { selectedTrack ->
                            clickedPosition = index
                            doLoadData = true
                            viewModel.setActiveSongsList(playListItems, index)
                        },
                        onDeleteClick = { selectedTrack ->
                            // Handle the delete action, e.g., showing a confirmation dialog
                            tubeFyCoreItem=selectedTrack
                            showDialog = true
                        }
                    )
                }
            }
            item {
                Spacer(modifier = Modifier.height(90.dp))
            }
        }
    }


}

@Composable
fun LibraryItem(
    viewModel: TubeFyViewModel = hiltViewModel(),
    trackName: TubeFyCoreTypeData,
    onClick: (TubeFyCoreTypeData) -> Unit,
    onDeleteClick: (TubeFyCoreTypeData) -> Unit
) {

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 8.dp)
            .clickable {

            }
    ) {
        Card(
            modifier = Modifier
//                .padding(8.dp)
                .fillMaxWidth()
                .clickable { onClick(trackName!!) },
            elevation = CardDefaults.cardElevation(4.dp), // Use CardDefaults.cardElevation for elevation
            colors = CardDefaults.cardColors(containerColor = Color.DarkGray), // Set background color
            shape = RoundedCornerShape(8.dp) // Adjust the corner radius for a rounded effect
        ) {
            Row(
                modifier = Modifier
//                .padding(8.dp)
                    .fillMaxWidth()
            ) {

                trackName.videoImage?.let { thumbnailUrl ->
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(YoutubeCoreConstant.decodeThumpUrl(thumbnailUrl))
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
                } ?: AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(R.drawable.images)
                        .crossfade(true)
                        .placeholder(R.drawable.placeholder)
                        .error(R.drawable.placeholder)
                        .build(),
                    contentDescription = "Placeholder Image",
                    modifier = Modifier
                        .height(50.dp)
                        .width(50.dp),
                    contentScale = ContentScale.Crop
                )
                // Spacer to push the text and right image apart
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = trackName.videoTitle!!,
                    fontWeight = FontWeight.Thin,
                    color = Color.White,
                    modifier = Modifier
                        .align(Alignment.CenterVertically)
                        .fillMaxWidth()
                        .padding(horizontal = 5.dp)
                        .weight(2f),
                    textAlign = TextAlign.Left,
                    maxLines = 1,
                    fontSize = 16.sp
                )
                // Spacer to push the text and right image apart
                Spacer(modifier = Modifier.width(6.dp))
                Image(
                    painter = painterResource(id = R.drawable.ic_delete),
                    contentDescription = "Right Image",
                    modifier = Modifier
                        .height(30.dp)
                        .width(30.dp)
                        .align(Alignment.CenterVertically)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                        .clickable {
                            onDeleteClick(trackName)
                        },
                    contentScale = ContentScale.Crop
                )

            }
        }
//        Text(text = trackName.videoTitle,  color = Color.Black)
//        Divider(modifier = Modifier.padding(vertical = 4.dp), color = Color.Gray)
    }
}


@Composable
fun DeleteSongConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "Delete Song")
        },
        text = {
            Text(text = "Are you sure you want to delete this song from the playlist?")
        },
        confirmButton = {
            Button(
                onClick = onConfirm
            ) {
                Text("Yes")
            }
        },
        dismissButton = {
            Button(
                onClick = onDismiss
            ) {
                Text("No")
            }
        }
    )
}

fun deleteSongFromPlayList(viewModel: TubeFyViewModel, playListName: String, videoId: String) {
    if (playListName == "TubeFy-Favorites") {
        viewModel.removeFromFavorites(videoId)
        viewModel.getFavorites()
    } else {
        viewModel.deleteSongFromPlayList(playListName = playListName, videoId = videoId)
        viewModel.getSpecificPlayList(playListName)
    }

}

@Preview(showBackground = true)
@Composable
fun preview() {
//    LibraryListBaseViewiew(listOf(TubeFyCoreTypeData("sndfhiew","sdjfvjdnfv","https://www.jjj.com")))
}