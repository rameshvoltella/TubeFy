package com.ramzmania.tubefy.core.dataformatter.youtubemusic


import android.util.Log
import com.ramzmania.tubefy.core.dataformatter.FormattingResult
import com.ramzmania.tubefy.core.dataformatter.UniversalYoutubeDataFormatter
import com.ramzmania.tubefy.data.dto.home.HomePageResponse
import com.ramzmania.tubefy.data.dto.home.youtubei.YoutubeiMusicHomeApiResponse

import javax.inject.Inject

class YoutubeMusicYoutubeiDataHomeFormatter@Inject constructor() :
    UniversalYoutubeDataFormatter<YoutubeiMusicHomeApiResponse, FormattingResult<List<HomePageResponse?>, Exception>>() {
    override suspend fun runFormatting(inputData: YoutubeiMusicHomeApiResponse): FormattingResult<List<HomePageResponse?>, Exception> {

//        if (musicCategoryPlayList.size > 0) {
//            return FormattingResult.SUCCESS(musicCategoryPlayList)
//        } else {
//            return FormattingResult.FAILURE(Exception("No category Found"))
//        }

       val visiterData= inputData.responseContext?.visitorData
        Log.d("tadadada",visiterData+"")
        return FormattingResult.FAILURE(Exception("No category Found"))
//        inputData.contents.singleColumnBrowseResultsRenderer.tabs[0].tabRenderer.content.sectionListRenderer.contents[0].musicCarouselShelfRenderer.contents[0].musicResponsiveListItemRenderer.

    }
}