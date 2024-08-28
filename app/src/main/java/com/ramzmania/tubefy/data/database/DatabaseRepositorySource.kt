package com.ramzmania.tubefy.data.database

import com.ramzmania.tubefy.data.Resource
import com.ramzmania.tubefy.data.dto.base.searchformat.TubeFyCoreTypeData
import com.ramzmania.tubefy.data.dto.database.PlaylistNameWithUrl
import com.ramzmania.tubefy.data.dto.home.HomePageResponse
import com.ramzmania.tubefy.database.CustomPlaylist
import com.ramzmania.tubefy.database.DatabaseResponse
import com.ramzmania.tubefy.database.FavoritePlaylist
import com.ramzmania.tubefy.database.QuePlaylist
import kotlinx.coroutines.flow.Flow


interface DatabaseRepositorySource {
    suspend fun addPlayList(playlists: List<QuePlaylist>): Flow<Resource<DatabaseResponse>>

    suspend fun getPlaylists(): Flow<Resource<List<QuePlaylist>>>

    suspend fun addSongToQueue(playData: QuePlaylist): Flow<Resource<DatabaseResponse>>

    suspend fun addActivePlayList(playlists: List<TubeFyCoreTypeData?>,clickPosition:Int): Flow<Resource<DatabaseResponse>>

    suspend fun getAllActivePlaylists(): Flow<Resource<List<TubeFyCoreTypeData?>>>
    suspend fun isFavourite(videoId: String):Flow< Resource<Boolean>>
    suspend fun addToFavorites(favoriteItem: FavoritePlaylist): Flow<Resource<DatabaseResponse>>
    suspend fun getFavorites(): Flow<Resource<List<TubeFyCoreTypeData?>>>
    suspend fun removeFromFavorites(videoId:String): Flow<Resource<DatabaseResponse>>
    suspend fun addToPlaylist(customPlayListData: CustomPlaylist): Flow<Resource<DatabaseResponse>>
    suspend fun getSpecificPlayList(playlistName:String): Flow<Resource<List<TubeFyCoreTypeData?>>>
    suspend fun getAllSavedPlayList(): Flow<Resource<List<PlaylistNameWithUrl>>>
    suspend fun deleteSongFromPlayList(playlistName:String,songName:String):Flow< Resource<DatabaseResponse>>
    suspend fun deleteSpecificPlayList(playlistName:String): Flow<Resource<DatabaseResponse>>
    suspend fun deleteAllFavorites(): Flow<Resource<DatabaseResponse>>


}