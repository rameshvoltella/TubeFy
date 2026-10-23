package com.ramzmania.tubefy.data

import com.ramzmania.tubefy.data.youtube.StateLanguages
import com.ramzmania.tubefy.data.youtube.YouTubeFeedSource
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class YouTubeFeedTest {

    @Test
    fun `charts are interleaved one from each in turn`() {
        val result = YouTubeFeedSource.interleave(listOf(listOf("a1", "a2", "a3"), listOf("b1"), listOf("c1", "c2")))
        assertEquals(listOf("a1", "b1", "c1", "a2", "c2", "a3"), result)
    }

    @Test
    fun `video and playlist ids are read from urls`() {
        assertEquals("dQw4w9WgXcQ", YouTubeFeedSource.videoId("https://www.youtube.com/watch?v=dQw4w9WgXcQ"))
        assertEquals("dQw4w9WgXcQ", YouTubeFeedSource.videoId("https://www.youtube.com/live/dQw4w9WgXcQ"))
        assertEquals("PLabc_123-x", YouTubeFeedSource.playlistId("https://www.youtube.com/playlist?list=PLabc_123-x"))
        assertNull(YouTubeFeedSource.videoId("https://www.youtube.com/channel/UC123"))
    }

    @Test
    fun `states map to their language, matching geocoder names loosely`() {
        assertEquals("Malayalam", StateLanguages.languageOf("Kerala"))
        assertEquals("Malayalam", StateLanguages.languageOf("kerala"))
        assertEquals("Tamil", StateLanguages.languageOf("State of Tamil Nadu"))
        assertEquals("Kerala", StateLanguages.canonical("kerala"))
        assertNull(StateLanguages.languageOf("California"))
        assertNull(StateLanguages.languageOf(null))
    }
}

class YouTubeDescriptionTest {
    @Test
    fun `html descriptions become plain text with line breaks`() {
        val html = "Song by <a href=\"x\">Arijit</a><br>Lyrics &amp; music<br/>Tom &quot;&#39;s"
        assertEquals("Song by Arijit\nLyrics & music\nTom \"'s", YouTubeFeedSource.htmlToText(html))
    }
}

class YouTubeTimeAgoTest {
    private val now = 1_800_000_000_000L
    private fun ago(seconds: Long) = YouTubeFeedSource.timeAgo(now - seconds * 1000, now)

    @Test
    fun `upload dates read like YouTube`() {
        assertEquals("just now", ago(30))
        assertEquals("1 hour ago", ago(3_600))
        assertEquals("3 days ago", ago(3 * 86_400))
        assertEquals("2 weeks ago", ago(15 * 86_400))
        assertEquals("1 month ago", ago(40 * 86_400))
        assertEquals("2 years ago", ago(2 * 366 * 86_400L))
    }
}

class VideoAutoplayTest {
    private fun video(id: String, live: Boolean = false, playlist: Boolean = false) =
        com.ramzmania.tubefy.data.youtube.YouTubeItem(id, "", id, "", "", isLive = live, isPlaylist = playlist)

    @Test
    fun `next is the first related video not played yet, skipping playlists and live`() {
        val related = listOf(video("p", playlist = true), video("l", live = true), video("a"), video("b"))
        assertEquals("a", com.ramzmania.tubefy.ui.components.screen.youtube.nextRelated(related, emptyList())?.id)
        assertEquals("b", com.ramzmania.tubefy.ui.components.screen.youtube.nextRelated(related, listOf("a"))?.id)
        assertNull(com.ramzmania.tubefy.ui.components.screen.youtube.nextRelated(related, listOf("a", "b")))
    }
}

class VideoEndGuardTest {
    @Test
    fun `acts only in the last moments of a non-live video`() {
        val end = com.ramzmania.tubefy.ui.components.screen.youtube.END_GUARD_MS
        assertEquals(true, com.ramzmania.tubefy.ui.components.screen.youtube.isAtEnd(60_000 - end, 60_000, isLive = false))
        assertEquals(false, com.ramzmania.tubefy.ui.components.screen.youtube.isAtEnd(60_000 - end - 1, 60_000, isLive = false))
        assertEquals(false, com.ramzmania.tubefy.ui.components.screen.youtube.isAtEnd(59_900, 60_000, isLive = true))
        assertEquals(false, com.ramzmania.tubefy.ui.components.screen.youtube.isAtEnd(0, 0, isLive = false))
    }
}

class TvScrubTest {
    @Test
    fun `holding the key speeds up seeking`() {
        val hour = 3_600_000L
        assertEquals(10_000L, com.ramzmania.tubefy.ui.tv.scrubStepMs(0, hour))
        assertEquals(30_000L, com.ramzmania.tubefy.ui.tv.scrubStepMs(6, hour))
        assertEquals(90_000L, com.ramzmania.tubefy.ui.tv.scrubStepMs(20, hour))      // 1/40 of an hour
        assertEquals(60_000L, com.ramzmania.tubefy.ui.tv.scrubStepMs(20, 600_000L))  // at least a minute
    }
}

class VideoQualityTest {
    private val pick = com.ramzmania.tubefy.player.VideoQuality::bestIndex

    @Test
    fun `best quality is the tallest supported one that fits the screen`() {
        val heights = listOf(144, 360, 720, 1080, 2160)
        val all = List(5) { true }
        assertEquals(3, pick(heights, all, 1080))          // 1080p phone: 1080p, not 4K
        assertEquals(4, pick(heights, all, 2160))          // 4K TV
        assertEquals(2, pick(heights, listOf(true, true, true, false, false), 1080)) // 1080p not decodable
        assertEquals(0, pick(listOf(480, 720), listOf(true, true), 240)) // nothing fits: the smallest
        assertNull(pick(heights, List(5) { false }, 1080))
    }
}

class YouTubeAccessTest {
    private val access = com.ramzmania.tubefy.core.youtube.YouTubeAccess

    @Test
    fun `a not-a-bot refusal is recognised, also when wrapped`() {
        val refusal = org.schabi.newpipe.extractor.exceptions.SignInConfirmNotBotException("Sign in to confirm that you're not a bot")
        assertEquals(true, access.isBlock(refusal))
        assertEquals(true, access.isBlock(RuntimeException("wrapped", refusal)))
        assertEquals(false, access.isBlock(java.io.IOException("timeout")))
    }

    @Test
    fun `the block message shows only after a refusal`() {
        access.markOk()
        assertEquals("fallback", access.messageOr("fallback"))
        access.check(org.schabi.newpipe.extractor.exceptions.SignInConfirmNotBotException("not a bot"))
        assertEquals(access.BLOCKED_MESSAGE, access.messageOr("fallback"))
        access.markOk()
    }
}
