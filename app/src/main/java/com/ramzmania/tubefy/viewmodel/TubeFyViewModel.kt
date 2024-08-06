package com.ramzmania.tubefy.viewmodel

import MusicCarouselContent
import MusicHomeResponse
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.ramzmania.tubefy.core.YoutubeCoreConstant
import com.ramzmania.tubefy.core.YoutubeCoreConstant.extractYoutubeVideoId
import com.ramzmania.tubefy.core.dataformatter.dto.StreamUrlData
import com.ramzmania.tubefy.core.dataformatter.dto.TubeFyCoreUniversalData
import com.ramzmania.tubefy.core.yotubewebextractor.YoutubeJsonScrapping
import com.ramzmania.tubefy.core.yotubewebextractor.YoutubeScrapType
import com.ramzmania.tubefy.data.ContextModule
import com.ramzmania.tubefy.data.Resource
import com.ramzmania.tubefy.data.dto.youtubemusic.playlist.YoutubeMusicPlayListContent
import com.ramzmania.tubefy.data.dto.youtubestripper.ApiResponse
import com.ramzmania.tubefy.data.local.LocalRepositorySource
import com.ramzmania.tubefy.data.remote.RemoteRepositorySource
import com.ramzmania.tubefy.ui.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import org.schabi.newpipe.extractor.Page
import javax.inject.Inject

