package com.ramzmania.tubefy.data.local

import com.ramzmania.tubefy.core.dataformatter.BasicResponse
import com.ramzmania.tubefy.core.dataformatter.StreamUrlData
import com.ramzmania.tubefy.data.Resource
import com.ramzmania.tubefy.data.dto.youtubestripper.ApiResponse
import kotlinx.coroutines.flow.Flow

interface LocalRepositorySource {
    suspend fun manipulateYoutubeSearchStripData(youtubeJsonScrapping: ApiResponse): Flow<Resource<BasicResponse>>
    suspend fun getStreamUrl(videoId: String): Flow<Resource<StreamUrlData>>

}