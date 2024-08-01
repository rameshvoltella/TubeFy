package com.ramzmania.tubefy.data.local

import com.ramzmania.tubefy.core.dataformatter.BasicResponse
import com.ramzmania.tubefy.core.dataformatter.TubeFyBaseData
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
        var sortedVideoDataList: List<TubeFyBaseData>? = null
        withContext(Dispatchers.IO)
        {
//WriteLogic for data

        }

        return if (sortedVideoDataList != null) {
            // Thread.sleep(3000)

            Resource.Success(BasicResponse(sortedVideoDataList))
        } else {
            Resource.DataError(401)
        }

    }
}