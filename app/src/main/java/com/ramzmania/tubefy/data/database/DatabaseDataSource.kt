package com.ramzmania.tubefy.data.database

import com.ramzmania.tubefy.data.Resource
import com.ramzmania.tubefy.data.dto.base.searchformat.TubeFyCoreTypeData
import com.ramzmania.tubefy.data.dto.database.PlaylistNameWithUrl
import com.ramzmania.tubefy.database.CustomPlaylist
import com.ramzmania.tubefy.database.DatabaseResponse
import com.ramzmania.tubefy.database.FavoritePlaylist
import com.ramzmania.tubefy.database.QuePlaylist
import kotlinx.coroutines.flow.Flow

interface DatabaseDataSource {
    suspend fun addPlayList(playlists: List<QuePlaylist>): Resource<DatabaseResponse>
    suspend fun getPlaylists(): Resource<List<QuePlaylist>>
    suspend fun addSongToQueue(playData: QuePlaylist): Resource<DatabaseResponse>

    suspend fun addActivePlayList(playlists: List<TubeFyCoreTypeData?>): Resource<DatabaseResponse>

    suspend fun getAllActivePlaylists(): Resource<List<TubeFyCoreTypeData?>>


    suspend fun isFavourite(videoId: String): Resource<Boolean>
    suspend fun addToFavorites(favoriteItem:FavoritePlaylist): Resource<DatabaseResponse>
    suspend fun getFavorites(): Resource<List<TubeFyCoreTypeData?>>
    suspend fun removeFromFavorites(videoId:String): Resource<DatabaseResponse>
    suspend fun addToPlaylist(customPlayListData:CustomPlaylist): Resource<DatabaseResponse>
    suspend fun getSpecificPlayList(playlistName:String): Resource<List<TubeFyCoreTypeData?>>
    suspend fun getAllSavedPlayList(): Resource<List<PlaylistNameWithUrl>>
    suspend fun deleteSongFromPlayList(playlistName:String,songName:String): Resource<DatabaseResponse>
    suspend fun deleteSpecificPlayList(playlistName:String): Resource<DatabaseResponse>




}