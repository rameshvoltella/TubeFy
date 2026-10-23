package com.ramzmania.tubefy.core.lyrics

import android.os.Build
import java.text.Normalizer

/**
 * Lyrics in Indian scripts (Malayalam, Tamil, Hindi, …) written in English letters, the way people
 * type them ("തെളിമാനം" → "thelimaanam"-style), keeping each line's timing. Uses Android's built-in
 * transliteration (Android 10+); returns null where it isn't available.
 */
object Romanizer {

    private val transliterator by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) runCatching { android.icu.text.Transliterator.getInstance("Any-Latin") }.getOrNull() else null
    }

    fun romanize(lyrics: Lyrics): Lyrics? {
        val converter = transliterator ?: return null
        val dravidian = lyrics.lines.any { line -> line.text.any { it.code in 0x0D00..0x0D7F || it.code in 0x0B80..0x0BFF } }
        return runCatching {
            Lyrics(
                lines = lyrics.lines.map { line -> line.copy(text = readable(converter.transliterate(line.text), dravidian)) },
                synced = lyrics.synced,
                source = "${lyrics.source} · in English letters".trimStart(' ', '·')
            )
        }.getOrNull()
    }

    /** True when the lyrics are (almost) all in English letters. */
    fun isLatin(lyrics: Lyrics): Boolean {
        var latin = 0
        var other = 0
        lyrics.lines.forEach { line ->
            line.text.forEach { ch ->
                when {
                    ch in 'a'..'z' || ch in 'A'..'Z' -> latin++
                    ch.isLetter() -> other++
                }
            }
        }
        return latin + other > 0 && latin >= (latin + other) * 0.8
    }

    /**
     * Textbook transliteration (ISO 15919: "teḷimānaṁ maḻavilliṉ") → everyday spelling
     * ("thelimaanam mazhavillin"): long vowels doubled, special consonants spelled the usual way,
     * remaining accents dropped. [dravidian] (Malayalam / Tamil) follows how those are usually typed:
     * ത / த is "th" and ന്റ is "nt" ("ninte"); Hindi keeps "t" ("tere").
     */
    internal fun readable(iso: String, dravidian: Boolean = false): String {
        var text = iso
        if (dravidian) {
            // ത first, so the "t" of ന്റ / റ്റ below stays a plain "t"
            text = text.replace(Regex("t(?!h)"), "th").replace(Regex("T(?!h)"), "Th")
                .replace("ṟṟ", "tt").replace("nṟ", "nt")
        }
        REPLACEMENTS.forEach { (from, to) -> text = text.replace(from, to) }
        // Single "c" is the "ch" sound in this scheme ("ca" → "cha"), but keep an existing "ch"
        text = text.replace(Regex("c(?!h)"), "ch").replace(Regex("C(?!h)"), "Ch")
        // Any accents left: drop them ("ṭ" → "t")
        text = Normalizer.normalize(text, Normalizer.Form.NFD).replace(Regex("\\p{Mn}+"), "")
        return text
    }

    private val REPLACEMENTS = listOf(
        "ā" to "aa", "Ā" to "Aa", "ī" to "ee", "Ī" to "Ee", "ū" to "oo", "Ū" to "Oo",
        "ē" to "e", "Ē" to "E", "ō" to "o", "Ō" to "O",
        "r̥" to "ru", "ṛ" to "ru",
        "ḻ" to "zh", "Ḻ" to "Zh",
        "ṁ" to "m", "ṃ" to "m", "m̐" to "m",
        "ṟ" to "r", "ṉ" to "n", "ṅ" to "ng", "ñ" to "nj",
        "ś" to "sh", "Ś" to "Sh", "ṣ" to "sh", "Ṣ" to "Sh",
        "ḥ" to "h"
    )
}
