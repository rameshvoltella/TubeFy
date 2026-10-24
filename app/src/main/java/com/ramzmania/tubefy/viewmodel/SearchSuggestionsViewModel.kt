package com.ramzmania.tubefy.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ramzmania.tubefy.data.youtube.YouTubeFeedSource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/** YouTube's search suggestions for the Search tab's box, fetched once typing pauses briefly. */
@HiltViewModel
class SearchSuggestionsViewModel @Inject constructor(private val source: YouTubeFeedSource) : ViewModel() {

    private val _suggestions = MutableStateFlow<List<String>>(emptyList())
    val suggestions: StateFlow<List<String>> = _suggestions.asStateFlow()
    private var job: Job? = null

    fun onQueryChanged(query: String) {
        job?.cancel()
        val text = query.trim()
        if (text.length < 2) {
            _suggestions.value = emptyList()
            return
        }
        job = viewModelScope.launch {
            delay(250L)
            _suggestions.value = runCatching { source.suggestions(text) }.getOrDefault(emptyList()).take(10)
        }
    }

    fun clear() {
        job?.cancel()
        _suggestions.value = emptyList()
    }
}
