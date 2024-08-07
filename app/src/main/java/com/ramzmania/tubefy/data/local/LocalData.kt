package com.ramzmania.tubefy.data.local

import com.ramzmania.tubefy.core.dataformatter.FormattingResult
import com.ramzmania.tubefy.data.dto.searchformat.TubeFyCoreUniversalData
import com.ramzmania.tubefy.core.dataformatter.webscrapper.YoutubeWebDataFormatter
import com.ramzmania.tubefy.core.dataformatter.youtubemusic.YoutubeMusicHomeDataFormatter
import com.ramzmania.tubefy.data.Resource
import com.ramzmania.tubefy.data.dto.home.HomePageResponse
import com.ramzmania.tubefy.data.dto.youtubestripper.ApiResponse
import com.ramzmania.tubefy.data.dto.youtubestripper.MusicHomeResponse2
import com.ramzmania.tubefy.errors.YOUTUBE_V3_SEARCH_ERROR
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class LocalData @Inject
constructor(
    private val youtubeWebDataFormatter: YoutubeWebDataFormatter,private val youtubeMusicHomeDataFormatter: YoutubeMusicHomeDataFormatter
) : LocalDataSource {
    override suspend fun manipulateYoutubeSearchStripData(youtubeJsonScrapping: ApiResponse): Resource<TubeFyCoreUniversalData> {
        return withContext(Dispatchers.IO)
        {
            val result=youtubeWebDataFormatter.run(youtubeJsonScrapping)
             when(result)
            {
                is FormattingResult.SUCCESS->{  Resource.Success(result.data)
                }
                is FormattingResult.FAILURE->{Resource.DataError(YOUTUBE_V3_SEARCH_ERROR)}

            }

        }

//        return if (sortedVideoDataList != null) {
//            Resource.Success(BasicResponse(sortedVideoDataList!!))
//
//        } else {
//            Resource.DataError(401)
//        }

    }

    override suspend fun manipulateYoutubeMusicHomeStripData(musicHomeResponse: MusicHomeResponse2): Resource<List<HomePageResponse?>> {
        return withContext(Dispatchers.IO)
        {
            val result=youtubeMusicHomeDataFormatter.run(musicHomeResponse)
            when(result)
            {
                is FormattingResult.SUCCESS->{  Resource.Success(result.data)
                }
                is FormattingResult.FAILURE->{Resource.DataError(YOUTUBE_V3_SEARCH_ERROR)}

            }

        }
    }

}