package com.ramzmania.tubefy.data.remote

import com.ramzmania.tubefy.core.dataformatter.dto.StreamUrlData
import com.ramzmania.tubefy.data.Resource
import com.ramzmania.tubefy.data.dto.youtubeV3.YoutubeV3Response
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.schabi.newpipe.extractor.InfoItem
import org.schabi.newpipe.extractor.ListExtractor
import org.schabi.newpipe.extractor.Page
import org.schabi.newpipe.extractor.search.SearchInfo
import javax.inject.Inject

class RemoteRepository @Inject constructor(
    private val remoteRepository: RemoteData,
):RemoteRepositorySource {
    override suspend fun requestYoutubeV3(
        part: String,
        searchQuery: String,
        pageToken: String?
    ): Flow<Resource<YoutubeV3Response>> {
       return flow {emit(remoteRepository.requestYoutubeV3(part, searchQuery, pageToken))  }
    }


    override suspend fun getStreamUrl(videoId: String): Flow<Resource<StreamUrlData>> {
        return flow { emit(remoteRepository.getStreamUrl(videoId)) }
    }

    override suspend fun getPageSearch(
        serviceId: Int,
        searchString: String,
        contentFilter: List<String?>,
        sortFilter: String
    ): Flow<Resource<SearchInfo>> {
        return flow { emit(remoteRepository.getPageSearch(serviceId,searchString,contentFilter, sortFilter)) }
    }

    override suspend fun getPageNextSearch(
        serviceId: Int,
        searchString: String,
        contentFilter: List<String?>,
        sortFilter: String,
        page: Page
    ): Flow<Resource<ListExtractor.InfoItemsPage<InfoItem>>> {
        return flow { emit(remoteRepository.getPageNextSearch(serviceId, searchString, contentFilter, sortFilter, page)) }
    }

}