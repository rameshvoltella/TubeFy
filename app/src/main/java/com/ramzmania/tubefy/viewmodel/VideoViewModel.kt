package com.ramzmania.tubefy.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ramzmania.tubefy.core.download.Downloads
import com.ramzmania.tubefy.data.Resource
import com.ramzmania.tubefy.data.database.DatabaseRepositorySource
import com.ramzmania.tubefy.data.youtube.VideoHistory
import com.ramzmania.tubefy.data.youtube.YouTubeFeedSource
import com.ramzmania.tubefy.data.youtube.YouTubeVideoDetails
import com.ramzmania.tubefy.database.CustomPlaylist
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

/** The video page: details + related videos of the video on screen, and its "Save" state. */
@HiltViewModel
class VideoViewModel @Inject constructor(
    private val source: YouTubeFeedSource,
    private val database: DatabaseRepositorySource,
    private val history: VideoHistory
) : ViewModel() {

    sealed interface DetailsState {
        data object Loading : DetailsState
        data object Error : DetailsState
        data class Loaded(val details: YouTubeVideoDetails) : DetailsState
    }

    private val _details = MutableStateFlow<DetailsState>(DetailsState.Loading)
    val details: StateFlow<DetailsState> = _details.asStateFlow()

    private val _saved = MutableStateFlow(false)
    val saved: StateFlow<Boolean> = _saved.asStateFlow()

    private var loadedId: String? = null
    private var job: Job? = null

    /**
     * Loads [videoId] unless it's already shown; [force] retries. It goes into the YouTube tab's
     * "Recent" list right away, then again with the real title / channel once they are loaded.
     */
    fun load(videoId: String, title: String, thumbnail: String, channel: String, force: Boolean = false) {
        if (videoId.isBlank() || (!force && videoId == loadedId && _details.value !is DetailsState.Error)) return
        loadedId = videoId
        job?.cancel()
        _details.value = DetailsState.Loading
        history.record(videoId, title, channel, thumbnail)
        job = viewModelScope.launch {
            _saved.value = isSaved(videoId)
            _details.value = try {
                val details = source.details(videoId)
                // Only if it's still the most recent one (the user may have moved on)
                if (history.videos.value.firstOrNull()?.id == videoId) {
                    history.record(videoId, details.title, details.channel, thumbnail, details.durationSeconds)
                }
                DetailsState.Loaded(details)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                DetailsState.Error
            }
        }
    }

    /** Adds the video to (or removes it from) the "YouTube Videos" playlist. */
    fun toggleSave(videoId: String, title: String, thumbnail: String) {
        viewModelScope.launch {
            if (isSaved(videoId)) {
                database.deleteSongFromPlayList(SAVED_PLAYLIST, videoId).first()
            } else {
                database.addToPlaylist(
                    CustomPlaylist(playlistName = SAVED_PLAYLIST, videoId = videoId, videoThump = thumbnail, videoName = title)
                ).first()
            }
            _saved.value = isSaved(videoId)
        }
    }

    private suspend fun isSaved(videoId: String): Boolean =
        (database.getSpecificPlayList(SAVED_PLAYLIST).first() as? Resource.Success)?.data.orEmpty()
            .any { it?.videoId == videoId }

    companion object {
        const val SAVED_PLAYLIST = Downloads.YOUTUBE_VIDEOS
    }
}
