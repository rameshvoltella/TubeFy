package com.ramzmania.tubefy.probe

import com.ramzmania.tubefy.core.download.SongBook
import com.ramzmania.tubefy.core.download.SongBookEntry
import org.junit.Assume.assumeTrue
import org.junit.Test
import java.io.File

/** Manual: writes a sample Song Book to $TUBEFY_SONGBOOK_SAMPLE to preview the page. */
class SongBookSample {
    @Test
    fun sample() {
        val path = System.getenv("TUBEFY_SONGBOOK_SAMPLE") ?: return assumeTrue(false)
        val now = System.currentTimeMillis()
        val entries = listOf(
            SongBookEntry("-GyOKmoLbng", "Malare", "Premam", "2015", "Malayalam", "Vijay Yesudas", "Rajesh Murugesan", "Shabareesh Varma",
                "തെളിമാനം മഴവില്ലിൻ നിറമണിയും നേരം\nനിറമാർന്നൊരു കനവെന്നിൽ തെളിയുന്ന പോലെ\n\nമലരേ… നിന്നെ കാണാതിരുന്നാൽ", "LRCLIB", "Malare - Vijay Yesudas.m4a", "f", now),
            SongBookEntry("NJAv_7lHUIU", "Kesariya", "Brahmastra", "2022", "Hindi", "Arijit Singh", "Pritam", "Amitabh Bhattacharya",
                "मुझको इतना बताए कोई\nकैसे तुझसे दिल ना लगाए कोई", "LyricFind", "Kesariya - Arijit Singh.m4a", "f", now - 86_400_000),
            SongBookEntry("old", "An old song", fileName = "An old song.m4a", folder = "f", savedAt = now - 9 * 86_400_000)
        )
        File(path).writeText(SongBook.render(entries, presentFiles = setOf("Malare - Vijay Yesudas.m4a", "Kesariya - Arijit Singh.m4a"), now = now))
    }
}
