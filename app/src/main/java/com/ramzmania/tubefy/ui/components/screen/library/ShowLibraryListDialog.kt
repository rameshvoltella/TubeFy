package com.ramzmania.tubefy.ui.components.screen.library

import androidx.compose.runtime.Composable

import android.os.Parcelable
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
import com.ramzmania.tubefy.data.dto.database.PlaylistNameWithUrl
import com.ramzmania.tubefy.database.CustomPlaylist
import com.ramzmania.tubefy.viewmodel.TubeFyViewModel


@Composable
fun PlaylistDialog(
    playlists: List<PlaylistNameWithUrl?>,
    onDismiss: () -> Unit,
    onItemClick: (PlaylistNameWithUrl?) -> Unit
) {
    var showPlayListEditText by remember { mutableStateOf(false) }
    val textFieldValueSaver = Saver<TextFieldValue, String>(
        save = { it.text },  // Save only the text part
        restore = { TextFieldValue(it) }  // Restore TextFieldValue from the text
    )

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
                                    .padding(horizontal = 5.dp, vertical = 10.dp),
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
                                    // Handle text input


                                })
                            )

                            // Spacer to push the image to the right
                            Spacer(modifier = Modifier.width(8.dp))

                            Image(
                                painter = painterResource(id = R.drawable.ic_add_playlist),
                                contentDescription = "Right Image 1",
                                modifier = Modifier
                                    .height(30.dp)
                                    .width(30.dp)
                                    .padding(horizontal = 5.dp, vertical = 10.dp),
                                contentScale = ContentScale.Crop
                            )

                            // Second Image on the right
                            Image(
                                painter = painterResource(id = R.drawable.ic_add_playlist), // Replace with the appropriate drawable resource
                                contentDescription = "Right Image 2",
                                modifier = Modifier
                                    .height(30.dp)
                                    .width(30.dp)
                                    .padding(horizontal = 5.dp, vertical = 10.dp),
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


                                    Spacer(modifier = Modifier.width(16.dp))

                                    // Playlist Name on the Right
                                    Text(
                                        text = playlist?.playlistName!!,
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
fun PlayListDialogViewer(viewModel: TubeFyViewModel = hiltViewModel(),onDismiss: () -> Unit ) {
    var showDialog by remember { mutableStateOf(true) }
    val gettingMyPlayList by viewModel.gettingPrivatePlayList.observeAsState()
    var playListItems by rememberSaveable { mutableStateOf<List<PlaylistNameWithUrl?>>(emptyList()) }
    var doLoadData by remember { mutableStateOf(true) }  // Track loading state

    LaunchedEffect(key1 = gettingMyPlayList) {
        if (gettingMyPlayList is Resource.Success) {

            doLoadData = false
            if (gettingMyPlayList?.data != null && gettingMyPlayList?.data!!.isNotEmpty()) {
                playListItems = gettingMyPlayList?.data!!
            }

        }
        else if (gettingMyPlayList is Resource.DataError) {
            doLoadData = false
        }
    }

    LaunchedEffect(key1 = Unit) {
        if (doLoadData) {
            viewModel.getAllSavedPlayList()
        }

    }
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
            playlists = playListItems,
            onDismiss = {
                showDialog = false
                onDismiss() // Notify parent to reset the state
            },
            onItemClick = { playlist ->
                // Handle item click
                viewModel.addToPlaylist(CustomPlaylist(playlistName = playlist?.playlistName!!, videoId = "", videoName = "", videoThump = ""))
                showDialog = false
                onDismiss() // Notify parent to reset the state

            }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun kopl() {
//    PlayListDialogViewer()
}
