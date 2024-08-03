package com.ramzmania.tubefy.data.remote

import com.ramzmania.tubefy.core.dataformatter.dto.StreamUrlData
import com.ramzmania.tubefy.core.dataformatter.dto.TubeFyCoreUniversalData
import com.ramzmania.tubefy.data.Resource
import kotlinx.coroutines.flow.Flow
import org.schabi.newpipe.extractor.Page

interface RemoteRepositorySource {
    suspend fun getStreamUrl(videoId: String): Flow<Resource<StreamUrlData>>


    suspend fun getNewPipePageSearch( serviceId: Int, searchString: String,
                               contentFilter: List<String?>,
                               sortFilter: String):Flow<Resource<TubeFyCoreUniversalData>>

    suspend fun getNewPipePageNextSearch(serviceId: Int, searchString: String,
                                  contentFilter: List<String?>,
                                  sortFilter: String,page: Page
    ):Flow<Resource<TubeFyCoreUniversalData>>
   suspend fun requestYoutubeV3(part: String,
                                searchQuery: String,
                                pageToken: String?): Flow<Resource<TubeFyCoreUniversalData>>

}