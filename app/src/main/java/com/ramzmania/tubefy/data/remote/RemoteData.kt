package com.ramzmania.tubefy.data.remote

import android.util.Log
import androidx.media3.common.MediaItem
import com.ramzmania.tubefy.core.YoutubeCoreConstant
import com.ramzmania.tubefy.core.YoutubeCoreConstant.YOUTUBE_V3_MAX_RESULT
import com.ramzmania.tubefy.core.dataformatter.FormattingResult
import com.ramzmania.tubefy.data.dto.base.searchformat.NewPipeSortingData
import com.ramzmania.tubefy.core.dataformatter.newpipe.NewPipeDataFormatter
import com.ramzmania.tubefy.core.dataformatter.newpipe.NewPipeDataFormatterFactory
import com.ramzmania.tubefy.data.dto.base.searchformat.StreamUrlData
import com.ramzmania.tubefy.data.dto.base.searchformat.TubeFyCoreUniversalData
import com.ramzmania.tubefy.core.dataformatter.youtubeV3.YoutubeV3Formatter
import com.ramzmania.tubefy.core.dataformatter.youtubemusic.YoutubeMusicDataFormatterFactory
import com.ramzmania.tubefy.core.extractors.newpipeextractor.newPipePlayListData
import com.ramzmania.tubefy.core.extractors.newpipeextractor.newPipeSearchFor
import com.ramzmania.tubefy.core.extractors.newpipeextractor.newPipeSearchNextPageFor
import com.ramzmania.tubefy.data.NetworkConnectivity
import com.ramzmania.tubefy.data.Resource
import com.ramzmania.tubefy.data.dto.base.playlist.PlayListData
import com.ramzmania.tubefy.data.dto.home.youtubei.YoutubeiHomeBaseResponse
import com.ramzmania.tubefy.data.dto.home.youtubei.YoutubeiMusicHomeApiResponse
import com.ramzmania.tubefy.data.dto.home.youtubei.next.ContinuationContents
import com.ramzmania.tubefy.data.dto.youtubeV3.YoutubeSearchResponse
import com.ramzmania.tubefy.data.dto.youtubemusic.category.MusicCategoryPlayListBase
import com.ramzmania.tubefy.data.dto.youtubemusic.playlist.categoryplaylist.BrowseHomePaginationRequest
import com.ramzmania.tubefy.data.dto.youtubemusic.playlist.categoryplaylist.BrowseHomeRequest
import com.ramzmania.tubefy.data.dto.youtubemusic.playlist.categoryplaylist.BrowseRequest
import com.ramzmania.tubefy.data.dto.youtubemusic.playlist.categoryplaylist.CategoryPlayListRoot
import com.ramzmania.tubefy.data.dto.youtubemusic.playlist.categoryplaylist.Client
import com.ramzmania.tubefy.data.dto.youtubemusic.playlist.categoryplaylist.ClientPagination
import com.ramzmania.tubefy.data.dto.youtubemusic.playlist.categoryplaylist.ContextPagination
import com.ramzmania.tubefy.data.remote.api.ApiServices
import com.ramzmania.tubefy.data.remote.api.ServiceGenerator
import com.ramzmania.tubefy.errors.NETWORK_ERROR
import com.ramzmania.tubefy.errors.NEW_PIPE_SEARCH_ERROR
import com.ramzmania.tubefy.errors.NO_INTERNET_CONNECTION
import com.ramzmania.tubefy.errors.SERVER_ERROR
import com.ramzmania.tubefy.errors.YOUTUBE_SCRAP_ERROR
import com.ramzmania.tubefy.errors.YOUTUBE_V3_SEARCH_ERROR
import com.ramzmania.tubefy.player.YoutubePlayerPlaylistListModel
import com.ramzmania.tubefy.player.createMediaItems
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import org.schabi.newpipe.extractor.InfoItem
import org.schabi.newpipe.extractor.Page
import org.schabi.newpipe.extractor.search.SearchInfo
import org.schabi.newpipe.extractor.services.youtube.YoutubeService
import org.schabi.newpipe.extractor.services.youtube.extractors.YoutubeStreamExtractor
import org.schabi.newpipe.extractor.stream.StreamInfoItem
import retrofit2.Response
import java.io.IOException
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

