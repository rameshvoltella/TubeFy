package com.ramzmania.tubefy.data.database

import com.ramzmania.tubefy.data.Resource
import com.ramzmania.tubefy.data.dto.home.HomePageResponse
import com.ramzmania.tubefy.database.DatabaseResponse
import com.ramzmania.tubefy.database.QuePlaylist
import kotlinx.coroutines.flow.Flow


interface DatabaseRepositorySource {
    suspend fun addPlayList(playlists: List<QuePlaylist>): Flow<Resource<DatabaseResponse>>

    suspend fun getPlaylists(): Flow<Resource<List<QuePlaylist>>>

    suspend fun addSongToQueue(playData: QuePlaylist): Flow<Resource<DatabaseResponse>>

}