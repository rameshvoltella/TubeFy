package com.ramzmania.tubefy.data.local

import com.ramzmania.tubefy.core.dataformatter.dto.BasicResponse
import com.ramzmania.tubefy.core.dataformatter.dto.TubeFyCoreTypeData
import com.ramzmania.tubefy.core.dataformatter.stripper.YouTubePageStripDataFormatter
import com.ramzmania.tubefy.data.ContextModule
import com.ramzmania.tubefy.data.Resource
import com.ramzmania.tubefy.data.dto.youtubestripper.ApiResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class LocalData @Inject
constructor(
    private val contextModule: ContextModule
) : LocalDataSource {
    override suspend fun manipulateYoutubeSearchStripData(youtubeJsonScrapping: ApiResponse): Resource<BasicResponse> {
        var sortedVideoDataList: List<TubeFyCoreTypeData>? = null
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