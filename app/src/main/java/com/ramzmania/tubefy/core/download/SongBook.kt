package com.ramzmania.tubefy.core.download

import android.content.Context
import android.net.Uri
import android.provider.DocumentsContract
import com.squareup.moshi.Moshi
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** One song saved to phone storage, as shown in the Song Book. Blank fields weren't found. */
data class SongBookEntry(
    val videoId: String,
    val title: String,
    val movie: String = "",
    val year: String = "",
    val language: String = "",
    val singers: String = "",
    val composer: String = "",
    val lyricist: String = "",
    val lyrics: String = "",
    /** With "Both": the lyrics again in English letters, shown after [lyrics]. */
    val lyricsEnglish: String = "",
    val lyricsSource: String = "",
    val fileName: String = "",
    /** The folder (tree URI) the song was saved to; each folder has its own book. */
    val folder: String = "",
    val savedAt: Long = 0L
)

/**
 * "TubeFy Song Book.html": a readable page, kept next to the songs saved to phone storage, with every
 * song's details and lyrics. TubeFy keeps its own list of entries and rewrites the whole page after
 * each save (never appends), so it's always complete, has no duplicates and its index stays correct.
 */
object SongBook {

    const val FILE_NAME = "TubeFy Song Book.html"
    private const val STORE = "songbook.json"

    private val adapter = Moshi.Builder().build().adapter(Any::class.java)

    /** Adds (or updates) [entry] and rewrites the book in its folder. Blocking: call off the main thread. */
    @Synchronized
    fun record(context: Context, entry: SongBookEntry) {
        val entries = add(load(context), entry)
        save(context, entries)
        write(context, Uri.parse(entry.folder), entries.filter { it.folder == entry.folder })
    }

    /** [entries] with [entry] first; an earlier save of the same song is replaced, not duplicated. */
    internal fun add(entries: List<SongBookEntry>, entry: SongBookEntry): List<SongBookEntry> =
        listOf(entry) + entries.filterNot { it.videoId == entry.videoId && it.folder == entry.folder }

    // --- Writing the page into the folder ---

    private fun write(context: Context, folder: Uri, entries: List<SongBookEntry>) {
        val resolver = context.contentResolver
        val treeId = DocumentsContract.getTreeDocumentId(folder)
        // What's in the folder now: the existing book (to overwrite) and which songs are still there
        var bookUri: Uri? = null
        val names = mutableSetOf<String>()
        resolver.query(
            DocumentsContract.buildChildDocumentsUriUsingTree(folder, treeId),
            arrayOf(DocumentsContract.Document.COLUMN_DOCUMENT_ID, DocumentsContract.Document.COLUMN_DISPLAY_NAME),
            null, null, null
        )?.use { cursor ->
            while (cursor.moveToNext()) {
                val name = cursor.getString(1).orEmpty()
                names += name
                if (name == FILE_NAME) bookUri = DocumentsContract.buildDocumentUriUsingTree(folder, cursor.getString(0))
            }
        }
        val html = render(entries, presentFiles = names, now = System.currentTimeMillis())
        val target = bookUri ?: DocumentsContract.createDocument(
            resolver,
            DocumentsContract.buildDocumentUriUsingTree(folder, treeId),
            "text/html",
            FILE_NAME
        ) ?: return
        // "wt": replace the whole file
        resolver.openOutputStream(target, "wt")?.use { it.write(html.toByteArray(Charsets.UTF_8)) }
    }

    // --- TubeFy's own list ---

    private fun storeFile(context: Context) = File(context.filesDir, STORE)

    private fun load(context: Context): List<SongBookEntry> = runCatching {
        (adapter.fromJson(storeFile(context).readText()) as? List<*>).orEmpty().filterIsInstance<Map<*, *>>().map { map ->
            fun text(key: String) = map[key] as? String ?: ""
            SongBookEntry(
                videoId = text("videoId"), title = text("title"), movie = text("movie"), year = text("year"),
                language = text("language"), singers = text("singers"), composer = text("composer"),
                lyricist = text("lyricist"), lyrics = text("lyrics"), lyricsEnglish = text("lyricsEnglish"), lyricsSource = text("lyricsSource"),
                fileName = text("fileName"), folder = text("folder"), savedAt = (map["savedAt"] as? Number)?.toLong() ?: 0L
            )
        }
    }.getOrDefault(emptyList())

