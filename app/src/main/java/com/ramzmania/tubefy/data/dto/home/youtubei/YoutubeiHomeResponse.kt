package com.ramzmania.tubefy.data.dto.home.youtubei

import com.ramzmania.tubefy.data.dto.home.youtubei.next.AccessibilityData
import com.ramzmania.tubefy.data.dto.home.youtubei.next.Content
import com.ramzmania.tubefy.data.dto.home.youtubei.next.Continuation
import com.ramzmania.tubefy.data.dto.home.youtubei.next.MusicCarouselShelfBasicHeaderRenderer
import com.ramzmania.tubefy.data.dto.home.youtubei.next.MusicCarouselShelfRenderer
import com.ramzmania.tubefy.data.dto.home.youtubei.next.MusicThumbnailRenderer
import com.ramzmania.tubefy.data.dto.home.youtubei.next.NavigationEndpoint
import com.ramzmania.tubefy.data.dto.home.youtubei.next.NextContinuationData
import com.ramzmania.tubefy.data.dto.home.youtubei.next.Run
import com.ramzmania.tubefy.data.dto.home.youtubei.next.Thumbnail
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class YoutubeiHomeApiResponse(
    @Json(name = "responseContext") val responseContext: ResponseContext,
    @Json(name = "contents") val contents: Contents
)

@JsonClass(generateAdapter = true)
data class ResponseContext(
    @Json(name = "visitorData") val visitorData: String
)

@JsonClass(generateAdapter = true)
data class Contents(
    @Json(name = "singleColumnBrowseResultsRenderer") val singleColumnBrowseResultsRenderer: SingleColumnBrowseResultsRenderer
)

@JsonClass(generateAdapter = true)
data class SingleColumnBrowseResultsRenderer(
    @Json(name = "tabs") val tabs: List<Tab>
)

@JsonClass(generateAdapter = true)
data class Tab(
    @Json(name = "tabRenderer") val tabRenderer: TabRenderer
)

@JsonClass(generateAdapter = true)
data class TabRenderer(
    @Json(name = "content") val content: Content
)

@JsonClass(generateAdapter = true)
data class Content(
    @Json(name = "sectionListRenderer") val sectionListRenderer: SectionListRenderer
)

@JsonClass(generateAdapter = true)
data class SectionListRenderer(
    @Json(name = "contents") val contents: List<SectionContent>,
    @Json(name = "continuations") val continuations: List<Continuation>?
)

@JsonClass(generateAdapter = true)
data class SectionContent(
    @Json(name = "musicCarouselShelfRenderer") val musicCarouselShelfRenderer: MusicCarouselShelfRenderer
)

@JsonClass(generateAdapter = true)
data class MusicCarouselShelfRenderer(
    @Json(name = "header") val header: MusicCarouselShelfBasicHeaderRenderer,
    @Json(name = "contents") val contents: List<MusicResponsiveListItemRenderer>
)

@JsonClass(generateAdapter = true)
data class MusicCarouselShelfBasicHeaderRenderer(
    @Json(name = "accessibilityData") val accessibilityData: AccessibilityData
)

@JsonClass(generateAdapter = true)
data class AccessibilityData(
    @Json(name = "accessibilityData") val accessibilityData: InnerAccessibilityData
)

@JsonClass(generateAdapter = true)
data class InnerAccessibilityData(
    @Json(name = "label") val label: String
)

@JsonClass(generateAdapter = true)
data class MusicResponsiveListItemRenderer(
    @Json(name = "thumbnail") val thumbnail: MusicThumbnailRenderer,
    @Json(name = "flexColumns") val flexColumns: List<MusicResponsiveListItemFlexColumnRenderer>
)

@JsonClass(generateAdapter = true)
data class MusicThumbnailRenderer(
    @Json(name = "thumbnail") val thumbnail: Thumbnail
)

@JsonClass(generateAdapter = true)
data class Thumbnail(
    @Json(name = "thumbnails") val thumbnails: List<ThumbnailDetails>
)

@JsonClass(generateAdapter = true)
data class ThumbnailDetails(
    @Json(name = "url") val url: String,
    @Json(name = "width") val width: Int,
    @Json(name = "height") val height: Int
)

@JsonClass(generateAdapter = true)
data class MusicResponsiveListItemFlexColumnRenderer(
    @Json(name = "text") val text: Text
)

@JsonClass(generateAdapter = true)
data class Text(
    @Json(name = "runs") val runs: List<Run>
)

@JsonClass(generateAdapter = true)
data class Run(
    @Json(name = "text") val text: String,
    @Json(name = "navigationEndpoint") val navigationEndpoint: NavigationEndpoint
)

@JsonClass(generateAdapter = true)
data class NavigationEndpoint(
    @Json(name = "clickTrackingParams") val clickTrackingParams: String,
    @Json(name = "watchEndpoint") val watchEndpoint: WatchEndpoint
)

@JsonClass(generateAdapter = true)
data class WatchEndpoint(
    @Json(name = "videoId") val videoId: String,
    @Json(name = "playlistId") val playlistId: String,
    @Json(name = "params") val params: String,
    @Json(name = "loggingContext") val loggingContext: LoggingContext,
    @Json(name = "watchEndpointMusicSupportedConfigs") val watchEndpointMusicSupportedConfigs: WatchEndpointMusicSupportedConfigs
)

@JsonClass(generateAdapter = true)
data class LoggingContext(
    @Json(name = "vssLoggingContext") val vssLoggingContext: VssLoggingContext
)

@JsonClass(generateAdapter = true)
data class VssLoggingContext(
    @Json(name = "serializedContextData") val serializedContextData: String
)

@JsonClass(generateAdapter = true)
data class WatchEndpointMusicSupportedConfigs(
    @Json(name = "watchEndpointMusicConfig") val watchEndpointMusicConfig: WatchEndpointMusicConfig
)

@JsonClass(generateAdapter = true)
data class WatchEndpointMusicConfig(
    @Json(name = "musicVideoType") val musicVideoType: String
)

@JsonClass(generateAdapter = true)
data class Continuation(
    @Json(name = "nextContinuationData") val nextContinuationData: NextContinuationData,
    @Json(name = "clickTrackingParams") val clickTrackingParams: String
)

@JsonClass(generateAdapter = true)
data class NextContinuationData(
    @Json(name = "continuation") val continuation: String
)
