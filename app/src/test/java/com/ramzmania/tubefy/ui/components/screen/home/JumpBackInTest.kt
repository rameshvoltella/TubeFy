package com.ramzmania.tubefy.ui.components.screen.home

import com.ramzmania.tubefy.data.local.PlayedSong
import com.ramzmania.tubefy.data.local.PlaylistVisit
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class JumpBackInTest {

    private fun song(i: Int) = PlayedSong("song$i", "Song $i", "", playedAt = i.toLong())
    private fun playlist(i: Int) = PlaylistVisit("yt:pl$i", false, "pl$i", "Playlist $i", "", lastPlayedAt = i.toLong(), score = 1.0)

    @Test
    fun `hidden until there are at least 5 items`() {
        assertTrue(jumpBackInItems(listOf(song(1), song(2)), listOf(playlist(3), playlist(4))).isEmpty())
        assertEquals(5, jumpBackInItems(listOf(song(1), song(2)), listOf(playlist(3), playlist(4), playlist(5))).size)
    }

    @Test
    fun `keeps the 15 most recent songs and playlists mixed`() {
        val songs = (1..12).map { song(it * 2) }        // even timestamps 2..24
        val playlists = (1..12).map { playlist(it * 2 - 1) } // odd timestamps 1..23
        val items = jumpBackInItems(songs, playlists)
        assertEquals(15, items.size)
        assertEquals((10..24).toSet(), items.map { it.lastPlayedAt.toInt() }.toSet())
        assertTrue(items.any { it is JumpBackInItem.Song } && items.any { it is JumpBackInItem.Playlist })
    }

    @Test
    fun `order is stable within one launch`() {
        val songs = (1..10).map { song(it) }
        val playlists = (11..16).map { playlist(it) }
        assertEquals(jumpBackInItems(songs, playlists).map { it.key }, jumpBackInItems(songs, playlists).map { it.key })
    }
}
