package com.ramzmania.tubefy.data.dto.youtubeV3

import com.ramzmania.tubefy.data.dto.youtubeV3.Snippet
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class YoutubeDataModel(
    @Json(name = "snippet") val snippet : Snippet
)
