package com.ramzmania.tubefy.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.ramzmania.tubefy.core.dataformatter.BasicResponse
import com.ramzmania.tubefy.core.dataformatter.StreamUrlData
import com.ramzmania.tubefy.core.yotubewebscrapper.scrapping.YoutubeJsonScrapping
import com.ramzmania.tubefy.data.ContextModule
import com.ramzmania.tubefy.data.Resource
import com.ramzmania.tubefy.data.dto.youtubestripper.ApiResponse
import com.ramzmania.tubefy.data.local.LocalRepositorySource
import com.ramzmania.tubefy.ui.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import org.schabi.newpipe.extractor.search.SearchInfo
import java.util.Arrays
import javax.inject.Inject

@HiltViewModel
class TubeFyViewModel @Inject constructor(val contextModule: ContextModule,val scrapping: YoutubeJsonScrapping, private val localRepositorySource: LocalRepositorySource):BaseViewModel() {

    init {
        viewModelScope.launch {
            scrapping.sharedJsonContent.collect { result ->
                setHtmlContent(result)
            }
        }
    }

    private val htmlContentPrivate = MutableLiveData<String>()
    val htmlContent: LiveData<String> get() = htmlContentPrivate

    private val formattedListPrivate = MutableLiveData<Resource<BasicResponse>>()
    val formattedList: LiveData<Resource<BasicResponse>> get() = formattedListPrivate

    private val streamUrlDataPrivate=MutableLiveData<Resource<StreamUrlData>>()
     val streamUrlData:LiveData<Resource<StreamUrlData>>get() = streamUrlDataPrivate


    private val newPipeSearchPrivate=MutableLiveData<Resource<SearchInfo>>()
    val newPipeSearch:LiveData<Resource<SearchInfo>>get() = newPipeSearchPrivate

    fun setHtmlContent(content: ApiResponse?) {
//        htmlContentPrivate.value = content
        viewModelScope.launch { localRepositorySource.manipulateYoutubeSearchStripData(content!!).collect{

            formattedListPrivate.value=it
        }}
    }

    fun getStreamUrl(videoId:String)
    {
        viewModelScope.launch {
            localRepositorySource.getStreamUrl(videoId).collect{
                streamUrlDataPrivate.value=it
            }
        }
    }

    fun startScrapping( searchQuery:String) {
        scrapping.fetchPageSource("https://www.youtube.com/results?search_query=$searchQuery")
    }


    fun searchNewPipePage()
    {
        val contentFilter = arrayOf<String>("music_songs")

        viewModelScope.launch {
            localRepositorySource.getPageSearch(0, "wwe", listOf(*contentFilter),"").collect{
                newPipeSearchPrivate.value=it
            }
        }
    }




}