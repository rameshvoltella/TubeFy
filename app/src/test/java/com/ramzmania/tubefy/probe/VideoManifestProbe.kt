package com.ramzmania.tubefy.probe

import com.ramzmania.tubefy.core.extractors.newpipeextractor.PipeDownloader
import com.ramzmania.tubefy.player.VideoManifest
import okhttp3.OkHttpClient
import org.junit.Assume.assumeTrue
import org.junit.Test
import org.schabi.newpipe.extractor.NewPipe
import org.w3c.dom.Element
import javax.xml.parsers.DocumentBuilderFactory

/** Manual: builds real manifests and checks they are valid XML; runs only with env TUBEFY_PROBE=true. */
class VideoManifestProbe {
    @Test
    fun probe() {
        assumeTrue(System.getenv("TUBEFY_PROBE") == "true")
        NewPipe.init(PipeDownloader(OkHttpClient()))
        for (id in listOf("O5gwxm3NxFU", "0e3GPea1Tyg")) {
            val result = VideoManifest.fetch(id)!!
            val doc = DocumentBuilderFactory.newInstance().apply { isNamespaceAware = true }.newDocumentBuilder()
                .parse(result.manifest.byteInputStream())
            val sets = doc.getElementsByTagName("AdaptationSet")
            val summary = (0 until sets.length).map { i ->
                val set = sets.item(i) as Element
                val reps = set.getElementsByTagName("Representation")
                val label = set.getElementsByTagName("Label").item(0)?.textContent
                val role = (set.getElementsByTagName("Role").item(0) as? Element)?.getAttribute("value")
                "${set.getAttribute("contentType")}[${set.getAttribute("lang")}|$label|$role]x${reps.length}" +
                    if (set.getAttribute("contentType") == "video") (0 until reps.length).joinToString(",", " (", ")") { (reps.item(it) as Element).getAttribute("height") } else ""
            }
            println("MANIFEST $id: ${result.manifest.length} chars, ${sets.length} sets, captions=${result.subtitles.size} (${result.subtitles.take(3).joinToString { it.label }})")
            summary.take(6).forEach { println("   $it") }
        }
    }
}
