package com.ramzmania.tubefy.core.recommendation

import com.ramzmania.tubefy.data.dto.base.searchformat.TubeFyCoreTypeData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DailyRecommendationsTest {

    private fun song(id: String, artist: String = "") =
        TubeFyCoreTypeData(videoId = id, videoTitle = "Song $id", videoImage = "", artist = artist)

    private fun playlist(id: String) =
        TubeFyCoreTypeData(videoTitle = "Playlist $id", videoImage = "", plaListUrl = "https://www.youtube.com/playlist?list=$id")

    @Test
    fun `alternates mixes, skips played songs and duplicates, every third is a playlist`() {
        val mixA = listOf(song("aaaaaaaaaa1"), song("aaaaaaaaaa2"), song("shared00001"))
        val mixB = listOf(song("bbbbbbbbbb1"), song("shared00001"), song("bbbbbbbbbb2"))
        val result = DailyRecommendations.compose(
            mixes = listOf(mixA, mixB),
            playlists = listOf(playlist("P1"), playlist("P1"), playlist("P2")),
            played = setOf("aaaaaaaaaa2")
        )
        assertEquals(
            listOf("aaaaaaaaaa1", "bbbbbbbbbb1", "P1", "shared00001", "bbbbbbbbbb2", "P2"),
            result.map { it.videoId.ifEmpty { it.plaListUrl!!.substringAfter("list=") } }
        )
    }

    @Test
    fun `never more than 12 items`() {
        val mixes = listOf((1..20).map { song("a%010d".format(it)) }, (1..20).map { song("b%010d".format(it)) })
        val playlists = (1..10).map { playlist("P$it") }
        val result = DailyRecommendations.compose(mixes, playlists, played = emptySet())
        assertEquals(12, result.size)
        assertEquals(4, result.count { it.videoId.isEmpty() })
    }

    @Test
    fun `dominant artist per mix`() {
        val mixes = listOf(
            listOf(song("1", "Anirudh"), song("2", "Anirudh"), song("3", "Sun TV")),
            listOf(song("4", "Vidyasagar"), song("5", ""), song("6", "Vidyasagar")),
            listOf(song("7", "Anirudh"))
        )
        assertEquals(listOf("Anirudh", "Vidyasagar"), DailyRecommendations.dominantArtists(mixes))
    }

    @Test
    fun `no mixes and no playlists gives nothing`() {
        assertTrue(DailyRecommendations.compose(emptyList(), emptyList(), emptySet()).isEmpty())
    }
}
