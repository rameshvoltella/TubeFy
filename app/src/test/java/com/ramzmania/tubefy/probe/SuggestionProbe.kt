package com.ramzmania.tubefy.probe

import com.ramzmania.tubefy.core.extractors.newpipeextractor.PipeDownloader
import okhttp3.OkHttpClient
import org.junit.Assume.assumeTrue
import org.junit.Test
import org.schabi.newpipe.extractor.NewPipe
import org.schabi.newpipe.extractor.ServiceList
import org.schabi.newpipe.extractor.localization.ContentCountry
import org.schabi.newpipe.extractor.localization.Localization

/** Manual: YouTube search suggestions; runs only with env TUBEFY_PROBE=true. */
class SuggestionProbe {
    @Test
    fun probe() {
        assumeTrue(System.getenv("TUBEFY_PROBE") == "true")
        NewPipe.init(PipeDownloader(OkHttpClient()), Localization("en", "IN"), ContentCountry("IN"))
        for (q in listOf("malayala", "arijit", "kerala blasters")) {
            val start = System.currentTimeMillis()
            val list = ServiceList.YouTube.suggestionExtractor.suggestionList(q)
            println("SUGGEST '$q' (${System.currentTimeMillis() - start} ms): $list")
        }
    }
}
