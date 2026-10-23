package com.ramzmania.tubefy.data.remote

import com.ramzmania.tubefy.data.dto.base.searchformat.TubeFyCoreTypeData
import android.util.Log
import androidx.media3.common.MediaItem
import com.ramzmania.tubefy.core.YoutubeCoreConstant
import com.ramzmania.tubefy.core.YoutubeCoreConstant.YOUTUBE_V3_MAX_RESULT
import com.ramzmania.tubefy.core.dataformatter.FormattingResult
import com.ramzmania.tubefy.data.dto.base.searchformat.NewPipeSortingData
import com.ramzmania.tubefy.core.dataformatter.newpipe.NewPipeDataFormatter
import com.ramzmania.tubefy.core.dataformatter.newpipe.NewPipeDataFormatterFactory
import com.ramzmania.tubefy.core.dataformatter.newpipe.bestThumbnailUrl
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
import com.ramzmania.tubefy.player.CarLibrary
import com.ramzmania.tubefy.core.youtube.YouTubeStreams
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
        var artist: String = ""

        withContext(Dispatchers.IO)
        {
            try {
                // Shared lookup: reuses a recent answer for this video instead of asking YouTube again
                val info = YouTubeStreams.info(videoId)
                val muxed = info.videoStreams.orEmpty().firstOrNull()?.content
                if (!muxed.isNullOrBlank()) {
                    streamUrl = muxed
                    artist = info.uploaderName ?: ""
                } else if (!info.hlsUrl.isNullOrBlank()) {
                    // Live streams only come as HLS
                    streamUrl = info.hlsUrl
                    artist = info.uploaderName ?: ""
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return if (streamUrl.length > 0) {
            Resource.Success(StreamUrlData(streamUrl,mediaIndex,artist))
        } else {
            Resource.DataError(404)
        }
    }


    /**
     * Queue items for many songs at once (playlists, smart queue). No link is fetched here: each item
     * gets a `tubefy://video/<id>` address that [com.ramzmania.tubefy.player.StreamUrlRefresher] turns into
     * a real link just before that song plays. Asking YouTube for 10+ links up front got IPs blocked.
     */
    override suspend fun getStreamBulkUrl(youtubePlayerPlaylistListModel: YoutubePlayerPlaylistListModel): Resource<List<MediaItem>> {
        val songs = youtubePlayerPlaylistListModel.playListData.filterNotNull()
            .mapNotNull { song ->
                val id = YoutubeCoreConstant.extractYoutubeVideoId(song.videoId) ?: return@mapNotNull null
                Triple(id, song.videoTitle, song.artist)
            }
        if (songs.isEmpty()) return Resource.DataError(404)
        return Resource.Success(
            createMediaItems(
                mediaUris = songs.map { (id, _, _) -> "${CarLibrary.LAZY_SCHEME}://video/$id" },
                videoThumpUrls = songs.map { (id, _, _) -> "https://i.ytimg.com/vi/$id/hq720.jpg" },
                videoTitles = songs.map { (_, title, _) -> title },
                videoIdList = songs.map { (id, _, _) -> id },
                artists = songs.map { (_, _, artist) -> artist }
            )
        )
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
            params = playerId.ifBlank { null }
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
                    searchInfo!!.nextPage,
                    contentFilter
                )
            )
            when (result) {
                is FormattingResult.SUCCESS -> {

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
                    pageSearchInfo.nextPage,
                    contentFilter
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

    /**
     * Songs similar to [videoId] from YouTube's auto-generated "Mix" (the radio YouTube Music
     * plays after a song): YouTube Music's mix first, the regular YouTube mix as a backup.
     * Not saved anywhere, so it can't mix contexts like the cached queue does.
     */
    override suspend fun getMixQueue(videoId: String): Resource<List<TubeFyCoreTypeData>> {
        return withContext(Dispatchers.IO) {
            val id = YoutubeCoreConstant.extractYoutubeVideoId(videoId) ?: videoId
            for (mixId in listOf("RDAMVM$id", "RD$id")) {
                try {
                    val songs = newPipePlayListData("${YoutubeCoreConstant.YOUTUBE_WATCH_URL}$id&list=$mixId")
                        .relatedItems
                        .filterIsInstance<StreamInfoItem>()
                        .filter { item ->
                            YoutubeCoreConstant.extractYoutubeVideoId(item.url) != id &&
                                (item.duration <= 0 || item.duration in 60..600)
                        }
                        .map { item ->
                            TubeFyCoreTypeData(
                                videoId = item.url,
                                videoTitle = item.name,
                                videoImage = bestThumbnailUrl(item.thumbnails),
                                artist = item.uploaderName.orEmpty().removeSuffix(" - Topic").trim(),
                                durationSeconds = item.duration
                            )
                        }
                    if (songs.isNotEmpty()) return@withContext Resource.Success(songs)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            Resource.DataError(NEW_PIPE_SEARCH_ERROR)
        }
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
                    nextPageSearchInfo.nextPage,
                    contentFilter
                )
            )
//            val baseDataModel= TubeFyCoreUniversalData(NewPipeSortingInput(result,nextPageSearchInfo.nextPage))
            when (result) {
                is FormattingResult.SUCCESS -> {

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