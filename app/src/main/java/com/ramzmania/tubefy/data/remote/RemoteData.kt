package com.ramzmania.tubefy.data.remote

import android.util.Log
import com.ramzmania.tubefy.core.YoutubeCoreConstant
import com.ramzmania.tubefy.core.YoutubeCoreConstant.YOUTUBE_V3_MAX_RESULT
import com.ramzmania.tubefy.core.dataformatter.FormattingResult
import com.ramzmania.tubefy.data.dto.searchformat.NewPipeSortingData
import com.ramzmania.tubefy.core.dataformatter.newpipe.NewPipeDataFormatter
import com.ramzmania.tubefy.core.dataformatter.newpipe.NewPipeDataFormatterFactory
import com.ramzmania.tubefy.data.dto.searchformat.StreamUrlData
import com.ramzmania.tubefy.data.dto.searchformat.TubeFyCoreUniversalData
import com.ramzmania.tubefy.core.dataformatter.youtubeV3.YoutubeV3Formatter
import com.ramzmania.tubefy.core.extractors.newpipeextractor.newPipePlayListData
import com.ramzmania.tubefy.core.extractors.newpipeextractor.newPipeSearchFor
import com.ramzmania.tubefy.core.extractors.newpipeextractor.newPipeSearchNextPageFor
import com.ramzmania.tubefy.data.NetworkConnectivity
import com.ramzmania.tubefy.data.Resource
import com.ramzmania.tubefy.data.dto.playlist.PlayListData
import com.ramzmania.tubefy.data.dto.youtubeV3.YoutubeSearchResponse
import com.ramzmania.tubefy.data.remote.api.ApiServices
import com.ramzmania.tubefy.data.remote.api.ServiceGenerator
import com.ramzmania.tubefy.errors.NETWORK_ERROR
import com.ramzmania.tubefy.errors.NEW_PIPE_SEARCH_ERROR
import com.ramzmania.tubefy.errors.NO_INTERNET_CONNECTION
import com.ramzmania.tubefy.errors.SERVER_ERROR
import com.ramzmania.tubefy.errors.YOUTUBE_V3_SEARCH_ERROR
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.schabi.newpipe.extractor.InfoItem
import org.schabi.newpipe.extractor.NewPipe
import org.schabi.newpipe.extractor.Page
import org.schabi.newpipe.extractor.playlist.PlaylistInfo
import org.schabi.newpipe.extractor.search.SearchInfo
import org.schabi.newpipe.extractor.services.youtube.YoutubeService
import org.schabi.newpipe.extractor.services.youtube.extractors.YoutubeStreamExtractor
import org.schabi.newpipe.extractor.stream.StreamInfoItem
import retrofit2.Response
import java.io.IOException
import javax.inject.Inject

