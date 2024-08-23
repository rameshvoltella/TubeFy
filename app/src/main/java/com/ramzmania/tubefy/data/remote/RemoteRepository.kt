package com.ramzmania.tubefy.data.remote

import androidx.media3.common.MediaItem
import androidx.room.Index
import com.ramzmania.tubefy.data.dto.base.searchformat.StreamUrlData
import com.ramzmania.tubefy.data.dto.base.searchformat.TubeFyCoreUniversalData
import com.ramzmania.tubefy.data.Resource
import com.ramzmania.tubefy.data.dto.base.playlist.PlayListData
import com.ramzmania.tubefy.data.dto.home.youtubei.YoutubeiHomeBaseResponse
import com.ramzmania.tubefy.data.dto.youtubemusic.category.MusicCategoryPlayListBase
import com.ramzmania.tubefy.player.YoutubePlayerPlaylistListModel
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

    override suspend fun getPlayListInfo(playListUrl: String): Flow<Resource<PlayListData>> {
        return flow {emit(remoteRepository.getPlayListInfo(playListUrl))  }
    }

    override suspend fun getStreamBulkUrl(youtubePlayerPlaylistListModel: YoutubePlayerPlaylistListModel): Flow<Resource<List<MediaItem>>> {
        return flow {emit(remoteRepository.getStreamBulkUrl(youtubePlayerPlaylistListModel))  }
    }

    override suspend fun getCategoryPlayList(
        browseId: String,
        playerId: String
    ): Flow<Resource<List<MusicCategoryPlayListBase?>>> {
        return flow {emit(remoteRepository.getCategoryPlayList(browseId,playerId))  }
    }

    override suspend fun getMusicHomeYoutubei(): Flow<Resource<YoutubeiHomeBaseResponse>> {
        return flow {emit(remoteRepository.getMusicHomeYoutubei())  }
    }

    override suspend fun getMusicHomePaginationYoutubei(
        paginationHex: String,
        paginationId: String,
        visitorData: String
    ): Flow<Resource<YoutubeiHomeBaseResponse>> {
        return flow {emit(remoteRepository.getMusicHomePaginationYoutubei(paginationHex,paginationId,visitorData))  }

    }


    override suspend fun getStreamUrl(videoId: String,mediaIndex: Int): Flow<Resource<StreamUrlData>> {
        return flow { emit(remoteRepository.getStreamUrl(videoId,mediaIndex)) }
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