class RemoteData @Inject
constructor(
    private val serviceGenerator: ServiceGenerator,
    private val networkConnectivity: NetworkConnectivity,
    private val newPipeFormatterFactory: NewPipeDataFormatterFactory,
    private val youtubeV3Formatter: YoutubeV3Formatter,
    private val youtubeMusicDataFormatterFactory: YoutubeMusicDataFormatterFactory
) : RemoteDataSource {
    override suspend fun requestYoutubeV3(
        part: String,
        searchQuery: String,
        pageToken: String?
    ): Resource<TubeFyCoreUniversalData> {
        val youtubeV3Service = serviceGenerator.createService(ApiServices::class.java)

        return when (val response = processCall {
            youtubeV3Service.getVideo(
                part, searchQuery, pageToken, YOUTUBE_V3_MAX_RESULT
            )
        }) {
            is Any -> {
                try {
                    (response is YoutubeSearchResponse).let {
                        val result = youtubeV3Formatter.run(response as YoutubeSearchResponse)
                        return when (result) {
                            is FormattingResult.SUCCESS -> {
                                Resource.Success(result.data)
                            }

                            is FormattingResult.FAILURE -> {
                                Resource.DataError(YOUTUBE_V3_SEARCH_ERROR)
                            }

                        }
//                        Resource.Success(result)
//                        Resource.DataError(YOUTUBE_V3_SEARCH_ERROR)
                    }
//                    Resource.DataError(YOUTUBE_V3_SEARCH_ERROR)

                } catch (e: Exception) {
                    e.printStackTrace()
                    Resource.DataError(YOUTUBE_V3_SEARCH_ERROR)
                }
            }

            else -> {
                Resource.DataError(errorCode = response as Int)
            }
        }
    }


    override suspend fun getStreamUrl(videoId: String,mediaIndex:Int): Resource<StreamUrlData> {
        var streamUrl: String = ""

        withContext(Dispatchers.IO)
        {
            try {
                val extractor =
                    YoutubeService(0).getStreamExtractor("${YoutubeCoreConstant.YOUTUBE_WATCH_URL}$videoId") as YoutubeStreamExtractor
                extractor.fetchPage()

                if (extractor.videoStreams.isNotEmpty()) {
                    streamUrl = extractor.videoStreams.first().content ?: ""
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return if (streamUrl.length > 0) {
            Resource.Success(StreamUrlData(streamUrl,mediaIndex))
        } else {
            Resource.DataError(404)
        }
    }


    override suspend fun getStreamBulkUrl(youtubePlayerPlaylistListModel: YoutubePlayerPlaylistListModel): Resource<List<MediaItem>> {
        var streamUrlArray: ArrayList<String> = ArrayList()
        var videoThumpUrls: ArrayList<String> = ArrayList()
        var videoTitles: ArrayList<String> = ArrayList()
        var videoIdArray: ArrayList<String> = ArrayList()
        var mediaItems: List<MediaItem>? = null

//         var mediaUris:Array<String> = listOf("http://example.com/audio1.mp3", "http://example.com/audio2.mp3")
//         val videoThumpUrls = listOf("http://example.com/thumb1.jpg", "http://example.com/thumb2.jpg")
//         val videoTitles = listOf("Title 1", "Title 2")
        withContext(Dispatchers.IO)
        {
            try {
                var currentIndex=0;
                for (videoIds in youtubePlayerPlaylistListModel.playListData) {
                    ensureActive()
                    val extractor =
                        YoutubeService(0).getStreamExtractor(
                            "${YoutubeCoreConstant.YOUTUBE_WATCH_URL}${
                                YoutubeCoreConstant.extractYoutubeVideoId(
                                    videoIds!!.videoId
                                )
                            }"
                        ) as YoutubeStreamExtractor
                    extractor.fetchPage()

                    if (extractor.videoStreams.isNotEmpty()) {
                        Log.d("9papa",videoIds!!.videoId+"")
                        videoIdArray.add(YoutubeCoreConstant.extractYoutubeVideoId(videoIds.videoId)!!)
//                        if(videoIds.videoId.equals("https://www.youtube.com/watch?v=roz9sXFkTuE",ignoreCase = true)) {
//                            streamUrlArray?.add("https://olakka"+extractor.videoStreams.first().content ?: "")
//
//                        }else
//                        {
                            streamUrlArray?.add(extractor.videoStreams.first().content ?: "")

//                        }
                        videoTitles.add(videoIds.videoTitle)
                        videoThumpUrls.add(
                            "https://i.ytimg.com/vi/${
                                YoutubeCoreConstant.extractYoutubeVideoId(
                                    videoIds.videoId
                                )
                            }/hq720.jpg"
                        )

                        currentIndex++

                    }
                }
                mediaItems = createMediaItems(streamUrlArray, videoThumpUrls, videoTitles,videoIdArray)
            }catch (e: CancellationException) {
                // Handle the cancellation, e.g., clean up resources if needed
                Log.d("getStreamBulkUrl", "Operation was canceled")
                e.printStackTrace() // Re-throw the cancellation exception to properly handle coroutine cancellation
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return if (mediaItems != null) {
            Resource.Success(mediaItems!!)
        } else {
            Resource.DataError(404)
        }
    }

    override suspend fun getCategoryPlayList(
        browseId: String,
        playerId: String
    ): Resource<List<MusicCategoryPlayListBase?>> {

        val categoryPlaylistService = serviceGenerator.createService(ApiServices::class.java)
        val client = Client(
            clientName = "WEB_REMIX",
            clientVersion = "1.20240729.01.00"
//            originalUrl = "https://music.youtube.com/moods_and_genres"
        )

        val context =
            com.ramzmania.tubefy.data.dto.youtubemusic.playlist.categoryplaylist.Context(client = client)

        val request = BrowseRequest(
            context = context,
            browseId = browseId,
            params = playerId
        )
        return when (val response = processCall {
            categoryPlaylistService.getCategoryPlaylistInfo(
                request, "https://music.youtube.com/youtubei/v1/browse?prettyPrint=false"
            )
        }) {
            is Any -> {
                try {
                    (response is CategoryPlayListRoot).let {

                        val result =
                            youtubeMusicDataFormatterFactory.createForYoutubeMusicCategoryPlayListDataFormatter()
                                .run((response as CategoryPlayListRoot))
                        when (result) {
                            is FormattingResult.SUCCESS -> {
                                Resource.Success(result.data)
                            }

                            is FormattingResult.FAILURE -> {
                                Resource.DataError(YOUTUBE_SCRAP_ERROR)
                            }


                        }

                    }

                } catch (e: Exception) {
                    e.printStackTrace()
                    Resource.DataError(YOUTUBE_SCRAP_ERROR)
                }
            }

            else -> {
                Resource.DataError(errorCode = response as Int)
            }
        }
    }

    override suspend fun getMusicHomeYoutubei(): Resource<YoutubeiHomeBaseResponse> {
        val categoryPlaylistService = serviceGenerator.createService(ApiServices::class.java)
        val client = Client(
            clientName = "WEB_REMIX",
            clientVersion = "1.20240729.01.00"
//            originalUrl = "https://music.youtube.com/moods_and_genres"
        )

        val context =
            com.ramzmania.tubefy.data.dto.youtubemusic.playlist.categoryplaylist.Context(client = client)

        val request = BrowseHomeRequest(
            context = context
        )
        return when (val response = processCall {
            categoryPlaylistService.getMusicHomeYoutubeiInfo(
                request, "https://music.youtube.com/youtubei/v1/browse?prettyPrint=false"
            )
        }) {
            is Any -> {
                try {
                    (response is YoutubeiMusicHomeApiResponse).let {
                        Log.d("kiiii", "yooo")
                        return withContext(Dispatchers.IO)
                        {
                            val result =
                                youtubeMusicDataFormatterFactory.createForYoutubeMusicHomeYoutubeiDataFormatter()
                                    .run((response as YoutubeiMusicHomeApiResponse))
                            when (result) {
                                is FormattingResult.SUCCESS -> {
                                    if (result.data.homePageContentDataList != null) {
                                        Resource.Success(result.data)
                                    } else {
                                        Resource.DataError(YOUTUBE_SCRAP_ERROR)
                                    }
                                }

                                is FormattingResult.FAILURE -> {
                                    Resource.DataError(YOUTUBE_SCRAP_ERROR)
                                }
                            }

                        }
//                        Resource.DataError(YOUTUBE_SCRAP_ERROR)


                    }

                } catch (e: Exception) {
                    e.printStackTrace()
                    Resource.DataError(YOUTUBE_SCRAP_ERROR)
                }
            }

            else -> {
                Resource.DataError(errorCode = response as Int)
            }
        }
    }

    override suspend fun getMusicHomePaginationYoutubei(
        paginationHex: String,
        paginationId: String,
        visitorData: String
    ): Resource<YoutubeiHomeBaseResponse> {
        val categoryPlaylistService = serviceGenerator.createService(ApiServices::class.java)
        val client = ClientPagination(
            clientName = "WEB_REMIX",
            clientVersion = "1.20240729.01.00",
            visitorData = visitorData
//            originalUrl = "https://music.youtube.com/moods_and_genres"
        )

        val context =
            ContextPagination(client = client)

        val request = BrowseHomePaginationRequest(
            context = context
        )
        return when (val response = processCall {
            categoryPlaylistService.getMusicHomeYoutubeiPaginationInfo(
                request,
                "https://music.youtube.com/youtubei/v1/browse?continuation=" + paginationId + "&type=next&itct=" + paginationHex + "&prettyPrint=false"
            )
        }) {
            is Any -> {
                try {
                    (response is YoutubeiMusicHomeApiResponse).let {
                        Log.d("kiiii", "yooo")
                        return withContext(Dispatchers.IO)
                        {
                        val result =
                            youtubeMusicDataFormatterFactory.createForYoutubeMusicYoutubeiDataHomePaginationFormatter()
                                .run((response as ContinuationContents))
                        when (result) {
                            is FormattingResult.SUCCESS -> {
                                if (result.data.homePageContentDataList != null) {
                                    Resource.Success(result.data)
                                } else {
                                    Resource.DataError(YOUTUBE_SCRAP_ERROR)
                                }
                            }

                            is FormattingResult.FAILURE -> {
                                Resource.DataError(YOUTUBE_SCRAP_ERROR)
                            }

                        }
                        }
//                        Resource.DataError(YOUTUBE_SCRAP_ERROR)


                    }

                } catch (e: Exception) {
                    e.printStackTrace()
                    Resource.DataError(YOUTUBE_SCRAP_ERROR)
                }
            }

            else -> {
                Resource.DataError(errorCode = response as Int)
            }
        }
    }

    override suspend fun getNewPipePageSearch(
        serviceId: Int,
        searchString: String,
        contentFilter: List<String?>,
        sortFilter: String
    ): Resource<TubeFyCoreUniversalData> {
        var searchInfo: SearchInfo? = null

        withContext(Dispatchers.IO)
        {
//            val contentFilter2 = arrayOf<String>("music_playlists")
//val dataaaa=PlaylistInfo.getInfo("https://music.youtube.com/playlist?list=PLDNtAuXIhbEOkqGHXzjWh8sLnnhFRSxl_")
//            searchInfo2= SearchInfo.getInfo(
//                NewPipe.getService(0),
//                NewPipe.getService(0)
//                    .searchQHFactory
//                    .fromQuery("https://music.youtube.com/playlist?list=PLDNtAuXIhbEOkqGHXzjWh8sLnnhFRSxl_", contentFilter2.toMutableList(),"")
//            )
//            for (ff in dataaaa!!.relatedItems)
//            {
//               Log.d("data", ff.url)
//            }

            searchInfo = newPipeSearchFor(serviceId, searchString, contentFilter, sortFilter)
            val newPipeFormatter: NewPipeDataFormatter<InfoItem> =
                newPipeFormatterFactory.createForInfoItem()
            val result = newPipeFormatter.run(
                NewPipeSortingData(
                    searchInfo!!.relatedItems,
                    searchInfo!!.nextPage
                )
            )
            when (result) {
                is FormattingResult.SUCCESS -> {
                    Log.d("TAGGIZ", "" + result.data.youtubeSortedData.youtubeSortedList!!.size)

                    Resource.Success(result)

                }

                is FormattingResult.FAILURE -> {
                    Resource.DataError(NEW_PIPE_SEARCH_ERROR)

                }
            }

        }
        return withContext(Dispatchers.IO) {
            val pageSearchInfo =
                newPipeSearchFor(serviceId, searchString, contentFilter, sortFilter)
            val newPipeFormatter: NewPipeDataFormatter<InfoItem> =
                newPipeFormatterFactory.createForInfoItem()
            val result = newPipeFormatter.run(
                NewPipeSortingData(
                    pageSearchInfo.relatedItems,
                    pageSearchInfo.nextPage
                )
            )
            when (result) {
                is FormattingResult.SUCCESS -> {
//                    Log.d("TAGGIZ",""+result.data.youtubeSortedData.youtubeSortedList!!.size)
                    Resource.Success(result.data)

                }

                is FormattingResult.FAILURE -> {
                    Resource.DataError(NEW_PIPE_SEARCH_ERROR)

                }
            }

        }
//        return if(searchInfo!=null)
//        {
//            Resource.Success(searchInfo!!)
//        }else
//        {
//            Resource.DataError(NEW_PIPE_SEARCH_ERROR)
//        }
    }

    override suspend fun getPlayListInfo(playListUrl: String): Resource<PlayListData> {
        return withContext(Dispatchers.IO) {
            val newPipeFormatter: NewPipeDataFormatter<StreamInfoItem?> =
                newPipeFormatterFactory.createForPlayListItem()
            val newPipePlayList = newPipePlayListData(playListUrl)
            val result = newPipeFormatter.run(
                NewPipeSortingData(
                    newPipePlayList.relatedItems,
                    newPipePlayList.nextPage
                )
            )
            when (result) {
                is FormattingResult.SUCCESS -> {
//                    Log.d("TAGGIZ",""+result.data.youtubeSortedData.youtubeSortedList!!.size)
                    Resource.Success(
                        PlayListData(
                            newPipePlayList.thumbnails[0].url,
                            result.data.youtubeSortedData.youtubeSortedList
                        )
                    )

                }

                is FormattingResult.FAILURE -> {
                    Resource.DataError(NEW_PIPE_SEARCH_ERROR)

                }
            }

        }

    }

    override suspend fun getNewPipePageNextSearch(
        serviceId: Int,
        searchString: String,
        contentFilter: List<String?>,
        sortFilter: String,
        page: Page
    ): Resource<TubeFyCoreUniversalData> {
        /* var nextPageSearchInfo:ListExtractor.InfoItemsPage<InfoItem>?=null
             withContext(Dispatchers.IO)
             {
                 nextPageSearchInfo= newPipeSearchNextPageFor(serviceId, searchString, contentFilter, sortFilter,page)
             }
             return if(nextPageSearchInfo!=null)
             {
                 Resource.Success(nextPageSearchInfo!!)
             }else
             {
                 Resource.DataError(NEW_PIPE_SEARCH_MORE_ERROR)
             }*/
        return withContext(Dispatchers.IO) {
            val newPipeFormatter: NewPipeDataFormatter<InfoItem> =
                newPipeFormatterFactory.createForInfoItem()
            val nextPageSearchInfo =
                newPipeSearchNextPageFor(serviceId, searchString, contentFilter, sortFilter, page)
            val result = newPipeFormatter.run(
                NewPipeSortingData(
                    nextPageSearchInfo.items,
                    nextPageSearchInfo.nextPage
                )
            )
//            val baseDataModel= TubeFyCoreUniversalData(NewPipeSortingInput(result,nextPageSearchInfo.nextPage))
            when (result) {
                is FormattingResult.SUCCESS -> {
                    Log.d(
                        "TAGGIZNEXTPAGE",
                        "" + result.data.youtubeSortedData.youtubeSortedList!!.size
                    )
                    Resource.Success(result.data)

                }

                is FormattingResult.FAILURE -> {
                    Resource.DataError(NEW_PIPE_SEARCH_ERROR)

                }
            }

        }

    }


    private suspend fun processCall(responseCall: suspend () -> Response<*>): Any? {
        try {
            if (!networkConnectivity.isConnected()) {
                return NO_INTERNET_CONNECTION
            }
            return try {
                val response = responseCall.invoke()
                val responseCode = response.code()
                if (response.isSuccessful) {

                    response.body()
                } else {
                    responseCode
                }
            } catch (e: IOException) {
                e.printStackTrace()
                NETWORK_ERROR
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
            return SERVER_ERROR
        }
    }
}