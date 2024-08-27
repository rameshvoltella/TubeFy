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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.ramzmania.tubefy.data.dto.base.PlaylistItem
import com.ramzmania.tubefy.data.dto.base.playlist.PlayListCategory
import com.ramzmania.tubefy.data.dto.base.searchformat.TubeFyCoreTypeData
import com.ramzmania.tubefy.data.dto.database.PlaylistNameWithUrl
import com.ramzmania.tubefy.database.CustomPlaylist
import com.ramzmania.tubefy.ui.components.NavigationItem
import com.ramzmania.tubefy.utils.LocalNavController
import com.ramzmania.tubefy.viewmodel.TubeFyViewModel
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Composable
fun MyLibraryPage(viewModel: TubeFyViewModel = hiltViewModel()) {
    var doLoadData by remember { mutableStateOf(true) }  // Track loading state
    val gettingMyPlayList by viewModel.gettingPrivatePlayList.observeAsState()
    var playListItems by rememberSaveable { mutableStateOf<List<PlaylistNameWithUrl?>>(emptyList()) }

    LaunchedEffect(key1 = gettingMyPlayList) {
        if (gettingMyPlayList is Resource.Success) {
            doLoadData = false
            if (gettingMyPlayList?.data != null && gettingMyPlayList?.data!!.isNotEmpty()) {
                playListItems = gettingMyPlayList?.data!!
            }

        }

    }

    LaunchedEffect(key1 = Unit) {
        if (doLoadData) {
            viewModel.getAllSavedPlayList()
        }

    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(id = R.color.colorPrimary))
    ) {
        Text(
            text = URLDecoder.decode(
                "Saved library",
                StandardCharsets.UTF_8.toString()
            ),
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(10.dp, 30.dp, 10.dp, 10.dp),
            textAlign = TextAlign.Left,
            maxLines = 2,
            fontSize = 40.sp
        )
        if (playListItems != null && playListItems.isNotEmpty()) {
            LibraryHomeView(playListItems)
        }

    }

}

@Composable
fun LibraryHomeView(tracks: List<PlaylistNameWithUrl?>) {
    val navController = LocalNavController.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 10.dp)
            .background(colorResource(id = R.color.colorPrimary))
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 16.dp) // Padding for the whole LazyColumn
        ) {

            items(tracks!!) { track ->
                LibraryHomeItem(playListItem = track!!) { selectedItem ->
                    navController!!.navigate(
                        NavigationItem.MyPlayList.createRoute(

                            URLEncoder.encode(
                                YoutubeCoreConstant.decodeThumpUrl(selectedItem.videoThump!!),
                                StandardCharsets.UTF_8.toString()
                            ), selectedItem.playlistName!!
                        )
                    ) {
                        navController.graph.route?.let { route ->
                            popUpTo(route) {
                                saveState = true
                            }
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            }
            item {
                Spacer(modifier = Modifier.height(90.dp))
            }
        }
    }


}

@Composable
fun LibraryHomeItem(playListItem: PlaylistNameWithUrl, onClick: (PlaylistNameWithUrl) -> Unit) {

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
                .clickable { onClick(playListItem!!) },
            elevation = CardDefaults.cardElevation(4.dp), // Use CardDefaults.cardElevation for elevation
            colors = CardDefaults.cardColors(containerColor = Color.DarkGray), // Set background color
            shape = RoundedCornerShape(8.dp) // Adjust the corner radius for a rounded effect
        ) {
            Row(
                modifier = Modifier
//                .padding(8.dp)
                    .fillMaxWidth()
            ) {

                playListItem.videoThump?.let { thumbnailUrl ->
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
                    text = if (playListItem.playlistName!! == "TubeFy-Favorites") "Favorite" else playListItem.playlistName!!,
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
                Spacer(modifier = Modifier.width(8.dp))
                Image(
                    painter = painterResource(id = R.drawable.ic_delete),
                    contentDescription = "Right Image",
                    modifier = Modifier
                        .height(50.dp)
                        .width(50.dp)
                        .padding(horizontal = 5.dp, vertical = 10.dp),
                    contentScale = ContentScale.Crop
                )

            }
        }
//        Text(text = trackName.videoTitle,  color = Color.Black)
//        Divider(modifier = Modifier.padding(vertical = 4.dp), color = Color.Gray)
    }
}

@Preview(showBackground = true)
@Composable
fun previews() {
    LibraryHomeView(
        listOf(
            PlaylistNameWithUrl(
                playlistName = "sndfhiew",
                videoThump = "https://www.jjj.com",
            )
        )
    )
}