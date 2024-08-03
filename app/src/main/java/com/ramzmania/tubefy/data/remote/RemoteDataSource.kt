package com.ramzmania.tubefy.data.remote

import com.ramzmania.tubefy.core.dataformatter.dto.StreamUrlData
import com.ramzmania.tubefy.data.Resource
import com.ramzmania.tubefy.data.dto.youtubeV3.YoutubeV3Response
import org.schabi.newpipe.extractor.InfoItem
import org.schabi.newpipe.extractor.ListExtractor
import org.schabi.newpipe.extractor.Page
import org.schabi.newpipe.extractor.search.SearchInfo

interface RemoteDataSource {
    suspend fun requestYoutubeV3(part: String,
                                  searchQuery: String,
                                 pageToken: String?): Resource<YoutubeV3Response>

    suspend fun getStreamUrl(videoId:String):Resource<StreamUrlData>

    /*NewPipe search sources*/

    suspend fun getPageSearch( serviceId: Int, searchString: String,
                               contentFilter: List<String?>,
                               sortFilter: String):Resource<SearchInfo>

    suspend fun getPageNextSearch(serviceId: Int, searchString: String,
                                  contentFilter: List<String?>,
                                  sortFilter: String,page: Page
    ):Resource<ListExtractor.InfoItemsPage<InfoItem>>

}