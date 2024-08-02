package com.ramzmania.tubefy.core.newpipeextractor

import android.util.Log
import org.schabi.newpipe.extractor.NewPipe
import org.schabi.newpipe.extractor.Page
import org.schabi.newpipe.extractor.search.SearchInfo

class NewPipeCommands {
    fun searchFor(
        serviceId: Int, searchString: String,
        contentFilter: List<String?>,
        sortFilter: String
    ) {
        Log.d("data", ">>>$serviceId>>>>$searchString<>$contentFilter<>$sortFilter")
            checkServiceId(serviceId)
//        return Single.fromCallable {
            SearchInfo.getInfo(
                NewPipe.getService(serviceId),
                NewPipe.getService(serviceId)
                    .searchQHFactory
                    .fromQuery(searchString, contentFilter, sortFilter)
            )
//        }
    }

    fun searchNextPageFor(
        serviceId: Int, searchString: String,
        contentFilter: List<String?>,
        sortFilter: String,page: Page
    ) {
        Log.d("data", ">>>$serviceId>>>>$searchString<>$contentFilter<>$sortFilter")
     checkServiceId(serviceId)
//        return Single.fromCallable {
        SearchInfo.getMoreItems(
            NewPipe.getService(serviceId),
            NewPipe.getService(serviceId)
                .searchQHFactory
                .fromQuery(searchString, contentFilter, sortFilter), page
        )
    }
    private fun checkServiceId(serviceId: Int) {
        require(serviceId != NO_SERVICE_ID) { "serviceId is NO_SERVICE_ID" }
    }
}