package com.ramzmania.tubefy.database

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity
data class ActivePlaylist(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val videoId: String,
    val videoName: String,
    val videoThumbnail: String
)

@Entity
data class QuePlaylist(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val videoId: String,
    val videoName: String,
    val videoThumbnail: String
)

@Entity
data class FavoritePlaylist(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val videoId: String,
    val videoUrl: String,
    val videoName: String
)

@Entity
data class CustomPlaylist(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val playlistName: String,
    val videoId: String,
    val videoUrl: String,
    val videoName: String
)