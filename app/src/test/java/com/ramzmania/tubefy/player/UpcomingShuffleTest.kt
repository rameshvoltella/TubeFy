package com.ramzmania.tubefy.player

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class UpcomingShuffleTest {

    @Test
    fun `played songs stay before the current one and the rest come after`() {
        val ids = listOf("a", "b", "c", "d", "e", "f")
        val order = UpcomingShuffle.order(ids, current = 2, playedIds = listOf("a", "b", "c"), random = Random(1))
        assertEquals(listOf(0, 1, 2), order.take(3))
        assertEquals(setOf(3, 4, 5), order.drop(3).toSet())
    }

    @Test
    fun `current song always has upcoming songs after it`() {
        val ids = (0 until 20).map { "s$it" }
        repeat(50) { seed ->
            val order = UpcomingShuffle.order(ids, current = 19, playedIds = emptyList(), random = Random(seed))
            assertEquals(19, order.first())
            assertEquals(ids.indices.toSet(), order.toSet())
        }
    }

    @Test
    fun `songs added later are never placed before the current one`() {
        // a, b played; c playing; d, e queued earlier; x, y just added by the auto-queue
        val ids = listOf("a", "b", "c", "d", "e", "x", "y")
        val order = UpcomingShuffle.order(ids, current = 2, playedIds = listOf("a", "b", "c"), random = Random(7))
        val currentPosition = order.indexOf(2)
        assertTrue(order.indexOf(5) > currentPosition && order.indexOf(6) > currentPosition)
    }

    @Test
    fun `history of songs no longer in the queue is ignored and result is a permutation`() {
        val ids = listOf("a", "b", "c")
        val order = UpcomingShuffle.order(ids, current = 1, playedIds = listOf("gone", "a", "b"), random = Random(3))
        assertEquals(listOf(0, 1, 2), order)
    }
}

class UpcomingShuffleStabilityTest {

    @Test
    fun `an order that is already right is returned unchanged (no rebuild loop)`() {
        val ids = listOf("a", "b", "c", "d", "e", "f")
        val existing = listOf(0, 1, 5, 3, 2, 4) // a, b played; current = f(5); upcoming 3, 2, 4
        repeat(20) { seed ->
            val order = UpcomingShuffle.order(ids, current = 5, playedIds = listOf("a", "b", "f"), previousOrder = existing, random = Random(seed))
            assertEquals(existing, order)
        }
    }

    @Test
    fun `newly added songs are fitted after the current one, keeping the rest of the upcoming order`() {
        val ids = listOf("a", "b", "c", "d", "x", "y")
        // ExoPlayer put the new x(4), y(5) before the current song c(2)
        val existing = listOf(4, 0, 1, 5, 2, 3)
        val order = UpcomingShuffle.order(ids, current = 2, playedIds = listOf("a", "b", "c"), previousOrder = existing, random = Random(1))
        assertEquals(listOf(0, 1, 2), order.take(3))
        assertEquals(setOf(3, 4, 5), order.drop(3).toSet())
        // applying again with the result is stable
        assertEquals(order, UpcomingShuffle.order(ids, 2, listOf("a", "b", "c"), previousOrder = order, random = Random(99)))
    }
}
