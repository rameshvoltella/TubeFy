package com.ramzmania.tubefy.core.dataformatter.youtubemusic

import android.util.Log
import com.ramzmania.tubefy.core.dataformatter.FormattingResult
import com.ramzmania.tubefy.core.dataformatter.UniversalYoutubeDataFormatter
import com.ramzmania.tubefy.data.dto.base.BaseContentData
import com.ramzmania.tubefy.data.dto.home.CellType
import com.ramzmania.tubefy.data.dto.home.HomePageResponse
import com.ramzmania.tubefy.data.dto.youtubestripper.MusicHomeResponse2
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class YoutubeMusicHomeDataFormatter @Inject constructor()  :
    UniversalYoutubeDataFormatter<MusicHomeResponse2, FormattingResult<List<HomePageResponse?>, Exception>>() {
    override suspend fun runFormatting(inputData: MusicHomeResponse2): FormattingResult<List<HomePageResponse?>, Exception> {
//        val videoInfoList = mutableListOf<YoutubeMusicHomeVideoData>()
       var input= inputData!!.contents?.singleColumnBrowseResultsRenderer?.tabs?.firstOrNull()?.tabRenderer?.content?.sectionListRenderer?.contents?.flatMap {
           it.musicCarouselShelfRenderer?.contents ?: emptyList()
       }
        try{
        val videoSortedList = mutableListOf<HomePageResponse>()
        val videoSortedListWithPlaylist = mutableListOf<BaseContentData>()
        val videoSortedListWithoutPlaylist = mutableListOf<BaseContentData>()

        input?.forEach { content ->
            val musicTwoRowItemRenderer = content.musicTwoRowItemRenderer
            val musicResponsiveListItemRenderer = content.musicResponsiveListItemRenderer

            if (musicTwoRowItemRenderer != null) {
                val videoId = musicTwoRowItemRenderer.navigationEndpoint?.browseEndpoint?.browseId
                val playlistId =
                    musicTwoRowItemRenderer.menu?.menuRenderer?.items?.firstOrNull()?.menuNavigationItemRenderer?.navigationEndpoint?.watchPlaylistEndpoint?.playlistId
                val thumbnail =
                    musicTwoRowItemRenderer.thumbnailRenderer?.musicThumbnailRenderer?.thumbnail?.thumbnails?.firstOrNull()?.url
                val title = musicTwoRowItemRenderer.title?.runs?.firstOrNull()?.text
                val subtitle = musicTwoRowItemRenderer.subtitle?.runs?.firstOrNull()?.text
                Log.d("check first","$videoId <><> $playlistId")
                if (videoId!!.length> 11) {
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
                val playlistId = musicResponsiveListItemRenderer.flexColumns?.flatMap {
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
                Log.d("check two","$videoId <><> $playlistId")

                if (videoId!!.length> 11) {
                    videoSortedListWithPlaylist.add(
                        BaseContentData(
                            videoId,
                            playlistId,
                            thumbnail,
                            if (subtitle.isNullOrBlank()) title else "$title\n$subtitle",true
                        )
                    )
                } else {
                    videoSortedListWithoutPlaylist.add(
                        BaseContentData(
                            videoId,
                            playlistId,
                            thumbnail,
                            if (subtitle.isNullOrBlank()) title else "$title\n$subtitle",true
                        )
                    )
                }
            }
        }
            Log.d("what size","videoSortedListWithoutPlaylist>>>"+videoSortedListWithoutPlaylist.size)
            Log.d("what size","videoSortedListWithPlaylist>>>"+videoSortedListWithPlaylist.size)

            if (videoSortedListWithoutPlaylist.size > 0) {
            videoSortedList.add(HomePageResponse("Most Played",CellType.LIST, videoSortedListWithoutPlaylist))
        }
        if (videoSortedListWithPlaylist.size > 0) {

            videoSortedList.add(
                HomePageResponse("Top Albums",
                    CellType.HORIZONTAL_LIST,
                    videoSortedListWithPlaylist
                )
            )
        }
        return FormattingResult.SUCCESS(videoSortedList)
    }
        catch (ex:Exception)
        {
            return FormattingResult.FAILURE(Exception("ERROR IN MUSIC HOME PAGE SORT"))

        }
    }
}