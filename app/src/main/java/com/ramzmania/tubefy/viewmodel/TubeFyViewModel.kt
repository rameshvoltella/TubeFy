package com.ramzmania.tubefy.viewmodel


import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import com.ramzmania.tubefy.core.YoutubeCoreConstant
import com.ramzmania.tubefy.core.YoutubeCoreConstant.extractYoutubeVideoId
import com.ramzmania.tubefy.data.dto.base.searchformat.StreamUrlData
import com.ramzmania.tubefy.data.dto.base.searchformat.TubeFyCoreUniversalData
import com.ramzmania.tubefy.core.extractors.yotubewebextractor.YoutubeJsonScrapping
import com.ramzmania.tubefy.core.download.Downloads
import com.ramzmania.tubefy.data.local.PlaylistHistory
import com.ramzmania.tubefy.data.local.PlaylistVisit
import com.ramzmania.tubefy.data.local.RecentSearches
import com.ramzmania.tubefy.data.local.SongHistory
import com.ramzmania.tubefy.core.extractors.yotubewebextractor.YoutubeScrapType
import com.ramzmania.tubefy.errors.YOUTUBE_SCRAP_ERROR
import com.ramzmania.tubefy.data.ContextModule
import com.ramzmania.tubefy.data.Resource
import com.ramzmania.tubefy.data.database.DatabaseRepository
import com.ramzmania.tubefy.data.dto.base.playlist.PlayListCategory
import com.ramzmania.tubefy.data.dto.home.HomePageResponse
import com.ramzmania.tubefy.data.dto.base.playlist.PlayListData
import com.ramzmania.tubefy.data.dto.base.searchformat.TubeFyCoreTypeData
import com.ramzmania.tubefy.data.dto.database.PlaylistNameWithUrl
import com.ramzmania.tubefy.data.dto.home.youtubei.YoutubeiHomeBaseResponse
import com.ramzmania.tubefy.data.dto.youtubemusic.category.MusicCategoryPlayListBase
import com.ramzmania.tubefy.data.dto.youtubemusic.category.YtMusicCategoryBase
import com.ramzmania.tubefy.data.dto.youtubemusic.playlist.YoutubeMusicPlayListContent
import com.ramzmania.tubefy.data.dto.youtubestripper.ApiResponse
import com.ramzmania.tubefy.data.dto.youtubestripper.MusicHomeResponse2
import com.ramzmania.tubefy.data.local.LocalRepositorySource
import com.ramzmania.tubefy.data.remote.RemoteRepositorySource
import com.ramzmania.tubefy.database.CustomPlaylist
import com.ramzmania.tubefy.database.DatabaseResponse
import com.ramzmania.tubefy.database.FavoritePlaylist
import com.ramzmania.tubefy.database.QuePlaylist

import com.ramzmania.tubefy.ui.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.schabi.newpipe.extractor.Page
import javax.inject.Inject

