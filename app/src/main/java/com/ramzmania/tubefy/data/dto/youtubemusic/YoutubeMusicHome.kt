import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class MusicHomeResponse(
    @Json(name = "contents") val contents: Contents?
)

@JsonClass(generateAdapter = true)
data class Contents(
    @Json(name = "singleColumnBrowseResultsRenderer") val singleColumnBrowseResultsRenderer: SingleColumnBrowseResultsRenderer?
)

@JsonClass(generateAdapter = true)
data class SingleColumnBrowseResultsRenderer(
    @Json(name = "tabs") val tabs: List<Tab>?
)

@JsonClass(generateAdapter = true)
data class Tab(
    @Json(name = "tabRenderer") val tabRenderer: TabRenderer?
)

@JsonClass(generateAdapter = true)
data class TabRenderer(
    @Json(name = "content") val content: Content?
)

@JsonClass(generateAdapter = true)
data class Content(
    @Json(name = "sectionListRenderer") val sectionListRenderer: SectionListRenderer?
)

@JsonClass(generateAdapter = true)
data class SectionListRenderer(
    @Json(name = "contents") val contents: List<SectionContent>?
)

@JsonClass(generateAdapter = true)
data class SectionContent(
    @Json(name = "musicCarouselShelfRenderer") val musicCarouselShelfRenderer: MusicCarouselShelfRenderer?
)

@JsonClass(generateAdapter = true)
data class MusicCarouselShelfRenderer(
    @Json(name = "contents") val contents: List<MusicCarouselContent>?
)

@JsonClass(generateAdapter = true)
data class MusicCarouselContent(
    @Json(name = "musicTwoRowItemRenderer") val musicTwoRowItemRenderer: MusicTwoRowItemRenderer?,
    @Json(name = "musicResponsiveListItemRenderer") val musicResponsiveListItemRenderer: MusicResponsiveListItemRenderer?
)

@JsonClass(generateAdapter = true)
data class MusicTwoRowItemRenderer(
    @Json(name = "thumbnailRenderer") val thumbnailRenderer: ThumbnailRenderer?,
    @Json(name = "title") val title: Title?,
    @Json(name = "subtitle") val subtitle: Subtitle?,
    @Json(name = "navigationEndpoint") val navigationEndpoint: NavigationEndpoint?,
    @Json(name = "menu") val menu: Menu?
)

@JsonClass(generateAdapter = true)
data class ThumbnailRenderer(
    @Json(name = "musicThumbnailRenderer") val musicThumbnailRenderer: MusicThumbnailRenderer?
)

@JsonClass(generateAdapter = true)
data class MusicThumbnailRenderer(
    @Json(name = "thumbnail") val thumbnail: Thumbnail?
)
@JsonClass(generateAdapter = true)
data class MusicThumbnailRendererBase(
    @Json(name = "musicThumbnailRenderer") val musicThumbnailRenderer: MusicThumbnailRendererThump?
)
@JsonClass(generateAdapter = true)
data class MusicThumbnailRendererThump(
    @Json(name = "thumbnail") val thumbnail: Thumbnail?
)
@JsonClass(generateAdapter = true)
data class Thumbnail(
    @Json(name = "thumbnails") val thumbnails: List<ThumbnailUrl>?
)

@JsonClass(generateAdapter = true)
data class ThumbnailUrl(
    @Json(name = "url") val url: String?
)

@JsonClass(generateAdapter = true)
data class Title(
    @Json(name = "runs") val runs: List<TextRun>?
)

@JsonClass(generateAdapter = true)
data class Subtitle(
    @Json(name = "runs") val runs: List<TextRun>?
)

@JsonClass(generateAdapter = true)
data class TextRun(
    @Json(name = "text") val text: String?
)

@JsonClass(generateAdapter = true)
data class NavigationEndpoint(
    @Json(name = "browseEndpoint") val browseEndpoint: BrowseEndpoint?
)

@JsonClass(generateAdapter = true)
data class BrowseEndpoint(
    @Json(name = "browseId") val browseId: String?
)

@JsonClass(generateAdapter = true)
data class Menu(
    @Json(name = "menuRenderer") val menuRenderer: MenuRenderer?
)

@JsonClass(generateAdapter = true)
data class MenuRenderer(
    @Json(name = "items") val items: List<MenuItem>?
)

@JsonClass(generateAdapter = true)
data class MenuItem(
    @Json(name = "menuNavigationItemRenderer") val menuNavigationItemRenderer: MenuNavigationItemRenderer?
)

@JsonClass(generateAdapter = true)
data class MenuNavigationItemRenderer(
    @Json(name = "navigationEndpoint") val navigationEndpoint: WatchPlaylistEndpoint?
)

@JsonClass(generateAdapter = true)
data class WatchPlaylistEndpoint(
    @Json(name = "watchPlaylistEndpoint") val watchPlaylistEndpoint: WatchPlaylist?
)

@JsonClass(generateAdapter = true)
data class WatchPlaylist(
    @Json(name = "playlistId") val playlistId: String?
)

@JsonClass(generateAdapter = true)
data class MusicResponsiveListItemRenderer(
    @Json(name = "trackingParams") val trackingParams: String,
//    @Json(name = "menu") val menu: List<Any>,
    @Json(name = "thumbnail") val thumbnail: MusicThumbnailRendererBase?,
    @Json(name = "flexColumns") val flexColumns: List<MusicResponsiveListItemFlexColumn>?
)



@JsonClass(generateAdapter = true)
data class MusicResponsiveListItemFlexColumn(
    @Json(name = "musicResponsiveListItemFlexColumnRenderer") val musicResponsiveListItemFlexColumnRenderer: MusicResponsiveListItemFlexColumnRenderer?
)

@JsonClass(generateAdapter = true)
data class MusicResponsiveListItemFlexColumnRenderer(
    @Json(name = "text") val text: Text?
)

@JsonClass(generateAdapter = true)
data class Text(
    @Json(name = "runs") val runs: List<TextRunWithNavigation>?
)

@JsonClass(generateAdapter = true)
data class TextRunWithNavigation(
    @Json(name = "text") val text: String,
    @Json(name = "navigationEndpoint") val navigationEndpoint: WatchEndpointBase?
)

@JsonClass(generateAdapter = true)
data class WatchEndpoint(
    @Json(name = "videoId") val videoId: String?,
    @Json(name = "playlistId") val playlistId: String?
)
@JsonClass(generateAdapter = true)
data class WatchEndpointBase(
    @Json(name = "watchEndpoint") val watchEndpoint: WatchEndpoint?
)
