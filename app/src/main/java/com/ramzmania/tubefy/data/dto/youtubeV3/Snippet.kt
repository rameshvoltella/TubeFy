package com.ramzmania.tubefy.data.dto.youtubeV3

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Snippet(
    @Json(name = "publishTime") val publishTime: String,
    @Json(name = "publishedAt")val publishedAt: String,
    @Json(name = "thumbnails")val thumbnails: Thumbnails,
    @Json(name = "title")val title: String
)
@JsonClass(generateAdapter = true)
data class Default(
    @Json(name = "height")val height: Int,
    @Json(name = "url")val url: String,
    @Json(name = "width")val width: Int
)

@JsonClass(generateAdapter = true)
data class High(
    @Json(name = "height")val height: Int,
    @Json(name = "url")val url: String,
    @Json(name = "width")val width: Int
)
@JsonClass(generateAdapter = true)
data class Medium(
    @Json(name = "height")val height: Int,
    @Json(name = "url")val url: String,
    @Json(name = "width")val width: Int
)

@JsonClass(generateAdapter = true)
data class Thumbnails(
    @Json(name = "default") val default: Default,
    @Json(name = "high")val high: High,
    @Json(name = "medium")val medium: Medium
)
