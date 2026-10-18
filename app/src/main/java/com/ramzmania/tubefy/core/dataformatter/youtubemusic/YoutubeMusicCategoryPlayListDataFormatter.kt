package com.ramzmania.tubefy.core.dataformatter.youtubemusic

import android.util.Log
import com.ramzmania.tubefy.core.YoutubeCoreConstant
import com.ramzmania.tubefy.core.dataformatter.FormattingResult
import com.ramzmania.tubefy.core.dataformatter.UniversalYoutubeDataFormatter
import com.ramzmania.tubefy.data.dto.youtubemusic.category.MusicCategoryPlayList
import com.ramzmania.tubefy.data.dto.youtubemusic.category.MusicCategoryPlayListBase
import com.ramzmania.tubefy.data.dto.youtubemusic.playlist.categoryplaylist.CategoryPlayListRoot
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class YoutubeMusicCategoryPlayListDataFormatter @Inject constructor() :
    UniversalYoutubeDataFormatter<CategoryPlayListRoot, FormattingResult<List<MusicCategoryPlayListBase?>, Exception>>() {
    override suspend fun runFormatting(inputData: CategoryPlayListRoot): FormattingResult<List<MusicCategoryPlayListBase?>, Exception> {
        val musicCategoryPlayList = mutableListOf<MusicCategoryPlayListBase>()

        for (plaListTabData in inputData.contents?.singleColumnBrowseResultsRenderer?.tabs!!) {

            for (tabContents in plaListTabData.tabRenderer?.content?.sectionListRenderer?.contents!!) {
                if (tabContents.musicCarouselShelfRenderer != null) {

                    var categoryPlayListBaseName: String? = ""
                    var plaListId: String? = ""
                    var videoId: String? = ""
                    var plaListThumpNail: String? = ""
                    var plaListName: String? = ""
                    var checkingPlayerId = ""
//                                tabContents.musicCarouselShelfRenderer.contents[0].musicTwoRowItemRenderer.thumbnailRenderer.musicThumbnailRenderer.thumbnail.thumbnails[0].url
                    categoryPlayListBaseName =
                        tabContents.musicCarouselShelfRenderer?.header?.musicCarouselShelfBasicHeaderRenderer?.accessibilityData?.accessibilityData?.label
                            ?: continue
                    val musicCategoryPlayListContentList = mutableListOf<MusicCategoryPlayList>()
                    for (shelfContent in tabContents.musicCarouselShelfRenderer?.contents!!) {
                        if (shelfContent.musicTwoRowItemRenderer != null) {
                            val item = shelfContent.musicTwoRowItemRenderer
                            val name = item.title?.runs?.firstOrNull()?.text
                            val thumb = item.thumbnailRenderer?.musicThumbnailRenderer?.thumbnail?.thumbnails?.firstOrNull()?.url
                            val itemPlaylistId = twoRowPlaylistId(item)
                            if (!name.isNullOrBlank() && !itemPlaylistId.isNullOrBlank()) {
                                musicCategoryPlayListContentList.add(
                                    MusicCategoryPlayList(
                                        playListId = itemPlaylistId,
                                        playListName = name,
                                        playListThump = thumb.orEmpty()
                                    )
                                )
                            }
                        } else if (shelfContent.musicMultiRowListItemRenderer != null) {
                            // Podcast episode: plays directly
                            val episode = shelfContent.musicMultiRowListItemRenderer
                            val episodeId = episode.onTap?.watchEndpoint?.videoId
                            val name = episode.title?.runs?.firstOrNull()?.text
                            if (!episodeId.isNullOrBlank() && !name.isNullOrBlank()) {
                                musicCategoryPlayListContentList.add(
                                    MusicCategoryPlayList(
                                        videoId = episodeId,
                                        playListName = name,
                                        playListThump = episode.thumbnail?.musicThumbnailRenderer?.thumbnail?.thumbnails?.lastOrNull()?.url
                                            ?: "https://i.ytimg.com/vi/$episodeId/hqdefault.jpg"
                                    )
                                )
                            }
                        } else if (shelfContent.musicResponsiveListItemRenderer != null) {
                            /* for (thumpNail in shelfContent.musicResponsiveListItemRenderer?.thumbnail?.musicThumbnailRenderer?.thumbnail?.thumbnails!!) {
                            plaListThumpNail = thumpNail.url
                            break
                        }*/
                            if (shelfContent.musicResponsiveListItemRenderer.flexColumns != null) {
                                for (flexColum in shelfContent.musicResponsiveListItemRenderer.flexColumns!!) {
                                    if (flexColum.musicResponsiveListItemFlexColumnRenderer?.text?.runs != null) {
                                        for (playListNameData in flexColum.musicResponsiveListItemFlexColumnRenderer?.text?.runs.orEmpty()) {
                                            if (playListNameData.text != null) {
                                                plaListName = playListNameData.text + "\n"
                                            }
                                            if (playListNameData.navigationEndpoint != null) {
                                                if (playListNameData.navigationEndpoint.watchEndpoint != null) {
                                                    if (playListNameData.navigationEndpoint.watchEndpoint.videoId != null) {
                                                        videoId =
                                                            playListNameData.navigationEndpoint.watchEndpoint.videoId
                                                    }
                                                }
                                            }
                                        }

                                    }

                                }
                            }
                            if (videoId != null) {
                                musicCategoryPlayListContentList.add(
                                    MusicCategoryPlayList(
                                        videoId = videoId,
                                        playListName = plaListName!!,
                                        playListThump = "https://i.ytimg.com/vi/${
                                            YoutubeCoreConstant.extractYoutubeVideoId(
                                                URLDecoder.decode(
                                                    videoId,
                                                    StandardCharsets.UTF_8.toString()
                                                )
                                            )
                                        }/hq720.jpg"
                                    )
                                )
                            }

                        }
                    }
//                musicCategoryPlayList.add(MusicCategoryPlayListBase(plaListBaseName = categoryPlayListBaseName!!,musicCategoryPlayListContentList)
                    if (musicCategoryPlayListContentList.size > 0) {
                        musicCategoryPlayList.add(
                            MusicCategoryPlayListBase(
                                plaListBaseName = categoryPlayListBaseName!!,
                                musicCategoryPlayListContentList
                            )
                        )
                    }


                }


            }
        }


        if (musicCategoryPlayList.size > 0) {
            return FormattingResult.SUCCESS(musicCategoryPlayList)
        } else {
            return FormattingResult.FAILURE(Exception("No category Found"))
        }

    }
}

/**
 * The playlist a two-row card opens. Podcast shows link to "MPSP<playlistId>"; music playlists
 * expose it through their play button / menu. Null-safe: some menu items have no navigation.
 */
private fun twoRowPlaylistId(item: com.ramzmania.tubefy.data.dto.youtubemusic.playlist.categoryplaylist.MusicTwoRowItemRenderer): String? {
    val browseId = item.navigationEndpoint?.browseEndpoint?.browseId
    if (browseId != null && browseId.startsWith("MPSP")) return browseId.removePrefix("MPSP")
    val playButtonId = item.thumbnailOverlay?.musicItemThumbnailOverlayRenderer?.content
        ?.musicPlayButtonRenderer?.playNavigationEndpoint?.watchPlaylistEndpoint?.playlistId
    val menuIds = item.menu?.menuRenderer?.items.orEmpty()
        .mapNotNull { it.menuNavigationItemRenderer?.navigationEndpoint?.watchPlaylistEndpoint?.playlistId }
    return if (playButtonId != null) menuIds.firstOrNull { playButtonId.contains(it) } ?: playButtonId
    else menuIds.firstOrNull()
}
