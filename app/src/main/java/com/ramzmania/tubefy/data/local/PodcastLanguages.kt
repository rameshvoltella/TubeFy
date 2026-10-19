package com.ramzmania.tubefy.data.local

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/** Languages on the Podcasts page: built-in Indian languages plus the ones the user added. */
@Singleton
class PodcastLanguages @Inject constructor(@ApplicationContext context: Context) {

    private val prefs = context.getSharedPreferences("podcast_languages", Context.MODE_PRIVATE)
    private val customPrivate = MutableStateFlow(
        prefs.getString(KEY, "").orEmpty().split('\n').filter { it.isNotBlank() }
    )

    /** Languages the user added, in the order they were added. */
    val custom: StateFlow<List<String>> = customPrivate.asStateFlow()

    fun isKnown(language: String) =
        (DEFAULTS + customPrivate.value).any { it.equals(language.trim(), ignoreCase = true) }

    fun add(language: String) {
        val name = language.trim().replaceFirstChar { it.uppercase() }
        if (name.isEmpty() || isKnown(name)) return
        save(customPrivate.value + name)
    }

    fun remove(language: String) = save(customPrivate.value.filterNot { it.equals(language, ignoreCase = true) })

    private fun save(list: List<String>) {
        customPrivate.value = list
        prefs.edit().putString(KEY, list.joinToString("\n")).apply()
    }

    companion object {
        private const val KEY = "custom"

        /** Top Indian regional languages shown by default. */
        val DEFAULTS = listOf(
            "Malayalam", "Hindi", "Tamil", "Telugu", "Kannada",
            "Bengali", "Marathi", "Punjabi", "Gujarati"
        )
    }
}
