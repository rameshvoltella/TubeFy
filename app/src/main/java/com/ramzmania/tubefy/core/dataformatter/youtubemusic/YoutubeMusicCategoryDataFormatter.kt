package com.ramzmania.tubefy.core.dataformatter.youtubemusic

import android.util.Log
import com.ramzmania.tubefy.core.dataformatter.FormattingResult
import com.ramzmania.tubefy.core.dataformatter.UniversalYoutubeDataFormatter
import com.ramzmania.tubefy.data.dto.base.BaseContentData
import com.ramzmania.tubefy.data.dto.base.playlist.PlayListCategory
import com.ramzmania.tubefy.data.dto.base.playlist.PlayListData
import com.ramzmania.tubefy.data.dto.home.CellType
import com.ramzmania.tubefy.data.dto.home.HomePageResponse
import com.ramzmania.tubefy.data.dto.youtubemusic.category.YtMusicCategoryBase
import com.ramzmania.tubefy.data.dto.youtubemusic.category.YtMusicCategoryContent
import com.ramzmania.tubefy.data.dto.youtubestripper.MusicHomeResponse2
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class YoutubeMusicCategoryDataFormatter @Inject constructor()  :
    UniversalYoutubeDataFormatter<YtMusicCategoryBase, FormattingResult<List<PlayListCategory?>, Exception>>() {
    override suspend fun runFormatting(inputData: YtMusicCategoryBase): FormattingResult<List<PlayListCategory?>, Exception> {
        val musicCategoryList = mutableListOf<PlayListCategory>()


        inputData.YtMusicCategoryContent?.singleColumnBrowseResultsRenderer.let {
            inputData.YtMusicCategoryContent?.singleColumnBrowseResultsRenderer?.tabs.let {
                for (tabs in inputData.YtMusicCategoryContent?.singleColumnBrowseResultsRenderer?.tabs!!) {
                    var playListCategoryId=""
                    var playListBrowserId=""
                    var playlistName=""
                    var stripCode=0L;
                    tabs.tabRenderer.let {tabRenderer ->
                        for (sectionListContents in tabRenderer?.content?.sectionListRenderer?.contents!!) {
                            if(sectionListContents.gridRenderer!=null) {
                                for (gridRendererItems in sectionListContents.gridRenderer?.items!!) {
                                    if (gridRendererItems.musicNavigationButtonRenderer?.buttonText?.runs?.size!! > 0) {
                                        playlistName =
                                            gridRendererItems.musicNavigationButtonRenderer?.buttonText?.runs[0].text!!
                                    }
                                    playListBrowserId =
                                        gridRendererItems.musicNavigationButtonRenderer.clickCommand?.browseEndpoint?.browseId!!
                                    playListCategoryId =
                                        gridRendererItems.musicNavigationButtonRenderer.clickCommand?.browseEndpoint?.params!!
                                    if(gridRendererItems.musicNavigationButtonRenderer.solid!=null)
                                    {
                                        if(gridRendererItems.musicNavigationButtonRenderer.solid.leftStripeColor!=0L)
                                        {
                                            stripCode=gridRendererItems.musicNavigationButtonRenderer.solid.leftStripeColor
                                        }
                                    }
                                    musicCategoryList.add(
                                        PlayListCategory(
                                            playListCategoryId,
                                            playlistName,
                                            playListBrowserId,stripCode
                                        )
                                    )

                                }

                            }

                        }


                    }

                }
            }
        }
if(musicCategoryList.size>0) {
    return FormattingResult.SUCCESS(musicCategoryList)
}else
{
    return FormattingResult.FAILURE(Exception("No category Found"))
}

    }
}