package com.ramzmania.tubefy.data.dto.youtubeV3

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class YoutubeSearchResponse(
    val kind: String,
    val etag: String,
    @Json(name = "nextPageToken") val nextPageToken: String,
    @Json(name = "regionCode") val regionCode: String,
    @Json(name = "pageInfo") val pageInfo: PageInfo,
    @Json(name = "items") val items: List<YoutubeSearchResult>
)

@JsonClass(generateAdapter = true)
data class PageInfo(
    @Json(name = "totalResults") val totalResults: Int,
    @Json(name = "resultsPerPage") val resultsPerPage: Int
)

@JsonClass(generateAdapter = true)
data class YoutubeSearchResult(
    val kind: String,
    val etag: String,
    @Json(name = "id") val id: VideoId,
    @Json(name = "snippet") val snippet: Snippet
)

@JsonClass(generateAdapter = true)
data class VideoId(
    val kind: String,
    @Json(name = "videoId") val videoId: String
)

@JsonClass(generateAdapter = true)
data class Snippet(
    @Json(name = "publishedAt") val publishedAt: String,
    @Json(name = "channelId") val channelId: String,
    @Json(name = "title") val title: String,
    @Json(name = "description") val description: String,
    @Json(name = "thumbnails") val thumbnails: Thumbnails,
    @Json(name = "channelTitle") val channelTitle: String,
    @Json(name = "liveBroadcastContent") val liveBroadcastContent: String,
    @Json(name = "publishTime") val publishTime: String
)

@JsonClass(generateAdapter = true)
data class Thumbnails(
    @Json(name = "default") val defaultThumbnail: Thumbnail,
    @Json(name = "medium") val medium: Thumbnail,
    @Json(name = "high") val high: Thumbnail
)

@JsonClass(generateAdapter = true)
data class Thumbnail(
    val url: String,
    val width: Int,
    val height: Int
)
