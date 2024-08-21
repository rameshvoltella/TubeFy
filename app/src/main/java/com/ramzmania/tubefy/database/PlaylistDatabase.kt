package com.ramzmania.tubefy.database

import androidx.room.Database
import androidx.room.RoomDatabase


@Database(entities = [ActivePlaylist::class, QuePlaylist::class], version = 1)
abstract class PlaylistDatabase : RoomDatabase() {
    abstract fun playlistDao(): PlaylistDao
}