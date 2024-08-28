package com.ramzmania.tubefy.ui.components.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


@Composable
fun MusicScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(id = com.ramzmania.tubefy.R.color.colorPrimary))
            .wrapContentSize(Alignment.Center)
    ) {
        Text(
            text = "Music View",
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.align(Alignment.CenterHorizontally),
            textAlign = TextAlign.Center,
            fontSize = 25.sp
        )
    }
}

@Preview(showBackground = true)
@Composable
fun MusicScreenPreview() {
    MusicScreen()
}

@Composable
fun MoviesScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(id = com.ramzmania.tubefy.R.color.colorPrimary))
            .wrapContentSize(Alignment.Center)
    ) {
        Text(
            text = "Movies View",
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.align(Alignment.CenterHorizontally),
            textAlign = TextAlign.Center,
            fontSize = 25.sp
        )
    }
}

@Preview(showBackground = true)
@Composable
fun MoviesScreenPreview() {
    MoviesScreen()
}


@Composable
fun BooksScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(id = com.ramzmania.tubefy.R.color.purple_200))
            .wrapContentSize(Alignment.Center)
    ) {
        Text(
            text = "Books View",
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.align(Alignment.CenterHorizontally),
            textAlign = TextAlign.Center,
            fontSize = 25.sp
        )
    }
}

@Preview(showBackground = true)
@Composable
fun BooksScreenPreview() {
    BooksScreen()
}

@Composable
fun ProfileScreen() {
    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .background(colorResource(id = com.ramzmania.tubefy.R.color.colorPrimary))
            .wrapContentSize(Alignment.TopStart)
    ) {
        Text(
            text = "TubeFy",
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Center,
            fontSize = 25.sp,
            modifier = Modifier.padding(10.dp)
        )
        Text(
            text = "Discover Your Favorite Music with Ease!\n" +
                    "\n" +
                    "Transform your YouTube experience into a seamless music streaming journey with our app. Just like Spotify, our app lets you play YouTube videos as songs, giving you access to your favorite tracks without interruption. Whether you're into the latest hits or classic tunes, you can enjoy them all in a Spotify-like interface.\n" +
                    "\n" +
                    "Key Features:\n" +
                    "\n" +
                    "    Play YouTube Videos as Songs: Enjoy your favorite YouTube music videos as if they were on a streaming service, with audio-only playback for a better listening experience.\n" +
                    "    Curated Playlists: Create and manage playlists with your favorite tracks, just like you would on Spotify.\n" +
                    "    Discover New Music: Explore new music and discover trending songs easily with personalized recommendations.\n" +
                    "    Seamless Playback: Enjoy uninterrupted music playback with background support, so you can listen while using other apps.\n" +
                    "    User-Friendly Interface: Navigate your music library and playlists with an intuitive and sleek interface designed for ease of use.\n" +
                    "\n" +
                    "Elevate your music experience and enjoy YouTube's vast music library like never before. Download now and start listening!",
            fontWeight = FontWeight.Thin,
            color = Color.White,
            modifier = Modifier.align(Alignment.CenterHorizontally).padding(5.dp),
            textAlign = TextAlign.Center,
            fontSize = 18.sp
        )
//        MediaPlayerScreen("")
    }
}

@Preview(showBackground = true)
@Composable
fun ProfileScreenPreview() {
    ProfileScreen()
}