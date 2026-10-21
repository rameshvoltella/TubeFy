package com.ramzmania.tubefy.ui.components.screen.youtube

import com.ramzmania.tubefy.data.youtube.YouTubeItem

/**
 * Autoplay: the related video to play when the current one ends — the first one (YouTube's top pick)
 * that wasn't already played in this run, so it never bounces between two videos. Null if none.
 */
fun nextRelated(related: List<YouTubeItem>, played: Collection<String>): YouTubeItem? =
    related.firstOrNull { !it.isPlaylist && !it.isLive && it.id !in played }

/** Seconds before the end when TV shows the "Up next" card. */
const val UP_NEXT_LEAD_SECONDS = 10

/**
 * The page acts this close to the end, before the player reaches it: at the very end the player would
 * jump to the next item in its queue (a song, or the same video looping) and show it for a moment.
 */
const val END_GUARD_MS = 400L

/** While this close to the end, the position is checked often so [END_GUARD_MS] isn't missed. */
const val END_WATCH_MS = 2_500L

/** True when a playing, non-live video is within [END_GUARD_MS] of its end. */
fun isAtEnd(positionMs: Long, durationMs: Long, isLive: Boolean): Boolean =
    !isLive && durationMs > 0 && positionMs > 0 && durationMs - positionMs <= END_GUARD_MS
