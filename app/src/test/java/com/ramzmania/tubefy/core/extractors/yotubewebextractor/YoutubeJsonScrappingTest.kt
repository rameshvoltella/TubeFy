package com.ramzmania.tubefy.core.extractors.yotubewebextractor

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class YoutubeJsonScrappingTest {

    @Test
    fun `extracts and unescapes YouTube Music initial data`() {
        val html = """<script>try {const initialData = [];initialData.push({path: '\/browse', """ +
            """params: JSON.parse('\x7b\x22browseId\x22:\x22FEmusic\x22\x7d'), """ +
            """data: '\x7b\x22title\x22:\x22R\x26B \x5b\x27Soul\x27\x5d\x22,\x22tracking\x22:\x22ab\x3d\x22,\x22path\x22:\x22\/a\\\/b\x22\x7d'});""" +
            """ytcfg.set({'YTMUSIC_INITIAL_DATA': initialData});} catch (e) {}</script>"""

        assertEquals(
            """{"title":"R&B ['Soul']","tracking":"ab=","path":"/a\/b"}""",
            YoutubeJsonScrapping.extractYtMusicInitialData(html)
        )
    }

    @Test
    fun `picks the browse entry when the page pushes several`() {
        val html = """initialData.push({path: '\/guide', params: {}, data: '\x7b\x22guide\x22:1\x7d'});""" +
            """initialData.push({path: '\/browse', params: {}, data: '\x7b\x22contents\x22:2\x7d'});"""

        assertEquals("""{"contents":2}""", YoutubeJsonScrapping.extractYtMusicInitialData(html))
    }

    @Test
    fun `returns null when the page has no initial data`() {
        assertNull(YoutubeJsonScrapping.extractYtMusicInitialData("<html>consent page</html>"))
    }

    @Test
    fun `reads ytInitialData object literal`() {
        val html = """<script>var ytInitialData = {"a":1};</script>"""
        assertEquals("""{"a":1}""", YoutubeJsonScrapping.extractYtInitialData(html))
    }
}
