package com.ramzmania.tubefy.data.local

import com.ramzmania.tubefy.data.dto.base.searchformat.TubeFyCoreUniversalData
import com.ramzmania.tubefy.data.Resource
import com.ramzmania.tubefy.data.dto.base.playlist.PlayListCategory
import com.ramzmania.tubefy.data.dto.home.HomePageResponse
import com.ramzmania.tubefy.data.dto.youtubemusic.category.YtMusicCategoryBase
import com.ramzmania.tubefy.data.dto.youtubemusic.category.YtMusicCategoryContent
import com.ramzmania.tubefy.data.dto.youtubestripper.ApiResponse
import com.ramzmania.tubefy.data.dto.youtubestripper.MusicHomeResponse2
import kotlinx.coroutines.flow.Flow

interface LocalRepositorySource {
    suspend fun manipulateYoutubeSearchStripData(youtubeJsonScrapping: ApiResponse): Flow<Resource<TubeFyCoreUniversalData>>
    suspend fun manipulateYoutubeMusicHomeStripData(youtubeMusicHomeJsonScrapping: MusicHomeResponse2): Flow<Resource<List<HomePageResponse?>>>
    suspend fun loadDefaultHomePageData(): Flow<Resource<List<HomePageResponse?>>>
    suspend fun manipulateYoutubeMusicCategorySearchStripData(youtubeJsonScrapping: YtMusicCategoryBase): Flow<Resource<List<PlayListCategory?>>>



}