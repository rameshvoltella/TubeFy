package com.ramzmania.tubefy.probe

import com.ramzmania.tubefy.core.extractors.newpipeextractor.PipeDownloader
import okhttp3.OkHttpClient
import org.junit.Assume.assumeTrue
import org.junit.Test
import org.schabi.newpipe.extractor.NewPipe
import org.schabi.newpipe.extractor.ServiceList
import org.schabi.newpipe.extractor.channel.ChannelInfo
import org.schabi.newpipe.extractor.channel.tabs.ChannelTabInfo
import org.schabi.newpipe.extractor.stream.StreamInfo
import org.schabi.newpipe.extractor.stream.StreamInfoItem

/** Manual: channel page data; runs only with env TUBEFY_PROBE=true. */
class ChannelProbe {
    @Test
    fun probe() {
        assumeTrue(System.getenv("TUBEFY_PROBE") == "true")
        NewPipe.init(PipeDownloader(OkHttpClient()))
        val yt = ServiceList.YouTube
        val stream = StreamInfo.getInfo(yt, "https://www.youtube.com/watch?v=0e3GPea1Tyg")
        println("UPLOADER url=${stream.uploaderUrl} name=${stream.uploaderName}")
        val info = ChannelInfo.getInfo(yt, stream.uploaderUrl)
        println("CHANNEL name=${info.name} subs=${info.subscriberCount} avatar=${info.avatars.size} banner=${info.banners.size} verified=${info.isVerified} desc=${info.description?.take(60)?.replace("\n"," ")}")
        println("TABS ${info.tabs.map { it.contentFilters }}")
        val videos = info.tabs.first { it.contentFilters.contains("videos") }
        val tab = ChannelTabInfo.getInfo(yt, videos)
        val first = tab.relatedItems.firstOrNull() as? StreamInfoItem
        println("VIDEOS ${tab.relatedItems.size} next=${tab.hasNextPage()} first='${first?.name}' views=${first?.viewCount} dur=${first?.duration} ago=${first?.textualUploadDate} uploaderUrl=${first?.uploaderUrl}")
        if (tab.hasNextPage()) println("MORE ${ChannelTabInfo.getMoreItems(yt, videos, tab.nextPage).items.size}")
        // a search result item's uploader url
        val item = org.schabi.newpipe.extractor.search.SearchInfo.getInfo(yt, yt.searchQHFactory.fromQuery("arijit singh", listOf("videos"), "")).relatedItems.first() as StreamInfoItem
        println("SEARCHITEM uploaderUrl=${item.uploaderUrl}")
        // a chart (kiosk) item's uploader url
        val kiosk = yt.kioskList.getExtractorById("trending_music", null).also { it.fetchPage() }
        println("KIOSKITEM uploaderUrl=${(kiosk.initialPage.items.first() as StreamInfoItem).uploaderUrl}")
    }
}
