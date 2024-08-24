package com.ramzmania.tubefy.data.remote

import androidx.media3.common.MediaItem
import com.ramzmania.tubefy.data.dto.base.searchformat.StreamUrlData
import com.ramzmania.tubefy.data.dto.base.searchformat.TubeFyCoreUniversalData
import com.ramzmania.tubefy.data.Resource
import com.ramzmania.tubefy.data.dto.base.playlist.PlayListData
import com.ramzmania.tubefy.data.dto.home.youtubei.YoutubeiHomeBaseResponse
import com.ramzmania.tubefy.data.dto.youtubemusic.category.MusicCategoryPlayListBase
import com.ramzmania.tubefy.player.YoutubePlayerPlaylistListModel
import kotlinx.coroutines.flow.Flow
import org.schabi.newpipe.extractor.Page

interface RemoteRepositorySource {
    suspend fun getStreamUrl(videoId: String,mediaIndex:Int =-1): Flow<Resource<StreamUrlData>>


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

    suspend fun  getCategoryPlayList(browseId:String,playerId:String):Flow<Resource<List<MusicCategoryPlayListBase?>>>

    suspend fun  getMusicHomeYoutubei():Flow<Resource<YoutubeiHomeBaseResponse>>
    suspend fun  getMusicHomePaginationYoutubei(paginationHex:String,paginationId:String,visitorData:String):Flow<Resource<YoutubeiHomeBaseResponse>>

}