package com.ramzmania.tubefy.database

import android.util.Log
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.ramzmania.tubefy.data.dto.database.PlaylistNameWithUrl

@Dao
interface PlaylistDao {

    @Query("SELECT * FROM QuePlaylist")
    suspend fun getAllQuePlaylist(): List<QuePlaylist>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertQuePlaylist(playlist: QuePlaylist)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertQuePlaylists(playlists: List<QuePlaylist>)

    @Query("SELECT COUNT(*) FROM QuePlaylist")
    suspend fun getQuePlaylistCount(): Int

    @Query("SELECT COUNT(*) FROM QuePlaylist WHERE videoId = :videoId")
    suspend fun isVideoIdPresent(videoId: String): Int

    @Query("DELETE FROM QuePlaylist WHERE id IN (SELECT id FROM QuePlaylist ORDER BY id ASC LIMIT :limit)")
    suspend fun deleteOldestQuePlaylistEntries(limit: Int)

    @Transaction
    suspend fun addQuePlaylists(playlists: List<QuePlaylist>): Boolean {
        val validPlaylists = playlists.filter { playlist ->
            isVideoIdPresent(playlist.videoId) == 0
        }

        val currentCount = getQuePlaylistCount()
        val newEntriesCount = validPlaylists.size
        val totalRequired = currentCount + newEntriesCount

        if (totalRequired > 1000) {
            val entriesToDelete = totalRequired - 1000
            deleteOldestQuePlaylistEntries(entriesToDelete)
        }

        insertQuePlaylists(validPlaylists)
        return true
    }

    @Transaction
    suspend fun addQueSingleSongPlaylists(data: QuePlaylist): Boolean {
//        val validPlaylists = playlists.filter { playlist ->
        Log.d("Tagger","yono22222")

        val validPlaylists = isVideoIdPresent(data.videoId) == 0
        Log.d("kadapa","came"+validPlaylists)
//        }
        if (validPlaylists) {
            val currentCount = getQuePlaylistCount()
            val newEntriesCount = 1
            val totalRequired = currentCount + newEntriesCount

            if (totalRequired > 1000) {
                val entriesToDelete = totalRequired - 1000
                deleteOldestQuePlaylistEntries(entriesToDelete)
            }

            insertQuePlaylist(data)
        }
        return true
    }

    // Insert new list, replacing existing data
    @Transaction
    suspend fun addAndReplaceActivePlaylist(newPlaylist: List<ActivePlaylist>): Boolean {
        clearActivePlaylist()
        insertActivePlaylist(newPlaylist)
        return true

    }

    // Clear all entries from the ActivePlaylist table
    @Query("DELETE FROM ActivePlaylist")
    suspend fun clearActivePlaylist()

    // Insert a list of ActivePlaylist entries
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActivePlaylist(playlist: List<ActivePlaylist>)

    // Get the entire active playlist
    @Query("SELECT * FROM ActivePlaylist")
    fun getActivePlaylist(): List<ActivePlaylist>


    // FavoritePlaylist methods
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(playlist: FavoritePlaylist): Int

    @Query("DELETE FROM FavoritePlaylist WHERE videoId = :videoId")
    suspend fun deleteFavorite(videoId: String):Int

    @Query("SELECT * FROM FavoritePlaylist")
    suspend fun getAllFavorites(): List<FavoritePlaylist>

    @Query("SELECT COUNT(*) FROM FavoritePlaylist WHERE videoId = :videoId")
    suspend fun isFavorite(videoId: String): Int

    // CustomPlaylist methods
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomPlaylistEntry(entry: CustomPlaylist):Int

    @Query("SELECT * FROM CustomPlaylist WHERE playlistName = :playlistName")
    suspend fun getCustomPlaylistByName(playlistName: String): List<CustomPlaylist>

    @Query("DELETE FROM CustomPlaylist WHERE playlistName = :playlistName AND videoId = :videoId")
    suspend fun deleteCustomPlaylistEntry(playlistName: String, videoId: String):Int

    @Query("SELECT DISTINCT playlistName FROM CustomPlaylist")
    suspend fun getAllPlaylistNames(): List<String>

    // Delete all entries with the same playlist name
    @Query("DELETE FROM CustomPlaylist WHERE playlistName = :playlistName")
    suspend fun deleteCustomPlaylistByName(playlistName: String):Int

    @Query("SELECT DISTINCT playlistName, videoThump FROM CustomPlaylist")
    suspend fun getAllPlaylistNamesWithUrls(): List<PlaylistNameWithUrl>
}
