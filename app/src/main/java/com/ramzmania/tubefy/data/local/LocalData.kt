package com.ramzmania.tubefy.data.local

import com.ramzmania.tubefy.core.dataformatter.BasicResponse
import com.ramzmania.tubefy.core.dataformatter.StreamUrlData
import com.ramzmania.tubefy.core.dataformatter.TubeFyBaseData
import com.ramzmania.tubefy.core.dataformatter.YouTubePageStripDataFormatter
import com.ramzmania.tubefy.core.yotubesearch.YoutubeConstant.YOUTUBE_WATCH_URL
import com.ramzmania.tubefy.data.ContextModule
import com.ramzmania.tubefy.data.Resource
import com.ramzmania.tubefy.data.dto.youtubestripper.ApiResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
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
}