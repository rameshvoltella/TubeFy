package com.ramzmania.tubefy.data.remote

import androidx.media3.common.MediaItem
import com.ramzmania.tubefy.data.dto.searchformat.StreamUrlData
import com.ramzmania.tubefy.data.dto.searchformat.TubeFyCoreUniversalData
import com.ramzmania.tubefy.data.Resource
import com.ramzmania.tubefy.data.dto.playlist.PlayListData
import com.ramzmania.tubefy.player.YoutubePlayerPlaylistListModel
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

    suspend fun getPlayListInfo(playListUrl:String):Flow<Resource<PlayListData>>

    suspend fun  getStreamBulkUrl(youtubePlayerPlaylistListModel: YoutubePlayerPlaylistListModel):Flow<Resource<List<MediaItem>>>

}