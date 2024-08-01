package com.ramzmania.tubefy.data.youtubestripper
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ApiResponse(
    @Json(name = "contents") val contents: Contents
)

@JsonClass(generateAdapter = true)
data class Contents(
    @Json(name = "sectionListRenderer") val sectionListRenderer: SectionListRenderer
)

@JsonClass(generateAdapter = true)
data class SectionListRenderer(
    @Json(name = "contents") val contents: List<ItemSection>
)

@JsonClass(generateAdapter = true)
data class ItemSection(
    @Json(name = "itemSectionRenderer") val itemSectionRenderer: ItemSectionRendererTwo?,
    @Json(name = "continuationItemRenderer") val continuationItemRenderer: ContinuationItemRenderer?
)

@JsonClass(generateAdapter = true)
data class ItemSectionRendererTwo(
    @Json(name = "contents") val contents: List<ItemSectionRendererContent>?
)

@JsonClass(generateAdapter = true)
data class ContinuationItemRenderer(
    @Json(name = "trigger") val trigger: String?
)

@JsonClass(generateAdapter = true)
data class ItemSectionRendererContent(
    @Json(name = "itemSectionRenderer") val itemSectionRenderer: ItemSectionRendererTwo?,
    @Json(name = "continuationItemRenderer") val continuationItemRenderer: ContinuationItemRenderer?
)
