package com.ramzmania.tubefy.data.dto.youtubemusic.category

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class YtMusicCategoryContent(
    @Json(name = "singleColumnBrowseResultsRenderer") val singleColumnBrowseResultsRenderer: SingleColumnBrowseResultsRenderer?
)

@JsonClass(generateAdapter = true)
data class SingleColumnBrowseResultsRenderer(
    val tabs: List<Tab>?
)

@JsonClass(generateAdapter = true)
data class Tab(
    @Json(name = "tabRenderer") val tabRenderer: TabRenderer?
)

@JsonClass(generateAdapter = true)
data class TabRenderer(
    val content: Content?
)

@JsonClass(generateAdapter = true)
data class Content(
    @Json(name = "sectionListRenderer") val sectionListRenderer: SectionListRenderer?
)

@JsonClass(generateAdapter = true)
data class SectionListRenderer(
    val contents: List<SectionContent>?
)

@JsonClass(generateAdapter = true)
data class SectionContent(
    @Json(name = "gridRenderer") val gridRenderer: GridRenderer?
)

@JsonClass(generateAdapter = true)
data class GridRenderer(
    val items: List<GridItem>?
)

@JsonClass(generateAdapter = true)
data class GridItem(
    @Json(name = "musicNavigationButtonRenderer") val musicNavigationButtonRenderer: MusicNavigationButtonRenderer?
)

@JsonClass(generateAdapter = true)
data class MusicNavigationButtonRenderer(
    @Json(name = "clickCommand") val clickCommand: ClickCommand?,
    @Json(name = "buttonText") val buttonText: ButtonText?
)

@JsonClass(generateAdapter = true)
data class ClickCommand(
    @Json(name = "browseEndpoint") val browseEndpoint: BrowseEndpoint?
)

@JsonClass(generateAdapter = true)
data class BrowseEndpoint(
    val browseId: String?,
    val params: String?
)

@JsonClass(generateAdapter = true)
data class ButtonText(
    val runs: List<TextRun>?
)

@JsonClass(generateAdapter = true)
data class TextRun(
    val text: String?
)
