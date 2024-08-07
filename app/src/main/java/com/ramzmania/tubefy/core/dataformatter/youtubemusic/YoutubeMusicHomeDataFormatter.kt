package com.ramzmania.tubefy.core.dataformatter.youtubemusic

import MusicCarouselContent
import com.ramzmania.tubefy.core.dataformatter.FormattingResult
import com.ramzmania.tubefy.core.dataformatter.UniversalYoutubeDataFormatter
import com.ramzmania.tubefy.data.dto.base.BaseContentData
import com.ramzmania.tubefy.data.dto.home.HomePageResponse
import com.ramzmania.tubefy.data.dto.youtubemusic.YoutubeMusicHomeVideoData

class YoutubeHomeDataFormatter :
    UniversalYoutubeDataFormatter<List<MusicCarouselContent>?, FormattingResult<List<HomePageResponse?>, Exception>>() {
    override suspend fun runFormatting(input: List<MusicCarouselContent>?): FormattingResult<List<HomePageResponse?>, Exception> {
//        val videoInfoList = mutableListOf<YoutubeMusicHomeVideoData>()
        val videoSortedList = Array
        val videoSortedListWithPlaylist = mutableListOf<BaseContentData>()
        val videoSortedListWithoutPlaylist = mutableListOf<BaseContentData>()

        input?.forEach { content ->
            val musicTwoRowItemRenderer = content.musicTwoRowItemRenderer
            val musicResponsiveListItemRenderer = content.musicResponsiveListItemRenderer

            if (musicTwoRowItemRenderer != null) {
                val videoId = musicTwoRowItemRenderer.navigationEndpoint?.browseEndpoint?.browseId
                val playlistId =
                    videoId
                        ?: musicTwoRowItemRenderer.menu?.menuRenderer?.items?.firstOrNull()?.menuNavigationItemRenderer?.navigationEndpoint?.watchPlaylistEndpoint?.playlistId
                val thumbnail =
                    musicTwoRowItemRenderer.thumbnailRenderer?.musicThumbnailRenderer?.thumbnail?.thumbnails?.firstOrNull()?.url
                val title = musicTwoRowItemRenderer.title?.runs?.firstOrNull()?.text
                val subtitle = musicTwoRowItemRenderer.subtitle?.runs?.firstOrNull()?.text
                if (playlistId != null) {
                    videoSortedListWithPlaylist.add(
                        BaseContentData(
                            videoId,
                            playlistId,
                            thumbnail,
                            if (subtitle.isNullOrBlank()) title else "$title\n$subtitle"

                        )
                    )
                } else {
                    videoSortedListWithoutPlaylist.add(
                        BaseContentData(
                            videoId,
                            playlistId,
                            thumbnail,
                            if (subtitle.isNullOrBlank()) title else "$title\n$subtitle"

                        )
                    )
                }

            }

            if (musicResponsiveListItemRenderer != null) {
                val videoId = musicResponsiveListItemRenderer.flexColumns?.flatMap {
                    it.musicResponsiveListItemFlexColumnRenderer?.text?.runs ?: emptyList()
                }
                    ?.firstOrNull { it.navigationEndpoint?.watchEndpoint?.videoId != null }?.navigationEndpoint?.watchEndpoint?.videoId
                val playlistId = videoId ?: musicResponsiveListItemRenderer.flexColumns?.flatMap {
                    it.musicResponsiveListItemFlexColumnRenderer?.text?.runs ?: emptyList()
                }
                    ?.firstOrNull { it.navigationEndpoint?.watchEndpoint?.playlistId != null }?.navigationEndpoint?.watchEndpoint?.playlistId
                val thumbnail =
                    musicResponsiveListItemRenderer.thumbnail?.musicThumbnailRenderer?.thumbnail?.thumbnails?.firstOrNull()?.url
                val title = musicResponsiveListItemRenderer.flexColumns?.flatMap {
                    it.musicResponsiveListItemFlexColumnRenderer?.text?.runs ?: emptyList()
                }
                    ?.firstOrNull { it.text != null }?.text
                val subtitle =
                    "" // Assuming subtitle is not available in MusicResponsiveListItemRenderer
//              "kk".toInt()
                if (playlistId != null) {
                    videoSortedListWithPlaylist.add(
                        BaseContentData(
                            videoId,
                            playlistId,
                            thumbnail,
                            if (subtitle.isNullOrBlank()) title else "$title\n$subtitle"
                        )
                    )
                } else {
                    videoSortedListWithoutPlaylist.add(
                        BaseContentData(
                            videoId,
                            playlistId,
                            thumbnail,
                            if (subtitle.isNullOrBlank()) title else "$title\n$subtitle"
                        )
                    )
                }
            }
        }
    }
}