@HiltViewModel
class TubeFyViewModel @Inject constructor(
    val contextModule: ContextModule,
    val scrapping: YoutubeJsonScrapping,
    private val localRepositorySource: LocalRepositorySource,
    private val remoteRepositorySource: RemoteRepositorySource
) : BaseViewModel() {

    private var nextYoutubeV3PageToken: String? = null

    init {
        viewModelScope.launch {
            scrapping.sharedJsonContent.collect { result ->
                setHtmlContent(result)
            }
        }

        viewModelScope.launch {
            scrapping.sharedJsonMusicHomeContent.collect { result ->
                setHtmlMusicContent(result)
            }
        }

        viewModelScope.launch {
            scrapping.sharedJsonMusicHomePlayListContent.collect { result ->
                setHtmlMusicPlayListContent(result)
            }
        }
    }

    private val htmlContentPrivate = MutableLiveData<String>()
    val htmlContent: LiveData<String> get() = htmlContentPrivate


    private val streamUrlDataPrivate = MutableLiveData<Resource<StreamUrlData>>()
    val streamUrlData: LiveData<Resource<StreamUrlData>> get() = streamUrlDataPrivate

    private val youTubeSearchDataPrivate = MutableLiveData<Resource<TubeFyCoreUniversalData>>()
    val youTubeSearchData: LiveData<Resource<TubeFyCoreUniversalData>> get() = youTubeSearchDataPrivate


    fun setHtmlContent(content: ApiResponse?) {
//        htmlContentPrivate.value = content
        viewModelScope.launch {
            localRepositorySource.manipulateYoutubeSearchStripData(content!!).collect {

                youTubeSearchDataPrivate.value = it
            }
        }
    }

    fun setHtmlMusicContent(content: MusicHomeResponse?) {
//        htmlContentPrivate.value = content
        val videoInfoList =
            extractVideoInfo(content!!.contents?.singleColumnBrowseResultsRenderer?.tabs?.firstOrNull()?.tabRenderer?.content?.sectionListRenderer?.contents?.flatMap {
                it.musicCarouselShelfRenderer?.contents ?: emptyList()
            })

        videoInfoList.forEach {
            println("Video ID: ${it.videoId}, Playlist ID: ${it.playlistId}, Thumbnail: ${it.thumbnail}, Title: ${it.title}, Subtitle: ${it.subtitle}")
        }

    }

    fun setHtmlMusicPlayListContent(content: YoutubeMusicPlayListContent?) {
        if (content != null) {
//            Log.d("dad",""+ content.contents?.twoColumnBrowseResultsRenderer?.contents?.sectionListRenderer?.contents?.get(0)?.musicPlaylistShelfRenderer!!.contents?.get(0)?.musicResponsiveListItemRenderer?.flexColumns?.get(0)?.musicResponsiveListItemFlexColumnRenderer!!.text!!.runs?.get(0)!!.text)
//            Log.d("dad",""+ content.contents?.twoColumnBrowseResultsRenderer?.contents?.sectionListRenderer?.contents?.get(0)?.musicPlaylistShelfRenderer!!.contents?.get(0)?.musicResponsiveListItemRenderer?.flexColumns?.get(0)?.musicResponsiveListItemFlexColumnRenderer!!.text!!.runs?.get(0)!!.navigationEndpoint!!.watchEndpoint!!.videoId)
//            Log.d("dad",""+ content.contents?.twoColumnBrowseResultsRenderer?.contents?.sectionListRenderer?.contents?.get(0)?.musicPlaylistShelfRenderer!!.contents?.get(0)?.musicResponsiveListItemRenderer?.thumbnail?.musicThumbnailRenderer?.thumbnail?.thumbnails?.get(0)!!.url)
//
            for (sectionContents in content.contents?.twoColumnBrowseResultsRenderer?.contents?.sectionListRenderer?.contents!!) {
                for (musicPlaylistShelfRendererContents in sectionContents.musicPlaylistShelfRenderer?.contents!!) {
//                    musicPlaylistShelfRendererConten
                    var videoName: String = ""
                    var videoId: String? = ""
                    var tnhumpNail: String? = null
                    for (flexColumList in musicPlaylistShelfRendererContents.musicResponsiveListItemRenderer?.flexColumns!!) {
                        for (musicResponsiveRunData in flexColumList.musicResponsiveListItemFlexColumnRenderer?.text?.runs!!) {
                            if (musicResponsiveRunData.navigationEndpoint?.watchEndpoint != null) {
                                videoId =
                                    musicResponsiveRunData.navigationEndpoint.watchEndpoint.videoId
                            }
                            videoName=videoName+"\n"+ musicResponsiveRunData.text
                        }

                    }
                    for (thumpDetails in musicPlaylistShelfRendererContents.musicResponsiveListItemRenderer?.thumbnail?.musicThumbnailRenderer?.thumbnail?.thumbnails!!) {
                        if (tnhumpNail == null) {
                            tnhumpNail = thumpDetails.url
                        }
                        else
                        {
                            break
                        }
                    }

                    Log.d("fulldeatils","video->"+videoId+"<tnhumpNail>"+tnhumpNail+"<><name"+videoName)
                }


            }


        }
    }

    fun getStreamUrl(videoId: String) {
        viewModelScope.launch {
            remoteRepositorySource.getStreamUrl(
                extractYoutubeVideoId(
                    videoId
                )!!
            ).collect {
                streamUrlDataPrivate.value = it
            }
        }
    }

    fun startWebScrappingYO(searchQuery: String) {
//        scrapping.fetchPageSource("https://www.youtube.com/results?search_query=$searchQuery")
    }

    fun startWebMusicHomeScrapping(searchQuery: String) {
//        scrapping.fetchPageSource("https://music.youtube.com/")
    }

    fun startWebScrapping(searchQuery: String) {
//        scrapping.fetchPageSource("https://music.youtube.com/", YoutubeScrapType.YOUTUBE_MUSIC)
        scrapping.fetchPageSource(
            "https://music.youtube.com/playlist?list=RDCLAK5uy_n6_pc7SPVqtuPg_cK3AUxh9AbQP-_Qh-w",
            YoutubeScrapType.YOUTUBE_PLAYLIST
        )

    }


    fun searchNewPipePage() {
        val contentFilter = arrayOf<String>("music_songs")

        viewModelScope.launch {
            remoteRepositorySource.getNewPipePageSearch(0, "aavesham", listOf(*contentFilter), "")
                .collect {
                    youTubeSearchDataPrivate.value = it
                }
        }
    }

    fun searchNewPipeNextPage(page: Page) {
        val contentFilter = arrayOf<String>("music_songs")

        viewModelScope.launch {
            remoteRepositorySource.getNewPipePageNextSearch(
                0,
                "aavesham",
                listOf(*contentFilter),
                "",
                page
            ).collect {
                youTubeSearchDataPrivate.value = it
            }
        }
    }


    fun searchYoutubeV3() {

        viewModelScope.launch {
            remoteRepositorySource.requestYoutubeV3("snippet", "aavesham", nextYoutubeV3PageToken)
                .collect {
                    youTubeSearchDataPrivate.value = it
                }
        }
    }

    data class SearchHomeScreen(
        val playList: String,
        val videoId: String,
        val title: String,
        val subTitle: String,
        val thump: String
    )

    data class VideoInfo(
        val videoId: String?,
        val playlistId: String?,
        val thumbnail: String?,
        val title: String?,
        val subtitle: String?
    )

    fun extractVideoInfo(contents: List<MusicCarouselContent>?): List<VideoInfo> {
        val videoInfoList = mutableListOf<VideoInfo>()

        contents?.forEach { content ->
            val musicTwoRowItemRenderer = content.musicTwoRowItemRenderer
            val musicResponsiveListItemRenderer = content.musicResponsiveListItemRenderer

            if (musicTwoRowItemRenderer != null) {
                val videoId = musicTwoRowItemRenderer.navigationEndpoint?.browseEndpoint?.browseId
                val playlistId =
                    musicTwoRowItemRenderer.menu?.menuRenderer?.items?.firstOrNull()?.menuNavigationItemRenderer?.navigationEndpoint?.watchPlaylistEndpoint?.playlistId
                val thumbnail =
                    musicTwoRowItemRenderer.thumbnailRenderer?.musicThumbnailRenderer?.thumbnail?.thumbnails?.firstOrNull()?.url
                val title = musicTwoRowItemRenderer.title?.runs?.firstOrNull()?.text
                val subtitle = musicTwoRowItemRenderer.subtitle?.runs?.firstOrNull()?.text

                videoInfoList.add(VideoInfo(videoId, playlistId, thumbnail, title, subtitle))
            }

            if (musicResponsiveListItemRenderer != null) {
                val videoId = musicResponsiveListItemRenderer.flexColumns?.flatMap {
                    it.musicResponsiveListItemFlexColumnRenderer?.text?.runs ?: emptyList()
                }
                    ?.firstOrNull { it.navigationEndpoint?.watchEndpoint?.videoId != null }?.navigationEndpoint?.watchEndpoint?.videoId
                val playlistId = musicResponsiveListItemRenderer.flexColumns?.flatMap {
                    it.musicResponsiveListItemFlexColumnRenderer?.text?.runs ?: emptyList()
                }
                    ?.firstOrNull { it.navigationEndpoint?.watchEndpoint?.playlistId != null }?.navigationEndpoint?.watchEndpoint?.playlistId
                val thumbnail =
                    musicResponsiveListItemRenderer.thumbnail?.musicThumbnailRenderer?.thumbnail?.thumbnails?.firstOrNull()?.url
                val title = musicResponsiveListItemRenderer.flexColumns?.flatMap {
                    it.musicResponsiveListItemFlexColumnRenderer?.text?.runs ?: emptyList()
                }
                    ?.firstOrNull { it.text != null }?.text
                val subtitle =
                    "" // Assuming subtitle is not available in MusicResponsiveListItemRenderer
//              "kk".toInt()
                videoInfoList.add(VideoInfo(videoId, playlistId, thumbnail, title, subtitle))
            }
        }

        return videoInfoList
    }

}