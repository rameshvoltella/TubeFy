package com.ramzmania.tubefy.probe

import com.ramzmania.tubefy.core.extractors.newpipeextractor.PipeDownloader
import com.ramzmania.tubefy.data.youtube.YouTubeFeed
import com.ramzmania.tubefy.data.youtube.YouTubeFeedSource
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import org.junit.Assume.assumeTrue
import org.junit.Test
import org.schabi.newpipe.extractor.NewPipe

/** Manual live check of every YouTube tab feed and its second page; runs only with env TUBEFY_PROBE=true. */
class YouTubeFeedProbe {
    @Test
    fun probe() = runBlocking {
        assumeTrue(System.getenv("TUBEFY_PROBE") == "true")
        NewPipe.init(PipeDownloader(OkHttpClient()))
        val source = YouTubeFeedSource()
        val feeds = listOf(
            YouTubeFeed.InState("Kerala", "Malayalam"), YouTubeFeed.Trending, YouTubeFeed.Music,
            YouTubeFeed.Mixes("Malayalam"), YouTubeFeed.Live, YouTubeFeed.Gaming, YouTubeFeed.Search("arijit singh"), YouTubeFeed.Channel("https://www.youtube.com/channel/UCX6OQ3DkcsbYNE6H8uQQuVA", "MrBeast")
        )
        try {
            val d = source.details("O5gwxm3NxFU")
            println("DETAILS '${d.title.take(40)}' ch=${d.channel} subs=${d.subscribers} views=${d.views} ago=${d.uploadedAgo} " +
                "avatar=${d.channelAvatar?.take(30)} tags=${d.tags.take(3)} desc=${d.description.take(60).replace("\n", " ")} related=${d.related.size} " +
                "firstRelated='${d.related.firstOrNull()?.title?.take(40)}' playlists=${d.related.count { it.isPlaylist }}")
        } catch (e: Exception) {
            println("DETAILS FAILED: ${e.javaClass.simpleName}: ${e.message?.take(200)}")
        }
        source.channel("https://www.youtube.com/channel/UCX6OQ3DkcsbYNE6H8uQQuVA").let {
            println("CHANNEL ${it.name} subs=${it.subscribers} avatar=${it.avatar != null} banner=${it.banner != null} verified=${it.verified}")
        }
        for (feed in feeds) {
            try {
                val first = source.load(source.start(feed, "IN"))
                val second = first.next?.let { source.load(it) }
                val sample = first.items.firstOrNull()
                println("FEED ${feed.key}: ${first.items.size} + ${second?.items?.size ?: "-"} more; " +
                    "first='${sample?.title?.take(50)}' ch=${sample?.channel} views=${sample?.views} dur=${sample?.durationSeconds} " +
                    "ago=${sample?.uploadedAgo} live=${sample?.isLive} playlist=${sample?.isPlaylist} thumb=${sample?.thumbnail?.take(40)}")
            } catch (e: Exception) {
                println("FEED ${feed.key} FAILED: ${e.javaClass.simpleName}: ${e.message?.take(150)}")
            }
        }
    }
}
