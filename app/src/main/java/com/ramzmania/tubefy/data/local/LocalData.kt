package com.ramzmania.tubefy.data.local

import com.ramzmania.tubefy.core.dataformatter.BasicResponse
import com.ramzmania.tubefy.core.dataformatter.StreamUrlData
import com.ramzmania.tubefy.core.dataformatter.TubeFyBaseData
import com.ramzmania.tubefy.core.dataformatter.YouTubePageStripDataFormatter
import com.ramzmania.tubefy.core.YoutubeCoreConstant.YOUTUBE_WATCH_URL
import com.ramzmania.tubefy.core.newpipeextractor.newPipeSearchFor
import com.ramzmania.tubefy.core.newpipeextractor.newPipeSearchNextPageFor
import com.ramzmania.tubefy.data.ContextModule
import com.ramzmania.tubefy.data.Resource
import com.ramzmania.tubefy.data.dto.youtubestripper.ApiResponse
import com.ramzmania.tubefy.errors.NEW_PIPE_SEARCH_ERROR
import com.ramzmania.tubefy.errors.NEW_PIPE_SEARCH_MORE_ERROR
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.schabi.newpipe.extractor.InfoItem
import org.schabi.newpipe.extractor.ListExtractor
import org.schabi.newpipe.extractor.Page
import org.schabi.newpipe.extractor.search.SearchInfo
import org.schabi.newpipe.extractor.services.youtube.YoutubeService
import org.schabi.newpipe.extractor.services.youtube.extractors.YoutubeStreamExtractor
import javax.inject.Inject

class LocalData @Inject
constructor(
    private val contextModule: ContextModule
) : LocalDataSource {
    override suspend fun manipulateYoutubeSearchStripData(youtubeJsonScrapping: ApiResponse): Resource<BasicResponse> {
        var sortedVideoDataList: List<TubeFyBaseData>? = null
        withContext(Dispatchers.IO)
        {
            sortedVideoDataList =
                YouTubePageStripDataFormatter().getFormattedData(youtubeJsonScrapping)
        }

        return if (sortedVideoDataList != null) {
            Resource.Success(BasicResponse(sortedVideoDataList!!))

        } else {
            Resource.DataError(401)
        }

    }

    override suspend fun getStreamUrl(videoId: String): Resource<StreamUrlData> {
        var streamUrl: String = ""

        withContext(Dispatchers.IO)
        {
            try {
                val extractor =
                    YoutubeService(0).getStreamExtractor("$YOUTUBE_WATCH_URL$videoId") as YoutubeStreamExtractor
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
        var searchInfo:SearchInfo?=null
        withContext(Dispatchers.IO)
        {
            searchInfo= newPipeSearchFor(serviceId, searchString, contentFilter, sortFilter)
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
            nextPageSearchInfo.let {
                Resource.Success(it)
            } ?: Resource.DataError(NEW_PIPE_SEARCH_MORE_ERROR)
        }

    }
}