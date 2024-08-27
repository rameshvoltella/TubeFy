package com.ramzmania.tubefy.data.database

import android.util.Log
import com.ramzmania.tubefy.core.YoutubeCoreConstant
import com.ramzmania.tubefy.core.dataformatter.FormattingResult
import com.ramzmania.tubefy.core.dataformatter.database.DatabaseFormatterFactory
import com.ramzmania.tubefy.data.Resource
import com.ramzmania.tubefy.data.dto.base.searchformat.TubeFyCoreTypeData
import com.ramzmania.tubefy.data.dto.database.PlaylistNameWithUrl
import com.ramzmania.tubefy.database.ActivePlaylist
import com.ramzmania.tubefy.database.CustomPlaylist
import com.ramzmania.tubefy.database.DatabaseResponse
import com.ramzmania.tubefy.database.FavoritePlaylist
import com.ramzmania.tubefy.database.PlaylistDao
import com.ramzmania.tubefy.database.QuePlaylist
import com.ramzmania.tubefy.errors.DATABASE_INSERTION_ERROR
import com.ramzmania.tubefy.errors.DATABASE_PLAYLIST_ERROR
import com.ramzmania.tubefy.utils.swap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class DatabaseData @Inject constructor(
    private val playlistDao: PlaylistDao,
    private val databaseFormatterFactory: DatabaseFormatterFactory
) : DatabaseDataSource {
    override suspend fun addPlayList(playlists: List<QuePlaylist>): Resource<DatabaseResponse> {
        return withContext(Dispatchers.IO)
        {
            val result = playlistDao.addQuePlaylists(playlists)
            if (result) {
                Resource.Success(DatabaseResponse(200))
            } else {
                Resource.DataError(DATABASE_INSERTION_ERROR)

            }

        }

    }

    override suspend fun getPlaylists(): Resource<List<QuePlaylist>> {
        return withContext(Dispatchers.IO)
        {
            Log.d("handleMediaItemChange,", "<><><><><><6666666")

            val result = playlistDao.getAllQuePlaylist()

            if (result != null && result.size > 0) {
                Log.d("handleMediaItemChange,", "<><><><><><90909")

                Resource.Success(result)
            } else {
                Log.d("handleMediaItemChange,", "<><><><><><88888")

                Resource.DataError(DATABASE_PLAYLIST_ERROR)
            }
        }
    }

    override suspend fun addSongToQueue(songData: QuePlaylist): Resource<DatabaseResponse> {
        return withContext(Dispatchers.IO)
        {
            Log.d("yono", "yonopoda")
            val result = playlistDao.addQueSingleSongPlaylists(songData)
            if (result) {
                Resource.Success(DatabaseResponse(200))
            } else {
                Resource.DataError(DATABASE_INSERTION_ERROR)

            }

        }
    }

    override suspend fun addActivePlayList(playlists: List<TubeFyCoreTypeData?>,clickPosition: Int): Resource<DatabaseResponse> {
        return withContext(Dispatchers.IO)
        {
            Log.d("yono", "yonopodacheker"+clickPosition)
            var formattedActivePlayList =
                databaseFormatterFactory.formatActivePlayList().run(playlists)
            when (formattedActivePlayList) {
                is FormattingResult.SUCCESS -> {
//                    var data=formattedActivePlayList.data.toMutableList()
//
//                    if(clickPosition>0)
//                    {
//                        data=formattedActivePlayList.data.toMutableList().swap(0,clickPosition)
//                    }
                    val activePlaylists: MutableList<ActivePlaylist> = formattedActivePlayList.data.toMutableList()
                    if(clickPosition>0)
                    {
                        activePlaylists.swap(0,clickPosition)
                    }
                    val result =
                        playlistDao.addAndReplaceActivePlaylist(activePlaylists)
                    if (result) {
                        Resource.Success(DatabaseResponse(200))
                    } else {
                        Resource.DataError(DATABASE_INSERTION_ERROR)

                    }
                }

                is FormattingResult.FAILURE -> {
                    Resource.DataError(DATABASE_PLAYLIST_ERROR)
                }

            }


            // Run the formatter


        }
    }

    override suspend fun getAllActivePlaylists(): Resource<List<TubeFyCoreTypeData?>> {
        return withContext(Dispatchers.IO)
        {
            Log.d("yono", "yonopoda")
            val activePlayListData = playlistDao.getActivePlaylist()
            if (activePlayListData.isNotEmpty()) {
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
                        Resource.DataError(DATABASE_PLAYLIST_ERROR)
                    }

                }
            } else {
                Resource.DataError(DATABASE_PLAYLIST_ERROR)
            }

            // Run the formatter


        }
    }

    override suspend fun isFavourite(videoId: String): Resource<Boolean> {
        val isFavorite = playlistDao.isFavorite(YoutubeCoreConstant.extractYoutubeVideoId(videoId)!!) > 0
        Log.d("ISFAVE CAL",videoId+"yoyoy"+isFavorite)
        return Resource.Success(isFavorite)

    }

    override suspend fun addToFavorites(favoriteItem: FavoritePlaylist): Resource<DatabaseResponse> {
//        val favorite = FavoritePlaylist(
//            videoId = "abc123",
//            videoThump = "https://example.com/video/abc123",
//            videoName = "Sample Video"
//        )
        val isFavorite = playlistDao.isFavorite(YoutubeCoreConstant.extractYoutubeVideoId(favoriteItem.videoId)!!) > 0

Log.d("fadak","isfav"+isFavorite)
//
//
// Insert into the database
        return if (!isFavorite) {
            Log.d("fadak","inserting"+favoriteItem.videoId+"<>"+favoriteItem.videoThump+"<>"+favoriteItem.videoName)

            playlistDao.insertFavorite(favoriteItem)
//            isFavourite(YoutubeCoreConstant.extractYoutubeVideoId(favoriteItem.videoId)!!)
//            if (result) {
                Resource.Success(DatabaseResponse(200))
//            } else {
//                Resource.DataError(DATABASE_INSERTION_ERROR)
//
//            }
        }else
        {
            Resource.DataError(DATABASE_INSERTION_ERROR)

        }

    }

    override suspend fun getFavorites(): Resource<List<TubeFyCoreTypeData?>> {
        val favoriteVideosList: List<FavoritePlaylist> = playlistDao.getAllFavorites()
        return if (favoriteVideosList.isNotEmpty()) {
            val formattedFavoritePlayList =
                databaseFormatterFactory.formatTubeFyFavouritePlayList().run(favoriteVideosList)
            when (formattedFavoritePlayList) {
                is FormattingResult.SUCCESS -> {

                    Resource.Success(formattedFavoritePlayList.data)
                }

                is FormattingResult.FAILURE -> {
                    Resource.DataError(DATABASE_PLAYLIST_ERROR)
                }

            }
        } else {
            Resource.DataError(DATABASE_PLAYLIST_ERROR)
        }
    }

    override suspend fun removeFromFavorites(videoId:String): Resource<DatabaseResponse> {
        val result=playlistDao.deleteFavorite(YoutubeCoreConstant.extractYoutubeVideoId(videoId)!!)>0
       return if (result) {
//           isFavourite(videoId)
           Resource.Success(DatabaseResponse(200))
        } else {
            Resource.DataError(DATABASE_INSERTION_ERROR)
        }
    }


    override suspend fun addToPlaylist(customPlayListData:CustomPlaylist): Resource<DatabaseResponse> {
//        val customPlaylistEntry = CustomPlaylist(
//            playlistName = "My Playlist",
//            videoId = "def456",
//            videoThump = "https://example.com/video/def456",
//            videoName = "Another Video"
//        )

// Insert into the database
       playlistDao.insertCustomPlaylistEntryIfNotExists(customPlayListData)
           return Resource.Success(DatabaseResponse(200))

    }

    override suspend fun getSpecificPlayList(playlistName:String): Resource<List<TubeFyCoreTypeData?>> {
//        val playlistName = "My Playlist"
        val customPlaylist: List<CustomPlaylist> = playlistDao.getCustomPlaylistByName(playlistName)
        return if (customPlaylist.isNotEmpty()) {
            val formattedSpecificPlayList =
                databaseFormatterFactory.formatTubeFyCustomPlayList().run(customPlaylist)
            when (formattedSpecificPlayList) {
                is FormattingResult.SUCCESS -> {

                    Resource.Success(formattedSpecificPlayList.data)
                }

                is FormattingResult.FAILURE -> {
                    Resource.DataError(DATABASE_PLAYLIST_ERROR)
                }

            }
        } else {
            Resource.DataError(DATABASE_PLAYLIST_ERROR)
        }

    }


    override suspend fun getAllSavedPlayList(): Resource<List<PlaylistNameWithUrl>> {
        val allSavedPlayList: MutableList<PlaylistNameWithUrl> =
            playlistDao.getAllPlaylistNamesWithUrls().toMutableList()

        if(getFavorites().data!=null&&getFavorites().data?.size!!>0)
        {
            allSavedPlayList.add(0,PlaylistNameWithUrl("TubeFy-Favorites","Favorites"))
        }

        return if(allSavedPlayList.isNotEmpty())
        {
            Resource.Success(allSavedPlayList)
        }else
        {
            Resource.DataError(DATABASE_PLAYLIST_ERROR)

        }


    }

    override suspend fun deleteSongFromPlayList(playlistName:String,songName:String): Resource<DatabaseResponse> {
        val result=playlistDao.deleteCustomPlaylistEntry(playlistName, songName)>0
        return if (result) {
            Resource.Success(DatabaseResponse(200))
        } else {
            Resource.DataError(DATABASE_INSERTION_ERROR)
        }
    }

    override suspend fun deleteSpecificPlayList(playlistName:String): Resource<DatabaseResponse> {
        val result=playlistDao.deleteCustomPlaylistByName(playlistName)>0
        return if (result) {
            Resource.Success(DatabaseResponse(200))
        } else {
            Resource.DataError(DATABASE_INSERTION_ERROR)
        }
    }


}