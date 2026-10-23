package com.ramzmania.tubefy.core.youtube

import org.schabi.newpipe.extractor.ServiceList
import org.schabi.newpipe.extractor.exceptions.ReCaptchaException
import org.schabi.newpipe.extractor.exceptions.SignInConfirmNotBotException
import org.schabi.newpipe.extractor.stream.StreamInfo
import java.util.concurrent.ConcurrentHashMap

/**
 * One place that asks YouTube about a video. Answers are kept for a while and shared, so opening a
 * video page (details + stream + manifest) costs one request, not three: YouTube blocks IPs that send
 * too many ("Sign in to confirm that you're not a bot"). Blocking network calls: use off the main thread.
 */
object YouTubeStreams {

    private const val TTL_MS = 20 * 60 * 1000L
    private const val MAX_ENTRIES = 40

    private class Entry(val info: StreamInfo, val at: Long)

    private val cache = ConcurrentHashMap<String, Entry>()
    private val locks = ConcurrentHashMap<String, Any>()

    /** [videoId]'s info; [fresh] skips the cache (e.g. its links expired or were rejected). */
    fun info(videoId: String, fresh: Boolean = false): StreamInfo {
        cached(videoId, fresh)?.let { return it }
        // One fetch per video at a time: a second caller waits and reuses the answer
        synchronized(locks.getOrPut(videoId) { Any() }) {
            cached(videoId, fresh)?.let { return it }
            try {
                val info = StreamInfo.getInfo(ServiceList.YouTube, "https://www.youtube.com/watch?v=$videoId")
                if (cache.size >= MAX_ENTRIES) {
                    cache.entries.minByOrNull { it.value.at }?.let { cache.remove(it.key) }
                }
                cache[videoId] = Entry(info, System.currentTimeMillis())
                YouTubeAccess.markOk()
                return info
            } catch (e: Exception) {
                YouTubeAccess.check(e)
                throw e
            }
        }
    }

    /** The info for [videoId] if it was looked up recently, without asking YouTube. */
    fun peek(videoId: String): StreamInfo? = cached(videoId, fresh = false)

    private fun cached(videoId: String, fresh: Boolean): StreamInfo? {
        if (fresh) return null
        val entry = cache[videoId] ?: return null
        return entry.info.takeIf { System.currentTimeMillis() - entry.at < TTL_MS }
    }
}

/** Remembers when YouTube last refused this network, to explain it instead of a generic error. */
object YouTubeAccess {

    const val BLOCKED_MESSAGE = "YouTube is limiting this network right now. Try mobile data or try again later."

    private const val REMEMBER_MS = 10 * 60 * 1000L

    @Volatile private var blockedAt = 0L

    /** True when YouTube refused us in the last few minutes. */
    val recentlyBlocked: Boolean get() = blockedAt > 0 && System.currentTimeMillis() - blockedAt < REMEMBER_MS

    /** Call with any YouTube failure: notes it if it was a "not a bot" / rate limit refusal. */
    fun check(error: Throwable) {
        if (isBlock(error)) blockedAt = System.currentTimeMillis()
    }

    fun markOk() {
        blockedAt = 0L
    }

    /** The message to show for a failed YouTube load: the block explanation, or [fallback]. */
    fun messageOr(fallback: String): String = if (recentlyBlocked) BLOCKED_MESSAGE else fallback

    internal fun isBlock(error: Throwable): Boolean {
        var cause: Throwable? = error
        while (cause != null) {
            if (cause is SignInConfirmNotBotException || cause is ReCaptchaException) return true
            if (cause.message?.contains("not a bot", ignoreCase = true) == true) return true
            cause = cause.cause
        }
        return false
    }
}
