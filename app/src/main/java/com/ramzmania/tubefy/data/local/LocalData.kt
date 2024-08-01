package com.ramzmania.tubefy.data.local

import com.ramzmania.tubefy.core.yotubesearch.BasicResponse
import com.ramzmania.tubefy.data.ContextModule
import com.ramzmania.tubefy.data.Resource
import com.ramzmania.tubefy.data.dto.youtubestripper.ApiResponse
import javax.inject.Inject

class LocalData @Inject
constructor(
    private val contextModule: ContextModule
) :LocalDataSource {
    override suspend fun manipulateYoutubeSearchStripData(youtubeJsonScrapping: ApiResponse): Resource<BasicResponse> {
        TODO("Not yet implemented")
    }
}