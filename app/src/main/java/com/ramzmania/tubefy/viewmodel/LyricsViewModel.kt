package com.ramzmania.tubefy.viewmodel

import com.ramzmania.tubefy.core.analytics.Analytics
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
class LyricsViewModel @Inject constructor(
    private val source: SongInfoSource,
    @dagger.hilt.android.qualifiers.ApplicationContext context: android.content.Context
) : ViewModel() {

    private val prefs = context.getSharedPreferences("lyrics_settings", android.content.Context.MODE_PRIVATE)
    private val _englishLetters = MutableStateFlow(prefs.getBoolean(KEY_ENGLISH, false))
    /** Show lyrics in English letters (when the song is in another script); remembered. */
    val englishLetters: StateFlow<Boolean> = _englishLetters.asStateFlow()

    /** The playing song's album, for the player's "Go to album" (null if it isn't from one). */
    suspend fun album(videoId: String) = source.album(videoId)

    fun setEnglishLetters(enabled: Boolean) {
        if (enabled) Analytics.lyricsFeature("english_letters")
        _englishLetters.value = enabled
        prefs.edit().putBoolean(KEY_ENGLISH, enabled).apply()
    }

    // Manual lyrics timing, per song (added to the automatic intro shift); kept across restarts
    private val syncPrefs = context.getSharedPreferences("lyrics_sync", android.content.Context.MODE_PRIVATE)
    private val _syncOffset = MutableStateFlow(0L)
    /** The user's timing correction for the song shown, in ms (+ = lyrics later). */
    val syncOffset: StateFlow<Long> = _syncOffset.asStateFlow()

    fun adjustSync(deltaMs: Long) {
        Analytics.lyricsFeature("sync_adjust")
        setSync((_syncOffset.value + deltaMs).coerceIn(-MAX_SYNC_MS, MAX_SYNC_MS))
    }

    /** Back to the automatic timing. */
    fun resetSync() = setSync(0L)

    private fun setSync(offsetMs: Long) {
        val id = loadedId ?: return
        _syncOffset.value = offsetMs
        syncPrefs.edit().apply { if (offsetMs == 0L) remove(id) else putLong(id, offsetMs) }.apply()
    }

    private companion object {
        const val KEY_ENGLISH = "english_letters"
        const val MAX_SYNC_MS = 60_000L
    }

    sealed interface State {
        data object Idle : State
        data object Loading : State
        data object Error : State
        data class Loaded(val info: SongInfo, val videoId: String = "") : State
    }

    private val _state = MutableStateFlow<State>(State.Idle)
    val state: StateFlow<State> = _state.asStateFlow()

    private var loadedId: String? = null
    private var job: Job? = null

    /** Loads [videoId] unless it's already shown; [force] retries. */
    fun load(videoId: String, title: String, artist: String, durationMs: Long, force: Boolean = false) {
        if (videoId.isBlank() || (!force && videoId == loadedId && _state.value !is State.Error)) return
        loadedId = videoId
        _syncOffset.value = syncPrefs.getLong(videoId, 0L)
        job?.cancel()
        _state.value = State.Loading
        job = viewModelScope.launch {
            _state.value = try {
                State.Loaded(source.load(videoId, title, artist, durationMs), videoId).also { loaded ->
                    val lyrics = loaded.info.lyrics
                    Analytics.lyricsView(hasLyrics = lyrics != null, timed = lyrics?.synced == true, source = lyrics?.source)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                State.Error
            }
        }
    }
}
