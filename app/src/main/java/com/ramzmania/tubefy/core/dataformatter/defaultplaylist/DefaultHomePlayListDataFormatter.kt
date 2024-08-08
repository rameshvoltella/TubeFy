package com.ramzmania.tubefy.core.dataformatter.defaultplaylist

import com.ramzmania.tubefy.core.dataformatter.FormattingResult
import com.ramzmania.tubefy.core.dataformatter.UniversalYoutubeDataFormatter
import com.ramzmania.tubefy.data.dto.base.BaseContentData
import com.ramzmania.tubefy.data.dto.home.CellType
import com.ramzmania.tubefy.data.dto.home.HomePageResponse
import com.ramzmania.tubefy.data.dto.home.defaultmodel.DefaultBaseModel
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DefaultHomePlayListDataFormatter @Inject constructor()  :
    UniversalYoutubeDataFormatter<DefaultBaseModel?, FormattingResult<List<HomePageResponse?>, Exception>>() {
    override suspend fun runFormatting(input: DefaultBaseModel?): FormattingResult<List<HomePageResponse?>, Exception> {
        val videoSortedList = mutableListOf<HomePageResponse>()
        val videoSortedListWithPlaylist = mutableListOf<BaseContentData>()

        try{
            input?.categoryData?.playlistCategories?.forEach {
                println("Title: ${it.title}, Description: ${it.description}")
                videoSortedListWithPlaylist.add(BaseContentData("",it.title,"",it.title))
            }
            videoSortedList.add(HomePageResponse(CellType.PLAYLIST_ONLY,videoSortedListWithPlaylist))
            if(input?.composersList!=null)
            {
                input.composersList.forEach {
                    val composerSortedListWithPlaylist = mutableListOf<BaseContentData>()

                    it?.composers?.forEach {
                        composerSortedListWithPlaylist.add(BaseContentData("",it.name,"",it.name))
                    }
                    videoSortedList.add(HomePageResponse(CellType.PLAYLIST_ONLY,composerSortedListWithPlaylist))

                }
            }
           return FormattingResult.SUCCESS(videoSortedList)
        }
        catch (ex:Exception)
        {
            return FormattingResult.FAILURE(Exception("ERROR IN MUSIC HOME PAGE SORT"))

        }
    }
}