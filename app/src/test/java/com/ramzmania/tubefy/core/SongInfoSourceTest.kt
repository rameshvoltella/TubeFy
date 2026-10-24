package com.ramzmania.tubefy.core

import com.ramzmania.tubefy.core.lyrics.LyricLine
import com.ramzmania.tubefy.core.lyrics.Lyrics
import com.ramzmania.tubefy.core.lyrics.SongInfoSource
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test

class SongInfoSourceTest {

    @Test
    fun `timed lyrics are parsed and ordered`() {
        val lines = SongInfoSource.parseLrc("[00:12.50] Thelimaanam\n[01:02.3] Malare\n[00:05.00][00:30.00] Chorus\nno stamp")
        assertEquals(listOf(5_000L, 12_500L, 30_000L, 62_300L), lines.map { it.timeMs })
        assertEquals(listOf("Chorus", "Thelimaanam", "Chorus", "Malare"), lines.map { it.text })
    }

    @Test
    fun `movie comes from the soundtrack album`() {
        assertEquals("Brahmastra", SongInfoSource.movieFrom("Kesariya (From \"Brahmastra\")"))
        assertEquals("Velipadinte Pusthakam", SongInfoSource.movieFrom("Velipadinte Pusthakam (Original Motion Picture Soundtrack)"))
        assertEquals("Premam", SongInfoSource.movieFrom("Premam"))
    }

    @Test
    fun `credits are read from the description`() {
        val description = """
            Provided to YouTube by Muzik 247
            Malare · Vijay Yesudas
            Composer: Rajesh Murugesan
            Lyricist: Shabareesh Varma
        """.trimIndent()
        val credits = SongInfoSource.creditsFrom(description)
        assertEquals("Shabareesh Varma", credits.lyricist)
        assertEquals("Rajesh Murugesan", credits.composer)
        val html = SongInfoSource.creditsFrom("Provided to YouTube by Sony<br><br>Composer: Pritam<br>Lyricist: <a href=\"x\">Amitabh Bhattacharya</a>")
        assertEquals("Pritam", html.composer)
        assertEquals("Amitabh Bhattacharya", html.lyricist)
        val indian = SongInfoSource.creditsFrom("Song: Kesariya\nMusic - Pritam\nLyrics - Amitabh Bhattacharya")
        assertEquals("Amitabh Bhattacharya", indian.lyricist)
        assertEquals("Pritam", indian.composer)
    }

    @Test
    fun `timed lyrics win, then YouTube Music, then plain`() {
        val timed = Lyrics(listOf(LyricLine(0, "a")), synced = true, source = "LRCLIB")
        val plain = Lyrics(listOf(LyricLine(null, "a")), synced = false, source = "LRCLIB")
        val ytm = Lyrics(listOf(LyricLine(null, "a")), synced = false, source = "Musixmatch")
        assertSame(timed, SongInfoSource.pickLyrics(timed, ytm))
        assertSame(ytm, SongInfoSource.pickLyrics(plain, ytm))
        assertSame(plain, SongInfoSource.pickLyrics(plain, null))
    }
}

class PhoneDownloadsTest {
    @Test
    fun `file names are clean and readable`() {
        val name = com.ramzmania.tubefy.core.download.PhoneDownloads::fileName
        assertEquals("Malare - Vijay Yesudas", name("Malare", "Vijay Yesudas"))
        assertEquals("Kesariya (From Brahmastra) - Arijit Singh", name("Kesariya (From \"Brahmastra\")", "Arijit Singh"))
        assertEquals("AC DC Song", name("AC/DC: Song?", ""))
        assertEquals("Tum Hi Ho - Arijit Singh", name("Tum Hi Ho - Arijit Singh", "Arijit Singh"))
        assertEquals("TubeFy song", name("", ""))
    }
}

class LyricsVersionsTest {
    private val malayalamTimed = Lyrics(listOf(LyricLine(1000, "തെളിമാനം")), synced = true, source = "LRCLIB")
    private val manglish = Lyrics(listOf(LyricLine(null, "Thelimaanam mazhavillin")), synced = false, source = "Musixmatch")
    private val isLatin = com.ramzmania.tubefy.core.lyrics.Romanizer::isLatin

