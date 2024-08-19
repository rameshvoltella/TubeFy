import android.content.Context
import android.net.Uri
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView

@Composable
fun VideoPlayerView(videoUrl: String) {
    val context = LocalContext.current

    // Create and configure ExoPlayer
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(Uri.parse(videoUrl)))
            prepare()
            playWhenReady=true
        }
    }

    // Dispose ExoPlayer when Composable leaves the composition
    DisposableEffect(context) {
        onDispose {
            exoPlayer.release()
        }
    }

    // Use AndroidView to integrate PlayerView with Compose
    AndroidView(
        factory = {
            PlayerView(context).apply {
                player = exoPlayer
                useController = true // Show playback controls
            }
        },
        modifier = Modifier.fillMaxSize()
    )
}
