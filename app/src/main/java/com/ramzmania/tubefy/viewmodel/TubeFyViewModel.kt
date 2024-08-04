package com.ramzmania.tubefy.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.ramzmania.tubefy.core.YoutubeCoreConstant
import com.ramzmania.tubefy.core.YoutubeCoreConstant.extractYoutubeVideoId
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



    private val streamUrlDataPrivate=MutableLiveData<Resource<StreamUrlData>>()
     val streamUrlData:LiveData<Resource<StreamUrlData>>get() = streamUrlDataPrivate

    private val youTubeSearchDataPrivate=MutableLiveData<Resource<TubeFyCoreUniversalData>>()
    val youTubeSearchData:LiveData<Resource<TubeFyCoreUniversalData>>get() = youTubeSearchDataPrivate


    fun setHtmlContent(content: ApiResponse?) {
//        htmlContentPrivate.value = content
        viewModelScope.launch { localRepositorySource.manipulateYoutubeSearchStripData(content!!).collect{

            youTubeSearchDataPrivate.value=it
        }}
    }

    fun getStreamUrl(videoId:String)
    {
        viewModelScope.launch {
            remoteRepositorySource.getStreamUrl(extractYoutubeVideoId(videoId
            )!!).collect{
                streamUrlDataPrivate.value=it
            }
        }
    }

    fun startWebScrapping( searchQuery:String) {
        scrapping.fetchPageSource("https://music.youtube.com/search?q=aavesham")
    }


    fun searchNewPipePage()
    {
        val contentFilter = arrayOf<String>("music_songs")

        viewModelScope.launch {
            remoteRepositorySource.getNewPipePageSearch(0, "aavesham", listOf(*contentFilter),"").collect{
                youTubeSearchDataPrivate.value=it
            }
        }
    }

    fun searchNewPipeNextPage(page: Page)
    {
        val contentFilter = arrayOf<String>("music_songs")

        viewModelScope.launch {
            remoteRepositorySource.getNewPipePageNextSearch(0, "aavesham", listOf(*contentFilter),"",page).collect{
                youTubeSearchDataPrivate.value=it
            }
        }
    }


    fun searchYoutubeV3()
    {

        viewModelScope.launch {
            remoteRepositorySource.requestYoutubeV3("snippet", "aavesham", nextYoutubeV3PageToken).collect{
                youTubeSearchDataPrivate.value=it
            }
        }
    }




}