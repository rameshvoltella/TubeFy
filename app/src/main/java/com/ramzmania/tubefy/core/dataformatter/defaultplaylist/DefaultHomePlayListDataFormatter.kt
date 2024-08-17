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
//                println("Title: ${it.title}, Description: ${it.description}")
//                if(it.playlist.equals("non")) {
                    videoSortedListWithPlaylist.add(
                        BaseContentData(
                            it.title,
                            "PLAYLIST-ID-YT${it.title}",
                            it.image,
                            it.title
                        )
                    )
//                }else
//                {
//                    videoSortedListWithPlaylist.add(
//                        BaseContentData(it.playlist,
//                            it.playlist,
//                            "",
//                            it.title
//                        )
//                    )
//                }
            }
            videoSortedList.add(HomePageResponse("Trending",CellType.PLAYLIST_ONLY,videoSortedListWithPlaylist))
            if(input?.composersList!=null)
            {
                input.composersList.forEach {
                    val composerSortedListWithPlaylist = mutableListOf<BaseContentData>()

                    it?.composers?.forEach {
                        composerSortedListWithPlaylist.add(BaseContentData("","PLAYLIST-ID-${it.name}",""+it.image,it.name))
                    }
                    videoSortedList.add(HomePageResponse(it?.heading!!,CellType.PLAYLIST_ONLY,composerSortedListWithPlaylist))

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