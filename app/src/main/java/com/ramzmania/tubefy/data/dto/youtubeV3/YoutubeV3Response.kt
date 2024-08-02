package com.ramzmania.tubefy.data.dto.youtubeV3

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class  YoutubeV3Response (
    @Json(name = "nextPageToken")val nextPageToken: String?,
    @Json(name = "items")val items :  List<YoutubeDataModel>
)