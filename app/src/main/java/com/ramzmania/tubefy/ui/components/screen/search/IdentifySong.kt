package com.ramzmania.tubefy.ui.components.screen.search

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.border
import androidx.compose.ui.res.painterResource
import com.ramzmania.tubefy.R
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.ramzmania.tubefy.ui.components.screen.album.openTrack
import com.ramzmania.tubefy.ui.theme.DiscoverAccent
import com.ramzmania.tubefy.utils.LocalNavController
import com.ramzmania.tubefy.viewmodel.RecognitionViewModel
import kotlinx.coroutines.flow.MutableStateFlow

/** A search handed to the Search tab from elsewhere (e.g. Identify song on Home → "Search"). */
object PendingSearch {
    val query = MutableStateFlow<String?>(null)
}

/**
 * "Identify song" wherever it's offered: asks for the microphone, shows the listening / result sheet,
 * plays the song or hands "title artist" to [onSearch]. Returns the action that starts it.
 */
@Composable
fun rememberIdentifySong(onSearch: (String) -> Unit): () -> Unit {
    val context = LocalContext.current
    val navController = LocalNavController.current
    val recognition: RecognitionViewModel = hiltViewModel()
    val state by recognition.state.collectAsState()
    var show by remember { mutableStateOf(false) }
    val permission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            show = true
            recognition.start()
        } else {
            Toast.makeText(context, "TubeFy needs the microphone to identify songs", Toast.LENGTH_LONG).show()
        }
    }

    if (show) {
        IdentifySongSheet(
            state = state,
            onPlay = {
                (state as? RecognitionViewModel.State.Found)?.track?.let { track ->
                    show = false
                    recognition.cancel()
                    navController?.let { openTrack(it, track) }
                }
            },
            onSearch = {
                (state as? RecognitionViewModel.State.Found)?.let { found ->
                    show = false
                    recognition.cancel()
                    onSearch(recognition.query(found.song))
                }
            },
            onRetry = recognition::start,
            onDismiss = {
                show = false
                recognition.cancel()
            }
        )
    }

    return {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            show = true
            recognition.start()
        } else permission.launch(Manifest.permission.RECORD_AUDIO)
    }
}

/** The round Identify-song button floating on Home: the identify symbol on TubeFy's red accent. */
@Composable
fun IdentifySongButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .shadow(14.dp, CircleShape, ambientColor = DiscoverAccent, spotColor = DiscoverAccent)
            .size(60.dp)
            .clip(CircleShape)
            .background(Brush.linearGradient(listOf(Color(0xFFFF5A6A), DiscoverAccent)))
            .border(1.5.dp, Color.White.copy(alpha = 0.25f), CircleShape)
            .clickable(onClick = onClick)
    ) {
        Image(
            painter = painterResource(R.drawable.ic_identify_song_symbol),
            contentDescription = "Identify song",
            modifier = Modifier.size(36.dp)
        )
    }
}
