package com.ramzmania.tubefy.core.analytics

import android.content.Context
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics

/** "full" edition: usage analytics go to Firebase (when google-services.json was present at build time). */
internal class AnalyticsBackend private constructor(private val firebase: FirebaseAnalytics) {

    fun setEnabled(on: Boolean) = firebase.setAnalyticsCollectionEnabled(on)

    fun setUserProperty(name: String, value: String) = firebase.setUserProperty(name, value)

    fun logEvent(name: String, params: Bundle) = firebase.logEvent(name, params)

    companion object {
        const val AVAILABLE = true

        /** Null when Firebase isn't set up (built without google-services.json). */
        fun create(context: Context): AnalyticsBackend? =
            runCatching { AnalyticsBackend(FirebaseAnalytics.getInstance(context)) }.getOrNull()
    }
}
