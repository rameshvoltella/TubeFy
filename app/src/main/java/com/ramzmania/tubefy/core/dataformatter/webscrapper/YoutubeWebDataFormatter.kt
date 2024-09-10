package com.ramzmania.tubefy.core.dataformatter.webscrapper

import android.util.Log
import com.ramzmania.tubefy.core.dataformatter.FormattingResult
import com.ramzmania.tubefy.core.dataformatter.UniversalYoutubeDataFormatter
import com.ramzmania.tubefy.core.dataformatter.YoutubeApiType
import com.ramzmania.tubefy.data.dto.base.searchformat.TubeFyCoreFormattedData
import com.ramzmania.tubefy.data.dto.base.searchformat.TubeFyCoreTypeData
import com.ramzmania.tubefy.data.dto.base.searchformat.TubeFyCoreUniversalData
import com.ramzmania.tubefy.data.dto.youtubestripper.ApiResponse
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class YoutubeWebDataFormatter@Inject constructor() : UniversalYoutubeDataFormatter<ApiResponse, FormattingResult<TubeFyCoreUniversalData, Exception>>() {
    override suspend fun runFormatting(input: ApiResponse): FormattingResult<TubeFyCoreUniversalData, Exception> {
        try {
            var sortedVideoList: ArrayList<TubeFyCoreTypeData> = ArrayList()
            for (contents in input.contents.sectionListRenderer.contents) {
                var videoName = ""
                var videoId = ""
                var videoImage = ""
                if (contents.itemSectionRenderer != null) {

                    for (contentsBaseRenderer in contents.itemSectionRenderer.contentsBaseRenderer) {

                        if (contentsBaseRenderer != null) {

                            if (contentsBaseRenderer.reelShelfRenderer != null) {
                                if (contentsBaseRenderer.reelShelfRenderer.shortsList != null) {

                                    for (shortData in contentsBaseRenderer.reelShelfRenderer.shortsList) {
                                        if (shortData.trackingParams != null) {

                                            shortData.trackingParams.reelsOnTap.innerTubeCommand.reelWatchEndpoint.videoId
                                            videoName = shortData.trackingParams.accessibilityText
                                            if (shortData.trackingParams.reelsOnTap?.innerTubeCommand?.reelWatchEndpoint != null) {
                                                videoId =
                                                    shortData.trackingParams.reelsOnTap.innerTubeCommand.reelWatchEndpoint.videoId


                                            }
                                            if (shortData.trackingParams.reelsThumpNail?.thumpList != null) {
                                                for (thumpList in shortData.trackingParams.reelsThumpNail?.thumpList) {
                                                    videoImage = thumpList.reelShortsThumpNailUrl
                                                }
                                            }
                                        }
                                    }
                                    sortedVideoList?.add(
                                        TubeFyCoreTypeData(
                                            videoId,
                                            videoName,
                                            videoImage
                                        )
                                    )
                                }

                            } else if (contentsBaseRenderer.videoWithContextRenderer != null) {

                                videoId = contentsBaseRenderer.videoWithContextRenderer.videoId
                                if (contentsBaseRenderer?.videoWithContextRenderer?.headline?.accessibility?.accessibilityData?.label != null) {
                                    videoName =
                                        contentsBaseRenderer.videoWithContextRenderer.headline.accessibility.accessibilityData.label
                                }
                                if (contentsBaseRenderer.videoWithContextRenderer.thumbnail != null && contentsBaseRenderer.videoWithContextRenderer.thumbnail.thumbnails != null) {
                                    for (thumbnails in contentsBaseRenderer.videoWithContextRenderer.thumbnail.thumbnails) {
                                        videoImage = thumbnails.reelShortsThumpNailUrl
                                    }
                                }
                                sortedVideoList?.add(
                                    TubeFyCoreTypeData(
                                        videoId,
                                        videoName,
                                        videoImage
                                    )
                                )


                            }
                        }
                    }
                }

            }
            if (sortedVideoList.size > 0) {
                return FormattingResult.SUCCESS(
                    TubeFyCoreUniversalData(
                        TubeFyCoreFormattedData(sortedVideoList),
                        YoutubeApiType.WEB_SCRAPPER
                    )
                )
            } else {
                return FormattingResult.FAILURE(Exception("Unable to get Youtube URL"))
            }
        }catch (ex:Exception)
        {
            return FormattingResult.FAILURE(Exception("Unable to get Youtube URL"))

        }
    }
}