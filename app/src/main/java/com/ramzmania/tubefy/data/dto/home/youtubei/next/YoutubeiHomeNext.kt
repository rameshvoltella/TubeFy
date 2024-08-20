package com.ramzmania.tubefy.data.dto.home.youtubei.next
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ContinuationContents(
    @Json(name = "continuationContents") val continuationContents: ContinuationContentsData?
)

@JsonClass(generateAdapter = true)
data class ContinuationContentsData(
    @Json(name = "sectionListContinuation") val sectionListContinuation: SectionListContinuation?
)

@JsonClass(generateAdapter = true)
data class SectionListContinuation(
    @Json(name = "contents") val contents: List<ContentItem?>?,
    @Json(name = "continuations") val continuations: List<ContinuationData?>?
)

@JsonClass(generateAdapter = true)
data class ContentItem(
    @Json(name = "musicCarouselShelfRenderer") val musicCarouselShelfRenderer: MusicCarouselShelfRenderer?
)

@JsonClass(generateAdapter = true)
data class MusicCarouselShelfRenderer(
    @Json(name = "header") val header: Header?,
    @Json(name = "contents") val contents: List<MusicContentItem?>?
)

@JsonClass(generateAdapter = true)
data class Header(
    @Json(name = "musicCarouselShelfBasicHeaderRenderer") val musicCarouselShelfBasicHeaderRenderer: MusicCarouselShelfBasicHeaderRenderer?
)

@JsonClass(generateAdapter = true)
data class MusicCarouselShelfBasicHeaderRenderer(
    @Json(name = "accessibilityData") val accessibilityData: AccessibilityData?
)

@JsonClass(generateAdapter = true)
data class AccessibilityData(
    @Json(name = "accessibilityData") val accessibilityLabel: LabelData?
)

@JsonClass(generateAdapter = true)
data class LabelData(
    @Json(name = "label") val label: String?
)

@JsonClass(generateAdapter = true)
data class MusicContentItem(
    @Json(name = "musicTwoRowItemRenderer") val musicTwoRowItemRenderer: MusicTwoRowItemRenderer?,
    @Json(name = "musicResponsiveListItemRenderer") val musicResponsiveListItemRenderer: MusicResponsiveListItemRenderer?
)

@JsonClass(generateAdapter = true)
data class MusicTwoRowItemRenderer(
    @Json(name = "thumbnailRenderer") val thumbnailRenderer: MusicThumbnailRenderer?,
    @Json(name = "title") val title: Title?,
    @Json(name = "thumbnailOverlay") val thumbnailOverlay: MusicItemThumbnailOverlayRenderer?
)
@JsonClass(generateAdapter = true)
data class MusicItemThumbnailOverlayRenderer(
    @Json(name = "musicItemThumbnailOverlayRenderer") val musicItemThumbnailOverlayRenderer: MusicItemThumbnailOverlayRendererContent? = null
)

@JsonClass(generateAdapter = true)
data class MusicThumbnailRenderer(
    @Json(name = "musicThumbnailRenderer") val musicThumbnailRenderer: MusicThumbnailRenderer2?
)

@JsonClass(generateAdapter = true)
data class MusicThumbnailRenderer2(
    @Json(name = "thumbnail") val thumbnail: Thumbnail? = null,
    @Json(name = "thumbnailCrop") val thumbnailCrop: String? = null,
    @Json(name = "thumbnailScale") val thumbnailScale: String? = null,
    @Json(name = "trackingParams") val trackingParams: String? = null
)

@JsonClass(generateAdapter = true)
data class Thumbnail(
    @Json(name = "thumbnail") val thumbnails: List<ThumbnailData?>?
)

@JsonClass(generateAdapter = true)
data class ThumbnailData(
    @Json(name = "url") val url: String?,
    @Json(name = "width") val width: Int?,
    @Json(name = "height") val height: Int?
)

@JsonClass(generateAdapter = true)
data class Title(
    @Json(name = "runs") val runs: List<RunData?>?
)

@JsonClass(generateAdapter = true)
data class RunData(
    @Json(name = "text") val text: String?,
    @Json(name = "navigationEndpoint") val navigationEndpoint: NavigationEndpoint?
)

