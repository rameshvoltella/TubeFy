package com.ramzmania.tubefy.data.dto.youtubemusic.playlist.categoryplaylist

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass


@JsonClass(generateAdapter = true)
data class CategoryPlayListRoot(
    @Json(name = "contents")
    val contents: Contents?
)

@JsonClass(generateAdapter = true)
data class Contents(
    @Json(name = "singleColumnBrowseResultsRenderer")
    val singleColumnBrowseResultsRenderer: SingleColumnBrowseResultsRenderer?
)

@JsonClass(generateAdapter = true)
data class SingleColumnBrowseResultsRenderer(
    @Json(name = "tabs")
    val tabs: List<Tab>?
)

@JsonClass(generateAdapter = true)
data class Tab(
    @Json(name = "tabRenderer")
    val tabRenderer: TabRenderer?
)

@JsonClass(generateAdapter = true)
data class TabRenderer(
    @Json(name = "content")
    val content: Content?
)

@JsonClass(generateAdapter = true)
data class Content(
    @Json(name = "sectionListRenderer")
    val sectionListRenderer: SectionListRenderer?
)

@JsonClass(generateAdapter = true)
data class SectionListRenderer(
    @Json(name = "contents")
    val contents: List<SectionContent>?
)

@JsonClass(generateAdapter = true)
data class SectionContent(
    @Json(name = "musicCarouselShelfRenderer")
    val musicCarouselShelfRenderer: MusicCarouselShelfRenderer?
)

@JsonClass(generateAdapter = true)
data class MusicCarouselShelfRenderer(
    @Json(name = "header")
    val header: Header?,
    @Json(name = "contents")
    val contents: List<MusicContent>?
)

@JsonClass(generateAdapter = true)
data class Header(
    @Json(name = "musicCarouselShelfBasicHeaderRenderer")
    val musicCarouselShelfBasicHeaderRenderer: MusicCarouselShelfBasicHeaderRenderer?
)

@JsonClass(generateAdapter = true)
data class MusicCarouselShelfBasicHeaderRenderer(
    @Json(name = "accessibilityData")
    val accessibilityData: AccessibilityData?
)

@JsonClass(generateAdapter = true)
data class AccessibilityData(
    @Json(name = "accessibilityData")
    val accessibilityData: LabelData?
)

@JsonClass(generateAdapter = true)
data class LabelData(
    @Json(name = "label")
    val label: String?
)

@JsonClass(generateAdapter = true)
data class MusicContent(
    @Json(name = "musicTwoRowItemRenderer")
    val musicTwoRowItemRenderer: MusicTwoRowItemRenderer?
)

@JsonClass(generateAdapter = true)
data class MusicTwoRowItemRenderer(
    @Json(name = "title")
    val title: Title?,
    @Json(name = "thumbnailRenderer")
    val thumbnailRenderer: ThumbnailRenderer?,
    @Json(name = "navigationEndpoint")
    val navigationEndpoint: NavigationEndpoint?,
    @Json(name = "menu")
    val menu: Menu?,
    @Json(name = "thumbnailOverlay")
    val thumbnailOverlay: ThumbnailOverlay?
)

@JsonClass(generateAdapter = true)
data class Title(
    @Json(name = "runs")
    val runs: List<Run>?
)

@JsonClass(generateAdapter = true)
data class Run(
    @Json(name = "text")
    val text: String?,
    @Json(name = "navigationEndpoint")
    val navigationEndpoint: NavigationEndpoint? = null
)

@JsonClass(generateAdapter = true)
data class ThumbnailRenderer(
    @Json(name = "musicThumbnailRenderer")
    val musicThumbnailRenderer: MusicThumbnailRenderer?
)

@JsonClass(generateAdapter = true)
data class MusicThumbnailRenderer(
    @Json(name = "thumbnail")
    val thumbnail: Thumbnail?
)

@JsonClass(generateAdapter = true)
data class Thumbnail(
    @Json(name = "thumbnails")
    val thumbnails: List<ThumbnailImage>?
)

@JsonClass(generateAdapter = true)
data class ThumbnailImage(
    @Json(name = "url")
    val url: String?,
    @Json(name = "width")
    val width: Int?,
    @Json(name = "height")
    val height: Int?
)

@JsonClass(generateAdapter = true)
data class NavigationEndpoint(
    @Json(name = "browseEndpoint")
    val browseEndpoint: BrowseEndpoint? = null,
    @Json(name = "watchPlaylistEndpoint")
    val watchPlaylistEndpoint: WatchPlaylistEndpoint? = null
)

@JsonClass(generateAdapter = true)
data class BrowseEndpoint(
    @Json(name = "browseId")
    val browseId: String?
)

@JsonClass(generateAdapter = true)
data class WatchPlaylistEndpoint(
    @Json(name = "playlistId")
    val playlistId: String?,
    @Json(name = "params")
    val params: String? = null
)

@JsonClass(generateAdapter = true)
data class Menu(
    @Json(name = "menuRenderer")
    val menuRenderer: MenuRenderer?
)

@JsonClass(generateAdapter = true)
data class MenuRenderer(
    @Json(name = "items")
    val items: List<MenuItem>?
)

@JsonClass(generateAdapter = true)
data class MenuItem(
    @Json(name = "menuNavigationItemRenderer")
    val menuNavigationItemRenderer: MenuNavigationItemRenderer?
)

@JsonClass(generateAdapter = true)
data class MenuNavigationItemRenderer(
    @Json(name = "text")
    val text: Text?,
    @Json(name = "icon")
    val icon: Icon?,
    @Json(name = "navigationEndpoint")
    val navigationEndpoint: NavigationEndpoint?,
    @Json(name = "trackingParams")
    val trackingParams: String?
)

@JsonClass(generateAdapter = true)
data class Text(
    @Json(name = "runs")
    val runs: List<Run>?
)

@JsonClass(generateAdapter = true)
data class Icon(
    @Json(name = "iconType")
    val iconType: String?
)

@JsonClass(generateAdapter = true)
data class ThumbnailOverlay(
    @Json(name = "musicItemThumbnailOverlayRenderer")
    val musicItemThumbnailOverlayRenderer: MusicItemThumbnailOverlayRenderer?
)

@JsonClass(generateAdapter = true)
data class MusicItemThumbnailOverlayRenderer(
    @Json(name = "background")
    val background: Background?,
    @Json(name = "content")
    val content: ContentOverlay?
)

@JsonClass(generateAdapter = true)
data class Background(
    @Json(name = "verticalGradient")
    val verticalGradient: VerticalGradient?
)

@JsonClass(generateAdapter = true)
data class VerticalGradient(
    @Json(name = "gradientLayerColors")
    val gradientLayerColors: List<String>?
)

@JsonClass(generateAdapter = true)
data class ContentOverlay(
    @Json(name = "musicPlayButtonRenderer")
    val musicPlayButtonRenderer: MusicPlayButtonRenderer?
)

@JsonClass(generateAdapter = true)
data class MusicPlayButtonRenderer(
    @Json(name = "playNavigationEndpoint")
    val playNavigationEndpoint: NavigationEndpoint?
)
