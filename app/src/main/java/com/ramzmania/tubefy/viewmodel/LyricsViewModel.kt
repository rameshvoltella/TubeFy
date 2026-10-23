package com.ramzmania.tubefy.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ramzmania.tubefy.core.lyrics.SongInfo
import com.ramzmania.tubefy.core.lyrics.SongInfoSource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/** The music player's Lyrics tab: the playing song's details and lyrics. */
@HiltViewModel
class LyricsViewModel @Inject constructor(private val source: SongInfoSource) : ViewModel() {

    sealed interface State {
        data object Idle : State
        data object Loading : State
        data object Error : State
        data class Loaded(val info: SongInfo) : State
    }

    private val _state = MutableStateFlow<State>(State.Idle)
    val state: StateFlow<State> = _state.asStateFlow()

    private var loadedId: String? = null
    private var job: Job? = null

    /** Loads [videoId] unless it's already shown; [force] retries. */
    fun load(videoId: String, title: String, artist: String, durationMs: Long, force: Boolean = false) {
        if (videoId.isBlank() || (!force && videoId == loadedId && _state.value !is State.Error)) return
        loadedId = videoId
        job?.cancel()
        _state.value = State.Loading
        job = viewModelScope.launch {
            _state.value = try {
                State.Loaded(source.load(videoId, title, artist, durationMs))
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                State.Error
            }
        }
    }
}
