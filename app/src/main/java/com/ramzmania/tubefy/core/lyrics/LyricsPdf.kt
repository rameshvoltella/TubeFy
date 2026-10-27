package com.ramzmania.tubefy.core.lyrics

import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import androidx.core.content.FileProvider
import com.ramzmania.tubefy.core.analytics.ShareTracker
import java.io.File

/**
 * The song's lyrics as a one-or-more page A4 PDF to share (WhatsApp shows it with a preview): the
 * song's details on top, the lyrics, and the English-letters version after them when there is one.
 * Uses the phone's own fonts, so Malayalam, Tamil, Hindi… print as they show in the app.
 */
object LyricsPdf {

    private const val PAGE_WIDTH = 595 // A4 in points
    private const val PAGE_HEIGHT = 842
    private const val MARGIN = 48f
    private const val FOOTER = 40f
    private val ACCENT = Color.rgb(0xFF, 0x3B, 0x4E)
    private val TEXT = Color.rgb(0x1C, 0x19, 0x1A)
    private val MUTED = Color.rgb(0x77, 0x70, 0x72)

    /** Opens the share menu (WhatsApp and others) for a PDF made by [create]. */
    fun share(context: Context, info: SongInfo, file: File, videoId: String) {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val caption = buildString {
            append("🎵 ").append(info.details.title)
            if (info.details.singers.isNotBlank()) append(" – ").append(info.details.singers)
            append("\nLyrics shared from TubeFy")
        }
        val send = Intent(Intent.ACTION_SEND)
            .setType("application/pdf")
            .putExtra(Intent.EXTRA_STREAM, uri)
            .putExtra(Intent.EXTRA_TEXT, caption)
            .putExtra(Intent.EXTRA_SUBJECT, "${info.details.title} – Lyrics")
            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        send.clipData = ClipData.newRawUri(file.name, uri)
        context.startActivity(
            // Analytics logs a `share` (lyrics, and to which app) once an app is picked
            Intent.createChooser(send, "Share lyrics", ShareTracker.callback(context, "lyrics", videoId, info.details.title)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION)
        )
    }

    fun create(context: Context, info: SongInfo): File {
        val document = PdfDocument()
        val writer = PageWriter(document)
        val width = (PAGE_WIDTH - 2 * MARGIN).toInt()
        val details = info.details

        // Header: TubeFy label, the title, then the song's details
        writer.text("TUBEFY · LYRICS", paint(10f, ACCENT, bold = true, spacing = 0.15f), width, after = 10f)
        writer.text(details.title, paint(24f, TEXT, bold = true), width, after = 8f)
        listOf(
            "Singers" to details.singers,
            "Movie / Album" to details.movie,
            "Lyrics" to details.lyricist,
            "Music" to details.composer,
            "Year" to details.year
        ).filter { it.second.isNotBlank() }.forEach { (label, value) ->
            writer.text("$label:  $value", paint(11f, MUTED), width, after = 3f)
        }
        writer.divider(after = 18f)

        val original = info.lyrics
        val english = info.englishLetters
        if (original == null) {
            writer.text("No lyrics found for this song.", paint(13f, MUTED), width)
        } else {
            writer.lyrics(original, width)
            // The English-letters version too, for people who can't read the script
            if (english != null && english.lines.map { it.text } != original.lines.map { it.text }) {
                writer.space(14f)
                writer.text("IN ENGLISH LETTERS", paint(10f, ACCENT, bold = true, spacing = 0.15f), width, after = 12f)
                writer.lyrics(english, width)
            }
            if (original.source.isNotBlank()) {
                writer.space(16f)
                writer.text("Source: ${original.source}", paint(9f, MUTED), width)
            }
        }
        writer.finish()

        val dir = File(context.cacheDir, "shared_lyrics").apply {
            mkdirs()
            listFiles()?.forEach { it.delete() } // only the latest share is kept
        }
        val name = details.title.replace(Regex("[\\\\/:*?\"<>|\\n]"), " ").trim().ifBlank { "Lyrics" }.take(80)
        val file = File(dir, "$name - Lyrics.pdf")
        file.outputStream().use { document.writeTo(it) }
        document.close()
        return file
    }

    private fun paint(size: Float, color: Int, bold: Boolean = false, spacing: Float = 0f) = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = size
        this.color = color
        typeface = if (bold) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
        letterSpacing = spacing
    }

    /** Writes top to bottom, starting a new page when the next block doesn't fit. */
    private class PageWriter(private val document: PdfDocument) {
        private var pageNumber = 0
        private var page: PdfDocument.Page? = null
        private var y = 0f

        private val canvas: Canvas
            get() = (page ?: newPage()).canvas

        private fun newPage(): PdfDocument.Page {
            page?.let { finishPage(it) }
            pageNumber++
            val next = document.startPage(PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create())
            // Accent strip along the top
            next.canvas.drawRect(RectF(0f, 0f, PAGE_WIDTH.toFloat(), 6f), Paint().apply { color = ACCENT })
            page = next
            y = MARGIN
            return next
        }

        private fun finishPage(done: PdfDocument.Page) {
            val footer = TextPaint(Paint.ANTI_ALIAS_FLAG).apply { textSize = 9f; color = MUTED }
            done.canvas.drawText("Shared from TubeFy", MARGIN, PAGE_HEIGHT - FOOTER / 2, footer)
            val number = "$pageNumber"
            done.canvas.drawText(number, PAGE_WIDTH - MARGIN - footer.measureText(number), PAGE_HEIGHT - FOOTER / 2, footer)
            document.finishPage(done)
        }

        fun text(value: String, paint: TextPaint, width: Int, after: Float = 0f) {
            val layout = StaticLayout.Builder.obtain(value, 0, value.length, paint, width)
                .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                .setLineSpacing(0f, 1.15f)
                .build()
            if (page == null || y + layout.height > PAGE_HEIGHT - MARGIN - FOOTER) newPage()
            canvas.save()
            canvas.translate(MARGIN, y)
            layout.draw(canvas)
            canvas.restore()
            y += layout.height + after
        }

        /** Lyrics lines; blank lines (between verses) become a gap. */
        fun lyrics(lyrics: Lyrics, width: Int) {
            val line = TextPaint(Paint.ANTI_ALIAS_FLAG).apply { textSize = 13f; color = TEXT }
            lyrics.lines.forEach {
                if (it.text.isBlank()) space(10f) else text(it.text.trim(), line, width, after = 4f)
            }
        }

        fun space(height: Float) {
            y += height
        }

        fun divider(after: Float) {
            if (page == null) newPage()
            y += 6f
            canvas.drawRect(RectF(MARGIN, y, MARGIN + 40f, y + 3f), Paint().apply { color = ACCENT })
            y += 3f + after
        }

        fun finish() {
            (page ?: newPage()).let { finishPage(it) }
            page = null
        }
    }
}
