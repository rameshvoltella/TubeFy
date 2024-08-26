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
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.ramzmania.tubefy.R
import com.ramzmania.tubefy.core.YoutubeCoreConstant
import com.ramzmania.tubefy.data.dto.base.searchformat.TubeFyCoreTypeData
import com.ramzmania.tubefy.ui.components.NavigationItem
import com.ramzmania.tubefy.utils.LocalNavController
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Composable
fun LibraryBaseView(tracks: List<TubeFyCoreTypeData?>) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(id = R.color.colorPrimary))
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 16.dp) // Padding for the whole LazyColumn
        ) {
            items(tracks!!) { track ->
                LibraryItem(trackName = track!!)
            }
            item {
                Spacer(modifier = Modifier.height(90.dp))
            }
        }
    }


}

@Composable
fun LibraryItem(trackName: TubeFyCoreTypeData) {

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
fun preview()
{
    LibraryBaseView(listOf(TubeFyCoreTypeData("sndfhiew","sdjfvjdnfv","https://www.jjj.com")))
}