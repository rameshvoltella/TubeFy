package com.ramzmania.tubefy.data.local

import com.ramzmania.tubefy.core.yotubesearch.BasicResponse
import com.ramzmania.tubefy.core.yotubesearch.scrapping.YoutubeJsonScrapping
import com.ramzmania.tubefy.data.Resource
import com.ramzmania.tubefy.data.dto.youtubestripper.ApiResponse

interface LocalDataSource {

    suspend fun manipulateYoutubeSearchStripData(youtubeJsonScrapping: ApiResponse): Resource<BasicResponse>
}