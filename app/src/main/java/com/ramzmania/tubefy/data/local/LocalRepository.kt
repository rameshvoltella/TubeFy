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

    override suspend fun manipulateYoutubeMusicCategorySearchStripData(youtubeJsonScrapping: YtMusicCategoryBase): Flow<Resource<List<PlayListCategory?>>> {
        return flow { emit(localRepository.manipulateYoutubeMusicCategorySearchStripData(youtubeJsonScrapping)) }
    }

}