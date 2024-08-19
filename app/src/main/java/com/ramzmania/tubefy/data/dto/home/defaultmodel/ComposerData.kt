package com.ramzmania.tubefy.data.dto.home.defaultmodel

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Composer(
    @Json(name = "name") val name: String?,
    @Json(name = "notable_works") val notableWorks: List<String>?,
    @Json(name = "awards") val awards: List<String>?,
    @Json(name = "image") val image: String?
)

@JsonClass(generateAdapter = true)
data class LocalHomeData(
    @Json(name = "data") val composers: List<Composer>?,
    @Json(name = "heading")val heading:String
)
