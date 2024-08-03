package com.ramzmania.tubefy.data.local

import com.ramzmania.tubefy.core.dataformatter.FormattingResult
import com.ramzmania.tubefy.core.dataformatter.dto.BasicResponse
import com.ramzmania.tubefy.core.dataformatter.dto.TubeFyCoreTypeData
import com.ramzmania.tubefy.core.dataformatter.webscrapper.YouTubePageStripDataFormatter
import com.ramzmania.tubefy.core.dataformatter.webscrapper.YoutubeWebDataFormatter
import com.ramzmania.tubefy.data.ContextModule
import com.ramzmania.tubefy.data.Resource
import com.ramzmania.tubefy.data.dto.youtubeV3.YoutubeSearchResponse
import com.ramzmania.tubefy.data.dto.youtubestripper.ApiResponse
import com.ramzmania.tubefy.errors.YOUTUBE_V3_SEARCH_ERROR
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class LocalData @Inject
constructor(
    private val youtubeWebDataFormatter: YoutubeWebDataFormatter
) : LocalDataSource {
    override suspend fun manipulateYoutubeSearchStripData(youtubeJsonScrapping: ApiResponse): Resource<BasicResponse> {
        var sortedVideoDataList: List<TubeFyCoreTypeData>? = null
        withContext(Dispatchers.IO)
        {
            val result=youtubeWebDataFormatter.run(youtubeJsonScrapping)
             when(result)
            {
                is FormattingResult.SUCCESS->{  Resource.Success(result.data)
                }
                is FormattingResult.FAILURE->{Resource.DataError(YOUTUBE_V3_SEARCH_ERROR)}

            }
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