class RemoteData@Inject
constructor(
    private val serviceGenerator: ServiceGenerator,
    private val networkConnectivity: NetworkConnectivity,
    private val newPipeFormatterFactory: NewPipeDataFormatterFactory, private val youtubeV3Formatter: YoutubeV3Formatter
) :RemoteDataSource {
    override suspend fun requestYoutubeV3(
        part: String,
        searchQuery: String,
        pageToken: String?
    ): Resource<TubeFyCoreUniversalData> {
        val youtubeV3Service = serviceGenerator.createService(ApiServices::class.java)

        return when (val response = processCall {
            youtubeV3Service.getVideo(
               part,searchQuery,pageToken,YOUTUBE_V3_MAX_RESULT
            )
        }) {
            is Any -> {
                try {
                    (response is YoutubeSearchResponse).let {
                        val result=youtubeV3Formatter.run(response as YoutubeSearchResponse)
                        return when(result)
                        {
                            is FormattingResult.SUCCESS->{  Resource.Success(result.data)
                            }
                            is FormattingResult.FAILURE->{Resource.DataError(YOUTUBE_V3_SEARCH_ERROR)}

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
        }    }


    override suspend fun getStreamUrl(videoId: String): Resource<StreamUrlData> {
        var streamUrl: String = ""

        withContext(Dispatchers.IO)
        {
            try {
                val extractor =
                    YoutubeService(0).getStreamExtractor("${YoutubeCoreConstant.YOUTUBE_WATCH_URL}$videoId") as YoutubeStreamExtractor
                extractor.fetchPage()

                if (extractor.videoStreams.isNotEmpty()) {
                    streamUrl = extractor.videoStreams.first().url ?: ""
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return if(streamUrl.length>0) {
            Resource.Success(StreamUrlData(streamUrl))
        }else{
            Resource.DataError(404)
        }
    }

    override suspend fun getNewPipePageSearch(
        serviceId: Int,
        searchString: String,
        contentFilter: List<String?>,
        sortFilter: String
    ): Resource<TubeFyCoreUniversalData> {
        var searchInfo: SearchInfo?=null
        var searchInfo2: SearchInfo?=null

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

            searchInfo= newPipeSearchFor(serviceId, searchString, contentFilter, sortFilter)
            val newPipeFormatter: NewPipeDataFormatter<InfoItem> = newPipeFormatterFactory.createForInfoItem()
            val result = newPipeFormatter.run(NewPipeSortingData(searchInfo!!.relatedItems,searchInfo!!.nextPage))
            when(result)
            {
                is FormattingResult.SUCCESS ->{
                    Log.d("TAGGIZ",""+result.data.youtubeSortedData.youtubeSortedList!!.size)

                    Resource.Success(result)

                }
                is FormattingResult.FAILURE ->{
                    Resource.DataError(NEW_PIPE_SEARCH_ERROR)

                }
            }

        }
        return withContext(Dispatchers.IO) {
            val pageSearchInfo = newPipeSearchFor(serviceId, searchString, contentFilter, sortFilter)
            val newPipeFormatter: NewPipeDataFormatter<InfoItem> = newPipeFormatterFactory.createForInfoItem()
            val result = newPipeFormatter.run(NewPipeSortingData(pageSearchInfo.relatedItems,pageSearchInfo.nextPage))
            when(result)
            {
                is FormattingResult.SUCCESS ->{
//                    Log.d("TAGGIZ",""+result.data.youtubeSortedData.youtubeSortedList!!.size)
                    Resource.Success(result.data)

                }
                is FormattingResult.FAILURE ->{
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

    override suspend fun getPlayListInfo( playListUrl: String):Resource<PlayListData>
    {
        return withContext(Dispatchers.IO) {
             val newPipeFormatter: NewPipeDataFormatter<StreamInfoItem?> = newPipeFormatterFactory.createForPlayListItem()
             val newPipePlayList = newPipePlayListData(playListUrl)
             val result = newPipeFormatter.run(NewPipeSortingData(newPipePlayList.relatedItems,newPipePlayList.nextPage))
            when(result)
            {
                is FormattingResult.SUCCESS ->{
//                    Log.d("TAGGIZ",""+result.data.youtubeSortedData.youtubeSortedList!!.size)
                    Resource.Success(PlayListData(newPipePlayList.thumbnails[0].url,result.data.youtubeSortedData.youtubeSortedList))

                }
                is FormattingResult.FAILURE ->{
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
            val newPipeFormatter: NewPipeDataFormatter<InfoItem> = newPipeFormatterFactory.createForInfoItem()
            val nextPageSearchInfo = newPipeSearchNextPageFor(serviceId, searchString, contentFilter, sortFilter, page)
            val result = newPipeFormatter.run(NewPipeSortingData(nextPageSearchInfo.items,nextPageSearchInfo.nextPage))
//            val baseDataModel= TubeFyCoreUniversalData(NewPipeSortingInput(result,nextPageSearchInfo.nextPage))
            when(result)
            {
                is FormattingResult.SUCCESS ->{
                    Log.d("TAGGIZNEXTPAGE",""+result.data.youtubeSortedData.youtubeSortedList!!.size)
                    Resource.Success(result.data)

                }
                is FormattingResult.FAILURE ->{
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