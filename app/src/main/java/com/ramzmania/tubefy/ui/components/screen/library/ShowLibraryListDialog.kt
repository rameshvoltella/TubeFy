package com.ramzmania.tubefy.ui.components.screen.library

import android.util.Log
import android.widget.Toast
import android.content.res.Configuration
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.runtime.Composable

import androidx.compose.foundation.Image
import androidx.compose.foundation.background

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import com.ramzmania.tubefy.ui.theme.DiscoverAccent
import com.ramzmania.tubefy.ui.theme.DiscoverAccentContainer
import com.ramzmania.tubefy.ui.theme.DiscoverBorder
import com.ramzmania.tubefy.ui.theme.DiscoverSurface
import com.ramzmania.tubefy.ui.theme.DiscoverTextSecondary
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
import com.ramzmania.tubefy.core.download.Downloads
import com.ramzmania.tubefy.database.CustomPlaylist
import com.ramzmania.tubefy.database.FavoritePlaylist
import com.ramzmania.tubefy.viewmodel.TubeFyViewModel


/** "Add to playlist" bottom sheet: create a new playlist or add the song to an existing one. */
@kotlin.OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistDialog(currentSongDetails: TubeFyCoreTypeData,viewModel: TubeFyViewModel = hiltViewModel(),
    playlists: List<PlaylistNameWithUrl?>,
    onDismiss: () -> Unit,
    onItemClick: (PlaylistNameWithUrl?) -> Unit
) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var creating by rememberSaveable { mutableStateOf(false) }
    var newName by rememberSaveable { mutableStateOf("") }

    fun createPlaylist() {
        val name = newName.trim()
        when {
            name.isEmpty() -> Unit
            Downloads.isReservedName(name) ->
                Toast.makeText(context, "\"$name\" is reserved for downloads", Toast.LENGTH_LONG).show()
            playlists.any { it?.playlistName.equals(name, ignoreCase = true) } ->
                Toast.makeText(context, "Playlist already exists", Toast.LENGTH_LONG).show()
            else -> {
                addToPlayList(currentSongDetails, viewModel, name)
                Toast.makeText(context, "Added to $name", Toast.LENGTH_SHORT).show()
                viewModel.dismissPlaListDialog(true)
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = SheetBackground,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 10.dp, bottom = 6.dp)
                    .size(width = 40.dp, height = 4.dp)
                    .clip(RoundedCornerShape(50))
                    .background(Color.White.copy(alpha = 0.25f))
            )
        }
    ) {
        val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
        Column(
            modifier = Modifier
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
        ) {
            // Header: the song being added
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(YoutubeCoreConstant.decodeThumpUrl(currentSongDetails.videoImage))
                        .crossfade(true)
                        .placeholder(R.drawable.placeholder)
                        .error(R.drawable.placeholder)
                        .build(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(10.dp))
                )
                Column(modifier = Modifier.padding(start = 14.dp)) {
                    Text("Add to playlist", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Text(
                        text = currentSongDetails.videoTitle,
                        color = DiscoverTextSecondary,
                        fontSize = 14.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // New playlist
            if (creating) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 4.dp)
                ) {
                    OutlinedTextField(
                        value = newName,
                        onValueChange = { newName = it },
                        placeholder = { Text("Playlist name", color = DiscoverTextSecondary) },
                        singleLine = true,
                        textStyle = TextStyle(color = Color.White, fontSize = 16.sp),
                        keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { createPlaylist() }),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = DiscoverAccent,
                            unfocusedBorderColor = DiscoverBorder,
                            focusedContainerColor = DiscoverSurface,
                            unfocusedContainerColor = DiscoverSurface,
                            cursorColor = DiscoverAccent
                        ),
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Create",
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(if (newName.isBlank()) DiscoverAccent.copy(alpha = 0.4f) else DiscoverAccent)
                            .clickable(enabled = newName.isNotBlank()) { createPlaylist() }
                            .padding(horizontal = 18.dp, vertical = 12.dp)
                    )
                }
            } else {
                SheetRow(
                    onClick = { creating = true },
                    leading = {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(52.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(DiscoverAccentContainer)
                        ) {
                            Icon(Icons.Rounded.Add, contentDescription = null, tint = DiscoverAccent, modifier = Modifier.size(28.dp))
                        }
                    },
                    title = "New playlist",
                    subtitle = "Create and add this song"
                )
            }

            Text(
                text = "Your playlists",
                color = DiscoverTextSecondary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(start = 20.dp, top = 14.dp, bottom = 4.dp)
            )
            LazyColumn(modifier = Modifier.heightIn(max = if (isLandscape) 200.dp else 380.dp)) {
                items(playlists.filterNotNull()) { playlist ->
                    val isFavorites = playlist.playlistName == "TubeFy-Favorites"
                    SheetRow(
                        onClick = { onItemClick(playlist) },
                        leading = {
                            if (isFavorites) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .size(52.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Brush.linearGradient(listOf(Color(0xFFD6334A), Color(0xFF3A1219))))
                                ) {
                                    Icon(Icons.Rounded.Star, contentDescription = null, tint = Color(0xFFFFD54F), modifier = Modifier.size(28.dp))
                                }
                            } else {
                                AsyncImage(
                                    model = ImageRequest.Builder(context)
                                        .data(YoutubeCoreConstant.decodeThumpUrl(playlist.videoThump))
                                        .crossfade(true)
                                        .placeholder(R.drawable.placeholder)
                                        .error(R.drawable.placeholder)
                                        .build(),
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(52.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                )
                            }
                        },
                        title = if (isFavorites) "Favorite" else playlist.playlistName.trim(),
                        subtitle = if (playlist.songCount == 1) "1 song" else "${playlist.songCount} songs"
                    )
                }
                item { Spacer(modifier = Modifier.height(12.dp)) }
            }
        }
    }
}

@Composable
private fun SheetRow(
    onClick: () -> Unit,
    leading: @Composable () -> Unit,
    title: String,
    subtitle: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 8.dp)
    ) {
        leading()
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 14.dp)
        ) {
            Text(title, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(subtitle, color = DiscoverTextSecondary, fontSize = 13.sp, maxLines = 1)
        }
    }
}

private val SheetBackground = Color(0xFF121011)

@Composable
fun PlayListDialogViewer(viewModel: TubeFyViewModel = hiltViewModel(),onDismiss: () -> Unit ,currentSongDetails: TubeFyCoreTypeData) {
    viewModel.dismissPlaListDialog(false)
    val context = LocalContext.current
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
            playlists = playListItems.filter { it?.playlistName != Downloads.PLAYLIST },
            onDismiss = {
                showDialog = false
                onDismiss() // Notify parent to reset the state
            },
            onItemClick = { playlist ->
                val name = playlist?.playlistName ?: return@PlaylistDialog
                addToPlayList(currentSongDetails, viewModel, name)
                Toast.makeText(
                    context,
                    "Added to ${if (name == "TubeFy-Favorites") "Favorite" else name.trim()}",
                    Toast.LENGTH_SHORT
                ).show()
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
