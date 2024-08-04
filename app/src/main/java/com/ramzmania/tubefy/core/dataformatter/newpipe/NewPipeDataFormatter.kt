package com.ramzmania.tubefy.core.dataformatter.newpipe

import com.ramzmania.tubefy.core.dataformatter.FormattingResult
import com.ramzmania.tubefy.core.dataformatter.UniversalYoutubeDataFormatter
import com.ramzmania.tubefy.core.dataformatter.YoutubeApiType
import com.ramzmania.tubefy.core.dataformatter.dto.NewPipeSortingData
import com.ramzmania.tubefy.core.dataformatter.dto.TubeFyCoreFormattedData
import com.ramzmania.tubefy.core.dataformatter.dto.TubeFyCoreTypeData
import com.ramzmania.tubefy.core.dataformatter.dto.TubeFyCoreUniversalData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NewPipeDataFormatter @Inject constructor():
    UniversalYoutubeDataFormatter<NewPipeSortingData, FormattingResult<TubeFyCoreUniversalData,Exception>>() {
    override suspend fun runFormatting(input: NewPipeSortingData): FormattingResult<TubeFyCoreUniversalData,Exception> {
        return withContext(Dispatchers.IO){
            try {
                val sortedVideoList: ArrayList<TubeFyCoreTypeData> = ArrayList()
                for (newPipeSearchData in input.result) {
                    sortedVideoList.add(
                        TubeFyCoreTypeData(
                            newPipeSearchData.url,
                            newPipeSearchData.name,
                            newPipeSearchData.thumbnails[0].url
                        )
                    )
                }

                FormattingResult.SUCCESS( TubeFyCoreUniversalData(
                    TubeFyCoreFormattedData(sortedVideoList, input.nextPage),
                    YoutubeApiType.NEW_PIPE_API
                ))

            }catch (ex:Exception)
            {
                FormattingResult.FAILURE(Exception("Unable to get Youtube URL"))
            }
        }

    }
}