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

/*Shorts data model*/
@JsonClass(generateAdapter = true)
data class ReelShelfRenderer(
    @Json(name = "trackingParams") val trackingParams: String,
    @Json(name = "items") val shortsList: List<ReelShortsLockupBaseViewModel>
)

@JsonClass(generateAdapter = true)
data class ReelShortsLockupBaseViewModel(
    @Json(name = "shortsLockupViewModel") val trackingParams: ReelShortsLockupViewModel
)

@JsonClass(generateAdapter = true)
data class ReelShortsLockupViewModel(
    @Json(name = "accessibilityText") val accessibilityText: String,
    @Json(name = "thumbnail") val reelsThumpNail: ReelShortsThumpNail,
    @Json(name = "onTap") val reelsOnTap: ReelShortsOnTap


)

@JsonClass(generateAdapter = true)
data class ReelShortsOnTap(
    @Json(name = "innertubeCommand") val innerTubeCommand: ReelShortsInnerTubeCommand

)

@JsonClass(generateAdapter = true)
data class ReelShortsInnerTubeCommand(
    @Json(name = "reelWatchEndpoint") val reelWatchEndpoint: ReelShortsReelWatchEndpoint

)

@JsonClass(generateAdapter = true)
data class ReelShortsReelWatchEndpoint(
    @Json(name = "videoId") val reelWatchEndpoint: String

)

@JsonClass(generateAdapter = true)
data class ReelShortsThumpNail(
    @Json(name = "sources") val thumpList: List<ReelShortsThumpSource>

)

@JsonClass(generateAdapter = true)
data class ReelShortsThumpSource(
    @Json(name = "url") val reelShortsThumpNailUrl: String,

    )


/*-------END OF Shorts data model--------*/


/*------------MAinVideo data model------------*/

@JsonClass(generateAdapter = true)
data class VideoWithContextRenderer(
    @Json(name = "videoId") val videoId: String,
    @Json(name = "headline") val headline: VideoHeadlineRunData,
    @Json(name = "thumbnail") val thumbnail: VideoHeadlineMainData

)

@JsonClass(generateAdapter = true)
data class VideoHeadlineRunData(
    @Json(name = "accessibility") val accessibility: VideoHeadlineRunAccessibilityData

)

@JsonClass(generateAdapter = true)
data class VideoHeadlineRunAccessibilityData(
    @Json(name = "accessibilityData") val accessibilityData: VideoHeadlineRunAccessibilityInnerData

)

@JsonClass(generateAdapter = true)
data class VideoHeadlineRunAccessibilityInnerData(
    @Json(name = "label") val label: VideoHeadlineRunAccessibilityInnerData

)

@JsonClass(generateAdapter = true)
data class VideoHeadlineMainData(
    @Json(name = "thumbnails") val thumbnails: List<VideoThumpUrlData>

)

@JsonClass(generateAdapter = true)
data class VideoThumpUrlData(
    @Json(name = "url") val reelShortsThumpNailUrl: String,

    )
/*------------END OF MAinVideo data model------------*/
