package com.ramzmania.tubefy.data.database

import android.util.Log
import com.ramzmania.tubefy.data.Resource
import com.ramzmania.tubefy.data.dto.base.searchformat.TubeFyCoreTypeData
import com.ramzmania.tubefy.database.DatabaseResponse
import com.ramzmania.tubefy.database.PlaylistDao
import com.ramzmania.tubefy.database.QuePlaylist
import com.ramzmania.tubefy.errors.DATABASE_INSERTION_ERROR
import com.ramzmania.tubefy.errors.DATABASE_PLAYLIST_ERROR
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class DatabaseData @Inject constructor(private val playlistDao: PlaylistDao):DatabaseDataSource {
    override suspend fun addPlayList(playlists: List<QuePlaylist>): Resource<DatabaseResponse> {
        return withContext(Dispatchers.IO)
        {
            val result=playlistDao.addQuePlaylists(playlists)
            if(result)
            {
                 Resource.Success(DatabaseResponse(200))
            }else
            {
                 Resource.DataError(DATABASE_INSERTION_ERROR)

            }

        }

    }

    override suspend fun getPlaylists(): Resource<List<QuePlaylist>> {
        return withContext(Dispatchers.IO)
        {
            Log.d("handleMediaItemChange,","<><><><><><6666666")

            val result = playlistDao.getAllQuePlaylist()

            if (result != null && result.size > 0) {
                Log.d("handleMediaItemChange,","<><><><><><90909")

                Resource.Success(result)
            } else {
                Log.d("handleMediaItemChange,","<><><><><><88888")

                Resource.DataError(DATABASE_PLAYLIST_ERROR)
            }
        }
    }

    override suspend fun addSongToQueue(songData: QuePlaylist): Resource<DatabaseResponse> {
        return withContext(Dispatchers.IO)
        {
            Log.d("Tagger","yono")
            val result=playlistDao.addQueSingleSongPlaylists(songData)
            if(result)
            {
                Resource.Success(DatabaseResponse(200))
            }else
            {
                Resource.DataError(DATABASE_INSERTION_ERROR)

            }

        }
    }

    override suspend fun addActivePlayList(playlists: List<TubeFyCoreTypeData>): Resource<DatabaseResponse> {
        TODO("Not yet implemented")
    }

    override suspend fun getAllActivePlaylists(): Resource<List<TubeFyCoreTypeData>> {
        TODO("Not yet implemented")
    }
}