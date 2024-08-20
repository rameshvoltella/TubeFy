package com.ramzmania.tubefy.data.dto.home.youtubei

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ApiResponse(
    @Json(name = "responseContext") val responseContext: ResponseContext?,
    @Json(name = "contents") val contents: Contents?
)

@JsonClass(generateAdapter = true)
data class ResponseContext(
    @Json(name = "visitorData") val visitorData: String?
)

@JsonClass(generateAdapter = true)
data class Contents(
    @Json(name = "singleColumnBrowseResultsRenderer") val singleColumnBrowseResultsRenderer: SingleColumnBrowseResultsRenderer?
)

@JsonClass(generateAdapter = true)
data class SingleColumnBrowseResultsRenderer(
    @Json(name = "tabs") val tabs: List<TabRendererWrapper>?
)

@JsonClass(generateAdapter = true)
data class TabRendererWrapper(
    @Json(name = "tabRenderer") val tabRenderer: TabRenderer?
)

@JsonClass(generateAdapter = true)
data class TabRenderer(
    @Json(name = "content") val content: SectionListRendererWrapper?
)

@JsonClass(generateAdapter = true)
data class SectionListRendererWrapper(
    @Json(name = "sectionListRenderer") val sectionListRenderer: SectionListRenderer?
)

@JsonClass(generateAdapter = true)
data class SectionListRenderer(
    @Json(name = "contents") val contents: List<MusicCarouselShelfRendererWrapper>?,
    @Json(name = "continuations") val continuations: List<ContinuationWrapper>?
)

@JsonClass(generateAdapter = true)
data class MusicCarouselShelfRendererWrapper(
    @Json(name = "musicCarouselShelfRenderer") val musicCarouselShelfRenderer: MusicCarouselShelfRenderer?
)

@JsonClass(generateAdapter = true)
data class MusicCarouselShelfRenderer(
    @Json(name = "header") val header: MusicCarouselShelfBasicHeaderRendererWrapper?,
    @Json(name = "contents") val contents: List<MusicResponsiveListItemRendererWrapper>?
)

@JsonClass(generateAdapter = true)
data class MusicCarouselShelfBasicHeaderRendererWrapper(
    @Json(name = "musicCarouselShelfBasicHeaderRenderer") val musicCarouselShelfBasicHeaderRenderer: MusicCarouselShelfBasicHeaderRenderer?
)

@JsonClass(generateAdapter = true)
data class MusicCarouselShelfBasicHeaderRenderer(
    @Json(name = "accessibilityData") val accessibilityData: AccessibilityDataWrapper?
)

@JsonClass(generateAdapter = true)
data class AccessibilityDataWrapper(
    @Json(name = "accessibilityData") val accessibilityData: AccessibilityData?
)

@JsonClass(generateAdapter = true)
data class AccessibilityData(
    @Json(name = "label") val label: String?
)

@JsonClass(generateAdapter = true)
data class MusicResponsiveListItemRendererWrapper(
    @Json(name = "musicResponsiveListItemRenderer") val musicResponsiveListItemRenderer: MusicResponsiveListItemRenderer?
)

@JsonClass(generateAdapter = true)
data class MusicResponsiveListItemRenderer(
    @Json(name = "thumbnail") val thumbnail: MusicThumbnailRendererWrapper?,
    @Json(name = "flexColumns") val flexColumns: List<MusicResponsiveListItemFlexColumnRendererWrapper>?
)

@JsonClass(generateAdapter = true)
data class MusicThumbnailRendererWrapper(
    @Json(name = "musicThumbnailRenderer") val musicThumbnailRenderer: MusicThumbnailRenderer?
)

@JsonClass(generateAdapter = true)
data class MusicThumbnailRenderer(
    @Json(name = "thumbnail") val thumbnail: Thumbnail?,
    @Json(name = "thumbnailCrop") val thumbnailCrop: String?,
    @Json(name = "thumbnailScale") val thumbnailScale: String?,
    @Json(name = "trackingParams") val trackingParams: String?
)

