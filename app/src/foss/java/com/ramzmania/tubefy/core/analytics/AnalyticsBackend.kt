package com.ramzmania.tubefy.core.analytics

import android.content.Context
import android.os.Bundle

/** "foss" (F-Droid) edition: no analytics library and nothing is ever sent. */
internal class AnalyticsBackend private constructor() {

    fun setEnabled(on: Boolean) = Unit

    fun setUserProperty(name: String, value: String) = Unit

    fun logEvent(name: String, params: Bundle) = Unit

    companion object {
        const val AVAILABLE = false

        fun create(context: Context): AnalyticsBackend? = null
    }
}
