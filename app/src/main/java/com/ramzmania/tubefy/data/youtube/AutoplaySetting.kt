package com.ramzmania.tubefy.data.youtube

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/** The video page's Autoplay switch (on by default): when a video ends, play the next related one. */
@Singleton
class AutoplaySetting @Inject constructor(@ApplicationContext context: Context) {
    private val prefs = context.getSharedPreferences("video_settings", Context.MODE_PRIVATE)
    private val _enabled = MutableStateFlow(prefs.getBoolean(KEY, true))
    val enabled: StateFlow<Boolean> = _enabled.asStateFlow()

    fun set(enabled: Boolean) {
        _enabled.value = enabled
        prefs.edit().putBoolean(KEY, enabled).apply()
    }

    private companion object {
        const val KEY = "autoplay"
    }
}