    private fun save(context: Context, entries: List<SongBookEntry>) {
        val maps = entries.map { e ->
            mapOf(
                "videoId" to e.videoId, "title" to e.title, "movie" to e.movie, "year" to e.year,
                "language" to e.language, "singers" to e.singers, "composer" to e.composer,
                "lyricist" to e.lyricist, "lyrics" to e.lyrics, "lyricsEnglish" to e.lyricsEnglish, "lyricsSource" to e.lyricsSource,
                "fileName" to e.fileName, "folder" to e.folder, "savedAt" to e.savedAt
            )
        }
        val file = storeFile(context)
        val temp = File(file.parentFile, "$STORE.tmp")
        temp.writeText(adapter.toJson(maps))
        temp.renameTo(file)
    }

    // --- The page ---

    /** The language from the lyrics' script (Malayalam, Tamil, Hindi, …); blank when unsure. */
    fun languageOf(lyrics: String): String {
        val counts = mutableMapOf<String, Int>()
        lyrics.forEach { ch ->
            val language = when (ch.code) {
                in 0x0D00..0x0D7F -> "Malayalam"
                in 0x0B80..0x0BFF -> "Tamil"
                in 0x0C00..0x0C7F -> "Telugu"
                in 0x0C80..0x0CFF -> "Kannada"
                in 0x0900..0x097F -> "Hindi"
                in 0x0980..0x09FF -> "Bengali"
                in 0x0A00..0x0A7F -> "Punjabi"
                else -> null
            }
            if (language != null) counts[language] = (counts[language] ?: 0) + 1
        }
        return counts.maxByOrNull { it.value }?.takeIf { it.value >= 10 }?.key.orEmpty()
    }

