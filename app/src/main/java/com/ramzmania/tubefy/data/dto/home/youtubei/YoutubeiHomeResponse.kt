package com.ramzmania.tubefy.data.dto.home.youtubei
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class YoutubeiMusicHomeApiResponse(
    @Json(name = "responseContext") val responseContext: ResponseContext? = null,
    @Json(name = "contents") val contents: Contents? = null
)

@JsonClass(generateAdapter = true)
data class ResponseContext(
    @Json(name = "visitorData") val visitorData: String? = null
)

@JsonClass(generateAdapter = true)
data class Contents(
    @Json(name = "singleColumnBrowseResultsRenderer") val singleColumnBrowseResultsRenderer: SingleColumnBrowseResultsRenderer? = null
)

@JsonClass(generateAdapter = true)
data class SingleColumnBrowseResultsRenderer(
    @Json(name = "tabs") val tabs: List<TabRendererWrapper>? = null
)

@JsonClass(generateAdapter = true)
data class TabRendererWrapper(
    @Json(name = "tabRenderer") val tabRenderer: TabRenderer? = null
)

@JsonClass(generateAdapter = true)
data class TabRenderer(
    @Json(name = "content") val content: Content? = null
)

@JsonClass(generateAdapter = true)
data class Content(
    @Json(name = "sectionListRenderer") val sectionListRenderer: SectionListRenderer? = null
)

@JsonClass(generateAdapter = true)
data class SectionListRenderer(
    @Json(name = "contents") val contents: List<MusicCarouselShelfRendererWrapper>? = null,
    @Json(name = "continuations") val continuations: List<NextContinuationDataWrapper>? = null
)

@JsonClass(generateAdapter = true)
data class MusicCarouselShelfRendererWrapper(
    @Json(name = "musicCarouselShelfRenderer") val musicCarouselShelfRenderer: MusicCarouselShelfRenderer? = null
)

@JsonClass(generateAdapter = true)
data class MusicCarouselShelfRenderer(
    @Json(name = "header") val header: MusicCarouselShelfBasicHeaderRendererWrapper? = null,
    @Json(name = "contents") val contents: List<MusicResponsiveListItemRendererWrapper>? = null
)

@JsonClass(generateAdapter = true)
data class MusicCarouselShelfBasicHeaderRendererWrapper(
    @Json(name = "musicCarouselShelfBasicHeaderRenderer") val musicCarouselShelfBasicHeaderRenderer: MusicCarouselShelfBasicHeaderRenderer? = null
)

@JsonClass(generateAdapter = true)
data class MusicCarouselShelfBasicHeaderRenderer(
    @Json(name = "accessibilityData") val accessibilityData: AccessibilityDataWrapper? = null
)

@JsonClass(generateAdapter = true)
data class AccessibilityDataWrapper(
    @Json(name = "accessibilityData") val accessibilityData: AccessibilityData? = null
)

@JsonClass(generateAdapter = true)
data class AccessibilityData(
    @Json(name = "label") val label: String? = null
)

@JsonClass(generateAdapter = true)
data class MusicResponsiveListItemRendererWrapper(
    @Json(name = "musicResponsiveListItemRenderer") val musicResponsiveListItemRenderer: MusicResponsiveListItemRenderer? = null,
    @Json(name = "musicTwoRowItemRenderer") val musicTwoRowItemRenderer: MusicTwoRowItemRenderer? = null
)

@JsonClass(generateAdapter = true)
data class MusicResponsiveListItemRenderer(
    @Json(name = "thumbnail") val thumbnail: MusicThumbnailRendererWrapper? = null,
    @Json(name = "flexColumns") val flexColumns: List<MusicResponsiveListItemFlexColumnRendererWrapper>? = null
)

@JsonClass(generateAdapter = true)
data class MusicTwoRowItemRenderer(
    @Json(name = "thumbnailRenderer") val thumbnailRenderer: MusicThumbnailRendererWrapper? = null,
    @Json(name = "title") val title: TitleWrapper? = null,
    @Json(name = "thumbnailOverlay") val thumbnailOverlay: MusicItemThumbnailOverlayRenderer? = null
)



@JsonClass(generateAdapter = true)
data class MusicResponsiveListItemFlexColumnRendererWrapper(
    @Json(name = "musicResponsiveListItemFlexColumnRenderer") val musicResponsiveListItemFlexColumnRenderer: MusicResponsiveListItemFlexColumnRenderer? = null
)

@JsonClass(generateAdapter = true)
data class MusicResponsiveListItemFlexColumnRenderer(
    @Json(name = "text") val text: TextWrapper? = null,
    @Json(name = "displayPriority") val displayPriority: String? = null
)

@JsonClass(generateAdapter = true)
data class TextWrapper(
    @Json(name = "runs") val runs: List<Run>? = null
)

@JsonClass(generateAdapter = true)
data class Run(
    @Json(name = "text") val text: String? = null,
    @Json(name = "navigationEndpoint") val navigationEndpoint: NavigationEndpoint? = null
)

@JsonClass(generateAdapter = true)
data class NavigationEndpoint(
    @Json(name = "clickTrackingParams") val clickTrackingParams: String? = null,
    @Json(name = "watchEndpoint") val watchEndpoint: WatchEndpoint? = null,
    @Json(name = "browseEndpoint") val browseEndpoint: BrowseEndpoint? = null
)

