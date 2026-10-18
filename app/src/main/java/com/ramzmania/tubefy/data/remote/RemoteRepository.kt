package com.ramzmania.tubefy.data.remote

import com.ramzmania.tubefy.data.dto.base.searchformat.TubeFyCoreTypeData
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
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.CancellationException
import com.ramzmania.tubefy.errors.NETWORK_ERROR
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
       return safeFlow { remoteRepository.requestYoutubeV3(part, searchQuery, pageToken) }
    }

    override suspend fun getPlayListInfo(playListUrl: String): Flow<Resource<PlayListData>> {
        return safeFlow { remoteRepository.getPlayListInfo(playListUrl) }
    }

    override suspend fun getMixQueue(videoId: String): Flow<Resource<List<TubeFyCoreTypeData>>> {
        return safeFlow { remoteRepository.getMixQueue(videoId) }
    }

    override suspend fun getStreamBulkUrl(youtubePlayerPlaylistListModel: YoutubePlayerPlaylistListModel): Flow<Resource<List<MediaItem>>> {
        return safeFlow { remoteRepository.getStreamBulkUrl(youtubePlayerPlaylistListModel) }
    }

    override suspend fun getCategoryPlayList(
        browseId: String,
        playerId: String
    ): Flow<Resource<List<MusicCategoryPlayListBase?>>> {
        return safeFlow { remoteRepository.getCategoryPlayList(browseId,playerId) }
    }

    override suspend fun getMusicHomeYoutubei(): Flow<Resource<YoutubeiHomeBaseResponse>> {
        return safeFlow { remoteRepository.getMusicHomeYoutubei() }
    }

    override suspend fun getMusicHomePaginationYoutubei(
        paginationHex: String,
        paginationId: String,
        visitorData: String
    ): Flow<Resource<YoutubeiHomeBaseResponse>> {
        return safeFlow { remoteRepository.getMusicHomePaginationYoutubei(paginationHex,paginationId,visitorData) }

    }


    override suspend fun getStreamUrl(videoId: String,mediaIndex: Int): Flow<Resource<StreamUrlData>> {
        return safeFlow { remoteRepository.getStreamUrl(videoId,mediaIndex) }
    }

    override suspend fun getNewPipePageSearch(
        serviceId: Int,
        searchString: String,
        contentFilter: List<String?>,
        sortFilter: String
    ): Flow<Resource<TubeFyCoreUniversalData>> {
        return safeFlow { remoteRepository.getNewPipePageSearch(serviceId,searchString,contentFilter, sortFilter) }
    }

    override suspend fun getNewPipePageNextSearch(
        serviceId: Int,
        searchString: String,
        contentFilter: List<String?>,
        sortFilter: String,
        page: Page
    ): Flow<Resource<TubeFyCoreUniversalData>> {
        return safeFlow { remoteRepository.getNewPipePageNextSearch(serviceId, searchString, contentFilter, sortFilter, page) }
    }

    /**
     * Network failures (no signal, timeouts, dropped connections) become a [Resource.DataError]
     * instead of an exception that would crash the app.
     */
    private fun <T> safeFlow(block: suspend () -> Resource<T>): Flow<Resource<T>> =
        flow { emit(block()) }.catch { error ->
            if (error is CancellationException) throw error
            error.printStackTrace()
            emit(Resource.DataError(NETWORK_ERROR))
        }
}
