package com.ramzmania.tubefy.core.extractors.newpipeextractor

import android.util.Log
import com.ramzmania.tubefy.core.YoutubeCoreConstant.NO_SERVICE_ID
import org.schabi.newpipe.extractor.InfoItem
import org.schabi.newpipe.extractor.ListExtractor
import org.schabi.newpipe.extractor.NewPipe
import org.schabi.newpipe.extractor.Page
import org.schabi.newpipe.extractor.channel.ChannelInfo
import org.schabi.newpipe.extractor.playlist.PlaylistInfo
import org.schabi.newpipe.extractor.search.SearchInfo

fun newPipeSearchFor(
    serviceId: Int, searchString: String,
    contentFilter: List<String?>,
    sortFilter: String
): SearchInfo {
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


fun newPipePlayListData(
    playListUrl: String
): PlaylistInfo {

    return PlaylistInfo.getInfo(playListUrl)
//        }
}

fun newPipeChannelPlayListData(
    playListUrl: String
): ChannelInfo {

    return ChannelInfo.getInfo(playListUrl)
}

fun newPipeSearchNextPageFor(
    serviceId: Int, searchString: String,
    contentFilter: List<String?>,
    sortFilter: String, page: Page
): ListExtractor.InfoItemsPage<InfoItem> {
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
