package com.ramzmania.tubefy.core.analytics

import android.content.Context
import android.os.Bundle
import android.util.Log
import com.google.firebase.analytics.FirebaseAnalytics

/**
 * Usage analytics (Firebase), in one place: every event name and parameter lives here, screens just
 * call the typed functions. No personal data: no account, no playlist names. Turned off with
 * Settings → "Share usage data", and a no-op in builds without Firebase config (debug, F-Droid).
 */
object Analytics {

    private var firebase: FirebaseAnalytics? = null
    private var enabled = true
    private const val PREFS = "analytics"
    private const val KEY_ENABLED = "enabled"
    private const val MAX_TEXT = 100

    fun init(context: Context) {
        enabled = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getBoolean(KEY_ENABLED, true)
        // Without google-services.json Firebase isn't set up; analytics then does nothing
        firebase = runCatching { FirebaseAnalytics.getInstance(context.applicationContext) }.getOrNull()
        firebase?.setAnalyticsCollectionEnabled(enabled)
    }

    fun isEnabled() = enabled

    /** Settings → "Share usage data". */
    fun setEnabled(context: Context, on: Boolean) {
        enabled = on
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putBoolean(KEY_ENABLED, on).apply()
        firebase?.setAnalyticsCollectionEnabled(on)
    }

    // --- Where a song / video was started from (set just before it plays) ---

    /** Set by places that start playback without a screen of their own (Shazam, shared links, the car). */
    @Volatile var pendingSource: String? = null

    fun takeSource(fallback: String): String = (pendingSource ?: fallback).also { pendingSource = null }

    // --- 1. Screens ---

    fun screen(name: String) = log(FirebaseAnalytics.Event.SCREEN_VIEW, FirebaseAnalytics.Param.SCREEN_NAME to name, FirebaseAnalytics.Param.SCREEN_CLASS to name)

    fun playerTab(mode: String) = log("player_tab_view", "mode" to mode)

    // --- 2. Search ---

    fun search(term: String, area: String, input: String) =
        log(FirebaseAnalytics.Event.SEARCH, FirebaseAnalytics.Param.SEARCH_TERM to term.trim().lowercase(), "search_area" to area, "input" to input)

    fun searchResultClick(area: String, position: Int, itemType: String) =
        log("search_result_click", "search_area" to area, "position" to position, "item_type" to itemType)

    fun searchNoResults(term: String, area: String) =
        log("search_no_results", FirebaseAnalytics.Param.SEARCH_TERM to term.trim().lowercase(), "search_area" to area)

    // --- 3. Music ---

    fun songPlay(videoId: String, title: String?, artist: String?, source: String, offline: Boolean) =
        log("song_play", "video_id" to videoId, "title" to title, "artist" to artist, "source" to source, "offline" to offline)

    fun songEnd(videoId: String, listenedSec: Long, listenedPct: Int, reason: String) =
        log("song_end", "video_id" to videoId, "listened_sec" to listenedSec, "listened_pct" to listenedPct, "reason" to reason)

    fun playbackError(errorCode: String, retrying: Boolean) =
        log("playback_error", "error_code" to errorCode, "recovered" to retrying)

    // --- 4. YouTube ---

    fun videoOpen(videoId: String, title: String?, channel: String?, source: String, isLive: Boolean) =
        log("video_open", "video_id" to videoId, "title" to title, "channel" to channel, "source" to source, "is_live" to isLive)

    fun videoWatch(videoId: String, watchSec: Long, watchPct: Int, quality: String?, fullscreenUsed: Boolean) =
        log("video_watch", "video_id" to videoId, "watch_sec" to watchSec, "watch_pct" to watchPct, "quality" to quality, "fullscreen_used" to fullscreenUsed)

    fun videoSettingChange(setting: String, value: String) = log("video_setting_change", "setting" to setting, "value" to value)

    fun videoPlayAsMusic(videoId: String) = log("video_play_as_music", "video_id" to videoId)

    fun channelOpen(channel: String) = log("channel_open", "channel" to channel)

    // --- 5. Identify song ---

    fun identifyStart(entry: String) = log("identify_start", "entry" to entry)

    fun identifyResult(result: String, secondsToMatch: Int?, inBackground: Boolean) =
        log("identify_result", "result" to result, "seconds_to_match" to secondsToMatch, "in_background" to inBackground)

    fun identifyAction(action: String) = log("identify_action", "action" to action)

    fun micPermission(result: String) = log("mic_permission", "result" to result)

    // --- 6. Lyrics ---

    fun lyricsView(hasLyrics: Boolean, timed: Boolean, source: String?) =
        log("lyrics_view", "has_lyrics" to hasLyrics, "timed" to timed, "source" to source)

    fun lyricsFeature(feature: String) = log("lyrics_feature", "feature" to feature)

    // --- 7. Library & downloads ---

    fun favoriteToggle(added: Boolean) = log("favorite_toggle", "added" to added)

    fun addToPlaylist(playlistCount: Int? = null) = log("add_to_playlist", "playlist_count" to playlistCount)

    fun playlistCreate() = log("playlist_create")

    fun goToPage(page: String) = log("go_to_page", "page" to page)

    fun downloadStart(kind: String, quality: String? = null) = log("download_start", "kind" to kind, "quality" to quality)

    fun downloadResult(kind: String, result: String) {
        log("download_result", "kind" to kind, "result" to result)
        if (result == "done") userProperty("uses_downloads", "yes")
    }

    // --- 8. Player features ---

    fun playerFeature(feature: String, value: String? = null) = log("player_feature", "feature" to feature, "value" to value)

    // --- 9. Sharing, car, TV, health ---

    fun sharedLinkOpen(choice: String) = log("shared_link_open", "choice" to choice)

    fun carSession(action: String) = log("car_session", "action" to action)

    fun tvSession() = log("tv_session")

    private var limitedLogged = false

    /** YouTube's "confirm you're not a bot" block (once per app run). */
    fun youtubeLimited() {
        if (limitedLogged) return
        limitedLogged = true
        log("youtube_limited")
    }

    // --- User properties ---

    fun userProperty(name: String, value: String) {
        if (!enabled) return
        runCatching { firebase?.setUserProperty(name, value.take(36)) }
    }

    // --- Internals ---

    private fun log(event: String, vararg params: Pair<String, Any?>) {
        val analytics = firebase ?: return
        if (!enabled) return
        val bundle = Bundle()
        params.forEach { (key, value) ->
            when (value) {
                null -> Unit
                is String -> if (value.isNotBlank()) bundle.putString(key, value.take(MAX_TEXT))
                is Int -> bundle.putLong(key, value.toLong())
                is Long -> bundle.putLong(key, value)
                is Boolean -> bundle.putString(key, if (value) "true" else "false")
                else -> bundle.putString(key, value.toString().take(MAX_TEXT))
            }
        }
        runCatching { analytics.logEvent(event, bundle) }.onFailure { Log.w("Analytics", "Couldn't log $event", it) }
    }
}
