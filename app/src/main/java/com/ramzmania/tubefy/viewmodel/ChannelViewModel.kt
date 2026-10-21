package com.ramzmania.tubefy.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ramzmania.tubefy.data.youtube.YouTubeChannel
import com.ramzmania.tubefy.data.youtube.YouTubeFeed
import com.ramzmania.tubefy.data.youtube.YouTubeFeedSource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/** A channel page: its header and its videos (newest first, more as you scroll). */
@HiltViewModel
class ChannelViewModel @Inject constructor(private val source: YouTubeFeedSource) : ViewModel() {

    private val _channel = MutableStateFlow<YouTubeChannel?>(null)
    val channel: StateFlow<YouTubeChannel?> = _channel.asStateFlow()

    private val _videos = MutableStateFlow(FeedState(loading = true))
    val videos: StateFlow<FeedState> = _videos.asStateFlow()

    private var feed: YouTubeFeed.Channel? = null
    private var job: Job? = null

    fun load(url: String, name: String, force: Boolean = false) {
        if (url.isBlank() || (!force && feed?.url == url && !_videos.value.error)) return
        val channelFeed = YouTubeFeed.Channel(url, name)
        feed = channelFeed
        job?.cancel()
        _videos.value = FeedState(loading = true)
        job = viewModelScope.launch {
            runCatching { source.channel(url) }.onSuccess { _channel.value = it }
            try {
                val page = source.load(source.start(channelFeed, ""))
                _videos.value = FeedState(items = page.items.distinctBy { it.id }, next = page.next, ended = page.next == null)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _videos.value = FeedState(error = true)
            }
        }
    }

    fun loadMore() {
        val state = _videos.value
        val next = state.next ?: return
        if (state.loading || state.loadingMore || state.ended) return
        _videos.update { it.copy(loadingMore = true, error = false) }
        job = viewModelScope.launch {
            try {
                val page = source.load(next)
                _videos.update { old ->
                    val known = old.items.mapTo(HashSet()) { it.id }
                    old.copy(items = old.items + page.items.filter { it.id !in known }, loadingMore = false, next = page.next, ended = page.next == null)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _videos.update { it.copy(loadingMore = false, error = true) }
            }
        }
    }
}
