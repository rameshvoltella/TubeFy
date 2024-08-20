package com.ramzmania.tubefy.viewmodel


import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import com.ramzmania.tubefy.core.YoutubeCoreConstant.extractYoutubeVideoId
import com.ramzmania.tubefy.data.dto.base.searchformat.StreamUrlData
import com.ramzmania.tubefy.data.dto.base.searchformat.TubeFyCoreUniversalData
import com.ramzmania.tubefy.core.extractors.yotubewebextractor.YoutubeJsonScrapping
import com.ramzmania.tubefy.core.extractors.yotubewebextractor.YoutubeScrapType
import com.ramzmania.tubefy.data.ContextModule
import com.ramzmania.tubefy.data.Resource
import com.ramzmania.tubefy.data.dto.base.playlist.PlayListCategory
import com.ramzmania.tubefy.data.dto.home.HomePageResponse
import com.ramzmania.tubefy.data.dto.base.playlist.PlayListData
import com.ramzmania.tubefy.data.dto.base.searchformat.TubeFyCoreTypeData
import com.ramzmania.tubefy.data.dto.home.youtubei.YoutubeiHomeFirstResponse
import com.ramzmania.tubefy.data.dto.youtubemusic.category.MusicCategoryPlayListBase
import com.ramzmania.tubefy.data.dto.youtubemusic.category.YtMusicCategoryBase
import com.ramzmania.tubefy.data.dto.youtubemusic.category.YtMusicCategoryContent
import com.ramzmania.tubefy.data.dto.youtubemusic.playlist.YoutubeMusicPlayListContent
import com.ramzmania.tubefy.data.dto.youtubemusic.playlist.categoryplaylist.CategoryPlayListRoot
import com.ramzmania.tubefy.data.dto.youtubestripper.ApiResponse
import com.ramzmania.tubefy.data.dto.youtubestripper.MusicHomeResponse2
import com.ramzmania.tubefy.data.local.LocalRepositorySource
import com.ramzmania.tubefy.data.remote.RemoteRepositorySource
import com.ramzmania.tubefy.player.PlayListSingleton
import com.ramzmania.tubefy.player.YoutubePlayerPlaylistListModel
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
    private var reloadHomeScreen=true
    var youtubePlayerPlaylistListModel: YoutubePlayerPlaylistListModel?=null
    var koko: String?="koko"


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

        viewModelScope.launch {
            scrapping.sharedJsonMusicCategoryPlayListContent.collect { result ->
                setHtmlMusicCategoryContent(result)
            }
        }
    }



    private val htmlContentPrivate = MutableLiveData<String>()
    val htmlContent: LiveData<String> get() = htmlContentPrivate


    private val streamUrlDataPrivate = MutableLiveData<Resource<StreamUrlData>>()
    val streamUrlData: LiveData<Resource<StreamUrlData>> get() = streamUrlDataPrivate

    private val youTubeSearchDataPrivate = MutableLiveData<Resource<TubeFyCoreUniversalData>>()
    val youTubeSearchData: LiveData<Resource<TubeFyCoreUniversalData>> get() = youTubeSearchDataPrivate

    private val youTubeMusicHomeDataPrivate = MutableLiveData<Resource<List<HomePageResponse?>>>()
    val youTubeMusicHomeData: LiveData<Resource<List<HomePageResponse?>>> get() = youTubeMusicHomeDataPrivate

    private val youTubeMusicHomeDefaultDataPrivate = MutableLiveData<Resource<List<HomePageResponse?>>>()
    val youTubeMusicHomeDefaultData: LiveData<Resource<List<HomePageResponse?>>> get() = youTubeMusicHomeDefaultDataPrivate


    private val youTubePlayListPrivate = MutableLiveData<Resource<PlayListData>>()
    val youTubePlayListData: LiveData<Resource<PlayListData>> get() = youTubePlayListPrivate

    private val youTubePlayListBulkPrivate = MutableLiveData<Resource<List<MediaItem>>>()
    val youTubePlayListBulkData: LiveData<Resource<List<MediaItem>>> get() = youTubePlayListBulkPrivate

    private val youTubeMusicCategoryDataPrivate = MutableLiveData<Resource<List<PlayListCategory?>>>()
    val youTubeMusicCategoryData: LiveData<Resource<List<PlayListCategory?>>> get() = youTubeMusicCategoryDataPrivate


    private val youTubeCategoryPlayListPrivate = MutableLiveData<Resource<List<MusicCategoryPlayListBase?>>>()
    val youTubeCategoryPlayList: LiveData<Resource<List<MusicCategoryPlayListBase?>>> get() = youTubeCategoryPlayListPrivate


    private val youTubeiMusicHomePrivate = MutableLiveData<Resource<YoutubeiHomeFirstResponse>>()
    val youTubeiMusicHomeData: LiveData<Resource<YoutubeiHomeFirstResponse>> get() = youTubeiMusicHomePrivate

    fun setHtmlContent(content: ApiResponse?) {
//        htmlContentPrivate.value = content
        viewModelScope.launch {
            localRepositorySource.manipulateYoutubeSearchStripData(content!!).collect {

                youTubeSearchDataPrivate.value = it
            }
        }
    }

    fun setHtmlMusicContent(content: MusicHomeResponse2?) {
//        htmlContentPrivate.value = content
    /*    val videoInfoList =
            extractVideoInfo(content!!.contents?.singleColumnBrowseResultsRenderer?.tabs?.firstOrNull()?.tabRenderer?.content?.sectionListRenderer?.contents?.flatMap {
                it.musicCarouselShelfRenderer?.contents ?: emptyList()
            })

        videoInfoList.forEach {
            println("Video ID: ${it.videoId}, Playlist ID: ${it.playlistId}, Thumbnail: ${it.thumbnail}, Title: ${it.title}, Subtitle: ${it.subtitle}")
        }*/
        viewModelScope.launch {
            localRepositorySource.manipulateYoutubeMusicHomeStripData(content!!).collect {

                youTubeMusicHomeDataPrivate.value = it
            }
        }

    }

    fun loadDefaultHomeData()
    {
        Log.d("loading","called")

        viewModelScope.launch {
            localRepositorySource.loadDefaultHomePageData().collect {
                Log.d("loading","came")

                youTubeMusicHomeDefaultDataPrivate.value = it
            }
        }
    }

    fun loadPlayList(playListUrl:String)
    {
        if(!playListUrl.startsWith("https:"))
        {
            viewModelScope.launch {
                remoteRepositorySource.getPlayListInfo("https://music.youtube.com/playlist?list=$playListUrl").collect {
                    youTubePlayListPrivate.value = it
                }
            }
        }
        else{
            viewModelScope.launch {
                remoteRepositorySource.getPlayListInfo(playListUrl).collect {
                    youTubePlayListPrivate.value = it
                }
            }
        }


    }


    fun getBulkStreamUrl()
    {
//        Log.d("bulk calling","bulk"+youTubePlayListBulkData)
        Log.d("bulkmode","added 2222"+koko)

        if(PlayListSingleton.getDataList()!=null&&PlayListSingleton.getDataList()?.playListData?.size!!>0) {
            viewModelScope.launch {
                Log.d("bulkmode","new arra"+PlayListSingleton.getDataList()?.playListData?.size)
                 var list=PlayListSingleton.getDataList()?.playListData?.take(2)
                remoteRepositorySource.getStreamBulkUrl(YoutubePlayerPlaylistListModel(list!!)).collect {
                    youTubePlayListBulkPrivate.value = it
                    if (youTubePlayListBulkPrivate.value is Resource.Success) {
                        var list= PlayListSingleton.getDataList()?.playListData?.drop(2)
                        list?.let { data -> PlayListSingleton.addData(data) }
                        getBulkStreamUrl()
                    }
                }
            }
        }else
        {
            Log.d("bulkmode","added 3333333")

        }
    }

    fun callCategoryPlayList(browserId:String,playerListId:String)
    {
//        Log.d("bulk calling","bulk"+youTubePlayListBulkData)

            viewModelScope.launch {

                remoteRepositorySource.getCategoryPlayList(browserId,playerListId).collect {
                    youTubeCategoryPlayListPrivate.value = it

                }
            }

    }
    fun callCategoryPlayList()
    {
//        Log.d("bulk calling","bulk"+youTubePlayListBulkData)

        viewModelScope.launch {

            remoteRepositorySource.getCategoryPlayList("FEmusic_moods_and_genres_category","ggMPOg1uX1JOQWZFeDByc2Jm").collect {
                youTubeCategoryPlayListPrivate.value = it

            }
        }

    }


    fun callYoutubeiHome()
    {
//        Log.d("bulk calling","bulk"+youTubePlayListBulkData)

        viewModelScope.launch {

            remoteRepositorySource.getMusicHomeYoutubei().collect {
                youTubeiMusicHomePrivate.value = it

            }
        }

    }

    fun setCurrentPlayListData(playListItems: List<TubeFyCoreTypeData?>)
    {
        youtubePlayerPlaylistListModel=YoutubePlayerPlaylistListModel(playListItems)
        PlayListSingleton.addData(playListItems)
        Log.d("bulkmode","added 111"+koko)

    }
    private fun setHtmlMusicCategoryContent(result: YtMusicCategoryBase?) {

        viewModelScope.launch {
            localRepositorySource.manipulateYoutubeMusicCategorySearchStripData(result!!).collect {

                youTubeMusicCategoryDataPrivate.value = it
            }
        }

    }
    fun setHtmlMusicPlayListContent(content: YoutubeMusicPlayListContent?) {
        if (content != null) {
//            Log.d("dad",""+ content.contents?.twoColumnBrowseResultsRenderer?.contents?.sectionListRenderer?.contents?.get(0)?.musicPlaylistShelfRenderer!!.contents?.get(0)?.musicResponsiveListItemRenderer?.flexColumns?.get(0)?.musicResponsiveListItemFlexColumnRenderer!!.text!!.runs?.get(0)!!.text)
//            Log.d("dad",""+ content.contents?.twoColumnBrowseResultsRenderer?.contents?.sectionListRenderer?.contents?.get(0)?.musicPlaylistShelfRenderer!!.contents?.get(0)?.musicResponsiveListItemRenderer?.flexColumns?.get(0)?.musicResponsiveListItemFlexColumnRenderer!!.text!!.runs?.get(0)!!.navigationEndpoint!!.watchEndpoint!!.videoId)
//            Log.d("dad",""+ content.contents?.twoColumnBrowseResultsRenderer?.contents?.sectionListRenderer?.contents?.get(0)?.musicPlaylistShelfRenderer!!.contents?.get(0)?.musicResponsiveListItemRenderer?.thumbnail?.musicThumbnailRenderer?.thumbnail?.thumbnails?.get(0)!!.url)
//
           /* var playlistData=ArrayList<VideoPlayListModel>()
            for (sectionContents in content.contents?.twoColumnBrowseResultsRenderer?.contents?.sectionListRenderer?.contents!!) {
                for (musicPlaylistShelfRendererContents in sectionContents.musicPlaylistShelfRenderer?.contents!!) {
//                    musicPlaylistShelfRendererConten
                    var videoName: String? = ""
                    var videoId: String? = ""
                    var thumpNail: String? = null
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
                        if (thumpNail == null) {
                            thumpNail = thumpDetails.url
                        }
                        else
                        {
                            break
                        }
                    }
                    playlistData.add(VideoPlayListModel(videoId,videoName,thumpNail))

                    Log.d("fulldeatils","video->"+videoId+"<tnhumpNail>"+thumpNail+"<><name"+videoName)
                }


            }*/
            val playlistData = ArrayList<VideoPlayListModel>()
            content.contents?.twoColumnBrowseResultsRenderer?.contents?.sectionListRenderer?.contents?.forEach { sectionContents ->
                sectionContents.musicPlaylistShelfRenderer?.contents?.forEach { musicPlaylistShelfRendererContents ->
                    var videoName = StringBuilder()
                    var videoId: String? = null
                    var thumpNail: String? = null

                    musicPlaylistShelfRendererContents.musicResponsiveListItemRenderer?.flexColumns?.forEach { flexColumList ->
                        flexColumList.musicResponsiveListItemFlexColumnRenderer?.text?.runs?.forEach { musicResponsiveRunData ->
                            musicResponsiveRunData.navigationEndpoint?.watchEndpoint?.videoId?.let {
                                videoId = it
                            }
                            videoName.append("\n").append(musicResponsiveRunData.text)
                        }
                    }

                    musicPlaylistShelfRendererContents.musicResponsiveListItemRenderer?.thumbnail?.musicThumbnailRenderer?.thumbnail?.thumbnails?.firstOrNull()?.let {
                        thumpNail = it.url
                    }

                    playlistData.add(VideoPlayListModel(videoId, videoName.toString(), thumpNail))
                    Log.d("fulldeatils", "video->$videoId<thumpNail>$thumpNail<><name>${videoName.toString()}")
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

    fun startWebScrapping(url: String,youtubeScrapType: YoutubeScrapType) {
        scrapping.fetchPageSource(url, youtubeScrapType)

//        scrapping.fetchPageSource("https://music.youtube.com/", YoutubeScrapType.YOUTUBE_MUSIC)
//        scrapping.fetchPageSource(
//            "https://music.youtube.com/playlist?list=RDCLAK5uy_n6_pc7SPVqtuPg_cK3AUxh9AbQP-_Qh-w",
//            YoutubeScrapType.YOUTUBE_PLAYLIST
//        )

    }


    fun searchNewPipePage(searchKey:String, contentFilter: MutableList<String>) {
//        val contentFilter = arrayOf<String>("music_songs")
Log.d("incomming<>","<>"+contentFilter)
        viewModelScope.launch {
            remoteRepositorySource.getNewPipePageSearch(0, searchKey, contentFilter, "")
                .collect {
                    Log.d("resultya","yadadada")
                    youTubeSearchDataPrivate.value = it
                }
        }
    }

    fun searchNewPipeNextPage(page: Page?,contentFilter: MutableList<String>,searchString:String) {
//        val contentFilter = arrayOf<String>("all")

        viewModelScope.launch {
            page?.let {
                remoteRepositorySource.getNewPipePageNextSearch(
                    0,
                    searchString,
                    contentFilter,
                    "",
                    it
                ).collect {
                    youTubeSearchDataPrivate.value = it
                }
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

    data class VideoPlayListModel(
        val videoId: String?,
        val title: String?,
        val thumbnail: String?
    )

   /* data class VideoInfo(
        val videoId: String?,
        val playlistId: String?,
        val thumbnail: String?,
        val title: String?,
        val subtitle: String?
    )*/

/*    fun extractVideoInfo(contents: List<MusicCarouselContent>?): List<VideoInfo> {
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
    }*/

    fun setHomeScreenReload(reloadHomeScreen:Boolean)
    {
        this.reloadHomeScreen=reloadHomeScreen
    }

    fun getHomeScreenReloadStatus():Boolean
    {
        return reloadHomeScreen
    }

}