package com.ramzmania.tubefy.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ramzmania.tubefy.core.yotubesearch.scrapping.YoutubeJsonScrapping
import com.ramzmania.tubefy.data.ContextModule
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TubeFyViewModel @Inject constructor(val contextModule: ContextModule,val scrapping: YoutubeJsonScrapping):ViewModel() {

    init {
        viewModelScope.launch {
            scrapping.sharedJsonContent.collect { result ->
                setHtmlContent(result)
            }
        }
    }

    private val htmlContentPrivate = MutableLiveData<String>()
    val htmlContent: LiveData<String> get() = htmlContentPrivate

    fun setHtmlContent(content: String) {
        htmlContentPrivate.value = content
    }

    fun startScrapping() {
        scrapping.fetchPageSource("https://www.youtube.com/results?search_query=wwe")
    }


}