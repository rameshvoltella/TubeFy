package com.ramzmania.tubefy.core.dataformatter.youtubemusic


import android.util.Log
import com.ramzmania.tubefy.core.dataformatter.FormattingResult
import com.ramzmania.tubefy.core.dataformatter.UniversalYoutubeDataFormatter
import com.ramzmania.tubefy.data.dto.base.BaseContentData
import com.ramzmania.tubefy.data.dto.home.CellType
import com.ramzmania.tubefy.data.dto.home.HomePageResponse
import com.ramzmania.tubefy.data.dto.home.youtubei.PaginationContent
import com.ramzmania.tubefy.data.dto.home.youtubei.YoutubeiHomeBaseResponse
import com.ramzmania.tubefy.data.dto.home.youtubei.YoutubeiMusicHomeApiResponse

import javax.inject.Inject

class YoutubeMusicYoutubeiDataHomeFormatter @Inject constructor() :
    UniversalYoutubeDataFormatter<YoutubeiMusicHomeApiResponse, FormattingResult<YoutubeiHomeBaseResponse, Exception>>() {
    override suspend fun runFormatting(inputData: YoutubeiMusicHomeApiResponse): FormattingResult<YoutubeiHomeBaseResponse, Exception> {

//        if (musicCategoryPlayList.size > 0) {
//            return FormattingResult.SUCCESS(musicCategoryPlayList)
//        } else {
//            return FormattingResult.FAILURE(Exception("No category Found"))

        try {
//        }
            val youtubeMusicHomeDataList = mutableListOf<HomePageResponse>()
            var youtubeiHomeFirstResponse: YoutubeiHomeBaseResponse? = null
            var paginationHex: String? = ""
            var paginationId: String? = ""
            var visitorData: String? = ""

            for (tabsData in inputData.contents?.singleColumnBrowseResultsRenderer?.tabs!!) {
                if (tabsData.tabRenderer?.content?.sectionListRenderer?.continuations != null) {
                    for (continuations in tabsData.tabRenderer?.content?.sectionListRenderer?.continuations!!) {
                        paginationHex = continuations.nextContinuationData?.continuation
                        paginationId = continuations.nextContinuationData?.clickTrackingParams
                        if (paginationHex != null) {
                            break
                        }
                    }

                }
                for (sectionListRendererContents in tabsData.tabRenderer?.content?.sectionListRenderer?.contents!!) {
                    var headingData = ""
                    if (sectionListRendererContents.musicCarouselShelfRenderer?.header?.musicCarouselShelfBasicHeaderRenderer?.accessibilityData?.accessibilityData?.label != null) {
                        headingData =
                            sectionListRendererContents.musicCarouselShelfRenderer?.header?.musicCarouselShelfBasicHeaderRenderer?.accessibilityData?.accessibilityData?.label
                    } else {
                        headingData = "TOP"
                    }


                    var isList = false
                    val listOfBaseContent = mutableListOf<BaseContentData>()

                    if (sectionListRendererContents?.musicCarouselShelfRenderer != null) {
                        for (musicCarouselShelfRendererContents in sectionListRendererContents.musicCarouselShelfRenderer?.contents!!) {


                            if (musicCarouselShelfRendererContents.musicResponsiveListItemRenderer != null) {
                                var videoName = ""
                                var videoId = ""
                                var videoPlayList = ""
                                var videoThump = ""
                                isList = true
                                for (thumpNailData in musicCarouselShelfRendererContents.musicResponsiveListItemRenderer.thumbnail?.musicThumbnailRenderer?.thumbnail?.thumbnails!!) {
                                    if (videoThump.isEmpty()) {
                                        videoThump = thumpNailData.url!!
                                        break
                                    }
                                }

                                for (flexColumData in musicCarouselShelfRendererContents.musicResponsiveListItemRenderer.flexColumns!!) {

                                    if (flexColumData.musicResponsiveListItemFlexColumnRenderer?.text?.runs != null) {
                                        for (runsData in flexColumData.musicResponsiveListItemFlexColumnRenderer?.text?.runs!!) {
                                            videoName = runsData.text+"\n"+videoName
                                            if (runsData.navigationEndpoint != null && runsData.navigationEndpoint.watchEndpoint != null) {
                                                videoId =
                                                    runsData.navigationEndpoint.watchEndpoint.videoId!!
                                                if (videoId.length >= 11) {
                                                    break
                                                }
                                            }

                                        }
                                    }

                                }


                                if (videoId != null && videoId.length > 1) {
                                    listOfBaseContent.add(
                                        BaseContentData(
                                            videoId,
                                            videoPlayList,
                                            videoThump,
                                            videoName
                                        )
                                    )
                                }
                            } else if (musicCarouselShelfRendererContents.musicTwoRowItemRenderer != null) {
                                var videoName = ""
                                var videoId = ""
                                var videoPlayList = ""
                                var videoThump = ""
                                isList = false
                                for (runsData in musicCarouselShelfRendererContents.musicTwoRowItemRenderer.title?.runs!!) {
                                    videoName = videoName + "\n" + runsData.text
                                    if (videoName.length > 1) {
                                        break
                                    }
                                }

                                for (thumpNailData in musicCarouselShelfRendererContents.musicTwoRowItemRenderer.thumbnailRenderer?.musicThumbnailRenderer?.thumbnail?.thumbnails!!) {
                                    if (videoThump.isEmpty()) {
                                        videoThump = thumpNailData.url!!
                                        break
                                    }
                                }

                                if (musicCarouselShelfRendererContents.musicTwoRowItemRenderer.thumbnailOverlay?.musicItemThumbnailOverlayRenderer?.content?.musicPlayButtonRenderer?.playNavigationEndpoint?.watchPlaylistEndpoint?.playlistId != null) {
                                    videoPlayList =
                                        musicCarouselShelfRendererContents.musicTwoRowItemRenderer.thumbnailOverlay?.musicItemThumbnailOverlayRenderer?.content?.musicPlayButtonRenderer?.playNavigationEndpoint?.watchPlaylistEndpoint?.playlistId!!
                                }


                                if (videoPlayList != null && videoPlayList.length > 1) {
                                    listOfBaseContent.add(
                                        BaseContentData(
                                            videoId,
                                            videoPlayList,
                                            videoThump,
                                            videoName
                                        )
                                    )
                                }
                            }


                        }

                        youtubeMusicHomeDataList.add(
                            HomePageResponse(
                                headingData ?: "Top",
                                if (isList == true) CellType.LIST else CellType.HORIZONTAL_LIST,
                                listOfBaseContent
                            )
                        )
                    }


                }

            }

            if (inputData.responseContext?.visitorData != null) {
                val visiterData = inputData.responseContext?.visitorData
                if (paginationHex != null && paginationHex.length > 0) {
                    youtubeiHomeFirstResponse = YoutubeiHomeBaseResponse(
                        PaginationContent(
                            paginationId,
                            paginationHex,
                            visiterData
                        ), youtubeMusicHomeDataList
                    )

                } else {
                    youtubeiHomeFirstResponse =
                        YoutubeiHomeBaseResponse(null, youtubeMusicHomeDataList)

                }
            } else {
                youtubeiHomeFirstResponse =
                    YoutubeiHomeBaseResponse(null, youtubeMusicHomeDataList)

            }
            if (youtubeiHomeFirstResponse != null) {


                return FormattingResult.SUCCESS(youtubeiHomeFirstResponse)

            } else {
                return FormattingResult.FAILURE(Exception("No data Found"))

            }

//        inputData.contents.singleColumnBrowseResultsRenderer.tabs[0].tabRenderer.content.sectionListRenderer.contents[0].musicCarouselShelfRenderer.contents[0].musicResponsiveListItemRenderer.
        } catch (ex: Exception) {
            ex.printStackTrace()
            return FormattingResult.FAILURE(Exception("No category Found"))

        }
    }
}