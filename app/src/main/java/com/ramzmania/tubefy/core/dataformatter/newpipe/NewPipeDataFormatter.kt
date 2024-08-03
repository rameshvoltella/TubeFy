package com.ramzmania.tubefy.core.dataformatter.newpipe

import com.ramzmania.tubefy.core.dataformatter.UniversalYoutubeDataFormatter
import com.ramzmania.tubefy.core.dataformatter.YoutubeApiType
import com.ramzmania.tubefy.core.dataformatter.dto.NewPipeSortingInput
import com.ramzmania.tubefy.core.dataformatter.dto.TubeFyCoreFormattedData
import com.ramzmania.tubefy.core.dataformatter.dto.TubeFyCoreTypeData
import com.ramzmania.tubefy.core.dataformatter.dto.TubeFyCoreUniversalData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.schabi.newpipe.extractor.InfoItem
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NewPipeDataFormatter @Inject constructor():
    UniversalYoutubeDataFormatter<NewPipeSortingInput, TubeFyCoreUniversalData>() {
    override suspend fun runFormatting(input: NewPipeSortingInput): TubeFyCoreUniversalData {
        return withContext(Dispatchers.IO){
            val sortedVideoList: ArrayList<TubeFyCoreTypeData> =ArrayList()
            for(newPipeSearchData in input.result)
            {
                sortedVideoList.add(TubeFyCoreTypeData(newPipeSearchData.url,newPipeSearchData.name,newPipeSearchData.thumbnails[0].url))
            }
            TubeFyCoreUniversalData(TubeFyCoreFormattedData(sortedVideoList,input.nextPage),YoutubeApiType.NEW_PIPE_API)
        }

    }
}