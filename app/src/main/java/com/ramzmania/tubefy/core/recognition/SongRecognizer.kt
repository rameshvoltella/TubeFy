package com.ramzmania.tubefy.core.recognition

import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.Base64
import com.squareup.moshi.Moshi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.TimeZone
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.coroutineContext

/** A song recognised from the microphone. */
data class RecognizedSong(
    val title: String,
    val artist: String,
    /** Album, which for film songs is usually the movie. */
    val album: String,
    val year: String,
    val coverUrl: String?
)

sealed interface RecognitionResult {
    data class Found(val song: RecognizedSong) : RecognitionResult
    data object NotFound : RecognitionResult
    data class Failed(val message: String) : RecognitionResult
}

/**
 * "Identify song": listens through the microphone and asks Shazam what's playing, using only an
 * audio fingerprint made on the phone ([ShazamSignature]). Uses Shazam's recognition service the way
 * its app does (unofficial: it may stop working if Shazam changes it). Tries after a few seconds and
 * again with more audio, stopping at the first match.
 */
@Singleton
class SongRecognizer @Inject constructor(private val http: OkHttpClient) {

    private val json = Moshi.Builder().build().adapter(Any::class.java)

    /** Listens (up to [MAX_SECONDS]) and identifies the song; [onListening] gets the seconds heard so far. */
    @SuppressLint("MissingPermission") // the caller asks for RECORD_AUDIO first
    suspend fun listen(onListening: (seconds: Int) -> Unit): RecognitionResult = withContext(Dispatchers.IO) {
        val minBuffer = AudioRecord.getMinBufferSize(SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT)
        val recorder = runCatching {
            AudioRecord(MediaRecorder.AudioSource.MIC, SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, maxOf(minBuffer, SAMPLE_RATE))
        }.getOrNull()
        if (recorder == null || recorder.state != AudioRecord.STATE_INITIALIZED) {
            recorder?.release()
            return@withContext RecognitionResult.Failed("Couldn't use the microphone")
        }
        val samples = ShortArray(SAMPLE_RATE * MAX_SECONDS)
        var recorded = 0
        var lastFailure: RecognitionResult.Failed? = null
        try {
            recorder.startRecording()
            val chunk = ShortArray(SAMPLE_RATE / 4)
            var nextAttempt = 0
            while (recorded < samples.size) {
                coroutineContext.ensureActive()
                val read = recorder.read(chunk, 0, minOf(chunk.size, samples.size - recorded))
                if (read <= 0) break
                System.arraycopy(chunk, 0, samples, recorded, read)
                recorded += read
                onListening(recorded / SAMPLE_RATE)
                // Ask after 4, 8 and 12 seconds: clear audio usually matches on the first try
                if (nextAttempt < ATTEMPT_SECONDS.size && recorded >= ATTEMPT_SECONDS[nextAttempt] * SAMPLE_RATE) {
                    nextAttempt++
                    when (val result = recognize(samples.copyOf(recorded))) {
                        is RecognitionResult.Found -> return@withContext result
                        is RecognitionResult.Failed -> lastFailure = result
                        RecognitionResult.NotFound -> Unit
                    }
                }
            }
        } finally {
            runCatching { recorder.stop() }
            recorder.release()
        }
        lastFailure ?: RecognitionResult.NotFound
    }

    /** One recognition request for [samples] (16 kHz mono). */
    private fun recognize(samples: ShortArray): RecognitionResult {
        val signature = ShazamSignature.from(samples)
        val uri = ShazamSignature.DATA_URI_PREFIX + Base64.encodeToString(signature.encode(), Base64.NO_WRAP)
        val now = System.currentTimeMillis()
        val body = json.toJson(
            mapOf(
                "geolocation" to mapOf("altitude" to 300, "latitude" to 45, "longitude" to 2),
                "signature" to mapOf("samplems" to samples.size * 1000L / SAMPLE_RATE, "timestamp" to now.toInt(), "uri" to uri),
                "timestamp" to now.toInt(),
                "timezone" to TimeZone.getDefault().id
            )
        )
        val url = "https://amp.shazam.com/discovery/v5/en/US/android/-/tag/${UUID.randomUUID().toString().uppercase()}/${UUID.randomUUID()}" +
            "?sync=true&webv3=true&sampling=true&connected=&shazamapiversion=v3&sharehub=true&video=v3"
        val request = Request.Builder()
            .url(url)
            .header("Content-Language", "en_US")
            // The phone's own identity (no disguise)
            .header("User-Agent", System.getProperty("http.agent") ?: "TubeFy")
            .post(body.toRequestBody("application/json".toMediaType()))
            .build()
        return try {
            http.newCall(request).execute().use { response ->
                when {
                    response.code == 429 -> RecognitionResult.Failed("Too many tries right now. Please wait a minute.")
                    !response.isSuccessful -> RecognitionResult.Failed("Song recognition isn't available right now")
                    else -> parse(response.body?.string().orEmpty())
                }
            }
        } catch (e: java.io.IOException) {
            RecognitionResult.Failed("No internet connection")
        }
    }

    internal fun parse(text: String): RecognitionResult {
        val root = json.fromJson(text) as? Map<*, *> ?: return RecognitionResult.NotFound
        val matches = root["matches"] as? List<*>
        val track = root["track"] as? Map<*, *>
        if (matches.isNullOrEmpty() || track == null) return RecognitionResult.NotFound
        val metadata = (track["sections"] as? List<*>).orEmpty().filterIsInstance<Map<*, *>>()
            .flatMap { (it["metadata"] as? List<*>).orEmpty().filterIsInstance<Map<*, *>>() }
        fun meta(name: String) = metadata.firstOrNull { (it["title"] as? String).equals(name, ignoreCase = true) }?.get("text") as? String ?: ""
        val images = track["images"] as? Map<*, *>
        return RecognitionResult.Found(
            RecognizedSong(
                title = track["title"] as? String ?: "",
                artist = track["subtitle"] as? String ?: "",
                album = meta("Album"),
                year = meta("Released"),
                coverUrl = (images?.get("coverarthq") ?: images?.get("coverart")) as? String
            )
        )
    }

    private companion object {
        const val SAMPLE_RATE = ShazamSignature.SAMPLE_RATE
        const val MAX_SECONDS = 12
        val ATTEMPT_SECONDS = intArrayOf(4, 8, 12)
    }
}
