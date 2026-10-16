package com.ramzmania.tubefy.ui.components

import android.annotation.SuppressLint
import android.content.ComponentName
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Search
import com.ramzmania.tubefy.ui.theme.DiscoverAccent
import com.ramzmania.tubefy.ui.theme.DiscoverAccentContainer
import com.ramzmania.tubefy.ui.theme.DiscoverBackground
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.MutableState

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.TileMode

import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
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
import com.ramzmania.tubefy.player.PlaybackService
import com.ramzmania.tubefy.ui.components.screen.BooksScreen
import com.ramzmania.tubefy.ui.components.screen.home.HomeInitialScreen
import com.ramzmania.tubefy.ui.components.screen.ProfileScreen
import com.ramzmania.tubefy.ui.components.screen.album.AlbumScreen
import com.ramzmania.tubefy.ui.components.screen.category.CategoryPlaylistView
import com.ramzmania.tubefy.ui.components.screen.library.LibraryDetailPage
import com.ramzmania.tubefy.ui.components.screen.library.MyLibraryPage
import com.ramzmania.tubefy.ui.components.screen.player.MiniPlayerView
import com.ramzmania.tubefy.ui.components.screen.player.PlayerBaseView
import com.ramzmania.tubefy.ui.components.screen.search.AudioSearchScreen
import com.ramzmania.tubefy.utils.LocalNavController
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.UnstableApi

@AndroidEntryPoint
class HomeActivity : ComponentActivity() {
    private var mediaController: MediaController? = null
    private val mediaControllerState = mutableStateOf<MediaController?>(null)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Edge-to-edge is enforced from targetSdk 35; keep light system bar icons on the black UI
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
        )
        setContent {
            MainScreen(mediaControllerState = mediaControllerState)

        }
    }

    @androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
    @UnstableApi
    override fun onStart() {
        super.onStart()
        try {
            val sessionToken = SessionToken(this, ComponentName(this, PlaybackService::class.java))
            val controllerFuture = MediaController.Builder(this, sessionToken).buildAsync()
            controllerFuture.addListener(
                {

                    try {

                        mediaController = controllerFuture.get()
                        mediaControllerState.value = mediaController // Update state
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
        mediaControllerState.value = mediaController // Update state
        super.onStop()
    }
    fun playPDFAudio() {
//        Toast.makeText(applicationContext,"playing"+File(filesDir.absolutePath
//                + "/pdfAudio/audio.wav").absolutePath,1).show()

//        Log.d("path",File(filesDir.absolutePath
//                + "/pdfAudio/audio.wav").absolutePath)
        try {
            val mediaController = mediaController ?: return
            if (!mediaController.isConnected) return

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
                        .setArtist("Reading Page No : ")
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



private val edgeToEdgeRoutes = setOf(NavigationItem.AudioPlayer.route, NavigationItem.PlayList.route)

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun MainScreen(mediaControllerState: MutableState<MediaController?>) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DiscoverBackground)
    ) {
        // Content that extends behind the bottom bar
        CompositionLocalProvider(
            LocalNavController provides navController // Added missing comma
        ) {
            Scaffold(
                bottomBar = {
                    if (currentRoute != NavigationItem.AudioPlayer.route) {
                        Column {
                            if (mediaControllerState.value != null&&mediaControllerState.value!!.currentMediaItem!=null) {
                                MiniPlayerView()
                            }
                            BottomNavigationBar(navController)
                        }
                    }
                },
                // Only the bottom bar (mini player + tabs) should push content up; screens handle the status bar
                contentWindowInsets = WindowInsets(0, 0, 0, 0),
                content = { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            // The player and album screens draw behind the status bar themselves
                            .then(if (currentRoute in edgeToEdgeRoutes) Modifier else Modifier.statusBarsPadding())
                            // Keep content above the mini player and tabs so the last list item stays visible
                            .padding(bottom = innerPadding.calculateBottomPadding())
                    ) {
                        Navigation(navController = navController)
                    }
                },
                containerColor = DiscoverBackground,
                contentColor = colorResource(R.color.colorPrimary)
            )
        }
        // Place BottomNavigationBar at the bottom
    }
}


/*@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    MainScreen()
}*/

@Composable
fun Navigation(navController: NavHostController) {
    NavHost(navController, startDestination = NavigationItem.Home.route) {
        composable(NavigationItem.Home.route) {
            HomeInitialScreen(navController = navController)
        }
        composable(NavigationItem.Search.route) {
            AudioSearchScreen()
        }

        composable(NavigationItem.Books.route) {
            MyLibraryPage()
        }
        composable(NavigationItem.Profile.route) {
            ProfileScreen()
        }
        composable(NavigationItem.PlayList.route) { backStackEntry ->
            AlbumScreen()
        }
        composable(NavigationItem.AudioPlayer.route) { backStackEntry ->
            PlayerBaseView()
        }
        composable(NavigationItem.MyPlayList.route) { backStackEntry ->
            LibraryDetailPage()
        }
        composable(NavigationItem.CategoryPlayList.route) { backStackEntry ->
            CategoryPlaylistView()
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
            titleContentColor = Color.White
        )
    )
}

@Preview(showBackground = true)
@Composable
fun TopBarPreview() {
    TopBar()
}

@Composable
fun BottomNavigationBar(navController: NavController) {
    // (item, selected icon, unselected icon)
    val items = listOf(
        Triple(NavigationItem.Home, Icons.Rounded.Home, Icons.Outlined.Home),
        Triple(NavigationItem.Search, Icons.Rounded.Search, Icons.Outlined.Search),
        Triple(NavigationItem.Books, LibraryIconFilled, LibraryIconOutlined),
        Triple(NavigationItem.Profile, Icons.Rounded.Info, Icons.Outlined.Info)
    )
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    NavigationBar(
        containerColor = DiscoverBackground,
        contentColor = Color.White
    ) {
        items.forEach { (item, selectedIcon, unselectedIcon) ->
            val selected = currentRoute == item.route
            NavigationBarItem(
                icon = {
                    Icon(
                        if (selected) selectedIcon else unselectedIcon,
                        contentDescription = item.title,
                        modifier = Modifier.size(26.dp)
                    )
                },
                label = { Text(text = item.title, fontSize = 13.sp) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = DiscoverAccent,
                    unselectedIconColor = Color.White.copy(alpha = 0.75f),
                    selectedTextColor = DiscoverAccent,
                    unselectedTextColor = Color.White.copy(alpha = 0.75f),
                    indicatorColor = DiscoverAccentContainer
                ),
                alwaysShowLabel = true,
                selected = selected,
                onClick = { navController.navigateToTab(item.route) }
            )
        }
    }
}
