package com.ramzmania.tubefy.data.remote

import android.util.Log
import com.ramzmania.tubefy.core.YoutubeCoreConstant
import com.ramzmania.tubefy.core.YoutubeCoreConstant.YOUTUBE_V3_MAX_RESULT
import com.ramzmania.tubefy.core.dataformatter.dto.NewPipeSortingInput
import com.ramzmania.tubefy.core.dataformatter.newpipe.NewPipeDataFormatter
import com.ramzmania.tubefy.core.dataformatter.dto.StreamUrlData
import com.ramzmania.tubefy.core.dataformatter.dto.TubeFyCoreUniversalData
import com.ramzmania.tubefy.core.newpipeextractor.newPipeSearchFor
import com.ramzmania.tubefy.core.newpipeextractor.newPipeSearchNextPageFor
import com.ramzmania.tubefy.data.NetworkConnectivity
import com.ramzmania.tubefy.data.Resource
import com.ramzmania.tubefy.data.dto.youtubeV3.YoutubeV3Response
import com.ramzmania.tubefy.data.remote.api.ApiServices
import com.ramzmania.tubefy.data.remote.api.ServiceGenerator
import com.ramzmania.tubefy.errors.NETWORK_ERROR
import com.ramzmania.tubefy.errors.NEW_PIPE_SEARCH_ERROR
import com.ramzmania.tubefy.errors.NEW_PIPE_SEARCH_MORE_ERROR
import com.ramzmania.tubefy.errors.NO_INTERNET_CONNECTION
import com.ramzmania.tubefy.errors.SERVER_ERROR
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.schabi.newpipe.extractor.InfoItem
import org.schabi.newpipe.extractor.ListExtractor
import org.schabi.newpipe.extractor.Page
import org.schabi.newpipe.extractor.search.SearchInfo
import org.schabi.newpipe.extractor.services.youtube.YoutubeService
import org.schabi.newpipe.extractor.services.youtube.extractors.YoutubeStreamExtractor
import retrofit2.Response
import java.io.IOException
import javax.inject.Inject

class RemoteData@Inject
constructor(
    private val serviceGenerator: ServiceGenerator,
    private val networkConnectivity: NetworkConnectivity,
    private val newPipeFormatter: NewPipeDataFormatter,
) :RemoteDataSource {
    override suspend fun requestYoutubeV3(
        part: String,
        searchQuery: String,
        pageToken: String?
    ): Resource<YoutubeV3Response> {
        val youtubeV3Service = serviceGenerator.createService(ApiServices::class.java)

        return when (val response = processCall {
            youtubeV3Service.getVideo(
               part,searchQuery,pageToken,YOUTUBE_V3_MAX_RESULT
            )
        }) {
            is Any -> {
                try {
                    (response is YoutubeV3Response).let {

                        Resource.Success(data = response as YoutubeV3Response)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    Resource.DataError(errorCode = response as Int)
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

    override suspend fun getPageSearch(
        serviceId: Int,
        searchString: String,
        contentFilter: List<String?>,
        sortFilter: String
    ): Resource<SearchInfo> {
        var searchInfo: SearchInfo?=null
        withContext(Dispatchers.IO)
        {
            searchInfo= newPipeSearchFor(serviceId, searchString, contentFilter, sortFilter)
            val result = newPipeFormatter.run(NewPipeSortingInput(searchInfo!!.relatedItems,searchInfo!!.nextPage))
            Log.d("TAGGIZ",""+result.youtubeSortedData.youtubeSortedList!!.size)

        }
        return if(searchInfo!=null)
        {
            Resource.Success(searchInfo!!)
        }else
        {
            Resource.DataError(NEW_PIPE_SEARCH_ERROR)
        }
    }

    override suspend fun getPageNextSearch(
        serviceId: Int,
        searchString: String,
        contentFilter: List<String?>,
        sortFilter: String,
        page: Page
    ): Resource<ListExtractor.InfoItemsPage<InfoItem>> {
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
            val nextPageSearchInfo = newPipeSearchNextPageFor(serviceId, searchString, contentFilter, sortFilter, page)
            val result = newPipeFormatter.run(NewPipeSortingInput(nextPageSearchInfo.items,nextPageSearchInfo.nextPage))
//            val baseDataModel= TubeFyCoreUniversalData(NewPipeSortingInput(result,nextPageSearchInfo.nextPage))
            Log.d("TAGGIZ",""+result.youtubeSortedData.youtubeSortedList!!.size)
            nextPageSearchInfo.let {
                Resource.Success(it)
            } ?: Resource.DataError(NEW_PIPE_SEARCH_MORE_ERROR)
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