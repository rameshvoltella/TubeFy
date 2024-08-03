package com.ramzmania.tubefy.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.ramzmania.tubefy.core.dataformatter.dto.BasicResponse
import com.ramzmania.tubefy.core.dataformatter.dto.StreamUrlData
import com.ramzmania.tubefy.core.dataformatter.dto.TubeFyCoreUniversalData
import com.ramzmania.tubefy.core.yotubewebextractor.YoutubeJsonScrapping
import com.ramzmania.tubefy.data.ContextModule
import com.ramzmania.tubefy.data.Resource
import com.ramzmania.tubefy.data.dto.youtubestripper.ApiResponse
import com.ramzmania.tubefy.data.local.LocalRepositorySource
import com.ramzmania.tubefy.data.remote.RemoteRepositorySource
import com.ramzmania.tubefy.ui.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import org.schabi.newpipe.extractor.Page
import javax.inject.Inject

@HiltViewModel
class TubeFyViewModel @Inject constructor(val contextModule: ContextModule, val scrapping: YoutubeJsonScrapping, private val localRepositorySource: LocalRepositorySource,private val remoteRepositorySource: RemoteRepositorySource):BaseViewModel() {

    private var nextYoutubeV3PageToken: String? = null

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


    private val newPipeSearchPrivate=MutableLiveData<Resource<TubeFyCoreUniversalData>>()
    val newPipeSearch:LiveData<Resource<TubeFyCoreUniversalData>>get() = newPipeSearchPrivate

    private val newPipeSearchNextPrivate=MutableLiveData<Resource<TubeFyCoreUniversalData>>()
    val newPipeSearchNext:LiveData<Resource<TubeFyCoreUniversalData>>get() = newPipeSearchNextPrivate

    private val youtubeV3SearchPrivate=MutableLiveData<Resource<TubeFyCoreUniversalData>>()
    val youtubeV3Search:LiveData<Resource<TubeFyCoreUniversalData>>get() = youtubeV3SearchPrivate


    fun setHtmlContent(content: ApiResponse?) {
//        htmlContentPrivate.value = content
        viewModelScope.launch { localRepositorySource.manipulateYoutubeSearchStripData(content!!).collect{

            formattedListPrivate.value=it
        }}
    }

    fun getStreamUrl(videoId:String)
    {
        viewModelScope.launch {
            remoteRepositorySource.getStreamUrl(videoId).collect{
                streamUrlDataPrivate.value=it
            }
        }
    }

    fun startWebScrapping( searchQuery:String) {
        scrapping.fetchPageSource("https://www.youtube.com/results?search_query=$searchQuery")
    }


    fun searchNewPipePage()
    {
        val contentFilter = arrayOf<String>("music_songs")

        viewModelScope.launch {
            remoteRepositorySource.getNewPipePageSearch(0, "aavesham", listOf(*contentFilter),"").collect{
                newPipeSearchPrivate.value=it
            }
        }
    }

    fun searchNewPipeNextPage(page: Page)
    {
        val contentFilter = arrayOf<String>("music_songs")

        viewModelScope.launch {
            remoteRepositorySource.getNewPipePageNextSearch(0, "aavesham", listOf(*contentFilter),"",page).collect{
                newPipeSearchNextPrivate.value=it
            }
        }
    }


    fun searchYoutubeV3()
    {

        viewModelScope.launch {
            remoteRepositorySource.requestYoutubeV3("snippet", "aavesham", nextYoutubeV3PageToken).collect{
                youtubeV3SearchPrivate.value=it
            }
        }
    }




}