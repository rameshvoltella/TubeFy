package com.ramzmania.tubefy.core.dataformatter.newpipe

import com.ramzmania.tubefy.core.dataformatter.FormattingResult
import com.ramzmania.tubefy.core.dataformatter.UniversalYoutubeDataFormatter
import com.ramzmania.tubefy.core.dataformatter.YoutubeApiType
import com.ramzmania.tubefy.data.dto.base.searchformat.NewPipeSortingData
import com.ramzmania.tubefy.data.dto.base.searchformat.TubeFyCoreFormattedData
import com.ramzmania.tubefy.data.dto.base.searchformat.TubeFyCoreTypeData
import com.ramzmania.tubefy.data.dto.base.searchformat.TubeFyCoreUniversalData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.schabi.newpipe.extractor.InfoItem
import org.schabi.newpipe.extractor.stream.StreamInfoItem
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NewPipeDataFormatter <T> @Inject constructor():
    UniversalYoutubeDataFormatter<NewPipeSortingData<T>, FormattingResult<TubeFyCoreUniversalData,Exception>>() {
    override suspend fun runFormatting(input: NewPipeSortingData<T>): FormattingResult<TubeFyCoreUniversalData,Exception> {
        return withContext(Dispatchers.IO){
            try {
                val sortedVideoList: ArrayList<TubeFyCoreTypeData> = ArrayList()
                for (newPipeSearchData in input.result) {
                    when (newPipeSearchData) {
                        is StreamInfoItem -> {
                            sortedVideoList.add(
                                TubeFyCoreTypeData(
                                    newPipeSearchData.url,
                                    newPipeSearchData.name,
                                    newPipeSearchData.thumbnails[0].url
                                )
                            )
                        }
                        is InfoItem -> {
                            sortedVideoList.add(
                                TubeFyCoreTypeData(
                                    newPipeSearchData.url,
                                    newPipeSearchData.name,
                                    newPipeSearchData.thumbnails[0].url
                                )
                            )
                        }

                        // Handle other types if necessary
                        else -> {
                            // Handle cases where T is not InfoItem
                        }
                    }
                }
                FormattingResult.SUCCESS( TubeFyCoreUniversalData(
                    TubeFyCoreFormattedData(sortedVideoList, input.nextPage),
                    YoutubeApiType.NEW_PIPE_API
                )
                )

            }catch (ex:Exception)
            {
                FormattingResult.FAILURE(Exception("Unable to get Youtube URL"))
            }
        }

    }
}