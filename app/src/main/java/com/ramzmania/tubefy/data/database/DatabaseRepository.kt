package com.ramzmania.tubefy.data.database

import com.ramzmania.tubefy.data.Resource
import com.ramzmania.tubefy.database.DatabaseResponse
import com.ramzmania.tubefy.database.PlaylistDao
import com.ramzmania.tubefy.database.QuePlaylist
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DatabaseRepository @Inject constructor(
    val databaseData: DatabaseData
):DatabaseRepositorySource {

//    suspend fun addPlaylists(playlists: List<QuePlaylist>) {
//        playlistDao.addQuePlaylists(playlists)
//    }
//
//    suspend fun getPlaylists(): List<QuePlaylist> {
//        return playlistDao.getAllQuePlaylist()
//    }

    override suspend fun addPlayList(playlists: List<QuePlaylist>): Flow<Resource<DatabaseResponse>> {
        return flow {emit(databaseData.addPlayList(playlists))  }
    }

    override suspend fun getPlaylists(): Flow<Resource<List<QuePlaylist>>> {
        return flow { emit(databaseData.getPlaylists()) }
    }
}