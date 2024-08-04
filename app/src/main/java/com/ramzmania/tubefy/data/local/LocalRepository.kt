package com.ramzmania.tubefy.data.local

import com.ramzmania.tubefy.core.dataformatter.dto.TubeFyCoreUniversalData
import com.ramzmania.tubefy.data.Resource
import com.ramzmania.tubefy.data.dto.youtubestripper.ApiResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class LocalRepository @Inject constructor(
    private val localRepository: LocalData,
):LocalRepositorySource {
    override suspend fun manipulateYoutubeSearchStripData(youtubeJsonScrapping: ApiResponse): Flow<Resource<TubeFyCoreUniversalData>> {
        return flow { emit(localRepository.manipulateYoutubeSearchStripData(youtubeJsonScrapping)) }
    }

}