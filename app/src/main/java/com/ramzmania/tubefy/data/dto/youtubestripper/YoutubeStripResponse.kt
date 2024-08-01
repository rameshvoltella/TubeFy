package com.ramzmania.tubefy.data.dto.youtubestripper
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
    @Json(name = "contents") val contentsBaseRenderer: List<BaseContent?>
)

@JsonClass(generateAdapter = true)
data class BaseContent(
    @Json(name = "compactChannelRenderer") val compactChannelRenderer: CompactChannelRenderer?,
    @Json(name = "reelShelfRenderer") val reelShelfRenderer: ReelShelfRenderer?,
    @Json(name = "videoWithContextRenderer") val videoWithContextRenderer: VideoWithContextRenderer?
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

@JsonClass(generateAdapter = true)
data class CompactChannelRenderer(
    @Json(name = "channelId") val channelId: String
)

@JsonClass(generateAdapter = true)
data class ReelShelfRenderer(
    @Json(name = "trackingParams") val trackingParams: String
)

@JsonClass(generateAdapter = true)
data class VideoWithContextRenderer(
    @Json(name = "videoId") val videoId: String
)
