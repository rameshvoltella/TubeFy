package com.ramzmania.tubefy.probe

import com.ramzmania.tubefy.core.extractors.newpipeextractor.PipeDownloader
import com.ramzmania.tubefy.core.lyrics.SongInfoSource
import com.ramzmania.tubefy.core.youtube.YouTubeStreams
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import org.junit.Assume.assumeTrue
import org.junit.Test
import org.schabi.newpipe.extractor.NewPipe

/** Manual: the Lyrics tab's data for real songs; runs only with env TUBEFY_PROBE=true. */
class SongInfoProbe {
    @Test
    fun probe() = runBlocking {
        assumeTrue(System.getenv("TUBEFY_PROBE") == "true")
        val http = OkHttpClient()
        NewPipe.init(PipeDownloader(http))
        val source = SongInfoSource(http)
        // Malare: looked up as playback would (so its description credits are available)
        val malare = YouTubeStreams.info("-GyOKmoLbng")
        println("DESC type=${malare.description?.type}: " + malare.description?.content?.take(700)?.replace("\n", " ⏎ "))
        if (System.getenv("TUBEFY_PROBE_DESC_ONLY") == "true") return@runBlocking
        for ((id, durationMs) in listOf("-GyOKmoLbng" to malare.duration * 1000, "ao_uP84V6oE" to 0L)) {
            val info = source.load(id, "?", "?", durationMs)
            println("INFO $id: ${info.details}")
            println("   LYRICS: synced=${info.lyrics?.synced} source=${info.lyrics?.source} lines=${info.lyrics?.lines?.size} first=${info.lyrics?.lines?.firstOrNull { it.text.isNotBlank() }}")
        }
    }
}
