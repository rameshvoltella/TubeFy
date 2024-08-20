package com.ramzmania.tubefy.data.dto.home.youtubei.next

import com.ramzmania.tubefy.data.dto.home.youtubei.AccessibilityData
import com.ramzmania.tubefy.data.dto.home.youtubei.Content
import com.ramzmania.tubefy.data.dto.home.youtubei.Continuation
import com.ramzmania.tubefy.data.dto.home.youtubei.MusicCarouselShelfBasicHeaderRenderer
import com.ramzmania.tubefy.data.dto.home.youtubei.MusicCarouselShelfRenderer
import com.ramzmania.tubefy.data.dto.home.youtubei.MusicThumbnailRenderer
import com.ramzmania.tubefy.data.dto.home.youtubei.NavigationEndpoint
import com.ramzmania.tubefy.data.dto.home.youtubei.NextContinuationData
import com.ramzmania.tubefy.data.dto.home.youtubei.Run
import com.ramzmania.tubefy.data.dto.home.youtubei.Thumbnail
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Json


@JsonClass(generateAdapter = true)
data class YoutubeiHomeContinuationContentRoot(
    @Json(name = "continuationContents")
    val continuationContents: ContinuationContents
)
@JsonClass(generateAdapter = true)
data class ContinuationContents(
    @Json(name = "sectionListContinuation")
    val sectionListContinuation: SectionListContinuation
)

@JsonClass(generateAdapter = true)
data class SectionListContinuation(
    @Json(name = "contents")
    val contents: List<Content>,
    @Json(name = "continuations")
    val continuations: List<Continuation>
)

@JsonClass(generateAdapter = true)
data class Content(
    @Json(name = "musicCarouselShelfRenderer")
    val musicCarouselShelfRenderer: MusicCarouselShelfRenderer
)

@JsonClass(generateAdapter = true)
data class MusicCarouselShelfRenderer(
    @Json(name = "header")
    val header: Header,
    @Json(name = "contents")
    val contents: List<MusicContent>
)

@JsonClass(generateAdapter = true)
data class Header(
    @Json(name = "musicCarouselShelfBasicHeaderRenderer")
    val musicCarouselShelfBasicHeaderRenderer: MusicCarouselShelfBasicHeaderRenderer
)

@JsonClass(generateAdapter = true)
data class MusicCarouselShelfBasicHeaderRenderer(
    @Json(name = "accessibilityData")
    val accessibilityData: AccessibilityData
)

@JsonClass(generateAdapter = true)
data class AccessibilityData(
    @Json(name = "accessibilityData")
    val accessibilityData: AccessibilityDataLabel
)

@JsonClass(generateAdapter = true)
data class AccessibilityDataLabel(
    @Json(name = "label")
    val label: String
)

@JsonClass(generateAdapter = true)
data class MusicContent(
    @Json(name = "musicTwoRowItemRenderer")
    val musicTwoRowItemRenderer: MusicTwoRowItemRenderer
)

@JsonClass(generateAdapter = true)
data class MusicTwoRowItemRenderer(
    @Json(name = "thumbnailRenderer")
    val thumbnailRenderer: ThumbnailRenderer,
    @Json(name = "title")
    val title: Title,
    @Json(name = "thumbnailOverlay")
    val thumbnailOverlay: ThumbnailOverlay
)

@JsonClass(generateAdapter = true)
data class ThumbnailRenderer(
    @Json(name = "musicThumbnailRenderer")
    val musicThumbnailRenderer: MusicThumbnailRenderer
)

@JsonClass(generateAdapter = true)
data class MusicThumbnailRenderer(
    @Json(name = "thumbnail")
    val thumbnail: Thumbnail
)

@JsonClass(generateAdapter = true)
data class Thumbnail(
    @Json(name = "thumbnails")
    val thumbnails: List<ThumbnailDetail>
)

@JsonClass(generateAdapter = true)
data class ThumbnailDetail(
    @Json(name = "url")
    val url: String,
    @Json(name = "width")
    val width: Int,
    @Json(name = "height")
    val height: Int
)

@JsonClass(generateAdapter = true)
data class Title(
    @Json(name = "runs")
    val runs: List<Run>
)

@JsonClass(generateAdapter = true)
data class Run(
    @Json(name = "text")
    val text: String,
    @Json(name = "navigationEndpoint")
    val navigationEndpoint: NavigationEndpoint
)

@JsonClass(generateAdapter = true)
data class NavigationEndpoint(
    @Json(name = "clickTrackingParams")
    val clickTrackingParams: String,
    @Json(name = "browseEndpoint")
    val browseEndpoint: BrowseEndpoint
)

@JsonClass(generateAdapter = true)
data class BrowseEndpoint(
    @Json(name = "browseId")
    val browseId: String,
    @Json(name = "browseEndpointContextSupportedConfigs")
    val browseEndpointContextSupportedConfigs: BrowseEndpointContextSupportedConfigs
)

@JsonClass(generateAdapter = true)
data class BrowseEndpointContextSupportedConfigs(
    @Json(name = "browseEndpointContextMusicConfig")
    val browseEndpointContextMusicConfig: BrowseEndpointContextMusicConfig
)

@JsonClass(generateAdapter = true)
data class BrowseEndpointContextMusicConfig(
    @Json(name = "pageType")
    val pageType: String
)

@JsonClass(generateAdapter = true)
data class ThumbnailOverlay(
    @Json(name = "musicItemThumbnailOverlayRenderer")
    val musicItemThumbnailOverlayRenderer: MusicItemThumbnailOverlayRenderer
)

@JsonClass(generateAdapter = true)
data class MusicItemThumbnailOverlayRenderer(
    @Json(name = "background")
    val background: Background,
    @Json(name = "content")
    val content: ContentOverlay
)

@JsonClass(generateAdapter = true)
data class Background(
    @Json(name = "verticalGradient")
    val verticalGradient: VerticalGradient
)

@JsonClass(generateAdapter = true)
data class VerticalGradient(
    @Json(name = "gradientLayerColors")
    val gradientLayerColors: List<String>
)

@JsonClass(generateAdapter = true)
data class ContentOverlay(
    @Json(name = "musicPlayButtonRenderer")
    val musicPlayButtonRenderer: MusicPlayButtonRenderer
)

@JsonClass(generateAdapter = true)
data class MusicPlayButtonRenderer(
    @Json(name = "playNavigationEndpoint")
    val playNavigationEndpoint: PlayNavigationEndpoint
)

@JsonClass(generateAdapter = true)
data class PlayNavigationEndpoint(
    @Json(name = "clickTrackingParams")
    val clickTrackingParams: String,
    @Json(name = "watchPlaylistEndpoint")
    val watchPlaylistEndpoint: WatchPlaylistEndpoint
)

@JsonClass(generateAdapter = true)
data class WatchPlaylistEndpoint(
    @Json(name = "playlistId")
    val playlistId: String,
    @Json(name = "params")
    val params: String
)

@JsonClass(generateAdapter = true)
data class Continuation(
    @Json(name = "nextContinuationData")
    val nextContinuationData: NextContinuationData
)

@JsonClass(generateAdapter = true)
data class NextContinuationData(
    @Json(name = "continuation")
    val continuation: String,
    @Json(name = "clickTrackingParams")
    val clickTrackingParams: String
)
