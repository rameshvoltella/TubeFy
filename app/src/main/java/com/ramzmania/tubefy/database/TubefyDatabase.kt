package com.ramzmania.tubefy.database

import androidx.room.Database
import androidx.room.RoomDatabase


@Database(entities = [ActivePlaylist::class, QuePlaylist::class,
    FavoritePlaylist::class,
    CustomPlaylist::class], version = 1)
abstract class TubefyDatabase : RoomDatabase() {
    abstract fun playlistDao(): PlaylistDao
}