@HiltViewModel
class TubeFyViewModel @Inject constructor(
    val contextModule: ContextModule,
    val scrapping: YoutubeJsonScrapping,
    private val localRepositorySource: LocalRepositorySource,
    private val remoteRepositorySource: RemoteRepositorySource,
    private val playlistDatabaseRepository: DatabaseRepository,
    private val recentSearches: RecentSearches,
    private val playlistHistory: PlaylistHistory,
    private val songHistory: SongHistory
) : BaseViewModel() {

    /** Songs the user started themselves (not auto-queued), newest first. */
    val playedSongs = songHistory.songs

    fun recordSongPlayed(videoId: String, title: String, thumbnail: String) =
        songHistory.record(videoId, title, thumbnail)

    /** Playlists the user plays from, ranked by recent + frequent use. */
    val playlistVisits = playlistHistory.visits

    fun topPlaylists(visits: List<PlaylistVisit>, minCount: Int, maxCount: Int) =
        playlistHistory.top(visits, minCount, maxCount)

    fun recordYoutubePlaylistPlayed(playlistId: String, title: String, thumbnail: String) =
        playlistHistory.recordYoutubePlaylist(playlistId, title, thumbnail)

    fun recordCustomPlaylistPlayed(name: String, thumbnail: String) =
        playlistHistory.recordCustomPlaylist(name, thumbnail)

    fun removePlaylistVisit(visit: PlaylistVisit) = playlistHistory.remove(visit.key)

    /** Songs/playlists picked from search results, newest first. */
    val recentSearchItems = recentSearches.items

    fun addRecentSearch(item: TubeFyCoreTypeData) = recentSearches.add(item)

    fun removeRecentSearch(item: TubeFyCoreTypeData) = recentSearches.remove(item)

    fun clearRecentSearches() = recentSearches.clear()

    private var nextYoutubeV3PageToken: String? = null
    private var reloadHomeScreen = true

    //    var youtubePlayerPlaylistListModel: YoutubePlayerPlaylistListModel?=null
    var koko: String? = "koko"


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

    private val youTubeMusicHomeDefaultDataPrivate =
        MutableLiveData<Resource<List<HomePageResponse?>>>()
    val youTubeMusicHomeDefaultData: LiveData<Resource<List<HomePageResponse?>>> get() = youTubeMusicHomeDefaultDataPrivate


    private val youTubePlayListPrivate = MutableLiveData<Resource<PlayListData>>()
    val youTubePlayListData: LiveData<Resource<PlayListData>> get() = youTubePlayListPrivate

    private val youTubePlayListBulkPrivate = MutableLiveData<Resource<List<MediaItem>>>()
    val youTubePlayListBulkData: LiveData<Resource<List<MediaItem>>> get() = youTubePlayListBulkPrivate

    private val youTubeMusicCategoryDataPrivate =
        MutableLiveData<Resource<List<PlayListCategory?>>>()
    val youTubeMusicCategoryData: LiveData<Resource<List<PlayListCategory?>>> get() = youTubeMusicCategoryDataPrivate


    private val youTubeCategoryPlayListPrivate =
        MutableLiveData<Resource<List<MusicCategoryPlayListBase?>>>()
    val youTubeCategoryPlayList: LiveData<Resource<List<MusicCategoryPlayListBase?>>> get() = youTubeCategoryPlayListPrivate


    private val youTubeiMusicHomePrivate = MutableLiveData<Resource<YoutubeiHomeBaseResponse>>()
    val youTubeiMusicHomeData: LiveData<Resource<YoutubeiHomeBaseResponse>> get() = youTubeiMusicHomePrivate

    private val youTubeiMusicHomePaginationPrivate =
        MutableLiveData<Resource<YoutubeiHomeBaseResponse>>()
    val youTubeiMusicHomePaginationData: LiveData<Resource<YoutubeiHomeBaseResponse>> get() = youTubeiMusicHomePaginationPrivate


    private val loadMoreHomePagePrivate = MutableStateFlow(false)
    val loadMoreHomeData = loadMoreHomePagePrivate.asStateFlow()

    private val pullToRefreshPagePrivate = MutableStateFlow(false)
    val pullToRefreshPage = pullToRefreshPagePrivate.asStateFlow()

    private val loadMoreHomePageEndedPrivate = MutableStateFlow(false)
    val loadMoreHomePageEnded = loadMoreHomePageEndedPrivate.asStateFlow()

    private val dismissPlayListDialogPrivate = MutableStateFlow(false)
    val dismissPlayListDialog = dismissPlayListDialogPrivate.asStateFlow()

    private val reloadAllPlayListPrivate = MutableStateFlow(false)
    val reloadAllPlayList = reloadAllPlayListPrivate.asStateFlow()

    private val showPlayListDialogPrivate = MutableStateFlow(false)
    val showPlayListDialog = showPlayListDialogPrivate.asStateFlow()

    private var selectedTrackPrivate =MutableStateFlow<TubeFyCoreTypeData?>(null)
    val selectedTrack=selectedTrackPrivate.asStateFlow()


    private val getPlayListFromDatabasePrivate = MutableLiveData<Resource<List<QuePlaylist>>>()
    val getPlayListFromDatabase: LiveData<Resource<List<QuePlaylist>>> get() = getPlayListFromDatabasePrivate


    private val addSongToDatabasePrivate = MutableLiveData<Resource<DatabaseResponse>>()
    val addSongToDatabase: LiveData<Resource<DatabaseResponse>> get() = addSongToDatabasePrivate


    private val addToActiveDatabasePrivate = MutableLiveData<Resource<DatabaseResponse>>()
    val addToActiveDatabase: LiveData<Resource<DatabaseResponse>> get() = addToActiveDatabasePrivate

    private val getAllActiveListPrivate = MutableLiveData<Resource<List<TubeFyCoreTypeData?>>>()
    val getAllActiveList: LiveData<Resource<List<TubeFyCoreTypeData?>>> get() = getAllActiveListPrivate
///////////////////////////////////////////////////
    private val isFavouriteSongMutableStatePrivate = MutableStateFlow(false)
    val isFavouriteState = isFavouriteSongMutableStatePrivate.asStateFlow()

    private val isFavouritePrivate = MutableLiveData<Resource<Boolean>>()
    val isFavourite: LiveData<Resource<Boolean>> get() = isFavouritePrivate

    private val addingSongListPlayListOperationPrivate =
        MutableLiveData<Resource<DatabaseResponse>>()
    val addingSongListPlayListOperation: LiveData<Resource<DatabaseResponse>> get() = addingSongListPlayListOperationPrivate

    private val gettingSongPlayListOperationPrivate =
        MutableLiveData<Resource<List<TubeFyCoreTypeData?>>>()
    val gettingSongPlayListOperation: LiveData<Resource<List<TubeFyCoreTypeData?>>> get() = gettingSongPlayListOperationPrivate

    private val gettingPrivatePlayListPrivate =
        MutableLiveData<Resource<List<PlaylistNameWithUrl>>>()
    val gettingPrivatePlayList: LiveData<Resource<List<PlaylistNameWithUrl>>> get() = gettingPrivatePlayListPrivate


    fun setHtmlContent(content: ApiResponse?) {
//        htmlContentPrivate.value = content
        viewModelScope.launch {
            localRepositorySource.manipulateYoutubeSearchStripData(content!!).collect {

                youTubeSearchDataPrivate.value = it
            }
        }
    }

    fun pullToRefreshHome(refresh: Boolean) {
        pullToRefreshPagePrivate.value = refresh
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

    fun loadDefaultHomeData() {

        viewModelScope.launch {
            localRepositorySource.loadDefaultHomePageData().collect {

                youTubeMusicHomeDefaultDataPrivate.value = it
            }
        }
    }

    fun loadPlayList(playListUrl: String) {
        if (!playListUrl.startsWith("https:")) {
            viewModelScope.launch {
                remoteRepositorySource.getPlayListInfo("https://music.youtube.com/playlist?list=$playListUrl")
                    .collect {
                        youTubePlayListPrivate.value = it
                    }
            }
        } else {
            viewModelScope.launch {
                remoteRepositorySource.getPlayListInfo(playListUrl).collect {
                    youTubePlayListPrivate.value = it
                }
            }
        }


    }


    /*  fun getBulkStreamUrl()
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
  */
    fun callCategoryPlayList(browserId: String, playerListId: String) {
//        Log.d("bulk calling","bulk"+youTubePlayListBulkData)

        viewModelScope.launch {

            remoteRepositorySource.getCategoryPlayList(browserId, playerListId).collect {
                youTubeCategoryPlayListPrivate.value = it

            }
        }

    }

    fun callCategoryPlayList() {
//        Log.d("bulk calling","bulk"+youTubePlayListBulkData)

        viewModelScope.launch {

            remoteRepositorySource.getCategoryPlayList(
                "FEmusic_moods_and_genres_category",
                "ggMPOg1uX1JOQWZFeDByc2Jm"
            ).collect {
                youTubeCategoryPlayListPrivate.value = it

            }
        }

    }


    fun callYoutubeiHome() {
//        Log.d("bulk calling","bulk"+youTubePlayListBulkData)

        viewModelScope.launch {

            remoteRepositorySource.getMusicHomeYoutubei().collect {
                youTubeiMusicHomePrivate.value = it

            }
        }

    }

    fun callYoutubeiHomePagination(
        paginationHex: String,
        paginationId: String,
        visitorData: String
    ) {
//        Log.d("bulk calling","bulk"+youTubePlayListBulkData)

        viewModelScope.launch {

            remoteRepositorySource.getMusicHomePaginationYoutubei(
                paginationHex,
                paginationId,
                visitorData
            ).collect {
                youTubeiMusicHomePaginationPrivate.value = it

            }
        }

    }

//    fun setCurrentPlayListData(playListItems: List<TubeFyCoreTypeData?>) {
////        youtubePlayerPlaylistListModel=YoutubePlayerPlaylistListModel(playListItems)
//        setActiveSongsList(playListItems)
////        PlayListSingleton.addData(playListItems)
////        Log.d("bulkmode","added 111"+koko)
//
//
//    }

    private fun setHtmlMusicCategoryContent(result: YtMusicCategoryBase?) {
        if (result == null) {
            youTubeMusicCategoryDataPrivate.value = Resource.DataError(YOUTUBE_SCRAP_ERROR)
            return
        }
        viewModelScope.launch {
            localRepositorySource.manipulateYoutubeMusicCategorySearchStripData(result)
                .catch { emit(Resource.DataError(YOUTUBE_SCRAP_ERROR)) }
                .collect {
                    youTubeMusicCategoryDataPrivate.value = it
                }
        }
    }

    private val homeCategoryContentPrivate =
        MutableStateFlow<Map<String, Resource<List<MusicCategoryPlayListBase?>>>>(emptyMap())

    /** Playlists for the home screen chips, keyed by category id, cached for the session. */
    val homeCategoryContent = homeCategoryContentPrivate.asStateFlow()

    fun loadHomeCategory(category: PlayListCategory, forceRefresh: Boolean = false) {
        val key = category.playListCategoryId ?: return
        val browseId = category.playListBrowserId ?: return
        val current = homeCategoryContentPrivate.value[key]
        if (!forceRefresh && (current is Resource.Success || current is Resource.Loading)) return
        homeCategoryContentPrivate.update { it + (key to Resource.Loading()) }
        viewModelScope.launch {
            remoteRepositorySource.getCategoryPlayList(browseId, key)
                .catch { emit(Resource.DataError(YOUTUBE_SCRAP_ERROR)) }
                .collect { result -> homeCategoryContentPrivate.update { it + (key to result) } }
        }
    }

    /** Loads the search tab's mood & genre categories unless they are already loaded. */
    fun loadMusicCategories(forceRefresh: Boolean = false) {
        if (!forceRefresh && youTubeMusicCategoryDataPrivate.value is Resource.Success) return
        youTubeMusicCategoryDataPrivate.value = Resource.Loading()
        startWebScrapping(
            "https://music.youtube.com/moods_and_genres",
            YoutubeScrapType.YOUTUBE_MUSIC_CATEGORY
        )
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

                    musicPlaylistShelfRendererContents.musicResponsiveListItemRenderer?.thumbnail?.musicThumbnailRenderer?.thumbnail?.thumbnails?.firstOrNull()
                        ?.let {
                            thumpNail = it.url
                        }

                    playlistData.add(VideoPlayListModel(videoId, videoName.toString(), thumpNail))

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

    fun startWebScrapping(url: String, youtubeScrapType: YoutubeScrapType) {
        scrapping.fetchPageSource(url, youtubeScrapType)

//        scrapping.fetchPageSource("https://music.youtube.com/", YoutubeScrapType.YOUTUBE_MUSIC)
//        scrapping.fetchPageSource(
//            "https://music.youtube.com/playlist?list=RDCLAK5uy_n6_pc7SPVqtuPg_cK3AUxh9AbQP-_Qh-w",
//            YoutubeScrapType.YOUTUBE_PLAYLIST
//        )

    }


    fun searchNewPipePage(searchKey: String, contentFilter: MutableList<String>) {
//        val contentFilter = arrayOf<String>("music_songs")
        viewModelScope.launch {
            remoteRepositorySource.getNewPipePageSearch(0, searchKey, contentFilter, "")
                .collect {
                    youTubeSearchDataPrivate.value = it
                }
        }
    }

    fun searchNewPipeNextPage(
        page: Page?,
        contentFilter: MutableList<String>,
        searchString: String
    ) {
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

    fun setHomeScreenReload(reloadHomeScreen: Boolean) {
        this.reloadHomeScreen = reloadHomeScreen
    }

    fun getHomeScreenReloadStatus(): Boolean {
        return reloadHomeScreen
    }

    fun setHomePageLoadMoreState(isLoading: Boolean) {
        loadMoreHomePagePrivate.value = isLoading
    }

    fun homePagePaginationEnded(value: Boolean) {
        loadMoreHomePageEndedPrivate.value = value
    }


    fun insertSongTOData(playlists: List<QuePlaylist>) {
        viewModelScope.launch {

            playlistDatabaseRepository.addPlayList(playlists).collect {
                addSongToDatabasePrivate.value = it

            }
        }
    }

    fun getSongsList() {
        viewModelScope.launch {

            playlistDatabaseRepository.getPlaylists().collect {
                getPlayListFromDatabasePrivate.value = it

            }
        }
    }


    fun setActiveSongsList(playlists: List<TubeFyCoreTypeData?>,clickPosition:Int=-1) {
        viewModelScope.launch {

            playlistDatabaseRepository.addActivePlayList(playlists,clickPosition).collect {
                addToActiveDatabasePrivate.value = it

            }
        }
    }

    fun getActivePlayList() {
        viewModelScope.launch {

            playlistDatabaseRepository.getAllActivePlaylists().collect {
                getAllActiveListPrivate.value = it

            }
        }
    }

    fun isFavourite(videoId: String) {
        viewModelScope.launch {

            playlistDatabaseRepository.isFavourite(YoutubeCoreConstant.extractYoutubeVideoId(videoId)!!).collect {
                isFavouritePrivate.value = it
//                Log.d("y?oyoy","yoyoy>>>"+it.data!!)
//                if(it ==Resource.Success)
                isFavouriteSongMutableStatePrivate.value=it.data!!

            }
        }
    }

    fun addToFavorites(favorite: FavoritePlaylist) {
        viewModelScope.launch {

            playlistDatabaseRepository.addToFavorites(favorite).collect {
                addingSongListPlayListOperationPrivate.value = it
                isFavourite(favorite.videoId)
            }
        }
    }

    fun getFavorites() {
        viewModelScope.launch {

            playlistDatabaseRepository.getFavorites().collect {
                gettingSongPlayListOperationPrivate.value = it

            }
        }
    }

    fun removeFromFavorites(videoId: String) {
        viewModelScope.launch {

            playlistDatabaseRepository.removeFromFavorites(videoId).collect {
                addingSongListPlayListOperationPrivate.value = it
                isFavourite(videoId)
            }
        }
    }

    fun addToPlaylist(customPlaylistEntry: CustomPlaylist) {
        viewModelScope.launch {

            playlistDatabaseRepository.addToPlaylist(customPlaylistEntry).collect {
                addingSongListPlayListOperationPrivate.value = it

            }
        }
    }

    fun getSpecificPlayList(playListName: String) {
        viewModelScope.launch {

            playlistDatabaseRepository.getSpecificPlayList(playListName).collect {
                gettingSongPlayListOperationPrivate.value = it

            }
        }
    }

    fun getAllSavedPlayList() {
        viewModelScope.launch {

            playlistDatabaseRepository.getAllSavedPlayList().collect {
                gettingPrivatePlayListPrivate.value = it

            }
        }
    }

    /**
     * Removes a song from a saved playlist (or favorites) and then reloads that list.
     * Both steps run in one coroutine so the reload always sees the deletion.
     */
    fun removeSongFromSavedList(playListName: String, videoId: String) {
        viewModelScope.launch {
            if (playListName == "TubeFy-Favorites") {
                playlistDatabaseRepository.removeFromFavorites(videoId).collect {
                    addingSongListPlayListOperationPrivate.value = it
                }
                playlistDatabaseRepository.getFavorites().collect {
                    gettingSongPlayListOperationPrivate.value = it
                }
            } else {
                if (playListName == Downloads.PLAYLIST) Downloads.delete(contextModule.context, videoId)
                playlistDatabaseRepository.deleteSongFromPlayList(playListName, videoId).collect {
                    addingSongListPlayListOperationPrivate.value = it
                }
                playlistDatabaseRepository.getSpecificPlayList(playListName).collect {
                    gettingSongPlayListOperationPrivate.value = it
                }
            }
        }
    }

    fun deleteSongFromPlayList(playListName: String, videoId: String) {
        viewModelScope.launch {

            playlistDatabaseRepository.deleteSongFromPlayList(playListName, videoId).collect {
                addingSongListPlayListOperationPrivate.value = it

            }
        }
    }

    /** Clears the automated playlist; [onCleared] runs on the main thread when done. */
    fun clearAutomatedPlaylist(onCleared: () -> Unit) {
        viewModelScope.launch {
            playlistDatabaseRepository.clearAutomatedPlaylist().collect { onCleared() }
        }
    }

    /** Persists a drag-and-drop reorder of a saved playlist (or favorites). */
    fun savePlaylistOrder(playlistName: String, songs: List<TubeFyCoreTypeData?>) {
        viewModelScope.launch {
            playlistDatabaseRepository.savePlayListOrder(playlistName, songs.filterNotNull()).collect {}
        }
    }

    private val isAlbumSavedPrivate = MutableStateFlow(false)

    /** Whether the album currently open is saved as a playlist in "Your Library". */
    val isAlbumSaved = isAlbumSavedPrivate.asStateFlow()

    fun checkAlbumSaved(albumName: String) {
        val playlistName = if (Downloads.isReservedName(albumName)) "$albumName (Album)" else albumName
        viewModelScope.launch {
            playlistDatabaseRepository.isPlayListSaved(playlistName).collect {
                isAlbumSavedPrivate.value = it is Resource.Success && it.data == true
            }
        }
    }

    /** Saves the album's songs as a new playlist named after the album, or removes that playlist. */
    fun toggleAlbumSaved(albumName: String, tracks: List<TubeFyCoreTypeData?>) {
        // "Download(s)" is reserved for offline downloads
        val playlistName = if (Downloads.isReservedName(albumName)) "$albumName (Album)" else albumName
        viewModelScope.launch {
            if (isAlbumSavedPrivate.value) {
                playlistDatabaseRepository.deleteSpecificPlayList(playlistName).collect {
                    if (it is Resource.Success) isAlbumSavedPrivate.value = false
                }
            } else {
                val entries = tracks.filterNotNull()
                    .filter { it.videoId.isNotEmpty() }
                    .map {
                        CustomPlaylist(
                            playlistName = playlistName,
                            videoId = it.videoId,
                            videoThump = it.videoImage,
                            videoName = it.videoTitle
                        )
                    }
                if (entries.isEmpty()) return@launch
                playlistDatabaseRepository.saveAsPlayList(entries).collect {
                    if (it is Resource.Success) isAlbumSavedPrivate.value = true
                }
            }
        }
    }

    fun deleteSpecificPlayList(playListName: String) {
        viewModelScope.launch {

            if (playListName == Downloads.PLAYLIST) Downloads.deleteAll(contextModule.context)
            playlistHistory.removeCustomPlaylist(playListName)
            playlistDatabaseRepository.deleteSpecificPlayList(playListName).collect {
                addingSongListPlayListOperationPrivate.value = it
                reloadAllPlayListPrivate.value = true
            }
        }
    }

    fun dismissPlaListDialog(value: Boolean) {
        dismissPlayListDialogPrivate.value=value
    }

    fun reloadAllCustomPlayListData(value: Boolean) {
        reloadAllPlayListPrivate.value=value
    }

    fun deleteAllFavorites() {
        viewModelScope.launch {

            playlistDatabaseRepository.deleteAllFavorites().collect {
                addingSongListPlayListOperationPrivate.value = it
                reloadAllPlayListPrivate.value = true

            }
        }
    }

    fun showPlayListDialog(showDialog:Boolean,selectedSong:TubeFyCoreTypeData?=null) {

        showPlayListDialogPrivate.value=showDialog
        selectedTrackPrivate.value=selectedSong

    }

}