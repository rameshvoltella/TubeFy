package com.ramzmania.tubefy.data.database

import com.ramzmania.tubefy.data.Resource
import com.ramzmania.tubefy.data.dto.base.searchformat.TubeFyCoreTypeData
import com.ramzmania.tubefy.database.DatabaseResponse
import com.ramzmania.tubefy.database.QuePlaylist
import kotlinx.coroutines.flow.Flow

interface DatabaseDataSource {
    suspend fun addPlayList(playlists: List<QuePlaylist>): Resource<DatabaseResponse>
    suspend fun getPlaylists(): Resource<List<QuePlaylist>>
    suspend fun addSongToQueue(playData: QuePlaylist): Resource<DatabaseResponse>

    suspend fun addActivePlayList(playlists: List<TubeFyCoreTypeData?>): Resource<DatabaseResponse>

    suspend fun getAllActivePlaylists(): Resource<List<TubeFyCoreTypeData?>>


}