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
