package com.ramzmania.tubefy.data.remote

import androidx.media3.common.MediaItem
import com.ramzmania.tubefy.data.dto.base.searchformat.StreamUrlData
import com.ramzmania.tubefy.data.dto.base.searchformat.TubeFyCoreUniversalData
import com.ramzmania.tubefy.data.Resource
import com.ramzmania.tubefy.data.dto.base.playlist.PlayListData
import com.ramzmania.tubefy.data.dto.youtubemusic.category.MusicCategoryPlayListBase
import com.ramzmania.tubefy.data.dto.youtubemusic.playlist.categoryplaylist.CategoryPlayListRoot
import com.ramzmania.tubefy.player.YoutubePlayerPlaylistListModel
import org.schabi.newpipe.extractor.Page

interface RemoteDataSource {
    suspend fun requestYoutubeV3(part: String,
                                  searchQuery: String,
                                 pageToken: String?): Resource<TubeFyCoreUniversalData>

    suspend fun getStreamUrl(videoId:String):Resource<StreamUrlData>

    suspend fun getPlayListInfo(playListUrl:String):Resource<PlayListData>


    /*NewPipe search sources*/

    suspend fun getNewPipePageSearch( serviceId: Int, searchString: String,
                               contentFilter: List<String?>,
                               sortFilter: String):Resource<TubeFyCoreUniversalData>

    suspend fun getNewPipePageNextSearch(serviceId: Int, searchString: String,
                                  contentFilter: List<String?>,
                                  sortFilter: String,page: Page
    ):Resource<TubeFyCoreUniversalData>


    suspend fun  getStreamBulkUrl(youtubePlayerPlaylistListModel: YoutubePlayerPlaylistListModel):Resource<List<MediaItem>>

    suspend fun  getCategoryPlayList(browseId:String,playerId:String):Resource<List<MusicCategoryPlayListBase?>>


}