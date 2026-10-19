package com.ramzmania.tubefy.player

import androidx.media3.common.C
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.ShuffleOrder
import kotlin.random.Random

/**
 * Keeps ExoPlayer's shuffle order sensible: songs already played stay behind the current song,
 * and only the upcoming songs are shuffled after it. ExoPlayer's default order is random over the
 * whole queue, so turning shuffle on could leave the current song last (no "next": the button
 * disappears and playback stops), and songs added later could land before the current one.
 */
@UnstableApi
class UpcomingShuffle {

    /** Song ids in the order they were played in this queue (most recent last). */
    private val history = mutableListOf<String>()

    fun onSongStarted(mediaId: String?) {
        if (mediaId.isNullOrBlank()) return
        history.remove(mediaId)
        history += mediaId
        if (history.size > MAX_HISTORY) history.removeAt(0)
    }

    /** A new queue was started (new song/playlist): forget what was played before. */
    fun reset() = history.clear()

    /**
     * Rebuilds the shuffle order as: played songs (in play order) → current song → the rest.
     * With [reshuffle] (shuffle just turned on) the rest is freshly shuffled; otherwise the existing
     * upcoming order is kept and only out-of-place songs (e.g. just added) are fitted in, so this
     * settles: once the order is right it changes nothing (no endless rebuild/notification loop).
     */
    fun apply(player: ExoPlayer, reshuffle: Boolean = false) {
        if (!player.shuffleModeEnabled) return
        val count = player.mediaItemCount
        val current = player.currentMediaItemIndex
        if (count == 0 || current !in 0 until count) return

        val ids = (0 until count).map { player.getMediaItemAt(it).mediaId }
        val existing = currentOrder(player.shuffleOrder)
        val newOrder = order(ids, current, history, previousOrder = existing.takeUnless { reshuffle }, random = Random.Default)
        if (newOrder == existing) return
        player.setShuffleOrder(ShuffleOrder.DefaultShuffleOrder(newOrder.toIntArray(), Random.nextLong()))
    }

    private fun currentOrder(order: ShuffleOrder): List<Int> {
        val result = mutableListOf<Int>()
        var index = order.firstIndex
        while (index != C.INDEX_UNSET && result.size < order.length) {
            result += index
            index = order.getNextIndex(index)
        }
        return result
    }

    companion object {
        private const val MAX_HISTORY = 500

        /**
         * The shuffle order for a queue of [ids] playing [current]: items whose id was played
         * (in play order), then [current], then the upcoming items. Without [previousOrder] the
         * upcoming items are shuffled; with it, their existing order after [current] is kept and
         * any other items are inserted at random places among them. A permutation of all indices.
         */
        fun order(
            ids: List<String>,
            current: Int,
            playedIds: List<String>,
            previousOrder: List<Int>? = null,
            random: Random
        ): List<Int> {
            val currentId = ids[current]
            val used = mutableSetOf(current)
            val played = playedIds.filter { it != currentId }.mapNotNull { id ->
                ids.indices.firstOrNull { it !in used && ids[it] == id }?.also { used += it }
            }
            val remaining = ids.indices.filter { it !in used }
            val upcoming = if (previousOrder == null) {
                remaining.shuffled(random)
            } else {
                val remainingSet = remaining.toSet()
                val afterCurrent = previousOrder.dropWhile { it != current }.drop(1)
                val kept = afterCurrent.filter { it in remainingSet }.distinct().toMutableList()
                val keptSet = kept.toSet()
                remaining.filter { it !in keptSet }.forEach { kept.add(random.nextInt(kept.size + 1), it) }
                kept
            }
            return played + current + upcoming
        }
    }
}
