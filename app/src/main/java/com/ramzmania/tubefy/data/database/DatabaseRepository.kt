package com.ramzmania.tubefy.data.database

import android.util.Log
import com.ramzmania.tubefy.data.Resource
import com.ramzmania.tubefy.data.dto.base.searchformat.TubeFyCoreTypeData
import com.ramzmania.tubefy.data.dto.database.PlaylistNameWithUrl
import com.ramzmania.tubefy.database.CustomPlaylist
import com.ramzmania.tubefy.database.DatabaseResponse
import com.ramzmania.tubefy.database.FavoritePlaylist
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

    override suspend fun addSongToQueue(playData: QuePlaylist): Flow<Resource<DatabaseResponse>> {
        return flow {emit(databaseData.addSongToQueue(playData))  }
    }

    override suspend fun addActivePlayList(playlists: List<TubeFyCoreTypeData?>,clickPosition:Int): Flow<Resource<DatabaseResponse>> {
        return flow { emit(databaseData.addActivePlayList(playlists,clickPosition)) }
    }

    override suspend fun getAllActivePlaylists(): Flow<Resource<List<TubeFyCoreTypeData?>>> {
       return flow { emit(databaseData.getAllActivePlaylists()) }
    }

    override suspend fun isFavourite(videoId: String): Flow<Resource<Boolean>> {
        return flow { emit(databaseData.isFavourite(videoId)) }
    }

    override suspend fun addToFavorites(favoriteItem: FavoritePlaylist): Flow<Resource<DatabaseResponse>> {

        return flow { emit(databaseData.addToFavorites(favoriteItem)) }
    }

    override suspend fun getFavorites(): Flow<Resource<List<TubeFyCoreTypeData?>>> {
        return flow { emit(databaseData.getFavorites()) }
    }

    override suspend fun removeFromFavorites(videoId: String): Flow<Resource<DatabaseResponse>> {
        return flow { emit(databaseData.removeFromFavorites(videoId)) }
    }

    override suspend fun addToPlaylist(customPlayListData: CustomPlaylist): Flow<Resource<DatabaseResponse>> {
        return flow { emit(databaseData.addToPlaylist(customPlayListData)) }
    }

    override suspend fun getSpecificPlayList(playlistName: String): Flow<Resource<List<TubeFyCoreTypeData?>>> {
        return flow { emit(databaseData.getSpecificPlayList(playlistName)) }
    }

    override suspend fun getAllSavedPlayList(): Flow<Resource<List<PlaylistNameWithUrl>>> {
        return flow { emit(databaseData.getAllSavedPlayList()) }
    }

    override suspend fun deleteSongFromPlayList(
        playlistName: String,
        songName: String
    ): Flow<Resource<DatabaseResponse>> {
        return flow { emit(databaseData.deleteSongFromPlayList(playlistName,songName)) }
    }

    override suspend fun deleteSpecificPlayList(playlistName: String): Flow<Resource<DatabaseResponse>> {
        return flow { emit(databaseData.deleteSpecificPlayList(playlistName)) }
    }

    override suspend fun deleteAllFavorites(): Flow<Resource<DatabaseResponse>> {
        return flow { emit(databaseData.deleteAllFavorites()) }
    }


}