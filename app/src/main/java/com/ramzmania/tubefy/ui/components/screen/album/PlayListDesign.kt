package com.ramzmania.tubefy.ui.components.screen.album

import android.content.Intent
import android.net.Uri
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
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.compose.rememberImagePainter
import coil.request.ImageRequest
import com.ramzmania.tubefy.R
import com.ramzmania.tubefy.core.YoutubeCoreConstant
import com.ramzmania.tubefy.core.YoutubeCoreConstant.decodeThumpUrl
import com.ramzmania.tubefy.core.extractors.yotubewebextractor.YoutubeScrapType
import com.ramzmania.tubefy.data.Resource
import com.ramzmania.tubefy.data.dto.home.HomePageResponse
import com.ramzmania.tubefy.data.dto.searchformat.StreamUrlData
import com.ramzmania.tubefy.data.dto.searchformat.TubeFyCoreTypeData
import com.ramzmania.tubefy.ui.components.NavigationItem
import com.ramzmania.tubefy.utils.LocalNavController
import com.ramzmania.tubefy.viewmodel.TubeFyViewModel
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Composable
fun Album2Screen(final:List<TubeFyCoreTypeData?>) {

    var finalItems by remember { mutableStateOf<List<TubeFyCoreTypeData?>>(final) }

    Column(modifier = Modifier
        .fillMaxSize()
        .background(Color.Black)) {
        Album2Header(
            imageUrl = "https://www.udiscovermusic.com/wp-content/uploads/2015/10/100-Greatest-Album-Covers.jpg", // Replace with your image URL
            title = "Real Steel (Original Motion Picture Score)"
        )
        Album2TrackList(tracks =finalItems)
    }


}

@Composable
fun Album2Header(imageUrl: String, title: String) {
    Column(modifier = Modifier.fillMaxWidth()) {
      /*  AsyncImage(
            painter = rememberImagePainter(data = imageUrl),
            contentDescription = null,
            modifier = Modifier
                .height(200.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
        )*/
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(YoutubeCoreConstant.decodeThumpUrl(imageUrl))
                .crossfade(true)
                .placeholder(R.drawable.placeholder)
                .error(R.drawable.placeholder)
                .build(),
            contentDescription = "Drawable Image",
            modifier = Modifier
                .padding(horizontal = 5.dp, vertical = 5.dp)
                .width(200.dp)
                .height(200.dp)
                .align(Alignment.CenterHorizontally)
                .fillMaxWidth(),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = title,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            fontWeight = FontWeight.Medium,
            color = colorResource(id = R.color.tubefyred),
            textAlign = TextAlign.Left,
            maxLines = 2,
            fontSize = 18.sp)

    }
}

@Composable
fun Album2TrackList(tracks:List<TubeFyCoreTypeData?>) {
    LazyColumn {
        items(tracks!!) { track ->
            Track2Item(trackName = track!!)
        }
    }
}

@Composable
fun Track2Item(trackName: TubeFyCoreTypeData) {

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 8.dp)
            .clickable {
//                val videoId = "someVideoId"
//                val videoName = "Mere Khwabon Mein | Lyrical Song"

// Encode videoName if it contains special characters
                val encodedVideoUrl = URLEncoder.encode(
                    decodeThumpUrl(trackName.videoImage),
                    StandardCharsets.UTF_8.toString()
                )
                val encodedVideoId =
                    URLEncoder.encode(trackName.videoId, StandardCharsets.UTF_8.toString())
//                LocalNavController.current

            }
    ) {
        Card(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth(),
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
                            .data(decodeThumpUrl(thumbnailUrl))
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

                Text(
                    text = trackName.videoTitle!!,
                    fontWeight = FontWeight.Thin,
                    color = Color.White,
                    modifier = Modifier
                        .align(Alignment.CenterVertically)
                        .fillMaxWidth()
                        .padding(horizontal = 5.dp),
                    textAlign = TextAlign.Left,
                    maxLines = 1,
                    fontSize = 16.sp
                )
            }
        }
    }
}

@Preview
@Composable
fun Preview2AlbumScreen() {
   val kk= listOf(TubeFyCoreTypeData("video","sjn","sjdfb"))
    Album2Screen(kk)
}
