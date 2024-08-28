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
                    Log.d("chtiop", ">>>" + tabContents)
                    categoryPlayListBaseName =
                        tabContents.musicCarouselShelfRenderer?.header?.musicCarouselShelfBasicHeaderRenderer?.accessibilityData?.accessibilityData?.label!!
                    val musicCategoryPlayListContentList = mutableListOf<MusicCategoryPlayList>()
                    for (shelfContent in tabContents.musicCarouselShelfRenderer?.contents!!) {
                        if (shelfContent.musicTwoRowItemRenderer != null) {

                            for (thumpNail in shelfContent.musicTwoRowItemRenderer?.thumbnailRenderer?.musicThumbnailRenderer?.thumbnail?.thumbnails!!) {
                                plaListThumpNail = thumpNail.url
                                break
                            }
                            if (shelfContent.musicTwoRowItemRenderer.title?.runs!!.isNotEmpty()) {
                                plaListName =
                                    shelfContent.musicTwoRowItemRenderer.title?.runs[0].text
                            }
                            try {
                                checkingPlayerId =
                                    shelfContent.musicTwoRowItemRenderer.thumbnailOverlay?.musicItemThumbnailOverlayRenderer?.content?.musicPlayButtonRenderer?.playNavigationEndpoint?.watchPlaylistEndpoint?.playlistId!!
                            } catch (ex: Exception) {
                                checkingPlayerId = "exception"
                            }
                            for (menuItem in shelfContent.musicTwoRowItemRenderer?.menu?.menuRenderer?.items!!) {


                                if (checkingPlayerId.equals("exception")) {
                                    plaListId =
                                        menuItem.menuNavigationItemRenderer?.navigationEndpoint?.watchPlaylistEndpoint?.playlistId
                                    break
                                } else if (checkingPlayerId.contains(menuItem.menuNavigationItemRenderer?.navigationEndpoint?.watchPlaylistEndpoint?.playlistId!!)) {
                                    plaListId =
                                        menuItem.menuNavigationItemRenderer?.navigationEndpoint?.watchPlaylistEndpoint?.playlistId
                                    break
                                }
                            }
                            if (plaListId != null) {
                                musicCategoryPlayListContentList.add(
                                    MusicCategoryPlayList(
                                        playListId = plaListId!!,
                                        playListName = plaListName!!,
                                        playListThump = plaListThumpNail!!
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
                                        for (playListNameData in flexColum.musicResponsiveListItemFlexColumnRenderer?.text?.runs) {
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
                    Log.d("DETAILS", "---------------------------------------")
//                musicCategoryPlayList.add(MusicCategoryPlayListBase(plaListBaseName = categoryPlayListBaseName!!,musicCategoryPlayListContentList)
                    if (musicCategoryPlayListContentList.size > 0) {
                        musicCategoryPlayList.add(
                            MusicCategoryPlayListBase(
                                plaListBaseName = categoryPlayListBaseName!!,
                                musicCategoryPlayListContentList
                            )
                        )
                    }

                    Log.d(
                        "DETAILS",
                        "<categoryPlayListBaseName>" + categoryPlayListBaseName + "<plaListId>" + plaListId + "<plaListName>" + plaListName + "<plaListThumpNail>" + plaListThumpNail
                    )

                    Log.d("DETAILS", "---------------ENDED------------------------")
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