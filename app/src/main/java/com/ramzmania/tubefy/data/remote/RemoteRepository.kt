package com.ramzmania.tubefy.data.remote

import com.ramzmania.tubefy.core.dataformatter.dto.StreamUrlData
import com.ramzmania.tubefy.core.dataformatter.dto.TubeFyCoreUniversalData
import com.ramzmania.tubefy.data.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.schabi.newpipe.extractor.Page
import javax.inject.Inject

class RemoteRepository @Inject constructor(
    private val remoteRepository: RemoteData,
):RemoteRepositorySource {
    override suspend fun requestYoutubeV3(
        part: String,
        searchQuery: String,
        pageToken: String?
    ): Flow<Resource<TubeFyCoreUniversalData>> {
       return flow {emit(remoteRepository.requestYoutubeV3(part, searchQuery, pageToken))  }
    }


    override suspend fun getStreamUrl(videoId: String): Flow<Resource<StreamUrlData>> {
        return flow { emit(remoteRepository.getStreamUrl(videoId)) }
    }

    override suspend fun getNewPipePageSearch(
        serviceId: Int,
        searchString: String,
        contentFilter: List<String?>,
        sortFilter: String
    ): Flow<Resource<TubeFyCoreUniversalData>> {
        return flow { emit(remoteRepository.getNewPipePageSearch(serviceId,searchString,contentFilter, sortFilter)) }
    }

    override suspend fun getNewPipePageNextSearch(
        serviceId: Int,
        searchString: String,
        contentFilter: List<String?>,
        sortFilter: String,
        page: Page
    ): Flow<Resource<TubeFyCoreUniversalData>> {
        return flow { emit(remoteRepository.getNewPipePageNextSearch(serviceId, searchString, contentFilter, sortFilter, page)) }
    }

}