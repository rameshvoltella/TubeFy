package com.ramzmania.tubefy.core.recognition

import org.junit.Assert.assertEquals
import org.junit.Test

/** Rows taken from a real Shazam export ("SyncedSongs.csv"), including its awkward quoting. */
class ShazamCsvTest {

    private val sample = listOf(
        "\"artist\",\"title\",\"status\",\"date\",\"longitude\",\"latitude\"",
        "\"Job Kurian & Neha Nair\",\"Melle Kollum (from \"\"22 Female Kottayam\"\")\",\"N/A\",\"2026-08-14T01:21:13.327Z\",\"N/A\",\"N/A\"",
        "\"K. S. Harisankar\",\"En Nenjoram (From \"\"Santhosham\"\")\"\"\",\"N/A\",\"2025-02-02T06:38:55.469Z\",\"N/A\",\"N/A\"",
        "\"EMIN & JONY\",\"Камин\",\"N/A\",\"2025-05-13T04:01:24Z\",\"N/A\",\"N/A\"",
        "\"Adam Gill\",\"VILLAGE FUNK\",\"N/A\",\"2025-12-24T04:09:16.483Z\",\"N/A\",\"N/A\"",
        "\"Adam Gill\",\"VILLAGE FUNK\",\"N/A\",\"2025-12-24T04:09:12.003Z\",\"N/A\",\"N/A\"",
        "\"Astrid S\",\"Hurts So Good\",\"N/A\",\"2022-12-12T10:05:19.012Z\",\"76.47\",\"9.74\""
    ).joinToString("\n")

    @Test
    fun parsesQuotesAndUnicode() {
        val rows = ShazamImport.parseCsv(sample)
        assertEquals(7, rows.size)
        assertEquals(listOf("artist", "title", "status", "date", "longitude", "latitude"), rows[0])
        assertEquals("Melle Kollum (from \"22 Female Kottayam\")", rows[1][1])
        assertEquals("En Nenjoram (From \"Santhosham\")\"", rows[2][1])
        assertEquals("Камин", rows[3][1])
        assertEquals("2025-05-13T04:01:24Z", rows[3][3])
        assertEquals(6, rows[6].size)
    }

    @Test
    fun duplicatesShareAKey() {
        val rows = ShazamImport.parseCsv(sample).drop(1)
        val unique = rows.map { ImportedSong.keyOf(it[1], it[0]) }.toSet()
        assertEquals(5, unique.size) // VILLAGE FUNK appears twice
    }
}
