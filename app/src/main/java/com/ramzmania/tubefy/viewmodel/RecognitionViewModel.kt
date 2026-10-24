package com.ramzmania.tubefy.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ramzmania.tubefy.core.recognition.ListeningService
import com.ramzmania.tubefy.core.recognition.RecognitionResult
import com.ramzmania.tubefy.core.recognition.RecognizedSong
import com.ramzmania.tubefy.core.recognition.SongRecognizer
import com.ramzmania.tubefy.data.Resource
import com.ramzmania.tubefy.data.dto.base.searchformat.TubeFyCoreTypeData
import com.ramzmania.tubefy.data.remote.RemoteRepositorySource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

/** "Identify song": listening, the recognised song, and the YouTube Music track to play it. */
@HiltViewModel
class RecognitionViewModel @Inject constructor(
    private val recognizer: SongRecognizer,
    private val remote: RemoteRepositorySource,
    @dagger.hilt.android.qualifiers.ApplicationContext private val context: android.content.Context
) : ViewModel() {

    sealed interface State {
        data object Idle : State
        data class Listening(val seconds: Int) : State
        /** [track] is the song on YouTube Music (null while it's being looked up, or if not found). */
        data class Found(val song: RecognizedSong, val track: TubeFyCoreTypeData?, val findingTrack: Boolean) : State
        data object NotFound : State
        data class Failed(val message: String) : State
    }

    private val _state = MutableStateFlow<State>(State.Idle)
    val state: StateFlow<State> = _state.asStateFlow()
    private var job: Job? = null

    fun start() {
        job?.cancel()
        // Whatever TubeFy is playing pauses, so the microphone hears the song to identify, not TubeFy
        pausePlayback()
        _state.value = State.Listening(0)
        // Keeps the microphone on if the user switches to the app playing the song
        ListeningService.start(context)
        job = viewModelScope.launch {
            when (val result = recognizer.listen { seconds -> _state.value = State.Listening(seconds) }) {
                is RecognitionResult.Found -> {
                    _state.value = State.Found(result.song, null, findingTrack = true)
                    val track = findOnYouTube(result.song)
                    _state.value = State.Found(result.song, track, findingTrack = false)
                    ListeningService.showResult(context, result.song)
                    // Library → Shazam
                    runCatching {
                        com.ramzmania.tubefy.core.recognition.IdentifiedSongs.record(
                            context,
                            com.ramzmania.tubefy.core.recognition.IdentifiedSong(
                                title = result.song.title,
                                artist = result.song.artist,
                                album = result.song.album,
                                year = result.song.year,
                                coverUrl = result.song.coverUrl,
                                videoId = track?.videoId?.takeIf { it.isNotBlank() },
                                identifiedAt = System.currentTimeMillis()
                            )
                        )
                    }
                }
                RecognitionResult.NotFound -> {
                    _state.value = State.NotFound
                    ListeningService.showResult(context, null)
                }
                is RecognitionResult.Failed -> {
                    _state.value = State.Failed(result.message)
                    ListeningService.showResult(context, null, result.message)
                }
            }
        }
    }

    fun cancel() {
        job?.cancel()
        _state.value = State.Idle
        ListeningService.stop(context)
    }

    override fun onCleared() {
        ListeningService.stop(context)
    }

    private fun pausePlayback() {
        val token = androidx.media3.session.SessionToken(
            context,
            android.content.ComponentName(context, com.ramzmania.tubefy.player.PlaybackService::class.java)
        )
        val future = androidx.media3.session.MediaController.Builder(context, token).buildAsync()
        future.addListener({
            runCatching {
                val controller = future.get()
                if (controller.isPlaying) controller.pause()
                controller.release()
            }
        }, androidx.core.content.ContextCompat.getMainExecutor(context))
    }

    /** What to search YouTube Music for: "title artist". */
    fun query(song: RecognizedSong) = listOf(song.title, song.artist.substringBefore(" & ").substringBefore(","))
        .filter { it.isNotBlank() }.joinToString(" ")

    private suspend fun findOnYouTube(song: RecognizedSong): TubeFyCoreTypeData? = runCatching {
        (remote.getNewPipePageSearch(0, query(song), listOf("music_songs"), "").first() as? Resource.Success)
            ?.data?.youtubeSortedData?.youtubeSortedList.orEmpty()
            .firstOrNull { it != null && it.videoId.isNotBlank() }
    }.getOrNull()
}
