package com.ramzmania.tubefy.data.local

import com.ramzmania.tubefy.core.dataformatter.BasicResponse
import com.ramzmania.tubefy.core.dataformatter.StreamUrlData
import com.ramzmania.tubefy.data.Resource
import com.ramzmania.tubefy.data.dto.youtubestripper.ApiResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.schabi.newpipe.extractor.InfoItem
import org.schabi.newpipe.extractor.ListExtractor
import org.schabi.newpipe.extractor.Page
import org.schabi.newpipe.extractor.search.SearchInfo
import javax.inject.Inject

class LocalRepository @Inject constructor(
    private val localRepository: LocalData,
):LocalRepositorySource {
    override suspend fun manipulateYoutubeSearchStripData(youtubeJsonScrapping: ApiResponse): Flow<Resource<BasicResponse>> {
        return flow { emit(localRepository.manipulateYoutubeSearchStripData(youtubeJsonScrapping)) }
    }

    override suspend fun getStreamUrl(videoId: String): Flow<Resource<StreamUrlData>> {
        return flow { emit(localRepository.getStreamUrl(videoId)) }
    }

    override suspend fun getPageSearch(
        serviceId: Int,
        searchString: String,
        contentFilter: List<String?>,
        sortFilter: String
    ): Flow<Resource<SearchInfo>> {
        return flow { emit(localRepository.getPageSearch(serviceId,searchString,contentFilter, sortFilter)) }
    }

    override suspend fun getPageNextSearch(
        serviceId: Int,
        searchString: String,
        contentFilter: List<String?>,
        sortFilter: String,
        page: Page
    ): Flow<Resource<ListExtractor.InfoItemsPage<InfoItem>>> {
        return flow { emit(localRepository.getPageNextSearch(serviceId, searchString, contentFilter, sortFilter, page)) }
    }
}