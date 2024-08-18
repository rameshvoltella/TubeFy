package com.ramzmania.tubefy.data.dto.youtubemusic.playlist

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class YoutubeMusicPlayListContent(
    @Json(name = "contents") val contents: Contents?,
    val background: Background?
)

@JsonClass(generateAdapter = true)
data class Contents(
    @Json(name = "twoColumnBrowseResultsRenderer") val twoColumnBrowseResultsRenderer: TwoColumnBrowseResultsRenderer?
)
@JsonClass(generateAdapter = true)
data class TwoColumnBrowseResultsRenderer(
    @Json(name = "secondaryContents")  val contents: secondaryContents
)
@JsonClass(generateAdapter = true)
data class secondaryContents(
    @Json(name = "sectionListRenderer") val sectionListRenderer:SectionListRenderer?)

@JsonClass(generateAdapter = true)
data class SectionListRenderer(
    @Json(name = "contents") val contents: List<ContentItem>?
)

@JsonClass(generateAdapter = true)
data class ContentItem(
    @Json(name = "musicPlaylistShelfRenderer") val musicPlaylistShelfRenderer: MusicPlaylistShelfRenderer?
)

@JsonClass(generateAdapter = true)
data class MusicPlaylistShelfRenderer(
   @Json(name = "contents") val contents: List<MusicContent>?
)

@JsonClass(generateAdapter = true)
data class MusicContent(
    @Json(name = "musicResponsiveListItemRenderer") val musicResponsiveListItemRenderer: MusicResponsiveListItemRenderer?
)

@JsonClass(generateAdapter = true)
data class MusicResponsiveListItemRenderer(
    @Json(name = "trackingParams")val trackingParams: String?,
    @Json(name = "thumbnail")val thumbnail: Thumbnail?,
    @Json(name = "flexColumns")val flexColumns: List<FlexColumn>?
)

@JsonClass(generateAdapter = true)
data class Thumbnail(
    @Json(name = "musicThumbnailRenderer") val musicThumbnailRenderer: MusicThumbnailRenderer?
)

@JsonClass(generateAdapter = true)
data class MusicThumbnailRenderer(
    val thumbnail: ThumbnailDetail?,
    val thumbnailCrop: String?,
    val thumbnailScale: String?,
    val trackingParams: String?
)

@JsonClass(generateAdapter = true)
data class ThumbnailDetail(
    val thumbnails: List<ThumbnailImage>?
)

@JsonClass(generateAdapter = true)
data class ThumbnailImage(
    val url: String?,
    val width: Int?,
    val height: Int?
)

@JsonClass(generateAdapter = true)
data class FlexColumn(
    @Json(name = "musicResponsiveListItemFlexColumnRenderer") val musicResponsiveListItemFlexColumnRenderer: MusicResponsiveListItemFlexColumnRenderer?
)

@JsonClass(generateAdapter = true)
data class MusicResponsiveListItemFlexColumnRenderer(
    val text: Text?,
    val displayPriority: String?
)

@JsonClass(generateAdapter = true)
data class Text(
    val runs: List<Run>?
)

@JsonClass(generateAdapter = true)
data class Run(
    val text: String?,
    val navigationEndpoint: NavigationEndpoint? = null
)

@JsonClass(generateAdapter = true)
data class NavigationEndpoint(
    val clickTrackingParams: String?,
    val watchEndpoint: WatchEndpoint?
)

@JsonClass(generateAdapter = true)
data class WatchEndpoint(
    val videoId: String?,
    val playlistId: String?,
    val params: String?,
    val loggingContext: LoggingContext?,
    val watchEndpointMusicSupportedConfigs: WatchEndpointMusicSupportedConfigs?
)

@JsonClass(generateAdapter = true)
data class LoggingContext(
    val vssLoggingContext: VssLoggingContext?
)

@JsonClass(generateAdapter = true)
data class VssLoggingContext(
    val serializedContextData: String?
)

@JsonClass(generateAdapter = true)
data class WatchEndpointMusicSupportedConfigs(
    val watchEndpointMusicConfig: WatchEndpointMusicConfig?
)

@JsonClass(generateAdapter = true)
data class WatchEndpointMusicConfig(
    val musicVideoType: String?
)

@JsonClass(generateAdapter = true)
data class Background(
    @Json(name = "musicThumbnailRenderer") val musicThumbnailRenderer: MusicThumbnailRenderer
)
