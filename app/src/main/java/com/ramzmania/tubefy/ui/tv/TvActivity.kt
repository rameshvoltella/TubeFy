package com.ramzmania.tubefy.ui.tv

import android.app.UiModeManager
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.ramzmania.tubefy.ui.theme.DiscoverAccent
import com.ramzmania.tubefy.ui.theme.DiscoverBackground
import dagger.hilt.android.AndroidEntryPoint
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

/**
 * Android TV: just the YouTube tab, made for the remote — a side menu with Search, Recent and the
 * chips, grids of video cards, and a full-screen player. Playback, history and the video manifest
 * (quality / audio language / captions) are the phone app's.
 */
@AndroidEntryPoint
class TvActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(colorScheme = darkColorScheme(primary = DiscoverAccent, background = DiscoverBackground, surface = DiscoverBackground)) {
                val nav = rememberNavController()
                Box(Modifier.fillMaxSize().background(DiscoverBackground)) {
                    NavHost(nav, startDestination = ROUTE_HOME) {
                        composable(ROUTE_HOME) {
                            TvYouTubeHome(onOpenVideo = { id, title, thumb, channel ->
                                nav.navigate(videoRoute(id, title, thumb, channel))
                            })
                        }
                        composable(
                            ROUTE_VIDEO,
                            arguments = listOf("title", "thumb", "channel").map { name ->
                                navArgument(name) { type = NavType.StringType; nullable = true; defaultValue = null }
                            }
                        ) { entry ->
                            TvVideoPlayer(args = entry.arguments, onExit = { nav.popBackStack() })
                        }
                    }
                }
            }
        }
    }

    companion object {
        private const val ROUTE_HOME = "tvHome"
        private const val ROUTE_VIDEO = "tvVideo/{videoId}?title={title}&thumb={thumb}&channel={channel}"

        private fun videoRoute(id: String, title: String, thumb: String, channel: String): String {
            fun enc(value: String) = URLEncoder.encode(value, StandardCharsets.UTF_8.toString())
            return "tvVideo/${enc(id)}?title=${enc(title)}&thumb=${enc(thumb)}&channel=${enc(channel)}"
        }

        /** True on Android TV / Google TV, where the TV screens are used instead of the phone ones. */
        fun isTv(context: Context): Boolean {
            val uiMode = context.getSystemService(UiModeManager::class.java)?.currentModeType
            return uiMode == Configuration.UI_MODE_TYPE_TELEVISION ||
                context.packageManager.hasSystemFeature(PackageManager.FEATURE_LEANBACK)
        }
    }
}