@JsonClass(generateAdapter = true)
data class NavigationEndpoint(
    @Json(name = "clickTrackingParams") val clickTrackingParams: String?,
    @Json(name = "browseEndpoint") val browseEndpoint: BrowseEndpoint?,
    @Json(name = "watchEndpoint") val watchEndpoint: WatchEndpoint?
)

@JsonClass(generateAdapter = true)
data class BrowseEndpoint(
    @Json(name = "browseId") val browseId: String?,
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
data class MusicItemThumbnailOverlayRendererContent(
    @Json(name = "background") val background: VerticalGradient?,
    @Json(name = "content") val content: MusicPlayButtonRendererWrapper? = null,
    @Json(name = "contentPosition") val contentPosition: String?,
    @Json(name = "displayStyle") val displayStyle: String?
)

@JsonClass(generateAdapter = true)
data class MusicPlayButtonRendererWrapper(
    @Json(name = "musicPlayButtonRenderer") val musicPlayButtonRenderer: MusicPlayButtonRenderer? = null
)
@JsonClass(generateAdapter = true)
data class VerticalGradient(
    @Json(name = "verticalGradient") val verticalGradient: GradientLayerColors?
)

@JsonClass(generateAdapter = true)
data class GradientLayerColors(
    @Json(name = "gradientLayerColors") val gradientLayerColors: List<String?>?
)

@JsonClass(generateAdapter = true)
data class MusicPlayButtonRenderer(
    @Json(name = "playNavigationEndpoint") val playNavigationEndpoint: PlayNavigationEndpoint?,
    @Json(name = "trackingParams") val trackingParams: String?,
    @Json(name = "playIcon") val playIcon: Icon?,
    @Json(name = "pauseIcon") val pauseIcon: Icon?,
    @Json(name = "iconColor") val iconColor: Int?,
    @Json(name = "backgroundColor") val backgroundColor: Int?,
    @Json(name = "activeBackgroundColor") val activeBackgroundColor: Int?,
    @Json(name = "loadingIndicatorColor") val loadingIndicatorColor: Int?,
    @Json(name = "playingIcon") val playingIcon: Icon?,
    @Json(name = "iconLoadingColor") val iconLoadingColor: Int?,
    @Json(name = "activeScaleFactor") val activeScaleFactor: Double?,
    @Json(name = "buttonSize") val buttonSize: String?,
    @Json(name = "rippleTarget") val rippleTarget: String?,
    @Json(name = "accessibilityPlayData") val accessibilityPlayData: AccessibilityData?,
    @Json(name = "accessibilityPauseData") val accessibilityPauseData: AccessibilityData?
)

@JsonClass(generateAdapter = true)
data class PlayNavigationEndpoint(
    @Json(name = "clickTrackingParams") val clickTrackingParams: String?,
    @Json(name = "watchPlaylistEndpoint") val watchPlaylistEndpoint: WatchPlaylistEndpoint?
)

@JsonClass(generateAdapter = true)
data class WatchPlaylistEndpoint(
    @Json(name = "playlistId") val playlistId: String?,
    @Json(name = "params") val params: String?
)

@JsonClass(generateAdapter = true)
data class Icon(
    @Json(name = "iconType") val iconType: String?
)

@JsonClass(generateAdapter = true)
data class MusicResponsiveListItemRenderer(
    @Json(name = "thumbnail") val thumbnail: MusicThumbnailRenderer?,
    @Json(name = "flexColumns") val flexColumns: List<MusicResponsiveListItemFlexColumnRenderer?>?
)

@JsonClass(generateAdapter = true)
data class MusicResponsiveListItemFlexColumnRenderer(
    @Json(name = "musicResponsiveListItemFlexColumnRenderer") val text: Text?,
    @Json(name = "displayPriority") val displayPriority: String?
)

@JsonClass(generateAdapter = true)
data class Text(
    @Json(name = "text") val text: String?,
    @Json(name = "runs") val runs: List<RunData?>?
)

@JsonClass(generateAdapter = true)
data class ContinuationData(
    @Json(name = "nextContinuationData") val nextContinuationData: NextContinuationData?
)

@JsonClass(generateAdapter = true)
data class NextContinuationData(
    @Json(name = "continuation") val continuation: String?,
    @Json(name = "clickTrackingParams") val clickTrackingParams: String?
)