@JsonClass(generateAdapter = true)
data class WatchEndpoint(
    @Json(name = "videoId") val videoId: String? = null,
    @Json(name = "playlistId") val playlistId: String? = null,
    @Json(name = "params") val params: String? = null,
    @Json(name = "loggingContext") val loggingContext: LoggingContext? = null,
    @Json(name = "watchEndpointMusicSupportedConfigs") val watchEndpointMusicSupportedConfigs: WatchEndpointMusicSupportedConfigs? = null
)

@JsonClass(generateAdapter = true)
data class WatchEndpointMusicSupportedConfigs(
    @Json(name = "watchEndpointMusicConfig") val watchEndpointMusicConfig: WatchEndpointMusicConfig? = null
)

@JsonClass(generateAdapter = true)
data class WatchEndpointMusicConfig(
    @Json(name = "musicVideoType") val musicVideoType: String? = null
)

@JsonClass(generateAdapter = true)
data class LoggingContext(
    @Json(name = "vssLoggingContext") val vssLoggingContext: VssLoggingContext? = null
)

@JsonClass(generateAdapter = true)
data class VssLoggingContext(
    @Json(name = "serializedContextData") val serializedContextData: String? = null
)

@JsonClass(generateAdapter = true)
data class BrowseEndpoint(
    @Json(name = "browseId") val browseId: String? = null,
    @Json(name = "params") val params: String? = null,
    @Json(name = "browseEndpointContextSupportedConfigs") val browseEndpointContextSupportedConfigs: BrowseEndpointContextSupportedConfigs? = null
)

@JsonClass(generateAdapter = true)
data class BrowseEndpointContextSupportedConfigs(
    @Json(name = "browseEndpointContextMusicConfig") val browseEndpointContextMusicConfig: BrowseEndpointContextMusicConfig? = null
)

@JsonClass(generateAdapter = true)
data class BrowseEndpointContextMusicConfig(
    @Json(name = "pageType") val pageType: String? = null
)

@JsonClass(generateAdapter = true)
data class MusicThumbnailRendererWrapper(
    @Json(name = "musicThumbnailRenderer") val musicThumbnailRenderer: MusicThumbnailRenderer? = null
)

@JsonClass(generateAdapter = true)
data class MusicThumbnailRenderer(
    @Json(name = "thumbnail") val thumbnail: Thumbnail? = null,
    @Json(name = "thumbnailCrop") val thumbnailCrop: String? = null,
    @Json(name = "thumbnailScale") val thumbnailScale: String? = null,
    @Json(name = "trackingParams") val trackingParams: String? = null
)

@JsonClass(generateAdapter = true)
data class Thumbnail(
    @Json(name = "thumbnails") val thumbnails: List<ThumbnailDetail>? = null
)

@JsonClass(generateAdapter = true)
data class ThumbnailDetail(
    @Json(name = "url") val url: String? = null,
    @Json(name = "width") val width: Int? = null,
    @Json(name = "height") val height: Int? = null
)

@JsonClass(generateAdapter = true)
data class TitleWrapper(
    @Json(name = "runs") val runs: List<Run>? = null
)

@JsonClass(generateAdapter = true)
data class MusicItemThumbnailOverlayRenderer(
    @Json(name = "musicItemThumbnailOverlayRenderer") val musicItemThumbnailOverlayRenderer: MusicItemThumbnailOverlayRendererContent? = null
)

@JsonClass(generateAdapter = true)
data class MusicItemThumbnailOverlayRendererContent(
    @Json(name = "background") val background: VerticalGradientWrapper? = null,
    @Json(name = "content") val content: MusicPlayButtonRendererWrapper? = null,
    @Json(name = "contentPosition") val contentPosition: String? = null,
    @Json(name = "displayStyle") val displayStyle: String? = null
)

@JsonClass(generateAdapter = true)
data class VerticalGradientWrapper(
    @Json(name = "verticalGradient") val verticalGradient: VerticalGradient? = null
)

@JsonClass(generateAdapter = true)
data class VerticalGradient(
    @Json(name = "gradientLayerColors") val gradientLayerColors: List<String>? = null
)

@JsonClass(generateAdapter = true)
data class MusicPlayButtonRendererWrapper(
    @Json(name = "musicPlayButtonRenderer") val musicPlayButtonRenderer: MusicPlayButtonRenderer? = null
)

@JsonClass(generateAdapter = true)
data class MusicPlayButtonRenderer(
    @Json(name = "playNavigationEndpoint") val playNavigationEndpoint: PlayNavigationEndpoint? = null
)

@JsonClass(generateAdapter = true)
data class PlayNavigationEndpoint(
    @Json(name = "clickTrackingParams") val clickTrackingParams: String? = null,
    @Json(name = "watchPlaylistEndpoint") val watchPlaylistEndpoint: WatchPlaylistEndpoint? = null
)

@JsonClass(generateAdapter = true)
data class WatchPlaylistEndpoint(
    @Json(name = "playlistId") val playlistId: String? = null,
    @Json(name = "params") val params: String? = null,
    @Json(name = "loggingContext") val loggingContext: LoggingContext? = null
)

@JsonClass(generateAdapter = true)
data class NextContinuationDataWrapper(
    @Json(name = "nextContinuationData") val nextContinuationData: NextContinuationData? = null
)

@JsonClass(generateAdapter = true)
data class NextContinuationData(
    @Json(name = "continuation") val continuation: String? = null,
    @Json(name = "clickTrackingParams") val clickTrackingParams: String? = null
)
