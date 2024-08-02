package com.ramzmania.tubefy.core.newpipeextractor

import android.util.Log
import com.ramzmania.tubefy.core.YoutubeCoreConstant.NO_SERVICE_ID
import org.schabi.newpipe.extractor.InfoItem
import org.schabi.newpipe.extractor.ListExtractor
import org.schabi.newpipe.extractor.NewPipe
import org.schabi.newpipe.extractor.Page
import org.schabi.newpipe.extractor.search.SearchInfo

    fun newPipeSearchFor(
        serviceId: Int, searchString: String,
        contentFilter: List<String?>,
        sortFilter: String
    ):SearchInfo {
        Log.d("data", ">>>$serviceId>>>>$searchString<>$contentFilter<>$sortFilter")
            checkServiceId(serviceId)
//        return Single.fromCallable {
           return SearchInfo.getInfo(
                NewPipe.getService(serviceId),
                NewPipe.getService(serviceId)
                    .searchQHFactory
                    .fromQuery(searchString, contentFilter, sortFilter)
            )
//        }
    }

    fun newPipeSearchNextPageFor(
        serviceId: Int, searchString: String,
        contentFilter: List<String?>,
        sortFilter: String,page: Page
    ): ListExtractor.InfoItemsPage<InfoItem> {
        Log.d("data", ">>>$serviceId>>>>$searchString<>$contentFilter<>$sortFilter")
     checkServiceId(serviceId)
//        return Single.fromCallable {
       return SearchInfo.getMoreItems(
            NewPipe.getService(serviceId),
            NewPipe.getService(serviceId)
                .searchQHFactory
                .fromQuery(searchString, contentFilter, sortFilter), page
        )
    }
    private fun checkServiceId(serviceId: Int) {
        require(serviceId != NO_SERVICE_ID) { "serviceId is NO_SERVICE_ID" }
    }
