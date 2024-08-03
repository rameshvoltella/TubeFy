package com.ramzmania.tubefy.core.dataformatter

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.schabi.newpipe.extractor.InfoItem
import org.schabi.newpipe.extractor.search.SearchInfo
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NewPipeDataFormatter @Inject constructor():UniversalYoutubeDataFormatter<MutableList<InfoItem>,ArrayList<TubeFyBaseData>?>() {
    override suspend fun runFormatting(input: MutableList<InfoItem>): ArrayList<TubeFyBaseData>? {
        return withContext(Dispatchers.IO){
            var sortedVideoList:ArrayList<TubeFyBaseData>?=ArrayList()
            for(newPipeSearchData in input)
            {
                sortedVideoList?.add(TubeFyBaseData(newPipeSearchData.url,newPipeSearchData.name,newPipeSearchData.thumbnails[0].url))
            }
            sortedVideoList
        }

    }
}