@JsonClass(generateAdapter = true)
data class Thumbnail(
    @Json(name = "thumbnails") val thumbnails: List<ThumbnailItem>?
)

@JsonClass(generateAdapter = true)
data class ThumbnailItem(
    @Json(name = "url") val url: String?,
    @Json(name = "width") val width: Int?,
    @Json(name = "height") val height: Int?
)

@JsonClass(generateAdapter = true)
data class MusicResponsiveListItemFlexColumnRendererWrapper(
    @Json(name = "musicResponsiveListItemFlexColumnRenderer") val musicResponsiveListItemFlexColumnRenderer: MusicResponsiveListItemFlexColumnRenderer?
)

@JsonClass(generateAdapter = true)
data class MusicResponsiveListItemFlexColumnRenderer(
    @Json(name = "text") val text: Text?,
    @Json(name = "displayPriority") val displayPriority: String?
)

@JsonClass(generateAdapter = true)
data class Text(
    @Json(name = "runs") val runs: List<Run>?
)

@JsonClass(generateAdapter = true)
data class Run(
    @Json(name = "text") val text: String?,
    @Json(name = "navigationEndpoint") val navigationEndpoint: NavigationEndpoint?
)

@JsonClass(generateAdapter = true)
data class NavigationEndpoint(
    @Json(name = "clickTrackingParams") val clickTrackingParams: String?,
    @Json(name = "watchEndpoint") val watchEndpoint: WatchEndpoint?,
    @Json(name = "browseEndpoint") val browseEndpoint: BrowseEndpoint?
)

@JsonClass(generateAdapter = true)
data class WatchEndpoint(
    @Json(name = "videoId") val videoId: String?,
    @Json(name = "playlistId") val playlistId: String?,
    @Json(name = "params") val params: String?,
    @Json(name = "loggingContext") val loggingContext: LoggingContext?,
    @Json(name = "watchEndpointMusicSupportedConfigs") val watchEndpointMusicSupportedConfigs: WatchEndpointMusicSupportedConfigs?
)

@JsonClass(generateAdapter = true)
data class LoggingContext(
    @Json(name = "vssLoggingContext") val vssLoggingContext: VssLoggingContext?
)

@JsonClass(generateAdapter = true)
data class VssLoggingContext(
    @Json(name = "serializedContextData") val serializedContextData: String?
)

@JsonClass(generateAdapter = true)
data class WatchEndpointMusicSupportedConfigs(
    @Json(name = "watchEndpointMusicConfig") val watchEndpointMusicConfig: WatchEndpointMusicConfig?
)

@JsonClass(generateAdapter = true)
data class WatchEndpointMusicConfig(
    @Json(name = "musicVideoType") val musicVideoType: String?
)

@JsonClass(generateAdapter = true)
data class BrowseEndpoint(
    @Json(name = "browseId") val browseId: String?,
    @Json(name = "params") val params: String?,
    @Json(name = "browseEndpointContextSupportedConfigs") val browseEndpointContextSupportedConfigs: BrowseEndpointContextSupportedConfigs?
)

@JsonClass(generateAdapter = true)
data class BrowseEndpointContextSupportedConfigs(
    @Json(name = "browseEndpointContextMusicConfig") val browseEndpointContextMusicConfig: BrowseEndpointContextMusicConfig?
)

@JsonClass(generateAdapter = true)
data class BrowseEndpointContextMusicConfig(
    @Json(name = "pageType") val pageType: String?
)

@JsonClass(generateAdapter = true)
data class ContinuationWrapper(
    @Json(name = "nextContinuationData") val nextContinuationData: NextContinuationData?
)

@JsonClass(generateAdapter = true)
data class NextContinuationData(
    @Json(name = "continuation") val continuation: String?,
    @Json(name = "clickTrackingParams") val clickTrackingParams: String?
)
