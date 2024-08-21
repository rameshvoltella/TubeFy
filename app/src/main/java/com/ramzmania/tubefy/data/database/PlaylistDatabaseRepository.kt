package com.ramzmania.tubefy.data.database

import com.ramzmania.tubefy.database.PlaylistDao
import com.ramzmania.tubefy.database.QuePlaylist
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlaylistDatabaseRepository @Inject constructor(
    private val playlistDao: PlaylistDao
) {

    suspend fun addPlaylists(playlists: List<QuePlaylist>) {
        playlistDao.addQuePlaylists(playlists)
    }

    suspend fun getPlaylists(): List<QuePlaylist> {
        return playlistDao.getAllQuePlaylist()
    }
}