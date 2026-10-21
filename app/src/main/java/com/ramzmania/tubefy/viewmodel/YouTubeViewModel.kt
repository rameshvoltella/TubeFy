package com.ramzmania.tubefy.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ramzmania.tubefy.data.youtube.Region
import com.ramzmania.tubefy.data.youtube.RegionStore
import com.ramzmania.tubefy.data.youtube.PlayedVideo
import com.ramzmania.tubefy.data.youtube.StateLanguages
import com.ramzmania.tubefy.data.youtube.VideoHistory
import com.ramzmania.tubefy.data.youtube.YouTubeCursor
import com.ramzmania.tubefy.data.youtube.YouTubeFeed
import com.ramzmania.tubefy.data.youtube.YouTubeFeedSource
import com.ramzmania.tubefy.data.youtube.YouTubeItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/** One list on the YouTube tab (a chip, or search results) and its paging. */
data class FeedState(
    val items: List<YouTubeItem> = emptyList(),
    /** First page loading (or reloading after an error / pull-to-refresh). */
    val loading: Boolean = false,
    val loadingMore: Boolean = false,
    val refreshing: Boolean = false,
    val error: Boolean = false,
    /** No more pages. */
    val ended: Boolean = false,
    internal val next: YouTubeCursor? = null
)

