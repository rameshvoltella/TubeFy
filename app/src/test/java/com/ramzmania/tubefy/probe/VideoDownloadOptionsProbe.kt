package com.ramzmania.tubefy.probe

import com.ramzmania.tubefy.core.download.VideoDownloads
import com.ramzmania.tubefy.core.extractors.newpipeextractor.PipeDownloader
import com.ramzmania.tubefy.core.youtube.YouTubeStreams
import okhttp3.OkHttpClient
import org.junit.Assume.assumeTrue
import org.junit.Test
import org.schabi.newpipe.extractor.NewPipe

/** Manual: the Download sheet's choices for a real video; runs only with env TUBEFY_PROBE=true. */
class VideoDownloadOptionsProbe {
    @Test
    fun probe() {
        assumeTrue(System.getenv("TUBEFY_PROBE") == "true")
        NewPipe.init(PipeDownloader(OkHttpClient()))
        val (video, audio) = VideoDownloads.options(YouTubeStreams.info("0e3GPea1Tyg"))
        video.forEach { println("VIDEO ${it.label} ${it.detail} ${VideoDownloads.sizeLabel(it.sizeBytes)} sound=${it.withSound}") }
        println("AUDIO ${audio.size} options; first: " + audio.take(6).joinToString(" | ") { "${it.label} ${it.detail} ${VideoDownloads.sizeLabel(it.sizeBytes)}" })
    }
}
