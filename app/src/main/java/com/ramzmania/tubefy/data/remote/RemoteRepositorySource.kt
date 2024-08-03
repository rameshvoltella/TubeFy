package com.ramzmania.tubefy.data.remote

import com.ramzmania.tubefy.core.dataformatter.dto.StreamUrlData
import com.ramzmania.tubefy.data.Resource
import com.ramzmania.tubefy.data.dto.youtubeV3.YoutubeV3Response
import kotlinx.coroutines.flow.Flow
import org.schabi.newpipe.extractor.InfoItem
import org.schabi.newpipe.extractor.ListExtractor
import org.schabi.newpipe.extractor.Page
import org.schabi.newpipe.extractor.search.SearchInfo

interface RemoteRepositorySource {
    suspend fun getStreamUrl(videoId: String): Flow<Resource<StreamUrlData>>


    suspend fun getPageSearch( serviceId: Int, searchString: String,
                               contentFilter: List<String?>,
                               sortFilter: String):Flow<Resource<SearchInfo>>

    suspend fun getPageNextSearch(serviceId: Int, searchString: String,
                                  contentFilter: List<String?>,
                                  sortFilter: String,page: Page
    ):Flow<Resource<ListExtractor.InfoItemsPage<InfoItem>>>
   suspend fun requestYoutubeV3(part: String,
                                searchQuery: String,
                                pageToken: String?): Flow<Resource<YoutubeV3Response>>

}