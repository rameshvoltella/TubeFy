package com.ramzmania.tubefy.data.local

import com.ramzmania.tubefy.core.dataformatter.dto.BasicResponse
import com.ramzmania.tubefy.data.Resource
import com.ramzmania.tubefy.data.dto.youtubestripper.ApiResponse
import kotlinx.coroutines.flow.Flow

interface LocalRepositorySource {
    suspend fun manipulateYoutubeSearchStripData(youtubeJsonScrapping: ApiResponse): Flow<Resource<BasicResponse>>

}