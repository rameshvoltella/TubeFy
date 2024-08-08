package com.ramzmania.tubefy.data.local

import com.ramzmania.tubefy.data.dto.searchformat.TubeFyCoreUniversalData
import com.ramzmania.tubefy.data.Resource
import com.ramzmania.tubefy.data.dto.home.HomePageResponse
import com.ramzmania.tubefy.data.dto.youtubestripper.ApiResponse
import com.ramzmania.tubefy.data.dto.youtubestripper.MusicHomeResponse2
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class LocalRepository @Inject constructor(
    private val localRepository: LocalData,
):LocalRepositorySource {
    override suspend fun manipulateYoutubeSearchStripData(youtubeJsonScrapping: ApiResponse): Flow<Resource<TubeFyCoreUniversalData>> {
        return flow { emit(localRepository.manipulateYoutubeSearchStripData(youtubeJsonScrapping)) }
    }

    override suspend fun manipulateYoutubeMusicHomeStripData(youtubeMusicHomeJsonScrapping: MusicHomeResponse2): Flow<Resource<List<HomePageResponse?>>> {
        return flow { emit(localRepository.manipulateYoutubeMusicHomeStripData(youtubeMusicHomeJsonScrapping)) }
    }

    override suspend fun loadDefaultHomePageData(): Flow<Resource<List<HomePageResponse?>>> {
        return flow { emit(localRepository.loadDefaultHomePageData()) }
    }

}