package com.ramzmania.tubefy.ui.components

import android.annotation.SuppressLint
import android.content.ComponentName
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults

import androidx.compose.runtime.getValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.TileMode

import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.google.common.util.concurrent.MoreExecutors
import com.ramzmania.tubefy.R
import com.ramzmania.tubefy.ui.components.screen.BooksScreen
import com.ramzmania.tubefy.ui.components.screen.home.HomeInitialScreen
import com.ramzmania.tubefy.ui.components.screen.MusicScreen
import com.ramzmania.tubefy.ui.components.screen.ProfileScreen
import com.ramzmania.tubefy.ui.components.screen.album.AlbumScreen
import com.ramzmania.tubefy.ui.components.screen.player.MediaPlayerScreen
import com.ramzmania.tubefy.ui.components.screen.player.PlaybackService
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeActivity: ComponentActivity() {
    private var mediaController: MediaController? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
                MainScreen()

        }
    }

    /*@UnstableApi
    override fun onStart() {
        super.onStart()
        try {
            Log.d("kona","1111")
            val sessionToken = SessionToken(this, ComponentName(this, PlaybackService::class.java))
            val controllerFuture = MediaController.Builder(this, sessionToken).buildAsync()
            controllerFuture.addListener(
                {
                    Log.d("kona","22222")

                    try {
                        Log.d("kona","3333333")

                        mediaController = controllerFuture.get()

                      //  playPDFAudio()

                    } catch (ex: Exception) {
ex.printStackTrace()
                    }
                },
                MoreExecutors.directExecutor()
            )
        } catch (ex: Exception) {
ex.printStackTrace()
        }
    }

    override fun onStop() {
        mediaController?.release()
        mediaController = null
        super.onStop()
    }
*/
    fun playPDFAudio() {
//        Toast.makeText(applicationContext,"playing"+File(filesDir.absolutePath
//                + "/pdfAudio/audio.wav").absolutePath,1).show()

//        Log.d("path",File(filesDir.absolutePath
//                + "/pdfAudio/audio.wav").absolutePath)
        try {
Log.d("konaaa","innnn")
            val mediaController = mediaController ?: return
            if (!mediaController.isConnected) return
            Log.d("konaaa","retunrd")

//            val drawableUri: Uri =
//                Uri.parse("android.resource://${BuildConfig.APPLICATION_ID}/${R.drawable.logo}")

            fun mediaItemData() = MediaItem.Builder()
                .setMediaId("id")
                .setUri(
                    Uri.parse("https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4")
                )
                .setMediaMetadata(
                    MediaMetadata.Builder()
                        .setTitle("tadadada")
                        .setArtist("Reading Page No : " )
                        .setIsBrowsable(false)
                        .setIsPlayable(true)
//                        .setArtworkUri(drawableUri)
                        .setMediaType(MediaMetadata.MEDIA_TYPE_MUSIC)
                        .build()
                )
                .build()

            mediaController.setMediaItem(mediaItemData())
            mediaController.playWhenReady = true
            mediaController.prepare()

        } catch (ex: Exception) {
ex.printStackTrace()
        }
    }
}

@Composable
fun MainScrmeen() {
    val navController = rememberNavController()
    Scaffold(
        topBar = { TopBar() },
        bottomBar = { BottomNavigationBar(navController) },
        content = { padding ->
            Box(modifier = Modifier.padding(padding)) {
                Navigation(navController = navController)
            }
        },
        contentColor = colorResource(R.color.colorPrimary)
    )
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun MainScreen() {
    val navController = rememberNavController()

    Box(modifier = Modifier.fillMaxSize()) {
        // Content that extends behind the bottom bar
        Scaffold(
            topBar = { TopBar() },
            bottomBar = { BottomNavigationBar(navController) },
            content = {
                Box(modifier = Modifier.fillMaxSize()) {
                    Navigation(navController = navController)
                }
            },
            contentColor = colorResource(R.color.colorPrimary)
        )

        // Place BottomNavigationBar at the bottom

    }
}


@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    MainScreen()
}

@Composable
fun Navigation(navController: NavHostController) {
    NavHost(navController, startDestination = NavigationItem.Home.route) {
        composable(NavigationItem.Home.route) {
            HomeInitialScreen(navController = navController)
        }
        composable(NavigationItem.Music.route) {
            MusicScreen()
        }

        composable(NavigationItem.Books.route) {
            BooksScreen()
        }
        composable(NavigationItem.Profile.route) {
            ProfileScreen()
        }
        composable(NavigationItem.PlayList.route) { backStackEntry ->
            AlbumScreen(navController = navController)
        }
        composable(NavigationItem.AudioPlayer.route) { backStackEntry ->
            MediaPlayerScreen(navController = navController)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar() {
    TopAppBar(
        title = { Text(text = stringResource(R.string.app_name), fontSize = 18.sp) },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = colorResource(id = R.color.colorPrimary),
            titleContentColor = Color.White)
    )
}

@Preview(showBackground = true)
@Composable
fun TopBarPreview() {
    TopBar()
}

@Composable
fun BottomNavigationBar(navController: NavController) {
    val items = listOf(
        NavigationItem.Home,
        NavigationItem.Music,
        NavigationItem.Books,
        NavigationItem.Profile
    )
    // Create a linear gradient brush
    val gradientBrush = Brush.linearGradient(
        colors = listOf( Color.Transparent,Color.Black),
        start = Offset(0f, 0f),
        end = Offset(0f, Float.POSITIVE_INFINITY),
        tileMode = TileMode.Clamp
    )
    NavigationBar(
        containerColor = Color.Transparent, // Set container color to transparent
        contentColor = Color.White,
        modifier = Modifier.background(gradientBrush) // Apply gradient brush as background
    )  {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route
        items.forEach { item ->
            NavigationBarItem(
                icon = { Icon(painterResource(id = item.icon), contentDescription = item.title) },
                label = { Text(text = item.title) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color.White,
                    unselectedIconColor = Color.White.copy(0.4f),
                    selectedTextColor = Color.White,
                    unselectedTextColor = Color.White.copy(0.4f)
                ),
                alwaysShowLabel = true,
                selected = currentRoute == item.route,
                onClick = {
                    navController.navigate(item.route) {
                        navController.graph.startDestinationRoute?.let { route ->
                            popUpTo(route) {
                                saveState = true
                            }
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}



@Preview(showBackground = true)
@Composable
fun BottomNavigationBarPreview() {
    // BottomNavigationBar()
}