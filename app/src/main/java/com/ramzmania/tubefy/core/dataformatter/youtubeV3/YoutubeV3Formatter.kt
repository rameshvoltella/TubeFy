package com.ramzmania.tubefy.core.dataformatter.youtubeV3

import android.util.Log
import com.ramzmania.tubefy.core.dataformatter.FormattingResult
import com.ramzmania.tubefy.core.dataformatter.UniversalYoutubeDataFormatter
import com.ramzmania.tubefy.core.dataformatter.YoutubeApiType
import com.ramzmania.tubefy.data.dto.base.searchformat.TubeFyCoreFormattedData
import com.ramzmania.tubefy.data.dto.base.searchformat.TubeFyCoreTypeData
import com.ramzmania.tubefy.data.dto.base.searchformat.TubeFyCoreUniversalData
import com.ramzmania.tubefy.data.dto.youtubeV3.YoutubeSearchResponse
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class YoutubeV3Formatter@Inject constructor() :
    UniversalYoutubeDataFormatter<YoutubeSearchResponse, FormattingResult<TubeFyCoreUniversalData, Exception>>() {
    override suspend fun runFormatting(input: YoutubeSearchResponse): FormattingResult<TubeFyCoreUniversalData, Exception> {
        return if (input != null) {

            try {
                val sortedVideoList: ArrayList<TubeFyCoreTypeData> = ArrayList()

                for (youtubeV3Data in input.items) {

                    sortedVideoList.add(
                        TubeFyCoreTypeData(
                            youtubeV3Data.id.videoId,
                            youtubeV3Data.snippet.title,
                            youtubeV3Data.snippet.thumbnails.medium.url
                        )
                    )
                }

                FormattingResult.SUCCESS(
                    TubeFyCoreUniversalData(
                        TubeFyCoreFormattedData(
                            sortedVideoList,
                            youtubeV3NextToken = input.nextPageToken
                        ), YoutubeApiType.YOUTUBE_V3_API
                    )
                )
            } catch (ex: Exception) {

                FormattingResult.FAILURE(Exception("Unable to get Youtube URL"))

            }
        } else {

            FormattingResult.FAILURE(Exception("Unable to get Youtube URL"))
        }
    }
}