package com.ramzmania.tubefy.core

import com.ramzmania.tubefy.core.download.SongBook
import com.ramzmania.tubefy.core.download.SongBookEntry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SongBookTest {
    private fun entry(id: String, title: String = id, folder: String = "f", file: String = "$id.m4a") =
        SongBookEntry(videoId = id, title = title, folder = folder, fileName = file, savedAt = 1_800_000_000_000)

    @Test
    fun `a new save goes first and replaces an earlier save of the same song`() {
        val list = SongBook.add(SongBook.add(listOf(entry("a")), entry("b")), entry("a", title = "A again"))
        assertEquals(listOf("a", "b"), list.map { it.videoId })
        assertEquals("A again", list.first().title)
        // the same song in another folder is a separate entry
        assertEquals(3, SongBook.add(list, entry("a", folder = "other")).size)
    }

    @Test
    fun `the page lists every song with details and lyrics, safely escaped`() {
        val malare = entry("m1", "Malare").copy(movie = "Premam", singers = "Vijay Yesudas", year = "2015", lyrics = "line 1\nline <2>", lyricsSource = "LRCLIB")
        val html = SongBook.render(listOf(malare, entry("x", "Gone song")), presentFiles = setOf("m1.m4a"), now = 1_800_000_000_000)
        assertTrue(html.contains("2 songs"))
        assertTrue(html.contains("href=\"#song-m1\""))
        assertTrue(html.contains("<dt>Movie</dt><dd>Premam</dd>"))
        assertTrue(html.contains("line 1\nline &lt;2&gt;"))
        assertFalse(html.contains("line <2>"))
        assertTrue(html.contains("Gone song <span class=\"gone\">file removed</span>"))
        assertFalse(html.contains("Malare <span class=\"gone\">"))
    }

    @Test
    fun `"Both" shows the original lyrics, then English letters`() {
        val both = entry("m1", "Malare").copy(language = "Malayalam", lyrics = "തെളിമാനം", lyricsEnglish = "thelimaanam")
        val html = SongBook.render(listOf(both), presentFiles = null, now = 0)
        val original = html.indexOf("<h3>Malayalam</h3>")
        val english = html.indexOf("<h3>English letters</h3>")
        assertTrue(original in 0 until english)
        assertTrue(html.contains("thelimaanam"))
    }

    @Test
    fun `language comes from the lyrics' script`() {
        assertEquals("Malayalam", SongBook.languageOf("തെളിമാനം മഴവില്ലിൻ നിറമണിയും നേരം"))
        assertEquals("Hindi", SongBook.languageOf("मुझको इतना बताए कोई कैसे तुझसे"))
        assertEquals("", SongBook.languageOf("Hum tere bin ab reh nahi sakte"))
    }
}
