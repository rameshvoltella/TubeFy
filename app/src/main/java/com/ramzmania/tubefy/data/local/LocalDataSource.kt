package com.ramzmania.tubefy.data.local

import com.ramzmania.tubefy.core.dataformatter.dto.BasicResponse
import com.ramzmania.tubefy.core.dataformatter.dto.TubeFyCoreUniversalData
import com.ramzmania.tubefy.data.Resource
import com.ramzmania.tubefy.data.dto.youtubestripper.ApiResponse

interface LocalDataSource {

    suspend fun manipulateYoutubeSearchStripData(youtubeJsonScrapping: ApiResponse): Resource<TubeFyCoreUniversalData>

}