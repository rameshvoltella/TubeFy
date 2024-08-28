package com.ramzmania.tubefy.data.dto.database

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class PlaylistNameWithUrl(
    val playlistName: String,
    val videoThump: String
):Parcelable