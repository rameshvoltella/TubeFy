package com.ramzmania.tubefy.data.remote

import com.ramzmania.tubefy.core.dataformatter.dto.StreamUrlData
import com.ramzmania.tubefy.core.dataformatter.dto.TubeFyCoreUniversalData
import com.ramzmania.tubefy.data.Resource
import org.schabi.newpipe.extractor.Page

interface RemoteDataSource {
    suspend fun requestYoutubeV3(part: String,
                                  searchQuery: String,
                                 pageToken: String?): Resource<TubeFyCoreUniversalData>

    suspend fun getStreamUrl(videoId:String):Resource<StreamUrlData>

    /*NewPipe search sources*/

    suspend fun getNewPipePageSearch( serviceId: Int, searchString: String,
                               contentFilter: List<String?>,
                               sortFilter: String):Resource<TubeFyCoreUniversalData>

    suspend fun getNewPipePageNextSearch(serviceId: Int, searchString: String,
                                  contentFilter: List<String?>,
                                  sortFilter: String,page: Page
    ):Resource<TubeFyCoreUniversalData>

}