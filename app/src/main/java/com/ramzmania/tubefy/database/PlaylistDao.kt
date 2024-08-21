package com.ramzmania.tubefy.database
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction

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
    suspend fun addQuePlaylists(playlists: List<QuePlaylist>) {
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
    }
}
