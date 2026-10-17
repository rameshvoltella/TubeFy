package com.ramzmania.tubefy.player

import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.SessionCommand
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Stops playback after a set time (fading out over the last [FADE_MS]) or at the end of the
 * current song. Lives in the playback service so it keeps running with the screen off.
 * The state is published as session extras ([EXTRA_END_AT], [EXTRA_END_OF_SONG]) for the UI.
 */
@UnstableApi
class SleepTimer(
    private val scope: CoroutineScope,
    private val sessionProvider: () -> MediaSession?,
    private val publishExtras: (Bundle) -> Unit
) {
    private var job: Job? = null
    private var endAt = 0L
    private var endOfSong = false

    private val player get() = sessionProvider()?.player as? ExoPlayer

    fun handleCommand(action: String, args: Bundle) {
        when (action) {
            CMD_SET -> if (args.getBoolean(KEY_END_OF_SONG)) startEndOfSong() else start(args.getInt(KEY_MINUTES))
            CMD_EXTEND -> extend(args.getInt(KEY_MINUTES, 5))
            CMD_CANCEL -> cancel()
        }
    }

    /** Called when the player paused itself at the end of a song (end-of-song mode). */
    fun onPausedAtEndOfSong() {
        if (!endOfSong) return
        player?.pauseAtEndOfMediaItems = false
        endOfSong = false
        publish()
    }

    fun release() {
        job?.cancel()
    }

    private fun start(minutes: Int) {
        if (minutes <= 0) return
        cancel()
        endAt = System.currentTimeMillis() + minutes * 60_000L
        schedule()
        publish()
    }

    private fun startEndOfSong() {
        cancel()
        endOfSong = true
        player?.pauseAtEndOfMediaItems = true
        publish()
    }

    private fun extend(minutes: Int) {
        if (endOfSong) return
        if (endAt == 0L) {
            start(minutes)
            return
        }
        player?.volume = 1f // in case the fade had already started
        endAt += minutes * 60_000L
        schedule()
        publish()
    }

    private fun cancel() {
        job?.cancel()
        job = null
        player?.volume = 1f
        if (endOfSong) player?.pauseAtEndOfMediaItems = false
        endOfSong = false
        endAt = 0L
        publish()
    }

    private fun schedule() {
        job?.cancel()
        job = scope.launch {
            val untilFade = endAt - System.currentTimeMillis() - FADE_MS
            if (untilFade > 0) delay(untilFade)
            fadeOutAndPause()
        }
    }

    private suspend fun fadeOutAndPause() {
        val exoPlayer = player
        if (exoPlayer != null && exoPlayer.isPlaying) {
            val startVolume = exoPlayer.volume
            val remaining = (endAt - System.currentTimeMillis()).coerceIn(0L, FADE_MS)
            val steps = 30
            repeat(steps) { step ->
                exoPlayer.volume = startVolume * (1f - (step + 1f) / steps)
                delay(remaining / steps)
            }
            exoPlayer.pause()
        }
        exoPlayer?.volume = 1f
        endAt = 0L
        job = null
        publish()
    }

    private fun publish() {
        publishExtras(bundleOf(EXTRA_END_AT to endAt, EXTRA_END_OF_SONG to endOfSong))
    }

    companion object {
        const val CMD_SET = "com.ramzmania.tubefy.SLEEP_TIMER_SET"
        const val CMD_EXTEND = "com.ramzmania.tubefy.SLEEP_TIMER_EXTEND"
        const val CMD_CANCEL = "com.ramzmania.tubefy.SLEEP_TIMER_CANCEL"
        const val KEY_MINUTES = "minutes"
        const val KEY_END_OF_SONG = "endOfSong"
        const val EXTRA_END_AT = "com.ramzmania.tubefy.SLEEP_END_AT"
        const val EXTRA_END_OF_SONG = "com.ramzmania.tubefy.SLEEP_END_OF_SONG"
        private const val FADE_MS = 20_000L

        val COMMANDS = listOf(CMD_SET, CMD_EXTEND, CMD_CANCEL).map { SessionCommand(it, Bundle.EMPTY) }
    }
}