@HiltViewModel
class YouTubeViewModel @Inject constructor(
    private val source: YouTubeFeedSource,
    private val regionStore: RegionStore,
    private val videoHistory: VideoHistory
) : ViewModel() {

    /** Videos played on the video page, newest first (the "Recent" list). */
    val recent: StateFlow<List<PlayedVideo>> = videoHistory.videos

    private val _showRecent = MutableStateFlow(false)
    val showRecent: StateFlow<Boolean> = _showRecent.asStateFlow()

    fun setShowRecent(show: Boolean) {
        _showRecent.value = show
    }

    fun removeRecent(id: String) = videoHistory.remove(id)

    fun clearRecent() = videoHistory.clear()

    val region: StateFlow<Region> = regionStore.region

    private val _feeds = MutableStateFlow<Map<String, FeedState>>(emptyMap())
    val feeds: StateFlow<Map<String, FeedState>> = _feeds.asStateFlow()

    private val _selected = MutableStateFlow<YouTubeFeed>(defaultFeed())
    val selected: StateFlow<YouTubeFeed> = _selected.asStateFlow()

    /** The submitted search, or null when showing the chips. */
    private val _search = MutableStateFlow<YouTubeFeed.Search?>(null)
    val search: StateFlow<YouTubeFeed.Search?> = _search.asStateFlow()

    private val _detecting = MutableStateFlow(false)
    val detecting: StateFlow<Boolean> = _detecting.asStateFlow()

    private val _showStatePrompt = MutableStateFlow(regionStore.region.value.state == null && !regionStore.promptDismissed)
    val showStatePrompt: StateFlow<Boolean> = _showStatePrompt.asStateFlow()

    private val jobs = mutableMapOf<String, Job>()

    /** Chips, with "<state>" first when the state is known. */
    fun chips(region: Region): List<YouTubeFeed> = listOfNotNull(
        stateFeed(region),
        YouTubeFeed.Trending,
        YouTubeFeed.Music,
        YouTubeFeed.Mixes(StateLanguages.languageOf(region.state)),
        YouTubeFeed.Live,
        YouTubeFeed.Gaming
    )

    init {
        load(_selected.value)
    }

    fun select(feed: YouTubeFeed) {
        _selected.value = feed
        load(feed)
    }

    private val _suggestions = MutableStateFlow<List<String>>(emptyList())
    /** Search suggestions for the text being typed. */
    val suggestions: StateFlow<List<String>> = _suggestions.asStateFlow()
    private var suggestJob: Job? = null

    /** Call as the user types: fetches suggestions once typing pauses briefly. */
    fun onQueryChanged(query: String) {
        suggestJob?.cancel()
        val text = query.trim()
        if (text.length < MIN_SUGGEST_CHARS) {
            _suggestions.value = emptyList()
            return
        }
        suggestJob = viewModelScope.launch {
            kotlinx.coroutines.delay(SUGGEST_DELAY_MS)
            _suggestions.value = runCatching { source.suggestions(text) }.getOrDefault(emptyList()).take(MAX_SUGGESTIONS)
        }
    }

    fun clearSuggestions() {
        suggestJob?.cancel()
        _suggestions.value = emptyList()
    }

    fun submitSearch(query: String) {
        val text = query.trim()
        if (text.isEmpty()) return
        clearSuggestions()
        val feed = YouTubeFeed.Search(text)
        _showRecent.value = false
        _search.value = feed
        load(feed, force = true)
    }

    fun clearSearch() {
        _search.value = null
    }

    /** Loads the first page unless it's already there; [force] reloads it. */
    fun load(feed: YouTubeFeed, force: Boolean = false) {
        val current = _feeds.value[feed.key]
        if (!force && current != null && (current.items.isNotEmpty() || current.loading)) return
        start(feed, refreshing = false)
    }

    /** Pull-to-refresh: reloads while keeping the current list on screen. */
    fun refresh(feed: YouTubeFeed) = start(feed, refreshing = true)

    fun loadMore(feed: YouTubeFeed) {
        val state = _feeds.value[feed.key] ?: return
        val next = state.next ?: return
        if (state.loading || state.loadingMore || state.refreshing || state.ended) return
        update(feed) { it.copy(loadingMore = true, error = false) }
        jobs[feed.key] = viewModelScope.launch {
            try {
                val page = source.load(next)
                update(feed) { old ->
                    val known = old.items.mapTo(HashSet()) { it.id }
                    old.copy(
                        items = old.items + page.items.filter { it.id !in known },
                        loadingMore = false,
                        next = page.next,
                        ended = page.next == null
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                update(feed) { it.copy(loadingMore = false, error = true) }
            }
        }
    }

    private fun start(feed: YouTubeFeed, refreshing: Boolean) {
        jobs.remove(feed.key)?.cancel()
        update(feed) {
            if (refreshing) it.copy(refreshing = true, error = false)
            else FeedState(loading = true)
        }
        jobs[feed.key] = viewModelScope.launch {
            try {
                val page = source.load(source.start(feed, region.value.country))
                update(feed) {
                    FeedState(items = page.items.distinctBy { item -> item.id }, next = page.next, ended = page.next == null)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                update(feed) { it.copy(loading = false, refreshing = false, error = true) }
            }
        }
    }

    // --- Region ---

    private val _detectFailed = MutableStateFlow(false)
    /** True when detection found nothing (offline, location off): the UI offers the state list. */
    val detectFailed: StateFlow<Boolean> = _detectFailed.asStateFlow()

    /** Call after the location permission was granted. */
    fun detectState() {
        if (_detecting.value) return
        _detecting.value = true
        viewModelScope.launch {
            val state = runCatching { regionStore.detectState() }.getOrNull()
            _detecting.value = false
            if (state != null) onStateChanged() else _detectFailed.value = true
        }
    }

    fun consumeDetectFailed() {
        _detectFailed.value = false
    }

    fun pickState(state: String) {
        regionStore.setState(state)
        onStateChanged()
    }

    fun dismissStatePrompt() {
        regionStore.promptDismissed = true
        _showStatePrompt.value = false
    }

    private fun onStateChanged() {
        _showStatePrompt.value = false
        regionStore.promptDismissed = true
        // Mixes follow the state's language too
        _feeds.update { feeds -> feeds.filterKeys { !it.startsWith("state:") && it != YouTubeFeed.Mixes(null).key } }
        stateFeed(region.value)?.let(::select)
    }

    private fun stateFeed(region: Region): YouTubeFeed.InState? {
        val state = region.state ?: return null
        val language = StateLanguages.languageOf(state) ?: return null
        return YouTubeFeed.InState(state, language)
    }

    private fun defaultFeed(): YouTubeFeed = stateFeed(regionStore.region.value) ?: YouTubeFeed.Trending

    private companion object {
        const val MIN_SUGGEST_CHARS = 2
        const val SUGGEST_DELAY_MS = 250L
        const val MAX_SUGGESTIONS = 10
    }

    private fun update(feed: YouTubeFeed, change: (FeedState) -> FeedState) {
        _feeds.update { it + (feed.key to change(it[feed.key] ?: FeedState())) }
    }
}
