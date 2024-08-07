package com.ramzmania.tubefy.data.local

import com.ramzmania.tubefy.data.dto.searchformat.TubeFyCoreUniversalData
import com.ramzmania.tubefy.data.Resource
import com.ramzmania.tubefy.data.dto.home.HomePageResponse
import com.ramzmania.tubefy.data.dto.youtubestripper.ApiResponse
import com.ramzmania.tubefy.data.dto.youtubestripper.MusicHomeResponse2
import kotlinx.coroutines.flow.Flow

interface LocalRepositorySource {
    suspend fun manipulateYoutubeSearchStripData(youtubeJsonScrapping: ApiResponse): Flow<Resource<TubeFyCoreUniversalData>>
    suspend fun manipulateYoutubeMusicHomeStripData(youtubeMusicHomeJsonScrapping: MusicHomeResponse2): Flow<Resource<List<HomePageResponse?>>>

}