    /** The whole HTML page for [entries] (newest first). Songs whose file is gone are marked. */
    internal fun render(entries: List<SongBookEntry>, presentFiles: Set<String>?, now: Long): String {
        val dateFormat = SimpleDateFormat("d MMM yyyy", Locale.ENGLISH)
        fun esc(text: String) = text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;")
        fun anchor(entry: SongBookEntry) = "song-" + entry.videoId.replace(Regex("[^A-Za-z0-9_-]"), "")

        val index = entries.joinToString("\n") { e ->
            val sub = listOf(e.movie, e.singers).filter { it.isNotBlank() }.joinToString(" · ")
            """<li><a href="#${anchor(e)}"><span class="t">${esc(e.title)}</span>${if (sub.isNotEmpty()) "<span class=\"s\">${esc(sub)}</span>" else ""}</a></li>"""
        }

        val songs = entries.joinToString("\n") { e ->
            val rows = listOf(
                "Movie" to e.movie, "Year" to e.year, "Language" to e.language,
                "Singers" to e.singers, "Music" to e.composer, "Lyrics" to e.lyricist
            ).filter { it.second.isNotBlank() }
                .joinToString("") { (label, value) -> "<dt>$label</dt><dd>${esc(value)}</dd>" }
            val missing = presentFiles != null && e.fileName.isNotBlank() && e.fileName !in presentFiles
            val lyrics = if (e.lyrics.isBlank()) "<p class=\"none\">Lyrics not available</p>"
            else if (e.lyricsEnglish.isNotBlank()) {
                // "Both": the original script, then English letters
                "<h3>${esc(e.language.ifBlank { "Original" })}</h3><div class=\"lyrics\">${esc(e.lyrics.trim())}</div>" +
                    "<h3>English letters</h3><div class=\"lyrics\">${esc(e.lyricsEnglish.trim())}</div>"
            } else "<div class=\"lyrics\">${esc(e.lyrics.trim())}</div>"
            val lyricsBlock = lyrics +
                (if (e.lyricsSource.isNotBlank()) "<p class=\"src\">Lyrics by ${esc(e.lyricsSource)}</p>" else "")
            """
<section class="song" id="${anchor(e)}">
  <h2>${esc(e.title)}${if (missing) " <span class=\"gone\">file removed</span>" else ""}</h2>
  ${if (rows.isNotEmpty()) "<dl>$rows</dl>" else ""}
  $lyricsBlock
  <p class="foot">Saved ${dateFormat.format(Date(e.savedAt))}${if (e.fileName.isNotBlank()) " · ${esc(e.fileName)}" else ""} · <a href="https://www.youtube.com/watch?v=${esc(e.videoId)}">YouTube</a> · <a href="#top">↑ Top</a></p>
</section>"""
        }

        return """<!doctype html>
<html lang="en">
<head>
<meta charset="utf-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>TubeFy Song Book</title>
<style>
:root { --bg:#fbf8f8; --card:#ffffff; --text:#1d1a1b; --muted:#6f6668; --accent:#d62839; --line:#ece4e5; }
@media (prefers-color-scheme: dark) { :root { --bg:#0e0c0d; --card:#1a1617; --text:#f3eeee; --muted:#a59c9e; --accent:#ff4d5e; --line:#2a2425; } }
* { box-sizing:border-box; }
body { margin:0; background:var(--bg); color:var(--text); font:16px/1.5 system-ui,-apple-system,"Segoe UI",Roboto,"Noto Sans","Noto Sans Malayalam","Noto Sans Tamil","Noto Sans Devanagari",sans-serif; }
main { max-width:760px; margin:0 auto; padding:20px 16px 64px; }
header h1 { margin:0; font-size:28px; } header h1 b { color:var(--accent); }
header p { margin:4px 0 16px; color:var(--muted); font-size:14px; }
#q { width:100%; padding:12px 16px; border-radius:24px; border:1px solid var(--line); background:var(--card); color:var(--text); font-size:16px; }
nav ol { list-style:none; margin:16px 0 28px; padding:0; border-radius:16px; overflow:hidden; border:1px solid var(--line); }
nav li a { display:flex; flex-direction:column; padding:10px 16px; background:var(--card); color:var(--text); text-decoration:none; border-bottom:1px solid var(--line); }
nav li:last-child a { border-bottom:0; }
nav .t { font-weight:600; } nav .s { color:var(--muted); font-size:13px; }
.song { background:var(--card); border:1px solid var(--line); border-radius:20px; padding:18px 18px 12px; margin-bottom:18px; }
.song h2 { margin:0 0 10px; font-size:22px; }
dl { display:grid; grid-template-columns:auto 1fr; gap:4px 14px; margin:0 0 14px; font-size:15px; }
dt { color:var(--muted); } dd { margin:0; font-weight:500; }
.lyrics { white-space:pre-wrap; font-size:17px; line-height:1.7; border-top:1px solid var(--line); padding-top:12px; }
.none { color:var(--muted); font-style:italic; }
.song h3 { margin:16px 0 0; font-size:13px; text-transform:uppercase; letter-spacing:.06em; color:var(--accent); }
.src, .foot { color:var(--muted); font-size:13px; } .foot a { color:var(--accent); }
.gone { font-size:12px; font-weight:600; color:var(--accent); border:1px solid var(--accent); border-radius:10px; padding:1px 8px; vertical-align:middle; }
</style>
</head>
<body>
<main id="top">
<header>
  <h1>Tube<b>Fy</b> Song Book</h1>
  <p>${entries.size} ${if (entries.size == 1) "song" else "songs"} · updated ${dateFormat.format(Date(now))}</p>
  <input id="q" type="search" placeholder="Search songs, movies, singers, lyrics…" oninput="filter(this.value)">
</header>
<nav><ol id="index">
$index
</ol></nav>
$songs
</main>
<script>
function filter(q) {
  q = q.trim().toLowerCase();
  document.querySelectorAll('.song').forEach(function (s) {
    var show = !q || s.textContent.toLowerCase().indexOf(q) >= 0;
    s.style.display = show ? '' : 'none';
    var link = document.querySelector('#index a[href="#' + s.id + '"]');
    if (link) link.parentElement.style.display = show ? '' : 'none';
  });
}
</script>
</body>
</html>
"""
    }
}
