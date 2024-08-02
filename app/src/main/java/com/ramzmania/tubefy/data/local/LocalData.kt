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

}