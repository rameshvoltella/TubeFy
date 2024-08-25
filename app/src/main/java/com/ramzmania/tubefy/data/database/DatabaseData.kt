package com.ramzmania.tubefy.data.database

import android.util.Log
import com.ramzmania.tubefy.core.dataformatter.FormattingResult
import com.ramzmania.tubefy.core.dataformatter.database.DatabaseFormatterFactory
import com.ramzmania.tubefy.data.Resource
import com.ramzmania.tubefy.data.dto.base.searchformat.TubeFyCoreTypeData
import com.ramzmania.tubefy.data.dto.database.PlaylistNameWithUrl
import com.ramzmania.tubefy.database.CustomPlaylist
import com.ramzmania.tubefy.database.DatabaseResponse
import com.ramzmania.tubefy.database.FavoritePlaylist
import com.ramzmania.tubefy.database.PlaylistDao
import com.ramzmania.tubefy.database.QuePlaylist
import com.ramzmania.tubefy.errors.DATABASE_INSERTION_ERROR
import com.ramzmania.tubefy.errors.DATABASE_PLAYLIST_ERROR
import com.ramzmania.tubefy.errors.YOUTUBE_V3_SEARCH_ERROR
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class DatabaseData @Inject constructor(private val playlistDao: PlaylistDao,private val databaseFormatterFactory: DatabaseFormatterFactory):DatabaseDataSource {
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
            Log.d("yono","yonopoda")
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

    override suspend fun addActivePlayList(playlists: List<TubeFyCoreTypeData?>): Resource<DatabaseResponse> {
        return withContext(Dispatchers.IO)
        {
            Log.d("yono","yonopoda")
            var formattedActivePlayList=databaseFormatterFactory.formatActivePlayList().run(playlists)
            when(formattedActivePlayList)
            {
                is FormattingResult.SUCCESS->{
                    val result=playlistDao.addAndReplaceActivePlaylist(formattedActivePlayList.data)
                    if(result)
                    {
                        Resource.Success(DatabaseResponse(200))
                    }else
                    {
                        Resource.DataError(DATABASE_INSERTION_ERROR)

                    }
                }
                is FormattingResult.FAILURE->{Resource.DataError(YOUTUBE_V3_SEARCH_ERROR)}

            }


            // Run the formatter



        }
    }

    override suspend fun getAllActivePlaylists(): Resource<List<TubeFyCoreTypeData?>> {
        return withContext(Dispatchers.IO)
        {
            Log.d("yono","yonopoda")
            val activePlayListData=playlistDao.getActivePlaylist()
            if(activePlayListData.isNotEmpty()) {
                var formattedActivePlayList =
                    databaseFormatterFactory.formatTubeFyPlayList().run(activePlayListData)
                when (formattedActivePlayList) {
                    is FormattingResult.SUCCESS -> {

//                    val result=playlistDao.replaceActivePlaylist(formattedActivePlayList.data)
//                    if(result)
//                    {
//                        Resource.Success(DatabaseResponse(200))
//                    }else
//                    {
//                        Resource.DataError(DATABASE_INSERTION_ERROR)
//
//                    }
                        Resource.Success(formattedActivePlayList.data)
                    }

                    is FormattingResult.FAILURE -> {
                        Resource.DataError(YOUTUBE_V3_SEARCH_ERROR)
                    }

                }
            }else{
                Resource.DataError(YOUTUBE_V3_SEARCH_ERROR)
            }

            // Run the formatter



        }
    }

    suspend fun addToFavorites(){
        val isFavorite = playlistDao.isFavorite("abc123")>0
        val favorite = FavoritePlaylist(
            videoId = "abc123",
            videoUrl = "https://example.com/video/abc123",
            videoName = "Sample Video"
        )

// Insert into the database
        if (!isFavorite) {
        playlistDao.insertFavorite(favorite)
        }

    }

   suspend fun getFavorites()
    {
        val favoriteVideos: List<FavoritePlaylist> = playlistDao.getAllFavorites()
    }

    suspend fun removeFromFavorites()
    {
        playlistDao.deleteFavorite("abc123")
    }

    suspend fun addtoplalist()
    {
        val customPlaylistEntry = CustomPlaylist(
            playlistName = "My Playlist",
            videoId = "def456",
            videoUrl = "https://example.com/video/def456",
            videoName = "Another Video"
        )

// Insert into the database
        playlistDao.insertCustomPlaylistEntry(customPlaylistEntry)
    }

    suspend fun getSpecificPlayList()
    {
        val playlistName = "My Playlist"
        val customPlaylist: List<CustomPlaylist> = playlistDao.getCustomPlaylistByName(playlistName)

    }



    suspend fun getAllPlayList()
    {
//        val playlistNames: List<String> = playlistDao.getAllPlaylistNames()
        val playlistNamesWithUrls: List<PlaylistNameWithUrl> = playlistDao.getAllPlaylistNamesWithUrls()
    }

    suspend fun deleteSongFromPlayList()
    {
        playlistDao.deleteCustomPlaylistEntry("My Playlist", "def456")
    }

    suspend fun deleteSpecificPlayList()
    {
        playlistDao.deleteCustomPlaylistByName("My Playlist")
    }
}