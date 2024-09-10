package com.ramzmania.tubefy.ui.components.screen.library

import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.Composable

import androidx.compose.foundation.Image
import androidx.compose.foundation.background

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.ramzmania.tubefy.R
import com.ramzmania.tubefy.core.YoutubeCoreConstant
import com.ramzmania.tubefy.data.Resource
import com.ramzmania.tubefy.data.dto.base.searchformat.TubeFyCoreTypeData
import com.ramzmania.tubefy.data.dto.database.PlaylistNameWithUrl
import com.ramzmania.tubefy.database.CustomPlaylist
import com.ramzmania.tubefy.database.FavoritePlaylist
import com.ramzmania.tubefy.viewmodel.TubeFyViewModel


@Composable
fun PlaylistDialog(currentSongDetails: TubeFyCoreTypeData,viewModel: TubeFyViewModel = hiltViewModel(),
    playlists: List<PlaylistNameWithUrl?>,
    onDismiss: () -> Unit,
    onItemClick: (PlaylistNameWithUrl?) -> Unit
) {
    var showPlayListEditText by remember { mutableStateOf(false) }
    val textFieldValueSaver = Saver<TextFieldValue, String>(
        save = { it.text },  // Save only the text part
        restore = { TextFieldValue(it) }  // Restore TextFieldValue from the text
    )
    val context= LocalContext.current

    // Use rememberSaveable with the custom Saver
    var textFieldValue by rememberSaveable(stateSaver = textFieldValueSaver) {
        mutableStateOf(TextFieldValue())
    }
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .background(color = colorResource(id = R.color.colorPrimary)),
            shape = RoundedCornerShape(8.dp)
        ) {
            Column(modifier = Modifier.background(color = colorResource(id = R.color.colorPrimary))) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .background(color = colorResource(id = R.color.colorPrimary))
                ) {
                    if (!showPlayListEditText) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(color = colorResource(id = R.color.colorPrimary)),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Text on the left
                            Text(
                                text = "Playlist",
                                modifier = Modifier.weight(1f),
                                color = colorResource(id = R.color.white),
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp
                            )

                            // Spacer to push the image to the right
                            Spacer(modifier = Modifier.width(8.dp))
                            Image(
                                painter = painterResource(id = R.drawable.ic_add_playlist),
                                contentDescription = "Right Image",
                                modifier = Modifier
                                    .height(50.dp)
                                    .width(50.dp)
                                    .padding(horizontal = 5.dp, vertical = 10.dp)
                                    .clickable { showPlayListEditText = true },
                                contentScale = ContentScale.Crop
                            )


                        }
                    } else {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(color = colorResource(id = R.color.colorPrimary)),
                            verticalAlignment = Alignment.CenterVertically
                        ) {

                            TextField(
                                modifier = Modifier.weight(1f),
                                value = textFieldValue,
                                placeholder = { Text("Add new playlist") },
                                onValueChange = { newValue -> textFieldValue = newValue },
                                keyboardOptions = KeyboardOptions.Default.copy(
                                    imeAction = ImeAction.Done
                                ),
                                keyboardActions = KeyboardActions(onDone = {

                                    if (!(playlists.any { it?.playlistName == textFieldValue.text })) {
                                        // Handle text input
                                        showPlayListEditText=false
                                        addToPlayList(
                                            currentSongDetails,
                                            viewModel,
                                            textFieldValue.text
                                        )
                                        viewModel.dismissPlaListDialog(true)
                                    } else {
                                        Toast.makeText(
                                            context,
                                            "Playlist Already Available",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }

                                })
                            )

                            // Spacer to push the image to the right
                            Spacer(modifier = Modifier.width(8.dp))

                            Image(
                                painter = painterResource(id = R.drawable.ic_remove),
                                contentDescription = "Right Image 1",
                                modifier = Modifier
                                    .height(30.dp)
                                    .width(30.dp)
                                    .padding(horizontal = 2.dp, vertical = 2.dp)
                                    .clickable { showPlayListEditText = false },
                                contentScale = ContentScale.Crop
                            )

                            // Second Image on the right
                            Image(
                                painter = painterResource(id = R.drawable.ic_correct), // Replace with the appropriate drawable resource
                                contentDescription = "Right Image 2",
                                modifier = Modifier
                                    .height(30.dp)
                                    .width(30.dp)
                                    .padding(horizontal = 2.dp, vertical = 2.dp)
                                    .clickable {
                                        if(!( playlists.any { it?.playlistName == textFieldValue.text})) {
                                            showPlayListEditText=false
                                            // Handle text input
                                            addToPlayList(
                                                currentSongDetails,
                                                viewModel,
                                                textFieldValue.text
                                            )
                                            viewModel.dismissPlaListDialog(true)
                                        }else
                                        {
                                            Toast.makeText(context,"Playlist Already Available",Toast.LENGTH_LONG).show()
                                        }

                                     /*   showPlayListEditText = false
                                        addToPlayList(
                                            currentSongDetails,
                                            viewModel,
                                            textFieldValue.text
                                        )
                                        viewModel.dismissPlaListDialog(true)*/
                                    },
                                contentScale = ContentScale.Crop
                            )

                        }
                    }
                    // Top Text


                    // List of Playlists
                    Column {
                        playlists.forEach { playlist ->
                            Card(
                                shape = RoundedCornerShape(8.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.DarkGray),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp)
                                    .clickable { onItemClick(playlist) }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Thumbnail Image on the Left
                                    if(playlist?.videoThump.equals("Favorites"))
                                    {
                                        Image(
                                            painter = painterResource(id = R.drawable.fav_playlist),
                                            contentDescription = "Right Image",
                                            modifier = Modifier
                                                .height(50.dp)
                                                .width(50.dp),
                                            contentScale = ContentScale.Crop
                                        )

                                    }else {
                                        AsyncImage(
                                            model = ImageRequest.Builder(LocalContext.current)
                                                .data(YoutubeCoreConstant.decodeThumpUrl(playlist?.videoThump!!))
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
                                    }

                                    Spacer(modifier = Modifier.width(16.dp))

                                    // Playlist Name on the Right
                                    Text(
                                        text = if (playlist?.playlistName!! == "TubeFy-Favorites") "Favorite" else playlist.playlistName!!,
//                                        text = playlist?.playlistName!!,
                                        color = colorResource(id = R.color.white),
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PlayListDialogViewer(viewModel: TubeFyViewModel = hiltViewModel(),onDismiss: () -> Unit ,currentSongDetails: TubeFyCoreTypeData) {
    viewModel.dismissPlaListDialog(false)
    var showDialog by remember { mutableStateOf(true) }
    val gettingMyPlayList by viewModel.gettingPrivatePlayList.observeAsState()
    var playListItems by rememberSaveable { mutableStateOf<List<PlaylistNameWithUrl?>>(emptyList()) }
//    var doLoadData by remember { mutableStateOf(true) }  // Track loading state
    var dismissPlayListDialog=viewModel.dismissPlayListDialog.collectAsState()
//    var reloadAllPlayList=viewModel.reloadAllPlayList.collectAsState()

    LaunchedEffect(key1 = gettingMyPlayList) {
        if (gettingMyPlayList is Resource.Success) {

            if (gettingMyPlayList?.data != null && gettingMyPlayList?.data!!.isNotEmpty()) {
                playListItems = gettingMyPlayList?.data!!

            }

        }
        else if (gettingMyPlayList is Resource.DataError) {
        }
    }

    LaunchedEffect(key1 = Unit) {
            viewModel.getAllSavedPlayList()


    }
    /*if(reloadAllPlayList.value)
    {
        Log.d("homedaaa2222","yoda")

        viewModel.getAllSavedPlayList()
//        doLoadData=true
//        viewModel.reloadAllCustomPlayListData(false)

    }*/
   /* val playlists = listOf(
        PlaylistNameWithUrl("Playlist 1", "https://placekitten.com/64/64"),
        PlaylistNameWithUrl("Playlist 2", "https://placekitten.com/64/64"),
        PlaylistNameWithUrl("Playlist 3", "https://placekitten.com/64/64")
    )

    Button(onClick = { showDialog = true }) {
        Text(text = "Show Playlist Dialog")
    }
*/
    if (showDialog) {
        PlaylistDialog(
            currentSongDetails=currentSongDetails,
            playlists = playListItems,
            onDismiss = {
                showDialog = false
                onDismiss() // Notify parent to reset the state
            },
            onItemClick = { playlist ->
                // Handle item click
                addToPlayList(currentSongDetails,viewModel,playlist?.playlistName!!)
                showDialog = false
                onDismiss() // Notify parent to reset the state

            }
        )
    }

    if(dismissPlayListDialog.value)
    {
        showDialog = false
        onDismiss()
    }
}

fun addToPlayList(
    currentSongDetails: TubeFyCoreTypeData,
    viewModel: TubeFyViewModel,
    playlistName: String
) {

//    Log.d("kolpo","added"+playlistName+"<><"+currentSongDetails.videoId+"<><"+currentSongDetails.videoTitle+"<><>"+currentSongDetails.videoImage)
    if(!playlistName.equals("TubeFy-Favorites")) {
        viewModel.addToPlaylist(
            CustomPlaylist(
                playlistName = playlistName,
                videoId = currentSongDetails.videoId,
                videoName = currentSongDetails.videoTitle,
                videoThump = currentSongDetails.videoImage
            )
        )
    }else{
//        Log.d("kolpo","added favourties")

        viewModel.addToFavorites(
            FavoritePlaylist(
                videoId = currentSongDetails.videoId,
                videoName = currentSongDetails.videoTitle,
                videoThump = currentSongDetails.videoImage
            )
        )
    }
    viewModel.reloadAllCustomPlayListData(true)
}

@Preview(showBackground = true)
@Composable
fun kopl() {
//    PlayListDialogViewer()
}