    @Test
    fun `script lyrics first, with a timed English-letters version converted on the phone`() {
        val converted = Lyrics(listOf(LyricLine(1000, "thelimaanam")), synced = true, source = "LRCLIB · in English letters")
        val (shown, english) = SongInfoSource.versions(malayalamTimed, manglish, isLatin) { converted }
        assertSame(malayalamTimed, shown)
        assertSame(converted, english)
    }

    @Test
    fun `without conversion the source's own English letters are used`() {
        val (_, english) = SongInfoSource.versions(malayalamTimed, manglish, isLatin) { null }
        assertSame(manglish, english)
    }

    @Test
    fun `English-only songs get no switch`() {
        val (shown, english) = SongInfoSource.versions(null, manglish, isLatin) { error("not needed") }
        assertSame(manglish, shown)
        assertEquals(null, english)
    }

    @Test
    fun `textbook transliteration reads like everyday spelling`() {
        val readable = com.ramzmania.tubefy.core.lyrics.Romanizer::readable
        // Malayalam: ത is "th", ന്റ is "nt", ഴ is "zh"
        assertEquals("thelimaanam mazhavillin", readable("teḷimānaṁ maḻavilliṉ", true))
        assertEquals("ninte kannil", readable("ninṟe kaṇṇil", true))
        assertEquals("chandran", readable("candran", true))
        // Hindi keeps "t"
        assertEquals("tere binaa", readable("tērē binā", false))
    }
}

class OfflineLyricsTest {
    @Test
    fun `saved lyrics come back the same, both versions and timings`() {
        val info = com.ramzmania.tubefy.core.lyrics.SongInfo(
            details = com.ramzmania.tubefy.core.lyrics.SongDetails("Malare", "Vijay Yesudas", "Premam", "", "Rajesh Murugesan", "2015"),
            lyrics = Lyrics(listOf(LyricLine(17_830, "തെളിമാനം"), LyricLine(null, "")), synced = true, source = "LRCLIB"),
            englishLetters = Lyrics(listOf(LyricLine(17_830, "thelimaanam")), synced = true, source = "LRCLIB · in English letters")
        )
        // Through JSON text, as on the phone
        val adapter = com.squareup.moshi.Moshi.Builder().build().adapter(Any::class.java)
        val restored = com.ramzmania.tubefy.core.lyrics.OfflineLyrics.fromMap(
            adapter.fromJson(adapter.toJson(com.ramzmania.tubefy.core.lyrics.OfflineLyrics.toMap(info))) as Map<*, *>
        )
        assertEquals(info, restored)
    }
}

class IntroOffsetTest {
    private val offset = SongInfoSource::introOffset

    @Test
    fun `a music video with an intro shifts the lyrics by the extra time`() {
        assertEquals(0L, offset(240_000, 242_000))        // same recording (±4 s): no shift
        assertEquals(7_000L, offset(240_000, 247_000))    // 7 s intro
        assertEquals(30_000L, offset(240_000, 270_000))   // up to 30 s
        assertEquals(null, offset(240_000, 300_000))      // a minute longer: another version
        assertEquals(null, offset(240_000, 230_000))      // shorter: another version
        assertEquals(0L, offset(null, 240_000))           // the lyrics' length unknown: no shift
        assertEquals(0L, offset(240_000, 0))              // the playing length unknown: no shift
    }

    @Test
    fun `the shift is kept with offline lyrics`() {
        val info = com.ramzmania.tubefy.core.lyrics.SongInfo(
            details = com.ramzmania.tubefy.core.lyrics.SongDetails("A", "", "", "", "", ""),
            lyrics = Lyrics(listOf(LyricLine(1_000, "a")), synced = true, source = "LRCLIB", offsetMs = 7_000)
        )
        val adapter = com.squareup.moshi.Moshi.Builder().build().adapter(Any::class.java)
        val restored = com.ramzmania.tubefy.core.lyrics.OfflineLyrics.fromMap(
            adapter.fromJson(adapter.toJson(com.ramzmania.tubefy.core.lyrics.OfflineLyrics.toMap(info))) as Map<*, *>
        )
        assertEquals(7_000L, restored.lyrics?.offsetMs)
    }
}
