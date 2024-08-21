package com.ramzmania.tubefy.core.dataformatter.youtubemusic


import android.util.Log
import com.ramzmania.tubefy.core.dataformatter.FormattingResult
import com.ramzmania.tubefy.core.dataformatter.UniversalYoutubeDataFormatter
import com.ramzmania.tubefy.data.dto.base.BaseContentData
import com.ramzmania.tubefy.data.dto.home.CellType
import com.ramzmania.tubefy.data.dto.home.HomePageResponse
import com.ramzmania.tubefy.data.dto.home.youtubei.PaginationContent
import com.ramzmania.tubefy.data.dto.home.youtubei.YoutubeiHomeBaseResponse
import com.ramzmania.tubefy.data.dto.home.youtubei.next.ContinuationContents

import javax.inject.Inject

class YoutubeMusicYoutubeiDataHomePaginationFormatter @Inject constructor() :
    UniversalYoutubeDataFormatter<ContinuationContents, FormattingResult<YoutubeiHomeBaseResponse, Exception>>() {
    override suspend fun runFormatting(inputData: ContinuationContents): FormattingResult<YoutubeiHomeBaseResponse, Exception> {

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

            if (inputData.continuationContents?.sectionListContinuation != null) {

                if (inputData.continuationContents?.sectionListContinuation?.continuations != null) {
                    for (continuations in inputData.continuationContents?.sectionListContinuation?.continuations) {
                        paginationHex = continuations?.nextContinuationData?.continuation
                        paginationId = continuations?.nextContinuationData?.clickTrackingParams
                        if (paginationHex != null) {
                            break
                        }
                    }

                }

                if (inputData.continuationContents?.sectionListContinuation?.contents != null) {

                    for (contentData in inputData.continuationContents?.sectionListContinuation?.contents) {
                        var headingData = ""
                        if (contentData?.musicCarouselShelfRenderer?.header?.musicCarouselShelfBasicHeaderRenderer?.accessibilityData?.accessibilityLabel?.label
                            != null
                        ) {
                            headingData =
                                contentData.musicCarouselShelfRenderer?.header?.musicCarouselShelfBasicHeaderRenderer?.accessibilityData?.accessibilityLabel?.label
                        } else {
                            headingData = "TOP"
                        }
                        Log.d("videotestyy >>", "---------------------")

                        Log.d("videotestyy >>", "" + headingData)
                        var isList = false
                        val listOfBaseContent = mutableListOf<BaseContentData>()
//                        contentData.musicCarouselShelfRenderer.contents[0].
                        if (contentData?.musicCarouselShelfRenderer != null) {
                            Log.d(
                                "videotestyy >>",
                                headingData + "-----------musicCarouselShelfRenderer not null----------" + contentData
                            )

                            for (musicCarouselShelfRendererContents in contentData.musicCarouselShelfRenderer?.contents!!) {
                                Log.d(
                                    "videotestyy >>",
                                    headingData + "-----------musicCarouselShelfRendererContents not null----------"
                                )

                                if (musicCarouselShelfRendererContents?.musicResponsiveListItemRenderer != null) {
                                    Log.d(
                                        "videotestyy >>",
                                        headingData + "-----------musicResponsiveListItemRenderer not null----------"
                                    )

                                    var videoName = ""
                                    var videoId = ""
                                    var videoPlayList = ""
                                    var videoThump = ""
                                    isList = true
                                    for (thumpNailData in musicCarouselShelfRendererContents.musicResponsiveListItemRenderer.thumbnail?.musicThumbnailRenderer?.thumbnail?.thumbnails!!) {
                                        Log.d(
                                            "videotestyy >>",
                                            headingData + "-----------thump not null----------"
                                        )

                                        if (videoThump.isEmpty()) {
                                            videoThump = thumpNailData?.url!!
                                            break
                                        }
                                    }

                                    for (flexColumData in musicCarouselShelfRendererContents.musicResponsiveListItemRenderer.flexColumns!!) {
                                        Log.d(
                                            "videotestyy >>",
                                            headingData + "-----------flexcol not null----------" + flexColumData?.text?.text
                                        )

                                        if (flexColumData?.text?.text?.runs != null) {
                                            Log.d(
                                                "videotestyy >>",
                                                headingData + "-----------flexcol not nul222l----------"
                                            )

                                            for (runsData in flexColumData?.text?.text?.runs!!) {
                                                if (videoName.length == 0) {
                                                    videoName = runsData?.text + "" + videoName
                                                }
                                                Log.d(
                                                    "videotestyy >>",
                                                    headingData + "-----------flexcol not null3333----------"
                                                )

                                                if (runsData?.navigationEndpoint != null && runsData.navigationEndpoint.watchEndpoint != null) {
                                                    videoId =
                                                        runsData.navigationEndpoint.watchEndpoint.videoId!!
                                                    Log.d(
                                                        "videotestyy >>",
                                                        headingData + "-----------flexcol not null4444----------"
                                                    )

                                                    if (videoId.length >= 11) {
                                                        break
                                                    }
                                                }

                                            }
                                        }

                                    }

                                    Log.d(
                                        "videotestyy >>",
                                        headingData + "-----------settingggg----------"
                                    )

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
                                } else if (musicCarouselShelfRendererContents?.musicTwoRowItemRenderer != null) {
                                    var videoName = ""
                                    var videoId = ""
                                    var videoPlayList = ""
                                    var videoThump = ""
                                    isList = false
                                    for (runsData in musicCarouselShelfRendererContents.musicTwoRowItemRenderer.title?.runs!!) {
                                        videoName =  runsData?.text+" "+videoName

                                        if (videoName.length > 1) {
                                            break
                                        }
                                    }

                                    for (thumpNailData in musicCarouselShelfRendererContents.musicTwoRowItemRenderer.thumbnailRenderer?.musicThumbnailRenderer?.thumbnail?.thumbnails!!) {
                                        if (videoThump.isEmpty()) {
                                            videoThump = thumpNailData?.url!!
                                            break
                                        }
                                    }

                                    if (musicCarouselShelfRendererContents.musicTwoRowItemRenderer.thumbnailOverlay?.musicItemThumbnailOverlayRenderer?.content?.musicPlayButtonRenderer?.playNavigationEndpoint?.watchPlaylistEndpoint?.playlistId != null) {
                                        videoPlayList =
                                            musicCarouselShelfRendererContents.musicTwoRowItemRenderer.thumbnailOverlay?.musicItemThumbnailOverlayRenderer?.content?.musicPlayButtonRenderer?.playNavigationEndpoint?.watchPlaylistEndpoint?.playlistId!!
                                    }
                                    if(videoPlayList.length<3)
                                    {
//                                        Log.d("undzi",headingData+"<>"+musicCarouselShelfRendererContents.musicTwoRowItemRenderer.thumbnailOverlay)
                                        isList=true
                                      if(musicCarouselShelfRendererContents.musicTwoRowItemRenderer.thumbnailOverlay?.musicItemThumbnailOverlayRenderer?.content?.musicPlayButtonRenderer?.playNavigationEndpoint?.watchEndpoint?.videoId!=null)
                                      {
                                          videoId=musicCarouselShelfRendererContents.musicTwoRowItemRenderer.thumbnailOverlay?.musicItemThumbnailOverlayRenderer?.content?.musicPlayButtonRenderer?.playNavigationEndpoint?.watchEndpoint?.videoId
                                      }
                                    }

//                                    if(videoPlayList.length<2)
//                                    {
//                                        for (runsData in musicCarouselShelfRendererContents.musicTwoRowItemRenderer.title?.runs!!) {
//                                            if(runsData?.navigationEndpoint?.watchEndpoint?.videoId!=null)
//                                            {
//                                                videoId=runsData?.navigationEndpoint?.watchEndpoint?.videoId
//                                            }
//                                        }
//                                    }
                                    Log.d("finalcheck", "-------start-----")

                                    Log.d("finalcheck", headingData + "<><><" + videoName+"<>"+videoId+"<><>"+videoPlayList)
                                    Log.d("finalcheck", "-------ended-----")

                                    if (videoPlayList != null && videoPlayList.length > 1 || videoId != null && videoId.length >= 11) {
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

            }


            if (paginationHex != null && paginationHex.length > 0) {
                youtubeiHomeFirstResponse = YoutubeiHomeBaseResponse(
                    PaginationContent(
                        paginationId,
                        paginationHex,
                        null
                    ), youtubeMusicHomeDataList
                )
//                Log.d("Tagger", "innn" + youtubeiHomeFirstResponse)

            } else {
                youtubeiHomeFirstResponse =
                    YoutubeiHomeBaseResponse(null, youtubeMusicHomeDataList)
//                Log.d("Tagger", "innn222" + youtubeiHomeFirstResponse)

            }

            if (youtubeiHomeFirstResponse != null) {
//                Log.d(
//                    "Tagger",
//                    "innnsuccc" + youtubeiHomeFirstResponse.homePageContentDataList!!.size
//                )

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