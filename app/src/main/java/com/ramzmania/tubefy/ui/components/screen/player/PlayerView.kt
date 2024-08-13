package com.ramzmania.tubefy.ui.components.screen.player

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
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


@Composable
fun PlayerDummyBaseView() {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(id = R.color.colorPrimary))
    ) {
        Text(
            text = "Playing Now",
            modifier = Modifier
                .fillMaxWidth()
                .padding(top=10.dp, start = 10.dp, end = 10.dp),
            fontWeight = FontWeight.Thin,
            color = Color.White,
            textAlign = TextAlign.Center,
            maxLines = 2,
            fontSize = 16.sp
        )
        Text(
            text = "HanumanKind",
            modifier = Modifier
                .fillMaxWidth()
                .padding(top=2.dp, start = 10.dp, end = 10.dp),
             fontWeight = FontWeight.Medium,
            color = Color.White,
            textAlign = TextAlign.Center,
            maxLines = 2,
            fontSize = 16.sp
        )

        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(YoutubeCoreConstant.decodeThumpUrl("https://yt3.googleusercontent.com/BypOGtr2lsqjZ8C-VBzyBf6oZphNo6VAeVYnEzLOrTAe9w0iKa-dcPXb_pBpNYgZnq7w2zDh=s900-c-k-c0x00ffffff-no-rj"))
                .crossfade(true)
                .placeholder(R.drawable.placeholder)
                .error(R.drawable.placeholder)
                .build(),
            contentDescription = "Drawable Image",
            modifier = Modifier
                .padding(horizontal = 60.dp, vertical = 20.dp)
                .fillMaxWidth()
                .height(250.dp)
                .align(Alignment.CenterHorizontally)
                .fillMaxWidth(),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.height(8.dp))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(top = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Jeevamshamayi (From \"Theevan\")",
                fontSize = 20.sp,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "K. S. Harisankar, Shreya Ghoshal, Kailas",
                fontSize = 14.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Slider(
                value = 1.19f,
                onValueChange = {},
                valueRange = 0f..5.23f,
                modifier = Modifier.fillMaxWidth()
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "1:19", color = Color.Gray, fontSize = 12.sp)
                Text(text = "5:23", color = Color.Gray, fontSize = 12.sp)
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.CenterHorizontally),
                horizontalArrangement = Arrangement.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_player_previous),
                    contentDescription = "Image 1",
                    modifier = Modifier.size(30.dp) // Adjust the size as needed
                )
                Spacer(modifier = Modifier.width(40.dp)) // Optional: Adds spacing between images
                Image(
                    painter = painterResource(id = R.drawable.ic_player_play),
                    contentDescription = "Image 2",
                    modifier = Modifier.size(30.dp)
                )
                Spacer(modifier = Modifier.width(40.dp)) // Optional: Adds spacing between images
                Image(
                    painter = painterResource(id = R.drawable.ic_player_next),
                    contentDescription = "Image 3",
                    modifier = Modifier.size(30.dp)
                )
            }
        }

    }

}


@Composable
fun PlayerBaseView() {

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
                text = "HanumanKind",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 2.dp, start = 10.dp, end = 10.dp),
                fontWeight = FontWeight.Medium,
                color = Color.White,
                textAlign = TextAlign.Center,
                maxLines = 2,
                fontSize = 16.sp
            )

            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(YoutubeCoreConstant.decodeThumpUrl("https://yt3.googleusercontent.com/BypOGtr2lsqjZ8C-VBzyBf6oZphNo6VAeVYnEzLOrTAe9w0iKa-dcPXb_pBpNYgZnq7w2zDh=s900-c-k-c0x00ffffff-no-rj"))
                    .crossfade(true)
                    .placeholder(R.drawable.placeholder)
                    .error(R.drawable.placeholder)
                    .build(),
                contentDescription = "Drawable Image",
                modifier = Modifier
                    .padding(horizontal = 60.dp, vertical = 50.dp)
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .align(Alignment.CenterHorizontally),
                contentScale = ContentScale.Crop
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Jeevamshamayi (From \"Theevan\")",
                fontSize = 20.sp,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "K. S. Harisankar, Shreya Ghoshal, Kailas",
                fontSize = 14.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Slider(
                value = 1.19f,
                onValueChange = {},
                valueRange = 0f..5.23f,
                modifier = Modifier.fillMaxWidth()
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "1:19", color = Color.Gray, fontSize = 12.sp)
                Text(text = "5:23", color = Color.Gray, fontSize = 12.sp)
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.CenterHorizontally),
                horizontalArrangement = Arrangement.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_player_previous),
                    contentDescription = "Image 1",
                    modifier = Modifier.size(30.dp) // Adjust the size as needed
                )
                Spacer(modifier = Modifier.width(40.dp)) // Optional: Adds spacing between images
                Image(
                    painter = painterResource(id = R.drawable.ic_player_play),
                    contentDescription = "Image 2",
                    modifier = Modifier.size(30.dp)
                )
                Spacer(modifier = Modifier.width(40.dp)) // Optional: Adds spacing between images
                Image(
                    painter = painterResource(id = R.drawable.ic_player_next),
                    contentDescription = "Image 3",
                    modifier = Modifier.size(30.dp)
                )
            }
        }
    }
}


@Composable
fun MusicPlayerScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121212)),
        verticalArrangement = Arrangement.SpaceBetween
    ) {

    }
}


@Preview(showBackground = true)
@Composable
fun PlayerPreview() {
    PlayerBaseView()
}
