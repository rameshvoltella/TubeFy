package com.ramzmania.tubefy.core.dataformatter.database

import com.ramzmania.tubefy.core.dataformatter.FormattingResult
import com.ramzmania.tubefy.core.dataformatter.UniversalYoutubeDataFormatter
import com.ramzmania.tubefy.data.dto.base.searchformat.TubeFyCoreTypeData
import com.ramzmania.tubefy.database.ActivePlaylist
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DatabaseFormatter<T> @Inject constructor() :
    UniversalYoutubeDataFormatter<T, FormattingResult<List<ActivePlaylist>, Exception>>() {

    override suspend fun runFormatting(input: T): FormattingResult<List<ActivePlaylist>, Exception> {
        val activePlayListList: ArrayList<ActivePlaylist> = ArrayList()

        when (input) {
            is List<*> -> {
                input.forEach { dataValue ->
                    when (dataValue) {
                        is TubeFyCoreTypeData -> {
                            activePlayListList.add(
                                ActivePlaylist(
                                    videoThumbnail = dataValue.videoImage,
                                    videoId = dataValue.videoId,
                                    videoName = dataValue.videoTitle
                                )
                            )
                        }
                        else -> {
                            // Handle other types if needed
                        }
                    }
                }
            }
            else -> {
                return FormattingResult.FAILURE(Exception("Unsupported input type"))
            }
        }

        return if (activePlayListList.isNotEmpty()) {
            FormattingResult.SUCCESS(activePlayListList)
        } else {
            FormattingResult.FAILURE(Exception("No data"))
        }
    }
}
