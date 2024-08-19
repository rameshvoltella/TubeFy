package com.ramzmania.tubefy.data.dto.home.defaultmodel

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class PlaylistCategory(
    @Json(name = "title") val title: String?,
    @Json(name = "image") val image: String?,
    @Json(name = "playlist") val playlist: String?,
    @Json(name = "description") val description: String?
)

@JsonClass(generateAdapter = true)
data class PlaylistCategories(
    @Json(name = "playlist_categories") val playlistCategories: List<PlaylistCategory>?
)
