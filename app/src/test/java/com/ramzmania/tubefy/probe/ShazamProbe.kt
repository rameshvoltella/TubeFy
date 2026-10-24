package com.ramzmania.tubefy.probe

import com.ramzmania.tubefy.core.download.downloadInChunks
import com.ramzmania.tubefy.core.extractors.newpipeextractor.PipeDownloader
import com.ramzmania.tubefy.core.recognition.ShazamSignature
import com.ramzmania.tubefy.core.youtube.YouTubeStreams
import kotlinx.coroutines.runBlocking
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.junit.Assume.assumeTrue
import org.junit.Test
import org.schabi.newpipe.extractor.MediaFormat
import org.schabi.newpipe.extractor.NewPipe
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.Base64
import java.util.UUID

/** Manual: real-song check of the Shazam fingerprint port; runs only with env TUBEFY_SHAZAM_DIR set. */
class ShazamProbe {
    private val dir get() = System.getenv("TUBEFY_SHAZAM_DIR")

    @Test
    fun download() = runBlocking {
        assumeTrue(dir != null && System.getenv("TUBEFY_SHAZAM_STEP") == "download")
        NewPipe.init(PipeDownloader(OkHttpClient()))
        val audio = YouTubeStreams.info(System.getenv("TUBEFY_SHAZAM_VIDEO") ?: "-GyOKmoLbng").audioStreams
            .filter { it.format == MediaFormat.M4A }.maxBy { it.averageBitrate }
        File(dir, "song.m4a").outputStream().use { downloadInChunks(audio.content, it) {} }
        println("DOWNLOADED ${File(dir, "song.m4a").length()} bytes")
    }

    @Test
    fun recognize() {
        assumeTrue(dir != null && System.getenv("TUBEFY_SHAZAM_STEP") == "recognize")
        val samples = readWav(File(dir, "song.wav"))
        // 12 s from the middle, like SongRec
        val middle = samples.size / 2
        val clip = samples.copyOfRange(middle - 6 * 16000, middle + 6 * 16000)
        val signature = ShazamSignature.from(clip)
        println("PEAKS per band: ${signature.bands.map { it.size }}")
        val uri = ShazamSignature.DATA_URI_PREFIX + Base64.getEncoder().encodeToString(signature.encode())
        val now = System.currentTimeMillis()
        val body = """{"geolocation":{"altitude":300,"latitude":45,"longitude":2},"signature":{"samplems":${clip.size * 1000L / 16000},"timestamp":${now.toInt()},"uri":"$uri"},"timestamp":${now.toInt()},"timezone":"Asia/Kolkata"}"""
        val url = "https://amp.shazam.com/discovery/v5/en/US/android/-/tag/${UUID.randomUUID().toString().uppercase()}/${UUID.randomUUID()}" +
            "?sync=true&webv3=true&sampling=true&connected=&shazamapiversion=v3&sharehub=true&video=v3"
        val response = OkHttpClient().newCall(
            Request.Builder().url(url)
                .header("Content-Language", "en_US")
                .header("User-Agent", "Dalvik/2.1.0 (Linux; U; Android 15; SM-S911B Build/AP3A)")
                .post(body.toRequestBody("application/json".toMediaType())).build()
        ).execute()
        val text = response.body!!.string()
        println("STATUS ${response.code}")
        println("TITLE " + Regex("\"title\":\"([^\"]+)\"").find(text)?.groupValues?.get(1) + " | SUBTITLE " + Regex("\"subtitle\":\"([^\"]+)\"").find(text)?.groupValues?.get(1))
        println("MATCHES " + Regex("\"matches\":\\[(.{0,80})").find(text)?.groupValues?.get(1))
    }

    /** 16-bit PCM samples from a WAV file (finds the "data" chunk). */
    private fun readWav(file: File): ShortArray {
        val bytes = file.readBytes()
        var pos = 12
        while (pos + 8 <= bytes.size) {
            val id = String(bytes, pos, 4)
            val size = ByteBuffer.wrap(bytes, pos + 4, 4).order(ByteOrder.LITTLE_ENDIAN).int
            if (id == "data") {
                val data = ByteBuffer.wrap(bytes, pos + 8, minOf(size, bytes.size - pos - 8)).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer()
                return ShortArray(data.remaining()).also { data.get(it) }
            }
            pos += 8 + size + (size and 1)
        }
        error("No data chunk")
    }
}
